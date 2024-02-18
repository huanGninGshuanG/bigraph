package org.bigraph.bigsim.BRS;

import org.bigraph.bigsim.model.Bigraph;
import org.bigraph.bigsim.model.component.*;
import org.bigraph.bigsim.utils.BidMap;
import org.bigraph.bigsim.utils.DebugPrinter;
import org.bigraph.bigsim.utils.GlobalCfg;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author huangningshuang
 * @date 2023/12/4
 */
public class CSPMatcher {
    private static final Logger logger = LoggerFactory.getLogger(CSPMatcher.class);

    public Iterable<? extends CSPMatch> match(Bigraph agent, Bigraph redex) {
        return new MatchIterable(agent, redex);
    }

    private boolean areMatchable(Bigraph agent, Node fromAgent, Bigraph redex, Node fromRedex) {
        return fromAgent.getControl().equals(fromRedex.getControl());
    }

    private class MatchIterable implements Iterable<CSPMatch> {
        final Bigraph agent, redex;
        final List<? extends Root> agent_roots;
        final List<? extends Site> agent_sites;
        final Collection<? extends Node> agent_nodes;
        final Collection<Node.Port> agent_ports;
        final Collection<Point> agent_points;
        final Collection<? extends Edge> agent_edges;
        final List<Handle> agent_handles;

        final List<? extends Root> redex_roots;
        final List<? extends Site> redex_sites;
        final Collection<? extends Node> redex_nodes;
        final Collection<Point> redex_points;
        final Collection<? extends Edge> redex_edges;
        final List<Handle> redex_handles;

        /*
         * naming policy for sizes: a- agent r- redex -rs roots -ns nodes -ss
         * sites -hs handles -ps points -prs ports -ins bigInner() -ots bigOuter()
         */
        final int ars, ans, ass, ahs, aps, aprs, rrs, rns, rss, rhs, rps, rprs, rins;

        private MatchIterable(Bigraph agent, Bigraph redex) {
            if (!agent.bigSignature().equals(redex.bigSignature())) {
                throw new UnsupportedOperationException("Agent and redex should have the same singature.");
            }
            this.agent = agent;
            this.redex = redex;

            this.agent_roots = agent.getRoots();
            this.agent_nodes = agent.getNodes();
            this.agent_sites = agent.getSites();
            this.agent_edges = agent.getEdges();
            this.agent_handles = new LinkedList<>(agent_edges);
            agent_handles.addAll(agent.getOuterNames());

            ars = agent_roots.size();
            ans = agent_nodes.size();
            ass = agent_sites.size();
            ahs = agent_handles.size();

            this.agent_ports = new HashSet<>(2 * ans);
            this.agent_points = new HashSet<>(2 * ans);
            for (Node n : agent_nodes) {
                agent_ports.addAll(n.getPorts());
            }
            aprs = agent_ports.size();
            agent_points.addAll(agent_ports);
            agent_points.addAll(agent.getInnerNames());
            aps = agent_points.size();

            this.redex_roots = redex.getRoots();
            this.redex_sites = redex.getSites();
            this.redex_nodes = redex.getNodes();
            this.redex_edges = redex.getEdges();
            this.redex_handles = new LinkedList<>(redex_edges);
            redex_handles.addAll(redex.getOuterNames());

            rrs = redex_roots.size();
            rns = redex_nodes.size();
            rss = redex_sites.size();
            rhs = redex_handles.size();

            this.redex_points = new HashSet<>(rns);
            for (Node n : redex_nodes) {
                redex_points.addAll(n.getPorts());
            }
            rprs = redex_points.size(); // only ports
            redex_points.addAll(redex.getInnerNames());
            rps = redex_points.size();
            rins = rps - rprs;
        }

        @Override
        public Iterator<CSPMatch> iterator() {
            return new MatchIterator();
        }

        private class MatchIterator implements Iterator<CSPMatch> {

            private boolean mayHaveNext = true;
            private boolean firstRun = true;
            private CSPMatch nextMatch = null;
            final private Model model;
            final private Solver solver;

            /// place embedding中涉及到的变量
            final Map<PlaceEntity, Map<PlaceEntity, IntVar>> p_vars = new IdentityHashMap<>(ars + ans + ass);

            /// link embedding中涉及到的变量
            final Map<LinkEntity, Map<LinkEntity, IntVar>> e_vars = new IdentityHashMap<>(ahs * rhs + aps * (1 + rps));

            /// link embedding中涉及到的flux变量
            final Map<Handle, Map<Handle, IntVar>> f_vars = new IdentityHashMap<>(rhs);

            MatchIterator() {
                this.model = new Model();
                solver = instantiateModel();
                DebugPrinter.print(logger, "- MODEL CREATED ---------------------");
                DebugPrinter.print(logger, "- AGENT -----------------------------");
                DebugPrinter.print(logger, "agent:");
                agent.print();
                DebugPrinter.print(logger, "- REDEX -----------------------------");
                DebugPrinter.print(logger, "redex:");
                redex.print();
                DebugPrinter.print(logger, "-------------------------------------");
            }

            private Variable findVariable(String name, Variable[] vars) {
                for (Variable v : vars) {
                    if (name.equals(v.getName())) {
                        return v;
                    }
                }
                return null;
            }

