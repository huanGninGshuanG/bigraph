package org.bigraph.bigsim.model

import java.util
import java.util.{Collections, Comparator, Optional}

import org.bigraph.bigsim.BRS.{CSPMatch, CSPMatcher, InstantiationMap, Rewrite, SharedCSPMatcher, SharedRewrite}
import org.bigraph.bigsim.exceptions.{IncompatibleInterfaceException, IncompatibleSignatureException}
import org.bigraph.bigsim.model.component.shared._
import org.bigraph.bigsim.model.component.{BigraphHandler, Edge, Handle, InnerName, OuterName, Point, Root, Signature, Site}
import org.bigraph.bigsim.parser.{BGMParser, BGMTerm}
import org.bigraph.bigsim.simulator.{BuildKripkeStructure, LTLSimulator, TransitionSystem}
import org.bigraph.bigsim.utils.{BigraphToTerm, CachingProxy, DebugPrinter}
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.JavaConversions._

/**
 * @author huangningshuang
 * @date 2024/1/20
 */

object SharedBigraph {
  def logger: Logger = LoggerFactory.getLogger(this.getClass)

  def makeEmpty(signature: Signature): SharedBigraph = {
    val bigraph = new SharedBigraph(signature)
    bigraph
  }

  def juxtapose(lhs: SharedBigraph, rhs: SharedBigraph): SharedBigraph = {
    if (lhs == rhs)
      throw new IllegalArgumentException("A bigraph can not juxtapose with itself.")
    if (!(lhs.bigSignature == rhs.bigSignature)) throw new IncompatibleSignatureException(lhs.bigSignature, rhs.bigSignature)
    if (!Collections.disjoint(lhs.bigInner.keySet, rhs.bigInner.keySet))
      throw new IncompatibleInterfaceException(new UnsupportedOperationException("joint name juxtapose: " + lhs.bigInner.keySet() + " and " + rhs.bigInner.keySet()))
    if (!Collections.disjoint(lhs.bigOuter.keySet, rhs.bigOuter.keySet))
      throw new IncompatibleInterfaceException(new UnsupportedOperationException("joint name juxtapose: " + lhs.bigOuter.keySet() + " and " + rhs.bigOuter.keySet()))
    val l: SharedBigraph = lhs.clone()
    val r: SharedBigraph = rhs.clone()
    val edges = r.getEdges
    l.onEdgeAdded(edges)
    l.onSharedNodeAdded(r.getSharedNodes)
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

  def makeId(signature: Signature, width: Integer, names: util.ArrayList[String]): SharedBigraph = {
    val bigraph = new SharedBigraph(signature)
    val builder: BigraphBuilder = new BigraphBuilder(signature)
    builder.setBigraph(bigraph)
    for (_ <- 0 until width) {
      builder.addSharedSite(builder.addSharedRoot())
    }
    for (name <- names) {
      builder.addInnerName(name, builder.addOuterName(name))
    }
    builder.makeBigraph(true).asInstanceOf[SharedBigraph]
  }
}

class SharedBigraph(signature: Signature) extends Bigraph {

  super.setSignature(signature)

  // 共享偶图数据结构
  var sharedRoots: util.List[SharedRoot] = new util.ArrayList[SharedRoot]()
  var sharedSites: util.List[SharedSite] = new util.ArrayList[SharedSite]()

  private var ancestors: util.Map[SharedChild, util.Collection[SharedParent]] = new util.WeakHashMap[SharedChild, util.Collection[SharedParent]]()

  private final val sharedroRoots: util.List[SharedRoot] = sharedRoots
  private final val sharedroSites: util.List[SharedSite] = sharedSites

  def findSharedSite(name: String): Optional[SharedSite] = {
    var res: SharedSite = null
    for (s <- sharedSites) {
      if (s.getName.equals(name)) {
        if (res != null) throw new RuntimeException("two sites with the same name: " + name)
        res = s
      }
    }
    Optional.ofNullable(res)
  }

  def findSharedNode(name: String, ctrlName: String): Optional[SharedNode] = {
    var res: SharedNode = null
    for (s <- sharedNodesProxy.get()) {
      if (s.getName.equals(name) && s.getControl.getName.equals(ctrlName)) {
        if (res != null) throw new RuntimeException("two nodes with the same name: " + name)
        res = s
      }
    }
    Optional.ofNullable(res)
  }

