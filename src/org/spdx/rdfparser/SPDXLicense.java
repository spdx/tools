/**
 * Copyright (c) 2011 Source Auditor Inc.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.spdx.rdfparser;

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
public class SPDXLicense {

	private String name;
	private String id;
	private String text;
	private String sourceUrl;
	private String notes;
	private String standardLicenseHeader;
	private String template;
	private Node licenseNode = null;
	private Model model = null;
	
	public SPDXLicense(String name, String id, String text, String sourceUrl, String notes,
			String standardLicenseHeader, String template) {
		this.name = name;
		this.id = id;
		this.text = text;
		this.sourceUrl = sourceUrl;
		this.notes = notes;
		this.standardLicenseHeader = standardLicenseHeader;
		this.template = template;
	}
	/**
	 * Constructs an SPDX License from the licenseNode
	 * @param licenseNode RDF graph node representing the SPDX License
	 */
	public SPDXLicense(Model spdxModel, Node licenseNode) {
		// id
		this.licenseNode  = licenseNode;
		this.model = spdxModel;
		Node p = spdxModel.getProperty(SPDXDocument.SPDX_NAMESPACE, SPDXDocument.PROP_LICENSE_ID).asNode();
		Triple m = Triple.createMatch(licenseNode, p, null);
		ExtendedIterator<Triple> tripleIter = spdxModel.getGraph().find(m);	
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			this.id = t.getObject().toString(false);
		}
		// name
		//TODO: Implement name rdf parsing
		this.name = id;
		// text
		p = spdxModel.getProperty(SPDXDocument.SPDX_NAMESPACE, SPDXDocument.PROP_LICENSE_TEXT).asNode();
		m = Triple.createMatch(licenseNode, p, null);
		tripleIter = spdxModel.getGraph().find(m);	
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			this.text = t.getObject().toString(false);
		}
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
	/**
	 * @return the id
	 */
	public String getId() {
		return this.id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
		if (this.licenseNode != null) {
			// delete any previous created
			Property p = model.getProperty(SPDXDocument.SPDX_NAMESPACE, SPDXDocument.PROP_LICENSE_ID);
			Resource s = model.getResource(licenseNode.getURI());
			model.removeAll(s, p, null);
			// add the property
			p = model.createProperty(SPDXDocument.SPDX_NAMESPACE, SPDXDocument.PROP_LICENSE_ID);
			s.addProperty(p, id);
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
		if (this.licenseNode != null) {
			// delete any previous created
			Property p = model.getProperty(SPDXDocument.SPDX_NAMESPACE, SPDXDocument.PROP_LICENSE_TEXT);
			Resource s = model.getResource(licenseNode.getURI());
			model.removeAll(s, p, null);
			// add the property
			p = model.createProperty(SPDXDocument.SPDX_NAMESPACE, SPDXDocument.PROP_LICENSE_TEXT);
			s.addProperty(p, text);
		}
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (this.name != null) {
			sb.append(this.name);
		}
		if (this.id != null) {
			sb.append("; ID: ");
			sb.append(this.id);
		}
		return sb.toString();
	}
	public Resource createResource(Model model) {
		this.model = model;
		Property licenseProperty = model.createProperty(SPDXDocument.SPDX_NAMESPACE, 
				SPDXDocument.PROP_SPDX_NONSTANDARD_LICENSES);
		Resource r = model.createResource(licenseProperty);
		if (id != null) {
			Property idProperty = model.createProperty(SPDXDocument.SPDX_NAMESPACE, 
					SPDXDocument.PROP_LICENSE_ID);
			r.addProperty(idProperty, this.id);
		}
		if (this.text != null) {
			Property textProperty = model.createProperty(SPDXDocument.SPDX_NAMESPACE, 
					SPDXDocument.PROP_LICENSE_TEXT);
			r.addProperty(textProperty, this.text);
		}
		this.licenseNode = r.asNode();
		return r;
	}
}
