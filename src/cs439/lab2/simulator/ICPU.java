package cs439.lab2.simulator;

/**
 * Title:        
 * Description:
 * Copyright:    Copyright (c) 2012
 * Company:      University of Texas at Austin
 * @author R.L. Rockhold, Ph.D.
 * @version 1.0
 */

public interface ICPU extends Runnable {
	
	/**
	 * Returns this CPU's id.
	 * @return  this CPUs 'id'
	 */
	public int getID();
	
	/**
	 * Tell the CPU to Preempt the running process on the next clock interrupt
	 * At the next clock tick (interrupt):
	 *    if the process blocks, processBlocked() will be called
	 *    if the quantum expires, quantumExpired() will be called
	 *    otherwise, quantumInterrupted() will be called
	 *    Regardless, the CPU will reset this request.
	 */
	public void setPreemptRequest();
	
}