  override def matchRule(r: ReactionRule): util.Set[(Bigraph, ReactionRule)] = {
    DebugPrinter.print(logger, "shared bigraph match" + r.redex)
    val redex: SharedBigraph = r.redexBig.asInstanceOf[SharedBigraph]
    val reactum: SharedBigraph = r.reactumBig.asInstanceOf[SharedBigraph]
    val eta: InstantiationMap = r.eta // 共享偶图衍化目前不支持参数化反应规则
    // redex和reactum会乱序，这里按照$0重排序
    Collections.sort(redex.sharedSites, (o1: SharedSite, o2: SharedSite) => {
      o1.getName.compareTo(o2.getName)
    })
    Collections.sort(reactum.sharedSites, (o1: SharedSite, o2: SharedSite) => {
      o1.getName.compareTo(o2.getName)
    })
    DebugPrinter.print(logger, "- REACTUM -----------------------------")
    reactum.print()
    val matcher: SharedCSPMatcher = new SharedCSPMatcher()
    val iter = matcher.`match`(this, redex).iterator()
    val result = new util.HashSet[(Bigraph, ReactionRule)]()
    while (iter.hasNext) {
      val pMatch = iter.next()
      val nb = SharedRewrite.rewrite(pMatch, redex, reactum)
      nb.rules = rules
      result.add((nb, r))
    }
    result
  }

  def getAncestors(child: SharedChild): util.Collection[SharedParent] = {
    if (child == null)
      throw new IllegalArgumentException("the argument can't be null")
    var s = ancestors.get(child)
    if (s == null) {
      val parents = child.getParents
      s = new util.HashSet[SharedParent]()
      for (parent <- parents) {
        if (parent.isNode) {
          s.add(parent)
          s.addAll(getAncestors(parent.asInstanceOf[SharedChild]))
        }
      }
      ancestors.put(child, s)
    }
    s
  }

  // bigraph的所有nodes，通过softReference缓存
  var sharedNodesProxy: CachingProxy[util.Collection[SharedNode]] = new CachingProxy[util.Collection[SharedNode]](
    () => provideSharedNodes
  )

  def getSharedNodes: util.Collection[SharedNode] = {
    sharedNodesProxy.get()
  }

