package com.logrit.simulator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class SJFQueue extends Queue {
	
	public SJFQueue(ProcessQueue bursting) {
		super(bursting);
	}
	public SJFQueue(ProcessQueue bursting, ProcessQueue sleeping,
			int running_time, ArrayList<State> states) {
		super(bursting, sleeping, running_time, states);
	}

	@Override
	public ProcessState selectProcess() {
		ArrayList<ProcessState> processes = bursting.getProcesses();
		
		Collections.sort(processes, new Comparator<ProcessState>() {
			public int compare(ProcessState a, ProcessState b) {
				// Do not let things that aren't in the queue win yet!
				if(a.arrive_at > 0 && b.arrive_at <= 0)
					return 1;
				else if(b.arrive_at > 0 && a.arrive_at <= 0)
					return -1;
				return a.process.getBurst_time() - b.process.getBurst_time();
			}
		});
		
		return processes.get(0);
	}

}
