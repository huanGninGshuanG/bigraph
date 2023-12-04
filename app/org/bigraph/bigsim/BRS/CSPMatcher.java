package org.bigraph.bigsim.BRS;

import org.bigraph.bigsim.model.Bigraph;
import org.bigraph.bigsim.model.component.*;
import org.bigraph.bigsim.utils.DebugPrinter;
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
         * sites -hs handles -ps points -prs ports -ins inners -ots outers
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
                DebugPrinter.print(logger, "agent:\n" + agent);
                DebugPrinter.print(logger, "- REDEX -----------------------------");
                DebugPrinter.print(logger, "redex:\n" + redex);
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
                    // todo: roots-roots部分是否漏掉了
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
                        // constraint(29) agent中的每个nodes最多和redex中的一个nodes/sites匹配
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
                        IntVar[] vars = new IntVar[(ancs.size()) * rss];
                        int k = 0;
                        for (Parent f : ancs) {
                            if (f.isNode()) {
                                Map<PlaceEntity, IntVar> f_row = p_vars.get(f);
                                for (Site g : redex_sites) {
                                    vars[k++] = f_row.get(g);
                                }
                            }
                        }

                        IntVar sum = model.intVar(0);
                        for (IntVar v : vars) {
                            sum = sum.add(v).intVar();
                        }
                        Map<PlaceEntity, IntVar> i_row = p_vars.get(i);
                        for (Root j : redex_roots) {
                            // constraint(32) agent nodes和redex root匹配，那么该node的ancestor就不能和redex sites匹配
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
            }

            @Override
            public boolean hasNext() {
                return false;
            }

            @Override
            public CSPMatch next() {
                return null;
            }
        }
    }
}
