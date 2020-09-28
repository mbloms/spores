import scala.spores._

class OuterReference {
  val text = Some("Hello, World!")
  def alphabeticPattern = "^a"

  text.map((spore {
    val thisRef = this
    (t: String) =>
      t.trim
        .split(thisRef.alphabeticPattern)
        .map(word => (word, ""))
        .toTraversable
  }): Spore[String, Traversable[(String, String)]] { type Captured = Nothing; type Excluded = Nothing })
}
