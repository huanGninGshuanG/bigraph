package org.bigraph.bigsim.value

import org.bigraph.bigsim.parser.{ArithExpressionParser, BGMParser, BGMTerm, Cond, TermParser}
import org.bigraph.bigsim.utils.GlobalCfg
import org.bigraph.bigsim.BRS.Match
import org.bigraph.bigsim.model.{Bigraph, Name, Nil, Node, Paraller, Prefix, ReactionRule, Regions, Term, TermType}
import org.bigraph.bigsim.simulator.{EnumSimulator, Simulator}

import scala.collection.mutable
import scala.io.Source
import scala.collection.mutable.Map
import scala.collection.mutable.{Queue, Set}

/**
 * 20220317
 * @author kongguanqiao
 */
object ValueOpCtrl {

  val OPlus: String = "Plus"
  val OMinus: String = "Minus"
  val OMulti: String = "Multiply"
  val ODivision: String = "Division"

  // 目前实现四种 二元运算符： + - * /
  val binaryOpCtrl: List[String] = List(OPlus, OMinus, OMulti, ODivision)

  val OAbs: String = "Abs"
  val ORever: String = "Reverse"

  // 目前实现两种 一元运算符： abs, -
  val unaryOpCtrl: List[String] = List(OAbs, ORever)

  def isArithOpCtrl(s: String): Boolean = {
    if (binaryOpCtrl.contains(s) || unaryOpCtrl.contains(s))
      true
    else
      false
  }

  def isArithOpCtrl(n: Node): Boolean = {
    isArithOpCtrl(n.ctrl.name)
  }
}

/**
 * 这个类用来实现数值计算控制的识别、计算、更新。
 * 以使用偶图“算术网”的方式来实现数值的更新。
 *
 * @param t
 */
class ValueOpCtrl(t: Term) {

  var term: Term = t
  var retTerm: Term = t                   // 计算之后的根有可能会变化，所以最后要取出retTerm

  // 记录 当前Term中的计算控件
  var opNode: Set[Prefix] = Set()
  var opEdge: Set[Name] = Set()

  // 记录 边到起点的映射 : 算术网要求，每条边最多一个Source点
  var nameToSource: Map[Name, Node] = Map()       // source 和 target的定义，详见 Robin 经典书籍中6.2中关于算术网的论述
  var nameToTarget: Map[Name, Set[Node]] = Map()  // 这个记录的是算术节点上的 t point，主要是在运算的时候，可以快速找到后继节点，更新后继节点的入度

  // 用于拓扑排序的数据结构： Node -> (InDegree, reached)
  var opMap: Map[Node, Tuple2[Int, Boolean]] = Map()

  // 更新节点列表
  var updateNode: Map[Node, Name] = Map()

  // 算术节点 到父Term映射， 用于计算之后对算术节点的删除
  var nameToNum: Map[Name, Int] = Map()       // 注意，这里只记录非算术节点连接的name及其数量

  var debug: Boolean = false                  // 调试的时候打印日志

  /**
   * 向 nameToTarget 中添加。因为一个 name 可能对应多个target，所以用 Set[Node] 来记录同一个name的多个target
   * @param n
   * @param node
   */
  def nameToTargetAdd(n: Name, node: Node): Unit ={
    var tSet: Set[Node] = nameToTarget.getOrElse(n, Set())
    tSet.add(node)
    nameToTarget += (n -> tSet)
  }

  /**
   * 递归查找，判断 t 中是不是存在运算控制。
   *      只有存在运算控制的时候，才进行下一步运算。
   * @param t
   * @return
   */
  private def hasArithCtrl(t: Term): Boolean = {
    var ret: Boolean = false
    if (t.termType == TermType.TREGION) {
      val tchild = t.asInstanceOf[Regions].getChildren
      tchild.foreach(x => if (!ret) ret = hasArithCtrl(x))    // 如果 ret 是false，才会查看x
    } else if (t.termType == TermType.TPAR) {
      val tchild = t.asInstanceOf[Paraller].getChildren
      tchild.foreach(x => if (!ret) ret = hasArithCtrl(x))
    } else if (t.termType == TermType.TPREF) {
      val tnode = t.asInstanceOf[Prefix].node
      if (ValueOpCtrl.isArithOpCtrl(tnode))              // 如果当前节点是一个 运算符Control，返回True
        ret =  true
      else
        ret = hasArithCtrl(t.asInstanceOf[Prefix].suffix)
    }
    // 还有两种： Nil 和 Num， 不做处理
    ret
  }

  /**
   * 向外提供的接口，用于判断term是否有算术节点
   * @return
   */
  def hasArithCtrl: Boolean = hasArithCtrl(term)

