package org.bigraph.bigsim.utils
import org.bigraph.bigsim.simulator.testCTLSimulator

object bankV3 {

  val normal =
    """
      |# Controls
      |%active Plus : 3;
      |%active Minus : 3;
      |%active Multiply : 3;
      |%active Division : 3;
      |%active Opposite : 2;
      |%active Abs : 2;
      |%active Balance : 1;
      |%active Bank : 0;
      |%active BankAccount : 1;
      |%binding Bind;
      |%active Count : 1;
      |%active Deposit : 2;
      |%active Fallback : 2;
      |%active Gas : 1;
      |%active Miner : 0;
      |%active Money : 2;
      |%active SC_RT : 1;
      |%active Save : 1;
      |%active TakeOut : 1;
      |%active User : 1;
      |%active WithDraw : 4;
      |%active Address : 1;
      |
      |# Rules
      |%rule r_DP-1Prepare user:SC_RT[idle].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(Fallback[idle,idle].$1 | Save[idle].Money[idle,idle] | $0)) | bank:SC_RT[idle].(bankb:Balance[idle].bankm:Money[idle,idle] | banka:Address[idle] | bankc:Bank.(deposit:Deposit[idle,idle].depg:Gas[idle].depm:Money[idle,idle] | $2)) -> user:SC_RT[a:edge].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(Fallback[idle,idle].$1 | Save[a:edge].Money[idle,idle] | $0)) | bank:SC_RT[idle].(bankb:Balance[idle].bankm:Money[idle,idle] | banka:Address[idle] | bankc:Bank.(deposit:Deposit[a:edge,idle].depg:Gas[idle].depm:Money[idle,idle] | $2)){};
      |%rule r_DP-2FindAccount user:SC_RT[a:edge].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(Fallback[idle,idle].$1 | Save[a:edge].Money[idle,idle] | $0)) | bank:SC_RT[idle].(bankb:Balance[idle].bankm:Money[idle,idle] | banka:Address[idle] | bankc:Bank.(deposit:Deposit[a:edge,idle].depg:Gas[idle].depm:Money[idle,idle] | BankAccount[idle].(Address[idle] | Money[idle,idle]) | $2)) -> user:SC_RT[a:edge].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(Fallback[idle,idle].$1 | Save[a:edge].Money[idle,idle] | $0)) | bank:SC_RT[idle].(bankb:Balance[idle].bankm:Money[idle,idle] | banka:Address[idle] | bankc:Bank.(deposit:Deposit[a:edge,b:edge].depg:Gas[idle].depm:Money[idle,idle] | BankAccount[b:edge].(Address[idle] | Money[idle,idle]) | $2)){Condition:user.Balance.Money>=(user.User.Save.Money+bank.Bank.Deposit.Gas.Money),user.Address==bank.Bank.BankAccount.Address};
      |%rule r_DP-3NotEnough user:SC_RT[a:edge].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(Fallback[idle,idle].$1 | Save[a:edge].Money[idle,idle] | $0)) | bank:SC_RT[idle].(bankb:Balance[idle].bankm:Money[idle,idle] | banka:Address[idle] | bankc:Bank.(deposit:Deposit[a:edge,idle].depg:Gas[idle].depm:Money[idle,idle] | $2)) -> user:SC_RT[idle].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(Fallback[idle,idle].$1 | Save[idle].Money[idle,idle] | $0)) | bank:SC_RT[idle].(bankb:Balance[idle].bankm:Money[idle,idle] | banka:Address[idle] | bankc:Bank.(deposit:Deposit[idle,idle].depg:Gas[idle].depm:Money[idle,idle] | $2)){Condition:user.Balance.Money<(user.User.Save.Money+bank.Bank.Deposit.Gas.Money)};
      |%rule r_DP-4Save user:SC_RT[a:edge].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(Fallback[idle,idle].$1 | Save[a:edge].Money[idle,idle] | $0)) | bank:SC_RT[idle].(bankb:Balance[idle].bankm:Money[idle,idle] | banka:Address[idle] | bankc:Bank.(deposit:Deposit[a:edge,b:edge].depg:Gas[idle].depm:Money[idle,idle] | BankAccount[b:edge].(Address[idle] | Money[idle,idle]) | $2)) | minerb:Balance[idle].minerm:Money[idle,idle] -> user:SC_RT[idle].(usera:Address[idle] | userb:Balance[idle].userm:Money[a:edge,e:edge] | User[idle].(Fallback[idle,idle].$1 | Save[idle].Money[b:edge,idle] | $0)) | bank:SC_RT[idle].(bankb:Balance[idle].bankm:Money[f:edge,g:edge] | banka:Address[idle] | bankc:Bank.(deposit:Deposit[idle,idle].depg:Gas[idle].depm:Money[d:edge,idle] | BankAccount[idle].(Address[idle] | Money[h:edge,i:edge]) | $2)) | minerb:Balance[idle].minerm:Money[j:edge,k:edge] | a:Minus[b:edge,c:edge,a:edge] | b:Minus[d:edge,e:edge,c:edge] | c:Plus[f:edge,g:edge,b:edge] | d:Plus[h:edge,i:edge,b:edge] | e:Plus[d:edge,k:edge,j:edge]{};
      |
      |%rule r_WD-1Prepare user:SC_RT[idle].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(TakeOut[idle].Money[idle,idle] | Fallback[idle,idle].$1 | $0)) | bank:SC_RT[idle].(bankb:Balance[idle].bankm:Money[idle,idle] | banka:Address[idle] | bankc:Bank.(withdraw:WithDraw[idle,idle,idle,idle].witg:Gas[idle].witm:Money[idle,idle] | $2)) -> user:SC_RT[a:edge].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(TakeOut[a:edge].Money[idle,idle] | Fallback[idle,idle].$1 | $0)) | bank:SC_RT[idle].(bankb:Balance[idle].bankm:Money[idle,idle] | banka:Address[idle] | bankc:Bank.(withdraw:WithDraw[a:edge,idle,idle,idle].witg:Gas[idle].witm:Money[idle,idle] | $2)){};
      |%rule r_WD-2FindAccount user:SC_RT[a:edge].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(TakeOut[a:edge].Money[idle,idle] | Fallback[idle,idle].$1 | $0)) | bank:SC_RT[idle].(bankb:Balance[idle].bankm:Money[idle,idle] | banka:Address[idle] | bankc:Bank.(BankAccount[idle].(Address[idle] | Money[idle,idle]) | withdraw:WithDraw[a:edge,idle,idle,idle].witg:Gas[idle].witm:Money[idle,idle] | $2)) -> user:SC_RT[a:edge].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(TakeOut[a:edge].Money[idle,idle] | Fallback[idle,idle].$1 | $0)) | bank:SC_RT[idle].(bankb:Balance[idle].bankm:Money[idle,idle] | banka:Address[idle] | bankc:Bank.(BankAccount[b:edge].(Address[idle] | Money[idle,idle]) | withdraw:WithDraw[a:edge,b:edge,idle,idle].witg:Gas[idle].witm:Money[idle,idle] | $2)){Condition:user.Address==bank.Bank.BankAccount.Address};
      |%rule r_WD-3IsEnough user:SC_RT[a:edge].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(TakeOut[a:edge].Money[idle,idle] | Fallback[idle,idle].$1 | $0)) | bank:SC_RT[idle].(bankb:Balance[idle].bankm:Money[idle,idle] | banka:Address[idle] | bankc:Bank.(BankAccount[b:edge].(Address[idle] | Money[idle,idle]) | withdraw:WithDraw[a:edge,b:edge,idle,idle].witg:Gas[idle].witm:Money[idle,idle] | $2)) -> user:SC_RT[a:edge].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(TakeOut[a:edge].Money[idle,idle] | Fallback[b:edge,idle].$1 | $0)) | bank:SC_RT[idle].(bankb:Balance[idle].bankm:Money[idle,idle] | banka:Address[idle] | bankc:Bank.(BankAccount[b:edge].(Address[idle] | Money[idle,idle]) | withdraw:WithDraw[a:edge,idle,idle,b:edge].witg:Gas[idle].witm:Money[idle,idle] | $2)){Condition:user.Balance.Money>=bank.Bank.WithDraw.Gas.Money && user.User.TakeOut.Money<=bank.Bank.BankAccount.Money};
      |%rule r_WD-3NotEnough user:SC_RT[a:edge].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(TakeOut[a:edge].Money[idle,idle] | Fallback[idle,idle].$1 | $0)) | bank:SC_RT[idle].(bankb:Balance[idle].bankm:Money[idle,idle] | banka:Address[idle] | bankc:Bank.(BankAccount[b:edge].(Address[idle] | Money[idle,idle]) | withdraw:WithDraw[a:edge,b:edge,idle,idle].witg:Gas[idle].witm:Money[idle,idle] | $2)) -> user:SC_RT[idle].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(TakeOut[idle].Money[idle,idle] | Fallback[idle,idle].$1 | $0)) | bank:SC_RT[idle].(bankb:Balance[idle].bankm:Money[idle,idle] | banka:Address[idle] | bankc:Bank.(BankAccount[idle].(Address[idle] | Money[idle,idle]) | withdraw:WithDraw[idle,idle,idle,idle].witg:Gas[idle].witm:Money[idle,idle] | $2)){Condition:user.Balance.Money<bank.Bank.WithDraw.Gas.Money||user.User.TakeOut.Money>bank.Bank.BankAccount.Money};
      |%rule r_WD-4Send user:SC_RT[a:edge].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(TakeOut[a:edge].Money[idle,idle] | Fallback[b:edge,idle] | $0)) | bank:SC_RT[idle].(bankb:Balance[idle].bankm:Money[idle,idle] | banka:Address[idle] | bankc:Bank.(BankAccount[b:edge].(Address[idle] | Money[idle,idle]) | withdraw:WithDraw[a:edge,idle,idle,b:edge].witg:Gas[idle].witm:Money[idle,idle] | $2)) | minerb:Balance[idle].minerm:Money[idle,idle] -> user:SC_RT[a:edge].(usera:Address[idle] | userb:Balance[idle].userm:Money[c:edge,i:edge] | User[idle].(TakeOut[a:edge].Money[d:edge,idle] | Fallback[idle,idle] | $0)) | bank:SC_RT[idle].(bankb:Balance[idle].bankm:Money[j:edge,k:edge] | banka:Address[idle] | bankc:Bank.(BankAccount[b:edge].(Address[idle] | Money[idle,idle]) | withdraw:WithDraw[a:edge,idle,b:edge,idle].witg:Gas[idle].witm:Money[f:edge,idle] | $2)) | minerb:Balance[idle].minerm:Money[g:edge,h:edge] | a:Plus[c:edge,e:edge,d:edge] | b:Minus[f:edge,i:edge,e:edge] | c:Minus[d:edge,k:edge,j:edge] | d:Plus[f:edge,h:edge,g:edge]{};
      |%rule r_WD-5Update user:SC_RT[a:edge].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(TakeOut[a:edge].Money[idle,idle] | Fallback[idle,idle].$1 | $0)) | bank:SC_RT[idle].(bankb:Balance[idle].bankm:Money[idle,idle] | banka:Address[idle] | bankc:Bank.(BankAccount[b:edge].(Address[idle] | Money[idle,idle]) | withdraw:WithDraw[a:edge,idle,b:edge,idle].witg:Gas[idle].witm:Money[idle,idle] | $2)) -> user:SC_RT[idle].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(TakeOut[idle].Money[a:edge,idle] | Fallback[idle,idle].$1 | $0)) | bank:SC_RT[idle].(bankb:Balance[idle].bankm:Money[idle,idle] | banka:Address[idle] | bankc:Bank.(BankAccount[idle].(Address[idle] | Money[b:edge,c:edge]) | withdraw:WithDraw[idle,idle,idle,idle].witg:Gas[idle].witm:Money[idle,idle] | $2)) | a:Minus[a:edge,c:edge,b:edge]{};
      |
      |# op
      |%op-bigraph attackercount nil;
      |%op-bigraph attacker attackerc:User[idle].(attackers:Save[idle].attackersm:Money<3>[idle,idle] | attackert:TakeOut[idle].attackertm:Money<3>[idle,idle] | attack:Fallback[idle,idle].(c1:Count[idle] | c2:Count[idle] | $0));
      |%op-bigraph bank bankc:Bank.(deposit:Deposit[idle,idle].depg:Gas[idle].depm:Money<1>[idle,idle] | withdraw:WithDraw[idle,idle,idle,idle].witg:Gas[idle].witm:Money<1>[idle,idle] | a1:BankAccount[idle].(a1a:Address<0>[idle] | a1m:Money<0>[idle,idle]) | a2:BankAccount[idle].(a2a:Address<1>[idle] | a2m:Money<4>[idle,idle]));
      |%op-bigraph minerother nil;
      |%op-bigraph miner miner:Miner.(minerb:Balance[idle].minerm:Money<0>[idle,idle] | user:SC_RT[idle].(usera:Address<0>[idle] | userb:Balance[idle].userm:Money<6>[idle,idle] | $1) | bank:SC_RT[idle].(banka:Address<2>[idle] | bankb:Balance[idle].bankm:Money<20>[idle,idle] | $2) | $0);
      |%op-bigraph user userc:User[idle].(users:Save[idle].usersm:Money<3>[idle,idle] | usert:TakeOut[idle].usertm:Money<3>[idle,idle] | fallback:Fallback[idle,idle]);
      |
      |# OP_Formula
      |%bigraphformula miner○(minerother⊗user⊗bank);
      |
      |# Model
      |%agent  nil;
      |
      |# prop
      |%prop p user:SC_RT[idle].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | $0) | bank:SC_RT[idle].(banka:Address[idle] | bankb:Balance[idle].bankm:Money[idle,idle] | bankc:Bank.(BankAccount[idle].(Money[idle,idle] | Address[idle]) | $1)) | minerb:Balance[idle].minerm:Money[idle,idle]{PropExpr:user.Address==bank.Bank.BankAccount.Address,(user.Balance.Money+bank.Bank.BankAccount.Money+minerb.Money)==6};
      |
      |# CTL_Formula
      |%ctlSpec AX(AF(p));
      |
      |# Go!
      |%check;
      |""".stripMargin

