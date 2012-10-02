package student;

/**
 * Title:        
 * Description:
 * Copyright:    Copyright (c) 2012
 * Company:      University of Texas at Austin
 * @author 
 * @version 1.0
 */

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;

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
	private Semaphore sv;
	private Semaphore must_wait;
	private Thread lockOwnerThread;
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
		waiters = new LinkedList<Waiter>();
		sv = new Semaphore(1);
		must_wait = new Semaphore(1);
		depth = 0;
		locked = false;
	}

	public static int getInstanceCount() {
		return instanceCount;
	}

	// To be modified
	public int acquire() {
		Waiter w = new Waiter();
		try {
			sv.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		if (lockOwnerThread == null) {
			fl.acquire();
			lockOwnerThread = Thread.currentThread();
			depth++;
		}
		else if (lockOwnerThread == Thread.currentThread()) {
			depth++;
		}
		else {
			sv.release();
			try {
				must_wait.acquire();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			try {
				waiters.add(w);
				sv.acquire();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			must_wait.release();
			
			// We own the lock now
			fl.acquire();
			lockOwnerThread = Thread.currentThread();
			depth++;
		}
		
		sv.release();

		return 1;
	}

	/* (non-Javadoc)
	 * @see ILock#acquire_try()
	 */
	public int release() {
		try {
			sv.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (lockOwnerThread == Thread.currentThread()) {
			depth--;
			if (depth == 0)
				fl.release();
		}
		// No op otherwise
		
		sv.release();

		return 1; // TBD
	}

	/* (non-Javadoc)
	 * @see ILock#acquire_try()
	 */
	public int acquire_try() {

		return 0; // TBD
	}

	public String getName() { return name; }
	public int getWaitersCount() { return waiters.size(); }
	public int getTrySuccessCount() { return trySuccessCount; }
	public int getTryFailCount() { return tryFailCount; }
	public long getTotalLockHeldTime() { return lockHeldTime; }
	public int getUseCount() { return useCount; }

}
