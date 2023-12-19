package org.bigraph.bigsim.model.component;

/**
 * @author huangningshuang
 * @date 2023/11/30
 */
public interface PlaceEntity {
    boolean isSite();

    boolean isNode();

    boolean isRoot();

    boolean isParent();

    boolean isChild();
}
