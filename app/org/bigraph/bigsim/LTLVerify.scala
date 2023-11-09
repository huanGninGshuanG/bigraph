package org.bigraph.bigsim

import org.bigraph.bigsim.model.Specification;
import org.bigraph.bigsim.model.BiNode
import scala.collection.mutable.Set
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.ListBuffer
import scala.math._
object LTLVerify {
  var length = BiNode.allBiNodes.length;
  var specification: Specification  = Specification.processSpec();
  var graph:ArrayBuffer[ArrayBuffer[Int]] = new ArrayBuffer[ArrayBuffer[Int]](length);
  //var spec:ArrayBuffer[Int] = new ArrayBuffer[Int]();
  var spec:Map[String,Int] = Map();
  var pathLength:Int = 0;
  def Init()={
    var allBiNodes = BiNode.allBiNodes;
    for(node <-allBiNodes){
      var childList = new ArrayBuffer[Int];
      for(child<-node.childList){
        var index = BiNode.indexAt(child.bigraph);
        childList+=index;
      }
      graph+=childList;
    }
  }
 
  
  def getSpecification()={
     println("spec="+specification);//lry
     specification.proposition.keySet.foreach { x => 
     var b = specification.proposition(x);
     var index = BiNode.indexAt(b)
     println("x="+x+" index="+index);
     spec+=(x -> index);
    }
     /*specification.foreach { x => 
      if((x.isInitial==false)&&(x.isFinal==false))
      {
        println(x.root);
        println(BiNode.indexAt(x));
        spec+=BiNode.indexAt(x);
      }
     }*/
  }
  
  def dfs_rect(adjListsGraph:ArrayBuffer[ArrayBuffer[Int]],visited:Array[Boolean],v:Int,d:Int,path:ListBuffer[Int]):Unit={
    //println("v="+v);
    visited(v)=true;
    path+=v;
    if(d==v){
      var i = 0;
      //println("path.length="+path.length);
      if(path.length>pathLength)
      {
        pathLength=path.length;
      }
      for(i <-0 to path.length-1){
        if(i!=0){
         print(getNameOfReactionRule(path(i-1),path(i)));
         print("->");
        }
        print(path(i));
        if(i!=path.length-1){
         print("->");
        }
      }
      println();
    }
    else{
      adjListsGraph(v).foreach{node=>
        if(!visited(node)){
          dfs_rect(adjListsGraph,visited,node,d,path);
        }
      }
    }
    path.trimEnd(1);
    visited(v)=false;
  }
  
  def dfs(adjListsGraph:ArrayBuffer[ArrayBuffer[Int]],s:Int,d:Int):Boolean={
    var n = adjListsGraph.length;
    //println("n="+n);
    var visited:Array[Boolean] = new Array[Boolean](n);
    var i=0;
    for(i <- 0 to n-1){
      visited(i)=false;
    }
    /*visited.foreach{f=>
      println(f);
    }*/
    var path:ListBuffer[Int] = new ListBuffer[Int]();
    dfs_rect(adjListsGraph,visited,s,d,path);
    //println(pathLength);
    if(pathLength>=2)
      return true;
    else 
      return false;
  }
  
   def getNameOfReactionRule(index1:Int,index2:Int):String={
      var rule =  BiNode.allBiNodes(index1).NodeToReactionRule.get(BiNode.allBiNodes(index2));
      var rule2 = rule.get;
      var name = rule2.name;
      //println(rule2.name);
      return name;
  }
  
  def Verify():Boolean={
    return VerifyByString(specification.formula);
  }
  def VerifyByString(s:String):Boolean={
    println(s);
    Init();
    getSpecification();
    pathLength = 0;
    //println("spec="+spec);
    var s1 = s;
    if(s.startsWith("(")&&s.endsWith(")"))
    {
      s1 = s.substring(1,s.length()-1).trim();
    }
    //println("s1="+s1);
    if(firstSymbol(s1)==0){
      return VerifyNOT(s1);
    }
    if(firstSymbol(s1)==1){
      return VerifyG(s1);
    }
    if(firstSymbol(s1)==2){
      return VerifyF(s1);
    }
    if(firstSymbol(s1)==3){
      return VerifyU(s1);
    }
    if(firstSymbol(s1)==4){
      return VerifyR(s1);
    }
    if(firstSymbol(s1)==5){
      return VerifyX(s1);
    }
    
    return false;//是否合理？
  }
  
