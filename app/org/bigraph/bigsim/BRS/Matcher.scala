package org.bigraph.bigsim.BRS

import scala.collection.mutable.Map
import scala.collection.mutable.Set
import org.bigraph.bigsim.model.Control
import org.bigraph.bigsim.model.Hole
import org.bigraph.bigsim.parser.TermParser
import org.bigraph.bigsim.utils.GlobalCfg
import org.bigraph.bigsim.model.Term
import org.bigraph.bigsim.model.Num
import org.bigraph.bigsim.model.Regions
import org.bigraph.bigsim.model.Paraller
import org.bigraph.bigsim.model.TermType
import org.bigraph.bigsim.model.Prefix
import org.bigraph.bigsim.model.Nil
import org.bigraph.bigsim.model.ReactionRule
import org.bigraph.bigsim.model.Name
import org.bigraph.bigsim.model.Bigraph
import org.slf4j.{Logger, LoggerFactory}

/**
 * @author zhaoxin
 * version 0.1
 */

object Matcher {

  def logger : Logger = LoggerFactory.getLogger(this.getClass)

  def tryMatch(t: Term, r: Term, m: Match): Set[Match] = {
    // if (GlobalCfg.DEBUG) println("[kgq] tryMatch")
    if (t.termType == TermType.TPREF && r.termType == TermType.TPREF) {
      var tempT = t.asInstanceOf[Prefix]
      var tempR = r.asInstanceOf[Prefix]
      tryMatchPrefixPrefix(tempT, tempR, m)
    } else if (t.termType == TermType.TPAR && r.termType == TermType.TPREF) {
      var tempT = t.asInstanceOf[Paraller]
      var tempR = r.asInstanceOf[Prefix]
      tryMatchParallerPrefix(tempT, tempR, m)
    } else if (t.termType == TermType.TPREF && r.termType == TermType.TPAR) {
      var tempT = t.asInstanceOf[Prefix]
      var tempR = r.asInstanceOf[Paraller]
      tryMatchPrefixParaller(tempT, tempR, m)
    } else if (t.termType == TermType.TPAR && r.termType == TermType.TPAR) {
      var tempT = t.asInstanceOf[Paraller]
      var tempR = r.asInstanceOf[Paraller]
      tryMatchParallerPareller(tempT, tempR, m)
    } else if (t.termType == TermType.TNUM && r.termType == TermType.TNUM) {
      var tempT = t.asInstanceOf[Num]
      var tempR = r.asInstanceOf[Num]
      tryMatchNumNum(tempT, tempR, m)
    } else if (r.termType == TermType.TREGION) {
      var tempR = r.asInstanceOf[Regions]
      tryMatchTermRegions(t, tempR, m)
    } else if (r.termType == TermType.THOLE) {
      var tempR = r.asInstanceOf[Hole]
      tryMatchTermHole(t, tempR, m)
    } else if (t.termType == TermType.TNIL && r.termType == TermType.TNIL) {
      var tempT = t.asInstanceOf[Nil]
      var tempR = r.asInstanceOf[Nil]
      tryMatchNilTerm(tempT, tempR, m)
    } else {
      m.failure
    }
  }

  // t matches r if the value of t equals the value of r
  //比较两个Num类型的话，就是比较值，相同则返回包含传入match的Set，否则返回空Set
  //这个在原来版本中没有，应该是qq加上的。
  def tryMatchNumNum(t: Num, r: Num, m: Match): Set[Match] = {
    if (GlobalCfg.DEBUG) {
      println("\t" * GlobalCfg.debugident + "[kgq] tryMatchNumNum")
      GlobalCfg.debugident += 1
    }
    if (t.value == r.value) {
      if (m.root == null) {
        m.root = t
      }
      if (GlobalCfg.DEBUG) {
        println("\t" * GlobalCfg.debugident + "[kgq] tryMatchNumNum SUCCESS!")
        GlobalCfg.debugident -= 1
      }
      m.success
      Match.singleton(m);
    } else {
      if (GlobalCfg.DEBUG) {
        println("\t" * GlobalCfg.debugident + "[kgq] tryMatchNumNum FAILED!")
        GlobalCfg.debugident -= 1
      }
      m.failure
    }
  }

  // Term always matches Hole
  def tryMatchTermHole(t: Term, r: Hole, m: Match): Set[Match] = {
    if (GlobalCfg.DEBUG) {
      println("\t" * GlobalCfg.debugident + "[kgq] tryMatchTermHole SUCCESS!")
    }
    m.success
    m.addParam(r.index, t)
    Match.singleton(m)
  }

