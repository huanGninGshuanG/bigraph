//package org.bigraph.bigsim.simulator
//
//import org.bigraph.bigsim.BRS.{Graph, Match, Vertex}
//import org.bigraph.bigsim.Verify
//import org.bigraph.bigsim.blockchain.consensus_mechanism.PBFTConsensus
//import org.bigraph.bigsim.blockchain.core.BlockChain
//import org.bigraph.bigsim.blockchain.ecommerce_chain.EcommerceChain
//import org.bigraph.bigsim.model.{Bigraph, Nil, ReactionRule}
//import org.bigraph.bigsim.parser.{BGMParser, BGMTerm}
//import org.bigraph.bigsim.utils.GlobalCfg
//
//import scala.collection.mutable.{Map, Queue, Set}
//
//class BlockChainSimulator(b: Bigraph) extends Simulator {
//  var v: Vertex = new Vertex(b, null, null); // 每个节点包含一个bigraph、第一个节点是初始agent
//  var g: Graph = new Graph(v);
//  var workQueue: Queue[Vertex] = Queue();
//  workQueue.enqueue(v);//初始节点进队列
//  var steps: Int = 0;
//  var reachedAgent: Map[Long, Boolean] = Map(); // 记录已经产生的agent
//  var ecommerceChain: EcommerceChain = _
//  init()
//  def init(): Unit = {
//    println("start initing blockchain ...")
//    ecommerceChain = new EcommerceChain(1, 1, new PBFTConsensus, 4)
//    println("blockchain inits successfully...")
//    ecommerceChain.start()
//  }
//  def simulate: Unit = {
//    if (b == null || b.root == null) {
//      return ;
//    } else {
//      while (step()) {};
//      EnumSimulator.matchGC;
//    }
//  }
//
//  def report(step: Int): String = {
//    GlobalCfg.node = false
//    if (GlobalCfg.outputPath)
//      g.dumpPaths
//    GlobalCfg.node = true
//    g.dumpDotForward;//调的Graph类的dumpDotForward，本类没有重写父类抽象方法，只是简单置为空实现
//  }
//
//  def step(): Boolean = {
//    /** if reach the max steps */
//    if (steps >= GlobalCfg.maxSteps) {
//      report(steps);  //output to dot
//      return false;
//    }
//    /** if the working queue is empty */
//    if (workQueue.size == 0) {
//      report(steps);
//      EnumSimulator.matchGC;
//      return false;
//    }
//    /** get the top element of working queue */
//    var v: Vertex = workQueue.dequeue();
//    /** if the current agent has been reachedAgent, then stop */
//    if (reachedAgent.contains(v.hash)) {//又回到刚才到过的节点，出现环，跳出本次循环，进入下一次循环
//      return true;
//    }
//    steps += 1;
//    var step: Int = steps;
//    var b: Bigraph = v.bigraph; // 每个节点有一个bigraph
//    Verify.AddModel(b);
//    var matches: Set[Match] = b.findMatches;
//    if (matches.isEmpty) {
//      v.terminal = true;
//    }
//    reachedAgent(v.hash) = true;
//    matches.foreach(it => {
//        var rule: ReactionRule = it.rule;
//        processRule(rule)
//        var nb: Bigraph = b.applyMatch(it);
//        if (nb.root == null) nb.root = new Nil();
//        var nv: Vertex = new Vertex(nb, v, rule);
//        if (!GlobalCfg.checkLocal) {
//          if (g.lut.contains(nv.hash)) {
//            nv = g.lut(nv.hash);
//            nv.addParents(v)
//          } else {
//            workQueue.enqueue(nv);
//            g.add(nv);
//          }
//          v.addTarget(nv, rule);
//
//        } else {
//          if (!reachedAgent.contains(nv.hash) && nv.bigraph.root != null) {
//            workQueue.enqueue(nv);
//          }
//        }
//      })
//    matches.clear();
//    EnumSimulator.matchGC;
//    if (GlobalCfg.reportInterval > 0 && step % GlobalCfg.reportInterval == 0) {
//      println(report(step));
//    }
//    if (GlobalCfg.printMode) {
//      printf("%s:%s\n", "N_" + Math.abs(v.hash), v.bigraph.root.toString);
//    }
//    true;
//  }
//
//  def processRule(rule: ReactionRule): Unit = {
//    if(findTransaction(rule)){
//      executeTransaction()
//    }else if(findScore(rule)){
//      executeScore()
//    }else if(findLocalCredit(rule)){
//      executeLocalCredit()
//    }else if(findGlobalCredit(rule)){
//      executeGlobalCredit()
//    }
//  }
//
//  def executeTransaction(): Unit = {
//    ecommerceChain.executeTransaction();
//  }
//
//  def executeScore(): Unit = {
//    ecommerceChain.executeScore()
//  }
//
//  def executeLocalCredit(): Unit = {
//    ecommerceChain.executeLocalCredit()
//  }
//
//  def executeGlobalCredit(): Unit = {
//    ecommerceChain.executeGlobalCredit()
//  }
//
//  def findTransaction(rule: ReactionRule): Boolean = {
//    rule.name.equals(GlobalCfg.transaction)
//  }
//
//  def findScore(rule: ReactionRule): Boolean = {
//    rule.name.equals(GlobalCfg.score)
//  }
//
//  def findLocalCredit(rule: ReactionRule): Boolean = {
//    rule.name.equals(GlobalCfg.localCredit)
//  }
//
//  def findGlobalCredit(rule: ReactionRule): Boolean = {
//    rule.name.equals(GlobalCfg.globalCredit)
//  }
//
//  def checkProperties(v: Vertex): Boolean = {
//    if (v.visited) return true;
//    /** check sorting */
////    if (GlobalCfg.checkSorting) {
////      var sortingCheckRes = Bigraph.sorting.check(v);
////      if (!sortingCheckRes) {
////        println("*** Found violation of Sorting: " + Bigraph.sorting.violationInfo);
////        if (!GlobalCfg.localCheck)
////          println(g.backTrace(v));
////        else
////          println("[Backtrace unavailable in local checking mode]");
////        return false;
////      }
////    }
//    v.visited = true;
//    true;
//  }
//
//    def dumpDotForward(dot: String): String = {
//      val dotStr = g.dumpDotFile //打印到dot文件
//      dotStr
//    }
//    def dumpPaths(): String = {
//      val paths = g.dumpPaths
//      paths
//    }
//
//}
//object BlockChainSimulator{
//  def main(args: Array[String]): Unit = {
//    val bgm = "# Controls\n%active Greater : 2;\n%active Less : 2;\n%active GreaterOrEqual : 2;\n%active LessOrEqual : 2;\n%active Equal : 2;\n%active NotEqual : 2;\n%active Exist : 1;\n%active InstanceOf : 2;\n%active Client : 3;\n%active BlockChainSystem : 0;\n%active ConsensusNode : 1;\n%active TransactionSC : 2;\n%active LocalCreditSC : 1;\n%active GlobalCreditSC : 1;\n%active TransactionBinding : 0;\n%active Money : 0;\n%active DB : 1;\n%active LocalCredit : 0;\n%active ScoreBinding : 0;\n%active GlobalCredit : 0;\n%active TrMsg : 0;\n%active ScoreSC : 1;\n%active Score : 0;\n%active CalLocalCreditNeed : 0;\n%active CalGlobalCreditNeed : 0;\n\n# Rules\n%rule r_beginCalGobalCredit buyer:Client[idle,idle,idle] | seller:Client[idle,idle,idle] | c:BlockChainSystem.(d:ConsensusNode[idle].(tl:TransactionSC[idle,idle] | l1:LocalCreditSC[idle] | d1:DB[idle] | g1:GlobalCreditSC[idle]) | e:ConsensusNode[idle].(t2:TransactionSC[idle,idle] | l2:LocalCreditSC[idle] | d2:DB[idle] | g2:GlobalCreditSC[idle]) | f:ConsensusNode[idle].(t3:TransactionSC[idle,idle] | l3:LocalCreditSC[idle] | d3:DB[idle] | g3:GlobalCreditSC[idle]) | g:ConsensusNode[idle].(t4:TransactionSC[idle,idle] | l4:LocalCreditSC[idle] | v:DB[idle] | g4:GlobalCreditSC[idle])) | cgn:CalGlobalCreditNeed -> buyer:Client[idle,idle,idle].cgn:CalGlobalCreditNeed | seller:Client[idle,idle,idle] | c:BlockChainSystem.(d:ConsensusNode[idle].(tl:TransactionSC[idle,idle] | l1:LocalCreditSC[idle] | d1:DB[idle] | g1:GlobalCreditSC[idle]) | e:ConsensusNode[idle].(t2:TransactionSC[idle,idle] | l2:LocalCreditSC[idle] | d2:DB[idle] | g2:GlobalCreditSC[idle]) | f:ConsensusNode[idle].(t3:TransactionSC[idle,idle] | l3:LocalCreditSC[idle] | d3:DB[idle] | g3:GlobalCreditSC[idle]) | g:ConsensusNode[idle].(t4:TransactionSC[idle,idle] | l4:LocalCreditSC[idle] | v:DB[idle] | g4:GlobalCreditSC[idle])){};\n\n%rule r_beginCalLocalCredit buyer:Client[idle,idle,idle] | seller:Client[idle,idle,idle] | c:BlockChainSystem.(d:ConsensusNode[idle].(tl:TransactionSC[idle,idle] | l1:LocalCreditSC[idle] | d1:DB[idle] | g1:GlobalCreditSC[idle]) | e:ConsensusNode[idle].(t2:TransactionSC[idle,idle] | l2:LocalCreditSC[idle] | d2:DB[idle] | g2:GlobalCreditSC[idle]) | f:ConsensusNode[idle].(t3:TransactionSC[idle,idle] | l3:LocalCreditSC[idle] | d3:DB[idle] | g3:GlobalCreditSC[idle]) | g:ConsensusNode[idle].(t4:TransactionSC[idle,idle] | l4:LocalCreditSC[idle] | v:DB[idle] | g4:GlobalCreditSC[idle])) | ccn:CalLocalCreditNeed -> buyer:Client[idle,idle,idle].ccn:CalLocalCreditNeed | seller:Client[idle,idle,idle] | c:BlockChainSystem.(d:ConsensusNode[idle].(tl:TransactionSC[idle,idle] | l1:LocalCreditSC[idle] | d1:DB[idle] | g1:GlobalCreditSC[idle]) | e:ConsensusNode[idle].(t2:TransactionSC[idle,idle] | l2:LocalCreditSC[idle] | d2:DB[idle] | g2:GlobalCreditSC[idle]) | f:ConsensusNode[idle].(t3:TransactionSC[idle,idle] | l3:LocalCreditSC[idle] | d3:DB[idle] | g3:GlobalCreditSC[idle]) | g:ConsensusNode[idle].(t4:TransactionSC[idle,idle] | l4:LocalCreditSC[idle] | v:DB[idle] | g4:GlobalCreditSC[idle])){};\n\n%rule r_calculateGlobalCredits buyer:Client[idle,idle,idle].cgn:CalGlobalCreditNeed | seller:Client[idle,idle,idle] | c:BlockChainSystem.(d:ConsensusNode[idle].(tl:TransactionSC[idle,idle] | l1:LocalCreditSC[idle] | d1:DB[idle] | g1:GlobalCreditSC[idle]) | e:ConsensusNode[idle].(t2:TransactionSC[idle,idle] | l2:LocalCreditSC[idle] | d2:DB[idle] | g2:GlobalCreditSC[idle]) | f:ConsensusNode[idle].(t3:TransactionSC[idle,idle] | l3:LocalCreditSC[idle] | d3:DB[idle] | g3:GlobalCreditSC[idle]) | g:ConsensusNode[idle].(t4:TransactionSC[idle,idle] | l4:LocalCreditSC[idle] | v:DB[idle] | g4:GlobalCreditSC[idle])) -> buyer:Client[idle,idle,a:edge].cgn:CalGlobalCreditNeed | seller:Client[idle,idle,idle] | c:BlockChainSystem.(d:ConsensusNode[idle].(tl:TransactionSC[idle,idle] | l1:LocalCreditSC[idle] | d1:DB[idle] | g1:GlobalCreditSC[a:edge]) | e:ConsensusNode[idle].(t2:TransactionSC[idle,idle] | l2:LocalCreditSC[idle] | d2:DB[idle] | g2:GlobalCreditSC[a:edge]) | f:ConsensusNode[idle].(t3:TransactionSC[idle,idle] | l3:LocalCreditSC[idle] | d3:DB[idle] | g3:GlobalCreditSC[a:edge]) | g:ConsensusNode[idle].(t4:TransactionSC[idle,idle] | l4:LocalCreditSC[idle] | v:DB[idle] | g4:GlobalCreditSC[a:edge])){};\n\n%rule r_calculateLocalCredit buyer:Client[idle,idle,idle].ccn:CalLocalCreditNeed | seller:Client[idle,idle,idle] | c:BlockChainSystem.(d:ConsensusNode[idle].(tl:TransactionSC[idle,idle] | l1:LocalCreditSC[idle] | d1:DB[idle] | g1:GlobalCreditSC[idle]) | e:ConsensusNode[idle].(t2:TransactionSC[idle,idle] | l2:LocalCreditSC[idle] | d2:DB[idle] | g2:GlobalCreditSC[idle]) | f:ConsensusNode[idle].(t3:TransactionSC[idle,idle] | l3:LocalCreditSC[idle] | d3:DB[idle] | g3:GlobalCreditSC[idle]) | g:ConsensusNode[idle].(t4:TransactionSC[idle,idle] | l4:LocalCreditSC[idle] | v:DB[idle] | g4:GlobalCreditSC[idle])) -> seller:Client[idle,idle,idle] | c:BlockChainSystem.(d:ConsensusNode[idle].(tl:TransactionSC[idle,idle] | l1:LocalCreditSC[a:edge] | d1:DB[idle] | g1:GlobalCreditSC[idle]) | e:ConsensusNode[idle].(t2:TransactionSC[idle,idle] | l2:LocalCreditSC[a:edge] | d2:DB[idle] | g2:GlobalCreditSC[idle]) | f:ConsensusNode[idle].(t3:TransactionSC[idle,idle] | l3:LocalCreditSC[a:edge] | d3:DB[idle] | g3:GlobalCreditSC[idle]) | g:ConsensusNode[idle].(t4:TransactionSC[idle,idle] | l4:LocalCreditSC[a:edge] | v:DB[idle] | g4:GlobalCreditSC[idle])) | buyer:Client[idle,idle,a:edge].ccn:CalLocalCreditNeed{};\n\n%rule r_consensusFails buyer:Client[a:edge,idle,idle] | seller:Client[idle,a:edge,idle] | c:BlockChainSystem.(d:ConsensusNode[idle].(tl:TransactionSC[a:edge,idle] | l1:LocalCreditSC[idle] | d1:DB[idle] | g1:GlobalCreditSC[idle] | tr1:TrMsg) | e:ConsensusNode[idle].(t2:TransactionSC[a:edge,idle] | l2:LocalCreditSC[idle] | d2:DB[idle] | g2:GlobalCreditSC[idle] | tr2:TrMsg) | f:ConsensusNode[idle].(t3:TransactionSC[a:edge,idle] | l3:LocalCreditSC[idle] | d3:DB[idle] | g3:GlobalCreditSC[idle] | tr3:TrMsg) | g:ConsensusNode[idle].(t4:TransactionSC[a:edge,idle] | l4:LocalCreditSC[idle] | v:DB[idle] | g4:GlobalCreditSC[idle] | tr4:TrMsg)) -> buyer:Client[idle,idle,idle] | seller:Client[idle,idle,idle] | c:BlockChainSystem.(d:ConsensusNode[idle].(tl:TransactionSC[idle,idle] | l1:LocalCreditSC[idle] | d1:DB[idle] | g1:GlobalCreditSC[idle]) | e:ConsensusNode[idle].(t2:TransactionSC[idle,idle] | l2:LocalCreditSC[idle] | d2:DB[idle] | g2:GlobalCreditSC[idle]) | f:ConsensusNode[idle].(t3:TransactionSC[idle,idle] | l3:LocalCreditSC[idle] | d3:DB[idle] | g3:GlobalCreditSC[idle]) | g:ConsensusNode[idle].(t4:TransactionSC[idle,idle] | l4:LocalCreditSC[idle] | v:DB[idle] | g4:GlobalCreditSC[idle])){};\n\n%rule r_consensusSuccess buyer:Client[a:edge,idle,idle] | seller:Client[idle,a:edge,idle] | c:BlockChainSystem.(d:ConsensusNode[idle].(tl:TransactionSC[a:edge,idle] | l1:LocalCreditSC[idle] | d1:DB[idle] | g1:GlobalCreditSC[idle] | tr1:TrMsg) | e:ConsensusNode[idle].(t2:TransactionSC[a:edge,idle] | l2:LocalCreditSC[idle] | d2:DB[idle] | g2:GlobalCreditSC[idle] | tr2:TrMsg) | f:ConsensusNode[idle].(t3:TransactionSC[a:edge,idle] | l3:LocalCreditSC[idle] | d3:DB[idle] | g3:GlobalCreditSC[idle] | tr3:TrMsg) | g:ConsensusNode[idle].(t4:TransactionSC[a:edge,idle] | l4:LocalCreditSC[idle] | v:DB[idle] | g4:GlobalCreditSC[idle] | tr4:TrMsg)) -> buyer:Client[a:edge,idle,idle] | seller:Client[idle,a:edge,idle] | c:BlockChainSystem.(d:ConsensusNode[idle].(tl:TransactionSC[a:edge,idle] | l1:LocalCreditSC[idle] | d1:DB[idle].tr1:TrMsg | g1:GlobalCreditSC[idle]) | e:ConsensusNode[idle].(t2:TransactionSC[a:edge,idle] | l2:LocalCreditSC[idle] | d2:DB[idle].tr2:TrMsg | g2:GlobalCreditSC[idle]) | f:ConsensusNode[idle].(t3:TransactionSC[a:edge,idle] | l3:LocalCreditSC[idle] | d3:DB[idle].tr3:TrMsg | g3:GlobalCreditSC[idle]) | g:ConsensusNode[idle].(t4:TransactionSC[a:edge,idle] | l4:LocalCreditSC[idle] | v:DB[idle].tr4:TrMsg | g4:GlobalCreditSC[idle])){};\n\n%rule r_genGlobalCredit buyer:Client[idle,idle,a:edge].cgn:CalGlobalCreditNeed | seller:Client[idle,idle,idle] | c:BlockChainSystem.(d:ConsensusNode[idle].(tl:TransactionSC[idle,idle] | l1:LocalCreditSC[idle] | d1:DB[idle] | g1:GlobalCreditSC[a:edge]) | e:ConsensusNode[idle].(t2:TransactionSC[idle,idle] | l2:LocalCreditSC[idle] | d2:DB[idle] | g2:GlobalCreditSC[a:edge]) | f:ConsensusNode[idle].(t3:TransactionSC[idle,idle] | l3:LocalCreditSC[idle] | d3:DB[idle] | g3:GlobalCreditSC[a:edge]) | g:ConsensusNode[idle].(t4:TransactionSC[idle,idle] | l4:LocalCreditSC[idle] | v:DB[idle] | g4:GlobalCreditSC[a:edge])) -> buyer:Client[idle,idle,a:edge].cgn:CalGlobalCreditNeed | seller:Client[idle,idle,idle] | c:BlockChainSystem.(d:ConsensusNode[idle].(tl:TransactionSC[idle,idle] | l1:LocalCreditSC[idle] | d1:DB[idle] | g1:GlobalCreditSC[a:edge] | gc1:GlobalCredit) | e:ConsensusNode[idle].(t2:TransactionSC[idle,idle] | l2:LocalCreditSC[idle] | d2:DB[idle] | g2:GlobalCreditSC[a:edge] | gc2:GlobalCredit) | f:ConsensusNode[idle].(t3:TransactionSC[idle,idle] | l3:LocalCreditSC[idle] | d3:DB[idle] | g3:GlobalCreditSC[a:edge] | gc3:GlobalCredit) | g:ConsensusNode[idle].(t4:TransactionSC[idle,idle] | l4:LocalCreditSC[idle] | v:DB[idle] | g4:GlobalCreditSC[a:edge] | gc4:GlobalCredit)){};\n\n%rule r_genLocalCredit buyer:Client[idle,idle,a:edge].ccn:CalLocalCreditNeed | seller:Client[idle,idle,idle] | c:BlockChainSystem.(d:ConsensusNode[idle].(tl:TransactionSC[idle,idle] | l1:LocalCreditSC[a:edge] | d1:DB[idle] | g1:GlobalCreditSC[idle]) | e:ConsensusNode[idle].(t2:TransactionSC[idle,idle] | l2:LocalCreditSC[a:edge] | d2:DB[idle] | g2:GlobalCreditSC[idle]) | f:ConsensusNode[idle].(t3:TransactionSC[idle,idle] | l3:LocalCreditSC[a:edge] | d3:DB[idle] | g3:GlobalCreditSC[idle]) | g:ConsensusNode[idle].(t4:TransactionSC[idle,idle] | l4:LocalCreditSC[a:edge] | v:DB[idle] | g4:GlobalCreditSC[idle])) -> buyer:Client[idle,idle,a:edge].ccn:CalLocalCreditNeed | seller:Client[idle,idle,idle] | c:BlockChainSystem.(d:ConsensusNode[idle].(tl:TransactionSC[idle,idle] | l1:LocalCreditSC[a:edge] | d1:DB[idle] | g1:GlobalCreditSC[idle] | lc1:LocalCredit) | e:ConsensusNode[idle].(t2:TransactionSC[idle,idle] | l2:LocalCreditSC[a:edge] | d2:DB[idle] | g2:GlobalCreditSC[idle] | lc2:LocalCredit) | f:ConsensusNode[idle].(t3:TransactionSC[idle,idle] | l3:LocalCreditSC[a:edge] | d3:DB[idle] | g3:GlobalCreditSC[idle] | lc3:LocalCredit) | g:ConsensusNode[idle].(t4:TransactionSC[idle,idle] | l4:LocalCreditSC[a:edge] | v:DB[idle] | g4:GlobalCreditSC[idle] | lc4:LocalCredit)){};\n\n%rule r_genScore buyer:Client[idle,idle,a:edge] | seller:Client[idle,idle,a:edge] | c:BlockChainSystem.(d:ConsensusNode[idle].(tl:TransactionSC[idle,a:edge] | l1:LocalCreditSC[idle] | d1:DB[idle] | g1:GlobalCreditSC[idle]) | e:ConsensusNode[idle].(t2:TransactionSC[idle,a:edge] | l2:LocalCreditSC[idle] | d2:DB[idle] | g2:GlobalCreditSC[idle]) | f:ConsensusNode[idle].(t3:TransactionSC[idle,a:edge] | l3:LocalCreditSC[idle] | d3:DB[idle] | g3:GlobalCreditSC[idle]) | g:ConsensusNode[idle].(t4:TransactionSC[idle,a:edge] | l4:LocalCreditSC[idle] | v:DB[idle] | g4:GlobalCreditSC[idle])) -> buyer:Client[idle,idle,a:edge] | seller:Client[idle,idle,a:edge] | c:BlockChainSystem.(d:ConsensusNode[idle].(tl:TransactionSC[idle,a:edge] | l1:LocalCreditSC[idle] | d1:DB[idle] | g1:GlobalCreditSC[idle] | s1:Score) | e:ConsensusNode[idle].(t2:TransactionSC[idle,a:edge] | l2:LocalCreditSC[idle] | d2:DB[idle] | g2:GlobalCreditSC[idle] | s2:Score) | f:ConsensusNode[idle].(t3:TransactionSC[idle,a:edge] | l3:LocalCreditSC[idle] | d3:DB[idle] | g3:GlobalCreditSC[idle] | s3:Score) | g:ConsensusNode[idle].(t4:TransactionSC[idle,a:edge] | l4:LocalCreditSC[idle] | v:DB[idle] | g4:GlobalCreditSC[idle] | s4:Score)){};\n\n%rule r_genTransaction buyer:Client[idle,idle,idle] | seller:Client[idle,idle,idle] | c:BlockChainSystem.(d:ConsensusNode[idle].(tl:TransactionSC[idle,idle] | l1:LocalCreditSC[idle] | d1:DB[idle] | g1:GlobalCreditSC[idle]) | e:ConsensusNode[idle].(t2:TransactionSC[idle,idle] | l2:LocalCreditSC[idle] | d2:DB[idle] | g2:GlobalCreditSC[idle]) | f:ConsensusNode[idle].(t3:TransactionSC[idle,idle] | l3:LocalCreditSC[idle] | d3:DB[idle] | g3:GlobalCreditSC[idle]) | g:ConsensusNode[idle].(t4:TransactionSC[idle,idle] | l4:LocalCreditSC[idle] | v:DB[idle] | g4:GlobalCreditSC[idle])) -> buyer:Client[a:edge,idle,idle] | seller:Client[idle,a:edge,idle] | c:BlockChainSystem.(d:ConsensusNode[idle].(tl:TransactionSC[a:edge,idle] | l1:LocalCreditSC[idle] | d1:DB[idle] | g1:GlobalCreditSC[idle]) | e:ConsensusNode[idle].(t2:TransactionSC[a:edge,idle] | l2:LocalCreditSC[idle] | d2:DB[idle] | g2:GlobalCreditSC[idle]) | f:ConsensusNode[idle].(t3:TransactionSC[a:edge,idle] | l3:LocalCreditSC[idle] | d3:DB[idle] | g3:GlobalCreditSC[idle]) | g:ConsensusNode[idle].(t4:TransactionSC[a:edge,idle] | l4:LocalCreditSC[idle] | v:DB[idle] | g4:GlobalCreditSC[idle])){};\n\n%rule r_geneTrMsg a:Client[a:edge,idle,idle] | b:Client[idle,a:edge,idle] | c:BlockChainSystem.(d:ConsensusNode[idle].(h:TransactionSC[idle,idle] | i:LocalCreditSC[idle] | j:DB[idle] | k:GlobalCreditSC[idle]) | e:ConsensusNode[idle].(l:TransactionSC[idle,idle] | m:LocalCreditSC[idle] | n:DB[idle] | o:GlobalCreditSC[idle]) | f:ConsensusNode[idle].(p:TransactionSC[idle,idle] | q:LocalCreditSC[idle] | r:DB[idle] | s:GlobalCreditSC[idle]) | g:ConsensusNode[idle].(t:TransactionSC[idle,idle] | u:LocalCreditSC[idle] | v:DB[idle] | w:GlobalCreditSC[idle])) -> a:Client[a:edge,idle,idle] | b:Client[idle,a:edge,idle] | c:BlockChainSystem.(d:ConsensusNode[idle].(h:TransactionSC[idle,idle] | i:LocalCreditSC[idle] | j:DB[idle] | k:GlobalCreditSC[idle]) | e:ConsensusNode[idle].(l:TransactionSC[idle,idle] | m:LocalCreditSC[idle] | n:DB[idle] | o:GlobalCreditSC[idle]) | f:ConsensusNode[idle].(p:TransactionSC[idle,idle] | q:LocalCreditSC[idle] | r:DB[idle] | s:GlobalCreditSC[idle]) | g:ConsensusNode[idle].(t:TransactionSC[idle,idle] | u:LocalCreditSC[idle] | v:DB[idle] | w:GlobalCreditSC[idle])){};\n\n%rule r_globalCreditConFail buyer:Client[idle,idle,a:edge].cgn:CalGlobalCreditNeed | seller:Client[idle,idle,idle] | c:BlockChainSystem.(d:ConsensusNode[idle].(tl:TransactionSC[idle,idle] | l1:LocalCreditSC[idle] | d1:DB[idle] | g1:GlobalCreditSC[a:edge] | gc1:GlobalCredit) | e:ConsensusNode[idle].(t2:TransactionSC[idle,idle] | l2:LocalCreditSC[idle] | d2:DB[idle] | g2:GlobalCreditSC[a:edge] | gc2:GlobalCredit) | f:ConsensusNode[idle].(t3:TransactionSC[idle,idle] | l3:LocalCreditSC[idle] | d3:DB[idle] | g3:GlobalCreditSC[a:edge] | gc3:GlobalCredit) | g:ConsensusNode[idle].(t4:TransactionSC[idle,idle] | l4:LocalCreditSC[idle] | v:DB[idle] | g4:GlobalCreditSC[a:edge] | gc4:GlobalCredit)) -> buyer:Client[idle,idle,idle] | seller:Client[idle,idle,idle] | c:BlockChainSystem.(d:ConsensusNode[idle].(tl:TransactionSC[idle,idle] | l1:LocalCreditSC[idle] | d1:DB[idle] | g1:GlobalCreditSC[idle]) | e:ConsensusNode[idle].(t2:TransactionSC[idle,idle] | l2:LocalCreditSC[idle] | d2:DB[idle] | g2:GlobalCreditSC[idle]) | f:ConsensusNode[idle].(t3:TransactionSC[idle,idle] | l3:LocalCreditSC[idle] | d3:DB[idle] | g3:GlobalCreditSC[idle]) | g:ConsensusNode[idle].(t4:TransactionSC[idle,idle] | l4:LocalCreditSC[idle] | v:DB[idle] | g4:GlobalCreditSC[idle])){};\n\n%rule r_globalCreditConSucc buyer:Client[idle,idle,a:edge].cgn:CalGlobalCreditNeed | seller:Client[idle,idle,idle] | c:BlockChainSystem.(d:ConsensusNode[idle].(tl:TransactionSC[idle,idle] | l1:LocalCreditSC[idle] | d1:DB[idle] | g1:GlobalCreditSC[a:edge] | gc1:GlobalCredit) | e:ConsensusNode[idle].(t2:TransactionSC[idle,idle] | l2:LocalCreditSC[idle] | d2:DB[idle] | g2:GlobalCreditSC[a:edge] | gc2:GlobalCredit) | f:ConsensusNode[idle].(t3:TransactionSC[idle,idle] | l3:LocalCreditSC[idle] | d3:DB[idle] | g3:GlobalCreditSC[a:edge] | gc3:GlobalCredit) | g:ConsensusNode[idle].(t4:TransactionSC[idle,idle] | l4:LocalCreditSC[idle] | v:DB[idle] | g4:GlobalCreditSC[a:edge] | gc4:GlobalCredit)) -> buyer:Client[idle,idle,a:edge] | seller:Client[idle,idle,idle] | c:BlockChainSystem.(d:ConsensusNode[idle].(tl:TransactionSC[idle,idle] | l1:LocalCreditSC[idle] | d1:DB[idle] | g1:GlobalCreditSC[a:edge] | gc1:GlobalCredit) | e:ConsensusNode[idle].(t2:TransactionSC[idle,idle] | l2:LocalCreditSC[idle] | d2:DB[idle] | g2:GlobalCreditSC[a:edge] | gc2:GlobalCredit) | f:ConsensusNode[idle].(t3:TransactionSC[idle,idle] | l3:LocalCreditSC[idle] | d3:DB[idle] | g3:GlobalCreditSC[a:edge] | gc3:GlobalCredit) | g:ConsensusNode[idle].(t4:TransactionSC[idle,idle] | l4:LocalCreditSC[idle] | v:DB[idle] | g4:GlobalCreditSC[a:edge] | gc4:GlobalCredit)){};\n\n%rule r_localCreditConFail buyer:Client[idle,idle,a:edge].ccn:CalLocalCreditNeed | seller:Client[idle,idle,idle] | c:BlockChainSystem.(d:ConsensusNode[idle].(tl:TransactionSC[idle,idle] | l1:LocalCreditSC[a:edge] | d1:DB[idle] | g1:GlobalCreditSC[idle] | lc1:LocalCredit) | e:ConsensusNode[idle].(t2:TransactionSC[idle,idle] | l2:LocalCreditSC[a:edge] | d2:DB[idle] | g2:GlobalCreditSC[idle] | lc2:LocalCredit) | f:ConsensusNode[idle].(t3:TransactionSC[idle,idle] | l3:LocalCreditSC[a:edge] | d3:DB[idle] | g3:GlobalCreditSC[idle] | lc3:LocalCredit) | g:ConsensusNode[idle].(t4:TransactionSC[idle,idle] | l4:LocalCreditSC[a:edge] | v:DB[idle] | g4:GlobalCreditSC[idle] | lc4:LocalCredit)) -> buyer:Client[idle,idle,idle] | seller:Client[idle,idle,idle] | c:BlockChainSystem.(d:ConsensusNode[idle].(tl:TransactionSC[idle,idle] | l1:LocalCreditSC[idle] | d1:DB[idle] | g1:GlobalCreditSC[idle]) | e:ConsensusNode[idle].(t2:TransactionSC[idle,idle] | l2:LocalCreditSC[idle] | d2:DB[idle] | g2:GlobalCreditSC[idle]) | f:ConsensusNode[idle].(t3:TransactionSC[idle,idle] | l3:LocalCreditSC[idle] | d3:DB[idle] | g3:GlobalCreditSC[idle]) | g:ConsensusNode[idle].(t4:TransactionSC[idle,idle] | l4:LocalCreditSC[idle] | v:DB[idle] | g4:GlobalCreditSC[idle])){};\n\n%rule r_localCreditConSucc buyer:Client[idle,idle,a:edge].ccn:CalLocalCreditNeed | seller:Client[idle,idle,idle] | c:BlockChainSystem.(d:ConsensusNode[idle].(tl:TransactionSC[idle,idle] | l1:LocalCreditSC[a:edge] | d1:DB[idle] | g1:GlobalCreditSC[idle] | lc1:LocalCredit) | e:ConsensusNode[idle].(t2:TransactionSC[idle,idle] | l2:LocalCreditSC[a:edge] | d2:DB[idle] | g2:GlobalCreditSC[idle] | lc2:LocalCredit) | f:ConsensusNode[idle].(t3:TransactionSC[idle,idle] | l3:LocalCreditSC[a:edge] | d3:DB[idle] | g3:GlobalCreditSC[idle] | lc3:LocalCredit) | g:ConsensusNode[idle].(t4:TransactionSC[idle,idle] | l4:LocalCreditSC[a:edge] | v:DB[idle] | g4:GlobalCreditSC[idle] | lc4:LocalCredit)) -> buyer:Client[idle,idle,a:edge].ccn:CalLocalCreditNeed | seller:Client[idle,idle,idle] | c:BlockChainSystem.(d:ConsensusNode[idle].(tl:TransactionSC[idle,idle] | l1:LocalCreditSC[a:edge] | d1:DB[idle].lc1:LocalCredit | g1:GlobalCreditSC[idle]) | e:ConsensusNode[idle].(t2:TransactionSC[idle,idle] | l2:LocalCreditSC[a:edge] | d2:DB[idle].lc2:LocalCredit | g2:GlobalCreditSC[idle]) | f:ConsensusNode[idle].(t3:TransactionSC[idle,idle] | l3:LocalCreditSC[a:edge] | d3:DB[idle].lc3:LocalCredit | g3:GlobalCreditSC[idle]) | g:ConsensusNode[idle].(t4:TransactionSC[idle,idle] | l4:LocalCreditSC[a:edge] | v:DB[idle].lc4:LocalCredit | g4:GlobalCreditSC[idle])){};\n\n%rule r_makeScore buyer:Client[a:edge,idle,idle] | seller:Client[idle,a:edge,idle] | c:BlockChainSystem.(d:ConsensusNode[idle].(tl:TransactionSC[a:edge,idle] | l1:LocalCreditSC[idle] | d1:DB[idle].tr1:TrMsg | g1:GlobalCreditSC[idle]) | e:ConsensusNode[idle].(t2:TransactionSC[a:edge,idle] | l2:LocalCreditSC[idle] | d2:DB[idle].tr2:TrMsg | g2:GlobalCreditSC[idle]) | f:ConsensusNode[idle].(t3:TransactionSC[a:edge,idle] | l3:LocalCreditSC[idle] | d3:DB[idle].tr3:TrMsg | g3:GlobalCreditSC[idle]) | g:ConsensusNode[idle].(t4:TransactionSC[a:edge,idle] | l4:LocalCreditSC[idle] | v:DB[idle].tr4:TrMsg | g4:GlobalCreditSC[idle])) -> seller:Client[idle,idle,a:edge] | c:BlockChainSystem.(d:ConsensusNode[idle].(tl:TransactionSC[idle,a:edge] | l1:LocalCreditSC[idle] | d1:DB[idle] | g1:GlobalCreditSC[idle]) | e:ConsensusNode[idle].(t2:TransactionSC[idle,a:edge] | l2:LocalCreditSC[idle] | d2:DB[idle] | g2:GlobalCreditSC[idle]) | f:ConsensusNode[idle].(t3:TransactionSC[idle,a:edge] | l3:LocalCreditSC[idle] | d3:DB[idle] | g3:GlobalCreditSC[idle]) | g:ConsensusNode[idle].(t4:TransactionSC[idle,a:edge] | l4:LocalCreditSC[idle] | v:DB[idle] | g4:GlobalCreditSC[idle])) | buyer:Client[idle,idle,a:edge]{};\n\n%rule r_scoreConFail buyer:Client[idle,idle,a:edge] | seller:Client[idle,idle,a:edge] | c:BlockChainSystem.(d:ConsensusNode[idle].(tl:TransactionSC[idle,a:edge] | l1:LocalCreditSC[idle] | d1:DB[idle] | g1:GlobalCreditSC[idle] | s1:Score) | e:ConsensusNode[idle].(t2:TransactionSC[idle,a:edge] | l2:LocalCreditSC[idle] | d2:DB[idle] | g2:GlobalCreditSC[idle] | s2:Score) | f:ConsensusNode[idle].(t3:TransactionSC[idle,a:edge] | l3:LocalCreditSC[idle] | d3:DB[idle] | g3:GlobalCreditSC[idle] | s3:Score) | g:ConsensusNode[idle].(t4:TransactionSC[idle,a:edge] | l4:LocalCreditSC[idle] | v:DB[idle] | g4:GlobalCreditSC[idle] | s4:Score)) -> buyer:Client[idle,idle,idle] | seller:Client[idle,idle,idle] | c:BlockChainSystem.(d:ConsensusNode[idle].(tl:TransactionSC[idle,idle] | l1:LocalCreditSC[idle] | d1:DB[idle] | g1:GlobalCreditSC[idle]) | e:ConsensusNode[idle].(t2:TransactionSC[idle,idle] | l2:LocalCreditSC[idle] | d2:DB[idle] | g2:GlobalCreditSC[idle]) | f:ConsensusNode[idle].(t3:TransactionSC[idle,idle] | l3:LocalCreditSC[idle] | d3:DB[idle] | g3:GlobalCreditSC[idle]) | g:ConsensusNode[idle].(t4:TransactionSC[idle,idle] | l4:LocalCreditSC[idle] | v:DB[idle] | g4:GlobalCreditSC[idle])){};\n\n%rule r_scoreConsucc buyer:Client[idle,idle,a:edge] | seller:Client[idle,idle,a:edge] | c:BlockChainSystem.(d:ConsensusNode[idle].(tl:TransactionSC[idle,a:edge] | l1:LocalCreditSC[idle] | d1:DB[idle] | g1:GlobalCreditSC[idle] | s1:Score) | e:ConsensusNode[idle].(t2:TransactionSC[idle,a:edge] | l2:LocalCreditSC[idle] | d2:DB[idle] | g2:GlobalCreditSC[idle] | s2:Score) | f:ConsensusNode[idle].(t3:TransactionSC[idle,a:edge] | l3:LocalCreditSC[idle] | d3:DB[idle] | g3:GlobalCreditSC[idle] | s3:Score) | g:ConsensusNode[idle].(t4:TransactionSC[idle,a:edge] | l4:LocalCreditSC[idle] | v:DB[idle] | g4:GlobalCreditSC[idle] | s4:Score)) -> buyer:Client[idle,idle,idle] | seller:Client[idle,idle,idle] | c:BlockChainSystem.(d:ConsensusNode[idle].(tl:TransactionSC[idle,idle] | l1:LocalCreditSC[idle] | d1:DB[idle].s1:Score | g1:GlobalCreditSC[idle]) | e:ConsensusNode[idle].(t2:TransactionSC[idle,idle] | l2:LocalCreditSC[idle] | d2:DB[idle].s2:Score | g2:GlobalCreditSC[idle]) | f:ConsensusNode[idle].(t3:TransactionSC[idle,idle] | l3:LocalCreditSC[idle] | d3:DB[idle].s3:Score | g3:GlobalCreditSC[idle]) | g:ConsensusNode[idle].(t4:TransactionSC[idle,idle] | l4:LocalCreditSC[idle] | v:DB[idle].s4:Score | g4:GlobalCreditSC[idle])){};\n\n%rule r_wantCalGlobalCredit buyer:Client[idle,idle,idle] | seller:Client[idle,idle,idle] | c:BlockChainSystem.(d:ConsensusNode[idle].(tl:TransactionSC[idle,idle] | l1:LocalCreditSC[idle] | d1:DB[idle].s1:Score | g1:GlobalCreditSC[idle]) | e:ConsensusNode[idle].(t2:TransactionSC[idle,idle] | l2:LocalCreditSC[idle] | d2:DB[idle].s2:Score | g2:GlobalCreditSC[idle]) | f:ConsensusNode[idle].(t3:TransactionSC[idle,idle] | l3:LocalCreditSC[idle] | d3:DB[idle].s3:Score | g3:GlobalCreditSC[idle]) | g:ConsensusNode[idle].(t4:TransactionSC[idle,idle] | l4:LocalCreditSC[idle] | v:DB[idle].s4:Score | g4:GlobalCreditSC[idle])) -> buyer:Client[idle,idle,idle] | seller:Client[idle,idle,idle] | c:BlockChainSystem.(d:ConsensusNode[idle].(tl:TransactionSC[idle,idle] | l1:LocalCreditSC[idle] | d1:DB[idle] | g1:GlobalCreditSC[idle]) | e:ConsensusNode[idle].(t2:TransactionSC[idle,idle] | l2:LocalCreditSC[idle] | d2:DB[idle] | g2:GlobalCreditSC[idle]) | f:ConsensusNode[idle].(t3:TransactionSC[idle,idle] | l3:LocalCreditSC[idle] | d3:DB[idle] | g3:GlobalCreditSC[idle]) | g:ConsensusNode[idle].(t4:TransactionSC[idle,idle] | l4:LocalCreditSC[idle] | v:DB[idle] | g4:GlobalCreditSC[idle])) | cgn:CalGlobalCreditNeed{};\n\n%rule r_wantCalLocalCredit buyer:Client[idle,idle,idle] | seller:Client[idle,idle,idle] | c:BlockChainSystem.(d:ConsensusNode[idle].(tl:TransactionSC[idle,idle] | l1:LocalCreditSC[idle] | d1:DB[idle].s1:Score | g1:GlobalCreditSC[idle]) | e:ConsensusNode[idle].(t2:TransactionSC[idle,idle] | l2:LocalCreditSC[idle] | d2:DB[idle].s2:Score | g2:GlobalCreditSC[idle]) | f:ConsensusNode[idle].(t3:TransactionSC[idle,idle] | l3:LocalCreditSC[idle] | d3:DB[idle].s3:Score | g3:GlobalCreditSC[idle]) | g:ConsensusNode[idle].(t4:TransactionSC[idle,idle] | l4:LocalCreditSC[idle] | v:DB[idle].s4:Score | g4:GlobalCreditSC[idle])) -> buyer:Client[idle,idle,idle] | seller:Client[idle,idle,idle] | c:BlockChainSystem.(d:ConsensusNode[idle].(tl:TransactionSC[idle,idle] | l1:LocalCreditSC[idle] | d1:DB[idle] | g1:GlobalCreditSC[idle]) | e:ConsensusNode[idle].(t2:TransactionSC[idle,idle] | l2:LocalCreditSC[idle] | d2:DB[idle] | g2:GlobalCreditSC[idle]) | f:ConsensusNode[idle].(t3:TransactionSC[idle,idle] | l3:LocalCreditSC[idle] | d3:DB[idle] | g3:GlobalCreditSC[idle]) | g:ConsensusNode[idle].(t4:TransactionSC[idle,idle] | l4:LocalCreditSC[idle] | v:DB[idle] | g4:GlobalCreditSC[idle])) | ccn:CalLocalCreditNeed{};\n\n\n\n# Model\n%agent buyer:Client[idle,idle,idle] | c:BlockChainSystem.(e:ConsensusNode[idle].(t2:TransactionSC[idle,idle] | l2:LocalCreditSC[idle] | d2:DB[idle] | g2:GlobalCreditSC[idle]) | f:ConsensusNode[idle].(t3:TransactionSC[idle,idle] | l3:LocalCreditSC[idle] | d3:DB[idle] | g3:GlobalCreditSC[idle]) | g:ConsensusNode[idle].(t4:TransactionSC[idle,idle] | l4:LocalCreditSC[idle] | v:DB[idle] | g4:GlobalCreditSC[idle]) | d:ConsensusNode[idle].(t1:TransactionSC[idle,idle] | l1:LocalCreditSC[idle] | d1:DB[idle] | g1:GlobalCreditSC[idle])) | seller:Client[idle,idle,idle];\n\n\n\n\n\n\n\n\n\n#SortingLogic\n\n\n# Go!\n%check;"
//    val t: List[BGMTerm] = BGMParser.parseFromString(bgm);
//    val b: Bigraph = BGMTerm.toBigraph(t);
//    var simulator = new BlockChainSimulator(b)
//  //  simulator.simulate
//  }
//}