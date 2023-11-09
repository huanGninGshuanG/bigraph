package org.bigraph.bigsim.simulator

import scala.collection.mutable.Map
import scala.collection.mutable.Queue
import scala.collection.mutable.Set
import org.bigraph.bigsim.BRS.Graph
import org.bigraph.bigsim.BRS.Vertex
import org.bigraph.bigsim.utils.GlobalCfg
import org.bigraph.bigsim.BRS.Match
import org.bigraph.bigsim.model.Bigraph
import org.bigraph.bigsim.model.ReactionRule
import org.bigraph.bigsim.model.Nil
import org.bigraph.bigsim.Verify
import org.bigraph.bigsim.value.Value

/**
 * Enum Simulator is designed for enumerate all the agents
 * can be reacted into from the initial agent.
 * It will give the graph based state machines of agents.
 * Here only consider the relation of agents and rules, no time,
 * condition, and data calculation.
 */
object EnumSimulator {
  var matchDiscard: Set[Match] = Set();

  def matchMarkDelete(m: Match): Unit = {
    assert(m != null);
    matchDiscard.add(m);
  }

  def matchGC: Unit = {
    matchDiscard.clear();
  }
}

class EnumSimulator(b: Bigraph) extends Simulator {
  var v: Vertex = new Vertex(b, null, null); // 每个节点包含一个bigraph、第一个节点是初始agent，Vertex传入的参数分别表示：当前偶图，该偶图的“父节点”，父节点反应到当前的反应规则
  var g: Graph = new Graph(v);
  var workQueue: Queue[Vertex] = Queue();
  workQueue.enqueue(v);//初始节点进队列
  var steps: Int = 0;
  var reachedAgent: Map[Long, Boolean] = Map(); // 记录已经产生的agent

  var debug: Boolean = true                    // add by kgq 20220318  控制日志输出
  def setDebug(): Unit = {
    debug = true
  }


  def simulate: Unit = {
    if (b == null || b.root == null) {
      println("enum simulator::simulate(): null");
      return ;
    } else {
      while (step()) {};
      //logger.debug("kgq: ------------------------------finish simulate loop---------------------------------")
      EnumSimulator.matchGC;
    }
  }

  // kgq 看样子是向dot文件进行输出
  def report(step: Int): String = {

    GlobalCfg.node = false
    if (GlobalCfg.outputPath) {    // outputPath = true
      g.dumpPaths;
    }
    GlobalCfg.node = true
    g.dumpDotForward;//调的Graph类的dumpDotForward，本类没有重写父类抽象方法，只是简单置为空实现
  }

