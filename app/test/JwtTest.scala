package test

import org.junit.Test
import authentikat.jwt._


class JwtTest {

  @Test
  def a = {
    val ss : String = "afds 3fdk iki li hpku"
    println(java.net.URLEncoder.encode(ss, "UTF-8"))
  }


  @Test
  def jwtTest = {
    val header = JwtHeader("HS256")
    val claimsSet = JwtClaimsSet(Map("Hey" -> "foo")) //用户id
    val jwt: String = JsonWebToken(header, claimsSet, "secretkey")//用户密码
    println(jwt)
    val isValid = JsonWebToken.validate(jwt, "secretkey")
    println(isValid)
  }

}
