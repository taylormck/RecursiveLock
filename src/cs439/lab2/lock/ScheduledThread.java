/**
 * Title:        
 * Description:
 * Copyright:    Copyright (c) 2012
 * Company:      University of Texas at Austin
 * @author 		 R.L.Rockhold, Ph.D.
 * @version 1.0
 */

/*
 * ScheduledTread.
 * Provides for deterministic scheduling of ReaderWriterLock methods.
 * To use this facilty, create all scheduled threads. 
 * Only after all threads have been created should the threads be started.
 * Then, instead of calling X.acquire() on ILock X directly, call:
 *     ((TimedThread) Thread.currentThread()).syn_acquire(X, long t), 
 * 			where t is a "simulated" time value.
 * ScheduledTreads guarantees that calls to Lock methods for time t will 
 * not be executed if there are pending calls at time <t.
 * 
 * NOTE :  This implementation only works with code that informs ScheduledThread
 *  before and after *every* blocking operation.  Blockers must call 
 *  ScheduledThread.setWillBlock(Thread thread_that_will_block), and 
 *  ScheduledThread.setWillUnBlock(Thread thread_that_will_unblock).
 */

package cs439.lab2.lock;
import java.util.TreeSet;
import java.util.concurrent.Semaphore;

public class ScheduledThread extends Thread {
	
	public class CountedLong implements Comparable<CountedLong> {
		private final long m_val;
		private int  m_wait_count = 0;
		CountedLong(long val) {
			m_val = val;
		}
		public int compareTo(CountedLong rhs) {
			long rhs_val = rhs.m_val;
			if (m_val < rhs_val) return -1;
			if (m_val == rhs_val) return 0;
			return 1;
		}
		public long longValue() {
			return m_val;
		}
		public void do_wait() {
			++m_wait_count;
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		public int getWaitCount() {
			return m_wait_count;
		}
	}
	
	private static volatile long s_current_time;
	private static TreeSet<CountedLong> s_wait_set;
	private static int s_free_running_count;
	//private static SimpleLock s_class_lock;
	private static Semaphore s_class_lock;
	
	
	private boolean	m_free_running = false;
	private Runnable m_runnable;
 	
 	static {
 		reset();
 	}
 	public static void reset() {
 		s_current_time = 0;
 		s_wait_set = new TreeSet<CountedLong>();
 		s_free_running_count = 0;
 		s_class_lock = new Semaphore(1, true);
 	}
 	public synchronized static void log(String s) {
 		synchronized(System.err) {
 			System.err.println("Time(" + getTime() + "): " + s);
 		}
 	}
 	public ScheduledThread(Runnable r, String name) {
		super(name);
		m_runnable = r;
		incrFreeRunning(1);
		setFreeRunning();
	}
	
	public static void setWillBlock(Thread t) {
		s_class_lock.acquireUninterruptibly();
		if (t instanceof ScheduledThread) {
			ScheduledThread st = (ScheduledThread)t;
			st.willBlock(false);
		}
		s_class_lock.release();
	}
	
	private void willBlock(boolean isThreadExit) {
		setNotFreeRunning();
		if (s_free_running_count == 0) {
			/* everyone would be waiting -- except us */
			/* get the earliest wakeup time */
			if (s_wait_set.size() > 0) {
				CountedLong wake_time = s_wait_set.first();
				/* wake them all up */
				synchronized(wake_time) {
					s_wait_set.remove(wake_time);	// remove the CountedLong
					s_current_time = wake_time.longValue();	// advance time
					incrFreeRunning(wake_time.getWaitCount());
					wake_time.notifyAll();	// wake them all
				}
			} else {
				if (! isThreadExit) {
					// Odd.  No one's running, and we're not holding
					//   anyone back!
					log("Must be a deadlock!");
				}
			}
		}
	}

		
	
	public static void setWillUnBlock(Thread t) {
		s_class_lock.acquireUninterruptibly();
		if (t instanceof ScheduledThread) {
			ScheduledThread st = (ScheduledThread)t;
			st.incrFreeRunning(1);
			st.setFreeRunning();
		}
		s_class_lock.release();
	}
	
