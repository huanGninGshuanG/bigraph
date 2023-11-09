package org.bigraph.bigsim.model

import org.slf4j.{Logger, LoggerFactory}

import scala.collection.mutable.{ListBuffer, Map, Set}


/**
  * @author amy
  */

object BiNode {

  var head: BiNode = null

  var allBiNodes: ListBuffer[BiNode] = ListBuffer()

  def logger : Logger = LoggerFactory.getLogger(BiNode.getClass)

  def addBiNode(b: BiNode): Unit = {
    allBiNodes.append(b)
  }

  var curNode: BiNode = null

  def isContains(b: Bigraph): Boolean = {
    logger.debug("before compare:" + allBiNodes.size); //lry
    logger.debug("before compare:" + allBiNodes); //lry
    allBiNodes.foreach { x =>
      if ((x.bigraph.root).toString().equals(b.root.toString())) {
        curNode = x;
        return true;
      }
    }
    curNode = null;
    return false;
  }

  //add by lry
  def indexAt(b: Bigraph): Int = {
    var i = 0;
    allBiNodes.foreach { x =>
      if ((x.bigraph.root).toString().equals(b.root.toString())) {
        //println("--------------***----------------");
        //println((x.bigraph.root).toString());
        //println(b.root.toString());
        //println("--------------***----------------");
        return i;
      }
      i = i + 1;
    }
    return -1;
  }

  var activeTrans: Set[ReactionRule] = Set()

  def  printAllBiNode() = {
    logger.debug("BiNode的数量" + allBiNodes.size + "\n");
    allBiNodes.foreach { x =>
      logger.debug(x.toString())
    }
  }
}


class BiNode(b: Bigraph, ff: Map[ReactionRule, BiNode]) {
  val bigraph: Bigraph = b;
  //lry val
  var childList: ListBuffer[BiNode] = ListBuffer();
  var TSP: Map[ReactionRule, BiNode] = ff; //变迁-状态对
  var NodeToReactionRule: Map[BiNode, ReactionRule] = Map();
  //added by lry
  var ample: List[ReactionRule] = null;

  def addChild(node: BiNode) {
    childList.append(node)
  }

  var isTotalExpansion: Boolean = true; //默认完全展开

  def GetEnable: Map[ReactionRule, BiNode] = {
    return TSP;
  }

  def SetAmple(ample: List[ReactionRule]) = {
    this.ample = ample;
  }

  def GetAmple: List[ReactionRule] = {
    return ample;
  }

  override def toString = {
    val s: StringBuffer = new StringBuffer();
    s.append("BiNode: \n");
    s.append("当前节点:" + bigraph.root + "\n");
    /*for (key <- TSP.keySet.toArray) {
      s.append("变迁内容：" + key.toString() + "\t");
      s.append("变迁后Bigraph" + TSP.get(key));
    }*/
    s.append("子节点个数:" + childList.size + "\n");
    childList.foreach { x =>
      s.append("\tchild" + x.bigraph.root + "\n");
    }
    //   s.append("childList" + childList + "\n");
    //s.append("TSP" + TSP.toList + "\n");
    s.toString();
  }
}