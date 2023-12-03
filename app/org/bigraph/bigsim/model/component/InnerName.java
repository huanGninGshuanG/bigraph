package org.bigraph.bigsim.model.component;

/**
 * @author huangningshuang
 * @date 2023/12/1
 */
public class InnerName implements Point {
    private Handle handle;
    private String name;

    public InnerName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public void setHandle(Handle handle) {
        if (this.handle == handle) return;
        Handle old = this.handle;
        this.handle = handle;
        if (old != null) old.unlinkPoint(this);
        if (handle != null) handle.linkPoint(this);
    }

    @Override
    public Handle getHandle() {
        return handle;
    }

    @Override
    public boolean isHandle() {
        return false;
    }

    @Override
    public boolean isPoint() {
        return false;
    }

    @Override
    public boolean isPort() {
        return false;
    }

    @Override
    public boolean isInnerName() {
        return true;
    }

    @Override
    public boolean isOuterName() {
        return false;
    }

    @Override
    public boolean isEdge() {
        return false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
