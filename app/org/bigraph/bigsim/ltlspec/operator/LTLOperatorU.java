package org.bigraph.bigsim.ltlspec.operator;

import org.bigraph.bigsim.ctlspec.Formula;

import java.util.Objects;

public class LTLOperatorU implements Formula {
    private final Formula operand1;//第一个命题
    private final Formula operand2;//第二个命题
    public static LTLOperatorU Imply(Formula operand1, Formula operand2){
        return new LTLOperatorU(operand1,operand2);
    }
    public LTLOperatorU(Formula operand1, Formula operand2){
        this.operand1 = operand1;
        this.operand2 = operand2;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LTLOperatorU i = (LTLOperatorU) o;
        return Objects.equals(operand1, i.operand1) &&
                Objects.equals(operand2, i.operand2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operand1, operand2);
    }

    @Override
    public String toString() {
        return operand1 + " U " + operand2;
    }//描述形式为E(pUq)

    public Formula getOperand1() {
        return operand1;
    }

    public Formula getOperand2() {
        return operand2;
    }

    @Override
    public Formula convertToCTLBase() {
        return this;
    }
}
