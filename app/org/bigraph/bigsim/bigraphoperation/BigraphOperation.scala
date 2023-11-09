package org.bigraph.bigsim.bigraphoperation

import org.bigraph.bigsim.parser.{ArithExpressionParser, BGMParser, BGMTerm, Cond, TermParser}
import org.bigraph.bigsim.utils.GlobalCfg
import org.bigraph.bigsim.BRS.Match
import org.bigraph.bigsim.model.{Bigraph, Hole, Name, Node, Paraller, Prefix, ReactionRule, Regions, Term, TermType}
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.mutable.Map
import scala.collection.mutable.{Queue, Set}




/**
 * BigraphOperation 类
 * 实现偶图的四种操作： 组和 composition， 并置 juxtaposition， 平行积 parallel product， 混合积 merge product
 *
 */

object BigraphOperation {

  var op_bigraph: Map[String, Term] = Map()
  var DEBUG: Boolean = false

  def addOpBigraph(n: String, t: String): Boolean = {
    val term = TermParser.apply(t)
    if (op_bigraph.contains(n))
      false
    else {
      op_bigraph += (n -> TermParser.apply(t))
      true
    }
  }
  def getOpBigraph(n: String): Term = {
    op_bigraph.getOrElse(n, null)
  }

  def calNodeSet(t: Term): Set[String] = {
    var ret: Set[String] = Set();
    if (t.termType == TermType.TREGION) {               // handle Regions
      val tchild = t.asInstanceOf[Regions].getChildren    // find node name in Regions.children recursively
      tchild.foreach(x => {
        val tnode = calNodeSet(x)
        ret = ret ++ tnode
      })
    } else if (t.termType == TermType.TPAR) {           // handle Paraller
      val tchild = t.asInstanceOf[Paraller].getChildren   // find node name in Paraller.children recursively
      tchild.foreach(x => {
        val tnode = calNodeSet(x)
        ret = ret ++ tnode
      })
    } else if (t.termType == TermType.TPREF) {          // handle Prefix
      val tnode = t.asInstanceOf[Prefix].node             // record cur node name
      ret.add(tnode.name)
      val tsuf = t.asInstanceOf[Prefix].suffix
      ret = ret ++ calNodeSet(tsuf)                       // find node name in suffix recursively
    }
    // 还有两种： Nil 和 Num， 不做处理
    ret
  }

  def calEdgeSet(t: Term): Set[String] = {
    var ret: Set[String] = Set();
    if (t.termType == TermType.TREGION) {               // handle Regions
      val tchild = t.asInstanceOf[Regions].getChildren
      tchild.foreach(x => {
        val tnode = calEdgeSet(x)
        ret = ret ++ tnode
      })
    } else if (t.termType == TermType.TPAR) {           // handle Paraller
      val tchild = t.asInstanceOf[Paraller].getChildren
      tchild.foreach(x => {
        val tnode = calEdgeSet(x)
        ret = ret ++ tnode
      })
    } else if (t.termType == TermType.TPREF) {          // handle Prefix
      val tnode = t.asInstanceOf[Prefix].node
      tnode.ports.foreach(x => {
        if (x.nameType == "edge")
          ret.add(x.name)
      })
      val tsuf = t.asInstanceOf[Prefix].suffix
      ret = ret ++ calNodeSet(tsuf)
    }
    // 还有两种： Nil 和 Num， 不做处理
    ret
  }

  def checkIntersect(a: Term, b: Term, calmethod:(Term) => Set[String]): Tuple2[Boolean, String] = {

    var report: String = ""
    var result: Boolean = false

    val aRes = calmethod(a)
    val bRes = calmethod(b)
    val both = aRes & bRes
    if (!both.isEmpty) {
      report += "\n有交集： " + both
      result = true
    }
    (result, report)
  }

