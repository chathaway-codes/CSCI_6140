package com.logrit.simulator;

import java.util.ArrayList;

import com.logrit.simulator.ProcessState.PROCESS_STATE;

public class ProcessQueue {
	private ArrayList<ProcessState> queue;
	private PROCESS_STATE type;
	
	public ProcessQueue() {
		this(new ArrayList<ProcessState>(), PROCESS_STATE.BURSTING);
	}
	
	public ProcessQueue(PROCESS_STATE type) {
		this(new ArrayList<ProcessState>(), type);
	}
	
	public ProcessQueue(ArrayList<ProcessState> queue, PROCESS_STATE type) {
		this.queue = queue;
		this.type = type;
	}
	
	public void updateTime(int delta) {
		for(ProcessState p : queue) {
			p.updateTime(delta);
		}
	}
	
	public ArrayList<Process> getPastTimeProcesses() {
		ArrayList<Process> t = new ArrayList<Process>();
		
		for(ProcessState p : queue) {
			if(p.timeRemaining() <= 0) {
				t.add(p.process);
				t.remove(p);
			}
		}
		
		return t;
	}
	
	public boolean addProcess(Process p) {
		if(!queue.contains(p)) {
			queue.add(new ProcessState(p, type));
			return true;
		}
		return false;
	}
	
	public ArrayList<ProcessState> getProcesses() {
		return this.queue;
	}
	
	public boolean equals(Object other) {
		if(other instanceof ProcessQueue) {
			ProcessQueue o = (ProcessQueue)other;
			boolean ret = true;
			ret &= o.type == this.type;
			ret &= o.queue.containsAll(this.queue);
			ret &= this.queue.containsAll(o.queue);
			
			return ret;
		}
		return false;
	}
}