  // Paraller doesn't match Prefix---No need to change for new format!
  def tryMatchParallerPrefix(t: Paraller, r: Prefix, m: Match): Set[Match] = {
    if (GlobalCfg.DEBUG) println("\t" * GlobalCfg.debugident + "[kgq] tryMatchParallerPrefix FAILED!")
    //println("matcher::tryMatchParallerPrefix " + "matching par: " + t.toString() + " against pref redex: " + r.toString())
    m.failure
  }

  def tryMatchPrefixPrefix(t: Prefix, r: Prefix, m: Match): Set[Match] = {
    val fingerprint: String = "PrefixPrefix" + scala.util.Random.nextInt(100).toString      // add by kgq 20220405。用于打印日志的时候，对于结果进行区分

    if (GlobalCfg.DEBUG){
      println("\t" * GlobalCfg.debugident + "[kgq] tryMatchPrefixPrefix ++++++++++++++++++++ " + fingerprint + t.node.toString() + " against redex: " + r.node.toString())
      GlobalCfg.debugident += 1
    }

    assert(m != null)

    // compare the controls of t1 and t2, return failure if t1's control does not equal with t2's
    /**
     *  @author liangwei
     *  if the RR has node, it will try match the given node's name.
     */
    if (t.node.ctrl.name == r.node.ctrl.name &&
      (r.node.name.equals("") || r.node.name.equals(t.node.name))) {           // 这里的意思是说，首先节点的控制的名称要相同，同时，要么反应物节点没有名字，要么反应物节点和agent节点的名字相同！！！

      // println("matcher::tryMatch " + "Prefix match: " + t.node.ports + " with " + r.node.ports.toString() + " Active: " + t.activeContext)
      if (m.root == null && !(t.parent == null || (t.parent != null && t.parent.activeContext))) {
        if (GlobalCfg.DEBUG){
          GlobalCfg.debugident -= 1
          println("\t" * GlobalCfg.debugident + "[kgq] tryMatchPrefixPrefix -------------------- FAILED-0")
        }
        return m.failure
      } else if (m.root == null) {
        m.root = t
      }

      m.addMatch(r, t)//放入mapping
      m.nodeMap += (r.node -> t.node);

      var tnm: List[Name] = t.node.ports
      var rnm: List[Name] = r.node.ports
      //外层的t.ctrl == r.ctrl 首先比较control是否相同，然后，tnm取得里面的name的值
      //首先判断，如果里面包含的name个数相同再继续比较，否则的话就不同了。
      if (tnm.size != rnm.size) {
        //println("matcher: tnm: " + tnm.size + " rnm: " + rnm.size)
        if (GlobalCfg.DEBUG){
          GlobalCfg.debugident -= 1
          println("\t" * GlobalCfg.debugident + "[kgq] tryMatchPrefixPrefix -------------------- FAILED-1")
        }
        return m.failure
      }

      //这一段是比较port列表是否相同 lbj
      //println("    tryMatchPrefixPrefix: MATCHER LINK START")


      for (i <- 0 until tnm.size) {
        //println("        MATCH: " + i + ": of term: " + t.toString() + ". " + m.toString())
        // Comments by zhaoxin: check the names. check if the ports names are the same
        if (true) {         // 这个true是用来选择端口名的匹配策略的：宽松，紧缩。
          //   if (!Bigraph.isFree(rnm(i))) {
          //println("matcher: !free: " + Bigraph.nameToString(rnm(i)))

          // 为了区分多个实例，modle中link name使用字母+数字来标识，而rule中也用字母+数字，比较的时候只是比较字母
          var linkNameEqual: Boolean = false
          var tempModleLinkName: String = getAlpPartFromStr(tnm(i).name)
          var tempRuleLinkName: String = getAlpPartFromStr(rnm(i).name)
          //println("tempModleLinkName is " + tempModleLinkName)
          //println("tempRuleLinkName is " + tempRuleLinkName)

          //if (tempModleLinkName.equals(tempRuleLinkName)) {
          if (tnm(i).name.equals(rnm(i).name)) {
            linkNameEqual = true
          } /*
          
          if(tempRuleLinkName.size < tempModleLinkName.size){
            if(tempModleLinkName.startsWith(tempRuleLinkName) 
              && tempModleLinkName.charAt(tempRuleLinkName.size).>=('0')
              && tempModleLinkName.charAt(tempRuleLinkName.size).<=('9')) {
            	linkNameEqual = true
            }
          } else if(tempRuleLinkName.size == tempModleLinkName.size){
            linkNameEqual = true
          }
          */

          // if (tnm(i).name != rnm(i).name) {
          if (!linkNameEqual) {
            //println("MATCH FAILED: expected: " + Bigraph.nameToString(rnm(i)) + " got " + Bigraph.nameToString(tnm(i)))
            if (GlobalCfg.DEBUG){
              GlobalCfg.debugident -= 1
              println("\t" * GlobalCfg.debugident + "[kgq] tryMatchPrefixPrefix -------------------- FAILED-2")
            }
            return m.failure
          } else { //这里添加对于link的类型(idle-->idle, edge---->edge, innername/outername--->edge)的处理，暂时先不抽取成为方法
            var nameTypeR: String = rnm(i).nameType
            var nameTypeT: String = tnm(i).nameType
            var nameTypeSet: Set[String] = Set()
            nameTypeSet.add("edge")
            nameTypeSet.add("innername")
            nameTypeSet.add("outername")

            nameTypeSet.add("binding")

            // 上面判断了名字，这里再判断类型： 反应物中的edge, innername, outername 都能和 edge匹配上
//            if ((tempModleLinkName.equals("idle") && tempRuleLinkName.equals("idle")) || (nameTypeSet.contains(nameTypeR) && nameTypeT == "edge")) {
            // modified by kongguanqiao
            // 这里为了增加binding的功能，当前的匹配逻辑：
            // ==== Agent ==== Rule ==== Result
            //      idle        idle      true
            //      edge        edge      true
            //      edge        innername true
            //      edge        outername true
            //      binding     edge      true
            //      binding     binding   true
            //      edge        binding   false
            if ((tempModleLinkName.equals("idle") && tempRuleLinkName.equals("idle")) || (nameTypeSet.contains(nameTypeR) && nameTypeT == "edge") ||
              (nameTypeSet.contains(nameTypeR) && nameTypeT == "binding")) {
              m.captureName(rnm(i), tnm(i))   // 把 redex 到 agent的映射保存到 m 的 names 中去。
              //println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!1 matcher: !free: captured " + rnm(i) + "   " + tnm(i))
            } else {
              if (GlobalCfg.DEBUG){
                GlobalCfg.debugident -= 1
                println("\t" * GlobalCfg.debugident + "[kgq] tryMatchPrefixPrefix -------------------- FAILED-3")
              }
              return m.failure
            }
          }
        } else {
          // This is a symbolic link name, not a literal one
          // We need to look it up in the existing mappings
          // If it exists, tnm[i] must match what it previously matched
          // If not, we bind this name to tnm[i]
          //println("matcher: free: " + rnm(i) + " match object: " + m.toString())

          var nmap: Map[Name, Name] = m.getNames

          if (!nmap.contains(rnm(i))) {
            m.captureName(rnm(i), tnm(i))
//            println("Bigraph nameMap is: " + Bigraph.nameMap)
//            println("matcher: free: new " + Bigraph.nameToString(rnm(i)) + " = " + Bigraph.nameToString(tnm(i)))

          } else {
//              println("matcher: free: old " + rnm(i))

            if (nmap(rnm(i)) != tnm(i)) {
              if (GlobalCfg.DEBUG){
                GlobalCfg.debugident -= 1
                println("\t" * GlobalCfg.debugident + "[kgq] tryMatchPrefixPrefix --------------------- FAILED-4")
              }
              return m.failure
            }
//            println("matcher: free: old matched " + rnm(i))

          }
        }
      }

      //  if  the ctrl of redex is changed, add change info就是这里set了Match的reactNodes和reactNodesMap的值
      // println("[kgq] -------------  " + m.rule.reactNodes)  这里的 m.rule.reactNodes 是反应规则中的反应节点，这个和反应规则的表达式有关，所以不编辑反应规则的表达式，这里就是空的，那么match中的reactNodes和reactNodesMap也是空的，也就是不起作用的。
      if (m.rule.reactNodes.contains(r.node.ctrl.name)) {
        m.reactNodes.add(t.node.name)
        val rns = m.reactNodesMap.getOrElse(r.node.ctrl.name, Set())
        rns.add(t.node.name)
        m.reactNodesMap.put(r.node.ctrl.name, rns)
      }   // 上面这一段，不配置反应规则中的 React: 这里是不起作用的。

      var ret =  tryMatch(t.suffix, r.suffix, m)   // 递归检查后继节点是不是满足
      if (GlobalCfg.DEBUG){
        GlobalCfg.debugident -= 1
        println("\t" * GlobalCfg.debugident + "[kgq] tryMatchPrefixPrefix "+ fingerprint + " SUCCESS!:" + ret.size)
      }
      ret
    } else {
      if (GlobalCfg.DEBUG){
        GlobalCfg.debugident -= 1
        println("\t" * GlobalCfg.debugident + "[kgq] tryMatchPrefixPrefix --------------------- FAILED-5")
      }
      return m.failure
    }
  }

