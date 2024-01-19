package org.bigraph.bigsim.BRS

import java.io.{File, FileWriter, Writer}
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent._

import org.bigraph.bigsim.ctlimpl.CTLCheckResult
import org.bigraph.bigsim.data.Data
import org.bigraph.bigsim.model.ReactionRule
import org.bigraph.bigsim.strategy.{ParseRules, ParseXML, PatternFlow}
import org.bigraph.bigsim.transitionsystem.State
import org.bigraph.bigsim.utils.{DebugPrinter, GlobalCfg, Graphviz}
import org.slf4j.{Logger, LoggerFactory}
import utils.BigSimThreadFactory

import scala.collection.mutable.{Map, Set, Stack}
import scala.util.Random
import scala.xml.XML

//object Graph {
//  var pathNum: Int = 0; //对每次只产生一条模拟路径的模拟器，pathNum即loopNum
//  var loopNum: Int = 0;
//  var loopNumPath: Int = 0;
//  var path: String = "";
//  var rrs: String = "";
//  var variables: String = "";
//  var dot: String = "";
//  //  var patternPathStr: String = "";
//  //  var patternPaths: Set[Stack[Vertex]] = Set();
//}

class Graph(init: Vertex) {

  def logger: Logger = LoggerFactory.getLogger(this.getClass)

  val root: Vertex = init;
  var pathColor: String = Graphviz.getColor()
  //lut: look up table
  val lut: Map[Int, Vertex] = Map(); // lut表的功能就是从一个节点的hash，找到这个节点Vertex
  if (root != null) lut(root.hash) = root;

  def add(v: Vertex): Unit = {
    lut(v.hash) = v;
  }

  def size: Int = lut.size;

  def backTrace(v: Vertex): String = {
    //todo
    var vertex: Vertex = v;
    var i: Int = 0;
    var here: String = "";
    while (vertex != null) {
      if (i == 0) here += "   <- *** VIOLATION *** \n";
      // FIXME: 字符串连接顺序可能有误，需要反过来？
      here += "#" + i + " " + vertex.bigraph.root.toString() + "\n";
      if (vertex.reactionRule == null) {
        here += " >> (root)\n";
      } else {
        here += " >> " + vertex.reactionRule.toString + "\n";
      }
      i += 1;
      vertex = vertex.parent;
    }
    here;
  }

  /**
   * get Pathes has interested pattern
   */
  def getPathsHasInterestPatterns: Set[Stack[Vertex]] = {
    var result: Set[Vertex] = Set()
    var allPaths: Set[Stack[Vertex]] = getAllPaths
    logger.debug("All pathes size is: " + allPaths.size)
    var selectedPaths: Set[Stack[Vertex]] = Set() //这里用于存储筛选掉之后的所有path

    allPaths.map(ite => {
      var pathStack: Stack[Vertex] = Stack()
      var size: Int = ite.size

      var ruleName: Array[String] = new Array[String](size)

      for (i <- 0 to size - 1) {
        if (ite.head.reactionRule != null) {
          ruleName(i) = ite.head.reactionRule.name
        } else {
          ruleName(i) = null
        }
        pathStack.push(ite.head)
        ite.pop
      }

      var containsInteresPattern: Boolean = false
      var interesRules: Set[String] = ParseXML.getNamesOfIntrestedPatterns(root.bigraph, XML.load(GlobalCfg.patternFile))
      interesRules.map(it => {
        if (ruleName.contains(it)) {
          containsInteresPattern = true
        }

      })

      if (containsInteresPattern) {
        pathStack.map(itps => {
          ite.push(itps)
        })
      }
      if (ite != null) {
        selectedPaths += ite
      }
      pathStack.clear
    })
    selectedPaths
  }

  /*
	 * 找一个数据结构，存储根据Vertexs中属性terminal为true的找出来每一条路径。
	 * 中间，根绝策略筛选路径。
	 * 筛选好的所有路径放入到一个set中去,返回的路径是以dot文件的形式，所以
	 */
  def findPathsByStrategy(rules: Set[ReactionRule]): Set[Stack[Vertex]] = {
    //这里，到底提供那种strategy，可以再global里面定义
    var result: Set[Vertex] = Set()
    var allPathes: Set[Stack[Vertex]] = getAllPaths
    var selectedPathes: Set[Stack[Vertex]] = Set() //这里用于存储筛选掉之后的所有path

    var dcuMaps: Map[String, Set[String]] = ParseRules.getAllDCUs(rules)
    var dpuMaps: Map[String, Set[String]] = ParseRules.getAllDPUs(rules)
    var sameDefRules: Map[String, Set[String]] = ParseRules.getAllRulesWithSameDef(rules)

    allPathes.map(ite => {
      var pathStack: Stack[Vertex] = Stack()
      var size: Int = ite.size
      var ruleName: Array[String] = new Array[String](size)

      for (i <- 0 to size - 1) {
        if (ite.head.reactionRule != null) {
          ruleName(i) = ite.head.reactionRule.name
        } else {
          ruleName(i) = null
        }
        pathStack.push(ite.head)
        ite.pop
      }

      var containsDU: Boolean = false

      //保证du的定义是清纯的，其实可以直接找路径中是否存在du对，已经概括了其中的情况了。
      for (i <- 0 to (size - 2) if containsDU == false) {
        for (j <- (i + 1) to (size - 1) if containsDU == false) {
          if (ruleName(i) != null) {
            if (GlobalCfg.allUses) {
              if (dcuMaps(ruleName(i)).contains(ruleName(j))) {
                for (k <- (j + 1) to (size - 1) if containsDU == false) {
                  if (dpuMaps(ruleName(i)).contains(ruleName(k))) {
                    containsDU = true
                  }
                }
              } else if (dpuMaps(ruleName(i)).contains(ruleName(j))) {
                for (k <- (j + 1) to (size - 1) if containsDU == false) {
                  if (dcuMaps(ruleName(i)).contains(ruleName(k))) {
                    containsDU = true
                  }
                }
              }
            } else if (GlobalCfg.allDefs) {
              if (dcuMaps(ruleName(i)).contains(ruleName(j)) || dpuMaps(ruleName(i)).contains(ruleName(j))) {
                containsDU = true
              }
            }

          }
        }
      }

      if (containsDU) {
        pathStack.map(itps => {
          ite.push(itps)
        })
      }
      if (ite != null) {
        selectedPathes += ite
      }
      pathStack.clear
    })

    selectedPathes
  }

