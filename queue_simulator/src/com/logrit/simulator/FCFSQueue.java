package com.logrit.simulator;

import java.util.ArrayList;

public class FCFSQueue extends Queue {
	
	public FCFSQueue(ProcessQueue bursting) {
		super(bursting);
	}

	@Override
	public int tick() {
		// Get the first process in the list
		ArrayList<ProcessState> processes = bursting.getProcesses();
		
		ProcessState p = processes.get(0);
		processes.remove(0);
		
		sleeping.addProcess(p.process);
		
		return p.timeRemaining();
	}
}
