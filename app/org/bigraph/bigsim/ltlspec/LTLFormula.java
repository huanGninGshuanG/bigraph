package org.bigraph.bigsim.ltlspec;

public interface LTLFormula {
    LTLFormula convertToPNF(); // positive normal form

    void accept(LTLFormulaVisitor visitor);
}
