package jsonp.decoder;

import java.util.LinkedList;
import java.util.List;

import jsonp.regex.Term;

public class Decoder {
    public final Lexer lexer;

    public Decoder() {
        Term True = Term.string("true").group("true");
        Term False = Term.string("false").group("false");
        Term Null = Term.string("null").group("null");
        Term String = Term.concat(List.of(
                Term.string("\""),
                Term.repeat(Term.Any),
                Term.string("\""))).group("string");
        Term Number = Term.concat(List.of(
                Term.optional(Term.string("-")),
                Term.alter(List.of(
                        Term.string("0"),
                        Term.srange("123456789", "[1-9]"))),
                Term.repeat(Term.Digits),
                Term.optional(Term.concat(List.of(
                        Term.string("."),
                        Term.plus(Term.Digits)))),
                Term.optional(Term.concat(List.of(
                        Term.srange("eE", "[eE]"),
                        Term.optional(Term.srange("+-", "[+-]")),
                        Term.plus(Term.Digits))))))
                .group("number");
        Term Space = Term.srange(" \t\r\n", "Space").group("space");
        Term Colon = Term.string(":").group(":");
        Term Comma = Term.string(",").group(",");
        Term CurlyLeft = Term.string("{").group("{");
        Term CurlyRight = Term.string("}").group("}");
        Term SquareLeft = Term.string("[").group("[");
        Term SquareRight = Term.string("]").group("]");
        this.lexer = new Lexer(List.of(
                True, False, Null, String, Number,
                Space, Comma, Colon, CurlyLeft, CurlyRight, SquareLeft, SquareRight));
    }

    public List<Token> tokenize(String jsonText) {
        List<Token> records = new LinkedList<>();
        for (Character c : jsonText.toCharArray()) {
            Token record = this.lexer.read(c);
            if (!record.nothing)
                records.add(record);
        }
        Token record = this.lexer.read();
        records.add(record);
        return records;
    }
}
