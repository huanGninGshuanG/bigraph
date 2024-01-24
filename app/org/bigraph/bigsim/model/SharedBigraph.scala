package org.bigraph.bigsim.model

import java.util
import java.util.Optional

import org.bigraph.bigsim.BRS.{CSPMatch, CSPMatcher, InstantiationMap, Rewrite, SharedCSPMatcher}
import org.bigraph.bigsim.model.component.shared._
import org.bigraph.bigsim.model.component.{BigraphHandler, Edge, Handle, InnerName, OuterName, Point, Root, Signature, Site}
import org.bigraph.bigsim.utils.{CachingProxy, DebugPrinter}
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.JavaConversions._

/**
 * @author huangningshuang
 * @date 2024/1/20
 */
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

  def findSharedNode(name: String): Optional[SharedNode] = {
    var res: SharedNode = null
    for (s <- sharedNodesProxy.get()) {
      if (s.getName.equals(name)) {
        if (res != null) throw new RuntimeException("two nodes with the same name: " + name)
        res = s
      }
    }
    Optional.ofNullable(res)
  }

  override def matchRule(r: ReactionRule): util.Set[(Bigraph, ReactionRule)] = {
    DebugPrinter.print(logger, "shared bigraph match")
    val redex: SharedBigraph = r.redexBig.asInstanceOf[SharedBigraph]
    val reactum: SharedBigraph = r.reactumBig.asInstanceOf[SharedBigraph]
    val eta: InstantiationMap = r.eta
    DebugPrinter.print(logger, "- REACTUM -----------------------------")
    val matcher: SharedCSPMatcher = new SharedCSPMatcher()
    val iter = matcher.`match`(this, redex).iterator()
    val result = new util.HashSet[(Bigraph, ReactionRule)]()
    while (iter.hasNext) {
      iter.next()
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
    for (site <- sharedSites) DebugPrinter.print(logger, "Site: " + site)
    for (root <- sharedRoots) DebugPrinter.print(logger, "Root: " + root)
    for (node <- sharedNodesProxy.get()) DebugPrinter.print(logger, "Node: " + node)
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
  override def clone(): SharedBigraph = {
    var big: SharedBigraph = new SharedBigraph(this.bigSignature)
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
        val n2: SharedNode = n1.replicate
        n2.addParent(t._1)
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
        val s1: SharedSite = t._2.asInstanceOf[SharedSite]
        val s2: SharedSite = s1.replicate()
        s2.addParent(t._1)
        sites.set(this.sharedSites.indexOf(s1), s2)
      }
    }
    big.sharedSites.addAll(sites)
    big
  }

  override def findAllMatchByCSP(): util.Set[(Bigraph, ReactionRule)] = {
    val result = new util.HashSet[(Bigraph, ReactionRule)]()
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
