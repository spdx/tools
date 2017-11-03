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

import java.io.IOException;

import org.spdx.html.InvalidLicenseTemplateException;
import org.spdx.rdfparser.license.LicenseException;
import org.spdx.rdfparser.license.SpdxListedLicense;
import org.spdx.tools.LicenseGeneratorException;

/**
 * Writes licenses in a specific format
 * @author Gary O'Neall
 *
 */
public interface ILicenseFormatWriter {

	/**
	 * Formats and writes a specific license
	 * @param license License to be added
	 * @param deprecated True if deprecated
	 * @param deprecatedVersion License list version when the license was deprecated, null otherwise
	 * @throws IOException 
	 * @throws LicenseGeneratorException 
	 */
	void writeLicense(SpdxListedLicense license, boolean deprecated, String deprecatedVersion) throws IOException, LicenseGeneratorException;

	/**
	 * Write the Table of Contents file for the format if applicable
	 * @throws IOException 
	 * @throws LicenseGeneratorException 
	 */
	void writeToC() throws IOException, LicenseGeneratorException;

	/**
	 * @param exception Exception to be formatted and written
	 * @param deprecated True if deprecated
	 * @param deprecatedVersion License list version when the license was deprecated, null otherwise
	 * @throws IOException
	 * @throws LicenseGeneratorException 
	 * @throws InvalidLicenseTemplateException 
	 */
	void writeException(LicenseException exception, boolean deprecated, String deprecatedVersion) throws IOException, LicenseGeneratorException, InvalidLicenseTemplateException;

}
