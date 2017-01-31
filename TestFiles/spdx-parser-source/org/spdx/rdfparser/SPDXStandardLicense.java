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

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.iterator.ExtendedIterator;

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
	private boolean osiApproved;

	public SPDXStandardLicense(String name, String id, String text, String sourceUrl, String notes,
			String standardLicenseHeader, String template, boolean osiApproved) {
		super(id, text);
		this.name = name;
		this.sourceUrl = sourceUrl;
		this.notes = notes;
		this.standardLicenseHeader = standardLicenseHeader;
		this.template = template;
		this.osiApproved = osiApproved;
	}
	/**
	 * Constructs an SPDX License from the licenseNode
	 * @param licenseNode RDF graph node representing the SPDX License
	 * @throws InvalidSPDXAnalysisException
	 */
	public SPDXStandardLicense(Model spdxModel, Node licenseNode) throws InvalidSPDXAnalysisException {
		super(spdxModel, licenseNode);
		// name
		Node p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_NAME).asNode();
		Triple m = Triple.createMatch(licenseNode, p, null);
		ExtendedIterator<Triple> tripleIter = model.getGraph().find(m);
		if (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			this.name = t.getObject().toString(false);
		} else {
			this.name = id;
		}
		// SourceUrl
		p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_URL).asNode();
		m = Triple.createMatch(licenseNode, p, null);
		tripleIter = model.getGraph().find(m);
		if (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			this.sourceUrl = t.getObject().toString(false);
		} else {
			this.sourceUrl = "";
		}
		// notes
		p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_NOTES).asNode();
		m = Triple.createMatch(licenseNode, p, null);
		tripleIter = model.getGraph().find(m);
		if (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			this.notes = t.getObject().toString(false);
		} else {
			this.notes = "";
		}
		// standardLicenseHeader
		p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_HEADER).asNode();
		m = Triple.createMatch(licenseNode, p, null);
		tripleIter = model.getGraph().find(m);
		if (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			this.standardLicenseHeader = t.getObject().toString(false);
		} else {
			this.standardLicenseHeader = "";
		}
		// template
		p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_TEMPLATE).asNode();
		m = Triple.createMatch(licenseNode, p, null);
		tripleIter = model.getGraph().find(m);
		if (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			this.template = t.getObject().toString(false);
		} else {
			this.template = "";
		}
		// OSI Approved
		p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_OSI_APPROVED).asNode();
		m = Triple.createMatch(licenseNode, p, null);
		tripleIter = model.getGraph().find(m);
		if (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			if (t.getObject().toString(false).toUpperCase().startsWith("T")) {
				this.osiApproved = true;
			} else {
				this.osiApproved = false;
			}
		} else {
			this.osiApproved = false;
		}
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
	 * @see org.spdx.rdfparser.SPDXLicenseInfo#_createResource(org.apache.jena.rdf.model.Model)
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
			// covers o == null, as null is not an instance of anything
			return false;
		}
		SPDXStandardLicense comp = (SPDXStandardLicense)o;
		if (this.id == null) {
			return (comp.getId() == null);
		} else {
			return (this.id.equals(comp.getId()));
		}
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
			retval.add("Missing required license text");
		}
		return retval;
	}
	/**
	 * @return true if the license is listed as an approved license on the OSI website
	 */
	public boolean isOsiApproved() {
		return this.osiApproved;
	}

	public void setOsiApproved(boolean osiApproved) {
		this.osiApproved = osiApproved;
	}
}
