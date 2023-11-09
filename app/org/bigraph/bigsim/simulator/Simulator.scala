package org.bigraph.bigsim.simulator

import java.io.File
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

import domain.enums.SimDataTypeEnum
import domain.enums.SimDataTypeEnum.SimDataTypeEnum
import domain.{PkuLog, PkuLogItem, PkuResource, PkuStrategy}
import org.bigraph.bigsim.BRS.Match
import org.bigraph.bigsim.data.Data
import org.bigraph.bigsim.model.Bigraph
import org.bigraph.bigsim.modelchecker.MCSimulator
import org.bigraph.bigsim.parser.{BGMParser, BGMTerm, HMM}
import org.bigraph.bigsim.utils.{GlobalCfg, OS, bankV3}
import org.joda.time.DateTime
import org.slf4j.{Logger, LoggerFactory}
import play.libs.exception.ExceptionUtils

import scala.collection.mutable.Set

abstract class Simulator {
  def logger : Logger = LoggerFactory.getLogger(this.getClass)
  var dot: String = "";//子类构造dot
  def simulate: Unit;//每个子类重写的是这个抽象方法
  def dumpDotForward(dot: String): String;
  def dumpPaths(): String;
  def simulatorRes(): String;
  def getFormula(): String;

  var concurrent = new ConcurrentHashMap[String, String]();
  var charIndex = 0;

  def formatHash(hash: Int): String = {
    var str = "";
    if (hash < 0) str = "" + hash.abs;
    else str = hash.toString;

    if (!concurrent.containsKey(str)) {
      concurrent.put(str, getStrValue(charIndex))
      charIndex = charIndex + 1
    }

    concurrent.get(str)
  }
  def getStrValue(index: Int) = {
    val k = index / 26
    var str = ""
    if (k > 0) {
      str += ((k % 26) - 1 + 'A').toChar + "_"
    }
    str += ((index % 26) + 'A').toChar
    str
  }
}

object Simulator {

  var matchDiscard: Set[Match] = Set();
  def matchMarkDelete(m: Match): Unit = {
    assert(m != null);
    matchDiscard.add(m);
  }

  def matchGC: Unit = {
    matchDiscard.clear();
  }

  def simulate(pkuResource: PkuResource, strategy : PkuStrategy,extMap:Map[SimDataTypeEnum,String]):String = {
    var simFactory = new SimulatorFactory();
    return simFactory.simulate(pkuResource, strategy, extMap);
  }
}

class SimulatorFactory {

  var simulator: Simulator = null;

//  def simulate() {
//
//    def logger : Logger = LoggerFactory.getLogger(this.getClass)
//
//    var start = System.currentTimeMillis();
//    var middle: Long = 0;
//
//    var t:List[BGMTerm] = null;
//
//    // parse the BGM input file
//    try{
//      if(GlobalCfg.IsFromNetWork){
//        t = BGMParser.parseFromString(GlobalCfg.bgmContent)
//      }
//      else
//        t = BGMParser.parse(new File(GlobalCfg.filename));
//    }catch {
//      case t: Throwable => {t.printStackTrace();return;} // TODO: handle error
//    }
//
//    var dot: String = ""
//
//
//
//    for (i <- 1 to GlobalCfg.simLoop) {//每次loop都new了一个新的simulator
//
//      val b: Bigraph = BGMTerm.toBigraph(t);//每次loop从bgm文件构建一个偶图
//
//      GlobalCfg.violateSorting=false
//      GlobalCfg.violateBinding=false
//      GlobalCfg.violateExceptionTracking=false
//
//      // init variables for each loop simulation
//      GlobalCfg.SysClk = 0
//      GlobalCfg.curLoop = i
//
//      if (GlobalCfg.checkData) Data.parseData(GlobalCfg.dataInput)
//      if (GlobalCfg.isBackDerivation) Data.parseData(GlobalCfg.bdDataInput)
//      if (GlobalCfg.checkHMM) HMM.parseHMM(GlobalCfg.hmmInput)
//      //      if (GlobalCfg.checkSorting) Bigraph.sorting.init(GlobalCfg.sortingInput)
//
//      middle = System.currentTimeMillis();
//
//      simulate(GlobalCfg.SimulatorClass, b)
//      dot += simulator.dot
//    }
//    simulator.dumpDotForward(dot)//调用生成dot文件
//    var end = System.currentTimeMillis();
//    //    println("\n****************************************************************")
//    //    println("  Total:\tmiddle:" + middle + ", end:" + end + ", used:" + (end - middle) + " ms");
//    //    println("  Total:\tmiddle:" + start + ", end:" + middle + ", used:" + (middle - start) + " ms");
//    //    println("  Total:\tstart:" + start + ", end:" + end + ", used:" + (end - start) + " ms");
//    //    println("****************************************************************")
//
//    //分别是不包含读写的模拟时间和总时间（模拟时间+读写文件）
//    logger.debug("  Total:\tmiddle:" + MainSimulator.start + ", end:" + MainSimulator.middle + ", used:" + (MainSimulator.middle - MainSimulator.start) + " ms");
//    logger.debug("  Total:\tstart:" + MainSimulator.start + ", end:" + MainSimulator.end + ", used:" + (MainSimulator.end - MainSimulator.start) + " ms");
//  }


