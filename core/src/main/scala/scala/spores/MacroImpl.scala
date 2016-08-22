/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2002-2013, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

package scala.spores

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

  def createSpore(funTree: c.Tree, targs: List[c.Type]): c.Tree = {
    val (paramSyms, retTpe, funBody, valDefEnv) = conforms(funTree)
    val validEnv = valDefEnv.map(_.symbol)
    val generator = new SporeGenerator[c.type](c)
    val sporeName = c.freshName(anonSporeName)

    if (validEnv.isEmpty) {
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
        else ???
      generator.generateSpore(sporeName, sporeType, Nil, sporeBody)
    } else {
      val capturedTypes = validEnv.map(_.typeSignature).toArray
      debug(s"Captured types: ${capturedTypes.mkString(",")}")
      val newRefs = generator.generateCapturedReferences(validEnv)
      val sporeBody = generator.createNewDefDef(paramSyms,
                                                funBody,
                                                retTpe,
                                                environment = validEnv,
                                                capturedRefs = newRefs)
      val valDefRhss = valDefEnv.map(_.rhs).toArray
      val constructorParams = List(generator.toTuple(valDefRhss))
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
        else ???
      generator.generateSpore(sporeName,
                              sporeType,
                              List(capturedType),
                              sporeBody,
                              constructorParams)
    }
  }
}
