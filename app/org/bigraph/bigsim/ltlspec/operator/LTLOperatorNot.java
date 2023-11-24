package org.bigraph.bigsim.ltlspec.operator;

import visitor.LTLFormula;
import visitor.LTLFormulaVisitor;
import visitor.visitor.LTLNegationVisitor;

import java.util.Objects;

public class LTLOperatorNot implements LTLFormula {

    private final LTLFormula operand;

    public LTLOperatorNot(LTLFormula operand) {
        this.operand = operand;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LTLOperatorNot not = (LTLOperatorNot) o;
        return Objects.equals(operand, not.operand);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operand);
    }

    public static LTLFormula not(LTLFormula op) {
        return new LTLOperatorNot(op);
    }

    @Override
    public String toString() {
        return "NOT(" + operand + ")";
    }

    public LTLFormula getOperand() {
        return operand;
    }

    @Override
    public LTLFormula convertToPNF() {
        LTLNegationVisitor visitor = new LTLNegationVisitor();
        operand.accept(visitor);
        return visitor.getResult();
    }

    @Override
    public void accept(LTLFormulaVisitor visitor) {
        visitor.visit(this);
    }
}
