package jsonp.automata;

import jsonp.regex.*;

/**
 * Transition record contains source state and destination state and matcher between them.
 */
public class TransitionRecord {
    public final NFAState from;
    public final NFAState to;
    public final TransitionableTerm matcher;

    public TransitionRecord(NFAState from, NFAState to, TransitionableTerm matcher) {
        this.from = from;
        this.to = to;
        this.matcher = matcher;
    }
}