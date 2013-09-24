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
	public ProcessState selectProcess() {
		// Get the first process in the list
		ArrayList<ProcessState> processes = bursting.getProcesses();
		
//		Collections.sort(processes, new Comparator<ProcessState>() {
//			public int compare(ProcessState a, ProcessState b) {
//				return a.arrive_at - b.arrive_at;
//			}
//		});
		
		return processes.get(0);
	}
	
	public String toString() {
		return "First Come First Serve Queue";
	}
}
