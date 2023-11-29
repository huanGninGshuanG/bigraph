package visitor;

public interface LTLFormula {
    LTLFormula convertToPNF(); // positive normal form

    void accept(LTLFormulaVisitor visitor);
}
