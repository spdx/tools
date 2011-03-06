/**
 * Copyright (c) 2010, 2011 Source Auditor Inc.
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

import org.apache.poi.ss.usermodel.Sheet;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * 
 * Simple model for the SPDX Document.  The document is stored in a Jena RDF
 * model which can be accessed through the model property.
 * 
 * The class can be constructed using an already populated Jenna model.  It can
 * also be constructed with a blank model and the SPDX document constructed using
 * the "set" methods.
 * 
 * The createSpdxDocument(uri) must be called first for a blank model
 * 
 * The license, file, and package objects can be constructed then added to the model
 * by using the set functions.
 * 
 * @author Gary O'Neall
 *
 */
public class SPDXDocument {
	static final String RDF_NAMESPACE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	static final String RDF_PROP_TYPE = "type";
	static final String RDF_PROP_RESOURCE = "resource";
	
	static final String SPDX_NAMESPACE = "http://spdx.org/ont#";
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
	static final String PROP_PACKAGE_URL = "DownloadURL";
	static final String PROP_PACKAGE_FILE_CHECKSUMS = "FileChecksums";	//TODO: Update with correct property name
	
	static final String PROP_LICENSE_ID = "LicenseID";
	static final String PROP_LICENSE_TEXT = "LicenseText";
	static final String PROP_DISJUNCTIVE_LICENSE = "DisjunctiveLicense";
	
	static final String PROP_FILE_NAME = "FileName"; 	//TODO: Resolve inconsistency with document - uses file/name; examples usese FileName
	static final String PROP_FILE_TYPE = "FileType"; 	//TODO: Resolve inconsistency w/Doc - File/Type vs. FileType
	static final String PROP_FILE_LICENSE = "FileLicense";	//TODO: Resolve inconsistency w/Doc - File/License vs. FileLicense
	static final String PROP_FILE_COPYRIGHT = "FileCopyright"; 	//TODO: Resolve inconsistency w/Doc - File/Copyright vs FileCopyright
	static final String PROP_FILE_SHA1 = "SHA1";
	public static final String PROP_FILE_SEEN_LICENSE = "SeenLicense";	//TODO: Update with official field 
	public static final String PROP_FILE_LIC_COMMENTS = "LicenseComments";	//TODO: Update with offical field
	
	/**
	 * Simple class representing an SPDX Package.  This is stored in an RDF
	 * model.
	 * 
	 * This package is initialized using an existing SPDXPackage in an 
	 * RDF document by constructing the package with the node representing the
	 * SPDX package.

	 * 
	 * @author Gary O'Neall
	 *
	 */
	public class SPDXPackage {
		private Node node = null;
		/**
		 * Construct a new SPDX package and populate the properties from the node
		 * @param pkgNode Node in the RDF graph representing the SPDX package
		 */
		public SPDXPackage(Node pkgNode) {
			this.node = pkgNode;
			//TODO: Validate parsed properties
		}
		/**
		 * @return the declaredName
		 * @throws InvalidSPDXDocException 
		 */
		public String getDeclaredName() throws InvalidSPDXDocException {
			String[] declaredNames = findDocPropertieStringValues(this.node, PROP_PACKAGE_DECLARED_NAME);
			if (declaredNames == null || declaredNames.length == 0) {
				return null;
			}
			if (declaredNames.length > 1) {
				throw(new InvalidSPDXDocException("More than one declared name for a package"));
			}
			return(declaredNames[0]);
		}

