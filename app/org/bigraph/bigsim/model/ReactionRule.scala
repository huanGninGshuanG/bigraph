package org.bigraph.bigsim.model

import org.bigraph.bigsim.data.Data
import org.bigraph.bigsim.data.DataSpliter
import org.bigraph.bigsim.parser.{Arith, ArithmeticExprParser, BooleanExprParser, Cond, ConditionExprParser, HMM, TermParser}
import org.bigraph.bigsim.utils.{DebugPrinter, GlobalCfg}

import scala.collection.mutable.Set
import cern.jet.random.Poisson
import cern.jet.random.engine.RandomEngine
import cern.jet.random.Exponential
import org.bigraph.bigsim.BRS.Match
import org.bigraph.bigsim.model.component.Signature
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.mutable.Queue
import scala.collection.mutable.ListBuffer

/**
 * @author zhaoxin
 *         version 0.1
 * @author liangwei
 *         version 0.2
 * @author kongguanqiao
 *         version 0.3
 */

//class ReactionRule(n: String, m: String, red: Term, react: Term, exp: String) {
class ReactionRule(n: String, red: Term, react: Term, exp: String, signature: Signature) {

  var logger: Logger = LoggerFactory.getLogger(this.getClass)

  def this(n: String, red: Term, react: Term, exp: String) = this(n, red, react, exp, null)

  /** Basic element of RR */
  var name: String = n
  //var pName: String = m 
  var redex: Term = red
  var reactum: Term = react
  var sig: Signature = signature
  val redexBig: Bigraph = {
    val bb = new BigraphBuilder(sig)
    DebugPrinter.print(logger, "parse redex:" + redex)
    bb.parseTerm(redex)
    bb.makeBigraph(true)
  }
  val reactumBig: Bigraph = {
    val bb = new BigraphBuilder(sig)
    DebugPrinter.print(logger, "parse reactum:" + reactum)
    bb.parseTerm(reactum)
    bb.makeBigraph(true)
  }

  var express: String = exp;

  var timeDistribution: String = ""

  var sysClkIncr: Int = 0 //bgm文件中的SysClk
  var hmms: Set[String] = Set()
  var reverse: Boolean = false
  var reactNodes: Set[String] = Set() //放React:Passenger,Airplane 的集合


  // xin
  var hmmState: Int = -1
  var hmmRandom: Boolean = false
  private[this] var _hmmProbability: Double = 0

  //add by wm
  var hash: Int = {
    var s: StringBuilder = new StringBuilder();
    s.append(name + ":");
    //s.append(pName + ":");
    s.append(redex.toString() + " ->");
    s.append(reactum.toString() + ":");
    s.append(express);
    s.toString().hashCode();
  }


  /** If a reaction rule contains data model, calculate it and split it */
  DataSpliter.preOrderData(redex, "root", this)
  DataSpliter.preOrderData(reactum, "root", this)

  /** redexCtrl <--> reactumCtrl */
  var nodeMap: Map[Node, Node] = calcNodeMap

  var data: Set[Prefix] = Set()
  var causation: Set[ReactionRule] = null

  /** If this is a stochastic bigraph, a rate constant will be assigned. */
  var rate: Double = 0
  var hasProbability: Boolean = false
  var probability: Double = 0 //lbj
  var dataCalcs: Queue[Tuple2[String, String]] = Queue() //Expr
  var conds: Set[String] = Set() //Cond
  var bdconds: Set[String] = Set() //back derivation conditions
  var random: Boolean = false

  /** 条件表达式，赋值表达式  add by kgq   */
  var conditions: Set[Cond] = Set() // 存放构造好的条件
  var nameToTerm: Map[String, Term] = Map()
  var assignments: List[Tuple2[String, Arith]] = List(); // kgq 存放赋值关系

  override def clone(): ReactionRule = {
    var rr = new ReactionRule(this.n, this.red, this.react, this.exp)
    rr.name = n
    rr.redex = redex.clone
    rr.reactum = reactum.clone
    rr.express = express
    rr.reactNodes = reactNodes
    rr.hash = hash
    rr.nodeMap = nodeMap
    rr.data = data
    rr.causation = causation
    rr.dataCalcs = dataCalcs
    return rr
  }

  def hmmProbability: Double = _hmmProbability

  def hmmProbability_=(value: Double): Unit = {
    _hmmProbability = value
  }

  initExprs

  /** Possion Distribution */
  var poTimeSpend: Double = getContiRRIncr

