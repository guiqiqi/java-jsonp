package jsonp;

import jsonp.encoder.Encoder;

import java.util.List;
import java.util.Map;

import jsonp.decoder.Decoder;
import jsonp.decoder.JsonObject;

public class App {
    public static void main(String[] args) {
        List<Map<String, Integer>> data = List.of(Map.of("A test", 1, "B", 2), Map.of("C", 3));
        System.out.println("Raw: " + data);
        String encoded = Encoder.encode(data);
        Decoder decoder = new Decoder();
        System.out.println("Encoded: " + encoded);
        List<JsonObject> decoded = decoder.decode(encoded).as();
        System.out.println("Decoded: " + decoded);
    }
}
