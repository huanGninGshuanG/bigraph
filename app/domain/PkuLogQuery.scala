package domain

case class PkuLogQuery(
                   startDate:  String = "",
                   endDate:  String = "",
                   strategyId: String = "",
                   strategyName: String = "",
                   resourceId: String = "",
                   resourceName: String = "",
                   finalResult: String = ""
                 )


