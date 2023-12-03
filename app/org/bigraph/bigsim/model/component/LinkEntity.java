package org.bigraph.bigsim.model.component;

/**
 * @author huangningshuang
 * @date 2023/11/30
 */
public interface LinkEntity {
    boolean isHandle();

    boolean isPoint();

    boolean isPort();

    boolean isInnerName();

    boolean isOuterName();

    boolean isEdge();
}
