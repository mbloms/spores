package scala.spores;

import java.io.Serializable;

/** This file is only used for testing purposes. */
public class JavaTransientMemberHolder implements Serializable {
    public String member;
    public transient Integer shouldBeIgnored1;
    public transient Integer shouldBeIgnored2;
}
