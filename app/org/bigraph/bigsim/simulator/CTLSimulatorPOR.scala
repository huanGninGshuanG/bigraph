package org.bigraph.bigsim.simulator

import org.bigraph.bigsim.BRS.{Graph, Vertex}
import org.bigraph.bigsim.ctlimpl.CTLCheckResult
import org.bigraph.bigsim.model.Bigraph
import org.bigraph.bigsim.modelchecker.CTLModelChecker
import org.bigraph.bigsim.parser.{BGMParser, BGMTerm}
import org.bigraph.bigsim.transitionsystem.State
import org.bigraph.bigsim.utils.OS

import scala.collection.mutable.Map
// 需要使用到java中的变量
import scala.collection.JavaConversions._


class CTLSimulatorPOR
(nb: Bigraph) extends Simulator {

  // 这里的b用于测试， 不测试记得把这句注释掉,并且把主构造器里面的 nb 改成b
  //val b = testCTLSimulator.b
  val b = nb

  var v: Vertex = new Vertex(b, null, null); // 每个节点包含一个bigraph、第一个节点是初始agent，Vertex传入的参数分别表示：当前偶图，该偶图的“父节点”，父节点反应到当前的反应规则
  var g: Graph = new Graph(v);

  val transition = new TransitionSystemPOR(b)
  val ctlParser = new CTLSpec(b.ctlSpec, b.prop.toMap)

  var checkRes: Boolean = false;
  var recordPath: List[State] = List()
  var recordMap: Map[State, State] = Map()


  def simulate: Unit = {
    if (b == null || b.root == null) {
      println("CTL simulator::simulate(): null");
      return;
    } else {
      val buildKripke = new BuildKripkeStructurePOR(transition)
      val kripke = buildKripke.buildKripke
      val ctlModelChecker = new CTLModelChecker(kripke)
      this.checkRes = ctlModelChecker.satisfies(ctlParser.getCTLFormula())
      if (ctlModelChecker.recordPath != null) {
        this.recordPath = ctlModelChecker.recordPath.toList
        if (recordPath.nonEmpty) {
          var pre: State = this.recordPath.head
          this.recordPath.tail.foreach(x => {
            this.recordMap += (pre -> x)
            pre = x
          })
        }
      }
      this.v = transition.v
      this.g = transition.g
      this.g.addCTLRes(recordPath, checkRes, CTLCheckResult.PathType.CounterExample)

      //logger.debug("CTL模型检测结果: " + ctlModelChecker.satisfies(ctlParser.getCTLFormula()))
      logger.debug("==========================CTL模型检测结果: " + this.checkRes)
      logger.debug("==========================生成的状态个数: " + buildKripke.bigraphList.size)
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

  override def simulatorRes(): String = {
    var res = ""
    if (this.checkRes) {
      res = "成功"
    } else {
      res = "失败"
    }
    val r = res;
    r
  }

  override def getFormula(): String = {
    return ctlParser.getCTLFormula().toString
  }
}


object testCTLSimulatorPOR {


  val t = BGMParser.parseFromString(OS.rw4DEBUG)
  val b = BGMTerm.toBigraph(t)

  def main(args: Array[String]): Unit = {
    val startTime = System.currentTimeMillis()
    val simulator = new CTLSimulatorPOR(b)
    simulator.simulate
    //var dotStr = simulator.dumpDotForward("")
    //println(dotStr)
    val endTime = System.currentTimeMillis()
    printf("============================================================模型检测耗时: ")
    print((endTime - startTime) / 1000f)
    println("s")
  }
}