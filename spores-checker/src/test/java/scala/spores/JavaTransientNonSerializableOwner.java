package scala.spores;

import java.io.Serializable;

/** This file is only used for testing purposes. */
public class JavaTransientNonSerializableOwner implements Serializable {
    public transient Object shouldBeIgnored1;
    public Object member;
    public transient Integer shouldBeIgnored2;

    JavaTransientNonSerializableOwner() {

    }
}
