package org.bigraph.bigsim.modelchecker;

import org.bigraph.bigsim.ctlspec.Formula;
import org.bigraph.bigsim.ctlspec.atom.Atom;
import org.bigraph.bigsim.ctlspec.atom.True;
import org.bigraph.bigsim.ctlspec.atom.False;
import org.bigraph.bigsim.ctlspec.operator.*;
import org.bigraph.bigsim.transitionsystem.KripkeStructure;
import org.bigraph.bigsim.transitionsystem.State;
import org.bigraph.bigsim.ctlimpl.CheckAUResult;
import org.bigraph.bigsim.ctlimpl.CheckEUResult;
import org.bigraph.bigsim.ctlimpl.TarjansDepthFirstSearchData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
public class CTLModelChecker {
    public enum SearchContinuation{
        CONTINUE,
        ABORT;
    }
    private static final Logger logger = LoggerFactory.getLogger(CTLModelChecker.class);
    private final KripkeStructure kripkeStructure;
    private Map<State, Map<Formula,Boolean>> labels = new HashMap<>();
    public ArrayList<State> recordPath;  // add
    public CTLModelChecker(KripkeStructure kripkeStructure){
        this.kripkeStructure = kripkeStructure;
    }
    public static boolean satisfies(KripkeStructure kripkeStructure,Formula formula){
        return new CTLModelChecker(kripkeStructure).satisfies(formula);
    }
    public boolean satisfies(Formula formula){
        kripkeStructure.validate();//先判断kripke结构是否为空
        logger.info("hello");
        logger.debug("Starting to check whether the given Kripke structure satisifies {}.",formula);
//        System.out.println(formula.toString());
        Formula formulaBase = formula.convertToCTLBase();//通过调用这个方法使用重言式把CTL formula转换为基本的CTL formula，基础的CTL公式就是那8种
        logger.debug("Converted given formula to formula in out CTL base using tautologies:{}.",formulaBase);
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
        if(formula instanceof AX){//AX表示当前状态state的所有路径下的下一个状态都要满足规约
            Formula subFormula = ((AX) formula).getOperand();
            for(State successorState:kripkeStructure.getAllSuccessorStates(state)){
                if(!satisfies(successorState,subFormula)){//如果有一个路径的下一个状态不满足规约就返回false
                    return addLabel(state,formula,false);
                }
            }
            return addLabel(state,formula,true);//所有路径的下一个状态都满足规约返回true
        }
        if(formula instanceof EX){//EX表示当前状态state的存在某条路径下的下一个状态需要满足规约
            Formula subFormula = ((EX) formula).getOperand();
            for(State successorState:kripkeStructure.getAllSuccessorStates(state)){
                if(satisfies(successorState,subFormula)){
                    return addLabel(state,formula,true);//如果存在一条路径的下一状态满足规约就返回true
                }
            }
            return addLabel(state,formula,false);//如果所有路径的下一个状态都不满足规约就返回false
        }
        if(formula instanceof EU){
            CheckEUResult checkEUResult = checkEU(state,(EU)formula);
            boolean isFormulaSatisfied = getLabel(state,formula).get();
            if(isFormulaSatisfied){
                this.recordPath = checkEUResult.getWitnessPath();
                logger.debug("EU:Found witness path for {} starting from {}:{}.",formula,state,this.recordPath);
                //logger.debug("EU:Found witness path for {} starting from {}:{}.",formula,state,checkEUResult.getWitnessPath());
            }else{
                logger.debug("EU:Found no witness path for {} starting from {}.",formula,state);
            }
            return isFormulaSatisfied;
        }
        if(formula instanceof AU){
            CheckAUResult checkAUResult = checkAU(state,(AU) formula);
            boolean isFormulaSatisfied = getLabel(state,formula).get();
            if(!isFormulaSatisfied){
//                String tmp_path = "N_646e27->r_bank_out_counter->N_98135c->r_bank_in_saferoom->N_bfbadf->r_bank_open_safe->N_a81db7->r_bank_close_safe->N_bfbadf->r_bank_out_saferoom->N_98135c";
//                System.out.println("AU:Found counter example for "+formula+" starting from N_646e27: "+ tmp_path );
//                logger.debug("AU:Found counter example for {} starting from {}:{}.",formula,"N_646e27",tmp_path);
                this.recordPath = checkAUResult.getCounterExample();
                logger.debug("AU:Found counter example for {} starting from {}:{}.", formula, state, this.recordPath);
                //logger.debug("AU:Found counter example for {} starting from {}:{}.",formula,state,checkAUResult.getCounterExample());
            }else{
                logger.debug("AU:Found no counter example for {} starting from {}.",formula,state);
            }
            return isFormulaSatisfied;
        }
        throw new IllegalArgumentException(formula.toString());
    }
    private CheckEUResult checkEU(State state,EU formula){
        TarjansDepthFirstSearchData dfsData = new TarjansDepthFirstSearchData();
        return checkEU(state,formula,dfsData);
    }
//    private CheckEUResult checkEU1(State state,EU formula,TarjansDepthFirstSearchData dfsData){
//        Formula op1 = formula.getOperand1();
//        Formula op2 = formula.getOperand2();
//        Optional<Boolean> label = getLabel(state,formula);
//        if(label.isPresent()){
//            if(label.get()){
//                return new CheckEUResult(CheckEUResult.SearchContinuation.ABORT,state);
//            }else {
//                return new CheckEUResult(CheckEUResult.SearchContinuation.CONTINUE,state);
//            }
//        }
//        if(satisfies(state,op2)){
//            addLabel(state,formula,true);
//            return new CheckEUResult(CheckEUResult.SearchContinuation.ABORT,state);
//        }
//        if(!satisfies(state,op1)){
//            addLabel(state,formula,false);
//            return new CheckEUResult(CheckEUResult.SearchContinuation.CONTINUE,state);
//        }
//        addLabel(state,formula,true);
//        dfsData.visit(state);
//        for(State prefixState: kripkeStructure.getPrefixStates(state)){
//            if(!dfsData.isVisited(prefixState)){
//                CheckEUResult checkEUResult = checkEU(prefixState,formula,dfsData);
//                l = label.get();
//                if (!labels.containsKey(EU(op1,op2)) & l.contains(op1)){
//                    labels.put(prefixState,label(prefixState.satisfies())+EU(op1,op2));
//                    labels.remove(prefixState);
//                }
//                if(checkEUResult.getSearchContinuation() == CheckEUResult.SearchContinuation.ABORT){
//                    checkEUResult.prependWitnessPathWith(state);
//                    return checkEUResult;
//                }
//                dfsData.setLowLink(state,Math.min(dfsData.getLowLink(state),dfsData.getLowLink(prefixState)));
//            }else{
//                if(dfsData.isOnstack(prefixState)){
//                    dfsData.setLowLink(state,Math.min(dfsData.getLowLink(state),dfsData.getDfs(prefixState)));
//                }
//            }
//        }
//        if(labels.isEmpty()){
//            return true;
//        }
//        if(dfsData.dfsEqualsLowLink(state)){
//            State stateFromStack;
//            do{
//                stateFromStack = dfsData.popFromStack();
//                addLabel(stateFromStack,formula,false);
//            }while(!state.equals(stateFromStack));
//        }
//        return new CheckEUResult(CheckEUResult.SearchContinuation.CONTINUE,null);
//    }

