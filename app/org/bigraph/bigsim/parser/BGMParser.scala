package org.bigraph.bigsim.parser

import java.io.File
import java.util

import org.bigraph.bigsim.BRS.InstantiationMap
import org.bigraph.bigsim.bigraphoperation.BigraphOperation
import org.bigraph.bigsim.data.Data
import org.bigraph.bigsim.model.component.{OuterName, Signature}
import org.bigraph.bigsim.model.{Specification, _}
import org.bigraph.bigsim.simulator.StochasticSimulator
import org.bigraph.bigsim.utils.{DebugPrinter, GlobalCfg}
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.mutable
import scala.collection.mutable.Set
import scala.util.parsing.combinator.RegexParsers

abstract class BGMTerm {
}

//case class BGMControl(n: String, arity: Int, active: Boolean) extends BGMTerm
case class BGMControl(n: String, arity: Int, active: Boolean, binding: Boolean) extends BGMTerm //add binding control
case class BGMNode(n: String, active: Boolean, ctrl: BGMControl) extends BGMTerm

case class BGMName(n: String, isouter: Boolean) extends BGMTerm

//case class BGMRule(n: String, m: String, redex: String, reactum: String, exp: String) extends BGMTerm  //add reactionrule belongs to which process
case class BGMRule(n: String, redex: String, reactum: String, exp: String, eta: InstantiationMap = null) extends BGMTerm

case class BGMAgent(n: String) extends BGMTerm

case class BGMProp(n: String, p: String) extends BGMTerm

case class BGMNil() extends BGMTerm

//add by lbj
case class BGMPattern(n: String) extends BGMTerm

case class BGMError(n: String) extends BGMTerm

case class BGMTracking(n: String, t: String) extends BGMTerm

case class BGMBinding(n: String, arity: Int) extends BGMTerm //  bindingConstraint
//case class BGMPort(n: String,cn:String) extends BGMTerm
case class BGMPlaceSort(n: String, cns: String) extends BGMTerm

case class BGMLinkSort(n: String, pns: String) extends BGMTerm

case class BGMPlaceSortConstraint(n: String) extends BGMTerm

case class BGMLinkSortConstraint(n: String) extends BGMTerm

//add by wm （王敏）
case class BGMFormula(n: String) extends BGMTerm

case class BGMProposition(n: String, content: String) extends BGMTerm

// z
case class BGMProperty(n: String) extends BGMTerm

//add by kgq
case class BGMCTLSpec(n: String) extends BGMTerm

case class BGMCTLProp(n: String, content: String, exp: String) extends BGMTerm

case class BGMLTLSpec(n: String) extends BGMTerm

case class BGMBaseAgent(n: String, term: String) extends BGMTerm // baseAgent 用来定义批量节点的格式
case class BGMOpBigraph(n: String, term: String) extends BGMTerm // add by kgq 20220308 for bigraph operation
case class BGMBigraphFormula(n: String) extends BGMTerm // add by kgq 20220308 for bigraph operation


