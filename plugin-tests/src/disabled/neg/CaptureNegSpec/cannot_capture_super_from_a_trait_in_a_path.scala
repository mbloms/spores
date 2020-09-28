
         import scala.spores._

         trait Foo { val bar = "1" }

         class A extends Foo {
           val s: Spore[Int, String] = spore { (x: Int) =>
             s"arg: $x, c1: ${super.bar}"
           }
         }
      