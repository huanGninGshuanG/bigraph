package domain

import java.util.Date

import anorm.{Macro, ToParameterList}

case class PkuLog(
                   id: String,
                   createDate: Date,
                   creator: String,
                   del: Boolean,
                   description: String,
                   invalid: Boolean,
                   modifiedBy: String,
                   modifyDate: Date,
                   orderNo: Int,
                   batchId: String,
                   businessDesc: String,
                   strategyId: String,
                   strategyName: String,
                   resourceId: String,
                   resourceName: String,
                   counts: Int,
                   finalResult: String,
                   detectionResult:String,
                   formula:String
                 )


object PkuLog {
  implicit def toParameters: ToParameterList[PkuLog] =
    Macro.toParameters[PkuLog]
}