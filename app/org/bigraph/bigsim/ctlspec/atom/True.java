package org.bigraph.bigsim.ctlspec.atom;

import visitor.Formula;
import visitor.FormulaVisitor;

public class True implements Formula {
    public static True True(){
        return new True();
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

    @Override
    public String toString(){
        return "true";
    }
    @Override
    public boolean equals(Object o){
        return this == o || (getClass()==o.getClass());
    }
    @Override
    public int hashCode(){
        return 1;
    }
}