  val attack =
    """
      |# Controls
      |%active Plus : 3;
      |%active Minus : 3;
      |%active Multiply : 3;
      |%active Division : 3;
      |%active Opposite : 2;
      |%active Abs : 2;
      |%active Balance : 1;
      |%active Bank : 0;
      |%active BankAccount : 1;
      |%binding Bind;
      |%active Count : 1;
      |%active Deposit : 2;
      |%active Fallback : 2;
      |%active Gas : 1;
      |%active Miner : 0;
      |%active Money : 2;
      |%active SC_RT : 1;
      |%active Save : 1;
      |%active TakeOut : 1;
      |%active User : 1;
      |%active WithDraw : 4;
      |%active Address : 1;
      |
      |# Rules
      |%rule r_WD-1Prepare user:SC_RT[idle].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(TakeOut[idle].Money[idle,idle] | Fallback[idle,idle].$1 | $0)) | bank:SC_RT[idle].(bankb:Balance[idle].bankm:Money[idle,idle] | banka:Address[idle] | bankc:Bank.(withdraw:WithDraw[idle,idle,idle,idle].witg:Gas[idle].witm:Money[idle,idle] | $2)) -> user:SC_RT[a:edge].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(TakeOut[a:edge].Money[idle,idle] | Fallback[idle,idle].$1 | $0)) | bank:SC_RT[idle].(bankb:Balance[idle].bankm:Money[idle,idle] | banka:Address[idle] | bankc:Bank.(withdraw:WithDraw[a:edge,idle,idle,idle].witg:Gas[idle].witm:Money[idle,idle] | $2)){};
      |%rule r_WD-2FindAccount user:SC_RT[a:edge].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(TakeOut[a:edge].Money[idle,idle] | Fallback[idle,idle].$1 | $0)) | bank:SC_RT[idle].(bankb:Balance[idle].bankm:Money[idle,idle] | banka:Address[idle] | bankc:Bank.(BankAccount[idle].(Address[idle] | Money[idle,idle]) | withdraw:WithDraw[a:edge,idle,idle,idle].witg:Gas[idle].witm:Money[idle,idle] | $2)) -> user:SC_RT[a:edge].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(TakeOut[a:edge].Money[idle,idle] | Fallback[idle,idle].$1 | $0)) | bank:SC_RT[idle].(bankb:Balance[idle].bankm:Money[idle,idle] | banka:Address[idle] | bankc:Bank.(BankAccount[b:edge].(Address[idle] | Money[idle,idle]) | withdraw:WithDraw[a:edge,b:edge,idle,idle].witg:Gas[idle].witm:Money[idle,idle] | $2)){Condition:user.Address==bank.Bank.BankAccount.Address};
      |%rule r_WD-3IsEnough user:SC_RT[a:edge].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(TakeOut[a:edge].Money[idle,idle] | Fallback[idle,idle].$1 | $0)) | bank:SC_RT[idle].(bankb:Balance[idle].bankm:Money[idle,idle] | banka:Address[idle] | bankc:Bank.(BankAccount[b:edge].(Address[idle] | Money[idle,idle]) | withdraw:WithDraw[a:edge,b:edge,idle,idle].witg:Gas[idle].witm:Money[idle,idle] | $2)) -> user:SC_RT[a:edge].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(TakeOut[a:edge].Money[idle,idle] | Fallback[b:edge,idle].$1 | $0)) | bank:SC_RT[idle].(bankb:Balance[idle].bankm:Money[idle,idle] | banka:Address[idle] | bankc:Bank.(BankAccount[b:edge].(Address[idle] | Money[idle,idle]) | withdraw:WithDraw[a:edge,idle,idle,b:edge].witg:Gas[idle].witm:Money[idle,idle] | $2)){Condition:user.Balance.Money>=bank.Bank.WithDraw.Gas.Money && user.User.TakeOut.Money<=bank.Bank.BankAccount.Money};
      |%rule r_WD-3NotEnough user:SC_RT[a:edge].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(TakeOut[a:edge].Money[idle,idle] | Fallback[idle,idle].$1 | $0)) | bank:SC_RT[idle].(bankb:Balance[idle].bankm:Money[idle,idle] | banka:Address[idle] | bankc:Bank.(BankAccount[b:edge].(Address[idle] | Money[idle,idle]) | withdraw:WithDraw[a:edge,b:edge,idle,idle].witg:Gas[idle].witm:Money[idle,idle] | $2)) -> user:SC_RT[idle].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(TakeOut[idle].Money[idle,idle] | Fallback[idle,idle].$1 | $0)) | bank:SC_RT[idle].(bankb:Balance[idle].bankm:Money[idle,idle] | banka:Address[idle] | bankc:Bank.(BankAccount[idle].(Address[idle] | Money[idle,idle]) | withdraw:WithDraw[idle,idle,idle,idle].witg:Gas[idle].witm:Money[idle,idle] | $2)){Condition:user.Balance.Money<bank.Bank.WithDraw.Gas.Money||user.User.TakeOut.Money>bank.Bank.BankAccount.Money};
      |%rule r_WD-4Send user:SC_RT[a:edge].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(TakeOut[a:edge].Money[idle,idle] | Fallback[b:edge,idle] | $0)) | bank:SC_RT[idle].(bankb:Balance[idle].bankm:Money[idle,idle] | banka:Address[idle] | bankc:Bank.(BankAccount[b:edge].(Address[idle] | Money[idle,idle]) | withdraw:WithDraw[a:edge,idle,idle,b:edge].witg:Gas[idle].witm:Money[idle,idle] | $2)) | minerb:Balance[idle].minerm:Money[idle,idle] -> user:SC_RT[a:edge].(usera:Address[idle] | userb:Balance[idle].userm:Money[c:edge,i:edge] | User[idle].(TakeOut[a:edge].Money[d:edge,idle] | Fallback[idle,idle] | $0)) | bank:SC_RT[idle].(bankb:Balance[idle].bankm:Money[j:edge,k:edge] | banka:Address[idle] | bankc:Bank.(BankAccount[b:edge].(Address[idle] | Money[idle,idle]) | withdraw:WithDraw[a:edge,idle,b:edge,idle].witg:Gas[idle].witm:Money[f:edge,idle] | $2)) | minerb:Balance[idle].minerm:Money[g:edge,h:edge] | a:Plus[c:edge,e:edge,d:edge] | b:Minus[f:edge,i:edge,e:edge] | c:Minus[d:edge,k:edge,j:edge] | d:Plus[f:edge,h:edge,g:edge]{};
      |%rule r_WD-5Update user:SC_RT[a:edge].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(TakeOut[a:edge].Money[idle,idle] | Fallback[idle,idle].$1 | $0)) | bank:SC_RT[idle].(bankb:Balance[idle].bankm:Money[idle,idle] | banka:Address[idle] | bankc:Bank.(BankAccount[b:edge].(Address[idle] | Money[idle,idle]) | withdraw:WithDraw[a:edge,idle,b:edge,idle].witg:Gas[idle].witm:Money[idle,idle] | $2)) -> user:SC_RT[idle].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(TakeOut[idle].Money[a:edge,idle] | Fallback[idle,idle].$1 | $0)) | bank:SC_RT[idle].(bankb:Balance[idle].bankm:Money[idle,idle] | banka:Address[idle] | bankc:Bank.(BankAccount[idle].(Address[idle] | Money[b:edge,c:edge]) | withdraw:WithDraw[idle,idle,idle,idle].witg:Gas[idle].witm:Money[idle,idle] | $2)) | a:Minus[a:edge,c:edge,b:edge]{};
      |
      |%rule r_ATK-4Send user:SC_RT[a:edge].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(TakeOut[a:edge].Money[idle,idle] | Fallback[b:edge,idle].(Count[idle] | $1) | $0)) | bank:SC_RT[idle].(bankb:Balance[idle].bankm:Money[idle,idle] | banka:Address[idle] | bankc:Bank.(BankAccount[b:edge].(Address[idle] | Money[idle,idle]) | withdraw:WithDraw[a:edge,idle,idle,b:edge].witg:Gas[idle].witm:Money[idle,idle] | $2)) | minerb:Balance[idle].minerm:Money[idle,idle] -> user:SC_RT[a:edge].(usera:Address[idle] | userb:Balance[idle].userm:Money[b:edge,j:edge] | User[idle].(TakeOut[a:edge].Money[c:edge,idle] | Fallback[idle,idle].$1 | $0)) | bank:SC_RT[idle].(bankb:Balance[idle].bankm:Money[h:edge,i:edge] | banka:Address[idle] | bankc:Bank.(BankAccount[idle].(Address[idle] | Money[idle,idle]) | withdraw:WithDraw[a:edge,idle,idle,idle].witg:Gas[idle].witm:Money[e:edge,idle] | $2)) | minerb:Balance[idle].minerm:Money[f:edge,g:edge] | a:Plus[b:edge,d:edge,c:edge] | b:Minus[e:edge,j:edge,d:edge] | c:Plus[e:edge,g:edge,f:edge] | d:Minus[c:edge,i:edge,h:edge]{};
      |
      |# op
      |%op-bigraph attackercount nil;
      |%op-bigraph attacker attackerc:User[idle].(attackers:Save[idle].attackersm:Money<3>[idle,idle] | attackert:TakeOut[idle].attackertm:Money<3>[idle,idle] | attack:Fallback[idle,idle].(c1:Count[idle] | c2:Count[idle] | $0));
      |%op-bigraph bank bankc:Bank.(deposit:Deposit[idle,idle].depg:Gas[idle].depm:Money<1>[idle,idle] | withdraw:WithDraw[idle,idle,idle,idle].witg:Gas[idle].witm:Money<1>[idle,idle] | a1:BankAccount[idle].(a1a:Address<0>[idle] | a1m:Money<0>[idle,idle]) | a2:BankAccount[idle].(a2a:Address<1>[idle] | a2m:Money<4>[idle,idle]));
      |%op-bigraph minerother nil;
      |%op-bigraph miner miner:Miner.(minerb:Balance[idle].minerm:Money<0>[idle,idle] | user:SC_RT[idle].(usera:Address<1>[idle] | userb:Balance[idle].userm:Money<6>[idle,idle] | $1) | bank:SC_RT[idle].(banka:Address<2>[idle] | bankb:Balance[idle].bankm:Money<20>[idle,idle] | $2) | $0);
      |%op-bigraph user userc:User[idle].(users:Save[idle].usersm:Money<3>[idle,idle] | usert:TakeOut[idle].usertm:Money<3>[idle,idle] | fallback:Fallback[idle,idle]);
      |
      |# OP_Formula
      |%bigraphformula miner○(minerother⊗(attacker○attackercount)⊗bank);
      |
      |# Model
      |%agent  nil;
      |
      |# prop
      |%prop p user:SC_RT[idle].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | $0) | bank:SC_RT[idle].(banka:Address[idle] | bankb:Balance[idle].bankm:Money[idle,idle] | bankc:Bank.(BankAccount[idle].(Money[idle,idle] | Address[idle]) | $1)) | minerb:Balance[idle].minerm:Money[idle,idle]{PropExpr:user.Address==bank.Bank.BankAccount.Address,(user.Balance.Money+bank.Bank.BankAccount.Money+minerb.Money)==10};
      |
      |# CTL_Formula
      |%ctlSpec AX(AF(p));
      |
      |# Go!
      |%check;
      |""".stripMargin

