package org.bigraph.bigsim.utils

class LtlKmp {
  /**
   * 用于ltlShortest匹配
   */
  object KMPv2 {

    def kmpMatch(source:String, pattern: String):scala.collection.mutable.Seq[Int] = {

      var res = scala.collection.mutable.Seq[Int]()

      val statusArr = kmpPrefixFunc(pattern)

      var k =0//表示已经匹配的个数
      val n = source.length
      val m = pattern.length

      for(q <- 0 until n){//n次
        while(k>0 && source.charAt(q) != pattern.charAt(k)){
          k = statusArr(k-1)
        }
        if(source.charAt(q) == pattern.charAt(k)){
          k+=1
        }
        if(k == m){
          res = res.:+(q-m+1)
          k = statusArr(k-1)
        }
      }
      res
    }

    //pattern字符串第k位前缀的与自身匹配的最长后缀
    //P[1..m],1≤q≤m,0≤k<q，求k使得P[1..k]是P[1..q]的最长后缀，kmpPrefixFunc(q)=k
    def kmpPrefixFunc(pattern:String):Array[Int]={
      val m = pattern.length
      val res = new Array[Int](m)
      res(0) = 0
      var k = res(0)
      for(q <- 1 until m){
        while(k>0 && pattern.charAt(k) != pattern.charAt(q)){
          k = res(k)
        }
        if(pattern.charAt(k) == pattern.charAt(q)){
          k+=1
        }
        res(q) = k
      }
      res
    }
  }
}
