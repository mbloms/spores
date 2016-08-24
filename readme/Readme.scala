package scala.spores.readme

import scalatags.Text.TypedTag
import scalatags.Text.all._
import java.text.SimpleDateFormat
import java.util.Date

import com.twitter.util.Eval
import org.scalafmt.{AlignToken, Scalafmt, ScalafmtRunner, ScalafmtStyle}

object hl extends scalatex.site.Highlighter

object Readme {

  val eval = new com.twitter.util.Eval()
  implicit def bool2frag(boolean: Boolean): StringFrag =
    stringFrag(boolean.toString)

  /** Repl session, inspired by tut.
    *
    * Example: code="1 + 1" returns
    * "scala> 1 + 1
    * res0: Int = 2"
    */
  def repl(code: String) = {
    import scala.meta._
    val expressions = s"{$code}".parse[Stat].get.asInstanceOf[Term.Block].stats
    val evaluated = eval[Any](code)
    val output = evaluated match {
      case s: String =>
        s"""
           |"$s"""".stripMargin
      case x => x.toString
    }
    val result = s"""${expressions
                      .map(x => s"scala> ${x.toString().trim}")
                      .mkString("\n")}
                    |res0: ${evaluated.getClass.getName} = $output
                    |""".stripMargin
    hl.scala(result)
  }

  def note = b("NOTE")

  def github: String = "https://github.com"
  def repo: String = "https://github.com/jvican/spores"

  def user(name: String) = a(href := s"$github/$name", s"@$name")
  def users(names: String*) =
    span(
      names.dropRight(1).map(x => span(user(x), ", ")) :+ user(names.last): _*
    )

  def issue(id: Int) = a(href := repo + s"/issues/$id", s"#$id")
  def issues(ids: Int*) = span(ids.map(issue): _*)

  def half(frags: Frag*) = div(frags, width := "50%", float.left)

  def pairs(frags: Frag*) = div(frags, div(clear := "both"))

  def snippet(code: String): TypedTag[String] = {
    snippet(code, ScalafmtStyle.default)
  }

  val stripMarginStyle =
    ScalafmtStyle.default.copy(alignStripMarginStrings = true)

  def fmt(style: ScalafmtStyle)(code: String): TypedTag[String] =
    snippet(code, style)

  def lastUpdated =
    new SimpleDateFormat("MMM d, y").format(new Date())

  def snippet(code: String, style: ScalafmtStyle): TypedTag[String] = {
    val formatted = Scalafmt.format(code, style).get
    hl.scala(formatted)
  }

  def statement(code: String): TypedTag[String] = {
    val formatted = Scalafmt
      .format(code,
        style = ScalafmtStyle.default,
        runner = ScalafmtRunner.statement)
      .get
    hl.scala(formatted)
  }
}
