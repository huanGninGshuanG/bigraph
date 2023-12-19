package org.bigraph.bigsim.model.component;

/**
 * @author huangningshuang
 * @date 2023/11/30
 */
public interface Child extends PlaceEntity, Replicable {
    Parent getParent();

    void setParent(Parent p);

    @Override
    Child replicate();
}
