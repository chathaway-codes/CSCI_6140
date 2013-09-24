package com.logrit.simulator;

import java.util.ArrayList;

public class ProcessState implements Cloneable {
	protected Process process;
	protected int priority;
	protected int time;
	protected int arrive_at;
	protected PROCESS_STATE state;
	
	public ProcessState(Process process, PROCESS_STATE state, int arrive_at) {
		this(process, state, 0, 0, arrive_at);
	}
	
	public ProcessState(Process process, PROCESS_STATE state, int priority, int time, int arrive_at) {
		this.process = process;
		this.state = state;
		this.priority = priority;
		this.time = time;
		this.arrive_at = arrive_at;
	}
	
	public void updateTime(int delta) {
		this.time += delta;
	}
	
	public void updateArriveAt(int delta) {
		arrive_at -= delta;
	}
	
	public Process getProcess() {
		return this.process;
	}
	
	public int timeRemaining() {
		switch(state) {
		case SLEEPING:
			return process.getSleep_time() - time;
		default:
			return process.getBurst_time() - time;
		}
	}
	
	public String toString() {
		return process.getPid() + "(" + state + "): " 
				+ this.timeRemaining() + " of " 
				+ (state == PROCESS_STATE.SLEEPING ? process.getSleep_time() : process.getBurst_time())
				+ " starting in " + this.arrive_at;
	}
	
	public boolean equals(Object other) {
		if(other instanceof ProcessState) {
			ProcessState o = (ProcessState) other;
			boolean ret = true;
			ret &= o.process == this.process;
			ret &= o.priority == this.priority;
			ret &= o.timeRemaining() == this.timeRemaining();
			ret &= o.arrive_at == this.arrive_at;
			ret &= o.state == this.state;
			return ret;
		}
		return false;
	}
	
	public Object clone() throws CloneNotSupportedException {
		ProcessState o = (ProcessState) super.clone();
		return o;
	}
	
	public enum PROCESS_STATE {
		SLEEPING,
		BURSTING
	}
}