    // 这里执行的是每一步的操作，循环的执行体
  def step(): Boolean = {
    /** if reach the max steps */
    if (steps >= GlobalCfg.maxSteps) {
      println("mc::step Interrupted!  Reached maximum steps: " + GlobalCfg.maxSteps);
      report(steps);  //output to dot
      return false;
    }
    /** if the working queue is empty */
    if (workQueue.size == 0) {
      println("enum simulator::step Complete!");
      report(steps);
      EnumSimulator.matchGC;
      return false;
    }
    /** get the top element of working queue */
    var v: Vertex = workQueue.dequeue();
    /** if the current agent has been reachedAgent, then stop */
    if (reachedAgent.contains(v.hash)) {//又回到刚才到过的节点，出现环，跳出本次循环，进入下一次循环
      if (debug)
        println("Skip!!! one")
      return true;
    }
    // 从工作队列里拿出一个节点、就反应一步
    steps += 1;
    var step: Int = steps;
    var b: Bigraph = v.bigraph; // 每个节点有一个bigraph

    Verify.AddModel(b);    // 把偶图 添加到一起，用一个集合存放，同时偶图中的linked组成一条链
    
    var matches: Set[Match] = b.findMatches;
    if (debug) {
      println("Bigraph: " + b.root.hashCode() + " match " + matches.size + "rules")
      if (matches.size == 0){
        GlobalCfg.DEBUG = true
        println("cur b is: " + b.root)
        val mtch = b.findMatches
      }
    }



    /** if no match of this agent, it will be a terminal agent */
    if (matches.size == 0) {
      v.terminal = true;// last agent
    }
    reachedAgent(v.hash) = true;

    matches.foreach(it => {     // 当前的节点，对找到的所有匹配，进行应用，构造出来新的节点，放入到工作列中去，并对一些字段和变量进行更新。
        var rr: ReactionRule = it.rule;

        val value = new Value(rr)     // add by kgq 20220301 数值模式
        if (value.checkConditions(it)) {  // add by kgq 20220301 检查条件表达式  ，之后满足条件表达式的时候，才会applyMatch 生成新偶图
          value.calAssignments(it)        // add by kgq 20220301 计算赋值表达式
          /** apply match to turn into new agent */
          if (debug) println("\t*** old b: " + b.root)
          var nb: Bigraph = b.applyMatch(it);
          if (debug) println("\t*** new b: " + nb.root + "\n*** via rr: " + rr.name)
          if (nb.root == null) nb.root = new Nil();
          var nv: Vertex = new Vertex(nb, v, rr);
          if (!GlobalCfg.checkLocal) {   // 现在在config.properties中是false
            if (g.lut.contains(nv.hash)) {
              nv = g.lut(nv.hash);            //  注意这个地方！！！！！！！！！！！！！， 虽然说上面根据新建的偶图新建了一个vertex，但是，在这里会根据vertex的hash（这个是根据偶图 的字符串形式计算出来的）来查表，如果已经在表中存在，那么nv就取为已有的vertex，新建的那个就放弃掉了
              nv.addParents(v)                 // 因为是沿用了之前的vertex，所以这个地方就把这个点父亲放到 parents 这个集合里面
            } else {
              /** new agent has not been reached, put into the working queue */
              if (debug) {
                println("[From] " + b.root.hashCode() + " [to] " + nb.root.hashCode() + " [via] " + it.rule.name)  // tmp comment 2002.02.22
                //println("[CurMatch] " + it) // tmp comment 2020.02.24
                //println("[MatchRoot] " + it.root)
              }
              workQueue.enqueue(nv);
              g.add(nv);                //  这个是把这个新的节点放到查找表 lut中去， 这里不用把v 放到新节点的 parents ，是因为第一个父亲在新建节点的时候把v就放进来了
            }
            v.addTarget(nv, rr);
            //v.addTargets(rr, nv);
          } else { // 这条通路没有走，因为checklocal 是false，这里可能是用来本地测试用的吧~
            /** We've not reachedAgent this one! */
            if (!reachedAgent.contains(nv.hash) && nv.bigraph.root != null) {   // 如果 新反应出来的偶图没有访问过，且偶图的根不为空，那么就把新生成的偶图“节点”放入队列中。
              workQueue.enqueue(nv);
            }
          }
      }
    })
    matches.clear();     //  清空匹配集合
    EnumSimulator.matchGC;

    if (GlobalCfg.reportInterval > 0 && step % GlobalCfg.reportInterval == 0) {
      println(report(step));
    }
    if (GlobalCfg.printMode) {
      printf("%s:%s\n", "N_" + Math.abs(v.hash), v.bigraph.root.toString);
    }
    if (!checkProperties(v)) { //模型性质检测   目前checkProperties一直是返回
      println("mc::step Counter-example found.")
      return false;
    }
    true;
  }

  def checkProperties(v: Vertex): Boolean = {
    if (v.visited) return true;    // 如果这个节点访问过，那么直接返回True
    /** check sorting */
//    if (GlobalCfg.checkSorting) {   // 不然，检查sorting
//      var sortingCheckRes = Bigraph.sorting.check(v);
//      if (!sortingCheckRes) {
//        println("*** Found violation of Sorting: " + Bigraph.sorting.violationInfo);
//        if (!GlobalCfg.localCheck)
//          println(g.backTrace(v));
//        else
//          println("[Backtrace unavailable in local checking mode]");
//        return false;
//      }
//    }
    v.visited = true;         // 如果这个节点没有被标记，那么就标记上，表示这个节点已经访问过
    true;
  }
  
    def dumpDotForward(dot: String): String = {
      val dotStr = g.dumpDotFile //打印到dot文件
      dotStr
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