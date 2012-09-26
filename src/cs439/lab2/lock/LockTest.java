package cs439.lab2.lock;
/**
 * Title:        LockTest
 * Description:
 * Copyright:    Copyright (c) 2012
 * Company:      University of Texas at Austin
 * @author R.L. Rockhold, Ph.D.
 * @version 1.0
 */

public class LockTest {

	private ILock m_lock = new FIFOLock("FIFOLock0");
	
	LockTest() {
		final int tcount = 10;
		Thread[] t = new Thread[tcount];
		for (int i=0; i<tcount; ++i) {
			t[i] = new Thread(
					new Runnable() {
						public void run() {
							for (int x=0; x<20; ++x) {
								try {
									Thread.sleep(x*10);
								} catch (InterruptedException e2) {
									// TODO Auto-generated catch block
									e2.printStackTrace();
								}
								//System.out.println(Thread.currentThread().getName() + " eady to lock");
								m_lock.acquire();
								//System.out.println(Thread.currentThread().getName() + " locked");
								try {
									Thread.sleep(10);
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								//System.out.println(Thread.currentThread().getName() + " unlocking");
								m_lock.release();
							}
							
						}
					}, "Thread " + i);
			t[i].start();
		}
		System.out.println(tcount + " threads started.");
		for (int i=0; i<tcount; ++i) {
			try {
				t[i].join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	public static void main(String[] args) {
		new LockTest();
		System.out.println("All done!");
					
	}
}
