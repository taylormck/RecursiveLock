package cs439.lab2.simulator;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * Title:        
 * Description:
 * Copyright:    Copyright (c) 2012
 * Company:      University of Texas at Austin
 * @author R.L. Rockhold, Ph.D.
 * @version 1.0
 */

public class Simulator {
	
	static class WaitingElement {
		private final Object waiter;
		private int wake_tick;
		private boolean waiting = false;
		WaitingElement(final Object waiter) {
			this.wake_tick = -1;
			this.waiter = waiter;
			
		}
		synchronized WaitingElement setTick(int tick) {
			this.wake_tick = tick;
			return this;
		}
		synchronized int getTick() {
			return this.wake_tick;
		}
		synchronized Object getWaiter() {
			return this.waiter;
		}
		boolean isWaiting() { return waiting; }
		void setWaiting(boolean t) { waiting = t; }
	}


	private static volatile int idle_count = 0;
	private static volatile int waiting_count = 0;
	private static volatile int process_count = 0;
	private static volatile int cpu_count;
	private static IScheduler scheduler;
	private static ICPU[] cpus;
	private static Thread[] cpu_threads;
	private static volatile int total_process_count = 0;
	private static volatile int current_time = 0;
	private static final String CONFIG_DIR = "configs/";
	//private static final List<WaitingElement> to_dos = Collections.synchronizedList(new ArrayList<WaitingElement>());
	private static final List<WaitingElement> to_dos = new ArrayList<WaitingElement>();
	private static final IdentityHashMap<Object,WaitingElement> obj_to_we = new IdentityHashMap<Object,WaitingElement>();

	
	public Integer getCurrentPID(final ICPU cpu) {
		return Simulator.scheduler.getCurrentPID(cpu);
	}


//	 Only CPU threads may use the methods in the next section	
	
