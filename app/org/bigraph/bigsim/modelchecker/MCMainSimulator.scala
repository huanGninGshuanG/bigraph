package org.bigraph.bigsim.modelchecker

import domain.{PkuResource, PkuStrategy}
import org.bigraph.bigsim.BRS.{Graph, Match, Vertex}
import org.bigraph.bigsim.model.{BiNode, Bigraph, ReactionRule}
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.mutable.{Map, Set}


/**
 * @author amy
 */
object MCMainSimulator {
  var matchDiscard: Set[Match] = Set();
  var v: Vertex = _
  var g: Graph = _
  var start: Long = System.currentTimeMillis()
  var middle: Long = 0
  var end: Long = 0

  def matchMarkDelete(m: Match): Unit = {
    assert(m != null);
    matchDiscard.add(m);
  }

  def matchGC: Unit = {
    matchDiscard.clear();
  }
}

class MCMainSimulator(val b: Bigraph) extends MCSimulator {
  def logger : Logger = LoggerFactory.getLogger(this.getClass)

  MCMainSimulator.v = new Vertex(b, null, null);
  MCMainSimulator.g = new Graph(MCMainSimulator.v);
  
  def simulate(pkuResource: PkuResource, strategy : PkuStrategy): String = {
    if (b == null || b.root == null) {
      logger.debug("MCMainSimulator::simulate(): initial bigraph is null");
      return ""
    }
    
    var rules: Set[ReactionRule] = b.rules
    if (rules == null || rules.size == 0) {
      logger.debug("MCMainSimulator::simulate(): no reaction rules");
    }
    
    var enumFeature = new MCEnumFeature(b)
    // set the head of the BiNode, initial Bigraph
    var biNode: BiNode = new BiNode(b,Map());
    BiNode.head = biNode
    BiNode.addBiNode(biNode)
    
    while (enumFeature.step()) {
      MCSimulator.matchGC
    }
    logger.debug("Graph: " + MCMainSimulator.g.lut.size);

    // MCMainSimulator.g.dumpPath //打印到data和path文件

    val dotStr = MCMainSimulator.g.dumpDotFile //打印到dot文件

    MCMainSimulator.matchGC; //Test，可能无用

    dotStr
  }
 
  def dumpDotForward(dot: String): String = ""
}
