package org.bigraph.bigsim.ltlspec.operator;

import visitor.LTLFormula;
import visitor.LTLFormulaVisitor;

import java.util.Objects;

public class LTLOperatorW implements LTLFormula {
    private final LTLFormula operand1;//第一个命题
    private final LTLFormula operand2;//第二个命题

    public LTLOperatorW(LTLFormula operand1, LTLFormula operand2) {
        this.operand1 = operand1;
        this.operand2 = operand2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LTLOperatorW i = (LTLOperatorW) o;
        return Objects.equals(operand1, i.operand1) &&
                Objects.equals(operand2, i.operand2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operand1, operand2);
    }

    @Override
    public String toString() {
        return operand1 + " W " + operand2;
    }//描述形式为(pWq)

    public LTLFormula getOperand1() {
        return operand1;
    }

    public LTLFormula getOperand2() {
        return operand2;
    }

    @Override
    public LTLFormula convertToPNF() {
        return new LTLOperatorW(operand1.convertToPNF(), operand2.convertToPNF());
    }

    @Override
    public void accept(LTLFormulaVisitor visitor) {
        visitor.visit(this);
    }
}
