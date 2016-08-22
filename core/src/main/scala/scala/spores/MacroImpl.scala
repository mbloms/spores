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
      debug(s"capturedTypes: ${capturedTypes.mkString(",")}")

      val newRefs = generator.generateCapturedReferences(validEnv)
      val applyDefDef = generator.createNewDefDef(paramSyms,
                                                  funBody,
                                                  retTpe,
                                                  environment = validEnv,
                                                  capturedRefs = newRefs)
      val valDefRhss = valDefEnv.map(_.rhs).toArray
      val constructorParams = List(List(generator.toTuple(valDefRhss)))
      val capturedTypeDef = generator.createCapturedType(capturedTypes)
      val q"type $_ = $capturedType" = capturedTypeDef

      if (paramSyms.size == 2) {
        q"""
            class $sporeClassName(val captured: $capturedType) extends scala.spores.Spore2WithEnv[${tpes(
          1)}, ${tpes(2)}, ${tpes(0)}] {
              self =>
              $capturedTypeDef
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
              $capturedTypeDef
              this._className = this.getClass.getName
              $applyDefDef
            }
            new $sporeClassName(...$constructorParams)
          """
      } else ???
    }
  }

  /**
     spore {
       val x = outer
       delayed { ... }
     }
    */
  def checkNullary(funTree: c.Tree, rtpe: c.Type): c.Tree = {
    debug(s"SPORES: enter checkNullary")

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
      debug(s"capturedTypes: ${capturedTypes.mkString(",")}")

      val newRefs = generator.generateCapturedReferences(validEnv)
      val applyDefDef = generator.createNewDefDef(paramSyms,
                                                  funBody,
                                                  retTpe,
                                                  environment = validEnv,
                                                  capturedRefs = newRefs)

      val valDefRhss = valDefEnv.map(_.rhs).toArray
      val constructorParams = List(List(generator.toTuple(valDefRhss)))
      val superclassName = TypeName("NullarySporeWithEnv")

      val capturedTypeDef = generator.createCapturedType(capturedTypes.toArray)
      val q"type $_ = $capturedType" = capturedTypeDef

      q"""
        class $sporeClassName(val captured: $capturedType) extends $superclassName[$rtpe] {
          self =>
          $capturedTypeDef
          this._className = this.getClass.getName
          $applyDefDef
        }
        new $sporeClassName(...$constructorParams)
      """
    }
  }

  def toTuple(lst: List[c.Tree]): c.Tree = {
    if (lst.size == 1) lst(0)
    else if (lst.size == 2) q"(${lst(0)}, ${lst(1)})"
    else if (lst.size == 3) q"(${lst(0)}, ${lst(1)}, ${lst(2)})"
    else if (lst.size == 4) q"(${lst(0)}, ${lst(1)}, ${lst(2)}, ${lst(3)})"
    else if (lst.size == 5)
      q"(${lst(0)}, ${lst(1)}, ${lst(2)}, ${lst(3)}, ${lst(4)})"
    else if (lst.size == 6)
      q"(${lst(0)}, ${lst(1)}, ${lst(2)}, ${lst(3)}, ${lst(4)}, ${lst(5)})"
    else if (lst.size == 7)
      q"(${lst(0)}, ${lst(1)}, ${lst(2)}, ${lst(3)}, ${lst(4)}, ${lst(5)}, ${lst(6)})"
    else if (lst.size == 8) q"(${lst(0)}, ${lst(1)}, ${lst(2)}, ${lst(3)}, ${lst(
      4)}, ${lst(5)}, ${lst(6)}, ${lst(7)})"
    else ???
  }

  /**
     spore {
       val x = outer
       (y: T) => { ... }
     }
    */
  def check(funTree: c.Tree, ttpe: c.Type, rtpe: c.Type): c.Tree = {
    debug(s"SPORES: enter check, tree:\n$funTree")

    val (paramSyms, retTpe, funBody, valDefEnv) = conforms(funTree)
    val validEnv = valDefEnv.map(_.symbol)
    val generator = new SporeGenerator[c.type](c)
    val paramSym = paramSyms.head

    if (paramSym != null) {
      val sporeClassName = c.freshName(anonSporeName)

      if (validEnv.isEmpty) {
        val applyDefDef = generator.createNewDefDef(paramSyms, funBody, retTpe)

        q"""
          class $sporeClassName extends scala.spores.Spore[$ttpe, $rtpe] {
            self =>
            type Captured = scala.Nothing
            this._className = this.getClass.getName
            $applyDefDef
          }
          new $sporeClassName
        """
      } else {
        // replace reference to paramSym with reference to applyParamSymbol
        // and references to captured variables with references to new fields
        val capturedTypes = validEnv.map(_.typeSignature)
        debug(s"capturedTypes: ${capturedTypes.mkString(",")}")

        val newRefs = generator.generateCapturedReferences(validEnv)
        val applyDefDef = generator.createNewDefDef(paramSyms,
                                                    funBody,
                                                    retTpe,
                                                    environment = validEnv,
                                                    capturedRefs = newRefs)

        val valDefRhss = valDefEnv.map(_.rhs).toArray
        val constructorParams = List(List(generator.toTuple(valDefRhss)))
        val superclassName = TypeName("SporeWithEnv")

        val capturedTypeDef =
          generator.createCapturedType(capturedTypes.toArray)
        val q"type $_ = $capturedType" = capturedTypeDef

        q"""
          class $sporeClassName(val captured : $capturedType) extends $superclassName[$ttpe, $rtpe] {
            self =>
            $capturedTypeDef
            this._className = this.getClass.getName
            $applyDefDef
          }
          new $sporeClassName(...$constructorParams)
        """
      }
    } else {
      ???
    }
  }

}
