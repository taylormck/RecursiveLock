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

public class Test_OneSimpleThread extends TestRecursiveLockBase {
	public void testOneThreadSimple() {
		ScheduledThread.log("Begin OneSimpleThread");
		createLock("Apple");
		Runnable r1 = new Runnable() {
			public void run() {
				RecursiveLock rl = locks.get("Apple");
				do_acquire("Apple", 42);
				do_acquire("Apple", fromNow(4));	// 46
				do_release("Apple", fromNow(5));	// 51
				do_release("Apple", fromNow(6));	// 57
				assertEquals("Total Lock Held Time for Apple",15L, rl.getTotalLockHeldTime());
				assertEquals("Total Use Count for Apple",1, rl.getUseCount());
				do_acquire_try("Apple", fromNow(10));	// 67
				do_acquire_try("Apple", fromNow(10));	// 77
				do_acquire("Apple", fromNow(10));		// 87
				do_release("Apple", fromNow(5));		// 92
				do_release("Apple", fromNow(5));		// 97
				do_release("Apple", fromNow(5));		// 102
				assertEquals("Total Lock Held Time for Apple",50L, rl.getTotalLockHeldTime());
				assertEquals("Total Use Count for Apple",2, rl.getUseCount());				
				do_acquire_try("Apple", fromNow(10));	// 112
				do_release("Apple", fromNow(5));		// 117
				
			}

		};
		Thread[] t_a = createThreads(new Runnable[] {r1}, new String[]{"Red"});
		startThreads(t_a);
		Thread.yield();
		joinThreads(t_a, 0); 
        RecursiveLock k0 = locks.get("Apple");
        //int total_lock_instances = k0.getInstanceCount();
        //ScheduledThread.log("Instance Count = " + total_lock_instances + " Finished testCounts!\n");
        //assertEquals("Total Lock Instance count", 11, total_lock_instances);
        long tlht = k0.getTotalLockHeldTime();
        int tfc = k0.getTryFailCount();
        int tsc = k0.getTrySuccessCount();
        int uc = k0.getUseCount();
        int wc = k0.getWaitersCount();
        assertEquals("Total Lock Held Time for Apple",55L, tlht);
        assertEquals("Total Try Fail Count for Apple", 0,tfc);
        assertEquals("Total Try Success Count  for Apple", 3,tsc);
        assertEquals("Total Use Count for Apple",3 ,uc);
        assertEquals("Total Waiters Count for Apple",0,wc);
        assertEquals("Test completed at", 117L, ScheduledThread.getTime());
		
        t_log_stat(k0,"**** Final Stats******");
		ScheduledThread.log("Finished OneSimpleThread!\n");
	}


}
