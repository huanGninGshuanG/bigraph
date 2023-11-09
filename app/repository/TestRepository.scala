package repository

import java.util.Date

import anorm.SqlParser.{get, scalar}
import anorm._
import javax.inject.Inject
import play.api.db.DBApi

import scala.concurrent.Future

case class PkuResource(id: String,
                       createDate: Date,
                       creator: String,
                       del: Boolean,
                       description: String,
                       invalid: Boolean,
                       modifiedBy: String,
                       modifyDate: Date,
                       orderNo: Int,
                       code: String,
                       fileContent: String,
                       fileType: String,
                       name: String)

object PkuResource {
  implicit def toParameters: ToParameterList[PkuResource] =
    Macro.toParameters[PkuResource]
}

case class Page[A](items: Seq[A], page: Int, offset: Long, total: Long) {
  lazy val prev = Option(page - 1).filter(_ >= 0)
  lazy val next = Option(page + 1).filter(_ => (offset + items.size) < total)
}

@javax.inject.Singleton
class TestRepository @Inject()(dbapi: DBApi)(implicit ec: DatabaseExecutionContext) {

  private val db = dbapi.database("default")

  /**
    * SELECT id, createDate, creator, del,
    * description, invalid, modifiedBy,
    * modifyDate, orderNo, `code`, fileContent,
    * fileType, `name` FROM pku_resource
    */
  private val simple = {
    get[String]("id") ~
      get[Date]("createDate") ~
      get[String]("creator") ~
      get[Boolean]("del") ~
      get[String]("description") ~
      get[Boolean]("invalid") ~
      get[String]("modifiedBy") ~
      get[Date]("modifyDate") ~
      get[Int]("orderNo") ~
      get[String]("code") ~
      get[String]("fileContent") ~
      get[String]("fileType") ~
      get[String]("name") map {
      case id ~ createDate ~ creator ~ del ~ description ~ invalid ~ modifiedBy ~ modifyDate ~ orderNo ~ code ~ fileContent ~ fileType ~ name =>
        PkuResource(id, createDate, creator, del, description, invalid, modifiedBy, modifyDate, orderNo, code, fileContent, fileType, name)
    }
  }

  def findById(id: String): Future[Option[PkuResource]] = Future {
    db.withConnection { implicit connection =>
      SQL"SELECT id, createDate, creator, del, description, invalid, modifiedBy, modifyDate, orderNo, code, fileContent, fileType, name FROM pku_resource where id = $id".as(simple.singleOpt)
    }
  }(ec)

  def findResource(id: String) : PkuResource = {
    db.withConnection{ implicit connection =>
      SQL"""
            SELECT id, createDate, creator, del, description, invalid, modifiedBy, modifyDate,
            orderNo, code, fileContent, fileType, name FROM pku_resource where id = $id
        """.as(simple.single)
    }
  }

  def list(page: Int = 0, pageSize: Int = 10, orderBy: Int = 1, filter: String = "%"): Future[Page[(PkuResource)]] = Future {

    val offset = pageSize * page

    db.withConnection { implicit connection =>

      val pkuResource = SQL"""
        SELECT id, createDate, creator, del, description, invalid, modifiedBy, modifyDate,
        orderNo, `code`, fileContent, fileType, `name`
        FROM pku_resource
        limit ${pageSize} offset ${offset}
      """.as(simple.*)

      val totalRows = SQL"""
        select count(*) from pku_resource
      """.as(scalar[Long].single)

      Page(pkuResource, page, offset, totalRows)
    }
  }(ec)
}
