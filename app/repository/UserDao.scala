package repository

import java.util.Date

import anorm.SqlParser.{get, scalar}
import anorm._
import javax.inject.Inject
import play.api.db.DBApi

import scala.concurrent.Future


@javax.inject.Singleton
class UserDao @Inject()(dbapi: DBApi)(implicit ec: DatabaseExecutionContext) {

  private val db = dbapi.database("default")

  private val userTrans = {
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
      get[Date]("expireTime") ~
      get[String]("headPic") ~
      get[String]("linkaddress") ~
      get[String]("linkmail") ~
      get[String]("linkphone") ~
      get[String]("loginname") ~
      get[String]("loginpwd") ~
      get[String]("loginpwdShow") ~
      get[String]("name") ~
      get[String]("userType") map {
      case id ~ createDate ~ creator ~ del ~ description ~ invalid ~ modifiedBy ~ modifyDate ~ orderNo ~ code   ~ expireTime  ~ headPic  ~ linkaddress  ~ linkmail  ~ linkphone  ~ loginname  ~ loginpwd  ~ loginpwdShow  ~ name ~ userType =>
        domain.PkuUser(id, createDate, creator, del, description, invalid, modifiedBy, modifyDate, orderNo, code, expireTime,headPic,linkaddress,linkmail,linkphone,loginname,loginpwd,loginpwdShow,name,userType)
    }
  }


  def getById(id: String) :  domain.PkuUser = {
    db.withConnection{ implicit connection =>
      SQL"""
            SELECT id, createDate, creator, del, description, invalid, modifiedBy, modifyDate,
            orderNo,  code, expireTime,headPic,linkaddress,linkmail,linkphone,loginname,loginpwd,loginpwdShow,name,userType FROM pku_users where id = $id
        """.as(userTrans.single)
    }
  }

  def getList(page: Int = 0, pageSize: Int = 10, filter: String = "%"): Future[Page[( domain.PkuUser)]] = Future {

    val offset = pageSize * page

    db.withConnection { implicit connection =>

      val pkuUsers = SQL"""
        SELECT id, createDate, creator, del, description, invalid, modifiedBy, modifyDate,
        orderNo,  code, expireTime,headPic,linkaddress,linkmail,linkphone,loginname,loginpwd,loginpwdShow,name,userType
        FROM pku_users where `name` like ${filter} order by orderNo
        limit ${pageSize} offset ${offset}
      """.as(userTrans.*)

      val totalRows = SQL"""
        select count(*) from pku_users where `name` like ${filter}
      """.as(scalar[Long].single)

      Page(pkuUsers, page, offset, totalRows)
    }
  }(ec)


  def delById(id: String) =  {
    db.withConnection { implicit connection =>
      SQL"delete from pku_users where id = ${id}".executeUpdate()
    }
  }


  def save(user:  domain.PkuUser) =  {
    db.withConnection { implicit connection =>
      SQL("""
            INSERT INTO `pku_users`
              (`id`, `createDate`, `creator`, `del`, `description`, `invalid`, `modifiedBy`, `modifyDate`, `orderNo`, code, expireTime,headPic,linkaddress,linkmail,linkphone,loginname,loginpwd,loginpwdShow,name,userType)
              VALUES
              (REPLACE(UUID(), "-", ""), NOW(), {creator}, {del}, {description}, {invalid}, {modifiedBy}, NOW(), {orderNo}, {code}, {expireTime},{headPic},{linkaddress},{linkmail},{linkphone},{loginname},{loginpwd},{loginpwdShow},{name},{userType});
      """).bind(user).executeInsert()
    }
  }




  def updateById(user:  domain.PkuUser) =  {
    db.withConnection { implicit connection =>
      SQL("""
            UPDATE `pku_users`
            SET
            `createDate` = NOW(),
            `creator` = {creator},
            `del` = {del},
            `description` = {description},
            `invalid` = {invalid},
            `modifiedBy` = {modifiedBy},
            `modifyDate` = NOW(),
            `orderNo` = {orderNo},
            `expireTime` = {expireTime},
            `headPic` = {headPic},
            `linkaddress` = {linkaddress},
            `linkmail` = {linkmail},
            `linkphone` = {linkphone},
             `loginname` = {loginname},
            `loginpwd` = {loginpwd},
            `loginpwdShow` = {loginpwdShow},
             `name` = {name},
            `userType` = {userType}
            WHERE `id` = {id};
      """).bind(user.copy(id = user.id)).executeUpdate()
    }
  }

  def getByPass(loginname: String,loginpwd:String) :  domain.PkuUser = {
    db.withConnection{ implicit connection =>
      SQL"""
            SELECT id, createDate, creator, del, description, invalid, modifiedBy, modifyDate,
            orderNo,  code, expireTime,headPic,linkaddress,linkmail,linkphone,loginname,loginpwd,loginpwdShow,name,userType FROM pku_users where loginname = $loginname and loginpwd = $loginpwd
        """.as(userTrans.single)
    }
  }

}
