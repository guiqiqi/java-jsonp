package jsonp.regex;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class CharTerm extends Term implements TransitionableTerm {
    public final String name;
    public final Set<Character> form;

    public CharTerm(String form) {
        this(form, form);
    }

    public CharTerm(String form, String name) {
        this.form = form.chars()
                .mapToObj(e -> (char) e)
                .collect(Collectors.toCollection(HashSet::new));
        this.name = name;
    }

    /**
     * Check if current char term accept given char.
     */
    @Override
    public Boolean accept(Character c) {
        return this.form.contains(c);
    }

    @Override
    public String toString() {
        return this.name;
    }
}
