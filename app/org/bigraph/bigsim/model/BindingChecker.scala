package org.bigraph.bigsim.model

import org.bigraph.bigsim.BRS.{Match, Vertex}

import scala.collection.mutable.Set
import scala.collection.mutable.Map
import org.bigraph.bigsim.parser.{BGMParser, BGMTerm, TermParser}

import scala.collection.mutable

/**
 * @author kongguanqiao
 */
class BindingChecker {

  var bindingSet: Set[String] = Set();
  var bindingMap: Map[String, Node] = Map();

  var bindPortMap: Map[String, String] = Map();   // 这个记录的是普通的边名字，到 绑定名字的映射

  var DEBUG: Boolean = false

  /**
   * @author kongguanqiao
   *         合并两个集合
   */
  def mergeResults(result1: Set[Node], result2: Set[Node]): Set[Node] = {
    result2.map(it => {
      result1 += it
    })
    result1
  }


  /**
   * @author kongguanqiao
   *         检查所有节点的父节点是不是有的非空
   *
   */
  def checkNodeParent(term: Term):Boolean = {
    var result: Boolean = true
    if (term.termType == TermType.TPAR) {
      var paraResult: Set[Term] = term.asInstanceOf[Paraller].getChildren;
      paraResult.map(it => {
        if (!checkNodeParent(it)) result = false
      })
    } else if (term.termType == TermType.TPREF) {
      if (term.asInstanceOf[Prefix].suffix.termType == TermType.THOLE || term.asInstanceOf[Prefix].suffix.termType == TermType.TNIL){
        result = (term.asInstanceOf[Prefix].node.parent == null)
      } else {
        result = (term.asInstanceOf[Prefix].node.parent == null)
        if (!result) return false
        return checkNodeParent(term.asInstanceOf[Prefix].suffix)
      }
    }
    result
  }

  /**
   * @author kongguanqiao
   *          因为在反应衍化的过程中，对新生成的偶图进行检测的时候，会调用getNodeParent来递归构造每个term的parent，
   *          但是呢，这个过程中有些term的parent字段被修改了，而且在bigraph中共享了，或者是复制的时候给复制过来了。
   *          所以 在调用getNodeParent之前，需要对parent字段进行清理，保证getNodeParent构造结果的正确性
   *
   */
  def refreshNodeParent(term: Term): Boolean = {
    if (term.termType == TermType.TPAR) {
      var paraResult: Set[Term] = term.asInstanceOf[Paraller].getChildren;
      paraResult.map(it => {
        refreshNodeParent(it)
      })
    } else if (term.termType == TermType.TPREF) {
      if (term.asInstanceOf[Prefix].suffix.termType == TermType.THOLE || term.asInstanceOf[Prefix].suffix.termType == TermType.TNIL){
        term.asInstanceOf[Prefix].node.parent = null
      } else {
        term.asInstanceOf[Prefix].node.parent = null
        refreshNodeParent(term.asInstanceOf[Prefix].suffix)
      }
    }
    true
  }

  /**
   * @author kongguanqiao
   *         获得所有节点的父节点
   */
  def getNodeParent(term: Term): Set[Node] ={


    var result: Set[Node] = Set()
    if (term.termType == TermType.TPAR) {             // 如果当前的term是一个 paraller结构
      var paraResult: Set[Term] = term.asInstanceOf[Paraller].getChildren;
      paraResult.map(it => {
        result = mergeResults(result, getNodeParent(it))
      })
    } else if (term.termType == TermType.TPREF) {     //  如果当前的term是一个prefix结构
      if (term.asInstanceOf[Prefix].suffix.termType == TermType.THOLE || term.asInstanceOf[Prefix].suffix.termType == TermType.TNIL) {  // 如果后缀是一个site，或者空的
        result += term.asInstanceOf[Prefix].node;
      } else {        // 如果后缀里面还有节点，那么先获得这些节点，并且把parent为null的 设置为当前节点。
        result += term.asInstanceOf[Prefix].node;
        var tmpResult: Set[Node] = getNodeParent(term.asInstanceOf[Prefix].suffix)
        tmpResult.map(it => {
          if (it.parent == null) it.parent = term.asInstanceOf[Prefix].node
        })
        result = mergeResults(result,tmpResult)
      }
    }
    result;
  }

  def addBind(node: Node) = {
    bindingSet.add(node.name);
    bindingMap += (node.name -> node)

    if (node.ports.nonEmpty) {  // 如果当前绑定的port列表不为空 （为了兼容老版本binding）
      for (n <- node.ports){
        bindPortMap += (n.name -> node.name)      // edge.name -> bind.name 便于反向查找
      }
    }

    true;
  }

