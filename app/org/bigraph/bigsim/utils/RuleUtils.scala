package org.bigraph.bigsim.utils

import scala.Array.concat
import scala.collection.mutable.Set
import scala.util.Random
import scala.util.matching.Regex

object RuleUtils {
  def main(args: Array[String]): Unit = {
  //  var s1 = "# Controls\n%active Greater : 2;\n%active Less : 2;\n%active GreaterOrEqual : 2;\n%active LessOrEqual : 2;\n%active Equal : 2;\n%active NotEqual : 2;\n%active person : 0;\n%active lightzone : 0;\n%active light : 0;\n%active illum : 0;\n\n# Rules\n%rule r_0 a:person | b:lightzone.s1:light.low:illum -> b:lightzone.(s1:light.low:illum | a:person){};\n\n\n\n# Model\n%agent a:person | b:lightzone.s1:light.low:illum;\n\n\n# Properties\n%properties s1(nodeNum:44,56,78 maxAge:2,3,4 minAge:3,4,56 minMoney:2,56,34 maxMoney:1,3,4)\n\n\n\n\n\n\n\n\n#SortingLogic\n\n\n# Go!\n%check;"
  var s = "# Controls\n%active Greater : 2;\n%active Less : 2;\n%active GreaterOrEqual : 2;\n%active LessOrEqual : 2;\n%active Equal : 2;\n%active NotEqual : 2;\n%active Exist : 1;\n%active InstanceOf : 2;\n%active ConsensusNode : 1;\n%active BuyerNode : 1;\n%active SellerNode : 1;\n%active ReputationModule : 0;\n%active SmartContract : 1;\n%active TrMsg : 0;\n%active Money : 0;\n%active Evaluation : 1;\n%active ConsensuModule : 0;\n%active DealModule : 0;\n%active TrPool : 0;\n%active Block : 0;\n%active DB : 0;\n%active AckMsg : 0;\n%active RefuseMsg : 0;\n%active TrAck : 0;\n%active TrRefuse : 0;\n%active LocalRepu : 0;\n%active GlobalRepu : 0;\n%active Time : 0;\n%active leaderFlag : 0;\n%active age : 0;\n%active SystemClock : 0;\n%active LocalReputationSmartContract : 1;\n%active GlobalReputationSmartContract : 1;\n\n# Rules\n%rule r_consensusFail a:BuyerNode[idle] | b:SellerNode[idle] | c:ConsensusNode[idle].(d:ConsensuModule.h:Money | e:TrPool | f:ReputationModule | g:DB) -> a:BuyerNode[idle] | b:SellerNode[idle] | c:ConsensusNode[idle].(d:ConsensuModule | e:TrPool | f:ReputationModule | g:DB){};\n\n%rule r_consensusSucc2 a:BuyerNode[idle] | b:SellerNode[idle] | c:ConsensusNode[idle].(d:ConsensuModule.h:Money | e:TrPool | f:ReputationModule | g:DB) -> a:BuyerNode[idle] | b:SellerNode[idle] | c:ConsensusNode[idle].(d:ConsensuModule | e:TrPool | f:ReputationModule | g:DB.h:Money){};\n\n%rule r_generateEval2 a:BuyerNode[idle].h:Money | b:SellerNode[idle] | c:ConsensusNode[idle].(d:ConsensuModule | e:TrPool | f:ReputationModule | g:DB) -> a:BuyerNode[idle].h:Evaluation[idle] | b:SellerNode[idle] | c:ConsensusNode[idle].(d:ConsensuModule | e:TrPool | f:ReputationModule | g:DB){};\n\n%rule r_informBuyer2 a:BuyerNode[idle] | b:SellerNode[a:edge] | c:ConsensusNode[a:edge].(d:ConsensuModule | e:TrPool.h:Money | f:ReputationModule | g:DB) -> a:BuyerNode[idle].h:Money | b:SellerNode[idle] | c:ConsensusNode[idle].(d:ConsensuModule | e:TrPool | f:ReputationModule | g:DB){};\n\n%rule r_sellerAgree2 a:BuyerNode[idle] | b:SellerNode[a:edge].h:Money | c:ConsensusNode[a:edge].(d:ConsensuModule | e:TrPool | f:ReputationModule | g:DB) -> a:BuyerNode[idle] | b:SellerNode[a:edge] | c:ConsensusNode[a:edge].(d:ConsensuModule | e:TrPool.h:Money | f:ReputationModule | g:DB){};\n\n%rule r_sellerDisAgree2 a:BuyerNode[idle] | b:SellerNode[a:edge].h:Money | c:ConsensusNode[a:edge].(d:ConsensuModule | e:TrPool | f:ReputationModule | g:DB) -> a:BuyerNode[idle] | b:SellerNode[idle] | c:ConsensusNode[a:edge].(d:ConsensuModule | e:TrPool | f:ReputationModule | g:DB){};\n\n%rule r_sendEval2 a:BuyerNode[idle].h:Evaluation[idle] | b:SellerNode[idle] | c:ConsensusNode[idle].(d:ConsensuModule | e:TrPool | f:ReputationModule | g:DB) -> a:BuyerNode[a:edge] | b:SellerNode[idle] | c:ConsensusNode[a:edge].(d:ConsensuModule | e:TrPool | f:ReputationModule.(h:Evaluation[idle] | i:LocalReputationSmartContract[idle] | j:GlobalReputationSmartContract[idle]) | g:DB){};\n\n%rule r_sendToSeller2 a:BuyerNode[idle] | b:SellerNode[idle] | c:ConsensusNode[idle].(d:ConsensuModule.h:Money | e:TrPool | f:ReputationModule | g:DB) -> a:BuyerNode[idle] | b:SellerNode[idle] | c:ConsensusNode[idle].(d:ConsensuModule.h:Money | e:TrPool | f:ReputationModule | g:DB){};\n\n%rule r_sendTr2 a:BuyerNode[idle].h:Money | b:SellerNode[idle] | c:ConsensusNode[idle].(d:ConsensuModule | e:TrPool | f:ReputationModule | g:DB) -> a:BuyerNode[a:edge] | b:SellerNode[idle] | c:ConsensusNode[a:edge].(d:ConsensuModule | e:TrPool.h:Money | f:ReputationModule | g:DB){};\n\n%rule r_sendTran2 a:BuyerNode[idle] | b:SellerNode[idle] | c:ConsensusNode[idle].(d:ConsensuModule | e:TrPool | f:ReputationModule | g:DB.h:Money) -> a:BuyerNode[idle] | b:SellerNode[a:edge].h:Money | c:ConsensusNode[a:edge].(d:ConsensuModule | e:TrPool | f:ReputationModule | g:DB){};\n\n%rule r_consensus2 a:BuyerNode[a:edge] | b:SellerNode[idle] | c:ConsensusNode[a:edge].(d:ConsensuModule | e:TrPool.h:Money | g:DB | f:ReputationModule) -> b:SellerNode[idle] | c:ConsensusNode[idle].(d:ConsensuModule.h:Money | e:TrPool | g:DB | f:ReputationModule) | a:BuyerNode[idle]{};\n\n%rule r_calGlobalRepu2 a:BuyerNode[a:edge] | b:SellerNode[idle] | c:ConsensusNode[a:edge].(d:ConsensuModule | e:TrPool | f:ReputationModule.(h:Evaluation[idle] | i:LocalReputationSmartContract[idle] | j:GlobalReputationSmartContract[idle]) | g:DB) -> b:SellerNode[idle] | c:ConsensusNode[idle].(d:ConsensuModule | e:TrPool | f:ReputationModule.(i:LocalReputationSmartContract[idle] | j:GlobalReputationSmartContract[a:edge] | k:GlobalRepu | h:Evaluation[a:edge]) | g:DB) | a:BuyerNode[idle]{};\n\n%rule r_calLocalRepu2 a:BuyerNode[a:edge] | b:SellerNode[idle] | c:ConsensusNode[a:edge].(d:ConsensuModule | e:TrPool | f:ReputationModule.(h:Evaluation[idle] | i:LocalReputationSmartContract[idle] | j:GlobalReputationSmartContract[idle]) | g:DB) -> a:BuyerNode[idle] | b:SellerNode[idle] | c:ConsensusNode[idle].(d:ConsensuModule | e:TrPool | f:ReputationModule.(h:Evaluation[a:edge] | i:LocalReputationSmartContract[a:edge] | j:GlobalReputationSmartContract[idle] | k:LocalRepu) | g:DB){};\n\n%rule r_globalRepuConsens2 a:BuyerNode[idle] | b:SellerNode[idle] | c:ConsensusNode[idle].(d:ConsensuModule | e:TrPool | f:ReputationModule.(h:Evaluation[a:edge] | i:LocalReputationSmartContract[idle] | j:GlobalReputationSmartContract[a:edge] | k:GlobalRepu) | g:DB) -> a:BuyerNode[idle] | b:SellerNode[idle] | c:ConsensusNode[idle].(d:ConsensuModule.k:GlobalRepu | e:TrPool | f:ReputationModule.(i:LocalReputationSmartContract[idle] | j:GlobalReputationSmartContract[idle]) | g:DB){};\n\n%rule r_globalRepuSave2 a:BuyerNode[idle] | b:SellerNode[idle] | c:ConsensusNode[idle].(d:ConsensuModule.k:GlobalRepu | e:TrPool | f:ReputationModule.(i:LocalReputationSmartContract[idle] | j:GlobalReputationSmartContract[idle]) | g:DB) -> a:BuyerNode[idle] | b:SellerNode[idle] | c:ConsensusNode[idle].(d:ConsensuModule | e:TrPool | f:ReputationModule.(i:LocalReputationSmartContract[idle] | j:GlobalReputationSmartContract[idle]) | g:DB.k:GlobalRepu){};\n\n%rule r_localRepuConsen2 a:BuyerNode[idle] | b:SellerNode[idle] | c:ConsensusNode[idle].(d:ConsensuModule | e:TrPool | f:ReputationModule.(h:Evaluation[a:edge] | i:LocalReputationSmartContract[a:edge] | j:GlobalReputationSmartContract[idle] | k:LocalRepu) | g:DB) -> a:BuyerNode[idle] | b:SellerNode[idle] | c:ConsensusNode[idle].(d:ConsensuModule.k:LocalRepu | e:TrPool | f:ReputationModule.(j:GlobalReputationSmartContract[idle] | i:LocalReputationSmartContract[idle]) | g:DB){};\n\n%rule r_localRepuSave2 a:BuyerNode[idle] | b:SellerNode[idle] | c:ConsensusNode[idle].(d:ConsensuModule.k:LocalRepu | e:TrPool | f:ReputationModule.(i:LocalReputationSmartContract[idle] | j:GlobalReputationSmartContract[idle]) | g:DB) -> a:BuyerNode[idle] | b:SellerNode[idle] | c:ConsensusNode[idle].(d:ConsensuModule | e:TrPool | f:ReputationModule.(i:LocalReputationSmartContract[idle] | j:GlobalReputationSmartContract[idle]) | g:DB.k:LocalRepu){};\n\n\n\n# Model\n%agent a:BuyerNode[idle].h:Money | b:SellerNode[idle] | c:ConsensusNode[idle].(d:ConsensuModule | e:TrPool | f:ReputationModule | g:DB);\n\n\n\n\n\n\n\n\n\n#SortingLogic\n\n\n# Go!\n%check;";
   /* var l  = BGMParser.parseFromString(s1)
    var bigraph = BGMTerm.toBigraph(l)
    bigraph.rules.foreach(x => println("rules.reactum:" + x.reactum))
*/
    // println(bigraph.rules)
    var s1 = Scale(s, "a1", 2, 3)
    var rs = getRules(s1)
    rs.foreach(x => {
      println(x)
    })
  }
  // main api  n: zong  return complete bgm
  def Scale(s: String, name: String, n: Int, lateralLen: Int): String = {
    var newRule = RuleHandle(s, name, n, lateralLen)
    var newStr = makeNewRules(s, newRule)
     print(newStr)
    newStr
  }

