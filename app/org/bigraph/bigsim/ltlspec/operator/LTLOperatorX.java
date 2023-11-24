package org.bigraph.bigsim.ltlspec.operator;

import org.bigraph.bigsim.ltlspec.LTLFormula;
import org.bigraph.bigsim.ltlspec.LTLFormulaVisitor;

import java.util.Objects;

public class LTLOperatorX implements LTLFormula {
    private final LTLFormula operand;

    public LTLOperatorX(LTLFormula operand) {
        this.operand = operand;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LTLOperatorX not = (LTLOperatorX) o;
        return Objects.equals(operand, not.operand);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operand);
    }

    public static LTLFormula not(LTLFormula op) {
        return new LTLOperatorX(op);
    }

    @Override
    public String toString() {
        return "X(" + operand + ")";
    }

    public LTLFormula getOperand() {
        return operand;
    }

    @Override
    public LTLFormula convertToPNF() {
        return new LTLOperatorX(operand.convertToPNF());
    }

    @Override
    public void accept(LTLFormulaVisitor visitor) {
        visitor.visit(this);
    }
}
