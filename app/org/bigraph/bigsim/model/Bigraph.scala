package org.bigraph.bigsim.model

import java.io.File

import scala.collection.mutable.{Map, Set}
import org.bigraph.bigsim.parser.{BGMParser, BGMTerm, TermParser}
import org.bigraph.bigsim.utils.GlobalCfg
import org.bigraph.bigsim.BRS.WideMatch
import org.bigraph.bigsim.BRS.Matcher
import org.bigraph.bigsim.BRS.Match
import org.bigraph.bigsim.data.Data
import org.bigraph.bigsim.data.DataModel
import org.bigraph.bigsim.simulator.Simulator
import org.slf4j.{Logger, LoggerFactory}
import java.lang.NumberFormatException
import java.util.Objects

import org.bigraph.bigsim.value.ValueOpCtrl

import scala.collection.mutable
import scala.util.Random

object Node {
  private var id: Int = 0

  def idIncrement: Int = {
    id += 1
    id
  }
}

class Node(n: String, act: Boolean, p: List[Name], c: Control, par: Node, num: String) {

  def this(n: String, act: Boolean, p: List[Name], c: Control, par: Node) = this(n, act, p, c, null, "")

  def this(n: String, act: Boolean, p: List[Name], c: Control) = this(n, act, p, c, null);

  var id: Int = Node.idIncrement
  var name = n
  var ports: List[Name] = p
  var active = act
  val ctrl = c
  var parent = par //add by lbj
  var hasChild: Boolean = false //add by lbj


  if (!ctrl.active)
    active = ctrl.active

  if (ctrl.arity == 0) //对只给名称的默认Control修正参数个数 
    ctrl.arity = ports.size;

  if (ports.size > c.arity) {
    println("Error: control " + Bigraph.controlToString(c) + " has arity" + c.arity
      + " but " + ports.size + " ports have been linked!");
    sys.exit(1);
  }

  var hasNum: Boolean = false // add by kgq
  var number: Any = null // 初始值为空， 后面会根据输入数字的类型，对该变量进行赋值
  if (num != "") {
    hasNum = true
    try {
      number = num.toInt
    } catch {
      case e: NumberFormatException => number = num.toDouble
    }
  }

  def getDoubleNum: Double = { // add by kgq 20220318 获得节点的Double数值，缺省值为0
    var ret: Double = 0
    try {
      ret = number.asInstanceOf[Double]
    } catch {
      case e: ClassCastException => ret = number.asInstanceOf[Int].toDouble
    }
    ret
  }

  def getNodeStr: String = {
    name + ":" + ctrl.name
  }

  def getNodeParent: String = {
    parent + ":" + parent.name
  }

  def getNodePortsStr: String = {
    "[" + ports.map(_.name).mkString(",") + "]"
  }

  // kgq 比较两个 port 列表的相似度： 长度不等->0，长度相等时->对位匹配的名字个数
  def getMatchPortsCount(otherPorts: List[Name]): Int = {
    var m: Int = 0
    if (ports.size == otherPorts.size) { // kgq 这个的匹配算是一个保险的检查吧，实际上应用的时候这个判断恒为true。 getMatchPortsCount方法只在calcNodeMap方法中使用，而比较的两个port，都是在name:ctrl 相等的情况下，所以port列表等长
      var index: Int = 0
      ports.foreach(p => {
        if (p.name.equals(otherPorts(index)))
          m += 1
        index += 1
      })
    }
    m
  }

  override def toString = {
    var ret = "Node_" + id + ":(" + getNodeStr + getNodePortsStr + ")"
    if (hasNum) {
      ret = ret + "value:" + number.toString
    }
    ret
  }

  /**
   * add by kgq 20220302 解决 BUG3： applyMatch 后，原偶图被修改的问题
   * 这里实现的功能是对当前的节点的一个复制
   * 需要注意的是，
   *
   * @return
   */
  override def clone(): Node = {

    var newNode = new Node(name, active, ports, ctrl, parent)
    newNode.hasChild = hasChild
    newNode.hasNum = hasNum
    newNode.number = number
    newNode.id=id
    newNode
  }

  def checkIsAnonymous():Boolean={
    return name==""
  }

//  override def equals(o: Any): Boolean = {
//    if (o == null || (getClass != o.getClass)) return false
//    val other = o.asInstanceOf[Node]
//    Objects.equals(other.id,id)
//  }
//
//  override def hashCode: Int = id
}

object Control {
  private var id: Int = 0;

