package org.bigraph.bigsim.value

import org.bigraph.bigsim.parser.{ArithExpressionParser, BGMParser, BGMTerm, Cond}
import org.bigraph.bigsim.utils.GlobalCfg
import org.bigraph.bigsim.BRS.Match
import org.bigraph.bigsim.model.{Bigraph, Node, Paraller, Prefix, ReactionRule, Term, TermType}
import org.bigraph.bigsim.simulator.{EnumSimulator, Simulator}

import scala.io.Source
import scala.collection.mutable.Map
import scala.collection.mutable.{Queue, Set}


/**
 * 这个类用来完成反应规则 和 赋值语句的计算
 * 为了 和 主干功能解耦合，因此把主要的功能尽量都封装在这个类里面。
 * @param rr
 *
 * @author kongguanqiao
 */
class Value(rr: ReactionRule) {

  var cache: Map[String, Double] = Map();     // 这个cache 是用来存放计算反应条件时候的节点数值，只允许查找，新增。不允许修改。
  var assignCache: Map[String, Double] = Map();   // 这个cache是用来存放 计算赋值语句时的节点数值，允许 查找， 更新， 修改。

  var DEBUG: Boolean = false;

  /**
   * 这个findNode 实现的功能是这样的，对于输入的每个名字names，以及当前查找的下标idx，
   * 在curTerm中进行递归查找，找到一条匹配上names的路径，也就能定位到某个term
   * 值得注意的是，这里查找的curTerm是反应规则中的redex，然后再通过Match中的 mapping 来获取
   * @param names  这个是已经切分好的名字 SC.Account.ID -> ["SC", "Account", "ID"]
   * @param idx
   * @param curTerm
   * @return
   */
  def _findNode(names: Array[String], idx: Int, curTerm: Term): Set[Term] = {
    val curName = names(idx)  // 取出当前查询的名字 or 控制
    var candidate: Set[Term] = Set()

    if (curTerm.termType == TermType.TPAR) {
      var paraResult: Set[Term] = curTerm.asInstanceOf[Paraller].getChildren;
      paraResult.map(it => {
        val res = _findNode(names, idx, it)    // 递归查找子节点的
        candidate ++= res
      })
    } else if (curTerm.termType == TermType.TPREF) {
      if (curTerm.asInstanceOf[Prefix].node.ctrl.name == curName || curTerm.asInstanceOf[Prefix].node.name == curName) {  // 如果是个前缀，并且当前节点的控制或者名字匹配当前的查询，那么就继续向下查找
        if (idx >= names.size - 1) {
          candidate += curTerm.asInstanceOf[Prefix]
        } else if (curTerm.asInstanceOf[Prefix].suffix.termType != TermType.THOLE && curTerm.asInstanceOf[Prefix].suffix.termType != TermType.TNIL){  // 此时query names没到头，后缀也没空
          val res = _findNode(names, idx + 1, curTerm.asInstanceOf[Prefix].suffix)
          candidate ++= res
        }
      }
    }
    candidate
  }

  /**
   * 先查找cache，
   * 然后切分名字，在反应规则的redex中查找，如果没有找到，或者找到多余一个结果，那么返回null
   * 查找成功，则
   * @param name
   * @return
   */
  def findNode(name: String): Term = {
    var ret: Term = null
    if (cache.contains(name)) {
      ret = cache.getOrElse(name, null).asInstanceOf[Term]
    } else {
      val names = name.split('.')
      val findRes = _findNode(names, 0, rr.redex)
      if (findRes.nonEmpty)
        findRes.foreach(x => ret = x)
    }
    ret
  }

  def getValue(name: String, m: Match): Double = {
    var ret = 0
    val cterm = findNode(name)

    if (DEBUG) {
      println("node name is: " + name)
      println("node term is: " + cterm)
    }

    val cnode = cterm.asInstanceOf[Prefix].node

    if (m.nodeMap.contains(cnode)) {
      val tnode = m.nodeMap.getOrElse(cnode, null)
      if (tnode.number != null) {
        try {
          if (DEBUG)
            println("\t node: " + name + " Value is: " + tnode.number.asInstanceOf[Double])
          tnode.number.asInstanceOf[Double]
        } catch {
          case e: ClassCastException => {
            if (DEBUG)
              println("\t node: " + name + " Value is: " + tnode.number.asInstanceOf[Int].toDouble)
            tnode.number.asInstanceOf[Int].toDouble
          }
        }
      } else {
        if (DEBUG)
          println("\t node: " + name + " Value is: " + ret)
        ret
      }
    } else {
      if (DEBUG)
        println("\t node: " + name + " Value is: " + ret)
      ret
    }
  }

