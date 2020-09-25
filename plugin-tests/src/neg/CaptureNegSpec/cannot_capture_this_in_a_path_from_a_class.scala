
         import scala.spores._

         class Foo { def bar = "1" }

         class A extends Foo {
           val s: Spore[Int, String] = spore { (x: Int) =>
             s"arg: $x, c1: ${this.bar}"
           }
         }
