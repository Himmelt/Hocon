package org.soraworld.hocon.serializer;

import org.soraworld.hocon.exception.DeserializeException;
import org.soraworld.hocon.exception.NotBaseException;
import org.soraworld.hocon.exception.NullValueException;
import org.soraworld.hocon.node.Node;
import org.soraworld.hocon.node.NodeBase;
import org.soraworld.hocon.node.Options;

import javax.annotation.Nonnull;
import java.lang.reflect.Type;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class PatternSerializer implements TypeSerializer<Pattern> {
    public Pattern deserialize(@Nonnull Type type, @Nonnull Node node) throws NotBaseException, NullValueException, DeserializeException {
        if (node instanceof NodeBase) {
            String string = ((NodeBase) node).getString();
            if (string == null) throw new NullValueException(getRegType());
            try {
                return Pattern.compile(string);
            } catch (PatternSyntaxException e) {
                throw new DeserializeException(e);
            }
        }
        throw new NotBaseException(getRegType());
    }

    public Node serialize(@Nonnull Type type, Pattern value, @Nonnull Options options) {
        return new NodeBase(options, value == null ? null : value.pattern(), false);
    }

    @Nonnull
    public Type getRegType() {
        return Pattern.class;
    }
}
