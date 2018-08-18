package org.soraworld.hocon.node;

import org.soraworld.hocon.serializer.TypeSerializers;
import org.soraworld.hocon.serializer.TypeSerializer;

import javax.annotation.Nonnull;
import java.util.function.Function;

public class Options {

    private boolean seal;
    private int indent = 2;
    private boolean debug = false;
    private String headLine = "---------------------------------------------";
    private Function<String, String> translator = key -> key;
    private final TypeSerializers serializers = TypeSerializers.build();

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

    public void seal() {
        this.seal = true;
    }

    public int getIndent() {
        return indent;
    }

    public void setIndent(int indent) {
        if (!seal) this.indent = indent;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        if (!seal) this.debug = debug;
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

    public TypeSerializers getSerializers() {
        return serializers;
    }

    public <T> void registerType(@Nonnull TypeSerializer<? super T> serializer) {
        if (!seal) serializers.registerType(serializer);
    }
}