  def idIncrement: Int = {
    id += 1;
    id;
  }
}

class Control(n: String, ar: Int, act: Boolean, s: PlaceSort) {
  val id: Int = Control.idIncrement;
  val name: String = n;
  var arity: Int = ar;
  var active: Boolean = act;
  var placeSort: PlaceSort = s;
  var binding: Boolean = false; //add by lbj add binding control

  def this(n: String, ar: Int, act: Boolean) = this(n, ar, act, null);

  def this(n: String, ar: Int) = this(n, ar, true, null);

  def this(n: String) = this(n, 0, true, null);

  def this(n: String, ar: Int, act: Boolean, b: Boolean) = {
    this(n, ar, act); binding = b;
  }

  override def toString = "Control:(" + name + "," + id + "," + arity + "," + placeSort + "," + binding + ")";
}

object Name {
  private var id: Int = 0;

  def idIncrement: Int = {
    id += 1;
    id;
  }
}

class Name(n: String, nt: String, nl: List[String]) { //即port

  def this(n: String, nt: String) = this(n, nt, List())

  var name = n;
  var id = Name.idIncrement;
  var nameType = nt; //edge        这里的nameType 的添加，好像是在刘若愚学长论文中
  var innerNameList = nl; //  add by kgq 20220311 如果当前你是一条边，那么记录这条边连接的内部名列表

  override def toString = "Name:(" + name + "," + id + "," + nameType + getInnerNames + ")"; //打印ports

  def getInnerNames: String = { // add by kgq 20220311
    var ret = ""
    if (!innerNameList.isEmpty)
      ret = "innernames: " + nl.toString
    ret
  }

//  override def equals(o: Any): Boolean = {
//    //if (this == o) return true
//    if (o == null || (getClass != o.getClass)) return false
//    val other = o.asInstanceOf[Name]
//    Objects.equals(other.id,id)
//  }
//
//  override def hashCode: Int = id
}

// Bigraph is (V,E,ctrl,prnt,link) : <n,K> -> <m,L>
object Bigraph {
  var nameMap: Map[Pair[String, String], Name] = Map();
  var nodeMap: Map[String, Node] = Map();
  var controlMap: Map[String, Control] = Map();
  var modelNames: List[Name] = List();
  var bindingSet: Set[String] = Set();

  var baseAgentMap: Map[String, String] = Map(); // add by kgq
  //  var sorting: Sorting = new Sorting();

  def controlFromString(ctrlName: String): Control = {
    if (ctrlName == "")
      null;
    if (!controlMap.contains(ctrlName)) {
      val fresh: Control = new Control(ctrlName, 0);
      controlMap(ctrlName) = fresh;
    }
    controlMap(ctrlName);
  }

  def nameFromString(n: String, nt: String): Name = {
    var namePair = new Pair(n, nt);
    if (n == "")
      0;
    if (!nameMap.contains(namePair)) {
      val fresh: Name = new Name(n, nt);
      nameMap(namePair) = fresh;
    }
    nameMap(namePair);
  }

  def nameFromString(n: String, nt: String, innername: List[String]): Name = { //  add by kgq 20220311
    var namePair = new Pair(n, nt);
    if (n == "")
      0;
    if (!nameMap.contains(namePair)) {
      val fresh: Name = new Name(n, nt, innername);
      nameMap(namePair) = fresh;
    }
    nameMap(namePair)
  }


  def controlToString(c: Control): String = {
    for (entry <- controlMap.toList if entry._2 == c)
      return c.toString();
    return "<unknown control>";
  }

  def nameToString(n: Name): String = {
    /*		if (n == 0) return "-";
		for (entry <- nameMap.toList if entry._2 == n)
			return n.toString();
		return "<unknown name>";*/
    ""
  }

  def addControl(n: String, ar: Int, act: Boolean): Control = {
    val f: Control = new Control(n, ar, act);
    controlMap(n) = f;
    f;
  }

  def addControl(n: String, ar: Int, act: Boolean, binding: Boolean): Control = { //add by lbj
    val f: Control = new Control(n, ar, act, binding);
    controlMap(n) = f;
    f;
  }

  def addBinding(n: String, arity: Int): Control = {
    bindingSet.add(n);
    println("添加binding" + n)
    val f: Control = new Control(n, arity, true, true);
    println(f.binding)
    controlMap(n) = f;
    f
  }

  def addBaseAgent(n: String, term: String): String = {
    baseAgentMap(n) = term
    term
  }

