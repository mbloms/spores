import scala.spores._
class OuterReference {
  val text = Some("Hello, World!")
  def alphabeticPattern = "^a"

  val s3: Spore[String, ?] {
    type Captured = Nothing
    type Excluded = Nothing
  } = spore {
    val thisRef = this
    (t: String) =>
      t.trim
        .split(thisRef.alphabeticPattern)
        .map(word => (word, ""))
        .toTraversable
  }
  s3("")
}
