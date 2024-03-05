package org.bigraph.bigsim.utils;

import org.bigraph.bigsim.model.*;
import org.bigraph.bigsim.model.component.Child;
import org.bigraph.bigsim.model.component.Node;
import org.bigraph.bigsim.model.component.Root;
import org.bigraph.bigsim.model.component.shared.SharedChild;
import org.bigraph.bigsim.model.component.shared.SharedNode;
import org.bigraph.bigsim.model.component.shared.SharedRoot;
import org.bigraph.bigsim.model.component.shared.SharedSite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author huangningshuang
 * @date 2023/12/18
 */
public class BigraphToTerm {
    private static final Logger logger = LoggerFactory.getLogger(BigraphToTerm.class);

    public static Term toTerm(Bigraph big) {
        if (GlobalCfg.sharedMode()) {
            return parseSharedBigraph((SharedBigraph) big);
        } else {
            return parseNormalBigraph(big);
        }
    }

    private static Term parseSharedBigraph(SharedBigraph big) {
        big.trimBigraph();
        Term term = SharedBigraphToTerm.parseSharedRoots(big.sharedRoots(), big);
        DebugPrinter.print(logger, "shared term: " + term);
        return term;
    }

    private static class SharedBigraphToTerm {
        static Map<SharedNode, Term> nodeDict = new HashMap<>();
        static Map<SharedSite, Term> siteDict = new HashMap<>();

        private static Term parseSharedRoots(List<SharedRoot> roots, SharedBigraph big) {
            if (roots.size() == 0) return new Nil();
            Term first = parseSharedRoot(roots.get(0), big);
            if (roots.size() == 1) {
                return first;
            } else {
                roots.remove(0);
                return new Regions(first, parseSharedRoots(roots, big));
            }
        }

        private static Term parseSharedRoot(SharedRoot root, SharedBigraph big) {
            return parseSharedChildren(new ArrayList<>(root.getChildren()), big);
        }

        private static Term parseSharedChildren(List<SharedChild> children, SharedBigraph big) {
            if (children.size() == 0) return new Nil();
            Term first = parseSharedChild(children.get(0), big);
            if (children.size() == 1) return first;
            else {
                children.remove(0);
                return new Paraller(first, parseSharedChildren(children, big));
            }
        }

        private static Term parseSharedChild(SharedChild child, SharedBigraph big) {
            if (child.isRoot()) {
                throw new IllegalArgumentException("root can not be child");
            }
            if (child.isSite()) {
                if (siteDict.containsKey(child)) {
                    return siteDict.get(child);
                }
                int idx = big.sharedSites().indexOf(child);
                if (idx == -1) throw new RuntimeException("mismatch site");
                Hole hole = new Hole(idx);
                siteDict.put((SharedSite) child, hole);
                return hole;
            } else {
                SharedNode p = (SharedNode) child;
                if (nodeDict.containsKey(p)) {
                    return nodeDict.get(p);
                }
                if (p.getChildren().size() == 0) {
                    Prefix prefix = new Prefix(org.bigraph.bigsim.model.Node.sharedNodeAdapter(p), new Nil());
                    nodeDict.put(p, prefix);
                    return prefix;
                } else {
                    Prefix prefix = new Prefix(org.bigraph.bigsim.model.Node.sharedNodeAdapter(p),
                            parseSharedChildren(new ArrayList<>(p.getChildren()), big));
                    nodeDict.put(p, prefix);
                    return prefix;
                }
            }
        }
    }

    private static Term parseNormalBigraph(Bigraph big) {
        big.trimBigraph();
        Bigraph copy = big.clone();
        Term result = parseRoots(copy.bigRoots(), copy);
        DebugPrinter.print(logger, "target: " + result);
        return result;
    }

    private static Term parseRoots(List<Root> roots, Bigraph big) {
        if (roots.size() == 0) return new Nil();
        Term first = parseRoot(roots.get(0), big);
        if (roots.size() == 1) {
            return first;
        } else {
            roots.remove(0);
            return new Regions(first, parseRoots(roots, big));
        }
    }

    private static Term parseRoot(Root r, Bigraph big) {
        return parseChildren(new ArrayList<>(r.getChildren()), big);
    }

    private static Term parseChildren(List<Child> children, Bigraph big) {
        if (children.size() == 0) return new Nil();
        Term first = parseChild(children.get(0), big);
        if (children.size() == 1) return first;
        else {
            children.remove(0);
            return new Paraller(first, parseChildren(children, big));
        }
    }

    private static Term parseChild(Child child, Bigraph big) {
        if (child.isRoot()) {
            throw new IllegalArgumentException("root can not be child");
        }
        if (child.isSite()) {
            int idx = big.bigSites().indexOf(child);
            if (idx == -1) throw new RuntimeException("mismatch site");
            return new Hole(idx);
        } else {
            Node p = (Node) child;
            if (p.getChildren().size() == 0) {
                return new Prefix(org.bigraph.bigsim.model.Node.nodeAdapter(p), new Nil());
            } else {
                return new Prefix(org.bigraph.bigsim.model.Node.nodeAdapter(p),
                        parseChildren(new ArrayList<>(p.getChildren()), big));
            }
        }
    }
}