  /**
   * 使用一个stack来存储一条路径。
   * 然后把所有路径放到一个set里面去。
   */
  def getAllPaths: Set[Stack[Vertex]] = {
    var allPathSet: Set[Stack[Vertex]] = Set()
    logger.debug("Vertexs size is : " + lut.values.size)

    lut.values.map(ite => {
      if (ite.parent != null) {
        var lastVertex: Boolean = true
        lut.values.map(it => {
          if (it.parent != null && ite.hash == it.parent.hash) {
            lastVertex = false
          }
        })
        if (lastVertex == true) {
          var pathStack: Stack[Vertex] = Stack()
          var tempVertex: Vertex = ite
          pathStack.push(tempVertex)
          while (tempVertex.parent != null) {
            pathStack.push(tempVertex.parent)
            tempVertex = tempVertex.parent
          }
          allPathSet.add(pathStack)
        }
      }
    })
    allPathSet
  }

  def dumpPaths(): String = {
    logger.debug("kgq: --------------------- into dumpPaths------------------------------")
    var allRulesNum: Double = root.bigraph.rules.size
    var defPathMap: Map[String, Set[Int]] = Map()

    var paths: Set[Stack[Vertex]] = Set()
    if (GlobalCfg.allUses || GlobalCfg.allDefs) {
      paths ++= findPathsByStrategy(root.bigraph.rules)
    } else if (GlobalCfg.checkInterestPattern) {
      paths ++= getPathsHasInterestPatterns
    } else {
      paths ++= getAllPathss
    }

    var out: String = ""
    var pathNum: AtomicInteger = new AtomicInteger(-1)

    val BigSimPool = new ThreadPoolExecutor(10, 10,
      1L, TimeUnit.SECONDS,
      new LinkedBlockingQueue,
      new BigSimThreadFactory("Graph"),
      new ThreadPoolExecutor.AbortPolicy);

    val endGate = new CountDownLatch(math.min(200, paths.size));


    try {
      paths.foreach(ite => {
        val task = new Runnable() {
          @Override
          def run() {
            try {
              var ruleNameSet: Set[String] = Set()
              ite.map(itN => {
                if (itN.reactionRule != null) {
                  ruleNameSet.add(itN.reactionRule.name)
                }
              })
              var ruleNameSize: Double = ruleNameSet.size
              var ruleCoverage: String = (math floor (ruleNameSize / allRulesNum) * 100).toString + "%" //向下取整


              if (ite.size != 0) {
                pathNum.getAndIncrement()

                if (GlobalCfg.allDefs) {
                  // 得到每一个def和路径的映射，def1{0， 2， 4， 8}；
                  root.bigraph.rules.map(ruleIte => {
                    if (ruleNameSet.contains(ruleIte.name)) {
                      ruleIte.defTerm.map(defTermIte => {
                        if (defPathMap.contains(defTermIte.toString)) {
                          defPathMap(defTermIte.toString).add(pathNum.get())
                        } else {
                          defPathMap += (defTermIte.toString -> Set(pathNum.get()))
                        }
                      })
                    }
                  })
                }

                out += pathNum.get() + "{\n"
                //中间输出一个stack里面的Vertex的model，每一个model中间用分号分割
                while (ite.size > 0) {
                  var rootStr: String = ite.head.bigraph.root.toString
                  if (rootStr.charAt(0) != '(') {
                    out += rootStr
                  } else {
                    out += rootStr.substring(1, rootStr.size - 1)
                  }
                  out += ";\n"
                  ite.pop
                }
                out += "}" + ruleCoverage + "\n"
              }
            } catch {
              case e: Exception => {
                e.printStackTrace()
              }
            } finally {
              endGate.countDown();
            }
          }
        }
        BigSimPool.execute(task);
      })
      endGate.await();
    } catch {
      case e: Exception => {
        e.printStackTrace()
      }
    } finally {
      BigSimPool.shutdown()
    }


    if (GlobalCfg.pathOutput != "") {
      logger.debug("kgq: ------------------------------------GlobalCfg.pathOutput=" + GlobalCfg.pathOutput);
      var file: File = new File(GlobalCfg.pathOutput);
      var writer: Writer = new FileWriter(file);
      writer.write(out);
      writer.flush;
      writer.close()
    }

    if (GlobalCfg.defPathMapFile != "") { // 这条分支目前没有走
      var defPath: String = ""
      defPathMap.map(ite => {
        defPath += ite._1 + "{"
        var tempdefPath: String = ""
        ite._2.map(it => {
          tempdefPath += it + ", "
        })
        defPath += tempdefPath.substring(0, tempdefPath.size - 2)
        defPath += "}\n"
      })

      var file: File = new File(GlobalCfg.defPathMapFile);
      logger.debug("kgq: Globalcfg.defPathMapFile---------------------:" + GlobalCfg.defPathMapFile);
      var writer: Writer = new FileWriter(file);
      logger.debug("kgq: defPath----------------------------------------:" + defPath);
      writer.write(defPath);
      writer.flush;
    }
    logger.debug("kgq: ----------------------------this is out content: -----------------------------------")
    logger.debug(out)
    out
  }

