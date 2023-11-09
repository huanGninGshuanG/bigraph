package pku.ss.attackgraph.metrics;

import java.util.ArrayList;

/*
 *  author: Liu ruoyu
 */
public class StateNode {
	protected String id;
	protected boolean isInitial=false;
	protected ArrayList<String> next = null;
	protected boolean isVisited = false;
	protected boolean isAttackGoal = false;
	public StateNode(){
		next = new ArrayList<String>();
	}
	public StateNode(String id){
		this.id = id;
		next = new ArrayList<String>();
	}
	public void addNext(String node){
		next.add(node);
	}
	public ArrayList<String> getNext(){
		return next;
		
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public boolean getInitial() {
		return isInitial;
	}
	public void setInitial() {
		this.isInitial = true;
	}
	public void setVisited(){
		isVisited = true;
	}
	public boolean isVisited() {
		return isVisited;
	}
	public boolean isAttackGoal(){
		return this.isAttackGoal;
	}
	public void setAttackGoal(){
		this.isAttackGoal = true;
	}
}
