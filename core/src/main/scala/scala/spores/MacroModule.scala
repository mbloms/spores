/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2002-2013, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

package scala.spores

import scala.reflect.macros.whitebox

private[spores] class MacroModule[C <: whitebox.Context](val c: C) {
  import c.universe._

  /* Don't change this name since it's used
   * to check if a class is indeed a spore */
  val anonSporeName = TypeName("anonspore")

  private val sporesPath = q"scala.spores"

  type Env = List[(Symbol, Tree)]
  def conforms(funTree: c.Tree): (List[Symbol], Type, Tree, Env) = {
    val analysis = new SporeAnalysis[c.type](c)
    val (explicitSporeEnv, sporeFunDef) = analysis.stripSporeStructure(funTree)
    explicitSporeEnv foreach (s => debug(s"Explicitly captured symbol: $s"))

    val (funOpt, vparams, sporeBody) = analysis.readSporeFunDef(sporeFunDef)
    val functionSymbol = funOpt.map(_.symbol)
    val nonExplicitEnv = analysis.collectCaptured(sporeBody)
    val captured = nonExplicitEnv.map(_._1)
    val declared = analysis.collectDeclared(sporeBody)
    val symbolsEnv = explicitSporeEnv.map(_.symbol)
    val checker = new SporeChecker[c.type](c)(symbolsEnv,
                                              functionSymbol,
                                              captured,
                                              declared)

    debug(s"Checking conformance of ${showRaw(sporeBody)}...")
    checker.checkReferencesInBody(sporeBody)
    val explicitEnv = explicitSporeEnv.map(vd => vd.symbol -> vd.rhs)
    val fullSporeEnv = nonExplicitEnv ++ explicitEnv
    (vparams.map(_.symbol), sporeBody.tpe, sporeBody, fullSporeEnv)
  }

  def createSpore(funTree: c.Tree, targs: List[c.Type]): c.Tree = {
    val (paramSyms, retTpe, funBody, fullSporeEnv) = conforms(funTree)
    val (symbolsEnv, explicitRhsEnv) = fullSporeEnv.unzip
    val generator = new SporeGenerator[c.type](c)
    val sporeName = c.freshName(anonSporeName)

    if (symbolsEnv.isEmpty) {
      val sporeBody = generator.createNewDefDef(paramSyms, funBody, retTpe)
      val sporeType =
        if (paramSyms.isEmpty)
          tq"$sporesPath.NullarySpore[..$targs]"
        else if (paramSyms.size == 1)
          tq"$sporesPath.Spore[..$targs]"
        else if (paramSyms.size == 2)
          tq"$sporesPath.Spore2[..$targs]"
        else if (paramSyms.size == 3)
          tq"$sporesPath.Spore3[..$targs]"
        else if (paramSyms.size == 4)
          tq"$sporesPath.Spore4[..$targs]"
        else if (paramSyms.size == 5)
          tq"$sporesPath.Spore5[..$targs]"
        else if (paramSyms.size == 6)
          tq"$sporesPath.Spore6[..$targs]"
        else if (paramSyms.size == 7)
          tq"$sporesPath.Spore7[..$targs]"
        else c.abort(funTree.pos, Feedback.UnsupportedAritySpore)

      generator.generateSpore(sporeName, sporeType, Nil, sporeBody)
    } else {
      val capturedTypes = symbolsEnv.map(_.typeSignature).toArray
      debug(s"Captured types: ${capturedTypes.mkString(",")}")
      val newRefs = generator.generateCapturedReferences(symbolsEnv)
      val sporeBody = generator.createNewDefDef(paramSyms,
                                                funBody,
                                                retTpe,
                                                environment = symbolsEnv,
                                                capturedRefs = newRefs)
      val envRefs = explicitRhsEnv.toArray
      val constructorParams = List(generator.toTuple(envRefs))
      val capturedType = generator.toTypeTuple(capturedTypes)

      val sporeType =
        if (paramSyms.isEmpty)
          tq"$sporesPath.NullarySporeWithEnv[..$targs]"
        else if (paramSyms.size == 1)
          tq"$sporesPath.SporeWithEnv[..$targs]"
        else if (paramSyms.size == 2)
          tq"$sporesPath.Spore2WithEnv[..$targs]"
        else if (paramSyms.size == 3)
          tq"$sporesPath.Spore3WithEnv[..$targs]"
        else if (paramSyms.size == 4)
          tq"$sporesPath.Spore4WithEnv[..$targs]"
        else if (paramSyms.size == 5)
          tq"$sporesPath.Spore5WithEnv[..$targs]"
        else if (paramSyms.size == 6)
          tq"$sporesPath.Spore6WithEnv[..$targs]"
        else if (paramSyms.size == 7)
          tq"$sporesPath.Spore7WithEnv[..$targs]"
        else ???
      generator.generateSpore(sporeName,
                              sporeType,
                              List(capturedType),
                              sporeBody,
                              constructorParams)
    }
  }
}
