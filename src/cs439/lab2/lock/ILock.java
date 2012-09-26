package cs439.lab2.lock;
/**
 * Title:        
 * Description:
 * Copyright:    Copyright (c) 2012
 * Company:      University of Texas at Austin
 * @author R.L. Rockhold, Ph.D.
 * @version 1.0
 */

public interface ILock {
	/**
	 * Requests ownership of a lock.
	 * Caller will block if the lock is not available.
	 * @return recursion depth (always 1 for non-recursive locks)
	 */
	int acquire();

	/**
	 * Releases a lock
	 * @return remaining recursion depth (always 0 for non-recursive locks)
	 */
	int release();
	
	/**
	 * Attempts to acquire a lock immediately.
	 * Never blocks.
	 * @return 0 if lock not available, otherwise the recursion depth. 
	 */
	int acquire_try();
	
	
	/** 
	 * @return the lock name.
	 */
	String getName();

}
