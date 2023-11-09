//package org.bigraph.bigsim.utils.draw
//import breeze.linalg.{DenseVector, linspace}
//import breeze.optimize.linear.PowerMethod.BDV
//import breeze.plot.{Figure, plot}
//import org.bigraph.bigsim.calculation.reputation.EvaluationCredit
//import org.jfree.chart.annotations.XYTextAnnotation
//
//import java.util.Date
//class GraphvizDraw {
//
//}
//object GraphvizDraw {
//  // 画多个点连成的折线图
//  def drawLineChart(xAxis: Array[Double], yAxis: Array[Double], xLabel: String, yLabel: String, picName: String): Unit = {
//    var x = new BDV(xAxis)
//    var y = new BDV(yAxis)
//
//    val f = Figure()
//    val p = f.subplot(0)
//    // linspace指定横坐标并且是均匀分布
// //   val x1 = linspace(0, x.size, x.size)
//    //  p.title = ""
//    p.ylim = (0.0, 1.0)
//    p.legend = true
//    p += plot(x, y, name="seller_global_credit")
//    //  p += plot(x, m1, '+', "red")
//    p.xlabel = xLabel
//    p.ylabel = yLabel
//    p.setYAxisDecimalTickUnits() // 自动设置纵坐标间隔
//    //   p.plot.addAnnotation(new XYTextAnnotation("增加注释",2, 3.5))
//    // p.plot.addAnnotation(new XYTextAnnotation("",2, 3.5))
//    var now = new Date()
//    var time = now.getTime
//    f.saveas("pics/global_credit/" + picName + "_" + time + ".png")
//  }
//
//  // xLabel是横坐标、 yLabel是纵坐标
//  def multiDrawLineChart(multiList: List[XY], xLabel: String, yLabel: String, picName: String): Unit = {
//    var f = Figure()
//    var p = f.subplot(0)
//   // var i = 0
//
//    var i = 10
//    var na: String = ""
//    multiList.foreach(xy => {
//      var x = xy.xAxis
//      var y = xy.yAxis
//    //  var name1 = i.toString + "%"
//      println("=====" + i.toString + "%")
//      p += plot(x, y, name = i.toString + "%")
//  //    p += plot(x, y)
//      i = i + 20
//    })
//    p.xlabel = xLabel
//    p.ylabel = yLabel
//    p.legend = true
//  //  p.setYAxisDecimalTickUnits() // 自动设置纵坐标间隔
//    //   p.plot.addAnnotation(new XYTextAnnotation("增加注释",2, 3.5))
//    // p.plot.addAnnotation(new XYTextAnnotation("",2, 3.5))
//    var now = new Date()
//    var time = now.getTime
//    f.saveas("pics/global_eval_credit/" + picName + "_" + time + ".png")
//  }
//
//  def drawList(l: List[List[EvaluationCredit]]): Unit = {
//    var list: List[XY] = List()
//    l.foreach(x => {
//      var size = x.size
//      var xA = new Array[Double](size)
//      var yA = new Array[Double](size)
//      var index = 0
//      x.foreach(y => {
//        xA(index) = y.trTime
//        yA(index) = y.evaluationCredit
//        index = index + 1
//      })
//      var xy = new XY(xA, yA)
//      list = list :+ xy
//    })
//    multiDrawLineChart(list, "time", "global_evaluation_credit", "multi_ratio")
//
//  }
//
//  def main(args: Array[String]): Unit = {
//    var x1 = Array(1.9, 2.9, 3.4, 3.5)
//    var y1 = Array(3.9, 4.9, 9.4, 2.5)
//
//    var x2 = Array(1.9, 2.9, 3.4, 3.5, 4,6)
//    var y2 = Array(1.9, 2.9, 3.4, 3.5, 8.9)
//    var list: List[XY] = List()
//    var xy1 = new XY(x1, y1)
//    var xy2 = new XY(x2, y2)
//    list = list :+ xy1
//    list = list :+ xy2
//    multiDrawLineChart(list, "time", "test", "test")
//
//  }
//}
