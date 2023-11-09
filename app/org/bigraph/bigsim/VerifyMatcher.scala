package org.bigraph.bigsim

import org.bigraph.bigsim.model.Term
//import java.lang.Boolean
import org.bigraph.bigsim.model.Bigraph
import org.bigraph.bigsim.model.TermType
import org.bigraph.bigsim.model.Prefix
import org.bigraph.bigsim.model.Paraller
import scala.collection.Parallel
import org.bigraph.bigsim.parser.TermParser;


/*
 * this mod is for match in verify (curState with proposition match)
 * author: yanwei
 * */

object VerifyMatcher {
  
  def main(args: Array[String]): Unit = {
    var terml: Term = TermParser.apply("office:Office.student:Student.Nil");
    var termr: Term = TermParser.apply("office:Office.Nil");
    println(BigraphIsEqual(terml, termr));
  }
  
  def Match(bl:Bigraph,br:Bigraph):Boolean = {
     if(bl.isInitial ||br.isInitial) return true; 
     if(bl.isFinal&&bl.label!=null&&bl.label.equals("true"))return true;
     if(br.isFinal&&br.label!=null&&br.label.equals("true"))return true;
     return BigraphIsEqual(bl.root,br.root)
  }
  
  def BigraphIsEqual(bl:Term,br:Term):Boolean = {//lry:判断两个Bigraph是否相等，此函数似乎有问题?已改
    //println("bl.type="+bl.termType+"br.type"+br.termType);//lry
    //println("bl="+bl+"br="+br);//lry
    if(bl.termType != br.termType)
    {
      //println("return false");//lry
      return false;
    }
    if(bl.termType == TermType.TPREF){
      var p1 = bl.asInstanceOf[Prefix];
      var p2 = br.asInstanceOf[Prefix];
        if(p1.suffix.termType ==TermType.TNIL&&p2.suffix.termType ==TermType.TNIL){
          //if(!bl.termType.toString().equals(br.termType.toString())){//lry delete 2018.04.01
            if(p1.node.name.equals(p2.node.name)&&p1.node.ctrl.name.equals(p2.node.ctrl.name)){  //lry add 2018.04.01
              var a = 0;
              for( a <- 0 to p1.node.ports.size-1){
                var port1 = p1.node.ports.apply(a);
                var port2 = p2.node.ports.apply(a);
                if(!(port1.name.equals(port2.name)&&(port1.nameType.equals(port2.nameType))))
                //{println("port1.name="+port1.name+"port2.name"+port2.name);return false;}
                return false;
              }
              return true;
              /*p1.node.ports.foreach{port1 =>
                p2.node.ports.foreach{port2 =>
                  if(!(port1.name.equals(port2.name)&&port1.nameType.equals(port2.nameType)))
                  {println("port1.name="+port1.name+"port2.name="+port2.name+"port1.nameType="+port1.nameType+"port2.nameType="+port2.nameType);return false;}
                    
                }
              }
              return true; */              
             }else return false;
        }
        if(p1.node.name.equals(p2.node.name)&&p1.node.ctrl.name.equals(p2.node.ctrl.name)){
          return BigraphIsEqual(p1.suffix,p2.suffix)
        } else return false;
    }
    else if(bl.termType==TermType.TPAR){
      var p1 = bl.asInstanceOf[Paraller];
      var p2 = br.asInstanceOf[Paraller];
      //println("p1="+p1+"p2="+p2);//lry
      return (BigraphIsEqual(p1.leftTerm,p2.rightTerm)&&
        BigraphIsEqual(p1.rightTerm,p2.leftTerm))||
         (BigraphIsEqual(p1.leftTerm,p2.leftTerm)&&
        BigraphIsEqual(p1.rightTerm,p2.rightTerm))
    }
    return true;
  }
}