  /**
   * Step(1), 找出当前偶图中所有的算术节点，构造
   *    opNode: 所有包含算术节点的  Prefix
   *    opEdge: 所有与算术节点相连的边 Name
   *    nameToSource: 算术节点上的Source点 -> 只是nameToSource的一部分
   *    nameToTarget: 算术节点上的target点 -> nameToTarget的全部
   * @param t
   */
  private def findOpNode(t: Term): Unit = {
    if (t.termType == TermType.TREGION) {
      val tchild = t.asInstanceOf[Regions].getChildren
      tchild.foreach(x => findOpNode(x))
    } else if (t.termType == TermType.TPAR) {
      val tchild = t.asInstanceOf[Paraller].getChildren
      tchild.foreach(x => findOpNode(x))
    } else if (t.termType == TermType.TPREF) {
      val tnode = t.asInstanceOf[Prefix].node
      if (ValueOpCtrl.isArithOpCtrl(tnode.ctrl.name)) {         // 找到一个op Prefix
        opNode.add(t.asInstanceOf[Prefix])                        // construct opNode
        tnode.ports.foreach(x => opEdge.add(x))                   // construct opEdge
        if (ValueOpCtrl.unaryOpCtrl.contains(tnode.ctrl.name)) {   // if unary op
          nameToTargetAdd(tnode.ports(0), tnode)                        // add 1 t-point, 1 s-point
          nameToSource += (tnode.ports(1) -> tnode)
        } else {                                                   // else binary op
          nameToTargetAdd(tnode.ports(0), tnode)                        // add 2 t-point, 1 s-point
          nameToSource += (tnode.ports(1) -> tnode)
          nameToTargetAdd(tnode.ports(2), tnode)
        }
      }
      findOpNode(t.asInstanceOf[Prefix].suffix)
    }
    // 还有两种： Nil 和 Num， 不做处理
  }

  /**
   * Step(2) 找到和计算节点相关的普通节点，记录到对应数据结构中去，方便运算时查找及运算后更新:
   *    nameToSource: 普通节点上的Source点 -> nameToSource的另一部分
   *    updateNode:   普通节点上的target点 -> 用于计算完成之后，对于数值进行更新
   * 记录相应数据结构，方便完成运算后，删除算术节点：
   *    nameToNum: 记录普通节点上s-point, t-point在普通节点中出现的次数 -> 如果
   *                次数小于2，说明该point只连接了算术节点，因此删除掉算术节点之后，
   *                要置该name为idle （实际上，只需要考虑 t-point的情况就好，
   *                因为同一条边只能有一个 s-point）
   *    Term.directParent: 记录Term的直接父亲，保存到Term的 directParent字段中，
   *                方便后续计算完成后，删除算术Prefix。 详见delPrefix方法
   * @param t
   */
  private def buildCalMap(t: Term): Unit ={
    if (t.termType == TermType.TREGION) {
      val cur: Regions = t.asInstanceOf[Regions]
      cur.leftTerm.directParent = cur             //  construct directParent
      buildCalMap(cur.leftTerm)
      cur.rightTerm.directParent = cur
      buildCalMap(cur.rightTerm)
    } else if (t.termType == TermType.TPAR) {
      val cur: Paraller = t.asInstanceOf[Paraller]
      cur.leftTerm.directParent = cur
      buildCalMap(cur.leftTerm)
      cur.rightTerm.directParent = cur
      buildCalMap(cur.rightTerm)
    } else if (t.termType == TermType.TPREF) {
      val cur: Prefix = t.asInstanceOf[Prefix]
      val tnode = cur.node
      if (!ValueOpCtrl.isArithOpCtrl(tnode)) {     // if not arith node
        tnode.ports.foreach(x => {
          if (opEdge.contains(x)) {               // x is a s-point or t-point on ordinary node
            val tn: Int = nameToNum.getOrElse(x, 0)
            nameToNum += (x -> (tn + 1))          // construct nameToNum. Only s-point/t-point recorded
            if (nameToSource.contains(x)) {             // Check whether x connected to a s-point
              updateNode += (tnode -> x)                // So x is a t-point, record in updateNode
            } else {
              nameToSource += (x -> tnode)              // So x is a s-point, add to nameToSource
            }
          }
        })
      }
      cur.suffix.directParent = cur
      buildCalMap(cur.suffix)
    }
    // 还有两种： Nil 和 Num 不做处理
  }

