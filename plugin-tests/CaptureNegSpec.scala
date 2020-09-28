import java.io.File
import java.io.PrintWriter

object CaptureNegSpec extends App {
  def printTest(name: String, code: String) = {
    val goodName = name.replace(' ','_').filter("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_".contains(_))
    val writer = new PrintWriter(new File(s"src/neg/CaptureNegSpec/${goodName}.scala"))
    writer.write(code)
    writer.close()
  }
  printTest(
  "wrong shape, incorrect val def list",{
  {
      """
        import scala.spores._
        val v1 = 10
        val s: Spore[Int, Unit] = spore {
          val c1 = v1
          println("hi")
          (x: Int) => println(s"arg: $x, c1: $c1")
        }
      """
    }
  })

  printTest(
  "no lazy vals allowed",{
  {
      """
        import scala.spores._
        lazy val v1 = 10
        val s: Spore[Int, Unit] = spore {
          (x: Int) => println("arg: " + x + ", c1: " + capture(v1))
        }
      """
    }
  })

  printTest(
  "no static selects are allowed in any path in 'capture'",{
  {
      """
        object NoSelect {
          val v1 = 10
        }
        import scala.spores._
        val s: Spore[Int, Unit] = spore {
          (x: Int) => println("arg: " + x + ", c1: " + capture(NoSelect.v1))
        }
      """
    }
  })

  printTest(
  "no selects are allowed in any path in 'capture'",{
  {
      """
        class NoSelect {
          val v1 = 10
        }
        val noSelect = new NoSelect
        import scala.spores._
        val s: Spore[Int, Unit] = spore {
          (x: Int) => println("arg: " + x + ", c1: " + capture(noSelect.v1))
        }
      """
    }
  })

  printTest(
  "this is not allowed in 'capture'",{
  {
      """
        class NoThisReference {
          val v1 = 10
          import scala.spores._
          val s: Spore[Int, Unit] = spore {
            (x: Int) => println("arg: " + x + ", c1: " + capture(this.v1))
          }
        }
      """
    }
  })

  printTest(
  "implicit this is not allowed in 'capture'",{
  {
      """
        class NoThisReference {
          val v1 = 10
          import scala.spores._
          val s: Spore[Int, Unit] = spore {
            (x: Int) => println("arg: " + x + ", c1: " + capture(v1))
          }
        }
      """
    }
  })

  printTest(
  "no outer references allowed",{
  {
      """
        import scala.spores._
        val outer = "hello"
        val s = spore {
          (x: Int) =>
            val s1 = outer
            s1 + "!"
        }
      """
    }
  })

  printTest(
  "no references to objects in classes allowed",{
  {
      """
        import scala.spores._
        class C {
          object A {
            def foo(a: Int, b: Int): Int =
              a * b * 3
          }
          def m(): Unit = {
            val s = spore {
              val y = 3
              (x: Int) => A.foo(x, y)
            }
          }
        }
      """
    }
  })

  printTest(
  "no indirect references to outer objects allowed",{
  {
      """
        import scala.spores._

        class C {
          def m(i: Int): Any = "example " + i
        }

        object TopLevelObject {
          val f = new C
        }

        val outerObject = TopLevelObject.f
        val s = spore {
          (x: Int) =>
            val s1 = outerObject.m(x).asInstanceOf[String]
            s1 + "!"
        }
      """
    }
  })

  printTest(
  "no indirect references to outer objects allowed II",{
  {
      """
        import scala.spores._

        class C {
          def m(i: Int): Any = "example " + i
        }

        object TopLevelObject {
          val f = new C
        }

        var outerObject = TopLevelObject.f
        val s = spore {
          (x: Int) =>
            val s1 = outerObject.m(x).asInstanceOf[String]
            s1 + "!"
        }
      """
    }
  })

  printTest(
  "no references to extended members allowed from a class",{
  {
      """
        import scala.spores._

        trait C {
          val f = 1
        }

        class D extends C {
          val s = spore {
            (x: Int) =>
              val s1 = f.toString
              s1 + "!"
          }
        }
      """
    }
  })

  printTest(
  "no references to extended members allowed from a class II",{
  {
      """
        import scala.spores._

        trait C {
          val f = 1
        }

        class D extends C {
          val s = spore {
            delayed {
              val s1 = f.toString
              s1 + "!"
            }
          }
        }
      """
    }
  })

  printTest(
  "no references to extended members allowed within a method",{
  {
      """
        import scala.spores._

        trait B

        trait C extends B {
          val f = 1
        }

        class D extends C {
          def caca: String => String = {
            case "meh" =>
              println("HA")
              val s = spore {
                delayed {
                  println(f)
                }
              }
              "SHOULD HAVE FAILED"
            case _ => ""
          }
        }
      """
    }
  })

  printTest(
  "cannot capture super in a path with capture",{
  {
      """
         import scala.spores._

         class Foo { val bar = "1" }

         object A extends Foo {
           val s: Spore[Int, String] = spore { (x: Int) =>
             s"arg: $x, c1: ${capture(super.bar)}"
           }
         }
      """.stripMargin
    }
  })

