package com.logrit.simulator;

import java.util.ArrayList;

import com.logrit.simulator.ProcessState.PROCESS_STATE;

public class ProcessQueue implements Cloneable {
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
	
	public void updateArriveAt(int delta) {
		for(ProcessState p : queue) {
			p.updateArriveAt(delta);
		}
	}
	
	public ArrayList<ProcessState> getPastTimeProcesses() {
		ArrayList<ProcessState> t = new ArrayList<ProcessState>();
		
		for(ProcessState p : queue) {
			if(p.timeRemaining() <= 0) {
				t.add(p);
			}
		}
		
		return t;
	}
	
	public ProcessState addProcess(Process p, int arrive_at) {
		if(!queue.contains(p)) {
			ProcessState new_p = new ProcessState(p, type, arrive_at); 
			queue.add(new_p);
			return new_p;
		}
		return null;
	}
	
	public boolean removeProcess(Process p) {
		for(ProcessState q : queue) {
			if(q.process.equals(p)) {
				queue.remove(q);
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns the state of process p in this queue, 
	 *  or null if it doesn't exist
	 * @param p
	 * @return
	 */
	public ProcessState findProcessState(Process p) {
		for(ProcessState q : queue) {
			if(q.process.equals(p))
				return q;
		}
		return null;
	}
	
	public ArrayList<ProcessState> getProcesses() {
		return this.queue;
	}
	
	public String toString() {
		StringBuffer sb = null;
		
		for(ProcessState p : this.queue) {
			if(sb == null) {
				sb = new StringBuffer();
			} else {
				sb.append(", ");
			}
			sb.append("{ " + p + " }");
		}
		if(sb == null) {
			sb = new StringBuffer();
			sb.append("{Empty}");
		}
		sb.append("\n");
		return sb.toString();
	}
	
	public boolean equals(Object other) {
		if(other instanceof ProcessQueue) {
			ProcessQueue o = (ProcessQueue)other;
			boolean ret = true;
			ret &= o.type == this.type;
			ret &= o.queue.size() == this.queue.size();
			for(int i=0; i < this.queue.size() && ret; i++) {
				ret &= o.queue.get(i).equals(this.queue.get(i));
			}
			
			return ret;
		}
		return false;
	}
	
	public Object clone() throws CloneNotSupportedException {
		ProcessQueue o = (ProcessQueue) super.clone();
		o.queue = new ArrayList<ProcessState>();
		for(ProcessState e : this.queue) {
			o.queue.add((ProcessState) e.clone());
		}
		return o;
	}
}