  def isFree(n: Name): Boolean = {
    /*if (n== null || modelNames == null || modelNames.size == 0)
			false;
		else !modelNames.contains(n);*/
    /*else {
		  var names : Set[String] = Set()
		modelNames.map(ite => {
		  names.add(ite.name)
		})
		!names.contains(n.name)
		}*/

    true;
  }

}

class Bigraph(roots: Int = 1) {

  def logger: Logger = LoggerFactory.getLogger(this.getClass)

  /*
   * yw added for verify
   */
  var linked: Bigraph = null; //for verify
  var verifyID: String = "";


  var root: Term = null;
  var inner: Set[Name] = Set();
  var outer: Set[Name] = Set();
  var rules: Set[ReactionRule] = Set();

  //add by lbj
  var pattern: Term = null; //只有一个关注子模式
  var error: Term = null; //目前仅支持一个错误模式
  var bindings: Set[Binding] = Set(); //每行binding是一个Binding
  var trackings: Set[Tracking] = Set();
  //  var ports: Set[Port] = Set();
  var placeSorts: Set[PlaceSort] = Set();
  var linkSorts: Set[LinkSort] = Set();
  var placeSortConstraints: Set[String] = Set();
  var linkSortConstraints: Set[String] = Set();

  // add by wm
  var isInitial: Boolean = false;
  var isFinal: Boolean = false;
  var label: String = null;
  var isNegative: Boolean = false;

  var needCheckCTL: Boolean = false;
  var properties: mutable.Map[String, mutable.Map[String, String]] = Map()
  var experimentTurns = 1;
  var experimentParameters: List[mutable.Map[String, mutable.Map[String, String]]] = List()
  var hasSetEx: Boolean = false

  var rex: Set[Term] = Set()

  // add by kgq
  var ltlSpec: List[String] = List(); // 存放ltl规约公式的列表，先是按照字符串形式存放
  var ctlSpec: List[String] = List(); // 存放ctl规约公式的列表，先是按照字符串形式存放
  var prop: Map[String, Tuple2[String, String]] = Map(); // 存放原子命题的字典，从原子命题的名字到原子命题的描述进行映射。

  def this() = this(1);

  def addOuterName(n: Name) = {
    outer.add(n);
    Bigraph.nameMap(new Pair(n.name, n.nameType)) = n;
  }

  def addInnerName(n: Name) = {
    inner.add(n);
    Bigraph.nameMap(new Pair(n.name, n.nameType)) = n;
  }

  def addRule(r: ReactionRule) = {
    rules.add(r);
  }

  //add by lbj
  def addBinding(b: Binding) = {
    bindings.add(b);
  }

  def addTracking(t: Tracking) = {
    trackings.add(t);
  }

  //  def addPort(p: Port) = {
  //    ports.add(p);
  //  }

  def addplaceSort(s: PlaceSort) = {
    placeSorts.add(s);
  }

  def addlinkSort(s: LinkSort) = {
    linkSorts.add(s);
  }

  def addPlaceSortConstraints(t: String) = {
    placeSortConstraints.add(t);
  }

  def addLinkSortConstraints(t: String) = {
    linkSortConstraints.add(t);
  }


  /**
   * Find matches of one bigraph with a given reaction rule 当前bigraph与一个反应规则匹配
   */
  def findMatchesOfRR(rr: ReactionRule): Set[Match] = {
    var res: Set[Match] = Set();

    //logger.debug("bigraph::find_matches(): redex: " + rr.redex.toString);
    /**
     * @author liangwei
     *         add relation decision
     */
    var relations = true
    if (rr.data != null && GlobalCfg.checkData) {
      rr.data.map(relation => {
        relations = DataModel.relationDecision(relation)
      })
    }

    /**
     * @author liangwei
     *         add RR conditions and realtions
     *         relations:  Greater, Lesser and so on
     *         conditions: whether check expression or not
     */

    if (relations) {
      var mp: Set[Match] = Matcher.tryMatchTermReactionRule(root, rr);
      mp.foreach(m => {
        if (rr.check(m)) { //如果cond过不去，这里rr虽然能匹配上但这里会被过滤掉不被加入res中
          res.add(m)
        }
      })
    }

    if (GlobalCfg.DEBUG) {
      logger.debug("Matches:");
      res.map(x => {
        logger.debug("*" + x + ":" + x.toString)
      });
    }
    res;
  }

