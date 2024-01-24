package org.bigraph.bigsim.model.component.shared;

import org.bigraph.bigsim.model.component.BigraphHandler;
import org.bigraph.bigsim.model.component.Parent;
import org.bigraph.bigsim.model.component.Site;
import org.bigraph.bigsim.utils.NameGenerator;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author huangningshuang
 * @date 2024/1/20
 */
public class SharedSite implements SharedChild {
    private Set<SharedParent> parents;
    private String name;

    public SharedSite(String name, SharedParent parent) {
        this.name = name;
        parents = new HashSet<>();
        addParent(parent);
    }

    public String getName() {
        return name;
    }

    public SharedSite(SharedParent parent) {
        this(NameGenerator.DEFAULT.generate(), parent);
    }

    public SharedSite() {
        this(NameGenerator.DEFAULT.generate(), null);
    }

    @Override
    public String toString() {
        return this.name + "-> [" + parents + "]";
    }

    @Override
    public Collection<? extends SharedParent> getParents() {
        return parents;
    }

    @Override
    public void addParent(SharedParent p) {
        if (p == null || parents.contains(p)) return;
        parents.add(p);
        p.addChild(this);
    }

    @Override
    public void removeParent(SharedParent p) {
        if (p == null || !parents.contains(p)) return;
        parents.remove(p);
        p.removeChild(this);
    }

    @Override
    public SharedSite replicate() {
        return new SharedSite();
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