  /** init data calculate exprs and conds in a RR */
  def initExprs {
    //println("exp: " + exp)
    exp.split("\t").foreach(f => {
      //println("f: "  + f)
      if (f.startsWith(GlobalCfg.sysClkPrefStr)) {
        sysClkIncr = f.substring(GlobalCfg.sysClkPrefStr.length).trim.toInt
      } else if (f.startsWith(GlobalCfg.condPrefStr)) { // Cond:
        f.substring(GlobalCfg.condPrefStr.length).split(",").foreach(c => conds += c)
      } else if (f.startsWith(GlobalCfg.exprPrefStr)) { // Expr:
        f.substring(GlobalCfg.exprPrefStr.length).split(",").foreach(e => {
          val kv = e.split("=")
          if (kv.length == 2)
            dataCalcs.enqueue(new Tuple2(kv(0), kv(1)))
        })
      } else if (f.startsWith(GlobalCfg.randomPrefStr)) {
        random = f.substring(GlobalCfg.randomPrefStr.length).toBoolean
      } else if (f.startsWith(GlobalCfg.hmmPrefStr)) {
        f.substring(GlobalCfg.hmmPrefStr.length).split(",").foreach(h => {
          hmms += h
        })
      } else if (f.startsWith(GlobalCfg.ratePrefStr)) { // Rate:
        rate = f.substring(GlobalCfg.ratePrefStr.length).toDouble
      } else if (f.startsWith(GlobalCfg.probabilityPrefStr)) { //lbj，可以为0  Probability:
        hasProbability = true
        probability = f.substring(GlobalCfg.probabilityPrefStr.length).toDouble
      } else if (f.startsWith(GlobalCfg.reversePrefStr)) { // Reverse:
        reverse = f.substring(GlobalCfg.reversePrefStr.length).toBoolean
      } else if (f.startsWith(GlobalCfg.reactPrefStr)) { // React:
        f.substring(GlobalCfg.reactPrefStr.length).split(",").foreach(reactNodes.add(_))
      }

      // lixin 20190914
      else if (f.startsWith(GlobalCfg.hmmStatePrefStr)) { // HmmState
        hmmState = Integer.parseInt(f.substring(GlobalCfg.hmmStatePrefStr.length))
      } else if (f.startsWith(GlobalCfg.hmmRandomPrefStr)) {
        hmmRandom = f.substring(GlobalCfg.hmmRandomPrefStr.length).toBoolean
      }

      else if (f.startsWith(GlobalCfg.timeDistributionPreStr)) {
        timeDistribution = f.substring(GlobalCfg.timeDistributionPreStr.length + 1)
      }

      // kgq 20220225
      else if (f.startsWith(GlobalCfg.conditionPrefStr)) { // 将每个字符串形式的条件表达式放入到
        //        println("解析 条件：" + name + " 中 的条件表达式")
        f.substring(GlobalCfg.conditionPrefStr.length).split(',').foreach(c => {
          val tmpC = ConditionExprParser.parse(c)
          conditions += tmpC
          //          println("[from}: " + c + " [to] " + tmpC)
        })
      }
      else if (f.startsWith(GlobalCfg.assignmentPrefStr)) { // 将每个被赋值的变量和赋值的表达式进行映射
        f.substring(GlobalCfg.assignmentPrefStr.length).split(',').foreach(c => {
          val tmp = c.split('=')
          assignments = assignments.:+(Tuple2(tmp(0), ArithmeticExprParser.apply(tmp(1))))
        })
      }

    })
  }

  /**
   * Get the time by Possion distribution if the reaction rule is random支持整数
   */
  def getRRIncr: Int = {
    if (random) { //如果规则随机
      val re: RandomEngine = RandomEngine.makeDefault
      val po: Poisson = new Poisson(sysClkIncr, re)
      val incr = po.nextInt()
      if (sysClkIncr != 0 && incr < GlobalCfg.SysClkIncr.toInt) { //若生成的反应时间小于系统最小时间片，则取一个单位时间片
        GlobalCfg.SysClkIncr.toInt
      } else {
        incr / GlobalCfg.SysClkIncr.toInt * GlobalCfg.SysClkIncr.toInt //生成的反应时间是有可能为0的
      }
    } else
      sysClkIncr //如果规则不随机，返回bgm中用户定义的SysClk即反应花费时间
  }

