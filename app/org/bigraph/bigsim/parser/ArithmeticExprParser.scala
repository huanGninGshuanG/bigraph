package org.bigraph.bigsim.parser

import scala.util.parsing.combinator.RegexParsers
import org.bigraph.bigsim.data.Data
import org.bigraph.bigsim.value.Value

class Arith {
  override  def toString = "<uninitialised arith>";

  def debug(): Boolean = {
    false
  }

  def check(): Boolean = {
    false;
  }

  def calculate(getValue:(String) => Double): Double = {
    0
  }
}

class ArithVariable(n: String) extends Arith {
  val name: String = n;
  override def toString = "(" + name + ")"
  override def calculate(getValue:(String) => Double): Double = {
    var ret = getValue(name)
    if (debug())
      println("node: " + name + " value: " + ret)
    ret
  }
}

class ArithNumber(v: String) extends Arith {
  val value: Double = v.toDouble
  override def toString = "(" + value.toString + ")"
  override def calculate(getValue:(String) => Double): Double = {
    value
  }
}

class ArithAbs(e: Arith) extends Arith {
  val expression: Arith = e
  override def toString = "abs(" + expression + ")"
  override def calculate(getValue:(String) => Double): Double = {
    Math.abs(expression.calculate(getValue))
  }
}

class ArithBinOp(lt: Arith, op: String, rt: Arith) extends Arith {
  val leftArith: Arith = lt
  val rightArith: Arith = rt
  val arithOp: String = op
  override def toString = "(" + leftArith + arithOp + rightArith + ")"
  override def calculate(getValue:(String) => Double): Double = {
    val lcal = leftArith.calculate(getValue)
    val rcal = rightArith.calculate(getValue)
    if (debug())
      println("ltr: " + lcal + " rtr: " + rcal + " op " + op)
    arithOp match {
      case "*" => lcal * rcal
      case "/" => lcal / rcal
      case "+" => lcal + rcal
      case "-" => lcal - rcal
      case "%" => lcal % rcal
    }
  }
}

object ArithmeticExprParser extends RegexParsers {

  def number: Parser[ArithNumber] = """\d+(\.\d*)?""".r ^^ { x => new ArithNumber(x)}
  def variable: Parser[ArithVariable] = """[a-zA-Z]+(\d*)?+(\.[a-zA-Z]*)*""".r ^^ { x => new ArithVariable(x)}

  def factor: Parser[Arith] = ("abs(" ~> expr <~ ")" ^^ {x => new ArithAbs(x)}) | "(" ~> expr <~ ")" | variable | number
  def term: Parser[Arith] = factor ~ rep("*" ~ factor | "/" ~ factor | "%" ~ factor) ^^ {
    case number ~ list => (number /: list) {
      case (x, "*" ~ y) => new ArithBinOp(x, "*", y)
      case (x, "/" ~ y) => new ArithBinOp(x, "/", y)
      case (x, "%" ~ y) => new ArithBinOp(x, "%", y)
    }
  }

  def expr: Parser[Arith] = term ~ rep("+" ~ term | "-" ~ term) ^^ {
    case nu ~ list => list.foldLeft(nu) {
      case (x, "+" ~ y) => new ArithBinOp(x, "+", y)
      case (x, "-" ~ y) => new ArithBinOp(x, "-", y)
    }
  }

  def apply(input: String): Arith = parseAll(expr, input) match {
  case Success(result, _) => result
  case failure: NoSuccess => scala.sys.error(failure.msg)
}

  def main(args: Array[String]) {
    var exp = "m1.power+energy*15+(0.5%500)";
    var exp2 = "(2-Container.Count)*a2.Number+2"
    println(apply(exp2))
  }
}