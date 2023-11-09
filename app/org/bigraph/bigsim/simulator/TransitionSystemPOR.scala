package org.bigraph.bigsim.simulator

import org.bigraph.bigsim.BRS.{Graph, Match, Matcher, Vertex}
import org.bigraph.bigsim.model.{Bigraph, BindingChecker, Nil, Paraller, ReactionRule, TermType}
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import org.bigraph.bigsim.parser.{BGMParser, BGMTerm, TermParser}
import org.bigraph.bigsim.transitionsystem.{KripkeStructure, State}
import java.util.concurrent.ConcurrentHashMap

import org.bigraph.bigsim.data.DataModel
import org.bigraph.bigsim.utils.GlobalCfg
import org.bigraph.bigsim.value.Value
import scala.util.control.Breaks._
import scala.collection.mutable.{Map, Queue, Set}

// add by 19 新修改的 TransitionSystem 以 Vertex为最小单位进行反应
class TransitionSystemPOR(b: Bigraph) {

  def logger: Logger = LoggerFactory.getLogger(this.getClass)

  var v: Vertex = new Vertex(b, null, rr = null);
  var g: Graph = new Graph(v);
  var workQueue: Queue[Vertex] = Queue();
  workQueue.enqueue(v); // 初始节点进队列
  val initAgent: Bigraph = v.bigraph
  val propRR: mutable.Map[String, ReactionRule] = mutable.Map()
  val propStr: mutable.Map[String, Tuple2[String, String]] = initAgent.prop
  var haveBuiltPropMap: Boolean = false

  var rulesClusters: mutable.Set[mutable.Set[ReactionRule]] = mutable.Set[mutable.Set[ReactionRule]]()
  var rulesVisited:Map[ReactionRule, Boolean]=Map();
  var rulesVisitedInited=false

  var reachedAgent: mutable.Map[Long, Boolean] = Map(); // 记录已经产生的agent
  var isOnTheFly: Boolean = false

  var bigraphList: ListBuffer[Bigraph] = ListBuffer();
  var matchers: Map[Bigraph, List[(Bigraph, String)]] = Map();

  var simulated: Boolean = false

  var DEBUG: Boolean = false

  def simulate(): Unit = {
    if (b == null || b.root == null) {
      println("transitionsystem::simulate() null");
    } else {
      while (step()) {};
    }
    simulated = true
  }

  def step(): Boolean = {
    if (workQueue.isEmpty) {
      println("transitionsystem::step Complete!");
      return false;
    }
    var v: Vertex = workQueue.dequeue();
    if (reachedAgent.contains(v.hash)) {
      return true
    }
    var b: Bigraph = v.bigraph;
    var matches: mutable.Set[Match] = findAmpleSet(b)

    if (DEBUG) {
      println("Transitionsystem::Start:: -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=, Cur bigraph: " + b.root.hashCode())
    }
    if (matches.isEmpty) {
      if (DEBUG) {
        println("\t\t匹配结果为空")
      }
      v.terminal = true; // last agent
    }
    reachedAgent(v.hash) = true;

    //    var hasdp =false
    //    var haswd = false
    //    matches.foreach(x => {
    //      if (x.rule.name == "r_DP-1Prepare")
    //        hasdp = true
    //      else if (x.rule.name == "r_WD-1Prepare")
    //        haswd = true
    //    })
    //    if (hasdp != haswd){
    //      println("**************** WTF???**************")
    //      println("cur bigraph is : " + b.root)
    //      println("***************88ganlinnian***********")
    //    }
    // 用来检查C3，如果找到回路，重新计算，不再使用ampleMatches，而是重新计算所有的Matches并计算新的偶图
    var vertexList=Set[Vertex]()
    if(!computeNewBigraph(b,v,matches,true,vertexList)){
      matches=b.findMatches
      vertexList=Set[Vertex]()
      computeNewBigraph(b,v,matches,false,vertexList)
    }

    for(v<-vertexList){
      workQueue.enqueue(v);
      g.add(v);
    }
    if (!checkProperties(v)) {
      println("")
      return false
    }
    println("")
    true
  }

