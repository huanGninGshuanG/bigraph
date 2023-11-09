package org.bigraph.bigsim.simulator

import java.util

import org.bigraph.bigsim.BRS.{Graph, Match, Vertex}
import org.bigraph.bigsim.Verify
import org.bigraph.bigsim.model.{Bigraph, BindingChecker, Nil, Paraller, Prefix, ReactionRule}
import org.bigraph.bigsim.modelchecker.{CTLModelChecker, CTLModelCheckerOnTheFly}
import org.bigraph.bigsim.parser.{BGMParser, BGMTerm}
import org.bigraph.bigsim.transitionsystem.State
import org.bigraph.bigsim.utils.{GlobalCfg, bankV3}
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.mutable
import scala.collection.mutable.Buffer
import scala.collection.mutable.{Map, Queue, Set}
// 需要使用到java中的变量
import scala.collection.JavaConverters._
import scala.collection.JavaConversions._

class CTLSimulatorOnTheFly(nb: Bigraph) extends Simulator {

  // 这里的b用于测试， 不测试记得把这句注释掉,并且把主构造器里面的 nb 改成b
  //val b = testCTLSimulator.b
  val b = nb

  var v: Vertex = new Vertex(b, null, null); // 每个节点包含一个bigraph、第一个节点是初始agent，Vertex传入的参数分别表示：当前偶图，该偶图的“父节点”，父节点反应到当前的反应规则
  var g: Graph = new Graph(v);

  val transition = new TransitionSystemOnTheFly(b)
  val ctlParser = new CTLSpec(b.ctlSpec, b.prop.toMap)

  var checkRes: Boolean = false;
  var recordPath: List[State] = List()
  var recordMap: Map[State, State] = Map()


  def simulate: Unit = {
    if (b == null || b.root == null) {
      println("CTL simulator::simulate(): null");
      return ;
    } else {
      val kripkeStructure = this.transition.buildKripke
      val ctlModelChecker = new CTLModelCheckerOnTheFly(kripkeStructure)
      this.checkRes = ctlModelChecker.satisfies(ctlParser.getCTLFormula())
      if(ctlModelChecker.recordPath != null){
      this.recordPath = ctlModelChecker.recordPath.toList
      if (recordPath.nonEmpty){
        var pre: State = this.recordPath(0)
        this.recordPath.tail.foreach(x => {
          this.recordMap += (pre -> x)
          pre = x
        })
      }}
      this.v = transition.v
      this.g = transition.g
      this.g.addCTLRes(recordPath, checkRes)

      logger.debug("CTL模型检测结果: " + ctlModelChecker.satisfies(ctlParser.getCTLFormula()))

      //logger.debug("kripkeStructure: " + kripkeStructure)
      logger.debug("所有生成的偶图为：")
      for(state<-kripkeStructure.getAllStates){
        var stateB=transition.stateToVertex(state).bigraph.root
        var ParallerB=stateB.asInstanceOf[Paraller]
        var ParallerBValue=getParallerNodeValue(ParallerB)
        var ParallerBValues=ParallerBValue.split(",")
        if(ParallerBValues.contains("LPC:3.0")&&ParallerBValues.contains("PC1:3.0")&&ParallerBValues.contains("PC2:3.0")&&ParallerBValues.contains("PC3:3.0")&&ParallerBValues.contains("CC1:3.0")&&ParallerBValues.contains("CC2:3.0")&&ParallerBValues.contains("CC3:3.0")&&ParallerBValues.contains("LPC:3.0")) {
          println(ParallerB)
          println(ParallerBValue)
        }

      }
      logger.debug("一共生成的偶图总和："+kripkeStructure.getAllStates.size())
      if (!checkRes) {
        println("recordPath is: " + recordPath)
        println(recordMap)
      }
    }
  }
  def getPrefixNodeValue(p:Prefix): String ={
    var s=""
    if(p.node.name!=""&&p.node.number!=null) {
      s=p.node.name+":"+p.node.number+","
    }
    p.suffix match {
      case prefix: Prefix => return s + getPrefixNodeValue(prefix)
      case paraller: Paraller=>return s+getParallerNodeValue(paraller)
      case _ =>
    }
    s
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
  def getParallerNodeValue(p: Paraller): String ={
    var s=""
    p.leftTerm match {
      case paraller: Paraller =>
        s+=getParallerNodeValue(paraller)
      case prefix: Prefix =>
        s+=getPrefixNodeValue(prefix)
      case _ =>
    }

    p.rightTerm match {
      case paraller: Paraller =>
        s+=getParallerNodeValue(paraller)
      case prefix: Prefix =>
        s+=getPrefixNodeValue(prefix)
      case _ =>
    }
    s
  }
  
  def dumpDotForward(dot: String): String = {
    val dotStr = this.g.dumpDotFile() //打印到dot文件
    dotStr
  }
  def dumpPaths(): String = {
    val paths = this.g.dumpPaths()
    paths
  }

  def getFormula():String={
    return ctlParser.getCTLFormula().toString
  }
}


object testCTLSimulatorOntTheFly {

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
      |%prop test a:Client[idle,a:edge].(c:Coin[idle,idle] | d:Coin[idle,idle] | e:Coin[idle,idle] | h:Register[idle]) | b:Client[idle,a:edge].(f:Coin[idle,idle] | g:Coin[idle,idle] | i:Register[idle]);
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
      |%prop p a:Register[idle].b:Coin[idle,idle];
      |%prop q b:Client[b:edge,a:outername].$0 | d:Coin[idle,b:edge];
      |
      |# CTL_Formula
      |%ctlSpec AF(p);
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

  val t = BGMParser.parseFromString(bankV3.normal)

  val b = BGMTerm.toBigraph(t)
  def main(args: Array[String]): Unit = {
    val startTime=System.currentTimeMillis()
    val simulator = new CTLSimulatorOnTheFly(b)
    simulator.simulate

//    var dotStr = simulator.dumpDotForward("")
//    println(dotStr)
//    //var paths = simulator.dumpPaths();
//    //pkuResource.fileContent += " <hr/>" + extMap + "<hr/>" + pkuResource.extContent;

    val endTime=System.currentTimeMillis()
    printf("===============time consuming: %d=====================",endTime-startTime)
  }
}