package org.bigraph.bigsim.modelchecker;

import org.bigraph.bigsim.ltlspec.LTLFormula;
import org.bigraph.bigsim.ltlspec.visitor.LTLCheckVisitor;
import org.bigraph.bigsim.ltlspec.visitor.LTLPathGenVisitor;
import org.bigraph.bigsim.transitionsystem.KripkeStructure;
import org.bigraph.bigsim.transitionsystem.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class LTLModelChecker {
    private static final Logger logger = LoggerFactory.getLogger(LTLModelChecker.class);
    private final KripkeStructure kripkeStructure;
    public List<State> recordPath = new ArrayList<>();  // add

    public LTLModelChecker(KripkeStructure kripkeStructure) {
        this.kripkeStructure = kripkeStructure;
    }

    public static boolean satisfies(KripkeStructure kripkeStructure, LTLFormula formula) {
        return new LTLModelChecker(kripkeStructure).satisfies(formula);
    }

    public boolean satisfies(LTLFormula formula) {
        kripkeStructure.validate();//先判断kripke结构是否为空
        logger.debug("Starting to check whether the given Kripke structure satisifies {}.", formula);
        LTLFormula formulaBase = formula.convertToPNF(); // 转换为标准的LTL格式
        logger.debug("Converted given formula to formula in out ltl base using tautologies:{}.", formulaBase);
        LTLCheckVisitor visitor = new LTLCheckVisitor(kripkeStructure);
        formulaBase.accept(visitor);
        boolean satisfy = visitor.isSatisfy(formulaBase);
        if (!satisfy) {
            LTLPathGenVisitor pathVisitor =
                    new LTLPathGenVisitor(kripkeStructure, visitor.getLabel());
            formulaBase.accept(pathVisitor);
            recordPath = pathVisitor.getResult();
        }
        return satisfy;
    }
}
