package members

trait BaseBaseMemberHolder {
  val member5: String
  val member6: Object
}


trait BaseMemberHolder extends BaseBaseMemberHolder {
  val member3: Double
  val member4: Object
}

abstract class MemberHolder(member0: Int) extends BaseMemberHolder {
  private val member1 = 1
  val member2: String
  println(member1)
}

class Bar(member0: Int, @transient val member5: String, member10: Int) extends MemberHolder(member0) {
  val member2 = ""
  val member3 = 4.0
  val member4 = new Integer(1)
  val member6 = member4
}
