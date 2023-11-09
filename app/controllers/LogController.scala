package controllers

import java.text.SimpleDateFormat
import java.util.Date

import domain.{PkuLog, PkuLogQuery}
import javax.inject.{Inject, _}
import play.api.mvc._
import services.LogService
import utils.JSONUtil

import scala.concurrent.ExecutionContext

/**
 * 日志处理
 */
@Singleton
class LogController @Inject()(logService: LogService,cc: ControllerComponents) (implicit ec: ExecutionContext)extends AbstractController(cc) {
  implicit val jsonFormats = org.json4s.DefaultFormats

  /**
   * Create an Action to render an HTML page.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def log() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.aipComponentLogs.log_main())
  }

  def list = Action.async { implicit request: Request[AnyContent] =>
    var strategyName =  request.body.asFormUrlEncoded.get("strategyName").head
    var resourceName = request.body.asFormUrlEncoded.get("resourceName").head
    var result =  request.body.asFormUrlEncoded.get("result").head
    var startTime =  request.body.asFormUrlEncoded.get("startTime").head
    var endTime =  request.body.asFormUrlEncoded.get("endTime").head
    val sDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss") //加上时间
    var startDate:Date = null
    if(!startTime.isEmpty){
      startDate = sDateFormat.parse(startTime)
    }
    var endDate:Date =null
    if(!endTime.isEmpty){
      endDate = sDateFormat.parse(endTime)
    }

    var pkuLogQuery = PkuLogQuery(startTime,endTime,"",strategyName,"",resourceName,result)
    println(request.body.asFormUrlEncoded.get("strategyName"))
    println(request.body.asFormUrlEncoded.get("resourceName"))
    println(request.body.asFormUrlEncoded.get("result"))
    println(request.body.asFormUrlEncoded.get("startTime"))
    println(request.body.asFormUrlEncoded.get("endTime"))

    logService.getList(request.body.asFormUrlEncoded.get("start").head.toInt/10, 10,pkuLogQuery).map { page =>
      val str = JSONUtil.listToJSONString(page)

      println(str)

      Ok(str).as("application/json")
    }
  }

  def getById(id:String) = Action { implicit request: Request[AnyContent] =>

    var log = logService.getById(id);

    val strRes =  JSONUtil.toJSONStr(log.get)

    Ok(strRes).as("application/json")
  }

  def getByBatchId(batchId:String) = Action { implicit request: Request[AnyContent] =>

    var log = logService.getByBatchId(batchId);

    val strRes =  JSONUtil.toJSONStr(log.get)

    Ok(strRes).as("application/json")
  }


  def delById(id:String) = Action { implicit request: Request[AnyContent] =>

    var isSuccess = logService.delById(id);

    val strRes =  JSONUtil.toJSONStr(isSuccess.toString)

    Ok(strRes).as("application/json")
  }


  def savaOrUpdate = Action {  implicit request: Request[AnyContent] =>

    println(request.body.asJson)

    val json = request.body.asJson

    println(json.get.toString)

    val log = JSONUtil.GSON.fromJson(json.get.toString, classOf[PkuLog])

    if(log.id!=null){
      logService.updateById(log);
    }else{
      logService.save(log);
    }

    val strRes =  JSONUtil.toJSONStr(log.id)


    Ok(strRes).as("application/json")
  }

  /**
    * 查询当天的执行组件消息
    */

  def findExecuteLogSum = Action {  implicit request: Request[AnyContent] =>

    println(request.body.asJson)

    val json = request.body.asJson

    var map = logService.getCount();

    val strRes =  JSONUtil.toJSONStr(map)

    Ok(strRes).as("application/json")
  }

}
