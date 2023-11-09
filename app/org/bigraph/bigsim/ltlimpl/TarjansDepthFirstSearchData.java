package org.bigraph.bigsim.ltlimpl;

import org.bigraph.bigsim.transitionsystem.State;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class TarjansDepthFirstSearchData {
    private int maxDfs = 0;
    private Map<State, Integer> dfs = new HashMap<>();
    private Map<State, Integer> lowlink = new HashMap<>();
    private Stack<State> stack = new Stack();

    public void visit(State state){
        stack.push(state);
        setDfs(state,maxDfs);
        setLowLink(state,maxDfs);
        maxDfs++;
    }
    public boolean isVisited(State state){
        return dfs.containsKey(state);
    }
    public int getMaxDfs(){
        return maxDfs;
    }
    public int getDfs(State state){
        return dfs.getOrDefault(state,0);
    }
    private void setDfs(State state,int dfs){
        this.dfs.put(state,dfs);
    }
    public int getLowLink(State state){
        return lowlink.getOrDefault(state,0);
    }
    public void setLowLink(State state,int lowlink){
        this.lowlink.put(state,lowlink);
    }
    public boolean dfsEqualsLowLink(State state){
        return getDfs(state) == getLowLink(state);
    }
    public boolean isOnstack(State state){
        return stack.contains(state);
    }
    public State popFromStack(){
        return stack.pop();
    }
    public void removeFromStack(State state){
        stack.remove(state);
    }
    public String getStackAsString(){
        return stack.toString();
    }
}