  val attackmore =
    """
      |# Controls
      |%active Plus : 3;
      |%active Minus : 3;
      |%active Multiply : 3;
      |%active Division : 3;
      |%active Opposite : 2;
      |%active Abs : 2;
      |%active Balance : 1;
      |%active Bank : 0;
      |%active BankAccount : 1;
      |%binding Bind;
      |%active Count : 1;
      |%active Deposit : 2;
      |%active Fallback : 2;
      |%active Gas : 1;
      |%active Miner : 0;
      |%active Money : 2;
      |%active SC_RT : 1;
      |%active Save : 1;
      |%active TakeOut : 1;
      |%active User : 1;
      |%active WithDraw : 4;
      |%active Address : 1;
      |
      |# Rules
      |%rule r_WD-1Prepare user:SC_RT[idle].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(TakeOut[idle].Money[idle,idle] | Fallback[idle,idle].$1 | $0)) | bank:SC_RT[idle].(bankb:Balance[idle].bankm:Money[idle,idle] | banka:Address[idle] | bankc:Bank.(withdraw:WithDraw[idle,idle,idle,idle].witg:Gas[idle].witm:Money[idle,idle] | $2)) -> user:SC_RT[a:edge].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(TakeOut[a:edge].Money[idle,idle] | Fallback[idle,idle].$1 | $0)) | bank:SC_RT[idle].(bankb:Balance[idle].bankm:Money[idle,idle] | banka:Address[idle] | bankc:Bank.(withdraw:WithDraw[a:edge,idle,idle,idle].witg:Gas[idle].witm:Money[idle,idle] | $2)){};
      |%rule r_WD-2FindAccount user:SC_RT[a:edge].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(TakeOut[a:edge].Money[idle,idle] | Fallback[idle,idle].$1 | $0)) | bank:SC_RT[idle].(bankb:Balance[idle].bankm:Money[idle,idle] | banka:Address[idle] | bankc:Bank.(BankAccount[idle].(Address[idle] | Money[idle,idle]) | withdraw:WithDraw[a:edge,idle,idle,idle].witg:Gas[idle].witm:Money[idle,idle] | $2)) -> user:SC_RT[a:edge].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(TakeOut[a:edge].Money[idle,idle] | Fallback[idle,idle].$1 | $0)) | bank:SC_RT[idle].(bankb:Balance[idle].bankm:Money[idle,idle] | banka:Address[idle] | bankc:Bank.(BankAccount[b:edge].(Address[idle] | Money[idle,idle]) | withdraw:WithDraw[a:edge,b:edge,idle,idle].witg:Gas[idle].witm:Money[idle,idle] | $2)){Condition:user.Address==bank.Bank.BankAccount.Address};
      |%rule r_WD-3IsEnough user:SC_RT[a:edge].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(TakeOut[a:edge].Money[idle,idle] | Fallback[idle,idle].$1 | $0)) | bank:SC_RT[idle].(bankb:Balance[idle].bankm:Money[idle,idle] | banka:Address[idle] | bankc:Bank.(BankAccount[b:edge].(Address[idle] | Money[idle,idle]) | withdraw:WithDraw[a:edge,b:edge,idle,idle].witg:Gas[idle].witm:Money[idle,idle] | $2)) -> user:SC_RT[a:edge].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(TakeOut[a:edge].Money[idle,idle] | Fallback[b:edge,idle].$1 | $0)) | bank:SC_RT[idle].(bankb:Balance[idle].bankm:Money[idle,idle] | banka:Address[idle] | bankc:Bank.(BankAccount[b:edge].(Address[idle] | Money[idle,idle]) | withdraw:WithDraw[a:edge,idle,idle,b:edge].witg:Gas[idle].witm:Money[idle,idle] | $2)){Condition:user.Balance.Money>=bank.Bank.WithDraw.Gas.Money && user.User.TakeOut.Money<=bank.Bank.BankAccount.Money};
      |%rule r_WD-3NotEnough user:SC_RT[a:edge].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(TakeOut[a:edge].Money[idle,idle] | Fallback[idle,idle].$1 | $0)) | bank:SC_RT[idle].(bankb:Balance[idle].bankm:Money[idle,idle] | banka:Address[idle] | bankc:Bank.(BankAccount[b:edge].(Address[idle] | Money[idle,idle]) | withdraw:WithDraw[a:edge,b:edge,idle,idle].witg:Gas[idle].witm:Money[idle,idle] | $2)) -> user:SC_RT[idle].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(TakeOut[idle].Money[idle,idle] | Fallback[idle,idle].$1 | $0)) | bank:SC_RT[idle].(bankb:Balance[idle].bankm:Money[idle,idle] | banka:Address[idle] | bankc:Bank.(BankAccount[idle].(Address[idle] | Money[idle,idle]) | withdraw:WithDraw[idle,idle,idle,idle].witg:Gas[idle].witm:Money[idle,idle] | $2)){Condition:user.Balance.Money<bank.Bank.WithDraw.Gas.Money||user.User.TakeOut.Money>bank.Bank.BankAccount.Money};
      |%rule r_WD-4Send user:SC_RT[a:edge].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(TakeOut[a:edge].Money[idle,idle] | Fallback[b:edge,idle] | $0)) | bank:SC_RT[idle].(bankb:Balance[idle].bankm:Money[idle,idle] | banka:Address[idle] | bankc:Bank.(BankAccount[b:edge].(Address[idle] | Money[idle,idle]) | withdraw:WithDraw[a:edge,idle,idle,b:edge].witg:Gas[idle].witm:Money[idle,idle] | $2)) | minerb:Balance[idle].minerm:Money[idle,idle] -> user:SC_RT[a:edge].(usera:Address[idle] | userb:Balance[idle].userm:Money[c:edge,i:edge] | User[idle].(TakeOut[a:edge].Money[d:edge,idle] | Fallback[idle,idle] | $0)) | bank:SC_RT[idle].(bankb:Balance[idle].bankm:Money[j:edge,k:edge] | banka:Address[idle] | bankc:Bank.(BankAccount[b:edge].(Address[idle] | Money[idle,idle]) | withdraw:WithDraw[a:edge,idle,b:edge,idle].witg:Gas[idle].witm:Money[f:edge,idle] | $2)) | minerb:Balance[idle].minerm:Money[g:edge,h:edge] | a:Plus[c:edge,e:edge,d:edge] | b:Minus[f:edge,i:edge,e:edge] | c:Minus[d:edge,k:edge,j:edge] | d:Plus[f:edge,h:edge,g:edge]{};
      |%rule r_WD-5Update user:SC_RT[a:edge].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(TakeOut[a:edge].Money[idle,idle] | Fallback[idle,idle].$1 | $0)) | bank:SC_RT[idle].(bankb:Balance[idle].bankm:Money[idle,idle] | banka:Address[idle] | bankc:Bank.(BankAccount[b:edge].(Address[idle] | Money[idle,idle]) | withdraw:WithDraw[a:edge,idle,b:edge,idle].witg:Gas[idle].witm:Money[idle,idle] | $2)) -> user:SC_RT[idle].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(TakeOut[idle].Money[a:edge,idle] | Fallback[idle,idle].$1 | $0)) | bank:SC_RT[idle].(bankb:Balance[idle].bankm:Money[idle,idle] | banka:Address[idle] | bankc:Bank.(BankAccount[idle].(Address[idle] | Money[b:edge,c:edge]) | withdraw:WithDraw[idle,idle,idle,idle].witg:Gas[idle].witm:Money[idle,idle] | $2)) | a:Minus[a:edge,c:edge,b:edge]{};
      |
      |%rule r_ATK-4Send user:SC_RT[a:edge].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(TakeOut[a:edge].Money[idle,idle] | Fallback[b:edge,idle].(Count[idle] | $1) | $0)) | bank:SC_RT[idle].(bankb:Balance[idle].bankm:Money[idle,idle] | banka:Address[idle] | bankc:Bank.(BankAccount[b:edge].(Address[idle] | Money[idle,idle]) | withdraw:WithDraw[a:edge,idle,idle,b:edge].witg:Gas[idle].witm:Money[idle,idle] | $2)) | minerb:Balance[idle].minerm:Money[idle,idle] -> user:SC_RT[a:edge].(usera:Address[idle] | userb:Balance[idle].userm:Money[b:edge,j:edge] | User[idle].(TakeOut[a:edge].Money[c:edge,idle] | Fallback[idle,idle].$1 | $0)) | bank:SC_RT[idle].(bankb:Balance[idle].bankm:Money[h:edge,i:edge] | banka:Address[idle] | bankc:Bank.(BankAccount[idle].(Address[idle] | Money[idle,idle]) | withdraw:WithDraw[a:edge,idle,idle,idle].witg:Gas[idle].witm:Money[e:edge,idle] | $2)) | minerb:Balance[idle].minerm:Money[f:edge,g:edge] | a:Plus[b:edge,d:edge,c:edge] | b:Minus[e:edge,j:edge,d:edge] | c:Plus[e:edge,g:edge,f:edge] | d:Minus[c:edge,i:edge,h:edge]{};
      |
      |# op
      |%op-bigraph attackercount c3:Count[idle] | c4:Count[idle];
      |%op-bigraph attacker attackerc:User[idle].(attackers:Save[idle].attackersm:Money<3>[idle,idle] | attackert:TakeOut[idle].attackertm:Money<3>[idle,idle] | attack:Fallback[idle,idle].(c1:Count[idle] | c2:Count[idle] | $0));
      |%op-bigraph bank bankc:Bank.(deposit:Deposit[idle,idle].depg:Gas[idle].depm:Money<1>[idle,idle] | withdraw:WithDraw[idle,idle,idle,idle].witg:Gas[idle].witm:Money<1>[idle,idle] | a1:BankAccount[idle].(a1a:Address<0>[idle] | a1m:Money<0>[idle,idle]) | a2:BankAccount[idle].(a2a:Address<1>[idle] | a2m:Money<4>[idle,idle]));
      |%op-bigraph minerother nil;
      |%op-bigraph miner miner:Miner.(minerb:Balance[idle].minerm:Money<0>[idle,idle] | user:SC_RT[idle].(usera:Address<1>[idle] | userb:Balance[idle].userm:Money<6>[idle,idle] | $1) | bank:SC_RT[idle].(banka:Address<2>[idle] | bankb:Balance[idle].bankm:Money<20>[idle,idle] | $2) | $0);
      |%op-bigraph user userc:User[idle].(users:Save[idle].usersm:Money<3>[idle,idle] | usert:TakeOut[idle].usertm:Money<3>[idle,idle] | fallback:Fallback[idle,idle]);
      |
      |# OP_Formula
      |%bigraphformula miner○(minerother⊗(attacker○attackercount)⊗bank);
      |
      |# Model
      |%agent  nil;
      |
      |# prop
      |%prop p user:SC_RT[idle].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | $0) | bank:SC_RT[idle].(banka:Address[idle] | bankb:Balance[idle].bankm:Money[idle,idle] | bankc:Bank.(BankAccount[idle].(Money[idle,idle] | Address[idle]) | $1)) | minerb:Balance[idle].minerm:Money[idle,idle]{PropExpr:user.Address==bank.Bank.BankAccount.Address,(user.Balance.Money+bank.Bank.BankAccount.Money+minerb.Money)==10};
      |
      |# CTL_Formula
      |%ctlSpec AX(AF(p));
      |
      |# Go!
      |%check;
      |""".stripMargin

