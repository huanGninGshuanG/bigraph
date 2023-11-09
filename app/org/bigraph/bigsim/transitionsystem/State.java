package org.bigraph.bigsim.transitionsystem;

import org.bigraph.bigsim.ctlspec.atom.Atom;

import java.util.HashSet;
import java.util.Set;

public class State {
    private final String name;
    private final Set<Atom> truePredicates = new HashSet<>();

    public State(String name, String... atomicPredicates) {
        this.name = name;
        for(String atomicPredicate : atomicPredicates){
            truePredicates.add(new Atom(atomicPredicate));
        }
    }

    public boolean satisfies(Atom predicate){
        return truePredicates.contains(predicate);
    }
    @Override
    public String toString(){
        String tmp = "";
        for (Atom a : truePredicates) {
            tmp += "\t" + a;
        }
        return name + tmp;
        //return name;
    }
    public String getName(){
        return name;
    }
}
