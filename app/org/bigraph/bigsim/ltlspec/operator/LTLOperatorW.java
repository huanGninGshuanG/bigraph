package org.bigraph.bigsim.ltlspec.operator;

import org.bigraph.bigsim.ctlspec.Formula;
import org.bigraph.bigsim.ctlspec.FormulaVisitor;
import org.bigraph.bigsim.ctlspec.operator.Or;

import java.util.Objects;

public class LTLOperatorW implements Formula {
    private final Formula operand1;//第一个命题
    private final Formula operand2;//第二个命题
    public static LTLOperatorW Imply(Formula operand1, Formula operand2){
        return new LTLOperatorW(operand1,operand2);
    }
    public LTLOperatorW(Formula operand1, Formula operand2){
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
    }//描述形式为E(pUq)

    public Formula getOperand1() {
        return operand1;
    }

    public Formula getOperand2() {
        return operand2;
    }

    @Override
    public Formula convertToCTLBase() {
        return new Or(new LTLOperatorU(operand1.convertToCTLBase(),operand2.convertToCTLBase()).convertToCTLBase(),new LTLOperatorG(operand1.convertToCTLBase()).convertToCTLBase());
    }

    @Override
    public Formula convertToENF() {
        throw new UnsupportedOperationException("LTL not supported yet");
    }

    @Override
    public void accept(FormulaVisitor visitor) {
        throw new UnsupportedOperationException("LTL not supported yet");
    }
}
