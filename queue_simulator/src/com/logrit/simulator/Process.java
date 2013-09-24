package com.logrit.simulator;

public class Process {
	private int pid;
	private int burst_time;
	private int sleep_time;
	
	private Process(int pid, int burst_time, int sleep_time) {
		this.pid = pid;
		this.burst_time = burst_time;
		this.sleep_time = sleep_time;
	}
	
	public int getPid() {
		return pid;
	}
	public int getBurst_time() {
		return burst_time;
	}
	public int getSleep_time() {
		return sleep_time;
	}
	
	public boolean equals(Object other) {
		if(other instanceof Process) {
			return ((Process)other).pid == this.pid;
		}
		return false;
	}
	
	public String toString() {
		return Integer.toString(this.pid);
	}
	
	private static int last_pid = 0;
	public static Process makeProcess(int burst_time, int sleep_time) {
		return new Process(++last_pid, burst_time, sleep_time);
	}
	
	/**
	 * Be careful when calling this!
	 * If you ever interact with a Process from another set,
	 *   they may appear to be equal.
	 */
	public static void resetPid() {
		last_pid = 0;
	}
}
