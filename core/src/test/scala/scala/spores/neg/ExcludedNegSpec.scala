package scala.spores.neg

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

import scala.spores.TestUtil._

@RunWith(classOf[JUnit4])
class ExcludedNegSpec {
  @Test
  def `nullary spores respect Excluded`(): Unit = {
    expectError("Expression has type 'String', but type 'String' is Excluded") {
      """
        import scala.spores._
        import scala.spores.SporeConv._
        val s: NullarySpore[Unit] {type Excluded = String} = spore {
          val s = "hej"
          delayed {
            ()
          }
        }
      """
    }
  }

  @Test
  def `nullary spores respect types inside composed Excluded`(): Unit = {
    expectError("Expression has type 'String', but type 'String' is Excluded") {
      """
        import scala.spores._
        import scala.spores.SporeConv._
        val s: NullarySpore[Unit] {type Excluded = (String, Int)} = spore {
          val s = "hej"
          delayed {
            ()
          }
        }
      """
    }
  }

}