object BGMTerm {
  var bgm = "# Controls\n%active Greater : 2;\n%active Less : 2;\n%active GreaterOrEqual : 2;\n%active LessOrEqual : 2;\n%active Equal : 2;\n%active NotEqual : 2;\n%active Exist : 1;\n%active InstanceOf : 2;\n%active ConsensusNode : 1;\n%active BuyerNode : 1;\n%active SellerNode : 1;\n%active ReputationModule : 0;\n%active SmartContract : 1;\n%active TrMsg : 0;\n%active Money : 0;\n%active Evaluation : 1;\n%active ConsensuModule : 0;\n%active DealModule : 0;\n%active TrPool : 0;\n%active Block : 0;\n%active DB : 0;\n%active AckMsg : 0;\n%active RefuseMsg : 0;\n%active TrAck : 0;\n%active TrRefuse : 0;\n%active LocalRepu : 0;\n%active GlobalRepu : 0;\n%active Time : 0;\n%active leaderFlag : 0;\n%active age : 0;\n%active SystemClock : 0;\n%active LocalReputationSmartContract : 1;\n%active GlobalReputationSmartContract : 1;\n\n# Rules\n%rule r_calGlobalRepu2 a:BuyerNode[a:edge] | b:SellerNode[idle] | c:ConsensusNode[a:edge].(d:ConsensuModule | e:TrPool | f:ReputationModule.(h:Evaluation[idle] | i:LocalReputationSmartContract[idle] | j:GlobalReputationSmartContract[idle]) | g:DB) -> a:BuyerNode[a:edge] | b:SellerNode[idle] | c:ConsensusNode[idle].(d:ConsensuModule | e:TrPool | f:ReputationModule.(h:Evaluation[b:edge] | i:LocalReputationSmartContract[idle] | j:GlobalReputationSmartContract[b:edge] | k:GlobalRepu) | g:DB){timeDistribution:};\n\n%rule r_calLocalRepu2 a:BuyerNode[a:edge] | b:SellerNode[idle] | c:ConsensusNode[a:edge].(d:ConsensuModule | e:TrPool | f:ReputationModule.(h:Evaluation[idle] | i:LocalReputationSmartContract[idle] | j:GlobalReputationSmartContract[idle]) | g:DB) -> a:BuyerNode[idle] | b:SellerNode[idle] | c:ConsensusNode[a:edge].(d:ConsensuModule | e:TrPool | f:ReputationModule.(h:Evaluation[b:edge] | i:LocalReputationSmartContract[b:edge] | j:GlobalReputationSmartContract[idle] | k:LocalRepu) | g:DB){timeDistribution:};\n\n%rule r_consensusFail a:BuyerNode[idle] | b:SellerNode[idle] | c:ConsensusNode[idle].(d:ConsensuModule.h:Money | e:TrPool | f:ReputationModule | g:DB) -> a:BuyerNode[idle] | b:SellerNode[idle] | c:ConsensusNode[idle].(d:ConsensuModule | e:TrPool | f:ReputationModule | g:DB){timeDistribution:};\n\n%rule r_consensusSucc2 a:BuyerNode[idle] | b:SellerNode[idle] | c:ConsensusNode[idle].(d:ConsensuModule.h:Money | e:TrPool | f:ReputationModule | g:DB) -> a:BuyerNode[idle] | b:SellerNode[idle] | c:ConsensusNode[idle].(d:ConsensuModule | e:TrPool | f:ReputationModule | g:DB.h:Money){timeDistribution:};\n\n%rule r_generateEval2 a:BuyerNode[idle].h:Money | b:SellerNode[idle] | c:ConsensusNode[idle].(d:ConsensuModule | e:TrPool | f:ReputationModule | g:DB) -> a:BuyerNode[idle].h:Evaluation[idle] | b:SellerNode[idle] | c:ConsensusNode[idle].(d:ConsensuModule | e:TrPool | f:ReputationModule | g:DB){timeDistribution:};\n\n%rule r_globalRepuConsens2 a:BuyerNode[idle] | b:SellerNode[idle] | c:ConsensusNode[idle].(d:ConsensuModule | e:TrPool | f:ReputationModule.(h:Evaluation[a:edge] | i:LocalReputationSmartContract[idle] | j:GlobalReputationSmartContract[a:edge] | k:GlobalRepu) | g:DB) -> a:BuyerNode[idle] | b:SellerNode[idle] | c:ConsensusNode[idle].(d:ConsensuModule.k:GlobalRepu | e:TrPool | f:ReputationModule.(i:LocalReputationSmartContract[idle] | j:GlobalReputationSmartContract[idle]) | g:DB){timeDistribution:};\n\n%rule r_globalRepuSave2 a:BuyerNode[idle] | b:SellerNode[idle] | c:ConsensusNode[idle].(d:ConsensuModule.k:GlobalRepu | e:TrPool | f:ReputationModule.(i:LocalReputationSmartContract[idle] | j:GlobalReputationSmartContract[idle]) | g:DB) -> a:BuyerNode[idle] | b:SellerNode[idle] | c:ConsensusNode[idle].(d:ConsensuModule | e:TrPool | f:ReputationModule.(i:LocalReputationSmartContract[idle] | j:GlobalReputationSmartContract[idle]) | g:DB.k:GlobalRepu){timeDistribution:};\n\n%rule r_informBuyer2 a:BuyerNode[idle] | b:SellerNode[a:edge] | c:ConsensusNode[a:edge].(d:ConsensuModule | e:TrPool.h:Money | f:ReputationModule | g:DB) -> a:BuyerNode[idle].h:Money | b:SellerNode[idle] | c:ConsensusNode[idle].(d:ConsensuModule | e:TrPool | f:ReputationModule | g:DB){timeDistribution:};\n\n%rule r_localRepuConsen2 a:BuyerNode[idle] | b:SellerNode[idle] | c:ConsensusNode[idle].(d:ConsensuModule | e:TrPool | f:ReputationModule.(h:Evaluation[a:edge] | i:LocalReputationSmartContract[a:edge] | j:GlobalReputationSmartContract[idle] | k:LocalRepu) | g:DB) -> a:BuyerNode[idle] | b:SellerNode[idle] | c:ConsensusNode[idle].(d:ConsensuModule.k:LocalRepu | e:TrPool | f:ReputationModule.(i:LocalReputationSmartContract[a:edge] | j:GlobalReputationSmartContract[idle]) | g:DB){timeDistribution:};\n\n%rule r_localRepuSave2 a:BuyerNode[idle] | b:SellerNode[idle] | c:ConsensusNode[idle].(d:ConsensuModule.k:LocalRepu | e:TrPool | f:ReputationModule.(i:LocalReputationSmartContract[idle] | j:GlobalReputationSmartContract[idle]) | g:DB) -> a:BuyerNode[idle] | b:SellerNode[idle] | c:ConsensusNode[idle].(d:ConsensuModule.k:LocalRepu | e:TrPool | f:ReputationModule.(i:LocalReputationSmartContract[idle] | j:GlobalReputationSmartContract[idle]) | g:DB){timeDistribution:};\n\n%rule r_sellerAgree2 a:BuyerNode[idle] | b:SellerNode[a:edge].h:Money | c:ConsensusNode[a:edge].(d:ConsensuModule | e:TrPool | f:ReputationModule | g:DB) -> a:BuyerNode[idle] | b:SellerNode[a:edge] | c:ConsensusNode[a:edge].(d:ConsensuModule | e:TrPool.h:Money | f:ReputationModule | g:DB){timeDistribution:};\n\n%rule r_sellerDisAgree2 a:BuyerNode[idle] | b:SellerNode[a:edge].h:Money | c:ConsensusNode[a:edge].(d:ConsensuModule | e:TrPool | f:ReputationModule | g:DB) -> a:BuyerNode[idle] | b:SellerNode[idle] | c:ConsensusNode[a:edge].(d:ConsensuModule | e:TrPool | f:ReputationModule | g:DB){timeDistribution:};\n\n%rule r_sendEval2 a:BuyerNode[idle].h:Evaluation[idle] | b:SellerNode[idle] | c:ConsensusNode[idle].(d:ConsensuModule | e:TrPool | f:ReputationModule | g:DB) -> a:BuyerNode[a:edge] | b:SellerNode[idle] | c:ConsensusNode[a:edge].(d:ConsensuModule | e:TrPool | f:ReputationModule.(h:Evaluation[idle] | i:LocalReputationSmartContract[idle] | j:GlobalReputationSmartContract[idle]) | g:DB){timeDistribution:};\n\n%rule r_sendToSeller2 a:BuyerNode[idle] | b:SellerNode[idle] | c:ConsensusNode[idle].(d:ConsensuModule.h:Money | e:TrPool | f:ReputationModule | g:DB) -> a:BuyerNode[idle] | b:SellerNode[idle] | c:ConsensusNode[idle].(d:ConsensuModule.h:Money | e:TrPool | f:ReputationModule | g:DB){timeDistribution:};\n\n%rule r_sendTr2 a:BuyerNode[idle].h:Money | b:SellerNode[idle] | c:ConsensusNode[idle].(d:ConsensuModule | e:TrPool | f:ReputationModule | g:DB) -> a:BuyerNode[a:edge] | b:SellerNode[idle] | c:ConsensusNode[a:edge].(d:ConsensuModule | e:TrPool.h:Money | f:ReputationModule | g:DB){timeDistribution:};\n\n%rule r_sendTran2 a:BuyerNode[idle] | b:SellerNode[idle] | c:ConsensusNode[idle].(d:ConsensuModule | e:TrPool | f:ReputationModule | g:DB.h:Money) -> a:BuyerNode[idle] | b:SellerNode[a:edge].h:Money | c:ConsensusNode[a:edge].(d:ConsensuModule | e:TrPool | f:ReputationModule | g:DB){timeDistribution:};\n\n\n\n# Model\n%agent a:BuyerNode[idle] | b:SellerNode[idle] | c:ConsensusNode[idle].(d:ConsensuModule | e:TrPool | f:ReputationModule | g:DB.h:Money);\n\n\n\n\n\n\n\n\n\n#SortingLogic\n\n\n# Go!\n%check;"

