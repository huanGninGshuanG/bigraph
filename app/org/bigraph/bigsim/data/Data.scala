package org.bigraph.bigsim.data

import org.bigraph.bigsim.parser.ArithExpressionParser
import org.bigraph.bigsim.utils.GlobalCfg

import scala.io.Source

class Data(n: String, dt: String, v: String, u: String) {
  val name = n
  val dataType = dt
  val unit = u
  var value = v
  val ratio = getRatio(unit)
  val percentage = getPercentage(unit)

  def getRatio(unit: String): Boolean = {
    unit.contains("/")
  }

  def getPercentage(unit: String): Boolean = {
    unit.equals("%")
  }
}

class WeightExpr(expr: String) {
  val expression = expr

  def getReport(): Double = {
    ArithExpressionParser.apply(expression)
  }
}

object Data {
  var data: Map[String, Data] = Map()
  var bdInitData: Map[String, Data] = Map() //add by lbj Back Derivation Init Data
  var wExpr: WeightExpr = null //权值表达式
  var dataCalcsWithClk: Map[String, String] = Map()

  def parseAgentExpr(expr: String) {
    expr.split("\t").foreach(f => {     // 在BigSim中的表达式输入框中，没办法输入 \t 制表符啊，按tab键就会跳转到下一个输入框中去。
      if (f.startsWith(GlobalCfg.wExprPrefStr)) {       // 提取项语言中的权值表达式，记录到wExpr中去
        addWeightExpr(f.substring(GlobalCfg.wExprPrefStr.length).trim)  // substring 取前面字符串的一段， .trim 把
      } else if (f.startsWith(GlobalCfg.exprPrefStr)) {   // 提取agent中的赋值表达式
        f.substring(GlobalCfg.exprPrefStr.length).split(",").foreach(e => {
          val kv = e.split("=")
          if (kv.length == 2)   // 每个赋值语句只能有一个等号=，
            dataCalcsWithClk += kv(0) -> kv(1)
        })
      }
    })
  }

  def addWeightExpr(expr: String) { //这里set的权值表达式
    wExpr = new WeightExpr(expr)
  }

  def getWeightExpr: String = {
    if (wExpr == null)
      "wExpr="
    else
      return "wExpr=" + wExpr.expression
  }

  def updateDataCalcsWithClk(clkIncr: String) {
    /**
     * Add ClkIncr data for dataCalcsWithClk
     */
    data += "ClkIncr" -> new Data("ClkIncr", "Double", clkIncr, "tick")
    dataCalcsWithClk.foreach(dc => {
      update(dc._1, dc._2)
    })
    data -= "ClkIncr"
  }

  def getValues(sep: String): String = {
    var res: List[String] = List()
    data.map(d => {
      res = res.:+(d._1 + "=" + d._2.value)
    })
    res.mkString(sep)
  }

  def getReport: Double = {
    if (wExpr == null) {
      0
    } else {
      wExpr.getReport
    }
  }

  def parseData(path: String) {
    data += "SysClk" -> new Data("SysClk", "Double", GlobalCfg.SysClk.toString, "tick")
    for (line <- Source.fromFile(path).getLines) {
      if (!line.startsWith("#")) {
        val items = line.split("\t");
        if (items.length != 4) {
          println("initial data format error!")
        } else {
          data += items(0) -> new Data(items(0), items(1), items(2), items(3))
        }
      }
    }
  }

  def parseDataWithData(oriData: String) {
    data += "SysClk" -> new Data("SysClk", "Double", GlobalCfg.SysClk.toString, "tick")
    for (line <- oriData.split("\n")) {
      if (!line.startsWith("#")) {
        val items = line.split("\t");
        if (items.length != 4) {
          println("initial data format error!")
        } else {
          data += items(0) -> new Data(items(0), items(1), items(2), items(3))
        }
      }
    }
  }


  def parseBDData(path: String) { //add by lbj
    bdInitData += "SysClk" -> new Data("SysClk", "Double", GlobalCfg.SysClk.toString, "tick")
    for (line <- Source.fromFile(path).getLines) {
      if (!line.startsWith("#")) {
        val items = line.split("\t");
        if (items.length != 4) {
          println("initial data format error!")
        } else {
          bdInitData += items(0) -> new Data(items(0), items(1), items(2), items(3))
        }
      }
    }
  }

  def getValue(name: String): Double = {
    if (!data.contains(name)) {
      0
    } else if (data(name).dataType.equals("String")) {
      data(name).value.hashCode.toDouble
    } else {
      data(name).value.toDouble
    }
  }

  def update(name: String, exp: String) { //根据已有表达式列表data更新表达式name的数据
    if (data.contains(name) && data.getOrElse(name, null).dataType.equals("String")) {  // 什么表达式用单引号分割？
      var strs = exp.split("'", -1)
      if (strs.length == 3) {
        data(name).value = strs(1)
      }
    } else if (data.contains(name)) { //data是解析好的值列表，如taxi.businessfee，这种可直接找到，但David.hasChecked这种就要先从Passenger翻译过来才能找到，data就是直接把data文件解析过来的键值对
      data(name).value = ArithExpressionParser.apply(exp).toString
    } else {
      println("data update error!")
    }
  }
}

object testData {
  def main(args: Array[String]) {
    Data.parseData("Examples/MobileCloud/data/checker.txt")
    Data.data.foreach(f => println(f._2.name + " " + f._2.ratio + " " + f._2.percentage))
    Data.addWeightExpr("(fee+energy*15+(0.7*5))")
    println(Data.getReport)
  }
}