package org.bigraph.bigsim.simulator

import org.bigraph.bigsim.BRS.{Graph, Match, Matcher, Vertex}
import org.bigraph.bigsim.model.{Bigraph, BindingChecker, Nil, ReactionRule}
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import org.bigraph.bigsim.parser.{BGMParser, BGMTerm, TermParser}
import org.bigraph.bigsim.transitionsystem.{KripkeStructure, KripkeStructureOnTheFly, State}
import java.util.concurrent.ConcurrentHashMap

import org.bigraph.bigsim.utils.GlobalCfg
import org.bigraph.bigsim.value.Value

import scala.collection.mutable.{Map, Queue, Set}


class SuccessorBuilder(trans: TransitionSystemOnTheFly, predecessor: State, matches: mutable.Set[Match]) {

  var matchiter = matches.iterator
  /**
   * @return 检查是否还有后继偶图
   */
  def hasNext(): Boolean = {
    matchiter.hasNext
  }

  def next(): State = {
    assert(hasNext())                     // 增加断言，保证能取到下一个
    val curmatch = matchiter.next()
    this.trans.buildSuccessorState(predecessor, curmatch)
  }
}

/**
 * 这个类用来实现on-the-fly模式下，进行的偶图的反应衍化
 * @author kongguanqiao 2022.04.19
 * @param b
 */

class TransitionSystemOnTheFly(b: Bigraph) {

  def logger : Logger = LoggerFactory.getLogger(this.getClass)

  var v: Vertex = new Vertex(b, null, rr = null);
  var g: Graph = new Graph(v);

  val initAgent: Bigraph = b

  var stateToVertex: Map[State, Vertex] = Map();        // 状态到Vertex的映射
  var vertexToState: Map[Vertex, State] = Map();        // Vertex到状态的映射
  var vertexToSuccessorBuilder: Map[Vertex, SuccessorBuilder] = Map();      // Vertex 到 SuccessorBuilder的映射

  val propRR: mutable.Map[String, ReactionRule] = mutable.Map()
  buildPropMap
  var DEBUG: Boolean = false

  def buildPropMap: Unit = {
    for ((k, v) <- b.prop) {
      val propRedex = TermParser.apply(v._1)
      val propReact = TermParser.apply("nil")
      val exp = v._2
      var propExp: String = ""
      if (exp.startsWith(GlobalCfg.ctlPropPreStr)) {
        propExp += GlobalCfg.conditionPrefStr + exp.substring(GlobalCfg.ctlPropPreStr.length)
      }
      var rr = new ReactionRule(k, propRedex, propReact, propExp)
      propRR += (k -> rr)
    }
  }

  /**
   * 检查某个状态是否含有后继状态
   * @param state 待查询的前驱状态
   * @return
   */
  def hasNextSuccessor(state: State): Boolean = {
    logger.debug("查看状态是否含有后继：" + state)
    // 如果当前的状态没有访问过
    if (!stateToVertex.contains(state)) {
      logger.warn("Warning: check unrecorded state -- " + state)
      return false
    }
    // 如果当前状态对应的Vertex.terminal是true，也就是在这里终止了，那么就返回false
    val curvertex = stateToVertex(state)
    if (curvertex.terminal) {
      return false
    }
    // 如果已经创建过后继创建器
    if (vertexToSuccessorBuilder.contains(curvertex)){
      val cursuccessorbuild = vertexToSuccessorBuilder(curvertex)
      return cursuccessorbuild.hasNext()
    } else {    // 当前vertex并未创建过后继创建器
      var b: Bigraph = curvertex.bigraph;
      var matches: mutable.Set[Match] = b.findMatches;
      matches = matches.filter(x => {                 // 检查反应条件，只保留满足反应条件的匹配结果。
        val value = new Value(x.rule)
        value.checkConditions(x)
      })
      logger.debug("新建后继创建器，并存储" + matches.size)
      val successorBuilder = new SuccessorBuilder(trans = this, predecessor = state, matches = matches)     // 新建后继创建器
      this.vertexToSuccessorBuilder += (curvertex -> successorBuilder)                                      // 将后继创建器进行记录
      if (matches.isEmpty) {
        v.terminal = true;
        return false
      } else {
        return true
      }
    }
    false
  }

