package org.bigraph.bigsim.ctlspec.visitor;

import org.bigraph.bigsim.ctlimpl.CTLCheckResult;
import org.bigraph.bigsim.ctlspec.Formula;
import org.bigraph.bigsim.ctlspec.FormulaVisitor;
import org.bigraph.bigsim.ctlspec.atom.Atom;
import org.bigraph.bigsim.ctlspec.atom.False;
import org.bigraph.bigsim.ctlspec.atom.True;
import org.bigraph.bigsim.ctlspec.operator.*;
import org.bigraph.bigsim.transitionsystem.KripkeStructure;
import org.bigraph.bigsim.transitionsystem.State;
import org.bigraph.bigsim.utils.DebugPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PathGenVisitor implements FormulaVisitor {

    private final Map<Formula, Set<State>> sat;
    private CTLCheckResult result = new CTLCheckResult();
    private final KripkeStructure ks;
    private boolean debug = true;
    private static final Logger logger = LoggerFactory.getLogger(PathGenVisitor.class);

    public PathGenVisitor(Map<Formula, Set<State>> sat, KripkeStructure ks, boolean result) {
        this.sat = sat;
        this.ks = ks;
        this.result.res = result;
    }

    public CTLCheckResult getCTLCheckResult() {
        return result;
    }

    private boolean satisfy(Formula formula, State state) {
        return sat.containsKey(formula) && sat.get(formula).contains(state);
    }

    @Override
    public void visit(True f) {
        result.type = CTLCheckResult.PathType.NoNeed;
    }

    @Override
    public void visit(Atom f) {
        result.type = CTLCheckResult.PathType.NoNeed;
    }

    @Override
    public void visit(And f) {
        // todo: 改进(EUa and EXb)这种情况应该不仅仅展示路径？
        result.type = CTLCheckResult.PathType.NoNeed;
    }

    @Override
    public void visit(Not f) {
        DebugPrinter.print(logger, "start gen Not: " + result.path);
        PathGenVisitor visitor = new PathGenVisitor(sat, ks, !result.res);
        f.getOperand().accept(visitor);
        CTLCheckResult innerRes = visitor.getCTLCheckResult();
        result.type = CTLCheckResult.PathType.NoNeed;
        if (innerRes.type == CTLCheckResult.PathType.WitnessType) {
            result.type = CTLCheckResult.PathType.CounterExample;
        } else if (innerRes.type == CTLCheckResult.PathType.CounterExample) {
            result.type = CTLCheckResult.PathType.WitnessType;
        }
        result.path = innerRes.path;
        DebugPrinter.print(logger, "end gen Not: " + result.path);
    }

    @Override
    public void visit(EX f) {
        DebugPrinter.print(logger, "start gen EX");
        if (!result.res) {
            result.type = CTLCheckResult.PathType.NoNeed;
            return;
        }
        State cur = null;
        for (State state : ks.getInitialStates()) {
            if (satisfy(f, state)) {
                cur = state;
                break;
            }
        }
        if (null == cur) throw new RuntimeException("所有初始节点均不满足条件");
        result.type = CTLCheckResult.PathType.WitnessType;
        result.path.add(cur);
        for (State state : ks.getAllSuccessorStates(cur)) {
            if (satisfy(f.getOperand(), state)) {
                result.path.add(state);
                break;
            }
        }
        DebugPrinter.print(logger, "end gen EX: " + result.path);
    }

    @Override
    public void visit(EU f) {
        DebugPrinter.print(logger, "start gen EU: " + result.res);
        if (!result.res) {
            result.type = CTLCheckResult.PathType.NoNeed;
            return;
        }
        State cur = null;
        for (State state : ks.getInitialStates()) {
            if (satisfy(f, state)) {
                cur = state;
                break;
            }
        }
        if (null == cur) throw new RuntimeException("所有初始节点均不满足条件");
        result.type = CTLCheckResult.PathType.WitnessType;
        Set<State> vis = new HashSet<>();
        while (true) {
            result.path.add(cur);
            vis.add(cur);
            if (satisfy(f.getOperand2(), cur)) break;
            for (State next : ks.getAllSuccessorStates(cur)) {
                if (!vis.contains(next) && satisfy(f, next)) {
                    cur = next;
                    break;
                }
            }
        }
        DebugPrinter.print(logger, "end gen EU: " + result.path);
    }

    @Override
    public void visit(EG f) {
        DebugPrinter.print(logger, "start gen EG");
        if (!result.res) {
            result.type = CTLCheckResult.PathType.NoNeed;
            return;
        }
        State cur = null;
        for (State state : ks.getInitialStates()) {
            if (satisfy(f, state)) {
                cur = state;
                break;
            }
        }
        if (null == cur) throw new RuntimeException("所有初始节点均不满足条件");
        result.type = CTLCheckResult.PathType.WitnessType;
        Set<State> vis = new HashSet<>();
        while (true) {
            result.path.add(cur);
            vis.add(cur);
            boolean terminate = false;
            for (State next : ks.getAllSuccessorStates(cur)) {
                if (vis.contains(next)) {
                    // 把环的入口也添加进来
                    result.path.add(next);
                    terminate = true;
                    break;
                } else if (satisfy(f, next)) {
                    cur = next;
                    break;
                }
            }
            if (terminate) break;
        }
        DebugPrinter.print(logger, "end gen EG: " + result.path);
    }

    @Override
    public void visit(EF f) {
        throw new UnsupportedOperationException("EF shouldn't occur in ENF");
    }

    @Override
    public void visit(False f) {
        throw new UnsupportedOperationException("False shouldn't occur in ENF");
    }

    @Override
    public void visit(AF f) {
        throw new UnsupportedOperationException("AF shouldn't occur in ENF");
    }

    @Override
    public void visit(AG f) {
        throw new UnsupportedOperationException("AG shouldn't occur in ENF");
    }

    @Override
    public void visit(AU f) {
        throw new UnsupportedOperationException("AU shouldn't occur in ENF");
    }

    @Override
    public void visit(AX f) {
        throw new UnsupportedOperationException("AX shouldn't occur in ENF");
    }

    @Override
    public void visit(Imply f) {
        throw new UnsupportedOperationException("Imply shouldn't occur in ENF");
    }

    @Override
    public void visit(Or f) {
        throw new UnsupportedOperationException("Or shouldn't occur in ENF");
    }
}
