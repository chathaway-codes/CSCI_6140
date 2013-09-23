package com.logrit.simulator;

import java.util.ArrayList;

import com.logrit.simulator.ProcessState.PROCESS_STATE;

public abstract class Queue {
	protected ArrayList<State> states;
	protected ProcessQueue sleeping;
	protected ProcessQueue bursting;
	protected int running_time;
	
	public Queue(ProcessQueue bursting) {
		this(bursting, new ProcessQueue(PROCESS_STATE.SLEEPING), 0, new ArrayList<State>());
	}
	
	public Queue(ProcessQueue bursting, ProcessQueue sleeping, int running_time, ArrayList<State> states) {
		this.bursting = bursting;
		this.sleeping = sleeping;
		this.running_time = running_time;
		this.states = states;
	}
	
	/**
	 * Selects the next process to run
	 * @return
	 */
	public abstract ProcessState selectProcess();
	
	/**
	 * Updates the sleeping processes, and puts any
	 *  that get wakened into the burst queue
	 * @param delta
	 */
	public void populate(int delta) {
		sleeping.updateTime(delta);
		ArrayList<ProcessState> new_processes = sleeping.getPastTimeProcesses();
		
		while(new_processes.size() > 0) {
			ProcessState p = new_processes.remove(0);
			sleeping.removeProcess(p.process);
			bursting.addProcess(p.process, p.timeRemaining());
		}
	}

	public int tick(ProcessState p) {
		ArrayList<ProcessState> processes = bursting.getProcesses();
		
		if(p.arrive_at > 0) {
			return p.arrive_at;
		}
		processes.remove(p);
		
		sleeping.addProcess(p.process, 0);
		
		return p.timeRemaining();
	}
	
	/**
	 * Runs through this queue until it is complete...
	 */
	public void run() {
		int delta = 0;
		ProcessState process = selectProcess();
		while(!this.complete()) {
			delta = this.tick(process);
			this.running_time += delta;
			this.bursting.updateArriveAt(delta);
			this.populate(delta);
			// It's possible the tick didn't remove the process,
			//  but left has us skip forward to where it comes in
			//   (ie, if it enters the queue later)
			//  If so, we need to NOT ask for a new process,
			//   or we would have a premptive queue, which is not what we want
			if(!bursting.getProcesses().contains(process)) {
				process = selectProcess();
			}
		}
	}
	
	/**
	 * Checks to see if the current state has existed before
	 *   If it does, that means we will be repeating something
	 *   that has happened before, so we are done/in an infinite loop
	 * @return
	 */
	protected boolean complete() {
		State current_state = new State(bursting, sleeping);
		
		for(State s : states) {
			if(s.equals(current_state)) {
				return true;
			}
		}
		
		states.add(current_state);
		
		return false;
	}
	
	public int getRunningTime() {
		return this.running_time;
	}
}
