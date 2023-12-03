package org.bigraph.bigsim.model.component;

/**
 * @author huangningshuang
 * @date 2023/11/30
 */
public class Port implements Point {
    private final int index; // 该port是node的第几个port
    private Handle handle;

    public Port(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public void setHandle(Handle handle) {
        if (handle == this.handle) return;
        Handle old = this.handle;
        this.handle = handle;
        if (old != null) old.unlinkPoint(this);
        if (this.handle != null) this.handle.linkPoint(this);
    }

    public Handle getHandle() {
        return handle;
    }

    @Override
    public boolean isHandle() {
        return false;
    }

    @Override
    public boolean isPoint() {
        return true;
    }

    @Override
    public boolean isPort() {
        return true;
    }

    @Override
    public boolean isInnerName() {
        return false;
    }

    @Override
    public boolean isOuterName() {
        return false;
    }

    @Override
    public boolean isEdge() {
        return false;
    }
}
