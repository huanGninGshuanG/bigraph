package org.bigraph.bigsim.simulator

import org.bigraph.bigsim.BRS.{Graph, Match, Vertex}
import org.bigraph.bigsim.Verify
import org.bigraph.bigsim.ctlspec._
import org.bigraph.bigsim.ctlspec.atom._
import org.bigraph.bigsim.ctlspec.operator._
import org.bigraph.bigsim.model.{Bigraph, Nil, ReactionRule}
import org.bigraph.bigsim.utils.GlobalCfg
import org.slf4j.{Logger, LoggerFactory}
import scala.collection.mutable.{Queue, Stack}

class CTLSpec(ctlSpec: List[String], ctlProp: Map[String, Tuple2[String, String]]) {            // 在这个小括号里定义的变量，会自动成为类的成员，不需要在类的内部增加新的定义

  val prior_map = Map( //  对操作符的优先级进行定义，最低的是: (), ->其次，然后或 且，最高的是！AEUFXG
    "(" -> 0, ")" -> 0,
    "-" -> 1, ">" -> 1,
    "|" -> 2, "&" -> 2,
    "!" -> 3,
    "A" -> 3, "E" -> 3,
    "U" -> 3, "F" -> 3, "X" -> 3, "G" -> 3,
  ) // 数字越大，优先级越高，括号的优先级最低，因为后面来什么操作符，遇到左括号都要入栈

  val _formula = getCTLFormula(ctlSpec, ctlProp)

  var debug: Boolean = true

  def getCTLFormula(): Formula = {
    this._formula
  }