  var concurrent = new ConcurrentHashMap[String, String]();
  var charIndex = 0;

  def formatHash(hash: Int): String = { //格式化生成的hashCode，若小于零则返回 "_hashcode绝对值"
        var str = "";
        if (hash < 0) str = "" + hash.abs;
        else str = hash.toString;

        if (!concurrent.containsKey(str)) {
          //      concurrent.put(str, ((charIndex + 'A').toChar).toString)
          concurrent.put(str, getStrValue(charIndex))
          charIndex = charIndex + 1
        }

        concurrent.get(str)
    // 上面的方法在节点数目很大的情况下生成的str冲突
//    "" + hash
  }

  def getStrValue(index: Int) = {
    val k = index / 26
    var str = ""
    if (k > 0) {
      str += ((k % 26) - 1 + 'A').toChar + "_"
    }
    str += ((index % 26) + 'A').toChar
    str
  }

  def getAllPathss: Set[Stack[Vertex]] = {
    var allPathSet: Set[Stack[Vertex]] = Set()
    var allPathString: Set[String] = Set()
    root.terminal = true
    addPaths("", allPathString, root) //调用后把结果都存在allPathString里面

    allPathString.map(path => {
      var p: Stack[Vertex] = Stack()
      var Vertexs = path.split("#")
      for (i <- 0 to Vertexs.size - 1) {
        if (!Vertexs(Vertexs.size - 1 - i).contains("r_")) { //反应规则必须以r_开头，要不这里逻辑走不通
          var Vertex = lut.getOrElse(Vertexs(Vertexs.size - 1 - i).toInt, null) //查不到就置为空，主要看lut
          if (Vertex != null)
            p.push(Vertex)
          else
            logger.error("error!")
        }
      }
      allPathSet.add(p)

    })

    allPathSet

  }

  var firstTime: Boolean = true
  //var pathFile: File = new File("Airport/paths/BusinessNormal.txt");
  //var writer: Writer = new FileWriter(pathFile);

  def addPaths(path: String, allPathString: Set[String], currentVertex: Vertex) { //调用时传入的是root，这个方法主要看target
    if (currentVertex.terminal && !firstTime) { //为终点且firstTime=false时才会退出这个方法

      //logger.debug("allPathString: " + path + currentVertex.hash);
      allPathString.add(path + currentVertex.hash)
      //writer.write(path + currentVertex.hash + "\n");
      //writer.flush;
      return
    } else { //不为终点或firstTime=true时
      firstTime = false
      currentVertex.target.map(target => { //var target: Map[Vertex, ReactionRule] = Map();
        if (!path.contains(currentVertex.hash + "#" + target._2.name + "#" + target._1.hash)) {
          addPaths(path + currentVertex.hash + "#" + target._2.name + "#", allPathString, target._1) //递归，自己调用自己
          //logger.debug("path:"+path+"   "+currentVertex.hash + "#" + target._2.name + "#" + target._1.hash);
          //logger.debug(path + currentVertex.hash + "#" + target._2.name + "#"+"  "+ allPathString + "  "+target._1);
        }
      })
    }

  }

  //add by lbj
  def dumpPath(): Unit = {
    var allRulesNum: Double = root.bigraph.rules.size
    var defPathMap: Map[String, Set[Int]] = Map()
    var loopNumPath = 1
    var enumPathNum: Int = 0
    var paths: Set[Stack[Vertex]] = Set()

    if (GlobalCfg.checkPattern) {
      var all = getPathSet //每次一条路径
      paths ++= getPathsHasPattern(all)
      logger.debug("paths: " + paths)
    } else {
      paths ++= getPathSet
      logger.debug("paths: " + paths)
    }

    var out: String = ""
    var rrStr: String = ""
    var variableStr: String = ""
    var pathNum: Int = -1
    paths.map(ite => {
      rrStr = (pathNum + 1) + "{\n"
      var ruleNameSet: Set[String] = Set() //不会重名
      ite.map(itN => {
        if (itN.reactionRule != null) {
          ruleNameSet.add(itN.reactionRule.name)
          rrStr += (itN.reactionRule.name + "\n")
        } else if (itN.reactionRules.size > 0) { // add by LBJ 添加并发反应处理
          itN.reactionRules.foreach { x => {
            ruleNameSet.add(x.name);
            rrStr += (x.name + "\n")
          }
          }
        }

      })
      rrStr += "}\n"

      var ruleNameSize: Double = ruleNameSet.size
      var ruleCoverage: String = (math floor (ruleNameSize / allRulesNum) * 100).toString + "%" //向下取整

      if (ite.size != 0) {
        pathNum += 1

        variableStr = pathNum + "{\n"
        if (GlobalCfg.checkTime) {
          ite.foreach { x => variableStr += ("SysClk=" + x.sysClk + "," + x.variables + ";\n") }
        } //考虑时间则打印时间
        else ite.foreach { x => variableStr += (x.variables + ";\n") }
        variableStr += "}\n"

        out += pathNum + "{\n"
        //中间输出一个stack里面的Vertex的model，每一个model中间用分号分割
        while (ite.size > 0) {
          var rootStr: String = ite.top.bigraph.root.toString //modify by lbj
          if (rootStr.charAt(0) != '(') {
            out += rootStr
          } else {
            out += rootStr.substring(1, rootStr.size - 1)
          }
          out += ";\n"
          ite.pop
        }
        out += "}" + ruleCoverage + "\n"

        //Graph.variables += variableStr // add by lbj 添加变量打印
        logger.debug("pathNum: " + pathNum)
        //logger.debug(out)
      }
    })

    //    if ((GlobalCfg.SimulatorClass.startsWith("Enum")) && (loopNumPath == GlobalCfg.simLoop)) { //枚举输出哪次结果都一样，这里输出最后一次的结果
    //
    //      if (GlobalCfg.pathOutput != "") {
    //        var file: File = new File(GlobalCfg.pathOutput);
    //        var writer: Writer = new FileWriter(file);
    //        writer.write(out);
    //        writer.flush;
    //        writer.close();
    //      }
    //    }

  }

