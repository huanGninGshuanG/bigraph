package org.bigraph.bigsim.modelchecker;

import org.bigraph.bigsim.ctlimpl.CTLCheckResult;
import visitor.Formula;
import visitor.visitor.ENFVisitor;
import visitor.visitor.PathGenVisitor;
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

    public static CTLCheckResult satisfy(KripkeStructure kripkeStructure, Formula formula) {
        formula = formula.convertToENF();
        logger.debug("ENF ctl: " + formula);
        return new CTLModelCheckerENF(kripkeStructure).satisfy(formula);
    }

    private CTLCheckResult satisfy(Formula formula) {
        ENFVisitor visitor = new ENFVisitor(kripkeStructure);
        formula.accept(visitor);
        boolean sat = true;
        for (State state : kripkeStructure.getInitialStates()) {
            if (!visitor.satisfy(formula, state)) {
                sat = false;
                break;
            }
        }
        PathGenVisitor pVisitor = new PathGenVisitor(visitor.getSat(), kripkeStructure, sat);
        formula.accept(pVisitor);
        logger.debug("get counter path: " + pVisitor.getCTLCheckResult().path);
        return pVisitor.getCTLCheckResult();
    }
}
