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

    /**
     * Move current state set driven by input char.
     * 
     * For each iteration of content given a char:
     *     0. current state set equals epsilon-closure of current state: T = epsilon-closure(state)
     *     1. enumerate all states which reachble from current state set T with given char: M = move(T, x)
     *     2. current state set updates to epsilon-closure of M: T = epsilon-closure(M)
     * 
     * While calculated M as empty set, means no transition allowed from current state with given char, then:
     *     0. if current closure contains a final state, means we get an output
     *     1. if current closure without a final state, raise a LexerError.
     * 
     * If char not given in, try to yield current buffer.
     * 
     * If multiple final states matched, we select the one with smallest index, 
     * which means the rule have higher priority.
     * 
     * @param c represents char reading in
     * @return lexer record contains tag and joint buffer
     */
    public Token read(Character c) {
        Set<NFAState> currentStateClosure = this.nfa.epsilonClosure(this.curretState);
        List<NFAState> reachedFinalStates = currentStateClosure.stream()
                .filter(state -> state.isFinal)
                .sorted((stateA, stateB) -> stateA.index.compareTo(stateB.index))
                .toList();
        Set<NFAState> nextStepStates = this.nfa.reachable(currentStateClosure, c);

        // If we could NOT reach any state from current closure - means we need to check returning
        if (nextStepStates.isEmpty()) {
            if (reachedFinalStates.isEmpty())
                throw new InvalidToken(String.format("invalid token %c", c));

            Token record = new Token(this.buffer, reachedFinalStates.getFirst().label);
            this.buffer.clear();
            this.buffer.add(c);
            this.curretState = this.nfa.reachable(this.nfa.epsilonClosure(Set.of(this.enter)), c);
            return record;
        }

        // Otherwise we need to move to nextstep's epsilon-closure and return nothing
        this.curretState = this.nfa.epsilonClosure(nextStepStates);
        this.buffer.add(c);
        return new Token();
    }

    public Token read() {
        Set<NFAState> currentStateClosure = this.nfa.epsilonClosure(this.curretState);
        List<NFAState> reachedFinalStates = currentStateClosure.stream()
                .filter(state -> state.isFinal)
                .sorted((stateA, stateB) -> stateA.index.compareTo(stateB.index))
                .toList();
        Token record = new Token(this.buffer, reachedFinalStates.getFirst().label);
        if (reachedFinalStates.isEmpty())
            throw new InvalidToken(String.format("invalid token %s", record.content));
        return record;
    }
}