  def makeNewRules(s: String, newRule: String): String = {
    var r = "# Rules\n" + newRule + "\n\n\n\n"
    var i = s.indexOf("# Rules")
    var j = s.indexOf("# Model")
    var firstPart = s.substring(0, i)
    var secondPart = s.substring(j)
    var eventualStr = firstPart + r + secondPart
    /*  println("======================== first part ==================")
      println(firstPart)
      println("======================== first part ==================")

      println("======================== second part =================")
      println(secondPart)
      println("======================== second part =================")*/
    println(eventualStr)

    /*   var newS = s.replaceAll("# Rules.*# Model", r)
       println(newS)
       newS*/
    //   var pattern = new Regex("# Rules.*# Model")
    /* println("======original s===========")
     println(s)
     println("======original s===========")*/

    /*
        var newStr = pattern replaceFirstIn(s, r)
        println("======new Str==========")
        println(newStr)

        println("======new Str==========")
        newStr*/
    " "
  }
  def splitRule(s: String): List[String]= {
    var h = s.indexOf("->")
    var first = s.substring(6, h - 1);
    var second = s.substring(h + 3);
    var l: List[String] = List()
    l = second +: l
    l = first +: l
    l
  }


  def getRules(s: String): Array[String] = {
    var pattern = new Regex("%rule.*;")
    var rules = (pattern findAllIn s).mkString(",")
    var rulesArray = rules.split(',')
    rulesArray
  }

