package org.bigraph.bigsim.model.component;

import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;

/**
 * @author huangningshuang
 * @date 2023/12/1
 */
public class OuterName implements Handle {
    private Collection<Point> points = Collections
            .newSetFromMap(new IdentityHashMap<>());
    private final Collection<? extends Point> roPoints = Collections
            .unmodifiableCollection(this.points);
    private String name;

    public OuterName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public Collection<? extends Point> getPoints() {
        return roPoints;
    }

    @Override
    public void linkPoint(Point point) {
        if (point == null) return;
        this.points.add(point);
        if (this != point.getHandle()) {
            point.setHandle(this);
        }
    }

    @Override
    public void unlinkPoint(Point point) {
        if (point == null)
            return;
        if (this.points.remove(point) && this == point.getHandle())
            point.setHandle(null);
    }

    @Override
    public boolean isHandle() {
        return true;
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
        return true;
    }

    @Override
    public boolean isEdge() {
        return false;
    }
}
