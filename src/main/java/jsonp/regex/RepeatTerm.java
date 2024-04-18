package jsonp.regex;

public class RepeatTerm extends Term {
    public final Term t;
    public final Integer n;
    public final Integer m;

    public RepeatTerm(Term t, Integer n, Integer m) {
        this.t = t;
        this.n = n;
        this.m = m;
    }

    @Override
    public String toString() {
        return String.format("(%s){%d,%d}", this.t, this.n, this.m);
    }
}
