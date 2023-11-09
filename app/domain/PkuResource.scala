package domain

import java.util.Date

import anorm.{Macro, ToParameterList}

case class PkuResource(var id: String,
                       createDate: Date,
                       creator: String = "PKU",
                       del: Boolean,
                       var description: String,
                       invalid: Boolean,
                       modifiedBy: String = "PKU",
                       modifyDate: Date,
                       orderNo: Int,
                       var code: String,
                       var fileContent: String,
                       fileType: String,
                       name: String,
                       var extContent: String = "extContent")

object PkuResource {
  implicit def toParameters: ToParameterList[PkuResource] =
    Macro.toParameters[PkuResource]
}


