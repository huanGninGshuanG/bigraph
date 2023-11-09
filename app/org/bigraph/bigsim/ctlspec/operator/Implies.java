package org.bigraph.bigsim.ctlspec.operator;
import org.bigraph.bigsim.ctlspec.Formula;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class Implies implements Formula{
    private Set<Formula> operands = new HashSet<>();//or运算符有多个操作数，
    public Implies(Formula... operands){
        Collections.addAll(this.operands,operands);
    }
    public boolean equals(Object o){
        if(this == o) return true;
        if(o==null || getClass()!=o.getClass()) return false;
        Implies imply =(Implies) o;
        return Objects.equals(operands,imply.operands);
    }
    @Override
    public int hashCode(){
        return Objects.hash(operands);
    }
    public static Implies implies(Formula... operands){
        return new Implies(operands);
    }
    @Override
    public String toString() {
        return "(" + operands.stream().map(Formula::toString).collect(Collectors.joining(" IMPLY ")) + ")";
    }

    public Set<Formula> getOperands() {
        return new HashSet<>(operands);
    }

    @Override
    public Formula convertToCTLBase() {
        return implies(operands.stream().map(Formula::convertToCTLBase).collect(Collectors.toList()).toArray(new Formula[]{}));
    }
}
