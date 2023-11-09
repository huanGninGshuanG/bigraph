package org.bigraph.bigsim.utils.generator

import scala.util.Random


class UniformGenerator extends Generator {
    def nextTime(time: Double): Double = {
      var interval: Double = 0.0
      do {
        interval = scala.util.Random.nextDouble()
      }while(interval <= 0)
      interval
    }

  def nextUniformMoney(minMoney: Int, maxMoney: Int): Int = {
    Random.nextInt(maxMoney - minMoney) + minMoney
  }
}
