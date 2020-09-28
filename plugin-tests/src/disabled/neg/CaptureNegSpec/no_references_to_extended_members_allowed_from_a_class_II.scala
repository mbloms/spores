
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
      