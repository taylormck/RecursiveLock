package cs439.lab2.simulator;

import java.io.IOException;
import java.util.HashMap;

/**
 * Title:        
 * Description:
 * Copyright:    Copyright (c) 2012
 * Company:      University of Texas at Austin
 * @author R.L. Rockhold, Ph.D.
 * @version 1.0
 */

public class ProcessManager {
	   private static final HashMap<Integer, PCB> all_pcb = new HashMap<Integer, PCB>();
	   static private int process_count = 0;
	   static PCB newProcess(final String name, final String ptrace, final Integer i_start) throws IOException {
		   return new PCB(ProcessManager.process_count++, name, ptrace, i_start);
	   }
	   
	   static void removeProcess(final PCB pcb) {
		   ProcessManager.all_pcb.remove(pcb.getPID());
	   }

	public static void processStarted(final PCB pcb) {
		ProcessManager.all_pcb.put(pcb.getPID(), pcb);
		pcb.started(Simulator.getTime());
	}

	public static PCB getPCB(final Integer pid) {
		if (pid == null) {
			return null;
		}
		return ProcessManager.all_pcb.get(pid);
	}
	   
}
