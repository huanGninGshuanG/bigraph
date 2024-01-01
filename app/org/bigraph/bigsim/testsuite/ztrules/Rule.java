package org.bigraph.bigsim.testsuite.ztrules;

import org.bigraph.bigsim.BRS.InstantiationMap;
import org.bigraph.bigsim.model.Bigraph;

/**
 * @author huangningshuang
 * @date 2024/1/1
 */
public class Rule {
    private Bigraph redex, reactum;
    private InstantiationMap eta;

    public Rule(Bigraph redex, Bigraph reactum, InstantiationMap eta) {
        this.redex = redex;
        this.reactum = reactum;
        this.eta = eta;
    }

    public Bigraph getRedex() {
        return redex;
    }

    public Bigraph getReactum() {
        return reactum;
    }

    public InstantiationMap getEta() {
        return eta;
    }
}
