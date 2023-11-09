package controllers

import java.util.concurrent.TimeUnit

import com.google.common.cache.{CacheBuilder, CacheLoader, RemovalListener}
import domain.PkuLogItem
import javax.inject.{Inject, _}
import play.api.mvc._
import services.LogItemService
import utils.JSONUtil

import scala.concurrent.ExecutionContext

/**
 * 日志处理
 */
@Singleton
class LogItemController @Inject()(logItemService: LogItemService, cc: ControllerComponents)(implicit ec: ExecutionContext)extends AbstractController(cc) {
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

    val batchId = request.body.asFormUrlEncoded.get("batchId").head;

    logItemService.getList(0, 100,batchId).map { page =>

      val str = JSONUtil.listToJSONString(page)

      println(str)

      Ok(str).as("application/json")
    }
  }

  val cacheLoader: CacheLoader[String, String] =
    new CacheLoader[String, String](){
      def load(id: String): String = {
        var log = logItemService.getById(id)
        if(null == log){
          return ""
        }
        JSONUtil.toJSONStr(log.get)
      }
    }
  lazy val cache = CacheBuilder.newBuilder()
    //设置大小，条目数
    .maximumSize(50)
    //设置时效时间，最后一次被访问
    .expireAfterAccess(30, TimeUnit.MINUTES)
    //缓存构建的回调
    .build[String, String](cacheLoader)

  def getById(id:String) = Action { implicit request: Request[AnyContent] =>

    //var log = logItemService.getById(id);
    //val strRes =  JSONUtil.toJSONStr(log.get)
    // lixin, 添加 guava 本地缓存， 测试性能
    val strRes = cache(id)
    Ok(strRes).as("application/json")
  }

  def delById(id:String) = Action { implicit request: Request[AnyContent] =>

    var isSuccess = logItemService.delById(id);

    val strRes =  JSONUtil.toJSONStr(isSuccess.toString)

    Ok(strRes).as("application/json")
  }


  def savaOrUpdate = Action {  implicit request: Request[AnyContent] =>

    println(request.body.asJson)

    val json = request.body.asJson

    println(json.get.toString)

    val log = JSONUtil.GSON.fromJson(json.get.toString, classOf[PkuLogItem])

    if(log.id!=null){
      logItemService.updateById(log);
    }else{
      logItemService.save(log);
    }

    val strRes =  JSONUtil.toJSONStr(log.id)


    Ok(strRes).as("application/json")
  }


}
