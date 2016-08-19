package scala.spores

import scala.reflect.macros.whitebox

protected class SporeBuilder[C <: whitebox.Context with Singleton](val ctx: C) {
  import ctx.universe._

  /** Create a type alias for `Captured` given the captured types in the spore header. */
  def createCapturedType(capturedTypes: Array[Type]): Tree = {
    if (capturedTypes.length == 1) q"type Captured = ${capturedTypes(0)}"
    else if (capturedTypes.length == 2)
      q"type Captured = (${capturedTypes(0)}, ${capturedTypes(1)})"
    else if (capturedTypes.length == 3)
      q"type Captured = (${capturedTypes(0)}, ${capturedTypes(1)}, ${capturedTypes(2)})"
    else if (capturedTypes.length == 4)
      q"type Captured = (${capturedTypes(0)}, ${capturedTypes(1)}, ${capturedTypes(
        2)}, ${capturedTypes(3)})"
    else if (capturedTypes.length == 5)
      q"type Captured = (${capturedTypes(0)}, ${capturedTypes(1)}, ${capturedTypes(
        2)}, ${capturedTypes(3)}, ${capturedTypes(4)})"
    else if (capturedTypes.length == 6)
      q"type Captured = (${capturedTypes(0)}, ${capturedTypes(1)}, ${capturedTypes(
        2)}, ${capturedTypes(3)}, ${capturedTypes(4)}, ${capturedTypes(5)})"
    else if (capturedTypes.length == 7)
      q"type Captured = (${capturedTypes(0)}, ${capturedTypes(1)}, ${capturedTypes(
        2)}, ${capturedTypes(3)}, ${capturedTypes(4)}, ${capturedTypes(5)}, ${capturedTypes(6)})"
    else if (capturedTypes.length == 8)
      q"type Captured = (${capturedTypes(0)}, ${capturedTypes(1)}, ${capturedTypes(
        2)}, ${capturedTypes(3)}, ${capturedTypes(4)}, ${capturedTypes(5)}, ${capturedTypes(
        6)}, ${capturedTypes(7)})"
    else if (capturedTypes.length == 9)
      q"type Captured = (${capturedTypes(0)}, ${capturedTypes(1)}, ${capturedTypes(
        2)}, ${capturedTypes(3)}, ${capturedTypes(4)}, ${capturedTypes(5)}, ${capturedTypes(
        6)}, ${capturedTypes(7)}, ${capturedTypes(8)})"
    else if (capturedTypes.length == 10)
      q"type Captured = (${capturedTypes(0)}, ${capturedTypes(1)}, ${capturedTypes(
        2)}, ${capturedTypes(3)}, ${capturedTypes(4)}, ${capturedTypes(5)}, ${capturedTypes(
        6)}, ${capturedTypes(7)}, ${capturedTypes(8)}, ${capturedTypes(9)})"
    else if (capturedTypes.length == 11)
      q"type Captured = (${capturedTypes(0)}, ${capturedTypes(1)}, ${capturedTypes(
        2)}, ${capturedTypes(3)}, ${capturedTypes(4)}, ${capturedTypes(5)}, ${capturedTypes(
        6)}, ${capturedTypes(7)}, ${capturedTypes(8)}, ${capturedTypes(9)}, ${capturedTypes(10)})"
    else if (capturedTypes.length == 12)
      q"type Captured = (${capturedTypes(0)}, ${capturedTypes(1)}, ${capturedTypes(
        2)}, ${capturedTypes(3)}, ${capturedTypes(4)}, ${capturedTypes(5)}, ${capturedTypes(
        6)}, ${capturedTypes(7)}, ${capturedTypes(8)}, ${capturedTypes(9)}, ${capturedTypes(
        10)}, ${capturedTypes(11)})"
    else if (capturedTypes.length == 13)
      q"type Captured = (${capturedTypes(0)}, ${capturedTypes(1)}, ${capturedTypes(
        2)}, ${capturedTypes(3)}, ${capturedTypes(4)}, ${capturedTypes(5)}, ${capturedTypes(
        6)}, ${capturedTypes(7)}, ${capturedTypes(8)}, ${capturedTypes(9)}, ${capturedTypes(
        10)}, ${capturedTypes(11)}, ${capturedTypes(12)})"
    else if (capturedTypes.length == 14)
      q"type Captured = (${capturedTypes(0)}, ${capturedTypes(1)}, ${capturedTypes(
        2)}, ${capturedTypes(3)}, ${capturedTypes(4)}, ${capturedTypes(5)}, ${capturedTypes(
        6)}, ${capturedTypes(7)}, ${capturedTypes(8)}, ${capturedTypes(9)}, ${capturedTypes(
        10)}, ${capturedTypes(11)}, ${capturedTypes(12)}, ${capturedTypes(13)})"
    else if (capturedTypes.length == 15)
      q"type Captured = (${capturedTypes(0)}, ${capturedTypes(1)}, ${capturedTypes(2)}, ${capturedTypes(
        3)}, ${capturedTypes(4)}, ${capturedTypes(5)}, ${capturedTypes(6)}, ${capturedTypes(
        7)}, ${capturedTypes(8)}, ${capturedTypes(9)}, ${capturedTypes(10)}, ${capturedTypes(
        11)}, ${capturedTypes(12)}, ${capturedTypes(13)}, ${capturedTypes(14)})"
    else if (capturedTypes.length == 16)
      q"type Captured = (${capturedTypes(0)}, ${capturedTypes(1)}, ${capturedTypes(
        2)}, ${capturedTypes(3)}, ${capturedTypes(4)}, ${capturedTypes(5)}, ${capturedTypes(
        6)}, ${capturedTypes(7)}, ${capturedTypes(8)}, ${capturedTypes(9)}, ${capturedTypes(
        10)}, ${capturedTypes(11)}, ${capturedTypes(12)}, ${capturedTypes(13)}, ${capturedTypes(
        14)}, ${capturedTypes(15)})"
    else if (capturedTypes.length == 17)
      q"type Captured = (${capturedTypes(0)}, ${capturedTypes(1)}, ${capturedTypes(
        2)}, ${capturedTypes(3)}, ${capturedTypes(4)}, ${capturedTypes(5)}, ${capturedTypes(
        6)}, ${capturedTypes(7)}, ${capturedTypes(8)}, ${capturedTypes(9)}, ${capturedTypes(
        10)}, ${capturedTypes(11)}, ${capturedTypes(12)}, ${capturedTypes(13)}, ${capturedTypes(
        14)}, ${capturedTypes(15)}, ${capturedTypes(16)})"
    else if (capturedTypes.length == 18)
      q"type Captured = (${capturedTypes(0)}, ${capturedTypes(1)}, ${capturedTypes(
        2)}, ${capturedTypes(3)}, ${capturedTypes(4)}, ${capturedTypes(5)}, ${capturedTypes(
        6)}, ${capturedTypes(7)}, ${capturedTypes(8)}, ${capturedTypes(9)}, ${capturedTypes(
        10)}, ${capturedTypes(11)}, ${capturedTypes(12)}, ${capturedTypes(13)}, ${capturedTypes(
        14)}, ${capturedTypes(15)}, ${capturedTypes(16)}, ${capturedTypes(17)})"
    else if (capturedTypes.length == 19)
      q"type Captured = (${capturedTypes(0)}, ${capturedTypes(1)}, ${capturedTypes(2)}, ${capturedTypes(
        3)}, ${capturedTypes(4)}, ${capturedTypes(5)}, ${capturedTypes(6)}, ${capturedTypes(
        7)}, ${capturedTypes(8)}, ${capturedTypes(9)}, ${capturedTypes(10)}, ${capturedTypes(
        11)}, ${capturedTypes(12)}, ${capturedTypes(13)}, ${capturedTypes(14)}, ${capturedTypes(
        15)}, ${capturedTypes(16)}, ${capturedTypes(17)}, ${capturedTypes(18)})"
    else if (capturedTypes.length == 20)
      q"type Captured = (${capturedTypes(0)}, ${capturedTypes(1)}, ${capturedTypes(
        2)}, ${capturedTypes(3)}, ${capturedTypes(4)}, ${capturedTypes(5)}, ${capturedTypes(
        6)}, ${capturedTypes(7)}, ${capturedTypes(8)}, ${capturedTypes(9)}, ${capturedTypes(
        10)}, ${capturedTypes(11)}, ${capturedTypes(12)}, ${capturedTypes(13)}, ${capturedTypes(
        14)}, ${capturedTypes(15)}, ${capturedTypes(16)}, ${capturedTypes(17)}, ${capturedTypes(
        18)}, ${capturedTypes(19)})"
    else if (capturedTypes.length == 21)
      q"type Captured = (${capturedTypes(0)}, ${capturedTypes(1)}, ${capturedTypes(
        2)}, ${capturedTypes(3)}, ${capturedTypes(4)}, ${capturedTypes(5)}, ${capturedTypes(
        6)}, ${capturedTypes(7)}, ${capturedTypes(8)}, ${capturedTypes(9)}, ${capturedTypes(
        10)}, ${capturedTypes(11)}, ${capturedTypes(12)}, ${capturedTypes(13)}, ${capturedTypes(
        14)}, ${capturedTypes(15)}, ${capturedTypes(16)}, ${capturedTypes(17)}, ${capturedTypes(
        18)}, ${capturedTypes(19)}, ${capturedTypes(20)})"
    else if (capturedTypes.length == 22)
      q"type Captured = (${capturedTypes(0)}, ${capturedTypes(1)}, ${capturedTypes(
        2)}, ${capturedTypes(3)}, ${capturedTypes(4)}, ${capturedTypes(5)}, ${capturedTypes(
        6)}, ${capturedTypes(7)}, ${capturedTypes(8)}, ${capturedTypes(9)}, ${capturedTypes(
        10)}, ${capturedTypes(11)}, ${capturedTypes(12)}, ${capturedTypes(13)}, ${capturedTypes(
        14)}, ${capturedTypes(15)}, ${capturedTypes(16)}, ${capturedTypes(17)}, ${capturedTypes(
        18)}, ${capturedTypes(19)}, ${capturedTypes(20)}, ${capturedTypes(21)})"
    else
      ctx.abort(ctx.enclosingPosition,
                "You cannot construct a tuple of more than 22 elements.")
  }
}