  /**
   * 执行组和操作： a ○ b
   * @param a
   * @param b
   * @return
   */
  def compose(a: Term, b: Term): Tuple3[Term, Boolean, String] = {
    // 如果操作失败，记录失败原因
    var report: String = "Term a: " + a + "\nTerm b: " + b
    var result: Boolean = true

    // 1. 判断 a 和 b 的节点集合是否有交集
    val resnode = checkIntersect(a, b, calNodeSet)
    report += resnode._2
    if (resnode._1) result = false

    // 2. 判断 a 和 b 的边 集合 是否有交集
    val resedge = checkIntersect(a, b, calEdgeSet)
    report += resedge._2
    if (resedge._1) result = false

    // 3. 判断 a 的内部接口和 b 的外部接口是否匹配
    val AFace = new Interface(a)
    val BFace = new Interface(b)
    //  3.1 首先判断 位置图接口
    if (AFace.sites.size != BFace.roots.size) {
      report += "\n因为位置图接口不匹配 -> sites num: " + AFace.sites.size + " != roots num: " + BFace.roots.size
      result = false
    }
    //  3.2 其次判断 连接图接口
    if (AFace.innernames != BFace.outernames) {
      report += "\n因为连接图接口不匹配 -> innernames: " + AFace.innernames + " !+ outernames: " + BFace.outernames
      result = false
    }

    // 条件判断完毕
    if (result)
      report += "\n满足组和条件，执行操作！"
    else {
      report += "\n无法执行[组和]操作，请按报告检查[组和]条件。"
      return (a, result, report)
    }

    // 4. 执行组和操作
    // 4.1 首先，组和连接图： 将 a 内部名， 和 b 外部名 连接成边
    AFace.innerToName.foreach(x => {    // for each innername, 只需要将对应节点ports中连接内部名的边 的内部名列表清空就好
      x._2.foreach(y => {               //    for each name, 清空内部名列表
        if (!y.innerNameList.isEmpty)
          y.innerNameList = List()
      })
    })
    BFace.outerToName.foreach(x => {    // for each outername
      x._2.foreach(y => {               //    for each name
        if (y.nameType == "outername" && y.name == x._1) {
          val edge: Set[Name] = AFace.innerToName.getOrElse(x._1, null)
          y.name = edge.head.name
          y.nameType = edge.head.nameType
        }
      })
    })
    // 4.2 然后，连接位置图： 将 b 的 roots，放入a 的 site中去
    var sitenum: Int = 0
    BFace.roots.foreach(x => {
      var site: Hole = null
      AFace.sites.foreach(y => {    // 找到 当前root 对应的site，注意，AFace.sites中站点并未按照节点名称排序
        if (y.termType == TermType.THOLE && y.asInstanceOf[Hole].index == sitenum)
          site = y.asInstanceOf[Hole]
      })
      val sitepar = AFace.siteParent.getOrElse(site, null)    // 找到当前site，把root(x) 嵌入进去
      if (sitepar.termType == TermType.TPREF) {               // 如果父节点是一个Prefix结构，那么site 只可能存放在 Prefix.suffix里面
        sitepar.asInstanceOf[Prefix].suffix = x
      } else if (sitepar.termType == TermType.TREGION) {      // 如果父节点是一个Regions结构，那么site 可能放在 leftTerm , 也可能放在 rightTerm
        val cterm = sitepar.asInstanceOf[Regions]
        if (cterm.leftTerm == site)
          cterm.leftTerm = x
        else if (cterm.rightTerm == site)
          cterm.rightTerm = x
      } else if (sitepar.termType == TermType.TPAR) {
        val cterm = sitepar.asInstanceOf[Paraller]
        if (cterm.leftTerm == site)
          cterm.leftTerm = x
        else if (cterm.rightTerm == site)
          cterm.rightTerm = x
      }
      sitenum += 1
    })
    if (result)
      report += "\n成功完成组和操作，结果为：" + a
    if (DEBUG) {
      println("Compose operation: " + report)
    }
    (a, result, report)
  }

