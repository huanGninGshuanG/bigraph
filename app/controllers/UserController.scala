package controllers

import com.google.gson.JsonParser
import domain.PkuUser
import javax.inject._
import play.api.mvc._
import services.UserService
import utils.JSONUtil

import scala.concurrent.ExecutionContext

/**
  * 用户管理
  */
@Singleton
class UserController @Inject()(userService:UserService, cc: ControllerComponents)(implicit ec: ExecutionContext) extends AbstractController(cc) {


  def user() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.aipUser.user_main())
  }

  def getById(id:String) =  Action {

    val strategy = userService.getById(id)

    val strRes = JSONUtil.toJSONString(strategy)

    Ok(strRes).as("application/json")
  }

  def list = Action.async { implicit request =>

    println(request.body.asJson)

    println(request.body.asFormUrlEncoded.get("start").head.toInt)

    println(request.body.asFormUrlEncoded.get("name").head)

    userService.getList(request.body.asFormUrlEncoded.get("start").head.toInt/10, 10, request.body.asFormUrlEncoded.get("name").head).map { page =>

      val str = JSONUtil.listToJSONString(page)

      println(str)

      Ok(str).as("application/json")
    }
  }

  def listAll = Action.async { implicit request =>

    println(request.body.asJson)

    userService.getList(0, 10000, "").map { page =>

      val str = JSONUtil.listToJSONString(page)

      println(str)

      Ok(str).as("application/json")
    }
  }

  def delById(id:String) = Action { implicit request: Request[AnyContent] =>

    var isSuccess = userService.delById(id);

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

    jsonArray.forEach(a=>userService.delById(a.getAsString))

    val strRes =  JSONUtil.toJSONStr(true.toString)

    Ok(strRes).as("application/json")
  }


  def savaOrUpdate = Action {  implicit request: Request[AnyContent] =>

    println(request.body.asJson)

    val json = request.body.asJson

    println(json.get.toString)

    val user = JSONUtil.GSON.fromJson(json.get.toString, classOf[PkuUser])

    user.modifiedBy = "PKU";

    if(user.id!=null&&user.id.trim.length>0){
      userService.updateById(user);
    }else{
      userService.save(user);
    }

    val strRes =  JSONUtil.toJSONStr(user.id)


    Ok(strRes).as("application/json")
  }

  def getUserByPass = Action {  implicit request: Request[AnyContent] =>

    println(request.body.asJson)

    val json = request.body.asJson

    println(json.get.toString)

    var loginname =  json.get("loginname").toString();
    loginname = JSONUtil.GSON.fromJson(loginname.toString, classOf[String])
    var password =json.get("password").toString();
    password = JSONUtil.GSON.fromJson(password.toString, classOf[String])
    println("loginname:"+loginname)
    println("password:"+password)
    var pkuUser = userService.getByPass(loginname,password);
    var strRes =  JSONUtil.toJSONStr(pkuUser)
    if(null == pkuUser||pkuUser.length==0){
      strRes =  JSONUtil.toJSONStr(pkuUser,success=false)
    }

    //保存用户信息
//    var session  = request.session.+("userId",pkuUser.id)
//    println("userId:"+ session.get("userId"))
    Ok(strRes).as("application/json")
  }

  def logOut = Action {  implicit request: Request[AnyContent] =>

    println(request.body.asJson)
    var session  = request.session
    println("userId:"+ session.get("userId"))
    session = session.-("userId")
    var strRes =  JSONUtil.toJSONStr("登出")

    Ok(strRes).as("application/json")

    Redirect("/");
  }

}