  /**
   * @author kongguanqiao
   *         获得当前偶图中的所有绑定节点，同时检查绑定是不是原子的
   *         如果绑定都是满足原子条件的，那么返回true，并把binding名字，和名字到node的映射分别记录到bindingSet bindingMap中去
   */
  def getBindingNode(term: Term): Boolean = {
    var valid: Boolean = true
//    println("当前term 类型" + term.termType)
    if (term.termType == TermType.TPAR) {
//      println("先进了paraller")
      var paraResult: Set[Term] = term.asInstanceOf[Paraller].getChildren;
      paraResult.map(it => {
        valid &= getBindingNode(it)
      })
    } else if (term.termType == TermType.TPREF) {
//      println("然后prefix" + term.asInstanceOf[Prefix].node.name + term.asInstanceOf[Prefix].node.ctrl)
      if (term.asInstanceOf[Prefix].node.ctrl.binding) {                  //  如果当前的节点是一个binding
//        println("当前是个binding")
        if (term.asInstanceOf[Prefix].suffix.termType == TermType.THOLE) {  // 如果当前的节点后缀是 站点site，则不满足binding的原子性
//          println("Violate atomic of binding:" + term)
          return false
        } else if (term.asInstanceOf[Prefix].suffix.termType == TermType.TNIL) {  //当前节点后缀是空的，说明满足binding性质，记录到bindingSet 和 bindingMap中去
          return addBind(term.asInstanceOf[Prefix].node);
        } else {    // 如果后缀是其他类型的，递归查找
//          println("后缀是其他")
          valid &= getBindingNode(term.asInstanceOf[Prefix].suffix)
        }
      } else {
//        println("当前不是binding" + term.asInstanceOf[Prefix].suffix.termType)
        if (term.asInstanceOf[Prefix].suffix.termType != TermType.THOLE && term.asInstanceOf[Prefix].suffix.termType != TermType.TNIL) {
//          println("接着往下找")
          valid &= getBindingNode(term.asInstanceOf[Prefix].suffix)
        }
      }
    }
//    println(bindingSet)
//    println(bindingMap)
    valid
  }

  /**
   * @author kongguanqiao
   *         判断node1 是不是 node2 的后代
   */
  def isdescendant(node1: Node, node2: Node): Boolean = {
    if (node1 == node2 || node2 == null) {
      return true
    }
    if (node1 == null)
      return false
//    println("panduan")
    var curnode: Node = node1
//    print(curnode)
    while(curnode.parent != null){
//      println(curnode)
      if (curnode.parent == node2)
        return true
      curnode = curnode.parent
    }
    false
  }

  /**
   * @author kongguanqiao
   *         检测是否满足绑定约束
   */
  def bindcheck(t: Term): Boolean = {
    println("???????现在检查的term是" + t)

    refreshNodeParent(t)

    if (!checkNodeParent(t)) println("这个当前的term里面parent不干净“”“”“”“”“”“”“”“”“”“”“”“”“”“”“”")

    var nodes = getNodeParent(t); // set[Node]
    if (!getBindingNode(t)) {
      println("Violate the atomic!")
      return false
    }
    nodes.foreach(x => {
      if (DEBUG)
        println("遍历到了" + x.name + x.ctrl.binding)
      if (!x.ctrl.binding) {    // 只对非binding节点进行检查
        x.ports.foreach(n => {
          if (DEBUG)
            println("检查端口" + n + n.nameType)
          if (n.nameType == "binding" || bindPortMap.contains(n.name)) {  // 如果某个端口连接了binding, 或者这个端口的边连接到了binding上
            var tmpName = n.name
            if (bindPortMap.contains(n.name)) tmpName = bindPortMap.get(n.name).get

            if (!bindingSet.contains(tmpName)){
              if (DEBUG)
                println("UnKnown binding!")  // 遇到了一个不存在的binding
              return false
            } else {
              val bindNode:Node = bindingMap.get(tmpName).get // 取得对应所连接的binding的节点
              if (DEBUG)
                println("取得了bindnode" + bindNode + bindNode.parent + x.parent)
              if (!isdescendant(x.parent, bindNode.parent)) {
                println("Violate the binding constraint!")
                println("Give up this Bigraph.")
                return false
              }
            }
          }
        })
      }
    })
    println("Satisify the binding constraint!")
    true;
  }
}

