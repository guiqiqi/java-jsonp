package jsonp;

import jsonp.decoder.Decoder;
import jsonp.decoder.Token;

public class App {
    public static void main(String[] args) {
        Decoder decoder = new Decoder();
        for (Token token : decoder.tokenize("{\"ABC\": [12e-3,  -123.456], \"Empty\": \"\", \"DEF\": \"This is a test.:)\"}")) {
            System.out.println(String.format("(%s) - %s", token.content, token.tag));
        }
    }
}
