package org.bigraph.bigsim.ctlspec.visitor;

import org.bigraph.bigsim.ctlspec.Formula;
import org.bigraph.bigsim.ctlspec.FormulaVisitor;
import org.bigraph.bigsim.ctlspec.atom.Atom;
import org.bigraph.bigsim.ctlspec.atom.False;
import org.bigraph.bigsim.ctlspec.atom.True;
import org.bigraph.bigsim.ctlspec.operator.*;
import org.bigraph.bigsim.transitionsystem.KripkeStructure;
import org.bigraph.bigsim.transitionsystem.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class ENFVisitor implements FormulaVisitor {
    private KripkeStructure ks;
    private HashMap<Formula, Set<State>> sat = new HashMap<>();
    private boolean debug = true;
    private static final Logger logger = LoggerFactory.getLogger(ENFVisitor.class);

    public ENFVisitor(KripkeStructure ks) {
        this.ks = ks;
    }

    private boolean merge(Formula key, State state) {
        Set<State> states = sat.getOrDefault(key, new HashSet<>());
        boolean added = states.add(state);
        sat.put(key, states);
        return added;
    }

    public boolean satisfy(Formula formula, State state) {
        return sat.containsKey(formula) && sat.get(formula).contains(state);
    }

    @Override
    public void visit(True f) {
        sat.put(f, ks.getAllStates());
        if (debug) logger.debug("end check True");
    }

    @Override
    public void visit(Atom f) {
        Set<State> valid = ks.getAllStates().stream().filter(state -> state.satisfies(f)).collect(Collectors.toSet());
        sat.put(f, valid);
        if (debug) logger.debug("end check Atom: " + valid);
    }

    @Override
    public void visit(And f) {
        f.getOperands().forEach(op -> op.accept(this));
        for (State state : ks.getAllStates()) {
            int tot = f.getOperands().size(), cnt = 0;
            // 该state满足And的sub-formula个数
            for (Formula subFormula : f.getOperands()) {
                if (satisfy(subFormula, state)) cnt++;
            }
            // 该state满足了所有sub-formula应该把这个state放入f的sat集合中
            if (tot == cnt) merge(f, state);
        }
        if (debug) logger.debug("end check And: " + sat.getOrDefault(f, new HashSet<>()));
    }

    @Override
    public void visit(Not f) {
        Formula op = f.getOperand();
        op.accept(this);
        for (State state : ks.getAllStates()) {
            if (!satisfy(op, state)) {
                merge(f, state);
            }
        }
        if (debug) logger.debug("end check Not: " + sat.getOrDefault(f, new HashSet<>()));
    }

    @Override
    public void visit(EX f) {
        Formula subFormula = f.getOperand();
        subFormula.accept(this);
        for (State state : ks.getAllStates()) {
            boolean valid = false;
            for (State next : ks.getAllSuccessorStates(state)) {
                if (satisfy(subFormula, next)) {
                    valid = true;
                    break;
                }
            }
            if (valid) merge(f, state);
        }
        if (debug) logger.debug("end check EX: " + sat.getOrDefault(f, new HashSet<>()));
    }

    @Override
    public void visit(EU f) {
        Formula op1 = f.getOperand1();
        Formula op2 = f.getOperand2();
        op1.accept(this);
        op2.accept(this);
        Set<State> vis = new HashSet<>();
        for (State state : sat.getOrDefault(op2, new HashSet<>())) {
            merge(f, state);
            vis.add(state);
        }
        // 不动点算法，不断添加后继在集合内的state
        Queue<State> workList = new LinkedList<>(sat.getOrDefault(op2, new HashSet<>()));
        while (!workList.isEmpty()) {
            State state = workList.poll();
            for (State pre : ks.getAllPreStates(state)) {
                if (!vis.contains(pre) && satisfy(op1, pre)) {
                    if (merge(f, pre)) {
                        workList.offer(pre);
                    }
                    vis.add(pre);
                }
            }
        }
        if (debug) logger.debug("end check EU: " + sat.getOrDefault(f, new HashSet<>()));
    }

    @Override
    public void visit(EG f) {
        Formula op = f.getOperand();
        op.accept(this);
        Map<State, Integer> cnt = new HashMap<>();
        Set<State> cur = sat.getOrDefault(op, new HashSet<>());
        Queue<State> workList = new LinkedList<>();
        // 计算出后继节点在集合中的个数
        for (State state : cur) {
            int inside = 0;
            for (State succ : ks.getAllSuccessorStates(state)) {
                if (cur.contains(succ)) inside++;
            }
            if (inside == 0) workList.add(state);
            else cnt.put(state, inside);
        }
        // 不动点算法，不断删除所有后继在集合外的state
        while (!workList.isEmpty()) {
            State state = workList.poll();
            for (State pre : ks.getAllPreStates(state)) {
                if (cnt.containsKey(pre)) {
                    int inside = cnt.get(pre);
                    inside--;
                    if (inside == 0) {
                        workList.offer(pre);
                        cnt.remove(pre);
                    } else cnt.put(pre, inside);
                }
            }
        }
        for (State state : cnt.keySet()) {
            if (cnt.get(state) != 0) {
                merge(f, state);
            }
        }
        if (debug) logger.debug("end check EG: " + sat.getOrDefault(f, new HashSet<>()));
    }

    @Override
    public void visit(False f) {
        throw new UnsupportedOperationException("False shouldn't occur in ENF");
    }

    @Override
    public void visit(EF f) {
        throw new UnsupportedOperationException("EF shouldn't occur in ENF");
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
