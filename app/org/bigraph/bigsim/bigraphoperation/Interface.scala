package org.bigraph.bigsim.bigraphoperation

import org.bigraph.bigsim.parser.{ArithExpressionParser, BGMParser, BGMTerm, Cond, TermParser}
import org.bigraph.bigsim.utils.GlobalCfg
import org.bigraph.bigsim.BRS.Match
import org.bigraph.bigsim.model.{Bigraph, Name, Node, Paraller, Prefix, ReactionRule, Regions, Term, TermType}
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.mutable.Map
import scala.collection.mutable.{Queue, Set}


/**
 *  Interface 类
 *  在 Term 类基础上实现偶图内外接口的计算， 同时，维护接口到 具体Term的映射
 *
 * @param t
 *
 * @author kongguanqiao
 *         v.1 2022.03.09
 */
class Interface(t: Term) {

  def logger: Logger = LoggerFactory.getLogger(this.getClass)

  val debug: Boolean = false

  // 四个字段，分别存放Term的四个接口
  var roots: List[Term] = List();
  var sites: List[Term] = List();
  var outernames: Set[String] = Set();
  var innernames: Set[String] = Set();

  // 维护 4个映射，方便偶图操作时，快速找到 相应实体
  var siteParent: Map[Term, Term] = Map();        // site -> parent
  var innerToName: Map[String, Set[Name]] = Map();  // innername -> node
  var outerToName: Map[String, Set[Name]] = Map();  // outername -> node

  initCal;
  /**
   * 实现计算Term的内外接口的功能
   * 在使用一个Term 实例化一个Interface 类的时候，就会默认执行计算操作，
   * 计算出位置图、连接图的内外接口，并保存到上面的四个字段中取，之后按需取用即可。
   */
  def initCal {
    // 计算 roots
    if (debug) logger.debug("Interface.initCal => \n\troots calculate, t.termType: " + t.termType)
    if (t.termType == TermType.TREGION) {
      roots = t.asInstanceOf[Regions].getChildren
    } else {
      roots = roots .:+ (t)
    }
    if (debug) logger.debug("\troots is: " + roots)

    // 计算 sites
    sites = findSites(t)
    if (debug) logger.debug("\tsites calculate: " + sites)

    // 计算 内部名、外部名
    findNames(t)
    if (debug) logger.debug("\touternames: " + outernames)
    if (debug) logger.debug("\tinnernames: " + innernames)
    if (debug) logger.debug("\tinnerToName: " + innerToName)
    if (debug) logger.debug("\touterToName: " + outerToName)
  }

  /**
   * 从当前 Term 递归查找出来所有的 Site
   * @param t
   * @return
   */
  private def findSites(t: Term): List[Term] = {
    var ret: List[Term] = List();
    if (t.termType == TermType.TREGION) {             // handle Regions
      val tleft = t.asInstanceOf[Regions].leftTerm
      val leftsite = findSites(tleft)
      leftsite.foreach(x => {
        if (!siteParent.contains(x))
          siteParent += (x -> t)
      })
      ret = ret ++ leftsite
      val tright = t.asInstanceOf[Regions].rightTerm
      val rightsite = findSites(tright)
      rightsite.foreach(x => {
        if (!siteParent.contains(x))
          siteParent += (x -> t)
      })
      ret = ret ++ rightsite
    } else if (t.termType == TermType.TPAR) {         // handle Paraller
      val tleft = t.asInstanceOf[Paraller].leftTerm
      val leftsite = findSites(tleft)
      leftsite.foreach(x => {
        if (!siteParent.contains(x))
          siteParent += (x -> t)
      })
      ret = ret ++ leftsite
      val tright = t.asInstanceOf[Paraller].rightTerm
      val rightsite = findSites(tright)
      rightsite.foreach(x => {
        if (!siteParent.contains(x))
          siteParent += (x -> t)
      })
      ret = ret ++ rightsite
    } else if (t.termType == TermType.TPREF) {        // handle Prefix
      val tchild = t.asInstanceOf[Prefix].suffix
      val tsites = findSites(tchild)
      tsites.foreach(y => {
        if (!siteParent.contains(y)) {
          siteParent += (y -> t)
        }
      })
      ret = ret ++ tsites  // 如果是Prefix， 那么只需要查看suffix 中是否存在site
    } else if (t.termType == TermType.THOLE) {
      ret = ret .:+ (t)               // 如果当前是一个 HOLE，也就是一个site， 表示找到一个结果，返回
    }
    // 还有两种类型 Num 和 Nil。 不做处理，返回为空
    ret
  }