  /**
   * Get the time by Possion distribution if the reaction rule is random支持浮点数，连续的
   */
  def getContiRRIncr: Double = {
    if (random) {
      val re: RandomEngine = RandomEngine.makeDefault
      val po: Poisson = new Poisson(sysClkIncr, re)
      poTimeSpend = po.nextDouble()
      poTimeSpend
    } else
      sysClkIncr
  }

  //add by lbj 指数分布随机数
  def getContiRRIncrExp: Double = {
    var exTimeSpend: Double = 0
    if (random) {
      val re: RandomEngine = RandomEngine.makeDefault
      val ex: Exponential = new Exponential(sysClkIncr, re)
      exTimeSpend = ex.nextDouble()
      exTimeSpend
    } else
      sysClkIncr
  }

  /**
   * Check if there is any condition not meet
   * Also Check HMM
   *
   * @return true/false
   */
  def check: Boolean = {
    if (GlobalCfg.checkData)
      conds.foreach(m => {
        val q = BooleanExprParser.parse(m)
        if (!q.check())
          return false
      })

    if (GlobalCfg.checkHMM)
      hmms.foreach(h => {
        if (HMM.hmms(h).getObsProbability < GlobalCfg.minProbability)
          return false
      })
    true
  }

  /**
   * Check if there is any condition not meet
   *
   * Control.variable can be mapped to node.variable based on
   * reactNodes and match.
   *
   * Also Check HMM
   *
   * @return true/false
   */
  def check(m: Match): Boolean = {

    if (GlobalCfg.checkData) {
      conds.foreach(c => { //有条件才会走这里，不管reactNodes是否为空，没有条件的话check会返回true
        var cond = c
        reactNodes.foreach(rn => { //如reactNodes为Set(Passenger)，//如果reactNodes为空则下面不会执行
          if (cond.contains(rn)) { //这里就是把条件出现了Control的翻译成node，如Cond:Passenger.hasCheckinLuggage=='true'翻译成David.hasCheckinLuggage=='true'
            var rns = m.reactNodesMap.getOrElse(rn, Set()) //如m.reactNodesMap为Map(Passenger -> Set(David))
            if (rns.size > 0) {
              cond = cond.replaceAll(rn, rns.head) //rn为Passenger rns为David 用David替换Passenger
            }
          }
        })
        //如果reactNodes为空上面的循环没有执行，没有将条件翻译Passenger.hasCheckinLuggage=='true'翻译成David.hasCheckinLuggage=='true'，找不到Passenger.hasCheckinLuggage这样的数据，自然条件就过不去了，导致带Passenger这种条件都会返回false
        val q = BooleanExprParser.parse(cond)
        //println("cond: "+cond)
        if (GlobalCfg.DEBUG)
        //println("check:" + cond + "\tresult:" + q.check)
          if (!q.check()) {
            return false
          }
      })
    }

    if (GlobalCfg.checkHMM)
      hmms.foreach(h => {
        if (HMM.hmms(h).getObsProbability < GlobalCfg.minProbability)
          return false
      })
    true
  }

  /**
   * Update variables once a RR takes place
   */
  def update {
    Data.data("SysClk").value = GlobalCfg.SysClk.toString
    dataCalcs.foreach(f => {
      Data.update(f._1, f._2)
    });
  }

  /**
   * Update variables once a RR takes place 反应规则发生后更新data
   */
  def update(m: Match) {
    Data.data("SysClk").value = GlobalCfg.SysClk.toString
    dataCalcs.foreach(dc => {

      var leftTerm = dc._1
      var rightTerm = dc._2

      reactNodes.foreach(rn => { //如果reactNodes为空则下面不会执行
        var rns = m.reactNodesMap.getOrElse(rn, Set()) //如RR的reactNodes为Set(Passenger)，rn为Passenger，则rns为Set(David)
        if (leftTerm.contains(rn)) { //这里就是把表达式左边或者右边出现了Control的翻译成node，如Passenger.hasChecked='true'的左边出现的Control，则根据m的reactNodesMap取出David，翻译成David.hasChecked='true'
          if (rns.size > 0) {
            leftTerm = leftTerm.replaceAll(rn, rns.head)
          }
        }
        if (rightTerm.contains(rn)) {
          if (rns.size > 0) {
            rightTerm = rightTerm.replaceAll(rn, rns.head)
          }
        }
      })
      //这句只能update表达式中已经是node的数据，比如taxi.businessfee，就是不牵扯要把Passenger映射为David的这种表达式
      Data.update(leftTerm, rightTerm) //如果reactNodes为空这里也会执行，但是上面的循环没有执行，则没办法将Passenger.hasChecked='true'翻译成David.hasChecked='true'，update数据就会不成功
    });
  }

