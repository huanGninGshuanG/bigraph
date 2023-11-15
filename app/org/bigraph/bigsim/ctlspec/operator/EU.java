package org.bigraph.bigsim.ctlspec.operator;
import org.bigraph.bigsim.ctlspec.Formula;
import org.bigraph.bigsim.ctlspec.FormulaVisitor;

import java.util.Objects;

public class EU implements Formula{//EU表示存在某条路径的某个状态上它的第二个命题为真，
    // 并且在这个状态之前的所有状态上其第一个命题都为真
    private final Formula operand1;//第一个命题
    private final Formula operand2;//第二个命题
    public static EU EU(Formula operand1, Formula operand2){
        return new EU(operand1,operand2);
    }
    public EU(Formula operand1,Formula operand2){
        this.operand1 = operand1;
        this.operand2 = operand2;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EU au = (EU) o;
        return Objects.equals(operand1, au.operand1) &&
                Objects.equals(operand2, au.operand2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operand1, operand2);
    }

    @Override
    public String toString() {
        return "E(" + operand1 + " U " + operand2 + ")";
    }//描述形式为E(pUq)

    public Formula getOperand1() {
        return operand1;
    }

    public Formula getOperand2() {
        return operand2;
    }

    @Override
    public Formula convertToCTLBase() {
        return EU(operand1.convertToCTLBase(), operand2.convertToCTLBase());
    }

    @Override
    public Formula convertToENF() {
        return EU(operand1.convertToENF(), operand2.convertToENF());
    }

    @Override
    public void accept(FormulaVisitor visitor) {
        visitor.visit(this);
    }
}