  /**
   * Find matches of one bigraph with rules 当前bigraph与所有反应规则匹配1
   */
  def findMatches: Set[Match] = {
    var res: Set[Match] = Set();
    rules.foreach(x => { //rules为所有反应规则
      /**
       * @author liangwei
       *         add relation decision
       */
      var relations = true
      if (x.data != null && GlobalCfg.checkData) { // 这里的x是反应规则， x.data是prefix的集合 //感觉这里应该判断x.data的size？
        x.data.foreach(relation => {
          relations = DataModel.relationDecision(relation)
        })
      }

      /**
       * @author liangwei
       *         add HMM
       */
      /**
       * @author liangwei
       *         add RR conditions and realtions
       *         relations:  Greater, Lesser and so on
       *         conditions: whether check expression or not
       */
      if (relations) {
        var mp: Set[Match] = Matcher.tryMatchTermReactionRule(root, x); //lry：此函数似乎落下了一部分rule

        mp.foreach(m => {
          if (x.check(m)) { //如果cond过不去，这里的x虽然能匹配上但这里会被过滤掉不被加入res中，例第一次就可以匹配r_takeoff这个规则，但因为不满足cond则不加入res中

            res.add(m); //如果这条RR没有cond也能加进来，check函数能过
          }
        })
      }
    });
    res
  }

  def applyMatch1(m: Match): Bigraph = {
    if (m == null) return this;
    if (!m.isWide) {

      var b: Bigraph = new Bigraph;

      b.root = root.applyMatch(m);

      b.inner = inner;
      b.outer = outer;
      b.rules = rules;
      b.trackings = trackings;
      b.bindings = bindings;
      b.placeSortConstraints = placeSortConstraints;
      b.placeSorts = placeSorts;
      b.linkSorts = linkSorts;
      b.linkSortConstraints = linkSortConstraints;
      b.pattern = pattern;
      Simulator.matchMarkDelete(m);
      return b;
    } else {
      if (m.rule.reactum.termType != TermType.TREGION) {
        logger.debug("bigraph::applyMatch Wide redexes must have wide reactums");
        sys.exit(1);
      }

      var reactum: Regions = m.rule.reactum.asInstanceOf[Regions];
      var wm: WideMatch = m.asInstanceOf[WideMatch];

      var b: Bigraph = new Bigraph();
      b.inner = inner;
      b.outer = outer;
      b.rules = rules;
      b.root = root;
      b.trackings = trackings;
      b.bindings = bindings;
      b.placeSortConstraints = placeSortConstraints;
      b.placeSorts = placeSorts;
      b.linkSorts = linkSorts;
      b.linkSortConstraints = linkSortConstraints;
      b.pattern = pattern;

      var mq: List[Match] = wm.submatches;
      var rc: List[Term] = List();
      var rRedex: List[Term] = m.rule.redex.asInstanceOf[Regions].getChildren;
      var rcRaw: List[Term] = reactum.getChildren;
      rRedex.map(x => {
        if (x.termType == TermType.TNIL) {
          var last: Term = rc.last;
          rc = rc.init;
          rc = rc.:+(new Paraller(last.id, last, rcRaw.head))
        } else {
          rc = rc.:+(rcRaw.head);
        }
        rcRaw = rcRaw.tail;
      });


      if (rc.size != wm.submatches.size) {
        logger.debug("bigraph::applyMatch Wide rules must match in the number of regions in the redex and reactum");
        sys.exit(1);
      }

      mq.map(x => {
        var nr: ReactionRule = new ReactionRule(null, rc.head);
        rc = rc.tail;
        x.rule = nr;
        b.root = b.root.applyMatch(x);
      });

      Simulator.matchMarkDelete(m);

      return b;
    }
  }

  def arithcal(root: Term): Term = {
    var ret: Term = root
    if (GlobalCfg.valueOperation) {
      val valopcal = new ValueOpCtrl(root)
      if (valopcal.arithCalculate()) { // 进行计算
        ret = valopcal.retTerm // 获得计算结果
      }
    }
    ret
  }

