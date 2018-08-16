package org.soraworld.hocon.node;

import org.soraworld.hocon.serializer.TypeSerializer;
import org.soraworld.hocon.serializer.TypeSerializerCollection;
import org.soraworld.hocon.serializer.TypeSerializers;

import javax.annotation.Nonnull;
import java.util.function.Function;

public class Options {

    private int indent = 2;
    private String headLine = "---------------------------------------------";
    private Function<String, String> translator = key -> key;

    private final boolean seal;
    private static final Options defaults = new Options(true);

    private Options(boolean seal) {
        this.seal = seal;
    }

    public static Options defaults() {
        return defaults;
    }

    public static Options build() {
        return new Options(false);
    }

    public TypeSerializerCollection getSerializers() {
        return TypeSerializers.getDefaultSerializers();
    }

    public <T> TypeSerializerCollection registerType(@Nonnull TypeSerializer<? super T> serializer) {
        return getSerializers().registerType(serializer);
    }


    public int getIndent() {
        return indent;
    }

    public void setIndent(int indent) {
        if (!seal) this.indent = indent;
    }

    public String getHeadLine() {
        return headLine == null ? "" : headLine;
    }

    public void setHeadLine(String headLine) {
        if (!seal) this.headLine = headLine;
    }

    public Function<String, String> getTranslator() {
        return translator;
    }

    public void setTranslator(Function<String, String> function) {
        if (!seal && function != null) translator = function;
    }
}
