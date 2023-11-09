package repository

import java.util.Date

import anorm.SqlParser.{get, scalar}
import anorm._
import domain.PkuAcl
import javax.inject.Inject
import play.api.db.DBApi

import scala.concurrent.Future


@javax.inject.Singleton
class AclDao @Inject()(dbapi: DBApi)(implicit ec: DatabaseExecutionContext) {

  private val db = dbapi.database("default")

  private val aclTrans = {
    get[String]("id") ~
      get[Date]("createDate") ~
      get[String]("creator") ~
      get[Boolean]("del") ~
      get[String]("description") ~
      get[Boolean]("invalid") ~
      get[String]("modifiedBy") ~
      get[Date]("modifyDate") ~
      get[Int]("orderNo") ~
      get[Int]("aclState") ~
      get[Int]("aclTriState") ~
      get[String]("menuId") ~
      get[String]("principalId") ~
      get[String]("principalType") map {
      case id ~ createDate ~ creator ~ del ~ description ~ invalid ~ modifiedBy ~ modifyDate ~ orderNo ~ aclState   ~ aclTriState  ~ menuId  ~ principalId  ~ principalType =>
        domain.PkuAcl(id, createDate, creator, del, description, invalid, modifiedBy, modifyDate, orderNo, aclState, aclTriState,menuId,principalId,principalType)
    }
  }


  def getById(id: String) :  domain.PkuAcl = {
    db.withConnection{ implicit connection =>
      SQL"""
            SELECT id, createDate, creator, del, description, invalid, modifiedBy, modifyDate,
            orderNo,  aclState, aclTriState,menuId,principalId,principalType FROM pku_acl where id = $id
        """.as(aclTrans.single)
    }
  }

  def getList(page: Int = 0, pageSize: Int = 10, filter: String = "%"): Future[Page[( domain.PkuAcl)]] = Future {

    val offset = pageSize * page

    db.withConnection { implicit connection =>

      val pkuAcls = SQL"""
        SELECT id, createDate, creator, del, description, invalid, modifiedBy, modifyDate,
        orderNo,  aclState, aclTriState,menuId,principalId,principalType
        FROM pku_acl where `name` like ${filter} order by orderNo
        limit ${pageSize} offset ${offset}
      """.as(aclTrans.*)

      val totalRows = SQL"""
        select count(*) from pku_acl where `name` like ${filter}
      """.as(scalar[Long].single)

      Page(pkuAcls, page, offset, totalRows)
    }
  }(ec)


  def delById(id: String) =  {
    db.withConnection { implicit connection =>
      SQL"delete from pku_acl where id = ${id}".executeUpdate()
    }
  }


  def save(user:  domain.PkuAcl) =  {
    db.withConnection { implicit connection =>
      SQL("""
            INSERT INTO `pku_acl`
              (`id`, `createDate`, `creator`, `del`, `description`, `invalid`, `modifiedBy`, `modifyDate`, `orderNo`, aclState, aclTriState,menuId,principalId,principalType)
              VALUES
              (REPLACE(UUID(), "-", ""), NOW(), {creator}, {del}, {description}, {invalid}, {modifiedBy}, NOW(), {orderNo}, {aclState}, {aclTriState},{menuId},{principalId},{principalType});
      """).bind(user).executeInsert()
    }
  }




  def updateById(user:  domain.PkuAcl) =  {
    db.withConnection { implicit connection =>
      SQL("""
            UPDATE `pku_acl`
            SET
            `createDate` = NOW(),
            `creator` = {creator},
            `del` = {del},
            `description` = {description},
            `invalid` = {invalid},
            `modifiedBy` = {modifiedBy},
            `modifyDate` = NOW(),
            `orderNo` = {orderNo},

            `aclState` = {aclState},
            `aclTriState` = {aclTriState},
            `menuId` = {menuId},
            `principalId` = {principalId},
            `principalType` = {principalType}
            WHERE `id` = {id};
      """).bind(user.copy(id = user.id)).executeUpdate()
    }
  }

  def delByPrincipalId(id: String) =  {
    db.withConnection { implicit connection =>
      SQL"delete from pku_acl where principalId = ${id}".executeUpdate()
    }
  }

  def getByPrincipalId(id: String) :  List[PkuAcl] = {
    db.withConnection{ implicit connection =>
      SQL"""
            SELECT id, createDate, creator, del, description, invalid, modifiedBy, modifyDate,
            orderNo,  aclState, aclTriState,menuId,principalId,principalType FROM pku_acl where principalId = $id
        """.as(aclTrans.*)
    }
  }

}
