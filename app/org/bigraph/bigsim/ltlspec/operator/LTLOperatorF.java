package org.bigraph.bigsim.ltlspec.operator;
import org.bigraph.bigsim.ctlspec.Formula;
import org.bigraph.bigsim.ctlspec.atom.True;

import java.util.Objects;

public class LTLOperatorF implements Formula {
    private final Formula operand;
    public LTLOperatorF(Formula operand){
        this.operand=operand;
    }
    @Override
    public boolean equals(Object o){
        if(this == o) return true;
        if(o==null || getClass()!=o.getClass()) return false;
        LTLOperatorF not = (LTLOperatorF) o;
        return Objects.equals(operand,not.operand);
    }
    @Override
    public int hashCode(){
        return Objects.hash(operand);
    }
    public static Formula not(Formula op){
        return new LTLOperatorF(op);
    }
    @Override
    public String toString(){
        return "F("+operand+")";
    }
    public Formula getOperand(){
        return operand;
    }
    @Override
    public Formula convertToCTLBase(){
        return new LTLOperatorU(new True(),operand.convertToCTLBase());
    }
}