  def computeNewBigraph(b:Bigraph,v:Vertex,matches:Set[Match], needCheck:Boolean,vertexList:Set[Vertex]):Boolean={
    for (it <- matches) {
      var rr: ReactionRule = it.rule;
      val value = new Value(rr)
      val condCheckRes = value.checkConditions(it)
      if (DEBUG) {
        println("\t\tCondCheckRes is: " + condCheckRes + " match rule: " + rr.name + " Conditions is: " + rr.conditions)
      }

      if (condCheckRes) { // add by kgq 20220304 增加条件检查
        value.calAssignments(it) // add by kgq 20220304 增加数值计算
        if (DEBUG) {
          println("\t\t\t\told bigraph via rule: " + rr.name + " bigraph is: " + b.root)
        }
        var nb: Bigraph = b.applyMatch(it);
        if (DEBUG) {
          println("\t\t\t\tnew Bigraph is: " + nb.root.hashCode())
          println("\t\t\t\tnew bigraph via rule: " + rr.name + " bigraph is: " + nb.root)
        }
        var bindingChecker = new BindingChecker();
        if (GlobalCfg.checkBinding && !bindingChecker.bindcheck(nb.root)) { // 如果需要 检查binding限制, 并且检测结果是错误的
          logger.debug("[kgq]new bigraph not satisify the binding constraint: " + nb.toString())
        } else { // 不需要检测binding限制，或者是检测结果是符合的
          if (nb.root == null) nb.root = new Nil();
          var nv: Vertex = new Vertex(nb, v, rr);
          if (g.lut.contains(nv.hash)) {
            if (DEBUG) {
              println("\t\t\t\t\t偶图重复，丢弃")
            }
            nv = g.lut(nv.hash);
            nv.addParents(v)
            if(needCheck)
              return false
            // 如果偶图重复，将ample集合扩大为所有matches
          } else {
            // println("\t\t !!! 未考察节点，保留，入队列")
            println("\t\t\t\t\t新偶图，入队列")
            vertexList.add(nv)
          }
          v.addTarget(nv, rr);
        }
      }
    }
    matches.clear();
    true
  }
  def checkRulesIsMatch(r1: ReactionRule, r2: ReactionRule): Boolean = {
    if (!r1.redex.isInstanceOf[Paraller]) return false
    if (!r2.redex.isInstanceOf[Paraller]) return false

    for (child1 <- r1.redex.asInstanceOf[Paraller].getChildren) {
      for (child2 <- r2.redex.asInstanceOf[Paraller].getChildren) {
        //ToDo：equalMatch

      }
    }
    return false
  }

  def checkRulesIsInclude(r1: ReactionRule, r2: ReactionRule): Boolean = {
    false
  }

  def isDependent(r1: ReactionRule, r2: ReactionRule): Boolean = {
    false
  }

  def getRulesClusters(): mutable.Set[mutable.Set[ReactionRule]] = {
    if (rulesClusters.nonEmpty) return rulesClusters
    for (rule <- v.bigraph.rules) {
      if (rulesClusters.isEmpty) {
        rulesClusters += mutable.Set[ReactionRule]()
      }
      var added: Boolean = false
      for (cluster <- rulesClusters) {
        if (cluster.isEmpty) {
          cluster += rule
          added = true
        } else {
          for (r <- cluster) {
            if (isDependent(r, rule)) {
              cluster += rule
              added = true
            }
          }
        }
      }
      if (!added) {
        var mSet = mutable.Set[ReactionRule]()
        mSet += rule
        rulesClusters += mSet
      }
    }
    rulesClusters
  }

  def findMatch(b: Bigraph, r: ReactionRule): Set[Match] = {
    var res: Set[Match] = Set();
    var relations = true
    if (r.data != null && GlobalCfg.checkData) {
      r.data.foreach(relation => {
        relations = DataModel.relationDecision(relation)
      })
    }
    if (relations) {
      var mp: Set[Match] = Matcher.tryMatchTermReactionRule(b.root, r);

      mp.foreach(m => {
        if (r.check(m)) {
          res.add(m);
        }
      })
    }
    return res
  }

  def checkC1(b:Bigraph,clusterMatch: Set[Match],curCluster:mutable.Set[ReactionRule]): Boolean = {
//    for(cluster<-rulesClusters){
//      if(cluster!=curCluster){
//        for(it<-cluster){
//          var ruleMatch = findMatch(b, it)
//          ruleMatch=conditionFilte(ruleMatch)
//          if(ruleMatch.nonEmpty)
//            return false
//        }
//      }
//    }
    true
  }

  def checkC2(b:Bigraph,clusterMatch: Set[Match]): Boolean = {
    for(it<-clusterMatch){
      if(!checkInvisible(b,it))
        return false
    }
    return true
  }

  def checkC4(m: Match,rr:ReactionRule): Boolean = {
    val value = new Value(rr)
    val condCheckRes = value.checkConditions(m)
    return condCheckRes
  }

