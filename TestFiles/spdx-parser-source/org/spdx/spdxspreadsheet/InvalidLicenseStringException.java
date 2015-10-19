/**
 * Copyright (c) 2011 Source Auditor Inc.
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
package org.spdx.spdxspreadsheet;

/**
 * Exceptions related to License Strings stored in the spreadsheet.  Typically
 * these are parsine exceptions.
 * @author gary O'Neall
 *
 */
public class InvalidLicenseStringException extends SpreadsheetException {
	/**
	 *
	 */
	private static final long serialVersionUID = -1688466911486933160L;
	public InvalidLicenseStringException(String message) {
		super(message);
	}
	public InvalidLicenseStringException(String message, Throwable inner) {
		super(message, inner);
	}
}
