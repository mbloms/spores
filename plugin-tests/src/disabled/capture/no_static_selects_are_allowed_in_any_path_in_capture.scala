
        object NoSelect {
          val v1 = 10
        }
        import scala.spores._
        val s: Spore[Int, Unit] = spore {
          (x: Int) => println("arg: " + x + ", c1: " + capture(NoSelect.v1))
        }
      