            private Solver instantiateModel() {
                /// model中所有变量的设置
                {
                    int ki = 0;
                    for (Root i : agent_roots) {
                        int kj = 0;
                        Map<PlaceEntity, IntVar> row = new HashMap<>(rrs + rns + rss);
                        for (Root j : redex_roots) {
                            IntVar var = model.boolVar(ki + "_" + kj++);
                            row.put(j, var);
                        }
                        for (Node j : redex_nodes) {
                            IntVar var = model.boolVar(ki + "_" + kj++);
                            // constrain(22) redex的nodes不能映射到agent的roots
                            model.arithm(var, "=", 0).post();
                            row.put(j, var);
                        }
                        for (Site j : redex_sites) {
                            IntVar var = model.boolVar(ki + "_" + kj++);
                            // constrain(22) redex的sites不能映射到agent的roots
                            model.arithm(var, "=", 0).post();
                            row.put(j, var);
                        }
                        p_vars.put(i, row);
                        ki++;
                    }
                    for (Node i : agent_nodes) {
                        int kj = 0;
                        Map<PlaceEntity, IntVar> row = new HashMap<>(rrs + rns + rss);
                        for (Root j : redex_roots) {
                            IntVar var = model.boolVar(ki + "_" + kj++);
                            row.put(j, var);
                        }
                        for (Node j : redex_nodes) {
                            IntVar var = model.boolVar(ki + "_" + kj++);
                            row.put(j, var);
                        }
                        for (Site j : redex_sites) {
                            IntVar var = model.boolVar(ki + "_" + kj++);
                            row.put(j, var);
                        }
                        p_vars.put(i, row);
                        ki++;
                    }
                    for (Site i : agent_sites) {
                        int kj = 0;
                        Map<PlaceEntity, IntVar> row = new HashMap<>(rss);
                        // constrain(23) agent的sites不能映射到redex的nodes和roots
                        for (Site j : redex_sites) {
                            IntVar var = model.boolVar(ki + "_" + kj++);
                            row.put(j, var);
                        }
                        p_vars.put(i, row);
                        ki++;
                    }
                }

                {
                    int ki = 0;
                    /// constraint(4) flux相关变量设置
                    for (Handle hr : redex_handles) {
                        int kj = 0;
                        Map<Handle, IntVar> row = new IdentityHashMap<>(ahs);
                        for (Handle ha : agent_handles) {
                            IntVar var = model.boolVar("F_" + ki + "_" + kj++);
                            row.put(ha, var);
                        }
                        f_vars.put(hr, row);
                        ki++;
                    }

                    ki = 0;
                    for (Point pi : agent_points) {
                        int kj = 0;
                        Map<LinkEntity, IntVar> row = new IdentityHashMap<>(rps + 1);
                        Handle hi = pi.getHandle();
                        /// constraint(2) Nph'相关变量设置
                        IntVar var = model.boolVar("PH_" + ki);
                        row.put(hi, var);
                        for (Point pj : redex_points) {
                            /// constraint(3) Npp'相关变量设置
                            var = model.boolVar("PP_" + ki + "_" + kj++);
                            row.put(pj, var);
                        }
                        e_vars.put(pi, row);
                        ki++;
                    }

                    ki = 0;
                    for (Handle hj : redex_handles) {
                        int kj = 0;
                        Map<LinkEntity, IntVar> row = new IdentityHashMap<>(ahs);
                        for (Handle hi : agent_handles) {
                            /// constaint(1) Nhh'相关变量设置
                            IntVar var = model.intVar("HH_" + ki + "_" + kj++, 0, hi.getPoints().size());
                            row.put(hi, var);
                        }
                        e_vars.put(hj, row);
                        ki++;
                    }
                }

                /// 约束条件
                // Place Constraint
                {
                    for (Node i : agent_nodes) {
                        Parent f = i.getParent();
                        Map<PlaceEntity, IntVar> i_row = p_vars.get(i);
                        Map<PlaceEntity, IntVar> f_row = p_vars.get(f);
                        for (Child j : redex_nodes) {
                            Parent g = j.getParent();
                            /// todo: 匿名节点匹配配置项
                            if (!GlobalCfg.anonymousNode() && !i.getName().equals(((Node) j).getName())) {
                                model.arithm(i_row.get(j), "=", 0).post();
                            }
                            /// constraint(26) nodes-nodes部分：父节点匹配该节点才可能匹配
                            model.arithm(i_row.get(j), "<=", f_row.get(g)).post();
                        }
                        for (Child j : redex_sites) {
                            Parent g = j.getParent();
                            /// constraint(26) nodes-sites部分：父节点匹配该节点才可能匹配
                            model.arithm(i_row.get(j), "<=", f_row.get(g)).post();
                        }
                    }
                    for (Site i : agent_sites) {
                        Parent f = i.getParent();
                        Map<PlaceEntity, IntVar> i_row = p_vars.get(i);
                        Map<PlaceEntity, IntVar> f_row = p_vars.get(f);
                        for (Child j : redex_sites) {
                            Parent g = j.getParent();
                            /// constraint(26) sites-sites部分：父节点匹配该节点才可能匹配
                            model.arithm(i_row.get(j), "<=", f_row.get(g)).post();
                        }
                    }
                }

                {
                    // 在place graph树形结构上广搜，将passive nodes视为叶节点
                    Deque<Node> qa = new ArrayDeque<>(); // active nodes
                    Deque<Child> qp = new ArrayDeque<>(); // passive nodes
                    for (Root r : agent_roots) {
                        for (Child c : r.getChildren()) {
                            if (c.isNode()) {
                                qa.add((Node) c);
                            }
                        }
                    }
                    while (!qa.isEmpty()) {
                        Node n = qa.poll();
                        if (n.getControl().isActive()) {
                            for (Child c : n.getChildren()) {
                                if (c.isNode()) {
                                    qa.add((Node) c);
                                }
                            }
                        } else {
                            qp.add(n);
                        }
                    }
                    while (!qp.isEmpty()) {
                        Child i = qp.poll();
                        Map<PlaceEntity, IntVar> row = p_vars.get(i);
                        for (Root j : redex_roots) {
                            // constraint(25) passive节点不能参与匹配
                            model.arithm(row.get(j), "=", 0).post();
                        }
                        if (i.isNode()) {
                            // passive node下的所有节点都是passive节点
                            for (Child c : ((Node) i).getChildren()) {
                                if (c.isNode()) {
                                    qp.add(c);
                                }
                            }
                        }
                    }
                }

                {
                    IntVar[] vars = new IntVar[ars + ans];
                    for (Root j : redex_roots) {
                        int k = 0;
                        for (PlaceEntity i : p_vars.keySet()) {
                            if (i.isSite())
                                continue;
                            vars[k++] = p_vars.get(i).get(j);
                        }
                        // constraint(27) 保证redex sites只和一个agent中nodes或roots匹配
                        model.sum(vars, "=", 1).post();
                    }
                    vars = new IntVar[ans + ass];
                    for (Node j : redex_nodes) {
                        int k = 0;
                        for (PlaceEntity i : p_vars.keySet()) {
                            if (i.isRoot())
                                continue;
                            vars[k++] = p_vars.get(i).get(j);
                        }
                        // constraint(28) 保证redex nodes只和一个agent中的sites或nodes匹配
                        model.sum(vars, "=", 1).post();
                    }
                }

                {
                    for (Node i : agent_nodes) {
                        Map<PlaceEntity, IntVar> row = p_vars.get(i);
                        IntVar[] vars = new IntVar[rns + rss];
                        int k = 0;
                        for (PlaceEntity j : redex_nodes) {
                            vars[k++] = row.get(j);
                        }
                        for (PlaceEntity j : redex_sites) {
                            vars[k++] = row.get(j);
                        }
                        IntVar t1 = model.intVar(rrs);
                        IntVar c = model.intVar(0);
                        for (IntVar v : vars) {
                            c = c.add(v).intVar();
                        }
                        c = c.mul(t1).intVar();

                        vars = new IntVar[rrs];
                        k = 0;
                        for (Root j : redex_roots) {
                            vars[k++] = row.get(j);
                        }

                        t1 = model.intVar(0);
                        for (IntVar v : vars) {
                            t1 = t1.add(v).intVar();
                        }
                        t1 = t1.add(c).intVar();
                        // constraint(29) agent中的nodes不能同时和redex中的nodes/sites以及root匹配
                        model.arithm(t1, "<=", rrs).post();
                    }
                }

                {
                    for (Parent f : agent_nodes) {
                        Collection<? extends Child> cf = f.getChildren();
                        for (Parent g : redex_nodes) {
                            Collection<? extends Child> cg = g.getChildren();
                            IntVar[] vars = new IntVar[cf.size() * cg.size()];
                            int k = 0;
                            for (PlaceEntity i : cf) {
                                for (PlaceEntity j : cg) {
                                    vars[k++] = p_vars.get(i).get(j);
                                }
                            }
                            IntVar chld = p_vars.get(f).get(g);
                            // constraint(30) 某个nodes匹配，所有的child也必须匹配
                            model.sum(vars, ">=", chld.mul(cf.size()).intVar()).post();
                        }
                    }
                }

                {
                    Map<Root, Collection<? extends Child>> cgs = new HashMap<>(rrs);
                    for (Root g : redex_roots) {
                        Collection<? extends Child> cg = new HashSet<>(g.getChildren());
                        cg.removeAll(redex_sites);
                        cgs.put(g, cg);
                    }
                    for (PlaceEntity f : p_vars.keySet()) {
                        if (f.isSite())
                            continue;
                        Collection<? extends Child> cf = ((Parent) f).getChildren();
                        for (Root g : redex_roots) {
                            Collection<? extends Child> cg = cgs.get(g);
                            IntVar[] vars = new IntVar[cf.size() * cg.size()];
                            int k = 0;
                            for (Child i : cf) {
                                for (Child j : cg) {
                                    vars[k++] = p_vars.get(i).get(j);
                                }
                            }
                            IntVar chld = p_vars.get(f).get(g);
                            // constraint(31) 和30类似，施加在redex roots和agent nodes上
                            model.sum(vars, ">=", chld.mul(cg.size()).intVar()).post();
                        }
                    }
                }

                {
                    for (Node i : agent_nodes) {
                        Collection<Parent> ancs = agent.getAncestors(i);
                        IntVar[] vars = new IntVar[(ancs.size() + 1) * rss];
                        int k = 0;
                        for (Parent f : ancs) {
                            if (f.isNode()) {
                                Map<PlaceEntity, IntVar> f_row = p_vars.get(f);
                                for (Site g : redex_sites) {
                                    vars[k++] = f_row.get(g);
                                }
                            }
                        }
                        Map<PlaceEntity, IntVar> i_row = p_vars.get(i);
                        for (Site g : redex_sites) {
                            vars[k++] = i_row.get(g);
                        }

                        IntVar sum = model.intVar(0);
                        for (IntVar v : vars) {
                            sum = sum.add(v).intVar();
                        }
                        for (Root j : redex_roots) {
                            // constraint(32) agent nodes在redex的sites中，就不能与redex中的nodes和roots匹配
                            model.arithm(sum.add(i_row.get(j)).intVar(), "<=", 1).post();
                        }
                    }
                }

                {
                    IntVar[] vars = new IntVar[rrs + rns + rss];
                    for (Site i : agent_sites) {
                        vars = p_vars.get(i).values().toArray(vars);
                        // agent的site只能映射到redex的site
                        model.sum(vars, "<=", 1).post();
                    }
                }

                /// LINK Constraints 看成网络流问题
                {
                    IntVar[] vars = new IntVar[rps + 1];
                    for (Point p : agent_points) {
                        vars = e_vars.get(p).values().toArray(vars);
                        // constraint(5) source为agent的point，发送一个单位
                        model.sum(vars, "=", 1).post();
                    }
                }

                {
                    for (Handle ha : agent_handles) {
                        Collection<? extends Point> ps = ha.getPoints();
                        IntVar[] vars1 = new IntVar[rhs + ps.size()];
                        int k = 0;
                        for (Point p : ps) {
                            vars1[k++] = e_vars.get(p).get(ha);
                        }
                        for (Handle hr : redex_handles) {
                            vars1[k++] = e_vars.get(hr).get(ha);
                        }
                        // constraint(6) sink为agent的handle h,接受|LINK(h)|个单位
                        model.sum(vars1, "=", ps.size()).post();
                    }
                }

                {
                    IntVar[] vars1 = new IntVar[ahs];
                    for (Handle hr : redex_handles) {
                        Collection<? extends Point> ps = hr.getPoints();
                        int k = 0;
                        IntVar[] vars2 = new IntVar[aps * ps.size()];
                        for (Point pa : agent_points) {
                            Map<LinkEntity, IntVar> row = e_vars.get(pa);
                            for (Point pr : ps) {
                                vars2[k++] = row.get(pr);
                            }
                        }
                        vars1 = e_vars.get(hr).values().toArray(vars1);
                        IntVar sum1 = model.intVar(0);
                        for (IntVar v : vars1) {
                            sum1 = sum1.add(v).intVar();
                        }
                        // constraint(7) redex handle的output与其相连的points的input相等
                        model.sum(vars2, "=", sum1).post();
                    }
                }

                {
                    IntVar[] vars = new IntVar[aps];
                    for (Point pr : redex_points) {
                        if (pr.isPort()) { //todo: 这里应该是innername?
                            int k = 0;
                            for (Point pa : agent_points) {
                                vars[k++] = e_vars.get(pa).get(pr);
                            }
                            // constraint(8) redex ports只与一个agent points匹配?
                            model.sum(vars, "=", 1).post();
                        }
                    }
                }
                {
                    IntVar[] vars = new IntVar[aprs];
                    for (Point pr : redex_points) {
                        if (pr.isInnerName()) { // todo: 这里应该是port?
                            int k = 0;
                            for (Point pa : agent_ports) {
                                vars[k++] = e_vars.get(pa).get(pr);
                            }
                            // constraint(8) redex innername只与一个agent ports匹配?
                            model.sum(vars, "=", 1).post();
                        }
                    }
                }

                {
                    for (Handle hr : redex_handles) {
                        Map<Handle, IntVar> f_row = f_vars.get(hr);
                        Map<LinkEntity, IntVar> e_row = e_vars.get(hr);
                        if (!hr.getPoints().isEmpty()) {
                            for (Handle ha : agent_handles) {
                                if (!ha.getPoints().isEmpty()) {
                                    IntVar vf = f_row.get(ha);
                                    IntVar ve = e_row.get(ha);
                                    // constraint(10)
                                    model.arithm(ve, "<=", vf.mul(ha.getPoints().size()).intVar()).post();
                                    model.arithm(vf, "<=", ve).post();
                                }
                            }
                        }
                    }
                }

                {
                    for (Handle hr : redex_handles) {
                        Map<Handle, IntVar> f_row = f_vars.get(hr);
                        Collection<? extends Point> ps = hr.getPoints();
                        for (Handle ha : agent_handles) {
                            IntVar vf = f_row.get(ha);
                            int k = 0;
                            IntVar[] vars = new IntVar[ps.size() * ha.getPoints().size()];
                            for (Point pa : ha.getPoints()) {
                                Map<LinkEntity, IntVar> e_row = e_vars.get(pa);
                                for (Point pr : ps) {
                                    IntVar ve = e_row.get(pr);
                                    vars[k++] = ve;
                                    // constraint(11)
                                    model.arithm(ve, "<=", vf).post();
                                }
                                if (hr.isEdge()) {
                                    // constraint(14) Fhh'为1，那么就必须detour redex，Nph=0
                                    model.arithm(vf.add(e_vars.get(pa).get(ha)).intVar(), "<=", 1).post();
                                }
                            }
                            if (!ps.isEmpty() || !ha.getPoints().isEmpty())
                                // constraint(12)
                                model.sum(vars, ">=", vf).post();
                        }
                    }
                }

                {
                    if (ahs != 0) {
                        IntVar[] vars = new IntVar[ahs];
                        for (Handle hr : redex_handles) {
                            Map<Handle, IntVar> f_row = f_vars.get(hr);
                            // constraint(13) 保证redex handle只映射到一个agent handle
                            model.sum(f_row.values().toArray(vars), "<=", 1).post();
                        }
                    }

                }

                {
                    ListIterator<Handle> ir1 = redex_handles.listIterator(0);
                    while (ir1.hasNext()) {
                        Handle hr1 = ir1.next();
                        Map<Handle, IntVar> f_row1 = f_vars.get(hr1);
                        if (hr1.isEdge()) {
                            for (Handle ha : agent_handles) {
                                // constraint(16) redex edge只能和agent edge匹配
                                if (ha.isOuterName()) model.arithm(f_row1.get(ha), "=", 0).post();
                            }
                        }
                        ListIterator<Handle> ir2 = redex_handles.listIterator(ir1.nextIndex());
                        while (ir2.hasNext()) {
                            Handle hr2 = ir2.next();
                            Map<Handle, IntVar> f_row2 = f_vars.get(hr2);
                            if (hr1.isEdge() != hr2.isEdge()) {
                                for (Handle ha : agent_handles) {
                                    // constraint(15) 保证agent的handle不能同时匹配redex edge和outername
                                    model.arithm(f_row1.get(ha).add(f_row2.get(ha)).intVar(), "<=", 1).post();
                                }
                            }
                        }
                    }
                }
                {
                    IntVar[] vars = new IntVar[redex_edges.size()];
                    for (Handle ha : agent_handles) {
                        int k = 0;
                        for (Handle hr : redex_edges) {
                            vars[k++] = f_vars.get(hr).get(ha);
                        }
                        // constraint(17) redex edge最多映射一个edge
                        if (redex_edges.size() > 0)
                            model.sum(vars, "<=", 1).post();
                    }
                }

                {
                    for (Node ni : agent_nodes) {
                        Map<PlaceEntity, IntVar> p_row = p_vars.get(ni);
                        for (Node nj : redex_nodes) {
                            IntVar m = p_row.get(nj);
                            boolean comp = areMatchable(agent, ni, redex, nj);
                            // constraint(24) 两个匹配的nodes control要一样
                            if (!comp) {
                                model.arithm(m, "=", 0).post();
                            }
                            for (int i = ni.getControl().getArity() - 1; 0 <= i; i--) {
                                Map<LinkEntity, IntVar> e_row = e_vars.get(ni.getPort(i));
                                for (int j = nj.getControl().getArity() - 1; 0 <= j; j--) {
                                    if (comp && i == j) {
                                        // constraint(18,19,33) 两个nodes匹配，对应的ports也要匹配
                                        model.arithm(e_row.get(nj.getPort(j)), "=", m).post();
                                    } else {
                                        model.arithm(e_row.get(nj.getPort(j)), "=", 0).post();
                                    }
                                }
                            }
                        }
                    }
                }

                {
                    for (Node ni : agent_nodes) {
                        Collection<Parent> ancs = agent.getAncestors(ni);
                        IntVar[] vars2 = new IntVar[(1 + ancs.size()) * rss];
                        int k2 = 0;
                        for (Parent f : ancs) {
                            Map<PlaceEntity, IntVar> row = p_vars.get(f);
                            for (Site g : redex_sites) {
                                vars2[k2++] = row.get(g);
                            }
                        }
                        {
                            Map<PlaceEntity, IntVar> row = p_vars.get(ni);
                            for (Site g : redex_sites) {
                                vars2[k2++] = row.get(g);
                            }
                        }
                        IntVar sum2 = model.intVar(0);
                        for (IntVar v : vars2) {
                            sum2 = sum2.add(v).intVar();
                        }

                        for (Node.Port pi : ni.getPorts()) {
                            Map<LinkEntity, IntVar> row = e_vars.get(pi);
                            IntVar[] vars4 = new IntVar[rins];
                            int k4 = 0;
                            for (Point in : redex.getInnerNames()) {
                                IntVar var = row.get(in);
                                vars4[k4++] = var;
                            }
                            // constraint(34) redex对应的innername，要么是agent的innername，要么在redex某个site的image下
                            model.sum(vars4, "<=", sum2).post();
                        }
                    }
                }

                return model.getSolver();
            }

