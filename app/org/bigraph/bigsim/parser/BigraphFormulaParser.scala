package org.bigraph.bigsim.parser

/**
 * @author kongguanqiao
 */

import scala.util.parsing.combinator.syntactical.StandardTokenParsers
import scala.util.parsing.combinator.JavaTokenParsers
import org.bigraph.bigsim.data.DataModel
import org.bigraph.bigsim.utils._
import org.bigraph.bigsim.model._
import java.lang.NumberFormatException

import org.bigraph.bigsim.bigraphoperation.BigraphOperation
import org.bigraph.bigsim.simulator.EnumSimulator

class BgFormula {
  override def toString = "<uninitialised bgFormula>"
}

class BgAgent(n: String) extends BgFormula {
  val name = n
  override def toString = "(Agent:" + name + ")"
}

class BgCompose(l: BgFormula, r: BgFormula) extends BgFormula {
  val leftBigraph = l
  val rightBigraph = r

  override def toString = "(Compose:" + l + "○" + r +")"
}

class BgParallel(l: BgFormula, r: BgFormula) extends BgFormula {
  val leftBigraph = l
  val rightBigraph = r

  override def toString = "(Paraller:" + l + "||" + r + ")"
}

class BgMerge(l: BgFormula, r: BgFormula) extends BgFormula {
  val leftBigraph = l
  val rightBigraph = r

  override def toString = "(Merge:" + l + "|" + r + ")"
}

class BgJuxtapose(l: BgFormula, r: BgFormula) extends BgFormula{
  val leftBigraph = l
  val rightBigraph = r

  override def toString = "(Juxtapose: " + l + "⊗" + r + ")"
}


/**
 * 解析偶图组合公式
 */
object BigraphFormulaParser extends StandardTokenParsers {

  var DEBUG: Boolean = false

  // delimiters： 分隔符
  lexical.delimiters ++= List("(", ")", "||", "|", "○", "⊗", ".")
  lazy val baseAgent: Parser[Term] = ident ^^ { s => BigraphOperation.getOpBigraph(s)}

  lazy val factor: Parser[Term] = baseAgent | ("(" ~> bgformula <~ ")")

  lazy val bgformula: Parser[Term] = compose | parallel | merge | juxtapose | nesting | factor;

  lazy val compose: Parser[Term] = (factor ~ ("○" ~> bgformula)) ^^ {
    case l ~ r => {
      val opres = BigraphOperation.compose(l, r)
      if (!opres._2) {
        if (DEBUG){
          println(opres._3)
        }
      }
      opres._1
    }
  }

  lazy val parallel: Parser[Term] = (factor ~ ("||" ~> bgformula)) ^^ {
    case l ~ r => BigraphOperation.parallel(l, r)._1
  }

  lazy val merge: Parser[Term] = (factor ~ ("|" ~> bgformula)) ^^ {
    case l ~ r => BigraphOperation.merge(l, r)._1
  }

  lazy val juxtapose: Parser[Term] = (factor ~ ("⊗" ~> bgformula)) ^^ {
    case l ~ r =>BigraphOperation.juxtapose(l, r)._1
  }

  lazy val nesting: Parser[Term] = (factor ~ ("." ~> bgformula)) ^^ {
    case l ~ r => BigraphOperation.nesting(l, r)._1
  }


  def parse(s: String) = {
    val tokens = new lexical.Scanner(s)
    phrase(bgformula)(tokens)
  }
  def apply(s: String): Term = {
    parse(s) match {
      case Success(tree, _) => tree;
      case e: NoSuccess => {
        Console.err.println(e);
        throw new IllegalArgumentException("Bad syntax: " + s);
      }
    }
  }
}

// 测试用例暂时不要删，版本完成的时候再处理
object testBigraphFormulaParser {

  def typeToString(termType: Int): String = {
    termType match {
      case TermType.TPREF => "Prefix";
      case TermType.TPAR => "Paraller";
      case TermType.THOLE => "Hole";
      case TermType.TNIL => "Nil";
      case TermType.TREGION => "Regions";
      case TermType.TNUM  => "Num";
      case _ => "Undefined TermType";
    }
  }

