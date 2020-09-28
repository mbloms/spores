import scala.spores._

case class Person(name: String, age: Int)

class Example:
  val outer1 = 0
  val outer2 = Person("Jim", 35)
  val s = spore {
    val inner = outer2
    (x: Int) => {
      s"The result is: ${x + inner.age + outer1}"
    }
  }
