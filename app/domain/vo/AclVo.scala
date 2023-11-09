package domain.vo

import anorm.{Macro, ToParameterList}
case class AclVo(
                  var userId: String,
                  var menuIds: List[String] ,
                  var roleIds: List[String])

object AclVo {
  implicit def toParameters: ToParameterList[AclVo] =
    Macro.toParameters[AclVo]
}


