package domain

import java.util.Date

import anorm.{Macro, ToParameterList}

case class PkuLogItem(
                   var id: String,
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
                   startTime: Date,
                   endTime: Date,
                   executeDetailLog: String,
                   executeResult: String,
                   flowSize: Int,
                   inputParam: String,
                   outputParam: String,
                   outputGraph: String,
                   detectionResult:String,
                   formula:String
                 )


object PkuLogItem {
  implicit def toParameters: ToParameterList[PkuLogItem] =
    Macro.toParameters[PkuLogItem]
}