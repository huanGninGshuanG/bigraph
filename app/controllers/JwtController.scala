package controllers

import authentikat.jwt.JsonWebToken._
import authentikat.jwt.{JsonWebToken, JwtClaimsSet, JwtHeader}
import javax.inject._
import play.api.mvc._
import services.UserService
import utils.JSONUtil

import scala.concurrent.ExecutionContext

/**
  * 用户管理
  */
@Singleton
class JwtController @Inject()(userService:UserService,cc: ControllerComponents)(implicit ec: ExecutionContext) extends AbstractController(cc) {

  def getToken =  Action {implicit request: Request[AnyContent] =>

    val username = request.body.asFormUrlEncoded.get("username").head

    val password =  request.body.asFormUrlEncoded.get("password").head

    println("username:"+username)
    println("password:"+password)
    var pkuUser = userService.getByPass(username,password);
    var token = ""
    if (pkuUser != null) {
      val header = JwtHeader("HS256")
      val claimsSet = JwtClaimsSet(Map("id" -> pkuUser.id)) //用户id
      token = JsonWebToken(header, claimsSet, password)//用户密码
    }
    val strRes = JSONUtil.toJSONStr(token)
    Ok(strRes).as("application/json")
  }

  def validToken =  Action {implicit request: Request[AnyContent] =>
    val token = request.body.asFormUrlEncoded.get("token").head
    println("token:"+token)
    var isValid:Boolean =  false
    if(token.nonEmpty){
      val claims: Option[Map[String, String]] = token match {
        case JsonWebToken(header, claimsSet, signature) =>
          claimsSet.asSimpleMap.toOption
        case x =>
          None
      }
      var userId = claims.getOrElse(Map.empty[String, String]).get("id")
      var pkuUser = userService.getById(userId.get)
      if (pkuUser != null) {
        isValid = validate(token, pkuUser.loginpwd)
      }
    }
    val strRes = JSONUtil.toJSONStr(Boolean.box(isValid))
    Ok(strRes).as("application/json")
  }

}
