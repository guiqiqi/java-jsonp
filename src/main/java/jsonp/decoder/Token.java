package jsonp.decoder;

import java.util.List;
import java.util.stream.Collectors;

public class Token {
    public final String content;
    public final String tag;
    public final Boolean nothing;

    protected Token(List<Character> buffer, String tag) {
        this.nothing = false;
        this.tag = tag;
        this.content = buffer.stream().map(String::valueOf).collect(Collectors.joining());
    }

    protected Token() {
        this.nothing = true;
        this.content = null;
        this.tag = null;
    }
}
