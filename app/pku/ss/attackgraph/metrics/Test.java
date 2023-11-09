package pku.ss.attackgraph.metrics;

import java.util.List;

/*
 *  author: Liu ruoyu
 */

public class Test {
	public void test(AttackGraph graph) {
		// TODO Auto-generated method stub
		int NodesCount = graph.nodesSize;
		Tarjan tarjan = new Tarjan(graph);
		//int stronglyConnectedCount = tarjan.getStronglyConnectedCount();
		//System.out.println("stronglyConnectedCount= "+stronglyConnectedCount);
		UnionFind unionfind = new UnionFind(graph);
		//int weeklyConnectedCount = unionfind.getWeeklyConnectedCount();
		//System.out.println("weeklyConnectedCount= "+weeklyConnectedCount);
		String connectivityMetric = unionfind.getConnectivityMetric();
		String cycleMetric = tarjan.getCycleMetric();
		ShortestPath shortestPath = new ShortestPath(graph,unionfind.getWeeklyConnected());
		String DepthMetric = shortestPath.getDepthMetric();
		System.out.println("connectivityMetric\t"+connectivityMetric);
		System.out.println("cycleMetric\t"+cycleMetric);
		System.out.println("DepthMetric\t"+DepthMetric);
	}

}
