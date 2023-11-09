package controllers

import com.google.gson.JsonParser
import domain.PkuStrategy
import javax.inject._
import play.api.mvc._
import services.{StrategyService}
import utils.JSONUtil

import scala.concurrent.ExecutionContext

/**
  * 策略管理
  */
@Singleton
class StrategyManagerController @Inject()(strategyService:StrategyService, cc: ControllerComponents)(implicit ec: ExecutionContext) extends AbstractController(cc) {


  def strategy() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.aipStrategyManager.strategy_main())
  }

  def getById(id:String) =  Action {

    val strategy = strategyService.getById(id)

    val strRes = JSONUtil.toJSONString(strategy)

    Ok(strRes).as("application/json")
  }

  def list = Action.async { implicit request =>

    println(request.body.asJson)

    println(request.body.asFormUrlEncoded.get("start").head.toInt)

    println(request.body.asFormUrlEncoded.get("name").head)

    strategyService.getList(request.body.asFormUrlEncoded.get("start").head.toInt/10, 10, request.body.asFormUrlEncoded.get("name").head).map { page =>

      val str = JSONUtil.listToJSONString(page)

      println(str)

      Ok(str).as("application/json")
    }
  }

  def listAll = Action.async { implicit request =>

    println(request.body.asJson)

    strategyService.getList(0, 10000, "").map { page =>

      val str = JSONUtil.listToJSONString(page)

      println(str)

      Ok(str).as("application/json")
    }
  }

  def delById(id:String) = Action { implicit request: Request[AnyContent] =>

    var isSuccess = strategyService.delById(id);

    val strRes =  JSONUtil.toJSONStr(isSuccess.toString)

    Ok(strRes).as("application/json")
  }

  def delByIds = Action { implicit request: Request[AnyContent] =>

    println(request.body.asJson)

    val json = request.body.asJson

    println(json.get.\("ids").get)
    //先转JsonObject
    val jsonObject = new JsonParser().parse(json.get.toString()).getAsJsonObject();
    //再转JsonArray 加上数据头
    val jsonArray = jsonObject.getAsJsonArray("ids");

    jsonArray.forEach(a=>strategyService.delById(a.getAsString))

    val strRes =  JSONUtil.toJSONStr(true.toString)

    Ok(strRes).as("application/json")
  }


  def savaOrUpdate = Action {  implicit request: Request[AnyContent] =>

    println(request.body.asJson)

    val json = request.body.asJson

    println(json.get.toString)

    val strategy = JSONUtil.GSON.fromJson(json.get.toString, classOf[PkuStrategy])

    //    strategy.creator = "PKU"
    //    strategy.modifiedBy = "PKU"

    strategy.description =  java.net.URLDecoder.decode(strategy.description,"utf-8")
    strategy.name = java.net.URLDecoder.decode(strategy.name,"utf-8")

    if(strategy.id!=null&&strategy.id.trim.length>0){
      strategyService.updateById(strategy);
    }else{
      strategyService.save(strategy);
    }

    val strRes =  JSONUtil.toJSONStr(strategy.id)


    Ok(strRes).as("application/json")
  }


}
