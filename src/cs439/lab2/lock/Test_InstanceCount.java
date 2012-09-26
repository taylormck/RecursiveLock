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

public class Test_InstanceCount extends TestRecursiveLockBase {

	public void testInstanceCount() {
        ScheduledThread.log("Begin InstanceCount");
        int initial_lock_instances = RecursiveLock.getInstanceCount();
        Runnable[] t = new Runnable[10];

        int tnum = -1;
        t[++tnum] = new Runnable() {
        	public void run() {
        		try { Thread.sleep((int)(Math.random()*100)); 
        		} catch(InterruptedException e) {}
                createLock("Kimchi0");
            }
        };
        t[1] = new Runnable() {
            public void run() {
                try { Thread.sleep((int)(Math.random()*100)); 
                } catch(InterruptedException e) {}
                createLock("Kimchi1");
            }
        };
        t[2] = new Runnable() {
            public void run() {
                try { Thread.sleep((int)(Math.random()*100)); 
                } catch (InterruptedException e) {}
                createLock("Kimchi2");
            }
        };
        t[3] = new Runnable() {
                public void run() {
                        try { Thread.sleep((int)(Math.random()*100)); }
                        catch (InterruptedException e) {}
                        createLock("Kimchi3");
                }
        };
        t[4] = new Runnable() {
                public void run() {
                        try { Thread.sleep((int)(Math.random()*100)); }
                        catch (InterruptedException e) {}
                        createLock("Kimchi4");
                }
        };
        t[5] = new Runnable() {
                public void run() {
                        try { Thread.sleep((int)(Math.random()*100)); }
                        catch (InterruptedException e) {}
                        createLock("Kimchi5");
                }
        };
        t[6] = new Runnable() {
                public void run() {
                        try { Thread.sleep((int)(Math.random()*100)); }
                        catch (InterruptedException e) {}
                        createLock("Kimchi6");
                }
        };
        t[7] = new Runnable() {
                public void run() {
                        try { Thread.sleep((int)(Math.random()*100)); }
                        catch(InterruptedException e) {}
                        createLock("Kimchi7");
                }
        };
        t[8] = new Runnable() {
                public void run() {
                        try { Thread.sleep((int)(Math.random()*100)); }
                        catch (InterruptedException e) {}
                        createLock("Kimchi8");
                }
        };
        t[9] = new Runnable() {
                public void run() {
                        try { Thread.sleep((int)(Math.random()*100)); }
                        catch (InterruptedException e) {}
                        createLock("Kimchi9");
                }
        };

        Thread[] t_a = createThreads(new Runnable[]
              {t[0] , t[1], t[2], t[3], t[4], t[5], t[6], t[7], t[8], t[9]},
              new String[]{"T1", "T2", "T3", "T4", "T5", "T6", "T7", "T8", "T9", "T10"});
        startThreads(t_a);
        Thread.yield();
        joinThreads(t_a, 0); // If debugging, set to 0 to keep the join from timing out
        //t_log((RecursiveLock) locks.get("Kimchi0"),"**** Final Stats******", 42);
        RecursiveLock k0 = locks.get("Kimchi0");
        //int total_lock_instances = k0.getInstanceCount();
        //ScheduledThread.log("Instance Count = " + total_lock_instances + " Finished testCounts!\n");
        //assertEquals("Total Lock Instance count should be 10", 10, total_lock_instances);
        long tlht = k0.getTotalLockHeldTime();
        int tfc = k0.getTryFailCount();
        int tsc = k0.getTrySuccessCount();
        int uc = k0.getUseCount();
        int wc = k0.getWaitersCount();
        assertEquals("Total instances ", 10, RecursiveLock.getInstanceCount() - initial_lock_instances);
        assertEquals("Total Lock Held Time for Kimchi0",0L, tlht);
        assertEquals("Total Try Fail Count for Kimchi0", 0,tfc);
        assertEquals("Total Try Success Count  for Kimchi0", 0,tsc);
        assertEquals("Total Use Count for Kimchi0",0 ,uc);
        assertEquals("Total Waiters Count for Kimchi0",0,wc);
        assertEquals("Test completed at", 0L, ScheduledThread.getTime());
        t_log_stat(k0,"**** Final Stats******");
		ScheduledThread.log("Finished TestCounts!\n");
        
	}

}
