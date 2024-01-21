package org.bigraph.bigsim.model.component;

import java.util.Collection;
import java.util.List;

/**
 * @author huangningshuang
 * @date 2023/11/30
 */
public interface BigraphHandler {
    boolean isEmpty();
    boolean isGround();
    List<? extends Root> getRoots();
    List<? extends Site> getSites();
    Collection<? extends OuterName> getOuterNames();
    Collection<? extends InnerName> getInnerNames();
    Collection<? extends Node> getNodes();
    Collection<? extends Edge> getEdges();
    boolean isConsistent();
}
