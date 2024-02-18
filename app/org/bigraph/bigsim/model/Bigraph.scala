package org.bigraph.bigsim.model

import java.io.File
import java.util
import java.util.{Collections, Comparator}

import org.bigraph.bigsim.BRS._
import org.bigraph.bigsim.data.DataModel
import org.bigraph.bigsim.exceptions.{IncompatibleInterfaceException, IncompatibleSignatureException}
import org.bigraph.bigsim.model.component._
import org.bigraph.bigsim.parser.{BGMParser, BGMTerm}
import org.bigraph.bigsim.simulator.Simulator
import org.bigraph.bigsim.utils.{BigraphToTerm, CachingProxy, DebugPrinter, GlobalCfg}
import org.bigraph.bigsim.value.ValueOpCtrl
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.collection.mutable.{Map, Set}

object Node {
  private var id: Int = 0

  def idIncrement: Int = {
    id += 1
    id
  }

  /// dirty code todo: 删去对Node的依赖
  def nodeAdapter(p: component.Node): Node = {
    val ctrl = new Control(p.getControl.getName, p.getControl.getArity)
    val ports = p.getPorts
    var ss: List[Name] = List()
    for (port <- ports) {
      val handle = port.getHandle
      var nameType = ""
      var handleName = ""
      var innerNames: List[String] = List()
      if (handle == null) nameType = "idle"
      else {
        handleName = handle.getName
        for (point <- handle.getPoints) {
          if (point.isInnerName) innerNames = innerNames :+ point.asInstanceOf[InnerName].getName
        }
        if (handle.isOuterName) nameType = "outername"
        else if (handle.isEdge) nameType = "edge"
      }
      ss = ss :+ (new Name(handleName, nameType, innerNames))
    }
    new Node(p.getName, p.getControl.isActive, ss, ctrl)
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
    newNode.id = id
    newNode
  }

  def checkIsAnonymous(): Boolean = {
    return name == ""
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
    this(n, ar, act);
    binding = b;
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

  override def toString = "Name:(" + name + "," + id + "," + nameType + "," + getInnerNames + ")"; //打印ports

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
  def logger: Logger = LoggerFactory.getLogger(this.getClass)

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

  def makeId(signature: Signature, width: Integer, names: util.ArrayList[String]): Bigraph = {
    val builder: BigraphBuilder = new BigraphBuilder(signature)
    for (_ <- 0 until width) {
      builder.addSite(builder.addRoot());
    }
    for (name <- names) {
      builder.addInnerName(name, builder.addOuterName(name))
    }
    builder.getBigraph
  }

  def makeEmpty(signature: Signature): Bigraph = {
    val bigraph = new Bigraph()
    bigraph.setSignature(signature)
    bigraph
  }

  def juxtapose(lhs: Bigraph, rhs: Bigraph): Bigraph = {
    if (lhs == rhs)
      throw new IllegalArgumentException("A bigraph can not juxtapose with itself.")
    if (!(lhs.bigSignature == rhs.bigSignature)) throw new IncompatibleSignatureException(lhs.bigSignature, rhs.bigSignature)
    if (!Collections.disjoint(lhs.bigInner.keySet, rhs.bigInner.keySet))
      throw new IncompatibleInterfaceException(new UnsupportedOperationException("joint name juxtapose: " + lhs.bigInner.keySet() + " and " + rhs.bigInner.keySet()))
    if (!Collections.disjoint(lhs.bigOuter.keySet, rhs.bigOuter.keySet))
      throw new IncompatibleInterfaceException(new UnsupportedOperationException("joint name juxtapose: " + lhs.bigOuter.keySet() + " and " + rhs.bigOuter.keySet()))
    val l: Bigraph = lhs.clone()
    val r: Bigraph = rhs.clone()
    val edges = r.getEdges
    l.onEdgeAdded(edges)
    l.onNodeAdded(r.getNodes)
    r.onEdgeSetChanged()
    r.onNodeSetChanged()
    l.bigRoots.addAll(r.bigRoots)
    l.bigSites.addAll(r.bigSites)
    l.bigOuter.putAll(r.bigOuter)
    l.bigInner.putAll(r.bigInner)
    if (!l.isConsistent) {
      throw new RuntimeException("Inconsistent bigraph")
    }
    l
  }
}

class Bigraph(roots: Int = 1) extends BigraphHandler {

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

