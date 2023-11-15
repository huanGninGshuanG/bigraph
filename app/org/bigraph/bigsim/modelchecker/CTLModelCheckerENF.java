package org.bigraph.bigsim.modelchecker;

import org.bigraph.bigsim.ctlspec.Formula;
import org.bigraph.bigsim.ctlspec.visitor.ENFVisitor;
import org.bigraph.bigsim.transitionsystem.KripkeStructure;
import org.bigraph.bigsim.transitionsystem.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CTLModelCheckerENF {
    private static final Logger logger = LoggerFactory.getLogger(CTLModelCheckerENF.class);
    private final KripkeStructure kripkeStructure;

    private CTLModelCheckerENF(KripkeStructure kripkeStructure) {
        this.kripkeStructure = kripkeStructure;
    }

    public static boolean satisfy(KripkeStructure kripkeStructure, Formula formula) {
        formula = formula.convertToENF();
        System.out.println("ENF ctl: " + formula);
        return new CTLModelCheckerENF(kripkeStructure).satisfy(formula);
    }

    private boolean satisfy(Formula formula) {
        ENFVisitor visitor = new ENFVisitor(kripkeStructure);
        formula.accept(visitor);
        boolean sat = true;
        for (State state : kripkeStructure.getInitialStates()) {
            if (!visitor.satisfy(formula, state)) {
                sat = false;
                break;
            }
        }
        return sat;
    }
}
