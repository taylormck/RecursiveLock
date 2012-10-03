package student;
/**
 * Title:        Recursive Lock
 * Description:  A simple recursive lock implemented with a FIFOLock class
 * Copyright:    Copyright (c) 2012
 * Company:      University of Texas at Austin
 * @author 		 Taylor McKinney
 * @version 1.0
 */
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
	private FIFOLock synch;
	private FIFOLock lock;
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

	// The constructor requires synchronized
	public RecursiveLock(String _name) {
		name = _name;
		lock = new FIFOLock(name + "-fl");
		synch = new FIFOLock(name + "-synch");

		synchronized (this){
			instanceCount++;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see cs439.lab2.lock.ILock#acquire()
	 */
	public int acquire() {
		synch.acquire();  // Enter
		
		// If this thread already owns the lock
		if (lockOwnerThread == Thread.currentThread()) {
			depth++;
		}
		
		// Otherwise, this thread doesn't own the lock
		// Functions the same when no threads own the lock as when
		// another thread owns the lock
		else {
			waitersCount++;
			synch.release();  // Release control to before waiting
			lock.acquire();

			// Now we have the lock
			synch.acquire();
			waitersCount--;
			lockOwnerThread = Thread.currentThread();
			startTimer = ScheduledThread.getTime();
			depth++;
		} 

		synch.release();  // Exit
		return depth;
	}

	/*
	 * (non-Javadoc)
	 * @see cs439.lab2.lock.ILock#release()
	 */
	public int release() {
		synch.acquire(); // Enter
		
		// If we own the lock
		if (lockOwnerThread == Thread.currentThread()) {
			depth--;
			
			// If we enter this block, it's time to release the lock
			if (depth == 0) {
				long t = ScheduledThread.getTime();
				lockHeldTime += t - startTimer;
				useCount++;
				lock.release();
				lockOwnerThread = null;
			}
		}
		
		// Otherwise, we don't own the lock and we need to throw
		// an LockProtocolViolation exception
		else {
			synch.release();
			throw new LockProtocolViolation(this, Thread.currentThread(), lockOwnerThread);
		}

		synch.release(); // Exit
		return depth;
	}

	/*
	 * (non-Javadoc)
	 * @see cs439.lab2.lock.ILock#acquire_try()
	 */
	public int acquire_try() {
		synch.acquire(); // Enter
		int result = 0;
		
		// Lock is available
		if (lockOwnerThread == null) {
			lock.acquire();
			lockOwnerThread = Thread.currentThread();
			depth++;
			trySuccessCount++;
			startTimer = ScheduledThread.getTime();
			result = depth;
		}
		
		// We already own the lock
		else if (lockOwnerThread == Thread.currentThread()) {
			depth++;
			trySuccessCount++;
			result = depth;
		}
		
		// Lock is already held by another thread
		else {
			tryFailCount++;
		}
		synch.release();  // Exit
		return result;
	}

	//--------------------------------------------------------
	// These do not need to be synchronized
	//--------------------------------------------------------
	public long getTotalLockHeldTime() { return lockHeldTime; }
	public String getName() { return name; }
	public int getWaitersCount() { return waitersCount; }
	public int getTrySuccessCount() { return trySuccessCount; }
	public int getTryFailCount() { return tryFailCount; }
	public int getUseCount() { return useCount; }
	public static int getInstanceCount() { return instanceCount; }
}