  /**
   * 检查 某条边连接的source点是否为算术节点. 用于opMap中计算算术节点的入度
   *        -> 入度表示算术节点上的 t-point 连接的算术节点的个数
   * @param n
   * @return 如果n连接了算术节点的s-point，返回true, 反之，返回false
   */
  private def checkSource(n: Name): Boolean = {
    var ret = false
    if (nameToSource.contains(n)) {
      val tnode = nameToSource.getOrElse(n, null)
      if (ValueOpCtrl.isArithOpCtrl(tnode.ctrl.name))
        ret = true
    }
    ret
  }

  /**
   * 构造OpMap，计算所有算术节点的初始状态，以便后续的 [拓扑排序] 来执行计算
   *    opMap: 记录算术节点的入度，以及是否已经访问（执行计算）
   */
  private def initOpMap(): Unit = {
    if (opNode.nonEmpty){
      opNode.foreach(x => {
        if (ValueOpCtrl.binaryOpCtrl.contains(x.node.ctrl.name)) {    // binary op
          var inDegree = 2
          if (!checkSource(x.node.ports(0)))   // 如果Source点不是算术节点，那么入度减一
            inDegree -= 1
          if (!checkSource(x.node.ports(2)))
            inDegree -= 1
          opMap += (x.node -> Tuple2(inDegree, false))
        } else if (ValueOpCtrl.unaryOpCtrl.contains(x.node.ctrl.name)) {  // unary op
          var inDegree = 1
          if (!checkSource(x.node.ports(0)))
            inDegree -= 1
          opMap += (x.node -> Tuple2(inDegree, false))
        }
      })
    }
  }

  /**
   * 执行某个计算节点的运算，返回计算结果
   * @param n 某个计算节点
   * @return 该节点的计算结果
   */
  private def _arithCal(n: Node): Double = {
    var ret: Double = 0
    if (ValueOpCtrl.unaryOpCtrl.contains(n.ctrl.name)) {      // if unary op
      val prename = n.ports.head
      if (nameToSource.contains(prename)) {
        val prenode: Node = nameToSource.getOrElse(prename, null)   // get number of s-point
        val prenum: Double = prenode.number.asInstanceOf[Double]
        if (n.ctrl.name == ValueOpCtrl.OAbs)
          ret = Math.abs(prenum)
        else if (n.ctrl.name == ValueOpCtrl.ORever)
          ret = -1 * prenum
        else
          println("Unknown unary operator: " + n.ctrl.name)
      } else {
        println("Can't find the input of node: " + n)
      }
    } else if (ValueOpCtrl.binaryOpCtrl.contains(n.ctrl.name)) {    // binary op
      val prename1 = n.ports(2)                                   // Attention, first operand is ports(2), second operand is ports(0)
      val prename2 = n.ports.head
      if (nameToSource.contains(prename1) && nameToSource.contains(prename2)) {
        // 取出前驱节点
        val prenode1: Node = nameToSource.getOrElse(prename1, null)
        val prenode2: Node = nameToSource.getOrElse(prename2, null)
        val prenum1: Double = prenode1.getDoubleNum
        val prenum2: Double = prenode2.getDoubleNum
        // 根据当前节点操作进行运算
        n.ctrl.name match {
          case ValueOpCtrl.OPlus => ret = prenum1 + prenum2;
          case ValueOpCtrl.OMinus => ret = prenum1 - prenum2;
          case ValueOpCtrl.OMulti => ret = prenum1 * prenum2;
          case ValueOpCtrl.ODivision => ret = prenum1 / prenum2;    // 这里有可能出现分母为0的异常，暂时未处理
          case _ => println("Unknown binary operator: " + n.ctrl.name)
        }
      } else {
        println("Can't find the input of node: " + n)
      }
    } else {
      println("Input Error: this node isn't an operation: " + n)
    }
    ret
  }

  /**
   * 进行节点的替换：oldt 是 parent的一个孩子，将oldt 替换为 newt
   * @param parent oldt 的 parent
   * @param oldt   要被替换的term
   * @param newt   用来替换oldt的term， 实际上是oldt的一部分，已实现删除oldt中某些内容的目的
   * @return 修改后的parent
   */
  private def updateTerm(parent: Term, oldt: Term, newt: Term): Term = {
    if (debug) {
      println("UpdateTerm: ")
      println("\t parent: " + parent)
      println("\t oldt: " + oldt)
      println("\t newt: " + newt)
    }
    if (parent == null) {
      println("UpdateTerm: ERROR INPUT, parent is null")
    } else {
      if (parent.termType == TermType.TREGION) {      // Regions
        val pa = parent.asInstanceOf[Regions]
        if (pa.leftTerm == oldt)
          pa.leftTerm = newt
        else
          pa.rightTerm = newt
      } else if (parent.termType == TermType.TPAR) {    // Paraller
        val pa = parent.asInstanceOf[Paraller]
        if (pa.leftTerm == oldt)
          pa.leftTerm = newt
        else
          pa.rightTerm = newt
      } else if (parent.termType == TermType.TPREF) {   // Prefix
        val pa = parent.asInstanceOf[Prefix]
        if (pa.suffix == oldt)
          pa.suffix = newt
        else
          println("UpdateTerm: Error, parent term have no child like oldt: " + parent)
      }
    }
    newt.directParent = parent    // 更新父亲关系
    parent
  }

