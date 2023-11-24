package org.bigraph.bigsim.ctlspec.atom;

import visitor.Formula;
import visitor.FormulaVisitor;
import org.bigraph.bigsim.ctlspec.operator.Not;

public class False implements Formula {
    public static False False() {
        return new False();
    }

    @Override
    public Formula convertToCTLBase() {
        return this;
    }

    @Override
    public Formula convertToENF() {
        return Not.not(True.True());
    }

    @Override
    public void accept(FormulaVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "false";
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (getClass() == o.getClass());
    }

    @Override
    public int hashCode() {
        return 0;
    }
}
