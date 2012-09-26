package student;

/**
 * Title:        
 * Description:
 * Copyright:    Copyright (c) 2012
 * Company:      University of Texas at Austin
 * @author 
 * @version 1.0
 */

import cs439.lab2.lock.FIFOLock;
import cs439.lab2.lock.IStatsLock;
import cs439.lab2.lock.LockProtocolViolation;
import cs439.lab2.lock.ScheduledThread;

public class RecursiveLock implements IStatsLock {


	public RecursiveLock(String name) {
		throw new IllegalStateException("Please implement me.");
	}
	
	/* (non-Javadoc)
	 * ADVISORY ONLY.
	 * DOES NOT NEED TO BE THREAD SAFE
	 * DO NOT SYNCHRONIZE 
	 */
	public static int getInstanceCount() {
		// return the total number of instances created (since last loaded by JVM)
		throw new IllegalStateException("Please implement me.");
	}

	/* (non-Javadoc)
	 * @see ILock#acquire()
	 */
 	public int acquire() {
		throw new IllegalStateException("Please implement me.");
	}

	/* (non-Javadoc)
	 * @see ILock#acquire_try()
	 */
	public int release() {
		throw new IllegalStateException("Please implement me.");
	}

	/* (non-Javadoc)
	 * @see ILock#acquire_try()
	 */
	public int acquire_try() {
		throw new IllegalStateException("Please implement me.");
	}

	/* (non-Javadoc)
	 * @see ILock#getName()
	 */
	public String getName() {
		throw new IllegalStateException("Please implement me.");
	}

	/* (non-Javadoc)
	 * @see IStatsLock#getWaitersCount()
	 * ADVISORY ONLY.
	 * DOES NOT NEED TO BE THREAD SAFE
	 * DO NOT SYNCHRONIZE 
	 */
	public int getWaitersCount() {
		throw new IllegalStateException("Please implement me.");
	}

	/* (non-Javadoc)
	 * @see IStatsLock#getTrySuccessCount()
	 * ADVISORY ONLY.
	 * DOES NOT NEED TO BE THREAD SAFE
	 * DO NOT SYNCHRONIZE 
	 */
	public int getTrySuccessCount() {
		throw new IllegalStateException("Please implement me.");
	}

	/* (non-Javadoc)
	 * @see IStatsLock#getTryFailCount()
	 * ADVISORY ONLY.
	 * DOES NOT NEED TO BE THREAD SAFE
	 * DO NOT SYNCHRONIZE 
	 */
	public int getTryFailCount() {
		throw new IllegalStateException("Please implement me.");
	}

	/* (non-Javadoc)
	 * @see IStatsLock#getTotalLockHeldTime()
	 * ADVISORY ONLY.
	 * DOES NOT NEED TO BE THREAD SAFE
	 * DO NOT SYNCHRONIZE 
	 */
	public long getTotalLockHeldTime() {
		throw new IllegalStateException("Please implement me.");
	}

	/* (non-Javadoc)
	 * @see IStatsLock#getUseCount()
	 * ADVISORY ONLY.
	 * DOES NOT NEED TO BE THREAD SAFE
	 * DO NOT SYNCHRONIZE 
	 */
	public int getUseCount() {
		throw new IllegalStateException("Please implement me.");
	}

}
