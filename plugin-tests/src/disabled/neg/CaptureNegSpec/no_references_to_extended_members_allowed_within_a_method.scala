
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
      