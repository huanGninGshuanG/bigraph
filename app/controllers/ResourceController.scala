package controllers

import com.google.gson.JsonParser
import domain.PkuResource
import javax.inject._
import play.api.mvc._
import services.ResourceService
import utils.JSONUtil

import scala.concurrent.ExecutionContext

/**
 * 资源处理
 */
@Singleton
class ResourceController @Inject()(resourceService:ResourceService, cc: ControllerComponents)(implicit ec: ExecutionContext) extends AbstractController(cc) {

  /**
   * Create an Action to render an HTML page.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def resource() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.aipResource.resource_main())
  }

  def form() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.aipResource.resource_form())
  }


  def getById(id:String) =  Action {

    val resource = resourceService.getById(id)

    val strRes = JSONUtil.toJSONString(resource)

    Ok(strRes).as("application/json")
  }

  def list = Action.async { implicit request =>

    println(request.body.asJson)

    println(request.body.asFormUrlEncoded.get("start").head.toInt)

    println(request.body.asFormUrlEncoded.get("name").head)

    resourceService.getList(request.body.asFormUrlEncoded.get("start").head.toInt/10, 10, request.body.asFormUrlEncoded.get("name").head).map { page =>

      val str = JSONUtil.listToJSONString(page)

      println(str)

      Ok(str).as("application/json")
    }
  }

  def listAll = Action.async { implicit request =>

    println(request.body.asJson)


    resourceService.getList(0, 10000, "").map { page =>

      val str = JSONUtil.listToJSONString(page)

      println(str)

      Ok(str).as("application/json")
    }
  }

  def delById(id:String) = Action { implicit request: Request[AnyContent] =>

    var isSuccess = resourceService.delById(id);

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

    jsonArray.forEach(a=>resourceService.delById(a.getAsString))

    val strRes =  JSONUtil.toJSONStr(true.toString)

    Ok(strRes).as("application/json")
  }


  def savaOrUpdate = Action {  implicit request: Request[AnyContent] =>

    println(request.body.asJson)

    val json = request.body.asJson

    println(json.get.toString)

    val resource = JSONUtil.GSON.fromJson(json.get.toString, classOf[PkuResource])

//    resource.creator = "PKU"
//    resource.modifiedBy = "PKU"

    resource.description =  java.net.URLDecoder.decode(resource.description,"utf-8")
    resource.fileContent = java.net.URLDecoder.decode(resource.fileContent,"utf-8")
    resource.extContent = "extContent"
    if(resource.id!=null&&resource.id.trim.length>0){
      resourceService.updateById(resource);
    }else{
      resourceService.save(resource);
    }

    val strRes =  JSONUtil.toJSONStr(resource.id)


    Ok(strRes).as("application/json")
  }


}
