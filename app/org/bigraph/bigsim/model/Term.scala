package org.bigraph.bigsim.model

import java.util.Objects

import org.bigraph.bigsim.BRS._
import org.bigraph.bigsim.model.Paraller.logger
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.immutable.List
import scala.collection.mutable.{Queue, Set}
import org.bigraph.bigsim.utils.GlobalCfg

import scala.collection.mutable

/**
 * @author tanch
 *         version 0.1
 */
object TermType {
  val TPREF: Int = 1;
  val TPAR: Int = 2;
  val THOLE: Int = 4;
  val TNIL: Int = 8;
  val TREGION: Int = 16;
  val TNUM: Int = 32;

  def typeToString(termType: Int): String = {
    termType match {
      case TermType.TPREF => "Prefix";
      case TermType.TPAR => "Paraller";
      case TermType.THOLE => "Hole";
      case TermType.TNIL => "Nil";
      case TermType.TREGION => "Regions";
      case TermType.TNUM => "Num";
      case _ => "Undefined TermType";
    }
  }
}

class T {
  def A = {
    var a = Term.uTermIncrement;
    a
  }
}

object Term {
  private var uTerm: Int = 1;

  def uTermIncrement: Long = {
    uTerm += 1;
    uTerm;
  }
}

class Term {

  def logger: Logger = LoggerFactory.getLogger(this.getClass)

  var termType: Int = 0;
  /**
   * use Queue in Scala to replace the deque in C++
   * Queue.enqueue() to replace deque->push_back()
   * Queue.dequeue() to replace deque->pop_front
   */
  var remaining: Queue[Term] = Queue();
  remaining.enqueue(this);
  var parent: Term = null;
  var id: Long = Term.uTermIncrement;

  var directParent: Term = null; // add by kgq 20220317  为了方便删除某个Term，要获得某个Term的 直接双亲的信息。

  override def clone: Term = {
    var t = new Term()
    t.termType = termType
    t.remaining = remaining
    t.parent = parent
    t.id = id
    t.directParent = directParent
    t
  }

  override def toString = TermString.preOrderString(this);

  def size: Int = 0;

  /**
   *
   * @return
   * kongguanqiao：我就是来写个注释
   * Term中维护了一个工作队列，当查找下一个的时候，就取队首Term，
   * 对于Prefix， Paraller， Region结构，分别把它们的子结构放入到工作队列中去。
   * next的使用，可以参考Matcher.tryMatchTermReactionRule
   * 虽然Term是递归定义的，而且工作队列remaining是非静态的，但是在使用这个队列的时候，只会使用到根节点的这个队列，问题不大。
   *
   */
  def next: Term = {
    if (remaining.size == 0) return null;
    val t: Term = remaining.dequeue();

    t.termType match {
      case TermType.TPREF => {
        // 递归定义的，过滤掉纯nil
        var tp: Term = t.asInstanceOf[Prefix].suffix;
        if (tp.termType != TermType.TNIL) {
          remaining.enqueue(tp);
        }
      }
      case TermType.TPAR => {
        val tp: Paraller = t.asInstanceOf[Paraller];
        tp.getChildren.map(remaining.enqueue(_));
      }
      case TermType.TREGION => {
        val tr: Regions = t.asInstanceOf[Regions];
        tr.getChildren.map(remaining.enqueue(_));
      }
      case TermType.THOLE => {};
      case TermType.TNIL => {};
      case _ => {
        //logger.debug("Matching encountered invalid term type " + t.termType);
        sys.exit(1);
      }
    }
    t;
  }

  def reset: Unit = {
    remaining.clear();
    remaining.enqueue(this);
  }

  def checkIsAnonymous(): Boolean = {
    return false
  }

  def activeContext: Boolean = {
    if (parent == null) true;
    else parent.activeContext;
  }

  def overlap(other: Term): Boolean = {
    if (other == null) false;
    else if (other == this) true;
    else overlap(other.parent);
  }

