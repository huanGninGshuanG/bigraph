package org.bigraph.bigsim.model.component.shared;

import org.bigraph.bigsim.model.component.*;

import java.util.Collection;

/**
 * @author huangningshuang
 * @date 2024/1/20
 */
public interface SharedParent extends PlaceEntity, Replicable {
    Collection<? extends SharedChild> getChildren();

    void addChild(SharedChild child);

    void removeChild(SharedChild child);

    @Override
    SharedParent replicate();
}