  def addLTLSpec(spec: String): Unit = {
    ltlSpec = ltlSpec.:+(spec)
  }

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
      b.root = root.applyMatch(m); //lry 已改
      var rr = Set[ReactionRule]()
      for (rule <- rules) {
        var r = rule.clone()
        rr += r
      }
      b.root = arithcal(b.root.clone) // add by kgq 20220318  增加对算术节点的计算和更新
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

  /*
    目前项目很多部分不知道是否还在使用，在此重构
    同时能够更加清晰地表示偶图结构 org.bigraph.bigsim.model.component
    另一个目的是方便 偶图匹配规约成SAT问题 使用相关API
   */
  var bigSignature: Signature = new Signature(new util.ArrayList[component.Control]())
  final var bigRoots: util.List[Root] = new util.ArrayList[Root]()
  final var bigSites: util.List[Site] = new util.ArrayList[Site]()
  final var bigOuter: util.Map[String, OuterName] = new util.HashMap[String, OuterName]()
  final var bigInner: util.Map[String, InnerName] = new util.HashMap[String, InnerName]()

  private var ancestors: util.Map[Child, util.Collection[Parent]] = new util.WeakHashMap[Child, util.Collection[Parent]]()

  private final val roRoots: util.List[Root] = bigRoots
  private final val roSites: util.List[Site] = bigSites

  def setSignature(signature: Signature): Unit = {
    bigSignature = signature
  }

  def getSignature(): Signature = {
    bigSignature
  }

  override def isEmpty: Boolean = {
    bigRoots.isEmpty && bigSites.isEmpty && bigOuter.isEmpty && bigInner.isEmpty
  }

  override def isGround: Boolean = {
    bigSites.isEmpty && bigInner.isEmpty
  }

  override def getRoots: util.List[_ <: Root] = {
    roRoots
  }

  override def getSites: util.List[_ <: Site] = {
    roSites
  }

  override def getOuterNames: util.Collection[_ <: OuterName] = {
    bigOuter.values()
  }

  override def getInnerNames: util.Collection[_ <: InnerName] = {
    bigInner.values()
  }

  final val EMPTY_ANCS_LST: util.Collection[Parent] = util.Collections.unmodifiableList(util.Collections.emptyList())

  def getAncestors(child: Child): util.Collection[Parent] = {
    if (child == null)
      throw new IllegalArgumentException("the argument can't be null")
    var s = ancestors.get(child)
    if (s == null) {
      val parent = child.getParent
      if (parent.isRoot) {
        s = EMPTY_ANCS_LST
      } else {
        s = new util.LinkedList[Parent](getAncestors(parent.asInstanceOf[Child]))
        s.add(parent)
      }
      ancestors.put(child, s)
    }
    s
  }

  var edgesProxy: CachingProxy[util.Collection[Edge]] = new CachingProxy[util.Collection[Edge]](
    () => provideEdges
  )

  def provideEdges: util.Collection[Edge] = {
    val nodes = getNodes
    val s: util.Set[Edge] = new util.HashSet[Edge]()
    for (n <- nodes) {
      val ports = n.getPorts
      for (port <- ports) {
        val handle = port.getHandle
        if (handle != null && handle.isEdge) s.add(handle.asInstanceOf[Edge])
      }
    }
    for ((_, v) <- bigInner) {
      val handle = v.getHandle
      if (handle != null && handle.isEdge) s.add(handle.asInstanceOf[Edge])
    }
    s
  }

  def onEdgeAdded(edge: Edge): Unit = {
    val edges = edgesProxy.softGet()
    if (edges != null) edges.add(edge)
  }