  def applyMatch(m: Match): Term = {
    this;
  }

  def instantiate(m: Match): Term = {
    null;
  }

  def getAllNames: List[Name] = List();

  //for test
  val remainingTest: Queue[Int] = Queue();

  //  override def equals(o: Any): Boolean = {
  //    if (o == null || (getClass != o.getClass)) return false
  //    val other = o.asInstanceOf[Term]
  //    Objects.equals(other.id,id)
  //  }
  //
  //  override def hashCode: Int = id.asInstanceOf[Int]
}

object Paraller {
  def logger: Logger = LoggerFactory.getLogger(this.getClass)

  def constuctParaller(terms: Set[Term]): Paraller = {
    if (terms == null || terms.size < 2) {
      //logger.debug("Error param terms to construct Paraller");
      sys.exit(1);
    } else if (terms.size == 2) {
      new Paraller(terms.head, terms.tail.head);
    } else {
      new Paraller(terms.head, constuctParaller(terms.tail))
    }
  }
}

// leftTerm | rightTerm
class Paraller(sid: Long, lt: Term, rt: Term) extends Term {

  termType = TermType.TPAR;
  id = sid;
  var leftTerm = lt;
  var rightTerm = rt;
  leftTerm.parent = this;
  rightTerm.parent = this;

  def this(lt: Term, rt: Term) = this(Term.uTermIncrement, lt, rt);

  override def size = leftTerm.size + rightTerm.size;

  def getChildren: Set[Term] = {
    var terms: Set[Term] = Set();
    if (leftTerm.termType == TermType.TPAR) {
      terms ++= leftTerm.asInstanceOf[Paraller].getChildren;
    } else {
      terms += leftTerm;
    }
    if (rightTerm.termType == TermType.TPAR) {
      terms ++= rightTerm.asInstanceOf[Paraller].getChildren;
    } else {
      terms += rightTerm;
    }
    terms;
  }


  /**
   * 这个函数是为了修复bug提供的，在 applymatch的过程中，当前 Paraller结构匹配了redex的根。
   * 函数的作用是，将匹配内容之外的term保留下来，和生成物的实例化结果一起组成最后结果
   *
   * @param m 匹配结果
   * @param r 生成物 reactum的实例化结果
   * @return
   * @author kongguanqiao 2022.04.10
   */
  def addExtraContent(m: Match, r: Term): Term = {
    if (GlobalCfg.DEBUG) logger.debug("Paraller.applyMatch, add extra content")
    var subterm = this.getChildren

    subterm = subterm.filter(x => { // 将已经匹配到的 child 过滤掉，只保留其余部分
      !m.mapping.contains(x)
    })

    if (subterm.isEmpty) { // 如果没有其余部分，则直接返回 r
      return r
    } else {
      var retTerm = r // 如果存在其余部分，则将其余部分 applymatch，并将结果和r并置到一起
      subterm.foreach(x => {
        retTerm = new Paraller(retTerm, x.applyMatch(m)) // 新构造Paraller结构
      })
      return retTerm
    }
  }

