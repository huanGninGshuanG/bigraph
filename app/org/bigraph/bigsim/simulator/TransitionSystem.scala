package org.bigraph.bigsim.simulator
import org.bigraph.bigsim.BRS.{Graph, Match, Matcher, Vertex}
import org.bigraph.bigsim.model.{Bigraph, BindingChecker, Nil, ReactionRule}
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import org.bigraph.bigsim.parser.{BGMParser, BGMTerm, TermParser}
import org.bigraph.bigsim.transitionsystem.{KripkeStructure, State}
import java.util.concurrent.ConcurrentHashMap

import org.bigraph.bigsim.utils.GlobalCfg
import org.bigraph.bigsim.value.Value

import scala.collection.mutable.{Map, Queue, Set}

// add by 19 新修改的 TransitionSystem 以 Vertex为最小单位进行反应
class TransitionSystem(b: Bigraph) {

  def logger : Logger = LoggerFactory.getLogger(this.getClass)

  var v: Vertex = new Vertex(b, null, rr = null);
  var g: Graph = new Graph(v);
  var workQueue: Queue[Vertex] = Queue();
  workQueue.enqueue(v);                   // 初始节点进队列

  var reachedAgent: Map[Long, Boolean] = Map(); // 记录已经产生的agent
  val initAgent: Bigraph = v.bigraph
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
    var matches: mutable.Set[Match] = b.findMatches;
    if (DEBUG) {
      println("Transitionsystem::Start:: -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=, Cur bigraph: " + b.root.hashCode())
    }
    if (matches.isEmpty) {
      if (DEBUG) {
        println("\t\t匹配结果为空")
      }
      v.terminal = true;     // last agent
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

    matches.foreach(it => {
      var rr: ReactionRule = it.rule;

      val value = new Value(rr)
      val condCheckRes = value.checkConditions(it)
      if (DEBUG) {
        println("\t\tCondCheckRes is: " + condCheckRes + " match rule: " + rr.name + " Conditions is: " + rr.conditions)
      }

      if (condCheckRes) {        // add by kgq 20220304 增加条件检查
        value.calAssignments(it)              // add by kgq 20220304 增加数值计算
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
          } else {
            // println("\t\t !!! 未考察节点，保留，入队列")
            println("\t\t\t\t\t新偶图，入队列")
            workQueue.enqueue(nv);
            g.add(nv);
          }
          v.addTarget(nv, rr);
        }
      }
    })
    matches.clear();
    if (!checkProperties(v)) {
      println("")
      return false
    }
    println("")
    true
  }

  def checkProperties(v: Vertex): Boolean = {
    if (v.visited) return true;
    v.visited = true;
    true;
  }
  def getAllBigraphs: List[Bigraph] = {
    if (!simulated) simulate();
    g.lut.values.map(x => {           //   遍历g的查找表中的每一个节点，把其中的bigraph取出来。
      val b: Bigraph = x.bigraph
      this.bigraphList += b;
    })
    this.bigraphList.toList
  }

  def getMatchers: Map[Bigraph, List[(Bigraph, String)]] = {
    if (!simulated) simulate();    // 如果还没有模拟过，那么先调用simulate方法进行模拟
    g.lut.values.map(x => {       // 遍历g的查找表中的每一个节点
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
class BuildKripkeStructure (trans: TransitionSystem) {

  var bigToState: Map[Int, State] = Map()

  val kripkeStruct: KripkeStructure = new KripkeStructure()
  var build: Boolean = false
  val propRR: mutable.Map[String, ReactionRule] = mutable.Map()
  val propStr: mutable.Map[String, Tuple2[String, String]] = trans.initAgent.prop   // 获取所有原子命题的名字, 以及表达式
  val transitionMap:Map[Bigraph, List[(Bigraph, String)]] = trans.getMatchers

  val bigraphList: List[Bigraph] = trans.getAllBigraphs
  // println("初始化的时候，这里的大小是", bigraphList.size)
  var DEBUG: Boolean = false

  def buildKripke: KripkeStructure = {
    if(this.build)
      return this.kripkeStruct
    buildPropMap        // 先构造原子命题映射： String -> ReactionRule
    buildInitialState
    buildState
    buildTransition
    println("*************创建的kripke为：" + this.kripkeStruct)
    this.build = true
    this.kripkeStruct
  }

  def buildPropMap = {
    // println("========== build PropMap==================")
    for((k, v) <- propStr){    // k是 prop的名字，v是prop的项语言描述
      val propRedex = TermParser.apply(v._1)    // v._1 是原子命题描述
      // println("---------- build prop React----------------")
      val propReact = TermParser.apply("nil")
      val exp = v._2
      var propExp: String = ""
      if (exp.startsWith(GlobalCfg.ctlPropPreStr)) {
        propExp += GlobalCfg.conditionPrefStr + exp.substring(GlobalCfg.ctlPropPreStr.length)
      }
      val rr = new ReactionRule(k, propRedex, propReact, propExp)     // 使用一个原子命题作为反应物构造一个反应规则
      propRR += (k -> rr)   // 把新构造的反应规则放到propRR中保存起来
    }
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
    val initialState = new State(stateName, stateAtom: _*)    // 这里创建一个state，因为构造方式是多参数的，所以要把stateAtom给展开
    kripkeStruct.addInitialState(initialState)        // 把初始状态放入到kripke结构中去
    bigToState+=(agentHash -> initialState)    // 把偶图到状态的映射关系也存起来
    // println(bigToState)
  }

  def buildState = {
    // println("===================build states===========")
    for(b <- this.bigraphList) {  // 遍历所有偶图
      //println("cur process: " + b.root)
      val b_hash: Int = {
        if (b.root != null)
          b.root.toString.hashCode;
        else "".hashCode;
      }
      //println("\t cur bigraph.hash: " + b_hash)
      if(bigToState.contains(b_hash)){
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
        if(this.transitionMap.contains(b))
          kripkeStruct.addState(curState)
        else
          kripkeStruct.addFinalState(curState)
        bigToState+=(b_hash -> curState)     //把偶图到状态的映射关系也存起来
      }
    }
    println(bigToState)
  }

  def checkAtom(b: Bigraph): List[String] = {    // 输入的是一个偶图，检测它所满足的原子命题
    val retList: ListBuffer[String] = ListBuffer()
    if (DEBUG)
      print("checkAtom.check: " + b.root)
    for((name, rr) <- propRR) {    // 对于每一个反应规则
      val mp: mutable.Set[Match] = Matcher.tryMatchTermReactionRule(b.root, rr)   // 每一条反应规则（原子命题） 都尝试和b进行反应
      if(mp.nonEmpty) {       //  如果返回的结果不空，那么说明反应规则能够匹配的上，也就是说 反应物的原子命题是可以被b满足的
        if (DEBUG)
          print("\t mp.nonEmpty")
        val value = new Value(rr)       // add by kgq 20220304 新增数值计算，来检测数值规约信息
        var condCheck: Boolean = false
        for (m <- mp if !condCheck) {      // 如果匹配上多个，那么开始依次检查匹配，只要满足一个，就表示满足原子命题
          val tmpRes = value.checkConditions(m)
          val valueCheckRes=value.checkValueConditions(m)
          condCheck = condCheck || (tmpRes&&valueCheckRes)
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
    for(b <- matchMap.keys) {    // 在偶图 状态 映射中遍历 key
      // println("注意这里！！！！！！！！！！！")
      val tmpList = matchMap(b)
      // println("这里也查看一下==============")
      val b_hash: Int = {if (b.root != null) b.root.toString.hashCode; else "".hashCode;}
      if(!bigToState.contains(b_hash)){
        println("error situation: " + b)
//        println(bigToState)
//        println(b_hash)
//        println(matchMap.keys)
      }
      val fromState = bigToState(b_hash)
      // println("fromState: ", fromState)
      for((toB, rr) <- tmpList) {   //   遍历下一个状态
        val toB_hash: Int = {if (toB.root != null) toB.root.toString.hashCode; else "".hashCode;}
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

object testTransitionSystem {
  def main(args: Array[String]): Unit = {

    val bigraphExam =
      """
        |# Controls
        |%active Greater : 2;
        |%active Less : 2;
        |%active GreaterOrEqual : 2;
        |%active LessOrEqual : 2;
        |%active Equal : 2;
        |%active NotEqual : 2;
        |%active Exist : 1;
        |%active InstanceOf : 2;
        |%active Client : 2;
        |%active Coin : 2;
        |%active Register : 1;
        |%active Data : 0;
        |%active call_foo : 0;
        |%active True : 0;
        |%active False : 0;
        |%active aNum : 1;
        |%active SmartContract : 1;
        |%active Control1 : 0;
        |
        |# Rules
        |%rule r_test3 a:Client[idle,a:edge].(c:Coin[idle,idle] | d:Coin[idle,idle] | e:Coin[idle,idle] | h:Register[idle]) | b:Client[idle,a:edge].(f:Coin[idle,idle] | g:Coin[idle,idle] | i:Register[idle]) -> a:Client[idle,a:edge].(c:Coin[idle,idle] | d:Coin[idle,idle] | h:Register[idle]) | b:Client[idle,a:edge].(f:Coin[idle,idle] | g:Coin[idle,idle] | i:Register[idle]) | e:Coin[idle,idle]{Probability:0.3};
        |
        |%rule r_test4 a:Client[idle,a:edge].(h:Register[idle] | _class_1639578794528:Coin[idle,idle] | f:Coin[idle,idle] | $0) | b:Client[idle,a:edge].(g:Coin[idle,idle] | i:Register[idle]) -> a:Client[idle,a:edge].(h:Register[idle] | _class_1639578794528:Coin[idle,idle] | f:Coin[idle,idle] | $0) | b:Client[idle,a:edge].(g:Coin[idle,idle] | i:Register[idle]){};
        |
        |# prop
        |%prop a a:Client[idle,idle].(b:Coin[idle,idle] | $0);
        |
        |# Model
        |%agent a:Client[idle,a:edge].(c:Coin[idle,idle] | d:Coin[idle,idle] | e:Coin[idle,idle] | h:Register[idle]) | b:Client[idle,a:edge].(f:Coin[idle,idle] | g:Coin[idle,idle] | i:Register[idle]);
        |#SortingLogic
        |# Go!
        |%check;
        |""".stripMargin
    val t = BGMParser.parseFromString(bigraphExam)
    val b = BGMTerm.toBigraph(t)
    val transition = new TransitionSystem(b)
    println("=-=-=-=-=-==-=-=-=-=-=-==-=-=-=-=-=-==-=-=-=- now to build KripStructure-=-=-=-=-=-=-==-=-=-=-=-=-=-=-=-")
    val buildKripke = new BuildKripkeStructure(transition)
    val kripke = buildKripke.buildKripke
    val matchRes = transition.getMatchers
    //println(matchRes)
    println("===========================================================")
    println(kripke.toString)
    println(kripke.getClass)

  }
}


object testTransitionSystemwithBind {
  def main(args: Array[String]): Unit = {

    var s =
      """
        |# Controls
        |%active Greater : 2;
        |%active Node : 2;
        |%active Container : 0;
        |%binding Bind;
        |
        |# Names
        |%outername d;

        |
        |# Rules
        |%rule r_test1 Container.(e:Container.(b:Node[idle,d:binding]) | c:Node[idle,d:binding] | d:Bind) ->
        |Container.(e:Container | b:Node[idle,d:binding] | c:Node[idle,d:binding] | d:Bind){};
        |%rule r_test2 Container.(Node[idle,d:outername] | $0) | $1 -> Container.$0| Node[idle,d:outername] | $1{};
        |
        |# Model
        |%agent a:Container.(e:Container.(b:Node[idle,d:binding]) | c:Node[idle,d:binding] | d:Bind);
        |
        |#SortingLogic
        |
        |# Go!
        |%check;
        |""".stripMargin

    var s2 =
      """
        |# Controls
        |%active Greater : 2;
        |%active Node : 2;
        |%active Container : 0;
        |%binding Bind;
        |
        |# Names
        |%outername d;

        |
        |# Rules
        |%rule r_test1 Container.(e:Container.(b:Node[idle,d:binding]) | c:Node[idle,d:binding] | d:Bind) ->
        |Container.(e:Container | b:Node[idle,d:binding] | c:Node[idle,d:binding] | d:Bind){};
        |%rule r_test2 Container.(Node[idle,d:outername] | $0) | $1 -> Container.$0 | Node[idle,d:outername] | $1{};
        |
        |# Model
        |%agent a:Container.(e:Container.(b:Node[idle,d:edge]) | c:Node[idle,d:edge]);
        |
        |#SortingLogic
        |
        |# Go!
        |%check;
        |""".stripMargin

    GlobalCfg.DEBUG = false
    val t = BGMParser.parseFromString(s)

    val b = BGMTerm.toBigraph(t)
    GlobalCfg.checkBinding = false

    val transition = new TransitionSystem(b)
    println("=-=-=-=-=-==-=-=-=-=-=-==-=-=-=-=-=-==-=-=-=- now to build KripStructure-=-=-=-=-=-=-==-=-=-=-=-=-=-=-=-")
    val buildKripke = new BuildKripkeStructure(transition)
    val kripke = buildKripke.buildKripke
    val matchRes = transition.getMatchers
    //println(matchRes)
    println("===========================================================")
    println(kripke.toString)
    println(kripke.getClass)

  }
}