  printTest(
  "cannot capture super in a path from a class",{
  {
      """
         import scala.spores._

         class Foo { def bar = "1" }

         class A extends Foo {
           val s: Spore[Int, String] = spore { (x: Int) =>
             s"arg: $x, c1: ${super.bar}"
           }
         }
      """.stripMargin
    }
  })

  printTest(
  "cannot capture super from a trait in a path with capture",{
  {
      """
         import scala.spores._

         trait Foo { val bar = "1" }

         class A extends Foo {
           val s: Spore[Int, String] = spore { (x: Int) =>
             s"arg: $x, c1: ${capture(super.bar)}"
           }
         }
      """.stripMargin
    }
  })

  printTest(
  "cannot capture super from a trait in a path",{
  {
      """
         import scala.spores._

         trait Foo { val bar = "1" }

         class A extends Foo {
           val s: Spore[Int, String] = spore { (x: Int) =>
             s"arg: $x, c1: ${super.bar}"
           }
         }
      """.stripMargin
    }
  })

  printTest(
  "reference to outer class member is catched",{
  {
      s"""
        |import scala.spores._
        |class Patterns {
        |  val alphabeticPattern = "^a"
        |}
        |
        |class OuterReference {
        |  val text = Some("Hello, World!")
        |  val ps = new Patterns
        |
        |  text.map(spore { (t: String) =>
        |    t.trim.split(ps.alphabeticPattern).map(word => (word, "")).toTraversable
        |  })
        |}
      """.stripMargin
    }
  })

  printTest(
  "User-written captured type parameter takes precedence over macro implementation",{
  {
      s"""import scala.spores._
         |
         |class OuterReference {
         |  val text = Some("Hello, World!")
         |  def alphabeticPattern = "^a"
         |
         |text.map(spore[String, Traversable[(String, String)], Nothing, Nothing] {
         |  val thisRef = this
         |  (t: String) =>
         |    t.trim
         |      .split(thisRef.alphabeticPattern)
         |      .map(word => (word, ""))
         |      .toTraversable
         |})
         |}
         |
       """.stripMargin
    }
  })

  printTest(
  "User-written captured type parameter takes precedence over macro implementation II",{
  {
      s"""import scala.spores._
         |
         |class OuterReference {
         |  val text = Some("Hello, World!")
         |  def alphabeticPattern = "^a"
         |
         |text.map((spore {
         |  val thisRef = this
         |  (t: String) =>
         |    t.trim
         |      .split(thisRef.alphabeticPattern)
         |      .map(word => (word, ""))
         |      .toTraversable
         |}): Spore[String, Traversable[(String, String)]] { type Captured = Nothing; type Excluded = Nothing })
         |}
         |
       """.stripMargin
    }
  })

  printTest(
  "Explicit and wrong captured type member in lhs of ValDef produces type mismatch",{
  {
      s"""import scala.spores._
         |class OuterReference {
         |  val text = Some("Hello, World!")
         |  def alphabeticPattern = "^a"
         |
         |  val s3: Spore[String, _] {
         |    type Captured = Nothing
         |    type Excluded = Nothing
         |  } = spore {
         |    val thisRef = this
         |    (t: String) =>
         |      t.trim
         |        .split(thisRef.alphabeticPattern)
         |        .map(word => (word, ""))
         |        .toTraversable
         |  }
         |  s3("")
         |}
       """.stripMargin
    }
  })

  printTest(
  "Ensure that apply arguments are checked",{
  {
      s"""import scala.spores._
         |
         |object TrapUtil {
         |  def defWithSeveralArgs(xs: String*) = xs.foreach(println)
         |}
         |
         |val trap = "I am a trap"
         |spore {
         |  () => TrapUtil.defWithSeveralArgs("Hello", "World", trap, "END.")
         |}
       """.stripMargin
    }
  })

  printTest(
  "Ensure that calling methods defined in classes is prohibited",{
  {
      s"""import scala.spores._
         |
         |class TrapUtil {
         |  def defWithSeveralArgs(xs: String*) = xs.foreach(println)
         |  def hehe(a: String) = {
         |    val trap = "I am a trap"
         |    spore {
         |      () => defWithSeveralArgs("Hello", "World", trap, "END.")
         |    }
         |  }
         |}
       """.stripMargin
    }
  })

  printTest(
  "Don't allow objects defined inside the method of a class",{
  {
      s"""import scala.spores._
         |
         |class Base { val f = 1 }
         |
         |class Outer {
         |  def method(): Unit = {
         |    object Inner extends Base {
         |      val s = spore { (x: Int) =>
         |        f.toString + x.toString + "!"
         |      }
         |    }
         |  }
         |}
       """.stripMargin
    }
  })

  printTest(
  "Example in getting started",{
  {
      """import scala.spores._
         |
         |case class Person(name: String, age: Int)
         |val outer1 = 0
         |val outer2 = Person("Jim", 35)
         |val s = spore {
         |  val inner = outer2
         |  (x: Int) => {
         |    s"The result is: ${x + inner.age + outer1}"
         |  }
         |}
       """.stripMargin
    }
  })
}