	/**
	 * CPUs call this when they're looking for work.
	 * This method will ask the Scheduler for the next "ready"
	 * process, and "peek" at what the process will do next.
	 * This code handles the cases where the process has asked to:
	 *    exit
	 *    do I/O
	 * Only when a process is found that wants a CPU burst will this
	 * call return.
	 * If there are no processes ready, this code will call the "idle loop".
	 * @param cpu
	 * @return The next process that has a CPU burst
	 */
	public static PCB schedule(final ICPU cpu) {
		do {
			final Integer pid = Simulator.scheduler.schedule(cpu);
			if (pid == null) {
				// No ready processes.  Call the "idle loop".
				Simulator.idleLoop(cpu);
				continue;
			}
			final PCB pcb = ProcessManager.getPCB(pid);
			int burst = pcb.peekRemainingCPUBurst();
			if (burst == Integer.MAX_VALUE) {
				Simulator.processExited(pcb);
				continue;
			}
			if (burst < 0) {
				Simulator.blockProcess(cpu, pcb, -burst);
				// TODO  Could this ever happen?  Doesn't CPU honor the remaining burst first?
				throw new IllegalStateException("Simulator.schedule() given a blocking process!");
				//scheduler.processBlocked(pid, cpu, 0);
				//continue;
			}
			// It's a real CPU Burst!
			return pcb;						
		} while (true);
	}
	
	
	/**
	 * A CPU thread uses this when a process has been dispatched.
	 * The caller will be blocked for the number of ticks specified.
	 * Once that much "time" has passed, the method will return to the caller,
	 *   with the guarantee that exactly "ticks" time has passed.
	 * @param cpu  The calling CPU
	 * @param p The process being executed
	 * @param ticks The number of ticks the CPU will "execute".
	 */
	public static void executeProcess(final ICPU cpu, final PCB pcb, final int ticks) {
		if (ticks <= 0) {
			throw new IllegalArgumentException("ticks must be > 0: " + ticks);
		}
		int block_should_end;
		final WaitingElement we = obj_to_we.get(cpu);
		synchronized(we) {
			synchronized (Simulator.class) {
				block_should_end = Simulator.getTime() + ticks;
				we.setTick(block_should_end);
				//info("CPU " + cpu.getID() + " running " + pcb.getPID() + " [quantum=" + pcb.getQuantum() + "] at time " + current_time + " for " + ticks + " ticks.");
				Simulator.to_dos.add(we);
				++ waiting_count;
			}
			//  idle_loop could try to awaken us, but 
			//  we still hold the WaitingElement's monitor
			while (true) {
				try {
					we.setWaiting(true);
					we.wait();
					// RLR_UNDO  The whole synchronized block
					//Debug.log("CPU " + cpu.getID() + " (thread " + Thread.currentThread().getName() + ") awakened at time " + current_time + ". for we " + we);
					if (!we.isWaiting()) {
						if (block_should_end != Simulator.current_time || to_dos.contains(we)) {
							throw new IllegalStateException("Process didn't block correctly! " +
											" Time now: " + getTime() + ". we alarm at: " + we.getTick() +
											((to_dos.contains(we)) ? " STILL ON to_dos list!!!" : " not on todos_list"));							
						}
					break;  // good to go
					}
										
				} catch (final InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		pcb.incrCPUTime(ticks);
	}

	/**
	 * A CPU calls this when a process' CPU Burst has complete,
	 * either before the quantum expired, at exactly as the quantum expired.
	 * 
	 * @param cpu  The calling CPU
	 * @param p  The process whose burst was completed
	 * @param ticks_given
	 */
	public static void burstCompleted(final ICPU cpu, final PCB pcb, final int ticks_given) {
		final Integer pid = pcb.getPID();
		Debug.info("Burst completed for process " + pcb + " at time " + Simulator.getTime() + " after " + ticks_given + " ticks.");
		int burst = pcb.getRemainingCPUBurst();
		if (burst == Integer.MAX_VALUE) {
			Simulator.processExited(pcb);
		} else if (burst < 0) {
			Simulator.blockProcess(cpu, pcb, -burst);
			Simulator.scheduler.processBlocked(pid, cpu, ticks_given);
		} else {
			throw new IllegalStateException("CPU Burst expired, but next burst isn't I/O or Exit!");
		}
	}
	
	/**
	 * A CPU calls this when a quantum has expired the process blocked.
	 * 
	 * @param p
	 * @param cpu
	 * @param ticks_given
	 */
	public static void quantumExpired(final ICPU cpu, final PCB pcb, final int ticks_given) {
		Debug.info("Quantum expired for process " + pcb + " at time " + Simulator.getTime() + " after " + ticks_given + " ticks.");
		Simulator.scheduler.quantumExpired(pcb.getPID(), cpu, ticks_given);
	}
	
	public static synchronized void blockProcess(final ICPU cpu, final PCB pcb, final int ticks) {
		if (ticks <= 0) {
			throw new IllegalArgumentException("ticks must be > 0: " + ticks);
		}
		Debug.info("Blocking process " + pcb + " at time " + Simulator.getTime() + " for " + ticks + " ticks.");
		WaitingElement we = obj_to_we.get(pcb);
		we.setTick(Simulator.getTime() + ticks);
		Simulator.to_dos.add(we);
	}
	
	public static void directorLoop() {
		/*   lower our priority -- we can't be starved since
		 *   we don't do useful work until all other threads are blocked.
		 */
		Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
		boolean exit = false;
		do {
			Thread.yield();
			synchronized (Simulator.class) {
				if (waiting_count == Simulator.cpu_count) {
					if (Simulator.process_count == 0) {
						exit = true;
					} else {
						/* Time to make the donuts */
						// info("Everyone's blocked at time " + getTime());
						/* See what happens next .... */
						// There must be something in our to_do list
						final int cnt = Simulator.to_dos.size();
						if (cnt == 0) {
							// Fault!
							Debug.log("**** Error **** All CPUs are blocked in idle and no processes are waiting!");
							throw new IllegalStateException("All CPUs are blocked in idle and no processes are waiting!");
						}
						// Find the next tick where something happens
						final int next_tick = Simulator.getNextWaitingTick();	
						// Debug.info("Advancing time to " + next_tick);
						//  Wake up anyone with wake_time < next_tick
						Simulator.current_time = next_tick;
						for (final Iterator it=Simulator.to_dos.iterator(); it.hasNext();) {
							final WaitingElement we = (WaitingElement) it.next();
							synchronized(we) {
								final int wake_tick = we.getTick();
								// debug
								if (wake_tick < Simulator.current_time) {
									Debug.log("Error. Time warp. Now: " + Simulator.current_time + " but we on to_dos has time " + wake_tick + "for we " + we);
									throw new IllegalStateException("Error. Time warp. Now: " + Simulator.current_time + " but we on to_dos has time " + wake_tick + "for we " + we);
								}
								if (wake_tick == Simulator.current_time) {
									it.remove();
									we.setWaiting(false);
									// See what type the value is
									final Object val = we.getWaiter();
									if (val instanceof PCB) {
										// Start it up!
									    final PCB pcb = ((PCB) val);
									    final Integer pid = pcb.getPID();
										if (pcb.isStarted()) {
											// Now it's unblocked
											Debug.info("Unblocking process " + pcb + " at time " + Simulator.current_time + ".");
											Simulator.scheduler.processUnblocked(pid);
											Debug.user("Unblocked process " + pcb + " at time " + Simulator.current_time + ".");
										} else {
											ProcessManager.processStarted(pcb);
											Debug.log("Starting process " + pcb + " at time " + Simulator.current_time + ".");
											Simulator.scheduler.processStarted(pcb);
											
										}
									} else if (val instanceof ICPU) {
										synchronized(we) {
											// RLR_TODO:  Remove
											// Debug.log("DLoop: Waking ICPU " + ((ICPU) val).getID() + " with wake_tick: " + we.getTick() + " for we " + we);
											we.notify();
											-- waiting_count;
											//Simulator.info("DLoop: Decr to " + cnt2 + " for we type ICPU");
										}
									} else {
										throw new IllegalStateException("Unknown we object: " + val);
									}
								}
							} // end synchronized(we)
						}	// end for
						// Now we need to wake all the CPUs that were idle
						//SchedControl.waiting_count -= SchedControl.idle_count;
						if (idle_count != 0) {
							Simulator.class.notifyAll();
							/* int val = */ waiting_count -= idle_count;
							//Simulator.info("DLoop: Decr by " + SchedControl.getIdleCount() + " for we type ICPU. Result " + val);
						}
						//SchedControl.idle_count = 0;
						idle_count = 0;
					}
				}
			}  // End synchronized Simulator.class block
			
		} while (!exit);
		/* No more work! */
	}
	
	public static synchronized int getNextWaitingTick() {
		int low_tick = Integer.MAX_VALUE;
		for (WaitingElement we : Simulator.to_dos) {
			final int t =  we.getTick();
			if (t < low_tick) {
				low_tick = t;
			}
		}
		return low_tick;
	}
	
	public static synchronized int getTime() {
		return Simulator.current_time;
	}
	
	//  Note that idle CPU threads block on Scheduler.class monitor.
	//    No need for a queue since they will all be awakened at once
	public static synchronized void idleLoop(final ICPU cpu) {
		//++ SchedControl.waiting_count;
		//int cnt = 
		++ waiting_count;
		//Simulator.info("idleLoop: Incr to " + cnt);

		//++ SchedControl.idle_count;
		++ idle_count;
		Debug.user("CPU " + cpu.getID() + " going idle at time " + Simulator.getTime() + ".");
		try {
			Simulator.class.wait();
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}
		Debug.user("CPU " + cpu.getID() + " re-thinking going idle at time " + Simulator.getTime() + ".");
		return;
	}

	public static synchronized void processExited(final PCB pcb) {
		// Tell the scheduler
		final Integer pid = pcb.getPID();
		Simulator.scheduler.processExited(pid);
		pcb.completed(Simulator.getTime());
		--Simulator.process_count;
	}

    /**
     * @param cpu
     * @param p
     * @param ticks_given
     */
    public static void quantumInterrupted(final CPU cpu, final PCB pcb, final int ticks_given) {
    	Debug.info("Execution interrupted for process " + pcb + " at time " + Simulator.getTime() + " after " + ticks_given + " ticks.");
		Simulator.scheduler.quantumInterrupted(pcb.getPID(), cpu, ticks_given);
    }
	
	private static Class do_GetClass(final String cname) throws ClassNotFoundException {
		Class c;
		try {
			c = Class.forName(cname);
		} catch (final ClassNotFoundException e) {
			Debug.log("Could not locate class " + cname);
			throw e;
		}
		return c;
	}

	private static Object do_GetClassInstance(final String cname) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		final Class c = Simulator.do_GetClass(cname);
		final Object o = c.newInstance(); 
		return o;
	}
	
	private static void do_CPUs(final Properties props) throws ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
		cpu_count = Integer.parseInt(props.getProperty("CPUs", "1").trim());
		Debug.log("CPUs: " + cpu_count);
		Simulator.cpus = new ICPU[Simulator.cpu_count];
		Simulator.cpu_threads = new Thread[Simulator.cpu_count];
		final Class cpuclass = CPU.class;
		//log("CPU implementation: " + cpuclass);
		final Constructor cpu_constructor = cpuclass.getConstructor(new Class[] {int.class});
		for (int c=0; c< Simulator.cpu_count; ++c) {
			ICPU cpu = (ICPU) cpu_constructor.newInstance(new Object[] {new Integer(c)});
			Simulator.cpus[c] = cpu;
			obj_to_we.put(Simulator.cpus[c], new WaitingElement(cpu));
			Simulator.cpu_threads[c] = new Thread(Simulator.cpus[c], "CPU " + c);
			Simulator.cpu_threads[c].setDaemon(true);
			Simulator.cpu_threads[c].start();
		}
	}

	private static void do_Processes(final Properties props) throws ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException, IOException {
	    //Class pcbclass = PCB.class;
		//Constructor pcb_constructor = pcbclass.getConstructor(new Class[] {int.class, String.class, String.class});
		final String p_names = props.getProperty("Processes").trim();
		Debug.log("Processes: " + p_names);
		final StringTokenizer stk = new StringTokenizer(p_names);
		// int p_num = 0;
		while (stk.hasMoreTokens()) {
			final String p_name = stk.nextToken();
			/* Get the specific info about this process */
			final String p_trace = props.getProperty("Process." + p_name + ".trace").trim();
			final String s_start = props.getProperty("Process." + p_name + ".start", "0").trim();
			final Integer i_start = Integer.decode(s_start);
			
			final PCB pcb = 
				ProcessManager.newProcess(p_name, CONFIG_DIR  + p_trace, i_start);
			++ Simulator.process_count;
/*				
				(PCB) pcb_constructor.newInstance(
							new Object[] { new Integer(process_count++), p_name, p_trace} );
*/							
			//final WaitingElement we = new WaitingElement(i_start.intValue() , pcb);
			final WaitingElement we = new WaitingElement(pcb).setTick(i_start.intValue());
			obj_to_we.put(pcb, we);
			Simulator.to_dos.add(we);
			++Simulator.total_process_count;					
		}
	}
	
	private static void do_Scheduler(final String class_name) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		Simulator.scheduler = (IScheduler) Simulator.do_GetClassInstance(class_name);
		Debug.log("Using scheduler " + Simulator.scheduler.getClass());
	}
	
