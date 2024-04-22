package jsonp;

import jsonp.encoder.Encoder;

import java.util.List;
import java.util.Map;

import jsonp.decoder.Decoder;
import jsonp.decoder.Token;

public class App {
    public static void main(String[] args) {
        List<Map<String, Integer>> data = List.of(Map.of("A test", 1, "B", 2), Map.of("C", 3, "D", 4));
        String encoded = Encoder.encode(data);
        Decoder decoder = new Decoder();
        System.out.println(encoded);
        for (Token token : decoder.tokenize(encoded)) {
            System.out.println(String.format("(%s) - %s", token.content, token.tag));
        }
    }
}
