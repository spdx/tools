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
public class SPDXFile {
	@SuppressWarnings("unused")
	private Node node = null;
	private Model model = null;
	private String name;
	private SPDXLicenseInfo[] fileLicenses;
	private String sha1;
	private String type;
	private SPDXLicenseInfo[] seenLicenses;
	private String licenseComments;
	private String copyright;
	private DOAPProject[] artifactOf;
	/**
	 * Construct an SPDX File form the fileNode
	 * @param fileNode RDF Graph node representing the SPDX File
	 * @throws InvalidSPDXAnalysisException 
	 */
	public SPDXFile(Model model, Node fileNode) throws InvalidSPDXAnalysisException {
		this.node = fileNode;
		this.model = model;
		// name
		Node p = model.getProperty(SPDXAnalysis.SPDX_NAMESPACE, SPDXAnalysis.PROP_FILE_NAME).asNode();
		Triple m = Triple.createMatch(fileNode, p, null);
		ExtendedIterator<Triple> tripleIter = model.getGraph().find(m);	
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			this.name = t.getObject().toString(false);
		}
		// checksum - sha1
		p = model.getProperty(SPDXAnalysis.SPDX_NAMESPACE, SPDXAnalysis.PROP_FILE_CHECKSUM).asNode();
		m = Triple.createMatch(fileNode, p, null);
		tripleIter = model.getGraph().find(m);	
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			SPDXChecksum cksum = new SPDXChecksum(model, t.getObject());
			if (cksum.getAlgorithm().equals(SPDXChecksum.ALGORITHM_SHA1)) {
				this.sha1 = cksum.getValue();
			}
		}
		// type
		p = model.getProperty(SPDXAnalysis.SPDX_NAMESPACE, SPDXAnalysis.PROP_FILE_TYPE).asNode();
		m = Triple.createMatch(fileNode, p, null);
		tripleIter = model.getGraph().find(m);	
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			this.type = t.getObject().toString(false);
		}
		// detectedLicense
		ArrayList<SPDXLicenseInfo> alLic = new ArrayList<SPDXLicenseInfo>();
		p = model.getProperty(SPDXAnalysis.SPDX_NAMESPACE, SPDXAnalysis.PROP_FILE_LICENSE).asNode();
		m = Triple.createMatch(fileNode, p, null);
		tripleIter = model.getGraph().find(m);	
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			alLic.add(SPDXLicenseInfoFactory.getLicenseInfoFromModel(model, t.getObject()));
		}
		this.fileLicenses = alLic.toArray(new SPDXLicenseInfo[alLic.size()]);
		// seenLicenses
		alLic.clear();		
		p = model.getProperty(SPDXAnalysis.SPDX_NAMESPACE, SPDXAnalysis.PROP_FILE_SEEN_LICENSE).asNode();
		m = Triple.createMatch(fileNode, p, null);
		tripleIter = model.getGraph().find(m);	
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			alLic.add(SPDXLicenseInfoFactory.getLicenseInfoFromModel(model, t.getObject()));
		}
		this.seenLicenses = alLic.toArray(new SPDXLicenseInfo[alLic.size()]);
		//licenseComments
		p = model.getProperty(SPDXAnalysis.SPDX_NAMESPACE, SPDXAnalysis.PROP_FILE_LIC_COMMENTS).asNode();
		m = Triple.createMatch(fileNode, p, null);
		tripleIter = model.getGraph().find(m);	
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			this.licenseComments = t.getObject().toString(false);
		}
		//copyright
		p = model.getProperty(SPDXAnalysis.SPDX_NAMESPACE, SPDXAnalysis.PROP_FILE_COPYRIGHT).asNode();
		m = Triple.createMatch(fileNode, p, null);
		tripleIter = model.getGraph().find(m);	
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			this.copyright = t.getObject().toString(false);
		}
		//artifactOf
		ArrayList<DOAPProject> alProjects = new ArrayList<DOAPProject>();
		p = model.getProperty(SPDXAnalysis.SPDX_NAMESPACE, SPDXAnalysis.PROP_FILE_ARTIFACTOF).asNode();
		m = Triple.createMatch(fileNode, p, null);
		tripleIter = model.getGraph().find(m);	
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			alProjects.add(new DOAPProject(model, t.getObject()));
		}
		this.artifactOf = alProjects.toArray(new DOAPProject[alLic.size()]);
	}
	
	public Resource createResource(Model model) {
		Resource type = model.createResource(SPDXAnalysis.SPDX_NAMESPACE + SPDXAnalysis.CLASS_SPDX_FILE);
		Resource retval = model.createResource(type);
		populateModel(model, retval);
		return retval;
	}
	
	/**
	 * Populates a Jena RDF model with the information from this file declaration
	 * @param licenseResource
	 * @param model
	 */
	private void populateModel(Model model, Resource fileResource) {
		// name
		Property p = model.createProperty(SPDXAnalysis.SPDX_NAMESPACE, SPDXAnalysis.PROP_FILE_NAME);
		fileResource.addProperty(p, this.getName());

		if (this.sha1 != null) {
			// sha1
			SPDXChecksum cksum = new SPDXChecksum(SPDXChecksum.ALGORITHM_SHA1, sha1);
			Resource cksumResource = cksum.createResource(model);

			p = model.createProperty(SPDXAnalysis.SPDX_NAMESPACE, SPDXAnalysis.PROP_FILE_CHECKSUM);
			fileResource.addProperty(p, cksumResource);
		}
		// type
		p = model.createProperty(SPDXAnalysis.SPDX_NAMESPACE, SPDXAnalysis.PROP_FILE_TYPE);
		fileResource.addProperty(p, this.getType());

		// detectedLicense
		if (this.fileLicenses != null && this.fileLicenses.length > 0) {
			p = model.createProperty(SPDXAnalysis.SPDX_NAMESPACE, SPDXAnalysis.PROP_FILE_LICENSE);
			for (int i = 0; i < this.fileLicenses.length; i++) {
				Resource lic = this.fileLicenses[i].createResource(model);
				fileResource.addProperty(p, lic);
			}
		}

		// seenLicenses
		if (this.seenLicenses != null && this.seenLicenses.length > 0) {
			p = model.createProperty(SPDXAnalysis.SPDX_NAMESPACE, SPDXAnalysis.PROP_FILE_SEEN_LICENSE);
			for (int i = 0; i < this.seenLicenses.length; i++) {
				Resource lic = this.seenLicenses[i].createResource(model);
				fileResource.addProperty(p, lic);
			}
		}
		//licenseComments
		if (this.licenseComments != null) {
			p = model.createProperty(SPDXAnalysis.SPDX_NAMESPACE, SPDXAnalysis.PROP_FILE_LIC_COMMENTS);
			fileResource.addProperty(p, this.getLicenseComments());
		}
		//copyright
		if (this.copyright != null) {
			p = model.createProperty(SPDXAnalysis.SPDX_NAMESPACE, SPDXAnalysis.PROP_FILE_COPYRIGHT);
			fileResource.addProperty(p, this.getCopyright());	
		}

		//artifactof
		if (this.artifactOf != null) {
			p = model.createProperty(SPDXAnalysis.SPDX_NAMESPACE, SPDXAnalysis.PROP_FILE_ARTIFACTOF);
			for (int i = 0; i < artifactOf.length; i++) {
				// we need to check on these if it already exists
				Resource projectResource = null;
				String uri = artifactOf[i].getProjectUri();
				if (uri != null) {
					projectResource = model.createResource(uri);
				} else {
					projectResource = artifactOf[i].createResource(model);
				}
				fileResource.addProperty(p, projectResource);
			}
		}
		
		this.model = model;
		this.node = fileResource.asNode();
	}
	public SPDXFile(String name, String type, String sha1,
			SPDXLicenseInfo[] fileLicenses,
			SPDXLicenseInfo[] seenLicenses, String licenseComments,
			String copyright, DOAPProject[] artifactOf) {
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
	public SPDXLicenseInfo[] getSeenLicenses() {
		return seenLicenses;
	}
	/**
	 * @param seenLicenses the seenLicenses to set
	 */
	public void setSeenLicenses(SPDXLicenseInfo[] seenLicenses) {
		this.seenLicenses = seenLicenses;
		if (this.model != null && this.node != null) {
			Property p = model.createProperty(SPDXAnalysis.SPDX_NAMESPACE, SPDXAnalysis.PROP_FILE_SEEN_LICENSE);
			Resource fileResource = model.createResource(node.getURI());
			model.removeAll(fileResource, p, null);
			p = model.createProperty(SPDXAnalysis.SPDX_NAMESPACE, SPDXAnalysis.PROP_FILE_SEEN_LICENSE);

			for (int i = 0; i < seenLicenses.length; i++) {
				Resource lic = seenLicenses[i].createResource(model);
				fileResource.addProperty(p, lic);
			}
		}
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
		if (this.model != null && this.node != null) {
			Property p = model.createProperty(SPDXAnalysis.SPDX_NAMESPACE, SPDXAnalysis.PROP_FILE_LIC_COMMENTS);
			Resource fileResource = model.createResource(node.getURI());
			model.removeAll(fileResource, p, null);
			p = model.createProperty(SPDXAnalysis.SPDX_NAMESPACE, SPDXAnalysis.PROP_FILE_LIC_COMMENTS);
			fileResource.addProperty(p, this.getLicenseComments());
		}	
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
		if (this.model != null && this.node != null) {
			Property p = model.createProperty(SPDXAnalysis.SPDX_NAMESPACE, SPDXAnalysis.PROP_FILE_COPYRIGHT);
			Resource fileResource = model.createResource(node.getURI());
			model.removeAll(fileResource, p, null);
			p = model.createProperty(SPDXAnalysis.SPDX_NAMESPACE, SPDXAnalysis.PROP_FILE_COPYRIGHT);
			fileResource.addProperty(p, this.getCopyright());
		}	
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
		if (this.model != null && this.node != null) {
			Property p = model.createProperty(SPDXAnalysis.SPDX_NAMESPACE, SPDXAnalysis.PROP_FILE_NAME);
			Resource fileResource = model.createResource(node.getURI());
			model.removeAll(fileResource, p, null);
			p = model.createProperty(SPDXAnalysis.SPDX_NAMESPACE, SPDXAnalysis.PROP_FILE_NAME);
			fileResource.addProperty(p, this.getName());
		}
	}
	/**
	 * @return the fileLicenses
	 */
	public SPDXLicenseInfo[] getFileLicenses() {
		return this.fileLicenses;
	}
	/**
	 * @param fileLicenses the fileLicenses to set
	 */
	public void setFileLicenses(SPDXLicenseInfo[] fileLicenses) {
		this.fileLicenses = fileLicenses;
		if (this.model != null && this.node != null) {
			Property p = model.createProperty(SPDXAnalysis.SPDX_NAMESPACE, SPDXAnalysis.PROP_FILE_LICENSE);
			Resource fileResource = model.createResource(node.getURI());
			model.removeAll(fileResource, p, null);
			p = model.createProperty(SPDXAnalysis.SPDX_NAMESPACE, SPDXAnalysis.PROP_FILE_LICENSE);

			for (int i = 0; i < fileLicenses.length; i++) {
				Resource lic = fileLicenses[i].createResource(model);
				fileResource.addProperty(p, lic);
			}
		}
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
		if (this.model != null && this.node != null) {
			Property p = model.createProperty(SPDXAnalysis.SPDX_NAMESPACE, SPDXAnalysis.PROP_FILE_CHECKSUM);
			Resource fileResource = model.createResource(node.getURI());
			model.removeAll(fileResource, p, null);
			SPDXChecksum cksum = new SPDXChecksum(SPDXChecksum.ALGORITHM_SHA1, sha1);
			Resource cksumResource = cksum.createResource(model);

			p = model.createProperty(SPDXAnalysis.SPDX_NAMESPACE, SPDXAnalysis.PROP_FILE_CHECKSUM);
			fileResource.addProperty(p, cksumResource);
		}
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
		if (this.model != null && this.node != null) {
			Property p = model.createProperty(SPDXAnalysis.SPDX_NAMESPACE, SPDXAnalysis.PROP_FILE_TYPE);
			Resource fileResource = model.createResource(node.getURI());
			model.removeAll(fileResource, p, null);
			p = model.createProperty(SPDXAnalysis.SPDX_NAMESPACE, SPDXAnalysis.PROP_FILE_TYPE);
			fileResource.addProperty(p, this.getType());
		}
	}

	/**
	 * @return the artifactOf
	 */
	public DOAPProject[] getArtifactOf() {
		return artifactOf;
	}

	/**
	 * @param artifactOf the artifactOf to set
	 */
	public void setArtifactOf(DOAPProject[] artifactOf) {
		this.artifactOf = artifactOf;
		if (this.model != null && this.name != null) {
			Resource fileResource = model.createResource(node.getURI());
			Property p = model.createProperty(SPDXAnalysis.SPDX_NAMESPACE, SPDXAnalysis.PROP_FILE_ARTIFACTOF);
			model.removeAll(fileResource, p, null);
			p = model.createProperty(SPDXAnalysis.SPDX_NAMESPACE, SPDXAnalysis.PROP_FILE_ARTIFACTOF);
			for (int i = 0; i < artifactOf.length; i++) {
				// we need to check on these if it already exists
				Resource projectResource = null;
				String uri = artifactOf[i].getProjectUri();
				if (uri != null) {
					projectResource = model.createResource(uri);
				} else {
					projectResource = artifactOf[i].createResource(model);
				}
				fileResource.addProperty(p, projectResource);
			}
		}

	}
}
