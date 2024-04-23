package jsonp.decoder;

import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.LinkedList;

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
    private Token currentToken;
    private Integer index;

    public Parser(List<Token> tokens) {
        this.tokens = tokens.stream().filter(token -> token.tag != Token.Type.Space).toList();
        this.index = 0;
    }

    /**
     * Parse tokens to object.
     * @return parsed json object
     */
    private Map<String, JsonObject> object() {
        Map<String, JsonObject> map = new HashMap<>();
        return members(map);
    }

    /**
     * Parse tokens to member into object.
     * @param map contains all parsed json object
     * @return parsed json object
     */
    private Map<String, JsonObject> members(Map<String, JsonObject> map) {
        Token token = this.scan();
        if (token.tag == Token.Type.ObjectEnd)
            return map;
        return member(pair(map));
    }

    /**
     * Parse single token into object.
     * @param map contains all parsed json object
     * @return parsed json object
     */
    private Map<String, JsonObject> member(Map<String, JsonObject> map) {
        Token token = this.scan();
        if (token.tag == Token.Type.Comma) {
            this.scan();
            return member(pair(map));
        }
        if (token.tag == Token.Type.ObjectEnd) {
            return map;
        }
        throw new InvalidToken(String.format("invalid json object %s", token.content));
    }

    /**
     * Parse single pair into object.
     * @param map contains all parsed json object
     * @return parsed json object
     */
    private Map<String, JsonObject> pair(Map<String, JsonObject> map) {
        Token token = this.peek();
        if (token.tag == Token.Type.String) {
            String key = JsonObject.parseString(token);
            if (key.length() == 0) {
                throw new InvalidToken("json object key cannot be empty string");
            }
            token = this.scan();
            if (token.tag == Token.Type.Colon) {
                this.scan();
                map.put(key, value());
                return map;
            }
            throw new InvalidToken(String.format("expected colon in json object but got %s", token.content));
        }
        throw new InvalidToken(String.format("json object key should be stirng but not %s", token.content));
    }

    /**
     * Parse tokens to value array.
     * @return parsed token array
     */
    private List<JsonObject> array() {
        List<JsonObject> array = new LinkedList<>();
        return elems(array);
    }

    /**
     * Parse array from tokens.
     * @return parsed token array
     */
    private List<JsonObject> elems(List<JsonObject> array) {
        Token token = this.scan();
        if (token.tag == Token.Type.ArrayEnd)
            return array;
        array.add(value());
        return elem(array);
    }

    /**
     * Parse more element to array from tokens.
     * @return parsed token array
     */
    private List<JsonObject> elem(List<JsonObject> array) {
        Token token = this.scan();
        if (token.tag == Token.Type.Comma) {
            this.scan();
            array.add(value());
            return elem(array);
        }
        if (token.tag == Token.Type.ArrayEnd) {
            return array;
        }
        throw new InvalidToken(String.format("%s cannot be parsed as list", token.content));
    }

    /**
     * Parse token into Value type.
     * @return value parsed from token
     */
    private JsonObject value() {
        Token token = this.peek();
        if (JsonObject.assignable(token))
            return new JsonObject(token);
        if (token.tag == Token.Type.ObjectBegin)
            return new JsonObject(object());
        if (token.tag == Token.Type.ArrayBegin)
            return new JsonObject(array());
        throw new InvalidToken(String.format("%s cannot be parsed as value", token.content));
    }

    /**
     * Scan next token and return it.
     * @return next token in token list
     */
    private Token scan() {
        this.currentToken = this.tokens.get(this.index++);
        return this.currentToken;
    }

    /**
     * Peek current token and return it.
     * @return current scanning token
     */
    private Token peek() {
        return this.currentToken;
    }

    /**
     * Parse json object.
     * @return parsed json object
     */
    public JsonObject parse() {
        Token token = this.scan();
        if (token.tag == Token.Type.ObjectBegin)
            return new JsonObject(object());
        if (token.tag == Token.Type.ArrayBegin)
            return new JsonObject(array());
        if (JsonObject.assignable(token))
            return value();
        throw new InvalidToken(String.format("invalid json object", token.content));
    }
}
