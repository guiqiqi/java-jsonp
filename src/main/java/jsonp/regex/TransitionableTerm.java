package jsonp.regex;

/**
 * CharTerm and EpsilonTerm could be used in NFA Transition matching.
 */
public interface TransitionableTerm {
    public Boolean accept(Character c);
}