object TestBindingChecker {
  def main(args: Array[String]) {
    var s =
      """
        |# Controls
        |%active Coin : 2;
        |%active Bank : 2;
        |%binding CoinInBank;
        |
        |# Names
        |%outername b;
        |
        |# Rules
        |%rule r_test1 a:Bank[idle,idle].(b:Coin[idle,b:edge] | c:Coin[a:edge,b:edge]) -> a:Bank[idle,idle].c:Coin[a:edge,b:edge] | b:Coin[idle,b:edge]{};
        |
        |%rule r_test2 a:Bank[idle,idle].(b:Coin[idle,a:edge] | c:Coin[idle,a:edge]) -> a:Bank[idle,idle].c:Coin[idle,a:edge] | b:Coin[idle,a:edge]{};
        |
        |%rule r_test3 a:Bank[idle,idle].(b:Coin[idle,idle] | c:Coin[idle,idle] | d:Coin[idle,idle]) -> a:Bank[idle,idle].(c:Coin[idle,idle] | d:Coin[idle,idle]) | b:Coin[idle,idle]{};
        |
        |# Model
        |%agent a:Bank[idle,idle].(b:Coin[b:outername,a:edge] | c:Coin[idle,a:edge]);
        |
        |#SortingLogic
        |
        |# Go!
        |%check;
        |""".stripMargin
    var s1 =
      """
        |# Controls
        |%active Greater : 2;
        |%active Node : 2;
        |%active Container : 0;
        |%binding Bind;
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
        |# Model
        |%agent a:Container.(d:Bind | e:Container.b:Node[idle,d:binding]) | c:Node[idle,d:binding];
        |
        |#SortingLogic
        |
        |# Go!
        |%check;
        |""".stripMargin
    var s3 =
      """
        |# Controls
        |%active Greater : 2;
        |%active Node : 2;
        |%active Container : 0;
        |%binding Bind;
        |
        |# Model
        |%agent a:Container.(b:Node[idle,d:binding] | c:Node[idle,d:binding] | d:Bind);
        |
        |#SortingLogic
        |
        |# Go!
        |%check;
        |""".stripMargin

    var sNewVersion =
      """
        |# Controls
        |%active Greater : 2;
        |%active Node : 2;
        |%active Container : 0;
        |%binding Bind;
        |
        |# Model
        |%agent a:Container.(b:Node[idle,a:edge] | d:Bind[a:edge,b:edge]) | c:Node[idle,b:edge];
        |
        |#SortingLogic
        |
        |# Go!
        |%check;
        |""".stripMargin

    var sc_model =
      """
        |# Controls
        |%active Address : 1;
        |%active Money : 2;
        |%active Balance : 1;
        |%active Bank : 0;
        |%active BankAccount : 1;
        |%active Count : 1;
        |%active Deposit : 2;
        |%active Fallback : 2;
        |%active Gas : 1;
        |%active Miner : 0;
        |%active SC_RT : 1;
        |%active User : 1;
        |%active WithDraw : 4;
        |%active Save : 1;
        |%active TakeOut : 1;
        |%binding Bind;
        |
        |# model
        |%agent user:SC_RT[a:edge].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | userc:User[idle].(fallback:Fallback[idle,idle] | takeout:TakeOut[a:edge].takeoutmMoney[idle,idle] | witg:Gas[b:edge].witm:Money[idle,idle])) | bank:SC_RT[idle].(banka:Address[idle] | bankb:Balance[idle].bankm:Money[idle,idle] | bankc:Bank.(a1:BankAccount[idle].(a1m:Money[idle,idle] | a1a:Address[idle]) | withdraw:WithDraw[a:edge,c:edge,idle,idle].(a:Bind[c:edge,b:edge] | attackg:Gas[idle].attackm:Money[idle,idle])));
        |
        |# Go!
        |%check;
        |""".stripMargin


    var sc_model2 =
      """
        |# Controls
        |%active Address : 1;
        |%active Money : 2;
        |%active Balance : 1;
        |%active Bank : 0;
        |%active BankAccount : 1;
        |%active Count : 1;
        |%active Deposit : 2;
        |%active Fallback : 2;
        |%active Gas : 1;
        |%active Miner : 0;
        |%active SC_RT : 1;
        |%active User : 1;
        |%active WithDraw : 4;
        |%active Save : 1;
        |%active TakeOut : 1;
        |%binding Binding;
        |
        |# model
        |%agent user:SC_RT[a:edge].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | userc:User[idle].(userfall:Fallback[idle,idle] | users:Save[a:edge].usersm:Money[idle,idle] | depg:Gas[b:edge].depm:Money[idle,idle])) | bank:SC_RT[idle].(bankb:Balance[idle].bankm:Money[idle,idle] | bankc:Bank.(deposit:Deposit[c:edge,a:edge].(a:Binding[c:edge,b:edge] | attackg:Gas[idle].attackm:Money[idle,idle]) | useraccount:BankAccount[idle].(useraddress:Address[idle] | userbalance:Money[idle,idle])) | banka:Address[idle]);
        |
        |# Go!
        |%check;
        |""".stripMargin

    println(sNewVersion)
    val p: List[BGMTerm] = BGMParser.parseFromString(sc_model2);
//    val p: List[BGMTerm] = BGMParser.parseFromString(s2);
    println(p)
    val b: Bigraph = BGMTerm.toBigraph(p);
    println(b)
    var bindingChecker = new BindingChecker();
    bindingChecker.bindcheck(b.root);
  }
}