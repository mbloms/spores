package scala.spores.neg

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

import scala.spores._
import scala.spores.TestUtil._

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
    expectError(Feedback.InvalidLazyMember) {
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
  def `no lazy vals allowed in any path`() {
    expectError(Feedback.InvalidLazyMember) {
      """
        object NoLazyValsObj {
          lazy val v1 = 10
        }
        import scala.spores._
        val s: Spore[Int, Unit] = spore {
          (x: Int) => println("arg: " + x + ", c1: " + capture(NoLazyValsObj.v1))
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
    expectError(Feedback.NonStaticInvocation("outerObject.m")) {
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
  def `cannot capture super in a path`(): Unit = {
    expectError(Feedback.InvalidOuterReference) {
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
}
