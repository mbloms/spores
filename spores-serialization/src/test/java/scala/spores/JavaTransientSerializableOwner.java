package scala.spores;

import java.io.Serializable;

/** This file is only used for testing purposes. */
public final class JavaTransientSerializableOwner implements Serializable {
    public String member;
    public transient Object shouldBeIgnored1;
    public transient Integer shouldBeIgnored2;
}
