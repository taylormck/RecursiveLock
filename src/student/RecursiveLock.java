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
	//-------------------------------------------
	// These are used to directly implement the 
	// synchronization
	//-------------------------------------------
	private String name;
	private Semaphore sv;
	private FIFOLock fl;
	private Thread lockOwnerThread = null;
	private int depth = 0;
	
	//-------------------------------------------
	// These are used for statistics
	//-------------------------------------------
	private static int instanceCount = 0;
	private int trySuccessCount = 0;
	private int tryFailCount = 0;
	private long lockHeldTime = 0;
	private int useCount = 0;
	private int waitersCount = 0;
	private long startTimer = 0;

	public RecursiveLock(String _name) {
		name = _name;
		fl = new FIFOLock(name + "-fl");
		sv = new Semaphore(1);
		
		synchronized (this){
			instanceCount++;
		}
	}

	public static int getInstanceCount() {
		return instanceCount;
	}

	
	public int acquire() {
		try {
			sv.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		if (lockOwnerThread == null) {
			lockOwnerThread = Thread.currentThread();
			fl.acquire();
			startTimer = ScheduledThread.getTime();
			depth++;
		}
		else if (lockOwnerThread == Thread.currentThread()) {
			depth++;
		}
		else {
			waitersCount++;
			sv.release();
			fl.acquire();
			try {
				sv.acquire();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			waitersCount--;
			lockOwnerThread = Thread.currentThread();
			startTimer = ScheduledThread.getTime();
			depth++;
		}
		
		sv.release();

		return depth;
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
			if (depth == 0) {
				fl.release();
				lockOwnerThread = null;
				
				lockHeldTime = ScheduledThread.getTime() - startTimer;
				useCount++;
			}
		}
		else if (lockOwnerThread != null) {
			throw new LockProtocolViolation(this, Thread.currentThread(), lockOwnerThread);
		}
		
		sv.release();

		return depth;
	}

	/* (non-Javadoc)
	 * @see ILock#acquire_try()
	 */
	public int acquire_try() {
		try {
			sv.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		int result = 0;
		if (lockOwnerThread == null) {
			fl.acquire();
			lockOwnerThread = Thread.currentThread();
			depth++;
			trySuccessCount++;
			result = depth;
		}
		else if (lockOwnerThread == Thread.currentThread()) {
			depth++;
			trySuccessCount++;
			result = depth;
		}
		else {
			tryFailCount++;
		}
		sv.release();
		return result;
	}

	public long getTotalLockHeldTime() { 
		if (lockOwnerThread == null)
			return lockHeldTime;
		else {
			return lockHeldTime + (ScheduledThread.getTime() - startTimer);
		}
	}
	public String getName() { return name; }
	public int getWaitersCount() { return waitersCount; }
	public int getTrySuccessCount() { return trySuccessCount; }
	public int getTryFailCount() { return tryFailCount; }
	public int getUseCount() { return useCount; }

}
