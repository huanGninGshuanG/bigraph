package org.bigraph.bigsim.model.component.shared;

import javafx.util.Pair;

/**
 * @author huangningshuang
 * @date 2024/1/23
 */
public class Degree {
    private Pair<Integer, Integer> inDeg, outDeg;

    Degree(Pair<Integer, Integer> in, Pair<Integer, Integer> out) {
        this.inDeg = in;
        this.outDeg = out;
    }

    public Pair<Integer, Integer> getIn() {
        return inDeg;
    }

    public Pair<Integer, Integer> getOut() {
        return outDeg;
    }

    public static boolean match(Degree a, Degree b) {
        return matchDeg(a.inDeg, b.inDeg) && matchDeg(a.outDeg, b.outDeg);
    }

    public static boolean matchDeg(Pair<Integer, Integer> x, Pair<Integer, Integer> y) {
        if (x.getValue() == 0) {
            return y.getValue() == 0 && x.getKey().equals(y.getKey());
        }
        return y.getKey() >= x.getKey();
    }
}
