import scala.spores._

/** It's unclear if this should be allowed.
 *  Capturing outer1 is currently allowed because it's a static field.
 *  This is not ok in the original spores implementation.
 */

case class Person(name: String, age: Int)
val outer1 = 0
val outer2 = Person("Jim", 35)
val s = spore {
  val inner = outer2
  (x: Int) => {
    s"The result is: ${x + inner.age + outer1}"
  }
}
