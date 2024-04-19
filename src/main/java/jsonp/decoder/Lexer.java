package jsonp.decoder;

import java.util.HashSet;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import jsonp.regex.Term;
import jsonp.automata.*;

/**
 * Build an empty automata for Lexer with just a single entry state and no transitions.
 * Then all patterns will be compiled as nfa so it could be added into this automata.
 * 
 * With structure like this:
 *            --epsilon--> term1
 *     (init) --epsilon--> term2
 *             --epsilon--> term3
 *            ...
 * 
 * Each pattern will have an final state for indicate output.
 * Then this NFA will be parsed into an equivalant DFA automata.
 */
public class Lexer {
    public final NFA nfa;
    private NFAState enter;
    private Set<NFAState> curretState;
    private List<Character> buffer;

    public Lexer(List<Term> terms) {
        this.buffer = new LinkedList<>();
        this.curretState = new HashSet<>();

        // If single term passed into lexer, just use it as NFA
        if (terms.size() == 1) {
            this.nfa = NFA.build(terms.getLast());
            this.enter = this.nfa.enter;
            this.reset();
            return;
        }

        // For multiple branches add them all
        this.enter = new NFAState();
        this.nfa = new NFA(this.enter, new HashSet<>(), new HashSet<>(), new ArrayList<>());
        this.nfa.states.add(this.enter);
        for (Term term : terms) {
            NFA branch = NFA.build(term);
            this.nfa.states.addAll(branch.states);
            this.nfa.alphabet.addAll(branch.alphabet);
            this.nfa.table.addAll(branch.table);
            this.nfa.table.add(new TransitionRecord(this.enter, branch.enter, Term.Epsilon));
        }
        this.reset();
    }

    /**
     * Clear current NFA state and read buffer.
     */
    public void reset() {
        this.buffer.clear();
        this.curretState.clear();
        this.curretState.add(this.enter);
    }
}