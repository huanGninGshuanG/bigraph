package domain

import java.util.Date

import anorm.{Macro, ToParameterList}

case class PkuUser(id: String,
                       createDate: Date,
                       creator: String = "PKU",
                       del: Boolean,
                       var description: String,
                       invalid: Boolean,
                      var modifiedBy: String = "PKU",
                       modifyDate: Date,
                       orderNo: Int,
                       code: String,
                       expireTime: Date,
                       headPic: String,
                       linkaddress: String,
                       linkmail: String,
                       linkphone: String,
                       loginname: String,
                       loginpwd: String,
                       loginpwdShow: String,
                        var name: String,
                       userType: String)

object PkuUser {
  implicit def toParameters: ToParameterList[PkuUser] =
    Macro.toParameters[PkuUser]
}