  /**
   * select paths has pattern's define/cuse/puse
   * add by lbj
   */
  def getPathsHasPattern(allpaths: Set[Stack[Vertex]]): Set[Stack[Vertex]] = {
    var result: Set[Vertex] = Set()
    //logger.debug("------All pathes size: " + allpaths.size + "------allpaths: " + allpaths)
    var selectedPaths: Set[Stack[Vertex]] = Set() //这里用于存储筛选掉之后的所有path

    allpaths.map(ite => {
      var containPattern = false
      ite.map(itN => {

        if (itN.reactionRules.size > 0) {
          logger.debug("itN.reactionRules: " + itN.reactionRules)
          itN.reactionRules.map { rr => {
            if (PatternFlow.patternDefRules.size > 0 && PatternFlow.patternDefRules.contains(rr.name)) {
              containPattern = true;
              selectedPaths.+=(ite)
            }
            if (PatternFlow.patternPUseRules.size > 0 && PatternFlow.patternPUseRules.contains(rr.name)) {
              containPattern = true;
              selectedPaths.+=(ite)
            }
            if (PatternFlow.patternCUseRules.size > 0 && PatternFlow.patternCUseRules.contains(rr.name)) {
              containPattern = true;
              selectedPaths.+=(ite)
            }
          }
          }
        }

        if (itN.reactionRule != null) {
          logger.debug("itN.reactionRule: " + itN.reactionRule)
          if (PatternFlow.patternDefRules.size > 0 && PatternFlow.patternDefRules.contains(itN.reactionRule.name)) {
            containPattern = true;
            selectedPaths.+=(ite)
          }
          if (PatternFlow.patternPUseRules.size > 0 && PatternFlow.patternPUseRules.contains(itN.reactionRule.name)) {
            containPattern = true;
            selectedPaths.+=(ite)
          }
          if (PatternFlow.patternCUseRules.size > 0 && PatternFlow.patternCUseRules.contains(itN.reactionRule.name)) {
            containPattern = true;
            selectedPaths.+=(ite)
          }
        }
      })
      if (containPattern) {
        ite.map { x => {
          x.visited = true
        }
        }
      }
    })

    logger.debug("selectedPaths: " + selectedPaths)
    selectedPaths
  }

  //add by lbj 获得模拟结果路径集
  def getPathSet: Set[Stack[Vertex]] = {
    var allPathSet: Set[Stack[Vertex]] = Set()
    var allPathString: Set[String] = Set()
    root.terminal = true
    getPathString("", allPathString, root) //调用后把结果都存在allPathString里面

    allPathString.map(path => {
      var p: Stack[Vertex] = Stack()
      var Vertexs = path.split("#")
      for (i <- 0 to Vertexs.size - 1) {
        if (!Vertexs(Vertexs.size - 1 - i).contains("r_")) { //反应规则必须以r_开头，要不这里逻辑走不通
          var Vertex = lut.getOrElse(Vertexs(Vertexs.size - 1 - i).toInt, null) //查不到就置为空，主要看lut
          if (Vertex != null)
            p.push(Vertex)
          else
            logger.error("error!")
        }
      }
      allPathSet.add(p)

    })

    allPathSet

  }

  //add by lbj
  def getPathString(path: String, allPathString: Set[String], currentVertex: Vertex) { //调用时传入的是root，这个方法主要看target
    if (currentVertex.terminal && !firstTime) { //为终点且firstTime=false时才会退出这个方法

      logger.debug("allPathString: " + path + currentVertex.hash);
      allPathString.add(path + currentVertex.hash)
      return
    } else { //不为终点或firstTime=true时
      firstTime = false
      if (currentVertex.target != null && currentVertex.target.size > 0) {
        currentVertex.target.map(target => {
          if (!path.contains(currentVertex.hash + "#" + target._2.name + "#" + target._1.hash)) {
            getPathString(path + currentVertex.hash + "#" + target._2.name + "#", allPathString, target._1) //递归，自己调用自己
          }
        })
      } else if (currentVertex.targets != null && currentVertex.targets.size > 0) { //考虑规则并发反应
        currentVertex.targets.map(targets => {
          var rrs: String = "";
          if (targets._2.size > 0) {
            var tarArry = targets._2.toArray
            rrs = tarArry(0).name.toString()
            for (i <- 1 to tarArry.size - 1) {
              rrs += "," + tarArry(i).name.toString() //逗号分隔当前并发反应的规则
            }
            if (!path.contains(currentVertex.hash + "#" + rrs + "#" + targets._1.hash)) {
              getPathString(path + currentVertex.hash + "#" + rrs + "#", allPathString, targets._1) //递归，自己调用自己
            }
          }
        })
      }
    }

  }


