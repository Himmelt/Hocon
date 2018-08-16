package org.soraworld.hocon.node;

import org.soraworld.hocon.serializer.TypeSerializer;
import org.soraworld.hocon.serializer.TypeSerializerCollection;
import org.soraworld.hocon.serializer.TypeSerializers;

import javax.annotation.Nonnull;
import java.util.function.Function;
import java.util.regex.Pattern;

public class Options {

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

    private static final Options defaults = new Options(true);

    private Options(boolean seal) {
        this.seal = seal;
    }

    public static Options defaults() {
        return defaults;
    }

    public static Options newOptions() {
        return new Options(false);
    }

    public TypeSerializerCollection getSerializers() {
        return TypeSerializers.getDefaultSerializers();
    }

    public <T> TypeSerializerCollection registerType(@Nonnull TypeSerializer<? super T> serializer) {
        return getSerializers().registerType(serializer);
    }

    public void setTranslator(Function<String, String> function) {
        if (function != null) translator = function;
    }

    public Function<String, String> getTranslator() {
        return translator;
    }

}
