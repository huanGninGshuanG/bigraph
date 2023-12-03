package org.bigraph.bigsim.model.component;

import org.bigraph.bigsim.utils.NameGenerator;

import java.util.*;

/**
 * @author huangningshuang
 * @date 2023/11/30
 */
public class Node implements Parent, Child {

    private Control control;
    private final List<Port> ports;
    private Parent parent;
    private Set<Child> children;
    private String name;

    private final List<? extends Port> roPorts; // read only ports
    private final Collection<? extends Child> roChildren; // read only children

    public Node(Control control, Parent parent) {
        this.name = "N_" + NameGenerator.DEFAULT.generate();
        this.control = control;
        this.parent = parent;
        List<Port> ports = new ArrayList<>();
        for (int i = 0; i < control.getArity(); i++) {
            ports.add(new Port(i));
        }
        this.ports = Collections.unmodifiableList(ports);
        this.children = new HashSet<>();
        this.roPorts = Collections.unmodifiableList(this.ports);
        this.roChildren = Collections.unmodifiableCollection(this.children);
    }

    public Node(Control control, Parent parent, List<? extends Handle> handles) {
        this(control, parent);
        for (int i = 0; i < Math.min(handles.size(), control.getArity()); i++) {
            this.ports.get(i).setHandle(handles.get(i));
        }
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
}
