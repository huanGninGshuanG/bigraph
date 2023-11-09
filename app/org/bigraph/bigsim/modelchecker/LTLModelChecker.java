package org.bigraph.bigsim.modelchecker;
import org.bigraph.bigsim.ctlspec.Formula;
import org.bigraph.bigsim.ctlspec.atom.Atom;
import org.bigraph.bigsim.ctlspec.atom.False;
import org.bigraph.bigsim.ctlspec.atom.True;
import org.bigraph.bigsim.ctlspec.operator.And;
import org.bigraph.bigsim.ctlspec.operator.Not;
import org.bigraph.bigsim.ctlspec.operator.Or;
import org.bigraph.bigsim.ltlimpl.CheckLTLUResult;
import org.bigraph.bigsim.ltlimpl.TarjansDepthFirstSearchData;
import org.bigraph.bigsim.ltlspec.operator.*;
import org.bigraph.bigsim.transitionsystem.KripkeStructure;
import org.bigraph.bigsim.transitionsystem.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class LTLModelChecker {
    public enum SearchContinuation{
        CONTINUE,
        ABORT;
    }
    private static final Logger logger = LoggerFactory.getLogger(LTLModelChecker.class);
    private final KripkeStructure kripkeStructure;
    private Map<State, Map<Formula,Boolean>> labels = new HashMap<>();
    public ArrayList<State> recordPath;  // add
    public LTLModelChecker(KripkeStructure kripkeStructure){
        this.kripkeStructure = kripkeStructure;
    }
    public static boolean satisfies(KripkeStructure kripkeStructure,Formula formula){
        return new LTLModelChecker(kripkeStructure).satisfies(formula);
    }
    public boolean satisfies(Formula formula){
        kripkeStructure.validate();//先判断kripke结构是否为空
        logger.debug("Starting to check whether the given Kripke structure satisifies {}.",formula);
//        System.out.println(formula.toString());
        Formula formulaBase = formula.convertToCTLBase();//通过调用这个方法使用重言式把CTL formula转换为基本的CTL formula，基础的CTL公式就是那8种
        logger.debug("Converted given formula to formula in out ltl base using tautologies:{}.",formulaBase);
        return kripkeStructure.getInitialStates().stream().allMatch(initialState -> satisfies(initialState,formulaBase));//对每一个初始状态都验证是否满足CTL formula
    }
    private boolean addLabel(State state, Formula formula, boolean value){
        Map<Formula,Boolean> labelsForState = labels.computeIfAbsent(state,k-> new HashMap<>());//如果state对应的value值不为空直接返回value值，如果为空返回一个空的map
        labelsForState.put(formula,value);
        logger.debug("Labelled:("+state+","+formula+")->"+value);
        return value;
    }
    private boolean satisfies(State state,Formula formula){
        return getLabel(state,formula).orElseGet(()->computeLabel(state,formula));
        //如果getLabel(state,formula)不为空则直接返回这个值，如果为空需要执行computeLabel(state,formula)
    }
    private Optional<Boolean> getLabel(State state,Formula formula){
        return Optional.ofNullable(labels.getOrDefault(state,new HashMap<>()).get(formula));
        //方法返回Boolean类型的optional容器，首先名称为labels的map查找是否含有key值为state的value值，如果有获取value值，
        // 如果没有新建一个空的map，获取的value值还是一个map类型，调用get方法，查找key值为formula的value值，这个value值是一个boolean，
        // 如果此时的value值非空，返回value值，否则返回空的optional类型元素
    }
    private boolean computeLabel(State state,Formula formula){
        if(formula instanceof True){
            return addLabel(state,formula,true);
        }
        if(formula instanceof False){
            return addLabel(state,formula,false);
        }
        if(formula instanceof Atom){
            return addLabel(state,formula,state.satisfies((Atom) formula));
        }
        if(formula instanceof Or){
            for(Formula subFormula:((Or) formula).getOperands()){//or运算符有多个操作数，对每一个操作数都进行是否满足规约的判断
                if(satisfies(state,subFormula)){//如果有一个操作数满足规约就返回true，如果所有的操作数都不满足规约就返回false
                    return addLabel(state,formula,true);
                }
            }
            return addLabel(state,formula,false);
        }
        if(formula instanceof And){//and运算符有多个操作数，对每一个操作数判断是否满足规约
            for(Formula subFormula:((And)formula).getOperands()){
                if(!satisfies(state,subFormula)){//如果有一个操作数不满足规约就返回false，否则直到所有的操作数都满足规约就返回true
                    return addLabel(state,formula,false);
                }
            }
            return addLabel(state,formula,true);
        }
        if(formula instanceof Not){
            return addLabel(state,formula,!satisfies(state,((Not) formula).getOperand()));//返回原本命题是否满足规约的取非
        }
        if(formula instanceof LTLOperatorX){//LTLOperatorX表示当前状态state的的下一个状态要满足规约
            Formula subFormula = ((LTLOperatorX) formula).getOperand();
            if(kripkeStructure.getAllSuccessorStates(state).iterator().hasNext()){
                State successorState= kripkeStructure.getAllSuccessorStates(state).iterator().next();
                if(satisfies(successorState,subFormula)){//如果路径的下一个状态满足规约就返回True
                    return addLabel(state,formula,true);
                }
            }
            return addLabel(state,formula,false);//不满足规约返回false
        }
        if(formula instanceof LTLOperatorU){
            CheckLTLUResult checkLTLUResult = checkLTLOperatorU(state,(LTLOperatorU)formula);
            boolean isFormulaSatisfied = getLabel(state,formula).get();
            if(isFormulaSatisfied){
                this.recordPath = checkLTLUResult.getWitnessPath();
                logger.debug("LTLOperatorU:Found witness path for {} starting from {}:{}.",formula,state,this.recordPath);
                //logger.debug("LTLOperatorU:Found witness path for {} starting from {}:{}.",formula,state,checkEUResult.getWitnessPath());
            }else{
                logger.debug("LTLOperatorU:Found no witness path for {} starting from {}.",formula,state);
            }
            return isFormulaSatisfied;
        }
        throw new IllegalArgumentException(formula.toString());
    }
    private CheckLTLUResult checkLTLOperatorU(State state, LTLOperatorU formula){
        TarjansDepthFirstSearchData dfsData = new TarjansDepthFirstSearchData();
        return checkLTLOperatorU(state,formula,dfsData);
    }
    private CheckLTLUResult checkLTLOperatorU(State state,LTLOperatorU formula,TarjansDepthFirstSearchData dfsData){
        Formula op1 = formula.getOperand1();
        Formula op2 = formula.getOperand2();
        Optional<Boolean> label = getLabel(state,formula);
        if(label.isPresent()){//isPresent()方法判断类对象是否存在
            if(label.get()){//如果存在，通过get方法返回对象
                return new CheckLTLUResult(CheckLTLUResult.SearchContinuation.ABORT,state);//停止查找
            }else {
                return new CheckLTLUResult(CheckLTLUResult.SearchContinuation.CONTINUE,state);//继续查找
            }
        }
        if(satisfies(state,op2)){//如果第二个命题为真，state满足U规约，停止查找
            logger.debug("LTLOperatorU:{} satisifies {}. So {} also satisifies{}.",state,op2,state,formula);
            addLabel(state,formula,true);
            return new CheckLTLUResult(CheckLTLUResult.SearchContinuation.ABORT,state);
        }
        if(!satisfies(state,op1)){//如果第一个命题不为真，state不满足U规约，继续查找
            logger.debug("LTLOperatorU:{} does not satisfy {}.So {} does not satisfy {} either.",state,op1,state,formula);
            addLabel(state,formula,false);
            return new CheckLTLUResult(CheckLTLUResult.SearchContinuation.CONTINUE,state);
        }
        logger.debug("LTLOperatorU:Initialize state label with true");
        addLabel(state,formula,true);
        dfsData.visit(state);
        logger.debug("LTLOperatorU:Visiting {}.dfs={};lowlink={},maxDfs={};stack={}",state,dfsData.getDfs(state),
                dfsData.getDfs(state),dfsData.getMaxDfs(),dfsData.getStackAsString());
        logger.debug("LTLOperatorU:Starting to check to check all successors {}.",state);
        if(kripkeStructure.getAllSuccessorStates(state).iterator().hasNext()){
            State successorState= kripkeStructure.getAllSuccessorStates(state).iterator().next();
            logger.debug("LTLOperatorU:Starting to check {} as successor of {}.",successorState,state);
            if(!dfsData.isVisited(successorState)){
                logger.debug("LTLOperatorU:{} was never visited.Starting checkEU({},{}).",successorState,successorState,formula);
                CheckLTLUResult checkLTLUResult = checkLTLOperatorU(successorState,formula,dfsData);
                if(checkLTLUResult.getSearchContinuation() == CheckLTLUResult.SearchContinuation.ABORT){
                    checkLTLUResult.prependWitnessPathWith(state);
                    return checkLTLUResult;
                }
                dfsData.setLowLink(state,Math.min(dfsData.getLowLink(state),dfsData.getLowLink(successorState)));
            }else{
                logger.debug("LTLOperatorU:{} has already been visited.",successorState);
                if(dfsData.isOnstack(successorState)){
                    dfsData.setLowLink(state,Math.min(dfsData.getLowLink(state),dfsData.getDfs(successorState)));
                }
            }
        }

        if(dfsData.dfsEqualsLowLink(state)){
            logger.debug("LTLOperatorU: Found {} to be root of strongly connected component. But no path from any successor of" +
                    "{} is witness for {}. So no path from {} can be witness either. Labelling all states on stack {}" +
                    " until {} with false.",state,state,formula,state,dfsData.getStackAsString(),state);
            State stateFromStack;
            do{
                stateFromStack = dfsData.popFromStack();
                addLabel(stateFromStack,formula,false);
            }while(!state.equals(stateFromStack));
        }
        return new CheckLTLUResult(CheckLTLUResult.SearchContinuation.CONTINUE,null);
    }
}