  def dumpDotForward: String = {
    //if (GlobalCfg.graphOutput == "") return "";
    var out: String = "";
    out += "digraph reaction_graph {\n";
    out += "   rankdir=LR;\n";
    out += "   Node [shape = circle];\n";
    out += "   BigSim_Report [shape = parallelogram color = aliceblue style=filled label=\"BigSim\nReport\"];\n"
    out += "BigSim_Report -> N_" + formatHash(root.hash) + "[color = aliceblue label = \"";
    if (!Data.getWeightExpr.equals("wExpr=")) //读取agent的权重表达式，输出模拟报告 表示权重表达式不为空，即有权重表达式
      out += Data.getWeightExpr + "=" + Data.getReport + "\n";
    out += Data.getValues(",") + "\"];\n";
    out += " N_" + formatHash(root.hash) + "\n" + " [shape=circle, color=lightblue2, style=filled];\n";

    lut.values.map(x => {
      var rr: String = "root";
      var dc: String = "";

      if (x.terminal) {
        dc = "shape = doublecircle, color=lightblue2, style=filled, ";
      }
      out += "N_" + formatHash(x.hash) + "[ " + dc + "label=\"N_" + formatHash(x.hash) + "\n" + x.variables + "\"];\n"; //data太多了不输出了
      //out += "N_" + formatHash(x.hash) + "[ " + dc + "label=\"N_" + formatHash(x.hash) + "\"];\n"; //输出Bigraph模型的HashCode作为名字
      x.target.map(y => {
        rr = "?";
        if (y._2 != null)
          rr = y._2.name; // 规则名

        if (y._1 != null) {
          rr = rr + "\nSystem Clock: " + y._1.sysClk
          if (GlobalCfg.checkData && y._2.conds.size != 0)
            rr = rr + "\nCond:" + y._2.getConds
          if (GlobalCfg.checkHMM && y._2.hmms.size != 0)
            rr = rr + "\nHMM:" + y._2.getHMM
          out += " N_" + formatHash(x.hash) + " -> N_" + formatHash(y._1.hash) + "[color = purple  label = \"" + rr + "\"];\n"
        }
      });

    });
    out += "}\n";
    if (GlobalCfg.graphOutput != "") {
      var writer: Writer = new FileWriter(GlobalCfg.graphOutput);
      writer.write(out);
      writer.flush;
    }
    out;
  }


  def dumpDotForward1: String = {

    //if (GlobalCfg.graphOutput == "") return "";
    var out: String = "";
    out += "digraph reaction_graph {\n";
    out += "   rankdir=LR;\n";
    out += "   Node [shape = circle];\n";
    out += "   BigSim_Report [shape = parallelogram color = aliceblue style=filled label=\"BigSim\nReport\"];\n"
    out += "BigSim_Report -> N_" + formatHash(root.hash) + "[color = aliceblue label = \"";
    //  getStateHashCode() + "->" + getStateHashCode()
    if (!Data.getWeightExpr.equals("wExpr=")) //读取agent的权重表达式，输出模拟报告 表示权重表达式不为空，即有权重表达式
      out += Data.getWeightExpr + "=" + Data.getReport + "\n";
    out += Data.getValues(",") + "\"];\n";
    out += " N_" + formatHash(root.hash) + "\n" + " [shape=circle, color=lightblue2, style=filled];\n";
    lut.values.map(x => {
      var rr: String = "root";
      var dc: String = "";

      if (x.terminal) {
        dc = "shape = doublecircle, color=lightblue2, style=filled, ";
      }
      out += "N_" + formatHash(x.hash) + "[ " + dc + "label=\"N_" + formatHash(x.hash) + "\n" + x.variables + "\"];\n"; //data太多了不输出了
      //out += "N_" + formatHash(x.hash) + "[ " + dc + "label=\"N_" + formatHash(x.hash) + "\"];\n"; //输出Bigraph模型的HashCode作为名字
      x.target.map(y => {
        rr = "?";
        if (y._2 != null)
          rr = y._2.name;

        if (y._1 != null) {
          rr = rr + "\nSystem Clock: " + y._1.sysClk
          if (GlobalCfg.checkData && y._2.conds.size != 0)
            rr = rr + "\nCond:" + y._2.getConds
          if (GlobalCfg.checkHMM && y._2.hmms.size != 0)
            rr = rr + "\nHMM:" + y._2.getHMM
          out += " N_" + formatHash(x.hash) + " -> N_" + formatHash(y._1.hash) + "[color = purple  label = \"" + rr + "\"];\n"
        }
      });

    });
    out += "}\n";
    if (GlobalCfg.graphOutput != "") {
      var writer: Writer = new FileWriter(GlobalCfg.graphOutput);
      writer.write(out);
      writer.flush;
    }
    out;
  }

