/**
 * Copyright (c) 2018 Source Auditor Inc.
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
package org.spdx.rdfparser.license;

import org.apache.jena.graph.Node;
import org.spdx.html.InvalidLicenseTemplateException;
import org.spdx.licenseTemplate.LicenseTemplateRuleException;
import org.spdx.licenseTemplate.SpdxLicenseTemplateHelper;
import org.spdx.rdfparser.IModelContainer;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SpdxRdfConstants;

/**
 * Exceptions listed in the SPDX license list
 * 
 * @author Gary O'Neall
 *
 */
public class ListedLicenseException extends LicenseException {
	
	private String deprecatedVersion = null;
	private String exceptionTextHtml = null;

	/**
	 * @param modelContainer
	 * @param node
	 * @throws InvalidSPDXAnalysisException
	 */
	public ListedLicenseException(IModelContainer modelContainer, Node node) throws InvalidSPDXAnalysisException {
		super(modelContainer, node);
		this.deprecatedVersion = null;
		this.exceptionTextHtml = null;
	}

	/**
	 * @param licenseExceptionId
	 * @param name
	 * @param licenseExceptionText
	 * @param seeAlso
	 * @param comment
	 */
	public ListedLicenseException(String licenseExceptionId, String name, String licenseExceptionText, String[] seeAlso,
			String comment) {
		super(licenseExceptionId, name, licenseExceptionText, seeAlso, comment);
	}

	/**
	 * @param licenseExceptionId Exception ID - short form ID
	 * @param name Full name of the Exception
	 * @param licenseExceptionText Text for the Exception
	 * @param licenseExceptionTemplate License exception template use for matching license exceptions per SPDX license matching guidelines
	 * @param comment Comments on the exception
	 * @param seeAlso URL references to external sources for the exception
	 * @param exceptionTextHtml
	 * @param isDeprecated
	 * @param deprecatedVersion
	 */
	public ListedLicenseException(String licenseExceptionId, String name, String licenseExceptionText,
			String licenseExceptionTemplate, String[] seeAlso, String comment, 
			String exceptionTextHtml, boolean isDeprecated, String deprecatedVersion) {
		super(licenseExceptionId, name, licenseExceptionText, licenseExceptionTemplate, seeAlso, comment);
		this.exceptionTextHtml = exceptionTextHtml;
		super.setDeprecated(isDeprecated);
		this.deprecatedVersion = deprecatedVersion;
	}

	/**
	 * @param licenseExceptionId Exception ID - short form ID
	 * @param name Full name of the Exception
	 * @param licenseExceptionText Text for the Exception
	 * @param licenseExceptionTemplate License exception template use for matching license exceptions per SPDX license matching guidelines
	 * @param seeAlso URL references to external sources for the exception
	 * @param comment Comments on the exception
	 */
	public ListedLicenseException(String licenseExceptionId, String name, String licenseExceptionText,
			String licenseExceptionTemplate, String[] seeAlso, String comment) {
		this(licenseExceptionId, name, licenseExceptionText, licenseExceptionTemplate, seeAlso, comment, null, false, null);
	}

	/**
	 * @param licenseExceptionId Exception ID - short form ID
	 * @param name Full name of the Exception
	 * @param licenseExceptionText Text for the Exception
	 */
	public ListedLicenseException(String licenseExceptionId, String name, String licenseExceptionText) {
		this(licenseExceptionId, name, licenseExceptionText, null, new String[0], null);
	}

	/**
	 * @param exceptionId
	 */
	public ListedLicenseException(String exceptionId) {
		super(exceptionId);
		this.deprecatedVersion = null;
		this.exceptionTextHtml = null;
	}

	/**
	 * @return the deprecatedVersion
	 */
	public String getDeprecatedVersion() {
		return deprecatedVersion;
	}

	/**
	 * @param deprecatedVersion the deprecatedVersion to set
	 */
	public void setDeprecatedVersion(String deprecatedVersion) {
		this.deprecatedVersion = deprecatedVersion;
	}

	/**
	 * @return HTML fragment containing the Exception Text
	 * @throws InvalidLicenseTemplateException 
	 */
	public String getExceptionTextHtml() throws InvalidLicenseTemplateException {
		if (exceptionTextHtml == null) {
			// Format the HTML using the text and template
			String templateText = this.getLicenseExceptionTemplate();
			if (templateText != null && !templateText.trim().isEmpty()) {
				try {
					exceptionTextHtml = SpdxLicenseTemplateHelper.templateTextToHtml(templateText);
				} catch(LicenseTemplateRuleException ex) {
					throw new InvalidLicenseTemplateException("Invalid license rule found in exception text for exception "+getName()+":"+ex.getMessage());
				}
			} else {
				exceptionTextHtml = SpdxLicenseTemplateHelper.formatEscapeHTML(this.getLicenseExceptionText());
			}
		}
		return exceptionTextHtml;
	}

	/**
	 * @param exceptionTextHtml the exceptionTextHtml to set
	 */
	public void setExceptionTextHtml(String exceptionTextHtml) {
		this.exceptionTextHtml = exceptionTextHtml;
	}
	
	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.license.LicenseException#getUri(org.spdx.rdfparser.IModelContainer)
	 */
	@Override
	public String getUri(IModelContainer modelContainer)
			throws InvalidSPDXAnalysisException {	
		return this.createListedExceptionUri(this.getLicenseExceptionId());
	}
	
	/**
	 * Creates a listed exception URI by appending the standard license ID to the URL hosting the SPDX licenses
	 * @param id listed exception ID
	 * @return
	 */
	private String createListedExceptionUri(String id) {
		return SpdxRdfConstants.STANDARD_LICENSE_URL + "/" + id;
	}
	
	@Override
    public LicenseException clone() {
		ListedLicenseException retval = new ListedLicenseException(this.getLicenseExceptionId(), this.getName(), this.getLicenseExceptionText(),
				this.getLicenseExceptionTemplate(), this.getSeeAlso(), this.getComment());
		retval.setDeprecated(this.isDeprecated());
		retval.setExample(this.getExample());
		retval.setExceptionTextHtml(this.exceptionTextHtml);
		return retval;
	}
}
