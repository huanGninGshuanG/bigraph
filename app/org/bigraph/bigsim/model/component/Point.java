package org.bigraph.bigsim.model.component;

/**
 * @author huangningshuang
 * @date 2023/11/30
 */
public interface Point extends LinkEntity{
    void setHandle(Handle handle);

    Handle getHandle();
}