            @Override
            public boolean hasNext() {
                if (mayHaveNext && nextMatch == null) {
                    fetchSolution();
                }
                return mayHaveNext && nextMatch != null;
            }

            private void fetchSolution() {
                firstRun = false;
                boolean hasSolution = solver.solve();
                DebugPrinter.print(logger, "hasSolution: " + hasSolution);
                if (!hasSolution) {
                    noMoreSolution();
                    return;
                }
//                printCSPSolution();

                // context
                Bigraph ctx = new Bigraph();
                ctx.setSignature(agent.getSignature());
                // redex
                Bigraph rdx = new Bigraph();
                rdx.setSignature(agent.getSignature());
                // parameters
                Bigraph[] prms = new Bigraph[rss];
                // linking medianting between parameters and redex+ID
                Bigraph lmb = Bigraph.makeId(redex.getSignature(), rss, new ArrayList<>());
                Bigraph id = Bigraph.makeEmpty(redex.getSignature());
                // an injective map from redex's nodes to rdx's ones
                BidMap<Node, Node> nEmb = new BidMap<>(rns);

                // replicated sites
                Site ctx_sites_dic[] = new Site[rrs];
                Site rdx_sites_dic[] = new Site[rss];
                Root rdx_roots_dic[] = new Root[rrs];

                // replicated handles lookup tables
                Map<Handle, Handle> ctx_hnd_dic = new IdentityHashMap<>();
                Map<Handle, Handle> rdx_hnd_dic = new IdentityHashMap<>();
                Map<Handle, Handle> lmb_hnd_dic = new IdentityHashMap<>();
                Map<Bigraph, Map<Handle, Handle>> prms_hnd_dic = new IdentityHashMap<>();

                Map<Handle, Handle> handle_img = new IdentityHashMap<>(rhs);

                class VState {
                    final PlaceEntity c; // the agent root/node to be visited
                    final PlaceEntity i; // if present, is the image of c in the redex
                    final Parent p; // the replicated parent
                    final Bigraph b;

                    VState(Bigraph b, Parent p, PlaceEntity c) {
                        this(b, p, c, null);
                    }

                    VState(Bigraph b, Parent p, PlaceEntity c,
                           PlaceEntity i) {
                        this.i = i;
                        this.c = c;
                        this.p = p;
                        this.b = b;
                    }
                }
                Deque<VState> q = new ArrayDeque<>();

                for (OuterName o1 : agent.bigOuter().values()) {
                    OuterName o2 = o1.replicate();
                    ctx.bigOuter().put(o2.getName(), o2);
                    ctx_hnd_dic.put(o1, o2);
                }
                for (OuterName o0 : redex.bigOuter().values()) {
                    // 反应物拷贝redex的外部名
                    String name = o0.getName();
                    OuterName o2 = new OuterName(name);
                    rdx.bigOuter().put(name, o2);
                    rdx_hnd_dic.put(o0, o2);
                    // 上下文ctx的内部名
                    InnerName i1 = new InnerName(name);
                    ctx.bigInner().put(name, i1);
                    // 找到i1的柄
                    Handle h1 = handle_img.get(o0);
                    if (h1 == null) {
                        // cache miss
                        Map<Handle, IntVar> f_row = f_vars.get(o0);
                        for (Handle h : agent_handles) {
                            IntVar v = findVariable(f_row.get(h).getName(), model.getVars()).asIntVar();
                            if (v.getValue() == 1) {
                                h1 = h;
                                break;
                            }
                        }
                        if (h1 == null) {
                            h1 = new Edge();
                        }
                        handle_img.put(o0, h1);
                    }
                    Handle h2 = ctx_hnd_dic.get(h1);
                    if (h2 == null) {
                        h2 = h1.replicate();
                        ctx_hnd_dic.put(h1, h2);
                    }
                    i1.setHandle(h2);
                }
                for (InnerName i0 : redex.bigInner().values()) {
                    String name = i0.getName();
                    InnerName i2 = new InnerName(name);
                    // 找到i2的handle
                    Handle h0 = i0.getHandle();
                    Handle h2 = rdx_hnd_dic.get(h0);
                    if (h2 == null) {
                        Handle h1 = handle_img.get(h0);
                        if (h1 == null) {
                            // cache miss
                            Map<Handle, IntVar> f_row = f_vars.get(h0);
                            for (Handle h : agent_handles) {
                                IntVar v = findVariable(f_row.get(h).getName(), model.getVars()).asIntVar();
                                if (v.getValue() == 1) {
                                    h1 = h;
                                    break;
                                }
                            }
                            if (h1 == null) {
                                h1 = new Edge();
                            }
                            handle_img.put(h0, h1);
                        }
                        h2 = h1.replicate();
                        rdx_hnd_dic.put(h0, h2);
                    }
                    i2.setHandle(h2);
                    rdx.bigInner().put(name, i2);

                    OuterName o2 = new OuterName(name);
                    lmb.bigOuter().put(name, o2);
                }
                for (Root r0 : agent.bigRoots()) {
                    q.add(new VState(ctx, null, r0));
                }
                Collection<Root> unseen_rdx_roots = new LinkedList<>(redex_roots);
                while (!q.isEmpty()) {
                    VState v = q.poll();
                    if (v.b == ctx) {
                        // 元素属于上下文ctx
                        Parent p1 = (Parent) v.c;
                        Parent p2 = p1.replicate();
                        if (p1.isRoot()) {
                            Root r2 = (Root) p2;
                            ctx.bigRoots().add(r2);
                        } else { // isNode()
                            Node n1 = (Node) p1;
                            Node n2 = (Node) p2;
                            n2.setParent(v.p);
                            // 拷贝端口的每个handle
                            for (int i = n1.getControl().getArity() - 1; -1 < i; i--) {
                                Node.Port o = n1.getPort(i);
                                Handle h1 = o.getHandle();
                                // looks for an existing replica
                                Handle h2 = ctx_hnd_dic.get(h1);
                                if (h2 == null) {
                                    h2 = h1.replicate();
                                    ctx_hnd_dic.put(h1, h2);
                                }
                                n2.getPort(i).setHandle(h2);
                            }
                        }
                        // enqueue children
                        Collection<Child> rcs = new HashSet<>(p1.getChildren());
                        Map<PlaceEntity, IntVar> p_row = p_vars.get(p1);
                        Iterator<Root> ir = unseen_rdx_roots.iterator();
                        while (ir.hasNext()) {
                            Root r0 = ir.next();
                            IntVar var = findVariable(p_row.get(r0).getName(), model.getVars()).asIntVar();
                            // make a site for each root whose image is p1
                            if (var.getValue() == 1) {
                                ir.remove();
                                int k = redex_roots.indexOf(r0);
                                Site s = new Site();
                                s.setParent(p2);
                                ctx_sites_dic[k] = s;
                                Root r2 = new Root();
                                rdx_roots_dic[k] = r2;
                                for (Child c0 : r0.getChildren()) {
                                    Iterator<Child> ic = rcs.iterator();
                                    boolean notMatched = true;
                                    while (ic.hasNext()) {
                                        Child c1 = ic.next();
                                        var = findVariable(p_vars.get(c1).get(c0).getName(), model.getVars()).asIntVar();
                                        if (var.getValue() == 1) {
                                            notMatched = false;
                                            q.add(new VState(rdx, r2, c1, c0));
                                            ic.remove();
                                        }
                                    }
                                    if (notMatched && c0.isSite()) {
                                        // closed site
                                        q.add(new VState(rdx, r2, null, c0));
                                    }
                                }
                            }
                        }
                        for (Child c1 : rcs) {
                            q.add(new VState(ctx, p2, c1));
                        }
                    } else if (v.b == rdx) {
                        // 放入上下文拷贝rdx中
                        if (v.i.isNode()) {
                            Node n0 = (Node) v.i;
                            Node n1 = (Node) v.c;
                            Node n2 = n1.replicate();
                            nEmb.put(n0, n1);
                            n2.setParent(v.p);
                            // replicate links from node ports
                            for (int i = n0.getControl().getArity() - 1; -1 < i; i--) {
                                Node.Port o0 = n0.getPort(i);
                                Handle h0 = o0.getHandle();
                                // looks for an existing replica
                                Handle h2 = rdx_hnd_dic.get(h0);
                                if (h2 == null) {
                                    h2 = n1.getPort(i).getHandle().replicate();
                                    rdx_hnd_dic.put(h0, h2);
                                }
                                n2.getPort(i).setHandle(h2);
                            }
                            Collection<Child> cs1 = new HashSet<>(n1.getChildren());
                            for (Child c0 : n0.getChildren()) {
                                Iterator<Child> ic = cs1.iterator();
                                boolean notMatched = true;
                                while (ic.hasNext()) {
                                    Child c1 = ic.next();
                                    IntVar var = findVariable(p_vars.get(c1).get(c0).getName(), model.getVars()).asIntVar();
                                    if (var.getValue() == 1) {
                                        notMatched = false;
                                        q.add(new VState(rdx, n2, c1, c0));
                                        ic.remove();
                                    }
                                }
                                if (notMatched && c0.isSite()) {
                                    // closed site
                                    q.add(new VState(rdx, n2, null, c0));
                                }
                            }
                        } else {
                            Site s0 = (Site) v.i;
                            int k = redex_sites.indexOf(s0);
                            if (rdx_sites_dic[k] == null) {
                                Site s2 = new Site();
                                s2.setParent(v.p);
                                rdx_sites_dic[k] = s2;
                            }
                            Bigraph prm = prms[k];
                            if (prm == null) {
                                prm = new Bigraph();
                                prm.setSignature(agent.getSignature());
                                prm.bigRoots().add(new Root(prm));
                                prms[k] = prm;
                                prms_hnd_dic.put(prm, new IdentityHashMap<>());
                            }
                            if (v.c != null)
                                q.add(new VState(prm, prm.bigRoots().get(0), v.c));
                        }
                    } else {
                        // the entity (node) visited belongs to some parameter
                        Node n1 = (Node) v.c;
                        Node n2 = n1.replicate();
                        n2.setParent(v.p);
                        for (int i = n1.getControl().getArity() - 1; -1 < i; i--) {
                            /*
                             * every handle with a point in the param is
                             * translated into an outer and the necessary wiring
                             * is delegated to the bigraph lambda.
                             */
                            Node.Port p1 = n1.getPort(i);
                            Node.Port p2 = n2.getPort(i);

                            Handle h2 = null;
                            Map<Handle, Handle> hnd_dic = prms_hnd_dic.get(v.b);
                            Map<LinkEntity, IntVar> row = e_vars.get(p1);
                            Handle h1 = p1.getHandle();

                            IntVar var_tmp = findVariable(row.get(h1).getName(), model.getVars()).asIntVar();
                            if (var_tmp.getValue() == 1) {
                                /*
                                 * this port bypasses the redex. Checks if the
                                 * handle already has an image in this parameter
                                 * otherwise creates a suitable name in the
                                 * parameter and in the wiring lambda. This may
                                 * require some additional step if the handle
                                 * already has an image in the context.
                                 */
                                h2 = hnd_dic.get(h1);
                                if (h2 == null) {
                                    Handle h3 = lmb_hnd_dic.get(h1);
                                    if (h3 == null) {
                                        Handle h4 = ctx_hnd_dic.get(h1);
                                        if (h4 != null) {
                                            /*
                                             * h1 has an image in the context,
                                             * add an inner to it and link it
                                             * down to the parameter passing
                                             * through id e lmb.
                                             */
                                            InnerName i4 = new InnerName();
                                            i4.setHandle(h4);
                                            String name = i4.getName();
                                            ctx.bigInner().put(name, i4);
                                            // add it also to id
                                            OuterName o5 = new OuterName(name);
                                            id.bigOuter().put(name, o5);
                                            InnerName i5 = new InnerName(name);
                                            i5.setHandle(o5);
                                            id.bigInner().put(name, i5);
                                            // and finally to lambda
                                            OuterName o3 = new OuterName(name);
                                            lmb.bigOuter().put(name, o3);
                                            h3 = o3;
                                        } else {
                                            /*
                                             * 上下文中没有对应的映射，lmb要完成闭包closure操作
                                             */
                                            h3 = new Edge();
                                        }
                                        lmb_hnd_dic.put(h1, h3);
                                    }
                                    InnerName i3 = new InnerName(h3);
                                    String name = i3.getName();
                                    lmb.bigInner().put(name, i3);
                                    OuterName o2 = new OuterName(name);
                                    v.b.bigOuter().put(name, o2);
                                    h2 = o2;
                                    hnd_dic.put(h1, h2);
                                }
                            } else {
                                for (InnerName i0 : redex.bigInner().values()) {
                                    IntVar var = findVariable(row.get(i0).getName(), model.getVars()).asIntVar();
                                    if (var.getValue() == 1) {
                                        Handle h3 = lmb.bigOuter().get(i0.getName());
                                        h2 = hnd_dic.get(h3);
                                        if (h2 == null) {
                                            InnerName i3 = new InnerName();
                                            String name = i3.getName();
                                            i3.setHandle(h3);
                                            lmb.bigInner().put(name, i3);
                                            OuterName o2 = new OuterName(name);
                                            v.b.bigOuter().put(name, o2);
                                            h2 = o2;
                                            hnd_dic.put(h3, o2);
                                        }
                                        break;
                                    }
                                }
                            }
                            p2.setHandle(h2);
                        }
                        for (Child c1 : n1.getChildren()) {
                            q.add(new VState(v.b, n2, c1));
                        }
                    }
                }
                ctx.bigSites().addAll(Arrays.asList(ctx_sites_dic));
                rdx.bigSites().addAll(Arrays.asList(rdx_sites_dic));
                rdx.bigRoots().addAll(Arrays.asList(rdx_roots_dic));

                if (!ctx.isConsistent()) {
                    throw new RuntimeException("Inconsistent bigraph (ctx)");
                }
                if (!rdx.isConsistent()) {
                    throw new RuntimeException("Inconsistent bigraph (rdx)");
                }
                if (!lmb.isConsistent()) {
                    throw new RuntimeException("Inconsistent bigraph (lmb)");
                }
                if (!id.isConsistent()) {
                    throw new RuntimeException("Inconsistent bigraph (id)");
                }
                for (int i = 0; i < rss; i++) {
                    if (!prms[i].isConsistent()) {
                        throw new RuntimeException(
                                "Inconsistent bigraph (prm " + i + ")");
                    }
                }
                DebugPrinter.print(logger, "ctx info:");
                ctx.print();

                DebugPrinter.print(logger, "rdx info:");
                rdx.print();

                DebugPrinter.print(logger, "lmb info:");
                lmb.print();

                for (int i = 0; i < rss; i++) {
                    DebugPrinter.print(logger, "prm " + i + " info:");
                    prms[i].print();
                }

                this.nextMatch = new CSPMatch(ctx, rdx, id, lmb, prms, nEmb);
            }

