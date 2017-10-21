/**
 * Copyright (c) 2017 Source Auditor Inc.
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
 */
package org.spdx.tools.licensegenerator;

/**
 * Singleton class which returns information maintained by the Free Software Foundation
 * 
 * NOTE: This is currently using a non authoritative data source
 * 
 * TODO: Update the class to use an official FSF data source once available
 * @author Gary O'Neall
 *
 */
public class FsfLicenseDataParser {
	
	private static FsfLicenseDataParser fsfLicenseDataParser = null;
	
	private FsfLicenseDataParser() {
		//TODO: Initialize any cached data
	}
	
	public static synchronized FsfLicenseDataParser getFsfLicenseDataParser() {
		if (fsfLicenseDataParser == null) {
			fsfLicenseDataParser = new FsfLicenseDataParser();
		}
		return fsfLicenseDataParser;
	}

	/**
	 * Determines if an SPDX license is designated as FSF Free / Libre by FSF.  Reference https://www.gnu.org/licenses/license-list.en.html
	 * @param spdxLicenseId
	 * @return
	 */
	public boolean isSpdxLicenseFsfLibre(String spdxLicenseId) {
		// TODO Implement
		return false;
	}

}
