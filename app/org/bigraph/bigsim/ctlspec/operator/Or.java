package org.bigraph.bigsim.ctlspec.operator;
import org.bigraph.bigsim.ctlspec.Formula;
import org.bigraph.bigsim.ctlspec.FormulaVisitor;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
public class Or implements Formula{
    private Set<Formula> operands = new HashSet<>();//or运算符有多个操作数，
    public Or(Formula... operands){
        Collections.addAll(this.operands,operands);
    }
    public boolean equals(Object o){
        if(this == o) return true;
        if(o==null || getClass()!=o.getClass()) return false;
        Or or =(Or) o;
        return Objects.equals(operands,or.operands);
    }
    @Override
    public int hashCode(){
        return Objects.hash(operands);
    }
    public static Or or(Formula... operands){
        return new Or(operands);
    }
    @Override
    public String toString() {
        return "(" + operands.stream().map(Formula::toString).collect(Collectors.joining(" OR ")) + ")";
    }

    public Set<Formula> getOperands() {
        return new HashSet<>(operands);
    }

    @Override
    public Formula convertToCTLBase() {
        return or(operands.stream().map(Formula::convertToCTLBase).collect(Collectors.toList()).toArray(new Formula[]{}));
    }

    @Override
    public Formula convertToENF() {
        return new Not(new And(operands.stream()
                .map(operand->new Not(operand.convertToENF()))
                .collect(Collectors.toList())
                .toArray(new Formula[]{})));
    }

    @Override
    public void accept(FormulaVisitor visitor) {
        visitor.visit(this);
    }
}
