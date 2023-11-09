package test

import org.junit.Test

class TestA {

  @Test
  def a = {
    val ss : String = "afds 3fdk iki li hpku"
    println(java.net.URLEncoder.encode(ss, "UTF-8"))
  }

  @Test
  def b = {
    val ss : String = "%23%20Controls%0A%25active%20Greater%20%3A%202%3B%0A%25active%20Less%20%3A%202%3B%0A%25active%20GreaterOrEqual%20%3A%202%3B%0A%25active%20LessOrEqual%20%3A%202%3B%0A%25active%20Equal%20%3A%202%3B%0A%25active%20NotEqual%20%3A%202%3B%0A%25active%20Exist%20%3A%201%3B%0A%25active%20InstanceOf%20%3A%202%3B%0A%25active%20Buyer%20%3A%200%3B%0A%25active%20Market%20%3A%200%3B%0A%25active%20Seller%20%3A%200%3B%0A%0A%23%20Rules%0A%25rule%20r_%20a%3AMarket.b%3ASeller%20%7C%20c%3ABuyer%20-%3E%20a%3AMarket.(b%3ASeller%20%7C%20c%3ABuyer)%7B%7D%3B%0A%0A%25rule%20r_%20nil%20-%3E%20nil%7B%7D%3B%0A%0A%0A%23%20Model%0A%25agent%20a%3AMarket.b%3ASeller%20%7C%20c%3ABuyer%3B%0A%0A%23%20Go!%0A%25check%3B%0A"
    println(java.net.URLDecoder.decode(ss, "utf-8"))
  }

  @Test
  def c = {
    val ss = "bgm%E6%96%87%E4%BB%B6%E4%BF%9D%E5%AD%98%E6%B5%8B%E8%AF%95";
    println(java.net.URLDecoder.decode(ss, "utf-8"))
  }

  @Test
  def d = {
    for(i <- 0 to 10){
      println(i + "->" + BigInt(1500, scala.util.Random).toString(36).substring(0, 10))
    }
  }

  @Test
  def e: Unit = {
    var hmmList: java.util.ArrayList[String] = new java.util.ArrayList()
    hmmList.add("a")
    hmmList.add("d")
    hmmList.add("f2")
    hmmList.add("f1")

    //val unit = hmmList.stream().filter(_.contains("f"))
    var bool = hmmList.stream().anyMatch(_.contains("f"))
    println(bool)

    val set = Set("a", "b", "c")
    bool = set.exists(_.contains("d"))
    println(bool)
  }
}
