package jsonp.regex;

public class KleeneTerm extends Term {
    public final Term t;

    public KleeneTerm(Term t) {
        this.t = t;
    }

    @Override
    public String toString() {
        return String.format("%s*", this.t);
    }
}
