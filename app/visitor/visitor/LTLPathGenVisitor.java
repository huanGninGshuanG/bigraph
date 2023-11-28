package visitor.visitor;

import org.bigraph.bigsim.ctlspec.atom.Atom;
import visitor.LTLFormula;
import visitor.LTLFormulaVisitor;
import org.bigraph.bigsim.ltlspec.atom.LTLFalse;
import org.bigraph.bigsim.ltlspec.atom.LTLTrue;
import org.bigraph.bigsim.ltlspec.operator.*;
import org.bigraph.bigsim.transitionsystem.KripkeStructure;
import org.bigraph.bigsim.transitionsystem.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class LTLPathGenVisitor implements LTLFormulaVisitor {
    private Set<State> vis = new HashSet<>();
    private static final Logger logger = LoggerFactory.getLogger(PathGenVisitor.class);
    private final KripkeStructure ks;
    private Map<State, Set<LTLFormula>> label;
    private List<State> path = new ArrayList<>();

    public LTLPathGenVisitor(KripkeStructure ks, Map<State, Set<LTLFormula>> label) {
        this.ks = ks;
        this.label = label;
    }

    private boolean satisfy(State state, LTLFormula f) {
        return label.containsKey(state) && label.get(state).contains(f);
    }

    public List<State> getResult() {
        return path;
    }

    private void dfs(State state, LTLFormula f) {
        path.add(state);
        vis.add(state);
        for (State next : ks.getAllSuccessorStates(state)) {
            if (vis.contains(next)) {
                path.add(next);
                break;
            }
            if (!satisfy(next, f) && !vis.contains(next)) {
                dfs(next, f);
                break;
            }
        }
    }

    @Override
    public void visit(Atom f) {
    }

    @Override
    public void visit(LTLFalse f) {
    }

    @Override
    public void visit(LTLTrue f) {
    }

    @Override
    public void visit(LTLOperatorNot f) {
    }

    private void addPath(LTLFormula f) {
        for (State state : ks.getInitialStates()) {
            if (!satisfy(state, f)) {
                dfs(state, f);
                break;
            }
        }
    }

    @Override
    public void visit(LTLOperatorAnd f) {
        // todo: 这样的语句不仅仅展示路径
        addPath(f);
    }

    @Override
    public void visit(LTLOperatorOr f) {
        addPath(f);
    }

    @Override
    public void visit(LTLOperatorW f) {
        addPath(f);
    }

    @Override
    public void visit(LTLOperatorX f) {
        for (State state : ks.getInitialStates()) {
            if (!satisfy(state, f)) {
                path.add(state);
                for (State next : ks.getAllSuccessorStates(state)) {
                    if (!satisfy(next, f.getOperand())) {
                        path.add(next);
                        break;
                    }
                }
                break;
            }
        }
    }

    @Override
    public void visit(LTLOperatorU f) {
        addPath(f);
    }

    @Override
    public void visit(LTLOperatorF f) {
        addPath(f);
    }

    @Override
    public void visit(LTLOperatorG f) {
        addPath(f);
    }
}
