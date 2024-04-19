package jsonp.automata;

import java.util.Set;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jsonp.regex.*;

public class NFA {
    public final NFAState enter;
    public final Set<NFAState> states;
    public final Set<CharTerm> alphabet;
    public final List<TransitionRecord> table;

    public NFA(
            NFAState enter,
            Set<NFAState> states,
            Set<CharTerm> alphabet,
            List<TransitionRecord> table) {
        this.table = table;
        this.alphabet = alphabet;
        this.states = states;
        this.enter = enter;
    }

    /**
     * Build NFA from regex expression using Thompson algorithm.
     * 
     * @param term used for building NFA
     * @param enter state for current part of NFA
     * @param exit state for current part of NFA
     * @return built NFA
     * 
     * The exported final state should be listed at first position of list.
     * 
     * 0. e -> epsilon
     *         (enter) --epsilon--> (exit)
     *     1. e -> char
     *         (enter) --char--> (exit)
     *     2. e -> e1 e2
     *         (enter) --e1--> (exit_e1) --epsilon--> (enter_e2) --e2--> (exit)
     *     3. e -> e1 | e2
     *                --epsilon--> (enter_e1) --e1--> (exit_e1) --epsilon-->
     *         (enter)                                                       (exit)
     *                --epsilon--> (enter_e2) --e2--> (exit_e2) --epsilon-->
     *     4. e -> e1*
     *                                  -----epsilon----
     *                                 |                |
     *                                 v                |
     *         (enter) --epsilon--> (enter_e1) --e1--> (exit_e1) --epsilon--> (exit)
     *            |                                                              ^
     *            |                                                              |
     *             ---------------------------epsilon----------------------------
     * 
     *     5. (Extended repeat term) e -> e1{n, m}
     *                                    |-----------epislon--------------|  
     *                                    |        |--------epsilon--------|  
     *                                    |        |                       v
     *         enter -a-> s0 -a-> s1 -a-> s2 -a-> s3 -a-> s4 --epsilon--> exit
     *         ----------------------------^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
     *                 first n times                 last (m - n) times
     * 
     */
    public static NFA build(Term term, NFAState enter, NFAState exit) {
        // If term grouped, set it as an final state
        if (term.grouped) {
            exit.isFinal = true;
            exit.label = term.label;
        }

        switch (term) {
            case EpsilonTerm matcher:
                return new NFA(
                        enter,
                        new HashSet<>(Arrays.asList(enter, exit)),
                        new HashSet<>(),
                        List.of(new TransitionRecord(enter, exit, matcher)));
            case CharTerm matcher:
                return new NFA(
                        enter,
                        new HashSet<>(Arrays.asList(enter, exit)),
                        new HashSet<>(Arrays.asList(matcher)),
                        List.of(new TransitionRecord(enter, exit, matcher)));
            case ConcatTerm matcher: {
                NFAState exitE1 = new NFAState();
                NFAState enterE2 = new NFAState();
                NFA e1 = build(matcher.tl, enter, exitE1);
                NFA e2 = build(matcher.tr, enterE2, exit);
                List<TransitionRecord> table = Stream.concat(e1.table.stream(), e2.table.stream())
                        .collect(Collectors.toList());
                table.add(new TransitionRecord(exitE1, enterE2, Term.Epsilon));
                Set<NFAState> states = Stream.concat(e1.states.stream(), e2.states.stream())
                        .collect(Collectors.toCollection(HashSet::new));
                Set<CharTerm> alphabet = Stream.concat(e1.alphabet.stream(), e2.alphabet.stream())
                        .collect(Collectors.toCollection(HashSet::new));
                return new NFA(enter, states, alphabet, table);
            }
            case AlterTerm matcher: {
                NFAState enterE1 = new NFAState();
                NFAState exitE1 = new NFAState();
                NFAState enterE2 = new NFAState();
                NFAState exitE2 = new NFAState();
                NFA e1 = build(matcher.tl, enterE1, exitE1);
                NFA e2 = build(matcher.tr, enterE2, exitE2);
                List<TransitionRecord> table = Stream.concat(e1.table.stream(), e2.table.stream())
                        .collect(Collectors.toList());
                table.add(new TransitionRecord(enter, enterE1, Term.Epsilon));
                table.add(new TransitionRecord(enter, enterE2, Term.Epsilon));
                table.add(new TransitionRecord(exitE1, exit, Term.Epsilon));
                table.add(new TransitionRecord(exitE2, exit, Term.Epsilon));
                Set<NFAState> states = Stream.concat(e1.states.stream(), e2.states.stream())
                        .collect(Collectors.toCollection(HashSet::new));
                states.add(enter);
                states.add(exit);
                Set<CharTerm> alphabet = Stream.concat(e1.alphabet.stream(), e2.alphabet.stream())
                        .collect(Collectors.toCollection(HashSet::new));
                return new NFA(enter, states, alphabet, table);
            }
            case KleeneTerm matcher: {
                NFAState enterE1 = new NFAState();
                NFAState exitE1 = new NFAState();
                NFA e1 = build(matcher.t, enterE1, exitE1);
                List<TransitionRecord> table = new ArrayList<>(e1.table);
                table.add(new TransitionRecord(enter, enterE1, Term.Epsilon));
                table.add(new TransitionRecord(enter, exit, Term.Epsilon));
                table.add(new TransitionRecord(exitE1, enterE1, Term.Epsilon));
                table.add(new TransitionRecord(exitE1, exit, Term.Epsilon));
                Set<NFAState> states = new HashSet<>(e1.states);
                states.add(enter);
                states.add(exit);
                Set<CharTerm> alphabet = Set.copyOf(e1.alphabet);
                return new NFA(enter, states, alphabet, table);
            }

            // Extended RepeatTerm, see `regex.Term.repeat`
            case RepeatTerm matcher: {
                List<NFAState> chain = Arrays.asList(enter);
                Set<NFAState> states = new HashSet<>(Arrays.asList(exit));
                Set<CharTerm> alphabet = new HashSet<>();
                List<TransitionRecord> table = new ArrayList<>();

                // Build states and merge NFAs
                for (Integer index = 0; index < matcher.m; index++) {
                    NFAState enterE = chain.getLast();
                    NFAState exitE = new NFAState();
                    chain.add(exitE);
                    NFA e = build(matcher.t, enterE, exitE);
                    table.addAll(e.table);
                    states.addAll(e.states);
                    alphabet = e.alphabet;
                }

                // Add connection for last n states to exit states
                for (NFAState state : chain.subList(matcher.n, chain.size())) {
                    table.add(new TransitionRecord(state, exit, Term.Epsilon));
                }
                return new NFA(enter, states, alphabet, table);
            }
            default:
                throw new RuntimeException(String.format("unknown term type %s", term.getClass().getName()));
        }
    }

