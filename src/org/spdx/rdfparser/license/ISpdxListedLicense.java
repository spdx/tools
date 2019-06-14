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
package org.spdx.rdfparser.license;

import org.spdx.rdfparser.InvalidSPDXAnalysisException;

/**
 * Interface for reading (not writing) SPDX Listed License properties.
 * 
 * @author Gary O'Neall
 *
 */
public interface ISpdxListedLicense {

	/**
	 * @return the id
	 */
	public String getLicenseId();

	/**
	 * @return standard license header template
	 */
	public String getStandardLicenseHeaderTemplate();

	/**
	 * @return the text of the license
	 */
	public String getLicenseText();
	
	/**
	 * @return the version this license was deprecated in
	 */
	public String getDeprecatedVersion();
	/**
	 * @return the template
	 */
	public String getStandardLicenseTemplate();
	
	/**
	 * @return the comments
	 */
	public String getComment();
	
	/**
	 * @return the name
	 */
	public String getName();
	
	/**
	 * @return the urls which reference the same license information
	 */
	public String[] getSeeAlso();
	
	/**
	 * @return the standardLicenseHeader
	 */
	public String getStandardLicenseHeader();
	
	/**
	 * @return true if FSF describes the license as free / libre, false if FSF describes the license as not free / libre, null if FSF does not reference the license
	 */
	public Boolean getFsfLibre();
	
	/**
	 * @return true if FSF describes the license as free / libre, false if FSF describes the license as not free / libre or if FSF does not reference the license
	 * @throws InvalidSPDXAnalysisException
	 */
	public boolean isFsfLibre();
	
	/**
	 * @return true if FSF specified this license as not free/libre, false if it has been specified by the FSF as free / libre or if it has not been specified
	 */
	public boolean isNotFsfLibre();
	
	/**
	 * @return true if the license is listed as an approved license on the OSI website
	 */
	public boolean isOsiApproved();
	
	/**
	 * @return true if this license is marked as being deprecated
	 */
	public boolean isDeprecated();
}
