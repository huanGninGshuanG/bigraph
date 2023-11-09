package org.bigraph.bigsim.ltlspec.operator;

import org.bigraph.bigsim.ctlspec.Formula;

import java.util.Objects;

public class LTLOperatorX implements Formula {
    private final Formula operand;
    public LTLOperatorX(Formula operand){
        this.operand=operand;
    }
    @Override
    public boolean equals(Object o){
        if(this == o) return true;
        if(o==null || getClass()!=o.getClass()) return false;
        LTLOperatorX not = (LTLOperatorX) o;
        return Objects.equals(operand,not.operand);
    }
    @Override
    public int hashCode(){
        return Objects.hash(operand);
    }
    public static Formula not(Formula op){
        return new LTLOperatorX(op);
    }
    @Override
    public String toString(){
        return "X("+operand+")";
    }
    public Formula getOperand(){
        return operand;
    }
    @Override
    public Formula convertToCTLBase(){
        return this;
    }
}
