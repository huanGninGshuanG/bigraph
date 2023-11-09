package org.bigraph.bigsim.model

import org.bigraph.bigsim.BRS.Vertex
import scala.collection.mutable.Set
import org.bigraph.bigsim.parser.TermParser

/**
 * @author libaijie
 */
class Binding { //linking(bindingControl) in placing(Control)
  var linking: String = "";
  var placing: String = "";

  override def toString = "Binding (Link:" + linking + " in Control:" + placing + ")";

  def this(n: String) = {
    this();
    init(n);
  }

  def init(n: String): Unit = {
    if (n.contains("in")) {
      var bindStr: Array[String] = n.split(" ");
      linking = bindStr(0);
      placing = bindStr(2);
    }
  }

  /**
   * 计算Term的所有控制实例：控制名称+端口名称列表
   * (Service,List(idle, idle, idle, idle)), (C,List(m)), (Y,List(m, n)), (D,List(n))
   * 有Node的话则节点也要输出
   */
  def getCtrlInstances(t: Term): List[Pair[String, List[String]]] = {
    var ctrlInstances: List[Pair[String, List[String]]] = List();
    var nodeName: String = "";
    if (t.isInstanceOf[Prefix]) {
      var prefix = t.asInstanceOf[Prefix];
      if (prefix.node.ports.size > 0) { // K[n1,n2,...]         这里只会把端口数量大于0 的节点给保留下来。
        if (prefix.node.name != null && !prefix.node.name.equals("")) { nodeName = prefix.node.name + ":" + prefix.node.ctrl.name } else { nodeName = prefix.node.ctrl.name }
        var pair = Pair(nodeName, prefix.node.ports.map(_.name)); //不用加上edge了，其中不是idle的就是edge或者innername或者outername
        ctrlInstances = ctrlInstances.:+(pair);
      }
      ctrlInstances = ctrlInstances ::: getCtrlInstances(prefix.suffix);
    } else if (t.isInstanceOf[Paraller]) {
      var children = t.asInstanceOf[Paraller].getChildren;
      children.map(child => {
        ctrlInstances = ctrlInstances ::: getCtrlInstances(child);
      });
    } else if (t.isInstanceOf[Regions]) {
      var children = t.asInstanceOf[Regions].getChildren;
      children.map(child => {
        ctrlInstances = ctrlInstances ::: getCtrlInstances(child);
      });
    }

    ctrlInstances; //应该可重复，因为同一个Control有不同的Node就有不同的连法
  }

  /**
   * 计算Term中的连接对，具有相同链接名称的控制表示有link
   * 如(C,List(m)), (Y,List(m))，C和Y通过m:edge连接在一起
   * 返回Set集合(C,Y)
   */
  //  def getLinkPair(t: Term): Set[String] = {
  //    var linkPairs: Set[String] = Set();
  //    //List[Pair[ctrlName, ports]]
  //    var ctrlInstances: List[Pair[String, List[String]]] = getCtrlInstances(t);
  //    //Map[linkName, List[Pair[ctrlName, index]]]
  //    var lnName2ctrlPairs: Map[String, List[String]] = Map(); //linking
  //
  //    var i = 0;
  //    while (i < ctrlInstances.size) {
  //        var ctrlEdges: List[String] = ctrlInstances(i)._2.distinct.filter { x => !"idle".equals(x) }; //idle不参与查找
  //        ctrlEdges.foreach { e => {
  //          if(e.equals(linking)){
  //            linkPairs = linkPairs.+(ctrlInstances(i)._1)
  //          }
  //         } 
  //        }
  //      i = i + 1;
  //    }
  //
  //    linkPairs;
  //  }

  /**
   * 输出bindingControl和两端的两个节点，（B，X），（B，Y）都可能有node
   */
  def getLinkControl(t: Term, n: Node): Set[String] = {
    var linkPairs: Set[String] = Set();
    //List[Pair[ctrlName, ports]]
    var ctrlInstances: List[Pair[String, List[String]]] = getCtrlInstances(t);        // 获得当前的所有控制实例。
    //Map[linkName, List[Pair[ctrlName, index]]]
    var lnName2ctrlPairs: Map[String, List[String]] = Map(); //linking
    var bindingLinks: List[String] = List();

    var i = 0;
    while (i < ctrlInstances.size) {
      if (ctrlInstances(i)._1.equals(linking) || (ctrlInstances(i)._1.split(":").size == 2 && ctrlInstances(i)._1.split(":")(1).equals(linking))) {   // 如果某个节点的control是binding，那么就把这个节点的端口列表记录下来
        bindingLinks = ctrlInstances(i)._2
      }
      i = i + 1;
    }
    if (bindingLinks.size == 2) {
      linkPairs = linkPairs.+(linking);
      ctrlInstances.foreach(x => {
        if (x._2.contains(bindingLinks(0)) || x._2.contains(bindingLinks(1))) {     // 在所有控制实例中查找端口表中包括binding所连接的两条边的节点。
          linkPairs = linkPairs.+(x._1);
        }
      })
    }
    linkPairs;
  }

