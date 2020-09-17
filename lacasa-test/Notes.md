# type members

the `typeMembers` function in the Types class returns a `Seq` of `SingleDenotation`s.
God knows how to interpret those as regular types.

# Denotations and Symbols
I need to actually understand the difference and the purposes, and understand them well.

# Completion

What does it mean?

* denot.info is always a real type!
* denot.infoOrCompleter is a lazy type

# Skolem Type

# Spores
* How to handle nested spores?

# Comparing Type Bounds
  type T1 >: Lo1 <: Hi1
  type T2 >: Lo2 <: Hi2

type comparer works like this:

for `T1 <:< T2` to be true
* `Lo2 <:< Lo1`
  - or `Lo2 = Nothing`
* `Hi1 <:< Hi2`
  - or `Hi2 = AnyKind`
  - or `Hi2 = Any`
    * and `Hi1` is not bounded by a type lambda or `AnyKind`
*             ((hi2 eq AnyType) && !hi1.isLambdaSub || (hi2 eq AnyKindType) || isSubType(hi1, hi2))

```scala
case tp2 @ TypeBounds(lo2, hi2) =>
  def compareTypeBounds = tp1 match {
    case tp1 @ TypeBounds(lo1, hi1) =>
      ((lo2 eq NothingType) || isSubType(lo2, lo1)) &&
      ((hi2 eq AnyType) && !hi1.isLambdaSub || (hi2 eq AnyKindType) || isSubType(hi1, hi2))
```

Ignorantly simplified `type T >: Lo <: Hi` behaves like
```scala
trait TypeBounds[-Lo,+Hi]
```

We know
* `Lo1 <:< Hi1`
* `Lo2 <:< Hi2`

if `T1 <:< T2` then `Lo2 <:< Lo1 <:< Hi1 <:< Hi2`
in other words, `T2` must contain `T1`