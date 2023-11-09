package domain

import java.util.Date

import anorm.{Macro, ToParameterList}

case class PkuAcl(id: String,
                       createDate: Date,
                       creator: String = "PKU",
                       del: Boolean,
                       var description: String,
                       invalid: Boolean,
                       var modifiedBy: String = "PKU",
                       modifyDate: Date,
                       orderNo: Int,
                       aclState: Int,
                       aclTriState: Int,
                       menuId: String,
                       principalId: String,
                       principalType: String)

object PkuAcl {
  implicit def toParameters: ToParameterList[PkuAcl] =
    Macro.toParameters[PkuAcl]
}