  def applyMatch(m: Match): Bigraph = {
    if (m == null) return this;
    //  logger.debug("Bigraph.applyMatch, m: " + m)
    if (!m.isWide) {
      //logger.debug("\t m is not wide!")
      //var b: Bigraph = new Bigraph();//lry
      var b: Bigraph = new Bigraph; //lry
      //println("new b="+b.root+b.rules);
      b.root = root.applyMatch(m);  //lry 已改
      var rr=Set[ReactionRule]()
      for(rule<-rules){
        var r=rule.clone()
        rr+= r
      }
      b.root = arithcal(b.root.clone)     // add by kgq 20220318  增加对算术节点的计算和更新
      b.inner = inner;
      b.outer = outer;
      b.rules = rules;
      b.trackings = trackings;
      b.bindings = bindings;
      b.placeSortConstraints = placeSortConstraints;
      b.placeSorts = placeSorts;
      b.linkSorts = linkSorts;
      b.linkSortConstraints = linkSortConstraints;
      b.pattern = pattern;
      b.needCheckCTL = needCheckCTL
      // ctlCheck()
      Simulator.matchMarkDelete(m);
      return b;
    } else {
      logger.debug("\tm. is wide!")
      if (m.rule.reactum.termType != TermType.TREGION) {
        logger.debug("bigraph::applyMatch Wide redexes must have wide reactums");
        sys.exit(1);
      }

      var reactum: Regions = m.rule.reactum.asInstanceOf[Regions];
      var wm: WideMatch = m.asInstanceOf[WideMatch];

      var b: Bigraph = new Bigraph();
      b.inner = inner;
      b.outer = outer;
      b.rules = rules;
      b.root = root;
      b.trackings = trackings;
      b.bindings = bindings;
      b.placeSortConstraints = placeSortConstraints;
      b.placeSorts = placeSorts;
      b.linkSorts = linkSorts;
      b.linkSortConstraints = linkSortConstraints;
      b.pattern = pattern;
      //ctlCheck()
      var mq: List[Match] = wm.submatches;
      var rc: List[Term] = List(); //reactum.getChildren;
      logger.debug("before before5=" + BiNode.allBiNodes); //lry
      // 对于反应规则中包含nil根节点的, 如 A.B||nil -> A || B匹配处理, C++版本处理会出错,
      // 这里进行了修正
      var rRedex: List[Term] = m.rule.redex.asInstanceOf[Regions].getChildren;
      var rcRaw: List[Term] = reactum.getChildren;
      logger.debug("before before6=" + BiNode.allBiNodes); //lry
      rRedex.map(x => {
        if (x.termType == TermType.TNIL) {
          // A.B||nil -> A||B 针对nil的处理方法：nil的生成物B放到反应物A中进行一块儿处理，在模型中，转化成A|B
          var last: Term = rc.last;
          rc = rc.init;
          rc = rc.:+(new Paraller(last.id, last, rcRaw.head))
        } else {
          rc = rc.:+(rcRaw.head);
        }
        rcRaw = rcRaw.tail;
      });
      logger.debug("before before7=" + BiNode.allBiNodes); //lry
      // 对nil修正后，反应物子项数目应与子match数目一致
      if (rc.size != wm.submatches.size) {
        logger.debug("bigraph::applyMatch Wide rules must match in the number of regions in the redex and reactum");
        sys.exit(1);
      }

      mq.map(x => {
        var nr: ReactionRule = new ReactionRule(null, rc.head);
        rc = rc.tail;
        x.rule = nr;
        b.root = b.root.applyMatch(x);
      });

      Simulator.matchMarkDelete(m);

      return b;
    }
  }

  //  def ctlCheck(): Unit = {
  //    if(GlobalCfg.needCtlCheck){
  //      GlobalCfg.formulas.foreach(formula => {
  //        GlobalCfg.ctlModelChecker.satisfies(formula)
  //      })
  //    }
  //  }
  //  def processMatch(m: Match): Unit = {
  //    var oldRule = m.rule
  //    var oldStr = oldRule.reactum.toString
  //    var oldR = oldStr.substring(1, oldStr.size - 1)
  //    var oldIndex = oldR.indexOf(":")
  //    var h = oldR.substring(0, oldIndex)
  //    var nx = Math.abs(Random.nextInt())
  //    var newR = h + nx + oldR.substring(oldIndex)
  //    var newArr = rules.toArray
  //    var size = newArr.size
  //    var index = Math.abs(Random.nextInt()) % size
  //    var name = newArr(index).name
  //    var newRedum = TermParser.apply(newR)
  //    var r = new ReactionRule(name, oldRule.reactum, newRedum, oldRule.express)
  //    rules.add(r)
  //  }

