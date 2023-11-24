package org.bigraph.bigsim.ltlspec.operator;

import visitor.LTLFormula;
import visitor.LTLFormulaVisitor;

import java.util.Objects;

public class LTLOperatorAnd implements LTLFormula {
    private final LTLFormula operand1;
    private final LTLFormula operand2;

    public LTLOperatorAnd(LTLFormula op1, LTLFormula op2) {
        this.operand1 = op1;
        this.operand2 = op2;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LTLOperatorAnd and = (LTLOperatorAnd) o;
        return Objects.equals(operand1, and.operand1) &&
                Objects.equals(operand2, and.operand2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operand1, operand2);
    }

    @Override
    public String toString() {
        return "(" + operand1 + " And " + operand2 + ")";
    }

    public LTLFormula getOperand1() {
        return operand1;
    }

    public LTLFormula getOperand2() {
        return operand2;
    }

    @Override
    public LTLFormula convertToPNF() {
        return new LTLOperatorAnd(operand1.convertToPNF(), operand2.convertToPNF());
    }

    @Override
    public void accept(LTLFormulaVisitor visitor) {
        visitor.visit(this);
    }
}
