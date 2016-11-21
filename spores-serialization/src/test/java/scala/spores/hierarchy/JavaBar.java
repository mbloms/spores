package scala.spores.hierarchy;

public final class JavaBar extends JavaFoo {
    private String name;

    public JavaBar(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
