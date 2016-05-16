/**
 * Copyright (c) 2016 Source Auditor Inc.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
*/
package org.spdx.licensexml;

/**
 * Exceptions parsing or processing License XML files
 * @author Gary O'Neall
 *
 */
public class LicenseXmlException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public LicenseXmlException(String msg) {
		super(msg);
	}
	
	public LicenseXmlException(String msg, Throwable inner) {
		super(msg, inner);
	}

}
