package org.bigraph.bigsim.model.component;

import org.bigraph.bigsim.utils.NameGenerator;

/**
 * @author huangningshuang
 * @date 2023/11/30
 */
public class Site implements Child {
    private Parent parent;
    private BigraphHandler owner;
    private String name;

    public Site(Parent parent, BigraphHandler owner) {
        this.parent = parent;
        this.owner = owner;
        this.name = "S_" + NameGenerator.DEFAULT.generate();
    }

    @Override
    public String toString() {
        if (owner != null) {
            int i = owner.getSites().indexOf(this);
            if (i >= 0) return i + ":s";
        }
        return this.name;
    }

    @Override
    public Parent getParent() {
        return parent;
    }

    @Override
    public void setParent(Parent p) {
        if (p != this.parent) {
            Parent old = this.parent;
            this.parent = p;
            if (old != null) old.removeChild(this);
            if (this.parent != null) this.parent.addChild(this);
        }
    }

    @Override
    public boolean isSite() {
        return true;
    }

    @Override
    public boolean isNode() {
        return false;
    }

    @Override
    public boolean isRoot() {
        return false;
    }

    @Override
    public boolean isParent() {
        return false;
    }

    @Override
    public boolean isChild() {
        return true;
    }
}