  def conditionFilte(ruleMatch:Set[Match]):Set[Match]={
    var res=Set[Match]()
    for(it<-ruleMatch){
      var rr: ReactionRule = it.rule;
      val value = new Value(rr)
      val condCheckRes = value.checkConditions(it)
      if(condCheckRes)
        res.add(it)
    }
    res
  }

  def checkRedexIsAnonymous(r:ReactionRule): Boolean ={
      return r.redex.checkIsAnonymous()&&r.reactum.checkIsAnonymous()
  }

  def findAmpleSet(b: Bigraph): Set[Match] = {
    var fullExpand = true
    var ampleMatch = Set[Match]()

    for(rule<-v.bigraph.rules){
        var ruleMatch =findMatch(b, rule)
        ruleMatch=conditionFilte(ruleMatch)
        if(ruleMatch.size>1&&checkRedexIsAnonymous(rule)){
          var rm=Set[Match]()
          rm.add(ruleMatch.head)
          ruleMatch=rm
        }
        if(ruleMatch.size==1&&checkC2(b,ruleMatch)) {
          if(ampleMatch.isEmpty)
            ampleMatch=Match.merge(ampleMatch,ruleMatch)
          else{
            val value = new Value(rule)
            value.calAssignments(ampleMatch.head)
            var nb: Bigraph = b.applyMatch(ampleMatch.head);
            var ruleMatch2=findMatch(nb,rule)
            if(!equlaMatch(ruleMatch,ruleMatch2))
              ampleMatch=Match.merge(ampleMatch,ruleMatch)
          }
        }
    }

    if(ampleMatch.nonEmpty)return ampleMatch
    ampleMatch = b.findMatches
    return trimAmpleMatch(ampleMatch)
  }

  def equlaMatch(ruleMatches:Set[Match],ruleMatches2:Set[Match]):Boolean={
    for(m1<-ruleMatches){
      for(m2<-ruleMatches2){
        if(m1.rule.name==m2.rule.name)
          return true
      }
    }
    return false
  }
  def findAmpleSetV2(b: Bigraph): Set[Match] = {
    var fullExpand = true
    var ampleMatch = Set[Match]()
    var clusters = getRulesClusters()
    for (cluster <- clusters) {
      var clusterMatch = Set[Match]()
      for (rule <- cluster) {
        var ruleMatch = findMatch(b, rule)
        if(ruleMatch.size>1&&checkRedexIsAnonymous(rule)){
          var rm=Set[Match]()
          rm.add(ruleMatch.head)
          ruleMatch=rm
        }
        ruleMatch=conditionFilte(ruleMatch)
        clusterMatch=Match.merge(clusterMatch,ruleMatch)
      }
      if (clusterMatch.nonEmpty && checkC1(b,clusterMatch,cluster) && checkC2(b,clusterMatch)) {
        fullExpand = false
        ampleMatch = clusterMatch
      }
    }
    if (fullExpand)
      ampleMatch = b.findMatches
    return trimAmpleMatch(ampleMatch)
  }

  def trimAmpleMatch(matches: mutable.Set[Match]): Set[Match] = {
    return matches
  }

  def buildPropMap = {
    if (!haveBuiltPropMap) {
      for ((k, v) <- propStr) { // k是 prop的名字，v是prop的项语言描述
        val propRedex = TermParser.apply(v._1) // v._1 是原子命题描述
        // println("---------- build prop React----------------")
        val propReact = TermParser.apply("nil")
        val exp = v._2
        var propExp: String = ""
        if (exp.startsWith(GlobalCfg.ctlPropPreStr)) {
          propExp += GlobalCfg.conditionPrefStr + exp.substring(GlobalCfg.ctlPropPreStr.length)
        }
        val rr = new ReactionRule(k, propRedex, propReact, propExp) // 使用一个原子命题作为反应物构造一个反应规则
        propRR += (k -> rr) // 把新构造的反应规则放到propRR中保存起来
      }
    }
    haveBuiltPropMap = true
  }

  def checkInvisible(b: Bigraph,m:Match): Boolean = {
    buildPropMap
    var rr: ReactionRule = m.rule;
    val value = new Value(rr)
    value.calAssignments(m)
    if (DEBUG) {
      println("\t\t\t\told bigraph via rule: " + rr.name + " bigraph is: " + b.root)
    }
    var nb: Bigraph = b.applyMatch(m);

    var bSatisfiedAtoms = checkAtom(b)
    var nvSatisfiedAtoms = checkAtom(nb)
    for (atom <- bSatisfiedAtoms) {
      if (!nvSatisfiedAtoms.contains(atom))
        return false
    }
    return true
  }

