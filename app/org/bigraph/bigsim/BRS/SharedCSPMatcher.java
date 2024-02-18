package org.bigraph.bigsim.BRS;

import javafx.util.Pair;
import org.bigraph.bigsim.model.Bigraph;
import org.bigraph.bigsim.model.SharedBigraph;
import org.bigraph.bigsim.model.component.*;
import org.bigraph.bigsim.model.component.shared.*;
import org.bigraph.bigsim.utils.BidMap;
import org.bigraph.bigsim.utils.DebugPrinter;
import org.bigraph.bigsim.utils.GlobalCfg;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author huangningshuang
 * @date 2024/1/22
 */
public class SharedCSPMatcher {
    private static final Logger logger = LoggerFactory.getLogger(SharedCSPMatcher.class);

    public Iterable<? extends SharedCSPMatch> match(SharedBigraph agent, SharedBigraph redex) {
        return new MatchIterable(agent, redex);
    }

    private class MatchIterable implements Iterable<SharedCSPMatch> {
        final SharedBigraph agent, redex;
        final List<? extends SharedRoot> agent_roots;
        final List<? extends SharedSite> agent_sites;
        final Collection<? extends SharedNode> agent_nodes;
        final Collection<SharedNode.Port> agent_ports;
        final Collection<Point> agent_points;
        final Collection<? extends Edge> agent_edges;
        final List<Handle> agent_handles;

        final List<? extends SharedRoot> redex_roots;
        final List<? extends SharedSite> redex_sites;
        final Collection<? extends SharedNode> redex_nodes;
        final Collection<Point> redex_points;
        final Collection<? extends Edge> redex_edges;
        final List<Handle> redex_handles;

        /*
         * naming policy for sizes: a- agent r- redex -rs roots -ns nodes -ss
         * sites -hs handles -ps points -prs ports -ins bigInner() -ots bigOuter()
         */
        final int ars, ans, ass, ahs, aps, aprs, rrs, rns, rss, rhs, rps, rprs, rins;

        private MatchIterable(SharedBigraph agent, SharedBigraph redex) {
            if (!agent.bigSignature().equals(redex.bigSignature())) {
                throw new UnsupportedOperationException("Agent and redex should have the same singature.");
            }
            this.agent = agent;
            this.redex = redex;

            this.agent_roots = agent.getSharedRoots();
            this.agent_nodes = agent.getSharedNodes();
            this.agent_sites = agent.getSharedSites();
            this.agent_edges = agent.getEdges();
            this.agent_handles = new LinkedList<>(agent_edges);
            agent_handles.addAll(agent.getOuterNames());

            ars = agent_roots.size();
            ans = agent_nodes.size();
            ass = agent_sites.size();
            ahs = agent_handles.size();

            this.agent_ports = new HashSet<>(2 * ans);
            this.agent_points = new HashSet<>(2 * ans);
            for (SharedNode n : agent_nodes) {
                agent_ports.addAll(n.getPorts());
            }
            aprs = agent_ports.size();
            agent_points.addAll(agent_ports);
            agent_points.addAll(agent.getInnerNames());
            aps = agent_points.size();

            this.redex_roots = redex.getSharedRoots();
            this.redex_sites = redex.getSharedSites();
            this.redex_nodes = redex.getSharedNodes();
            this.redex_edges = redex.getEdges();
            this.redex_handles = new LinkedList<>(redex_edges);
            redex_handles.addAll(redex.getOuterNames());

            rrs = redex_roots.size();
            rns = redex_nodes.size();
            rss = redex_sites.size();
            rhs = redex_handles.size();

            this.redex_points = new HashSet<>(rns);
            for (SharedNode n : redex_nodes) {
                redex_points.addAll(n.getPorts());
            }
            rprs = redex_points.size(); // only ports
            redex_points.addAll(redex.getInnerNames());
            rps = redex_points.size();
            rins = rps - rprs;
        }

        @Override
        public Iterator<SharedCSPMatch> iterator() {
            return new SharedCSPMatcher.MatchIterable.MatchIterator();
        }

        private class MatchIterator implements Iterator<SharedCSPMatch> {

            private boolean mayHaveNext = true;
            private SharedCSPMatch nextMatch = null;
            final private Model model;
            final private Solver solver;

            /// place embedding中涉及到的变量，A_SAT_based_algorithm_for_the_matching_problem中的结构
            final Map<PlaceEntity, Map<PlaceEntity, IntVar>> l_vars = new IdentityHashMap<>(rrs + rns + rss);

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

