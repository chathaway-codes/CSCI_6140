package com.logrit.simulator;

import com.logrit.simulator.ProcessState.PROCESS_STATE;

public class State {
	ProcessQueue queue;
	ProcessQueue sleep_queue;
	
	public State() {
		this(new ProcessQueue(PROCESS_STATE.BURSTING), new ProcessQueue(PROCESS_STATE.SLEEPING));
	}
	
	public State(ProcessQueue queue, ProcessQueue sleep_queue) {
		this.queue = queue;
		this.sleep_queue = sleep_queue;
	}
	
	public boolean equals(Object other) {
		if(other instanceof State) {
			State o = (State)other;
			boolean ret = true;
			ret &= o.queue == this.queue;
			ret &= o.sleep_queue == this.sleep_queue;
			return ret;
		}
		return false;
	}
}