  def getCTLFormula(ctlSpec: List[String], ctlProp: Map[String, Tuple2[String, String]]): Formula = {
    debug = true
    var formula: Stack[Formula] = Stack();
    var operand: Stack[String] = Stack();
    var Sbuff: String = ""; // 为了扩展原子命题的名称，采用Sbuff来暂存字符

    def oneOp() = { // 一次操作，先从操作符栈中取出来最上面的，然后根据情况，选择从公式栈中取出，构造当前的公式，然后再放入公式栈
      val top_o: String = operand.pop()
      if (debug) print("\t\t\toneOp, operand: " + operand)
      //println("process:", top_o)
      top_o match {
        case "!" => {
          val top_f = formula.pop()
          val currentF = new Not(top_f) // !
          formula.push(currentF)
          if (debug) println("\t [!] top_f: " + top_f + " formula.push " + formula)
        }
        case "&" => {
          val top_f1 = formula.pop()
          val top_f2 = formula.pop()
          val currentF = new And(top_f1, top_f2) // &
          formula.push(currentF)
          if (debug) println("\t [&] top_f1: " + top_f1 + " top_f2: " + top_f2 + " formula.push " + formula)
        }
        case "|" => {
          val top_f1 = formula.pop()
          val top_f2 = formula.pop()
          val currentF = new Or(top_f1, top_f2) // |
          formula.push(currentF)
          if (debug) println("\t [|] top_f1: " + top_f1 + " top_f2: " + top_f2 + " formula.push " + formula)
        }
        case ">" => {
          val top_f1 = formula.pop()
          val top_f2 = formula.pop()
          val top_o = operand.pop()
          val currentF = new Imply(top_f2, top_f1) // ->
          formula.push(currentF)
          if (debug) println("\t [>] top_f1: " + top_f1 + " top_f2: " + top_f2 + " top_o: " + top_o + " formula.push " + formula)
        }
        case "X" => {
          val top_f = formula.pop()
          val top_o2 = operand.pop()
          if (debug) print("\t [X] top_f: " + top_f + " top_o2: " + top_o2)
          top_o2 match {
            case "A" => {
              val currentF = new AX(top_f) //AX
              formula.push(currentF)
              if (debug) println("\t [A] formula.push " + formula)
            }
            case "E" => {
              val currentF = new EX(top_f) //EX
              formula.push(currentF)
              if (debug) println("\t [E] formula.push " + formula)
            }
          }
        }
        case "G" => {
          val top_f = formula.pop()
          val top_o2 = operand.pop()
          if (debug) print("\t [G] top_f: " + top_f + " top_o2: " + top_o2)
          top_o2 match {
            case "A" => {
              val currentF = new AG(top_f) //AG
              formula.push(currentF)
              if (debug) println("\t [A] formula.push " + formula)
            }
            case "E" => {
              val currentF = new EG(top_f) //EG
              formula.push(currentF)
              if (debug) println("\t [E] formula.push " + formula)
            }
          }
        }
        case "F" => {
          val top_f = formula.pop()
          val top_o2 = operand.pop()
          if (debug) print("\t [F] top_f: " + top_f + " top_o2: " + top_o2)
          top_o2 match {
            case "A" => {
              val currentF = new AF(top_f) // AF
              formula.push(currentF)
              if (debug) println("\t [A] formula.push " + formula)
            }
            case "E" => {
              val currentF = new EF(top_f) // EF
              formula.push(currentF)
              if (debug) println("\t [E] formula.push " + formula)
            }
          }
        }
        case "U" => {
          val top_f1 = formula.pop()
          val top_f2 = formula.pop()
          val top_l = operand.pop()
          val top_o2 = operand.pop()
          if (debug) print("\t [U] top_f1: " + top_f1 + " top_f2: " + top_f2 + " top_o2: " + top_o2)
          top_o2 match {
            case "A" => {
              val currentF = new AU(top_f2, top_f1) //AU
              formula.push(currentF)
              if (debug) println("\t [A] formula.push " + formula)
            }
            case "E" => {
              val currentF = new EU(top_f2, top_f1) // EU
              formula.push(currentF)
              if (debug) println("\t [E] formula.push " + formula)
            }
          }
          operand.push("(")   // 如果是 AU公式， A(a U b) 那么右括号匹配的是A之前的左括号，上面 top_l 的时候将左括号取出来了，为了上左右括号个数匹配，需要再放回去一个
        }
      }
    } // 进行一步构造操作

    if (ctlSpec.isEmpty || ctlProp.isEmpty) { //如果有一个是空的，那么直接返回 True公式，也就是认为永远都是对的
      return new True();
    }
    ctlSpec.foreach(x => { // 【kgq】 对于每一条规约进行处理
      if (debug) println("ctlSpec.foreach: 处理一条规约: " + x)
      x.foreach(y => { // 【kgq】 对于某条规约中的每一个字符
        if (debug) println("\tx.foreach: 处理当前规约: " + x + " 中的字符: " + y)

        if (y == " ") {
          if (debug) println("\t\t当前是一个 space")
        }
        else if (y >= 'a' && y <= 'z') {
          if (debug) println("\t\t当前是一个小写字母，暂存")
          Sbuff += y // 【kgq】 先把字符暂存起来
        }
        else {
          if (debug) println("\t\t不是小写字母，处理：")
          if (Sbuff != "") { // 【kgq】 遇到其他字符，就先判断字符缓冲区中是不是有内容
            if (debug) print("\t\t\t判断Sbuff缓存不为空: " + Sbuff)
            if (Sbuff == "true") { // 【kgq】 如果暂存的内容是true，则新建true公式，放入公式栈  （原子命题还有true false，只能是全小写）
              val currentF = new True();
              formula.push(currentF)
              if (debug) println("\t 创建公式 new True() " + formula)
            } else if (Sbuff == "false") { // 【kgq】如果暂存的是false，则新建false公式，放入公式栈
              val currentF = new False();
              formula.push(currentF)
              if (debug) println("\t 创建公式 new False() " + formula)
            } else {
              //val currentF = new Atom(ctlProp(Sbuff)) // 【kgq】 否则，暂存的是原子命题的名称，那么新建原子命题，放入公式栈
              val currentF = new Atom(Sbuff)
              formula.push(currentF)
              if (debug) println("\t 创建公式 new Atom(" + Sbuff + ") " + formula)
            }
            Sbuff = ""
          }
          // 继续处理当前的字符
          if (y == '(') { //这里遍历的y 是字符Char， 要转换成String才能放到operand中去
            if (debug) println("\t\t当前是左括号，压栈, operand.push(y.toString): " + operand)
            operand.push(y.toString)
          }
          else if (y == ')') { // 如果遍历到了右括号，那么开始迭代公式栈，构造公式，直到遇到左括号
            //println("遍历到了右括号",y)
            if (debug) println("\t\t当前是右括号，循环找左括号: " + operand)
            while (!operand.isEmpty && operand.top != "(") // 取出操作符，来构造
              oneOp()
            if (debug) println("\t\t要取出左括号: " + operand)
            val checkpop = operand.pop()     //最后要取出左括号
          }
          else if (this.prior_map.contains(y.toString)) { // 说明当前的是一个操作符
            //println("当前是一个操作符",y)
            //println(operand)
            while (!operand.isEmpty && (this.prior_map(operand.top.toString) > this.prior_map(y.toString))) { // 如果操作符栈顶的操作符优先级高于当前操作符，那么就执行一步构造
              //println(operand.top.toString)
              if (debug) println("\t\t\t 优先级低于栈顶操作符: " + operand)
              oneOp()
            }
            operand.push(y.toString) //最后把当前操作符放入到操作符栈中去, 此时，当前操作符优先级 >= 栈顶操作符优先级
            if (debug) println("\t\t当前是一个操作符: " + operand)
          }
        }
      })
    })
    while (!operand.isEmpty)
      oneOp()
    formula.pop()
  }
}

object TestCTLFunc {

  def main(args: Array[String]): Unit = {
    val example = "A((AX(a))U!c)"
    val ctlexample = List(example)
    val propexample: Map[String, Tuple2[String, String]] = Map(
      "ab" -> ("agent[a]", ""),
      "ba" -> ("agent[b]", "")
    )
    val ctlParser = new CTLSpec(ctlexample, propexample)
    println("InputSpec: ", ctlexample)
    println("InputProp: ", propexample)
    println("Ori Output:", ctlParser.getCTLFormula())
    println("Output:" , ctlParser.getCTLFormula().convertToENF())
  }
}
