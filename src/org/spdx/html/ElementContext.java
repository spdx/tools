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
package org.spdx.html;

import java.util.HashMap;

import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.model.SpdxElement;

/**
 * Mustache context holding only the element ID, name and the URL link to the element
 * @author Gary O'Neall
 *
 */
public class ElementContext {
	private String name;
	private String id;
	private String elementLink;
	String error = null;
	
	public ElementContext(InvalidSPDXAnalysisException e) {
		this.error = e.getMessage();
	}
	public ElementContext(SpdxElement element, 
			HashMap<String, String> spdxIdToUrl) {
		if (element == null) {
			return;
		}
		this.name = element.getName();
		this.id = element.getId();
		this.elementLink = spdxIdToUrl.get(this.id);
	}
	public String getName() {
		if (error != null) {
			return error;
		}
		return name;
	}
	public String getId() {
		if (error != null) {
			return error;
		}
		return id;
	}
	public String getElementLink() {
		if (error != null) {
			return null;
		}
		return elementLink;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setId(String id) {
		this.id = id;
	}
	public void setElementLink(String elementLink) {
		this.elementLink = elementLink;
	}
	
}
