package cs439.lab2.simulator;

/**
 * Title:        
 * Description:
 * Copyright:    Copyright (c) 2012
 * Company:      University of Texas at Austin
 * @author R.L. Rockhold, Ph.D.
 * @version 1.0
 */

public interface IScheduler {
	
	/**
	 * This is called when a new process enters the system
	 * and is ready to be scheduled.
	 * @param pcb  The pcb of the new process.
	 */
	void processStarted(PCB pcb);
	
	
	/**
	 * This is called when the process's
	 * assigned quantum has expired (i.e. when
	 * the process didn't block voluntarily).
	 * The process has been "undispatched" and should
	 * be added back to the scheduler's ready-queue.
	 * The scheduler's schedule() method will be called very soon 
	 *   after this call.
	 * Note that if the quantum expired at the same time it would have been
	 * preempted, only quantumExpired() is called.
	 * @param pid The id of the process whose quantum expired
	 * @param cpu  The cpu that had been running the process.
	 * @param ticks_given Number of ticks consumed during last dispatch
	 */
	void quantumExpired(Integer pid, ICPU cpu, int ticks_given);
	
	/**
	 * Called when looking for work to run on CPU cpu.
	 * Remember to change the quantum for this process if
	 * the default (5) is not appropriate.
	 * @param cpu
	 * @return The next process to run, or null if none are ready.
	 */
	Integer schedule(ICPU cpu);
	
	/**
	 * Get the pid for the currently running process on CPU cpu
	 * @return
	 */
	Integer getCurrentPID(ICPU cpu);


	/**
	 * Informational.  This process has left-the-building.
	 * @param pid
	 */
	void processExited(Integer pid);


	/**
	 * Informational.
	 * This process was executing, but has blocked for I/O.
	 * The scheudler's schedule() method will soon be called.
	 * Note that if the process blocked at the same time its quantum would have expired,
	 * or it would have been preempted, only processBlocked() is called.
	 * @param pid
	 * @param cpu
	 * @param ticks_given Number of ticks consumed during last dispatch before blocking
	 */
	void processBlocked(Integer pid, ICPU cpu, int burst_given);


	/**
	 * This process is no longer blocked (I/O complete) and should be added
	 * to the scheduler's list of ready processes.
	 * @param pid
	 */
	void processUnblocked(Integer pid);


    /**
	 * Informational.
	 * This process was executing, but had its quantum interrupted.
	 * The scheudler's schedule() method will soon be called.
	 * Note that if the quantumInterrupted is called only when the process did not block
	 * voluntarily and its quantum did not expire.
     * @param pid
     * @param cpu
     * @param ticks_given Number of ticks consumed during last dispatch
     */
    void quantumInterrupted(Integer pid, CPU cpu, int ticks_given);

}