  // 这个方法就是用来进行模拟的入口，其中PkuResource就是从数据库中读出来的模型信息，PkuStrategy就是从数据库中读取出来的策略的信息，extMap目前来看是空的
  def simulate(pkuResource: PkuResource, strategy : PkuStrategy, extMap:Map[SimDataTypeEnum,String]):String = {
//    println("getttt");
//    println(pkuResource);//filecontent 项语言
//    println("getttt");
//    println(strategy);//衍化策略
//    println("getttt");
//    println(extMap);//空
    def logger : Logger = LoggerFactory.getLogger(this.getClass)

    var start = System.currentTimeMillis();
    var middle: Long = 0;

    val batchId = UUID.randomUUID().toString

    var t:List[BGMTerm] = null;

    // parse the BGM input file
    try{
//      t = BGMParser.parse(new File(GlobalCfg.filename));


      if (GlobalCfg.kgqbankmc) {
        //t = BGMParser.parseFromString(GlobalCfg.kgqbankmodel)
        println("修改！！！")
        pkuResource.fileContent = bankV3.normal
        t = BGMParser.parseFromString(pkuResource.fileContent)
      } else {
        println(pkuResource.fileContent)
        t = BGMParser.parseFromString(pkuResource.fileContent)         // 先从pkuResource.fileContent 中把字符串形式的内容解析出来，fileContent的格式就是在bigm中导出时展示的那个
//        println("wao!")
//        println(t)
//        println("wao!")
        //bgmterm包括偶图 规则 。。。等
      }

      println("resolved bgm file:\n" + t)     // 处理之后的bgm file长这样： List(BGMControl(Greater,2,true,false), BGMControl(Less,2,true,false), BGMControl(GreaterOrEqual,2,true,false), BGMControl(LessOrEqual,2,true,false), BGMControl(Equal,2,true,false), BGMControl(NotEqual,2,true,false), BGMControl(Exist,1,true,false), BGMControl(InstanceOf,2,true,false), BGMControl(Client,2,true,false), BGMControl(Coin,2,true,false), BGMControl(Register,1,true,false), BGMControl(Data,0,true,false), BGMControl(call_foo,0,true,false), BGMControl(True,0,true,false), BGMControl(False,0,true,false), BGMControl(aNum,1,true,false), BGMControl(SmartContract,1,true,false), BGMControl(Control1,0,true,false), BGMAgent(a:Client[idle,a:edge].(c:Coin[idle,idle] | d:Coin[idle,idle] | e:Coin[idle,idle] | h:Register[idle]) | b:Client[idle,a:edge].(f:Coin[idle,idle] | g:Coin[idle,idle] | i:Register[idle])),BGMNil())
    }catch {
      case t: Throwable => {
        t.printStackTrace();
        return savePkuLog(pkuResource = pkuResource,
          strategy = strategy,
          batchId = batchId, finalResult = "failed", startTime = start, endTime = System.currentTimeMillis(),
          dot = "", result = "失败",ExceptionUtils.getStackTrace(t),"失败","nil");
      }
    }

    var dot: String = ""



//    for (i <- 1 to GlobalCfg.simLoop) {//每次loop都new了一个新的simulator

      val b: Bigraph = BGMTerm.toBigraph(t);      //每次loop从bgm文件构建一个偶图
      println(b);


      GlobalCfg.violateSorting=false
      GlobalCfg.violateBinding=false
      GlobalCfg.violateExceptionTracking=false

      // init variables for each loop simulation
      GlobalCfg.SysClk = 0
//      GlobalCfg.curLoop = i

//      if (GlobalCfg.checkData) Data.parseData(GlobalCfg.dataInput)
    // 若需要情景数据则从扩展map中取值, extMap size=0 时，获取的值为 None
    if(extMap.get(SimDataTypeEnum.SITUATION_DATA)!=null &&
      !None.equals(extMap.get(SimDataTypeEnum.SITUATION_DATA))){
       Data.parseDataWithData(extMap.get(SimDataTypeEnum.SITUATION_DATA).get);
    }
      if (GlobalCfg.isBackDerivation) Data.parseBDData(GlobalCfg.bdDataInput)
      if (GlobalCfg.checkHMM) HMM.parseHMM(GlobalCfg.hmmInput)
//      if (GlobalCfg.checkSorting) Bigraph.sorting.init(GlobalCfg.sortingInput)

      middle = System.currentTimeMillis();

//    simulate(GlobalCfg.SimulatorClass, b)
//      test by Kz
    //      println("comehere")
//      println(strategy.code)
      simulate(strategy.code, b, pkuResource)
      dot += simulator.dot

//    }
    logger.debug("kgq: -------------------- 生成 dot文件 ----------------------")
    println(dot)
    var dotStr = simulator.dumpDotForward(dot)//调用生成dot文件
    logger.debug("kgq: 这个dot 文件长这样：", dotStr)
    println(dotStr)
    logger.debug("kgq: --------------------- 调用 dumpPaths()------------------")
    var paths = simulator.dumpPaths();
    pkuResource.fileContent += " <hr/>" + extMap + "<hr/>" + pkuResource.extContent;

    logger.debug("kgq: ---------------------- 保存日志 -------------------------")
    val logItemId =  savePkuLog(pkuResource = pkuResource,
      strategy = strategy,
      batchId = batchId, finalResult = "successed",
      startTime = start, endTime = System.currentTimeMillis(),
      dot = dotStr, result = "成功",paths,simulator.simulatorRes(),simulator.getFormula())

    var end = System.currentTimeMillis();

    //分别是不包含读写的模拟时间和总时间（模拟时间+读写文件）
    logger.debug("  Total:\tmiddle:" + MainSimulator.start + ", end:" + MainSimulator.middle + ", used:" + (MainSimulator.middle - MainSimulator.start) + " ms");
    logger.debug("  Total:\tstart:" + MainSimulator.start + ", end:" + MainSimulator.end + ", used:" + (MainSimulator.end - MainSimulator.start) + " ms");

    logger.debug("kgq: --------------------模拟 simulate 结束--------------------")
    logItemId
  }
//  上面的simulate会对模型进行处理，成Bigraph格式，然后来模拟
  def simulate(sn: String, b: Bigraph, pkuResource: PkuResource): Unit = {
    sn match {
      case "TimeSlicingSimulator" => {
        println("TimeSlicingSimulator")
        simulator = new TimeSlicingSimulator(b)
      }
      case "TimeSlicingSimulator1" => {
        println("TimeSlicingSimulator1")
        simulator = new TimeSlicingSimulator1(b)
      }
     case "EnumSimulator" => {
        println("EnumSimulator")
        simulator = new EnumSimulator(b)
      }
      case "DiscreteEventSimulator" => {
        println("DiscreteEventSimulator")
        simulator = new DiscreteEventSimulator(b)
      }
      case "DiscreteEventSimulator1" => {
        println("DiscreteEventSimulator1")
        simulator = new DiscreteEventSimulator1(b)
      }
      case "StochasticSimulator" => {
        println("StochasticSimulator")
        simulator = new StochasticSimulator(b)
      }
      case "StochasticSimulator1" => {
        println("StochasticSimulator1")
        simulator = new StochasticSimulator1(b)
      }
      case "EnumSimulator1" => {
        println("EnumSimulator1")
        simulator = new EnumSimulator1(b)
      }
      case "MainSimulator" => {
        println("MainSimulator")
        simulator = new MainSimulator(b)
      }
      case "HMMSimulator" => {
        println("HMMSimulator")
        simulator = new HMMSimulator(b, pkuResource)
      }
      case "CTLSimulator" => {
        println("CTLSimulator")
        simulator = new CTLSimulator(b)
      }
      case "CTLSimulatorOnTheFly" => {
        println("CTLSimulatorOnTheFly")
        simulator = new CTLSimulatorOnTheFly(b)
      }
      case "LTLSimulator" => {
        println("LTLSimulator")
        simulator = new LTLSimulator(b)
      }
      case "CTLSimulatorPOR" => {
        println("CTLSimulatorPOR")
        simulator = new CTLSimulatorPOR(b)
      }
      case _ => {
        println("Error with Simulator: Class " + sn + " not found.")
        return
      }
    }
    // 调用方法，开始模拟，simulator 根据strategy名字分类实例化，并把构造好的偶图传入了。
    simulator.simulate //call
  }

