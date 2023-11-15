package org.bigraph.bigsim.ctlspec.operator;
import org.bigraph.bigsim.ctlspec.Formula;
import org.bigraph.bigsim.ctlspec.FormulaVisitor;

import java.util.Objects;
public class EX implements Formula {
    private final Formula operand;

    public static EX EX(Formula operand) {
        return new EX(operand);
    }

    public EX(Formula operand) {
        this.operand = operand;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EX not = (EX) o;
        return Objects.equals(operand, not.operand);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operand);
    }

    @Override
    public String toString() {
        return "EX " + operand;
    }

    public Formula getOperand() {
        return operand;
    }

    @Override
    public Formula convertToCTLBase() {
        return EX(operand.convertToCTLBase());
    }

    @Override
    public Formula convertToENF() {
        return EX(operand.convertToENF());
    }

    @Override
    public void accept(FormulaVisitor visitor) {
        visitor.visit(this);
    }
}
