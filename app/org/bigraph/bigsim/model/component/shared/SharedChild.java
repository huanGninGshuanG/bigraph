package org.bigraph.bigsim.model.component.shared;

import org.bigraph.bigsim.model.component.Child;
import org.bigraph.bigsim.model.component.Parent;
import org.bigraph.bigsim.model.component.PlaceEntity;
import org.bigraph.bigsim.model.component.Replicable;

import java.util.Collection;
import java.util.List;

/**
 * @author huangningshuang
 * @date 2024/1/20
 */
public interface SharedChild extends PlaceEntity, Replicable {
    Collection<? extends SharedParent> getParents();

    void addParent(SharedParent p);

    void removeParent(SharedParent p);

    @Override
    SharedChild replicate();
}