  /**
   * 并置 juxtaposition
   * 条件：
   *   1. 两个偶图有不相交的支撑 (support)
   *   2. 两个偶图接口不相交
   * @param a
   * @param b
   * @return
   */
  def juxtapose(a: Term, b: Term): Tuple3[Term, Boolean, String] = {
    // 如果操作失败，记录失败原因
    var report: String = "Term a: " + a + "\nTerm b: " + b
    var result: Boolean = true

    // 1. 判断 a 和 b 的节点集合是否有交集
    val resnode = checkIntersect(a, b, calNodeSet)
    report += resnode._2
    if (resnode._1) result = false

    // 2. 判断 a 和 b 的边集合是否有交集
    val resedge = checkIntersect(a, b, calEdgeSet)
    report += resedge._2
    if (resedge._1) result = false

    // 3. 判断 a 和 b 的内外部名是否有交集
    val aFace = new Interface(a)
    val bFace = new Interface(b)
    //  3.1 首先判断外部名是否有交集
    val iouter = aFace.outernames & bFace.outernames
    if (!iouter.isEmpty) {
      report += "\n因为外部名有交集： " + iouter
      result = false
    }
    //  3.2 然后判断内部名是否有交集
    val iinner = aFace.innernames & bFace.innernames
    if (!iinner.isEmpty) {
      report += "\n因为内部名有交集： " + iinner
      result = false
    }

    // 条件判断完毕
    if (result)
      report += "\n满足并置条件，执行操作！"
    else {
      report += "\n无法执行[并置]操作，请按报告检查[并置]条件。"
      return (a, result, report)
    }

    // 4. 执行并置操作
    val newTerm = new Regions(a, b)

    if (result)
      report += "\n成功完成并置操作， 结果为： " + newTerm
    if (DEBUG) {
      println("Juxtapose operation: " + report)
    }
    (newTerm, result, report)
  }

  /**
   *  平行积 parallel product
   *  条件：
   *    1. 两个偶图有不相交的支撑 (support)
   *  和并置的不同是，平行积允许两个偶图共享名字 name-sharing
   * @param a
   * @param b
   * @return
   */
  def parallel(a: Term, b: Term): Tuple3[Term, Boolean, String] = {
    // 如果操作失败，记录失败原因
    var report: String = "Term a: " + a + "\nTerm b: " + b
    var result: Boolean = true

    // 1. 判断 a 和 b 的节点集合是否有交集
    val resnode = checkIntersect(a, b, calNodeSet)
    report += resnode._2
    if (resnode._1) result = false

    // 2. 判断 a 和 b 的边集合是否有交集
    val resedge = checkIntersect(a, b, calEdgeSet)
    report += resedge._2
    if (resedge._1) result = false

    // 内部名和外部名不需要查看

    // 条件判断完毕
    if (result)
      report += "\n满足平行积条件，执行操作！"
    else {
      report += "\n无法执行[平行积]操作，请按报告检查[平行积]条件。"
      return (a, result, report)
    }

    // 3. 执行平行积操作
    val newTerm = new Regions(a, b)

    if (result)
      report += "\n成功完成平行积操作，结果为： " + newTerm
    (newTerm, result, report)
  }

  /**
   *  归并积 merge product
   *  条件：
   *    1. 两个偶图有不相交的支撑 (support)
   * @param a
   * @param b
   * @return
   */
  def merge(a: Term, b: Term): Tuple3[Term, Boolean, String] = {
    // 如果操作失败，记录失败原因
    var report: String = "Term a: " + a + "\nTerm b: " + b
    var result: Boolean = true

    // 1. 判断 a 和 b 的节点集合是否有交集
    val resnode = checkIntersect(a, b, calNodeSet)
    report += resnode._2
    if (resnode._1) result = false

    // 2. 判断 a 和 b 的边集合是否有交集
    val resedge = checkIntersect(a, b, calEdgeSet)
    report += resedge._2
    if (resnode._1) result = false

    // 内部名和外部名不需要查看
    // 条件判断完毕
    if (result)
      report += "\n满足归并积条件，执行操作！"
    else {
      report += "\n无法执行[归并积]操作，请按报告检查[归并积]条件。"
      return (a, result, report)
    }

    // 3. 执行归并积操作
    var roots: List[Term] = List();
    if (a.termType == TermType.TREGION) {   // 如果 a 类型为Regions，说明有多个root，找出来记录到aTerms中
      roots ++= a.asInstanceOf[Regions].getChildren
    } else {                                // 否则，只有一个root，把a记录到aTerms中
      roots = roots.:+(a)
    }
    if (b.termType == TermType.TREGION) {
      roots ++= b.asInstanceOf[Regions].getChildren
    } else {
      roots = roots.:+(b)
    }
    if (DEBUG) {
      println("roots is: !! " + roots.tail)
    }
    var ret: Term = roots.head
    roots.tail.foreach(x => {               // 将所有的root，用Paraller归并到一起。 只留一个root
      ret = new Paraller(ret, x)
    })

    if(result)
      report += "\n成功完成归并及操作， 结果为: " + ret
    (ret, result, report)
  }

