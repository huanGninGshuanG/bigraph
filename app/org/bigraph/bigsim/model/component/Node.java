package org.bigraph.bigsim.model.component;

import org.bigraph.bigsim.utils.NameGenerator;

import java.util.*;

/**
 * @author huangningshuang
 * @date 2023/11/30
 */
public class Node implements Parent, Child, Replicable {

    private Control control;
    private final List<Port> ports;
    private Parent parent;
    private Set<Child> children;
    private String name;

    private final List<? extends Port> roPorts; // read only ports
    private final Collection<? extends Child> roChildren; // read only children

    public Node(Control control, String name) {
        this(name, control, null);
    }

    public Node(String name, Control control, Parent parent) {
        this.name = name;
        this.control = control;
        List<Port> ports = new ArrayList<>();
        for (int i = 0; i < control.getArity(); i++) {
            ports.add(new Port(i));
        }
        this.ports = Collections.unmodifiableList(ports);
        this.children = new HashSet<>();
        this.roPorts = Collections.unmodifiableList(this.ports);
        this.roChildren = Collections.unmodifiableCollection(this.children);
        setParent(parent);
    }

    public Node(String name, Control control, Parent parent, List<? extends Handle> handles) {
        this(name, control, parent);
        for (int i = 0; i < Math.min(handles.size(), control.getArity()); i++) {
            this.ports.get(i).setHandle(handles.get(i));
        }
    }

    @Override
    public String toString() {
        return this.name + ':' + this.control.getName() + "->" + parent + "::" + ports;
    }

    @Override
    public int hashCode() {
        final int prime = 41;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public Parent getParent() {
        return parent;
    }

    @Override
    public void setParent(Parent p) {
        if (this.parent != p) {
            Parent old = this.parent;
            this.parent = p;
            if (old != null) old.removeChild(this);
            if (p != null) p.addChild(this);
        }
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
        return parent.getRoot();
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

    public List<? extends Port> getPorts() {
        return this.roPorts;
    }

    public Port getPort(int index) {
        return this.ports.get(index);
    }

    @Override
    public Node replicate() {
        return new Node(this.control, this.name);
    }

    public class Port implements Point {
        private final int index; // 该port是node的第几个port
        private Handle handle;

        public Port(int index) {
            this.index = index;
        }

        public Node getNode() {
            return Node.this;
        }

        public int getIndex() {
            return index;
        }

        @Override
        public String toString() {
            return index + "@" + Node.this.name + "->" + handle;
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