  //add by lbj
  def dumpDotFile(): String = {

    // 这是一个结果示例
    val resultExam =
      """
        |digraph reaction_graph {
        |   rankdir=LR;
        |   Node [shape = circle];
        |   BigSim_Report [shape = component color = forestgreen style=filled label="BigSim
        |Report"];
        |BigSim_Report -> N_A[color = aliceblue label = ""];
        | N_A
        | [shape=circle, color=lightblue2, style=filled tooltip="(a:Client[idle,a:edge].(c:Coin[idle,idle].nil|d:Coin[idle,idle].nil|e:Coin[idle,idle].nil|h:Register[idle].nil)|b:Client[idle,a:edge].(f:Coin[idle,idle].nil|g:Coin[idle,idle].nil|i:Register[idle].nil))"];
        |N_A[ label="N_A"];
        | N_A -> N_B[ color = coral1 label = "r_test3"];
        |N_B[ label="N_B" tooltip="(a:Client[idle,a:edge].(c:Coin[idle,idle].nil|d:Coin[idle,idle].nil|h:Register[idle].nil)|b:Client[idle,a:edge].(f:Coin[idle,idle].nil|g:Coin[idle,idle].nil|i:Register[idle].nil)|e:Coin[idle,idle].nil)"];
        |}
        |""".stripMargin

    var out: String = "";
    out = "digraph reaction_graph {\n";
    out += "   rankdir=LR;\n";
    out += "   Node [shape = circle];\n";
    out += "   BigSim_Report [shape = component color = forestgreen style=filled label=\"BigSim\nReport\"];\n"

    out += "BigSim_Report -> N_" + formatHash(root.hash) + "[color = aliceblue label = \"";
    if (!Data.getWeightExpr.equals("wExpr=")) { //读取agent的权重表达式，输出模拟报告 表示权重表达式不为空，即有权重表达式
      out += Data.getWeightExpr + "=" + Data.getReport + "\n"; //有权重表达式才输出到图上
    }
    out += "\"];\n";
    out += " N_" + formatHash(root.hash) + "\n" + " [shape=circle, color=lightblue2, style=filled tooltip=\"" + root.bigraph.root + "\"];\n"

    getConcurrenceRRs // 这个好像是基于节点的parents集合进行操作的，（目前应该还是可以使用的）   具体的作用我现在还是不太懂， 好像和并发反应规则有关系？？

    // lut : root.hash -> vertex,    checkPattern = true  x is vertex
    println(lut)
    lut.values.map(x => if (!GlobalCfg.checkPattern || (GlobalCfg.checkPattern && x.visited)) {
      var rr: String = "root";
      var dc: String = "";

      if (!x.terminal && x.violateRecordTracking) { //异常比记录优先级高，若后面发生异常则覆盖记录     // 先默认为false吧
        dc = "shape = circle, color=yellow, style=filled, ";
      }

      if (!x.terminal && GlobalCfg.checkPattern) { //终止节点的pattern flow就显示正常的蓝色
        if (x.reactionRules.size > 0) { // 这里考虑的是并发的反应规则
          x.reactionRules.map { y =>
            if (PatternFlow.patternDefRules.contains(y.name)) {
              dc = "shape = circle, color=green, style=filled, ";
            }
            if (PatternFlow.patternCUseRules.contains(y.name)) {
              dc = "shape = circle, color=orange, style=filled, ";
            } //cuse显示橙色
            if (PatternFlow.patternPUseRules.contains(y.name)) {
              dc = "shape = circle, color=pink, style=filled, ";
            } //puse显示粉色
          }
        }

        if (x.reactionRule != null) { // 对于正常的反应规则
          if (PatternFlow.patternDefRules.contains(x.reactionRule.name)) {
            dc = "shape = circle, color=green, style=filled, ";
          }
          if (PatternFlow.patternCUseRules.contains(x.reactionRule.name)) {
            dc = "shape = circle, color=orange, style=filled, ";
          } //cuse显示橙色
          if (PatternFlow.patternPUseRules.contains(x.reactionRule.name)) {
            dc = "shape = circle, color=pink, style=filled, ";
          } //puse显示粉色
        }
      }

      //logger.debug("GlobalCfg.violateSorting: "+GlobalCfg.violateSorting)
      //        if(x.terminal && x.sysClk>0 && GlobalCfg.violateSorting){//异常比记录优先级高，若后面发生异常则覆盖记录        
      //          dc = "shape = doublecircle, color=red, style=filled, ";
      //       }
      // violateExceptionTracking = false
      if (x.terminal && x.sysClk > 0) { //sysClk为0的terminal也会为true所以起始点被画圈了
        if (GlobalCfg.violateExceptionTracking || GlobalCfg.violateBinding || GlobalCfg.violateSorting) { //异常终止
          dc = "shape = doublecircle, color=red, style=filled, "
        } else { //正常终止
          dc = "shape = doublecircle, color=lightblue2, style=filled, "
        }
      } else if (x.terminal && GlobalCfg.SimulatorClass.startsWith("Enum")) {
        if (GlobalCfg.violateExceptionTracking || GlobalCfg.violateBinding || GlobalCfg.violateSorting) {
          dc = "shape = doublecircle, color=red, style=filled, "
        } else {
          dc = "shape = doublecircle, color=lightblue2, style=filled, "
        }
      }
      //logger.debug("[kgq] let see, the reactionRule is what", x.reactionRule)

      if (ctlCheck) { // 根据ctl检测结果对节点的属性进行设置，如果节点在返回的路径里面，且ctl检测结果为false，那么颜色为红，否则颜色为黄色
        if (recordPathInt.contains(x.hash)) {
          if (ctlCheckRes && pathType == CTLCheckResult.PathType.WitnessType) dc = "shape=doublecircle, color=yellow, style=filled, "
          else if (!ctlCheckRes && pathType == CTLCheckResult.PathType.CounterExample) dc = "shape = doublecircle, color=red, style=filled, "
        }
      }

      if (x.reactionRule == null) out += "N_" + formatHash(x.hash) + "[ " + dc + "label=\"N_" + formatHash(x.hash) + "\"];\n"
      else out += "N_" + formatHash(x.hash) + "[ " + dc + "label=\"N_" + formatHash(x.hash) + "\" tooltip=\"" + x.bigraph.root + "\"];\n"

      //Graph.dot += "N_" + formatHash(x.hash) + "[ " + dc + "label=\"N_" + formatHash(x.hash) + "\"];\n";

      if (x.target != null && x.target.size > 0) { //add by lbj 单条反应
        //logger.debug("This is a single reaction", x.target.size)
        // kgq  遍历 x.target ，根据每一个target，来生成一条“边”
        x.target.map(y => {
          rr = "?";
          // y._2 是反应规则，y._1 是 根据这条反应规则生成的vertex 首先根据反应规则的信息，把描述记录到rr中去
          if (y._2 != null) {
            rr = y._2.name;
            if (GlobalCfg.checkPattern) { // 搞不懂这些patternflow是用来干什么的
              if (PatternFlow.patternDefRules.contains(rr)) {
                var defStr = "(define: " + root.bigraph.pattern + ")";
                rr = rr + " " + defStr
              }
              if (PatternFlow.patternCUseRules.contains(rr)) {
                var cuseStr = "(cuse: " + root.bigraph.pattern + ")";
                rr = rr + " " + cuseStr
              }
              if (PatternFlow.patternPUseRules.contains(rr)) {
                var puseStr = "(puse: " + root.bigraph.pattern + ")";
                rr = rr + " " + puseStr
              }
            }
            // kgq x.violateRecordTracking = false (default)
            if (!x.terminal && x.violateRecordTracking) {
              var vioStr = "(violate: Tracking Record Assertion)";
              rr = rr + " " + vioStr
            }
            // kgq x.violateExceptionTrcking = false
            if (x.terminal && GlobalCfg.violateExceptionTracking) {
              var vioStr = "(violate: Tracking Exception Assertion)";
              rr = rr + " " + vioStr
            }
            // kgq x.violateBinding = false
            if (x.terminal && GlobalCfg.violateBinding) {
              var vioStr = "(violate: Tracking Binding Constraint)";
              rr = rr + " " + vioStr
            }
            // kgq violateSorging = false
            if (x.terminal && GlobalCfg.violateSorting) {
              var vioStr = "(violate: Tracking Sorting Constraint)";
              rr = rr + " " + vioStr
            }
          }
          // y._1 is next Vertex， 然后根据反应得到的下一个节点信息，来构造生成图中的边
          if (y._1 != null) {
            // kgq checkTime = false
            if (GlobalCfg.checkTime)
              rr = rr + "\nSystem Clock: " + y._1.sysClk
            // kgq checkData = true,  y._2.conds.size = 0
            if (GlobalCfg.checkData && y._2.conds.size != 0)
              rr = rr + "\nCond:" + y._2.getConds
            // kgq checkHMM = false
            if (GlobalCfg.checkHMM && y._2.hmms.size != 0)
              rr = rr + "\nHMM:" + y._2.getHMM
            // This is to add description of reactions, i.e. the edge in the graph
            out += " N_" + formatHash(x.hash) + " -> N_" + formatHash(y._1.hash) + "[ color = " + pathColor + " label = \"" + rr + "\"];\n"
          }
        })
      } else if (x.targets != null && x.targets.size > 0) { //并发反应
        logger.debug("This is a concurrence reaction", x.target.size) // kgq
        x.targets.map(y => {
          var rrs: String = "";

          if (y._1 != null) {
            rrs = "System Clock: " + y._1.sysClk + "\n"

            if (y._2 != null && y._2.size > 0) {
              var tarArry = y._2.toArray
              rrs = rrs + tarArry(0).name.toString()
              if (GlobalCfg.checkPattern) {
                if (PatternFlow.patternDefRules.contains(tarArry(0).name)) {
                  var defStr = "(define: " + root.bigraph.pattern + ")";
                  rrs = rrs + " " + defStr
                }
                if (PatternFlow.patternCUseRules.contains(tarArry(0).name)) {
                  var cuseStr = "(cuse: " + root.bigraph.pattern + ")";
                  rrs = rrs + " " + cuseStr
                }
                if (PatternFlow.patternPUseRules.contains(tarArry(0).name)) {
                  var puseStr = "(puse: " + root.bigraph.pattern + ")";
                  rrs = rrs + " " + puseStr
                }
              }
              if (!x.terminal && x.violateRecordTracking) {
                var vioStr = "(violate: Tracking Record Assertion)";
                rrs = rrs + " " + vioStr
              }
              if (x.terminal && GlobalCfg.violateExceptionTracking) {
                var vioStr = "(violate: Tracking Exception Assertion)";
                rrs = rrs + " " + vioStr
              }
              if (x.terminal && GlobalCfg.violateBinding) {
                var vioStr = "(violate: Tracking Binding Constraint)";
                rrs = rrs + " " + vioStr
              }
              if (x.terminal && GlobalCfg.violateSorting) {
                var vioStr = "(violate: Tracking Sorting Constraint)";
                rrs = rrs + " " + vioStr
              }
              for (i <- 1 to tarArry.size - 1) {
                rrs += "," + tarArry(i).name.toString() //逗号分隔当前并发反应的规则

                if (GlobalCfg.checkPattern) {
                  if (PatternFlow.patternDefRules.contains(tarArry(i).name)) {
                    var defStr = "(define: " + root.bigraph.pattern + ")";
                    rrs = rrs + " " + defStr
                  }
                  if (PatternFlow.patternCUseRules.contains(tarArry(i).name)) {
                    var cuseStr = "(cuse: " + root.bigraph.pattern + ")";
                    rrs = rrs + " " + cuseStr
                  }
                  if (PatternFlow.patternPUseRules.contains(tarArry(i).name)) {
                    var puseStr = "(puse: " + root.bigraph.pattern + ")";
                    rrs = rrs + " " + puseStr
                  }
                }
                if (!x.terminal && x.violateRecordTracking) {
                  var vioStr = "(violate: Tracking Record Assertion)";
                  rrs = rrs + " " + vioStr
                }
                if (x.terminal && GlobalCfg.violateExceptionTracking) {
                  var vioStr = "(violate: Tracking Exception Assertion)";
                  rrs = rrs + " " + vioStr
                }
                if (x.terminal && GlobalCfg.violateBinding) {
                  var vioStr = "(violate: Tracking Binding Constraint)";
                  rrs = rrs + " " + vioStr
                }
                if (x.terminal && GlobalCfg.violateSorting) {
                  var vioStr = "(violate: Tracking Sorting Constraint)";
                  rrs = rrs + " " + vioStr
                }
              }
            }
            out += " N_" + formatHash(x.hash) + " -> N_" + formatHash(y._1.hash) + "[ color = " + pathColor + " label = \"" + rrs + "\"];\n"
          }
        })
      }

    })
    //logger.debug("Graph.dot: " + out)

    out += "}\n";

    out
  }

