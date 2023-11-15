package org.bigraph.bigsim.transitionsystem;


import java.util.*;

public class KripkeStructure {
    private Set<State> states = new HashSet<>();//保存所有状态的状态列表
    private Set<State> initialStates = new HashSet<>();//只保存初始状态的初始状态列表
    private Map<State, Set<State>> transitions = new HashMap<>();//是一个map，key是当前状态state，value是当前状态的下一个可到达的状态的集合
    private Map<State, Set<State>> revTransitions = new HashMap<>(); // 反向边，方便不动点算法使用

    /**
     * 把参数 states状态加入到成员变量states集合中去
     *
     * @param states input states, to be added into this.states
     */
    public void addState(State... states) {
        Collections.addAll(this.states, states);
    }//把参数中的states数组添加到类成员变量states中

    /**
     * 把参数 states 状态分别加入到states 和 initialStates中去
     *
     * @param states : to be added into this.states & this.initialStates
     */
    public void addInitialState(State... states) {
        addState(states);//首先把状态保存到状态列表中
        Collections.addAll(this.initialStates, states);//在独立把初始状态保存到初始状态列表中
    }

    /**
     * This function is to add States into this.states set. Meanwhile, these states can react to them self
     * no terminal state
     *
     * @param states : input states,
     */
    public void addFinalState(State... states) {
        addState(states);
        for (State state : states) {
            addTransition(state, state);
        }
    }

    /**
     * This function is to add a transition relation into this.transition.
     * 如果原来src已存在，那么把dst存放到对应的集合中去，不然，新建单元素的集合（只有dst），作为src的value
     *
     * @param src : the source of the transition
     * @param dst : the destination of the transition
     */
    public void addTransition(State src, State dst) {//Collections.singleton(dst)返回只包含dst元素的不可变集合
        //如果src作为key在原有的map中不存在，merge方法相当于map.put(src,Collections.singleton(dst)),
        // 如果原有的map里已经存在，oldvalue是key在原有map中对应的value，newvalue是Collections.singleton(dst)，
        // 两者进行相应的操作之后作为新值重新存储到map中，改变原有key在map中的值
        transitions.merge(src, Collections.singleton(dst), (oldValue, newValue) -> {
            HashSet<State> mergeValue = new HashSet<>(oldValue);
            mergeValue.addAll(newValue);
            return mergeValue;
        });
        revTransitions.merge(dst, Collections.singleton(src), (oldValue, newValue) -> {
            HashSet<State> mergeValue = new HashSet<>(oldValue);
            mergeValue.addAll(newValue);
            return mergeValue;
        });
    }

    /**
     * To check states and initialStates not empty; And check each transition's dst not empty
     *
     * @return :boolean
     */
    public boolean isValid() {
        try {
            validate();
        } catch (RuntimeException e) {
            return false;
        }
        return true;
    }

    public void validate() {
        if (states.isEmpty()) {
            throw new RuntimeException("Set of states is empty");
        }
        if (initialStates.isEmpty()) {
            throw new RuntimeException("Set of initial state is empty");
        }
        validateTransitionAreLeftTotal();
    }

    public Set<State> getAllStates() {
        return states;
    }

    public Set<State> getInitialStates() {
        return new HashSet<>(initialStates);
    }

    /**
     * Get all the successor of state
     *
     * @param state: src
     * @return the successor of src
     */
    public Set<State> getAllSuccessorStates(State state) {//返回当前状态state的所有下一个状态
        return new HashSet<>(transitions.get(state));
    }

    public Set<State> getAllPreStates(State state) {
        return revTransitions.getOrDefault(state, new HashSet<>());
    }

    /**
     * validate the transition
     */
    public void validateTransitionAreLeftTotal() {
        states.forEach(state -> {
            if (transitions.getOrDefault(state, new HashSet<>()).isEmpty()) {
                throw new RuntimeException(String.format("There is no transition starting from state %s.", state));
            }
        });
    }

    public String toString() {
        StringBuilder ret = new StringBuilder("All States is: \n");
        for (State s : states) {
            ret.append(s).append("\n");
        }
        ret.append("All transition is: \t");
        for (Map.Entry<State, Set<State>> entry : transitions.entrySet()) {
            State key = entry.getKey();
            Set<State> tmp = entry.getValue();
            StringBuilder val = new StringBuilder();
            for (State s : tmp) {
                val.append("\t").append(s);
            }
            ret.append("\n\tkey:").append(key).append("\tvalue:").append(val);
        }
        ret.append("\nAll revTransition is:");
        for (Map.Entry<State, Set<State>> entry : revTransitions.entrySet()) {
            State key = entry.getKey();
            Set<State> tmp = entry.getValue();
            StringBuilder val = new StringBuilder();
            for (State s : tmp) {
                val.append("\t").append(s);
            }
            ret.append("\n\tkey:").append(key).append("\tvalue:").append(val);
        }
        ret.append("\nstate size: ").append(states.size()).append(" ").append(transitions.size()).append(" ").append(revTransitions.size());
        return ret.toString();
    }
}
