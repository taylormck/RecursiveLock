package cs439.lab2.lock;
import java.util.ArrayList;
import java.util.List;

/**
 * Title:        
 * Description:
 * Copyright:    Copyright (c) 2012
 * Company:      University of Texas at Austin
 * @author 		 R.L.Rockhold, Ph.D.
 * @version 1.0
 */

/*
 * A non-recursive lock.
 * 
 * This class uses FIFO ordering when unblocking lock requestors.
 * This class can be used with threads of type ScheduledThread, since
 * it calls ScheduledThread.setWillBlock(Thread t) 
 * and ScheduledThread.setWillUnBlock(Thread t).
 */

public class FIFOLock implements ILock {
	private static class Waiter {
		Thread m_t;
		Waiter() {
			m_t = Thread.currentThread();
		}
	}
	
	
	private String m_name;
	public FIFOLock(String name)
	{
		m_name=name;
	}	
	
	
	private boolean m_locked = false;
	
	private List<Waiter> m_waiters = new ArrayList<Waiter>();
	
	public int acquire() {
		Waiter w = new Waiter();
		boolean must_wait;
// Block 1 for questions 1a, 1b		
		synchronized (w) {
			synchronized (this) {
				if (m_locked) {
					must_wait = true;
					m_waiters.add(w);
				} else {
					must_wait = false;
					m_locked = true;
				}
			}
			if (must_wait) {
				// Let the simulator know we're going to block this Thread
				ScheduledThread.setWillBlock(Thread.currentThread());
				try {
					w.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
// End Block 1	
		return 1;
	}

	/* (non-Javadoc)
	 * @see ILock#unlock()
	 */
	public synchronized int release() {
		int wait_count = m_waiters.size();
		if (wait_count == 0) {
			// Nobody waiting
			m_locked = false;
		} else {
			// Find the oldest
			Waiter w = m_waiters.remove(0);
			// wake 'em up
			synchronized (w) {
				ScheduledThread.setWillUnBlock(w.m_t);
				w.notify();
			}
		}
		return 0;
	}

	public synchronized int acquire_try() {
		if (m_locked) return 0;
		m_locked = true;
		return 1;
	}

	public String getName() {
		return m_name;
	}


}
