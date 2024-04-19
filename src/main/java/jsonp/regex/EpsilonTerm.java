package jsonp.regex;

public class EpsilonTerm extends Term implements TransitionableTerm {

    /**
     * Epsilon term should not accept any char so make it as a empty set.
     */
    @Override
    public Boolean accept(Character c) {
        return false;
    }

    @Override
    public String toString() {
        return "";
    }
}
