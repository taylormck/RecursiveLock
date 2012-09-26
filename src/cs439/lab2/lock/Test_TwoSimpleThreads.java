package cs439.lab2.lock;

import student.RecursiveLock;

/**
 * Title:        
 * Description:
 * Copyright:    Copyright (c) 2012
 * Company:      University of Texas at Austin
 * @author R.L. Rockhold, Ph.D.
 * @version 1.0
 */

public class Test_TwoSimpleThreads extends TestRecursiveLockBase {

	public void testTwoThreadsSimple() {
		ScheduledThread.log("Begin TwoSimpleThreads");
		createLock("Pear");
		//TimedThreads[] tt_a = createTimedThreads(new String[] {"Red", "Green"});
		Runnable r1 = new Runnable() {
			public void run() {
		        RecursiveLock rl = locks.get("Pear");
				int rc = do_acquire("Pear", 42);
				assertEquals("Tick ", 42L, ScheduledThread.getTime());
				assertEquals("Depth ", 1, rc);
				
				rc = do_acquire("Pear", fromNow(4));
				assertEquals("Tick ", 46L, ScheduledThread.getTime());
				assertEquals("Depth ", 2, rc);
				assertEquals("Waiter for Pear ", 1, rl.getWaitersCount());
				
				rc = do_release("Pear", fromNow(5));
				assertEquals("Tick ", 51L, ScheduledThread.getTime());
				assertEquals("Depth ", 1, rc);
				
				rc = do_release("Pear", fromNow(6));
				assertEquals("Depth ", 0, rc);
				assertEquals("Tick ", 57L, ScheduledThread.getTime());
			}

		};
		Runnable r2 = new Runnable() {
			public void run() {
		        RecursiveLock rl = locks.get("Pear");
				int rc = do_acquire_try("Pear", 43);
				assertEquals("Try should have failed for Pear ", 0, rc);
				assertEquals("Try fail count ", 1, rl.getTryFailCount());
				rc = do_acquire("Pear", fromNow(1));
				assertEquals("Tick ", 57L, ScheduledThread.getTime());
				assertEquals("Depth ", 1, rc);
				assertEquals("Lock held time ", 15, rl.getTotalLockHeldTime());
				rc = do_acquire_try("Pear", fromNow(0));
				assertEquals("Depth ", 2, rc);
				rc = do_release("Pear", fromNow(10));
				assertEquals("Depth ", 1, rc);
				rc = do_release("Pear", fromNow(0));
				assertEquals("Depth ", 0, rc);
			}
		};
		Thread[] t_a = createThreads(new Runnable[] {r1, r2}, new String[]{"Pink", "Green"});
		startThreads(t_a);
		Thread.yield();
		joinThreads(t_a, 0); // If debugging, set to 0 to keep the join from timing out
        RecursiveLock k0 = locks.get("Pear");
        //int total_lock_instances = k0.getInstanceCount();
        //ScheduledThread.log("Instance Count = " + total_lock_instances + " Finished testCounts!\n");
        //assertEquals("Total Lock Instance count", 11, total_lock_instances);
        long tlht = k0.getTotalLockHeldTime();
        int tfc = k0.getTryFailCount();
        int tsc = k0.getTrySuccessCount();
        int uc = k0.getUseCount();
        int wc = k0.getWaitersCount();
        assertEquals("Total Lock Held Time for " + k0.getName() + " ",25L, tlht);
        assertEquals("Total Try Fail Count for " + k0.getName() + " ", 1,tfc);
        assertEquals("Total Try Success Count for " + k0.getName() + " ", 1,tsc);
        assertEquals("Total Use Count for " + k0.getName() + " ",2 ,uc);
        assertEquals("Total Waiters Count for " + k0.getName() + " ",0,wc);
        assertEquals("Test completed at", 67L, ScheduledThread.getTime());
		
        t_log_stat(k0,"**** Final Stats******");
		ScheduledThread.log("Finished TwoSimpleThreads!\n");
	}

}
