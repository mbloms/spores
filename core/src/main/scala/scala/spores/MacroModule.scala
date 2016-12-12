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
  implicit val c0 = c
  import c.universe._

  /* Don't change this name since it's used
   * to check if a class is indeed a spore */
  val anonSporeName = TypeName("anonspore")

  type Env = List[(Symbol, Tree)]
  def conforms(funTree: c.Tree,
               excluded: c.Type): (List[Symbol], Type, Tree, Env) = {
    val analysis = new SporeAnalysis[c.type](c)
    val (explicitSporeEnv, sporeFunDef) = analysis.stripSporeStructure(funTree)
    explicitSporeEnv foreach (s => debug(s"Explicitly captured symbol: $s"))

    val (funOpt, vparams, sporeBody) = analysis.readSporeFunDef(sporeFunDef)
    val functionSymbol = funOpt.map(_.symbol)
    val nonExplicitEnv = analysis.collectCaptured(sporeBody)
    val declared = analysis.collectDeclared(sporeBody)
    val env = explicitSporeEnv.map(_.symbol) ++ nonExplicitEnv.map(_._1)
    val checker = new SporeChecker[c.type](c)(env, functionSymbol, declared)

    debug(s"Checking conformance of ${showCode(sporeBody)}...")
    checker.checkReferencesInBody(sporeBody)
    checker.checkExcludedTypesInBody(excluded, sporeBody)
    val explicitEnv = explicitSporeEnv.map(vd => vd.symbol -> vd.rhs)
    val fullSporeEnv = nonExplicitEnv ++ explicitEnv
    (vparams.map(_.symbol), sporeBody.tpe, sporeBody, fullSporeEnv)
  }

  def createSpore(funTree: c.Tree,
                  targs: List[c.Type],
                  expectedCapturedType: c.Type,
                  expectedExcludedType: c.Type,
                  forceCaptured: Boolean = false): c.Tree = {
    val (paramSyms, retTpe, funBody, fullSporeEnv) =
      conforms(funTree, expectedExcludedType)
    val (symbolsEnv, explicitRhsEnv) = fullSporeEnv.unzip
    val generator = new SporeGenerator[c.type](c)
    val sporeName = c.freshName(anonSporeName)

    val arity = paramSyms.size
    val sporeType =
      generator.generateSporeType(arity, targs, symbolsEnv.nonEmpty)
    val (generatedSpore, capturedType) = if (symbolsEnv.isEmpty) {
      val sporeBody = generator.createNewDefDef(paramSyms, funBody, retTpe)
      generator.generateSpore(sporeName,
                              sporeType,
                              expectedCapturedType,
                              expectedExcludedType,
                              sporeBody,
                              Nil,
                              true,
                              expectedCapturedType) -> expectedCapturedType
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
      debug(s"Forcing $expectedCapturedType? $forceCaptured")
      generator.generateSpore(sporeName,
                              sporeType,
                              capturedType,
                              expectedExcludedType,
                              sporeBody,
                              constructorParams,
                              forceCaptured,
                              expectedCapturedType) -> capturedType
    }

    // Forces typecheck error if user expects nothing
    // Whitebox macros do not detect this error in type ascriptions
    if (forceCaptured &&
        expectedCapturedType =:= definitions.NothingTpe &&
        !(capturedType =:= expectedCapturedType)) {
      c.abort(c.enclosingPosition,
              util.Feedback.CapturedTypeMismatch(expectedCapturedType.toString,
                                                 capturedType.toString))
    }

    generatedSpore
  }
}