            private Solver instantiateModel() {
                {
                    int ki = 0;
                    for (SharedRoot rr : redex_roots) {
                        int kj = 0;
                        Map<PlaceEntity, IntVar> row = new HashMap<>(ars + ans + ass);
                        IntVar[] vars = new IntVar[ars + ans];
                        for (SharedRoot ar : agent_roots) {
                            IntVar var = model.boolVar("L_" + ki + "_" + kj);
                            vars[kj++] = var;
                            row.put(ar, var);
                        }
                        for (SharedNode an : agent_nodes) {
                            IntVar var = model.boolVar("L_" + ki + "_" + kj);
                            vars[kj++] = var;
                            row.put(an, var);
                        }
                        for (SharedSite as : agent_sites) {
                            IntVar var = model.boolVar("L_" + ki + "_" + kj);
                            // sat-constrain(1) agent site不能和redex root匹配
                            model.arithm(var, "=", 0).post();
                            row.put(as, var);
                        }
                        l_vars.put(rr, row);
                        model.sum(vars, "=", 1).post();
                        ki++;
                    }

                    for (SharedNode rn : redex_nodes) {
                        int kj = 0;
                        int k = 0;
                        IntVar[] vars = new IntVar[ans + ass];
                        Map<PlaceEntity, IntVar> row = new HashMap<>(ars + ans + ass);
                        for (SharedRoot ar : agent_roots) {
                            IntVar var = model.boolVar("L_" + ki + "_" + kj++);
                            // sat-constrain(2) agent的root不能和redex node匹配
                            model.arithm(var, "=", 0).post();
                            row.put(ar, var);
                        }
                        for (SharedNode an : agent_nodes) {
                            IntVar var = model.boolVar("L_" + ki + "_" + kj++);
                            if (!rn.getControl().getName().equals(an.getControl().getName())) {
                                // control必须匹配
                                model.arithm(var, "=", 0).post();
                            }
                            vars[k++] = var;
                            row.put(an, var);
                        }
                        for (SharedSite as : agent_sites) {
                            IntVar var = model.boolVar("L_" + ki + "_" + kj++);
                            // sat-constrain(3) agent的site不能和redex node匹配
                            model.arithm(var, "=", 0).post();
                            vars[k++] = var;
                            row.put(as, var);
                        }
                        // sub-graph iso clause(1)(2)
                        model.sum(vars, "=", 1).post();
                        l_vars.put(rn, row);
                        ki++;
                    }

                    for (SharedSite rs : redex_sites) {
                        int kj = 0;
                        Map<PlaceEntity, IntVar> row = new HashMap<>(ars + ans + ass);
                        for (SharedRoot ar : agent_roots) {
                            IntVar var = model.boolVar("L_" + ki + "_" + kj++);
                            // sat-constrain(4) agent的root不能和redex site匹配
                            model.arithm(var, "=", 0).post();
                            row.put(ar, var);
                        }
                        for (SharedNode an : agent_nodes) {
                            IntVar var = model.boolVar("L_" + ki + "_" + kj++);
                            row.put(an, var);
                        }
                        for (SharedSite as : agent_sites) {
                            IntVar var = model.boolVar("L_" + ki + "_" + kj++);
                            row.put(as, var);
                        }
                        l_vars.put(rs, row);
                        ki++;
                    }
                }

                /// Place graph constraints
                {
                    for (SharedRoot ar : agent_roots) {
                        int k = 0;
                        IntVar[] vars = new IntVar[rrs];
                        for (SharedRoot rr : redex_roots) {
                            vars[k++] = l_vars.get(rr).get(ar);
                        }
                        // sub-graph iso clause(3)
                        model.sum(vars, "<=", 1).post();
                    }
                    for (SharedNode an : agent_nodes) {
                        int k = 0;
                        IntVar[] vars = new IntVar[rns];
                        IntVar nnCnt = model.intVar(0);
                        for (SharedNode rn : redex_nodes) {
                            vars[k] = l_vars.get(rn).get(an);
                            nnCnt.add(vars[k]);
                            k++;
                        }
                        // agent_nodes只能match一个agent nodes，但是可以match多个site或者多个root
                        model.sum(vars, "<=", 1).post();
                        IntVar nrCnt = model.intVar(0); // agent node匹配redex root的个数
                        for (SharedRoot rr : redex_roots) {
                            nrCnt.add(l_vars.get(rr).get(an));
                        }
                        IntVar nsCnt = model.intVar(0); // agent node匹配redex site的个数
                        for (SharedSite rs : redex_sites) {
                            nsCnt.add(l_vars.get(rs).get(an));
                        }
                        // Fig.12 & Fig.13 clause(3) agent节点匹配了redex root，就不能匹配节点和site
                        // Fig.12 & Fig.13 clause(3) agent节点匹配了redex site，就不能匹配节点和root
                        model.min(model.intVar(0), nnCnt, nrCnt).post();
                        model.min(model.intVar(0), nnCnt, nsCnt).post();
                        model.min(model.intVar(0), nsCnt, nrCnt).post();
                    }

                    for (SharedNode an : agent_nodes) {
                        Collection<? extends SharedParent> aps = an.getParents();
                        for (SharedNode rn : redex_nodes) {
                            IntVar var = l_vars.get(rn).get(an);
                            Collection<? extends SharedParent> rps = rn.getParents();
                            IntVar[] pVars = new IntVar[rps.size() * aps.size()];
                            int k = 0;
                            for (SharedParent rp : rps) {
                                Map<PlaceEntity, IntVar> row = l_vars.get(rp);
                                for (SharedParent ap : aps) {
                                    pVars[k++] = row.get(ap);
                                }
                            }
                            // sub-graph iso clause(4) agent节点父元素全部被匹配，当前元素才能匹配
                            model.sum(pVars, ">=", var.mul(aps.size()).intVar()).post();
                        }

                        Collection<? extends SharedChild> aChild = an.getChildren();
                        for (SharedSite rs : redex_sites) {
                            IntVar var = l_vars.get(rs).get(an);
                            Collection<? extends SharedParent> rps = rs.getParents();
                            Map<PlaceEntity, IntVar> row = l_vars.get(rs);
                            for (SharedChild ac : aChild) {
                                IntVar cVar = row.get(ac);
                                IntVar val = model.intVar(1);
                                // var->cVar agent node和redex site匹配，这个node的所有后代都在site中
                                model.arithm(val.sub(var).add(cVar).intVar(), ">=", 1).post();
                            }
                        }
                        /* agent的node和redex的site发生了匹配， agent node的所有parent：
                            1. 要么和site的某个parent匹配
                            2. 要么在另一个site下
                            3. 与其他site的parent匹配 (该node匹配多个site)
                            由于没有想到一个比较好的约束表达方式，在求解后再对解进行筛选
                        */
                    }


                    // degree match
                    for (SharedNode rn : redex_nodes) {
                        Degree rDeg = rn.computeDegree();
                        Map<PlaceEntity, IntVar> row = l_vars.get(rn);
                        for (SharedNode an : agent_nodes) {
                            Degree aDeg = an.computeDegree();
                            if (!Degree.match(rDeg, aDeg)) { // 这两个参数不能颠倒顺序
                                IntVar var = row.get(an);
                                // degree不匹配，则这两个节点不能匹配
                                model.arithm(var, "=", 0).post();
                            }
                        }
                    }
                    for (SharedSite rs : redex_sites) {
                        Pair<Integer, Integer> rIn = rs.computeDegree().getIn();
                        Map<PlaceEntity, IntVar> row = l_vars.get(rs);
                        for (SharedNode an : agent_nodes) {
                            Pair<Integer, Integer> aIn = an.computeDegree().getIn();
                            if (!(rIn.getKey() <= aIn.getKey())) {
                                IntVar var = row.get(an);
                                model.arithm(var, "=", 0).post();
                            }
                        }
                    }

                    {
                        for (SharedParent f : agent_nodes) {
                            Collection<? extends SharedChild> cf = f.getChildren();
                            for (SharedParent g : redex_nodes) {
                                Collection<? extends SharedChild> cg = g.getChildren();
                                IntVar[] vars = new IntVar[cf.size() * cg.size()];
                                int k = 0;
                                for (PlaceEntity i : cf) {
                                    for (PlaceEntity j : cg) {
                                        vars[k++] = l_vars.get(j).get(i);
                                    }
                                }
                                IntVar chld = l_vars.get(g).get(f);
                                // constraint(30) 某个nodes匹配，所有的child也必须匹配
                                model.sum(vars, ">=", chld.mul(cf.size()).intVar()).post();
                            }
                        }
                    }

                    {
                        Map<SharedRoot, Collection<SharedChild>> cgs = new HashMap<>(rrs);
                        for (SharedRoot g : redex_roots) {
                            Collection<SharedChild> cg = new HashSet<>(g.getChildren());
                            cg.removeAll(redex_sites);
                            cgs.put(g, cg);
                        }
                        for (SharedRoot rr : redex_roots) {
                            Collection<SharedChild> rcs = cgs.get(rr);
                            for (PlaceEntity ap : l_vars.get(rr).keySet()) {
                                if (ap.isSite()) continue;
                                IntVar var = l_vars.get(rr).get(ap);
                                SharedParent an = (SharedParent) ap;
                                IntVar[] vars = new IntVar[rcs.size() * an.getChildren().size()];
                                int k = 0;
                                for (SharedChild rc : rcs) {
                                    for (SharedChild ac : an.getChildren()) {
                                        vars[k++] = l_vars.get(rc).get(ac);
                                    }
                                }
                                // constraint(31) redex某个root匹配，所有的child也必须匹配
                                model.sum(vars, ">=", var.mul(rcs.size()).intVar()).post();
                            }
                        }
                    }

                    {
                        // agent nodes在redex的sites中，就不能与redex中的nodes和roots匹配
                        for (SharedNode i : agent_nodes) {
                            Collection<SharedParent> ancs = agent.getAncestors(i);
                            IntVar[] vars = new IntVar[(ancs.size() + 1) * rss];
                            int k = 0;
                            for (SharedParent f : ancs) {
                                if (f.isNode()) {
                                    for (SharedSite g : redex_sites) {
                                        vars[k++] = l_vars.get(g).get(f);
                                    }
                                }
                            }
                            for (SharedSite g : redex_sites) {
                                vars[k++] = l_vars.get(g).get(i);
                            }

                            IntVar sum = model.intVar(0);
                            for (IntVar v : vars) {
                                sum = sum.add(v).intVar();
                            }
                            for (SharedRoot j : redex_roots) {
                                IntVar[] matchVars = new IntVar[2];
                                matchVars[0] = sum;
                                matchVars[1] = l_vars.get(j).get(i);
                                model.min(model.intVar(0), matchVars).post();
                            }
                            for (SharedNode rn : redex_nodes) {
                                IntVar[] matchVars = new IntVar[2];
                                matchVars[0] = sum;
                                matchVars[1] = l_vars.get(rn).get(i);
                                model.min(model.intVar(0), matchVars).post();
                            }
                        }
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

                /// LINK Constraints 看成网络流问题，共享偶图的位置图约束和普通偶图一致
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
                        if (pr.isPort()) {
                            int k = 0;
                            for (Point pa : agent_points) {
                                vars[k++] = e_vars.get(pa).get(pr);
                            }
                            // constraint(8) redex ports只能从agent point接受一个单位
                            model.sum(vars, "=", 1).post();
                        }
                    }
                }
                {
                    IntVar[] vars = new IntVar[aprs];
                    for (Point pr : redex_points) {
                        if (pr.isInnerName()) {
                            int k = 0;
                            for (Point pa : agent_ports) {
                                vars[k++] = e_vars.get(pa).get(pr);
                            }
                            // constraint(8) redex innername只能从agent port接受一个单位
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
                                    // constraint(11) handles之间没有流，handles连接的point之间也没有流
                                    model.arithm(ve, "<=", vf).post();
                                }
                                if (hr.isEdge()) {
                                    // constraint(14) Fhh'为1，那么就不能detour redex，Nph=0
                                    model.arithm(vf.add(e_vars.get(pa).get(ha)).intVar(), "<=", 1).post();
                                }
                            }
                            if (!ps.isEmpty() || !ha.getPoints().isEmpty())
                                // constraint(12) handles连接的points之间没有流，那么这个handles之间也没有流
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
                        // constraint(17) agent edge最多映射一个edge
                        if (redex_edges.size() > 0)
                            model.sum(vars, "<=", 1).post();
                    }
                }