		/**
		 * @param declaredName the declaredName to set
		 */
		public void setDeclaredName(String declaredName) {
			removeProperties(node, PROP_PACKAGE_DECLARED_NAME);
			addProperty(node, PROP_PACKAGE_DECLARED_NAME, new String[] {declaredName});
		}
		/**
		 * @return the fileName
		 * @throws InvalidSPDXDocException 
		 */
		public String getFileName() throws InvalidSPDXDocException {
			String[] fileNames = findDocPropertieStringValues(this.node, PROP_PACKAGE_FILE_NAME);
			if (fileNames == null || fileNames.length == 0) {
				return null;
			}
			if (fileNames.length > 1) {
				throw(new InvalidSPDXDocException("More than one machine name for a package"));
			}
			return fileNames[0];
		}
		/**
		 * @param fileName the fileName to set
		 */
		public void setFileName(String fileName) {
			removeProperties(node, PROP_PACKAGE_FILE_NAME);
			addProperty(node, PROP_PACKAGE_FILE_NAME, new String[] {fileName});
		}
		/**
		 * @return the sha1
		 * @throws InvalidSPDXDocException 
		 */
		public String getSha1() throws InvalidSPDXDocException {
			String[] sha1s = findDocPropertieStringValues(this.node, PROP_PACKAGE_SHA1);
			if (sha1s == null || sha1s.length == 0) {
				return null;
			}
			if (sha1s.length > 1) {
				throw(new InvalidSPDXDocException("More than one sha1 for a package"));
			}
			return(sha1s[0]);
		}
		/**
		 * @param sha1 the sha1 to set
		 */
		public void setSha1(String sha1) {
			removeProperties(node, PROP_PACKAGE_SHA1);
			addProperty(node, PROP_PACKAGE_SHA1, new String[] {sha1});
		}
		/**
		 * @return the sourceInfo
		 * @throws InvalidSPDXDocException 
		 */
		public String getSourceInfo() throws InvalidSPDXDocException {
			String[] sourceInfos = findDocPropertieStringValues(this.node, PROP_PACKAGE_SOURCE_INFO);
			if (sourceInfos == null || sourceInfos.length == 0) {
				return null;
			}
			if (sourceInfos.length > 1) {
				throw(new InvalidSPDXDocException("More than one source info for an SPDX package"));
			}
			return sourceInfos[0];
		}
		/**
		 * @param sourceInfo the sourceInfo to set
		 */
		public void setSourceInfo(String sourceInfo) {
			removeProperties(node, PROP_PACKAGE_SOURCE_INFO);
			addProperty(node, PROP_PACKAGE_SOURCE_INFO, new String[] {sourceInfo});
		}
		/**
		 * @return the declaredLicenses
		 */
		public LicenseDeclaration[] getDeclaredLicenses() {
			ArrayList<LicenseDeclaration> alLic = new ArrayList<LicenseDeclaration>();
			Node p = model.getProperty(SPDX_NAMESPACE, PROP_PACKAGE_DECLARED_LICENSE).asNode();
			Triple m = Triple.createMatch(this.node, p, null);
			ExtendedIterator<Triple> tripleIter = model.getGraph().find(m);	
			while (tripleIter.hasNext()) {
				Triple t = tripleIter.next();
				alLic.add(new LicenseDeclaration(t.getObject(), model));
			}
			LicenseDeclaration[] retval = new LicenseDeclaration[alLic.size()];
			retval = alLic.toArray(retval);
			return retval;
		}
		/**
		 * @param declaredLicenses the declaredLicenses to set
		 */
		public void setDeclaredLicenses(LicenseDeclaration[] declaredLicenses) {
			removeProperties(node, PROP_PACKAGE_DECLARED_LICENSE);
			Resource s = model.getResource(this.node.getURI());
			for (int i = 0; i < declaredLicenses.length; i++) {
				Property p = model.createProperty(SPDX_NAMESPACE, PROP_PACKAGE_DECLARED_LICENSE);
				Resource lic = model.createResource(p);
				s.addProperty(p, lic);
				declaredLicenses[i].populateModel(lic, model);
			}
		}
		/**
		 * @return the detectedLicenses
		 */
		public LicenseDeclaration[] getDetectedLicenses() {
			ArrayList<LicenseDeclaration> alLic = new ArrayList<LicenseDeclaration>();
			Node p = model.getProperty(SPDX_NAMESPACE, PROP_PACKAGE_DETECTED_LICENSE).asNode();
			Triple m = Triple.createMatch(this.node, p, null);
			ExtendedIterator<Triple> tripleIter = model.getGraph().find(m);	
			while (tripleIter.hasNext()) {
				Triple t = tripleIter.next();
				alLic.add(new LicenseDeclaration(t.getObject(), model));
			}
			LicenseDeclaration[] retval = new LicenseDeclaration[alLic.size()];
			retval = alLic.toArray(retval);
			return retval;
		}
		/**
		 * @param detectedLicenses the detectedLicenses to set
		 */
		public void setDetectedLicenses(LicenseDeclaration[] detectedLicenses) {
			removeProperties(node, PROP_PACKAGE_DETECTED_LICENSE);
			Resource s = model.getResource(this.node.getURI());
			for (int i = 0; i < detectedLicenses.length; i++) {
				Property p = model.createProperty(SPDX_NAMESPACE, PROP_PACKAGE_DETECTED_LICENSE);
				Resource lic = model.createResource(p);
				s.addProperty(p, lic);
				detectedLicenses[i].populateModel(lic, model);
			}
		}
		/**
		 * @return the declaredCopyright
		 * @throws InvalidSPDXDocException 
		 */
		public String getDeclaredCopyright() throws InvalidSPDXDocException {
			String[] copyrights = findDocPropertieStringValues(this.node, PROP_PACKAGE_DECLARED_COPYRIGHT);
			if (copyrights == null || copyrights.length == 0) {
				return null;
			}
			if (copyrights.length > 1) {
				throw(new InvalidSPDXDocException("More than one declared copyright for a package"));
			}
			return(copyrights[0]);
		}
		/**
		 * @param declaredCopyright the declaredCopyright to set
		 */
		public void setDeclaredCopyright(String declaredCopyright) {
			removeProperties(node, PROP_PACKAGE_DECLARED_COPYRIGHT);
			addProperty(node, PROP_PACKAGE_DECLARED_COPYRIGHT, new String[] {declaredCopyright});
		}
		/**
		 * @return the shortDescription
		 * @throws InvalidSPDXDocException 
		 */
		public String getShortDescription() throws InvalidSPDXDocException {
			String[] shortDescs = findDocPropertieStringValues(this.node, PROP_PACKAGE_SHORT_DESC);
			if (shortDescs == null || shortDescs.length == 0) {
				return null;
			}
			if (shortDescs.length > 1) {
				throw(new InvalidSPDXDocException("More than one short description for a package"));
			}
			return(shortDescs[0]);
		}
		/**
		 * @param shortDescription the shortDescription to set
		 */
		public void setShortDescription(String shortDescription) {
			removeProperties(node, PROP_PACKAGE_SHORT_DESC);
			addProperty(node, PROP_PACKAGE_SHORT_DESC, new String[] {shortDescription});
		}
		/**
		 * @return the description
		 * @throws InvalidSPDXDocException 
		 */
		public String getDescription() throws InvalidSPDXDocException {
			String[] desc = findDocPropertieStringValues(this.node, PROP_PACKAGE_DESCRIPTION);
			if (desc == null || desc.length == 0) {
				return null;
			}
			if (desc.length > 1) {
				throw(new InvalidSPDXDocException("More than one description for a package"));
			}
			return(desc[0]);
		}
		/**
		 * @param description the description to set
		 */
		public void setDescription(String description) {
			removeProperties(node, PROP_PACKAGE_DESCRIPTION);
			addProperty(node, PROP_PACKAGE_DESCRIPTION, new String[] {description});
		}
		/**
		 * @return the files
		 */
		public SPDXFile[] getFiles() {
			// files
			ArrayList<SPDXFile> alFiles = new ArrayList<SPDXFile>();
			Node p = model.getProperty(SPDX_NAMESPACE, PROP_PACKAGE_FILE).asNode();
			Triple m = Triple.createMatch(this.node, p, null);
			ExtendedIterator<Triple> tripleIter = model.getGraph().find(m);	
			while (tripleIter.hasNext()) {
				Triple t = tripleIter.next();
				alFiles.add(new SPDXFile(t.getObject(), model));
			}
			SPDXFile[] retval = new SPDXFile[alFiles.size()];
			return alFiles.toArray(retval);
		}
		/**
		 * @param files the files to set
		 */
		public void setFiles(SPDXFile[] files) {
			removeProperties(node, PROP_PACKAGE_FILE);
			Resource s = model.getResource(this.node.getURI());
			for (int i = 0; i < files.length; i++) {
				Property p = model.createProperty(SPDX_NAMESPACE, PROP_PACKAGE_FILE);
				Resource file = model.createResource(p);
				s.addProperty(p, file);
				files[i].populateModel(file, model);
			}
		}
		