  val repair =
    """
      |# Controls
      |%active Plus : 3;
      |%active Minus : 3;
      |%active Multiply : 3;
      |%active Division : 3;
      |%active Opposite : 2;
      |%active Abs : 2;
      |%active Balance : 1;
      |%active Bank : 0;
      |%active BankAccount : 1;
      |%binding Bind;
      |%active Count : 1;
      |%active Deposit : 2;
      |%active Fallback : 2;
      |%active Gas : 1;
      |%active Miner : 0;
      |%active Money : 2;
      |%active SC_RT : 1;
      |%active Save : 1;
      |%active TakeOut : 1;
      |%active User : 1;
      |%active WithDraw : 4;
      |%active Address : 1;
      |
      |# Rules
      |%rule r_WD-1Prepare user:SC_RT[idle].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(TakeOut[idle].Money[idle,idle] | Fallback[idle,idle].$1 | $0)) | bank:SC_RT[idle].(bankb:Balance[idle].bankm:Money[idle,idle] | banka:Address[idle] | bankc:Bank.(withdraw:WithDraw[idle,idle,idle,idle].witg:Gas[idle].witm:Money[idle,idle] | $2)) -> user:SC_RT[a:edge].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(TakeOut[a:edge].Money[idle,idle] | Fallback[idle,idle].$1 | $0)) | bank:SC_RT[idle].(bankb:Balance[idle].bankm:Money[idle,idle] | banka:Address[idle] | bankc:Bank.(withdraw:WithDraw[a:edge,idle,idle,idle].witg:Gas[idle].witm:Money[idle,idle] | $2)){};
      |%rule r_WD-2FindAccount user:SC_RT[a:edge].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(TakeOut[a:edge].Money[idle,idle] | Fallback[idle,idle].$1 | $0)) | bank:SC_RT[idle].(bankb:Balance[idle].bankm:Money[idle,idle] | banka:Address[idle] | bankc:Bank.(BankAccount[idle].(Address[idle] | Money[idle,idle]) | withdraw:WithDraw[a:edge,idle,idle,idle].witg:Gas[idle].witm:Money[idle,idle] | $2)) -> user:SC_RT[a:edge].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(TakeOut[a:edge].Money[idle,idle] | Fallback[idle,idle].$1 | $0)) | bank:SC_RT[idle].(bankb:Balance[idle].bankm:Money[idle,idle] | banka:Address[idle] | bankc:Bank.(BankAccount[b:edge].(Address[idle] | Money[idle,idle]) | withdraw:WithDraw[a:edge,b:edge,idle,idle].witg:Gas[idle].witm:Money[idle,idle] | $2)){Condition:user.Address==bank.Bank.BankAccount.Address};
      |%rule r_WD-3NotEnough user:SC_RT[a:edge].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(TakeOut[a:edge].Money[idle,idle] | Fallback[idle,idle].$1 | $0)) | bank:SC_RT[idle].(bankb:Balance[idle].bankm:Money[idle,idle] | banka:Address[idle] | bankc:Bank.(BankAccount[b:edge].(Address[idle] | Money[idle,idle]) | withdraw:WithDraw[a:edge,b:edge,idle,idle].witg:Gas[idle].witm:Money[idle,idle] | $2)) -> user:SC_RT[idle].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(TakeOut[idle].Money[idle,idle] | Fallback[idle,idle].$1 | $0)) | bank:SC_RT[idle].(bankb:Balance[idle].bankm:Money[idle,idle] | banka:Address[idle] | bankc:Bank.(BankAccount[idle].(Address[idle] | Money[idle,idle]) | withdraw:WithDraw[idle,idle,idle,idle].witg:Gas[idle].witm:Money[idle,idle] | $2)){Condition:user.Balance.Money<bank.Bank.WithDraw.Gas.Money||user.User.TakeOut.Money>bank.Bank.BankAccount.Money};
      |
      |%rule r_ATK-4Send user:SC_RT[a:edge].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(TakeOut[a:edge].Money[idle,idle] | Fallback[b:edge,idle].(Count[idle] | $1) | $0)) | bank:SC_RT[idle].(bankb:Balance[idle].bankm:Money[idle,idle] | banka:Address[idle] | bankc:Bank.(BankAccount[b:edge].(Address[idle] | Money[idle,idle]) | withdraw:WithDraw[a:edge,idle,idle,b:edge].witg:Gas[idle].witm:Money[idle,idle] | $2)) | minerb:Balance[idle].minerm:Money[idle,idle] -> user:SC_RT[a:edge].(usera:Address[idle] | userb:Balance[idle].userm:Money[b:edge,j:edge] | User[idle].(TakeOut[a:edge].Money[c:edge,idle] | Fallback[idle,idle].$1 | $0)) | bank:SC_RT[idle].(bankb:Balance[idle].bankm:Money[h:edge,i:edge] | banka:Address[idle] | bankc:Bank.(BankAccount[idle].(Address[idle] | Money[idle,idle]) | withdraw:WithDraw[a:edge,idle,idle,idle].witg:Gas[idle].witm:Money[e:edge,idle] | $2)) | minerb:Balance[idle].minerm:Money[f:edge,g:edge] | a:Plus[b:edge,d:edge,c:edge] | b:Minus[e:edge,j:edge,d:edge] | c:Plus[e:edge,g:edge,f:edge] | d:Minus[c:edge,i:edge,h:edge]{};
      |
      |%rule r_RP-3IsEnough user:SC_RT[a:edge].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(TakeOut[a:edge].Money[idle,idle] | Fallback[idle,idle].$1 | $0)) | bank:SC_RT[idle].(bankb:Balance[idle].bankm:Money[idle,idle] | banka:Address[idle] | bankc:Bank.(BankAccount[b:edge].(Address[idle] | Money[idle,idle]) | withdraw:WithDraw[a:edge,b:edge,idle,idle].witg:Gas[idle].witm:Money[idle,idle] | $2)) -> user:SC_RT[a:edge].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(TakeOut[a:edge].Money[idle,idle] | Fallback[idle,idle].$1 | $0)) | bank:SC_RT[idle].(bankb:Balance[idle].bankm:Money[idle,idle] | banka:Address[idle] | bankc:Bank.(BankAccount[b:edge].(Address[idle] | Money[idle,idle]) | withdraw:WithDraw[a:edge,idle,b:edge,idle].witg:Gas[idle].witm:Money[idle,idle] | $2)){Condition:user.Balance.Money>=bank.Bank.WithDraw.Gas.Money && user.User.TakeOut.Money<=bank.Bank.BankAccount.Money};
      |%rule r_RP-4Update user:SC_RT[a:edge].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(TakeOut[a:edge].Money[idle,idle] | Fallback[idle,idle].$1 | $0)) | bank:SC_RT[idle].(bankb:Balance[idle].bankm:Money[idle,idle] | banka:Address[idle] | bankc:Bank.(BankAccount[b:edge].(Address[idle] | Money[idle,idle]) | withdraw:WithDraw[a:edge,idle,b:edge,idle].witg:Gas[idle].witm:Money[idle,idle] | $2)) -> user:SC_RT[a:edge].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(TakeOut[a:edge].Money[c:edge,idle] | Fallback[b:edge,idle].$1 | $0)) | bank:SC_RT[idle].(bankb:Balance[idle].bankm:Money[idle,idle] | banka:Address[idle] | bankc:Bank.(BankAccount[b:edge].(Address[idle] | Money[d:edge,e:edge]) | withdraw:WithDraw[a:edge,idle,idle,b:edge].witg:Gas[idle].witm:Money[idle,idle] | $2)) | a:Minus[c:edge,e:edge,d:edge]{};
      |%rule r_RP-5Send user:SC_RT[b:edge].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(TakeOut[b:edge].Money[idle,idle] | Fallback[a:edge,idle].$1 | $0)) | bank:SC_RT[idle].(bankb:Balance[idle].bankm:Money[idle,idle] | banka:Address[idle] | bankc:Bank.(BankAccount[a:edge].(Address[idle] | Money[idle,idle]) | withdraw:WithDraw[b:edge,idle,idle,a:edge].witg:Gas[idle].witm:Money[idle,idle] | $2)) | minerb:Balance[idle].minerm:Money[idle,idle] -> user:SC_RT[idle].(usera:Address[idle] | userb:Balance[idle].userm:Money[c:edge,i:edge] | User[idle].(TakeOut[idle].Money[d:edge,idle] | Fallback[idle,idle].$1 | $0)) | bank:SC_RT[idle].(bankb:Balance[idle].bankm:Money[j:edge,k:edge] | banka:Address[idle] | bankc:Bank.(BankAccount[idle].(Address[idle] | Money[idle,idle]) | withdraw:WithDraw[idle,idle,idle,idle].witg:Gas[idle].witm:Money[f:edge,idle] | $2)) | minerb:Balance[idle].minerm:Money[g:edge,h:edge] | a:Plus[c:edge,e:edge,d:edge] | b:Minus[f:edge,i:edge,e:edge] | c:Plus[f:edge,h:edge,g:edge] | d:Minus[d:edge,k:edge,j:edge]{};
      |
      |# op
      |%op-bigraph attackercount nil;
      |%op-bigraph attacker attackerc:User[idle].(attackers:Save[idle].attackersm:Money<3>[idle,idle] | attackert:TakeOut[idle].attackertm:Money<3>[idle,idle] | attack:Fallback[idle,idle].(c1:Count[idle] | c2:Count[idle] | $0));
      |%op-bigraph bank bankc:Bank.(deposit:Deposit[idle,idle].depg:Gas[idle].depm:Money<1>[idle,idle] | withdraw:WithDraw[idle,idle,idle,idle].witg:Gas[idle].witm:Money<1>[idle,idle] | a1:BankAccount[idle].(a1a:Address<0>[idle] | a1m:Money<0>[idle,idle]) | a2:BankAccount[idle].(a2a:Address<1>[idle] | a2m:Money<4>[idle,idle]));
      |%op-bigraph minerother nil;
      |%op-bigraph miner miner:Miner.(minerb:Balance[idle].minerm:Money<0>[idle,idle] | user:SC_RT[idle].(usera:Address<1>[idle] | userb:Balance[idle].userm:Money<6>[idle,idle] | $1) | bank:SC_RT[idle].(banka:Address<2>[idle] | bankb:Balance[idle].bankm:Money<20>[idle,idle] | $2) | $0);
      |%op-bigraph user userc:User[idle].(users:Save[idle].usersm:Money<3>[idle,idle] | usert:TakeOut[idle].usertm:Money<3>[idle,idle] | fallback:Fallback[idle,idle]);
      |
      |# OP_Formula
      |%bigraphformula miner○(minerother⊗(attacker○attackercount)⊗bank);
      |
      |# Model
      |%agent  nil;
      |
      |# prop
      |%prop p user:SC_RT[idle].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | $0) | bank:SC_RT[idle].(banka:Address[idle] | bankb:Balance[idle].bankm:Money[idle,idle] | bankc:Bank.(BankAccount[idle].(Money[idle,idle] | Address[idle]) | $1)) | minerb:Balance[idle].minerm:Money[idle,idle]{PropExpr:user.Address==bank.Bank.BankAccount.Address,(user.Balance.Money+bank.Bank.BankAccount.Money+minerb.Money)==10};
      |
      |# CTL_Formula
      |%ctlSpec AX(AF(p));
      |
      |# Go!
      |%check;
      |""".stripMargin

}

object bankV2 {

