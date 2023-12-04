package org.bigraph.bigsim.model.component;

/**
 * @author huangningshuang
 * @date 2023/11/30
 */
public class Control {
    private int arity;
    private String name;
    private boolean active;

    public Control(String name, boolean active, int arity) {
        this.name = name;
        this.active = active;
        this.arity = arity;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getArity() {
        return arity;
    }

    public void setArity(int arity) {
        this.arity = arity;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        Control other = (Control) obj;
        return getArity() == other.getArity()
                && name.equals(other.getName());
    }

    private String _tostring;

    @Override
    public String toString() {
        StringBuilder builder;
        if (_tostring == null) {
            builder = new StringBuilder();
            builder.append(getName()).append(":(").append(getArity()).append((active) ? ",a)" : ",p)");
            _tostring = builder.toString();
        }
        return _tostring;
    }
}
