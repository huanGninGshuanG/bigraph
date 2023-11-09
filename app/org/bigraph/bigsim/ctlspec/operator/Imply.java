package org.bigraph.bigsim.ctlspec.operator;

import org.bigraph.bigsim.ctlspec.Formula;

import java.util.Objects;

public class Imply implements Formula{
    private final Formula operand1;//第一个命题
    private final Formula operand2;//第二个命题
    public static Imply Imply(Formula operand1, Formula operand2){
        return new Imply(operand1,operand2);
    }
    public Imply(Formula operand1,Formula operand2){
        this.operand1 = operand1;
        this.operand2 = operand2;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Imply i = (Imply) o;
        return Objects.equals(operand1, i.operand1) &&
                Objects.equals(operand2, i.operand2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operand1, operand2);
    }

    @Override
    public String toString() {
        return operand1 + " imply " + operand2;
    }//描述形式为E(pUq)

    public Formula getOperand1() {
        return operand1;
    }

    public Formula getOperand2() {
        return operand2;
    }

    @Override
    public Formula convertToCTLBase() {
        return new Or(new Not(operand1.convertToCTLBase()),operand2.convertToCTLBase());
    }
}
