//package org.bigraph.bigsim.simulator
//
//import org.bigraph.bigsim.BRS.{Graph, Match, Matcher, Vertex}
//import org.bigraph.bigsim.model.{Bigraph, Nil, ReactionRule}
//import org.slf4j.{Logger, LoggerFactory}
//
//import scala.collection.mutable
//import scala.collection.mutable.ListBuffer
//import org.bigraph.bigsim.parser.{BGMParser, BGMTerm, TermParser}
//import org.bigraph.bigsim.transitionsystem.{KripkeStructure, State}
//import java.util.concurrent.ConcurrentHashMap
//
//// add by 19
//class TransitionSystem(agent: Bigraph, rules: List[ReactionRule]) {
//
//  // 这些字段用于进行反应，生成反应系统
//  val initAgent: Bigraph = agent
//  var queue: mutable.Queue[Bigraph] = mutable.Queue();  // 工作队列
//  queue.enqueue(agent);    // 初始模型进队列
//  var isOnTheFly: Boolean = false
//  var matchers: Map[Bigraph, List[(Bigraph, String)]] = Map();
//  var visited: ListBuffer[Bigraph] = ListBuffer();                  // 这里使用一个可变数组 ListBuffer来记录中间产生的偶图
//  visited.append(agent)
//  var reachedAgent: Map[Int, Boolean] = Map(); // 记录已经产生的agent
//  def logger : Logger = LoggerFactory.getLogger(this.getClass)
//
//  def this(agent: Bigraph) = this(agent,agent.rules.toList)
//
//  def getAllBigraphs: List[Bigraph] = {
//    return this.visited.toList
//  }
//
//  def checkTransMap(trans: Map[Bigraph, List[(Bigraph, String)]]) = {
//    /// 这里先检查一下transitionMap 里面的内容
//    //println("现在检查一下tansitionMap里面的内容")
//    for(b <- trans.keys) {
//      val tmp = trans(b)
//      val b_hash: Int = {if (b.root != null) b.root.toString.hashCode; else "".hashCode;}
//      println(s"key:$b_hash", b.root.toString)
//      for((toB, rr) <- tmp) {
//        val toB_hash: Int = {if (toB.root != null) toB.root.toString.hashCode; else "".hashCode;}
//        print(s"\tval:$toB_hash $rr", toB.root.toString)
//      }
//      println("")
//    }
//  }
//
//
//  def getMatchers: Map[Bigraph, List[(Bigraph, String)]] = {
//    _getMatchers(agent.rules.toList, agent)
//  }
//  def _getMatchers(rules: List[ReactionRule], agent: Bigraph): Map[Bigraph, List[(Bigraph, String)]] = {
//    logger.debug("[kgq] clean the matchers")
//    this.matchers = Map();
//    // 【kgq】 判断初始agent是否为空
//    if (agent == null || agent.root == null) {
//      logger.debug("TransitionSystem::getMatchers(): initial bigraph is null");
//      return this.matchers
//    }
//    // 【kgq】 判断反应规则是否为空
//    if (rules == null || rules.isEmpty) {
//      logger.debug("TransitionSystem::getMatchers(): no reaction rules")
//      return this.matchers
//    }
//
//    while (queue.nonEmpty) {
//      val b: Bigraph = queue.dequeue();         // 从队首取出一个偶图
//      val b_hash: Int = {                       // 先计算该偶图的哈希值
//        if (b.root != null)
//          b.root.toString.hashCode();
//        else "".hashCode();
//      }
//      println("这个构造的内部循环了一次", visited, b_hash)
//      visited += b;                              // 已访问偶图进行记录
//      print()
//      reachedAgent += (b_hash -> true)          // 通过hash来记录已经产生的agent
//
//      var matches: scala.collection.mutable.Set[Match] = b.findMatches;        //  当前的偶图，去查找所有能匹配的上的反应规则
//
//      if (matches.nonEmpty) {
//        matches.foreach(it => {
//          var rr: ReactionRule = it.rule;
//          // 把匹配应用到新的agent中
//          val nb: Bigraph = b.applyMatch(it);
//
//
//          println("添加 进来之前")
//          this.checkTransMap(this.matchers)
//          if (!this.matchers.contains(b)){
//            println("添加新的映射")
//            this.matchers += (b -> List((nb, rr.toString)))
//            this.checkTransMap(this.matchers)
//            val nb_h: Int = {if (nb.root != null) nb.root.toString.hashCode(); else "".hashCode();}
//            println("再次检查hash", nb_h, nb.root.toString, "".hashCode)
//
//
//          } else {
//            val tmp: List[(Bigraph, String)] = this.matchers(b):+(nb, rr.toString)
//          }
//
//          val nb_hash: Int = {if (nb.root != null) nb.root.toString.hashCode(); else "".hashCode();}
//          println("生成了一个新的偶图", nb_hash)
//          println("添加进来之后")
//          this.checkTransMap(this.matchers)
//
//
//
//          if (nb.root == null) nb.root == new Nil();
//          if (!reachedAgent.contains(nb_hash) && nb.root != null) {           // 如果 新反应出来的偶图没有访问过，且偶图的根不为空，那么就把新生成的偶图“节点”放入队列中。
//            queue.enqueue(nb);
//          }
//        })
//      }
//      matches.clear(); // 清空匹配集合
//    }
//    this.matchers
//  }
//}
//
//// 从 TransitionSystem 构建一个 KripkeStructure
//class BuildKripkeStructure(trans: TransitionSystem) {
//
//  var bigToState: Map[Int, State] = Map()
//
//  val kripkeStruct: KripkeStructure = new KripkeStructure()
//  var build: Boolean = false
//  val propRR: mutable.Map[String, ReactionRule] = mutable.Map()
//  val propStr: mutable.Map[String, String] = trans.initAgent.prop   // 获取所有原子命题的名字
//  val transitionMap:Map[Bigraph, List[(Bigraph, String)]] = trans.getMatchers
//
//
//  def checkTransMap(trans: Map[Bigraph, List[(Bigraph, String)]]) = {
//    /// 这里先检查一下transitionMap 里面的内容
//    println("现在检查一下tansitionMap里面的内容")
//    for(b <- trans.keys) {
//      val tmp = trans(b)
//      val b_hash: Int = {if (b.root != null) b.root.toString.hashCode; else "".hashCode;}
//      for((toB, rr) <- tmp) {
//        val toB_hash: Int = {if (toB.root != null) toB.root.toString.hashCode; else "".hashCode;}
//        println(s"from $b_hash to $toB_hash")
//      }
//    }
//  }
//
//  val bigraphList: List[Bigraph] = trans.getAllBigraphs
//  println("初始化的时候，这里的大小是", bigraphList.size)
//
//  def buildKripke: KripkeStructure = {
//    if(this.build)
//      return this.kripkeStruct
//    buildPropMap        // 先构造原子命题映射： String -> ReactionRule
//    buildInitialState
//    buildState
//    buildTransition
//    this.build = true
//    this.kripkeStruct
//  }
//
//  def buildPropMap = {
//    println("========== build PropMap==================")
//    for((k, v) <- propStr){    // k是 prop的名字，v是prop的项语言描述
//      val propRedex = TermParser.apply(v)
//      println("---------- build prop React----------------")
//      val propReact = TermParser.apply("nil")
//      val rr = new ReactionRule(k, propRedex, propReact, "")     // 使用一个原子命题作为反应物构造一个反应规则
//      propRR += (k -> rr)   // 把新构造的反应规则放到propRR中保存起来
//    }
//  }
//
//  def buildInitialState = {
//    println("==============build initialState========")
//    val b: Bigraph = trans.initAgent
//    val agentHash: Int = {
//      if (b.root != null)
//        b.root.toString.hashCode;
//      else "".hashCode;
//    }
//    val stateName = formatHash(agentHash)
//    val stateAtom = checkAtom(b)
//    val initialState = new State(stateName, stateAtom: _*)    // 这里创建一个state，因为构造方式是多参数的，所以要把stateAtom给展开
//    kripkeStruct.addInitialState(initialState)        // 把初始状态放入到kripke结构中去
//    bigToState+=(agentHash -> initialState)    // 把偶图到状态的映射关系也存起来
//    println(bigToState)
//  }
//
//  def buildState = {
//    println("===================build states===========")
//    for(b <- this.bigraphList) {  // 遍历所有偶图
//      val b_hash: Int = {
//        if (b.root != null)
//          b.root.toString.hashCode;
//        else "".hashCode;
//      }
//      if(bigToState.contains(b_hash)){
//        println("Already processed to state")
//        println("bigraphList:::::::", bigraphList)
//      }
//      else {
//        val stateName = formatHash(b_hash)
//        val stateAtom = checkAtom(b)
//        val curState = new State(stateName, stateAtom: _*)
//        if(this.transitionMap.contains(b))
//          kripkeStruct.addState(curState)
//        else
//          kripkeStruct.addFinalState(curState)
//        bigToState+=(b_hash -> curState)     //把偶图到状态的映射关系也存起来
//      }
//    }
//    println(this.bigraphList)
//    println(bigToState)
//  }
//
//  def checkAtom(b: Bigraph): List[String] = {    // 输入的是一个偶图，检测它所满足的原子命题
//    val retList: ListBuffer[String] = ListBuffer()
//    for((name, rr) <- propRR) {    // 对于每一个反应规则
//      val mp: mutable.Set[Match] = Matcher.tryMatchTermReactionRule(b.root, rr)   // 每一条反应规则（原子命题） 都尝试和b进行反应
//      if(mp.nonEmpty)       //  如果返回的结果不空，那么说明反应规则能够匹配的上，也就是说 反应物的原子命题是可以被b满足的
//        retList.append(name)
//    }
//    retList.toList
//  }
//
//  def buildTransition = {
//    println("========== build transition ===============")
//    val matchMap = transitionMap
//    for(b <- matchMap.keys) {    // 在偶图 状态 映射中遍历 key
//      println("注意这里！！！！！！！！！！！")
//      val tmpList = matchMap(b)
//      println("这里也查看一下==============")
//      val b_hash: Int = {if (b.root != null) b.root.toString.hashCode; else "".hashCode;}
//      if(!bigToState.contains(b_hash)){
//        println("那就因该是没有保存下来了")
//        println(bigToState)
//        println(b_hash)
//        println(matchMap.keys)
//      }
//      val fromState = bigToState(b_hash)
//      println("fromState: ", fromState)
//      for((toB, rr) <- tmpList) {   //   遍历下一个状态
//        val toB_hash: Int = {if (toB.root != null) toB.root.toString.hashCode; else "".hashCode;}
//        val toState = bigToState(toB_hash)
//        println("add toState: ", toState)
//        kripkeStruct.addTransition(fromState, toState)
//      }
//    }
//  }
//  // 下面的这两个字段，以及两个方法，主要是用来从Bigraph创建State的时候，对状态进行命名。方法来源于 org.bigraph.bigsim.BRS.Graph
//  var concurrent = new ConcurrentHashMap[String, String]()
//  var charIndex = 0
//  def formatHash(hash: Int): String = {
//    var str = "";
//    if (hash < 0) str = "" + hash.abs;
//    else str = hash.toString;
//    if(!concurrent.containsKey(str)){
//      concurrent.put(str, getStrValue(charIndex))
//      charIndex = charIndex + 1
//    }
//    concurrent.get(str)
//  }
//  def getStrValue(index: Int): String = {
//    val k = index / 26
//    var str = ""
//    if (k > 0) {
//      str += ((k % 26) - 1 + 'A').toChar + "_"
//    }
//    str += ((index % 26) + 'A').toChar
//    str
//  }
//}
//
//object testTransitionSystem {
//  def main(args: Array[String]): Unit = {
//
//    val bigraphExam =
//      """
//        |# Controls
//        |%active Greater : 2;
//        |%active Less : 2;
//        |%active GreaterOrEqual : 2;
//        |%active LessOrEqual : 2;
//        |%active Equal : 2;
//        |%active NotEqual : 2;
//        |%active Exist : 1;
//        |%active InstanceOf : 2;
//        |%active Client : 2;
//        |%active Coin : 2;
//        |%active Register : 1;
//        |%active Data : 0;
//        |%active call_foo : 0;
//        |%active True : 0;
//        |%active False : 0;
//        |%active aNum : 1;
//        |%active SmartContract : 1;
//        |%active Control1 : 0;
//        |
//        |# Rules
//        |%rule r_test3 a:Client[idle,a:edge].(c:Coin[idle,idle] | d:Coin[idle,idle] | e:Coin[idle,idle] | h:Register[idle]) | b:Client[idle,a:edge].(f:Coin[idle,idle] | g:Coin[idle,idle] | i:Register[idle]) -> a:Client[idle,a:edge].(c:Coin[idle,idle] | d:Coin[idle,idle] | h:Register[idle]) | b:Client[idle,a:edge].(f:Coin[idle,idle] | g:Coin[idle,idle] | i:Register[idle]) | e:Coin[idle,idle]{Probability:0.3};
//        |
//        |%rule r_test4 a:Client[idle,a:edge].(h:Register[idle] | _class_1639578794528:Coin[idle,idle] | f:Coin[idle,idle] | $0) | b:Client[idle,a:edge].(g:Coin[idle,idle] | i:Register[idle]) -> a:Client[idle,a:edge].(h:Register[idle] | _class_1639578794528:Coin[idle,idle] | f:Coin[idle,idle] | $0) | b:Client[idle,a:edge].(g:Coin[idle,idle] | i:Register[idle]){};
//        |
//        |# prop
//        |%prop a a:Client[idle,idle].(b:Coin[idle,idle] | $0);
//        |
//        |# Model
//        |%agent a:Client[idle,a:edge].(c:Coin[idle,idle] | d:Coin[idle,idle] | e:Coin[idle,idle] | h:Register[idle]) | b:Client[idle,a:edge].(f:Coin[idle,idle] | g:Coin[idle,idle] | i:Register[idle]);
//        |#SortingLogic
//        |# Go!
//        |%check;
//        |""".stripMargin
//    val t = BGMParser.parseFromString(bigraphExam)
//    val b = BGMTerm.toBigraph(t)
//    val transition = new TransitionSystem(b)
//    println("=-=-=-=-=-==-=-=-=-=-=-==-=-=-=-=-=-==-=-=-=- now to build KripStructure-=-=-=-=-=-=-==-=-=-=-=-=-=-=-=-")
//    val buildKripke = new BuildKripkeStructure(transition)
//    val kripke = buildKripke.buildKripke
//    val matchRes = transition.getMatchers
//    //println(matchRes)
//    println("===========================================================")
//    println(kripke.toString)
//    println(kripke.getClass)
//
//  }
//}