  /**
   * 删除 某个Prefix
   * @param p 某个Prefix
   * @return
   */
  private def delPrefix(p: Prefix): Boolean = {
    var ret = true
    // p 的直接父亲为空，那么出现错误
    if (debug) println("DelPrefix x: " + p)
    if (p.directParent == null) {
      println("The direct parent of current prefix is null: " + p)
      ret = false
    } else if (p.directParent.termType == TermType.TPAR) {              // p 的直接父亲为 Paraller，那么用兄弟把父亲换掉。。
      val parent: Paraller = p.directParent.asInstanceOf[Paraller]
      val brother = if (parent.leftTerm == p) parent.rightTerm else parent.leftTerm   // 找到当前节点的兄弟
      if (parent.directParent == null && parent == retTerm) {           // 如果当前 Prefix 的父亲是根 Term
        println("The parent of cur Prefix is root: " + p)
        retTerm = brother                                               // -> 直接用兄弟代替root
      } else if (parent.directParent != null) {
        updateTerm(parent.directParent, parent, brother)                // 用 brother 把 grandpa 的子节点parent 替换掉
      } else {
        println("DelPrefix: ERROR3 grandpa is null " + p)               // 如果grandpa是null，parent还不是根，那么表示出现了错误，需要检查
        ret = false
      }
    } else if (p.directParent.termType == TermType.TPREF) {             // 如果当前Prefix的父亲是Prefix
      val parent: Prefix = p.directParent.asInstanceOf[Prefix]
      parent.suffix = new Nil                                           // 那么直接置 parent 的 suffix 为空
    }
    // 这里 暂时不考虑 p.directParent.termType == TermType.TREGION的情况。
    // 也就是某个root下直接放置了一个算术节点，而且只放置了一个算术节点。
    // 建模时避免出现这种情况
    ret
  }

  /**
   * 在计算完成后，要把term中的计算节点删除掉
   * @return 是否删除成功       modified by kgq 20220406，这个地方存在问题，不能这么设计，不然会导致结果错误
   */
  private def removeArithCtrl(): Boolean = {
    var ret = true
    // Step (1), Delete all arith node
    opNode.foreach(x => {
      if (!delPrefix(x)) {
        println("delPrefix false x: " + x)
        ret = false
      }
    })

    // Step (2), Update source/target name of oridinary node
    nameToSource.foreach(x => {                             // nameToSource: Map[Name, Node]
      if (!ValueOpCtrl.isArithOpCtrl(x._2.ctrl.name)) {     // is not arith node
        val nn = nameToNum.getOrElse(x._1, 0)
        if (nn < 2) {                                       // 如果小于2，那么置为idle。（实际上，如果是s-point，一定会<2）  modified by kgq 20220406， 这里不能简单的这样替换
          var nport: List[Name] = List();
          for (name <- x._2.ports) {
            if (name == x._1)
              nport = nport.:+(Bigraph.nameFromString("idle", "idle"))
            else {
              nport = nport.:+(name)
            }
          }
          x._2.ports = nport
        }
      }
    })

    updateNode.foreach(x => {                           // 对t-point进行更新       updateNode:Map[Node, Name]
      val nn = nameToNum.getOrElse(x._2, 0)
      if (nn < 2) {                                     // 如果小于2，置为idle。否则，保留
        var nport:List[Name] = List();
        for (name <- x._1.ports) {
          if (name == x._2)
            nport = nport.:+(Bigraph.nameFromString("idle", "idle"))
          else {
            nport = nport.:+(name)
          }
        }
        x._1.ports = nport
      }
    })
    ret
  }


