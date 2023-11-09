package repository

import java.util.Date

import anorm.SqlParser.{get, scalar}
import anorm._
import javax.inject.Inject
import play.api.db.DBApi

import scala.concurrent.Future


@javax.inject.Singleton
class ResourceDao @Inject()(dbapi: DBApi)(implicit ec: DatabaseExecutionContext) {

  private val db = dbapi.database("default")

  /**
    * SELECT id, createDate, creator, del,
    * description, invalid, modifiedBy,
    * modifyDate, orderNo, `code`, fileContent,
    * fileType, `name` FROM pku_resource
    */
  private val resourceTrans = {
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
      get[String]("name") ~
      get[String]("extContent") map {
      case id ~ createDate ~ creator ~ del ~ description ~ invalid ~ modifiedBy ~ modifyDate ~ orderNo ~ code ~ fileContent ~ fileType ~ name ~ extContent =>
        domain.PkuResource(id, createDate, creator, del, description, invalid, modifiedBy, modifyDate, orderNo, code, fileContent, fileType,name, extContent)
    }
  }


  def getById(id: String) :  domain.PkuResource = {
    db.withConnection{ implicit connection =>
      SQL"""
            SELECT id, createDate, creator, del, description, invalid, modifiedBy, modifyDate,
            orderNo, code, fileContent, fileType, name,extContent FROM pku_resource where id = $id
        """.as(resourceTrans.single)
    }
  }

  def getList(page: Int = 0, pageSize: Int = 10, filter: String = "%"): Future[Page[( domain.PkuResource)]] = Future {

    val offset = pageSize * page

    db.withConnection { implicit connection =>

      val pkuResource = SQL"""
        SELECT id, createDate, creator, del, description, invalid, modifiedBy, modifyDate,
        orderNo, `code`, fileContent, fileType, `name`, `extContent`
        FROM pku_resource where `name` like ${filter} order by orderNo
        limit ${pageSize} offset ${offset}
      """.as(resourceTrans.*)

      val totalRows = SQL"""
        select count(*) from pku_resource where `name` like ${filter}
      """.as(scalar[Long].single)

      Page(pkuResource, page, offset, totalRows)
    }
  }(ec)


  def delById(id: String) =  {
    db.withConnection { implicit connection =>
      SQL"delete from pku_resource where id = ${id}".executeUpdate()
    }
  }


  def save(resource:  domain.PkuResource) =  {
    import java.util.UUID
    resource.id = UUID.randomUUID.toString.replace("-", "")
    db.withConnection { implicit connection =>
      SQL("""
            INSERT INTO `pku_resource`
              (`id`, `createDate`, `creator`, `del`, `description`, `invalid`, `modifiedBy`, `modifyDate`, `orderNo`, `code`, `fileContent`, `fileType`, `name`, `extContent`)
              VALUES
              ({id}, NOW(), {creator}, {del}, {description}, {invalid}, {modifiedBy}, NOW(), {orderNo}, {code}, {fileContent}, {fileType}, {name},{extContent});
      """).bind(resource).executeInsert()
    }
    resource.id
  }




  def updateById(resource:  domain.PkuResource) =  {
    db.withConnection { implicit connection =>
      SQL("""
            UPDATE `pku_resource`
            SET
            `createDate` = NOW(),
            `creator` = {creator},
            `del` = {del},
            `description` = {description},
            `invalid` = {invalid},
            `modifiedBy` = {modifiedBy},
            `modifyDate` = NOW(),
            `orderNo` = {orderNo},
            `code` = {code},
            `fileContent` = {fileContent},
            `fileType` = {fileType},
            `name` = {name},
            `extContent` = {extContent}
            WHERE `id` = {id};
      """).bind(resource.copy(id = resource.id)).executeUpdate()
    }
  }

}