  val normal =
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
      |
      |%rule r_DP-1Prepare user:SC_RT[idle].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(Fallback[idle,idle] | Save[idle].Money[idle,idle] | $0)) | bank:SC_RT[idle].(bankb:Balance[idle].bankm:Money[idle,idle] | banka:Address[idle] | bankc:Bank.(deposit:Deposit[idle,idle].depg:Gas[idle].depm:Money[idle,idle] | $1)) | minerb:Balance[idle].minerm:Money[idle,idle] -> user:SC_RT[a:edge].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(Fallback[idle,idle] | Save[a:edge].Money[idle,idle] | $0)) | bank:SC_RT[idle].(bankb:Balance[idle].bankm:Money[idle,idle] | banka:Address[idle] | bankc:Bank.(deposit:Deposit[idle,a:edge].depg:Gas[idle].depm:Money[idle,idle] | $1)) | minerb:Balance[idle].minerm:Money[idle,idle]{};
      |%rule r_DP-2FindAccount user:SC_RT[a:edge].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(Fallback[idle,idle] | Save[a:edge].Money[idle,idle] | $0)) | bank:SC_RT[idle].(bankb:Balance[idle].bankm:Money[idle,idle] | banka:Address[idle] | bankc:Bank.(deposit:Deposit[idle,a:edge].depg:Gas[idle].depm:Money[idle,idle] | BankAccount[idle].(Address[idle] | Money[idle,idle]) | $1)) | minerb:Balance[idle].minerm:Money[idle,idle] -> user:SC_RT[a:edge].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(Fallback[idle,idle] | Save[a:edge].Money[idle,idle] | $0)) | bank:SC_RT[idle].(bankb:Balance[idle].bankm:Money[idle,idle] | banka:Address[idle] | bankc:Bank.(deposit:Deposit[b:edge,a:edge].depg:Gas[idle].depm:Money[idle,idle] | BankAccount[b:edge].(Address[idle] | Money[idle,idle]) | $1)) | minerb:Balance[idle].minerm:Money[idle,idle]{Condition:user.Balance.Money>=(user.User.Save.Money+bank.Bank.Deposit.Gas.Money),user.Address==bank.Bank.BankAccount.Address};
      |%rule r_DP-3IsNotEnough user:SC_RT[a:edge].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(Fallback[idle,idle] | Save[a:edge].Money[idle,idle] | $0)) | bank:SC_RT[idle].(bankb:Balance[idle].bankm:Money[idle,idle] | banka:Address[idle] | bankc:Bank.(deposit:Deposit[idle,a:edge].depg:Gas[idle].depm:Money[idle,idle] | $1)) | minerb:Balance[idle].minerm:Money[idle,idle] -> user:SC_RT[idle].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(Fallback[idle,idle] | Save[idle].Money[idle,idle] | $0)) | bank:SC_RT[idle].(bankb:Balance[idle].bankm:Money[idle,idle] | banka:Address[idle] | bankc:Bank.(deposit:Deposit[idle,idle].depg:Gas[idle].depm:Money[idle,idle] | $1)) | minerb:Balance[idle].minerm:Money[idle,idle]{Condition:user.Balance.Money<(user.User.Save.Money+bank.Bank.Deposit.Gas.Money)};
      |%rule r_DP-4Save user:SC_RT[a:edge].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(Fallback[idle,idle] | Save[a:edge].Money[idle,idle] | $0)) | bank:SC_RT[idle].(bankb:Balance[idle].bankm:Money[idle,idle] | banka:Address[idle] | bankc:Bank.(deposit:Deposit[b:edge,a:edge].depg:Gas[idle].depm:Money[idle,idle] | BankAccount[b:edge].(Address[idle] | Money[idle,idle]) | $1)) | minerb:Balance[idle].minerm:Money[idle,idle] -> user:SC_RT[idle].(usera:Address[idle] | userb:Balance[idle].userm:Money[g:edge,c:edge] | User[idle].(Fallback[idle,idle] | Save[idle].Money[idle,d:edge] | $0)) | bank:SC_RT[idle].(bankb:Balance[idle].bankm:Money[i:edge,h:edge] | banka:Address[idle] | bankc:Bank.(deposit:Deposit[idle,idle].depg:Gas[idle].depm:Money[idle,f:edge] | BankAccount[idle].(Address[idle] | Money[k:edge,j:edge]) | $1)) | minerb:Balance[idle].minerm:Money[m:edge,l:edge] | a:Minus[d:edge,e:edge,c:edge] | b:Minus[f:edge,g:edge,e:edge] | e:Plus[f:edge,m:edge,l:edge] | c:Plus[h:edge,i:edge,d:edge] | d:Plus[j:edge,k:edge,d:edge]{};
      |
      |%rule r_WD-1Prepare user:SC_RT[idle].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(Fallback[idle,idle] | TakeOut[idle].Money[idle,idle] | $0)) | bank:SC_RT[idle].(banka:Address[idle] | bankb:Balance[idle].bankm:Money[idle,idle] | bankc:Bank.(withdraw:WithDraw[idle,idle,idle,idle].witg:Gas[idle].witm:Money[idle,idle] | $1)) | minerb:Balance[idle].minerm:Money[idle,idle] -> user:SC_RT[a:edge].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(Fallback[idle,idle] | TakeOut[a:edge].Money[idle,idle] | $0)) | bank:SC_RT[idle].(banka:Address[idle] | bankb:Balance[idle].bankm:Money[idle,idle] | bankc:Bank.(withdraw:WithDraw[a:edge,idle,idle,idle].witg:Gas[idle].witm:Money[idle,idle] | $1)) | minerb:Balance[idle].minerm:Money[idle,idle]{};
      |%rule r_WD-2FindAccount user:SC_RT[a:edge].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(Fallback[idle,idle] | TakeOut[a:edge].Money[idle,idle] | $0)) | bank:SC_RT[idle].(banka:Address[idle] | bankb:Balance[idle].bankm:Money[idle,idle] | bankc:Bank.(BankAccount[idle].(Money[idle,idle] | Address[idle]) | withdraw:WithDraw[a:edge,idle,idle,idle].witg:Gas[idle].witm:Money[idle,idle] | $1)) | minerb:Balance[idle].minerm:Money[idle,idle] -> user:SC_RT[a:edge].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(Fallback[idle,idle] | TakeOut[a:edge].Money[idle,idle] | $0)) | bank:SC_RT[idle].(banka:Address[idle] | bankb:Balance[idle].bankm:Money[idle,idle] | bankc:Bank.(BankAccount[b:edge].(Money[idle,idle] | Address[idle]) | withdraw:WithDraw[a:edge,b:edge,idle,idle].witg:Gas[idle].witm:Money[idle,idle] | $1)) | minerb:Balance[idle].minerm:Money[idle,idle]{Condition:user.Address==bank.Bank.BankAccount.Address};
      |%rule r_WD-3IsEnough user:SC_RT[a:edge].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(Fallback[idle,idle] | TakeOut[a:edge].Money[idle,idle] | $0) | $2) | bank:SC_RT[idle].(banka:Address[idle] | bankb:Balance[idle].bankm:Money[idle,idle] | bankc:Bank.(BankAccount[b:edge].(Money[idle,idle] | Address[idle]) | withdraw:WithDraw[a:edge,b:edge,idle,idle].witg:Gas[idle].witm:Money[idle,idle] | $1)) | minerb:Balance[idle].minerm:Money[idle,idle] -> user:SC_RT[a:edge].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(Fallback[b:edge,idle] | TakeOut[a:edge].Money[idle,idle] | $0) | $2) | bank:SC_RT[idle].(banka:Address[idle] | bankb:Balance[idle].bankm:Money[idle,idle] | bankc:Bank.(BankAccount[b:edge].(Money[idle,idle] | Address[idle]) | withdraw:WithDraw[a:edge,idle,idle,b:edge].witg:Gas[idle].witm:Money[idle,idle] | $1)) | minerb:Balance[idle].minerm:Money[idle,idle]{Condition:user.Balance.Money >= bank.Bank.WithDraw.Gas.Money && user.User.TakeOut.Money <= bank.Bank.BankAccount.Money};
      |%rule r_WD-3IsNotEnough user:SC_RT[a:edge].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(Fallback[idle,idle] | TakeOut[a:edge].Money[idle,idle] | $0)) | bank:SC_RT[idle].(banka:Address[idle] | bankb:Balance[idle].bankm:Money[idle,idle] | bankc:Bank.(BankAccount[b:edge].(Money[idle,idle] | Address[idle]) | withdraw:WithDraw[a:edge,b:edge,idle,idle].witg:Gas[idle].witm:Money[idle,idle] | $1)) | minerb:Balance[idle].minerm:Money[idle,idle] -> user:SC_RT[idle].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(Fallback[idle,idle] | TakeOut[idle].Money[idle,idle] | $0)) | bank:SC_RT[idle].(banka:Address[idle] | bankb:Balance[idle].bankm:Money[idle,idle] | bankc:Bank.(BankAccount[idle].(Money[idle,idle] | Address[idle]) | withdraw:WithDraw[idle,idle,idle,idle].witg:Gas[idle].witm:Money[idle,idle] | $1)) | minerb:Balance[idle].minerm:Money[idle,idle]{Condition:user.Balance.Money<bank.Bank.WithDraw.Gas.Money||user.User.TakeOut.Money>bank.Bank.BankAccount.Money};
      |%rule r_WD-4Send user:SC_RT[a:edge].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(Fallback[b:edge,idle] | TakeOut[a:edge].Money[idle,idle] | $0)) | bank:SC_RT[idle].(banka:Address[idle] | bankb:Balance[idle].bankm:Money[idle,idle] | bankc:Bank.(BankAccount[b:edge].(Money[idle,idle] | Address[idle]) | withdraw:WithDraw[a:edge,idle,idle,b:edge].witg:Gas[idle].witm:Money[idle,idle] | $1)) | minerb:Balance[idle].minerm:Money[idle,idle] -> user:SC_RT[a:edge].(usera:Address[idle] | userb:Balance[idle].userm:Money[g:edge,c:edge] | User[idle].(Fallback[idle,idle] | TakeOut[a:edge].Money[idle,d:edge] | $0)) | bank:SC_RT[idle].(banka:Address[idle] | bankb:Balance[idle].bankm:Money[i:edge,h:edge] | bankc:Bank.(BankAccount[b:edge].(Money[idle,idle] | Address[idle]) | withdraw:WithDraw[a:edge,idle,b:edge,idle].witg:Gas[idle].witm:Money[idle,f:edge] | $1)) | minerb:Balance[idle].minerm:Money[k:edge,j:edge] | a:Plus[c:edge,e:edge,d:edge] | b:Minus[f:edge,g:edge,e:edge] | c:Minus[d:edge,i:edge,h:edge] | d:Plus[f:edge,k:edge,j:edge]{};
      |%rule r_WD-5Update user:SC_RT[a:edge].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(Fallback[idle,idle] | TakeOut[a:edge].Money[idle,idle] | $0)) | bank:SC_RT[idle].(banka:Address[idle] | bankb:Balance[idle].bankm:Money[idle,idle] | bankc:Bank.(BankAccount[b:edge].(Money[idle,idle] | Address[idle]) | withdraw:WithDraw[a:edge,idle,b:edge,idle].witg:Gas[idle].witm:Money[idle,idle] | $1)) | minerb:Balance[idle].minerm:Money[idle,idle] -> user:SC_RT[idle].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(Fallback[idle,idle] | TakeOut[idle].Money[idle,c:edge] | $0)) | bank:SC_RT[idle].(banka:Address[idle] | bankb:Balance[idle].bankm:Money[idle,idle] | bankc:Bank.(BankAccount[idle].(Money[e:edge,d:edge] | Address[idle]) | withdraw:WithDraw[idle,idle,idle,idle].witg:Gas[idle].witm:Money[idle,idle] | $1)) | a:Minus[c:edge,e:edge,d:edge] | minerb:Balance[idle].minerm:Money[idle,idle]{};
      |
      |# op
      |%op-bigraph attackcount nil;
      |%op-bigraph attacker attackerc:User[idle].(attack:Fallback[idle,idle] | c1:Count[idle] | c2:Count[idle] | attackers:Save[idle].attackersm:Money<3>[idle,idle] | attackert:TakeOut[idle].attackertm:Money<3>[idle,idle] | $0);
      |%op-bigraph bank bankc:Bank.(deposit:Deposit[idle,idle].depg:Gas[idle].depm:Money<1>[idle,idle] | withdraw:WithDraw[idle,idle,idle,idle].witg:Gas[idle].witm:Money<1>[idle,idle] | a1:BankAccount[idle].(a1a:Address<0>[idle] | a1m:Money<0>[idle,idle]) | a2:BankAccount[idle].(a2a:Address<1>[idle] | a2m:Money<0>[idle,idle]) | $0);
      |%op-bigraph minerother nil;
      |%op-bigraph miner miner:Miner.(bank:SC_RT[idle].(banka:Address<2>[idle] | bankb:Balance[idle].bankm:Money<20>[idle,idle] | $2) | user:SC_RT[idle].(usera:Address<0>[idle] | userb:Balance[idle].userm:Money<6>[idle,idle] | $1) | minerb:Balance[idle].minerm:Money<0>[idle,idle] | $0);
      |%op-bigraph user userc:User[idle].(fallback:Fallback[idle,idle] | users:Save[idle].usersm:Money<3>[idle,idle] | usert:TakeOut[idle].usertm:Money<3>[idle,idle]);
      |%op-bigraph bankother nil;
      |
      |# Model
      |%agent  nil;
      |
      |# OP_Formula
      |%bigraphformula miner○(minerother⊗user⊗(bank○bankother));
      |
      |# Go!
      |%check;
      |""".stripMargin

  val attack =
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
      |%rule r_WD-1Prepare user:SC_RT[idle].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(Fallback[idle,idle] | TakeOut[idle].Money[idle,idle] | $0)) | bank:SC_RT[idle].(banka:Address[idle] | bankb:Balance[idle].bankm:Money[idle,idle] | bankc:Bank.(withdraw:WithDraw[idle,idle,idle,idle].witg:Gas[idle].witm:Money[idle,idle] | $1)) | minerb:Balance[idle].minerm:Money[idle,idle] -> user:SC_RT[a:edge].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(Fallback[idle,idle] | TakeOut[a:edge].Money[idle,idle] | $0)) | bank:SC_RT[idle].(banka:Address[idle] | bankb:Balance[idle].bankm:Money[idle,idle] | bankc:Bank.(withdraw:WithDraw[a:edge,idle,idle,idle].witg:Gas[idle].witm:Money[idle,idle] | $1)) | minerb:Balance[idle].minerm:Money[idle,idle]{};
      |%rule r_WD-2FindAccount user:SC_RT[a:edge].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(Fallback[idle,idle] | TakeOut[a:edge].Money[idle,idle] | $0)) | bank:SC_RT[idle].(banka:Address[idle] | bankb:Balance[idle].bankm:Money[idle,idle] | bankc:Bank.(BankAccount[idle].(Money[idle,idle] | Address[idle]) | withdraw:WithDraw[a:edge,idle,idle,idle].witg:Gas[idle].witm:Money[idle,idle] | $1)) | minerb:Balance[idle].minerm:Money[idle,idle] -> user:SC_RT[a:edge].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(Fallback[idle,idle] | TakeOut[a:edge].Money[idle,idle] | $0)) | bank:SC_RT[idle].(banka:Address[idle] | bankb:Balance[idle].bankm:Money[idle,idle] | bankc:Bank.(BankAccount[b:edge].(Money[idle,idle] | Address[idle]) | withdraw:WithDraw[a:edge,b:edge,idle,idle].witg:Gas[idle].witm:Money[idle,idle] | $1)) | minerb:Balance[idle].minerm:Money[idle,idle]{Condition:user.Address==bank.Bank.BankAccount.Address};
      |%rule r_WD-3IsEnough user:SC_RT[a:edge].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(Fallback[idle,idle] | TakeOut[a:edge].Money[idle,idle] | $0) | $2) | bank:SC_RT[idle].(banka:Address[idle] | bankb:Balance[idle].bankm:Money[idle,idle] | bankc:Bank.(BankAccount[b:edge].(Money[idle,idle] | Address[idle]) | withdraw:WithDraw[a:edge,b:edge,idle,idle].witg:Gas[idle].witm:Money[idle,idle] | $1)) | minerb:Balance[idle].minerm:Money[idle,idle] -> user:SC_RT[a:edge].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(Fallback[b:edge,idle] | TakeOut[a:edge].Money[idle,idle] | $0) | $2) | bank:SC_RT[idle].(banka:Address[idle] | bankb:Balance[idle].bankm:Money[idle,idle] | bankc:Bank.(BankAccount[b:edge].(Money[idle,idle] | Address[idle]) | withdraw:WithDraw[a:edge,idle,idle,b:edge].witg:Gas[idle].witm:Money[idle,idle] | $1)) | minerb:Balance[idle].minerm:Money[idle,idle]{Condition:user.Balance.Money >= bank.Bank.WithDraw.Gas.Money && user.User.TakeOut.Money <= bank.Bank.BankAccount.Money};
      |%rule r_WD-3IsNotEnough user:SC_RT[a:edge].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(Fallback[idle,idle] | TakeOut[a:edge].Money[idle,idle] | $0)) | bank:SC_RT[idle].(banka:Address[idle] | bankb:Balance[idle].bankm:Money[idle,idle] | bankc:Bank.(BankAccount[b:edge].(Money[idle,idle] | Address[idle]) | withdraw:WithDraw[a:edge,b:edge,idle,idle].witg:Gas[idle].witm:Money[idle,idle] | $1)) | minerb:Balance[idle].minerm:Money[idle,idle] -> user:SC_RT[idle].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(Fallback[idle,idle] | TakeOut[idle].Money[idle,idle] | $0)) | bank:SC_RT[idle].(banka:Address[idle] | bankb:Balance[idle].bankm:Money[idle,idle] | bankc:Bank.(BankAccount[idle].(Money[idle,idle] | Address[idle]) | withdraw:WithDraw[idle,idle,idle,idle].witg:Gas[idle].witm:Money[idle,idle] | $1)) | minerb:Balance[idle].minerm:Money[idle,idle]{Condition:user.Balance.Money<bank.Bank.WithDraw.Gas.Money||user.User.TakeOut.Money>bank.Bank.BankAccount.Money};
      |%rule r_WD-4Send user:SC_RT[a:edge].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(Fallback[b:edge,idle] | TakeOut[a:edge].Money[idle,idle] | $0)) | bank:SC_RT[idle].(banka:Address[idle] | bankb:Balance[idle].bankm:Money[idle,idle] | bankc:Bank.(BankAccount[b:edge].(Money[idle,idle] | Address[idle]) | withdraw:WithDraw[a:edge,idle,idle,b:edge].witg:Gas[idle].witm:Money[idle,idle] | $1)) | minerb:Balance[idle].minerm:Money[idle,idle] -> user:SC_RT[a:edge].(usera:Address[idle] | userb:Balance[idle].userm:Money[g:edge,c:edge] | User[idle].(Fallback[idle,idle] | TakeOut[a:edge].Money[idle,d:edge] | $0)) | bank:SC_RT[idle].(banka:Address[idle] | bankb:Balance[idle].bankm:Money[i:edge,h:edge] | bankc:Bank.(BankAccount[b:edge].(Money[idle,idle] | Address[idle]) | withdraw:WithDraw[a:edge,idle,b:edge,idle].witg:Gas[idle].witm:Money[idle,f:edge] | $1)) | minerb:Balance[idle].minerm:Money[k:edge,j:edge] | a:Plus[c:edge,e:edge,d:edge] | b:Minus[f:edge,g:edge,e:edge] | c:Minus[d:edge,i:edge,h:edge] | d:Plus[f:edge,k:edge,j:edge]{};
      |%rule r_WD-5Update user:SC_RT[a:edge].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(Fallback[idle,idle] | TakeOut[a:edge].Money[idle,idle] | $0)) | bank:SC_RT[idle].(banka:Address[idle] | bankb:Balance[idle].bankm:Money[idle,idle] | bankc:Bank.(BankAccount[b:edge].(Money[idle,idle] | Address[idle]) | withdraw:WithDraw[a:edge,idle,b:edge,idle].witg:Gas[idle].witm:Money[idle,idle] | $1)) | minerb:Balance[idle].minerm:Money[idle,idle] -> user:SC_RT[idle].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(Fallback[idle,idle] | TakeOut[idle].Money[idle,c:edge] | $0)) | bank:SC_RT[idle].(banka:Address[idle] | bankb:Balance[idle].bankm:Money[idle,idle] | bankc:Bank.(BankAccount[idle].(Money[e:edge,d:edge] | Address[idle]) | withdraw:WithDraw[idle,idle,idle,idle].witg:Gas[idle].witm:Money[idle,idle] | $1)) | a:Minus[c:edge,e:edge,d:edge] | minerb:Balance[idle].minerm:Money[idle,idle]{};
      |%rule r_ATK-4Send user:SC_RT[a:edge].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | attackerc:User[idle].(attack:Fallback[b:edge,idle] | TakeOut[a:edge].Money[idle,idle] | Count[idle] | $0 | $2)) | bank:SC_RT[idle].(banka:Address[idle] | bankb:Balance[idle].bankm:Money[idle,idle] | bankc:Bank.(BankAccount[b:edge].(Money[idle,idle] | Address[idle]) | withdraw:WithDraw[a:edge,idle,idle,b:edge].witg:Gas[idle].witm:Money[idle,idle] | $1)) | minerb:Balance[idle].minerm:Money[idle,idle] -> user:SC_RT[a:edge].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | attackerc:User[idle].(attack:Fallback[b:edge,idle] | TakeOut[a:edge].Money[idle,idle] | $0 | $2)) | bank:SC_RT[idle].(banka:Address[idle] | bankb:Balance[idle].bankm:Money[idle,idle] | bankc:Bank.(BankAccount[idle].(Money[idle,idle] | Address[idle]) | withdraw:WithDraw[a:edge,idle,idle,b:edge].witg:Gas[idle].witm:Money[idle,idle] | $1)) | minerb:Balance[idle].minerm:Money[idle,idle]{};
      |
      |# op
      |%op-bigraph attackcount nil;
      |%op-bigraph attacker attackerc:User[idle].(attack:Fallback[idle,idle] | c1:Count[idle] | c2:Count[idle] | attackers:Save[idle].attackersm:Money<3>[idle,idle] | attackert:TakeOut[idle].attackertm:Money<3>[idle,idle] | $0);
      |%op-bigraph bank bankc:Bank.(deposit:Deposit[idle,idle].depg:Gas[idle].depm:Money<1>[idle,idle] | withdraw:WithDraw[idle,idle,idle,idle].witg:Gas[idle].witm:Money<1>[idle,idle] | a1:BankAccount[idle].(a1a:Address<0>[idle] | a1m:Money<0>[idle,idle]) | a2:BankAccount[idle].(a2a:Address<1>[idle] | a2m:Money<4>[idle,idle]) | $0);
      |%op-bigraph minerother nil;
      |%op-bigraph miner miner:Miner.(bank:SC_RT[idle].(banka:Address<2>[idle] | bankb:Balance[idle].bankm:Money<20>[idle,idle] | $2) | user:SC_RT[idle].(usera:Address<1>[idle] | userb:Balance[idle].userm:Money<5>[idle,idle] | $1) | minerb:Balance[idle].minerm:Money<0>[idle,idle] | $0);
      |%op-bigraph user userc:User[idle].(fallback:Fallback[idle,idle] | users:Save[idle].usersm:Money<3>[idle,idle] | usert:TakeOut[idle].usertm:Money<3>[idle,idle]);
      |%op-bigraph bankother nil;
      |
      |# Model
      |%agent  nil;
      |
      |# OP_Formula
      |%bigraphformula miner○(minerother⊗(attacker○attackcount)⊗(bank○bankother));
      |
      |# Go!
      |%check;
      |""".stripMargin      //测试用例

  var repair =
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
      |%rule r_WD-1Prepare user:SC_RT[idle].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(Fallback[idle,idle] | TakeOut[idle].Money[idle,idle] | $0)) | bank:SC_RT[idle].(banka:Address[idle] | bankb:Balance[idle].bankm:Money[idle,idle] | bankc:Bank.(withdraw:WithDraw[idle,idle,idle,idle].witg:Gas[idle].witm:Money[idle,idle] | $1)) | minerb:Balance[idle].minerm:Money[idle,idle] -> user:SC_RT[a:edge].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(Fallback[idle,idle] | TakeOut[a:edge].Money[idle,idle] | $0)) | bank:SC_RT[idle].(banka:Address[idle] | bankb:Balance[idle].bankm:Money[idle,idle] | bankc:Bank.(withdraw:WithDraw[a:edge,idle,idle,idle].witg:Gas[idle].witm:Money[idle,idle] | $1)) | minerb:Balance[idle].minerm:Money[idle,idle]{};
      |%rule r_WD-2FindAccount user:SC_RT[a:edge].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(Fallback[idle,idle] | TakeOut[a:edge].Money[idle,idle] | $0)) | bank:SC_RT[idle].(banka:Address[idle] | bankb:Balance[idle].bankm:Money[idle,idle] | bankc:Bank.(BankAccount[idle].(Money[idle,idle] | Address[idle]) | withdraw:WithDraw[a:edge,idle,idle,idle].witg:Gas[idle].witm:Money[idle,idle] | $1)) | minerb:Balance[idle].minerm:Money[idle,idle] -> user:SC_RT[a:edge].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(Fallback[idle,idle] | TakeOut[a:edge].Money[idle,idle] | $0)) | bank:SC_RT[idle].(banka:Address[idle] | bankb:Balance[idle].bankm:Money[idle,idle] | bankc:Bank.(BankAccount[b:edge].(Money[idle,idle] | Address[idle]) | withdraw:WithDraw[a:edge,b:edge,idle,idle].witg:Gas[idle].witm:Money[idle,idle] | $1)) | minerb:Balance[idle].minerm:Money[idle,idle]{Condition:user.Address==bank.Bank.BankAccount.Address};
      |
      |%rule r_WD-3IsNotEnough user:SC_RT[a:edge].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(Fallback[idle,idle] | TakeOut[a:edge].Money[idle,idle] | $0)) | bank:SC_RT[idle].(banka:Address[idle] | bankb:Balance[idle].bankm:Money[idle,idle] | bankc:Bank.(BankAccount[b:edge].(Money[idle,idle] | Address[idle]) | withdraw:WithDraw[a:edge,b:edge,idle,idle].witg:Gas[idle].witm:Money[idle,idle] | $1)) | minerb:Balance[idle].minerm:Money[idle,idle] -> user:SC_RT[idle].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(Fallback[idle,idle] | TakeOut[idle].Money[idle,idle] | $0)) | bank:SC_RT[idle].(banka:Address[idle] | bankb:Balance[idle].bankm:Money[idle,idle] | bankc:Bank.(BankAccount[idle].(Money[idle,idle] | Address[idle]) | withdraw:WithDraw[idle,idle,idle,idle].witg:Gas[idle].witm:Money[idle,idle] | $1)) | minerb:Balance[idle].minerm:Money[idle,idle]{Condition:user.Balance.Money<bank.Bank.WithDraw.Gas.Money||user.User.TakeOut.Money>bank.Bank.BankAccount.Money};
      |%rule r_RP-3IsEnough user:SC_RT[a:edge].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(Fallback[idle,idle] | TakeOut[a:edge].Money[idle,idle] | $0)) | bank:SC_RT[idle].(banka:Address[idle] | bankb:Balance[idle].bankm:Money[idle,idle] | bankc:Bank.(BankAccount[b:edge].(Money[idle,idle] | Address[idle]) | withdraw:WithDraw[a:edge,b:edge,idle,idle].witg:Gas[idle].witm:Money[idle,idle] | $1)) | $2 -> user:SC_RT[a:edge].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(Fallback[idle,idle] | TakeOut[a:edge].Money[idle,idle] | $0)) | bank:SC_RT[idle].(banka:Address[idle] | bankb:Balance[idle].bankm:Money[idle,idle] | bankc:Bank.(BankAccount[b:edge].(Money[idle,idle] | Address[idle]) | withdraw:WithDraw[a:edge,idle,b:edge,idle].witg:Gas[idle].witm:Money[idle,idle] | $1)) | $2{Condition:user.Balance.Money >= bank.Bank.WithDraw.Gas.Money && user.User.TakeOut.Money <= bank.Bank.BankAccount.Money};
      |%rule r_RP-4Update user:SC_RT[a:edge].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(Fallback[idle,idle] | TakeOut[a:edge].Money[idle,idle] | $0)) | bank:SC_RT[idle].(banka:Address[idle] | bankb:Balance[idle].bankm:Money[idle,idle] | bankc:Bank.(BankAccount[b:edge].(Money[idle,idle] | Address[idle]) | withdraw:WithDraw[a:edge,idle,b:edge,idle].witg:Gas[idle].witm:Money[idle,idle] | $1)) | $2 -> user:SC_RT[a:edge].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(Fallback[b:edge,idle] | TakeOut[a:edge].Money[idle,d:edge] | $0)) | bank:SC_RT[idle].(banka:Address[idle] | bankb:Balance[idle].bankm:Money[idle,idle] | bankc:Bank.(BankAccount[b:edge].(Money[e:edge,c:edge] | Address[idle]) | withdraw:WithDraw[a:edge,idle,idle,b:edge].(witg:Gas[idle].witm:Money[idle,idle] | a:Minus[d:edge,e:edge,c:edge]) | $1)) | $2{};
      |%rule r_RP-5Send user:SC_RT[a:edge].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | User[idle].(Fallback[b:edge,idle] | TakeOut[a:edge].Money[idle,idle] | $0)) | bank:SC_RT[idle].(banka:Address[idle] | bankb:Balance[idle].bankm:Money[idle,idle] | bankc:Bank.(BankAccount[b:edge].(Money[idle,idle] | Address[idle]) | withdraw:WithDraw[a:edge,idle,idle,b:edge].witg:Gas[idle].witm:Money[idle,idle] | $1)) | minerb:Balance[idle].minerm:Money[idle,idle] -> user:SC_RT[idle].(usera:Address[idle] | userb:Balance[idle].userm:Money[g:edge,c:edge] | User[idle].(Fallback[idle,idle] | TakeOut[idle].Money[idle,d:edge] | $0)) | bank:SC_RT[idle].(banka:Address[idle] | bankb:Balance[idle].bankm:Money[i:edge,h:edge] | bankc:Bank.(BankAccount[idle].(Money[idle,idle] | Address[idle]) | withdraw:WithDraw[idle,idle,idle,idle].witg:Gas[idle].witm:Money[idle,f:edge] | $1)) | minerb:Balance[idle].minerm:Money[k:edge,j:edge] | a:Plus[c:edge,e:edge,d:edge] | b:Minus[f:edge,g:edge,e:edge] | c:Minus[d:edge,i:edge,h:edge] | d:Plus[f:edge,k:edge,j:edge]{};
      |
      |%rule r_ATK-4Send user:SC_RT[a:edge].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | attackerc:User[idle].(attack:Fallback[b:edge,idle] | TakeOut[a:edge].Money[idle,idle] | Count[idle] | $0 | $2)) | bank:SC_RT[idle].(banka:Address[idle] | bankb:Balance[idle].bankm:Money[idle,idle] | bankc:Bank.(BankAccount[b:edge].(Money[idle,idle] | Address[idle]) | withdraw:WithDraw[a:edge,idle,idle,b:edge].witg:Gas[idle].witm:Money[idle,idle] | $1)) | minerb:Balance[idle].minerm:Money[idle,idle] -> user:SC_RT[a:edge].(usera:Address[idle] | userb:Balance[idle].userm:Money[idle,idle] | attackerc:User[idle].(attack:Fallback[b:edge,idle] | TakeOut[a:edge].Money[idle,idle] | $0 | $2)) | bank:SC_RT[idle].(banka:Address[idle] | bankb:Balance[idle].bankm:Money[idle,idle] | bankc:Bank.(BankAccount[idle].(Money[idle,idle] | Address[idle]) | withdraw:WithDraw[a:edge,idle,idle,b:edge].witg:Gas[idle].witm:Money[idle,idle] | $1)) | minerb:Balance[idle].minerm:Money[idle,idle]{};
      |
      |# op
      |%op-bigraph attackcount nil;
      |%op-bigraph attacker attackerc:User[idle].(attack:Fallback[idle,idle] | c1:Count[idle] | c2:Count[idle] | attackers:Save[idle].attackersm:Money<3>[idle,idle] | attackert:TakeOut[idle].attackertm:Money<3>[idle,idle] | $0);
      |%op-bigraph bank bankc:Bank.(deposit:Deposit[idle,idle].depg:Gas[idle].depm:Money<1>[idle,idle] | withdraw:WithDraw[idle,idle,idle,idle].witg:Gas[idle].witm:Money<1>[idle,idle] | a1:BankAccount[idle].(a1a:Address<0>[idle] | a1m:Money<0>[idle,idle]) | a2:BankAccount[idle].(a2a:Address<1>[idle] | a2m:Money<4>[idle,idle]) | $0);
      |%op-bigraph minerother nil;
      |%op-bigraph miner miner:Miner.(bank:SC_RT[idle].(banka:Address<2>[idle] | bankb:Balance[idle].bankm:Money<20>[idle,idle] | $2) | user:SC_RT[idle].(usera:Address<1>[idle] | userb:Balance[idle].userm:Money<5>[idle,idle] | $1) | minerb:Balance[idle].minerm:Money<0>[idle,idle] | $0);
      |%op-bigraph user userc:User[idle].(fallback:Fallback[idle,idle] | users:Save[idle].usersm:Money<3>[idle,idle] | usert:TakeOut[idle].usertm:Money<3>[idle,idle]);
      |%op-bigraph bankother nil;
      |
      |# Model
      |%agent  nil;
      |
      |# OP_Formula
      |%bigraphformula miner○(minerother⊗(attacker○attackcount)⊗(bank○bankother));
      |
      |# Go!
      |%check;
      |""".stripMargin      //测试用例

}

object banktest {

  val normal =
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
      |%active User : 0;
      |%active Bank : 0;
      |%active Account : 0;
      |%active Miner : 0;
      |%active Deposit : 0;
      |%active WithDraw : 0;
      |%active Save : 0;
      |%active Takeout : 0;
      |
      |# Rules
      |%rule r_DP-1Prepare a:User | b:Bank | c:Deposit | d:Miner | e:Save | f:Account | g:Takeout | h:WithDraw -> a:User.e:Save | b:Bank | c:Deposit | d:Miner | f:Account | g:Takeout | h:WithDraw | $0{};
      |%rule r_DP-2NotEnough a:User.e:Save | b:Bank | c:Deposit | d:Miner | f:Account | g:Takeout | h:WithDraw -> a:User | b:Bank | c:Deposit | d:Miner | f:Account | g:Takeout | h:WithDraw | e:Save | $0{Condition:User<(User.Save+Deposit)};
      |%rule r_DP-3Enough a:User.e:Save | b:Bank | c:Deposit | d:Miner | f:Account | g:Takeout | h:WithDraw -> a:User | b:Bank | c:Deposit.e:Save | d:Miner | f:Account | g:Takeout | h:WithDraw | $0{Condition:User>=(User.Save+Deposit)};
      |%rule r_DP-4Save a:User | b:Bank | c:Deposit.e:Save | d:Miner | f:Account | g:Takeout | h:WithDraw -> a:User | b:Bank | c:Deposit | d:Miner | f:Account | g:Takeout | h:WithDraw | e:Save | $0{Assign:User=(User-Deposit.Save-Deposit),Bank=(Bank+Deposit.Save),Account=(Account+Deposit.Save),Miner=(Miner-Deposit)};
      |
      |%rule r_WD-1Prepare a:User | b:Bank | c:Deposit | d:Miner | e:Save | f:Account | g:Takeout | h:WithDraw -> a:User.g:Takeout | b:Bank | c:Deposit | d:Miner | e:Save | f:Account | h:WithDraw | $0{};
      |%rule r_WD-2FindAccount a:User.g:Takeout | b:Bank | c:Deposit | d:Miner | e:Save | f:Account | h:WithDraw -> a:User | b:Bank | c:Deposit | d:Miner | e:Save | f:Account.g:Takeout | h:WithDraw | $0{};
      |%rule r_WD-3Enough a:User | b:Bank | c:Deposit | d:Miner | e:Save | f:Account.g:Takeout | h:WithDraw -> a:User | b:Bank | c:Deposit | d:Miner | e:Save | f:Account | h:WithDraw.g:Takeout | $0{Condition:(User>=WithDraw)&&(Account>=Account.Takeout)};
      |%rule r_WD-3NotEnough a:User | b:Bank | c:Deposit | d:Miner | e:Save | f:Account.g:Takeout | h:WithDraw -> a:User | b:Bank | c:Deposit | d:Miner | e:Save | f:Account | h:WithDraw | g:Takeout | $0{Condition:(User<WithDraw)||(Account<Account.Takeout)};
      |%rule r_WD-4Send a:User | b:Bank | c:Deposit | d:Miner | e:Save | f:Account | h:WithDraw.g:Takeout -> a:User | b:Bank.g:Takeout | c:Deposit | d:Miner | e:Save | f:Account | h:WithDraw | $0{Assign:User=(User-WithDraw+WithDraw.Takeout),Bank=(Bank-WithDraw.Takeout),Miner=(Miner+WithDraw)};
      |%rule r_WD-5Update a:User | b:Bank.g:Takeout | c:Deposit | d:Miner | e:Save | f:Account | h:WithDraw -> a:User | b:Bank | c:Deposit | d:Miner | e:Save | f:Account | h:WithDraw | g:Takeout | $0{Assign:Account=(Account-Bank.Takeout)};
      |
      |# prop
      |%prop  p a:User.$0 | f:Account | d:Miner{PropExpr:(User+Account+Miner)==6};
      |
      |# CTL_Formula
      |%ctlSpec AF(p);
      |
      |# Model
      |%agent  a:User<6> | b:Bank<20> | c:Deposit<1> | d:Miner<0> | e:Save<3> | f:Account<0> | g:Takeout<3> | h:WithDraw<1>;
      |
      |# Go!
      |%check;
      |""".stripMargin

  val attack =
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
      |%active User : 0;
      |%active Bank : 0;
      |%active Account : 0;
      |%active Miner : 0;
      |%active Deposit : 0;
      |%active WithDraw : 0;
      |%active Save : 0;
      |%active Takeout : 0;
      |
      |# Rules
      |%rule r_WD-1Prepare env:ENV.(a:User | b:Bank | c:Deposit | d:Miner | e:Save | f:Account | g:Takeout | h:WithDraw | $0) -> env:ENV.(a:User.g:Takeout | b:Bank | c:Deposit | d:Miner | e:Save | f:Account | h:WithDraw | $0){};
      |%rule r_WD-2FindAccount env:ENV.(a:User.g:Takeout | b:Bank | c:Deposit | d:Miner | e:Save | f:Account | h:WithDraw | $0) -> env:ENV.(a:User | b:Bank | c:Deposit | d:Miner | e:Save | f:Account.g:Takeout | h:WithDraw | $0){};
      |%rule r_WD-3Enough env:ENV.(a:User | b:Bank | c:Deposit | d:Miner | e:Save | f:Account.g:Takeout | h:WithDraw | $0) -> env:ENV.(a:User | b:Bank | c:Deposit | d:Miner | e:Save | f:Account | h:WithDraw.g:Takeout | $0){Condition:(ENV.User>=ENV.WithDraw)&&(ENV.Account>=ENV.Account.Takeout)};
      |%rule r_WD-3NotEnough env:ENV.(a:User | b:Bank | c:Deposit | d:Miner | e:Save | f:Account.g:Takeout | h:WithDraw | $0) -> env:ENV.(a:User | b:Bank | c:Deposit | d:Miner | e:Save | f:Account | h:WithDraw | g:Takeout | $0){Condition:(ENV.User<ENV.WithDraw)||(ENV.Account<ENV.Account.Takeout)};
      |%rule r_WD-4Send env:ENV.(a:User | b:Bank | c:Deposit | d:Miner | e:Save | f:Account | h:WithDraw.g:Takeout) -> env:ENV.(a:User | b:Bank.g:Takeout | c:Deposit | d:Miner | e:Save | f:Account | h:WithDraw){Assign:ENV.User=(ENV.User-ENV.WithDraw+ENV.WithDraw.Takeout),ENV.Bank=(ENV.Bank-ENV.WithDraw.Takeout),ENV.Miner=(ENV.Miner+ENV.WithDraw)};
      |%rule r_WD-5Update env:ENV.(a:User | b:Bank.g:Takeout | c:Deposit | d:Miner | e:Save | f:Account | h:WithDraw) -> env:ENV.(a:User | b:Bank | c:Deposit | d:Miner | e:Save | f:Account | h:WithDraw | g:Takeout){Assign:ENV.Account=(ENV.Account-ENV.Bank.Takeout)};
      |%rule r_ATK-4Send env:ENV.(a:User | b:Bank | c:Deposit | d:Miner | e:Save | f:Account | h:WithDraw.g:Takeout | Num | $0) -> env:ENV.(a:User.g:Takeout | b:Bank | c:Deposit | d:Miner | e:Save | f:Account | h:WithDraw | $0){Assign:ENV.User=(ENV.User+ENV.WithDraw.Takeout-ENV.WithDraw),ENV.Miner=(ENV.Miner+ENV.WithDraw),ENV.Bank=(ENV.Bank-ENV.WithDraw.Takeout)};
      |
      |# prop
      |%prop  p a:User.$0 | f:Account | d:Miner{PropExpr:(User+Account+Miner)==9};
      |
      |# CTL_Formula
      |%ctlSpec EG(p);
      |
      |# Model
      |%agent  env:ENV.(a:User<5> | b:Bank<20> | c:Deposit<1> | d:Miner<0> | e:Save<3> | f:Account<4> | g:Takeout<3> | h:WithDraw<1> | i:Num | j:Num);
      |
      |# Go!
      |%check;
      |""".stripMargin

  val repair =
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
      |%active User : 0;
      |%active Bank : 0;
      |%active Account : 0;
      |%active Miner : 0;
      |%active Deposit : 0;
      |%active WithDraw : 0;
      |%active Save : 0;
      |%active Takeout : 0;
      |%active ENV : 0;
      |
      |# Rules
      |%rule r_WD-1Prepare env:ENV.(a:User | b:Bank | c:Deposit | d:Miner | e:Save | f:Account | g:Takeout | h:WithDraw | $0) -> env:ENV.(a:User.g:Takeout | b:Bank | c:Deposit | d:Miner | e:Save | f:Account | h:WithDraw | $0){};
      |%rule r_WD-2FindAccount env:ENV.(a:User.g:Takeout | b:Bank | c:Deposit | d:Miner | e:Save | f:Account | h:WithDraw | $0) -> env:ENV.(a:User | b:Bank | c:Deposit | d:Miner | e:Save | f:Account.g:Takeout | h:WithDraw | $0){};
      |%rule r_WD-3NotEnough env:ENV.(a:User | b:Bank | c:Deposit | d:Miner | e:Save | f:Account.g:Takeout | h:WithDraw | $0) -> env:ENV.(a:User | b:Bank | c:Deposit | d:Miner | e:Save | f:Account | h:WithDraw | g:Takeout | $0){Condition:(ENV.User<ENV.WithDraw)||(ENV.Account<ENV.Account.Takeout)};
      |
      |%rule r_ATK-4Send env:ENV.(a:User | b:Bank | c:Deposit | d:Miner | e:Save | f:Account | h:WithDraw.g:Takeout | Num | $0) -> env:ENV.(a:User.g:Takeout | b:Bank | c:Deposit | d:Miner | e:Save | f:Account | h:WithDraw | $0){Assign:ENV.User=(ENV.User+ENV.WithDraw.Takeout-ENV.WithDraw),ENV.Miner=(ENV.Miner+ENV.WithDraw),ENV.Bank=(ENV.Bank-ENV.WithDraw.Takeout)};
      |
      |%rule r_RP-3Enough env:ENV.(c:Deposit | e:Save | a:User | f:Account.g:Takeout | h:WithDraw | b:Bank | d:Miner | $0) -> env:ENV.(c:Deposit | e:Save | a:User | f:Account | h:WithDraw | b:Bank.g:Takeout | d:Miner | $0){Condition:(ENV.User>=ENV.WithDraw)&&(ENV.Account>=ENV.Account.Takeout)};
      |%rule r_RP-4Update env:ENV.(a:User | c:Deposit | d:Miner | h:WithDraw | b:Bank.g:Takeout | f:Account | e:Save | $0) -> env:ENV.(a:User | c:Deposit | d:Miner | h:WithDraw.g:Takeout | b:Bank | f:Account | e:Save | $0){Assign:ENV.Account=(ENV.Account-ENV.Bank.Takeout)};
      |%rule r_RP-5Send env:ENV.(b:Bank | f:Account | e:Save | a:User | c:Deposit | h:WithDraw.g:Takeout | d:Miner) -> env:ENV.(b:Bank | f:Account | e:Save | a:User | c:Deposit | h:WithDraw | d:Miner | g:Takeout){Assign:ENV.User=(ENV.User+ENV.WithDraw.Takeout-ENV.WithDraw),ENV.Miner=(ENV.Miner+ENV.WithDraw),ENV.Bank=(ENV.Bank-ENV.WithDraw.Takeout)};
      |
      |# prop
      |%prop  p a:User.$0 | f:Account | d:Miner{PropExpr:(User+Account+Miner)==9};
      |
      |# CTL_Formula
      |%ctlSpec EG(p);
      |
      |# Model
      |%agent  env:ENV.(a:User<5> | b:Bank<20> | c:Deposit<1> | d:Miner<0> | e:Save<3> | f:Account<6> | g:Takeout<3> | h:WithDraw<1> | i:Num | j:Num);
      |
      |# Go!
      |%check;
      |""".stripMargin

}