  def savePkuLog(pkuResource: PkuResource,strategy: PkuStrategy,
                 batchId: String, finalResult:String, startTime : Long, endTime: Long,
                 dot:String, result : String, errMsg:String,detectionResult:String,formula:String):String = {

    val pkuLog = createPkuLog(pkuResource,  strategy.id, strategy.name, batchId, finalResult, detectionResult,formula)
    MCSimulator.getLogService.save(pkuLog)

    //    if("failed".equals(finalResult)){
    //      return ""
    //    }
    val pkuLogItem = createLogItem(pkuResource, batchId, startTime, endTime,dot, result, errMsg,detectionResult,formula);

    return MCSimulator.getLogItemService.save(pkuLogItem)
  }

  def createPkuLog(pkuResource: PkuResource, strategyId: String, strategyName: String,
                   batchId: String, finalResult:String,detectionResult:String,formula:String): PkuLog = {
    val pkuLog = new PkuLog(id = UUID.randomUUID().toString,
      createDate = DateTime.now().toDate,
      creator= "pku",
      del=false,
      description= "desc",
      invalid= false,
      modifiedBy= "pku",
      modifyDate=DateTime.now().toDate,
      orderNo= 0,
      batchId= batchId,
      businessDesc= "businessDesc",
      strategyId= strategyId,
      strategyName= strategyName,
      resourceId = pkuResource.id,
      resourceName= pkuResource.name,
      counts=  0,
      finalResult= finalResult,
      detectionResult= detectionResult,
      formula = formula
    )
    pkuLog
  }