  def checkAtom(b: Bigraph): mutable.Set[String] = { // 输入的是一个偶图，检测它所满足的原子命题
    val retList: mutable.Set[String] = mutable.Set[String]()
    if (DEBUG)
      print("checkAtom.check: " + b.root)
    for ((name, rr) <- propRR) { // 对于每一个反应规则
      val mp: mutable.Set[Match] = Matcher.tryMatchTermReactionRule(b.root, rr) // 每一条反应规则（原子命题） 都尝试和b进行反应
      if (mp.nonEmpty) { //  如果返回的结果不空，那么说明反应规则能够匹配的上，也就是说 反应物的原子命题是可以被b满足的
        if (DEBUG)
          print("\t mp.nonEmpty")
        val value = new Value(rr) // add by kgq 20220304 新增数值计算，来检测数值规约信息
        var condCheck: Boolean = false
        for (m <- mp if !condCheck) { // 如果匹配上多个，那么开始依次检查匹配，只要满足一个，就表示满足原子命题
          val tmpRes = value.checkConditions(m)
          val valueCheckRes = value.checkValueConditions(m)
          condCheck = condCheck || (tmpRes && valueCheckRes)
          if (DEBUG)
            print("\t condRes: " + tmpRes)
        }
        if (DEBUG)
          println("\t condCheck is: " + condCheck)
        if (condCheck)
          retList += (name)
      }
    }
    if (DEBUG)
      println("!*!*!*!* 对于偶图b：" + b.root + "\n" + "*!*!*!*! 所满足的原子命题为：" + retList)
    retList
  }

  def checkProperties(v: Vertex): Boolean = {
    if (v.visited) return true;
    v.visited = true;
    true;
  }

  def getAllBigraphs: List[Bigraph] = {
    if (!simulated) simulate();
    g.lut.values.map(x => { //   遍历g的查找表中的每一个节点，把其中的bigraph取出来。
      val b: Bigraph = x.bigraph
      this.bigraphList += b;
    })
    this.bigraphList.toList
  }

  def getMatchers: Map[Bigraph, List[(Bigraph, String)]] = {
    if (!simulated) simulate(); // 如果还没有模拟过，那么先调用simulate方法进行模拟
    g.lut.values.map(x => { // 遍历g的查找表中的每一个节点
      val cb = x.bigraph
      if (x.target != null && x.target.size > 0) {
        var tmpBuffer: ListBuffer[(Bigraph, String)] = ListBuffer();
        x.target.map(y => {
          val nb = y._1.bigraph
          val nr = y._2.toString
          tmpBuffer += ((nb, nr));
        })
        this.matchers += (cb -> tmpBuffer.toList)
      }
    })
    this.matchers
  }
}

// 从 TransitionSystem 构建一个 KripkeStructure
class BuildKripkeStructurePOR(trans: TransitionSystemPOR) {

  var bigToState: Map[Int, State] = Map()

  val kripkeStruct: KripkeStructure = new KripkeStructure()
  var build: Boolean = false
  var propRR: mutable.Map[String, ReactionRule] = mutable.Map()
  val propStr: mutable.Map[String, Tuple2[String, String]] = trans.initAgent.prop // 获取所有原子命题的名字, 以及表达式
  val transitionMap: Map[Bigraph, List[(Bigraph, String)]] = trans.getMatchers

  val bigraphList: List[Bigraph] = trans.getAllBigraphs
  // println("初始化的时候，这里的大小是", bigraphList.size)
  var DEBUG: Boolean = false

  def buildKripke: KripkeStructure = {
    if (this.build)
      return this.kripkeStruct
    buildPropMap // 先构造原子命题映射： String -> ReactionRule
    buildInitialState
    buildState
    buildTransition
    println("*************创建的kripke为：" + this.kripkeStruct)
    this.build = true
    this.kripkeStruct
  }

  def buildPropMap = {
    trans.buildPropMap
    propRR = trans.propRR
  }

  def buildInitialState = {
    // println("==============build initialState========")
    val b: Bigraph = trans.initAgent
    val agentHash: Int = {
      if (b.root != null)
        b.root.toString.hashCode;
      else "".hashCode;
    }
    val stateName = formatHash(agentHash)
    val stateAtom = checkAtom(b)
    val initialState = new State(stateName, stateAtom: _*) // 这里创建一个state，因为构造方式是多参数的，所以要把stateAtom给展开
    kripkeStruct.addInitialState(initialState) // 把初始状态放入到kripke结构中去
    bigToState += (agentHash -> initialState) // 把偶图到状态的映射关系也存起来
    // println(bigToState)
  }