  def getRuleNames(ar: Array[String]): Array[String] = {
    var size = ar.size
    var arr = new Array[String](ar.size)
    for(i <- 0 until size){
      var index = ar(i).indexOf(" ", 6)
      arr(i) = ar(i).substring(6, index)
    }
    arr
  }
  // original rule array, len: lateral len usage: lateralScale(getRules(), 2)
  def lateralScale(arr: Array[String], len: Int): Array[String] = {
    var size = arr.size
    var rns = getRuleNames(arr)
    var ans: Array[String] = Array[String]()
    for(i <- 1 until size){
      var ss = arr(i)
      var l = splitRule(ss)
      var h = l(1)
      var in1 = Random.nextInt(size)
      var hIndex = h.indexOf("{")
      var ret = h.substring(0, hIndex)
      var as = List[String]()
      var initialRuleIndex = size
      var index1 = ret.indexOf(":")
      var oldName = ret.substring(0, index1)

      for(i <- 1 to len){
        var index = ret.indexOf(":")
        var newName = oldName + i
        var newRet = newName + ret.substring(index)
        var newRule = "%rule " +  rns(in1) + " " + ret + " -> " + newRet + "{};"
        //   println(newRule)
        as = newRule +: as
        ret = newRet
      }
      var ar = as.toArray
      ans = concat(ans, ar)
    }
    var af = concat(arr, ans)
    af

  }

