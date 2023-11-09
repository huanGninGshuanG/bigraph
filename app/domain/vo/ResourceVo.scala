package domain.vo

import java.util.Date

case class ResourceVo(var id: String,
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
                       var extContent: String = "extContent",
                         strategyId:String)




