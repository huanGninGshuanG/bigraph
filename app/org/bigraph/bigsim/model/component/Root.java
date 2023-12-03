package org.bigraph.bigsim.model.component;

import org.bigraph.bigsim.utils.NameGenerator;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author huangningshuang
 * @date 2023/11/30
 */
public class Root implements Parent {
    private BigraphHandler owner;
    private Set<Child> children = new HashSet<>();
    private final Set<? extends Child> roChildren = Collections.unmodifiableSet(children);
    private String name;

    public Root(BigraphHandler owner) {
        this.owner = owner;
        this.name = "R_" + NameGenerator.DEFAULT.generate();
    }

    @Override
    public String toString() {
        if (owner != null) {
            int i = owner.getRoots().indexOf(this);
            if (i >= 0)
                return i + ":r";
        }
        return this.name;
    }

    @Override
    public Collection<? extends Child> getChildren() {
        return roChildren;
    }

    @Override
    public void addChild(Child child) {
        if (child == null) return;
        children.add(child);
        child.setParent(this);
    }

    @Override
    public void removeChild(Child child) {
        if (child == null) return;
        children.remove(child);
        if (child.getParent() == this) {
            child.setParent(null);
        }
    }

    @Override
    public Root getRoot() {
        return this;
    }

    @Override
    public boolean isSite() {
        return false;
    }

    @Override
    public boolean isNode() {
        return false;
    }

    @Override
    public boolean isRoot() {
        return true;
    }

    @Override
    public boolean isParent() {
        return true;
    }

    @Override
    public boolean isChild() {
        return false;
    }

    public BigraphHandler getOwner() {
        return owner;
    }

    public void setOwner(BigraphHandler owner) {
        this.owner = owner;
    }
}