  def getAlpPartFromStr(str: String): String = {     // 截取字符串前面的字母部分，若失败，保留整个字符串
    var resultStr: String = ""
    var digit: Boolean = false
    for (i <- 0 to (str.size - 1); if digit == false) {   // 一旦遇到数字，就停止向后查找
      if (str.charAt(i) >= '0' && str.charAt(i) <= '9') {
        resultStr = str.substring(0, i)
        digit = true
      }
    }
    if (resultStr.size == 0) {
      resultStr += str
    }
    resultStr
  }

  def tryMatchTermReactionRule(t: Term, r: ReactionRule): Set[Match] = {    // 这里传进来的t 在枚举策略里面是偶图agent的根root
    if (GlobalCfg.DEBUG) {
      println("\t" * GlobalCfg.debugident + "[kgq] tryMatchTermReationRule: *+*+*+*+*+*+*+*+*+*+*+*+*+*+*+*+*+*+*+*+*+*" + "rule is: " + r.name)
      println("\t" * GlobalCfg.debugident + "term hash: " + t.hashCode() + "term is: " + t)
      println("\t" * GlobalCfg.debugident + "redex is: " + r.redex)
      GlobalCfg.debugident += 1
    }
    var matches: Set[Match] = Set()

    if (r.redex.termType == TermType.TREGION) {     // 如果反应物的 类型是一个region，就直接和agent 进行匹配（这里是不是默认agent最上层的结构就是region？而且region如果存在的话，只能在最上层？）
      var tempMatch = new Match(r)
      var ret = tryMatch(t, r.redex, tempMatch)
      if (GlobalCfg.DEBUG) {
        println("\t" * GlobalCfg.debugident + "[kgq] tryMatchTermReactionRule DIRECT END!")
        GlobalCfg.debugident -= 1
      }
      return ret
    }

    var p: Term = t.next

    /**
     * Here we use get next, to get each children structure
     * in the agent term. The we use each to match the redex.
     *  kongguanqiao: 我来添加注释：这里t.next使用Term中的队列来实现了 从当前的Term开始的层次遍历。 所以是当前Term的所有子结构都和这个反应物进行匹配
     */
    while (p != null) {
      if (p.parent == null || p.parent.activeContext) {
        var nm: Match = new Match(r)
        var res = tryMatch(p, r.redex, nm)
        if(res.nonEmpty)
          matches = Match.merge(matches, res);
        else{
          nm.clear
          nm=null
        }
      }
      p = t.next
    }
    t.reset
//    var matches1: Set[Match] = Set()
//    if(matches.size>0) matches1.add(matches.head)
    if (GlobalCfg.DEBUG) {
      println("\t" * GlobalCfg.debugident + "[kgq] tryMatchTermReactionRule END!-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-")
      println("\t" * GlobalCfg.debugident + "match result is: " + matches)
      GlobalCfg.debugident -= 1
    }
    matches
//    matches1
  }

