package jsonp.decoder;

import java.util.List;
import java.util.Map;

public class JsonObject {

    public enum Type {
        Object, Array,
        String, Number, Boolean, Null
    }

    public final Type type;
    public Object object = null;

    public JsonObject(Token token) {
        switch (token.tag) {
            case Token.Type.String:
                this.type = Type.String;
                this.object = parseString(token);
                break;
            case Token.Type.Number:
                this.type = Type.Number;
                this.object = parseNumber(token);
                break;
            case Token.Type.True:
                this.type = Type.Boolean;
                this.object = true;
                break;
            case Token.Type.False:
                this.type = Type.Boolean;
                this.object = false;
                break;
            case Token.Type.Null:
                this.type = Type.Null;
                this.object = null;
                break;
            default:
                throw new InvalidToken(String.format("%s is cannot be parsed as value type", token.content));
        }
    }

    public JsonObject(List<JsonObject> array) {
        this.type = Type.Array;
        this.object = array;
    }

    public JsonObject(Map<String, JsonObject> object) {
        this.type = Type.Object;
        this.object = object;
    }

    /**
     * Parse a token to number.
     * @param token with Number type
     * @return Number value with Integer or Double type
     */
    public static Number parseNumber(Token token) {
        if (token.content.contains(".") || token.content.contains("E") || token.content.contains("e"))
            return Double.parseDouble(token.content);
        return Integer.parseInt(token.content);
    }

    /**
     * Parse a token to string.
     * @param token string type with double quote ""
     * @return unquoted string
     */
    public static String parseString(Token token) {
        return token.content.substring(1, token.content.length() - 1);
    }

    /**
     * Check if token is value type (which means String, Number, True, False, Null)
     * @param token going to check for value type
     * @return if token is value type
     */
    public static Boolean assignable(Token token) {
        return (token.tag == Token.Type.String || token.tag == Token.Type.Number ||
                token.tag == Token.Type.True || token.tag == Token.Type.False ||
                token.tag == Token.Type.Null) ? true : false;
    }

    /**
     * Convert json object to stirng by type of each filed.
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (this.type == Type.Object) {
            Integer index = 0;
            builder.append("{");
            Map<String, JsonObject> converted = this.as();
            for (String key : converted.keySet()) {
                builder.append(String.format(
                        "%s=%s", key,
                        converted.get(key).toString()));
                if (index++ < converted.size() - 1)
                    builder.append(", ");
            }
            builder.append("}");
            return builder.toString();
        }
        if (this.type == Type.Array) {
            builder.append("[");
            Integer index = 0;
            List<JsonObject> converted = this.as();
            for (JsonObject element : converted) {
                builder.append(element.toString());
                if (index++ < converted.size() - 1)
                    builder.append(", ");
            }
            builder.append("]");
            return builder.toString();
        }
        return this.object.toString();
    }

    @SuppressWarnings("unchecked")
    public <T> T as() {
        return (T) this.object;
    }
}
