package org.bigraph.bigsim.ctlspec.atom;
import org.bigraph.bigsim.ctlspec.Formula;
import org.bigraph.bigsim.ctlspec.FormulaVisitor;

import java.util.Objects;
//定义原子命题的谓词符号，原子命题是不含与或非的简单判断句
public class Atom implements Formula{
    private final String atomicPredicate;
    public Atom(String atomicPredicate){
        this.atomicPredicate = atomicPredicate;
    }
    @Override
    public boolean equals(Object o){
        if(this==o) return true;
        if(o==null || getClass()!=o.getClass()){
            return false;
        }
        Atom that = (Atom) o;
        return Objects.equals(atomicPredicate,that.atomicPredicate);
    }
    @Override
    public int hashCode(){
        return Objects.hash(atomicPredicate);
    }
    public static Atom atom(String atomicPredicate){
        return new Atom(atomicPredicate);
    }
    @Override
    public String toString(){
        return atomicPredicate;
    }
    @Override
    public Formula convertToCTLBase(){
        return this;
    }

    @Override
    public Formula convertToENF() {
        return this;
    }

    @Override
    public void accept(FormulaVisitor visitor) {
        visitor.visit(this);
    }
}