  /**
   * 执行嵌入操作： a 和 b
   * 条件：
   *      1. 两个偶图的边集和点集不能相交
   *      2. roots 和 sites 个数匹配
   *      3. a 的 内部名 是空集
   *      4. b 没有 sites
   * @param a
   * @param b
   * @return
   */
  def nesting(a: Term, b: Term): Tuple3[Term, Boolean, String] = {
    // 如果操作失败， 记录失败原因
    var report: String = "Term a: " + a + "\nTerm b: " + b
    var result: Boolean = true

    // 1. 判断 a 和 b 的节点集合是否有交集
    val resnode = checkIntersect(a, b, calNodeSet)
    report += resnode._2
    if (resnode._1) result = false

    // 2. 判断 a 和 b 的边集合是否有交集
    val resedge = checkIntersect(a, b, calEdgeSet)
    report += resedge._2
    if (resedge._1) result = false

    // 3. 对接口进行判断
    val aFace = new Interface(a)
    val bFace = new Interface(b)
    //  3.1 首先判断位置图接口
    if (aFace.sites.size != bFace.roots.size) {
      report += "\n因为位置图接口不匹配 -> sites num: " + aFace.sites.size + " != roots num: " + bFace.roots.size
      result = false
    }
    //  3.2 其次判断连接图接口
    if (!aFace.innernames.isEmpty) {
      report += "\n因为 a 内部名不为空集 -> innernames: " + aFace.innernames
      result = false
    }

    // 条件判断完毕
    if (result)
      report += "\n满足嵌入条件，执行操作！"
    else {
      report += "\n无法执行[嵌入]操作，请按报告检查[嵌入]条件。"
      return (a, result, report)
    }

    // 4. 执行嵌入操作， 组和位置图。（连接图也组合，但实际不做处理）
    var sitenum: Int = 0
    bFace.roots.foreach(x => {
      var site: Hole = null
      aFace.sites.foreach(y => {
        if (y.termType == TermType.THOLE && y.asInstanceOf[Hole].index == sitenum)
          site = y.asInstanceOf[Hole]
      })
      val sitepar = aFace.siteParent.getOrElse(site, null)
      if (sitepar.termType == TermType.TPREF) {
        sitepar.asInstanceOf[Prefix].suffix = x
      } else if (sitepar.termType == TermType.TREGION) {
        val cterm = sitepar.asInstanceOf[Regions]
        if (cterm.leftTerm == site)
          cterm.leftTerm = x
        else if (cterm.rightTerm == site)
          cterm.rightTerm = x
      } else if (sitepar.termType == TermType.TPAR) {
        val cterm = sitepar.asInstanceOf[Paraller]
        if (cterm.leftTerm == site)
          cterm.leftTerm = x
        else if (cterm.rightTerm == site)
          cterm.rightTerm = x
      }
      sitenum += 1
    })
    if (result)
      report += "\n成功完成嵌入操作，结果为： " + a

    (a, result, report)
  }