  override def applyMatch(m: Match) = {

    if (GlobalCfg.DEBUG) logger.debug("Paraller.applyMatch, m.root= " + m.root + " m.root.id= " + m.root.id + " curterm is " + this + " cur term id is " + id)
    //递归可能存在错误，需再仔细斟酌 m.root是否可能为空？
    if (id == m.root.id) {
      //var r: Term = m.rule.reactum;//关键的关键 lry
      var r: Term = m.rule.reactum.instantiate(m); //是否原地修改?lry delete 2018.3.30  // recovered by kgq 20220228，原为 上一行，现恢复为此条语句；否则会导致applymatch后的偶图存在“匿名节点”（如果reactum中存在“匿名节点”）
      //r = r1;
      if (GlobalCfg.DEBUG) logger.debug("\tParaller.applyMatch, 调用 m.rule.reactum.instantiate(m), result is: " + r)
      //logger.debug("instantiate()执行后Term r========"+r);
      //logger.debug("before before3.2="+BiNode.allBiNodes);//lry
      if (parent == null && m.getParam(999999) != null && r != null) {
        if (r.termType != TermType.TPAR) {
          r;
        } else {
          //logger.debug("before before3.3="+BiNode.allBiNodes);//lry
          var rp: Term = r.asInstanceOf[Paraller];
          var ht: Term = m.getParam(999999).instantiate(null);
          if (ht.termType == TermType.TNIL) {
            rp;
          } else {
            new Paraller(id, rp, ht);
          }
        }
      } else {
        addExtraContent(m, r);
        //r;
      }
    } else {
      //logger.debug("before before3.4="+BiNode.allBiNodes);//lry
      var lt: Term = leftTerm.applyMatch(m);
      //logger.debug("before before3.5="+BiNode.allBiNodes);//lry
      var rt: Term = rightTerm.applyMatch(m);
      //logger.debug("before before3.6="+BiNode.allBiNodes);//lry
      if (lt.termType == TermType.TNIL || rt.termType == TermType.TNIL) {
        if (lt.termType == TermType.TNIL) rt;
        else lt;
      } else {
        new Paraller(id, lt, rt);
      }
    }
  }

  override def instantiate(m: Match) = {
    //if (GlobalCfg.DEBUG) logger.debug("[kgq] Paraller.instantiate")
    if (GlobalCfg.DEBUG) logger.debug("Paraller.instantiate, cur term is: " + this + " leftTerm: " + leftTerm + " rightTerm: " + rightTerm)
    //    logger.debug("Paraller.instantiate: leftterm= " + leftTerm + "rightTerm= " + rightTerm)
    // 注意过滤Nil
    //logger.debug("this Term="+this+"leftTerm="+leftTerm+"rightTerm"+rightTerm);//lry
    //logger.debug("before before3.1.1="+BiNode.allBiNodes);//lry
    var lt: Term = leftTerm.instantiate(m);
    if (GlobalCfg.DEBUG) logger.debug("\tParaller.instantiate, leftTerm.instantiate(m), result is: " + lt)
    //logger.debug("before before3.1.2="+BiNode.allBiNodes);//lry
    var rt: Term = rightTerm.instantiate(m);
    if (GlobalCfg.DEBUG) logger.debug("\tParaller.instantiate, rightTerm.instantiate(m), result is: " + rt)
    //logger.debug("before before3.1.3="+BiNode.allBiNodes);//lry
    if (lt.termType == TermType.TNIL || rt.termType == TermType.TNIL) {
      if (lt.termType != TermType.TNIL) lt;
      else if (rt.termType != TermType.TNIL) rt;
      else null;
    } else {
      new Paraller(lt, rt);
    }
    //c++版本的一个bug：这里不能带id——对于Term t1,Rule r的reactum调用instantiate的时候，其id会传给新的term t2（t2是t1的一部分），
    //若t1的其他部分还使用r进行反应，则instantiate的时候会导致t2部分也重新被instantiate,因为其id与math的id相同
    //new Paraller(id, lt, rt);
  }

  override def getAllNames: List[Name] = {
    return leftTerm.getAllNames ++ rightTerm.getAllNames
  }

  /**
   * add by kgq 20220302
   *
   * @return
   */
  override def clone(): Paraller = {
    var newlt: Term = leftTerm.clone()
    var newrt: Term = rightTerm.clone()
    new Paraller(id, newlt, newrt)
  }

  override def checkIsAnonymous(): Boolean = {
    return leftTerm.checkIsAnonymous && rightTerm.checkIsAnonymous
  }
}

