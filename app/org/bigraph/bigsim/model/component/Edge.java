package org.bigraph.bigsim.model.component;

import org.bigraph.bigsim.utils.NameGenerator;

import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;

/**
 * @author huangningshuang
 * @date 2023/11/30
 */
public class Edge implements Handle, Replicable {
    private Collection<Point> points = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Collection<? extends Point> roPoints = Collections.unmodifiableCollection(this.points);
    private String name;

    public Edge() {
        this("E_" + NameGenerator.DEFAULT.generate());
    }

    public Edge(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "[Edge]" + this.name;
    }

    @Override
    public Collection<? extends Point> getPoints() {
        return roPoints;
    }

    @Override
    public void linkPoint(Point point) {
        if (point == null)
            return;
        this.points.add(point);
        if (this != point.getHandle()) {
            point.setHandle(this);
        }
    }

    @Override
    public void unlinkPoint(Point point) {
        if (point == null)
            return;
        this.points.remove(point);
        if (this == point.getHandle())
            point.setHandle(null);
    }

    @Override
    public int hashCode() {
        final int prime = 83;
        return prime * points.hashCode();
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
        return false;
    }

    @Override
    public boolean isOuterName() {
        return false;
    }

    @Override
    public boolean isEdge() {
        return true;
    }

    @Override
    public Edge replicate() {
        return new Edge(this.getName());
    }
}