  def tryMatchTermRegions(t: Term, r: Regions, m: Match): Set[Match] = {
    if (GlobalCfg.DEBUG) {
      println("\t" * GlobalCfg.debugident + "[kgq] tryMatchTermRegions")
      GlobalCfg.debugident += 1
    }

    if (m.root != null || t.parent != null) {
      if (GlobalCfg.DEBUG) {
        println("\t" * GlobalCfg.debugident + "[kgq] tryMatchTermRegions FAILED!")
        GlobalCfg.debugident -= 1
      }
      return m.failure
    }

    // r->get_children() put each element of T||T into list ch.
    var ch: List[Term] = r.getChildren
    m.root = t;
    m.addMatch(r, t)
    var wm: Match = new WideMatch(m.rule)
    var tempWM: Set[Match] = Match.singleton(wm)
    var ret = regionMatch(t, ch, tempWM)
    if (GlobalCfg.DEBUG) {
      println("[kgq] tryMatchTermRegions END!")
      GlobalCfg.debugident -= 1
    }
    ret
  }

  def regionMatch(t: Term, redexes: List[Term], m: Set[Match]): Set[Match] = {

    if (redexes.size == 0) {
      for (ele <- m.toList) {
        ele.success
      }
      return m
    }

    var redex = redexes.head
    var tempRedexes = redexes.drop(1)
    var result: Set[Match] = Set()
    for (element <- m.toList) {
      // For each existing wide match, we get a new set of matches:
      // bug fix, 当反应规则根区域中含nil时，跳过nil的匹配
      if (redex.termType != TermType.TNIL) {
        var ms: Set[Match] = tryMatchAnywhere(t, redex, element.rule, element)
        if (ms.size == 0) {
          element.failure
        }
        var cp: Set[Match] = crossprod(Match.singleton(element), ms)
        result ++= cp
      } else {
        var nilNode: Nil = new Nil();

        result.add(element)
      }
    }
    return regionMatch(t, tempRedexes, result)
  }