  override def provideEdges: util.Collection[Edge] = {
    val nodes = getSharedNodes
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

  def provideSharedNodes: util.Collection[SharedNode] = {
    val s: util.Set[SharedNode] = new util.HashSet[SharedNode]()
    val q: util.Queue[SharedNode] = new util.LinkedList[SharedNode]()
    for (r <- sharedRoots) {
      for (c <- r.getChildren) {
        if (c.isNode) q.add(c.asInstanceOf[SharedNode])
      }
    }
    while (q.nonEmpty) {
      val node: SharedNode = q.poll()
      s.add(node)
      for (c <- node.getChildren) {
        if (c.isNode) q.add(c.asInstanceOf[SharedNode])
      }
    }
    s
  }

  def onSharedNodeAdded(node: SharedNode): Unit = {
    val nodes = sharedNodesProxy.softGet()
    if (nodes != null) nodes.add(node)
  }

  def onSharedNodeAdded(nodes: util.Collection[SharedNode]): Unit = {
    val ns = sharedNodesProxy.softGet()
    if (ns != null) ns.addAll(nodes)
  }

  def onSharedNodeSetChanged(): Unit = {
    this.sharedNodesProxy.invalidate()
    this.ancestors.clear()
  }

  def onSharedNodeRemoved(node: SharedNode): Unit = {
    ancestors.clear() // conservative
    val nodes = sharedNodesProxy.softGet()
    if (nodes != null) nodes.remove(node)
  }

  def onSharedNodeRemoved(nodes: util.Collection[SharedNode]): Unit = {
    ancestors.clear() //conservative
    val ns = sharedNodesProxy.softGet()
    if (ns != null) ns.removeAll(nodes)
  }

  override def print(): Unit = {
    DebugPrinter.print(logger, "Shared bigraph Info:")
    for (inner <- bigInner) DebugPrinter.print(logger, "Inner: " + inner)
    for (outer <- bigOuter) DebugPrinter.print(logger, "Outer: " + outer)
    for (site <- sharedSites) DebugPrinter.print(logger, "SharedSite: " + site)
    for (root <- sharedRoots) DebugPrinter.print(logger, "Root: " + root)
    for (node <- sharedNodesProxy.get()) DebugPrinter.print(logger, "SharedNode: " + node)
    for (edge <- edgesProxy.get()) DebugPrinter.print(logger, "Edge: " + edge)
  }

  /// check whether the bigraph is consistent
  override def isConsistent: Boolean = {
    val seenPoint: util.HashSet[Point] = new util.HashSet[Point]()
    val seenHandles: util.HashSet[Handle] = new util.HashSet[Handle]()
    val unseenSites: util.HashSet[SharedSite] = new util.HashSet[SharedSite]()
    unseenSites.addAll(sharedroSites)
    val q = new util.ArrayDeque[SharedParent]()
    for (r <- sharedRoots) {
      q.add(r)
    }
    while (q.nonEmpty) {
      val parent = q.poll()
      for (c <- parent.getChildren) {
        // 父子节点不对应
        if (!c.getParents.contains(parent)) {
          DebugPrinter.err(logger, "INCOSISTENCY: parent/child mismatch")
          return false
        }
        if (c.isNode) {
          val node = c.asInstanceOf[SharedNode]
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
          val site = c.asInstanceOf[SharedSite]
          unseenSites.remove(site)
          if (!sharedSites.contains(site)) {
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
  override def structToTerm(force: Boolean = true): Term = {
    if (force || root == null || root.toString.contains("idle")) root = BigraphToTerm.toTerm(this)
    root
  }

  /// 按照名字排序所有内部名，外部名，节点，最后重命名所有边。主要用于hash计算，比较两个偶图是否相等
  @Override
  override def trimBigraph(): Unit = {
    val nodes = new util.ArrayList[SharedNode](sharedNodesProxy.get())
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

  private val nodeComparator = new Comparator[SharedNode] {
    override def compare(o1: SharedNode, o2: SharedNode): Int = o1.getName.compareTo(o2.getName)
  }

  @Override
  override def clone(): SharedBigraph = {
    var big: SharedBigraph = new SharedBigraph(this.bigSignature)
    val hndDict: util.Map[Handle, Handle] = new util.HashMap[Handle, Handle]()
    val nodeDict: util.Map[SharedNode, SharedNode] = new util.HashMap[SharedNode, SharedNode]()
    val siteDict: util.Map[SharedSite, SharedSite] = new util.HashMap[SharedSite, SharedSite]()
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
    var q: util.Queue[(SharedParent, SharedChild)] = new util.LinkedList[(SharedParent, SharedChild)]()
    for (r <- sharedRoots) {
      val copy = r.replicate()
      big.sharedRoots.add(copy)
      for (c <- r.getChildren) {
        q.add((copy, c))
      }
    }
    val sites: util.List[SharedSite] = new util.ArrayList[SharedSite]()
    for (_ <- 0 until this.sharedSites.size()) {
      sites.add(new SharedSite())
    }
    while (!q.isEmpty) {
      val t = q.poll()
      if (t._2.isNode) {
        val n1: SharedNode = t._2.asInstanceOf[SharedNode]
        var n2: SharedNode = nodeDict.get(n1)
        if (n2 == null) {
          n2 = n1.replicate
          nodeDict.put(n1, n2)
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
        }
        n2.addParent(t._1)
      } else {
        val s1: SharedSite = t._2.asInstanceOf[SharedSite]
        var s2: SharedSite = siteDict.get(s1)
        if (s2 == null) {
          s2 = s1.replicate()
          siteDict.put(s1, s2)
        }
        s2.addParent(t._1)
        sites.set(this.sharedSites.indexOf(s1), s2)
      }
    }
    big.sharedSites.addAll(sites)
    big
  }

  override def findAllMatchByCSP(): util.Set[(Bigraph, ReactionRule)] = {
    val result = new util.HashSet[(Bigraph, ReactionRule)]()
    for (r <- rules) {
      result.addAll(this.matchRule(r))
    }
    result
  }

  override def getNodes: util.Collection[component.Node] = {
    throw new UnsupportedOperationException("should use getSharedNodes")
  }

  override def getRoots: util.List[_ <: Root] = {
    throw new UnsupportedOperationException("should use getSharedRoots")
  }

  override def getSites: util.List[_ <: Site] = {
    throw new UnsupportedOperationException("should use getSharedSites")
  }

  def getSharedRoots: util.List[_ <: SharedRoot] = {
    sharedroRoots
  }

  def getSharedSites: util.List[_ <: SharedSite] = {
    sharedroSites
  }
}

object testSharedBigraph {
  def logger: Logger = LoggerFactory.getLogger(this.getClass)
  def main(args: Array[String]): Unit = {
    var shareTest =
      """
        |# Controls
        |%active A : 0;
        |%active B : 1;
        |
        |# Rules
        |%rule r_0 $0|u1:A.($1|u3:B[a:outername]) -> $0|u1:A.($1|u3:B[a:outername]|u4:B[a:outername]){};
        |
        |# prop
        |%prop p  a:CriticalSection[a:edge].(b:Process[a:edge] | $0){};
        |
        |
        |# Model
        |%agent  u0:A.(u2:B[a:edge])|u1:A.(u2:B[a:edge]|u3:B[a:edge]){};
        |
        |%mode ShareMode
        |
        |# LTL_Formula
        |%ltlSpec G!(p);
        |
        |#SortingLogic
        |
        |
        |# Go!
        |%check;
        |""".stripMargin
    var beyondCorp =
      """
        |# Controls
        |%active Device : 1;
        |%active Data : 0;
        |%active ACE : 1;
        |%active UserDB : 0;
        |%active DeviceDB : 0;
        |%active Did : 0;
        |%active Uid : 0;
        |%active UserInfo : 0;
        |%active DeviceInfo : 0;
        |%active AP : 3;
        |%active Business : 1;
        |%active Staging : 0;
        |%active Active : 0;
        |%active Request : 0;
        |%active Connection : 0;
        |%active DataTag : 0;
        |
        |# Names
        |
        |# Rules
        |%rule r_0 D1:Device[a:outername].(u1:UserInfo|d1:DeviceInfo) | ap:AP[a:outername, b:outername, c:outername].(sta:Staging.$0|$1) -> D1:Device[a:outername] | ap:AP[a:outername, b:outername, c:outername].(sta:Staging.($0|reqD1:Request.(u1:UserInfo|d1:DeviceInfo|data1:DataTag))|$1){};
        |%rule r_1 ap:AP[a:outername, b:edge, c:outername].(sta:Staging.($0|reqD1:Request.(u1:UserInfo|d1:DeviceInfo|data1:DataTag))|act:Active.$1) | ace:ACE[b:edge].(udb:UserDB.(u1:Uid|$2) | ddb:DeviceDB.(d1:Did|$3)) | ba:Business[c:outername].(u1:Uid|data1:Data|$4) -> ap:AP[a:outername, b:edge, c:outername].(sta:Staging.$0|act:Active.($1|connD1:Connection.(u1:UserInfo|d1:DeviceInfo|data1:DataTag))) | ace:ACE[b:edge].(udb:UserDB.(u1:Uid|$2) | ddb:DeviceDB.(d1:Did|$3)) | ba:Business[c:outername].(u1:Uid|data1:Data|$4){};
        |%rule r_2 D1:Device[a:outername] | ap:AP[a:outername, b:outername, c:outername].($0|act:Active.($1|connD1:Connection.(u1:UserInfo|d1:DeviceInfo|data1:DataTag))) -> D1:Device[a:outername].(u1:UserInfo|d1:DeviceInfo) | ap:AP[a:outername, b:outername, c:outername].($0|act:Active.$1){};
        |
        |# prop
        |
        |# Model
        |%agent D1:Device[a:edge].(u1:UserInfo|d1:DeviceInfo) | ap:AP[a:edge, b:edge, c:edge].(sta:Staging|act:Active) | ace:ACE[b:edge].(udb:UserDB.(u1:Uid|u2:Uid) | ddb:DeviceDB.d1:Did) | ba:Business[c:edge].(u1:Uid|u2:Uid|data1:Data){};
        |# %agent D1:Device[a:edge] | ap:AP[a:edge, b:edge, c:edge].(sta:Staging|act:Active.connD1:Connection.(u1:UserInfo|d1:DeviceInfo|data1:DataTag)) | ace:ACE[b:edge].(udb:UserDB.(u1:Uid|u2:Uid) | ddb:DeviceDB.(d1:Did)) | ba:Business[c:edge].(u1:Uid|u2:Uid|data1:Data){};
        |# %agent D1:Device[a:edge] | ap:AP[a:edge, b:edge, c:edge].(sta:Staging.reqD1:Request.(u1:UserInfo|d1:DeviceInfo|data1:DataTag)|act:Active) | ace:ACE[b:edge].(udb:UserDB.(u1:Uid|u2:Uid) | ddb:DeviceDB.(d1:Did)) | ba:Business[c:edge].(u1:Uid|u2:Uid|data1:Data){};
        |
        |%mode ShareMode
        |
        |# CTL_Formula
        |
        |#SortingLogic
        |
        |
        |# Go!
        |%check;
        |""".stripMargin
    val t = BGMParser.parseFromString(beyondCorp)
    val b = BGMTerm.toBigraph(t)
    DebugPrinter.print(logger, "dot file is:")
    val simulator = new LTLSimulator(b)
    simulator.simulate
    val str = simulator.dumpDotForward("")
    DebugPrinter.print(logger, "dot file is:" + str)
//    val transition = new TransitionSystem(b)
//    println("=-=-=-=-=-==-=-=-=-=-=-==-=-=-=-=-=-==-=-=-=- now to build KripStructure-=-=-=-=-=-=-==-=-=-=-=-=-=-=-=-")
//    val buildKripke = new BuildKripkeStructure(transition)
//    val kripke = buildKripke.buildKripke(b.bigSignature)


    //    val p: List[BGMTerm] = BGMParser.parseFromString(beyondCorp)
    //
    //    def logger = LoggerFactory.getLogger(this.getClass)
    //
    //    val b = BGMTerm.toBigraph(p);
    //    b.print()
    //    b.rules.foreach(x => {
    //      DebugPrinter.print(logger, "rule: " + x)
    //      b.matchRule(x)
    //    })
  }
}
