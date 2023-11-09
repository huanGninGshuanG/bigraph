package org.bigraph.bigsim

import org.bigraph.bigsim.model.BiNode
import org.bigraph.bigsim.model.Bigraph
import org.bigraph.bigsim.model.ReactionRule
import java.lang.Boolean
import jdk.nashorn.internal.ir.ContinueNode
import org.bigraph.bigsim.model.ReactionRule
import scala.collection.mutable.Set
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.ListBuffer
import org.bigraph.bigsim.specification.GenBigraph
import org.bigraph.bigsim.model.Specification;
import scala.util.control.Breaks._;
/*
 *  author: Liu ruoyu
 */

object Path {
  var length = BiNode.allBiNodes.length;
  var graph:ArrayBuffer[ArrayBuffer[Int]] = new ArrayBuffer[ArrayBuffer[Int]](length);
  var spec:ArrayBuffer[Int] = new ArrayBuffer[Int]();
  def Create()={
    var allBiNodes = BiNode.allBiNodes;
    for(node <-allBiNodes){
      var childList = new ArrayBuffer[Int];
      for(child<-node.childList){
        var index = BiNode.indexAt(child.bigraph);
        childList+=index;
      }
      graph+=childList;
    }
    //println(graph);
  }
  def dfs_rect(adjListsGraph:ArrayBuffer[ArrayBuffer[Int]],visited:Array[Boolean],v:Int,d:Int,path:ListBuffer[Int]):Unit={
    //println("v="+v);
    visited(v)=true;
    path+=v;
    if(d==v){
      var i = 0;
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
  
  def dfs(adjListsGraph:ArrayBuffer[ArrayBuffer[Int]],d:Int):Unit={
    var n = adjListsGraph.length;
    var visited:Array[Boolean] = new Array[Boolean](n);
    var i=0;
    for(i <- 0 to n-1){
      visited(i)=false;
    }
    /*visited.foreach{f=>
      println(f);
    }*/
    var path:ListBuffer[Int] = new ListBuffer[Int]();
    dfs_rect(adjListsGraph,visited,0,d,path);
  }
  
  def getSpecification()={
     //var specification: Set[Bigraph] = Set();
     //specification = GenBigraph.getAllBigraph();//lry:specification用于存储转化后的规约内容
     var specification: Specification = Specification.processSpec();
     println("spec="+specification);//lry
     specification.proposition.keySet.foreach { x => 
     var b = specification.proposition(x);
     var index = BiNode.indexAt(b)
     println("x="+x+" index="+index);
     spec+=index;
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
  
  def getNameOfReactionRule(index1:Int,index2:Int):String={
      var rule =  BiNode.allBiNodes(index1).NodeToReactionRule.get(BiNode.allBiNodes(index2));
      var rule2 = rule.get;
      var name = rule2.name;
      //println(rule2.name);
      return name;
  }
  
  def getPath()={
    getSpecification();
    var formula = Specification.processSpec().formula;
    //println("formula="+formula);
    if(formula.contains("!")){
        if(spec.length<1){
          println("\n\n\n\n\n\nNo Counter Example!");
        }
        else{
          println("\n\n\n\n\n\nCounter Example Path:");
          Create();
          //println(spec);
          spec.foreach{target=>
          //println("target="+target);
          dfs(graph,target);  
        }
       }
    }
    else{
      Create();
      var flag=0;
      spec.foreach{target=>
        if(target<graph.length){
          flag=1;
          println("\n\n\n\n\n\ntrue");
          break;
        }
      }
      if(flag==0){
        printf("\n\n\n\n\n\nfalse");
      }
    }
    println();
        
  }
  
}