  var logger: Logger = LoggerFactory.getLogger(this.getClass)

  def
  toSpecification(t: List[BGMTerm]): Specification = {
    val spec: Specification = new Specification();
    //BGMFormula
    t.filter(_ match {
      case BGMFormula(_) => true
      case _ => false
    }).map(x => {
      val f: BGMFormula = x.asInstanceOf[BGMFormula];
      spec.formula = f.n
    });
    //BGMProposition
    t.filter(_ match {
      case BGMProposition(_, _) => true
      case _ => false
    }).map(x => {
      val p: BGMProposition = x.asInstanceOf[BGMProposition];
      spec.proposition += (p.n -> proToBigraph(p.content));
      logger.debug("p.n=" + p.n + "p.content" + p.content); //lry
    });
    return spec;
  }

  def proToBigraph(str: String): Bigraph = {
    var b: Bigraph = new Bigraph(1);
    b.root = TermParser.apply(str);
    return b;
  }

  def ToBigraph(t: List[BGMTerm]): Bigraph = {
    var bgmTerm = BGMParser.parseFromString(bgm)
    var bigraph = BGMTerm.toBigraph(bgmTerm)
    bigraph
  }

  def toBigraph(t: List[BGMTerm]): Bigraph = {

    // BGMControl
    val controlList: List[BGMControl] = t.filter(_ match {
      case BGMControl(_, _, _, _) => true
      case _ => false
    }).map(_.asInstanceOf[BGMControl])
    val ctrls: util.List[component.Control] = new util.ArrayList[component.Control]()
    controlList.map(x => {
      ctrls.add(new component.Control(x.n, x.active, x.arity))
      Bigraph.addControl(x.n, x.arity, x.active, x.binding)
    })
    var b: Bigraph = null
    if (!GlobalCfg.sharedMode) b = new Bigraph(1)
    else b = new SharedBigraph(new Signature(ctrls))
    b.bigSignature = new Signature(ctrls)
    // BGMName
    t.filter(_ match {
      case BGMName(_, _) => true
      case _ => false
    }).map(x => {
      val xo: BGMName = x.asInstanceOf[BGMName];
      if (xo.isouter) {
        b.addOuterName(new Name(xo.n, "outername"))
      } else {
        b.addInnerName(new Name(xo.n, "innername"))
      }

    });

    val hasBinding = t.filter(_ match {
      case BGMBinding(_, _) => true
      case _ => false
    }).size > 0;

    // BGMBinding  modified by kgq
    if (hasBinding) {
      GlobalCfg.checkBinding = true
      val bindingList: List[BGMBinding] = t.filter(_ match {
        case BGMBinding(_, _) => true
        case _ => false
      }).map(_.asInstanceOf[BGMBinding])
      bindingList.map(x => Bigraph.addBinding(x.n, x.arity))
    }

    // added by kgq， 批量节点创建功能，默认不打开
    val hasBaseAgent = t.filter(_ match {
      case BGMBaseAgent(_, _) => true
      case _ => false
    }).size > 0
    if (hasBaseAgent) {
      GlobalCfg.batchCreate = true
      val baseAgentList: List[BGMBaseAgent] = t.filter(_ match {
        case BGMBaseAgent(_, _) => true
        case _ => false
      }).map(_.asInstanceOf[BGMBaseAgent])
      baseAgentList.map(x => Bigraph.addBaseAgent(x.n, x.term))
    }

    // 只读取一个model
    val agentList = t.filter(_ match {
      case BGMAgent(_) => true
      case _ => false
    });
    if (agentList.size > 0) {
      val agent = agentList.head.asInstanceOf[BGMAgent]
      b.root = TermParser.apply(agent.n)
    }

    // add by kgq 20220312 增加偶图操作功能
    val hasOpBigraph = t.filter(_ match {
      case BGMOpBigraph(_, _) => true
      case _ => false
    }).size > 0;
    if (hasOpBigraph) {
      val opbigraphs = t.filter(_ match {
        case BGMOpBigraph(_, _) => true
        case _ => false
      }).map(_.asInstanceOf[BGMOpBigraph])
      opbigraphs.map(x => BigraphOperation.addOpBigraph(x.n, x.term))
    }
    val hasBigraphFormula = t.filter(_ match {
      case BGMBigraphFormula(_) => true
      case _ => false
    }).size > 0
    if (hasBigraphFormula) {
      val bigraphformula = t.filter(_ match {
        case BGMBigraphFormula(_) => true
        case _ => false
      }).head.asInstanceOf[BGMBigraphFormula]
      val startTime = System.nanoTime
      val rawroot = BigraphFormulaParser.apply(bigraphformula.n)
      b.root = BigraphOperation.trimnil(rawroot) // modified by kgq 20220407 修剪并置的nil
      val endTime = System.nanoTime
      val delta = endTime - startTime
      logger.debug("偶图操作用时为：" + delta / 1000000d)
      logger.debug("偶图操作结果为：" + b.root)
    }

    Bigraph.modelNames = b.root.getAllNames;

    // 处理反应规则，从列表中，依据BGMRule过滤
    t.filter(_ match {
      //case BGMRule(_,_, _, _, _) => true
      case BGMRule(_, _, _, _, _) => true
      case _ => false
    }).map(rr => {
      val rrp = rr.asInstanceOf[BGMRule]

      //println("resolve bgm rule to term redex: " + rrp.redex)
      val redex = TermParser.apply(rrp.redex)
      b.rex.add(redex)
      //println("resolve bgm rule to term reactum: " + rrp.reactum)
      val reactum = TermParser.apply(rrp.reactum)
      b.rex.add(reactum)
      val rule: ReactionRule = new ReactionRule(rrp.n, redex, reactum, rrp.exp, b.bigSignature)
      if (rrp.eta != null) rule.eta = rrp.eta
      b.rules.add(rule); //toBigraph时把反应规则解析到Bigraph
    })

    // BGMProperty
    // Property是干什么的 好像和%properties相对应
    t.filter(_ match {
      case BGMProperty(_) => true
      case _ => false
    }).map(pro => {

      //  val bgmProperty = pro.asInstanceOf[BGMProperty]

      println("===========================")
      println("============properties=====")
      println(pro.toString)
      var s = pro.toString
      var propertyBody = s.substring(12)
      var leftParenthesis = propertyBody.indexOf('(')
      var rightParenthesis = propertyBody.indexOf(')')

      var name = propertyBody.substring(0, leftParenthesis)
      var pp = propertyBody.substring(leftParenthesis + 1, rightParenthesis)
      var sarray = pp.split(' ')
      for (a <- sarray) {
        var ar = a.split(':')
        if (!b.hasSetEx) {
          var arr = ar(1).split(',')
          println("ar: " + ar(1));
          b.experimentTurns = arr.length;
          println("reaction turns: " + b.experimentTurns)
          b.hasSetEx = true
        }
        if (b.properties.contains(name)) {
          var mapp = b.properties(name)
          mapp += (ar(0) -> ar(1))

        } else {
          var mm: mutable.Map[String, String] = mutable.Map[String, String]()
          mm += (ar(0) -> ar(1))
          b.properties += (name -> mm)
        }
      }
      var i = 0
      for (i <- 1 to b.experimentTurns) {
        var map1: mutable.Map[String, mutable.Map[String, String]] = mutable.Map()
        b.experimentParameters = map1 +: b.experimentParameters
      }

      for (name <- b.properties.keys) {
        var map = b.properties(name)
        for (propertyName <- map.keys) {
          var props = map(propertyName)
          var s = props.split(",")
          for (i <- 0 to b.experimentTurns - 1) {
            var expe1 = b.experimentParameters(i)
            if (expe1.contains(name)) {
              expe1(name) += (propertyName -> s(i))
            } else {
              var m: mutable.Map[String, String] = mutable.Map()
              m += (propertyName -> s(i))
              expe1 += (name -> m)
            }
          }
        }
      }
      println(b.properties)
      println(b.experimentParameters(0))
      println(b.experimentParameters(1))
      println(b.experimentParameters(2))

      println("===========================")

      //   b.rules.add(new ReactionRule(rrp.n, redex, reactum, rrp.exp)); //toBigraph时把反应规则解析到Bigraph

    })

    // 目前bigMC中没有声明的name全默认为inner name,只有%outer 说明的才是outer name，这里作一次修正
    Bigraph.nameMap.values.toList.map(x => b.inner.add(x));
    b.inner = b.inner.diff(b.outer)

    //BGMProp %property
    t.filter(_ match {
      case BGMProp(_, _) => true
      case _ => false
    }).map(p => {
      val pro = p.asInstanceOf[BGMProp];
      //MC.addProperty(pro.n, QueryParser.parse(pro.p))
      logger.debug("pro:" + pro) //lbj
    });

    val hasPattern = t.filter(_ match { //防止bgm文件中未定义报空指针
      case BGMPattern(_) => true
      case _ => false
    }).size > 0;

    //BGMPattern %pattern
    if (hasPattern) {
      GlobalCfg.checkPattern = true
      val patternList = t.filter(_ match {
        case BGMPattern(_) => true
        case _ => false
      }).head.asInstanceOf[BGMPattern];
      b.pattern = TermParser.apply(patternList.n);
    }

    val hasTracking = t.filter(_ match {
      case BGMTracking(_, _) => true
      case _ => false
    }).size > 0;

    // BGMTracking
    if (hasTracking) {
      GlobalCfg.checkTracking = true
      t.filter(_ match {
        case BGMTracking(_, _) => true
        case _ => false
      }).map(tt => {
        val tracking = tt.asInstanceOf[BGMTracking]
        b.addTracking(new Tracking(tracking.n, tracking.t, null)); //Exception或Record类型
      })
    }

    val hasPlaceSort = t.filter(_ match {
      case BGMPlaceSort(_, _) => true
      case _ => false
    }).size > 0;

    // BGMPlaceSort
    if (hasPlaceSort) {
      GlobalCfg.checkSorting = true
      t.filter(_ match {
        case BGMPlaceSort(_, _) => true
        case _ => false
      }).map(ps => {
        val placeSort = ps.asInstanceOf[BGMPlaceSort]
        var placeSortName: String = placeSort.n
        var aps: Array[String] = placeSort.cns.toString().split(",")
        var placeSet: Set[String] = Set()
        aps.map { x => placeSet = placeSet + x }
        var placeS: PlaceSort = new PlaceSort();
        placeS.placeSortName = placeSortName;
        placeS.controlList = placeSet;
        b.addplaceSort(placeS)
      })
    }

    val hasLinkSort = t.filter(_ match {
      case BGMLinkSort(_, _) => true
      case _ => false
    }).size > 0;

    // BGMLinkSort
    if (hasLinkSort) {
      GlobalCfg.checkSorting = true
      t.filter(_ match {
        case BGMLinkSort(_, _) => true
        case _ => false
      }).map(ps => {
        val linkSort = ps.asInstanceOf[BGMLinkSort]
        var portList: Set[Port] = Set();
        var linkSortName: String = linkSort.n
        var aps: Array[String] = linkSort.pns.toString().split(",")
        aps.map { p => {
          var ps: Array[String] = p.toString().split(":")
          var port: Port = new Port(ps(0), ps(1))
          portList = portList + port
        }
          var linkS: LinkSort = new LinkSort();
          linkS.linkSortName = linkSortName;
          linkS.portList = portList;
          b.addlinkSort(linkS)
        }
      })
    }

    val hasPlaceSortConstraint = t.filter(_ match {
      case BGMPlaceSortConstraint(_) => true
      case _ => false
    }).size > 0;

    // BGMPlaceSortConstraint
    if (hasPlaceSortConstraint) {
      t.filter(_ match {
        case BGMPlaceSortConstraint(_) => true
        case _ => false
      }).map(ps => {
        val placeSortConstraint = ps.asInstanceOf[BGMPlaceSortConstraint]
        var placeSortConstraintStr: String = placeSortConstraint.n
        b.addPlaceSortConstraints(placeSortConstraintStr)
      })
    }

    val hasLinkSortConstraint = t.filter(_ match {
      case BGMLinkSortConstraint(_) => true
      case _ => false
    }).size > 0;

    // BGMLinkSortConstraint
    if (hasLinkSortConstraint) {
      t.filter(_ match {
        case BGMLinkSortConstraint(_) => true
        case _ => false
      }).map(ps => {
        val linkSortConstraint = ps.asInstanceOf[BGMLinkSortConstraint]
        var linkSortConstraintStr: String = linkSortConstraint.n
        b.addLinkSortConstraints(linkSortConstraintStr)
      })
    }

    //[kgq] ctl检测功能： 首先判断输入的偶图是不是包含CTL公式
    val hasCtl = t.filter(_ match {
      case BGMCTLSpec(_) => true
      case _ => false
    }).size > 0;
    if (hasCtl) { // 如果包含CTL公式，就提取出ctl公式，和原子命题。
      // BGMCTLSpec
      t.filter(_ match { // 把CTL规约添加到Bigraph中去
        case BGMCTLSpec(_) => true
        case _ => false
      }).map(x => {
        val f: BGMCTLSpec = x.asInstanceOf[BGMCTLSpec];
        b.ctlSpec = b.ctlSpec ++ List(f.n);
      });

      // BGMCTLProp
      t.filter(_ match { // 提取原子命题  , 新增原子命题表达式
        case BGMCTLProp(_, _, _) => true
        case _ => false
      }).map(x => {
        val p: BGMCTLProp = x.asInstanceOf[BGMCTLProp];
        b.prop += (p.n -> Tuple2(p.content, p.exp))
      });
      GlobalCfg.needCtlCheck = true; // 包含CTL公式，所以把CTL检测的功能打开。
    }

    val hasLTL = t.filter(_ match {
      case BGMLTLSpec(_) => true
      case _ => false
    }).size > 0;
    if (hasLTL) { // 如果包含CTL公式，就提取出ctl公式，和原子命题。
      // BGMLTLSpec
      t.filter(_ match { // 把CTL规约添加到Bigraph中去
        case BGMLTLSpec(_) => true
        case _ => false
      }).map(x => {
        val f: BGMLTLSpec = x.asInstanceOf[BGMLTLSpec];
        b.ltlSpec = b.ltlSpec ++ List(f.n);
      });

      // BGMLTLProp
      t.filter(_ match { // 提取原子命题  , 新增原子命题表达式
        case BGMCTLProp(_, _, _) => true
        case _ => false
      }).map(x => {
        val p: BGMCTLProp = x.asInstanceOf[BGMCTLProp];
        b.prop += (p.n -> Tuple2(p.content, p.exp))
      });
      GlobalCfg.needCtlCheck = true; // 包含LTL公式，所以把CTL检测的功能打开。
    }
    val builder: BigraphBuilder = new BigraphBuilder(new Signature(ctrls))
    builder.setBigraph(b)
    builder.parseTerm(b.root)
    val res = builder.makeBigraph(true)
    res.root = res.structToTerm(true)
    res.print()
    res
  }
}