	private static void usage() {
		System.err.println("Usage:  java <package-name>/Simulator [-v[v]] config-properties-file<.properties>  { <cs372.lab1.student.>{RR|FCFS|MLFQ|SJF} | fully.qualified.scheduler.class.name }");
		System.err.println("For example:  java cs372/labx/simulator/Simulator simple_config RR");
	}
	public static void main(final String[] args) throws IOException, SecurityException, IllegalArgumentException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
		if (args.length < 1) {
			Simulator.usage();
			return;
		}
		int arg_next = 0;
		final String arg0 = args[0].trim();
		if (arg0.startsWith("-v")) {
			Debug.verbose = 1;
			if (arg0.startsWith("-vv")) {
				Debug.verbose = 2;
			}
			arg_next = 1;
			if (args.length != 3) {
				Simulator.usage();
				return;
			}
		} else {
			if (args.length != 2) {
				Simulator.usage();
				return;
			}
		}
		final String pfile_name = CONFIG_DIR + args[arg_next].trim() + ".properties";
		final Properties props = new Properties();
		Debug.log("Simulator using config file " + pfile_name);
		final FileInputStream in = new FileInputStream(pfile_name);
		props.load(in);
		/* Create the Scheduler */
		String sched_class_name = args[++arg_next].trim();
		if ( ! sched_class_name.contains(".")) {
			// Assume it's the same package base + ".student" package
			Package p = Simulator.class.getPackage(); String pname = p.getName(); int i = pname.lastIndexOf('.');
			String p_base = pname.substring(0, i);
			//sched_class_name = "cs372.lab1.student." + sched_class_name;
			sched_class_name = p_base + ".student." + sched_class_name;
		}
		Simulator.do_Scheduler(sched_class_name);
		/* Create each user process and add it to the incoming queue */
		Simulator.do_Processes(props);
		/* Create each CPU -- passing them their Scheduler instance */
		Simulator.do_CPUs(props);
		Simulator.directorLoop();
		final long end_time = Simulator.getTime();
		final long possible_ticks = Simulator.cpu_count * end_time;
		final double utilization = 100 * ((double)SchedStats.cpu_ticks) / possible_ticks;
		final double avg_completion_time = ((double)SchedStats.duration)/Simulator.total_process_count;
		final double dispatch_efficiency = 100 *((double)SchedStats.cpu_bursts) / SchedStats.dispatches;
		Debug.log("Finished at time " + Simulator.getTime());
		Debug.log("Input trace: " + pfile_name + ". Scheduler: " + sched_class_name);
		Debug.log("Average CPU Utilization (" 
			+ SchedStats.cpu_ticks + " used ticks" 
			+ "/" 
			+ Simulator.cpu_count + " cpus * " + Simulator.getTime() + " run-time " 
			+ ") == (" +
			SchedStats.cpu_ticks+ "/" + possible_ticks
					 + "): " + utilization + "%");
		Debug.log("Average Completion Time (" + SchedStats.duration + "/" +
				Simulator.total_process_count + " jobs): " + avg_completion_time);
		Debug.log("Dispatch Efficiency (" + SchedStats.cpu_bursts + " bursts /" +
				SchedStats.dispatches + " dispatches): " + dispatch_efficiency + "%");
		final double xtra_dispatches_per_cpu = ((double)(SchedStats.dispatches - SchedStats.cpu_bursts)) / Simulator.cpu_count;
		final double time_for_xtra_dispatches = xtra_dispatches_per_cpu/10; 
		final double score = avg_completion_time + time_for_xtra_dispatches;
		Debug.log("Score (smaller is better): " + score);
	}

}
