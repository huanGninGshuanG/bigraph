package org.bigraph.bigsim.ltlspec.operator;

import org.bigraph.bigsim.ctlspec.atom.False;
import org.bigraph.bigsim.ltlspec.LTLFormula;
import org.bigraph.bigsim.ltlspec.LTLFormulaVisitor;
import org.bigraph.bigsim.ltlspec.atom.LTLFalse;

import java.util.Objects;

public class LTLOperatorG implements LTLFormula {
    private final LTLFormula operand;

    public LTLOperatorG(LTLFormula operand) {
        this.operand = operand;
    }

    public LTLFormula getOperand() {
        return operand;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LTLOperatorG not = (LTLOperatorG) o;
        return Objects.equals(operand, not.operand);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operand);
    }

    @Override
    public String toString() {
        return "G " + operand;
    }

    @Override
    public LTLFormula convertToPNF() {
        return new LTLOperatorG(operand.convertToPNF());
    }

    @Override
    public void accept(LTLFormulaVisitor visitor) {
        visitor.visit(this);
    }
}
