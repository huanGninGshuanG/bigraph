package org.bigraph.bigsim.utils.generator

import scala.util.Random

class NormalGenerator extends Generator{
  def nextTime(posi: Double): Double ={
    var ans: Double = 0
    do{
      ans = Random.nextGaussian() + posi
    }while(ans <= 0)
    ans
  }

  def nextNormalMoney(posi: Int): Int ={
    var ans: Int = 0
    do{
      ans = Random.nextGaussian().toInt + posi
    }while(ans <= 0)
    ans
  }
}