  def getStateHashCode(): Int = {
    Math.abs(Random.nextString(10).hashCode)
  }

  def getConcurrenceRRs = {
    lut.values.map(x => { // 对于查找表中的每一个节点
      if (x.terminal && x.parents.size > 0 && !x.parents.contains(null)) { // 如果这个节点是终端节点 并且 其 parents 不为空，且parents 中不包括null
        addConcurrenceRRs(x)
      }
    });
  }

  def addConcurrenceRRs(v: Vertex) {
    if (!v.parents.contains(null) && v.parents.size > 0) {
      var parentArr: Array[Vertex] = new Array[Vertex](v.parents.size) // 声明一个array，大小是v.parents的大小，类型为vertex
      parentArr = v.parents.toArray // v.parents 本来是一个immutable.Set类型， 里面存放的是 当前这个节点的所有的父亲
      for (i <- 0 to parentArr.size - 1) {
        var lineSpendTime: Double = v.sysClk - parentArr(i).sysClk;
        parentArr(i).targets.map(y => {
          y._2.map(r => {
            if (r.sysClkIncr > lineSpendTime) { // 这个应该是根据反应规则的时间来判断的 当前这条线的反应时间
              //logger.debug("pathNum: " + parentArr(i).parent.getTargetsRRs(parentArr(i)).size)
              var newRRs: Set[ReactionRule] = parentArr(i).parent.getTargetsRRs(parentArr(i)).+=(r) // 先把对应的反应规则取出来，然后把当前的反应规则放进去
              //logger.debug("pathNum: " + newRRs.size)
              parentArr(i).parent.addTargets(newRRs, parentArr(i))
              r.sysClkIncr = r.sysClkIncr - lineSpendTime.intValue()
            }
          });
        });

        addConcurrenceRRs(parentArr(i))
      }
    }

  }

