package cs439.lab2.simulator;

/**
 * Title:        
 * Description:
 * Copyright:    Copyright (c) 2012
 * Company:      University of Texas at Austin
 * @author R.L. Rockhold, Ph.D.
 * @version 1.0
 */

public class SchedStats {
	
	public static int cpu_bursts = 0;
	public static int cpu_ticks = 0;
	public static int dispatches = 0;
	public static int duration = 0;
	
	public static void finishedData(
			final PCB pcb, 
			final int started_time, 
			final int current_time, 
			final int executed_ticks, 
			final int execution_burst_count, 
			final int context_switches) {
		Debug.log("Process " + pcb + " completed at time " + current_time + 
    			", used " + executed_ticks + " ticks, and required " +
				context_switches + " context switches for its " + execution_burst_count + 
				" CPU bursts.");
		//Debug.log("Process " + pcb + " completed at time " + current_time);
    	SchedStats.duration += current_time -started_time;
    	SchedStats.cpu_bursts += execution_burst_count;
    	SchedStats.dispatches += context_switches;
    	SchedStats.cpu_ticks += executed_ticks;
	}
	
}
