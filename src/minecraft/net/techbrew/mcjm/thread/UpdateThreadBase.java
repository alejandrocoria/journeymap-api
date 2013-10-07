package net.techbrew.mcjm.thread;


public abstract class UpdateThreadBase implements Runnable {

	public UpdateThreadBase() {
	}
	
	
	
	/**
	 * Do the real work.
	 */
	protected abstract void doTask();
	

}
