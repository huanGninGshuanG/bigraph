package org.bigraph.bigsim.simulator

import org.bigraph.bigsim.BRS.{Graph, Vertex}
import org.bigraph.bigsim.ctlimpl.CTLCheckResult
import org.bigraph.bigsim.model.Bigraph
import org.bigraph.bigsim.modelchecker.LTLModelChecker
import org.bigraph.bigsim.parser.{BGMParser, BGMTerm}
import org.bigraph.bigsim.transitionsystem.State
import org.bigraph.bigsim.utils.DebugPrinter
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.mutable.Map
// 需要使用到java中的变量
import scala.collection.JavaConversions._


class LTLSimulator
(nb: Bigraph) extends Simulator {

  // 这里的b用于测试， 不测试记得把这句注释掉,并且把主构造器里面的 nb 改成b
  //  val b = testLTLSimulator.b
  val b = nb

  var v: Vertex = new Vertex(b, null, null); // 每个节点包含一个bigraph、第一个节点是初始agent，Vertex传入的参数分别表示：当前偶图，该偶图的“父节点”，父节点反应到当前的反应规则
  var g: Graph = new Graph(v);

  val transition = new TransitionSystem(b)
  val ltlParser = new LTLSpec(b.ltlSpec, b.prop.toMap)

  var checkRes: Boolean = false;
  var recordPath: List[State] = List()
  var recordMap: Map[State, State] = Map()

  def simulate: Unit = {
    b.root = b.structToTerm()
    if (b == null || b.root == null) {
      println("LTL simulator::simulate(): null");
      return;
    } else {
      val buildKripke = new BuildKripkeStructure(transition)
      val kripke = buildKripke.buildKripke(b.bigSignature)
      val ltlModelChecker = new LTLModelChecker(kripke)
      this.checkRes = ltlModelChecker.satisfies(ltlParser.getLTLFormula())
      logger.debug("LTL check res: " + checkRes)
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
    var res = ""
    if (this.checkRes) {
      res = "成功"
    } else {
      res = "失败"
    }
    val r = res;
    r
  }

  def getFormula(): String = {
    return ltlParser.getLTLFormula().toString
  }
}


object testLTLSimulator {
  val example2 =
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
      |%ltlSpec G!p;
      |
      |# Go!
      |%check;
      |
      |""".stripMargin

  var example1 =
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
      |%active T : 0;
      |%active Turn : 1;
      |%active Value : 1;
      |%active DoSomething : 1;
      |
      |# Rules
      |%rule r_0 p0:Process[idle].$0 | p1:Process[idle].$1 | turn:Turn[a:edge].(zero:Value[a:edge] | one:Value[idle]) -> p0:Process[b:edge].a:DoSomething[b:edge] | p1:Process[idle].$1 | turn:Turn[a:edge].(zero:Value[a:edge] | one:Value[idle]){};
      |
      |%rule r_1 p0:Process[idle].t0:T | p1:Process[idle].t1:T | turn:Turn[idle].(zero:Value[idle] | one:Value[idle]) -> p0:Process[idle].t0:T | p1:Process[idle].t1:T | turn:Turn[a:edge].(zero:Value[a:edge] | one:Value[idle]){};
      |
      |%rule r_2 p0:Process[idle].t0:T | p1:Process[idle].t1:T | turn:Turn[idle].(zero:Value[idle] | one:Value[idle]) -> p0:Process[idle].t0:T | p1:Process[idle].t1:T | turn:Turn[a:edge].(zero:Value[idle] | one:Value[a:edge]){};
      |
      |%rule r_3 p0:Process[a:edge].a:DoSomething[a:edge] | p1:Process[idle].$1 | turn:Turn[b:edge].(zero:Value[b:edge] | one:Value[idle]) -> p0:Process[idle].t0:T | p1:Process[idle].t1:T | turn:Turn[idle].(one:Value[idle] | zero:Value[idle]){};
      |
      |
      |
      |# prop
      |%prop a  p1:Process[b:edge].b:DoSomething[b:edge] | p0:Process[a:edge].a:DoSomething[a:edge] | c:Turn[idle].$0{};
      |%prop b  p1:Process[a:edge].a:DoSomething[a:edge] | p0:Process[idle].$1 | c:Turn[idle].$0{};
      |
      |
      |# Model
      |%agent  p0:Process[idle].t0:T<1> | p1:Process[idle].t1:T<2> | turn:Turn[idle].(zero:Value[idle] | one:Value[idle]);
      |
      |
      |
      |# LTL_Formula
      |%ltlSpec G!(a);
      |
      |
      |
      |#SortingLogic
      |
      |
      |# Go!
      |%check;
      |""".stripMargin

  var example3 =
    """
      |# Controls
      |%active CriticalSection : 1;
      |%active Process : 1;
      |
      |# Names
      |%outername a;
      |
      |# Rules
      |%rule r_0 a:CriticalSection[a:outername].$0 | b:Process[a:outername] -> a:CriticalSection[a:outername].($0 | b:Process[a:outername]){};
      |
      |%rule r_1 a:CriticalSection[a:outername].(b:Process[a:outername] | $0) -> a:CriticalSection[a:outername].$0 | b:Process[idle]{};
      |
      |%rule r_2 a:CriticalSection[a:outername].$0 | b:Process[idle] -> a:CriticalSection[a:outername].$0 | b:Process[a:outername]{};
      |
      |%rule r_3 a:CriticalSection[a:outername].$0 | c:Process[a:outername] -> a:CriticalSection[a:outername].($0 | c:Process[a:outername]){};
      |
      |%rule r_4 a:CriticalSection[a:outername].(c:Process[a:outername] | $0) -> a:CriticalSection[a:outername].$0 | c:Process[idle]{};
      |
      |%rule r_5 a:CriticalSection[a:outername].$0 | c:Process[idle] -> a:CriticalSection[a:outername].$0 | c:Process[a:outername]{};
      |
      |# prop
      |%prop p  a:CriticalSection[a:edge].(b:Process[a:edge] | $0){};
      |
      |
      |# Model
      |%agent  a:CriticalSection[idle].nil|b:Process[idle].nil|c:Process[idle].nil;
      |
      |
      |
      |# LTL_Formula
      |%ltlSpec G!(p);
      |
      |#SortingLogic
      |
      |
      |# Go!
      |%check;
      |""".stripMargin

  var example4 =
    """
      |# Controls
      |%active Building : 0;
      |%active Laptop : 0;
      |%active FolderA : 1;
      |%active FolderB : 2;
      |%active Data : 0;
      |
      |# Names
      |%outername x;
      |%outername y;
      |
      |# Rules
      |%rule r_0 l2:Laptop.($0 | f3:FolderA[y:outername].$1) | l3:Laptop.($2 | f4:FolderA[y:outername].$4) -> l2:Laptop.($0 | f3:FolderA[y:outername].$1) | l3:Laptop.($2 | f3:FolderA[y:outername].$4){};
      |
      |# prop
      |%prop p  l2:Laptop.($0 | f3:FolderA[y:outername].$1) | l3:Laptop.($2 | f3:FolderA[y:outername].$4){};
      |
      |
      |# Model
      |%agent a:Building.(l1:Laptop.(f1:FolderA[E_1:edge].d1:Data.nil)) | b:Building.(l2:Laptop.(f2:FolderB[E_1:edge,E_1:edge].d2:Data.nil | f3:FolderA[x:outername].d3:Data.nil) | l3:Laptop.(f4:FolderA[x:outername].d4:Data.nil));
      |
      |
      |
      |# LTL_Formula
      |%ltlSpec G!(p);
      |
      |#SortingLogic
      |
      |
      |# Go!
      |%check;
      |""".stripMargin


  val t = BGMParser.parseFromString(example4)
  val b = BGMTerm.toBigraph(t)

  def logger: Logger = LoggerFactory.getLogger(this.getClass)

  def main(args: Array[String]): Unit = {
    val simulator = new LTLSimulator(b)
    DebugPrinter.print(logger, "bigraph sites: " + b.inner)
    simulator.simulate
    var dotStr = simulator.dumpDotForward("")
    println(dotStr)

  }
}