package scala.spores

protected object Feedback {
  val MissingSporeClassDef =
    "Expected spore class definition. The generated format of a spore has changed"

  val UnsupportedAritySpore =
    "The arity of this support is not yet supported, please file an issue."

  val IncorrectSporeHeader =
    "Incorrect spore header: Only val defs allowed at this position."

  val IncorrectSporeBody =
    "Incorrect spore body: expected function literal or `delayed`."

  val InvalidOuterReference =
    "Only stable paths can be captured inside a spore."

  val InvalidLazyMember =
    "The path of a captured variable inside a spore cannot contain lazy members."

  def NonStaticInvocation(method: String) =
    s"Spore contains invocation of a non-static method: '$method'."

  def InvalidReferenceTo(symbol: String) =
    s"Spore contains references to an invalid symbol: $symbol"

  def InvalidReferenceToExcludedType(tpe: String) =
    s"Unexpected expression with type '$tpe', but type '$tpe' is Excluded"
}
