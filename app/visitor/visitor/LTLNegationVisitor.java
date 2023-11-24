package visitor.visitor;

import org.bigraph.bigsim.ctlspec.atom.Atom;
import visitor.LTLFormula;
import visitor.LTLFormulaVisitor;
import org.bigraph.bigsim.ltlspec.atom.LTLFalse;
import org.bigraph.bigsim.ltlspec.atom.LTLTrue;
import org.bigraph.bigsim.ltlspec.operator.*;

public class LTLNegationVisitor implements LTLFormulaVisitor {
    private LTLFormula negFormula;

    public LTLFormula getResult() {
        return negFormula;
    }

    @Override
    public void visit(Atom f) {
        negFormula = new LTLOperatorNot(f);
    }

    @Override
    public void visit(LTLFalse f) {
        negFormula = new LTLTrue();
    }

    @Override
    public void visit(LTLTrue f) {
        negFormula = new LTLFalse();
    }

    @Override
    public void visit(LTLOperatorNot f) {
        negFormula = f.getOperand();
    }

    @Override
    public void visit(LTLOperatorAnd f) {
        LTLFormula lhs = new LTLOperatorNot(f.getOperand1()).convertToPNF();
        LTLFormula rhs = new LTLOperatorNot(f.getOperand2()).convertToPNF();
        negFormula = new LTLOperatorOr(lhs, rhs);
    }

    @Override
    public void visit(LTLOperatorOr f) {
        LTLFormula lhs = new LTLOperatorNot(f.getOperand1()).convertToPNF();
        LTLFormula rhs = new LTLOperatorNot(f.getOperand2()).convertToPNF();
        negFormula = new LTLOperatorAnd(lhs, rhs);
    }

    @Override
    public void visit(LTLOperatorW f) {
        LTLFormula lhs = new LTLOperatorNot(f.getOperand2()).convertToPNF();
        LTLFormula rhs = new LTLOperatorAnd(new LTLOperatorNot(f.getOperand1()), new LTLOperatorNot(f.getOperand2())).convertToPNF();
        negFormula = new LTLOperatorU(lhs, rhs);
    }

    @Override
    public void visit(LTLOperatorX f) {
        negFormula = new LTLOperatorX(new LTLOperatorNot(f.getOperand()).convertToPNF());
    }

    @Override
    public void visit(LTLOperatorU f) {
        LTLFormula lhs = new LTLOperatorNot(f.getOperand2()).convertToPNF();
        LTLFormula rhs = new LTLOperatorAnd(new LTLOperatorNot(f.getOperand1()), new LTLOperatorNot(f.getOperand2())).convertToPNF();
        negFormula = new LTLOperatorW(lhs, rhs);
    }

    @Override
    public void visit(LTLOperatorF f) {
        negFormula = new LTLOperatorG(new LTLOperatorNot(f.getOperand())).convertToPNF();
    }

    @Override
    public void visit(LTLOperatorG f) {
        negFormula = new LTLOperatorF(new LTLOperatorNot(f.getOperand())).convertToPNF();
    }
}