    private CheckEUResult checkEU(State state,EU formula,TarjansDepthFirstSearchData dfsData){
        Formula op1 = formula.getOperand1();
        Formula op2 = formula.getOperand2();
        Optional<Boolean> label = getLabel(state,formula);
        if(label.isPresent()){//isPresent()方法判断类对象是否存在
            if(label.get()){//如果存在，通过get方法返回对象
                return new CheckEUResult(CheckEUResult.SearchContinuation.ABORT,state);//停止查找
            }else {
                return new CheckEUResult(CheckEUResult.SearchContinuation.CONTINUE,state);//继续查找
            }
        }
        if(satisfies(state,op2)){//如果第二个命题为真，state满足EU规约，停止查找
            logger.debug("EU:{} satisifies {}. So {} also satisifies{}.",state,op2,state,formula);
            addLabel(state,formula,true);
            return new CheckEUResult(CheckEUResult.SearchContinuation.ABORT,state);
        }
        if(!satisfies(state,op1)){//如果第一个命题不为真，state不满足EU规约，继续查找
            logger.debug("EU:{} does not satisfy {}.So {} does not satisfy {} either.",state,op1,state,formula);
            addLabel(state,formula,false);
            return new CheckEUResult(CheckEUResult.SearchContinuation.CONTINUE,state);
        }
        logger.debug("EU:Initialize state label with true");
        addLabel(state,formula,true);
        dfsData.visit(state);
        logger.debug("Eu:Visiting {}.dfs={};lowlink={},maxDfs={};stack={}",state,dfsData.getDfs(state),
                dfsData.getDfs(state),dfsData.getMaxDfs(),dfsData.getStackAsString());
        logger.debug("EU:Starting to check to check all successors {}.",state);
        for(State successorState: kripkeStructure.getAllSuccessorStates(state)){
            logger.debug("EU:Starting to check {} as successor of {}.",successorState,state);
            if(!dfsData.isVisited(successorState)){
                logger.debug("EU:{} was never visited.Starting checkEU({},{}).",successorState,successorState,formula);
                CheckEUResult checkEUResult = checkEU(successorState,formula,dfsData);
                if(checkEUResult.getSearchContinuation() == CheckEUResult.SearchContinuation.ABORT){
                    checkEUResult.prependWitnessPathWith(state);
                    return checkEUResult;
                }
                dfsData.setLowLink(state,Math.min(dfsData.getLowLink(state),dfsData.getLowLink(successorState)));
            }else{
                logger.debug("EU:{} has already been visited.",successorState);
                if(dfsData.isOnstack(successorState)){
                    dfsData.setLowLink(state,Math.min(dfsData.getLowLink(state),dfsData.getDfs(successorState)));
                }
            }
        }
        if(dfsData.dfsEqualsLowLink(state)){
            logger.debug("EU: Found {} to be root of strongly connected component. But no path from any successor of" +
                    "{} is witness for {}. So no path from {} can be witness either. Labelling all states on stack {}" +
                    " until {} with false.",state,state,formula,state,dfsData.getStackAsString(),state);
            State stateFromStack;
            do{
                stateFromStack = dfsData.popFromStack();
                addLabel(stateFromStack,formula,false);
            }while(!state.equals(stateFromStack));
        }
        return new CheckEUResult(CheckEUResult.SearchContinuation.CONTINUE,null);
    }
    private CheckAUResult checkAU(State state,AU formula){
        TarjansDepthFirstSearchData dfsData = new TarjansDepthFirstSearchData();
        return checkAU(state,formula,dfsData);
    }
    private CheckAUResult checkAU(State state,AU formula ,TarjansDepthFirstSearchData dfsData){
        Formula op1 = formula.getOperand1();
        Formula op2 = formula.getOperand2();
        dfsData.visit(state);
        logger.debug("AU:Visiting{}. dfs={};lowlink={},maxDfs={};stack={}",state,dfsData.getDfs(state),
                dfsData.getDfs(state),dfsData.getMaxDfs(),dfsData.getStackAsString());
        Optional<Boolean> label = getLabel(state,formula);
        if(label.isPresent()){
            if(label.get()){
                return new CheckAUResult(CheckAUResult.SearchContinuation.CONTINUE,null);
            }else {
                return new CheckAUResult(CheckAUResult.SearchContinuation.ABORT,state);
            }
        }
        if(satisfies(state,op2)){//如果第二个命题为真，当前状态满足op2，所以满足AU规约，继续查找
            logger.debug("AU:{} satisfies {}. So {} also satisfies {}.",state,op2,state,formula);
            addLabel(state,formula,true);
            return new CheckAUResult(CheckAUResult.SearchContinuation.CONTINUE,null);
        }
        if(!satisfies(state,op1)){//如果第一个命题不为真，当前状态不满足op1，所以不满足AU规约，停止查找
            logger.debug("AU:{} does not satisfy {}.So {} does not satisfy {} either.",state,op1,state,formula);
            addLabel(state,formula,false);
            return new CheckAUResult(CheckAUResult.SearchContinuation.ABORT,state);
        }
        logger.debug("AU:Initialize state label with false");           // [kgq] 这里为什么要初始化，初始化为什么 是false，checkEU中初始化是true，为什么不一样。 -- 是因为，AU提前返回的时候，找到的是一条反例路径，label应该为false，而EU提前返回的时候，是找到一条示例路径，label应该为true。
        addLabel(state,formula,false);
        for(State successorState:kripkeStructure.getAllSuccessorStates(state)){
            logger.debug("AU:Starting to check {} as successor of {}.",successorState,state);
            if(!dfsData.isVisited(successorState)){
                logger.debug("AU:{} was never visited. Starting checkAU({},{}.",successorState,successorState,formula);
                CheckAUResult checkAUResult = checkAU(successorState,formula,dfsData);
                if(checkAUResult.getSearchContinuation() == CheckAUResult.SearchContinuation.ABORT){
                    checkAUResult.prependCountExampleWith(state);
                    return checkAUResult;
                }
            }else{
                logger.debug("AU:{} has already been visited.",successorState);
                if(dfsData.isOnstack(successorState)){
                    logger.debug("AU:{} is on Tarjan's dfs stack.Aborting depth first search.",successorState);
                    return new CheckAUResult(CheckAUResult.SearchContinuation.ABORT,successorState);                // 这里返回，应该算是查找失败了
                }
            }
        }
        dfsData.removeFromStack(state);
        addLabel(state,formula,true);
        return new CheckAUResult(CheckAUResult.SearchContinuation.CONTINUE,null);
    }
}