  def tryMatchPrefixParaller(t: Prefix, r: Paraller, m: Match): Set[Match] = {
    if (GlobalCfg.DEBUG) {
      println("\t" * GlobalCfg.debugident + "[kgq] tryMatchPrefixParaller")
      GlobalCfg.debugident += 1
    }
    // This covers cases like redex: a.(b | $0) matching against a.b where $0 = nil
    // should be redex: a.b | $0  ? no! do not act like these

    var ch: Set[Term] = r.getChildren

    if (ch.size > 2) {
      if (GlobalCfg.DEBUG) {
        GlobalCfg.debugident -= 1
        println("\t" * GlobalCfg.debugident + "[kgq] tryMatchPrefixParaller FAILED-0")
      }
      return m.failure
    }

    var hasHole: Hole = null
    var nonHole: Term = null

    for (ite <- ch.toList) {        // 这里的paraller只能一个是Hole，一个是非Hole
      if (ite.termType == TermType.THOLE) {
        if (hasHole != null) {
          //println("@Alert, a paraller has more than one site: " + r)
        } else {
          hasHole = ite.asInstanceOf[Hole]
        }
      } else {
        if (nonHole != null) {
          if (GlobalCfg.DEBUG) {
            GlobalCfg.debugident -= 1
            println("\t" * GlobalCfg.debugident + "[kgq] tryMatchPrefixParaller FAILED-1")
          }
          return m.failure
        }
        nonHole = ite
      }
    }

    /**
     * If like a | $0 | $1 and m is null,
     * it will be matched, if t.parent is paraller.
     *
     * try {
     * if (t.parent != null && t.parent.termType == TermType.TPAR && m.root == null) {
     * return m.failure
     * } } catch {
     * case ex: Exception => ex.printStackTrace()
     * }
     */

    if (nonHole == null || hasHole == null) {
      if (GlobalCfg.DEBUG) {
        println("\t" * GlobalCfg.debugident + "[kgq] tryMatchPrefixParaller FAILED!")
        GlobalCfg.debugident -= 1
      }
      return m.failure
    }
    m.addParam(hasHole.index, new Nil())

    var ret = tryMatch(t, nonHole, m)
    if (GlobalCfg.DEBUG) {
      println("\t" * GlobalCfg.debugident + "[kgq] tryMatchPrefixParaller END!")
      GlobalCfg.debugident -= 1
    }
    ret
  }

  // if the type of r is TNIL or THOLE, return match, otherwise return doesn't match
  def tryMatchNilTerm(t: Nil, r: Term, m: Match): Set[Match] = {
    if (GlobalCfg.DEBUG) {
      println("\t" * GlobalCfg.debugident + "[kgq] tryMatchNilTerm "  + "matching: " + t.toString() + " against redex: " + r.toString())
      GlobalCfg.debugident += 1
    }

    if (r.termType == TermType.TNIL) {
      if (GlobalCfg.DEBUG) {
        GlobalCfg.debugident -= 1
        println("\t" * GlobalCfg.debugident + "[kgq] tryMatchNilTerm match SUCCESS!")
      }
      m.addMatch(r, t)
      m.success
      Match.singleton(m)
    } else if (r.termType == TermType.THOLE) {
      if (GlobalCfg.DEBUG) {
        GlobalCfg.debugident -= 1
        println("\t" * GlobalCfg.debugident + "[kgq] tryMatchNilTerm match FAILED-0")
      }
      m.success
      var tempR = r.asInstanceOf[Hole]
      m.addParam(tempR.index, t)
      Match.singleton(m)
    } else {
      if (GlobalCfg.DEBUG) {
        GlobalCfg.debugident -= 1
        println("\t" * GlobalCfg.debugident + "[kgq] tryMatchNilTerm match FAILED-1")
      }
      m.failure
    }
  }

  def tryMatchAnywhere(t: Term, r: Term, rl: ReactionRule, m: Match): Set[Match] = {
    if (GlobalCfg.DEBUG) {
      println("\t" * GlobalCfg.debugident + "[kgq] tryMatchAnywhere")
      GlobalCfg.debugident += 1
    }

    var matches: Set[Match] = Set()
    var p: Term = t.next

    while (p != null) {
//      if (GlobalCfg.DEBUG) {
//        println("p: " + p.toString() + ": ")
//      }
      if (p.parent == null || p.parent.activeContext) {
//        if (GlobalCfg.DEBUG) {
//          println("active")
//        }
        var nm: Match = new Match(rl)
        nm.incorporate(m)
        if (m.parent != null) {
          nm.incorporate(m.parent);
        }
        matches = Match.merge(matches, tryMatch(p, r, nm))
      } else {
//        if (GlobalCfg.DEBUG)
//          println("passive")
      }

      p = t.next
    }
    t.reset
    if (GlobalCfg.DEBUG) {
      println("\t" * GlobalCfg.debugident + "[kgq] tryMatchAnywhere END!")
      GlobalCfg.debugident -= 1
    }
    return matches
  }

