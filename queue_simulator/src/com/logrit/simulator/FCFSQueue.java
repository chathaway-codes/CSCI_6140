package com.logrit.simulator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class FCFSQueue extends Queue {
	
	public FCFSQueue(ProcessQueue bursting) {
		super(bursting);
	}
	
	public FCFSQueue(ProcessQueue bursting, ProcessQueue sleeping, int running_time, ArrayList<State> states) {
		super(bursting, sleeping, running_time, states);
	}

	@Override
	public int tick() {
		// Get the first process in the list
		ArrayList<ProcessState> processes = bursting.getProcesses();
		
//		Collections.sort(processes, new Comparator<ProcessState>() {
//			public int compare(ProcessState a, ProcessState b) {
//				return a.arrive_at - b.arrive_at;
//			}
//		});
		
		ProcessState p = processes.get(0);
		
		if(p.arrive_at > 0) {
			return p.arrive_at;
		}
		processes.remove(0);
		
		sleeping.addProcess(p.process, 0);
		
		return p.timeRemaining();
	}
}