// leftTerm || rightTerm
class Regions(sid: Long, lt: Term, rt: Term) extends Term {
  termType = TermType.TREGION;
  id = sid;
  var leftTerm = lt;
  var rightTerm = rt;
  leftTerm.parent = this;
  rightTerm.parent = this;

  def this(lt: Term, rt: Term) = this(Term.uTermIncrement, lt, rt);

  override def size = leftTerm.size + rightTerm.size;

  def getChildren: List[Term] = {
    var terms: List[Term] = List(); // comment by kgq 20220309 因为Regions有顺序，所以要以列表的形式保留下来
    if (leftTerm.termType == TermType.TREGION) {
      terms ++= leftTerm.asInstanceOf[Regions].getChildren;
    } else {
      terms = terms.:+(leftTerm);
    }

    if (rightTerm != null) {
      if (rightTerm.termType == TermType.TREGION) {
        terms ++= rightTerm.asInstanceOf[Regions].getChildren;
      } else {
        terms = terms.:+(rightTerm);
      }
    }
    terms;
  }

  override def applyMatch(m: Match) = {
    //    println("[kgq] curterm: ", this, " Region.applyMatch")
    //if (GlobalCfg.DEBUG) logger.debug("[kgq] Region.applyMatch")
    if (GlobalCfg.DEBUG) logger.debug("Region.applyMatch, cur Term is: " + this + " leftTerm is: " + leftTerm + " rightTerm is: " + rightTerm)
    if (id == m.root.id) { // 如果是递归的起点，那么直接返回的是
      m.rule.reactum.instantiate(m);
    } else {
      var lt: Term = leftTerm.applyMatch(m);
      if (GlobalCfg.DEBUG) logger.debug("\tRegion.applyMatch, leftTerm.applyMatch(m), result is: " + lt)
      var rt: Term = rightTerm.applyMatch(m);
      if (GlobalCfg.DEBUG) logger.debug("\tRegion.applyMatch, rightTerm.applyMatch(m), result is: " + rt)
      if (lt.termType == TermType.TNIL || rt.termType == TermType.TNIL) {
        if (lt.termType == TermType.TNIL) rt;
        else lt;
      } else {
        new Regions(id, lt, rt);
      }
    }
  }

  override def instantiate(m: Match) = {
    //if (GlobalCfg.DEBUG) logger.debug("[kgq] Regions.instantiate")
    if (GlobalCfg.DEBUG) logger.debug("Regions.instantiate, cur Term is: " + this + " leftTerm is: " + leftTerm + " rightTerm is: " + rightTerm)
    var lt: Term = leftTerm.instantiate(m);
    if (GlobalCfg.DEBUG) logger.debug("\tRegions.instantiate, leftTerm.instantiate(m) is " + lt)
    var rt: Term = rightTerm.instantiate(m);
    if (GlobalCfg.DEBUG) logger.debug("\tRegions.instantiate, rightTerm.instantiate(m) is: " + rt)
    new Regions(lt, rt); // kgq 这时候不用考虑 lt 或者 rt 有一个是 TermType.TNIL 吗？
  }

  override def getAllNames: List[Name] = {
    return leftTerm.getAllNames ++ rightTerm.getAllNames;
  }

  /**
   * add by kgq 20220302
   */
  override def clone(): Regions = {
    var newlt = leftTerm.clone()
    val newrt = rightTerm.clone()
    new Regions(id, newlt, newrt)
  }

  override def checkIsAnonymous(): Boolean = {
    true
  }
}

// control[ports].term
class Prefix(sid: Long, n: Node, suff: Term) extends Term {
  //class Prefix(count: Int, sid: Long, c: Control, ports: List[Name], suff: Term) extends Term {
  val node: Node = n
  /**
   * use List in Scala to replace the vector in C++
   * because Name may be place holder for Prefix
   */
  var suffix: Term = suff;
  if (suffix != null) {
    suffix.parent = this;
  }
  termType = TermType.TPREF;

  id = sid;

