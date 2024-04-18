package jsonp.regex;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class CharTerm extends Term {
    public final Set<Character> accept;
    public final String name;

    public CharTerm(String form) {
        this(form, form);
    }

    public CharTerm(String form, String name) {
        this.accept = form.chars()
                .mapToObj(e -> (char) e)
                .collect(Collectors.toCollection(HashSet::new));
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
