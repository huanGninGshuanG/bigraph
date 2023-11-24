package org.bigraph.bigsim.ltlspec.atom;

import visitor.LTLFormula;
import visitor.LTLFormulaVisitor;

public class LTLTrue implements LTLFormula {
    public static LTLTrue True() {
        return new LTLTrue();
    }

    @Override
    public String toString() {
        return "true";
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (getClass() == o.getClass());
    }

    @Override
    public int hashCode() {
        return 1;
    }

    @Override
    public LTLFormula convertToPNF() {
        return this;
    }

    @Override
    public void accept(LTLFormulaVisitor visitor) {
        visitor.visit(this);
    }
}