  // s rule
  def generateNewRule(s: String, ruleArray: Array[String], name: String, n: Int): Set[String] = {
    var rn = getRuleNames(ruleArray)
    var in = rn(Random.nextInt(ruleArray.size))
    var rules: Set[String] = Set()
    for(i <- 1 to n){
      val pattern = new Regex(name)
      var newName = name + i
      var newString = pattern replaceAllIn (s, newName)
      var newStr1 = changeRN(newString, in)
      rules.add(newStr1)
    }
    rules
  }

  def changeRN(s: String, rn: String): String = {
    var pattern = new Regex("r_[\\d]")
    var newString = pattern replaceFirstIn(s, rn)
    //  println(newString)
    newString
  }
// n: zong  lateral ans zong
  def RuleHandle(s: String, name: String, n: Int, lateralLen: Int): String = {
    var rulesArray = getRules(s)
    var firstRule = rulesArray(0)
    var newArrs = lateralScale(rulesArray, lateralLen)
    var firstNewRules = HandleFirstRule(firstRule, name, n)
    var newRules: Set[String] = Set()
    newRules.add(firstRule)
    newRules = newRules ++ firstNewRules
    var ruleIndex = newRules.size
    for(i <- 1 until newArrs.size){
      var oldRule = newArrs(i)
      newRules.add(oldRule)
      var firNewRules = HandleFirstRule(oldRule, name, n)
      var newRule = generateNewRule(oldRule, newArrs, name, n)
      newRules = newRules ++ newRule
      newRules = newRules ++ firNewRules
    }
    var nr = ""
    newRules.foreach(x => {
      nr += x
      nr += "\n\n"
    })
    nr

  }

  def HandleFirstRule(s: String, name: String, n: Int): Set[String] = {

    var i = s.indexOf("->")
    var firstPart = s.substring(0, i)
    var lastPart = s.substring(i)
    /*   println(firstPart)
       println(s.substring(i))*/
    //   println(firstPart + lastPart)
    val pattern = new Regex(name)
    var firstRules: Set[String] = Set()
    for(i <- 1 to n) {
      var newName = name + i
      var newString = pattern replaceAllIn(lastPart, newName)
      var newS = firstPart + newString
      firstRules.add(newS)
    }
    firstRules
  }

}


