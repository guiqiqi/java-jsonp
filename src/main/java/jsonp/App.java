package jsonp;

import java.util.List;

import jsonp.automata.NFA;
import jsonp.regex.Term;

public class App {
    public static void main(String[] args) {
        Term term = Term.concat(List.of(
            Term.string("\""),
            Term.repeat(Term.Any),
            Term.string("\"")
        ));
        NFA nfa = NFA.build(term);
    }
}