  def isCompat(m1: Match, m2: Match): Boolean = {
    if (m2.hasFailed) {
      return false
    }

    // 判断两个match 的 names是否兼容
    var m1n: Map[Name, Name] = m1.getNames
    var m2n: Map[Name, Name] = m2.getNames
    for ((key, value) <- m1n) {
      if (m2n.contains(key) && m2n(key) != value) {
        println("!!!!!!!!!!!!!!!!!! 名字不兼容 !!!!!!!!!!!!!!!!")
        return false
      }
    }

    // 判断两个match 的 term 映射 是否兼容
    var mapping = m1.mapping
    for ((key, value) <- mapping) {
      if (m2.getMapping(key) != null && m2.getMapping(key) != value) {
        println("!!!!!!!!!!!!!!!!!!!! term 映射不兼容 !!!!!!!!!!!!!!!!!!!")
        return false
      }
    }
    return true
  }

  def noOverlap(prev: List[Match], cand: Match): Boolean = {
    for (ite <- prev) {
      if (ite.root.overlap(cand.root) || cand.root.overlap(ite.root)) {
        return false
      }
    }
    return true
  }

  def crossprod(m1: Set[Match], m2: Set[Match]): Set[Match] = {
    // We have two sets {a,b,c} and {d,e,f}:
    // We want to construct:
    // {a.incorporate(d), a.incorporate(e), a.incorporate(f), b.inc...}
//    if (GlobalCfg.DEBUG) {
//      println("crossprod(): " + m1.size + " with " + m2.size)
//    }

    var res: Set[Match] = Set()

    for (item1 <- m1.toList) {
      for (item2 <- m2.toList) {
        var item1Sbumatches = noOverlap(item1.asInstanceOf[WideMatch].submatches, item2)
        if (item1Sbumatches) {
          var nm: WideMatch = item1.asInstanceOf[WideMatch].clone
          nm.addSubMatch(item2)
          res.add(nm)
        }
      }
    }
    m1.clear()
    m2.clear()
//    if (GlobalCfg.DEBUG) {
//      println("crossprod(): res: " + res.size)
//    }
    return res
  }

  /*  def getSubsOfParaller(children : Set[Term]) : Set[Term] = {
    var result = Set[Term]()
    var inner = Set[Term]()
    for(child <- children){
      if(child.termType != TermType.TPAR){
        result += child
      } else {
        inner = getSubsOfParaller(child.asInstanceOf[Paraller].getChildren)
        result ++= inner
      }
    }
    result
  }*/