  def onEdgeAdded(edges: util.Collection[Edge]): Unit = {
    val es = edgesProxy.softGet()
    if (es != null) es.addAll(edges)
  }

  def onEdgeRemoved(edge: Edge): Unit = {
    val edges = edgesProxy.softGet()
    if (edges != null) edges.remove(edge)
  }

  def onEdgeRemoved(edges: util.Collection[Edge]): Unit = {
    val es = edgesProxy.softGet()
    if (es != null) es.removeAll(edges)
  }

  def onEdgeSetChanged(): Unit = {
    nodesProxy.invalidate()
  }

  override def getEdges: util.Collection[Edge] = {
    edgesProxy.get()
  }

  // bigraph的所有nodes，通过softReference缓存
  var nodesProxy: CachingProxy[util.Collection[component.Node]] = new CachingProxy[util.Collection[component.Node]](
    () => provideNodes
  )

  override def getNodes: util.Collection[component.Node] = {
    nodesProxy.get()
  }

  def provideNodes: util.Collection[component.Node] = {
    val s: util.Set[component.Node] = new util.HashSet[component.Node]()
    val q: util.Queue[component.Node] = new util.LinkedList[component.Node]()
    for (r <- bigRoots) {
      for (c <- r.getChildren) {
        if (c.isNode) q.add(c.asInstanceOf[component.Node])
      }
    }
    while (q.nonEmpty) {
      val node: component.Node = q.poll()
      s.add(node)
      for (c <- node.getChildren) {
        if (c.isNode) q.add(c.asInstanceOf[component.Node])
      }
    }
    s
  }

  def onNodeAdded(node: component.Node): Unit = {
    val nodes = nodesProxy.softGet()
    if (nodes != null) nodes.add(node)
  }

  def onNodeAdded(nodes: util.Collection[component.Node]): Unit = {
    val ns = nodesProxy.softGet()
    if (ns != null) ns.addAll(nodes)
  }

  def onNodeSetChanged(): Unit = {
    this.nodesProxy.invalidate()
    this.ancestors.clear()
  }

  def onNodeRemoved(node: component.Node): Unit = {
    ancestors.clear() // conservative
    val nodes = nodesProxy.softGet()
    if (nodes != null) nodes.remove(node)
  }

  def onNodeRemoved(nodes: util.Collection[component.Node]): Unit = {
    ancestors.clear() //conservative
    val ns = nodesProxy.softGet()
    if (ns != null) ns.removeAll(nodes)
  }

  /// 按照名字排序所有内部名，外部名，节点，最后重命名所有边。主要用于hash计算，比较两个偶图是否相等
  def trimBigraph(): Unit = {
    val nodes = new util.ArrayList[component.Node](nodesProxy.get())
    Collections.sort(nodes, nodeComparator)
    val edges = new util.HashSet[Handle]()
    var i: Integer = 0
    for (node <- nodes) {
      for (port <- node.getPorts) {
        val handle = port.getHandle
        if (handle != null && handle.isEdge && !edges.contains(handle)) {
          edges.add(handle)
          handle.asInstanceOf[Edge].setName("E_" + i)
          i = i + 1
        }
      }
    }
  }

  private val nodeComparator = new Comparator[component.Node] {
    override def compare(o1: component.Node, o2: component.Node): Int = o1.getName.compareTo(o2.getName)
  }

  private val innerComparator = new Comparator[InnerName] {
    override def compare(o1: InnerName, o2: InnerName): Int = o1.getName.compareTo(o2.getName)
  }

  private val outerComparator = new Comparator[OuterName] {
    override def compare(o1: OuterName, o2: OuterName): Int = o1.getName.compareTo(o2.getName)
  }

