package com.logrit.simulator;

public class ProcessState {
	protected Process process;
	protected int priority;
	protected int time;
	protected PROCESS_STATE state;
	
	public ProcessState(Process process, PROCESS_STATE state) {
		this(process, state, 0, 0);
	}
	
	public ProcessState(Process process, PROCESS_STATE state, int priority, int time) {
		this.process = process;
		this.state = state;
		this.priority = priority;
		this.time = time;
	}
	
	public void updateTime(int delta) {
		this.time += delta;
	}
	
	public int timeRemaining() {
		switch(state) {
		case SLEEPING:
			return process.getSleep_time() - time;
		default:
			return process.getBurst_time() - time;
		}
	}
	
	public enum PROCESS_STATE {
		SLEEPING,
		BURSTING
	}
}
