package pku.ss.attackgraph.metrics

import org.bigraph.bigsim.model.BiNode;
import scala.collection.mutable.Set
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.ListBuffer
import org.bigraph.bigsim.specification.GenBigraph
import org.bigraph.bigsim.model.Specification;

/*
 *  author: Liu ruoyu
 */

object MetricsGetter {
  var length = BiNode.allBiNodes.length;
  var graphBefore:ArrayBuffer[ArrayBuffer[Int]] = new ArrayBuffer[ArrayBuffer[Int]](length);
  var specs:ArrayBuffer[Int] = new ArrayBuffer[Int]();
  var graph:AttackGraph=new AttackGraph(); 
  def Create()={
    graph.setNodesSize(BiNode.allBiNodes.length);
    var allBiNodes = BiNode.allBiNodes;
    var id = 0;
    for(node <-allBiNodes){
      var nodeNew:StateNode = new StateNode();
      nodeNew.setId(id.toString());
      if(id==0){
        nodeNew.setInitial();
      }
      var childList = new ArrayBuffer[Int];
      for(child<-node.childList){
        var index = BiNode.indexAt(child.bigraph);
        nodeNew.addNext(index.toString());
        childList+=index;
      }
      graphBefore+=childList;
      graph.addOneNode(id.toString(),nodeNew);
      id = id+1;
    }
    println("\n\ngraph=");
    graph.traverse();
    getSpecification();
    for(spec<-specs){
      var node = graph.findNodeByID(spec.toString());
      node.setAttackGoal();
      graph.addOneNode(spec.toString(), node);
    }
  }
  def getSpecification()={
     //var specification: Set[Bigraph] = Set();
     //specification = GenBigraph.getAllBigraph();//lry:specification用于存储转化后的规约内容
     var specification: Specification = Specification.processSpec();
     //println("spec="+specification);//lry
     println("\n\nspecs=");
     specification.proposition.keySet.foreach { x => 
     var b = specification.proposition(x);
     println(BiNode.indexAt(b));
     specs+=BiNode.indexAt(b);
    }
  }
  def getMetrics()={
     Create();
     var test:Test = new Test();
     println("\n\nAll Security Metrics:");
     test.test(graph);
  }
  
}