package domain

import java.util.Date

import anorm.{Macro, ToParameterList}

case class PkuMenu(var id: String,
                       createDate: Date,
                       creator: String = "PKU",
                       del: Boolean,
                       var description: String,
                       invalid: Boolean,
                       var modifiedBy: String = "PKU",
                       modifyDate: Date,
                       orderNo: Int,
                       code: String,
                       icon: String,
                       leaf: Boolean,
                       var menuType: String,
                       var name: String,
                       target: String,
                       parentId: String
                  )


object PkuMenu {
  implicit def toParameters: ToParameterList[PkuMenu] =
    Macro.toParameters[PkuMenu]
}

