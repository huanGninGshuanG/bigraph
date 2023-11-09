package pku.ss.attackgraph.metrics;


import utils.BigSimThreadFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/*
 *  author: Liu ruoyu
 */

public class AttackGraph {
    public HashMap<String, StateNode> nodes;
    public int nodesSize;

    public AttackGraph() {
        nodes = new HashMap<String, StateNode>();
    }

    public void setNodesSize(int nodesSize) {
        this.nodesSize = nodesSize;
    }

    public StateNode findNodeByID(String id) {
        StateNode node = nodes.get(id);
        return node;
    }

    public void addOneNode(String id, StateNode node) {
        nodes.put(id, node);
    }

    Executor BigSimPool = new ThreadPoolExecutor(10,10,
            1L, TimeUnit.SECONDS,
            new LinkedBlockingQueue(),
            new BigSimThreadFactory("Graph"));

    public void traverse() {
        Iterator<Entry<String, StateNode>> iter = nodes.entrySet().iterator();
        while (iter.hasNext()) {

//            BigSimPool.execute(new Runnable() {
//                @Override
//                public void run() {
//                    Entry<String, StateNode> entry = iter.next();
//                    String id = entry.getKey();
//                    StateNode val = entry.getValue();
//                    System.out.println("------" + id + "------");
//                    for (int i = 0; i < val.next.size(); i++) {
//                        System.out.println("----" + val.next.get(i) + "----");
//                    }
//                }

//            });
            Entry<String, StateNode> entry = iter.next();
            String id = entry.getKey();
            StateNode val = entry.getValue();
            System.out.println("------" + id + "------");
            for (int i = 0; i < val.next.size(); i++) {
                System.out.println("----" + val.next.get(i) + "----");
            }
        }
    }

}