  /**
   * add by lbj: Back Derivation Data Update (Set to Init data)
   */
  def updateBD(m: Match) {
    Data.data("SysClk").value = GlobalCfg.SysClk.toString
    dataCalcs.foreach(dc => {

      var leftTerm = dc._1
      var rightTerm = ""
      //      var rightTerm = dc._2  //不管右边是什么，直接设置为初值
      if (Data.bdInitData.contains(leftTerm)) {
        println("Data.bdInitData(leftTerm).value: " + Data.bdInitData(leftTerm).value)
        if (Data.bdInitData(leftTerm).dataType.equals("String"))
          rightTerm = "'" + Data.bdInitData(leftTerm).value + "'"
        else rightTerm = Data.bdInitData(leftTerm).value
      }

      reactNodes.foreach(rn => { //如果reactNodes为空则下面不会执行
        var rns = m.reactNodesMap.getOrElse(rn, Set()) //如RR的reactNodes为Set(Passenger)，rn为Passenger，则rns为Set(David)
        if (leftTerm.contains(rn)) {
          if (rns.size > 0) {
            leftTerm = leftTerm.replaceAll(rn, rns.head)
          }
        }
      })
      //这句只能update表达式中已经是node的数据，比如taxi.businessfee，就是不牵扯要把Passenger映射为David的这种表达式
      Data.update(leftTerm, rightTerm)
      println(leftTerm + ": " + Data.data(leftTerm).value)
    });
  }

  /**
   * get expressions
   */
  def getExps: String = {
    var exps: String = ""
    dataCalcs.map(f => {
      if (exps.equals(""))
        exps = f._1 + "=" + Data.getValue(f._1)
      else
        exps += "," + f._1 + "=" + Data.getValue(f._1)
    })
    exps
  }

  /**
   * get conditions
   */
  def getConds: String = {
    var exps: String = ""
    conds.map(f => {
      if (exps.equals(""))
        exps = f
      else
        exps += "," + f
    })
    exps
  }

  /**
   * Get HMM    @weixin
   */
  def getHMM: String = {
    hmms.map(h => {
      h + "=" + (HMM.hmms(h).getObsProbability * 100).formatted("%.2f") + "%"
    }).toList.mkString(",")
  }

  /**
   * for data flow
   */
  //暂时先考虑，规则只有一个def的情况。cuse以及puse虽然用set，但是暂时考虑单个情况
  var cuses: Set[Term] = Set()
  var puses: Set[Term] = Set()
  var defTerm: Set[Term] = Set()

  var cuseRules: Set[String] = Set()
  var puseRules: Set[String] = Set()
  var defRules: Set[String] = Set() //其它有相同def的规则

  //def this(red: Term, react: Term) = this(null, null, red, react, "")
  //def this(n: String, m: String, red: Term, react: Term) = this(n, m, red, react, "")
  def this(red: Term, react: Term) = this(null, red, react, "")

  def this(n: String, red: Term, react: Term) = this(n, red, react, "")

  override def toString = {
    if (redex != null && reactum != null) {
      name + ":" + redex.toString + " -> " + reactum.toString;
    } else {
      if (redex != null)
        name + ":" + redex.toString() + " -> NULL";
      else if (reactum != null)
        name + ":" + "NULL -> " + reactum.toString();
      else
        "<error printing rule>";
    }
  }

