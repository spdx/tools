/**
 * Copyright (c) 2010 Source Auditor Inc.
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
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * 
 * Simple model for the SPDX Document
 * @author Gary O'Neall
 *
 */
public class SPDXDocument {
	static final String RDF_NAMESPACE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	static final String RDF_PROP_TYPE = "type";
	static final String RDF_PROP_RESOURCE = "resource";
	
	static final String SPDX_NAMESPACE = "http://spdx.org/ont/#";
	static final String PROP_SPDX_DOC = "SPDXDoc";
	static final String PROP_SPDX_VERSION = "SPDXVersion";
	static final String PROP_SPDX_CREATED_BY = "CreatedBy";
	static final String PROP_SPDX_CREATED = "Created";	// creation timestamp
	static final String PROP_SPDX_REVIEWED_BY = "ReviewedBy";
	static final String PROP_SPDX_NONSTANDARD_LICENSES = "hasNonStandardLicense";
	
	static final String PROP_SPDX_PACKAGE = "DescribesPackage";
	static final String PROP_PACKAGE_DECLARED_NAME = "DeclaredName";
	static final String PROP_PACKAGE_FILE_NAME = "MachineName";	//TODO: Resolve inconsistency w/example - uses FileName, standard uses MachineName
	static final String PROP_PACKAGE_SHA1 = "SHA1";
	static final String PROP_PACKAGE_DOWNLOAD_URL = "URL";
	static final String PROP_PACKAGE_SOURCE_INFO = "SourceInfo";
	static final String PROP_PACKAGE_DECLARED_LICENSE = "DeclaredLicense";
	static final String PROP_PACKAGE_DETECTED_LICENSE = "DetectedLicense";
	static final String PROP_PACKAGE_DISJUNCTIVE_LICENSE = "DisjunctiveLicense";
	static final String PROP_PACKAGE_DECLARED_COPYRIGHT = "DeclaredCopyright";
	static final String PROP_PACKAGE_SHORT_DESC = "ShortDesc";
	static final String PROP_PACKAGE_DESCRIPTION = "Description";
	static final String PROP_PACKAGE_FILE = "hasFile";
	
	static final String PROP_LICENSE_ID = "LicenseID";
	static final String PROP_LICENSE_TEXT = "LicenseText";
	static final String PROP_DISJUNCTIVE_LICENSE = "DisjunctiveLicense";
	
	static final String PROP_FILE_NAME = "FileName"; 	//TODO: Resolve inconsistency with document - uses file/name; examples usese FileName
	static final String PROP_FILE_TYPE = "FileType"; 	//TODO: Resolve inconsistency w/Doc - File/Type vs. FileType
	static final String PROP_FILE_LICENSE = "FileLicense";	//TODO: Resolve inconsistency w/Doc - File/License vs. FileLicense
	static final String PROP_FILE_COPYRIGHT = "FileCopyright"; 	//TODO: Resolve inconsistency w/Doc - File/Copyright vs FileCopyright
	static final String PROP_FILE_SHA1 = "SHA1"; 
	
	private String spdxVersion;
	private String[] createdBy;
	private String[] reviewers;
	private String created; 							// creation timestamp
	//TODO: Change created to Date and write a dateformatter for YYYY-MM-DDThh:mm:ssZ
	SPDXPackage spdxPackage;
	SPDXLicense[] nonStandardLicenses;

	class LicenseDeclaration {

		private String name;
		private String[] disjunctiveLicenses;
		
		/**
		 * Construct a new license declaration and populate the properties based on the licenseNode
		 * @param licenseNode Node in the RDF graph representing the license declaration
		 */
		public LicenseDeclaration(Node licenseNode) {
			// name
			this.name = licenseNode.toString(false);
			// disjunctiveLicenses
			Node p = model.getProperty(SPDX_NAMESPACE, PROP_DISJUNCTIVE_LICENSE).asNode();
			Triple m = Triple.createMatch(licenseNode, p, null);
			ExtendedIterator<Triple> tripleIter = model.getGraph().find(m);
			ArrayList<String> als = new ArrayList<String>();
			while (tripleIter.hasNext()) {
				Triple t = tripleIter.next();
				als.add(t.getObject().toString(false));
			}
			this.disjunctiveLicenses = als.toArray(new String[als.size()]);
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
		 * @return the disjunctiveLicenses
		 */
		public String[] getDisjunctiveLicenses() {
			return this.disjunctiveLicenses;
		}

		/**
		 * @param disjunctiveLicenses the disjunctiveLicenses to set
		 */
		public void setDisjunctiveLicenses(String[] disjunctiveLicenses) {
			this.disjunctiveLicenses = disjunctiveLicenses;
		}
	}
	