  def tryMatchParallerPareller(t: Paraller, r: Paraller, m: Match): Set[Match] = {
    if (GlobalCfg.DEBUG) {
      println("\t" * GlobalCfg.debugident + "[kgq] tryMatchParallerParaller ++++++++++++++++++++" + "matching: " + t.toString() + " against redex: " + r.toString())
      GlobalCfg.debugident += 1
    }

    // Term.getChildren is modified the same as C++
    var tch = t.getChildren //getSubsOfParaller(t.getChildren)
    var rch = r.getChildren //getSubsOfParaller(r.getChildren)

    var matches = Map[Term, Set[Match]]()
    // Is there a top level hole?  e.g. A | B | $0
    // Something like A.$0 | B | C does not count.
    // This will be the hole term itself, or NULL.
    var hasHole: Hole = null
    var isBreak = false

    /**
     * Filter holes and alert if has more than one
     */
    if (GlobalCfg.DEBUG) {
      println("\t" * GlobalCfg.debugident + "[kgq] ParallerParaller before filter redex is: " + rch)
      println("\t" * GlobalCfg.debugident + "[kgq] ParallerParaller before filter agent is: " + tch)
    }

    for (ite <- rch.toList) {
      if (ite.termType == TermType.THOLE) {
        if (hasHole != null) {
          //println("@Alert, a paraller has more than one site: " + r)
        } else
          hasHole = ite.asInstanceOf[Hole]
        rch -= ite
      }
    }
    /**
     * If has hole and the left is a prefix,
     * we should leave it to the tryPrefixParaller
     *
     * if (hasHole != null && rch.size == 1 && m.root == null) {
     * return m.failure
     * }
     */

    if (hasHole == null && rch.size > tch.size) {
      if (GlobalCfg.DEBUG) {
        GlobalCfg.debugident -= 1
        println("\t" * GlobalCfg.debugident + "[kgq] tryMatchParallerParaller FAILED-0")
      }
      return m.failure
    }

    /**
     * Bug Fix
     * @author liangwei
     * if the match paraller and paraller not the first time in,
     * t: A | B | C should not match r: A | B
     * But if it is the first time in,
     * t: A | B | C should match r: A | B
     * Here if m.root is not null, than it is not the first time in.
     */
    if (hasHole == null && m.root != null && rch.size != tch.size) {
      if (GlobalCfg.DEBUG) {
        GlobalCfg.debugident -= 1
        println("\t" * GlobalCfg.debugident + "[kgq] tryMatchParallerParaller FAILED!1")
      }
      return m.failure
    }

    if (m.root == null && !(t.parent == null || (t.parent != null && t.parent.activeContext))) {
      if (GlobalCfg.DEBUG) {
        GlobalCfg.debugident -= 1
        println("\t" * GlobalCfg.debugident + "[kgq] tryMatchParallerParaller FAILED!2")
      }
      return m.failure // We can't start a new match here.
    } else if (m.root == null) {
      m.root = t // We can start a new match here!
    }

    m.addMatch(r, t)

    if (GlobalCfg.DEBUG) {
      println("\t" * GlobalCfg.debugident + "[kgq] ParallerParaller redex is: " + rch)
      println("\t" * GlobalCfg.debugident + "[kgq] ParallerParaller agent is: " + tch)
    }

    for (iteR <- rch.toList) {
      var mcount = 0
      for (iteT <- tch.toList) {
        var mn: Match = m.clone()
        //var mn: Match = m
        var crossmatch: Set[Match] = tryMatch(iteT, iteR, mn)
        if (crossmatch.size > 0) {
          if (GlobalCfg.DEBUG) {
            println("\t" * GlobalCfg.debugident + "[kgq] 发现匹配，记录！")
          }
          mcount += 1;
          if (matches.contains(iteR)) {
            matches(iteR) ++= crossmatch
          } else {
            matches(iteR) = crossmatch
          }
//          if (GlobalCfg.DEBUG) {
//            println(": matches " + crossmatch.size + " times")
//          }
        } else {
          if (GlobalCfg.DEBUG) {
            println("\t" * GlobalCfg.debugident + "[kgq] 没有匹配:\niteT:"+iteT+"\nIteR:"+iteR)
          }
          mn.clear
          mn=null
        }
      }
      // We found nothing matching this part of the redex, so fail now.
      if (mcount == 0) {
        if (GlobalCfg.DEBUG) {
          GlobalCfg.debugident -= 1
          println("\t" * GlobalCfg.debugident + "[kgq] tryMatchParallerParaller FAILED!3")
        }
        return m.failure
      }
    }

//    if (GlobalCfg.DEBUG) {
//      println("PARALLEL MATCH:")
//      for ((key, value) <- matches) {
//        println("REDEX PART: " + key.toString())
//        println("MATCH SET: ")
//        for (ite <- value.toList) {
//          println(ite.toString())
//        }
//      }
//    }

    //// by tanch matches 传到cand时 ctrlMap信息丢失

    var cand: Set[Match] = Set()
    for (ite <- rch.toList) {
      if (cand.size == 0) {
        if (ite != rch.toList.head) {  //  [kgq] 感觉这个判断是不是有点多余？
          if (GlobalCfg.DEBUG) {
            GlobalCfg.debugident -= 1
            println("\t" * GlobalCfg.debugident + "[kgq] tryMatchParallerParaller FAILED!0")
          }
          return m.failure
        }
        //cand = matches(ite)
        matches(ite).map(x => cand.add(x))
      } else {                //  每次加入一个term的match，然后判断它和当前所有match的兼容关系，保留下来兼容的。
        var ns: Set[Match] = matches(ite)
        var newcand: Set[Match] = Set()
        for (iteJ <- cand.toList) {
          for (iteK <- ns.toList) {
            if (isCompat(iteJ, iteK)) {
              var mm: Match = iteJ.clone()
              mm.root = m.root
              mm.incorporate(iteK)
              newcand += mm
            }
          }
        }
        cand = newcand
      }
    }

    if (cand.size == 0) {
      if (GlobalCfg.DEBUG) {
        GlobalCfg.debugident -= 1
        println("\t" * GlobalCfg.debugident + "[kgq] tryMatchParallerParaller FAILED!4")
        println("\t" * GlobalCfg.debugident + "[kgq] tryMatchParallerParaller matches is: " + matches)
        println("\t" * GlobalCfg.debugident + "[kgq] tryMatchParallerParaller cand is: " + cand)
        println("\t" * GlobalCfg.debugident + "[kgq] tryMatchParallerParaller namemap is: " + Bigraph.nameMap)
      }
      return m.failure
    }

    // OK, now we have to go through and populate the parameter
    // with everything that was not matched.
    if (hasHole != null || t.parent == null) {
      if (t.parent == null && hasHole == null) {
        hasHole = new Hole(999999);
      }
      for (iteI <- cand.toList) {
        var ctx: Set[Term] = Set()
        for (iteJ <- tch.toList) {
          if (iteI.getMapping(iteJ) == null) {
            ctx += iteJ
          }
        }
        if (ctx.size == 0) {
          iteI.addParam(hasHole.index, new Nil())
        } else if (ctx.size == 1) {
          iteI.addParam(hasHole.index, ctx.head)
        } else {
          //iteI.addParam(hasHole.index, new Paraller(ctx.head, ctx.tail.head))
          iteI.addParam(hasHole.index, Paraller.constuctParaller(ctx))
        }
      }
    }

//    if (GlobalCfg.DEBUG) {
//      // println("PARALLEL MATCH: RESULT:")
//      for (ite <- cand.toList) {
//        println(ite.toString())
//      }
//    }
    if (GlobalCfg.DEBUG) {
      GlobalCfg.debugident -= 1
      println("\t" * GlobalCfg.debugident + "[kgq] tryMatchParallerParaller SUCCESS!")
    }
    return cand
  }
}

