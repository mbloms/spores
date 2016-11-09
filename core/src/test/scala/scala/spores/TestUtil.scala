package scala.spores

import reflect._

object TestUtil {
  import scala.language.postfixOps
  import scala.util.Try
  import tools.reflect.{ToolBox, ToolBoxError}

  implicit class stringops(text: String) {
    def mustContain(substring: String) = assert(text contains substring, text)
  }

  def intercept[T <: Throwable: ClassTag](test: => Any): T = {
    Try {
      test
      throw new Exception(s"Expected exception ${classTag[T]}")
    } recover {
      case t: Throwable =>
        if (classTag[T].runtimeClass != t.getClass) throw t
        else t.asInstanceOf[T]
    } get
  }

  def eval(code: String, compileOptions: String = ""): Any = {
    val tb = mkToolbox(compileOptions)
    tb.eval(tb.parse(code))
  }

  def mkToolbox(compileOptions: String = "")
    : ToolBox[_ <: scala.reflect.api.Universe] = {
    val m = scala.reflect.runtime.currentMirror
    import scala.tools.reflect.ToolBox
    m.mkToolBox(options = compileOptions)
  }

  lazy val toolboxClasspath = {
    val resource = getClass.getClassLoader.getResource("toolbox.classpath")
    val classpathFile = scala.io.Source.fromFile(resource.toURI)
    val completeSporesCoreClasspath = classpathFile.getLines.mkString
    completeSporesCoreClasspath
  }

  def expectError(
      errorSnippet: String,
      compileOptions: String = "",
      baseCompileOptions: String = s"-cp $toolboxClasspath")(code: String) {
    intercept[ToolBoxError] {
      eval(code, compileOptions + " " + baseCompileOptions)
    }.getMessage mustContain errorSnippet
  }
}
