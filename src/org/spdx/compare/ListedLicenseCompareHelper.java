/**
 * Copyright (c) 2019 Source Auditor Inc.
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
package org.spdx.compare;

import java.io.IOException;
import java.util.List;

import org.spdx.compare.CompareTemplateOutputHandler.DifferenceDescription;
import org.spdx.licenseTemplate.LicenseTemplateRuleException;
import org.spdx.licenseTemplate.SpdxLicenseTemplateHelper;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.license.ISpdxListedException;
import org.spdx.rdfparser.license.ISpdxListedLicense;
import org.spdx.rdfparser.license.LicenseInfoFactory;
import org.spdx.rdfparser.license.LicenseParserException;

import com.google.common.collect.Lists;

/**
 * Primarily a static class of helper functions for comparing text to SPDX Listed Licenses
 * 
 * See <code>LicenseCompareHelper</code> class for a more general set of license compare utilities.
 * @author Gary O'Neall
 *
 */
public class ListedLicenseCompareHelper {
	
	/**
	 * Compares license text to the license text of an SPDX Standard License
	 * @param license SPDX Standard License to compare
	 * @param compareText Text to compare to the standard license
	 * @return any differences found
	 * @throws SpdxCompareException
	 */
	public static DifferenceDescription isTextStandardLicense(ISpdxListedLicense license, String compareText) throws SpdxCompareException {
		String licenseTemplate = license.getStandardLicenseTemplate();
		if (licenseTemplate == null || licenseTemplate.trim().isEmpty()) {
			licenseTemplate = license.getLicenseText();
		}
		CompareTemplateOutputHandler compareTemplateOutputHandler = null;
		try {
			compareTemplateOutputHandler = new CompareTemplateOutputHandler(compareText);
		} catch (IOException e1) {
			throw(new SpdxCompareException("IO Error reading the compare text: "+e1.getMessage(),e1));
		}
		try {
			SpdxLicenseTemplateHelper.parseTemplate(licenseTemplate, compareTemplateOutputHandler);
		} catch (LicenseTemplateRuleException e) {
			throw(new SpdxCompareException("Invalid template rule found during compare: "+e.getMessage(),e));
		} catch (LicenseParserException e) {
			throw(new SpdxCompareException("Invalid template found during compare: "+e.getMessage(),e));
		}
		return compareTemplateOutputHandler.getDifferences();
	}
	
	/**
	 * Compares exception text to the exception text of an SPDX Standard exception
	 * @param exception SPDX Standard exception to compare
	 * @param compareText Text to compare to the standard exceptions
	 * @return any differences found
	 * @throws SpdxCompareException
	 */
	public static DifferenceDescription isTextStandardException(ISpdxListedException exception, String compareText) throws SpdxCompareException {
		String exceptionTemplate = exception.getLicenseExceptionTemplate();
		if (exceptionTemplate == null || exceptionTemplate.trim().isEmpty()) {
			exceptionTemplate = exception.getLicenseExceptionText();
		}
		CompareTemplateOutputHandler compareTemplateOutputHandler = null;
		try {
			compareTemplateOutputHandler = new CompareTemplateOutputHandler(compareText);
		} catch (IOException e1) {
			throw(new SpdxCompareException("IO Error reading the compare text: "+e1.getMessage(),e1));
		}
		try {
			SpdxLicenseTemplateHelper.parseTemplate(exceptionTemplate, compareTemplateOutputHandler);
		} catch (LicenseTemplateRuleException e) {
			throw(new SpdxCompareException("Invalid template rule found during compare: "+e.getMessage(),e));
		} catch (LicenseParserException e) {
			throw(new SpdxCompareException("Invalid template found during compare: "+e.getMessage(),e));
		}
		return compareTemplateOutputHandler.getDifferences();
	}
	
	/**
	 * Returns a list of SPDX Standard License ID's that match the text provided using
	 * the SPDX matching guidelines.
	 * @param licenseText Text to compare to the standard license texts
	 * @return Array of SPDX standard license IDs that match
	 * @throws InvalidSPDXAnalysisException If an error occurs accessing the standard licenses
	 * @throws SpdxCompareException If an error occurs in the comparison
	 */
	public static String[] matchingStandardLicenseIds(String licenseText) throws InvalidSPDXAnalysisException, SpdxCompareException {
		String[] stdLicenseIds = LicenseInfoFactory.getSpdxListedLicenseIds();
		List<String> matchingIds  = Lists.newArrayList();
		for (String stdLicId : stdLicenseIds) {
			ISpdxListedLicense license = LicenseInfoFactory.getListedLicenseById(stdLicId);
			if (!isTextStandardLicense(license, licenseText).isDifferenceFound()) {
				matchingIds.add(license.getLicenseId());
			}
		}
		return matchingIds.toArray(new String[matchingIds.size()]);
	}

}
