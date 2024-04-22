package jsonp.decoder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JSON LL(1) grammer parser using recursive descent.
 * 
 * Grammer:
 * 
 * obj -> { members }
 * members -> pair members' | eps
 * members' -> , pair members' | eps
 * pair -> string : value
 * array -> [ elem ]
 * elem -> value elem' | eps
 * elem' -> , value elem' | eps
 * value -> obj | array | number | string | true | false | null
 */
public class Parser {
    private List<Token> tokens;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public Object parse() {
        // Token token = this.tokens.removeFirst();
        // if (token.tag == Token.Type.ObjectBegin)
        //     return object();
        // if (token.tag == Token.Type.ArrayBegin)
        //     return array();
        return null;
    }
}