  def calcNodeMap: Map[Node, Node] = {
    //    println("[kgq] reactionrule: " + name + " calcNodeMap: ")
    var res: Map[Node, Node] = Map();
    var redexNodes = getTermNodes(redex);
    var reactumNodes = getTermNodes(reactum);
    //    println("\t[kgq]redexNodes is: " + redexNodes)
    //    println("\t[kgq]reactumNodes is: " + reactumNodes)

    // redex、reactum中Node/Control数量不一致修正
    /**
     * Match both the node or control both in redex and reactum
     */
    var redexNodesName = redexNodes.map((_.name));
    var reactumNodesName = reactumNodes.map(_.name);
    //    println("\t\t[kgq]redexNodesName is: " + redexNodesName)
    //    println("\t\t[kgq]reactumNodesName is: " + reactumNodesName)

    /**
     * If the reactum creates a new node, than it maps to itself.
     * Note that the link for a node may change during the reaction.
     * If not change link, than it is a full match, we take it as first place.
     * If change link, than just do as the node name and ctrl name.
     */
    reactumNodes.map(rn => {
      var fullMatch: Boolean = false // [kgq] 是否是完全匹配（port列表相同）
      var mNode: Node = null //kgq 记录 redex中匹配的node
      var hasMatch: Boolean = false //kgq  是否已经找到了redex 中匹配的node
      var maxMatch: Int = 0 //[kgq] 动态记录查找过程中port列表最大“相似度”
      //println("[kgq] cur reactum node is: " + rn)
      for (i <- 0 to redexNodes.length - 1 if !fullMatch) {
        //println("\t[kgq] check redex node: " + redexNodes(i))
        if (redexNodes(i).getNodeStr.equals(rn.getNodeStr)) { // [kgq] getNodeStr ->  name : ctrl
          /**
           * If it is the first time match, record it and the maxMatch name list.
           * If it is not the first time match, compare the maxMatch to find more
           * suitable match.
           */
          //println("\t\t[kgq] name matched!")
          if (!hasMatch) {
            hasMatch = true
            mNode = redexNodes(i)
            maxMatch = rn.getMatchPortsCount(redexNodes(i).ports) // [kgq] 根据getMatchPortsCount来计算“相似度” 即ports的匹配程度，
            //println(("\t\t\t[kgq] first match, mNode: " + mNode + " maxMatch: " + maxMatch))
          } else if (rn.getMatchPortsCount(redexNodes(i).ports) > maxMatch) { // [kgq] 如果找到更匹配的节点，那么
            mNode = redexNodes(i)
            maxMatch = rn.getMatchPortsCount(redexNodes(i).ports)
            //println("\t\t\t[kgq] other match mNode: " + mNode + " maxMatch: " + maxMatch)
          }

        }
        if (redexNodes(i).getNodeStr.equals(rn.getNodeStr)
          && redexNodes(i).getNodePortsStr.equals(rn.getNodePortsStr)) { // [kgq] 如果找到的结点port 能够完全匹配，那么终止本次在redex中的查找
          //println("\t\t[kgq] fullMatch !!")
          fullMatch = true
          mNode = redexNodes(i)
        }
      }
      if (hasMatch) {
        res += (rn -> mNode)
        // redexNodes.filter(_ != mNode)
        redexNodes = redexNodes.filter(_ != mNode) // modified by kgq 20220301 上面的语句无法成功修改redexNodes ，因为List是不可变的，所以.filter 会返回一个新的List，而不是原地修改
        //println("\t\t[kgq] hasMatch res: " + res + " redexNodes: " + redexNodes)
      } else {
        /**
         * if the node is a new node, assign the new node with new node name
         */
        //rn.name = rn.ctrl.name.substring(0, 1) + "_" + GlobalCfg.ranNameIndex.toString

        res += (rn -> rn)
        //println("\t\t[kgq] notMatch res: " + res + " redexNodes: " + redexNodes)
      }
    })

    /**
     * If reactum contails the redex node, keep it in the redexNodes.
     * So now, redexNodes is small than reactumNodes.
     *
     * If reactum node not in the redex, add itself
     * If in redex, keep it. So now, redexNodes and reactumNodes are the same.
     */

    /*
    redexNodes = redexNodes.filter(x => reactumNodesName.contains(x.name));
    reactumNodes = reactumNodes.filter(x => {
      var isIn = redexNodesName.contains(x.name);
      if (!isIn) res += (x -> x);
      isIn;
    });

    var index = 0;
    /**
     * The get node function return a sorted list.
     */
    if (redexNodes.size == reactumNodes.size) {
      reactumNodes.map(x => {
        res += (x -> redexNodes(index));
        index += 1;
      });
    } */
    res;
  }

  // 计算Term中所有的Node
  def getTermNodes(t: Term): List[Node] = {
    var nodes: List[Node] = List();
    if (t.isInstanceOf[Prefix]) {
      var pre: Prefix = t.asInstanceOf[Prefix];
      nodes = nodes.:+(pre.node);
      nodes = getTermNodes(pre.suffix) ::: nodes;
    } else if (t.isInstanceOf[Paraller]) {
      var par: Paraller = t.asInstanceOf[Paraller];
      nodes = getTermNodes(par.leftTerm) ::: nodes;
      nodes = getTermNodes(par.rightTerm) ::: nodes;
    } else if (t.isInstanceOf[Regions]) {
      var reg: Regions = t.asInstanceOf[Regions];
      nodes = getTermNodes(reg.leftTerm) ::: nodes;
      nodes = getTermNodes(reg.rightTerm) ::: nodes;
    }
    nodes = nodes.sortWith((n1: Node, n2: Node) => {
      n1.name < n2.name
    });
    nodes;
  }

