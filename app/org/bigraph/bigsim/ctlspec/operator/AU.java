package org.bigraph.bigsim.ctlspec.operator;
import org.bigraph.bigsim.ctlspec.Formula;
import org.bigraph.bigsim.ctlspec.FormulaVisitor;

import java.util.Objects;
import static org.bigraph.bigsim.ctlspec.operator.Not.not;

public class AU implements Formula {
    private final Formula operand1;
    private final Formula operand2;
    public static AU AU(Formula operand1,Formula operand2){
        return new AU(operand1,operand2);
    }
    public AU(Formula operand1,Formula operand2){
        this.operand1 = operand1;
        this.operand2 = operand2;
    }
    @Override
    public boolean equals(Object o){
        if(this == o) return true;
        if(o==null || getClass()!=o.getClass()) return false;
        AU au = (AU) o;
        return Objects.equals(operand1,au.operand1) &&
                Objects.equals(operand1,au.operand2);
    }
    @Override
    public int hashCode(){
        return Objects.hash(operand1,operand2);
    }
    @Override
    public String toString(){
        return "A(" + operand1 + " U "+operand2+")";
    }
    public Formula getOperand1(){
        return operand1;
    }
    public Formula getOperand2(){
        return operand2;
    }
    @Override
    public Formula convertToCTLBase(){
        return AU(operand1.convertToCTLBase(),operand2.convertToCTLBase());
    }

    @Override
    public Formula convertToENF() {
        Formula op1 = operand1.convertToENF(), op2 = operand2.convertToENF();
        return new And(not(new EU(not(op2), new And(not(op1), not(op2)))), not(new EG(not(op2)))).convertToENF();
    }

    @Override
    public void accept(FormulaVisitor visitor) {
        visitor.visit(this);
    }
}