object process {

  val model =
    """
      |# Controls
      |%active Process : 1;
      |%active CriticalResource : 1;
      |%active Semaphore : 2;
      |%active State : 1;
      |%active ApplyQueue : 0;
      |
      |# Rules
      |%rule r_both_process_apply Process[idle] | a:ApplyQueue.Process[idle] | $0 -> a:ApplyQueue.(Process[idle] | Process[idle]) | $0{};
      |%rule r_one_apply_one_access c:CriticalResource[a:edge] | s:Semaphore[a:edge,b:edge].(true:State[idle] | false:State[b:edge]) | a:ApplyQueue.(Process[idle] | Process[idle]) -> c:CriticalResource[a:edge].Process[idle] | s:Semaphore[a:edge,b:edge].(true:State[b:edge] | false:State[idle]) | a:ApplyQueue.Process[idle]{};
      |%rule r_one_access_resource c:CriticalResource[a:edge] | s:Semaphore[a:edge,b:edge].(true:State[idle] | false:State[b:edge]) | a:ApplyQueue.Process[idle] | Process[idle] -> c:CriticalResource[a:edge].Process[idle] | s:Semaphore[a:edge,b:edge].(true:State[b:edge] | false:State[idle]) | a:ApplyQueue | Process[idle]{};
      |%rule r_one_process_apply Process[idle] | Process[idle] | a:ApplyQueue | $0 -> Process[idle] | a:ApplyQueue.Process[idle] | $0{};
      |%rule r_release_critical_resource c:CriticalResource[a:edge].Process[idle] | s:Semaphore[a:edge,b:edge].(true:State[b:edge] | false:State[idle]) | $0 -> c:CriticalResource[a:edge] | s:Semaphore[a:edge,b:edge].(true:State[idle] | false:State[b:edge]) | Process[idle] | $0{};
      |
      |# Prop
      |%prop p c:CriticalResource[a:edge].(Process[idle] | Process[idle]) | s:Semaphore[a:edge,b:edge].(true:State[b:edge] | false:State[idle]) | $0{};
      |
      |# CTL_Formula
      |%ctlSpec AG(!p);
      |
      |# Model
      |%agent  p1:Process[idle] | p2:Process[idle] | c:CriticalResource[a:edge] | s:Semaphore[a:edge,b:edge].(true:State[idle] | false:State[b:edge]) | a:ApplyQueue;
      |
      |# Go!
      |%check;
      |
      |""".stripMargin

}

/**
 * 整理模型文件的时候，比较两个版本的BGM文件是否相同。
 */
object testModelFile {

  def compareBGM(s1: String, s2: String): Unit = {
    val l1 = s1.split(";")
    val set1 = l1.toSet

    val l2 = s2.split(";")
    val set2 = l2.toSet

    val diff12 = set1 diff set2
    val diff21 = set2 diff set1

    println("************** 第一个文件中内容 ****************")
    println(diff12)
    println("************** 第二个文件中内容 ****************")
    println(diff21)

  }

  def main(args: Array[String]): Unit = {
    // 比较两个模型文件
//    var s1 = process.model
//    var s2 = testCTLSimulator.kgqtestprocess
//    compareBGM(s1, s2)
  }
}