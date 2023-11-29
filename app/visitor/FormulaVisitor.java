package visitor;

import org.bigraph.bigsim.ctlspec.atom.Atom;
import org.bigraph.bigsim.ctlspec.atom.False;
import org.bigraph.bigsim.ctlspec.atom.True;
import org.bigraph.bigsim.ctlspec.operator.*;

public interface FormulaVisitor {
    // existential normal form needed
    void visit(True f);
    void visit(Atom f);
    void visit(And f);
    void visit(Not f);
    void visit(EX f);
    void visit(EU f);
    void visit(EG f);

    void visit(False f);
    void visit(EF f);
    void visit(AF f);
    void visit(AG f);
    void visit(AU f);
    void visit(AX f);
    void visit(Imply f);
    void visit(Or f);
}
