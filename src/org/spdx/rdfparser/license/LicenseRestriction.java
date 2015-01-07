/**
 * Copyright (c) 2014 Source Auditor Inc.
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
package org.spdx.rdfparser.license;

/**
 * Represents an SPDX license exception as defined in the License Expression Language
 * Used with the "with" unary expression.
 * 
 * @author Gary O'Neall
 *
 */
public class LicenseRestriction {
	
	//TODO: Implement RDF parsing
	
	private String id;
	private String name;
	private String text;
	private String[] sourceUrl;
	private String notes;
	private String example;
	
	/**
	 * @param id Exception ID - short form ID
	 * @param name Full name of the Exception
	 * @param text Text for the Exception
	 * @param notes Comments on the restriction
	 * @param example Example of use
	 * @param sourceUrl URL references to external sources for the exception
	 */
	public LicenseRestriction(String id, String name, String text,
			String[] sourceUrl, String notes, String example) {
		this.id = id;
		this.name = name;
		this.text = text;
		this.sourceUrl = sourceUrl;
		this.notes = notes;
		this.example = example;
	}
	
	/**
	 * @param id Exception ID - short form ID
	 * @param name Full name of the Exception
	 * @param text Text for the Exception
	 */
	public LicenseRestriction(String id, String name, String text) {
		this(id, name, text, new String[0], "", "");
	}
	
	public LicenseRestriction() {
		this(null, null, null);
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the text
	 */
	public String getText() {
		return text;
	}

	/**
	 * @param text the text to set
	 */
	public void setText(String text) {
		this.text = text;
	}

	/**
	 * @return the sourceUrl
	 */
	public String[] getSourceUrl() {
		return sourceUrl;
	}

	/**
	 * @param sourceUrl the sourceUrl to set
	 */
	public void setSourceUrl(String[] sourceUrl) {
		this.sourceUrl = sourceUrl;
	}

	/**
	 * @return
	 */
	public String getNotes() {
		return this.notes;
	}

	/**
	 * @return
	 */
	public String getExample() {
		return example;
	}
	
	public void setExample(String examples) {
		this.example = examples;
	}

}
