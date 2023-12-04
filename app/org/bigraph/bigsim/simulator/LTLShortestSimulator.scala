package org.bigraph.bigsim.simulator

import java.util

import org.bigraph.bigsim.BRS.{Graph, Match, Vertex}
import org.bigraph.bigsim.Verify
import org.bigraph.bigsim.ctlimpl.CTLCheckResult
import org.bigraph.bigsim.model.{Bigraph, BindingChecker, Nil, ReactionRule}
import org.bigraph.bigsim.modelchecker.{CTLModelChecker, LTLModelChecker}
import org.bigraph.bigsim.parser.{BGMParser, BGMTerm}
import org.bigraph.bigsim.transitionsystem.State
import org.bigraph.bigsim.utils.GlobalCfg
import org.bigraph.bigsim.utils.bankV3
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.mutable
import scala.collection.mutable.Buffer
import scala.collection.mutable.{Map, Queue, Set}
// 需要使用到java中的变量
import scala.collection.JavaConverters._
import scala.collection.JavaConversions._


class LTLShortestSimulator
(nb: Bigraph) extends Simulator {

  // 这里的b用于测试， 不测试记得把这句注释掉,并且把主构造器里面的 nb 改成b
  //val b = testLTLSimulator.b
  val b = nb

  var v: Vertex = new Vertex(b, null, null); // 每个节点包含一个bigraph、第一个节点是初始agent，Vertex传入的参数分别表示：当前偶图，该偶图的“父节点”，父节点反应到当前的反应规则
  var g: Graph = new Graph(v);

  val transition = new TransitionSystem(b)
  val ltlParser = new LTLSpec(b.ltlSpec, b.prop.toMap)

  var checkRes: Boolean = false;
  var recordPath: List[State] = List()
  var recordMap: Map[State, State] = Map()


  def simulate: Unit = {
    if (b == null || b.root == null) {
      println("LTL simulator::simulate(): null");
      return;
    } else {
      val buildKripke = new BuildKripkeStructure(transition)
      val kripke = buildKripke.buildKripke
      val ltlModelChecker = new LTLModelChecker(kripke)
      this.checkRes = ltlModelChecker.satisfies(ltlParser.getLTLFormula())
      if (ltlModelChecker.recordPath != null) {
        this.recordPath = ltlModelChecker.recordPath.toList
        if (recordPath.nonEmpty) {
          var pre: State = this.recordPath(0)
          this.recordPath.tail.foreach(x => {
            this.recordMap += (pre -> x)
            pre = x
          })
        }
      }
      this.v = transition.v
      this.g = transition.g
      this.g.addCTLRes(recordPath, checkRes, CTLCheckResult.PathType.CounterExample)

      logger.debug("LTL模型检测结果: " + ltlModelChecker.satisfies(ltlParser.getLTLFormula()))

      if (!checkRes) {
        println(recordPath)
        println(recordMap)
      }
    }
  }

  def dumpDotForward(dot: String): String = {
    val dotStr = this.g.dumpDotFile() //打印到dot文件
    dotStr
  }

  def dumpPaths(): String = {
    val paths = this.g.dumpPaths()
    paths
  }

  def simulatorRes(): String = {
    val res = "成功"
    res
  }

  def getFormula(): String = {
    return ltlParser.getLTLFormula().toString
  }
}


object testLTLShortestSimulator {
  val example1 =
    """
      |# Controls
      |%active Greater : 2;
      |%active Less : 2;
      |%active GreaterOrEqual : 2;
      |%active LessOrEqual : 2;
      |%active Equal : 2;
      |%active NotEqual : 2;
      |%active Exist : 1;
      |%active InstanceOf : 2;
      |%active Plus : 3;
      |%active Minus : 3;
      |%active Multiply : 3;
      |%active Division : 3;
      |%active Opposite : 2;
      |%active Abs : 2;
      |%active Process : 1;
      |%active Semaphore : 1;
      |%active CriticalSection : 1;
      |%active Value : 1;
      |
      |# Rules
      |%rule r_0 P0:Process[idle] | P1:Process[idle] | S:Semaphore[idle] | CS:CriticalSection[idle] | True:Value[idle] | False:Value[idle] -> P0:Process[idle] | P1:Process[idle] | S:Semaphore[a:edge].False:Value[a:edge] | CS:CriticalSection[idle] | True:Value[idle]{};
      |
      |%rule r_1 P0:Process[idle] | P1:Process[idle] | S:Semaphore[a:edge].False:Value[a:edge] | CS:CriticalSection[idle] | True:Value[idle] -> P0:Process[idle].CS:CriticalSection[idle] | P1:Process[idle] | S:Semaphore[a:edge].False:Value[a:edge] | True:Value[idle]{};
      |
      |%rule r_2 P0:Process[idle].CS:CriticalSection[idle] | P1:Process[idle] | S:Semaphore[a:edge].False:Value[a:edge] | True:Value[idle] -> P0:Process[b:edge].CS:CriticalSection[b:edge] | P1:Process[idle] | S:Semaphore[a:edge].True:Value[a:edge] | False:Value[idle]{};
      |
      |%rule r_3 P0:Process[b:edge].CS:CriticalSection[b:edge] | P1:Process[idle] | S:Semaphore[a:edge].True:Value[a:edge] | False:Value[idle] -> P0:Process[idle] | P1:Process[idle] | S:Semaphore[a:edge].False:Value[a:edge] | True:Value[idle] | CS:CriticalSection[b:edge]{};
      |
      |%rule r_4 P0:Process[idle] | P1:Process[idle] | S:Semaphore[a:edge].False:Value[a:edge] | CS:CriticalSection[idle] | True:Value[idle] -> P0:Process[idle] | P1:Process[idle] | S:Semaphore[idle] | CS:CriticalSection[idle] | True:Value[idle] | False:Value[idle]{};
      |
      |
      |
      |# prop
      |%prop p  P0:Process[idle].d:CriticalSection[idle] | P1:Process[idle] | S:Semaphore[a:edge].False:Value[a:edge] | True:Value[idle]{};
      |%prop q  P0:Process[b:edge].CS:CriticalSection[b:edge] | P1:Process[idle] | S:Semaphore[a:edge].True:Value[a:edge] | False:Value[idle]{};
      |
      |
      |# Model
      |%agent  P0:Process[idle] | P1:Process[idle] | CS:CriticalSection[idle] | True:Value[idle] | False:Value[idle] | S:Semaphore[idle];
      |
      |
      |
      |# LTL_Formula
      |%ltlSpec G(p);
      |
      |# Go!
      |%check;
      |
      |""".stripMargin


  val t = BGMParser.parseFromString(example1)
  val b = BGMTerm.toBigraph(t)._2

  def main(args: Array[String]): Unit = {
    val simulator = new LTLSimulator(b)
    simulator.simulate
    var dotStr = simulator.dumpDotForward("")
    println(dotStr)

  }
}