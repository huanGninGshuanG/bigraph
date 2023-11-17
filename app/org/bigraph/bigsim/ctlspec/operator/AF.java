package org.bigraph.bigsim.ctlspec.operator;

import org.bigraph.bigsim.ctlspec.Formula;
import org.bigraph.bigsim.ctlspec.FormulaVisitor;

import java.util.Objects;

import static org.bigraph.bigsim.ctlspec.atom.True.True;
import static org.bigraph.bigsim.ctlspec.operator.AU.AU;

public class AF implements Formula {
    private final Formula operand;

    public static AF AF(Formula operand) {
        return new AF(operand);
    }

    public AF(Formula operand) {
        this.operand = operand;
    }

    public Formula getOperand() {
        return operand;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AF not = (AF) o;
        return Objects.equals(operand, not.operand);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operand);
    }

    @Override
    public String toString() {
        return "AF " + operand;
    }

    @Override
    public Formula convertToCTLBase() {
        return AU(True(), operand).convertToCTLBase();
    }

    @Override
    public Formula convertToENF() {
        return new Not(new EG(new Not(operand.convertToENF()))).convertToENF();
    }

    @Override
    public void accept(FormulaVisitor visitor) {
        visitor.visit(this);
    }
}
