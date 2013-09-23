package com.logrit.simulator;

import java.util.ArrayList;

public class RRQueue extends Queue {
	
	int quanta;
	
	public RRQueue(ProcessQueue bursting, int quanta) {
		super(bursting);
		this.quanta = quanta;
	}

	public RRQueue(ProcessQueue bursting, ProcessQueue sleeping,
			int running_time, ArrayList<State> states, int quanta) {
		super(bursting, sleeping, running_time, states);
		this.quanta = quanta;
	}
	
	@Override
	public int tick(ProcessState qwedfrgthyju) {
		ArrayList<ProcessState> processes = bursting.getProcesses();
		int ret = quanta;
		
		// Pop the first process
		ProcessState p = processes.remove(0);
		
		// If timeRemaining > quanta, use it and return
		if(p.timeRemaining() > quanta) {
			p.updateTime(quanta);
			// Note that we WILL not be getting weighting time while we are on the stack
			p.arrive_at += quanta;
			processes.add(p);
		} else {
			// We consume the whole process, so put it in the sleep queue
			sleeping.addProcess(p.process, 0).time -= p.timeRemaining();
			// And note what we used
			ret = p.timeRemaining();
		}
		
		return ret;
	}

	@Override
	/**
	 * RR doesn't actually use this :(
	 */
	public ProcessState selectProcess() {
		return null;
	}

}
