package jsonp.regex;

public class AlterTerm extends Term {
    public final Term tl;
    public final Term tr;

    public AlterTerm(Term tl, Term tr) {
        this.tl = tl;
        this.tr = tr;
    }

    @Override
    public String toString() {
        return String.format("%s|%s", this.tl, this.tr);
    }
}
