package jsonp.decoder;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Token {

    public enum Type {
        ObjectBegin,
        ObjectEnd,
        ArrayBegin,
        ArrayEnd,
        Comma,
        Colon,
        Space,
        Number,
        String,
        Null,
        False,
        True
    }

    private static final Map<String, Type> TypeMapping = Map.ofEntries(
            new AbstractMap.SimpleEntry<>("{", Type.ObjectBegin),
            new AbstractMap.SimpleEntry<>("}", Type.ObjectEnd),
            new AbstractMap.SimpleEntry<>("[", Type.ArrayBegin),
            new AbstractMap.SimpleEntry<>("]", Type.ArrayEnd),
            new AbstractMap.SimpleEntry<>(",", Type.Comma),
            new AbstractMap.SimpleEntry<>(":", Type.Colon),
            new AbstractMap.SimpleEntry<>("space", Type.Space),
            new AbstractMap.SimpleEntry<>("number", Type.Number),
            new AbstractMap.SimpleEntry<>("string", Type.String),
            new AbstractMap.SimpleEntry<>("null", Type.Null),
            new AbstractMap.SimpleEntry<>("false", Type.False),
            new AbstractMap.SimpleEntry<>("true", Type.True));

    public final String content;
    public final Type tag;
    public final Boolean nothing;

    protected Token(List<Character> buffer, String tag) {
        this.nothing = false;
        this.tag = TypeMapping.get(tag);
        this.content = buffer.stream().map(String::valueOf).collect(Collectors.joining());
    }

    protected Token() {
        this.nothing = true;
        this.content = null;
        this.tag = null;
    }
}
