package jsonp;

import jsonp.decoder.Decoder;

public class App {
    public static void main(String[] args) {
        Decoder decoder = new Decoder();
        System.out.println(decoder.lexer.nfa.draw());
    }
}
