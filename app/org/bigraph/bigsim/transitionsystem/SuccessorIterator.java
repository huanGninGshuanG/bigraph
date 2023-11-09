package org.bigraph.bigsim.transitionsystem;

import java.util.*;

import org.bigraph.bigsim.simulator.SuccessorBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SuccessorIterator implements Iterable<State>, Iterator<State>{

    private static final Logger logger = LoggerFactory.getLogger(SuccessorIterator.class);
    private final KripkeStructureOnTheFly kripkeStructure;
    private final State predecessor;

    protected Set<State> curSuccessorStates;
    protected Iterator<State> iterator;
    protected SuccessorBuilder builder;

    public SuccessorIterator(KripkeStructureOnTheFly kripkeStructure, State state) {                    //   构造函数
        this.kripkeStructure = kripkeStructure;
        this.predecessor = state;
    }

    public void resetState() {
        this.curSuccessorStates = this.kripkeStructure.getCurSuccessorStates(this.predecessor);          // 重新查找后继状态的集合
        this.iterator = this.curSuccessorStates.iterator();// 已知后继状态的迭代器
        boolean hasnextsuccessor = this.kripkeStructure.transitionSystem.hasNextSuccessor(this.predecessor);        // 查找后继创建器，如果没有，此时会创建
        logger.debug("是否有待创建后继：" + hasnextsuccessor);
        this.builder = this.kripkeStructure.transitionSystem.getSuccessorBuilder(this.predecessor);      // 获得后继的创建器
    }

    /**
     * 首先判断
     * @return 是否还有下一个状态；
     */
    public boolean hasNext() {
        return iterator.hasNext() || this.kripkeStructure.transitionSystem.hasNextSuccessor(this.predecessor);
    }

    public State next() {
        assert(hasNext());                      // 确保还有下一个状态
        if (iterator.hasNext()) {               // 如果curSuccessorStates还没有迭代完
            logger.debug("返回curSuccessorStates的下一个");
            return iterator.next();             // 那么返回 curSuccessorStates 的下一个
        } else {         // 否则，如果创建器还没有迭代完
            assert(builder.hasNext());
            logger.debug("从创建器获得下一个");
            State successor = builder.next();
            return this.kripkeStructure.addSuccessor(this.predecessor, successor);      // 新建的状态添加到kripkestructure中去，并返回
        }
    }

    public void remove() {
        throw new UnsupportedOperationException("This remove method not implement so far");
    }

    public Iterator<State> iterator() {
        resetState();
        if (!hasNext()) {           // 如果当前的迭代器初始时查不到后继，那么说明当前状态是一个FinalState
            logger.debug("查不到后继，添加为终止状态");
            this.kripkeStructure.addFinalState(predecessor);        // 记录为一个FinalState
            resetState();           // 并且重新设置迭代器
        }
        return this;
    }

}