  def mergeResults(result1: Set[Node], result2: Set[Node]): Set[Node] = {
    result2.map(it => {
      result1 += it
    })
    result1
  }

  //获得所有Node集合，其中每个Node的parent属性为父节点名称，如果是顶层Node则父为null
  // kgq， 也就是说，之前Node 中的parent是为空的，没有对其进行赋值，这里是把parent给写上了。
  def getNodeParent(term: Term): Set[Node] = {
    var result: Set[Node] = Set()

    if (term.termType == TermType.TPAR) {     // if current term is paraller
      var paraResult: Set[Term] = term.asInstanceOf[Paraller].getChildren;      // get all children of current paraller
      paraResult.map(it => {
        result = mergeResults(result, getNodeParent(it))
      })
    } else if (term.termType == TermType.TPREF) {     // if current term is prefix
      if (term.asInstanceOf[Prefix].suffix.termType == TermType.THOLE || term.asInstanceOf[Prefix].suffix.termType == TermType.TNIL) {
        result += term.asInstanceOf[Prefix].node;
      } else {
        result += term.asInstanceOf[Prefix].node
        var tempResult: Set[Node] = getNodeParent(term.asInstanceOf[Prefix].suffix)
        tempResult.map(it => {                                                    //  明白了 ！！ 这里的是通过递归的方式，prefix的后缀部分里面的所有node，如果parent是空的，也就是表示这个节点是紧邻当前节点的子节点，就把这些节点的parent设置为当前的界定啊
          if (it.parent == null) it.parent = term.asInstanceOf[Prefix].node
        })
        result = mergeResults(result, tempResult)
      }
    }
    result;
  }

  def getPlaceStr(n: Node): String = {
    if (!"".equals(n.name) && n.name.length() > 0) {
      return n.name + ":" + n.ctrl.name
    }
    return n.ctrl.name
  }

  /**
   * 绑定关系判断，不满足则返回false
   */
  def bindingCheck(t: Term): Boolean = {
    var containBindingControl = false
    var bindingControls: Set[String] = Set()

    var nodes = getNodeParent(t);   // set[Node]
    nodes.foreach { x =>
      {
        if (x.ctrl.name == linking) {       // 这里的linking是binding constraint中的linking， 也就是说，当前的这个control就是一个control
          containBindingControl = true
          bindingControls = getLinkControl(t, x)
        }
      }
    }

    if (!containBindingControl) { return true; } //定义了一个模型里不存在的binding control则认为验证成功直接通过了

    var parentPlace: Set[String] = Set()
    nodes.foreach { n =>
      {
        var placeName: String = getPlaceStr(n) //自动解析为Node:Control的形式，或只有Control
        bindingControls.foreach { b =>
          {
            if (placeName.equals(b)) {         // 从所有的节点集合中，找到这些和binding相连的节点， 就是n， 并且把这个节点 的 父亲节点记录下来
              var par: String = getPlaceStr(n.parent)
              parentPlace = parentPlace.+(par)
            }
          }
        }
      }
    }

    if (parentPlace.size != 1 || !parentPlace.head.equals(placing)) {       //三个control有不同的父节点或共同的父节点不是定义的placing（默认只支持一层）       这里限制的意思是（要求所有和binding相连的节点都要在同一个层次，而且父节点必须为指定的control
      println("Binding Violation: " + "Binding (Link:" + linking + " in Control:" + placing + ")");
      return false
    }

    //    if(linkPairs.size==2){//返回linking两边的节点
    //      nodes.foreach { n => {
    //       var placeName:String = getPlaceStr(n) //自动解析为Node:Control的形式，或只有Control
    //       linkPairs.foreach { l => {
    //         if(placeName.equals(l)){
    //           var par:String = getPlaceStr(n.parent)
    //           if(!par.equals(placing)){
    //             println("Binding Violation: " + "Binding (Link:" + linking + " in Control:" + placing + ")");
    //             return false
    //           }
    //         }
    //        }
    //       }
    //      }
    //     }     
    //    }else{
    //      false
    //    }
    true;
  }

  def check(v: Vertex): Boolean = {
    if (v == null || v.bigraph == null || v.bigraph.root == null) {
      println("Vertex is null");
      return true;
    } else {
      var t = v.bigraph.root;
      return bindingCheck(t)
    }
  }

}

object TestBinding {
  def main(args: Array[String]) {
    var b: Binding = new Binding("useBlanket in Airplane")

    var prefixcompare2 = "A.(GUI.$1 | B.(w3a1:C[m:edge].(N|F.(G|Y[m:edge,n:edge])) | D[n:edge] | M)) | S4:Service[idle,idle,idle,idle] | S5:Service[idle,idle,idle,idle].(V|P.(Q|W)|O) | S6:Service[idle,idle,idle,idle] | $0"
    var testPre2 = TermParser.apply(prefixcompare2)

    var checkResult: Set[Node] = b.getNodeParent(testPre2);
    println("size: " + checkResult.size);
    checkResult.map(it => {
      println("Node: " + it.ctrl.name + " " + it.name)
      println("Parent: " + it.parent)
    })
    var linkres = b.getCtrlInstances(testPre2)
    println(linkres)
  }

}