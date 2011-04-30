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
package org.spdx.rdfparser;

import java.util.ArrayList;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * @author Source Auditor
 *
 */
public class SPDXStandardLicense extends SPDXLicense {

	private String name;
	private String sourceUrl;
	private String notes;
	private String standardLicenseHeader;
	private String template;
	
	public SPDXStandardLicense(String name, String id, String text, String sourceUrl, String notes,
			String standardLicenseHeader, String template) {
		super(id, text);
		this.name = name;
		this.sourceUrl = sourceUrl;
		this.notes = notes;
		this.standardLicenseHeader = standardLicenseHeader;
		this.template = template;
	}
	/**
	 * Constructs an SPDX License from the licenseNode
	 * @param licenseNode RDF graph node representing the SPDX License
	 * @throws InvalidSPDXAnalysisException 
	 */
	public SPDXStandardLicense(Model spdxModel, Node licenseNode) throws InvalidSPDXAnalysisException {
		super(spdxModel, licenseNode);
		// name
		//TODO: Implement name rdf parsing
		this.name = id;
		// SourceUrl
		//TODO: Implement SourceUrl rdf parsing
		this.sourceUrl = "";
		// notes
		//TODO: Implement notes rdf parsing
		this.notes = "";
		// standardLicenseHeader
		//TODO: Implement standardLicenseHeader rdf parsing
		this.standardLicenseHeader = "";
		// template
		//TODO: Implement template rdf parsing
		this.template = "";
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
	 * @return the sourceUrl
	 */
	public String getSourceUrl() {
		return sourceUrl;
	}
	/**
	 * @param sourceUrl the sourceUrl to set
	 */
	public void setSourceUrl(String sourceUrl) {
		this.sourceUrl = sourceUrl;
	}
	/**
	 * @return the notes
	 */
	public String getNotes() {
		return notes;
	}
	/**
	 * @param notes the notes to set
	 */
	public void setNotes(String notes) {
		this.notes = notes;
	}
	/**
	 * @return the standardLicenseHeader
	 */
	public String getStandardLicenseHeader() {
		return standardLicenseHeader;
	}
	/**
	 * @param standardLicenseHeader the standardLicenseHeader to set
	 */
	public void setStandardLicenseHeader(String standardLicenseHeader) {
		this.standardLicenseHeader = standardLicenseHeader;
	}
	/**
	 * @return the template
	 */
	public String getTemplate() {
		return template;
	}
	/**
	 * @param template the template to set
	 */
	public void setTemplate(String template) {
		this.template = template;
	}
	
	@Override
	public String toString() {
		// must be only the ID if we want to reuse the 
		// toString for creating parseable license info strings
		return this.id;
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.SPDXLicenseInfo#_createResource(com.hp.hpl.jena.rdf.model.Model)
	 */
	@Override
	protected Resource _createResource(Model model) {
		Resource type = model.createResource(SpdxRdfConstants.SPDX_NAMESPACE+SpdxRdfConstants.CLASS_SPDX_STANDARD_LICENSE);
		String uri = this.createStdLicenseUri(this.id);
		Resource r = super._createResource(model, type, uri);
		//TODO: Implement additional properties
		return r;
	}
	
	/**
	 * Creates a standard license URI by appending the standard license ID to the URL hosting the SPDX licenses
	 * @param id Standard License ID
	 * @return
	 */
	private String createStdLicenseUri(String id) {
		return SpdxRdfConstants.STANDARD_LICENSE_URL + "/" + id;
	}
	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.SPDXLicenseInfo#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof SPDXStandardLicense)) {
			return false;
		}
		SPDXStandardLicense comp = (SPDXStandardLicense)o;
		return (this.id.equals(comp.getId()));
	}
	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.SPDXLicenseInfo#verify()
	 */
	@Override
	public ArrayList<String> verify() {
		ArrayList<String> retval = new ArrayList<String>();
		String id = this.getId();
		if (id == null || id.isEmpty()) {
			retval.add("Missing required license ID");
		}
		//Todo check to see if the id is a standard license id
		String name = this.getName();
		if (name == null || name.isEmpty()) {
			retval.add("Missing required license name");
		}
		this.getNotes();
		this.getSourceUrl();
		this.getStandardLicenseHeader();
		this.getTemplate();
		String licenseText = this.getText();
		if (licenseText == null || licenseText.isEmpty()) {
//			retval.add("Missing required license text");
			//TODO: Fill in the properties from the standard license website if
			// available
		}
		return retval;
	}
}
