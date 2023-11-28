package org.bigraph.bigsim.ltlspec.atom;

import org.bigraph.bigsim.ltlspec.LTLFormula;
import org.bigraph.bigsim.ltlspec.LTLFormulaVisitor;
import org.bigraph.bigsim.ltlspec.operator.LTLOperatorNot;

public class LTLFalse implements LTLFormula {
    public static LTLFalse False() {
        return new LTLFalse();
    }

    @Override
    public String toString() {
        return "false";
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (getClass() == o.getClass());
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public LTLFormula convertToPNF() {
        return this;
    }

    @Override
    public void accept(LTLFormulaVisitor visitor) {
        visitor.visit(this);
    }
}
