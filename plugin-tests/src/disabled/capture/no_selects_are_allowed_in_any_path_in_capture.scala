
        class NoSelect {
          val v1 = 10
        }
        val noSelect = new NoSelect
        import scala.spores._
        val s: Spore[Int, Unit] = spore {
          (x: Int) => println("arg: " + x + ", c1: " + capture(noSelect.v1))
        }
      