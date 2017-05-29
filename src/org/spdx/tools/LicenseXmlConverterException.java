/**
 * 
 */
package org.spdx.tools;

/**
 * Exceptions generated from the license XML conversion utility
 * @author Gary O'Neall
 *
 */
public class LicenseXmlConverterException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public LicenseXmlConverterException(String msg) {
		super(msg);
	}
	
	public LicenseXmlConverterException(String msg, Throwable inner) {
		super(msg, inner);
	}

}
