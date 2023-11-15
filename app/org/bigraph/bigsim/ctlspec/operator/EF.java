package org.bigraph.bigsim.ctlspec.operator;
import org.bigraph.bigsim.ctlspec.Formula;
import org.bigraph.bigsim.ctlspec.FormulaVisitor;

import java.util.Objects;

import static org.bigraph.bigsim.ctlspec.atom.True.True;
import static org.bigraph.bigsim.ctlspec.operator.EU.EU;
public class EF implements Formula{
    private final Formula operand;
    public static EF EF(Formula operand){
        return new EF(operand);
    }
    public EF(Formula operand){
        this.operand = operand;
    }
    @Override
    public boolean equals(Object o){
        if(this == o) return true;
        if(o==null || getClass()!=o.getClass()) return false;
        EF ef = (EF) o;
        return Objects.equals(operand,ef.operand);
    }
    @Override
    public int hashCode(){
        return Objects.hash(operand);
    }
    @Override
    public String toString(){
        return "EF " + operand;
    }
    @Override
    public Formula convertToCTLBase(){
        return EU(True(),operand).convertToCTLBase();
    }

    @Override
    public Formula convertToENF() {
        return EU(True(),operand).convertToENF();
    }

    @Override
    public void accept(FormulaVisitor visitor) {
        visitor.visit(this);
    }
}