  // multiple constructors
  def this(n: Node, suff: Term) =
    this(Term.uTermIncrement, n, suff);

  override def size = {
    1 + suffix.size;
  }

  override def activeContext = {
    if (parent == null) node.active;
    else node.active && parent.activeContext;
  }

  override def applyMatch(m: Match) = {
    //    logger.debug("Prefix.applyMatch, m.root=" + m.root + "m.root.id=" + m.root.id)
    if (GlobalCfg.DEBUG) logger.debug("Prefix.applyMatch, curTerm is: " + this + " node is: " + node + " suffix is: " + suffix)
    if (id == m.root.id) {
      if (GlobalCfg.DEBUG) logger.debug("\tPrefix.applyMatch, id == m.root.id -> m.rule.reactum.instantiate(m)")
      m.rule.reactum.instantiate(m);
    } else {
      if (GlobalCfg.DEBUG) logger.debug("\tPrefix.applyMatch, id != m.root.id -> new Prefix(id, node, suffix,applyMatch(m)")
      new Prefix(id, node, suffix.applyMatch(m));
    }
  }

  //  def checkNode(n:Node, nodeMap:Map[Node, Node]): Boolean ={
  //    for(m<-nodeMap){
  //      if(n.ctrl.name==m._1.ctrl.name)
  //        return true
  //    }
  //    false
  //  }

  override def instantiate(m: Match) = {
    //    logger.debug("Prefix.instantiate: node=  " + node + "suffix= " + suffix)
    //if (GlobalCfg.DEBUG) logger.debug("[kgq] Prefix.instantiate")
    if (GlobalCfg.DEBUG) logger.debug("Prefix.instantiate, cur Term is: " + this + " node is: " + node + " suffix is: " + suffix)
    if (m == null) {
      new Prefix(node, suffix.instantiate(m));
    } else {
      var nport: List[Name] = List();
      for (name <- node.ports) { // kgq 将名字更新
        nport = nport.:+(m.getName(name));
      }
      if (GlobalCfg.DEBUG) logger.debug("\tPrefix.instantiate, update port list from: " + node.ports + " to: " + nport)

      var modelNode = node;
      //      logger.debug("Prefix.instantiate: m.rule.nodeMap is " + m.rule.nodeMap)

      //      if(!m.rule.nodeMap.contains(node)){
      //        for(nodeMapElement<-m.rule.nodeMap){
      //          if(node.id==nodeMapElement._1.id){
      //              modelNode=nodeMapElement._1
      //          }
      //        }
      //      }
      if (m.rule.nodeMap.contains(modelNode)) { //这里node应该是生成物的node，然后查hash表得到的是反应物的node
        var redexNode = m.rule.nodeMap(modelNode);
        if (GlobalCfg.DEBUG) logger.debug("\t\tPrefix.instantiate, find node in rr.redex: " + redexNode)
        //        if(!m.nodeMap.contains(redexNode)){
        //          for(nodeMapElement<-m.nodeMap){
        //            if(redexNode.id==nodeMapElement._1.id)
        //              redexNode=nodeMapElement._1
        //          }
        //        }
        if (m.nodeMap.contains(redexNode)) {
          // modelNode = m.nodeMap(redexNode)
          modelNode = m.nodeMap(redexNode).clone() // modified by kgq 20220302 解决 BUG3： applyMatch 后，原偶图被修改
          if (GlobalCfg.DEBUG) logger.debug("\t\t\tPrefix.instantiate, find bigraph node in old b: " + modelNode)
        };
      }
      modelNode.ports = nport

      if (m.assignValue.contains(node)) { // added by kgq 20220301 更新 instantiate 中的node 的数值
        modelNode.number = m.assignValue.getOrElse(node, null)
        //        logger.debug("\t\tPrefix.instantiate: m.nodeMap's number updated with: " + modelNode.number)
      }

      //      logger.debug("Prefix.instantiate new node is :" + modelNode)
      if (GlobalCfg.DEBUG) logger.debug("\tPrefix.instantiate, new Prefix(modelNode, suffix.instantiate(m), modelNode is: " + modelNode)
      new Prefix(modelNode, suffix.instantiate(m));
    }
  }

