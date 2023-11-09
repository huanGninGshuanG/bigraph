package org.bigraph.bigsim.ltlimpl;

import org.bigraph.bigsim.transitionsystem.State;

import java.util.ArrayList;

public class CheckLTLUResult {
    public enum SearchContinuation{
        CONTINUE,
        ABORT;
    }
    private SearchContinuation searchContinuation;
    private ArrayList<State> witnessPath;
    public CheckLTLUResult(SearchContinuation searchContinuation, State lastStateInWitnessPath){
        this.searchContinuation = searchContinuation;
        this.witnessPath = new ArrayList<>();
        this.witnessPath.add(lastStateInWitnessPath);
    }
    public SearchContinuation getSearchContinuation(){
        return searchContinuation;
    }
    public ArrayList<State> getWitnessPath(){
        return witnessPath;
    }
    public void prependWitnessPathWith(State state){
        witnessPath.add(0,state);
    }
}