		public String getUrl() throws InvalidSPDXDocException {
			String[] urls = findDocPropertieStringValues(this.node, PROP_PACKAGE_URL);
			if (urls == null || urls.length == 0) {
				return null;
			}
			if (urls.length > 1) {
				throw(new InvalidSPDXDocException("More than one URL for a package"));
			}
			return(urls[0]);
		}
		
		public void setUrl(String url) {
			removeProperties(node, PROP_PACKAGE_URL);
			addProperty(node, PROP_PACKAGE_URL, new String[] {url});
		}
		
		public String getFileChecksums() throws InvalidSPDXDocException {
			String[] cksums = findDocPropertieStringValues(this.node, PROP_PACKAGE_FILE_CHECKSUMS);
			if (cksums == null || cksums.length == 0) {
				return null;
			}
			if (cksums.length > 1) {
				throw(new InvalidSPDXDocException("More than one file checksums for a package"));
			}
			return(cksums[0]);
		}
		
		public void setFileChecksums(String fileChecksums) {
			removeProperties(node, PROP_PACKAGE_FILE_CHECKSUMS);
			addProperty(node, PROP_PACKAGE_FILE_CHECKSUMS, new String[] {fileChecksums});

		}
		public SPDXPackageInfo getPackageInfo() throws InvalidSPDXDocException {
			return new SPDXPackageInfo(this.getDeclaredName(), this.getFileName(), 
					this.getSha1(), this.getSourceInfo(), this.getDeclaredLicenses(), 
					this.getDetectedLicenses(), this.getDeclaredCopyright(), 
					this.getShortDescription(), this.getDescription(), this.getUrl(), 
					this.getFileChecksums());
		}
	}
	

	
	Model model;
	SPDXPackage spdxPackage = null;
	SPDXLicense[] nonStandardLicenses = null;
	