  var recordPathInt: List[Int] = List()
  var ctlCheckRes: Boolean = false
  var ctlCheck: Boolean = false
  var pathType: CTLCheckResult.PathType = CTLCheckResult.PathType.NoNeed

  def addCTLRes(recordPath: List[State], checkRes: Boolean, pathType: CTLCheckResult.PathType): Unit = {
    logger.debug("add CTL res: " + pathType + ", result: " + checkRes)
    this.recordPathInt = recordPath.map(x => x.getName.toInt)
    this.ctlCheckRes = checkRes
    this.ctlCheck = true
    this.pathType = pathType
  }

}

//object testMG{
//  def main(args: Array[String]): Unit = {
//    val BigSimPool = new ThreadPoolExecutor(10,10,
//      1L, TimeUnit.SECONDS,
//      new LinkedBlockingQueue,
//      new BigSimThreadFactory("Graph"),
//      new ThreadPoolExecutor.AbortPolicy);
//
//    val endGate = new CountDownLatch(9);
//
//
//    var paths: Set[Int] = Set()
//    paths.add(1)
//    paths.add(2)
//    paths.add(3)
//    paths.add(4)
//    paths.add(5)
//    paths.add(6)
//    paths.add(7)
//    paths.add(8)
//    paths.add(9)
//    paths.add(10)
//
//    try {
//      paths.foreach(ite => {
//        val task = new Runnable() {
//          @Override
//          def run() {
//            try {
//              //code
//              println(ite)
//            } catch {
//              case e: Exception => {
//                e.printStackTrace()
//              }
//            }finally {
//              endGate.countDown();
//            }
//          }
//        }
//        BigSimPool.execute(task);
//      })
//      endGate.await();
//    } catch {
//      case e: Exception => {
//        e.printStackTrace()
//      }
//    } finally {
//      BigSimPool.shutdown()
//    }
//
//
//
//
////    try {
////      val a = 0;
////      for (a <- 1 to 10) {
////        val task = new Runnable() {
////          @Override
////          def run() {
////            try {
////              //code
////              println(paths.)
////            } catch {
////              case e: Exception => {
////                e.printStackTrace()
////              }
////            }finally {
////              endGate.countDown();
////              println("fin")
////            }
////          }
////        };
////        BigSimPool.execute(task);
////      }
////      endGate.await();
////    } catch {
////      case e: Exception => {
////        e.printStackTrace()
////      }
////    } finally {
////      BigSimPool.shutdown()
////    }
//    print("end")
//    print(endGate)
//  }
//}