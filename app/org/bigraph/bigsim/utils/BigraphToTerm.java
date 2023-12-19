package org.bigraph.bigsim.utils;

import org.bigraph.bigsim.model.*;
import org.bigraph.bigsim.model.component.Child;
import org.bigraph.bigsim.model.component.Node;
import org.bigraph.bigsim.model.component.Root;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author huangningshuang
 * @date 2023/12/18
 */
public class BigraphToTerm {
    private static final Logger logger = LoggerFactory.getLogger(BigraphToTerm.class);

    public static Term toTerm(Bigraph big) {
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
