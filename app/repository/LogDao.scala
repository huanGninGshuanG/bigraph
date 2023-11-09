package repository

import java.text.SimpleDateFormat
import java.util.{Calendar, Date}

import anorm.SqlParser.{get, scalar}
import anorm.{~, _}
import domain.{PkuLog, PkuLogQuery}
import javax.inject.Inject
import play.api.db._

import scala.concurrent.Future



class LogDao @Inject()(db: Database)(implicit ec: DatabaseExecutionContext){

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
      get[String]("strategyId") ~
      get[String]("strategyName") ~
      get[String]("resourceId") ~
      get[String]("resourceName") ~
      get[Int]("counts") ~
      get[String]("finalResult") ~
      get[String]("detectionResult") ~
      get[String]("formula")map {
      case id ~ createDate ~ creator ~ del ~ description ~ invalid ~ modifiedBy ~ modifyDate ~ orderNo ~ batchId ~ businessDesc ~ strategyId ~ strategyName ~ resourceId ~ resourceName ~ counts ~  finalResult ~ detectionResult ~formula=>
        PkuLog(id, createDate, creator, del, description, invalid, modifiedBy, modifyDate, orderNo, batchId, businessDesc, strategyId, strategyName,resourceId,resourceName,counts,finalResult,detectionResult,formula)
    }
  }



  def getList(page: Int = 0, pageSize: Int = 10, filter: PkuLogQuery ): Future[Page[(PkuLog)]] = Future{
    val offset = pageSize * page

    db.withConnection { implicit c =>
//      val pkuResource =  SQL"SELECT * from pku_simulate_logs limit ${pageSize} offset ${offset} ".as(logTrans.*)
      var  weherSql = "";
      if(!filter.resourceName.isEmpty){
        weherSql += s" and resourceName like '%${filter.resourceName}%'"
      }
      if(!filter.strategyName.isEmpty){
        weherSql += s" and strategyName like '%${filter.strategyName}%'"
       }
      if(!filter.startDate.isEmpty){
        weherSql += s" and createDate >= '${filter.startDate}'"
      }
      if(!filter.endDate.isEmpty){
        weherSql += s" and createDate <= '${filter.endDate}'"
      }

      if(!filter.finalResult.isEmpty&&filter.finalResult!="全部"){
        weherSql += s"and finalResult = '${filter.finalResult}'"
      }
      var limitSql = s" order By createDate desc limit ${pageSize} offset ${offset}"

      val pkuLog = SQL("SELECT * from pku_simulate_logs where 1=1 "  + weherSql + limitSql ).as(logTrans.*);

      val totalRows = SQL("select count(1) from pku_simulate_logs where 1=1 "  + weherSql ).as(scalar[Long].single);


      /*      val pkuLog =  SQL"""
              SELECT * from pku_simulate_logs
              where 1=1 and resourceName like ${filter.resourceName} and strategyId = ${filter.strategyId}
              limit ${pageSize} offset ${offset}
              """.as(logTrans.*)

      val totalRows = SQL"""
        select count(*) from pku_simulate_logs
      """.as(scalar[Long].single)
       */
      Page(pkuLog, page, offset, totalRows)
    }

  }(ec)

  def getById(id:String): Option[PkuLog] = {
    db.withConnection { implicit c =>
      SQL"SELECT * from pku_simulate_logs where id = $id".as(logTrans.singleOpt)
    }
  }

  def getByBatchId(batchId:String): Option[PkuLog] = {
    db.withConnection { implicit c =>
      SQL"SELECT * from pku_simulate_logs where batchId = $batchId".as(logTrans.singleOpt)
    }
  }


  def delById(id: String) =  {
    db.withConnection { implicit connection =>
      SQL"delete from pku_simulate_logs where id = ${id}".executeUpdate()
    }
  }


  def save(log: PkuLog) =  {
    db.withConnection { implicit connection =>
      SQL("""
            INSERT INTO `pku_simulate_logs`
              (`id`, `createDate`, `creator`, `del`, `description`, `invalid`, `modifiedBy`, `modifyDate`, `orderNo`, `batchId`, `businessDesc`, `strategyId`, `strategyName`, `resourceId`, `resourceName`, `counts`, `finalResult`, `detectionResult`,`formula`)
              VALUES
              (REPLACE(UUID(), "-", ""), NOW(), {creator}, {del}, {description}, {invalid}, {modifiedBy}, NOW(), {orderNo}, {batchId}, {businessDesc}, {strategyId}, {strategyName}, {resourceId}, {resourceName}, {counts}, {finalResult}, {detectionResult},{formula});
      """).bind(log).executeInsert()
    }
  }




  def updateById(log: PkuLog) =  {
    db.withConnection { implicit connection =>
      SQL("""
            UPDATE `pku_simulate_logs`
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
            `strategyId` = {strategyId},
            `strategyName` = {strategyName},
            `resourceId` = {resourceId},
            `resourceName` = {resourceName},
            `counts` = {counts},
            `finalResult` = {finalResult},
            `detectionResult` = {detectionResult},
            `formula`={formula},
            WHERE `id` = {id};
      """).bind(log.copy(id = log.id)).executeUpdate()
    }
  }

  def getCount() =  {
    db.withConnection { implicit connection =>
      val cal = Calendar.getInstance
      cal.add(Calendar.DATE, -10)
      val time = cal.getTime
      val yestoday = new SimpleDateFormat("yyyy-MM-dd").format(time)
      var  weherSql = "";
      weherSql += s" and createDate >=  ${yestoday}  "
      var limitSql = s" order By createDate desc "
      val totalRows = SQL("select count(1) from pku_simulate_logs where 1=1 "  + weherSql ).as(scalar[Long].single);
      weherSql += s" and finalResult =  'successed'  "
      val succRows = SQL("select count(1) from pku_simulate_logs where 1=1 "  + weherSql ).as(scalar[Long].single);
      var failRows = totalRows - succRows;
      var map = Map("log"->totalRows,"logSuccess"->succRows,"logFail"->failRows)
      map
    }
  }

}
