package com.logrit.simulator;

import static org.junit.Assert.*;

import org.junit.Test;

import com.logrit.simulator.ProcessState.PROCESS_STATE;

import java.util.ArrayList;

public class QueueTests {
	// These tests will use the FCFS Queue, because it is easy
	
	public void setUp() {
		
	}
	
	@Test
	public void testCompleteDetectsPreviousState() {
		Process p1 = Process.makeProcess(8, 4);
		Process p2 = Process.makeProcess(4, 2);
		ProcessState t = null;
		int running_time = 0;
		
		ProcessQueue bursting = new ProcessQueue(PROCESS_STATE.BURSTING);
		ProcessQueue sleeping = new ProcessQueue(PROCESS_STATE.SLEEPING);
		
		bursting.addProcess(p1, 0);
		bursting.addProcess(p2, 0);
		
		ArrayList<State> states = new ArrayList<State>();

		// b: 1 2
		// s: 
		states.add(new State(bursting, sleeping));
		
		t = bursting.getProcesses().remove(0);
		running_time += t.process.getBurst_time();
		sleeping.addProcess(t.process, running_time);

		// b: 2
		// s: 1
		states.add(new State(bursting, sleeping));
		
		t = bursting.getProcesses().remove(0);
		running_time += t.process.getBurst_time();
		sleeping.addProcess(t.process, running_time);
		t = sleeping.getProcesses().remove(0);
		bursting.addProcess(t.process, running_time);

		// b: 1
		// s: 2
		states.add(new State(bursting, sleeping));
		
		t = sleeping.getProcesses().remove(0);
		running_time += t.process.getSleep_time();
		bursting.addProcess(t.process, running_time);

		// b: 1 2
		// s: 
		states.add(new State(bursting, sleeping));
		
		FCFSQueue queue = new FCFSQueue(bursting, sleeping, 0, states);
		
		// And we should now be complete
		assertTrue(queue.complete());
	}
	
	@Test
	public void testNotComplete() {
		Process p1 = Process.makeProcess(8, 4);
		Process p2 = Process.makeProcess(4, 2);
		ProcessState t = null;
		int running_time = 0;
		
		ProcessQueue bursting = new ProcessQueue(PROCESS_STATE.BURSTING);
		ProcessQueue sleeping = new ProcessQueue(PROCESS_STATE.SLEEPING);
		
		bursting.addProcess(p1, running_time);
		bursting.addProcess(p2, running_time);
		
		ArrayList<State> states = new ArrayList<State>();

		// b: 1 2
		// s: 
		states.add(new State(bursting, sleeping));
		
		t = bursting.getProcesses().remove(0);
		running_time += t.process.getBurst_time();
		sleeping.addProcess(t.process, running_time);

		// b: 2
		// s: 1
		states.add(new State(bursting, sleeping));
		
		t = bursting.getProcesses().remove(0);
		running_time += t.process.getBurst_time();
		sleeping.addProcess(t.process, running_time);
		t = sleeping.getProcesses().remove(0);
		bursting.addProcess(t.process, running_time);

		// b: 1
		// s: 2
		//states.add(new State(bursting, sleeping));
		// This will be added when we call the complete() method
		
		FCFSQueue queue = new FCFSQueue(bursting, sleeping, 0, states);
		
		// And we should now be complete
		assertTrue(!queue.complete());
	}
	
	@Test
	public void testTickTime() {

		ProcessQueue processes = new ProcessQueue();
		
		processes.addProcess(Process.makeProcess(8, 4), 0);
		processes.addProcess(Process.makeProcess(4, 2), 0);
		
		Queue FCFS = new FCFSQueue(processes);
		
		FCFS.run();
		
		assertTrue(FCFS.running_time == 20);
	}
	
	@Test
	public void testTickTimeWithIdleTime() {

		ProcessQueue processes = new ProcessQueue();
		
		processes.addProcess(Process.makeProcess(8, 4), 0);
		processes.addProcess(Process.makeProcess(4, 2), 9);
		
		Queue FCFS = new FCFSQueue(processes);
		
		FCFS.run();
		
		assertTrue(FCFS.running_time == 33);
	}

}
