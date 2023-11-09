package org.bigraph.bigsim.ltlimpl;

import org.bigraph.bigsim.transitionsystem.State;

import java.util.ArrayList;

public class CheckAUResult {
    public enum SearchContinuation{
        CONTINUE,
        ABORT;
    }
    private SearchContinuation searchContinuation;
    private ArrayList<State> counterExample;
    public CheckAUResult(SearchContinuation searchContinuation,State lastStateInCounterExample){
        this.searchContinuation = searchContinuation;
        this.counterExample = new ArrayList<>();
        this.counterExample.add(lastStateInCounterExample);
    }
    public SearchContinuation getSearchContinuation(){
        return searchContinuation;
    }
    public ArrayList<State> getCounterExample(){
        return counterExample;
    }
    public void prependCountExampleWith(State state){
        counterExample.add(0,state);
    }
}
