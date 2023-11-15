package org.bigraph.bigsim.ltlspec.operator;

import org.bigraph.bigsim.ctlspec.Formula;
import org.bigraph.bigsim.ctlspec.FormulaVisitor;
import org.bigraph.bigsim.ctlspec.operator.Not;

import java.util.Objects;

public class LTLOperatorG implements Formula {
    private final Formula operand;
    public LTLOperatorG(Formula operand){
        this.operand=operand;
    }
    @Override
    public boolean equals(Object o){
        if(this == o) return true;
        if(o==null || getClass()!=o.getClass()) return false;
        LTLOperatorG not = (LTLOperatorG) o;
        return Objects.equals(operand,not.operand);
    }
    @Override
    public int hashCode(){
        return Objects.hash(operand);
    }
    public static Formula not(Formula op){
        return new LTLOperatorG(op);
    }
    @Override
    public String toString(){
        return "G("+operand+")";
    }
    public Formula getOperand(){
        return operand;
    }
    @Override
    public Formula convertToCTLBase(){
        return new Not(new LTLOperatorF(new Not(operand.convertToCTLBase())).convertToCTLBase());
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