  def checkConditions(m: Match): Boolean = {
    var ret: Boolean = true
    val conditions = rr.conditions
    // print("checkConditions")
    for(cond <- conditions if ret) {
      val condRes = cond.calculate(getValue(_, m))    // kgq 计算每一条
      if (DEBUG)
        print("\t cond: " + cond + " condRes: " + condRes)
      ret = ret && condRes
    }
    if (DEBUG)
      println("\t ret is: " + ret)
    ret
  }

  // checkValueConditions会检查节点中的value是否一样
  def checkValueConditions(m: Match): Boolean = {
    for(node<-m.nodeMap){
      if(node._1.number!=node._2.number)
        return false
    }
    true
  }

  def getValueAssign(name: String, m: Match): Double = {
    var ret: Double = 0
    if (assignCache.contains(name)) {         // 如果cache 能够查到值，那么直接返回cache的值
      ret = assignCache.getOrElse(name, 0)
    } else {
      val cterm = findNode(name)                // 否则，就从redex中查找
      val cnode = cterm.asInstanceOf[Prefix].node     // 找到的节点是 rr.redex的节点

      if (m.nodeMap.contains(cnode)) {            // 需要映射到agent中的节点才能取到数值
        val tnode = m.nodeMap.getOrElse(cnode, null)
        if (tnode.number != null) {
          try {
            ret = tnode.number.asInstanceOf[Double]
          } catch {
            case e: ClassCastException =>  ret = tnode.number.asInstanceOf[Int].toDouble
          }
        }
      }
      // assignCache += (name -> ret)            // 取值不记录，assignCache只记录在 赋值的过程中的左值
    }
    ret
  }

  /**
   * 在 生成物中， 查找 名字对应的 Node
   * @param name，字符串表示的节点 例如 Container.Count
   * @return 返回 具体偶图中 具体节点Node  c
   */
  def findNodeReactum(name: String): Node = {
    val names = name.split('.')
    val findRes = _findNode(names, 0, rr.reactum)
    var tterm: Term = null
    if (findRes.size == 1)
      findRes.foreach(x => tterm = x)
    tterm.asInstanceOf[Prefix].node
  }

  def calAssignments(m: Match): Map[String, Double] = {
    val assignments = rr.assignments
    assignments.foreach(assign => {
      val left = assign._1        // kgq 被赋值的对象
      val exp = assign._2         // kgq 右侧的表达式
      val expRes = exp.calculate(getValueAssign(_, m))    // kgq 表达式计算数值
      assignCache += (left -> expRes)     // 把计算的结果先放到expRes中存起来
    })
    assignCache.foreach(t => m.assignValue += (findNodeReactum(t._1) -> t._2))    // 将assignCache中的每个 key，替换成对应的 Node, 这里直接修改m中的 assignCache字段，以便在applyMatch时修改
    assignCache
  }
}

object testValue {