  /**
   * 根据一个前驱，查找后继创建器，并返回
   * @param predecessor
   * @return
   */
  def getSuccessorBuilder(predecessor: State): SuccessorBuilder = {
    assert(stateToVertex.contains(predecessor))
    val vertex = stateToVertex(predecessor)
    assert(vertexToSuccessorBuilder.contains(vertex))
    vertexToSuccessorBuilder(vertex)
  }

  /**
   * 根据前驱状态predecessor 和 一条匹配的结果 m， 反应衍化得到新的偶图，并构造一个新的状态State
   * @param predecessor
   * @param m
   * @return
   */
  def buildSuccessorState(predecessor: State, m: Match): State = {
    assert(stateToVertex.contains(predecessor))
    val v: Vertex = stateToVertex(predecessor)
    val b: Bigraph = v.bigraph
    logger.debug("旧偶图为: " + b.root + "v的hash为: " + v.hash)
    val nb: Bigraph = b.applyMatch(m)               // 应用m， 生成新的偶图
    logger.debug("生成的新偶图为: " + nb.root + "\nvia rule: " + m.rule)

//    var bindingChecker = new BindingChecker();    // TODO 暂时不加入 Binding 机制，后续想想怎么实现
//    if (GlobalCfg.checkBinding && !bindingChecker.bindcheck(nb.root))
    if (nb.root == null) nb.root = new Nil();
    var nv: Vertex = new Vertex(nb, v, m.rule);
    logger.debug("新的hash为: " + nv.hash)
    if (g.lut.contains(nv.hash)) {                    // 如果新生成的节点已经访问过了
      logger.debug("已经访问过的节点" + g.lut)
      nv = g.lut(nv.hash)
      nv.addParents(v)
    } else {
      g.add(nv)
    }
    v.addTarget(nv, m.rule)
    v.visited = true
    val retstate = buildStatefromVertex(nv)
    logger.debug("从state ：" + predecessor + "新建的state为: " + retstate)
    retstate
  }

  /**
   * 从一个节点Vertex，创建一个状态State
   * @param v
   * @return
   */
  def buildStatefromVertex(v: Vertex): State = {
    if (vertexToState.contains(v)) {
      return vertexToState(v)
    } else {
      val stateName = v.hash.toString
      val stateAtom = checkAtom(v.bigraph)
      val curState = new State(stateName, stateAtom: _*)          // 创建新的状态，并记录到vertexToState中去，并返回
      vertexToState += (v -> curState)
      stateToVertex += (curState -> v)
      curState
    }
  }

  /**
   * 检查一个偶图 Bigraph 所满足的所有原子命题
   * @param b
   * @return
   */
  def checkAtom(b: Bigraph): List[String] = {       // 输入的是一个偶图，检测它所满足的原子命题
    val retList: ListBuffer[String] = ListBuffer()
    for ((name, rr) <- propRR) {
      val mp: mutable.Set[Match] = Matcher.tryMatchTermReactionRule(b.root, rr)

//      mp.foreach(m => {
//        if (rr.check(m)) {//如果cond过不去，这里的x虽然能匹配上但这里会被过滤掉不被加入res中，例第一次就可以匹配r_takeoff这个规则，但因为不满足cond则不加入res中
//
//          res.add(m);//如果这条RR没有cond也能加进来，check函数能过
//        }
//      })

      if(mp.nonEmpty) {
        val value = new Value(rr)
        var condCheck: Boolean = false
        for (m <- mp if !condCheck) {
          val tmpRes = value.checkConditions(m)
          //// 条件检查完，需要对数值进行检查
          val valueCheckRes=value.checkValueConditions(m)
          condCheck = condCheck || (tmpRes&&valueCheckRes)

        }

        if (condCheck) {
          retList.append(name)
        }
      }
    }
    retList.toList
  }

  def buildKripke: KripkeStructureOnTheFly = {
    val kripkeStructure = new KripkeStructureOnTheFly(this)
    val initialState = buildStatefromVertex(v)
    kripkeStructure.addInitialState(initialState)
    kripkeStructure
  }

}