	public void run() {
		/* pass control to our caller's run */
		try {
			m_runnable.run();
		} finally {
			/* remove ourselves from the active timed threads */
			if (m_free_running) {
				// The code in setWillBlock will ensure
				//  that any blocked threads that should run, will.
				willBlock(true);
				//setNotFreeRunning();
			} else {
				/* strange that the thread would die/exit 
				 * while waiting on a lock. 
				 */
				log("Thread " + currentThread().getName() +
				 	" exited while waiting to try to get a lock.\n");
			}
		}
	}
	
	void setFreeRunning() {
		m_free_running = true;
		//++ s_free_running_count;
	}
	
	void setNotFreeRunning() {
		m_free_running = false;
		-- s_free_running_count;
	}

	int sync_acquire(ILock lock, long time) {
		runAtTime(time);
		log("Thread " + currentThread().getName() + " attempting acquire of lock " + lock.getName());;
		return lock.acquire();
	}

	int sync_release(ILock lock, long time) {
		runAtTime(time);
		log("Thread " + currentThread().getName() + " releasing lock " + lock.getName());;
		int cnt = lock.release();
		return cnt;
		
	}
	
	int sync_acquire_try(ILock lock, long time) {
		runAtTime(time);
		log("Thread " + currentThread().getName() + " attempting acquire_try of lock " + lock.getName());;
		return lock.acquire_try();
	}
	
	public static synchronized long getTime() {
		return s_current_time;
	}
	
	private void runAtTime(long time) {
		s_class_lock.acquireUninterruptibly();
		/*  See if this time has already passed */
		if (time < s_current_time) {
			throw new IllegalArgumentException("Can't travel back " +
			"in time to " + time + ", from " + s_current_time);
		}
		CountedLong wake_time = null;
		setNotFreeRunning();
		if (s_free_running_count == 0) {
			/* everyone would be waiting -- except us */
			/* get the earliest wakeup time */
			if (s_wait_set.size() > 0) {
				wake_time = s_wait_set.first();
			} else {
				// Some logic to make it through the
				//  following code.  Originally hadn't
				//  allowed for only one thread to be alive.
				wake_time = new CountedLong(time + 42);
			}
		
			if (time >= wake_time.longValue()) {
				/* wake them all up */
				synchronized(wake_time) {
					s_wait_set.remove(wake_time);	// remove the CountedLong
					s_current_time = wake_time.longValue();	// advance time
					incrFreeRunning(wake_time.getWaitCount());
					wake_time.notifyAll();	// wake them all
				}
			}
			if (time > wake_time.longValue()) {
				wake_time = getBlockingObject(time);
			} else {		// time <= wake_time.longValue()
				s_current_time = time;	
				wake_time = null;	// Don't need to block
			}
			/* at this point:
			 * s_current_time has been set.
			 * anyone waiting at s_current time has been awakened
			 * If calling thread needs to block (time > s_current_time),
			 *     then wake_time -> CountedLong(wait) that
			 */
		} else {
			/* Until everyone's blocked, we don't know who runs next */
			wake_time = getBlockingObject(time);
		}
		if (wake_time != null) {	// we need to block
			synchronized(wake_time) {
				/* Only now can we unlock */
				do {
					s_class_lock.release();
					wake_time.do_wait();
					s_class_lock.acquireUninterruptibly();
				} while (wake_time.longValue() != s_current_time);
			}
		} else {
			/* the calling thread didn't need to be blocked */
			incrFreeRunning(1);
		}
		setFreeRunning();
		s_class_lock.release();
	}

	private void incrFreeRunning(int i) {
		s_free_running_count += i;
	}
	
	/* Caller must hold the class_lock */
	private CountedLong getBlockingObject(long time) {
		CountedLong val = null;
		/* See if our time is already in the Set */
		for (CountedLong cl: s_wait_set) {
			if (time == cl.longValue()) {
				val = cl;
				break;
			}
		}
		if (val==null) {
			/* create a new CountedLong */
			val = new CountedLong(time);
			/*  Add it */
			s_wait_set.add(val);
		}
		/* what we need to block on */
		return val;
	}
	
	

}
