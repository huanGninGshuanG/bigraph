package org.bigraph.bigsim.simulator

import java.util

import org.bigraph.bigsim.BRS.{Graph, Match, Vertex}
import org.bigraph.bigsim.Verify
import org.bigraph.bigsim.model.{Bigraph, BindingChecker, Nil, ReactionRule}
import org.bigraph.bigsim.modelchecker.CTLModelChecker
import org.bigraph.bigsim.parser.{BGMParser, BGMTerm}
import org.bigraph.bigsim.transitionsystem.State
import org.bigraph.bigsim.utils.GlobalCfg
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.mutable
import scala.collection.mutable.Buffer
import scala.collection.mutable.{Map, Queue, Set}
// 需要使用到java中的变量
import scala.collection.JavaConverters._
import scala.collection.JavaConversions._


object testBankMC {

  val bigraphExam =
    """
      |""".stripMargin




   val t = BGMParser.parseFromString(bigraphExam)
//  val t = BGMParser.parseFromString(s)
  val b = BGMTerm.toBigraph(t)._2
  def main(args: Array[String]): Unit = {
    val simulator = new CTLSimulator(b)
    simulator.simulate
    var dotStr = simulator.dumpDotForward("")
    println(dotStr)


    var paths = simulator.dumpPaths();
  }
}


