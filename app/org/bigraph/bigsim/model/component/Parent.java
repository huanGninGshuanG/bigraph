package org.bigraph.bigsim.model.component;

import java.util.Collection;

/**
 * @author huangningshuang
 * @date 2023/11/30
 */
public interface Parent extends PlaceEntity, Replicable {
    Collection<? extends Child> getChildren();

    void addChild(Child child);

    void removeChild(Child child);

    Root getRoot();

    @Override
    Parent replicate();
}
