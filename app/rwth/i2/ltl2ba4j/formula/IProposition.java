package rwth.i2.ltl2ba4j.formula;

/**
 * @author amy
 *
 * IFormula
 */
public interface IProposition extends IFormula {

    /**
     * @return the label
     */
    public String getLabel();
    
    /**
     * Clients should implement this method, since the
     * implementation uses HashSets, which require a proper
     * notion of equality.
     * @see Object#equals(Object)
     */
    public boolean equals(Object obj);
    
    /**
     * Clients should implement this method, since the
     * implementation uses HashSets, which require a proper
     * hash code.
     * @see Object#hashCode()
     */
    public int hashCode();
    
}
