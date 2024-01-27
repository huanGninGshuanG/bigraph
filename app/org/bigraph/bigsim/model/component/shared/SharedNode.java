package org.bigraph.bigsim.model.component.shared;

import javafx.util.Pair;
import org.bigraph.bigsim.model.component.Control;
import org.bigraph.bigsim.model.component.Handle;
import org.bigraph.bigsim.model.component.Point;
import org.bigraph.bigsim.utils.DebugPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author huangningshuang
 * @date 2024/1/20
 */
public class SharedNode implements SharedParent, SharedChild {
    private static final Logger logger = LoggerFactory.getLogger(SharedNode.class);

    private Control control;
    private final List<SharedNode.Port> ports;
    private Set<SharedParent> parents;
    private Set<SharedChild> children;
    private String name;

    private final List<? extends SharedNode.Port> roPorts; // read only ports
    private final Collection<? extends SharedChild> roChildren; // read only children

    public SharedNode(Control control, String name) {
        this(name, control, null);
    }

    public SharedNode(String name, Control control, SharedParent parent) {
        this.name = name;
        this.control = control;
        List<SharedNode.Port> ports = new ArrayList<>();
        for (int i = 0; i < control.getArity(); i++) {
            ports.add(new SharedNode.Port(i));
        }
        this.ports = Collections.unmodifiableList(ports);
        this.children = new HashSet<>();
        this.roPorts = Collections.unmodifiableList(this.ports);
        this.roChildren = Collections.unmodifiableCollection(this.children);
        this.parents = new HashSet<>();
        addParent(parent);
    }

    public SharedNode(String name, Control control, SharedParent parent, List<? extends Handle> handles) {
        this(name, control, parent);
        for (int i = 0; i < Math.min(handles.size(), control.getArity()); i++) {
            this.ports.get(i).setHandle(handles.get(i));
        }
    }

    @Override
    public Degree computeDegree() {
        int pNode = 0, pRoot = 0, cNode = 0, cSite = 0;
        for (SharedParent p : parents) {
            if (p.isNode()) pNode++;
            else if (p.isRoot()) pRoot++;
        }
        for (SharedChild c : children) {
            if (c.isNode()) cNode++;
            else if (c.isSite()) cSite++;
        }
        return new Degree(new Pair<>(pNode, pRoot), new Pair<>(cNode, cSite));
    }

    @Override
    public String toString() {
        return this.name + ':' + this.control.getName();
    }

    @Override
    public int hashCode() {
        final int prime = 41;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public Collection<? extends SharedParent> getParents() {
        return parents;
    }

    @Override
    public void addParent(SharedParent p) {
        if (p == null) return;
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
    public boolean isSite() {
        return false;
    }

    @Override
    public boolean isNode() {
        return true;
    }

    @Override
    public boolean isRoot() {
        return false;
    }

    @Override
    public boolean isParent() {
        return true;
    }

    @Override
    public boolean isChild() {
        return true;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Control getControl() {
        return control;
    }

    public void setControl(Control control) {
        this.control = control;
    }

    public List<? extends SharedNode.Port> getPorts() {
        return this.roPorts;
    }

    public SharedNode.Port getPort(int index) {
        return this.ports.get(index);
    }

    @Override
    public SharedNode replicate() {
        return new SharedNode(this.control, this.name);
    }

    public class Port implements Point {
        private final int index; // 该port是node的第几个port
        private Handle handle;

        public Port(int index) {
            this.index = index;
        }

        public SharedNode getSharedNode() {
            return SharedNode.this;
        }

        public int getIndex() {
            return index;
        }

        @Override
        public String toString() {
            return index + "@" + SharedNode.this.name + "->" + handle;
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
}
