package controllers

import java.util.{Date, UUID}

import com.google.gson.JsonParser
import domain.{PkuAcl, PkuMenu}
import javax.inject._
import play.api.mvc._
import services.{AclService, MenuService}
import utils.JSONUtil

import scala.concurrent.ExecutionContext

/**
  * 用户管理
  */
@Singleton
class AclController @Inject()(aclService:AclService, menuService:MenuService,cc: ControllerComponents)(implicit ec: ExecutionContext) extends AbstractController(cc) {

  def getById(id:String) =  Action {

    val strategy = aclService.getById(id)

    val strRes = JSONUtil.toJSONString(strategy)

    Ok(strRes).as("application/json")
  }

  def list = Action.async { implicit request =>

    println(request.body.asJson)

    println(request.body.asFormUrlEncoded.get("start").head.toInt)

    println(request.body.asFormUrlEncoded.get("name").head)

    aclService.getList(request.body.asFormUrlEncoded.get("start").head.toInt/10, 10, request.body.asFormUrlEncoded.get("name").head).map { page =>

      val str = JSONUtil.listToJSONString(page)

      println(str)

      Ok(str).as("application/json")
    }
  }

  def listAll = Action.async { implicit request =>

    println(request.body.asJson)

    aclService.getList(0, 10000, "").map { page =>

      val str = JSONUtil.listToJSONString(page)

      println(str)

      Ok(str).as("application/json")
    }
  }

  def delById(id:String) = Action { implicit request: Request[AnyContent] =>

    var isSuccess = aclService.delById(id);

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

    jsonArray.forEach(a=>aclService.delById(a.getAsString))

    val strRes =  JSONUtil.toJSONStr(true.toString)

    Ok(strRes).as("application/json")
  }


  def savaOrUpdate = Action {  implicit request: Request[AnyContent] =>

    println(request.body.asJson)

    val json = request.body.asJson

    println(json.get.toString)

    val acl = JSONUtil.GSON.fromJson(json.get.toString, classOf[PkuAcl])

    acl.modifiedBy = "PKU";

    if(acl.id!=null&&acl.id.trim.length>0){
      aclService.updateById(acl);
    }else{
      aclService.save(acl);
    }

    val strRes =  JSONUtil.toJSONStr(acl.id)


    Ok(strRes).as("application/json")
  }



  def saveRenewPermissions = Action {  implicit request: Request[AnyContent] =>

    println(request.body.asJson)

    val json = request.body.asJson

    println(json.get.toString)

   var userId =  json.get("userId").toString();
    var menuIds =json.get("menuIds").toString();
    println("userId:"+userId)
    println("menuIds:"+menuIds)
    userId = JSONUtil.GSON.fromJson(userId.toString, classOf[String])
    val menuList = JSONUtil.GSON.fromJson(menuIds, classOf[Array[String]])
//    val aclVo = JSONUtil.GSON.fromJson(json.get.toString, classOf[AclVo])
    aclService.delByPrincipalId(userId.replace("\"",""));
    for(menu <- menuList){
      var pkuAcl =  new PkuAcl(UUID.randomUUID().toString,new Date(),"PKU",false,"",
        false,"PKU",new Date(),0,0,0,menu.toString,userId,"2");
      aclService.save(pkuAcl);
    }

    val strRes =  JSONUtil.toJSONStr(userId)

    Ok(strRes).as("application/json")
  }

  def getByPrincipalId = Action {implicit request: Request[AnyContent] =>
    println(request.body.asJson)

    val json = request.body.asJson

    println(json.get.toString)

    var userId =  json.get("userId").toString();
    userId = JSONUtil.GSON.fromJson(userId.toString, classOf[String])
    val aclList = aclService.getByPrincipalId(userId)
    var menuList = List[PkuMenu]();
    for(acl <- aclList){
      menuList = menuList ::: List[PkuMenu]((menuService.getById(acl.menuId)))
    }
    val strRes = JSONUtil.seqToJSONString(menuList)
    Ok(strRes).as("application/json")
  }

}
