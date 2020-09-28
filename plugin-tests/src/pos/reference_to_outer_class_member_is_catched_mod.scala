
import scala.spores._
class Patterns {
  val alphabeticPattern = "^a"
}

class OuterReference {
  val text = Some("Hello, World!")
  val ps = new Patterns

  text.map(spore {
    val ps = this.ps
    (t: String) =>
      t.trim.split(ps.alphabeticPattern).map(word => (word, "")).toTraversable
  })
}
