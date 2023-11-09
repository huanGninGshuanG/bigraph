package org.bigraph.bigsim.parser

/**
  *
  * @param transitionProbabilityArray 状态转移矩阵
  * @param emissionProbabilityArray 观测概率矩阵（混淆矩阵）
  * @param startProbabilityArray 初始概率向量
  * @param statesArray 隐状态状态空间
  * @param observationsArray 观测状态空间
  * @param observerSeqArray 观测值
  */
case class HmmData (
                     transitionProbabilityArray: Array[Array[Double]] = Array(),
                     emissionProbabilityArray: Array[Array[Double]] = Array(),
                     startProbabilityArray: Array[Double] = Array(),
                     statesArray: Array[String] = Array(),
                     observationsArray: Array[String] = Array(),
                     observerSeqArray: Array[Int] = Array()
                   )
