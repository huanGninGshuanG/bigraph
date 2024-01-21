package org.bigraph.bigsim.model.component.shared;

import org.bigraph.bigsim.utils.NameGenerator;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author huangningshuang
 * @date 2024/1/20
 */
public class SharedRoot implements SharedParent {
    private Set<SharedChild> children = new HashSet<>();
    private final Set<? extends SharedChild> roChildren = Collections.unmodifiableSet(children);
    private String name;

    public SharedRoot() {
        this.name = "SR_" + NameGenerator.DEFAULT.generate();
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public Collection<? extends SharedChild> getChildren() {
        return roChildren;
    }

    @Override
    public void addChild(SharedChild child) {
        if (child == null || children.contains(child)) return;
        children.add(child);
        child.addParent(this);
    }

    @Override
    public void removeChild(SharedChild child) {
        if (child == null || !children.contains(child)) return;
        children.remove(child);
        child.removeParent(this);
    }

    @Override
    public SharedRoot replicate() {
        return new SharedRoot();
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
}