  def VerifyU(s:String):Boolean={
     var result = s.split("U", 2);
      //println("e=" +spec.apply("e"));
     var r1 = result.apply(1).trim();
     var r0 = result.apply(0).trim();
     //println("r1="+r1+"r0="+r0);
     //println(r1=="e");
     if(dfs(graph,spec.apply(r1),spec.apply(r0))){
       println("false");
       return false;
     }
     else 
        println("true");
        return true;
  }
  
  def VerifyNOT(s:String):Boolean={

//    println("VerifyNOT->:" + s);
//    if("".equals(s)){
//      return false;
//    }

      var result = s.substring(1);
      if(VerifyByString(result))
      {
        return false;
      }
      else 
        return true;
  }
  
  def VerifyX(s:String):Boolean={ //总是的判别法在于判断原公式的取反是否成立 总是a等价于不存在非a
     var result = s.split("X", 2);
     var r1 = result.apply(1).trim();
     var r0 = result.apply(0).trim();
     var childList = graph.apply(spec.apply(r0));
     //println("childList="+childList);
     if(childList.contains(spec.apply(r1)))
     {
       return true;
     }
     else
       return false;
  }
  
  def VerifyF(s:String):Boolean={
    //println("s="+s);
    var result = s.substring(2,s.length()).trim();
    //println(result);
    if(result.indexOf("X")>=0){
      return VerifyByString(result);
    }
    else{var index = spec.apply(result);
    if(index>=0)
      return true;
    else 
      return false;
    }
  }
  
  def VerifyG(s:String):Boolean={
    var result = s.substring(2,s.length()).trim();
    var result1 = result;
    if(result.indexOf("X")>=0){
      if(result.startsWith("(")&&result.endsWith(")"))
    {
      result1 = result.substring(1,result.length()-1).trim();
    }
      var result2 = result1.split("X", 2);
      var r1 = result2.apply(1).trim();
      var r0 = result2.apply(0).trim();
      var childList = graph.apply(spec.apply(r0));
      if(childList.length>1)
      {
        println("\n\nCounter Example:")
        childList.foreach{f =>
         if(f != spec.apply(r1))
         {
           print("\n"+spec.apply(r0));
           print("->");
           print(getNameOfReactionRule(spec.apply(r0),f));
           print("->");
           print(f);
         }
        }
        return false;
      }
      else if((childList.length==1)&&(childList.indexOf(spec.apply(r1))>=0)){
        return true;
      }
      else{
        return false;
      }
    }
    else{
      //println("*******************"+s);
      if(result.startsWith("!"))
      {
        //println("-----------------");
        if(spec.apply(result.substring(1, result.length()))>=0)
        {
          println("\n\nCounter Example:")
          dfs(graph,0,spec.apply(result.substring(1, result.length())));
          return false;
        }
        else
          return true;
      }
      return false;//lry
    }
  }
  
  def VerifyR(s:String):Boolean={
    return true;//wait for rewrite
  }
  
  def firstSymbol(s:String):Int={
    var index0 = s.indexOf("!");//NOT
    var index1 = s.indexOf("[]");//always
    var index2 = s.indexOf("<>");//eventually
    var index3 = s.indexOf("U");//until
    var index4 = s.indexOf("V");//realease
    var index5 = s.indexOf("X");//next
    var it = Iterator(index0,index1,index2,index3,index4,index5);
    var max = it.max;
    if(index0<0){
      index0 = max;
    }
    if(index1<0){
      index1 = max;
    }
    if(index2<0){
      index2 = max;
    }
    if(index3<0){
      index3 = max;
    }
    if(index4<0){
      index4 = max;
    }
    if(index5<0){
      index5 = max;
    }
    var it1 = Iterator(index0,index1,index2,index3,index4,index5);
    var min = it1.min;
    //println("min="+min);
    if(min==s.indexOf("!")){
      return 0;
    }
    else if(min==s.indexOf("[]")){
      return 1;
    }
    else if(min==s.indexOf("<>")){
      return 2;
    }
    else if(min==s.indexOf("U")){
      return 3;
    }
    else if(min==s.indexOf("V")){
      return 4;
    }
    else{
      return 5;
    }
  }
  
  
  
  
  
}


