/**
 * Ronald L. Rockhold
 * Copyright UT Austin, 2012
 * This work is unwarranted.
 */
package cs439.lab2.lock;

import student.RecursiveLock;

import java.util.HashMap;

//import RecursiveLock;

import junit.framework.TestCase;

/**
 * Title:        
 * Description:
 * Copyright:    Copyright (c) 2012
 * Company:      University of Texas at Austin
 * @author R.L. Rockhold, Ph.D.
 * @version 1.0
 */

public abstract class TestRecursiveLockBase extends TestCase {

	HashMap<String, RecursiveLock> locks;

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		locks = new HashMap<String, RecursiveLock>();
		ScheduledThread.reset();
	}
	

	
	void createLock(String name) {
		locks.put(name, new RecursiveLock(name));
	}
	
	static Thread[] createThreads(Runnable[] runnables, String[] names) {
		Thread[] t_a = new ScheduledThread[runnables.length];
		for (int i=0; i<runnables.length; ++i) {
			t_a[i] = new ScheduledThread(runnables[i], names[i]);
		}
		return t_a;
	}
	static void startThreads(Thread[] t_a) {
		for (int i=0; i<t_a.length; ++i) {
			t_a[i].start();
		}
	}

	static void joinThreads(Thread[] t_a, long d) {
		for (int i=0; i<t_a.length; ++i) {
			try {
				t_a[i].join(d);		
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}		

	int do_acquire(String string, long i) {
		/* Find our lock */
		RecursiveLock rwl = locks.get(string);
		int rc =  ((ScheduledThread) Thread.currentThread()).sync_acquire(rwl, i);
		t_log(rwl, "acquired", rc);
		return rc;
	}

	int do_acquire_try(String string, long i) {
		/* Find our lock */
		RecursiveLock rwl = locks.get(string);
		int rc = ((ScheduledThread) Thread.currentThread()).sync_acquire_try(rwl, i);
		t_log(rwl, "acquire_try", rc);
		return rc;
	}

	int do_acquire_try_til_works(String string, long i, int num_trys) {
		/* Find our lock */
		RecursiveLock rwl = locks.get(string);
		ScheduledThread t = (ScheduledThread) Thread.currentThread();
		int rc = 0;
		for (; num_trys > 0 && rc == 0; --num_trys) {
			rc = t.sync_acquire_try(rwl, i);
			Thread.yield();
		} 
		if (rc == 0) {
			t.sync_acquire(rwl, i);
		}
		t_log(rwl, "acquire_try_til_works", rc);
		return 1;
	}

	int do_release(String string, long t) {
		/* Find our lock */
		RecursiveLock rwl = (RecursiveLock) locks.get(string);
		int cnt = ((ScheduledThread) Thread.currentThread()).sync_release(rwl, t);
		// t_log(rwl, "released", rc);
		return cnt;
	}

	long fromNow(long delay) {
		return ScheduledThread.getTime() + delay;
	}

	void t_log(RecursiveLock rl, String msg, int rc) {
		// RLR_TODO -- This is non-deterministic. Accesses to r1.get*() not protected, so
		//   could get variance.  Consider not reporting anything other than the name and rc.
		ScheduledThread.log("Thread " + Thread.currentThread().getName() + " " + msg + " lock "
				+ rl.getName() + " with rc=" + rc + ".");
	}
	
	void t_log_stat(RecursiveLock rl, String msg) {
		// RLR_TODO -- This is non-deterministic. Accesses to r1.get*() not protected, so
		//   could get variance.  Consider not reporting anything other than the name and rc.
		ScheduledThread.log("Thread " + Thread.currentThread().getName() + " " + msg + " lock "
				+ rl.getName() + ": " + rl.getTrySuccessCount() 
				+ " successful trys, " + rl.getTryFailCount()
				+ " failed trys, and " + avgutil(rl) + " average hold time.");
	}
	
	double avgutil(RecursiveLock l) {
		long holdTime = l.getTotalLockHeldTime();
		int  t = l.getUseCount();
		if (t == 0) { return 0; }
		return ((double)holdTime) / t;
	}

	
}
