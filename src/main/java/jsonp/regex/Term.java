package jsonp.regex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Term {
    public Boolean grouped = false;
    public String label = null;

    /*
     * Set current term as a grouped regex term.
     */
    public Term group(String tag) {
        this.grouped = true;
        this.label = tag;
        return this;
    }

    /*
     * Concat multiple terms together, using as c1c2c3...
     */
    public static Term concat(List<Term> terms) {
        if (terms.size() == 1)
            return terms.get(0);
        return new ConcatTerm(terms.get(0), concat(terms.subList(1, terms.size() - 2)));
    }

    /**
     * Alter multiple terms together, using as c1|c2|c3.
     * 
     * @param terms going to be altered
     * @return alted terms
     */
    public static Term alter(List<Term> terms) {
        if (terms.size() == 1)
            return terms.get(0);
        return new AlterTerm(terms.get(0), alter(terms.subList(1, terms.size() - 2)));
    }

    /**
     * If repeat for unlimited times, use Kleene star for generating.
     * 
     * @param term going to be repeated
     * @return unlimited repeating term
     */
    public static Term repeat(Term term) {
        return new KleeneTerm(term);
    }

    /**
     * Repeat term for given times, just contact term for given times.
     * 
     * @param term going to be repeated
     * @param n    time of repeating
     * @return repeated term
     */
    public static Term repeat(Term term, Integer n) {
        return concat(Collections.nCopies(n, term));
    }

    /**
     * If repeat for a range of times (assume $(n, m)$ here), things get a little bit complex:
     * we need to concat term for $m$ times, and for the last $m - n$ times,
     * we need to connect enter state to the $m$-th state's exit state. :)
     * 
     * For example, "a{3,5}" will be compiled as:
     *                            | -----------ε----------- | 
     *                            |        | -------ε------ | 
     *                            |        |                v
     * enter -a-> s0 -a-> s1 -a-> s2 -a-> s3 -a-> s4 -ε-> exit_
     * ----------------------------^^^^^^^^^^^^^^^^^^^^^^^^^^^^
     *        first n times             last (m - n) times
     * 
     * Or, using hard way: `<term>{m, n} = term{m}|term{m+1}|...|term{n}`, such as:
     * ```
     * alter([concat([term] * i) for i in range(n, m + 1)])
     * ```
     * 
     * @param term going to be repeated
     * @param n    minimum time of repeating
     * @param m    maximum time of repeating
     * @return repeated term
     */
    public static Term repeat(Term term, Integer n, Integer m) {
        if (n == m)
            return repeat(term, n);
        assert (m > n) && (n >= 0);
        return new RepeatTerm(term, n, m);
    }

    /**
     * Kleene plus operator +.
     * 
     * @param term should exists 1 or more times
     * @return repeated term
     */
    public static Term plus(Term term) {
        return new ConcatTerm(term, new KleeneTerm(term));
    }

    /**
     * Zero or once opeator ?.
     * 
     * @param term could show once or not
     * @return repeated term
     */
    public static Term optional(Term term) {
        return new AlterTerm(term, Epsilon);
    }

    /**
     * Fully matched string pattern.
     * 
     * @param str should be matched
     * @return conbined term
     */
    public static Term string(String str) {
        List<Term> terms = new ArrayList<>();
        for (Character c : str.toCharArray()) {
            terms.add(new CharTerm(String.valueOf(c)));
        }
        return concat(terms);
    }

    /**
     * Return char range Regex for given form, like: [a-zA-Z].
     * 
     * @param form could be matched with any letter in it
     * @return conbined term
     */
    public static Term srange(String form, String name) {
        return new CharTerm(form, name);
    }

    private static final String digitForm = "0123456789";
    private static final String punctuationForm = "!@#$%^&*()_+-= ";
    private static final String upperLetterForm = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String lowerLetterForm = "abcdefghijklmnopqrstuvwxyz";

    // Useful terms
    public static final EpsilonTerm Epsilon = new EpsilonTerm();
    public static final Term Digits = srange(digitForm, "[0-9]");
    public static final Term Letters = srange(upperLetterForm + lowerLetterForm, "[a-zA-Z]");
    public static final Term Any = srange(digitForm + punctuationForm + upperLetterForm + lowerLetterForm, ".");
}
