package org.bigraph.bigsim.ctlspec.operator;
import org.bigraph.bigsim.ctlspec.Formula;
import java.util.Objects;

import static org.bigraph.bigsim.ctlspec.operator.Not.not;
import static org.bigraph.bigsim.ctlspec.operator.EF.EF;
public class AG implements Formula{
    private final Formula operand;
    public static AG AG(Formula operand){
        return new AG(operand);
    }
    public AG(Formula operand){
        this.operand = operand;
    }
    @Override
    public boolean equals(Object o){
        if(this == o) return true;
        if(o == null || getClass()!=o.getClass()) return false;
        AG not = (AG) o;
        return Objects.equals(operand,not.operand);
    }
    @Override
    public int hashCode(){
        return Objects.hash(operand);
    }
    @Override
    public String toString(){
        return "AG " + operand;
    }
    @Override
    public Formula convertToCTLBase(){
        return not(EF(not(operand))).convertToCTLBase();
    }
}