  /// check whether the bigraph is consistent
  override def isConsistent: Boolean = {
    val seenPoint: util.HashSet[Point] = new util.HashSet[Point]()
    val seenHandles: util.HashSet[Handle] = new util.HashSet[Handle]()
    val unseenSites: util.HashSet[Site] = new util.HashSet[Site]()
    unseenSites.addAll(getSites)
    val seenChildren: util.HashSet[Child] = new util.HashSet[Child]()
    val q = new util.ArrayDeque[Parent]()
    for (r <- bigRoots) {
      q.add(r)
    }
    while (q.nonEmpty) {
      val parent = q.poll()
      for (c <- parent.getChildren) {
        // 父子节点不对应
        if (parent != c.getParent) {
          DebugPrinter.err(logger, "INCOSISTENCY: parent/child mismatch")
          return false
        }
        // child有多个父节点
        if (!seenChildren.add(c)) {
          DebugPrinter.err(logger, "INCOSISTENCY: shared place")
          return false
        }
        if (c.isNode) {
          val node = c.asInstanceOf[component.Node]
          // control定义的arity与实际不一致
          if (node.getControl.getArity != node.getPorts.size() || !bigSignature.contains(node.getControl)) {
            DebugPrinter.err(logger, "INCOSISTENCY: control/arity")
            return false
          }
          q.add(node)
          for (port <- node.getPorts) {
            val handle = port.getHandle
            // 空悬的port
            //            if (handle == null) {
            //              DebugPrinter.err(logger, "INCOSISTENCY: broken or foreign handle")
            //              return false
            //            }
            // handle没有对应该port
            if (handle != null && !handle.getPoints.contains(port)) {
              DebugPrinter.err(logger, "INCOSISTENCY: handle/point mismatch")
              return false
            }
            seenPoint.add(port)
            if (handle != null) seenHandles.add(handle)
          }
        } else if (c.isSite) {
          val site = c.asInstanceOf[Site]
          unseenSites.remove(site)
          if (!bigSites.contains(site)) {
            DebugPrinter.err(logger, "INCOSISTENCY: foreign site")
            return false
          }
        } else {
          DebugPrinter.err(logger, "INCOSISTENCY: neither a node nor a site")
          return false
        }
      }
    }
    for (outerName <- bigOuter.values()) {
      seenHandles.add(outerName)
    }
    for (innerName <- bigInner.values()) {
      seenHandles.add(innerName.getHandle)
      seenPoint.add(innerName)
    }
    for (handle <- seenHandles) {
      for (point <- handle.getPoints) {
        // node看见的所有point与handle看见的point不对应（handle看见得更多）
        if (!seenPoint.remove(point)) {
          DebugPrinter.err(logger, "INCOSISTENCY: foreign point: " + point)
          return false
        }
      }
    }
    // node看见的所有point与handle看见的point不对应（node看见得更多）
    //    if (seenPoint.size() > 0) {
    //      DebugPrinter.err(logger, "INCOSISTENCY: handle chain broken " + seenPoint)
    //      return false
    //    }
    if (unseenSites.size() > 0) {
      DebugPrinter.err(logger, "INCOSISTENCY: unreachable site")
      return false
    }
    true
  }

