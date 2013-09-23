package com.logrit.simulator;

import com.logrit.simulator.ProcessState.PROCESS_STATE;

public class State {
	ProcessQueue queue;
	ProcessQueue sleep_queue;
	
	public State() {
		this(new ProcessQueue(PROCESS_STATE.BURSTING), new ProcessQueue(PROCESS_STATE.SLEEPING));
	}
	
	public State(ProcessQueue queue, ProcessQueue sleep_queue) {
		try {
			this.queue = (ProcessQueue) queue.clone();
			this.sleep_queue = (ProcessQueue) sleep_queue.clone();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public boolean equals(Object other) {
		if(other instanceof State) {
			State o = (State)other;
			boolean ret = true;
			ret &= o.queue.equals(this.queue);
			ret &= o.sleep_queue.equals(this.sleep_queue);
			return ret;
		}
		return false;
	}
	
	public String toString() {
		return "Bursting: " + queue + "\nSleeping: " + sleep_queue;
	}
}