	class SPDXPackage {
		private String declaredName;
		private String fileName;
		private String sha1;
		private String sourceInfo;
		private LicenseDeclaration[] declaredLicenses;
		private LicenseDeclaration[] detectedLicenses;
		private String declaredCopyright;
		private String shortDescription;
		private String description;
		private SPDXFile[] files;
		@SuppressWarnings("unused")
		private Node node;
		/**
		 * Construct a new SPDX package and populate the properties from the node
		 * @param pkgNode Node in the RDF graph representing the SPDX package
		 */
		public SPDXPackage(Node pkgNode) {
			this.node = pkgNode;
			//TODO: Validate parsed properties
			// declaredCopyright
			Node p = model.getProperty(SPDX_NAMESPACE, PROP_PACKAGE_DECLARED_COPYRIGHT).asNode();
			Triple m = Triple.createMatch(pkgNode, p, null);
			ExtendedIterator<Triple> tripleIter = model.getGraph().find(m);	
			while (tripleIter.hasNext()) {
				Triple t = tripleIter.next();
				this.declaredCopyright = t.getObject().toString(false);
			}
			// declaredLicenses
			ArrayList<LicenseDeclaration> alLic = new ArrayList<LicenseDeclaration>();
			p = model.getProperty(SPDX_NAMESPACE, PROP_PACKAGE_DECLARED_LICENSE).asNode();
			m = Triple.createMatch(pkgNode, p, null);
			tripleIter = model.getGraph().find(m);	
			while (tripleIter.hasNext()) {
				Triple t = tripleIter.next();
				alLic.add(new LicenseDeclaration(t.getObject()));
			}
			LicenseDeclaration[] decLics = new LicenseDeclaration[alLic.size()];
			decLics = alLic.toArray(decLics);
			this.declaredLicenses = decLics;
			// declaredName
			p = model.getProperty(SPDX_NAMESPACE, PROP_PACKAGE_DECLARED_NAME).asNode();
			m = Triple.createMatch(pkgNode, p, null);
			tripleIter = model.getGraph().find(m);	
			while (tripleIter.hasNext()) {
				Triple t = tripleIter.next();
				this.declaredName = t.getObject().toString(false);
			}
			// description
			p = model.getProperty(SPDX_NAMESPACE, PROP_PACKAGE_DESCRIPTION).asNode();
			m = Triple.createMatch(pkgNode, p, null);
			tripleIter = model.getGraph().find(m);	
			while (tripleIter.hasNext()) {
				Triple t = tripleIter.next();
				this.description = t.getObject().toString(false);
			}
			// detectedLicenses
			alLic = new ArrayList<LicenseDeclaration>();
			p = model.getProperty(SPDX_NAMESPACE, PROP_PACKAGE_DETECTED_LICENSE).asNode();
			m = Triple.createMatch(pkgNode, p, null);
			tripleIter = model.getGraph().find(m);	
			while (tripleIter.hasNext()) {
				Triple t = tripleIter.next();
				alLic.add(new LicenseDeclaration(t.getObject()));
			}
			this.detectedLicenses = new LicenseDeclaration[alLic.size()];
			this.detectedLicenses = alLic.toArray(this.detectedLicenses);
			// fileName
			p = model.getProperty(SPDX_NAMESPACE, PROP_PACKAGE_FILE_NAME).asNode();
			m = Triple.createMatch(pkgNode, p, null);
			tripleIter = model.getGraph().find(m);	
			while (tripleIter.hasNext()) {
				Triple t = tripleIter.next();
				this.fileName = t.getObject().toString(false);
			}
			// files
			ArrayList<SPDXFile> alFiles = new ArrayList<SPDXFile>();
			p = model.getProperty(SPDX_NAMESPACE, PROP_PACKAGE_FILE).asNode();
			m = Triple.createMatch(pkgNode, p, null);
			tripleIter = model.getGraph().find(m);	
			while (tripleIter.hasNext()) {
				Triple t = tripleIter.next();
				alFiles.add(new SPDXFile(t.getObject()));
			}
			this.files = alFiles.toArray(new SPDXFile[alFiles.size()]);
			// sha1
			p = model.getProperty(SPDX_NAMESPACE, PROP_PACKAGE_SHA1).asNode();
			m = Triple.createMatch(pkgNode, p, null);
			tripleIter = model.getGraph().find(m);	
			while (tripleIter.hasNext()) {
				Triple t = tripleIter.next();
				this.sha1 = t.getObject().toString(false);
			}
			// shortDescription
			p = model.getProperty(SPDX_NAMESPACE, PROP_PACKAGE_SHORT_DESC).asNode();
			m = Triple.createMatch(pkgNode, p, null);
			tripleIter = model.getGraph().find(m);	
			while (tripleIter.hasNext()) {
				Triple t = tripleIter.next();
				this.shortDescription = t.getObject().toString(false);
			}
			// sourceInfo
			p = model.getProperty(SPDX_NAMESPACE, PROP_PACKAGE_SOURCE_INFO).asNode();
			m = Triple.createMatch(pkgNode, p, null);
			tripleIter = model.getGraph().find(m);	
			while (tripleIter.hasNext()) {
				Triple t = tripleIter.next();
				this.sourceInfo = t.getObject().toString(false);
			}
		}
		/**
		 * @return the declaredName
		 */
		public String getDeclaredName() {
			return this.declaredName;
		}
		/**
		 * @param declaredName the declaredName to set
		 */
		public void setDeclaredName(String declaredName) {
			this.declaredName = declaredName;
		}
		/**
		 * @return the fileName
		 */
		public String getFileName() {
			return this.fileName;
		}
		/**
		 * @param fileName the fileName to set
		 */
		public void setFileName(String fileName) {
			this.fileName = fileName;
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
		 * @return the sourceInfo
		 */
		public String getSourceInfo() {
			return this.sourceInfo;
		}
		/**
		 * @param sourceInfo the sourceInfo to set
		 */
		public void setSourceInfo(String sourceInfo) {
			this.sourceInfo = sourceInfo;
		}
		/**
		 * @return the declaredLicenses
		 */
		public LicenseDeclaration[] getDeclaredLicenses() {
			return this.declaredLicenses;
		}
		/**
		 * @param declaredLicenses the declaredLicenses to set
		 */
		public void setDeclaredLicenses(LicenseDeclaration[] declaredLicenses) {
			this.declaredLicenses = declaredLicenses;
		}
		/**
		 * @return the detectedLicenses
		 */
		public LicenseDeclaration[] getDetectedLicenses() {
			return this.detectedLicenses;
		}
		/**
		 * @param detectedLicenses the detectedLicenses to set
		 */
		public void setDetectedLicenses(LicenseDeclaration[] detectedLicenses) {
			this.detectedLicenses = detectedLicenses;
		}
		/**
		 * @return the declaredCopyright
		 */
		public String getDeclaredCopyright() {
			return this.declaredCopyright;
		}
		/**
		 * @param declaredCopyright the declaredCopyright to set
		 */
		public void setDeclaredCopyright(String declaredCopyright) {
			this.declaredCopyright = declaredCopyright;
		}
		/**
		 * @return the shortDescription
		 */
		public String getShortDescription() {
			return this.shortDescription;
		}
		/**
		 * @param shortDescription the shortDescription to set
		 */
		public void setShortDescription(String shortDescription) {
			this.shortDescription = shortDescription;
		}
		/**
		 * @return the description
		 */
		public String getDescription() {
			return this.description;
		}
		/**
		 * @param description the description to set
		 */
		public void setDescription(String description) {
			this.description = description;
		}
		/**
		 * @return the files
		 */
		public SPDXFile[] getFiles() {
			return this.files;
		}
		/**
		 * @param files the files to set
		 */
		public void setFiles(SPDXFile[] files) {
			this.files = files;
		}
	}
	