    /**
     * Build NFA from given term.
     * @param term used for building NFA
     * @return built NFA
     */
    public static NFA build(Term term) {
        NFAState enter = new NFAState();
        NFAState exit = new NFAState();
        return build(term, enter, exit);
    }

    /**
     * Get all epsilon transition reachable states from current state set.
     * @param currentStates contains all current states
     * @return all reachable states from current states with epsilon transition
     */
    public Set<NFAState> reachable(Set<NFAState> currentStates) {
        return this.table.stream()
                .filter((record) -> {
                    return currentStates.contains(record.from) &&
                            record.matcher instanceof EpsilonTerm;
                })
                .map(record -> record.to)
                .collect(Collectors.toCollection(HashSet::new));
    }

    /**
     * Get all char term transition reachble states from current state set.
     * @param currentStates contains all current states
     * @param matcher is target char term
     * @return all reachable states from current states with char term transition
     */
    public Set<NFAState> reachable(Set<NFAState> currentStates, CharTerm matcher) {
        return this.table.stream()
                .filter((record) -> {
                    return currentStates.contains(record.from) &&
                            record.matcher == matcher;
                })
                .map(record -> record.to)
                .collect(Collectors.toCollection(HashSet::new));
    }

    /**
     * Get all char term transition reachble states from current state set.
     * @param currentStates contains all current states
     * @param matcher is target char term
     * @return all reachable states from current states with char term transition
     */
    public Set<NFAState> reachable(Set<NFAState> currentStates, Character matcher) {
        return this.table.stream()
                .filter((record) -> {
                    return currentStates.contains(record.from) &&
                            record.matcher.accept(matcher);
                })
                .map(record -> record.to)
                .collect(Collectors.toCollection(HashSet::new));
    }

    /**
     * Get epsilon closure for current state set.
     * 
     * Using BFS for searching epsilon-closure of a set of state.
     * 
     * @param currentStates contains all current states
     * @return current states epsilon closure
     */
    public Set<NFAState> epsilonClosure(Set<NFAState> currentStates) {
        Set<NFAState> visited = new HashSet<>();
        List<Set<NFAState>> unvisited = new ArrayList<>();
        unvisited.add(states);
        while (!unvisited.isEmpty()) {
            Set<NFAState> currentClosure = unvisited.removeLast();
            visited.addAll(currentClosure);
            Set<NFAState> reachableStatesFromCurrentClosure = this.reachable(currentClosure);
            reachableStatesFromCurrentClosure.removeAll(visited);
            if (!reachableStatesFromCurrentClosure.isEmpty())
                unvisited.add(reachableStatesFromCurrentClosure);
        }
        return visited;
    }

    /**
     * Get all final states set.
     * @return all final states
     */
    public Set<NFAState> finalStates() {
        Set<NFAState> finals = new HashSet<>();
        for (NFAState state : this.states)
            if (state.isFinal)
                finals.add(state);
        return finals;
    }

    /**
     * Build some iterable things to string.
     * @param <T> Type parameter for Iterable<T> generics
     * @param delimeter used for combining string
     * @param sequence of elements
     * @return combind string
     */
    private <T> String join(String delimeter, Iterable<T> sequence) {
        StringBuilder builder = new StringBuilder();
        Integer index = 0;
        for (T element : sequence) {
            if (index != 0)
                builder.append(delimeter);
            builder.append(element);
            index++;
        }
        return builder.toString();
    }

    /**
     * Draw current NFA.
     * @return NFA string for drawing online
     */
    public String draw() {
        String formatter = String.join("\n%s\n",
                "#states",
                "#initial",
                "#accepting",
                "#alphabet",
                "#transitions\n%s");
        StringBuilder transitions = new StringBuilder();
        for (TransitionRecord record : this.table) {
            String name;
            if (record.matcher instanceof EpsilonTerm)
                name = "$";
            else
                name = record.matcher.toString();
            transitions.append(String.format("%s:%s>%s", record.from, name, record.to));
            transitions.append("\n");
        }
        return String.format(
                formatter,
                this.join("\n", this.states),
                this.enter,
                this.join("\n", this.finalStates()),
                this.join("\n", this.alphabet),
                transitions);
    }
}