object BGMParser extends RegexParsers {
  def logger: Logger = LoggerFactory.getLogger(this.getClass)

  def exp = "[^;^{^}]*".r

  def ident = "[^ \t\n\r;]+".r

  def ws = "[ \t]*".r

  // 单行注释内容，解析时忽略
  def comment: Parser[String] = ".*".r;

  def EOL: Parser[String] = ws ~ ";" ~ ws ^^^ ""

  //
  def stmt: Parser[BGMTerm] = "%active" ~> (ws ~> ident ~ (ws ~> ":" ~> ws ~> ident)) ^^ {
    case i ~ a => BGMControl(i, a.toInt, true, false)
  } |
    "%passive" ~> (ws ~> ident ~ (ws ~> ":" ~> ws ~> ident)) ^^ {
      case i ~ a => BGMControl(i, a.toInt, false, false)
    } |
    //    "%binding" ~> (ws ~> ident ~ (ws ~> ":" ~> ws ~> ident)) ^^ { //add by lbj
    //      case i ~ a => BGMControl(i, a.toInt, true, true)
    //    } |
    "%binding" ~> (ws ~> ident ~ (ws ~> ":" ~> ws ~> ident)) ^^ { // added by kgq 这个为了和另一种项语言格式的binding  这两个binding 的顺序不要弄反了，不然会报错。
      case i ~ a => BGMBinding(i, a.toInt)
    } |
    "%binding" ~> (ws ~> ident) ^^ { // modified by kgq
      x => {
        BGMBinding(x, 0)
      }
    } |
    "%outername" ~> (ws ~> ident) ^^ { x => BGMName(x, true) } |
    "%outer" ~> (ws ~> ident) ^^ { x => BGMName(x, true) } |
    "%innername" ~> (ws ~> ident) ^^ { x => BGMName(x, false) } |
    "%inner" ~> (ws ~> ident) ^^ { x => BGMName(x, false) } |
    "%name" ~> (ws ~> ident) ^^ { x => BGMName(x, false) } |
    "%rule" ~> ((ws ~> ident) ~ (ws ~> exp) ~ ("{" ~> exp <~ "}")) ^^ { //有约束条件的反应规则
      case i ~ s ~ k => {
        val bdrr = s.split("<-")
        val rr = s.split("->")
        if (bdrr.length > 1) { //add by lbj BackDerivation Rules
          GlobalCfg.isBackDerivation = true
          BGMRule(i, bdrr(1), bdrr(0), k)
        } else { //非回朔规则就认为是正常规则
          var eta: InstantiationMap = null
          if (k.length() > 0) {
            // 解析eta e.g. {0->1, 1->0}
            val arr = k.split(",", 0)
            val map: mutable.Map[Int, Int] = new mutable.HashMap[Int, Int]()
            var cnt = 0
            for (m <- arr) {
              val nums = m.split("->")
              val dom = nums(0).trim.toInt
              val cod = nums(1).trim.toInt
              map.+=(dom -> cod)
              cnt = cod.max(cnt) + 1
            }
            val ma: Array[Int] = new Array[Int](map.size)
            for ((key, v) <- map) {
              ma(key) = v
            }
            eta = new InstantiationMap(cnt, ma)
          }
          BGMRule(i, rr(0), rr(1), k, eta)
        }
      }
    } |
    "%rule" ~> ((ws ~> ident) ~ (ws ~> exp)) ^^ { //没有约束条件的反应规则
      case i ~ s => {
        val bdrr = s.split("<-")
        val rr = s.split("->")
        if (bdrr.length > 1) { //add by lbj BackDerivation Rules
          GlobalCfg.isBackDerivation = true
          BGMRule(i, bdrr(1), bdrr(0), "")
        } else { //非回朔规则就认为是正常规则
          BGMRule(i, rr(0), rr(1), "")
        }
      }
    } |
    "%baseAgent" ~> (ws ~> ident ~ (ws ~> exp)) ^^ { // added by kgq 用来读取baseAgent
      case i ~ s => {
        BGMBaseAgent(i, s)
      }
    } |
    "%properties" ~> (ws ~> exp) ^^ { x => BGMProperty(x) } |
    "%agent" ~> ((ws ~> exp) ~ ("{" ~> exp <~ "}")) ^^ { //有expr的agent
      case i ~ e => {
        Data.parseAgentExpr(e)
        BGMAgent(i)
      }
    } |
    "%agent" ~> (ws ~> exp) ^^ { x => BGMAgent(x) } | //没有expr的agent
    "%mode" ~> (ws ~> exp) ^^ {
      x => {
        if (x.contains(GlobalCfg.shareModeStr)) GlobalCfg.sharedMode = true
        BGMNil()
      }
    } |
    "%pattern" ~> (ws ~> exp) ^^ { x => BGMPattern(x) } | //关注子模式
    "%error" ~> (ws ~> exp) ^^ { x => BGMError(x) } | //关注子模式
    "%bindingConstraint" ~> (ws ~> exp) ^^ { x => BGMBinding(x, 0) } | //绑定结构
    "%tracking ExceptionAssert:" ~> (ws ~> exp) ^^ { x => BGMTracking(x, "TrackException") } | //tracking异常断言
    "%tracking RecordAssert:" ~> (ws ~> exp) ^^ { x => BGMTracking(x, "TrackRecord") } | //tracking记录断言
    //    "%port" ~> (ws ~> ident ~ (ws ~> ":" ~> ws ~> ident)) ^^ {
    //      case i ~ a => BGMPort(i, a)
    //    } |
    "%placeSort" ~> (ws ~> ident ~ (ws ~> ":" ~> ws ~> exp) ~ ("{" ~> exp <~ "}")) ^^ {
      case i ~ s ~ k => {
        BGMPlaceSort(i, k)
      }
    } |
    "%linkSort" ~> (ws ~> ident ~ (ws ~> ":" ~> ws ~> exp) ~ ("{" ~> exp <~ "}")) ^^ {
      case i ~ s ~ k => {
        BGMLinkSort(i, k)
      }
    } |
    "%placeConstraint" ~> (ws ~> exp) ^^ { x => BGMPlaceSortConstraint(x) } |
    "%linkConstraint" ~> (ws ~> exp) ^^ { x => BGMLinkSortConstraint(x) } |
    "%property" ~> (ws ~> ident ~ (ws ~> exp)) ^^ { case i ~ p => BGMProp(i, p) } |
    "%placeConstraint" ~> (ws ~> exp) ^^ { x => BGMPlaceSortConstraint(x) } |
    "%formula" ~> (ws ~> exp) ^^ { x => BGMFormula(x) } | //LTL公式     added by wm
    "%proposition" ~> (ws ~> ident ~ (ws ~> exp)) ^^ { //命题对应的Bigraph
      case i ~ s => {
        BGMProposition(i, s)
      }
    } |
    "%ctlSpec" ~> (ws ~> exp) ^^ { x => BGMCTLSpec(x) } | // CTL 公式
    "%ltlSpec" ~> (ws ~> exp) ^^ { x => BGMLTLSpec(x) } | // LTL 公式
    "%prop" ~> (ws ~> ident ~ (ws ~> exp) ~ ("{" ~> exp <~ "}")) ^^ { // 对应的原子命题
      case i ~ s ~ e => {
        BGMCTLProp(i, s, e)
      }
    } |
    "%op-bigraph" ~> (ws ~> ident ~ (ws ~> exp)) ^^ { // add by kgq 20220312 操作子偶图
      case i ~ s => {
        BGMOpBigraph(i, s)
      }
    } |
    "%bigraphformula" ~> (ws ~> exp) ^^ { x => BGMBigraphFormula(x) } | // add by kgq 20220312 偶图操作公式
    "%check" ^^^ {
      BGMNil()
    } |
    "#" ~ comment ^^^ {
      BGMNil()
    }