  override def toString = {
    val s: StringBuffer = new StringBuffer();
    s.append("Bigraph:\n");
    s.append("nameMap:" + Bigraph.nameMap.toList + "\n");
    s.append("modelNames:" + Bigraph.modelNames + "\n");
    s.append("controlMap:" + Bigraph.controlMap.toList + "\n");
    s.append("bindingSet:" + Bigraph.bindingSet + "\n");
    //s.append("nRegions:" + nRegions + "\n");
    //s.append("nHoles:" + nHoles + "\n");
    s.append("inner name:" + inner + "\n");
    s.append("outer name" + outer + "\n");
    s.append("Rules:\n");
    for (rule <- rules if rule != null) {
      s.append("\t\t" + rule + "\n");
    }
    s.append("\tModel:\n\t\t" + root + "\n");
    s.append("IsStart:" + isInitial + "\n");
    s.append("IsFinal:" + isFinal + "\n");
    s.append("label:" + label + "\n");
    s.append("isNegative:" + isNegative + "\n")
    s.append("VerifyID" + verifyID + "\n")
    s.toString();
  }
}

object testBigraph {
  def main(args: Array[String]) {

    // testControlFromString
    println("controlMap:" + Bigraph.controlMap.toList);

    // testNameFromString
    println(Bigraph.nameFromString("tanch", "innername"));
    println(Bigraph.nameFromString("zhaoxin", "innername"));
    println(Bigraph.nameFromString("chenjing", "innername"));
    println(Bigraph.nameFromString("lijingchen", "innername"));

    println("nameMap:" + Bigraph.nameMap.toList);

    // testApplyMatch
    val p = BGMParser.parse(new File("Examples/Airport_513/models/Smart.bgm"));
    println(p)
    val b: Bigraph = BGMTerm.toBigraph(p);
    println(b);
    //    var mm:Set[Match]=b.findMatches();
    /**
     * 测试用例1
     * r = "%rule Acquire_a_left_fork P[lf,p,rf] || F[lf] -> P[lf,p,rf].F[lf] || Q[lf];"
     * b.root = "F[F1] | P[F1,P1,F2] | F[F2] | P[F2,P2,F3] | F[F3] | P[F3,P3,F4] | F[F4] |
     * P[F4,P4,F5] | F[F5] | P[F5,P5,F1]"
     *
     * 这里假设P[F1,P1,F2]与P[lf,p,rf]、F[F1]与F[lf] 匹配上，进行反应，检验与预期结果是否一致。
     */
    var r: ReactionRule = b.rules.filter(_.name match {
      case "Acquire_a_left_fork" => true;
      case _ => false;
    }).head;
    var m1: Match = new Match(r);
    var matchLeftTerm1: Term = b.root.asInstanceOf[Paraller].rightTerm.asInstanceOf[Paraller].leftTerm;
    var redexMatchTerm1: Term = r.redex.asInstanceOf[Regions].leftTerm;
    m1.root = matchLeftTerm1;
    m1.mapping += (matchLeftTerm1 -> redexMatchTerm1);
    m1.mapping += (matchLeftTerm1.asInstanceOf[Prefix].suffix -> redexMatchTerm1.asInstanceOf[Prefix].suffix);
    m1.hasSucceeded = true;
    m1.hasFailed = false;

    var m2: Match = new Match(r);
    var matchLeftTerm2: Term = b.root.asInstanceOf[Paraller].leftTerm;
    var redexMatchTerm2: Term = r.redex.asInstanceOf[Regions].rightTerm;
    m2.root = matchLeftTerm2;
    m2.mapping += (matchLeftTerm2 -> redexMatchTerm2);
    m2.mapping += (matchLeftTerm2.asInstanceOf[Prefix].suffix -> redexMatchTerm2.asInstanceOf[Prefix].suffix);
    m2.mapping += (matchLeftTerm1 -> redexMatchTerm1);
    m2.mapping += (matchLeftTerm1.asInstanceOf[Prefix].suffix -> redexMatchTerm1.asInstanceOf[Prefix].suffix);

    m2.hasSucceeded = true;
    m2.hasFailed = false;

    var m: WideMatch = new WideMatch(r);
    //m.names += (Bigraph.nameMap("lf") -> Bigraph.nameMap("F1"));
    //m.names += (Bigraph.nameMap("p") -> Bigraph.nameMap("P1"));
    //m.names += (Bigraph.nameMap("rf") -> Bigraph.nameMap("F2"));
    m.addSubMatch(m1);
    m.addSubMatch(m2);

    println("match, " + m);

    var b2: Bigraph = b.applyMatch(m);
    println("The original Node is:           " + b.root);
    println("After applyMatch, Node is:" + b2.root);

  }
}