  @Override
  override def clone(): Bigraph = {
    var big: Bigraph = new Bigraph()
    big.setSignature(this.bigSignature)
    val hndDict: util.Map[Handle, Handle] = new util.HashMap[Handle, Handle]()
    for ((k, ori) <- bigOuter) {
      val copy = ori.replicate()
      big.bigOuter.put(k, copy)
      hndDict.put(ori, copy)
    }
    for ((k, ori) <- bigInner) {
      val copy = ori.replicate()
      big.bigInner.put(k, copy)
      val h0 = ori.getHandle
      var h1 = hndDict.get(h0)
      if (h1 == null) {
        h1 = h0.replicate()
        hndDict.put(h0, h1)
      }
      copy.setHandle(h1)
    }
    var q: util.Queue[(Parent, Child)] = new util.LinkedList[(Parent, Child)]()
    for (r <- bigRoots) {
      val copy = r.replicate()
      big.bigRoots.add(copy)
      for (c <- r.getChildren) {
        q.add(new Tuple2[Parent, Child](copy, c))
      }
    }
    val sites: util.List[Site] = new util.ArrayList[Site]()
    for (_ <- 0 until this.bigSites.size()) {
      sites.add(new Site())
    }
    while (!q.isEmpty) {
      val t = q.poll()
      if (t._2.isNode) {
        val n1: component.Node = t._2.asInstanceOf[component.Node]
        val n2: component.Node = n1.replicate
        n2.setParent(t._1)
        // node每个端口的拷贝
        for (i <- 0 until n1.getPorts.size()) {
          val p1 = n1.getPort(i)
          val h1 = p1.getHandle
          var h2 = hndDict.get(h1)
          if (h2 == null) {
            h2 = h1.replicate()
            hndDict.put(h1, h2)
          }
          n2.getPort(i).setHandle(h2)
        }
        // node每个孩子节点入队
        for (c <- n1.getChildren) {
          q.add((n2, c))
        }
      } else {
        val s1: Site = t._2.asInstanceOf[Site]
        val s2: Site = s1.replicate()
        s2.setParent(t._1)
        sites.set(this.bigSites.indexOf(s1), s2)
      }
    }
    big.bigSites.addAll(sites)
    big
  }

  def structToTerm(): Term = {
    if (root == null || root.toString.contains("idle")) root = BigraphToTerm.toTerm(this)
    root
  }

  def findAllMatchByCSP(): util.Set[(Bigraph, ReactionRule)] = {
    val result = new util.HashSet[(Bigraph, ReactionRule)]()
    for (r <- rules) {
      result.addAll(this.matchRule(r))
    }
    result
  }

  def matchRule(r: ReactionRule): util.Set[(Bigraph, ReactionRule)] = {
    val redex: Bigraph = r.redexBig
    val reactum: Bigraph = r.reactumBig
    val eta: InstantiationMap = r.eta
    DebugPrinter.print(logger, "- REACTUM -----------------------------")
    val matcher: CSPMatcher = new CSPMatcher()
    val iter = matcher.`match`(this, redex).iterator()
    val result = new util.HashSet[(Bigraph, ReactionRule)]()
    while (iter.hasNext) {
      val pMatch: CSPMatch = iter.next()
      val nb = Rewrite.rewrite(pMatch, redex, reactum, eta)
      nb.rules = rules
      DebugPrinter.print(logger, "new bigraph::")
      result.add((nb, r))
    }
    result
  }

  def matchAndRewrite(redex: Bigraph, reactum: Bigraph): util.Set[Bigraph] = {
    DebugPrinter.print(logger, "- REDEX -----------------------------")
    redex.print()
    DebugPrinter.print(logger, "- REACTUM -----------------------------")
    reactum.print()
    val matcher: CSPMatcher = new CSPMatcher()
    val iter = matcher.`match`(this, redex).iterator()
    val result = new util.HashSet[Bigraph]()
    while (iter.hasNext) {
      val pMatch: CSPMatch = iter.next()
      val nb = Rewrite.rewrite(pMatch, redex, reactum, InstantiationMap.getIdMap(reactum.bigSites.size()))
      nb.rules = rules
      DebugPrinter.print(logger, "new bigraph::")
      result.add(nb)
    }
    result
  }

  def print(): Unit = {
    DebugPrinter.print(logger, "Bigraph Info:")
    for (inner <- bigInner) DebugPrinter.print(logger, "Inner: " + inner)
    for (outer <- bigOuter) DebugPrinter.print(logger, "Outer: " + outer)
    for (site <- bigSites) DebugPrinter.print(logger, "Site: " + site)
    for (root <- bigRoots) DebugPrinter.print(logger, "Root: " + root)
    for (node <- nodesProxy.get()) DebugPrinter.print(logger, "Node: " + node)
    for (edge <- edgesProxy.get()) DebugPrinter.print(logger, "Edge: " + edge)
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