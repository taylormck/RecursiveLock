package cs439.lab2.simulator;

/**
 * Title:        
 * Description:
 * Copyright:    Copyright (c) 2012
 * Company:      University of Texas at Austin
 * @author R.L. Rockhold, Ph.D.
 * @version 1.0
 */

public class CPU implements ICPU {

	private int id;
    private boolean must_do_schedule = false;

	public int getID() {
		return this.id;
	}
	
	public CPU(final int id) {
		this.id = id;
	}

	public void run() {
		do {
		    final PCB pcb = Simulator.schedule(this);
		    pcb.incrDispatches();
			
			final int burst = pcb.getRemainingCPUBurst();
			final int quantum = pcb.getQuantum();
			int burst_given = 0;
			Debug.info("Dispatching process " + pcb + " at time " + Simulator.getTime() + 
			        " with quantum " + quantum + " and remaining burst " + burst + ".");
			do {
			    Simulator.executeProcess(this, pcb, 1);
			    ++ burst_given;
			    if (burst_given == burst) {
			        this.must_do_schedule = false;
			        Simulator.burstCompleted(this, pcb, burst_given);
			        break;
			    }
			    else if (burst_given == quantum){
			        this.must_do_schedule = false;
			        Simulator.quantumExpired(this, pcb, burst_given);
			        break;
			    }
			    else if (this.must_do_schedule) {
			        this.must_do_schedule = false;
			        Simulator.quantumInterrupted(this, pcb, burst_given);
			        break;
			    }
			} while (true);
		} while (true);
	}

    public void setPreemptRequest() {
        this.must_do_schedule = true;     
    }
    
}
