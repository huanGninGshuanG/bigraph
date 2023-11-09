package org.bigraph.bigsim.ctlspec.operator;
import org.bigraph.bigsim.ctlspec.Formula;

import java.util.Objects;

public class AX implements Formula{
    private final Formula operand;
    public static AX AX(Formula operand){
        return new AX(operand);
    }
    public AX(Formula operand){
        this.operand = operand;
    }
    @Override
    public boolean equals(Object o){
        if(this == o) return true;
        if(o==null || getClass()!=o.getClass()) return false;
        AX ax = (AX) o;
        return Objects.equals(operand,ax.operand);
    }
    @Override
    public int hashCode(){
        return Objects.hash(operand);
    }
    @Override
    public String toString(){
        return "AX " + operand;
    }
    public Formula getOperand(){
        return operand;
    }
    @Override
    public Formula convertToCTLBase(){
        return AX(operand.convertToCTLBase());
    }
}
