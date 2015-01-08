/**
 * Copyright (c) 2015 Source Auditor Inc.
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
package org.spdx.rdfparser;

import org.spdx.rdfparser.license.AnyLicenseInfo;
import org.spdx.rdfparser.license.LicenseInfoFactory;
import org.spdx.rdfparser.license.SpdxListedLicense;
import org.spdx.spdxspreadsheet.InvalidLicenseStringException;

@Deprecated
/**
 * This class is provided for backwards compatibility.
 * 
 * The class org.spdx.rdfparser.license.LicenseInfoFactory should be used.
 * 
 * @author Gary O'Neall
 *
 */
public class SPDXLicenseInfoFactory {
	/**
	 * This function has been replaced by LicenseInfoFactory.getLicenseListVersion()
	 */
	@Deprecated
	public static String getLicenseListVersion() {
		return LicenseInfoFactory.getLicenseListVersion();
	}
	
	/**
	 * This function has been replaced by LicenseInfoFactory.getListedLicenseById(licenseId)
	 */
	@Deprecated
	public static SpdxListedLicense getStandardLicenseById(String licenseId) throws InvalidSPDXAnalysisException {
		return LicenseInfoFactory.getListedLicenseById(licenseId);
	}
	
	/**
	 * This function has been replaced by LicenseInfoFactory.getSpdxListedLicenseIds()
	 */
	@Deprecated 
	public static String[] getStandardLicenseIds() {
		return LicenseInfoFactory.getSpdxListedLicenseIds();
	}
	
	/**
	 * This function has been replaced by LicenseInfoFactory.isSpdxListedLicenseID(licenseId)
	 */
	@Deprecated
	public static boolean isStandardLicenseId(String licenseId) {
		return LicenseInfoFactory.isSpdxListedLicenseID(licenseId);
	}
	
	/**
	 * This function has been replaced by LicenseInfoFactory.parseSPDXLicenseString(String licenseString)
	 */
	@Deprecated
	public static AnyLicenseInfo parseSPDXLicenseString(String licenseString) throws InvalidLicenseStringException {
		return LicenseInfoFactory.parseSPDXLicenseString(licenseString);
	}
	

}