object testMatcher {
  def main(args: Array[String]) {
    println("--------------------Test crossprod function:--------------- ")
    var redexTest: Term = null //TermParser.apply("Pharmacy.(Patient[patient_prescription:edge,patient_bill_payed:edge,isDecoction:edge].IsDecoction[isDecoction:edge,value_is:edge,leftValue:edge].Value[value_is:edge] | Pill[idle] | $0) | Equal[leftValue:edge,rightValue:edge] | False[rightValue:edge] | $1");
    var reactumTest: Term = null //TermParser.apply("ChargingRoom.($0 | Patient[Patient_pill:edge,idle,isDecoction:edge].$2 | Pill[Patient_pill:edge]) | $1");
    var ruleA: ReactionRule = null //new ReactionRule(redexTest, reactumTest)
    var node: Term = null //TermParser.apply("a:Hospital.(Pill[patient_pill:edge].nil|b:Patient[patient_pill:edge,idle,isDecoction:edge].nil|c:ConsultingRoom.f:Computer[connected:edge].Prescription[patient_prescription:edge].nil|d:ChargingRoom.g:Computer[connected:edge].Bill[patient_bill_payed:edge].nil|e:Pharmacy.h:Computer[connected:edge].nil)")
    var matchA: Match = null //new Match(ruleA)

    //Matcher.tryMatchPrefixParaller(node.asInstanceOf[Prefix], redexTest.asInstanceOf[Paraller], matchA);
    //println(result)
    println("hole " + TermType.THOLE)
    println("nil " + TermType.TNIL)
    println("num " + TermType.TNUM)
    println("prefix " + TermType.TPREF)
    println("paraller " + TermType.TPAR)
    println("region " + TermType.TREGION)

    redexTest = TermParser.apply("a.b")
    reactumTest = TermParser.apply("a.c | b")
    node = TermParser.apply("a.b")
    val b: Bigraph = new Bigraph;
    b.root = node
    ruleA = new ReactionRule(redexTest, reactumTest)
    matchA = new Match(ruleA)
    var result = Matcher.tryMatchTermReactionRule(node, ruleA);
    println(result.size)
    println(result)
    result.foreach(f => {
      println(f.hasSucceeded)
      var nb = b.applyMatch(f)
      print(nb.root.toString())
    })

    /*
    var matchB = new Match(ruleA)
    var matchC = new WideMatch(ruleA)

    var nameA1: Name = new Name("nameA1", "innername")
    var nameA2: Name = new Name("nameA2", "innername")
    var nameA3: Name = new Name("nameA3", "innername")
    var nameA4: Name = new Name("nameA4", "innername")

    var nameB1: Name = new Name("nameB1", "innername")
    var nameB2: Name = new Name("nameB2", "innername")
    var nameB3: Name = new Name("nameB3", "innername")
    var nameB4: Name = new Name("nameB4", "innername")

    matchA.captureName(nameA1, nameA2)
    matchA.captureName(nameA3, nameA4)

    matchB.captureName(nameB1, nameB2)
    matchB.captureName(nameB3, nameB4)

    println("The value of matchA is: " + matchA.toString())
    println("The value of matchB is: " + matchB.toString())

    var matchTestSet: Set[Match] = Set(matchA, matchB)
    println("The set is: " + matchTestSet.toString())

    var crossResultSet = Matcher.crossprod(Match.singleton(matchC), matchTestSet)

    println("The crossResultSet is: " + crossResultSet.toString())

    var resListBuffer = new scala.collection.mutable.ListBuffer[Match]()
    resListBuffer += matchA
    println("The resListBuffer is: " + resListBuffer.toString())
    var set2Convert = scala.collection.mutable.Set(resListBuffer: _*)
    println("The set to set2Convert is: " + set2Convert.toString())
    
    var str : String = "abc12"
    var str2 : String = "abc"
//    println(str.startsWith(str2))
//    println(str.charAt(str2.size).<=('9') && str.charAt(str2.size).>=('0'))
    
    println("abc12 " + Matcher.getAlpPartFromStr(str))
    println("abc" + Matcher.getAlpPartFromStr(str2))
    */

  }
}