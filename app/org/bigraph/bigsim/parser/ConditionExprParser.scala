package org.bigraph.bigsim.parser

import org.bigraph.bigsim._

import scala.util.parsing.combinator.syntactical.StandardTokenParsers
import scala.util.parsing.combinator.JavaTokenParsers
import org.bigraph.bigsim.data.DataModel
import org.bigraph.bigsim.utils._
import org.bigraph.bigsim.model.Term

import scala.collection.mutable.Set

class Cond {
  override def toString = "<uninitialised cond>";
  def calculate(getValue:(String) => Double): Boolean = {
    false;
  }
}

class CondRela(lt: Arith, op: String, rt: Arith) extends Cond{
  override def toString = lt + op + rt
  override def calculate(getValue:(String) => Double): Boolean = {
    val ltr = lt.calculate(getValue)
    val rtr = rt.calculate(getValue)
    //println("ltr: " + ltr + " rtr: " + rtr + " op " + op)
    op match {
      case "!=" => ltr != rtr;
      case "==" => ltr == rtr;
      case "<=" => ltr <= rtr;
      case ">=" => {
        ltr >= rtr
      };
      case ">" => ltr > rtr;
      case "<" => ltr < rtr;
      case _ => {
        println("CondRela.calculate wrong operator " + op)
        false
      }
    }
  }
}

class CondNot(relaExpr: CondRela) extends Cond{
  override def toString = "CondNot:(!" + relaExpr + ")"
  override def calculate(getValue:(String) => Double): Boolean = {
    !relaExpr.calculate(getValue)
  }
}

class CondAnd(lt: Cond, rt: Cond) extends Cond{
  override def toString = "CondAnd:(" + lt + " && " + rt + ")"
  override def calculate(getValue:(String) => Double): Boolean = {
    val lres = lt.calculate(getValue)
    val rres = rt.calculate(getValue)
    if (lres && rres)
      true
    else
      false
  }
}

class CondOr(lt: Cond, rt: Cond) extends Cond{
  override def toString = "CondOr:(" + lt + " || " + rt + ")"
  override def calculate(getValue:(String) => Double): Boolean = {
    val lres = lt.calculate(getValue)
    val rres = rt.calculate(getValue)
    if (lres || rres)
      true
    else
      false
  }
}

object ConditionExprParser extends JavaTokenParsers {
  lazy val relaOp: Parser[String] = "!=" | "==" | ">=" | "<=" | ">" | "<"; // 关系运算符
  lazy val logicAnd: Parser[String] = "&&"
  lazy val logicOr: Parser[String] = "||"
  lazy val numericLit: Parser[String] = """\d+(\.\d*)?""".r // 数字字面量
  lazy val variable: Parser[String] = """(\d*)?+[a-zA-z]+(\d*)?+(\.[a-zA-Z]*)*""".r // 变量

  lazy val factor: Parser[String] = ("abs(" ~> expr <~ ")" ^^ { x => "abs(" + x + ")"}) | ("(" ~> expr <~ ")") ^^ { x => "(" + x + ")"} | variable | numericLit
  lazy val term: Parser[String] = factor ~ rep("*" ~ factor | "/" ~ factor | "%" ~ factor) ^^ {
    case number ~ list => (number /: list) {
      case (x, op ~ y) => x + op + y
    }
  }
  lazy val expr: Parser[String] = term ~ rep("+" ~ term | "-" ~ term) ^^ {
    case number ~ list => list.foldLeft(number) {
      case (x, op ~ y) => x + op + y
    }
  }
  lazy val arithExpr: Parser[Arith] = expr ^^ {case x => ArithmeticExprParser.apply(x)}         // 解析其中的算术表达式部分

  lazy val relaExpr: Parser[CondRela] = "(" ~> arithExpr ~ relaOp ~ arithExpr <~ ")" ^^ {       // 由两个算术表达式，加上一个关系运算符，得到一个关系表达式
    case l ~ op ~ r => new CondRela(l, op, r)
  } | arithExpr ~ relaOp ~ arithExpr ^^ {
    case l ~ op ~ r => new CondRela(l, op, r)
  }

  lazy val condNot: Parser[CondNot] = "!" ~> relaExpr ^^ {
    case x => new CondNot(x)
  }
  lazy val condAndWord: Parser[Cond] = condNot | relaExpr
  lazy val condAnd: Parser[Cond] = condAndWord ~ logicAnd ~ condAnd ^^ {
    case l ~ op ~ r => new CondAnd(l, r)
  } | condAndWord ~ logicAnd ~ condAndWord ^^ {
    case l ~ op ~ r => new CondAnd(l, r)
  }

  lazy val condOrWord: Parser[Cond] = condAnd | relaExpr | condNot
  lazy val condOr: Parser[Cond] = condOrWord ~ logicOr ~ condOr ^^ {
    case l ~ op ~ r => new CondOr(l, r)
  } | condOrWord ~ logicOr ~ condOrWord ^^ {
    case l ~ op ~ r => new CondOr(l, r)
  }

  lazy val condition: Parser[Cond] = condOr | condAnd | relaExpr | condNot

  def parse(s: String): Cond = parseAll(condition, s) match {
    case Success(res, _) => res
    case e => throw new Exception(e.toString)
  }
}

object testCondExprParser {

  def main(args: Array[String]) {
    var intest = "SC.Account.ID==SC.ContractAccount.ID"
    println("input is: " + intest + " \nparser result: " + ConditionExprParser.parse(intest))

    intest = "SC.Account.blc+10<=SC.blc||SC.blc>10"
    println("input is: " + intest + " \nparser result: " + ConditionExprParser.parse(intest))

    intest = "a1.Number==(2-Container.Count)*a2.Number+2"
    println("input is: " + intest + " \nparser result: " + ConditionExprParser.parse(intest))


    var testlist: scala.collection.mutable.ListBuffer[String] = scala.collection.mutable.ListBuffer()
    println(testlist += "gan")
    testlist += "cao"
    println(testlist)
    println(testlist(1))
  }
}