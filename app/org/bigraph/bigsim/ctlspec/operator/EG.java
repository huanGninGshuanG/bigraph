package org.bigraph.bigsim.ctlspec.operator;

import visitor.Formula;
import visitor.FormulaVisitor;

import java.util.Objects;

import static org.bigraph.bigsim.ctlspec.operator.Not.not;
import static org.bigraph.bigsim.ctlspec.operator.AF.AF;

public class EG implements Formula {
    private final Formula operand;

    public Formula getOperand() {
        return operand;
    }

    public static EG EG(Formula operand) {
        return new EG(operand);
    }

    public EG(Formula operand) {
        this.operand = operand;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EG eg = (EG) o;
        return Objects.equals(operand, eg.operand);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operand);
    }

    @Override
    public String toString() {
        return "EG " + operand;
    }

    @Override
    public Formula convertToCTLBase() {
        return not(AF(not(operand))).convertToCTLBase();
    }

    @Override
    public Formula convertToENF() {
        return EG(operand.convertToENF());
    }

    @Override
    public void accept(FormulaVisitor visitor) {
        visitor.visit(this);
    }
}
