package cs439.lab2.simulator;

import java.io.PrintStream;

/**
 * Title:        
 * Description:
 * Copyright:    Copyright (c) 2012
 * Company:      University of Texas at Austin
 * @author R.L. Rockhold, Ph.D.
 * @version 1.0
 */

public class Debug {
	
	static int verbose = 0;
	static PrintStream output = System.err;

	static void info(final String msg) {
		if (Debug.verbose > 0) {
			synchronized(output) {
				output.println("Info (" + Thread.currentThread().getName() + "): " + msg);
				output.flush();
			}
		}
	}
	
	public static void user(final String msg) {
		if (Debug.verbose > 1) {
			synchronized(output) {
				output.println("User (" + Thread.currentThread().getName() + "): " + msg);
				output.flush();
			}
		}
	}
	
	static void log(final String msg) {
		synchronized(output) {
			output.println("Log (" + Thread.currentThread().getName() + "): " + msg);
			output.flush();
		}
	}
		

}
