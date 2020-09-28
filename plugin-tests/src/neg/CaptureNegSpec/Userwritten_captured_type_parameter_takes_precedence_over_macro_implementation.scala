import scala.spores._

class OuterReference {
  val text = Some("Hello, World!")
  def alphabeticPattern = "^a"

  text.map(spore[String, Traversable[(String, String)], OuterReference, Nothing] {
    val thisRef = this
    (t: String) =>
      t.trim
        .split(thisRef.alphabeticPattern)
        .map(word => (word, ""))
        .toTraversable
  })
}