  def buildState = {
    // println("===================build states===========")
    for (b <- this.bigraphList) { // 遍历所有偶图
      //println("cur process: " + b.root)
      val b_hash: Int = {
        if (b.root != null)
          b.root.toString.hashCode;
        else "".hashCode;
      }
      //println("\t cur bigraph.hash: " + b_hash)
      if (bigToState.contains(b_hash)) {
        //         println("\t\t bigToState already record!, bigToState is: " + bigToState)
        //          for(b <- bigraphList) {
        //            println(b.root)
        //       }
      }
      else {
        val stateName = formatHash(b_hash)
        val stateAtom = checkAtom(b)
        //println("\t\t stateName is: " + stateName + " stateAtom is: " + stateAtom)
        val curState = new State(stateName, stateAtom: _*)
        if (this.transitionMap.contains(b))
          kripkeStruct.addState(curState)
        else
          kripkeStruct.addFinalState(curState)
        bigToState += (b_hash -> curState) //把偶图到状态的映射关系也存起来
      }
    }
    println(bigToState)
  }

  def checkAtom(b: Bigraph): List[String] = { // 输入的是一个偶图，检测它所满足的原子命题
    val retList: ListBuffer[String] = ListBuffer()
    if (DEBUG)
      print("checkAtom.check: " + b.root)
    for ((name, rr) <- propRR) { // 对于每一个反应规则
      val mp: mutable.Set[Match] = Matcher.tryMatchTermReactionRule(b.root, rr) // 每一条反应规则（原子命题） 都尝试和b进行反应
      if (mp.nonEmpty) { //  如果返回的结果不空，那么说明反应规则能够匹配的上，也就是说 反应物的原子命题是可以被b满足的
        if (DEBUG)
          print("\t mp.nonEmpty")
        val value = new Value(rr) // add by kgq 20220304 新增数值计算，来检测数值规约信息
        var condCheck: Boolean = false
        for (m <- mp if !condCheck) { // 如果匹配上多个，那么开始依次检查匹配，只要满足一个，就表示满足原子命题
          val tmpRes = value.checkConditions(m)
          val valueCheckRes = value.checkValueConditions(m)
          condCheck = condCheck || (tmpRes && valueCheckRes)
          if (DEBUG)
            print("\t condRes: " + tmpRes)
        }
        if (DEBUG)
          println("\t condCheck is: " + condCheck)
        if (condCheck)
          retList.append(name)
      }
    }
    if (DEBUG)
      println("!*!*!*!* 对于偶图b：" + b.root + "\n" + "*!*!*!*! 所满足的原子命题为：" + retList)
    retList.toList
  }

  def buildTransition = {
    // println("========== build transition ===============")
    val matchMap = transitionMap
    for (b <- matchMap.keys) { // 在偶图 状态 映射中遍历 key
      // println("注意这里！！！！！！！！！！！")
      val tmpList = matchMap(b)
      // println("这里也查看一下==============")
      val b_hash: Int = {
        if (b.root != null) b.root.toString.hashCode; else "".hashCode;
      }
      if (!bigToState.contains(b_hash)) {
        println("那就因该是没有保存下来了")
        //        println(bigToState)
        //        println(b_hash)
        //        println(matchMap.keys)
      }
      val fromState = bigToState(b_hash)
      // println("fromState: ", fromState)
      for ((toB, rr) <- tmpList) { //   遍历下一个状态
        val toB_hash: Int = {
          if (toB.root != null) toB.root.toString.hashCode; else "".hashCode;
        }
        val toState = bigToState(toB_hash)
        // println("add toState: ", toState)
        kripkeStruct.addTransition(fromState, toState)
      }
    }
  }

  // 下面的这两个字段，以及两个方法，主要是用来从Bigraph创建State的时候，对状态进行命名。方法来源于 org.bigraph.bigsim.BRS.Graph
  var concurrent = new ConcurrentHashMap[String, String]()
  var charIndex = 0

  def formatHash(hash: Int): String = {
    //    var str = "";
    //    if (hash < 0) str = "" + hash.abs;
    //    else str = hash.toString;
    //    if(!concurrent.containsKey(str)){
    //      concurrent.put(str, getStrValue(charIndex))
    //      charIndex = charIndex + 1
    //    }
    //    concurrent.get(str)
    hash.toString
  }

  def getStrValue(index: Int): String = {
    val k = index / 26
    var str = ""
    if (k > 0) {
      str += ((k % 26) - 1 + 'A').toChar + "_"
    }
    str += ((index % 26) + 'A').toChar
    str
  }
}