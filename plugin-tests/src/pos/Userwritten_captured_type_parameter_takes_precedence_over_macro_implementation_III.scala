import scala.spores._

class OuterReference {
  val text = Some("Hello, World!")
  def alphabeticPattern = "^a"

  text.map {
    val s: Spore[String, Traversable[(String, String)]] { type Captured <: OuterReference; type Excluded >: Nothing } = spore {
      val thisRef = this
      (t: String) =>
        t.trim
          .split(thisRef.alphabeticPattern)
          .map(word => (word, ""))
          .toTraversable
    }
    s
  }
}