  override def getAllNames: List[Name] = {
    var names: List[Name] = List();
    node.ports.map(x => names = names.:+(x));
    if (suffix != null)
      names = names ++ suffix.getAllNames;
    return names;
  }

  /**
   * add by kgq 20220302
   */
  override def clone(): Prefix = {
    var t: Term = suffix.clone()
    var p = new Prefix(id, node.clone(), t)
    p.termType = termType
    p
  }

  override def checkIsAnonymous(): Boolean = {
    return suffix.checkIsAnonymous() && node.checkIsAnonymous()
  }
}

class Hole(idx: Int) extends Term {

  val index: Int = idx;
  termType = TermType.THOLE;
  id = Term.uTermIncrement;
  //add by lbj
  var parNode: Node = null

  override def size = 1;

  override def applyMatch(m: Match) = {
    //    println("[kgq] curterm: ", this, " hole.applyMatch")

    //if (GlobalCfg.DEBUG) logger.debug("[kgq] Hole.applyMatch")
    null;
  }

  override def instantiate(m: Match) = {
    //if (GlobalCfg.DEBUG) logger.debug("[kgq] Hole.instantiate")
    // todo
    if (m == null) {
      //logger.debug("ERROR: hole::apply_match(): Invalid place graph contains a hole.");
      sys.exit(1);
    }
    var t: Term = m.getParam(index);

    if (t == null) t = new Nil();

    t.instantiate(null);
  }

  override def clone: Hole = {
    var h = new Hole(index)
    h.termType = TermType.THOLE;
    h.id = Term.uTermIncrement;
    h
  }

  override def checkIsAnonymous(): Boolean = {
    return true
  }
}

class Num(v: Int) extends Term {
  val value: Int = v;
  termType = TermType.TNUM;
  id = Term.uTermIncrement;

  override def toString = "Int:" + value;

  override def size = 1;

  override def applyMatch(m: Match) = {
    //    println("[kgq] curterm: ", this, " Num.applyMatch")

    //if (GlobalCfg.DEBUG) logger.debug("[kgq] Num.applyMatch")
    if (id == m.root.id) {
      m.rule.reactum.instantiate(m);
    } else {
      this;
    }
  }

  override def instantiate(m: Match) = {
    //if (GlobalCfg.DEBUG) logger.debug("[kgq] Num.instantiate")
    this;
  }

  override def checkIsAnonymous(): Boolean = {
    return true
  }
}

class Nil extends Term {
  termType = TermType.TNIL;
  id = Term.uTermIncrement;

  override def size = 0;

  override def applyMatch(m: Match) = {
    //    println("[kgq] curterm: ", this, " nil.applyMatch")

    //if (GlobalCfg.DEBUG) logger.debug("[kgq] Nil.applyMatch")
    new Nil();
  }

  override def instantiate(m: Match) = {
    //if (GlobalCfg.DEBUG) logger.debug("[kgq] Nil.instantiate")
    new Nil();
  }

  override def clone: Nil = {
    var n = new Nil()
    n.termType = TermType.TNIL;
    n.id = Term.uTermIncrement;
    n
  }

  override def checkIsAnonymous(): Boolean = {
    return true
  }
}

object testHashMap {
  def main(args: Array[String]): Unit = {
    var t1 = new Term
    t1.id = 1
    var t2 = new Term
    t2.id = 2
    var t3 = new Term
    t3.id = 2

    var mp = mutable.HashMap[Term, String]()
    mp += (t1 -> "一")
    mp += (t2 -> "二")
    mp += (t3 -> "三")

    println(mp.size)
  }
}