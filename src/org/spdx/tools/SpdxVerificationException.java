/**
 *
 */
package org.spdx.tools;

/**
 * @author Gary O'Neall
 *
 */
public class SpdxVerificationException extends Exception {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;


	/**
	 * @param msg
	 */
	public SpdxVerificationException(String msg) {
		super(msg);
	}

	/**
	 * @param inner
	 */
	public SpdxVerificationException(Throwable inner) {
		super(inner);
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public SpdxVerificationException(String msg, Throwable inner) {
		super(msg, inner);
	}

}
