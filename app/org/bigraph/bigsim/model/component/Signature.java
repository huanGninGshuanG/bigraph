package org.bigraph.bigsim.model.component;

import org.bigraph.bigsim.utils.DebugPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author huangningshuang
 * @date 2023/12/1
 */
public class Signature implements Iterable<Control> {
    final private Map<String, Control> ctrls = new HashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(Signature.class);

    public Signature(Iterable<Control> controls) {
        for (Control c : controls) {
            if (ctrls.put(c.getName(), c) != null) {
                throw new IllegalArgumentException("Controls must be uniquely named within the same signature");
            }
        }
    }

    public void add(Control control) {
        if (ctrls.containsKey(control.getName()) && !ctrls.containsValue(control)) {
            throw new IllegalArgumentException("Controls must be uniquely named within the same signature");
        }
        ctrls.put(control.getName(), control);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ctrls.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof Signature))
            return false;
        Signature other = (Signature) obj;
        for (Control c : ctrls.values()) {
            if (!c.equals(other.getByName(c.getName())))
                return false;
        }
        return ctrls.equals(other.ctrls);
    }

    public boolean equals(Signature other) {
        return this.equals(other, false);
    }

    public boolean equals(Signature other, boolean dummy) {
        if (this == other)
            return true;
        if (other == null)
            return false;
        for (Control c : ctrls.values()) {
            if (!c.equals(other.getByName(c.getName())))
                return false;
        }
        return ctrls.equals(other.ctrls);
    }

    public Control getByName(String name) {
        return ctrls.get(name);
    }

    @Override
    public String toString() {
        return "Signature:" + ctrls.values();
    }

    public boolean contains(String name) {
        return this.ctrls.containsKey(name);
    }

    public boolean contains(Control control) {
        return this.ctrls.containsValue(control);
    }

    public boolean isEmpty() {
        return this.ctrls.isEmpty();
    }

    @Override
    @Nonnull
    public Iterator<Control> iterator() {
        return Collections.unmodifiableMap(this.ctrls).values().iterator();
    }

    public int size() {
        return this.ctrls.size();
    }
}
