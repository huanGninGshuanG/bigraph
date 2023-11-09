package utils

import com.google.gson.{Gson, GsonBuilder, JsonParser}
import domain.{ResponseListData, ResponseTransferData}
import org.json4s.ShortTypeHints
import org.json4s.native.Serialization
import org.json4s.native.Serialization.write
import repository.Page

object JSONUtil {


  val GSON: Gson = new GsonBuilder().create()

  def toJSONString[A](a: A, responseCode: String = "0000",
                      success: Boolean = true,
                      responseMessage: String = "OK",
                      currentLoginName: String = "PKU",
                      currentUserName: String = "PKU"): String = {

    implicit val jsonFormats = org.json4s.DefaultFormats

    val strRes = write(a)

    val jsonStringAsObject = new JsonParser().parse(strRes).getAsJsonObject


    toJSONStr(jsonStringAsObject)
  }

  def toJSONStr(obj: Object,
                responseCode: String = "0000",
                success: Boolean = true,
                responseMessage: String = "OK",
                currentLoginName: String = "PKU",
                currentUserName: String = "PKU"): String = {


    val retData = new ResponseTransferData();
    retData.setResponseData(obj)
    retData.setCurrentLoginName(currentLoginName)
    retData.setCurrentUserName(currentUserName)
    retData.setResponseCode(responseCode)
    retData.setResponseMessage(responseMessage)
    retData.setSuccess(success)

    GSON.toJson(retData)
  }

  def listToJSONString[A](p: Page[A],
                          responseCode: String = "0000",
                          success: Boolean = true,
                          responseMessage: String = "OK",
                          currentLoginName: String = "PKU",
                          currentUserName: String = "PKU"): String = {
    implicit val formats = Serialization.formats(ShortTypeHints(List()))
    val ret = write(p.items)
    println(ret)

    val jsonStringAsObject = new JsonParser().parse(ret).getAsJsonArray

    val retList = new ResponseListData()
    retList.setDatas(jsonStringAsObject)
    retList.setPage(p.page)
    retList.setTotalRecordNums(p.total)

    toJSONStr(retList)
  }

  def seqToJSONString[A](p: Seq[A],
                          responseCode: String = "0000",
                          success: Boolean = true,
                          responseMessage: String = "OK",
                          currentLoginName: String = "PKU",
                          currentUserName: String = "PKU"): String = {
    implicit val formats = Serialization.formats(ShortTypeHints(List()))
    val ret = write(p)
    println(ret)

    val jsonStringAsObject = new JsonParser().parse(ret).getAsJsonArray

    val retList = new ResponseListData()
    retList.setDatas(jsonStringAsObject)
    toJSONStr(retList)
  }

}
