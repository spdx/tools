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

/**
 * Interface for reading (not writing) SPDX Listed Exception properties.
 * 
 * @author Gary O'Neall
 *
 */
public interface ISpdxListedException {

	/**
	 * @return Exception Template
	 */
	public String getLicenseExceptionTemplate();

	/**
	 * @return Exception Text
	 */
	public String getLicenseExceptionText();
	
	/**
	 * @return the name
	 */
	public String getName();
	
	/**
	 * @return the ID for the license exception
	 */
	public String getLicenseExceptionId();
	
	/**
	 * @return related URL's for the exception
	 */
	public String[] getSeeAlso();
	
	/**
	 * @return comment or notes
	 */
	public String getComment();
	
	/**
	 * @return Example where the exception is used
	 */
	public String getExample();
	
	/**
	 * @return true if the exception has been deprecated
	 */
	public boolean getDeprecated();
}
