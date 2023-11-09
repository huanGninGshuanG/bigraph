package org.bigraph.bigsim.ctlspec.atom;
import org.bigraph.bigsim.ctlspec.Formula;

public class False implements Formula {
    public static False False(){
        return new False();
    }
    @Override
    public Formula convertToCTLBase(){
        return this;
    }
    @Override
    public String toString(){
        return "false";
    }
    @Override
    public boolean equals(Object o){
        return this == o || (getClass()==o.getClass());
    }
    @Override
    public int hashCode(){
        return 0;
    }
}