  def stmtList: Parser[List[BGMTerm]] = stmt ~ (EOL ~> stmtList) ^^ { case x ~ xs => x :: xs } |
    stmt <~ EOL ^^ { x => x :: Nil } |
    stmt ~> stmtList ^^ { x => x }

  def parse(s: File): List[BGMTerm] = parseAll(stmtList, scala.io.Source.fromFile(s).mkString) match {
    case Success(res, _) => res
    case e => throw new Exception(e.toString)
  }

  def parseFromString(str: String): List[BGMTerm] = parseAll(stmtList, str) match {
    case Success(res, _) => res
    case e => throw new Exception(e.toString)
  }

}

object testBGMParser {
  // for test
  def main(args: Array[String]) {
    //    println("My BGMParser!");
    //    val fileName: String = "Examples/MobileCloud/models/hotel.bgm";
    //    val p: List[BGMTerm] = BGMParser.parse(new File(fileName));
    //    println("p:" + p);
    //    var b = BGMTerm.toBigraph(p);
    //    println("Bigraph:" + b);
    //    println("getAllNames:" + b.root.getAllNames);
    //
    //    b.rules.map(r => {
    //      println(r.nyame);
    //      println(r.reactum.getAllNames);
    //      println(r.redex.getAllNames);
    //      println();
    //    });

    /*  val fileName: String = "Examples/Airport_513/models/SmartJigWarehouseBackDerivation.bgm";
      val p: List[BGMTerm] = BGMParser.parse(new File(fileName));
      var b = BGMTerm.toBigraph(p);
      println("Bigraph:" + b);
      println("getAllNames:" + b.root.getAllNames);

      b.rules.map(r => {
        println(r.name);
        println(r.reactum);
        println(r.redex);
        println();
      });*/

    //    var s = "test"
    //    val rr = s.split("->")
    //    println(rr(0))

    var s =
      """
        |# Controls
        |%active CriticalSection : 1;
        |%active Process : 1;
        |
        |# Names
        |%outername a;
        |
        |# Rules
        |%rule r_0 a:CriticalSection[a:outername].$0 | b:Process[a:outername] -> a:CriticalSection[a:outername].($0 | b:Process[a:outername]){};
        |
        |%rule r_1 a:CriticalSection[a:outername].(b:Process[a:outername] | $0) -> a:CriticalSection[a:outername].$0 | b:Process[idle]{};
        |
        |%rule r_2 a:CriticalSection[a:outername].$0 | b:Process[idle] -> a:CriticalSection[a:outername].$0 | b:Process[a:outername]{};
        |
        |%rule r_3 a:CriticalSection[a:outername].$0 | c:Process[a:outername] -> a:CriticalSection[a:outername].($0 | c:Process[a:outername]){};
        |
        |%rule r_4 a:CriticalSection[a:outername].(c:Process[a:outername] | $0) -> a:CriticalSection[a:outername].$0 | c:Process[idle]{};
        |
        |%rule r_5 a:CriticalSection[a:outername].$0 | c:Process[idle] -> a:CriticalSection[a:outername].$0 | c:Process[a:outername]{};
        |
        |# prop
        |%prop p  a:CriticalSection[a:edge].(b:Process[a:edge] | $0){};
        |
        |
        |# Model
        |%agent  a:CriticalSection[idle].nil|b:Process[idle].nil|c:Process[idle].nil{};
        |
        |
        |
        |# LTL_Formula
        |%ltlSpec G!(p);
        |
        |#SortingLogic
        |
        |
        |# Go!
        |%check;
        |""".stripMargin

    var shareTest0 = // rule包括三个测试用例
      """
        |# Controls
        |%active A : 0;
        |%active B : 0;
        |
        |# Rules
        |# %rule r_0 v0:A.(v1:B.v2:A.$1|$0) -> v0:A.(v1:B.v2:A.$1|$0){};
        |# %rule r_0 v0:A.v1:B.$0 -> v0:A.v1:B.$0{};
        |# %rule r_0 v0:A.(v1:B.v2:A.v3:B.$1|$0) -> v0:A.(v1:B.v2:A.v3:B.$1|$0){};
        |
        |# prop
        |%prop p  a:CriticalSection[a:edge].(b:Process[a:edge] | $0){};
        |
        |
        |# Model
        |%agent  u0:A.(u1:B.u4:A.(u7:B)|u2:B.u5:A.(u7:B)|u3:A.u6:B){};
        |
        |%mode ShareMode
        |
        |# LTL_Formula
        |%ltlSpec G!(p);
        |
        |#SortingLogic
        |
        |
        |# Go!
        |%check;
        |""".stripMargin

    var shareTest1 =
      """
        |# Controls
        |%active A : 0;
        |%active B : 0;
        |
        |# Rules
        |%rule r_0 v0:A.(v1:B.$0|v2:B.$0) -> v0:A.(v1:B.$0|v2:B.$0){};
        |
        |# prop
        |%prop p  a:CriticalSection[a:edge].(b:Process[a:edge] | $0){};
        |
        |
        |# Model
        |%agent  u0:A.(u1:B.(u3:A|u4:A)|u2:B.(u4:A|u5:B)){};
        |
        |%mode ShareMode
        |
        |# LTL_Formula
        |%ltlSpec G!(p);
        |
        |#SortingLogic
        |
        |
        |# Go!
        |%check;
        |""".stripMargin

    var shareTest2 =
      """
        |# Controls
        |%active A : 0;
        |%active B : 0;
        |
        |# Rules
        |%rule r_0 v0:A.(v1:B.$0|v2:B.$0) -> v0:A.(v1:B.$0|v2:B.$0){};
        |
        |# prop
        |%prop p  a:CriticalSection[a:edge].(b:Process[a:edge] | $0){};
        |
        |
        |# Model
        |%agent  u0:A.(u1:B.(u3:A|u4:A)|u2:B.(u4:A|u5:B))|u6:B.u3:A|u7:B.u5:B{};
        |
        |%mode ShareMode
        |
        |# LTL_Formula
        |%ltlSpec G!(p);
        |
        |#SortingLogic
        |
        |
        |# Go!
        |%check;
        |""".stripMargin

    var shareTest3 =
      """
        |# Controls
        |%active A : 0;
        |%active B : 0;
        |
        |# Rules
        |%rule r_0 v0:A.(v1:B.v2:A.$1|$0) -> v0:A.(v1:B.v2:A.$1|$0){};
        |
        |# prop
        |%prop p  a:CriticalSection[a:edge].(b:Process[a:edge] | $0){};
        |
        |
        |# Model
        |%agent  u0:A.(u1:B.u2:A.u3:A|u3:A){};
        |
        |%mode ShareMode
        |
        |# LTL_Formula
        |%ltlSpec G!(p);
        |
        |#SortingLogic
        |
        |
        |# Go!
        |%check;
        |""".stripMargin

    var shareTest4 =
      """
        |# Controls
        |%active A : 0;
        |%active B : 0;
        |
        |# Rules
        |%rule r_0 v0:A.(v1:B.v2:A.$1|$0) -> v0:A.(v1:B.v2:A.$1|$0){};
        |
        |# prop
        |%prop p  a:CriticalSection[a:edge].(b:Process[a:edge] | $0){};
        |
        |
        |# Model
        |%agent  u0:A.(u1:B.u2:A.u3:A|u4:B.u3:A){};
        |
        |%mode ShareMode
        |
        |# LTL_Formula
        |%ltlSpec G!(p);
        |
        |#SortingLogic
        |
        |
        |# Go!
        |%check;
        |""".stripMargin

    var shareTest5 =
      """
        |# Controls
        |%active A : 0;
        |%active B : 0;
        |
        |# Rules
        |%rule r_0 v0:A.$0 -> v0:A.$0{};
        |
        |# prop
        |%prop p  a:CriticalSection[a:edge].(b:Process[a:edge] | $0){};
        |
        |
        |# Model
        |%agent  u0:A.(u1:B.u2:A|u2:A){};
        |
        |%mode ShareMode
        |
        |# LTL_Formula
        |%ltlSpec G!(p);
        |
        |#SortingLogic
        |
        |
        |# Go!
        |%check;
        |""".stripMargin
    var test =
      """
        |# Controls
        |%active Process : 1;
        |%active CriticalPart : 2;
        |%active Semaphore : 1;
        |%active Value : 1;
        |
        |# Rules
        |%rule r_apply_idle p0:Process[idle] | p1:Process[idle] | cp:CriticalPart[idle,idle] | sp:Semaphore[a:edge].false:Value[a:edge] | true:Value[idle] -> p1:Process[idle] | sp:Semaphore[a:edge].false:Value[a:edge] | cp:CriticalPart[idle,idle].p0:Process[idle] | true:Value[idle]{};
        |
        |# prop
        |%prop p  cp:CriticalPart[b:edge,c:edge].(p0:Process[c:edge] | p1:Process[b:edge]) | sp:Semaphore[a:edge].true:Value[a:edge] | false:Value[idle]{};
        |
        |# Model
        |%agent  p0:Process[idle] | p1:Process[idle] | cp:CriticalPart[idle,idle] | sp:Semaphore[a:edge].false:Value[a:edge] | true:Value[idle]{};
        |
        |# CTL_Formula
        |%ctlSpec AG!p;
        |
        |# Go!
        |%check;
        |""".stripMargin
    val p: List[BGMTerm] = BGMParser.parseFromString(test)

    def logger = LoggerFactory.getLogger(this.getClass)

    val b = BGMTerm.toBigraph(p);
    DebugPrinter.print(logger, "test111:")
    b.print()
    b.rules.foreach(x => {
      DebugPrinter.print(logger, "rule: " + x)
      b.matchRule(x)
    })
    //    var simulator = new StochasticSimulator(b)
    //    simulator.simulate
    // val p: List[BGMTerm] = BGMParser.parse(new File(fileName));
  }
}

