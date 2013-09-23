package com.logrit;

import com.logrit.simulator.FCFSQueue;
import com.logrit.simulator.Process;
import com.logrit.simulator.ProcessQueue;
import com.logrit.simulator.Queue;

public class queue_simulator {

	public static void main(String[] args) {
		ProcessQueue processes = new ProcessQueue();
		
		processes.addProcess(Process.makeProcess(8, 4));
		processes.addProcess(Process.makeProcess(4, 2));
		
		Queue FCFS = new FCFSQueue(processes);
		
		FCFS.run();
		
		System.out.println(FCFS.getRunningTime());
		
		
	}

}
