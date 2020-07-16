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
package org.spdx.html;

import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.model.ExternalRef;

/**
 * @author Gary O'Neall
 *
 */
public class ExternalRefContext {

	private String category = "[UNKNOWN]";
	private String type = "[UNKNOWN]";
	private String locator = "[UNKNOWN]";

	public ExternalRefContext(ExternalRef externalRef) {
		if (externalRef != null) {
			if (externalRef.getReferenceCategory() != null) {
				category = externalRef.getReferenceCategory().getTag();
			}
			try {
				if (externalRef.getReferenceType() != null) {
					type = externalRef.getReferenceType().toString();
				}
			} catch (InvalidSPDXAnalysisException e) {
				type = "[ERROR: "+e.getMessage()+"]";
			}
			if (externalRef.getReferenceLocator() != null) {
				locator = externalRef.getReferenceLocator();
			}
		}
	}

	/**
	 * @return the category
	 */
	public String getCategory() {
		return category;
	}

	/**
	 * @param category the category to set
	 */
	public void setCategory(String category) {
		this.category = category;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return the locator
	 */
	public String getLocator() {
		return locator;
	}

	/**
	 * @param locator the locator to set
	 */
	public void setLocator(String locator) {
		this.locator = locator;
	}
}
