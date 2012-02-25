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
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * @author Source Auditor
 *
 */
public class SPDXStandardLicense extends SPDXLicense {
	
	//TODO: Sync this up with the spec, since only licenseId and licenseText are currently standardized

	private String name;
	private String sourceUrl;
	private String notes;
	private String standardLicenseHeader;
	private String template;
	private String text;
	private boolean osiApproved;
	
	public SPDXStandardLicense(String name, String id, String text, String sourceUrl, String notes,
			String standardLicenseHeader, String template, boolean osiApproved) throws InvalidSPDXAnalysisException {
		super(id);
// The following check was removed since this class is used in creating the standard licenses for the website
//		if (!SPDXLicenseInfoFactory.isStandardLicenseID(id)) {
//		    throw new InvalidSPDXAnalysisException("Invalid standard license ID: " + id);
//		}
		this.name = name;
		this.sourceUrl = sourceUrl;
		this.notes = notes;
		this.standardLicenseHeader = standardLicenseHeader;
		this.template = template;
		this.osiApproved = osiApproved;
		this.text = text;
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
		// text
		p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_LICENSE_TEXT).asNode();
		m = Triple.createMatch(licenseInfoNode, p, null);
		tripleIter = model.getGraph().find(m);	
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			this.text = t.getObject().toString(true);
		}
		//KLUDGE - work around the fact that some of the old licenses were generated with the incorrect tag - "LicenseText"
		//Todo: Remove this once the licenses on the website have been updated
		if (text == null) {
			p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, "LicenseText").asNode();
			m = Triple.createMatch(licenseInfoNode, p, null);
			tripleIter = model.getGraph().find(m);	
			while (tripleIter.hasNext()) {
				Triple t = tripleIter.next();
				this.text = t.getObject().toString(false);
			}

		}
		//END KLUDGE
		// SourceUrl
		p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_URL).asNode();
		m = Triple.createMatch(licenseNode, p, null);
		tripleIter = model.getGraph().find(m);	
		if (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			this.sourceUrl = t.getObject().toString(false);
		} else {
			this.sourceUrl = null;
		}
		// notes
		p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_NOTES).asNode();
		m = Triple.createMatch(licenseNode, p, null);
		tripleIter = model.getGraph().find(m);	
		if (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			this.notes = t.getObject().toString(false);
		} else {
			this.notes = null;
		}
		// standardLicenseHeader
		p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_HEADER).asNode();
		m = Triple.createMatch(licenseNode, p, null);
		tripleIter = model.getGraph().find(m);	
		if (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			this.standardLicenseHeader = t.getObject().toString(false);
		} else {
			this.standardLicenseHeader = null;
		}
		// template
		p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_TEMPLATE).asNode();
		m = Triple.createMatch(licenseNode, p, null);
		tripleIter = model.getGraph().find(m);	
		if (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			this.template = t.getObject().toString(false);
		} else {
			this.template = null;
		}
		// OSI Approved
		p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_OSI_APPROVED).asNode();
		m = Triple.createMatch(licenseNode, p, null);
		tripleIter = model.getGraph().find(m);	
		if (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			String osiTextValue = t.getObject().toString(false);
			if (osiTextValue.equals("true") || osiTextValue.equals("1")) {
				this.osiApproved = true;
			} else if (osiTextValue.equals("false") || osiTextValue.equals("0")){
				this.osiApproved = false;
			} else {
				throw(new InvalidSPDXAnalysisException("Invalid value for OSI Approved - must be {true, false, 0, 1}"));
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
		if (this.licenseInfoNode != null) {
			// delete any previous created
			Property p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_NAME);
			model.removeAll(resource, p, null);
			// add the property
			p = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_NAME);
			resource.addProperty(p, this.name);
		}
	}
	
	/**
	 * @return the text
	 */
	public String getText() {
		return this.text;
	}

	/**
	 * @param text the text to set
	 */
	public void setText(String text) {
		this.text = text;
		if (this.licenseInfoNode != null) {
			// delete any previous created
			Property p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_LICENSE_TEXT);
			model.removeAll(resource, p, null);
			// add the property
			p = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_LICENSE_TEXT);
			resource.addProperty(p, text);
		}
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
		if (this.licenseInfoNode != null) {
			// delete any previous created
			Property p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_URL);
			model.removeAll(resource, p, null);
			// add the property
			p = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_URL);
			resource.addProperty(p, this.sourceUrl);
		}
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
		if (this.licenseInfoNode != null) {
			// delete any previous created
			Property p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_NOTES);
			model.removeAll(resource, p, null);
			// add the property
			p = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_NOTES);
			resource.addProperty(p, this.notes);
		}
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
		if (this.licenseInfoNode != null) {
			// delete any previous created
			Property p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_HEADER);
			model.removeAll(resource, p, null);
			// add the property
			p = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_HEADER);
			resource.addProperty(p, this.standardLicenseHeader);
		}
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
		if (this.licenseInfoNode != null) {
			// delete any previous created
			Property p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_TEMPLATE);
			model.removeAll(resource, p, null);
			// add the property
			p = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_TEMPLATE);
			resource.addProperty(p, this.template);
		}
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
		//text
		if (this.text != null) {
			Property textProperty = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, 
					SpdxRdfConstants.PROP_LICENSE_TEXT);
			model.removeAll(r, textProperty, null);
			r.addProperty(textProperty, this.text);
		}
		//name
		if (name != null) {
			Property namePropery = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, 
					SpdxRdfConstants.PROP_STD_LICENSE_NAME);
			r.addProperty(namePropery, this.name);
		}
		//notes
		if (this.notes != null) {
			Property notesPropery = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, 
					SpdxRdfConstants.PROP_STD_LICENSE_NOTES);
			r.addProperty(notesPropery, this.notes);
		}
		//source URL
		if (this.sourceUrl != null) {
			Property sourceUrlPropery = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, 
					SpdxRdfConstants.PROP_STD_LICENSE_URL);
			r.addProperty(sourceUrlPropery, this.sourceUrl);
		}
		//standard license header
		if (this.standardLicenseHeader != null) {
			Property standardLicenseHeaderPropery = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, 
					SpdxRdfConstants.PROP_STD_LICENSE_HEADER);
			r.addProperty(standardLicenseHeaderPropery, this.standardLicenseHeader);
		}
		//template
		if (this.template != null) {
			Property templatePropery = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, 
					SpdxRdfConstants.PROP_STD_LICENSE_TEMPLATE);
			r.addProperty(templatePropery, this.template);
		}
		//Osi Approved
		if (this.osiApproved) {
			Property osiApprovedPropery = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, 
					SpdxRdfConstants.PROP_STD_LICENSE_OSI_APPROVED);
			r.addProperty(osiApprovedPropery, String.valueOf(this.osiApproved));
		}
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
			retval.add("Missing required license text for " + id);
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
		if (this.licenseInfoNode != null) {
			// delete any previous created
			Property p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_OSI_APPROVED);
			model.removeAll(resource, p, null);
			// add the property
			if (this.osiApproved) {
				p = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_OSI_APPROVED);
				resource.addProperty(p, String.valueOf(this.osiApproved));
			}
		}
	}
}
