package cs439.lab2.lock;
/**
 * Title:        
 * Description:  Throw this exception on a protocol violation
 * Copyright:    Copyright (c) 2012
 * Company:      University of Texas at Austin
 * @author R.L. Rockhold, Ph.D.
 * @version 1.0
 */

public class LockProtocolViolation extends IllegalStateException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public LockProtocolViolation(IStatsLock lock, Thread caller, Thread owner) {
		super("Thread " + caller.getName() + " attempted to relase lock " + lock.getName() + " which is owned by " + owner.getName());
	}
}
