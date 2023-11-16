package org.bigraph.bigsim.simulator

import java.util

import org.bigraph.bigsim.BRS.{Graph, Match, Vertex}
import org.bigraph.bigsim.Verify
import org.bigraph.bigsim.model.{Bigraph, BindingChecker, Nil, ReactionRule}
import org.bigraph.bigsim.modelchecker.{CTLModelChecker, CTLModelCheckerENF}
import org.bigraph.bigsim.parser.{BGMParser, BGMTerm}
import org.bigraph.bigsim.transitionsystem.State
import org.bigraph.bigsim.utils.{GlobalCfg, OS, bankV3}
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.mutable
import scala.collection.mutable.Buffer
import scala.collection.mutable.{Map, Queue, Set}
// 需要使用到java中的变量
import scala.collection.JavaConverters._
import scala.collection.JavaConversions._


class CTLSimulator
(nb: Bigraph) extends Simulator {

  // 这里的b用于测试， 不测试记得把这句注释掉,并且把主构造器里面的 nb 改成b
  //val b = testCTLSimulator.b
  val b = nb

  var v: Vertex = new Vertex(b, null, null); // 每个节点包含一个bigraph、第一个节点是初始agent，Vertex传入的参数分别表示：当前偶图，该偶图的“父节点”，父节点反应到当前的反应规则
  var g: Graph = new Graph(v);

  val transition = new TransitionSystem(b)
  val ctlParser = new CTLSpec(b.ctlSpec, b.prop.toMap)

  var checkRes: Boolean = false;
  var recordPath: List[State] = List()
  var recordMap: Map[State, State] = Map()


  def simulate: Unit = {
    if (b == null || b.root == null) {
      println("CTL simulator::simulate(): null");
      return;
    } else {
      val buildKripke = new BuildKripkeStructure(transition)
      val kripke = buildKripke.buildKripke
      val ctlModelChecker = new CTLModelChecker(kripke)
      this.checkRes = ctlModelChecker.satisfies(ctlParser.getCTLFormula())
      val enfRes = CTLModelCheckerENF.satisfy(kripke, ctlParser.getCTLFormula())
      if (enfRes.res != this.checkRes) {
        throw new Exception("enf 实现错误")
      }
      logger.debug("test111: " + enfRes.path + " " + enfRes.`type`)
      if (ctlModelChecker.recordPath != null) {
        this.recordPath = ctlModelChecker.recordPath.toList
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
      this.g.addCTLRes(recordPath, checkRes)

      //logger.debug("CTL模型检测结果: " + ctlModelChecker.satisfies(ctlParser.getCTLFormula()))
      logger.debug("==========================CTL模型检测结果: " + this.checkRes)
      logger.debug("==========================生成的状态个数: " + buildKripke.bigraphList.size)
      if (!checkRes) {
        logger.debug("反例路径: " + recordPath)
        logger.debug("反例映射: " + recordMap)
        //        println(recordPath)
        //        println(recordMap)
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
    return ctlParser.getCTLFormula().toString
  }
}


object testCTLSimulator {

  val bigraphExam =
    """# Controls
      |%active Greater : 2;
      |%active Less : 2;
      |%active GreaterOrEqual : 2;
      |%active LessOrEqual : 2;
      |%active Equal : 2;
      |%active NotEqual : 2;
      |%active Exist : 1;
      |%active InstanceOf : 2;
      |%active Client : 2;
      |%active Coin : 2;
      |%active Register : 1;
      |%active Data : 0;
      |%active call_foo : 0;
      |%active True : 0;
      |%active False : 0;
      |%active aNum : 1;
      |%active SmartContract : 1;
      |%active Control1 : 0;
      |
      |# Rules
      |%rule r_test3 a:Client[idle,a:edge].(c:Coin[idle,idle] | d:Coin[idle,idle] | e:Coin[idle,idle] | h:Register[idle]) | b:Client[idle,a:edge].(f:Coin[idle,idle] | g:Coin[idle,idle] |i:Register[idle]) -> a:Client[idle,a:edge].(c:Coin[idle,idle] | d:Coin[idle,idle] | h:Register[idle]) | b:Client[idle,a:edge].(f:Coin[idle,idle] | g:Coin[idle,idle] | i:Register[idle]) | e:Coin[idle,idle]{Probability:0.3};
      |
      |%rule r_test4 a:Client[idle,a:edge].(h:Register[idle] | _class_1639578794528:Coin[idle,idle] | f:Coin[idle,idle] | $0) | b:Client[idle,a:edge].(g:Coin[idle,idle] | i:Register[idle]) -> a:Client[idle,a:edge].(h:Register[idle] | _class_1639578794528:Coin[idle,idle] | f:Coin[idle,idle] | $0) | b:Client[idle,a:edge].(g:Coin[idle,idle] | i:Register[idle]){};
      |
      |%rule r_test4 a:Client[idle,a:edge].(h:Register[idle] | c:Coin[idle,idle] | $0) | b:Client[idle,a:edge].(d:Coin[idle,idle] | i:Register[idle] | e:Coin[idle,idle]) -> nil{};
      |
      |# prop
      |%prop test a:Client[idle,a:edge].(c:Coin[idle,idle] | d:Coin[idle,idle] | e:Coin[idle,idle] | h:Register[idle]) | b:Client[idle,a:edge].(f:Coin[idle,idle] | g:Coin[idle,idle] | i:Register[idle]){};
      |
      |# Model
      |%agent a:Client[idle,a:edge].(c:Coin[idle,idle] | d:Coin[idle,idle] | e:Coin[idle,idle] | h:Register[idle]) | b:Client[idle,a:edge].(f:Coin[idle,idle] | g:Coin[idle,idle] | i:Register[idle]);
      |
      |# CTL_Formula
      |%ctlSpec AG(test);
      |
      |#SortingLogic
      |
      |# Go!
      |%check;
      |""".stripMargin

  val bigraphExam2 =
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
      |%active Client : 2;
      |%active Coin : 2;
      |%active Register : 1;
      |%active Data : 0;
      |%active call_foo : 0;
      |%active True : 0;
      |%active False : 0;
      |%active aNum : 1;
      |%active SmartContract : 1;
      |%active Control1 : 0;
      |
      |# Rules
      |%rule r_InMoveCoin1 a:Client[idle,a:edge].(d:Coin[idle,idle] | g:Register[idle].c:Coin[idle,idle]) | b:Client[idle,a:edge].(e:Coin[idle,idle] | f:Coin[idle,idle] | h:Register[idle]) -> a:Client[idle,a:edge].g:Register[idle].c:Coin[idle,idle] | b:Client[idle,a:edge].(e:Coin[idle,idle] | f:Coin[idle,idle] | h:Register[idle]) | d:Coin[idle,idle]{};
      |
      |%rule r_InMoveCoin2 a:Client[idle,a:edge].g:Register[idle].c:Coin[idle,idle] | b:Client[idle,a:edge].(e:Coin[idle,idle] | f:Coin[idle,idle] | h:Register[idle]) | d:Coin[idle,idle] -> a:Client[idle,a:edge].g:Register[idle].c:Coin[idle,idle] | b:Client[b:edge,a:edge].(e:Coin[idle,idle] | f:Coin[idle,idle] | h:Register[idle]) | d:Coin[idle,b:edge]{};
      |
      |%rule r_OutMoveCoin1 a:Client[idle,a:edge].(c:Coin[idle,idle] | d:Coin[idle,idle] | g:Register[idle]) | b:Client[idle,a:edge].(e:Coin[idle,idle] | f:Coin[idle,idle] | h:Register[idle]) -> a:Client[idle,a:edge].(c:Coin[idle,idle] | g:Register[idle]) | b:Client[idle,a:edge].(e:Coin[idle,idle] | f:Coin[idle,idle] | h:Register[idle]) | d:Coin[idle,idle]{};
      |
      |%rule r_OutMoveCoin2 a:Client[idle,a:edge].(c:Coin[idle,idle] | g:Register[idle]) | b:Client[idle,a:edge].(e:Coin[idle,idle] | f:Coin[idle,idle] | h:Register[idle]) | d:Coin[idle,idle] -> a:Client[idle,a:edge].(c:Coin[idle,idle] | g:Register[idle]) | b:Client[b:edge,a:edge].(e:Coin[idle,idle] | f:Coin[idle,idle] | h:Register[idle]) | d:Coin[idle,b:edge]{};
      |
      |%rule r_coinMoveToRegister a:Client[idle,a:edge].(c:Coin[idle,idle] | d:Coin[idle,idle] | g:Register[idle]) | b:Client[idle,a:edge].(e:Coin[idle,idle] | f:Coin[idle,idle] | h:Register[idle]) -> a:Client[idle,a:edge].(d:Coin[idle,idle] | g:Register[idle].c:Coin[idle,idle]) | b:Client[idle,a:edge].(e:Coin[idle,idle] | f:Coin[idle,idle] | h:Register[idle]){};
      |
      |
      |# prop
      |%prop p a:Register[idle].b:Coin[idle,idle]{};
      |%prop q b:Client[b:edge,a:outername].$0 | d:Coin[idle,b:edge]{};
      |
      |# CTL_Formula
      |%ctlSpec AX(p)->AF(q);
      |
      |# Model
      |%agent a:Client[idle,a:edge].(c:Coin[idle,idle] | d:Coin[idle,idle] | g:Register[idle]) | b:Client[idle,a:edge].(e:Coin[idle,idle] | f:Coin[idle,idle] | h:Register[idle]);
      |
      |#SortingLogic
      |
      |# Go!
      |%check;
      |""".stripMargin
  var s =
    """
      |# Controls
      |%active Greater : 2;
      |%active Node : 2;
      |%active Container : 0;
      |%binding Bind;
      |
      |# Names
      |%outername d;
      |
      |# Rules
      |%rule r_test1 Container.(e:Container.(b:Node[idle,d:binding]) | c:Node[idle,d:binding] | d:Bind) ->
      |Container.(e:Container | b:Node[idle,d:binding] | c:Node[idle,d:binding] | d:Bind){};
      |%rule r_test2 Container.(Node[idle,d:outername] | $0) | $1 -> Container.$0| Node[idle,d:outername] | $1{};
      |
      |# Model
      |%agent a:Container.(e:Container.(b:Node[idle,d:binding]) | c:Node[idle,d:binding] | d:Bind);
      |
      |#SortingLogic
      |
      |# Go!
      |%check;
      |""".stripMargin

  val propValue =
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
      |%active Container : 0;
      |%active Number : 1;
      |%active Count : 1;
      |
      |# Rules
      |%rule r_rule1 a1:Container.(c:Count[idle] | n1:Number[idle]) | a2:Container.n2:Number[idle] -> a1:Container.(c:Count[a:edge] | n1:Number[a:edge]) | a2:Container.n2:Number[a:edge]{};
      |%rule r_rule2 a1:Container.(c:Count[a:edge] | n1:Number[a:edge]) | a2:Container.n2:Number[a:edge] -> a1:Container.(c:Count[a:edge] | n1:Number[a:edge]) | a2:Container.n2:Number[a:edge]{Condition:Container.Count>0	Assign:Container.Count=Container.Count-1,a1.Number=a1.Number+a2.Number};
      |
      |# prop
      |%prop p a1:Container.(c:Count[a:edge] | n1:Number[a:edge]) | a2:Container.n2:Number[a:edge]
      | {PropExpr:a1.Number==(2-Container.Count)*a2.Number+2};
      |
      |# CTL_Formula
      |%ctlSpec AF(p);
      |
      |# Model
      |%agent a1:Container.(n1:Number<2>[idle] | c:Count<2>[idle]) | a2:Container.n2:Number<3>[idle] {};
      |
      |#SortingLogic
      |
      |# Go!
      |%check;
      |""".stripMargin

  val testbug2 =
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
      |%active Container : 0;
      |%active Num : 1;
      |%active Watch : 0;
      |
      |# Rules
      |%rule r_0 Num[idle] | Num[idle] -> Num[a:edge] | Num[a:edge]{};
      |
      |# Model
      |%agent  a:Container.(b:Num[idle] | c:Num[idle] | d:Watch | e:Watch.f:Num[idle]);
      |
      |#SortingLogic
      |
      |# Go!
      |%check;
      |""".stripMargin


  //val t = BGMParser.parseFromString(kgqbankattack)
  //  val t = BGMParser.parseFromString(s)


  val test = "# Controls\n%active Greater : 2;\n%active Less : 2;\n%active GreaterOrEqual : 2;\n%active LessOrEqual : 2;\n%active Equal : 2;\n%active NotEqual : 2;\n%active Exist : 1;\n%active InstanceOf : 2;\n%active Plus : 3;\n%active Minus : 3;\n%active Multiply : 3;\n%active Division : 3;\n%active Opposite : 2;\n%active Abs : 2;\n%active User : 2;\n%active File : 2;\n%active Permission : 2;\n%active Root : 0;\n%active Command : 2;\n%active Parameter : 0;\n%binding Bind;\n%active String : 2;\n%active OwnerGroup : 2;\n%active Group : 0;\n\n# Rules\n%rule r_0 file:File[idle,idle] | u1:User[idle,idle] | $0 -> file:File[idle,idle].(rwx:Permission[idle,idle].owner:User[idle,idle] | r:Permission[idle,idle].a:OwnerGroup[idle,idle]) | u1:User[idle,idle].(name1:String[idle,idle] | A:Group) | $0{};\n\n%rule r_1 file:File[idle,idle] | u2:User[idle,idle] | $0 -> file:File[idle,idle].rwx:Permission[idle,idle] | u2:User[idle,idle].(name2:String[idle,idle] | A:Group) | $0{};\n\n\n\n\n# Model\n%agent  file:File[idle,idle] | u1:User[idle,idle] | u2:User[idle,idle] | u3:User[idle,idle] | u4:User[idle,idle];\n\n\n\n\n\n\n\n\n#SortingLogic\n\n\n# Go!\n%check;"
  //  val t = BGMParser.parseFromString(OS.rwWithEFP)
  val t = BGMParser.parseFromString(bigraphExam)
  val b = BGMTerm.toBigraph(t)

  def main(args: Array[String]): Unit = {
    val startTime = System.currentTimeMillis()
    val simulator = new CTLSimulator(b)
    simulator.simulate
    //simulator.simulatorRes()
    //var dotStr = simulator.dumpPaths()
    //println(simulator.recordPath.size)

    val endTime = System.currentTimeMillis()
    printf("=========================================================模型检测耗时: ")
    print((endTime - startTime) / 1000f)
    println("s")
  }
}