  /**
   * 因为偶图操作之后，存在 节点并置 Paraller中存在 nil的情况，导致后面匹配衍化的时候出错
   * 因此需要在偶图操作之后，立刻将Paraller中的nil进行修剪删除
   *
   * @param root 需要进行 修剪nil的偶图的根
   * @return   返回被修剪后的偶图的根
   */
  def trimnil(root: Term): Term = {
    var retTerm: Term = root
    if (root.termType == TermType.TREGION) {                   //  1. 考虑根节点为 Region 的情况
      val curRegion = root.asInstanceOf[Regions]
      var hasNilChild: Boolean = curRegion.leftTerm.termType == TermType.TNIL || curRegion.rightTerm.termType == TermType.TNIL
      if (!hasNilChild) {                                               // 如果当前Term的直接孩子没有Nil，那么递归查找其子Term
        curRegion.leftTerm = trimnil(curRegion.leftTerm)
        curRegion.rightTerm = trimnil(curRegion.rightTerm)
      } else if (curRegion.leftTerm.termType == TermType.TNIL) {        // 如果当前Term的左子孩子为Nil，那么
        retTerm = trimnil(curRegion.rightTerm)                          // 递归处理右子Term，删除当前层次
      } else {                                                          // 当前Term的右子孩子为Nil，那么
        retTerm = trimnil(curRegion.leftTerm)                           // 递归处理左子Term，删除当前层次
      }
    } else if (root.termType == TermType.TPAR) {               // 2. 考虑根节点为 Paraller 的情况
      val curParaller = root.asInstanceOf[Paraller]
      var hasNilChild: Boolean = curParaller.leftTerm.termType == TermType.TNIL || curParaller.rightTerm.termType == TermType.TNIL
      if (!hasNilChild) {
        curParaller.leftTerm = trimnil(curParaller.leftTerm)
        curParaller.rightTerm = trimnil(curParaller.rightTerm)
      } else if (curParaller.leftTerm.termType == TermType.TNIL) {
        retTerm = trimnil(curParaller.rightTerm)
      } else {
        retTerm = trimnil(curParaller.leftTerm)
      }
    } else if (root.termType == TermType.TPREF) {               // 3. 考虑根节点为 Prefix的情况
      val curPrefix = root.asInstanceOf[Prefix]
      curPrefix.suffix = trimnil(curPrefix.suffix)
    }

    // 还有两种情况： Nil 和 Num，不作考虑
    retTerm
  }

}

class BigraphOperation {

}

object testBigraphOperation {

