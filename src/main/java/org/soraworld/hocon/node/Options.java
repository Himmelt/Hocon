package org.soraworld.hocon.node;

import org.soraworld.hocon.serializer.TypeSerializer;
import org.soraworld.hocon.serializer.TypeSerializers;

import javax.annotation.Nonnull;
import java.lang.reflect.Type;
import java.util.function.Function;

public class Options {

    private boolean seal;
    private int deep = 0;
    private int indent = 2;
    private boolean debug = false;
    private String headLine = "---------------------------------------------";
    private Function<String, String> translator = key -> key;
    private final TypeSerializers[] serializers = new TypeSerializers[5];

    private static final Options defaults = new Options(true);

    private Options(boolean seal) {
        this.seal = seal;
        serializers[0] = TypeSerializers.build();
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

    public TypeSerializer getSerializer(@Nonnull Type type) {
        return serializers[deep].get(type);
    }

    public <T> void registerType(@Nonnull TypeSerializer<? super T> serializer) {
        if (!seal) serializers[0].registerType(serializer);
    }

    public <T> void registerType(@Nonnull TypeSerializer<? super T> serializer, int level) {
        if (!seal && level >= 0) {
            level = level > 4 ? 4 : level;
            if (level > deep) {
                deep = level;
                for (int i = 1; i <= deep; i++) {
                    if (serializers[i] == null) serializers[i] = serializers[i - 1].newChild();
                }
                serializers[deep].registerType(serializer);
            } else serializers[level].registerType(serializer);
        }
    }
}
