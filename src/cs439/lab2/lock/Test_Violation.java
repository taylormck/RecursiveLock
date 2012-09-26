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

public class Test_Violation extends TestRecursiveLockBase {
	public void testViolation() {
		ScheduledThread.log("Begin Violation");
		createLock("Avacado");
		//TimedThreads[] tt_a = createTimedThreads(new String[] {"Red", "Green"});
		Runnable r1 = new Runnable() {
			public void run() {
				do_acquire("Avacado", 42);
				do_release("Avacado", fromNow(600));
			}

		};
		Runnable r2 = new Runnable() {
			public void run() {
				do_acquire_try("Avacado", 43);
				try {
					do_release("Avacado", fromNow(1));
					fail("Release should have thrown a violation!");
				} catch (LockProtocolViolation lpv) {
					String expected = "Protocol violation: " + lpv.getMessage();
					ScheduledThread.log(expected);
					assertEquals("Protocol Violation ", "Protocol violation: Thread Black attempted to relase lock Avacado which is owned by Mauve", expected);
				}
			}
		};
		Thread[] t_a = createThreads(new Runnable[] {r1, r2}, new String[]{"Mauve", "Black"});
		startThreads(t_a);
		Thread.yield();
		joinThreads(t_a, 0); // If debugging, set to 0 to keep the join from timing out
        RecursiveLock k0 = locks.get("Avacado");
        long tlht = k0.getTotalLockHeldTime();
        int tfc = k0.getTryFailCount();
        int tsc = k0.getTrySuccessCount();
        int uc = k0.getUseCount();
        int wc = k0.getWaitersCount();
        assertEquals("Total Lock Held Time for " + k0.getName() + " ",600L, tlht);
        assertEquals("Total Try Fail Count for " + k0.getName() + " ", 1,tfc);
        assertEquals("Total Try Success Count for " + k0.getName() + " ", 0,tsc);
        assertEquals("Total Use Count for " + k0.getName() + " ",1 ,uc);
        assertEquals("Total Waiters Count for " + k0.getName() + " ",0,wc);
        assertEquals("Test completed at", 642L, ScheduledThread.getTime());
		
		
        t_log_stat(k0,"**** Final Stats******");
		ScheduledThread.log("Finished Violation!\n");
	}


}