  def causes(rule: ReactionRule) {
    causation += rule
  }

}

object testRR {
  def main(args: Array[String]) {
    var redexS = "Philosopher[left11:edge,right11:edge].(Fork[right12:edge,left11:edge] | Lock) | Fork[right11:edge,left12:edge] | $0";
    var reactumS = "Philosopher[left11:edge,right11:edge].(Fork[right12:edge,left11:edge] | Lock | Fork[right11:edge,left12:edge]) | $0";
    redexS = "ChargingRoom.$0 | Patient[Patient_pill:edge,idle,isDecoction:edge].$2 | Pill[Patient_pill:edge] | $1";
    reactumS = "a:Hospital.(Pill[patient_pill:edge].nil|b:Patient[patient_pill:edge,idle,isDecoction:edge].nil|c:ConsultingRoom.f:Computer[connected:edge].Prescription[patient_prescription:edge].nil|d:ChargingRoom.g:Computer[connected:edge].Bill[patient_bill_payed:edge].nil|e:Pharmacy.h:Computer[connected:edge].nil)";
    redexS = "Pharmacy.(Patient[patient_prescription:edge,patient_bill_payed:edge,isDecoction:edge].IsDecoction[isDecoction:edge,value_is:edge,leftValue:edge].Value[value_is:edge] | Pill[idle] | $0) | Equal[leftValue:edge,rightValue:edge] | False[rightValue:edge] | $1";

    var redex = TermParser.apply(redexS);
    var reactum = TermParser.apply(reactumS);
    println(redex.termType)
    println(reactum.termType)

    redexS = "zone[idle,idle].network[idle,idle,idle,idle].(mobile[idle,idle,idle,idle,hasCAH1:edge,idle].(contextAware[hasCAH1:edge,idle,idle,powerIs1:edge,idle,idle,idle].power[powerIs1:edge] | application[idle,idle,idle,idle]) | mobile[idle,idle,idle,idle,hasCAH2:edge,idle].(contextAware[hasCAH2:edge,idle,idle,powerIs2:edge,idle,idle,idle].power[powerIs2:edge] | application[idle,idle,idle,idle]) | mobile[idle,idle,idle,idle,hasCAH3:edge,idle].(contextAware[hasCAH3:edge,idle,idle,powerIs3:edge,idle,idle,idle].power[powerIs3:edge] | application[idle,idle,idle,idle])) | $0";
    reactumS = "zone[idle,idle].network[idle,idle,idle,idle].(mobile[idle,idle,idle,idle,hasCAH1:edge,idle].(contextAware[hasCAH1:edge,idle,idle,powerIs1:edge,pointIs1:edge,lumIs1:edge,pressureIs1:edge].(power[powerIs1:edge] | pointXY[pointIs1:edge] | luminous[lumIs1:edge] | pressure[pressureIs1:edge]) | application[idle,idle,idle,idle].job1:job[idle]) | mobile[idle,idle,idle,idle,hasCAH2:edge,idle].(contextAware[hasCAH2:edge,idle,idle,powerIs2:edge,pointIs2:edge,lumIs2:edge,pressureIs2:edge].(power[powerIs2:edge] | pointXY[pointIs2:edge] | luminous[lumIs2:edge] | pressure[pressureIs2:edge]) | application[idle,idle,idle,idle].job2:job[idle]) | mobile[idle,idle,idle,idle,hasCAH3:edge,idle].(contextAware[hasCAH3:edge,idle,idle,powerIs3:edge,pointIs3:edge,lumIs3:edge,pressureIs3:edge].(power[powerIs3:edge] | pointXY[pointIs3:edge] | luminous[lumIs3:edge] | pressure[pressureIs3:edge]) | application[idle,idle,idle,idle].job3:job[idle])) | $0";
    redex = TermParser.apply(redexS)
    reactum = TermParser.apply(reactumS)
    //var r = new ReactionRule("r_28", "P0", redex, reactum, "time:5 cond:power +=5%;fee+=3");
    var r = new ReactionRule("r_28", redex, reactum, "time:5 cond:power +=5%;fee+=3");
    var a = r.nodeMap
    println(a.toString)
    println(r.conds)

  }
}