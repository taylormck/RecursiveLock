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

public class Test_Chaos extends TestRecursiveLockBase {
	public void testChaos() {
		ScheduledThread.log("Begin Chaos");
		createLock("Kiwi");
		Runnable[] t = new Runnable[10];
		for (int k=0; k<t.length; ++k) {
			t[k] = new Runnable() {
				public void run() {
					for (int i=0; i<100; ++i) {
						do_acquire_try_til_works("Kiwi", 1+10000*i, 10);
						
						for (int j=0; j<10; ++j) {
							Thread.yield();
							do_acquire_try("Kiwi", fromNow(0));
						}
						
						Thread.yield();
						do_release("Kiwi", fromNow(i+1));
						for (int j=0; j<10; ++j) {
							Thread.yield();
							do_release("Kiwi", fromNow(0));
						}
					}
				}
	
			};
		}
		Thread[] t_a = createThreads(new Runnable[] 
		      {t[0] , t[1], t[2], t[3], t[4], t[5], t[6], t[7], t[8], t[9]}, 
		      new String[]{"T1", "T2", "T3", "T4", "T5", "T6", "T7", "T8", "T9", "T10"});
		startThreads(t_a);
		Thread.yield();
		joinThreads(t_a, 0); // If debugging, set to 0 to keep the join from timing out
        RecursiveLock k0 = locks.get("Kiwi");
        long tlht = k0.getTotalLockHeldTime();
        int tfc = k0.getTryFailCount();
        int tsc = k0.getTrySuccessCount();
        int uc = k0.getUseCount();
        int wc = k0.getWaitersCount();
        assertEquals("Total Lock Held Time for " + k0.getName() + " ",50500L, tlht);
        assertEquals("Total Try Fail Count for " + k0.getName() + " ", 9000,tfc);
        assertEquals("Total Try Success Count for " + k0.getName() + " ", 10100,tsc);
        assertEquals("Total Use Count for " + k0.getName() + " ",1000 ,uc);
        assertEquals("Total Waiters Count for " + k0.getName() + " ",0,wc);
        assertEquals("Test completed at", 991001L, ScheduledThread.getTime());
		
        t_log_stat(locks.get("Kiwi"),"**** Final Stats******");
		ScheduledThread.log("Finished Chaos!\n");
	}


}
