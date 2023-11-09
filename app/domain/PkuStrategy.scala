package domain

import java.util.Date

import anorm.{Macro, ToParameterList}

case class PkuStrategy(id: String,
                       createDate: Date,
                       creator: String = "PKU",
                       del: Boolean,
                       var description: String,
                       invalid: Boolean,
                       modifiedBy: String = "PKU",
                       modifyDate: Date,
                       orderNo: Int,
                       code: String,
                       strategyType: String,
                       var name: String)

object PkuStrategy {
  implicit def toParameters: ToParameterList[PkuStrategy] =
    Macro.toParameters[PkuStrategy]
}