  def main(args: Array[String]) {
    val exam =
      """
        |# Controls
        |%active Greater : 2;
        |%active M : 1;
        |%active K : 2;
        |%active L : 0;
        |
        |# Rules
        |%rule r_DEBUG31 a1:Container.(c:Count[idle] | n1:Number[idle]) -> a1:Container.(c:Count[b:edge] | n1:Number[b:edge]){};
        |
        |# Op-bigraph
        |%op-bigraph attackcount nil;
        |%op-bigraph attacker attackerc:User[idle].(attack:Fallback[idle,idle] | c1:Count[idle] | c2:Count[idle] | attackers:Save[idle].attackersm:Money<4>[idle,idle] | attackert:TakeOut[idle].attackertm:Money<4>[idle,idle] | $0);
        |%op-bigraph bank bankc:Bank.(deposit:Deposit[idle,idle].depg:Gas[idle].depm:Money<1>[idle,idle] | withdraw:WithDraw[idle,idle,idle,idle].witg:Gas[idle].witm:Money<1>[idle,idle] | a1:BankAccount[idle].(a1a:Address<0>[idle] | a1m:Money<8>[idle,idle]) | a2:BankAccount[idle].(a2a:Address<1>[idle] | a2m:Money<8>[idle,idle]) | $0);
        |%op-bigraph minerother nil;
        |%op-bigraph bankother nil;
        |%op-bigraph miner miner:Miner.(bank:SC_RT[idle].(banka:Address<2>[idle] | bankb:Balance[idle].bankm:Money<20>[idle,idle] | $2) | user:SC_RT[idle].(usera:Address<0>[idle] | userb:Balance[idle].userm:Money<5>[idle,idle] | $1) | minerb:Balance[idle].minerm:Money<0>[idle,idle] | $0);
        |%op-bigraph user userc:User[idle].(fallback:Fallback[idle,idle] | users:Save[idle].usersm:Money<4>[idle,idle] | usert:TakeOut[idle].usertm:Money<4>[idle,idle]);
        |
        |
        |# OP_Formula
        |%bigraphformula miner○(minerother⊗user⊗(bank○bankother));
        |
        |#SortingLogic
        |
        |# Go!
        |%check;
        |""".stripMargin

    val exam2 =
      """
        |# Controls
        |%active Greater : 2;
        |%active M : 1;
        |%active K : 3;
        |%active L : 2;
        |
        |# Rules
        |%rule r_DEBUG31 a1:Container.(c:Count[idle] | n1:Number[idle]) -> a1:Container.(c:Count[b:edge] | n1:Number[b:edge]){};
        |
        |# Op-bigraph
        |%op-bigraph A v0:K[x:outername,y:outername,z:outername].$0;
        |%op-bigraph B v1:L[y:outername,z:outername].$0;
        |
        |# BigraphFormula
        |%bigraphformula A.B;
        |
        |#SortingLogic
        |
        |# Go!
        |%check;
        |""".stripMargin

    var exam3 =
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
        |%active Plus : 3;
        |%active Minus : 3;
        |%active Multiply : 3;
        |%active Division : 3;
        |%active Opposite : 2;
        |%active Abs : 2;
        |%active Address : 1;
        |%active Money : 2;
        |%active Balance : 1;
        |%active Bank : 0;
        |%active BankAccount : 1;
        |%active Count : 1;
        |%active Deposit : 2;
        |%active Fallback : 2;
        |%active Gas : 1;
        |%active Miner : 0;
        |%active SC_RT : 1;
        |%active User : 1;
        |%active WithDraw : 4;
        |%active Save : 1;
        |%active TakeOut : 1;
        |
        |# Rules
        |%rule r_0 user:SC_RT[idle].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(Fallback[idle,idle] | TakeOut[idle].Money[idle,idle] | $0)) | bank:SC_RT[idle].(banka:Address[idle] | bankb:Balance[idle].bankm:Money[idle,idle] | bankc:Bank.(BankAccount[idle].(Money[idle,idle] | Address[idle]) | withdraw:WithDraw[idle,idle,idle,idle].witg:Gas[idle].witm:Money[idle,idle] | $1)) -> user:SC_RT[idle].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(Fallback[idle,idle] | TakeOut[idle].Money[idle,idle] | $0)) | bank:SC_RT[idle].(banka:Address[idle] | bankb:Balance[idle].bankm:Money[idle,idle] | bankc:Bank.(BankAccount[idle].(Money[idle,idle] | Address[idle]) | withdraw:WithDraw[idle,idle,idle,idle].witg:Gas[idle].witm:Money[idle,idle] | $1)){};
        |%rule r_1 user:SC_RT[idle].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(Fallback[idle,idle] | TakeOut[idle].Money[idle,idle] | $0)) | bank:SC_RT[idle].(banka:Address[idle] | bankb:Balance[idle].bankm:Money[idle,idle] | bankc:Bank.(withdraw:WithDraw[idle,idle,idle,idle].witg:Gas[idle].witm:Money[idle,idle] | $1)) -> user:SC_RT[a:edge].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(Fallback[idle,idle] | TakeOut[a:edge].Money[idle,idle] | $0)) | bank:SC_RT[idle].(banka:Address[idle] | bankb:Balance[idle].bankm:Money[idle,idle] | bankc:Bank.(withdraw:WithDraw[a:edge,idle,idle,idle].witg:Gas[idle].witm:Money[idle,idle] | $1)){};
        |%rule r_2 user:SC_RT[a:edge].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(Fallback[idle,idle] | TakeOut[a:edge].Money[idle,idle] | $0)) | bank:SC_RT[idle].(banka:Address[idle] | bankb:Balance[idle].bankm:Money[idle,idle] | bankc:Bank.(BankAccount[idle].(Money[idle,idle] | Address[idle]) | withdraw:WithDraw[a:edge,idle,idle,idle].witg:Gas[idle].witm:Money[idle,idle] | $1)) -> user:SC_RT[a:edge].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(Fallback[idle,idle] | TakeOut[a:edge].Money[idle,idle] | $0)) | bank:SC_RT[idle].(banka:Address[idle] | bankb:Balance[idle].bankm:Money[idle,idle] | bankc:Bank.(BankAccount[b:edge].(Money[idle,idle] | Address[idle]) | withdraw:WithDraw[a:edge,b:edge,idle,idle].witg:Gas[idle].witm:Money[idle,idle] | $1)){Condition:user.Address==bank.Bank.BankAccount.Address};
        |%rule r_3 user:SC_RT[a:edge].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(Fallback[idle,idle] | TakeOut[a:edge].Money[idle,idle] | $0) | $2) | bank:SC_RT[idle].(banka:Address[idle] | bankb:Balance[idle].bankm:Money[idle,idle] | bankc:Bank.(BankAccount[b:edge].(Money[idle,idle] | Address[idle]) | withdraw:WithDraw[a:edge,b:edge,idle,idle].witg:Gas[idle].witm:Money[idle,idle] | $1)) -> user:SC_RT[a:edge].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(Fallback[b:edge,idle] | TakeOut[a:edge].Money[idle,idle] | $0) | $2) | bank:SC_RT[idle].(banka:Address[idle] | bankb:Balance[idle].bankm:Money[idle,idle] | bankc:Bank.(BankAccount[b:edge].(Money[idle,idle] | Address[idle]) | withdraw:WithDraw[a:edge,idle,idle,b:edge].witg:Gas[idle].witm:Money[idle,idle] | $1)){Condition:user.Balance.Money >= bank.Bank.WithDraw.Gas.Money && user.User.TakeOut.Money <= bank.Bank.BankAccount.Money};
        |%rule r_4 user:SC_RT[a:edge].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(Fallback[idle,idle] | TakeOut[a:edge].Money[idle,idle] | $0)) | bank:SC_RT[idle].(banka:Address[idle] | bankb:Balance[idle].bankm:Money[idle,idle] | a:Bank.(BankAccount[b:edge].(Money[idle,idle] | Address[idle]) | withdraw:WithDraw[a:edge,b:edge,idle,idle].witg:Gas[idle].witm:Money[idle,idle] | $1)) -> user:SC_RT[idle].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(Fallback[idle,idle] | TakeOut[idle].Money[idle,idle] | $0)) | bank:SC_RT[idle].(banka:Address[idle] | bankb:Balance[idle].bankm:Money[idle,idle] | a:Bank.(BankAccount[idle].(Money[idle,idle] | Address[idle]) | withdraw:WithDraw[a:edge,b:edge,idle,idle].witg:Gas[idle].witm:Money[idle,idle] | $1)){Condition:user.Balance.Money<bank.Bank.WithDraw.Gas.Money||user.User.TakeOut.Money>bank.Bank.BankAccount.Money};
        |%rule r_5 user:SC_RT[a:edge].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(Fallback[b:edge,idle] | TakeOut[a:edge].Money[idle,idle] | $0)) | bank:SC_RT[idle].(banka:Address[idle] | bankb:Balance[idle].bankm:Money[idle,idle] | bankc:Bank.(BankAccount[b:edge].(Money[idle,idle] | Address[idle]) | withdraw:WithDraw[a:edge,idle,idle,b:edge].witg:Gas[idle].witm:Money[idle,idle] | $1)) | minerb:Balance[idle].minerm:Money[idle,idle] -> user:SC_RT[a:edge].(usera:Address[idle] | userb:Balance[idle].userm:Money[g:edge,c:edge] | User[idle].(Fallback[idle,idle] | TakeOut[a:edge].Money[idle,d:edge] | $0)) | bank:SC_RT[idle].(banka:Address[idle] | bankb:Balance[idle].bankm:Money[i:edge,h:edge] | bankc:Bank.(BankAccount[b:edge].(Money[idle,idle] | Address[idle]) | withdraw:WithDraw[a:edge,idle,b:edge,idle].witg:Gas[idle].witm:Money[idle,f:edge] | $1)) | minerb:Balance[idle].minerm:Money[k:edge,j:edge] | a:Plus[c:edge,e:edge,d:edge] | b:Minus[f:edge,g:edge,e:edge] | c:Minus[d:edge,i:edge,h:edge] | d:Plus[f:edge,k:edge,j:edge]{};
        |%rule r_6 user:SC_RT[a:edge].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(Fallback[idle,idle] | TakeOut[a:edge].Money[idle,idle] | $0)) | bank:SC_RT[idle].(banka:Address[idle] | bankb:Balance[idle].bankm:Money[idle,idle] | bankc:Bank.(BankAccount[b:edge].(Money[idle,idle] | Address[idle]) | withdraw:WithDraw[a:edge,idle,b:edge,idle].witg:Gas[idle].witm:Money[idle,idle] | $1)) -> user:SC_RT[idle].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(Fallback[idle,idle] | TakeOut[a:edge].Money[idle,c:edge] | $0)) | bank:SC_RT[idle].(banka:Address[idle] | bankb:Balance[idle].bankm:Money[idle,idle] | bankc:Bank.(BankAccount[idle].(Money[e:edge,d:edge] | Address[idle]) | withdraw:WithDraw[idle,idle,b:edge,idle].witg:Gas[idle].witm:Money[idle,idle] | $1)) | a:Minus[c:edge,e:edge,d:edge]{};
        |
        |# op
        |%op-bigraph attackcount nil;
        |%op-bigraph attacker attackerc:User[idle].(attack:Fallback[idle,idle] | c1:Count[idle] | c2:Count[idle] | attackers:Save[idle].attackersm:Money<4>[idle,idle] | attackert:TakeOut[idle].attackertm:Money<4>[idle,idle] | $0);
        |%op-bigraph bank bankc:Bank.(deposit:Deposit[idle,idle].depg:Gas[idle].depm:Money<1>[idle,idle] | withdraw:WithDraw[idle,idle,idle,idle].witg:Gas[idle].witm:Money<1>[idle,idle] | a1:BankAccount[idle].(a1a:Address<0>[idle] | a1m:Money<8>[idle,idle]) | a2:BankAccount[idle].(a2a:Address<1>[idle] | a2m:Money<8>[idle,idle]));
        |%op-bigraph minerother nil;
        |%op-bigraph miner miner:Miner.(bank:SC_RT[idle].(banka:Address<2>[idle] | bankb:Balance[idle].bankm:Money<20>[idle,idle] | $2) | user:SC_RT[idle].(usera:Address<0>[idle] | userb:Balance[idle].userm:Money<6>[idle,idle] | $1) | minerb:Balance[idle].minerm:Money<0>[idle,idle] | $0);
        |%op-bigraph user userc:User[idle].(fallback:Fallback[idle,idle] | users:Save[idle].usersm:Money<3>[idle,idle] | usert:TakeOut[idle].usertm:Money<3>[idle,idle]);
        |%op-bigraph bankother nil;
        |
        |# Model
        |%agent  nil;
        |
        |# OP_Formula
        |%bigraphformula miner○(minerother⊗user⊗bank);
        |
        |
        |# Go!
        |%check;
        |""".stripMargin      //测试用例

    val t = BGMParser.parseFromString(exam3)    // 解析
    val b = BGMTerm.toBigraph(t)                      // 构造
    println(b.root)
    val simulator = new EnumSimulator(b)
    simulator.simulate
  }
}