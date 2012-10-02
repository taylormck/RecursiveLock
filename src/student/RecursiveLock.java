package student;

/**
 * Title:        
 * Description:
 * Copyright:    Copyright (c) 2012
 * Company:      University of Texas at Austin
 * @author 
 * @version 1.0
 */

import java.util.Queue;

import cs439.lab2.lock.FIFOLock;
import cs439.lab2.lock.IStatsLock;
import cs439.lab2.lock.LockProtocolViolation;
import cs439.lab2.lock.ScheduledThread;

public class RecursiveLock implements IStatsLock {
	private String name;
	private static int instanceCount = 0;
	private int depth;
	private FIFOLock fl;
	private Queue<Waiter> waiters;
	private int trySuccessCount;
	private int tryFailCount;
	private long lockHeldTime;
	private int useCount;
	private boolean locked;

	// Not sure if this is needed
	private static class Waiter {
		Thread t;
		Waiter() {
			t = Thread.currentThread();
		}
	}


	public RecursiveLock(String _name) {
		name = _name;
		synchronized (this){
			instanceCount++;
		}
		depth = 0;
		fl = new FIFOLock(name + "-fl");
	}
	
	/* (non-Javadoc)
	 * ADVISORY ONLY.
	 * DOES NOT NEED TO BE THREAD SAFE
	 * DO NOT SYNCHRONIZE 
	 */
	public static int getInstanceCount() {
		return instanceCount;
	}

	/* (non-Javadoc)
	 * @see ILock#acquire()
	 */
	
	// To be modified
 	public int acquire() {
		Waiter w = new Waiter();
		boolean must_wait;
// Block 1 for questions 1a, 1b		
		synchronized (w) {
			synchronized (this) {
				if (locked) {
					must_wait = true;
					waiters.add(w);
				} else {
					must_wait = false;
					locked = true;
				}
			}
			if (must_wait) {
				// Let the simulator know we're going to block this Thread
				ScheduledThread.setWillBlock(Thread.currentThread());
				try {
					w.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
// End Block 1	
		return 1;
	}

	/* (non-Javadoc)
	 * @see ILock#acquire_try()
	 */
	public int release() {

		return 0; // TBD
	}

	/* (non-Javadoc)
	 * @see ILock#acquire_try()
	 */
	public int acquire_try() {
		
		return 0; // TBD
	}

	/* (non-Javadoc)
	 * @see ILock#getName()
	 */
	public String getName() { return name; }

	/* (non-Javadoc)
	 * ADVISORY ONLY.
	 * DOES NOT NEED TO BE THREAD SAFE
	 * DO NOT SYNCHRONIZE 
	 */
	public int getWaitersCount() { return waiters.size(); }
	public int getTrySuccessCount() { return trySuccessCount; }
	public int getTryFailCount() { return tryFailCount; }
	public long getTotalLockHeldTime() { return lockHeldTime; }
	public int getUseCount() { return useCount; }

}
