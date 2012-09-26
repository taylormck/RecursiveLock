package cs439.lab2.lock;
/**
 * Title:        
 * Description:
 * Copyright:    Copyright (c) 2012
 * Company:      University of Texas at Austin
 * @author R.L. Rockhold, Ph.D.
 * @version 1.0
 */

public interface IStatsLock extends ILock {
	
	/**
	 * NON-ATOMIC
	 * @return the number of Threads currently blocked on this Lock
	 */
	int getWaitersCount();
	
	/**
	 * NON-ATOMIC
	 * @return the number of acquire_try() calls that succeeded
	 */
	int getTrySuccessCount();
	
	/**
	 * NON-ATOMIC
	 * @return the number of acquire_try() calls that failed
	 */
	int getTryFailCount();
	
	/**
	 * NON-ATOMIC
	 * This is the cumulative time the lock has been held.
	 * Note that this is only adjusted when a lock is (completely) released,
	 * i.e. release() returns 0.
	 * @return time
	 */
	long getTotalLockHeldTime();
	
	/**
	 * NON-ATOMIC
	 * The number of times the lock has been released (completely),
	 * i.e. with a return value == 0.
	 * @return number of (completed) acquire/release cycles.
	 */
	int getUseCount();
}