  def main(args: Array[String]) {
//    val txt = "SC.Account.ID"
//    val tst = txt.split('.')
//    tst.foreach(println(_))
//    println(tst)
//    println(tst(2))
//    var test: Set[String] = Set()
//    test += "gan"
//    println(test)
//    test ++= Set("cao")
//    println(test)
//    println(tst(1)=="Account")
//
//    def method(f:(String)=>Int) = {
//      f("9")
//    }
//
//    val f1=(a:String) => a.toInt
//    val f2=(a:String, b:String) => a.toInt + b.toInt
//    println(method(f2(_, "10")))


    val inputModel1 =
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
        |%active Container : 0;
        |%active Number : 2;
        |%active Count : 1;
        |
        |# Rules
        |%rule r_rule1 a1:Container.(c:Count[idle] | n1:Number[idle,b:edge]) | a2:Container.n2:Number[idle,b:edge] -> a1:Container.(c:Count[a:edge] | n1:Number[a:edge,b:edge]) | a2:Container.n2:Number[a:edge,b:edge]{};
        |%rule r_rule2 a1:Container.(c:Count[a:edge] | n1:Number[a:edge,b:edge]) | a2:Container.n2:Number[a:edge,b:edge] -> a1:Container.(c:Count[a:edge] | n1:Number[a:edge,b:edge]) | a2:Container.n2:Number[a:edge,b:edge]{Condition:Container.Count>0	Assign:Container.Count=Container.Count-1,a1.Number=a1.Number+a2.Number};
        |
        |# Model
        |%agent a1:Container.(n1:Number<2>[idle,b:edge] | c:Count<2>[idle]) | a2:Container.n2:Number<3>[idle,b:edge] {};
        |
        |#SortingLogic
        |
        |# Go!
        |%check;
        |""".stripMargin

    val inputModel2 =
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
        |%active Container : 0;
        |%active Number : 1;
        |%active Count : 1;
        |
        |# Rules
        |%rule r_rule1 Container.(Count[idle] | Number[idle]) | Container.Number[idle] -> Container.Number[idle] | Count[idle] | Container.Number[idle]{};
        |# Model
        |%agent a1:Container.(n1:Number<2>[idle] | c:Count<1>[idle]) | a2:Container.n2:Number<3>[idle] {};
        |
        |#SortingLogic
        |
        |# Go!
        |%check;
        |""".stripMargin

    val inputModel3 =
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
        |%active A : 1;
        |%active B : 1;
        |%active C : 1;
        |
        |# Rules
        |%rule r_test1 a:A[idle] | b:B[idle] -> a:A[a:edge] | b:B[a:edge]{};
        |%rule r_test2 b:B[idle] | c:C[idle] -> b:B[b:edge] | c:C[b:edge]{};
        |
        |# Model
        |%agent a:A[idle] | b:B[idle] | c:C[idle];
        |
        |#SortingLogic
        |
        |# Go!
        |%check;
        |""".stripMargin

    val bug3test =
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
        |%active Container : 0;
        |%active Number : 1;
        |%active Count : 1;
        |
        |# Rules
        |%rule r_DEBUG31 a1:Container.(c:Count[idle] | n1:Number[idle]) | a2:Container.n2:Number[idle] -> a1:Container.(c:Count[b:edge] | n1:Number[b:edge]) | a2:Container.n2:Number[idle]{};
        |%rule r_DEBUG32 a1:Container.(c:Count[idle] | n1:Number[idle]) | a2:Container.n2:Number[idle] -> a1:Container.(c:Count[idle] | n1:Number[a:edge]) | a2:Container.n2:Number[a:edge]{};
        |
        |# Model
        |%agent a1:Container.(n1:Number[idle] | c:Count[idle]) | a2:Container.n2:Number[idle] {};
        |
        |#SortingLogic
        |
        |# Go!
        |%check;
        |""".stripMargin
//    val t = BGMParser.parseFromString(bug3test)
    val t = BGMParser.parseFromString(inputModel1)    // 解析
    val b = BGMTerm.toBigraph(t)                      // 构造


    println("********* Bigraph.modelNames is: " )
    println(Bigraph.modelNames)
    println("********* The End ****************")
    val sim: Simulator = new EnumSimulator(b._2)
    sim.simulate

//    val matches: Set[Match] = b.findMatches           // 查找匹配
//    println(matches)
//    println(matches.head)
//
//    val nb = b.applyMatch(matches.head)
//    println(nb)
//    val matches2: Set[Match] = nb.findMatches
//    println("this new matche")
//    println(matches2)
//    println("the rule of this match is:~!!!@!#@#$%@#!%#!T")
//    println(matches2.head.rule)
//    val tmpv = new Value(matches2.head.rule)
//    val condres = tmpv.checkConditions(matches2.head)
//    println("condres: " + condres)
//    val assigncond = tmpv.calAssignments(matches2.head)
//    println("assignCache: " + tmpv.assignCache)
//    println("assigncond: " + assigncond)
//
//    println("======================")
//    println(matches2.head.rule)
//    println(nb.rules.head)
//    println(matches2.head.rule == nb.rules.head)
//
//    println("============= 第二次 applymatch ==========")
//    val nnb = nb.applyMatch(matches2.head)
//    println(nnb)
//    val testSet: Set[Double] = Set(1.0, 2.0, 3.0, 4.0)
//    var label: Boolean = false
//    println("testSet")
//    for(n <- testSet if !label) {
//      println("\tcurn: " + n)
//      if (n > 2.5)
//        label = true
//    }
//    val tmp: Set[Double] = Set()
//    testSet ++= tmp
//
//    val testMap: Map[String, Double] = Map();
//    val key = "gan"
//    var value: Double = 0
//    if (testMap.contains(key)){
//      value = testMap.getOrElse(key, 0)
//      println(value)
//    }
//    testMap += ("gan" -> 1)
//    if (testMap.contains(key)){
//      value = testMap.getOrElse(key, 0)
//      println(value)
//    }
//    testMap += ("gan" -> 3)
//    testMap += ("cao" -> 2)
//    println(testMap)


//    Data.parseData("Examples/MobileCloud/data/checker.txt")
//    Data.data.foreach(f => println(f._2.name + " " + f._2.ratio + " " + f._2.percentage))
//    Data.addWeightExpr("(fee+energy*15+(0.7*5))")
//    println(Data.getReport)

//    val testQueue: Queue[Double] = Queue();
//    testQueue += 9
//    testQueue ++= List(3,4,5)
//    println(testQueue)
//
//    for (n <- testQueue) {
//      println(n)
//    }
//    val test2: Queue[Double] = Queue();
//    test2 ++= testQueue
//    println(test2)
  }
}