object testBatchCreate {
  def main(args: Array[String]): Unit = {
    var s =
      """
        |# Controls
        |%active Greater : 2;
        |%active Less : 2;
        |%active GreaterOrEqual : 2;
        |%active LessOrEqual : 2;
        |%active Equal : 2;
        |%active NotEqual : 2;
        |%active Exist : 1;
        |%active InstanceOf : 2;
        |%active Buyer : 2;
        |%active Seller : 1;
        |%active Consensus : 1;
        |%active Transaction : 1;
        |%active TransactionSC : 1;
        |%active ScoreSC : 1;
        |%active LocalCreditSC : 1;
        |%active GlobalCreditSC : 1;
        |%active DataBase : 1;
        |%active Money : 0;
        |%active Score : 0;
        |%active LocalCreditData : 0;
        |%active GlobalCreditData : 0;
        |%active Age : 0;
        |%active CallLocalCredit : 0;
        |%active CallGlobalCredit : 0;
        |%active BlockchainSystem : 1;
        |
        |# baseModel
        |%baseAgent ConsensusNodeModel cn:Consensus[idle].(cntsc:TransactionSC[idle] | cngsc:GlobalCreditSC[idle] | cnssc:ScoreSC[idle] | cnlsc:LocalCreditSC[idle] | cndb:DataBase[idle]);
        |
        |# Rules
        |%rule r_generate_transaction bs:BlockchainSystem[idle].$0 | buyer:Buyer[idle,idle].ageB:Age | seller:Seller[idle].ageS:Age | clc:CallLocalCredit | cgc:CallGlobalCredit -> bs:BlockchainSystem[idle].($0 | a:Transaction[transaction:edge].b:Money) | buyer:Buyer[transaction:edge,idle].ageB:Age | seller:Seller[transaction:edge].ageS:Age | clc:CallLocalCredit | cgc:CallGlobalCredit{};
        |
        |# Model
        |%agent bs:BlockchainSystem[idle].(ConsensusNodeModel[*4]) | buyer:Buyer[idle,idle].ageB:Age | seller:Seller[idle].ageS:Age | clc:CallLocalCredit | cgc:CallGlobalCredit;
        |
        |#SortingLogic
        |
        |
        |# Go!
        |%check;
        |
        |""".stripMargin
  }
}