  /**
   * 完成当前term的计算
   * @return 是否成功计算
   */
  def arithCalculate(): Boolean = {
    if (!hasArithCtrl) {
      //println("don't have arith node")
      return false
    }
    // 第一步，计算OpNode
    findOpNode(t)
    // 第二步，计算普通节点
    buildCalMap(t)
    // 第三部，初始化OpMap
    initOpMap()
    // 第四步，构造工作队列，将入度为0的节点放入工作队列中
    val workQueue: mutable.Queue[Node] = mutable.Queue()
    opMap.foreach(x => {
      if (x._2._1 == 0)
        workQueue.enqueue(x._1)   // 将所有入度为0 的节点入队列
    })
    // 第五步，开始计算
    var flag = true
    while (flag) {
      if (workQueue.isEmpty) {
        flag = false
      } else {
        val tnode = workQueue.dequeue()   // 取队首节点
        val num = _arithCal(tnode)        // 当前节点计算
        // 把当前节点的计算结果记录在节点内
        tnode.hasNum = true
        tnode.number = num
        // 更新后继节点的入度
        val outName = tnode.ports(1)
        if (nameToTarget.contains(outName)) {
          val tmpSet: mutable.Set[Node] = nameToTarget.getOrElse(outName, null)
          if (tmpSet.nonEmpty) {
            tmpSet.foreach(x => {         // 遍历后继 节点
              if (opMap.contains(x)) {
                val xval = opMap.getOrElse(x, null)
                val newIn = xval._1 - 1
                if (newIn == 0) {   // 如果某个点的入度为 0， 那么入队列
                  workQueue.enqueue(x)
                }
                opMap += (x -> Tuple2(newIn, false))    // 后继节点入度减一
              }
            })
          }
        } else {
          if (debug) println("The out name isn't operation node: " + tnode)
        }
        // 在opMap中标记当前节点为 已访问
        opMap += (tnode -> Tuple2(0, true))
      }
    }
    // 第六步，检查 opMap中是否还有没有计算过的节点
    var checkRes = false
    opMap.foreach(x => if (!x._2._2) checkRes = true)
    if (checkRes) {
      println("Error! Some node not operate.")
      return false
    }
    // 第七步， 更新 t-point 连接的普通节点
    var ret = true
    updateNode.foreach(x => {
      val xname = x._2
      if (nameToSource.contains(xname)) {
        val snode = nameToSource.getOrElse(xname, null)
        x._1.number = snode.number
        x._1.hasNum = true
      } else {
        ret = false
        println("Can't find source node of: " + x._1)
      }
    })

    // 第八步， 删除计算节点
    ret = removeArithCtrl()
    ret
  }
}

object testValOpCtrl {

  def main(args: Array[String]) {
//    /**
//     * 测试 一
//     */
//    val testvalop = "a:A<2>[a1:edge] | b:B<3>[b1:edge] | p:Plus[b1:edge,c1:edge,a1:edge] | c:C[c1:edge]"
//    val testvalop2 = "a:A<2>[a1:edge,a2:edge] | b:B<3>[b1:edge] | c:C<4>[c1:edge] | d:D[d1:edge] | p:Plus[b1:edge,a2:edge,a1:edge] | m:Multiply[c1:edge,d1:edge,a2:edge]"
//    val testvalop3 = "a:A<2>[a1:edge,a2:edge] | b:B<3>[b1:edge] | c:C<4>[c1:edge] | d:D[d1:edge] | e:E[d1:edge] | p:Plus[b1:edge,a2:edge,a1:edge] | m:Multiply[c1:edge,d1:edge,a2:edge]"
//
//    val testterm = TermParser.apply(testvalop3)
//    println("Input is: " + testterm)
//    val valopcal = new ValueOpCtrl(testterm)
//    val calres = valopcal.arithCalculate()
//    println("Output is: " + valopcal.retTerm)

    /**
     * 测试 二
     */

    val test1 =
      """
        |# Controls
        |%active Greater : 2;
        |%active A : 2;
        |%active B : 1;
        |%active C : 1;
        |%active D : 1;
        |%active E : 1;
        |
        |# Rules
        |%rule r_rule1 a:A[idle,idle] | b:B[idle] | c:C[idle] | d:D[idle] | e:E[idle] -> a:A[a1:edge,a2:edge] | b:B[b1:edge] | c:C[c1:edge] | d:D[d1:edge] | e:E[d1:edge] | p:Plus[b1:edge,a2:edge,a1:edge] | m:Multiply[c1:edge,d1:edge,a2:edge]{};
        |# Model
        |%agent a:A<2>[idle,idle] | b:B<3>[idle] | c:C<4>[idle] | d:D[idle] | e:E[idle] {};
        |
        |#SortingLogic
        |
        |# Go!
        |%check;
        |""".stripMargin
    val t = BGMParser.parseFromString(test1)
    val b = BGMTerm.toBigraph(t)

    val sim: EnumSimulator = new EnumSimulator(b._2)
    sim.setDebug()
    sim.simulate

  }
}