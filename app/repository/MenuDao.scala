package repository

import java.util.Date

import anorm.SqlParser.{get, scalar}
import anorm._
import javax.inject.Inject
import play.api.db.DBApi

import scala.concurrent.Future


@javax.inject.Singleton
class MenuDao @Inject()(dbapi: DBApi)(implicit ec: DatabaseExecutionContext) {

  private val db = dbapi.database("default")

  /**
    * SELECT id, createDate, creator, del,
    * description, invalid, modifiedBy,
    * modifyDate, orderNo, `code`, fileContent,
    * fileType, `name` FROM pku_resource
    */
  private val menuTrans = {
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
      get[String]("icon") ~
      get[Boolean]("leaf") ~
      get[String]("menuType") ~
      get[String]("name") ~
      get[String]("target") ~
      get[String]("parentId") map {
      case id ~ createDate ~ creator ~ del ~ description ~ invalid ~ modifiedBy ~ modifyDate ~ orderNo ~ code ~ icon ~ leaf ~ menuType~ name~ target~ parentId =>
        domain.PkuMenu(id, createDate, creator, del, description, invalid, modifiedBy, modifyDate, orderNo, code, icon, leaf, menuType, name, target, parentId)
    }
  }

  def tree(): Seq[( domain.PkuMenu)] =  {

    db.withConnection { implicit connection =>

      val pkuMenus = SQL"""
                          SELECT
                          pku_menus.id,
                          pku_menus.createDate,
                          pku_menus.creator,
                          pku_menus.del,
                          pku_menus.description,
                          pku_menus.invalid,
                          pku_menus.modifiedBy,
                          pku_menus.modifyDate,
                          pku_menus.orderNo,
                          pku_menus.`code`,
                          pku_menus.icon,
                          pku_menus.leaf,
                          pku_menus.menuType,
                          pku_menus.`name`,
                          pku_menus.target,
                          pku_menus.parentId
                          FROM
                          pku_menus
                          order by orderNo
      """.as(menuTrans.*)
      pkuMenus
    }
  }

  def getList(page: Int = 0, pageSize: Int = 10, filter: String = "%",parentId:String="0" ): Future[Page[( domain.PkuMenu)]] = Future {

    val offset = pageSize * page

    db.withConnection { implicit connection =>

      val pkuMenu = SQL"""
                          SELECT
                          pku_menus.id,
                          pku_menus.createDate,
                          pku_menus.creator,
                          pku_menus.del,
                          pku_menus.description,
                          pku_menus.invalid,
                          pku_menus.modifiedBy,
                          pku_menus.modifyDate,
                          pku_menus.orderNo,
                          pku_menus.`code`,
                          pku_menus.icon,
                          pku_menus.leaf,
                          pku_menus.menuType,
                          pku_menus.`name`,
                          pku_menus.target,
                          pku_menus.parentId
                          FROM
                          pku_menus
                          where `name` like ${filter} and parentId = ${parentId} order by orderNo
                                 limit ${pageSize} offset ${offset}
      """.as(menuTrans.*)

      val totalRows = SQL"""
        select count(*) from pku_menus where `name` like ${filter} and parentId = ${parentId}
      """.as(scalar[Long].single)

      Page(pkuMenu, page, offset, totalRows)
    }
  }(ec)


  def getById(id: String) :  domain.PkuMenu = {
    db.withConnection{ implicit connection =>
      SQL"""
            SELECT id, createDate, creator, del, description, invalid, modifiedBy, modifyDate,
            orderNo, code, icon,leaf, menuType,name,target,parentId FROM pku_menus where id = $id
        """.as(menuTrans.single)
    }
  }

  def delById(id: String) =  {
    db.withConnection { implicit connection =>
      SQL"delete from pku_menus where id = ${id}".executeUpdate()
    }
  }

  def save(pkuMenu:  domain.PkuMenu) =  {
    db.withConnection { implicit connection =>
      SQL("""
            INSERT INTO `pku_menus`
              (`id`, `createDate`, `creator`, `del`, `description`, `invalid`, `modifiedBy`, `modifyDate`, `orderNo`, `code`, `icon`, `leaf`, `menuType`, `name`, `target`, `parentId`)
              VALUES
              (REPLACE(UUID(), "-", ""), NOW(), {creator}, {del}, {description}, {invalid}, {modifiedBy}, NOW(), {orderNo}, {code}, {icon},{leaf},{menuType},{name},{target}, {parentId});
      """).bind(pkuMenu).executeInsert()
    }
  }


  def updateById(pkuMenu:  domain.PkuMenu) =  {
    db.withConnection { implicit connection =>
      SQL("""
            UPDATE `pku_menus`
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
            `icon` = {icon},
            `leaf` = {leaf},
            `menuType` = {menuType},
            `name` = {name},
            `target` = {target},
            `parentId` = {parentId}
            WHERE `id` = {id};
      """).bind(pkuMenu.copy(id = pkuMenu.id)).executeUpdate()
    }
  }


}
