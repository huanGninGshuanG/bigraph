package repository

import java.util.Date

import anorm.SqlParser.{get, scalar}
import anorm.{~, _}
import domain.{PkuLogItem}
import javax.inject.Inject
import play.api.db._
import java.util.UUID
import scala.concurrent.Future


class LogItemDao @Inject()(db: Database)(implicit ec: DatabaseExecutionContext){

  private val logTrans = {
    get[String]("id") ~
      get[Date]("createDate") ~
      get[String]("creator") ~
      get[Boolean]("del") ~
      get[String]("description") ~
      get[Boolean]("invalid") ~
      get[String]("modifiedBy") ~
      get[Date]("modifyDate") ~
      get[Int]("orderNo") ~
      get[String]("batchId") ~
      get[String]("businessDesc") ~
      get[Date]("startTime") ~
      get[Date]("endTime") ~
      get[String]("executeDetailLog") ~
      get[String]("executeResult") ~
      get[Int]("flowSize") ~
      get[String]("inputParam") ~
      get[String]("outputParam") ~
      get[String]("outputGraph") ~
      get[String]("detectionResult") ~
      get[String]("formula")map {
      case id ~ createDate ~ creator ~ del ~ description ~ invalid ~ modifiedBy ~ modifyDate ~ orderNo ~ batchId ~ businessDesc ~ startTime ~ endTime ~ executeDetailLog ~ executeResult ~ flowSize ~ inputParam ~  outputParam ~ outputGraph ~ detectionResult ~formula=>
        PkuLogItem(id, createDate, creator, del, description, invalid, modifiedBy, modifyDate, orderNo, batchId, businessDesc, startTime, endTime,executeDetailLog,executeResult,flowSize,inputParam,outputParam,outputGraph,detectionResult,formula)
    }
  }



  def getList(page: Int = 0, pageSize: Int = 10, filter: String = "%"): Future[Page[(PkuLogItem)]] = Future{
    val offset = pageSize * page

    db.withConnection { implicit c =>
      val pkuResource =  SQL"SELECT * from pku_simulate_itemlogs where batchId =  ${filter}  limit  ${pageSize} offset ${offset} ".as(logTrans.*)

      val totalRows = SQL"""
        select count(*) from pku_simulate_itemlogs
      """.as(scalar[Long].single)

      Page(pkuResource, page, offset, totalRows)
    }

  }(ec)

  def getById(id:String): Option[PkuLogItem] = {
    db.withConnection { implicit c =>
      SQL"SELECT * from pku_simulate_itemlogs where id = $id".as(logTrans.singleOpt)
    }
  }


  def delById(id: String) =  {
    db.withConnection { implicit connection =>
      SQL"delete from pku_simulate_itemlogs where id = ${id}".executeUpdate()
    }
  }


  def save(log: PkuLogItem) =  {
    log.id = UUID.randomUUID().toString.replace("-","");
    db.withConnection { implicit connection =>
      SQL("""
            INSERT INTO `pku_simulate_itemlogs`
              (`id`, `createDate`, `creator`, `del`, `description`, `invalid`, `modifiedBy`, `modifyDate`, `orderNo`, `batchId`, `businessDesc`, `startTime`, `endTime`, `executeDetailLog`, `executeResult`, `flowSize`, `inputParam`,`outputParam`,`outputGraph`,`detectionResult`,`formula`)
              VALUES
              ({id}, NOW(), {creator}, {del}, {description}, {invalid}, {modifiedBy}, NOW(), {orderNo}, {batchId}, {businessDesc}, {startTime}, {endTime}, {executeDetailLog}, {executeResult}, {flowSize}, {inputParam}, {outputParam},{outputGraph},{detectionResult},{formula});
      """).bind(log).executeInsert()
    }
    log.id
  }




  def updateById(log: PkuLogItem) =  {
    db.withConnection { implicit connection =>
      SQL("""
            UPDATE `pku_simulate_itemlogs`
            SET
            `createDate` = NOW(),
            `creator` = {creator},
            `del` = {del},
            `description` = {description},
            `invalid` = {invalid},
            `modifiedBy` = {modifiedBy},
            `modifyDate` = NOW(),
            `orderNo` = {orderNo},
            `batchId` = {batchId},
            `businessDesc` = {businessDesc},
            `startTime` = {startTime},
            `endTime` = {endTime},
            `executeDetailLog` = {executeDetailLog},
            `executeResult` = {executeResult},
            `flowSize` = {flowSize},
            `inputParam` = {inputParam},
            `outputParam` = {outputParam},
            `outputGraph` = {outputGraph},
            `detectionResult` = {detectionResult},
            `formula`={formula},
            WHERE `id` = {id};
      """).bind(log.copy(id = log.id)).executeUpdate()
    }
  }

}
