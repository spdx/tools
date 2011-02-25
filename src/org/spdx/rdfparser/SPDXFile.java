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
public class SPDXFile {
	@SuppressWarnings("unused")
	private Node node = null;
	private String name;
	private LicenseDeclaration[] fileLicenses;
	private String sha1;
	private String type;
	private LicenseDeclaration[] seenLicenses;
	private String licenseComments;
	private String copyright;
	private String artifactOf;
	/**
	 * Construct an SPDX File form the fileNode
	 * @param fileNode RDF Graph node representing the SPDX File
	 */
	public SPDXFile(Node fileNode, Model model) {
		this.node = fileNode;
		// name
		Node p = model.getProperty(SPDXDocument.SPDX_NAMESPACE, SPDXDocument.PROP_FILE_NAME).asNode();
		Triple m = Triple.createMatch(fileNode, p, null);
		ExtendedIterator<Triple> tripleIter = model.getGraph().find(m);	
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			this.name = t.getObject().toString(false);
		}
		// sha1
		p = model.getProperty(SPDXDocument.SPDX_NAMESPACE, SPDXDocument.PROP_FILE_SHA1).asNode();
		m = Triple.createMatch(fileNode, p, null);
		tripleIter = model.getGraph().find(m);	
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			this.sha1 = t.getObject().toString(false);
		}
		// type
		p = model.getProperty(SPDXDocument.SPDX_NAMESPACE, SPDXDocument.PROP_FILE_TYPE).asNode();
		m = Triple.createMatch(fileNode, p, null);
		tripleIter = model.getGraph().find(m);	
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			this.type = t.getObject().toString(false);
		}
		// detectedLicense
		ArrayList<LicenseDeclaration> alLic = new ArrayList<LicenseDeclaration>();
		p = model.getProperty(SPDXDocument.SPDX_NAMESPACE, SPDXDocument.PROP_FILE_LICENSE).asNode();
		m = Triple.createMatch(fileNode, p, null);
		tripleIter = model.getGraph().find(m);	
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			alLic.add(new LicenseDeclaration(t.getObject(), model));
		}
		this.fileLicenses = alLic.toArray(new LicenseDeclaration[alLic.size()]);
		// seenLicenses
		alLic.clear();		
		p = model.getProperty(SPDXDocument.SPDX_NAMESPACE, SPDXDocument.PROP_FILE_SEEN_LICENSE).asNode();
		m = Triple.createMatch(fileNode, p, null);
		tripleIter = model.getGraph().find(m);	
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			alLic.add(new LicenseDeclaration(t.getObject(), model));
		}
		this.seenLicenses = alLic.toArray(new LicenseDeclaration[alLic.size()]);
		//licenseComments
		p = model.getProperty(SPDXDocument.SPDX_NAMESPACE, SPDXDocument.PROP_FILE_LIC_COMMENTS).asNode();
		m = Triple.createMatch(fileNode, p, null);
		tripleIter = model.getGraph().find(m);	
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			this.licenseComments = t.getObject().toString(false);
		}
		//copyright
		p = model.getProperty(SPDXDocument.SPDX_NAMESPACE, SPDXDocument.PROP_FILE_COPYRIGHT).asNode();
		m = Triple.createMatch(fileNode, p, null);
		tripleIter = model.getGraph().find(m);	
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			this.copyright = t.getObject().toString(false);
		}
		//artifactof
		//TODO: Implement artifactof
		this.artifactOf = "";
	}
	
	/**
	 * Populates a Jena RDF model with the information from this file declaration
	 * @param licenseResource
	 * @param model
	 */
	public void populateModel(Resource fileResource, Model model) {
		// name
		Property p = model.createProperty(SPDXDocument.SPDX_NAMESPACE, SPDXDocument.PROP_FILE_NAME);
		fileResource.addProperty(p, this.getName());

		if (this.sha1 != null) {
			// sha1
			p = model.createProperty(SPDXDocument.SPDX_NAMESPACE, SPDXDocument.PROP_FILE_SHA1);
			fileResource.addProperty(p, this.getSha1());
		}
		// type
		p = model.createProperty(SPDXDocument.SPDX_NAMESPACE, SPDXDocument.PROP_FILE_TYPE);
		fileResource.addProperty(p, this.getType());

		// detectedLicense
		if (this.fileLicenses != null && this.fileLicenses.length > 0) {
			p = model.createProperty(SPDXDocument.SPDX_NAMESPACE, SPDXDocument.PROP_FILE_LICENSE);
			for (int i = 0; i < this.getFileLicenses().length; i++) {
				Resource lic = model.createResource(p);
				fileResource.addProperty(p, lic);
				this.getFileLicenses()[i].populateModel(lic, model);
			}
		}

		// seenLicenses
		if (this.seenLicenses != null && this.seenLicenses.length > 0) {
			p = model.createProperty(SPDXDocument.SPDX_NAMESPACE, SPDXDocument.PROP_FILE_SEEN_LICENSE);
			for (int i = 0; i < this.getSeenLicenses().length; i++) {
				Resource lic = model.createResource(p);
				fileResource.addProperty(p, lic);
				this.getSeenLicenses()[i].populateModel(lic, model);
			}
		}
		//licenseComments
		if (this.licenseComments != null) {
			p = model.createProperty(SPDXDocument.SPDX_NAMESPACE, SPDXDocument.PROP_FILE_LIC_COMMENTS);
			fileResource.addProperty(p, this.getLicenseComments());
		}
		//copyright
		if (this.copyright != null) {
			p = model.getProperty(SPDXDocument.SPDX_NAMESPACE, SPDXDocument.PROP_FILE_COPYRIGHT);
			fileResource.addProperty(p, this.getCopyright());	
		}

		//artifactof
		//TODO: Implement artifactof
		this.artifactOf = "";
	}
	public SPDXFile(String name, String type, String sha1,
			LicenseDeclaration[] fileLicenses,
			LicenseDeclaration[] seenLicenses, String licenseComments,
			String copyright, String artifactOf) {
		this.name = name;
		this.type = type;
		this.sha1 = sha1;
		this.fileLicenses = fileLicenses;
		this.seenLicenses = seenLicenses;
		this.licenseComments = licenseComments;
		this.copyright = copyright;
		this.artifactOf = artifactOf;
	}
	/**
	 * @return the seenLicenses
	 */
	public LicenseDeclaration[] getSeenLicenses() {
		return seenLicenses;
	}
	/**
	 * @param seenLicenses the seenLicenses to set
	 */
	public void setSeenLicenses(LicenseDeclaration[] seenLicenses) {
		this.seenLicenses = seenLicenses;
	}
	/**
	 * @return the licenseComments
	 */
	public String getLicenseComments() {
		return licenseComments;
	}
	/**
	 * @param licenseComments the licenseComments to set
	 */
	public void setLicenseComments(String licenseComments) {
		this.licenseComments = licenseComments;
	}
	/**
	 * @return the copyright
	 */
	public String getCopyright() {
		return copyright;
	}
	/**
	 * @param copyright the copyright to set
	 */
	public void setCopyright(String copyright) {
		this.copyright = copyright;
	}
	/**
	 * @return the artifactOf
	 */
	public String getArtifactOf() {
		return artifactOf;
	}
	/**
	 * @param artifactOf the artifactOf to set
	 */
	public void setArtifactOf(String artifactOf) {
		this.artifactOf = artifactOf;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return this.name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the fileLicenses
	 */
	public LicenseDeclaration[] getFileLicenses() {
		return this.fileLicenses;
	}
	/**
	 * @param fileLicenses the fileLicenses to set
	 */
	public void setFileLicenses(LicenseDeclaration[] fileLicenses) {
		this.fileLicenses = fileLicenses;
	}
	/**
	 * @return the sha1
	 */
	public String getSha1() {
		return this.sha1;
	}
	/**
	 * @param sha1 the sha1 to set
	 */
	public void setSha1(String sha1) {
		this.sha1 = sha1;
	}
	/**
	 * @return the type
	 */
	public String getType() {
		return this.type;
	}
	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}
}
