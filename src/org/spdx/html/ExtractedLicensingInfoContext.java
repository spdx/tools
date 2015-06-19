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
package org.spdx.html;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.license.ExtractedLicenseInfo;

/**
 * @author Source Auditor
 *
 */
public class ExtractedLicensingInfoContext {

	private Exception error = null;
	private ExtractedLicenseInfo license = null;
	private String licenseLink;
	
	/**
	 * @param e
	 */
	public ExtractedLicensingInfoContext(InvalidSPDXAnalysisException e) {
		this.error = e;
	}

	/**
	 * @param spdxNonStandardLicense
	 * @param spdxIdToUrl 
	 */
	public ExtractedLicensingInfoContext(ExtractedLicenseInfo spdxNonStandardLicense, Map<String, String> spdxIdToUrl) {
		this.license = spdxNonStandardLicense;
		if (this.license != null) {
			this.licenseLink = spdxIdToUrl.get(this.license.getLicenseId());
		}	
	}
	
	public String licenseId() {
		if (this.license == null && this.error != null) {
			return "Error getting non-standard license: "+error.getMessage();
		}
		if (this.license != null) {
			return this.license.getLicenseId();
		} else {
			return null;
		}
	}
	
	public String extractedText() {
		if (this.license == null && this.error != null) {
			return "Error getting non-standard license: "+error.getMessage();
		}
		if (this.license != null) {
			return this.license.getExtractedText();
		} else {
			return null;
		}
	}
	
	public String comment() {
		if (this.license == null && this.error != null) {
			return "Error getting non-standard license: "+error.getMessage();
		}
		if (this.license != null) {
			return this.license.getComment();
		} else {
			return null;
		}
	}
	
	public String licenseName() {
		if (this.license == null && this.error != null) {
			return "Error getting non-standard license: "+error.getMessage();
		}
		if (this.license != null) {
			return this.license.getName();
		} else {
			return null;
		}
	}
	
	public List<String> crossReferenceUrls() {
		ArrayList<String> retval = new ArrayList<String>();
		if (this.license != null) {
			String[] crossRefUrls = this.license.getSeeAlso();
			if (crossRefUrls != null) {
				for (int i = 0; i < crossRefUrls.length; i++) {
					retval.add(crossRefUrls[i]);
				}
			}
		} else {
			if (this.error != null) {
				retval.add("Error getting extracted licensing info: "+this.error);
			}
		}
		return retval;
	}

	public String getLicenseLink() {
		return licenseLink;
	}

	public void setLicenseLink(String licenseLink) {
		this.licenseLink = licenseLink;
	}
	
	

}
