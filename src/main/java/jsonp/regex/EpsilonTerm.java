package jsonp.regex;

import java.util.HashSet;
import java.util.Set;

public class EpsilonTerm extends Term {
    public final static Set<Character> accept = new HashSet<>();

    @Override
    public String toString() {
        return "";
    }
}
