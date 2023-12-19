package org.bigraph.bigsim.modelchecker

import java.util.UUID

import domain.{PkuLog, PkuLogItem, PkuResource, PkuStrategy}
import org.bigraph.bigsim.BRS.Match
import org.bigraph.bigsim.model.Bigraph
import org.bigraph.bigsim.parser.{BGMParser, BGMTerm}
import org.joda.time.DateTime
import org.slf4j.{Logger, LoggerFactory}
import play.libs.exception.ExceptionUtils
import services.{LogItemService, LogService}

import scala.collection.mutable.Set

/**
  * @author amy
  */

abstract class MCSimulator {
  var dot: String = "" //子类构造dot
  def simulate(pkuResource: PkuResource, strategy : PkuStrategy): String //每个子类重写的是这个抽象方法
  def dumpDotForward(dot: String): String
}

object MCSimulator {

  var matchDiscard: Set[Match] = Set()

  def matchMarkDelete(m: Match): Unit = {
    assert(m != null)
    matchDiscard.add(m)
  }

  def matchGC: Unit = {
    matchDiscard.clear()
  }

  def simulate(pkuResource: PkuResource, strategy : PkuStrategy):String = {

    var simFactory = new MCSimulatorFactory()
    return simFactory.simulate(pkuResource, strategy)
  }


  private var logService : LogService = _
  def setLogService(_logService: LogService): Unit ={
    if(logService == null){
      logService = _logService
    }
  }
  def getLogService : LogService = {
    logService
  }
  private var logItemService : LogItemService = _
  def setLogItemService(_logItemService: LogItemService) : Unit ={
    if(logItemService == null){
      logItemService = _logItemService
    }
  }
  def getLogItemService: LogItemService = {
    logItemService
  }
}

class MCSimulatorFactory {
  def logger: Logger = LoggerFactory.getLogger(this.getClass)


  def simulate(pkuResource: PkuResource, strategy : PkuStrategy):String = {

    var start = System.currentTimeMillis()
    var middle: Long = 0

    val batchId = UUID.randomUUID().toString

    var t: List[BGMTerm] = null

    try {
      // xin 20190713
      t = BGMParser.parseFromString(pkuResource.fileContent)

      logger.debug("--------------------- lixin")
      t.foreach(x=>{
        logger.debug(x.toString)
      })
      logger.debug("--------------------- lixin end")

    } catch {
      case t: Throwable => {
        logger.error("BGMParser", t)
        return savePkuLog(pkuResource = pkuResource,
          strategy = strategy,
          batchId = batchId, finalResult = "failed", startTime = start, endTime = System.currentTimeMillis(),
          dot = "", result = "失败",ExceptionUtils.getStackTrace(t),detectionResult = "mc",formula ="")
    }
  }

    val b: Bigraph = BGMTerm.toBigraph(t) //从bgm文件构建一个偶图 lry val

    val simulator = new MCMainSimulator(b)
    val dotStr = simulator.simulate(pkuResource, strategy) //call

    //simulator.dumpDotForward(dot) //调用生成dot文件

    val logItemId =  savePkuLog(pkuResource = pkuResource,
      strategy = strategy,
      batchId = batchId, finalResult = "successed",
      startTime = start, endTime = System.currentTimeMillis(),
      dot = dotStr, result = "成功",null,"mc","formula")

    //分别是不包含读写的模拟时间和总时间（模拟时间+读写文件）
    logger.debug("模拟时间  Total:\tmiddle:" + MCMainSimulator.start + ", end:" + MCMainSimulator.middle + ", used:" + (MCMainSimulator.middle - MCMainSimulator.start) + " ms")
    logger.debug("模拟时间+读写文件  Total:\tstart:" + MCMainSimulator.start + ", end:" + MCMainSimulator.end + ", used:" + (MCMainSimulator.end - MCMainSimulator.start) + " ms")

    return logItemId
  }

  def savePkuLog(pkuResource: PkuResource,strategy: PkuStrategy,
                 batchId: String, finalResult:String, startTime : Long, endTime: Long,
                 dot:String, result : String, errMsg:String,detectionResult:String,formula:String):String = {

    val pkuLog = createPkuLog(pkuResource,  strategy.id, strategy.name, batchId, finalResult,detectionResult,formula)
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
      formula =formula
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
      executeDetailLog = "exec",
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

