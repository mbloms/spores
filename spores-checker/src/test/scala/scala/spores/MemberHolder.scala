package scala.spores

/** This file is for experimentation and is not affected by
  * the test execution if changes are not explicitly made in
  * the plugin. It helps check interop with Java transient and
  * checking of basic members. */

trait BaseBaseMemberHolder {
  val member5: String
  val member6: Object
  var member7: List[String]
  var memberIgnored: Int => Int
}

trait BaseMemberHolder extends BaseBaseMemberHolder {
  val member3: Double
  val member4: Object
}

abstract class MemberHolder(member0: Int) extends BaseMemberHolder {
  private val member1 = 1
  val member2: String
  var member8: Int
  println(member1)
}

trait Foo
class Bar(member0: Int, val member5: String, var member8: Int)
    extends MemberHolder(member0) {
  val member2 = ""
  val member3 = 4.0
  val member4: Object = new Foo {}
  val member6: Object = member4.asInstanceOf[Object]
  var member7: List[String] = Nil
  member7 = List("")
  @transient var memberIgnored: Int => Int = (a: Int) => a
  val member9 = new scala.spores.JavaTransientMemberHolder
}
