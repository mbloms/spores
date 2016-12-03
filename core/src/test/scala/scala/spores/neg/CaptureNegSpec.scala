package scala.spores.neg

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

import scala.spores._
import scala.spores.TestUtil._
import scala.spores.util.Feedback

@RunWith(classOf[JUnit4])
class CaptureNegSpec {
  @Test
  def `wrong shape, incorrect val def list`() {
    expectError(Feedback.IncorrectSporeHeader) {
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
  }

  @Test
  def `no lazy vals allowed`() {
    expectError(Feedback.InvalidLazyMember("v1")) {
      """
        import scala.spores._
        lazy val v1 = 10
        val s: Spore[Int, Unit] = spore {
          (x: Int) => println("arg: " + x + ", c1: " + capture(v1))
        }
      """
    }
  }

  @Test
  def `no static selects are allowed in any path in 'capture'`() {
    expectError(Feedback.InvalidOuterReference("NoSelect.v1")) {
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
  }

  @Test
  def `no selects are allowed in any path in 'capture'`() {
    expectError(Feedback.InvalidOuterReference("noSelect.v1")) {
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
  }

  @Test
  def `this is not allowed in 'capture'`() {
    expectError(Feedback.InvalidOuterReference("this.v1")) {
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
  }

  @Test
  def `implicit this is not allowed in 'capture'`() {
    expectError(Feedback.InvalidOuterReference("NoThisReference.this.v1")) {
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
  }

  @Test
  def `no outer references allowed`(): Unit = {
    expectError(Feedback.InvalidReferenceTo("value outer")) {
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
  }

  @Test
  def `no references to objects in classes allowed`(): Unit = {
    expectError(Feedback.InvalidReferenceTo("class C")) {
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
  }

  @Test
  def `no references to outer objects allowed`(): Unit = {
    expectError(Feedback.InvalidReferenceTo("value outerObject")) {
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
  }

  @Test
  def `no references to extended members allowed`(): Unit = {
    expectError(Feedback.InvalidReferenceTo("object BigC")) {
      """
        import scala.spores._

        class C {
          val f = 1
        }

        object BigC extends C {
          val s = spore {
            (x: Int) =>
              val s1 = f.toString
              s1 + "!"
          }
        }
      """
    }
  }

  @Test
  def `no references to extended members allowed in delayed`(): Unit = {
    expectError(Feedback.InvalidReferenceTo("object BigC")) {
      """
        import scala.spores._

        class C {
          val f = 1
        }

        object BigC extends C {
          val s = spore {
            delayed {
              val s1 = f.toString
              s1 + "!"
            }
          }
        }
      """
    }
  }

  @Test
  def `no references to extended members allowed from a class`(): Unit = {
    expectError(Feedback.InvalidReferenceTo("class D")) {
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
  }

  @Test
  def `no references to extended members allowed from a class II`(): Unit = {
    expectError(Feedback.InvalidReferenceTo("class D")) {
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
  }

  @Test
  def `no references to extended members allowed within a method`(): Unit = {
    expectError(Feedback.InvalidReferenceTo("class D")) {
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
  }

  /* No `Super` in paths because of https://issues.scala-lang.org/browse/SI-1938 */
  @Test
  def `cannot capture super in a path with capture`(): Unit = {
    expectError(Feedback.InvalidOuterReference("A.super.bar")) {
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
  }

  @Test
  def `cannot capture super in a path`(): Unit = {
    expectError(Feedback.InvalidReferenceTo("object A")) {
      """
         import scala.spores._

         class Foo { val bar = "1" }

         object A extends Foo {
           val s: Spore[Int, String] = spore { (x: Int) =>
             s"arg: $x, c1: ${super.bar}"
           }
         }
      """.stripMargin
    }
  }

  @Test
  def `cannot capture super from a trait in a path with capture`(): Unit = {
    expectError(Feedback.InvalidOuterReference("A.super.bar")) {
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
  }

  @Test
  def `cannot capture super from a trait in a path`(): Unit = {
    expectError(Feedback.InvalidReferenceTo("class A")) {
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
  }

  @Test
  def `reference to outer class member is catched`(): Unit = {
    expectError(Feedback.InvalidReferenceTo("object OuterReference")) {
      s"""
        |import scala.spores._
        |class Patterns {
        |  val alphabeticPattern = "^a"
        |}
        |
        |object OuterReference {
        |  val text = Some("Hello, World!")
        |  val ps = new Patterns
        |
        |  text.map(spore { (t: String) =>
        |    t.trim.split(ps.alphabeticPattern).map(word => (word, "")).toTraversable
        |  })
        |}
      """.stripMargin
    }
  }


  @Test
  def `Ensure that apply arguments are checked`(): Unit = {
    expectError(Feedback.InvalidReferenceTo("value trap")) {
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
  }

  @Test
  def `Ensure that calling methods defined in classes is prohibited`(): Unit = {
    expectError(Feedback.InvalidReferenceTo("class TrapUtil")) {
      s"""import scala.spores._
         |
         |class TrapUtil {
         |  def defWithSeveralArgs(xs: String*) = xs.foreach(println)
         |  def hehe(a: String) = {
         |    val trap = "I am a trap"
         |    spore {
         |      () => TrapUtil.defWithSeveralArgs("Hello", "World", trap, "END.")
         |    }
         |  }
         |}
       """.stripMargin
    }
  }
}
