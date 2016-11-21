package scala.spores.hierarchy;

public final class JavaPoisonedBar extends JavaFoo {
    private transient Object name;

    public JavaPoisonedBar(String name) {
        this.name = name;
    }
}