            private void printCSPSolution() {
                DebugPrinter.print(logger, "Solution: #" + solver.getSolutionCount());
                int p_cell_width[] = new int[1 + rrs + rns + rss];
                p_cell_width[0] = 6;
                for (Node n : agent_nodes) {
                    p_cell_width[0] = Math.max(p_cell_width[0], n.toString().length());
                }
                System.out.printf("%-" + p_cell_width[0] + "s|", "P_VARS");
                int c = 1;
                for (int k = 0; k < rrs; k++, c++) {
                    String s = "R_" + k;
                    p_cell_width[c] = s.length();
                    System.out.printf("%-" + p_cell_width[c] + "s|", s);
                }
                for (Node n : redex_nodes) {
                    String s = n.toString();
                    p_cell_width[c] = s.length();
                    System.out.printf("%-" + p_cell_width[c++] + "s|", s);
                }
                for (int k = 0; k < rss; k++, c++) {
                    String s = "S_" + k;
                    p_cell_width[c] = s.length();
                    System.out.printf("%-" + p_cell_width[c] + "s|", s);
                }
                for (int i = 0; i < ars; i++) {
                    System.out.printf("\nR_%-" + (p_cell_width[0] - 2)
                            + "d|", i);
                    c = 1;
                    Root ri = agent_roots.get(i);
                    Map<PlaceEntity, IntVar> row = p_vars.get(ri);
                    for (int j = 0; j < rrs; j++) {
                        Root rj = redex_roots.get(j);
                        IntVar v = findVariable(row.get(rj).getName(),
                                model.getVars()).asIntVar();
                        System.out.printf("%" + p_cell_width[c++] + "d|",
                                v.getValue());
                    }
                    for (Node nj : redex_nodes) {
                        IntVar v = findVariable(row.get(nj).getName(),
                                model.getVars()).asIntVar();
                        System.out.printf("%" + p_cell_width[c++] + "d|",
                                v.getValue());
                    }
                    for (int j = 0; j < rss; j++) {
                        Site sj = redex_sites.get(j);
                        IntVar v = findVariable(row.get(sj).getName(),
                                model.getVars()).asIntVar();
                        System.out.printf("%" + p_cell_width[c++] + "d|",
                                v.getValue());
                    }
                }
                for (Node ni : agent_nodes) {
                    System.out.printf("\n%-" + p_cell_width[0] + "s|", ni);
                    c = 1;
                    Map<PlaceEntity, IntVar> row = p_vars.get(ni);
                    for (int j = 0; j < rrs; j++) {
                        Root rj = redex_roots.get(j);
                        IntVar v = findVariable(row.get(rj).getName(),
                                model.getVars()).asIntVar();
                        System.out.printf("%" + p_cell_width[c++] + "d|",
                                v.getValue());
                    }
                    for (Node nj : redex_nodes) {
                        IntVar v = findVariable(row.get(nj).getName(),
                                model.getVars()).asIntVar();
                        System.out.printf("%" + p_cell_width[c++] + "d|",
                                v.getValue());
                    }
                    for (int j = 0; j < rss; j++) {
                        Site sj = redex_sites.get(j);
                        IntVar v = findVariable(row.get(sj).getName(),
                                model.getVars()).asIntVar();
                        System.out.printf("%" + p_cell_width[c++] + "d|",
                                v.getValue());
                    }
                }
                for (int i = 0; i < ass; i++) {
                    System.out.printf("\nS_%-" + (p_cell_width[0] - 2)
                            + "d|", i);
                    c = 1;
                    Root ri = agent_roots.get(i);
                    Map<PlaceEntity, IntVar> row = p_vars.get(ri);
                    for (int j = 0; j < rrs; j++) {
                        System.out.printf("%" + p_cell_width[c++] + "d|",
                                ' ');
                    }
                    for (int j = 0; j < rns; j++) {
                        System.out.printf("%" + p_cell_width[c++] + "d|",
                                ' ');
                    }
                    for (int j = 0; j < rss; j++) {
                        Site sj = redex_sites.get(j);
                        IntVar v = findVariable(row.get(sj).getName(),
                                model.getVars()).asIntVar();
                        System.out.printf("%" + p_cell_width[c++] + "d|",
                                v.getValue());
                    }
                }
                System.out.println('\n');

                int f_cell_width[] = new int[1 + ahs];
                int e_cell_width[] = new int[1 + rps + ahs];
                f_cell_width[0] = 6;
                for (Handle n : redex_handles) {
                    f_cell_width[0] = Math.max(f_cell_width[0], n
                            .toString().length());
                }
                e_cell_width[0] = f_cell_width[0];
                for (Point n : agent_points) {
                    e_cell_width[0] = Math.max(e_cell_width[0], n
                            .toString().length());
                }
                System.out.printf("%-" + e_cell_width[0] + "s|", "E_VARS");
                c = 1;
                for (Point p : redex_points) {
                    String s = p.toString();
                    e_cell_width[c] = s.length();
                    System.out.printf("%-" + e_cell_width[c++] + "s|", s);
                }
                for (Handle h : agent_handles) {
                    String s = h.toString();
                    e_cell_width[c] = s.length();
                    System.out.printf("%-" + e_cell_width[c++] + "s|", s);
                }
                for (Point pi : agent_points) {
                    System.out.printf("\n%-" + e_cell_width[0] + "s|", pi);
                    c = 1;
                    Map<LinkEntity, IntVar> row = e_vars.get(pi);
                    for (Point pj : redex_points) {
                        IntVar v = findVariable(row.get(pj).getName(),
                                model.getVars()).asIntVar();
                        System.out.printf("%" + e_cell_width[c++] + "d|",
                                v.getValue());
                    }
                    for (Handle hj : agent_handles) {
                        if (row.containsKey(hj)) {
                            IntVar v = findVariable(row.get(hj).getName(),
                                    model.getVars()).asIntVar();
                            System.out.printf("%" + e_cell_width[c++]
                                    + "d|", v.getValue());
                        } else {
                            System.out.printf("%" + e_cell_width[c++]
                                    + "c|", ' ');
                        }
                    }
                }
                for (Handle hi : redex_handles) {
                    System.out.printf("\n%-" + e_cell_width[0] + "s|", hi);
                    c = 1;
                    Map<LinkEntity, IntVar> row = e_vars.get(hi);
                    for (int j = rps; 0 < j; j--) {
                        System.out.printf("%" + e_cell_width[c++] + "c|",
                                ' ');
                    }
                    for (Handle hj : agent_handles) {
                        IntVar v = findVariable(row.get(hj).getName(),
                                model.getVars()).asIntVar();
                        System.out.printf("%" + e_cell_width[c++] + "d|",
                                v.getValue());
                    }
                }

                System.out.println('\n');

                System.out.printf("%" + f_cell_width[0] + "s|", "F_VARS");
                c = 1;
                for (Handle h : agent_handles) {
                    String s = h.toString();
                    f_cell_width[c] = s.length();
                    System.out.printf("%-" + f_cell_width[c++] + "s|", s);
                }
                for (Handle hi : redex_handles) {
                    System.out.printf("\n%-" + f_cell_width[0] + "s|", hi);
                    c = 1;
                    Map<Handle, IntVar> row = f_vars.get(hi);
                    for (Handle hj : agent_handles) {
                        IntVar v = findVariable(row.get(hj).getName(),
                                model.getVars()).asIntVar();
                        System.out.printf("%" + f_cell_width[c++] + "d|",
                                v.getValue());
                    }
                }
                System.out.println('\n');
            }

            private void noMoreSolution() {
                this.mayHaveNext = false;
                this.solver.hardReset();
            }

            @Override
            public CSPMatch next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                CSPMatch res = nextMatch;
                nextMatch = null;
                return res;
            }
        }
    }
}
