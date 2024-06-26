package org.bigraph.bigsim.simulator

import java.util.concurrent.{LinkedBlockingQueue, ThreadPoolExecutor, TimeUnit}

import org.bigraph.bigsim.BRS.{Graph, Match, Vertex}
import org.bigraph.bigsim.model.{Bigraph, ReactionRule}
import org.bigraph.bigsim.strategy.PatternFlow
import org.bigraph.bigsim.utils.GlobalCfg
import utils.BigSimThreadFactory

import scala.collection.mutable.Set

object MainSimulator {
  var matchDiscard: Set[Match] = Set();
  var v: Vertex = null
  var g: Graph = null
  var start: Long = System.currentTimeMillis();
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

class MainSimulator(b: Bigraph) extends Simulator {
  MainSimulator.v = new Vertex(b, null, null);
  MainSimulator.g = new Graph(MainSimulator.v);

  def simulate: Unit = {
    System.out.println("=========================================================")
    System.out.println("========================MainSimulator====================")
    System.out.println("=========================================================")

    if (b == null || b.root == null) {  //判断初始偶图模型是否为空
      println("MainSimulator::simulate(): initial bigraph is null");
      return ;
    }
    
    var rules: Set[ReactionRule] = b.rules;   // 获得反应规则集合
    if (rules == null || rules.size == 0) {     // 判断是否有反应规则
      println("MainSimulator::simulate(): no reaction rules");
      return ;
    }

    rules.foreach { x => if (x.sysClkIncr > 0) GlobalCfg.checkTime = true } //存在SysClk>0的规则则认为要考虑时间，其他没有给定时间的规则也默认为反应时间为0
    rules.foreach { x => if (x.rate > 0) GlobalCfg.stochastic = true }      // 表示当前规则考虑随机反应

    if (GlobalCfg.checkPattern) {      //考虑Pattern Flow机制
      println("================================= kgq start checkPattern ==============================")
      PatternFlow.setPatternDURules(b) //在执行模拟之前初始化
    }
    println("========================== kgq MainSimulator checkPattern finish ============================")

    val BigSimPool = new ThreadPoolExecutor(10,10,
      1L, TimeUnit.SECONDS,
      new LinkedBlockingQueue,
      new BigSimThreadFactory("Graph"),
      new ThreadPoolExecutor.AbortPolicy);

    if (GlobalCfg.checkTime) { //bgm文件中定义了SysClk，考虑离散模拟功能（离散时间模拟+离散事件模拟）
      if (GlobalCfg.isBackDerivation) {
        rules.map { r =>
          {
            println(r.name+"--r.conds: "+r.conds)            
            r.bdconds.++=(r.conds) //原来的规则条件放在bdconds里，反应结束后检测
            println("r.bdconds: "+r.bdconds) 
            r.conds.clear()
            println(r.name+"--r.conds: "+r.conds) 
            r.dataCalcs.map(d => {
              var con = d._1 + "==" + d._2
              r.conds.+=(con) //原本的情境数据计算式转为条件式，findMatches方法会自动监测cond
            })
            println(r.name+"--r.conds: "+r.conds) 
          }
        }
        var backDerivationFeature = new BackDerivationFeature();
        while (backDerivationFeature.step()) {
          Simulator.matchGC
        };
      } else {
        var discreteFeature = new DiscreteFeature();
        while (discreteFeature.step()) {
          Simulator.matchGC
        };
      }
      println("Graph: " + MainSimulator.g.lut.size);
      MainSimulator.middle = System.currentTimeMillis(); //第5次模拟完的时间

      MainSimulator.g.dumpPath //打印到data和path文件
      MainSimulator.g.dumpDotFile //打印到dot文件 
      //      MainSimulator.g.dumpRRFile()//打印到规则文件
      MainSimulator.end = System.currentTimeMillis(); //写完到文件的时间
    }

    if (GlobalCfg.stochastic) { //bgm文件中定义了Rate，模拟生物化学场景
      var stochasticSimulator = new StochasticSimulator(b);
      stochasticSimulator.simulate
    }

    if (!GlobalCfg.checkTime) { //不考虑时间的用枚举策略
      var enumFeature = new EnumFeature(b);
      while (enumFeature.step()) {
        Simulator.matchGC
      };
      println("Graph: " + MainSimulator.g.lut.size);
      MainSimulator.g.dumpPath //打印到data和path文件
      MainSimulator.g.dumpDotFile //打印到dot文件
    }
    MainSimulator.matchGC; //Test，可能无用

  }

  //  def report(step: Int): String = {
  //    GlobalCfg.node = false
  //    if (GlobalCfg.outputPath)
  //      g.dumpPaths
  //    GlobalCfg.node = true
  //    g.dumpDotForward;
  //  }

  def simulatorRes(): String = {
    val res = "成功"
    res
  }
  
  def dumpDotForward(dot: String): String ={
    println("Graph: " + MainSimulator.g.lut.size);
    MainSimulator.g.dumpPath //打印到data和path文件
    MainSimulator.g.dumpDotFile //打印到dot文件
  }
  def dumpPaths(): String = ""

  def getFormula():String={
    return "null"
  }
}