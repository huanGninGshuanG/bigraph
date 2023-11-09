package org.bigraph.bigsim.transitionsystem;


import javax.sql.rowset.spi.TransactionalWriter;
import java.util.*;
import org.bigraph.bigsim.simulator.TransitionSystemOnTheFly;
import shapeless.Succ;

public class KripkeStructureOnTheFly {

    private Set<State> states = new HashSet<>();//保存所有状态的状态列表
    private Set<State> initialStates = new HashSet<>();//只保存初始状态的初始状态列表
    private Map<State,Set<State>> transitions = new HashMap<>();//是一个map，key是当前状态state，value是当前状态的下一个可到达的状态的集合

    public TransitionSystemOnTheFly transitionSystem;

    public KripkeStructureOnTheFly(TransitionSystemOnTheFly transitionSystem) {
        this.transitionSystem = transitionSystem;
    }


    /**
     * 把参数 states状态加入到成员变量states集合中去
     * @param states input states, to be added into this.states
     */
    public void addState(State... states){
        Collections.addAll(this.states,states);
    }//把参数中的states数组添加到类成员变量states中

    /**
     * 把参数 states 状态分别加入到states 和 initialStates中去
     * @param states : to be added into this.states & this.initialStates
     */
    public void addInitialState(State... states){
        addState(states);//首先把状态保存到状态列表中
        Collections.addAll(this.initialStates,states);//在独立把初始状态保存到初始状态列表中
    }

    /**
     * This function is to add States into this.states set. Meanwhile, these states can react to them self。
     * @param states : input states,
     */
    public void addFinalState(State... states){
        addState(states);
        for(State state:states){
            addTransition(state,state);
        }
    }

    /**
     * This function is to add a transition relation into this.transition.
     * 如果原来src已存在，那么把dst存放到对应的集合中去，不然，新建单元素的集合（只有dst），作为src的value
     * @param src : the source of the transition
     * @param dst : the destination of the transition
     */
    public void addTransition(State src, State dst){//Collections.singleton(dst)返回只包含dst元素的不可变集合
        //如果src作为key在原有的map中不存在，merge方法相当于map.put(src,Collections.singleton(dst)),
        // 如果原有的map里已经存在，oldvalue是key在原有map中对应的value，newvalue是Collections.singleton(dst)，
        // 两者进行相应的操作之后作为新值重新存储到map中，改变原有key在map中的值
        transitions.merge(src,Collections.singleton(dst),(oldValue,newValue)->{
            HashSet<State> mergeValue = new HashSet<>(oldValue);
            mergeValue.addAll(newValue);
            return mergeValue;
        });
    }

    public State addSuccessor(State predecessor, State successor) {
        addState(successor);                                            // 首先，添加后继
        addTransition(predecessor, successor);                          // 然后，添加迁移关系
        return successor;                                               // 返回结果 是后继状态
    }

    /**
     *  To check states and initialStates not empty; And check each transition's dst not empty
     * @return :boolean
     */
    public boolean isValid(){
        try{
            validate();
        } catch (RuntimeException e){
            return false;
        }
        return true;
    }

    public void validate(){
        if(states.isEmpty()){
            throw new RuntimeException("Set of states is empty");
        }
        if(initialStates.isEmpty()){
            throw new RuntimeException("Set of initial state is empty");
        }
    }

    public Set<State> getInitialStates(){
        return new HashSet<>(initialStates);
    }


    public Set<State> getCurSuccessorStates(State state) {          // 返回当前已经衍化得到的后继状态（transition中已有的）
        return new HashSet<>(transitions.computeIfAbsent(state, key -> new HashSet<State>()));
    }


    /**
     * Get all the successor of state
     * @param state: src
     * @return the successor of src
     */
    public SuccessorIterator getAllSuccessorStates(State state){    //返回当前状态state的后继状态的迭代器
        return new SuccessorIterator(this, state);
    }

    public Set<State> getAllStates(){
        return states;
    }

    public Map<State,Set<State>> getAllTransition(){
        return transitions;
    }

    public String toString(){
        String ret = "All States is: \n";
        for (State s: states) {
            ret += s + "\n";
        }
        ret += "All transition is: \t";
        for (Map.Entry<State, Set<State>> entry : transitions.entrySet()) {
            State key = entry.getKey();
            Set<State> tmp = entry.getValue();
            String val = "";
            for (State s: tmp) {
                val += "\t" + s;
            }
            ret += "\n\tkey:" + key + "value:" + val;
        }
        return ret;
    }
}