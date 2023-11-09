package org.bigraph.bigsim

import org.bigraph.bigsim.model.Bigraph

import scala.collection.mutable.Set
import java.lang.Boolean

import scala.collection.mutable.ListBuffer
import scala.collection.Parallel
import org.bigraph.bigsim.specification.GenBigraph
import org.slf4j.{Logger, LoggerFactory};

/*
 * author: yanwei
 * this mod is for auto verify 
 */

class GraphNode(ss:Bigraph,qq:Bigraph){
  var s:Bigraph = ss;  //model bigraph
  var q:Bigraph = qq;  //spec bigraph
  var next:Set[GraphNode] = Set();
  
  def AddNext(n:GraphNode) = {
    next.add(n);
  }
}

object Verify{

  def logger : Logger = LoggerFactory.getLogger(this.getClass)

   var modelIndex:Int = 0; 
  
   private var models: Set[Bigraph] = Set();
   
   private var specification: Set[Bigraph] = Set();
   
   var graph:Set[GraphNode] = Set() ;
   
   var curBI:Bigraph = null;
   
   def AddModel(b:Bigraph):Unit = {
     models.foreach { model =>
       //println("循环");
       if(VerifyMatcher.BigraphIsEqual(b.root,model.root)){
         return;
       }
     }
     if(curBI!=null)
        curBI.linked = b;
      curBI = b;
     // logger.debug("models add"+b.root);//lry
      models.add(b);
   }


  // 刘若愚学长修复了一些bug，见论文
   def Calculate(){
     /*
      *  step1: 构建同步积
      */
     
     var sIndex = 0;
     var mIndex = 0;
     
     specification = GenBigraph.getAllBigraph();//lry:specification用于存储转化后的规约内容
     logger.debug("specification="+specification);//lry
     logger.debug("models.size="+models.size+"models="+models);//lry
      specification.foreach { x => 
         models.foreach { y =>
             if(y.verifyID.equals("")){
               y.verifyID = "S" +mIndex;//lry:y中使用"S"代表待检测性质
               mIndex = mIndex +1;
             }
              if(x.verifyID.equals("")){
               x.verifyID = "P" +sIndex;//lry:x中使用"P"代表原模型
               sIndex = sIndex +1;
             }
             
             var g = new GraphNode(y,x);
           logger.debug("spec="+x.root+" "+x.verifyID+"model="+y.root+" "+y.verifyID);//lry
             graph.add(g);   
         }    
      }
      
       /*
      *  step2: 去false节点 
      */
          
      var g:Set[GraphNode] = Set();
      
      graph.foreach { node =>
        logger.debug("s="+node.s.verifyID+"s.isInitial="+node.s.isInitial+"s.isFinal"+node.s.isFinal+"s.label="+node.s.label);//lry
        logger.debug("q="+node.q.verifyID+"q.isInitial="+node.q.isInitial+"q.isFinal"+node.q.isFinal+"q.label="+node.q.label+"\n");//lry
          if(VerifyMatcher.Match(node.s, node.q)){
            g.add(node);
          }
      }
      
      graph = g;
      graph.foreach{ node =>
        logger.debug("-------node="+node.s.verifyID+node.q.verifyID);//lry
      }
      
      /*
       * step3: 建立链接关系
       */
      graph.foreach { node => 
            graph.foreach { otherNode => 
              if(node!=otherNode){
                if(node.s.linked == otherNode.s){
                  logger.debug("lry"+node.s.verifyID+"->"+node.s.linked.verifyID)//lry
                  var sets = GenBigraph.getNextBigraph(node.q);
                  sets.foreach { allLinked =>
                      if(allLinked ==otherNode.q){
                        node.AddNext(otherNode);
                        logger.debug("node.s.id="+node.s.verifyID+"node.q.id="+node.q.verifyID+"otherNode.s.id="+otherNode.s.verifyID+"otherNode.q.id="+otherNode.q.verifyID);
                      }  
                  }
                }
              }
              }
      }        
       /*
       * step4: 找连通分量，判断满足性
       */
      var al = new TarJanAlgorithm(graph);
      al.run();
   }
   
}

class AAA{
  
}


object test extends App{
   override def main(args: Array[String]) = {     
        var a:Set[AAA] = Set();
        var bbb = new AAA()
       
        a.add(bbb)
        
        bbb = new AAA();
        a.add(bbb);
        a.foreach { x => println(x) }
   }
}
