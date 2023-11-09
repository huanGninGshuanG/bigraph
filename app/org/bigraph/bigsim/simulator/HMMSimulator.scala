package org.bigraph.bigsim.simulator

import java.io.{File, FileWriter, Writer}
import java.util
import java.util.concurrent.{LinkedBlockingQueue, ThreadPoolExecutor, TimeUnit}

import cn.edu.pku.ss.hmm.ForwardHiddenState
import com.google.common.base.Charsets
import com.google.common.io.Files
import domain.PkuResource
import org.apache.commons.lang3.StringUtils

import scala.collection.mutable.Map
import scala.collection.mutable.Queue
import scala.collection.mutable.Set
import org.bigraph.bigsim.BRS.Graph
import org.bigraph.bigsim.BRS.Vertex
import org.bigraph.bigsim.utils.GlobalCfg
import org.bigraph.bigsim.BRS.Match
import org.bigraph.bigsim.data.Data
import org.bigraph.bigsim.model.Bigraph
import org.bigraph.bigsim.model.ReactionRule
import org.bigraph.bigsim.model.Nil
import org.bigraph.bigsim.parser.{HmmData, HmmDataParser}
import utils.BigSimThreadFactory


/**
  * 隐马尔科夫模型随机概率
  * @author lixin
  * @version 1.0
  */
object HMMSimulator {
  var matchDiscard: Set[Match] = Set();

  def matchMarkDelete(m: Match): Unit = {
    assert(m != null);
    matchDiscard.add(m);
  }

  def matchGC: Unit = {
    matchDiscard.clear();
  }
}


class HMMSimulator(rootBigraph: Bigraph, pkuResource: PkuResource) extends Simulator {

  var rootVertex: Vertex = new Vertex(rootBigraph, null, null);
  var g: Graph = new Graph(rootVertex);
  var workQueue: Queue[Vertex] = Queue();
  workQueue.enqueue(rootVertex);//初始节点进队列
  var steps: Int = 0;
  var reachedAgent: Map[Long, Boolean] = Map();

  def initForwardHMM(hmmData: HmmData) : ForwardHiddenState = {
    // 状态转移矩阵
    val A = hmmData.transitionProbabilityArray

    // 混淆矩阵
    val B = hmmData.emissionProbabilityArray

    // 初始概率向量
    val PI = hmmData.startProbabilityArray

    val ob = hmmData.observerSeqArray

    val attacks = hmmData.statesArray
    val alerts = hmmData.observationsArray

    // 初始化
    val forwardHiddenState = new ForwardHiddenState(attacks, alerts, A, B, PI)

    // 设置观测状态
    forwardHiddenState.setObserState(ob)

    forwardHiddenState
  }

  /**
    * 简单实现数据的加载
    * @return
    */
  def getHmmData : HmmData = {
//    val filePath = "E:\\git\\pku\\liuruoyu\\BigSim\\Examples\\xin\\data\\hmm_data.txt"

//    val strings = Files.asCharSource(new File(filePath), Charsets.UTF_8).readLines

    val strings = pkuResource.extContent

    val str = StringUtils.join(strings, "\n")
    val hmmData = HmmDataParser.parseHMMData(str)

    hmmData
  }
  val BigSimPool = new ThreadPoolExecutor(10,10,
    1L, TimeUnit.SECONDS,
    new LinkedBlockingQueue,
    new BigSimThreadFactory("Graph"),
    new ThreadPoolExecutor.AbortPolicy);

  def simulate: Unit = {
    val hmmData: HmmData = getHmmData
    val forwardHiddenState = initForwardHMM(hmmData);
    if (rootBigraph == null || rootBigraph.root == null) {
      println("hmm simulator::simulate(): null");
      return ;
    } else {

      while (step(forwardHiddenState)) {};
      BigSimPool.execute(new Runnable {
        override def run(): Unit ={
          HMMSimulator.matchGC;
        }
      })
    }
  }

  def report(step: Int): String = {
    GlobalCfg.node = false
    if (GlobalCfg.outputPath)
      g.dumpPaths

    GlobalCfg.node = true

    //调的Graph类的dumpDotForward，本类没有重写父类抽象方法，只是简单置为空实现
    g.dumpDotForward;

  }

