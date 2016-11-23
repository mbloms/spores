package scala.spores

/* This file is part of spores-serialization, but is temporarily placed here
 * so that it can be found in the symbol table for the annotation checks. */

/** `AssumeClosed` is an annotation for those that depend on
  * compiled binaries. For third-party software, class hierarchy's
  * information is not enough for a full transitive check. By using
  * this annotation, the users *decides to trust* that the third-party
  * does not define non-serializable classes in any form or shape. */
class assumeClosed extends scala.annotation.StaticAnnotation