                {
                    for (SharedNode ni : agent_nodes) {
                        for (SharedNode nj : redex_nodes) {
                            IntVar m = l_vars.get(nj).get(ni);
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
                    for (SharedNode ni : agent_nodes) {
                        Collection<SharedParent> ancs = agent.getAncestors(ni);
                        IntVar[] vars2 = new IntVar[(1 + ancs.size()) * rss];
                        int k2 = 0;
                        for (SharedParent f : ancs) {
                            for (SharedSite g : redex_sites) {
                                vars2[k2++] = l_vars.get(g).get(f);
                            }
                        }
                        {
                            for (SharedSite g : redex_sites) {
                                vars2[k2++] = l_vars.get(g).get(ni);
                            }
                        }
                        IntVar sum2 = model.intVar(0);
                        for (IntVar v : vars2) {
                            sum2 = sum2.add(v).intVar();
                        }

                        for (SharedNode.Port pi : ni.getPorts()) {
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

            private boolean areMatchable(SharedBigraph agent, SharedNode fromAgent, SharedBigraph redex, SharedNode fromRedex) {
                return fromAgent.getControl().equals(fromRedex.getControl());
            }

            @Override
            public boolean hasNext() {
                if (mayHaveNext && nextMatch == null) {
                    fetchSolution();
                }
                return mayHaveNext && nextMatch != null;
            }

            @Override
            public SharedCSPMatch next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                SharedCSPMatch res = nextMatch;
                nextMatch = null;
                return res;
            }

            private void noMoreSolution() {
                this.mayHaveNext = false;
                this.solver.hardReset();
            }

            private Variable findVariable(String name, Variable[] vars) {
                for (Variable v : vars) {
                    if (name.equals(v.getName())) {
                        return v;
                    }
                }
                return null;
            }

            private void fetchSolution() {
                boolean hasSolution = true;
                while (hasSolution = solver.solve()) {
                    boolean match = true;
                    for (SharedNode an : agent_nodes) {
                        boolean matchedToSite = false;
                        Collection<? extends SharedParent> aps = an.getParents();
                        Set<SharedParent> matchedParent = new HashSet<>();
                        Set<SharedParent> sitesParent = new HashSet<>();
//                        DebugPrinter.print(logger, "test: " + an);
                        for (SharedSite rs : redex_sites) {
                            Map<PlaceEntity, IntVar> row = l_vars.get(rs);
                            Collection<? extends SharedParent> rps = rs.getParents();
                            IntVar var = findVariable(row.get(an).getName(), model.getVars()).asIntVar();
                            int value = var.getValue();
                            if (value != 1) continue;
                            matchedToSite = true;
                            sitesParent.addAll(rps);
                            for (SharedParent ap : aps) {
                                IntVar v1 = findVariable(row.get(ap).getName(), model.getVars()).asIntVar();
                                if (v1.getValue() == 1) {
                                    matchedParent.add(ap);
//                                    DebugPrinter.print(logger, "test: " + ap + " matches1 " + rs);
                                    continue;
                                }
                                for (SharedParent rp : rps) {
                                    IntVar v2 = findVariable(l_vars.get(rp).get(ap).getName(), model.getVars()).asIntVar();
                                    if (v2.getValue() == 1) {
                                        matchedParent.add(ap);
//                                        DebugPrinter.print(logger, "test: " + ap + " matches2 " + rp);
                                        break;
                                    }
                                }
                            }
                        }
//                        DebugPrinter.print(logger, "test?: " + an + " : " + matchedParent.size() + " : " + aps.size() + " : " + sitesParent.size());
                        if (matchedToSite && (matchedParent.size() != aps.size() || sitesParent.size() > aps.size()))
                            match = false;
                    }
                    if (!match) continue;
                    break;
                }
                if(!hasSolution) {
                    noMoreSolution();
                    return;
                }
                printCSPSolution();

                // context
                SharedBigraph ctx = new SharedBigraph(agent.bigSignature());
                // redex
                SharedBigraph rdx = new SharedBigraph(agent.bigSignature());
                // parameters
                SharedBigraph prm = new SharedBigraph(agent.bigSignature());
                // linking medianting between parameters and redex+ID
                SharedBigraph lmb = SharedBigraph.makeId(redex.getSignature(), rss, new ArrayList<>());
                SharedBigraph id = SharedBigraph.makeEmpty(redex.getSignature());
                // an injective map from redex's nodes to rdx's ones
                BidMap<SharedNode, SharedNode> nEmb = new BidMap<>(rns);

                // replicated sites
                SharedSite ctx_sites_dic[] = new SharedSite[rrs];
                SharedSite rdx_sites_dic[] = new SharedSite[rss];
                SharedRoot rdx_roots_dic[] = new SharedRoot[rrs];
                SharedRoot prm_roots_dic[] = new SharedRoot[rss];

                // replicated handles lookup tables
                Map<Handle, Handle> ctx_hnd_dic = new IdentityHashMap<>();
                Map<Handle, Handle> rdx_hnd_dic = new IdentityHashMap<>();
                Map<Handle, Handle> lmb_hnd_dic = new IdentityHashMap<>();
                Map<Handle, Handle> prm_hnd_dic = new IdentityHashMap<>();

                // replicated nodes lookup tables
                Map<SharedParent, SharedParent> ctx_parent_dict = new IdentityHashMap<>();
                Map<SharedNode, SharedNode> rdx_node_dict = new IdentityHashMap<>();
                Map<SharedNode, SharedNode> lmb_node_dict = new IdentityHashMap<>();
                Map<SharedNode, SharedNode> prm_node_dict = new IdentityHashMap<>();

                // replicated sites lookup tables
                Map<SharedSite, SharedSite> ctx_site_dict = new IdentityHashMap<>();
                Map<SharedSite, SharedSite> rdx_site_dict = new IdentityHashMap<>();
                Map<SharedSite, SharedSite> lmb_site_dict = new IdentityHashMap<>();
                Map<Bigraph, Map<SharedSite, SharedSite>> prms_site_dict = new IdentityHashMap<>();

                Map<Handle, Handle> handle_img = new IdentityHashMap<>(rhs);

                class VState {
                    final PlaceEntity c; // the agent root/node to be visited
                    final PlaceEntity i; // if present, is the image of c in the redex
                    final SharedParent p; // the replicated parent
                    final Bigraph b;

                    VState(Bigraph b, SharedParent p, PlaceEntity c) {
                        this(b, p, c, null);
                    }

                    VState(Bigraph b, SharedParent p, PlaceEntity c, PlaceEntity i) {
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
                for (SharedRoot r0 : agent.sharedRoots()) {
                    q.add(new VState(ctx, null, r0));
                }
                Collection<SharedRoot> unseen_rdx_roots = new LinkedList<>(redex_roots);
                while (!q.isEmpty()) {
                    VState v = q.poll();
                    if (v.b == ctx) {
                        // 元素属于上下文ctx
                        SharedParent p1 = (SharedParent) v.c;
                        SharedParent p2 = ctx_parent_dict.get(p1);
                        if (p2 == null) {
                            p2 = p1.replicate();
                            ctx_parent_dict.put(p1, p2);
                        }
                        if (p1.isRoot()) {
                            SharedRoot r2 = (SharedRoot) p2;
                            ctx.sharedRoots().add(r2);
                        } else { // isNode()
                            SharedNode n1 = (SharedNode) p1;
                            SharedNode n2 = (SharedNode) p2;
                            n2.addParent(v.p);
                            // 拷贝端口的每个handle
                            for (int i = n1.getControl().getArity() - 1; -1 < i; i--) {
                                SharedNode.Port o = n1.getPort(i);
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
                        Collection<SharedChild> rcs = new HashSet<>(p1.getChildren());
                        Iterator<SharedRoot> ir = unseen_rdx_roots.iterator();
                        while (ir.hasNext()) {
                            SharedRoot r0 = ir.next();
                            IntVar var = findVariable(l_vars.get(r0).get(p1).getName(), model.getVars()).asIntVar();
                            // make a site for each root whose image is p1
                            if (var.getValue() == 1) {
                                ir.remove();
                                int k = redex_roots.indexOf(r0);
                                SharedSite s = new SharedSite();
                                s.addParent(p2);
                                ctx_sites_dic[k] = s;
                                SharedRoot r2 = new SharedRoot();
                                rdx_roots_dic[k] = r2;
                                for (SharedChild c0 : r0.getChildren()) {
                                    Iterator<SharedChild> ic = rcs.iterator();
                                    boolean notMatched = true;
                                    while (ic.hasNext()) {
                                        SharedChild c1 = ic.next();
                                        DebugPrinter.print(logger, "test: " + c0 + " : " + c1);
                                        var = findVariable(l_vars.get(c0).get(c1).getName(), model.getVars()).asIntVar();
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
                        for (SharedChild c1 : rcs) {
                            q.add(new VState(ctx, p2, c1));
                        }
                    } else if (v.b == rdx) {
                        // 放入上下文拷贝rdx中
                        if (v.i.isNode()) {
                            SharedNode n0 = (SharedNode) v.i;
                            SharedNode n1 = (SharedNode) v.c;
                            SharedNode n2 = rdx_node_dict.get(n1);
                            if (n2 == null) {
                                n2 = n1.replicate();
                                rdx_node_dict.put(n1, n2);
                            }
                            nEmb.put(n0, n1);
                            n2.addParent(v.p);
                            // replicate links from node ports
                            for (int i = n0.getControl().getArity() - 1; -1 < i; i--) {
                                SharedNode.Port o0 = n0.getPort(i);
                                Handle h0 = o0.getHandle();
                                // looks for an existing replica
                                Handle h2 = rdx_hnd_dic.get(h0);
                                if (h2 == null) {
                                    h2 = n1.getPort(i).getHandle().replicate();
                                    rdx_hnd_dic.put(h0, h2);
                                }
                                n2.getPort(i).setHandle(h2);
                            }
                            Collection<SharedChild> cs1 = new HashSet<>(n1.getChildren());
                            for (SharedChild c0 : n0.getChildren()) {
                                Iterator<SharedChild> ic = cs1.iterator();
                                boolean notMatched = true;
                                while (ic.hasNext()) {
                                    SharedChild c1 = ic.next();
                                    IntVar var = findVariable(l_vars.get(c0).get(c1).getName(), model.getVars()).asIntVar();
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
                            SharedSite s0 = (SharedSite) v.i;
                            int k = redex_sites.indexOf(s0);
                            if (rdx_sites_dic[k] == null) {
                                SharedSite s2 = new SharedSite();
                                s2.addParent(v.p);
                                rdx_sites_dic[k] = s2;
                            }
                            prm_roots_dic[k] = new SharedRoot();
                            if (v.c != null)
                                q.add(new VState(prm, prm_roots_dic[k], v.c));
                        }
                    } else {
                        // the entity (node) visited belongs to some parameter
                        SharedNode n1 = (SharedNode) v.c;
                        SharedNode n2 = prm_node_dict.get(n1);
                        if (n2 == null) {
                            n2 = n1.replicate();
                            prm_node_dict.put(n1, n2);
                        }
                        n2.addParent(v.p);
                        for (int i = n1.getControl().getArity() - 1; -1 < i; i--) {
                            /*
                             * every handle with a point in the param is
                             * translated into an outer and the necessary wiring
                             * is delegated to the bigraph lambda.
                             */
                            SharedNode.Port p1 = n1.getPort(i);
                            SharedNode.Port p2 = n2.getPort(i);

                            Handle h2 = null;
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
                                h2 = prm_hnd_dic.get(h1);
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
                                    prm_hnd_dic.put(h1, h2);
                                }
                            } else {
                                for (InnerName i0 : redex.bigInner().values()) {
                                    IntVar var = findVariable(row.get(i0).getName(), model.getVars()).asIntVar();
                                    if (var.getValue() == 1) {
                                        Handle h3 = lmb.bigOuter().get(i0.getName());
                                        h2 = prm_hnd_dic.get(h3);
                                        if (h2 == null) {
                                            InnerName i3 = new InnerName();
                                            String name = i3.getName();
                                            i3.setHandle(h3);
                                            lmb.bigInner().put(name, i3);
                                            OuterName o2 = new OuterName(name);
                                            v.b.bigOuter().put(name, o2);
                                            h2 = o2;
                                            prm_hnd_dic.put(h3, o2);
                                        }
                                        break;
                                    }
                                }
                            }
                            p2.setHandle(h2);
                        }
                        for (SharedChild c1 : n1.getChildren()) {
                            q.add(new VState(v.b, n2, c1));
                        }
                    }
                }
                ctx.sharedSites().addAll(Arrays.asList(ctx_sites_dic));
                rdx.sharedSites().addAll(Arrays.asList(rdx_sites_dic));
                rdx.sharedRoots().addAll(Arrays.asList(rdx_roots_dic));
                prm.sharedRoots().addAll(Arrays.asList(prm_roots_dic));

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
                if (!prm.isConsistent()) {
                    throw new RuntimeException("Inconsistent bigraph (prm)");
                }
                DebugPrinter.print(logger, "ctx info:");
                ctx.print();

                DebugPrinter.print(logger, "rdx info:");
                rdx.print();

                DebugPrinter.print(logger, "id info:");
                id.print();

                DebugPrinter.print(logger, "lmb info:");
                lmb.print();

                DebugPrinter.print(logger, "prm info:");
                prm.print();

                this.nextMatch = new SharedCSPMatch(ctx, rdx, id, lmb, prm);
            }

            private void printCSPSolution() {
                printPlaceAns();
//                printLinkAns();
            }

            private void printPlaceAns() {
                int p_cell_width[] = new int[1 + ars + ans + ass];
                p_cell_width[0] = 6;
                for (SharedNode n : redex_nodes) {
                    p_cell_width[0] = Math.max(p_cell_width[0], n.toString().length());
                }
                StringBuilder line = new StringBuilder(String.format("\n%-" + p_cell_width[0] + "s|", "P_VARS"));
//                System.out.printf("%-" + p_cell_width[0] + "s|", "P_VARS");
                int c = 1;
                for (int k = 0; k < ars; k++, c++) {
                    String s = "R_" + k;
                    p_cell_width[c] = s.length();
                    line.append(String.format("%-" + p_cell_width[c] + "s|", s));
//                    System.out.printf("%-" + p_cell_width[c] + "s|", s);
                }
                for (SharedNode n : agent_nodes) {
                    String s = n.toString();
                    p_cell_width[c] = s.length();
                    line.append(String.format("%-" + p_cell_width[c++] + "s|", s));
//                    System.out.printf("%-" + p_cell_width[c++] + "s|", s);
                }
                for (int k = 0; k < ass; k++, c++) {
                    String s = "S_" + k;
                    p_cell_width[c] = s.length();
                    line.append(String.format("%-" + p_cell_width[c] + "s|", s));
//                    System.out.printf("%-" + p_cell_width[c] + "s|", s);
                }
                for (int i = 0; i < rrs; i++) {
                    line.append(String.format("\nR_%-" + (p_cell_width[0] - 2) + "d|", i));
//                    System.out.printf("\nR_%-" + (p_cell_width[0] - 2) + "d|", i);
                    c = 1;
                    SharedRoot ri = redex_roots.get(i);
                    Map<PlaceEntity, IntVar> row = l_vars.get(ri);
                    for (int j = 0; j < ars; j++) {
                        SharedRoot rj = agent_roots.get(j);
                        IntVar v = findVariable(row.get(rj).getName(), model.getVars()).asIntVar();
                        line.append(String.format("%" + p_cell_width[c++] + "d|", v.getValue()));
//                        System.out.printf("%" + p_cell_width[c++] + "d|", v.getValue());
                    }
                    for (SharedNode nj : agent_nodes) {
                        IntVar v = findVariable(row.get(nj).getName(), model.getVars()).asIntVar();
                        line.append(String.format("%" + p_cell_width[c++] + "d|", v.getValue()));
//                        System.out.printf("%" + p_cell_width[c++] + "d|", v.getValue());
                    }
                    for (int j = 0; j < ass; j++) {
                        line.append(String.format("%" + p_cell_width[c++] + "c|", ' '));
//                        System.out.printf("%" + p_cell_width[c++] + "c|", ' ');
                    }
                }
                for (SharedNode ni : redex_nodes) {
                    line.append(String.format("\n%-" + p_cell_width[0] + "s|", ni));
//                    System.out.printf("\n%-" + p_cell_width[0] + "s|", ni);
                    c = 1;
                    Map<PlaceEntity, IntVar> row = l_vars.get(ni);
                    for (int j = 0; j < ars; j++) {
                        line.append(String.format("%" + p_cell_width[c++] + "c|", ' '));
//                        System.out.printf("%" + p_cell_width[c++] + "c|", ' ');
                    }
                    for (SharedNode nj : agent_nodes) {
                        IntVar v = findVariable(row.get(nj).getName(), model.getVars()).asIntVar();
                        line.append(String.format("%" + p_cell_width[c++] + "d|", v.getValue()));
//                        System.out.printf("%" + p_cell_width[c++] + "d|", v.getValue());
                    }
                    for (int j = 0; j < ass; j++) {
                        line.append(String.format("%" + p_cell_width[c++] + "c|", ' '));
//                        System.out.printf("%" + p_cell_width[c++] + "c|", ' ');
                    }
                }
                for (int i = 0; i < rss; i++) {
                    SharedSite ri = redex_sites.get(i);
                    line.append(String.format("\nS_%-" + (p_cell_width[0] - 2) + "s|", ri));
//                    System.out.printf("\nS_%-" + (p_cell_width[0] - 2) + "d|", i);
                    c = 1;
                    Map<PlaceEntity, IntVar> row = l_vars.get(ri);
                    for (int j = 0; j < ars; j++) {
                        line.append(String.format("%" + p_cell_width[c++] + "c|", ' '));
//                        System.out.printf("%" + p_cell_width[c++] + "c|", ' ');
                    }
                    for (SharedNode nj : agent_nodes) {
                        IntVar v = findVariable(row.get(nj).getName(), model.getVars()).asIntVar();
                        line.append(String.format("%" + p_cell_width[c++] + "d|", v.getValue()));
//                        System.out.printf("%" + p_cell_width[c++] + "d|", v.getValue());
                    }
                    for (int j = 0; j < ass; j++) {
                        SharedSite sj = agent_sites.get(j);
                        IntVar v = findVariable(row.get(sj).getName(), model.getVars()).asIntVar();
                        line.append(String.format("%" + p_cell_width[c++] + "d|", v.getValue()));
//                        System.out.printf("%" + p_cell_width[c++] + "d|", v.getValue());
                    }
                }
                DebugPrinter.print(logger, line.toString());
//                System.out.println('\n');
            }

            private void printLinkAns() {
                int c = 1;
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
        }
    }
}
