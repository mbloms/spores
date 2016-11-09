package members

trait BaseMemberHolder {
  val member3: Float
  val member4: Object
}

abstract class MemberHolder(val member: Int) extends BaseMemberHolder {
  val member2: String
}
