package visitor.visitor;

import org.bigraph.bigsim.ctlspec.atom.Atom;
import visitor.LTLFormula;
import visitor.LTLFormulaVisitor;
import org.bigraph.bigsim.ltlspec.atom.LTLFalse;
import org.bigraph.bigsim.ltlspec.atom.LTLTrue;
import org.bigraph.bigsim.ltlspec.operator.*;
import org.bigraph.bigsim.transitionsystem.KripkeStructure;
import org.bigraph.bigsim.transitionsystem.State;
import org.bigraph.bigsim.utils.DebugPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class LTLCheckVisitor implements LTLFormulaVisitor {

    private static final Logger logger = LoggerFactory.getLogger(LTLCheckVisitor.class);
    private final KripkeStructure ks;
    private Map<State, Set<LTLFormula>> label = new HashMap<>();

    public LTLCheckVisitor(KripkeStructure ks) {
        this.ks = ks;
    }

    public Map<State, Set<LTLFormula>> getLabel() {
        return label;
    }

    public boolean isSatisfy(LTLFormula f) {
        for (State state : ks.getInitialStates()) {
            if (!satisfy(state, f)) {
                return false;
            }
        }
        return true;
    }

    private boolean satisfy(State state, LTLFormula formula) {
        return label.containsKey(state) && label.get(state).contains(formula);
    }

    private void merge(State state, LTLFormula formula) {
        Set<LTLFormula> sat = label.getOrDefault(state, new HashSet<>());
        sat.add(formula);
        label.put(state, sat);
    }

    @Override
    public void visit(Atom f) {
        ks.getAllStates().stream().filter(state -> state.satisfies(f)).forEach(state -> merge(state, f));
        DebugPrinter.print(logger, "end check Atom " + f + ": " + label);
    }

    @Override
    public void visit(LTLFalse f) {
        DebugPrinter.print(logger, "end check False: " + label);
    }

    @Override
    public void visit(LTLTrue f) {
        ks.getAllStates().forEach(state -> merge(state, f));
        DebugPrinter.print(logger, "end check True:" + label);
    }

    @Override
    public void visit(LTLOperatorNot f) {
        f.getOperand().accept(this);
        for (State state : ks.getAllStates()) {
            if (!satisfy(state, f.getOperand())) {
                merge(state, f);
            }
        }
        DebugPrinter.print(logger, "end check LtlNot: " + label);
    }

    @Override
    public void visit(LTLOperatorAnd f) {
        // TODO: 这里是否要生成反例路径
        LTLFormula op1 = f.getOperand1(), op2 = f.getOperand2();
        op1.accept(this);
        op2.accept(this);
        for (State state : ks.getAllStates()) {
            if (satisfy(state, op1) && satisfy(state, op2)) {
                merge(state, op1);
            }
        }
        DebugPrinter.print(logger, "end check LtlAnd: " + label);
    }

    @Override
    public void visit(LTLOperatorOr f) {
        LTLFormula op1 = f.getOperand1(), op2 = f.getOperand2();
        op1.accept(this);
        op2.accept(this);
        for (State state : ks.getAllStates()) {
            if (satisfy(state, op1) || satisfy(state, op2)) {
                merge(state, op1);
            }
        }
        DebugPrinter.print(logger, "end check LtlOr: " + label);
    }

    @Override
    public void visit(LTLOperatorW f) {
        LTLFormula op1 = f.getOperand1(), op2 = f.getOperand2();
        LTLFormula t = new LTLOperatorOr(new LTLOperatorU(op1, op2), new LTLOperatorG(op1));
        LTLCheckVisitor visitor = new LTLCheckVisitor(ks);
        t.accept(visitor);
        for (State state : ks.getAllStates()) {
            if (visitor.satisfy(state, t)) {
                merge(state, f);
            }
        }
    }

    @Override
    public void visit(LTLOperatorX f) {
        f.getOperand().accept(this);
        for (State state : ks.getAllStates()) {
            boolean ok = true;
            for (State next : ks.getAllSuccessorStates(state)) {
                if (!satisfy(next, f.getOperand())) {
                    ok = false;
                    break;
                }
            }
            if (ok) merge(state, f);
        }
        DebugPrinter.print(logger, "end check LtlX: " + label);
    }

    private Set<State> vis = new HashSet<>();

    private void dfsU(State state, LTLOperatorU f) {
        vis.add(state);
        if (satisfy(state, f.getOperand2())) {
            merge(state, f);
            return;
        }
        if (!satisfy(state, f.getOperand1())) {
            return;
        }
        boolean ok = true;
        for (State next : ks.getAllSuccessorStates(state)) {
            if (!vis.contains(next)) dfsU(next, f);
            if (!satisfy(next, f)) {
                ok = false;
                break;
            }
        }
        if (ok) merge(state, f);
    }

    @Override
    public void visit(LTLOperatorU f) {
        LTLFormula op1 = f.getOperand1();
        LTLFormula op2 = f.getOperand2();
        op1.accept(this);
        op2.accept(this);
        for (State state : ks.getAllStates()) {
            if (!vis.contains(state)) {
                dfsU(state, f);
            }
        }
        DebugPrinter.print(logger, "end check LtlU: " + label);
    }

    private void dfsF(State state, LTLOperatorF f) {
        vis.add(state);
        if (satisfy(state, f)) {
            merge(state, f);
            return;
        }
        boolean ok = true;
        for (State next : ks.getAllSuccessorStates(state)) {
            if (!vis.contains(next)) dfsF(next, f);
            if (!satisfy(next, f)) {
                ok = false;
                break;
            }
        }
        if (ok) merge(state, f);
    }

    @Override
    public void visit(LTLOperatorF f) {
        vis.clear();
        LTLFormula op = f.getOperand();
        op.accept(this);
        for (State state : ks.getAllStates()) {
            if (!vis.contains(state)) {
                dfsF(state, f);
            }
        }
        DebugPrinter.print(logger, "end check LTLF:" + label);
    }

    private class Tarjan {
        private Map<State, Integer> dfn = new HashMap<>(), low = new HashMap<>(), stateInfo = new HashMap<>();
        private Set<State> inStack = new HashSet<>();
        private int timeStamp = 0;
        private List<State> stack = new ArrayList<>();
        private List<Set<State>> allScc = new ArrayList<>();

        List<Set<Integer>> dag;

        int getSccSize() {
            return allScc.size();
        }

        /// 获取某个state所在scc的所有states
        Set<State> getSccAllStates(State u) {
            return allScc.get(stateInfo.get(u));
        }

        Set<State> getSccAllStates(Integer i) {
            return allScc.get(i);
        }

        Set<Integer> getSuccessor(Integer i) {
            return dag.get(i);
        }

        /// 强连通块缩点操作
        void shrink(KripkeStructure ks) {
            for (State state : ks.getAllStates()) {
                if (!dfn.containsKey(state)) tarjan(state, ks);
            }
            dag = new ArrayList<>(); // kripke结构缩点后的有向无环图
            for (int i = 0; i < allScc.size(); i++) dag.add(new HashSet<>());
            Map<State, Set<State>> transitions = ks.getTransitions();
            for (State u : transitions.keySet()) {
                for (State v : transitions.get(u)) {
                    Integer scc1 = stateInfo.get(u), scc2 = stateInfo.get(v);
                    if (!scc1.equals(scc2)) {
                        Set<Integer> target = dag.get(scc1);
                        target.add(scc2);
                        dag.set(scc1, target);
                    }
                }
            }
//            for (State state : stateInfo.keySet()) {
//                DebugPrinter.print(logger, "state " + state + " belong to " + stateInfo.get(state));
//            }
//            for (int i = 0; i < dag.size(); i++) {
//                DebugPrinter.print(logger, i + " : " + dag.get(i));
//            }
        }

        private void tarjan(State u, KripkeStructure ks) {
            timeStamp++;
            dfn.put(u, timeStamp);
            low.put(u, timeStamp);
            stack.add(u);
            inStack.add(u);
            for (State v : ks.getAllSuccessorStates(u)) {
                if (!dfn.containsKey(v)) {
                    tarjan(v, ks);
                    low.put(u, Math.min(low.get(v), low.get(u)));
                }
                if (inStack.contains(v)) {
                    low.put(u, Math.min(dfn.get(v), low.get(u)));
                }
            }
            if (dfn.get(u).equals(low.get(u))) {
                Set<State> scc = new HashSet<>();
                while (stack.get(stack.size() - 1) != u) {
                    State state = stack.remove(stack.size() - 1);
                    scc.add(state);
                    inStack.remove(state);
                    stateInfo.put(state, allScc.size());
                }
                State state = stack.remove(stack.size() - 1);
                scc.add(state);
                inStack.remove(state);
                stateInfo.put(state, allScc.size());
                allScc.add(scc);
            }
        }
    }

    private Tarjan t = new Tarjan();

    @Override
    public void visit(LTLOperatorG f) {
        LTLFormula op = f.getOperand();
        op.accept(this);
        t.shrink(ks);
        Map<Integer, Boolean> rec = new HashMap<>();
        // tarjan算法保证枚举顺序就是逆拓扑序
        for (int i = 0; i < t.getSccSize(); i++) {
            boolean ok = true;
            for (State state : t.getSccAllStates(i)) {
                if (!satisfy(state, op)) {
                    ok = false;
                    break;
                }
            }
            if (ok) {
                for (Integer v : t.getSuccessor(i)) {
                    if (!rec.get(v)) {
                        ok = false;
                        break;
                    }
                }
            }
            rec.put(i, ok);
            if (ok) {
                for (State state : t.getSccAllStates(i)) {
                    merge(state, f);
                }
            }
        }
        DebugPrinter.print(logger, "end check LTLG:" + label);
    }
}
