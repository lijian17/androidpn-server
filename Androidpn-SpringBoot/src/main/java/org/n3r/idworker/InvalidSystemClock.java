package org.n3r.idworker;

public class InvalidSystemClock extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public InvalidSystemClock(String message) {
		super(message);
	}
}
