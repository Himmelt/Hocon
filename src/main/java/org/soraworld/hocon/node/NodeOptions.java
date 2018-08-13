package org.soraworld.hocon.node;

import org.soraworld.hocon.serializer.TypeSerializerCollection;
import org.soraworld.hocon.serializer.TypeSerializers;

import java.util.function.Function;
import java.util.regex.Pattern;

public class NodeOptions {

    public final String COMMENT_HEAD = "# ";
    public final String TAB_SPACE = "    ";
    public final String EQUAL = " = ";
    public final String EQUAL_NODE = " = {";
    public final String EQUAL_LIST = " = [";
    public final char END_NODE = '}';
    public final char END_LIST = ']';
    public final String LINE = "-------------------------------------";

    public int INDENT_SIZE = 2;

    public final Pattern ILLEGAL = Pattern.compile(".*[\":=,+?`!@#$^&*{}\\[\\]\\\\].*");

    private final boolean seal;

    private Function<String, String> translator = key -> key;

    private static final NodeOptions defaults = new NodeOptions(true);

    public NodeOptions(boolean seal) {
        this.seal = seal;
    }

    public static NodeOptions defaults() {
        return defaults;
    }

    public static NodeOptions newOptions() {
        return new NodeOptions(false);
    }

    public TypeSerializerCollection getSerializers() {
        return TypeSerializers.getDefaultSerializers();
    }

    public Function<String, String> getTranslator() {
        return translator;
    }

}
