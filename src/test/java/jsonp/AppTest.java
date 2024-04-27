package jsonp;

import java.util.List;
import java.util.Map;

import jsonp.decoder.JsonObject;
import jsonp.encoder.Encoder;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest extends TestCase {
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */

    private final jsonp.decoder.Decoder decoder = new jsonp.decoder.Decoder();

    public AppTest(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(AppTest.class);
    }

    public void testIntegerNumber() {
        Integer a = 123456;
        String encoded = Encoder.encode(a);
        Integer decoded = decoder.decode(encoded).as();
        assertEquals(a, decoded);
    }

    public void testDoubleNumber() {
        Double a = 3.14;
        String encoded = Encoder.encode(a);
        Double decoded = decoder.decode(encoded).as();
        assertEquals(decoded, a);
    }

    public void testDoubleEFormatNumber() {
        final Double a = -12.3e-1;
        String encoded = "-12.3e-1";
        Double decoded = decoder.decode(encoded).as();
        assertEquals(a, decoded);
    }

    public void testSingleString() {
        String str = "This is a test";
        String encoded = Encoder.encode(str);
        String decoded = decoder.decode(encoded).as();
        assertEquals(decoded, str);
    }

    public void testEmptyList() {
        List<Integer> list = List.of();
        String encoded = Encoder.encode(list);
        List<Integer> decoded = decoder.decode(encoded).as();
        assertEquals(decoded.size(), 0);
    }

    public void testEmptyObject() {
        Map<String, Integer> mapping = Map.of();
        String encoded = Encoder.encode(mapping);
        Map<String, Integer> decoded = decoder.decode(encoded).as();
        assertEquals(mapping.size(), decoded.size());
    }

    public void testList() {
        List<Integer> list = List.of(1, 2, 3);
        String encoded = Encoder.encode(list);
        List<?> decoded = decoder.decode(encoded).as();
        assertEquals(list.size(), decoded.size());
    }

    public void testListObject() {
        List<Map<String, Integer>> data = List.of(Map.of("A test", 1, "B", 2), Map.of("C", 3));
        String encoded = Encoder.encode(data);
        List<JsonObject> decoded = decoder.decode(encoded).as();
        assertEquals(data.size(), decoded.size());
        Map<String, JsonObject> content = decoded.get(1).as();
        assertEquals(data.get(1).get("C"), content.get("C").as());
    }
}
