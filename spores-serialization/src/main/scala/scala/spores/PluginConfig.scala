package scala.spores

/** Defines the optional flags that spores-transitive-plugin takes. */
case class PluginConfig(forceTransitive: Boolean,
                        forceSerializableTypeParams: Boolean)
