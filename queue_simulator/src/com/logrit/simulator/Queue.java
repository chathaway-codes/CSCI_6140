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
	 * Moves forward the queue to the next event
	 * @return the delta's between when this was called and when it returns
	 */
	public abstract int tick();
	
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
	
	/**
	 * Runs through this queue until it is complete...
	 */
	public void run() {
		int delta = 0;
		while(!this.complete()) {
			delta = this.tick();
			this.running_time += delta;
			this.bursting.updateArriveAt(delta);
			this.populate(delta);
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
