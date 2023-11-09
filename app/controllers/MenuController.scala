package controllers

import java.util

import com.google.gson.JsonParser
import domain.PkuMenu
import domain.vo.MenuVo
import javax.inject._
import play.api.mvc._
import services.{AclService, MenuService}
import utils.JSONUtil
import utils.JSONUtil.GSON

import scala.concurrent.ExecutionContext

/**
  * 菜单管理
  */
@Singleton
class MenuController @Inject()(aclService:AclService,menuService:MenuService, cc: ControllerComponents)(implicit ec: ExecutionContext) extends AbstractController(cc) {


  def menu() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.aipMenu.menu_main())
  }


  def tree() =  Action {

    val menLlist = menuService.tree();
    val tree: util.List[util.Map[String, AnyRef]] = new util.ArrayList[util.Map[String, AnyRef]];
    for (d <- menLlist) {
      val m: util.Map[String, AnyRef] = new util.HashMap[String, AnyRef]
      m.put("id", d.id)
      m.put("pId", if (d.parentId == null) "0"
      else d.parentId)
      m.put("name", d.name)
      tree.add(m)
    }
    val strRes = GSON.toJson(tree);
    Ok(strRes).as("application/json")
  }
  def list = Action.async { implicit request =>

    println(request.body.asJson)

    println(request.body.asFormUrlEncoded.get("start").head.toInt)

    println(request.body.asFormUrlEncoded.get("name").head)

    menuService.getList(request.body.asFormUrlEncoded.get("start").head.toInt/10, 10, request.body.asFormUrlEncoded.get("name").head,request.body.asFormUrlEncoded.get("parent.id").head).map { page =>

      val str = JSONUtil.listToJSONString(page)

      println(str)

      Ok(str).as("application/json")
    }
  }


  def getById(id:String) =  Action {

    val strategy = menuService.getById(id)

    val strRes = JSONUtil.toJSONString(strategy)

    Ok(strRes).as("application/json")
  }


  def delById(id:String) = Action { implicit request: Request[AnyContent] =>

    var isSuccess = menuService.delById(id);

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

    jsonArray.forEach(a=>menuService.delById(a.getAsString))

    val strRes =  JSONUtil.toJSONStr(true.toString)

    Ok(strRes).as("application/json")
  }


  def savaOrUpdate = Action {  implicit request: Request[AnyContent] =>

    println(request.body.asJson)

    val json = request.body.asJson

    println(json.get.toString)

    val pkuMenu = JSONUtil.GSON.fromJson(json.get.toString, classOf[PkuMenu])

    pkuMenu.description =  java.net.URLDecoder.decode(pkuMenu.description,"utf-8")
    pkuMenu.name = java.net.URLDecoder.decode(pkuMenu.name,"utf-8")
    pkuMenu.menuType = "LEFT";
    pkuMenu.modifiedBy = "PKU"
    if(pkuMenu.id!=null&&pkuMenu.id.trim.length>0){
      menuService.updateById(pkuMenu);
    }else{
      menuService.save(pkuMenu);
    }

    val strRes =  JSONUtil.toJSONStr(pkuMenu.id)


    Ok(strRes).as("application/json")
  }

  def indexMenu() =  Action { implicit request: Request[AnyContent] =>
    val json = request.body.asJson
    var userId = json.get("userId").toString
    userId = JSONUtil.GSON.fromJson(userId.toString, classOf[String])
    var menLlist = aclService.getMenuListByPrincipalId(userId)
    var menuVo = new MenuVo()
    //    val menLlist = menuService.tree();
    for (d <- menLlist) {
      if(d.parentId.isEmpty){
        menuVo.setId(d.id)
        menuVo.setMenuClass(d.icon)
        menuVo.setMenuName(d.name)
        menuVo.setMenuUrl(d.target)
        menuVo.setMenuId(d.code)
      }
    }
    var list = new util.ArrayList[MenuVo]
    for (d <- menLlist) {
      if(!d.parentId.isEmpty){
        import domain.vo.MenuVo
        val menuVoChild = new MenuVo
        menuVoChild.setId(d.id)
        menuVoChild.setMenuClass(d.icon)
        menuVoChild.setMenuName(d.name)
        menuVoChild.setMenuUrl(d.target)
        menuVoChild.setMenuId(d.code)
        list.add(menuVoChild)
        menuVo.setChild(list)
      }
    }
    val strRes =  JSONUtil.toJSONStr(menuVo)
    Ok(strRes).as("application/json")
  }

}
