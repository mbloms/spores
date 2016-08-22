/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2002-2013, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

package scala.spores

// TODO(jvican): Can we just use blackbox?
import scala.reflect.macros.whitebox

private[spores] class MacroImpl[C <: whitebox.Context](val c: C) {
  import c.universe._

  /* Don't change this name since it's used
   * to check if a class is indeed a spore */
  val anonSporeName = TypeName("anonspore")

  private val sporesPath = q"scala.spores"

  def conforms(funTree: c.Tree): (List[Symbol], Type, Tree, List[ValDef]) = {
    val analysis = new SporeAnalysis[c.type](c)
    val (sporeEnv, sporeFunDef) = analysis.stripSporeStructure(funTree)
    sporeEnv foreach (sym => debug(s"Captured symbol: $sym"))

    val (funOpt, vparams, sporeBody) = analysis.readSporeFunDef(sporeFunDef)
    val functionSymbol = funOpt.map(_.symbol)
    val captured = analysis.collectCaptured(sporeBody)
    val declared = analysis.collectDeclared(sporeBody)
    val symbolsEnv = sporeEnv.map(_.symbol)
    val checker = new SporeChecker[c.type](c)(symbolsEnv,
                                              functionSymbol,
                                              captured,
                                              declared)

    debug(s"Checking $sporeBody...")
    checker.checkReferencesInBody(sporeBody)
    (vparams.map(_.symbol), sporeBody.tpe, sporeBody, sporeEnv)
  }

  def check2(funTree: c.Tree, tpes: List[c.Type]): c.Tree = {
    val (paramSyms, retTpe, funBody, valDefEnv) = conforms(funTree)
    val validEnv = valDefEnv.map(_.symbol)
    val generator = new SporeGenerator[c.type](c)

    val sporeClassName = c.freshName(anonSporeName)

    if (validEnv.isEmpty) {
      val applyDefDef = generator.createNewDefDef(paramSyms, funBody, retTpe)

      if (paramSyms.size == 2) {
        q"""
          class $sporeClassName extends scala.spores.Spore2[${tpes(1)}, ${tpes(
          2)}, ${tpes(0)}] {
            self =>
            type Captured = scala.Nothing
            this._className = this.getClass.getName
            $applyDefDef
          }
          new $sporeClassName
        """
      } else if (paramSyms.size == 3) {
        q"""
          class $sporeClassName extends scala.spores.Spore3[${tpes(1)}, ${tpes(
          2)}, ${tpes(3)}, ${tpes(0)}] {
            self =>
            type Captured = scala.Nothing
            this._className = this.getClass.getName
            $applyDefDef
          }
          new $sporeClassName
        """
      } else ???
    } else { // validEnv.size > 1 (TODO: size == 1)
      // replace references to paramSyms with references to applyParamSymbols
      // and references to captured variables to new fields
      val capturedTypes = validEnv.map(_.typeSignature).toArray
      debug(s"Captured types: ${capturedTypes.mkString(",")}")

      val newRefs = generator.generateCapturedReferences(validEnv)
      val applyDefDef = generator.createNewDefDef(paramSyms,
                                                  funBody,
                                                  retTpe,
                                                  environment = validEnv,
                                                  capturedRefs = newRefs)
      val valDefRhss = valDefEnv.map(_.rhs).toArray
      val constructorParams = List(List(generator.toTuple(valDefRhss)))
      val capturedType = generator.createCapturedType(capturedTypes)

      if (paramSyms.size == 2) {
        q"""
            class $sporeClassName(val captured: $capturedType) extends scala.spores.Spore2WithEnv[${tpes(
          1)}, ${tpes(2)}, ${tpes(0)}] {
              self =>
              type Captured = $capturedType
              this._className = this.getClass.getName
              $applyDefDef
            }
            new $sporeClassName(...$constructorParams)
          """
      } else if (paramSyms.size == 3) {
        q"""
            class $sporeClassName(val captured: $capturedType) extends scala.spores.Spore3WithEnv[${tpes(
          1)}, ${tpes(2)}, ${tpes(3)}, ${tpes(0)}] {
              self =>
              type Captured = $capturedType
              this._className = this.getClass.getName
              $applyDefDef
            }
            new $sporeClassName(...$constructorParams)
          """
      } else ???
    }
  }

  /** Ensure that a parameterless function is a spore. Expecting:
    * {{{
    * spore {
    *   val x = outer
    *   `delayed { ... }` or `() => { ... }`
    * }
    * }}}
    */
  def checkNullary(funTree: c.Tree, rtpe: c.Type): c.Tree = {
    debug(s"Received following nullary spore:\n$funTree")

    val (paramSyms, retTpe, funBody, valDefEnv) = conforms(funTree)
    val validEnv = valDefEnv.map(_.symbol)
    val generator = new SporeGenerator[c.type](c)

    val sporeClassName = c.freshName(anonSporeName)

    if (validEnv.isEmpty) {
      val applyDefDef = generator.createNewDefDef(paramSyms, funBody, retTpe)
      q"""
        class $sporeClassName extends scala.spores.NullarySpore[$rtpe] {
          type Captured = Nothing
          this._className = this.getClass.getName
          $applyDefDef
        }
        new $sporeClassName
      """
    } else {
      val capturedTypes = validEnv.map(_.typeSignature).toArray
      debug(s"Captured types: ${capturedTypes.mkString(",")}")

      val newRefs = generator.generateCapturedReferences(validEnv)
      val applyDefDef = generator.createNewDefDef(paramSyms,
                                                  funBody,
                                                  retTpe,
                                                  environment = validEnv,
                                                  capturedRefs = newRefs)

      val valDefRhss = valDefEnv.map(_.rhs).toArray
      val constructorParams = List(List(generator.toTuple(valDefRhss)))
      val superclassName = TypeName("NullarySporeWithEnv")
      val capturedType = generator.createCapturedType(capturedTypes.toArray)

      q"""
        class $sporeClassName(val captured: $capturedType) extends $superclassName[$rtpe] {
          self =>
          type Captured = $capturedType
          this._className = this.getClass.getName
          $applyDefDef
        }
        new $sporeClassName(...$constructorParams)
      """
    }
  }

  /**
     spore {
       val x = outer
       (y: T) => { ... }
     }
    */
  def check(funTree: c.Tree, ttpe: c.Type, rtpe: c.Type): c.Tree = {
    debug(s"Received following spore:\n$funTree")

    val (paramSyms, retTpe, funBody, valDefEnv) = conforms(funTree)
    val validEnv = valDefEnv.map(_.symbol)
    val generator = new SporeGenerator[c.type](c)
    val paramSym = paramSyms.head

    if (paramSym != null) {
      val sporeName = c.freshName(anonSporeName)
      val targs = List(ttpe, rtpe)
      if (validEnv.isEmpty) {
        val sporeType = tq"Spore[..$targs]"
        debug(s"Generated spore type is: $sporeType")
        val sporeBody = generator.createNewDefDef(paramSyms, funBody, retTpe)
        generator.generateSpore(sporeName, sporeType, Nil, sporeBody)
      } else {
        val sporeType = tq"SporeWithEnv[..$targs]"
        debug(s"Generated spore type is: $sporeType")
        val capturedTypes = validEnv.map(_.typeSignature)
        debug(s"Captured types: ${capturedTypes.mkString(",")}")
        val newRefs = generator.generateCapturedReferences(validEnv)
        val sporeBody = generator.createNewDefDef(paramSyms,
                                                  funBody,
                                                  retTpe,
                                                  environment = validEnv,
                                                  capturedRefs = newRefs)
        val valDefRhss = valDefEnv.map(_.rhs).toArray
        val constructorParams = List(generator.toTuple(valDefRhss))
        val capturedType = generator.createCapturedType(capturedTypes.toArray)
        generator.generateSpore(sporeName,
                                sporeType,
                                List(capturedType),
                                sporeBody,
                                constructorParams)
      }
    } else {
      ???
    }
  }

}