	public SPDXDocument(Model model) throws InvalidSPDXDocException {
		//TODO: Verify cardinality
		this.model = model;
	}
	
	public String verify() {
		return null;
		//TODO: Implement verify
	}
	
	/**
	 * @param propertyName
	 * @return
	 */
	/**
	 * Find all property string values belonging to the subject
	 * @param subject
	 * @param propertyName
	 * @return string values of the properties or null if the subject or propertyName is null
	 */
	private String[] findDocPropertieStringValues(Node subject, String propertyName) {
		if (subject == null || propertyName == null) {
			return null;
		}
		ArrayList<String> alResult = new ArrayList<String>();
		Node p = model.getProperty(SPDX_NAMESPACE, propertyName).asNode();
		Triple m = Triple.createMatch(subject, p, null);
		ExtendedIterator<Triple> tripleIter = model.getGraph().find(m);	
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			alResult.add(t.getObject().toString(false));
		}
		String[] retval = new String[alResult.size()];
		return alResult.toArray(retval);
	}

	/**
	 * @return the spdxVersion or null if there is no SPDX document
	 * @throws InvalidSPDXDocException 
	 */
	public String getSpdxVersion() throws InvalidSPDXDocException {
		String[] versions = findDocPropertieStringValues(getSpdxDocNode(), PROP_SPDX_VERSION);
		if (versions == null || versions.length == 0) {
			return null;
		}
		if (versions.length > 1) {
			throw(new InvalidSPDXDocException("More than one version exists for the SPDX Document"));
		}
		return versions[0];
	}
	
	/**
	 * Remove all properties by the property name from the subject node
	 * @param node
	 * @param propertyName
	 */
	private void removeProperties(Node subject, String propertyName) {
		Property p = model.getProperty(SPDX_NAMESPACE, propertyName);
		Resource s = model.getResource(subject.getURI());
		model.removeAll(s, p, null);
		
	}
	
	private void addProperty(Node subject, String propertyName, String[] propertyValue) {
		Resource s = model.getResource(subject.getURI());
		for (int i = 0; i < propertyValue.length; i++) {
			Property p = model.createProperty(SPDX_NAMESPACE, propertyName);
			s.addProperty(p, propertyValue[i]);
		}
	}

	/**
	 * @param spdxVersion the spdxVersion to set
	 * @throws InvalidSPDXDocException 
	 */
	public void setSpdxVersion(String spdxVersion) throws InvalidSPDXDocException {
		Node spdxDocNode = getSpdxDocNode();
		if (spdxDocNode == null) {
			throw(new InvalidSPDXDocException("Must create the SPDX document before setting spdxVersion"));
		}
		removeProperties(spdxDocNode, PROP_SPDX_VERSION);
		addProperty(spdxDocNode, PROP_SPDX_VERSION, new String[] {spdxVersion});
	}

	/**
	 * @return the createdBy
	 * @throws InvalidSPDXDocException 
	 */
	public String[] getCreatedBy() throws InvalidSPDXDocException {
		// createdBy
		Node spdxDocNode = getSpdxDocNode();
		if (spdxDocNode == null) {
			throw(new InvalidSPDXDocException("The SPDX Document need to be created before accessing createdBy"));
		}
		ArrayList<String> als = new ArrayList<String>();
		Node p = model.getProperty(SPDX_NAMESPACE, PROP_SPDX_CREATED_BY).asNode();
		Triple m = Triple.createMatch(spdxDocNode, p, null);
		ExtendedIterator<Triple> tripleIter = model.getGraph().find(m);	
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			als.add(t.getObject().toString(false));
		}
		String[] createdBy = new String[als.size()];
		createdBy = als.toArray(createdBy);
		return createdBy;
	}

	/**
	 * @param createdBy the createdBy to set
	 * @throws InvalidSPDXDocException 
	 */
	public void setCreatedBy(String[] createdBy) throws InvalidSPDXDocException {
		Node spdxDocNode = getSpdxDocNode();
		if (spdxDocNode == null) {
			throw(new InvalidSPDXDocException("Must create the SPDX document before setting createdBy"));
		}
		// delete the previous createdby's
		Property p = model.getProperty(SPDX_NAMESPACE, PROP_SPDX_CREATED_BY);
		Resource s = model.getResource(getSpdxDocNode().getURI());
		model.removeAll(s, p, null);
		for (int i = 0; i < createdBy.length; i++) {
			p = model.createProperty(SPDX_NAMESPACE, PROP_SPDX_CREATED_BY);
			s.addProperty(p, createdBy[i]);
		}
	}

	/**
	 * @return the reviewers
	 * @throws InvalidSPDXDocException 
	 */
	public String[] getReviewers() throws InvalidSPDXDocException {
		Node spdxDocNode = getSpdxDocNode();
		if (spdxDocNode == null) {
			throw(new InvalidSPDXDocException("Must have an SPDX document to get reviewers"));
		}
		ArrayList<String> als = new ArrayList<String>();
		als.clear();
		Node p = model.getProperty(SPDX_NAMESPACE, PROP_SPDX_REVIEWED_BY).asNode();
		Triple m = Triple.createMatch(spdxDocNode, p, null);
		ExtendedIterator<Triple >tripleIter = model.getGraph().find(m);	
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			als.add(t.getObject().toString(false));
		}
		String[] reviewers = new String[als.size()];
		reviewers = als.toArray(reviewers);
		return reviewers;
	}

	/**
	 * @param reviewers the reviewers to set
	 * @throws InvalidSPDXDocException 
	 */
	public void setReviewers(String[] reviewers) throws InvalidSPDXDocException {
		Node spdxDocNode = getSpdxDocNode();
		if (spdxDocNode == null) {
			throw(new InvalidSPDXDocException("Must have an SPDX document to set reviewers"));
		}
		// delete any previous created
		Property p = model.getProperty(SPDX_NAMESPACE, PROP_SPDX_REVIEWED_BY);
		Resource s = model.getResource(spdxDocNode.getURI());
		model.removeAll(s, p, null);
		// add the property
		for (int i = 0; i < reviewers.length; i++) {
			p = model.createProperty(SPDX_NAMESPACE, PROP_SPDX_REVIEWED_BY);
			s.addProperty(p, reviewers[i]);
		}
	}

	/**
	 * @return the created
	 * @throws InvalidSPDXDocException 
	 */
	public String getCreated() throws InvalidSPDXDocException {
		String created = null;
		Node spdxDocNode = getSpdxDocNode();
		if (spdxDocNode == null) {
			throw(new InvalidSPDXDocException("No SPDX Document."));
		}
		Node p = model.getProperty(SPDX_NAMESPACE, PROP_SPDX_CREATED).asNode();
		Triple m = Triple.createMatch(spdxDocNode, p, null);
		ExtendedIterator<Triple> tripleIter = model.getGraph().find(m);	
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			created = t.getObject().toString(false);
		}
		return created;
	}

	/**
	 * @param created the created to set
	 * @throws InvalidSPDXDocException 
	 */
	public void setCreated(String created) throws InvalidSPDXDocException {
		Node spdxDocNode = getSpdxDocNode();
		if (spdxDocNode == null) {
			throw(new InvalidSPDXDocException("Must create the SPDX document before setting created"));
		}
		// delete any previous created
		Property p = model.getProperty(SPDX_NAMESPACE, PROP_SPDX_CREATED);
		Resource s = model.getResource(spdxDocNode.getURI());
		model.removeAll(s, p, null);
		// add the property
		p = model.createProperty(SPDX_NAMESPACE, PROP_SPDX_CREATED);
		s.addProperty(p, created);
	}

	/**
	 * @return the spdxPackage
	 * @throws InvalidSPDXDocException 
	 */
	public SPDXPackage getSpdxPackage() throws InvalidSPDXDocException {
		if (this.spdxPackage != null) {
			return this.spdxPackage;
		}
		Node spdxDocNode = getSpdxDocNode();
		if (spdxDocNode == null) {
			throw(new InvalidSPDXDocException("Must set an SPDX doc before getting an SPDX package"));
		}
		Node p = model.getProperty(SPDX_NAMESPACE, PROP_SPDX_PACKAGE).asNode();
		Triple m = Triple.createMatch(spdxDocNode, p, null);
		ExtendedIterator<Triple> tripleIter = model.getGraph().find(m);	
		SPDXPackage newSpdxPackage = null;
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			newSpdxPackage = new SPDXPackage(t.getObject());
		}
		this.spdxPackage = newSpdxPackage;
		return newSpdxPackage;
	}

	/**
	 * Creates an empty SPDX package
	 * @param uri Unique URI representing the SPDX package
	 * @throws InvalidSPDXDocException 
	 */
	public void createSpdxPackage(String uri) throws InvalidSPDXDocException {
		Node spdxDocNode = getSpdxDocNode();
		if (spdxDocNode == null) {
			throw(new InvalidSPDXDocException("Must create the SPDX document before creating an SPDX Package"));
		}
		// delete the previous SPDX package
		Property p = model.getProperty(SPDX_NAMESPACE, PROP_SPDX_PACKAGE);
		Resource s = model.getResource(getSpdxDocNode().getURI());
		model.removeAll(s, p, null);
		p = model.createProperty(SPDX_NAMESPACE, PROP_SPDX_PACKAGE);
		Resource spdxPkg = model.createResource(uri, p);
		s.addProperty(p, spdxPkg);
		this.spdxPackage = new SPDXPackage(spdxPkg.asNode());
	}
	
	public void createSpdxPackage() throws InvalidSPDXDocException {
		// generate a unique URI by appending a "?package" to the end of the doc URI
		Node spdxDocNode = getSpdxDocNode();
		if (spdxDocNode == null) {
			throw(new InvalidSPDXDocException("Must create the SPDX document before creating an SPDX Package"));
		}
		createSpdxPackage(spdxDocNode.getURI()+"?package");
	}

	/**
	 * @return the nonStandardLicenses
	 * @throws InvalidSPDXDocException 
	 */
	public SPDXLicense[] getNonStandardLicenses() throws InvalidSPDXDocException {
		// nonStandardLicenses
		Node spdxDocNode = getSpdxDocNode();
		if (spdxDocNode == null) {
			throw(new InvalidSPDXDocException("No SPDX Document - can not get the Non Standard Licenses"));
		}
		ArrayList<SPDXLicense> alLic = new ArrayList<SPDXLicense>();
		Node p = model.getProperty(SPDX_NAMESPACE, PROP_SPDX_NONSTANDARD_LICENSES).asNode();
		Triple m = Triple.createMatch(spdxDocNode, p, null);
		ExtendedIterator<Triple> tripleIter = model.getGraph().find(m);	
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			alLic.add(new SPDXLicense(model, t.getObject()));
		}
		SPDXLicense[] nonStandardLicenses = new SPDXLicense[alLic.size()];
		nonStandardLicenses = alLic.toArray(nonStandardLicenses);
		return nonStandardLicenses;
	}

	/**
	 * @param nonStandardLicenses the nonStandardLicenses to set
	 * @throws InvalidSPDXDocException 
	 */
	public void setNonStandardLicenses(SPDXLicense[] nonStandardLicenses) throws InvalidSPDXDocException {
		Node spdxDocNode = getSpdxDocNode();
		if (spdxDocNode == null) {
			throw(new InvalidSPDXDocException("Must create the SPDX document before setting Non-Standard Licenses"));
		}
		// delete the previous createdby's
		Property p = model.getProperty(SPDX_NAMESPACE, PROP_SPDX_NONSTANDARD_LICENSES);
		Resource s = model.getResource(getSpdxDocNode().getURI());
		model.removeAll(s, p, null);
		for (int i = 0; i < nonStandardLicenses.length; i++) {
			p = model.createProperty(SPDX_NAMESPACE, PROP_SPDX_NONSTANDARD_LICENSES);
			s.addProperty(p, nonStandardLicenses[i].createResource(model));
		}
	}

	/**
	 * @return the URI of the SPDX Document
	 */
	public String getSpdxDocUri() {
		// populate the model
		Node spdxDocNode = getSpdxDocNode();
		if (spdxDocNode == null) {
			return null;
		}
		return spdxDocNode.toString(false);
	}
	
	/**
	 * @return the model
	 */
	public Model getModel() {
		return model;
	}

	/**
	 * @param model the model to set
	 */
	public void setModel(Model model) {
		this.model = model;
	}

	/**
	 * Creates a new empty SPDX Document.
	 * Note: Any previous SPDX documents will be deleted from the model to
	 * preserve the one and only one constraint.
	 * Note: Follow-up calls MUST be made to add the required properties for this
	 * to be a valid SPDX document
	 * @param uri URI for the SPDX Document
	 */
	public void createSpdxDocument(String uri) {
		Node spdxDocNode = getSpdxDocNode();
		if (spdxDocNode != null) {
			// delete
			model.removeAll();
		}
		model.setNsPrefix("", SPDX_NAMESPACE);
		Property spdxDocProperty = model.createProperty(SPDX_NAMESPACE, PROP_SPDX_DOC);
		model.createResource(uri, spdxDocProperty);
	}
	
	/**
	 * @return the spdx doc node from the model
	 */
	private Node getSpdxDocNode() {
		Node spdxDocNode = null;
		Node rdfTypePredicate = this.model.getProperty(RDF_NAMESPACE, RDF_PROP_TYPE).asNode();
		Node spdxDocObject = this.model.getProperty(SPDX_NAMESPACE, PROP_SPDX_DOC).asNode();
		Triple m = Triple.createMatch(null, rdfTypePredicate, spdxDocObject);
		ExtendedIterator<Triple> tripleIter = model.getGraph().find(m);	// find the document
		while (tripleIter.hasNext()) {
			Triple docTriple = tripleIter.next();
			spdxDocNode = docTriple.getSubject();
		}
		return spdxDocNode;
	}

	public void setAuthorsComments(Sheet sheet) {
		// TODO Implment authors comments
		
	}
}