  /**
   * 从当前 Term ，递归查找出来所有的 内部名 、 外部名
   * @param t
   */
  private def findNames(t: Term) {
    if (t.termType == TermType.TREGION) {               // handle Regions
      val tchild = t.asInstanceOf[Regions].getChildren
      tchild.foreach(x => {
        findNames(x)
      })
    } else if (t.termType == TermType.TPAR) {           // handle Paraller
      val tchild = t.asInstanceOf[Paraller].getChildren
      tchild.foreach(x => {
        findNames(x)
      })
    } else if (t.termType == TermType.TPREF) {          // handle Prefix
      val tnode = t.asInstanceOf[Prefix].node
      tnode.ports.foreach(x => {
        x.nameType match {
          case "outername" => {
            outernames.add(x.name)
            var outernode: Set[Name] = outerToName.getOrElse(x.name, Set())
            outernode.add(x)
            outerToName += (x.name -> outernode)
          }
          case _ => {
            x.innerNameList.foreach(y => {    //如果是边，则要查看这条边的内部名列表，考察是不是有外部名相连
              innernames.add(y)
              var innernode: Set[Name] = innerToName.getOrElse(y, Set())
              innernode.add(x)
              innerToName += (y -> innernode)
            })
          }
        }
      })
      val suf = t.asInstanceOf[Prefix].suffix
      findNames(suf)
    }
    // 还有两种： Nil 和 Num， 不做处理
  }

  def getRoots = roots

  def getSites = sites

  def getOuterNames = outernames

  def getInnerNames = innernames

  def report: String = {
    val inner: String = "<" + sites.size + ", {" + innernames.mkString(",") + "}>"
    val outer: String = "<" + roots.size + ", {" + outernames.mkString(",") + "}>"
    val ret = inner + " -> " + outer
    ret
  }

  override def toString = {
    var ret: String = ""
    ret += "curterm is: " + t + "\nroots is: " + roots + "\nsites is: " + sites + "\nouternames is: " + outernames + "\ninnernames is: " + innernames + "\nouterToNode is: " + outerToName + "\ninnerToNode is: " + innerToName + "\nsiteParent is: " + siteParent
    ret
  }
}

object testInterface {

  def main(args: Array[String]) {
    val inputModel1 =
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
        |%active Container : 0;
        |%active Number : 2;
        |%active Count : 1;
        |
        |# Rules
        |%rule r_rule1 a1:Container.(c:Count[idle] | n1:Number[idle,b:edge]) | a2:Container.n2:Number[idle,b:edge] -> a1:Container.(c:Count[a:edge] | n1:Number[a:edge,b:edge]) | a2:Container.n2:Number[a:edge,b:edge]{};
        |%rule r_rule2 a1:Container.(c:Count[a:edge] | n1:Number[a:edge,b:edge]) | a2:Container.n2:Number[a:edge,b:edge] -> a1:Container.(c:Count[a:edge] | n1:Number[a:edge,b:edge]) | a2:Container.n2:Number[a:edge,b:edge]{Condition:Container.Count>0	Assign:Container.Count=Container.Count-1,a1.Number=a1.Number+a2.Number};
        |
        |# Model
        |%agent a1:Container.(n1:Number<2>[idle,b:edge] | c:Count<2>[idle]) | a2:Container.n2:Number<3>[idle,b:edge] {};
        |
        |#SortingLogic
        |
        |# Go!
        |%check;
        |""".stripMargin

    val testTerm =
      """
        |a:Client[idle,a:outername].(c:Register[idle] | d:aNum[a:outername].e:False | g:SmartContract[b:outername] | $0) | b:Client[idle,a:edge].(f:Register[idle].$2 | $1) | h:Coin[idle,b:edge] | i:Coin[b:edge,idle] | j:Coin[idle,b:edge] || k:Client[idle,idle]
        |""".stripMargin
    val parsed = TermParser.apply(testTerm)
    println(parsed)

    val faceTest = new Interface(parsed)
    println(faceTest)




  }
}