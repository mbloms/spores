
         import scala.spores._

         class Foo { val bar = "1" }

         object A extends Foo {
           val s: Spore[Int, String] = spore { (x: Int) =>
             s"arg: $x, c1: ${capture(super.bar)}"
           }
         }
      