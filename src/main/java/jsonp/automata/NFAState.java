package jsonp.automata;

public class NFAState {

    private static Integer Counter = 0;
    public final Integer index;

    // Info for final state
    public boolean isFinal = false;
    public String label = null;

    public NFAState() {
        this.index = NFAState.Counter;
        NFAState.Counter += 1;
    }

    @Override
    public String toString() {
        return String.format("s%d", this.index);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof NFAState)
            return ((NFAState) obj).index == this.index;
        return false;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(this.index);
    }
}
