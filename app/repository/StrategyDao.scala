package repository

import java.util.Date

import anorm.SqlParser.{get, scalar}
import anorm._
import javax.inject.Inject
import play.api.db.DBApi

import scala.concurrent.Future


@javax.inject.Singleton
class StrategyDao @Inject()(dbapi: DBApi)(implicit ec: DatabaseExecutionContext) {

  private val db = dbapi.database("default")

  private val strategyTrans = {
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
      get[String]("strategyType") ~
      get[String]("name") map {
      case id ~ createDate ~ creator ~ del ~ description ~ invalid ~ modifiedBy ~ modifyDate ~ orderNo ~ code  ~ strategyType ~ name =>
        domain.PkuStrategy(id, createDate, creator, del, description, invalid, modifiedBy, modifyDate, orderNo, code, strategyType, name)
    }
  }


  def getById(id: String) :  domain.PkuStrategy = {
    db.withConnection{ implicit connection =>
      SQL"""
            SELECT id, createDate, creator, del, description, invalid, modifiedBy, modifyDate,
            orderNo, code, strategyType, name FROM pku_strategy where id = $id
        """.as(strategyTrans.single)
    }
  }

  def getList(page: Int = 0, pageSize: Int = 10, filter: String = "%"): Future[Page[( domain.PkuStrategy)]] = Future {

    val offset = pageSize * page

    db.withConnection { implicit connection =>

      val PkuStrategy = SQL"""
        SELECT id, createDate, creator, del, description, invalid, modifiedBy, modifyDate,
        orderNo, `code`, strategyType, `name`
        FROM pku_strategy where `name` like ${filter} order by orderNo
        limit ${pageSize} offset ${offset}
      """.as(strategyTrans.*)

      val totalRows = SQL"""
        select count(*) from pku_strategy where `name` like ${filter}
      """.as(scalar[Long].single)

      Page(PkuStrategy, page, offset, totalRows)
    }
  }(ec)


  def delById(id: String) =  {
    db.withConnection { implicit connection =>
      SQL"delete from pku_strategy where id = ${id}".executeUpdate()
    }
  }


  def save(strategy:  domain.PkuStrategy) =  {
    db.withConnection { implicit connection =>
      SQL("""
            INSERT INTO `pku_strategy`
              (`id`, `createDate`, `creator`, `del`, `description`, `invalid`, `modifiedBy`, `modifyDate`, `orderNo`, `code`, `strategyType`, `name`)
              VALUES
              (REPLACE(UUID(), "-", ""), NOW(), {creator}, {del}, {description}, {invalid}, {modifiedBy}, NOW(), {orderNo}, {code}, {strategyType}, {name});
      """).bind(strategy).executeInsert()
    }
  }




  def updateById(strategy:  domain.PkuStrategy) =  {
    db.withConnection { implicit connection =>
      SQL("""
            UPDATE `pku_strategy`
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
            `strategyType` = {strategyType},
            `name` = {name}
            WHERE `id` = {id};
      """).bind(strategy.copy(id = strategy.id)).executeUpdate()
    }
  }

}
