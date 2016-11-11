package scala.spores;

import java.io.Serializable;

/** This file is only used for testing purposes. */
public class JavaTransientNonSerializableOwner2 implements Serializable {
    public transient Object shouldBeIgnored1;
    public JavaTransientNonSerializableMember member;

    JavaTransientNonSerializableOwner2() {

    }
}
