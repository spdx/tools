package org.spdx.rdfparser;

public class SpreadsheetException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5174679000954342942L;

	public SpreadsheetException(String message) {
		super(message);
	}
	
	public SpreadsheetException(String message, Throwable inner) {
		super(message, inner);
	}
}