  def main(args: Array[String]) {


    var a: Term = null
    var b: Term = null

//    println("======= test compose =========")
//    a = TermParser.apply("v0:M[e0:edge(a,b)].(v2:L.$1 | $0) || $2")
//    b = TermParser.apply("v1:K[a:outername,e1:edge] || v3:K[e1:edge,e2:edge] || v4:K[b:outername,e2:edge].v5:M[e2:edge]")
//    val composeab = BigraphOperation.compose(a, b)
//    println(composeab._1)
//    println(composeab._3)
//
//    println("======= test juxtapose =======")
//    a = TermParser.apply("v0:M[e0:edge(a,b)].(v2:L.$1 | $0) || $2")
//    b = TermParser.apply("v1:K[a:outername,e1:edge] || v3:K[e1:edge,e2:edge] || v4:K[b:outername,e2:edge].v5:M[e2:edge]")
//    val juxtaposeab = BigraphOperation.juxtapose(a, b)
//    println(juxtaposeab._1)
//    println(juxtaposeab._3)
//
//    println("======= test parallel ========")
//    a = TermParser.apply("v0:M[e0:edge(a,b)].(v2:L.$1 | $0) || $2")
//    b = TermParser.apply("v1:K[a:outername,e1:edge] || v3:K[e1:edge,e2:edge] || v4:K[b:outername,e2:edge].v5:M[e2:edge]")
//    val parallelab = BigraphOperation.parallel(a, b)
//    println(parallelab._1)
//    println(parallelab._3)
//
//    println("======= test merge ===========")
//    a = TermParser.apply("v0:M[e0:edge(a,b)].(v2:L.$1 | $0) || $2")
//    b = TermParser.apply("v1:K[a:outername,e1:edge] || v3:K[e1:edge,e2:edge] || v4:K[b:outername,e2:edge].v5:M[e2:edge]")
//    val mergeab = BigraphOperation.merge(a, b)
//    println(mergeab._1)
//    println(mergeab._2)

    println("======= test nesting =========")
    a = TermParser.apply("v0:K[x:outername,y:outername,z:outername].$0")
    b = TermParser.apply("v1:L[y:outername,z:outername].$0")
    val nestingab = BigraphOperation.nesting(a, b)
    println(nestingab._1)
    println(nestingab._3)

    val c = TermParser.apply("a1:K[a:edge,idle] | a2:K[a:edge,idle]")
    println(c)

    // test trim nil
    val minerother = TermParser.apply("nil")
    val user = TermParser.apply("userc:User[idle].(fallback:Fallback[idle,idle] | users:Save[idle].usersm:Money<3>[idle,idle] | usert:TakeOut[idle].usertm:Money<3>[idle,idle])")
    val bank = TermParser.apply("bankc:Bank.(deposit:Deposit[idle,idle].depg:Gas[idle].depm:Money<1>[idle,idle] | withdraw:WithDraw[idle,idle,idle,idle].witg:Gas[idle].witm:Money<1>[idle,idle] | a1:BankAccount[idle].(a1a:Address<0>[idle] | a1m:Money<0>[idle,idle]) | a2:BankAccount[idle].(a2a:Address<1>[idle] | a2m:Money<0>[idle,idle]) | $0)")
    val bankother = TermParser.apply("nil")
    val miner = TermParser.apply("miner:Miner.(bank:SC_RT[idle].(banka:Address<2>[idle] | bankb:Balance[idle].bankm:Money<20>[idle,idle] | $2) | user:SC_RT[idle].(usera:Address<0>[idle] | userb:Balance[idle].userm:Money<4>[idle,idle] | $1) | minerb:Balance[idle].minerm:Money<0>[idle,idle] | $0)")
    val parminerother = BigraphOperation.parallel(minerother, user)
    val composebank = BigraphOperation.compose(bank, bankother)
    val paruser = BigraphOperation.parallel(parminerother._1,composebank._1)
    val composeminer = BigraphOperation.compose(miner, paruser._1)

    val oldres = """miner:Miner.(bank:SC_RT[idle5].(banka:Address<2>[idle5].nil|bankb:Balance[idle5].bankm:Money<20>[idle5,idle5].nil|bankc:Bank.(a1:BankAccount[idle5].(a1a:Address<0>[idle5].nil|a1m:Money<0>[idle5,idle5].nil)|a2:BankAccount[idle5].(a2a:Address<1>[idle5].nil|a2m:Money<0>[idle5,idle5].nil)|deposit:Deposit[idle5,idle5].depg:Gas[idle5].depm:Money<1>[idle5,idle5].nil|nil|withdraw:WithDraw[idle5,idle5,idle5,idle5].witg:Gas[idle5].witm:Money<1>[idle5,idle5].nil))|minerb:Balance[idle5].minerm:Money<0>[idle5,idle5].nil|nil|user:SC_RT[idle5].(usera:Address<0>[idle5].nil|userb:Balance[idle5].userm:Money<4>[idle5,idle5].nil|userc:User[idle5].(fallback:Fallback[idle5,idle5].nil|users:Save[idle5].usersm:Money<3>[idle5,idle5].nil|usert:TakeOut[idle5].usertm:Money<3>[idle5,idle5].nil))))"""
    val newres = """miner:Miner.(bank:SC_RT[idle5].(banka:Address<2>[idle5].nil|bankb:Balance[idle5].bankm:Money<20>[idle5,idle5].nil|bankc:Bank.(a1:BankAccount[idle5].(a1a:Address<0>[idle5].nil|a1m:Money<0>[idle5,idle5].nil)|a2:BankAccount[idle5].(a2a:Address<1>[idle5].nil|a2m:Money<0>[idle5,idle5].nil)|deposit:Deposit[idle5,idle5].depg:Gas[idle5].depm:Money<1>[idle5,idle5].nil|    withdraw:WithDraw[idle5,idle5,idle5,idle5].witg:Gas[idle5].witm:Money<1>[idle5,idle5].nil))|minerb:Balance[idle5].minerm:Money<0>[idle5,idle5].nil|    user:SC_RT[idle5].(usera:Address<0>[idle5].nil|userb:Balance[idle5].userm:Money<4>[idle5,idle5].nil|userc:User[idle5].(fallback:Fallback[idle5,idle5].nil|users:Save[idle5].usersm:Money<3>[idle5,idle5].nil|usert:TakeOut[idle5].usertm:Money<3>[idle5,idle5].nil)))"""
    println(composeminer)
    val newroot = BigraphOperation.trimnil(composeminer._1)
    println(newroot)

  }
}