  def step(forwardHiddenState: ForwardHiddenState): Boolean = {
    /** if reach the max steps */
    if (steps >= GlobalCfg.maxSteps) {
      println("mc::step Interrupted!  Reached maximum steps: " + GlobalCfg.maxSteps);
      report(steps);
      return false;
    }
    /** if the working queue is empty */
    if (workQueue.isEmpty) {
      println("hmm simulator::step Complete!");
      report(steps);
      HMMSimulator.matchGC;
      return false;
    }
    /** get the top element of working queue */
    val vertex: Vertex = workQueue.dequeue();
    /** if the current agent has been reachedAgent, then stop */
    if (reachedAgent.contains(vertex.hash)) {//又回到刚才到过的节点，出现环，跳出本次循环，进入下一次循环
      return true
    }

    steps += 1
    val step: Int = steps;
    val bigraph: Bigraph = vertex.bigraph;

    // 查询与模型匹配的所有规则
    val matches: Set[Match] = bigraph.findMatches;

    /** if no match of this agent, it will be a terminal agent */
    if (matches.isEmpty) {
      vertex.terminal = true
    }
    reachedAgent(vertex.hash) = true


    // 根据匹配结果，进行模型衍化：通过调用各 Term 的 applyMatch 完成
    // 查看是否在 reachedAgent 中，不存在，压入 workQueue。

    // 查看是否包含 random，如果包含 random ，则使用 HMM 进行计算
    val isRand = matches.exists(_.rule.hmmRandom)
    logger.debug("val isRand = matches.exists(_.rule.random), {}", isRand)

    if(isRand){
      forwardHiddenState.step()
      val index = forwardHiddenState.maxIndex()
      // 使用隐马尔科夫模型模拟，过滤结果集
      // .filter(_.rule.hmmState == index )
      matches.foreach(it=>{
        val rr: ReactionRule = it.rule
        val hmmStep = forwardHiddenState.getCurrentStep
        rr.hmmProbability = forwardHiddenState.getMaxValues.get(hmmStep)
        logger.debug(rr.hmmState + " ------- " + rr.hmmRandom + " ------- " + rr.hmmProbability + " --- " + index
        + ", maxValues: " + forwardHiddenState.getMaxValues)
        if(rr.hmmState == index || rr.hmmState == -1){
          doMatch(it, vertex, bigraph, workQueue)
        }
      })
    }else{
      // 与枚举策略一样，对匹配上的所有规则进行衍化
      matches.foreach(it => {
        doMatch(it, vertex, bigraph, workQueue)
      })
    }

    matches.clear()
    HMMSimulator.matchGC

    if (GlobalCfg.reportInterval > 0 && step % GlobalCfg.reportInterval == 0) {
      println(report(step));
    }
    if (GlobalCfg.printMode) {
      printf("%s:%s\n", "N_" + Math.abs(vertex.hash), vertex.bigraph.root.toString);
    }

    true
  }

  def doMatch(it: Match, vertex: Vertex, bigraph: Bigraph, workQueue: Queue[Vertex]) : Unit = {
    var rr: ReactionRule = it.rule

    /** apply match to turn into new agent */
    // 模型转化，生成新模型
    var nb: Bigraph = bigraph.applyMatch(it)
    // null过滤
    if (nb.root == null) nb.root = new Nil()
    var nv: Vertex = new Vertex(nb, vertex, rr)
    if (!GlobalCfg.checkLocal) {
      if (g.lut.contains(nv.hash)) {
        nv = g.lut(nv.hash)
        nv.addParents(vertex)
      } else {
        /** new agent has not been reached, put into the working queue */
        // 若新模型尚未检测过，新模型加入待检测队列。
        workQueue.enqueue(nv)
        g.add(nv)
      }
      // 记录反应路径
      vertex.addTarget(nv, rr)
      //v.addTargets(rr, nv);
    } else {
      /** We've not reachedAgent this one! */
      if (!reachedAgent.contains(nv.hash) && nv.bigraph.root != null) {
        workQueue.enqueue(nv)
      }
    }
  }

  def checkProperties(v: Vertex): Boolean = {
    if (v.visited) return true;
    v.visited = true;
    true;
  }


  def dumpDotForward(dot: String): String = {
    //if (GlobalCfg.graphOutput == "") return "";
    var out: String = "";
    out += "digraph reaction_graph {\n";
    out += "   rankdir=LR;\n";
    out += "   Node [shape = circle];\n";
    out += "   BigSim_Report [shape = component color = forestgreen style=filled label=\"BigSim\nHMM\nReport\"];\n"
    out += "BigSim_Report -> N_" + formatHash(g.root.hash) + "[color = aliceblue label = \""

    //读取agent的权重表达式，输出模拟报告 表示权重表达式不为空，即有权重表达式
    if (!Data.getWeightExpr.equals("wExpr=")) {
      out += Data.getWeightExpr + "=" + Data.getReport + "\n"
    }
    out += Data.getValues(",") + "\"];\n"
    out += " N_" + formatHash(g.root.hash) + "\n" + " [shape=circle, color=lightblue2, style=filled];\n"
    g.lut.values.map(x => {
      var rr: String = "root"
      var dc: String = ""

      if (x.terminal) {
        dc = "shape = doublecircle, color=cornflowerblue, style=filled, ";
      }
      out += "N_" + formatHash(x.hash) + "[ " + dc + "label=\"N_" + formatHash(x.hash) + "\n" + x.variables + "\"];\n"; //data太多了不输出了

      x.target.map(y => {
        rr = "?"
        if (y._2 != null)
          rr = y._2.name

        if (y._2.hmmRandom) {
            rr = rr + "\nHMM State:" + y._2.hmmState + "\nProbability:" + y._2.hmmProbability.formatted("%.6f")
        }

        out += " N_" + formatHash(x.hash) + " -> N_" + formatHash(y._1.hash) + "[color = purple label = \"" + rr + "\"];\n"
      })
    })

    out += "}\n"
    if (GlobalCfg.graphOutput != "") {
      var file: File = new File(GlobalCfg.graphOutput)
      var writer: Writer = new FileWriter(file)
      writer.write(out)
      writer.flush
    }
    out
  }

  def dumpPaths(): String = {
    val paths = g.dumpPaths
    paths
  }

  def simulatorRes(): String = {
    val res = "成功"
    res
  }

  def getFormula():String={
    return "null"
  }
}