  def createLogItem(pkuResource: PkuResource, batchId: String,startTime: Long, endTime: Long, dot:String, executeResult: String,errMsg:String,detectionResult:String,formula:String): PkuLogItem = {
    val pkuLogItem = new PkuLogItem(id= UUID.randomUUID().toString,
      createDate = DateTime.now().toDate,
      creator = "pku",
      del = false,
      description = "log item",
      invalid = false,
      modifiedBy = "pku",
      modifyDate = DateTime.now().toDate,
      orderNo = 0,
      batchId = batchId,
      businessDesc = "bigsim exec",
      startTime = new DateTime(startTime).toDate,
      endTime = new DateTime(endTime).toDate,
      executeDetailLog = errMsg,
      executeResult = executeResult,
      flowSize = 0,
      inputParam = pkuResource.fileContent,
      outputParam = errMsg,
      outputGraph = dot,
      detectionResult = detectionResult,
      formula=formula)
      pkuLogItem
  }
}

object quickTest {
  def main(args: Array[String]): Unit = {
    // 避免每次测试都要启动BigM
    var inputModel =
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
        |
        |%rule r_rule2 a1:Container.(c:Count[a:edge] | n1:Number[a:edge]) | a2:Container.n2:Number[a:edge] -> a1:Container.(c:Count[a:edge] | n1:Number[a:edge]) | a2:Container.n2:Number[a:edge]{Cond:c>0	Exp:c=c-1,n1=n1+n2};
        |
        |%rule r_rule3 a1:Container.(c:Count[a:edge] | n1:Number[a:edge]) | a2:Container.n2:Number[a:edge] -> a1:Container.(c:Count[a:edge] | n1:Number[idle]) | a2:Container.n2:Number[idle]{Cond:c==0};
        |
        |# Model
        |%agent a1:Container.(n1:Number[idle] | c:Count[idle]) | a2:Container.n2:Number[idle] {agentInitialState:c=2,n1=3,n2=4};
        |
        |#SortingLogic
        |
        |# Go!
        |%check;
        |""".stripMargin

    val inputModel2 =
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
        |%rule r_testControl Container.(Number[idle] | $0) -> Container.$0 | Number[idle]{};
        |%rule r_testControl2 Number[idle] -> Number[idle] | Number[idle]{};
        |
        |# Model
        |%agent a1:Container.(n1:Number[idle] | c:Count[idle]) | a2:Container.n2:Number[idle] {agentInitialState:c=1,n1=2,n2=3};
        |
        |#SortingLogic
        |
        |# Go!
        |%check;
        |""".stripMargin
    val t = BGMParser.parseFromString(inputModel2)
    val b = BGMTerm.toBigraph(t)
    val sim : Simulator = new EnumSimulator(b)
    sim.simulate



        }
}


