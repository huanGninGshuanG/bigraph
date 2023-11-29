package visitor;

import org.bigraph.bigsim.ctlspec.atom.Atom;
import org.bigraph.bigsim.ltlspec.atom.LTLFalse;
import org.bigraph.bigsim.ltlspec.atom.LTLTrue;
import org.bigraph.bigsim.ltlspec.operator.*;

public interface LTLFormulaVisitor {
    void visit(Atom f);

    void visit(LTLFalse f);

    void visit(LTLTrue f);

    void visit(LTLOperatorNot f);

    void visit(LTLOperatorAnd f);

    void visit(LTLOperatorOr f);

    void visit(LTLOperatorW f);

    void visit(LTLOperatorX f);

    void visit(LTLOperatorU f);

    void visit(LTLOperatorF ltlOperatorF);

    void visit(LTLOperatorG ltlOperatorG);
}
