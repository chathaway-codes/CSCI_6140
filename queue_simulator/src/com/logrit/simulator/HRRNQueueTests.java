package com.logrit.simulator;

import static org.junit.Assert.*;

import org.junit.Test;

public class HRRNQueueTests {
	
	@Test
	public void testTickTime() {

		ProcessQueue processes = new ProcessQueue();
		
		Process p1 = Process.makeProcess(8, 4);
		Process p2 = Process.makeProcess(4, 2);

		processes.addProcess(p1, 0);
		processes.addProcess(p2, 0);
		
		Queue queue = new HRRNQueue(processes);		
		queue.run();
		
		assertTrue(queue.running_time == 24);
		
		// Check the stats!
		for(ProcessStatistics p : ProcessStatistics.getAllStats()) {
			if(p.getProcess().equals(p1)) {
				double response_time = p.getResponseTime();
				double slowdown = p.getSlowDown(); 
				assertTrue(response_time == 10);
				assertTrue(slowdown == 1.5);
			}
		}
		
		double cpu_utilization = ProcessStatistics.getCPUUtilization();
		//assertTrue(cpu_utilization == 1);
	}
}