	class SPDXLicense {

		@SuppressWarnings("unused")
		private Node node;
		private String id;
		private String text;
		
		/**
		 * Constructs an SPDX License from the licenseNode
		 * @param licenseNode RDF graph node representing the SPDX License
		 */
		public SPDXLicense(Node licenseNode) {
			this.node = licenseNode;
			// id
			Node p = model.getProperty(SPDX_NAMESPACE, PROP_LICENSE_ID).asNode();
			Triple m = Triple.createMatch(licenseNode, p, null);
			ExtendedIterator<Triple> tripleIter = model.getGraph().find(m);	
			while (tripleIter.hasNext()) {
				Triple t = tripleIter.next();
				this.id = t.getObject().toString(false);
			}
			// text
			p = model.getProperty(SPDX_NAMESPACE, PROP_LICENSE_TEXT).asNode();
			m = Triple.createMatch(licenseNode, p, null);
			tripleIter = model.getGraph().find(m);	
			while (tripleIter.hasNext()) {
				Triple t = tripleIter.next();
				this.text = t.getObject().toString(false);
			}
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
		}
	}
	
	class SPDXFile {
		@SuppressWarnings("unused")
		private Node node;
		private String name;
		private LicenseDeclaration[] fileLicenses;
		private String sha1;
		private String type;
		/**
		 * Construct an SPDX File form the fileNode
		 * @param fileNode RDF Graph node representing the SPDX File
		 */
		public SPDXFile(Node fileNode) {
			this.node = fileNode;
			// name
			Node p = model.getProperty(SPDX_NAMESPACE, PROP_FILE_NAME).asNode();
			Triple m = Triple.createMatch(fileNode, p, null);
			ExtendedIterator<Triple> tripleIter = model.getGraph().find(m);	
			while (tripleIter.hasNext()) {
				Triple t = tripleIter.next();
				this.name = t.getObject().toString(false);
			}
			// sha1
			p = model.getProperty(SPDX_NAMESPACE, PROP_FILE_SHA1).asNode();
			m = Triple.createMatch(fileNode, p, null);
			tripleIter = model.getGraph().find(m);	
			while (tripleIter.hasNext()) {
				Triple t = tripleIter.next();
				this.sha1 = t.getObject().toString(false);
			}
			// type
			p = model.getProperty(SPDX_NAMESPACE, PROP_FILE_TYPE).asNode();
			m = Triple.createMatch(fileNode, p, null);
			tripleIter = model.getGraph().find(m);	
			while (tripleIter.hasNext()) {
				Triple t = tripleIter.next();
				this.type = t.getObject().toString(false);
			}
			// detectedLicense
			ArrayList<LicenseDeclaration> alLic = new ArrayList<LicenseDeclaration>();
			p = model.getProperty(SPDX_NAMESPACE, PROP_FILE_LICENSE).asNode();
			m = Triple.createMatch(fileNode, p, null);
			tripleIter = model.getGraph().find(m);	
			while (tripleIter.hasNext()) {
				Triple t = tripleIter.next();
				alLic.add(new LicenseDeclaration(t.getObject()));
			}
			this.fileLicenses = alLic.toArray(new LicenseDeclaration[alLic.size()]);
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
	
	Model model;
	private String name;
	
	public SPDXDocument(Model model) throws InvalidSPDXDocException {
		//TODO: Verify cardinality
		this.model = model;
		// populate the model
		Node spdxDocNode = null;
		Node rdfTypePredicate = this.model.getProperty(RDF_NAMESPACE, RDF_PROP_TYPE).asNode();
		Node spdxDocObject = this.model.getProperty(SPDX_NAMESPACE, PROP_SPDX_DOC).asNode();
		Triple m = Triple.createMatch(null, rdfTypePredicate, spdxDocObject);
		ExtendedIterator<Triple> tripleIter = model.getGraph().find(m);	// find the document
		while (tripleIter.hasNext()) {
			Triple docTriple = tripleIter.next();
			spdxDocNode = docTriple.getSubject();
		}
		if (spdxDocNode == null) {
			throw(new InvalidSPDXDocException("No SPDX Document Found"));
		}
		this.name = spdxDocNode.toString(false);
		// created
		Node p = model.getProperty(SPDX_NAMESPACE, PROP_SPDX_CREATED).asNode();
		m = Triple.createMatch(spdxDocNode, p, null);
		tripleIter = model.getGraph().find(m);	
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			this.created = t.getObject().toString(false);
		}
		// createdBy
		ArrayList<String> als = new ArrayList<String>();
		p = model.getProperty(SPDX_NAMESPACE, PROP_SPDX_CREATED_BY).asNode();
		m = Triple.createMatch(spdxDocNode, p, null);
		tripleIter = model.getGraph().find(m);	
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			als.add(t.getObject().toString(false));
		}
		this.createdBy = new String[als.size()];
		this.createdBy = als.toArray(this.createdBy);
		// nonStandardLicenses
		ArrayList<SPDXLicense> alLic = new ArrayList<SPDXLicense>();
		p = model.getProperty(SPDX_NAMESPACE, PROP_SPDX_NONSTANDARD_LICENSES).asNode();
		m = Triple.createMatch(spdxDocNode, p, null);
		tripleIter = model.getGraph().find(m);	
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			alLic.add(new SPDXLicense(t.getObject()));
		}
		this.nonStandardLicenses = new SPDXLicense[als.size()];
		this.nonStandardLicenses = alLic.toArray(this.nonStandardLicenses);
		// reviewers
		als.clear();
		p = model.getProperty(SPDX_NAMESPACE, PROP_SPDX_REVIEWED_BY).asNode();
		m = Triple.createMatch(spdxDocNode, p, null);
		tripleIter = model.getGraph().find(m);	
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			als.add(t.getObject().toString(false));
		}
		this.reviewers = new String[als.size()];
		this.reviewers = als.toArray(this.reviewers);
		// spdxPackage
		p = model.getProperty(SPDX_NAMESPACE, PROP_SPDX_PACKAGE).asNode();
		m = Triple.createMatch(spdxDocNode, p, null);
		tripleIter = model.getGraph().find(m);	
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			this.spdxPackage = new SPDXPackage(t.getObject());
		}
		// spdxVersion
		p = model.getProperty(SPDX_NAMESPACE, PROP_SPDX_VERSION).asNode();
		m = Triple.createMatch(spdxDocNode, p, null);
		tripleIter = model.getGraph().find(m);	
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			this.spdxVersion = t.getObject().toString(false);
		}
	}

	/**
	 * @return the spdxVersion
	 */
	public String getSpdxVersion() {
		return this.spdxVersion;
	}

	/**
	 * @param spdxVersion the spdxVersion to set
	 */
	public void setSpdxVersion(String spdxVersion) {
		this.spdxVersion = spdxVersion;
	}

	/**
	 * @return the createdBy
	 */
	public String[] getCreatedBy() {
		return this.createdBy;
	}

	/**
	 * @param createdBy the createdBy to set
	 */
	public void setCreatedBy(String[] createdBy) {
		this.createdBy = createdBy;
	}

	/**
	 * @return the reviewers
	 */
	public String[] getReviewers() {
		return this.reviewers;
	}

	/**
	 * @param reviewers the reviewers to set
	 */
	public void setReviewers(String[] reviewers) {
		this.reviewers = reviewers;
	}

	/**
	 * @return the created
	 */
	public String getCreated() {
		return this.created;
	}

	/**
	 * @param created the created to set
	 */
	public void setCreated(String created) {
		this.created = created;
	}

	/**
	 * @return the spdxPackage
	 */
	public SPDXPackage getSpdxPackage() {
		return this.spdxPackage;
	}

	/**
	 * @param spdxPackage the spdxPackage to set
	 */
	public void setSpdxPackage(SPDXPackage spdxPackage) {
		this.spdxPackage = spdxPackage;
	}

	/**
	 * @return the nonStandardLicenses
	 */
	public SPDXLicense[] getNonStandardLicenses() {
		return this.nonStandardLicenses;
	}

	/**
	 * @param nonStandardLicenses the nonStandardLicenses to set
	 */
	public void setNonStandardLicenses(SPDXLicense[] nonStandardLicenses) {
		this.nonStandardLicenses = nonStandardLicenses;
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
}
