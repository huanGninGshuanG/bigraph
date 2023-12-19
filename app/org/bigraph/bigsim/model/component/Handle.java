package org.bigraph.bigsim.model.component;

import java.util.Collection;

/**
 * @author huangningshuang
 * @date 2023/11/30
 */
public interface Handle extends LinkEntity, Replicable {
    Collection<? extends Point> getPoints();

    void linkPoint(Point point);

    void unlinkPoint(Point point);

    String getName();

    @Override
    Handle replicate();
}