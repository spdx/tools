/**
 * Copyright (c) 2010, 2011 Source Auditor Inc.
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 
 * Simple model for the SPDX Analysis document.  The document is stored in a Jena RDF
 * model which can be accessed through the model property.
 * 
 * The class should be constructed using the SPDXDocumentFactory class
 * 
 * The createSpdxDocument(uri) must be called first for a blank model
 * 
 * The license, file, and package objects can be constructed then added to the model
 * by using the set functions.
 * 
 * The non-standard licenses must contain a unique ID of the form LicenseRef-NN where NN is a
 * unique number.  The method <code>addNonStandardLicense(licenseText)</code> can be called to 
 * create a new unique Non-Standard License.
 * 
 * @author Gary O'Neall
 *
 */
public class SPDXDocument {
	
	// Namespaces
	static final String RDF_NAMESPACE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	static final String RDFS_NAMESPACE = "http://www.w3.org/2000/01/rdf-schema#";
	static final String SPDX_NAMESPACE = "http://spdx.org/rdf/terms#";
	static final String DOAP_NAMESPACE = "http://usefulinc.com/ns/doap#";

	// RDF Properties
	static final String RDF_PROP_TYPE = "type";
	static final String RDF_PROP_RESOURCE = "resource";
	
	// RDFS Properties
	static final String RDFS_PROP_COMMENT = "comment";
	
	// DOAP Class Names
	static final String CLASS_DOAP_PROJECT = "Project";
	
	// DOAP Project Property Names
	static final String PROP_PROJECT_NAME = "name";
	static final String PROP_PROJECT_HOMEPAGE = "homepage";
	
	// SPDX Class Names
	static final String CLASS_SPDX_ANALYSIS = "SpdxDocument";
	static final String CLASS_SPDX_PACKAGE = "Package";
	static final String CLASS_SPDX_CREATION_INFO = "CreationInfo";
	static final String CLASS_SPDX_CHECKSUM = "Checksum";
	static final String CLASS_SPDX_LICENSE = "License";
	static final String CLASS_SPDX_CONJUNCTIVE_LICENSE_SET = "ConjunctiveLicenseSet";
	static final String CLASS_SPDX_DISJUNCTIVE_LICENSE_SET = "DisjunctiveLicenseSet";
	static final String CLASS_SPDX_EXTRACTED_LICENSING_INFO = "ExtractedLicensingInfo";
	static final String CLASS_SPDX_STANDARD_LICENSE = "License";
	static final String CLASS_SPDX_FILE = "File";
	static final String CLASS_SPDX_REVIEW = "Review";
	
	// SPDX Document Properties
	static final String PROP_SPDX_REVIEWED_BY = "reviewed";
	static final String PROP_SPDX_NONSTANDARD_LICENSES = "hasNonStandardLicense";
	static final String PROP_SPDX_VERSION = "specVersion";
	static final String PROP_SPDX_CREATION_INFO = "creationInfo";
	static final String PROP_SPDX_PACKAGE = "describesPackage";
		
	// SPDX CreationInfo Properties
// - use rdfs:comment	static final String PROP_CREATION_CREATOR_COMMENT = "comment";
	static final String PROP_CREATION_CREATOR = "creator";
	static final String PROP_CREATION_CREATED = "created";	// creation timestamp
	
	// SPDX Checksum Properties
	static final String PROP_CHECKSUM_ALGORITHM = "algorithm";
	static final String PROP_CHECKSUM_VALUE = "checksumValue";
	
	// SPDX Package Properties
	static final String PROP_PACKAGE_DECLARED_NAME = "name";
	static final String PROP_PACKAGE_FILE_NAME = "packageFileName";	
	static final String PROP_PACKAGE_CHECKSUM = "checksum";
	static final String PROP_PACKAGE_DOWNLOAD_URL = "packageDownloadLocation";
	static final String PROP_PACKAGE_SOURCE_INFO = "sourceInfo";
	static final String PROP_PACKAGE_DECLARED_LICENSE = "licenseDeclared";
	static final String PROP_PACKAGE_DETECTED_LICENSE = "licenseConcluded";
	static final String PROP_PACKAGE_DECLARED_COPYRIGHT = "copyrightText";
	static final String PROP_PACKAGE_SHORT_DESC = "summary";
	static final String PROP_PACKAGE_DESCRIPTION = "description";
	static final String PROP_PACKAGE_FILE = "hasFile";
//	static final String PROP_PACKAGE_URL = "DownloadURL";
	static final String PROP_PACKAGE_VERIFICATION_CODE = "packageVerificationCode";	
	static final String PROP_PACKAGE_LICENSE_INFO_FROM_FILES = "licenseInfoFromFiles";
	static final String PROP_PACKAGE_LICENSE_COMMENT = "licenseComments";
	
	// SPDX License Properties
	static final String PROP_LICENSE_ID = "licenseID";
	static final String PROP_LICENSE_TEXT = "licenseText";
	static final String PROP_EXTRACTED_TEXT = "extractedText";
	static final String PROP_LICENSE_SET_MEMEBER = "member";
//	static final String PROP_DISJUNCTIVE_LICENSE = "DisjunctiveLicense";
	
	// SPDX File Properties
	static final String PROP_FILE_NAME = "fileName"; 	
	static final String PROP_FILE_TYPE = "fileType"; 	
	static final String PROP_FILE_LICENSE = "licenseConcluded";	
	static final String PROP_FILE_COPYRIGHT = "copyrightText"; 	
	static final String PROP_FILE_CHECKSUM = "checksum";
	public static final String PROP_FILE_SEEN_LICENSE = "licenseInfoInFile";	
	public static final String PROP_FILE_LIC_COMMENTS = "licenseComments";	
	public static final String PROP_FILE_ARTIFACTOF = "artifactOf";
	
	// SPDX Review Properties
	static final String PROP_REVIEW_REVIEWER = "reviewer";
	static final String PROP_REVIEW_DATE = "reviewDate";
	
	
	public static final String CURRENT_SPDX_VERSION = "SPDX-0.8";
	
	// Date format
	public static final String SPDX_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
	
	// license ID format
	public static String NON_STD_LICENSE_ID_PRENUM = "LicenseRef-";
	public static Pattern LICENSE_ID_PATTERN = Pattern.compile(NON_STD_LICENSE_ID_PRENUM+"(\\d+)$");

	/**
	 * Keeps tract of the next license reference number when generating the license ID's for
	 * non-standard licenses
	 */
	protected int nextLicenseRef = 1;
	
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
		 * @throws InvalidSPDXAnalysisException 
		 */
		public String getDeclaredName() throws InvalidSPDXAnalysisException {
			String[] declaredNames = findDocPropertieStringValues(this.node, PROP_PACKAGE_DECLARED_NAME);
			if (declaredNames == null || declaredNames.length == 0) {
				return null;
			}
			if (declaredNames.length > 1) {
				throw(new InvalidSPDXAnalysisException("More than one declared name for a package"));
			}
			return(declaredNames[0]);
		}

		/**
		 * @param declaredName the declaredName to set
		 * @throws InvalidSPDXAnalysisException 
		 */
		public void setDeclaredName(String declaredName) throws InvalidSPDXAnalysisException {
			removeProperties(node, PROP_PACKAGE_DECLARED_NAME);
			addProperty(node, PROP_PACKAGE_DECLARED_NAME, new String[] {declaredName});
		}
		/**
		 * @return the fileName
		 * @throws InvalidSPDXAnalysisException 
		 */
		public String getFileName() throws InvalidSPDXAnalysisException {
			String[] fileNames = findDocPropertieStringValues(this.node, PROP_PACKAGE_FILE_NAME);
			if (fileNames == null || fileNames.length == 0) {
				return null;
			}
			if (fileNames.length > 1) {
				throw(new InvalidSPDXAnalysisException("More than one machine name for a package"));
			}
			return fileNames[0];
		}
		/**
		 * @param fileName the fileName to set
		 * @throws InvalidSPDXAnalysisException 
		 */
		public void setFileName(String fileName) throws InvalidSPDXAnalysisException {
			removeProperties(node, PROP_PACKAGE_FILE_NAME);
			addProperty(node, PROP_PACKAGE_FILE_NAME, new String[] {fileName});
		}
		/**
		 * @return the sha1
		 * @throws InvalidSPDXAnalysisException 
		 */
		public String getSha1() throws InvalidSPDXAnalysisException {
			
			String retval = null;
			Node p = model.getProperty(SPDX_NAMESPACE, PROP_PACKAGE_CHECKSUM).asNode();
			Triple m = Triple.createMatch(this.node, p, null);
			ExtendedIterator<Triple> tripleIter = model.getGraph().find(m);	
			while (tripleIter.hasNext()) {
				Triple t = tripleIter.next();
				SPDXChecksum cksum = new SPDXChecksum(model, t.getObject());
				if (cksum.getAlgorithm().equals(SPDXChecksum.ALGORITHM_SHA1)) {
					retval = cksum.getValue();
				}
			}
			return retval;
		}
		/**
		 * @param sha1 the sha1 to set
		 * @throws InvalidSPDXAnalysisException 
		 */
		public void setSha1(String sha1) throws InvalidSPDXAnalysisException {
			removeProperties(node, PROP_PACKAGE_CHECKSUM);
			SPDXChecksum cksum = new SPDXChecksum(SPDXChecksum.ALGORITHM_SHA1, sha1);
			Resource cksumResource = cksum.createResource(model);
			Resource s = getResource(this.node);
			Property p = model.createProperty(SPDX_NAMESPACE, PROP_PACKAGE_CHECKSUM);
			s.addProperty(p, cksumResource);
		}
		/**
		 * @return the sourceInfo
		 * @throws InvalidSPDXAnalysisException 
		 */
		public String getSourceInfo() throws InvalidSPDXAnalysisException {
			String[] sourceInfos = findDocPropertieStringValues(this.node, PROP_PACKAGE_SOURCE_INFO);
			if (sourceInfos == null || sourceInfos.length == 0) {
				return null;
			}
			if (sourceInfos.length > 1) {
				throw(new InvalidSPDXAnalysisException("More than one source info for an SPDX package"));
			}
			return sourceInfos[0];
		}
		/**
		 * @param sourceInfo the sourceInfo to set
		 * @throws InvalidSPDXAnalysisException 
		 */
		public void setSourceInfo(String sourceInfo) throws InvalidSPDXAnalysisException {
			removeProperties(node, PROP_PACKAGE_SOURCE_INFO);
			addProperty(node, PROP_PACKAGE_SOURCE_INFO, new String[] {sourceInfo});
		}
		/**
		 * @return the declaredLicenses
		 * @throws InvalidSPDXAnalysisException 
		 */
		public SPDXLicenseInfo[] getDeclaredLicenses() throws InvalidSPDXAnalysisException {
			ArrayList<SPDXLicenseInfo> alLic = new ArrayList<SPDXLicenseInfo>();
			Node p = model.getProperty(SPDX_NAMESPACE, PROP_PACKAGE_DECLARED_LICENSE).asNode();
			Triple m = Triple.createMatch(this.node, p, null);
			ExtendedIterator<Triple> tripleIter = model.getGraph().find(m);	
			while (tripleIter.hasNext()) {
				Triple t = tripleIter.next();
				alLic.add(SPDXLicenseInfoFactory.getLicenseInfoFromModel(model, t.getObject()));
			}
			SPDXLicenseInfo[] retval = new SPDXLicenseInfo[alLic.size()];
			retval = alLic.toArray(retval);
			return retval;
		}
		/**
		 * @param declaredLicenses the declaredLicenses to set
		 * @throws InvalidSPDXAnalysisException 
		 */
		public void setDeclaredLicenses(SPDXLicenseInfo[] declaredLicenses) throws InvalidSPDXAnalysisException {
			removeProperties(node, PROP_PACKAGE_DECLARED_LICENSE);
			Resource s = getResource(this.node);
			Property p = model.createProperty(SPDX_NAMESPACE, PROP_PACKAGE_DECLARED_LICENSE);

			for (int i = 0; i < declaredLicenses.length; i++) {
				Resource lic = declaredLicenses[i].createResource(model);
				s.addProperty(p, lic);
			}
		}
		
		
		/**
		 * @return the detectedLicenses
		 * @throws InvalidSPDXAnalysisException 
		 */
		public SPDXLicenseInfo[] getDetectedLicenses() throws InvalidSPDXAnalysisException {
			ArrayList<SPDXLicenseInfo> alLic = new ArrayList<SPDXLicenseInfo>();
			Node p = model.getProperty(SPDX_NAMESPACE, PROP_PACKAGE_DETECTED_LICENSE).asNode();
			Triple m = Triple.createMatch(this.node, p, null);
			ExtendedIterator<Triple> tripleIter = model.getGraph().find(m);	
			while (tripleIter.hasNext()) {
				Triple t = tripleIter.next();
				alLic.add(SPDXLicenseInfoFactory.getLicenseInfoFromModel(model, t.getObject()));
			}
			SPDXLicenseInfo[] retval = new SPDXLicenseInfo[alLic.size()];
			retval = alLic.toArray(retval);
			return retval;
		}
		/**
		 * @param detectedLicenses the detectedLicenses to set
		 * @throws InvalidSPDXAnalysisException 
		 */
		public void setDetectedLicenses(SPDXLicenseInfo[] detectedLicenses) throws InvalidSPDXAnalysisException {
			removeProperties(node, PROP_PACKAGE_DETECTED_LICENSE);
			Resource s = getResource(this.node);
			Property p = model.createProperty(SPDX_NAMESPACE, PROP_PACKAGE_DETECTED_LICENSE);
			for (int i = 0; i < detectedLicenses.length; i++) {
				Resource lic = detectedLicenses[i].createResource(model);
				s.addProperty(p, lic);
			}
		}
		/**
		 * @return the licenseComment
		 * @throws InvalidSPDXAnalysisException 
		 */
		public String getLicenseComment() throws InvalidSPDXAnalysisException {
			String[] comments = findDocPropertieStringValues(this.node, PROP_PACKAGE_LICENSE_COMMENT);
			if (comments == null || comments.length == 0) {
				return null;
			}
			if (comments.length > 1) {
				throw(new InvalidSPDXAnalysisException("More than one license comment for a package"));
			}
			return(comments[0]);
		}
		/**
		 * @param comments the license comments to set
		 * @throws InvalidSPDXAnalysisException 
		 */
		public void setLicenseComment(String comments) throws InvalidSPDXAnalysisException {
			removeProperties(node, PROP_PACKAGE_LICENSE_COMMENT);
			addProperty(node, PROP_PACKAGE_LICENSE_COMMENT, new String[] {comments});
		}
		/**
		 * @return the declaredCopyright
		 * @throws InvalidSPDXAnalysisException 
		 */
		public String getDeclaredCopyright() throws InvalidSPDXAnalysisException {
			String[] copyrights = findDocPropertieStringValues(this.node, PROP_PACKAGE_DECLARED_COPYRIGHT);
			if (copyrights == null || copyrights.length == 0) {
				return null;
			}
			if (copyrights.length > 1) {
				throw(new InvalidSPDXAnalysisException("More than one declared copyright for a package"));
			}
			return(copyrights[0]);
		}
		/**
		 * @param declaredCopyright the declaredCopyright to set
		 * @throws InvalidSPDXAnalysisException 
		 */
		public void setDeclaredCopyright(String declaredCopyright) throws InvalidSPDXAnalysisException {
			removeProperties(node, PROP_PACKAGE_DECLARED_COPYRIGHT);
			addProperty(node, PROP_PACKAGE_DECLARED_COPYRIGHT, new String[] {declaredCopyright});
		}
		/**
		 * @return the shortDescription
		 * @throws InvalidSPDXAnalysisException 
		 */
		public String getShortDescription() throws InvalidSPDXAnalysisException {
			String[] shortDescs = findDocPropertieStringValues(this.node, PROP_PACKAGE_SHORT_DESC);
			if (shortDescs == null || shortDescs.length == 0) {
				return null;
			}
			if (shortDescs.length > 1) {
				throw(new InvalidSPDXAnalysisException("More than one short description for a package"));
			}
			return(shortDescs[0]);
		}
		/**
		 * @param shortDescription the shortDescription to set
		 * @throws InvalidSPDXAnalysisException 
		 */
		public void setShortDescription(String shortDescription) throws InvalidSPDXAnalysisException {
			removeProperties(node, PROP_PACKAGE_SHORT_DESC);
			addProperty(node, PROP_PACKAGE_SHORT_DESC, new String[] {shortDescription});
		}
		/**
		 * @return the description
		 * @throws InvalidSPDXAnalysisException 
		 */
		public String getDescription() throws InvalidSPDXAnalysisException {
			String[] desc = findDocPropertieStringValues(this.node, PROP_PACKAGE_DESCRIPTION);
			if (desc == null || desc.length == 0) {
				return null;
			}
			if (desc.length > 1) {
				throw(new InvalidSPDXAnalysisException("More than one description for a package"));
			}
			return(desc[0]);
		}
		/**
		 * @param description the description to set
		 * @throws InvalidSPDXAnalysisException 
		 */
		public void setDescription(String description) throws InvalidSPDXAnalysisException {
			removeProperties(node, PROP_PACKAGE_DESCRIPTION);
			addProperty(node, PROP_PACKAGE_DESCRIPTION, new String[] {description});
		}
		/**
		 * @return the files
		 * @throws InvalidSPDXAnalysisException 
		 */
		public SPDXFile[] getFiles() throws InvalidSPDXAnalysisException {
			// files
			ArrayList<SPDXFile> alFiles = new ArrayList<SPDXFile>();
			Node p = model.getProperty(SPDX_NAMESPACE, PROP_PACKAGE_FILE).asNode();
			Triple m = Triple.createMatch(this.node, p, null);
			ExtendedIterator<Triple> tripleIter = model.getGraph().find(m);	
			while (tripleIter.hasNext()) {
				Triple t = tripleIter.next();
				alFiles.add(new SPDXFile(model, t.getObject()));
			}
			SPDXFile[] retval = new SPDXFile[alFiles.size()];
			return alFiles.toArray(retval);
		}
		/**
		 * @param files the files to set
		 * @throws InvalidSPDXAnalysisException 
		 */
		public void setFiles(SPDXFile[] files) throws InvalidSPDXAnalysisException {
			removeProperties(node, PROP_PACKAGE_FILE);
			Resource s = getResource(this.node);
			Property p = model.createProperty(SPDX_NAMESPACE, PROP_PACKAGE_FILE);
			for (int i = 0; i < files.length; i++) {				
				Resource file = files[i].createResource(model);
				s.addProperty(p, file);
			}
		}
		
		public String getDownloadUrl() throws InvalidSPDXAnalysisException {
			String[] urls = findDocPropertieStringValues(this.node, PROP_PACKAGE_DOWNLOAD_URL);
			if (urls == null || urls.length == 0) {
				return null;
			}
			if (urls.length > 1) {
				throw(new InvalidSPDXAnalysisException("More than one URL for a package"));
			}
			return(urls[0]);
		}
		
		public void setDownloadUrl(String url) throws InvalidSPDXAnalysisException {
			removeProperties(node, PROP_PACKAGE_DOWNLOAD_URL);
			addProperty(node, PROP_PACKAGE_DOWNLOAD_URL, new String[] {url});
		}
		
		public String getVerificationCode() throws InvalidSPDXAnalysisException {
			String[] cksums = findDocPropertieStringValues(this.node, PROP_PACKAGE_VERIFICATION_CODE);
			if (cksums == null || cksums.length == 0) {
				return null;
			}
			if (cksums.length > 1) {
				throw(new InvalidSPDXAnalysisException("More than one file checksums for a package"));
			}
			return(cksums[0]);
		}
		
		public void setVerificationCode(String fileChecksums) throws InvalidSPDXAnalysisException {
			removeProperties(node, PROP_PACKAGE_VERIFICATION_CODE);
			addProperty(node, PROP_PACKAGE_VERIFICATION_CODE, new String[] {fileChecksums});

		}
		public SPDXPackageInfo getPackageInfo() throws InvalidSPDXAnalysisException {
			return new SPDXPackageInfo(this.getDeclaredName(), this.getFileName(), 
					this.getSha1(), this.getSourceInfo(), this.getDeclaredLicenses(), 
					this.getDetectedLicenses(), this.getDeclaredCopyright(), 
					this.getShortDescription(), this.getDescription(), this.getDownloadUrl(), 
					this.getVerificationCode());
		}
		
		public void setLicenseInfoFromFiles(SPDXLicenseInfo[] licenseInfo) throws InvalidSPDXAnalysisException {
			removeProperties(node, PROP_PACKAGE_LICENSE_INFO_FROM_FILES);
			Resource s = getResource(this.node);
			Property p = model.createProperty(SPDX_NAMESPACE, PROP_PACKAGE_LICENSE_INFO_FROM_FILES);

			for (int i = 0; i < licenseInfo.length; i++) {
				Resource lic = licenseInfo[i].createResource(model);
				s.addProperty(p, lic);
			}

		}
		
		public SPDXLicenseInfo[] getLicenseInfoFromFiles() throws InvalidSPDXAnalysisException {
			ArrayList<SPDXLicenseInfo> alLic = new ArrayList<SPDXLicenseInfo>();
			Node p = model.getProperty(SPDX_NAMESPACE, PROP_PACKAGE_LICENSE_INFO_FROM_FILES).asNode();
			Triple m = Triple.createMatch(this.node, p, null);
			ExtendedIterator<Triple> tripleIter = model.getGraph().find(m);	
			while (tripleIter.hasNext()) {
				Triple t = tripleIter.next();
				alLic.add(SPDXLicenseInfoFactory.getLicenseInfoFromModel(model, t.getObject()));
			}
			SPDXLicenseInfo[] retval = new SPDXLicenseInfo[alLic.size()];
			retval = alLic.toArray(retval);
			return retval;
		}
	}
		
	Model model;
	SPDXPackage spdxPackage = null;
	
	public SPDXDocument(Model model) throws InvalidSPDXAnalysisException {
		this.model = model;
		Node spdxDocNode = getSpdxDocNode();
		if (spdxDocNode != null) {	// not empty - we should verify
			verify();
			initializeNextLicenseRef();
		}
	}

	/**
	 * Initialize the next license reference by scanning all of the existing non-standard licenses
	 * @throws InvalidSPDXAnalysisException 
	 */
	private void initializeNextLicenseRef() throws InvalidSPDXAnalysisException {
		initializeNextLicenseRef(this.getNonStandardLicenses());
	}
	
	private void initializeNextLicenseRef(SPDXNonStandardLicense[] existingLicenses) throws InvalidSPDXAnalysisException {
		int highestNonStdLicense = 0;
		for (int i = 0; i < existingLicenses.length; i++) {
			int idNum = getLicenseRefNum(existingLicenses[i].getId());
			if (idNum > highestNonStdLicense) {
				highestNonStdLicense = idNum;
			}
		}	
		this.nextLicenseRef = highestNonStdLicense + 1;
	}
	/**
	 * Parses a license ID and return the integer representing the ID number (e.g. N in LicenseRef-N)
	 * @param licenseID
	 * @return
	 * @throws InvalidSPDXAnalysisException
	 */
	public int getLicenseRefNum(String licenseID) throws InvalidSPDXAnalysisException {
		Matcher matcher = LICENSE_ID_PATTERN.matcher(licenseID);
		if (!matcher.matches()) {
			throw(new InvalidSPDXAnalysisException("Invalid license ID found in the non-standard licenses: '"+licenseID+"'"));
		}
		int numGroups = matcher.groupCount();
		if (numGroups != 1) {
			throw(new InvalidSPDXAnalysisException("Invalid license ID found in the non-standard licenses: '"+licenseID+"'"));
		}
		int idNum = Integer.decode(matcher.group(1));
		return idNum;
	}
	
	public static String formNonStandardLicenseID(int idNum) {
		return NON_STD_LICENSE_ID_PRENUM + String.valueOf(idNum);
	}
	
	synchronized int getAndIncrementNextLicenseRef() {
		int retval = this.nextLicenseRef;
		this.nextLicenseRef++;
		return retval;
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
	 * @throws InvalidSPDXAnalysisException 
	 */
	public String getSpdxVersion() throws InvalidSPDXAnalysisException {
		String[] versions = findDocPropertieStringValues(getSpdxDocNode(), PROP_SPDX_VERSION);
		if (versions == null || versions.length == 0) {
			return null;
		}
		if (versions.length > 1) {
			throw(new InvalidSPDXAnalysisException("More than one version exists for the SPDX Document"));
		}
		return versions[0];
	}
	
	/**
	 * Remove all properties by the property name from the subject node
	 * @param node
	 * @param propertyName
	 * @throws InvalidSPDXAnalysisException 
	 */
	private void removeProperties(Node subject, String propertyName) throws InvalidSPDXAnalysisException {
		Property p = model.getProperty(SPDX_NAMESPACE, propertyName);
		Resource s = getResource(subject);
		model.removeAll(s, p, null);
		
	}
	
	private Resource getResource(Node node) throws InvalidSPDXAnalysisException {
		Resource s;
		if (node.isURI()) {
			s = model.createResource(node.getURI());
		} else if (node.isBlank()) {
			s = model.createResource(node.getBlankNodeId());
		} else {
			throw(new InvalidSPDXAnalysisException("Node can not be a literal"));
		}
		return s;
	}
	
	private void addProperty(Node subject, String propertyName, String[] propertyValue) throws InvalidSPDXAnalysisException {
		Resource s = getResource(subject);
		for (int i = 0; i < propertyValue.length; i++) {
			Property p = model.createProperty(SPDX_NAMESPACE, propertyName);
			s.addProperty(p, propertyValue[i]);
		}
	}

	/**
	 * @param spdxVersion the spdxVersion to set
	 * @throws InvalidSPDXAnalysisException 
	 */
	public void setSpdxVersion(String spdxVersion) throws InvalidSPDXAnalysisException {
		Node spdxDocNode = getSpdxDocNode();
		if (spdxDocNode == null) {
			throw(new InvalidSPDXAnalysisException("Must create the SPDX document before setting spdxVersion"));
		}
		removeProperties(spdxDocNode, PROP_SPDX_VERSION);
		addProperty(spdxDocNode, PROP_SPDX_VERSION, new String[] {spdxVersion});
	}

	@Deprecated
	/**
	 * This method id deprecated - please use the getCreator() method for this information
	 * @return the The creators of the Analysis
	 * @throws InvalidSPDXAnalysisException 
	 */
	public String[] getCreators() throws InvalidSPDXAnalysisException {
		SPDXCreatorInformation creator = getCreatorInfo();
		if (creator != null && creator.getCreators() != null) {
			return creator.getCreators();
		} else {
			return null;
		}
	}
	
	public SPDXCreatorInformation getCreatorInfo() throws InvalidSPDXAnalysisException {
		Node spdxDocNode = getSpdxDocNode();
		if (spdxDocNode == null) {
			throw(new InvalidSPDXAnalysisException("No SPDX Document was found.  Can not access the creator information"));
		}
		ArrayList<SPDXCreatorInformation> als = new ArrayList<SPDXCreatorInformation>();
		Node p = model.getProperty(SPDX_NAMESPACE, PROP_SPDX_CREATION_INFO).asNode();
		Triple m = Triple.createMatch(spdxDocNode, p, null);
		ExtendedIterator<Triple> tripleIter = model.getGraph().find(m);	
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			als.add(new SPDXCreatorInformation(model, t.getObject()));
		}
		if (als.size() > 1) {
			throw(new InvalidSPDXAnalysisException("Too many creation infos for document.  Only one is allowed."));
		}
		if (als.size() > 0) {
			return als.get(0);
		} else {
			return null;
		}
	}

	/**
	 * @param creators the creators of the analysis
	 * @throws InvalidSPDXDocException 
	 */
	public void setCreationInfo(SPDXCreatorInformation creator) throws InvalidSPDXAnalysisException {
		Node spdxDocNode = getSpdxDocNode();
		if (spdxDocNode == null) {
			throw(new InvalidSPDXAnalysisException("Must have an SPDX document to set creationInfo"));
		}
		// delete any previous created
		Property p = model.getProperty(SPDX_NAMESPACE, PROP_SPDX_CREATION_INFO);
		Resource s = getResource(spdxDocNode);
		model.removeAll(s, p, null);
		// add the property
		p = model.createProperty(SPDX_NAMESPACE, PROP_SPDX_CREATION_INFO);
		s.addProperty(p, creator.createResource(model));
	}

	@Deprecated
	/**
	 * This method id deprecated - please use the getCreator() method for this information
	 * @return the creator comments for the analysis
	 * @throws InvalidSPDXAnalysisException 
	 */
	public String getCreatorComment() throws InvalidSPDXAnalysisException {
		SPDXCreatorInformation creator = this.getCreatorInfo();
		if (creator != null) {
			return creator.getComment();
		} else {
			return null;
		}
	}

	/**
	 * @return the reviewers
	 * @throws InvalidSPDXAnalysisException 
	 */
	public SPDXReview[] getReviewers() throws InvalidSPDXAnalysisException {
		Node spdxDocNode = getSpdxDocNode();
		if (spdxDocNode == null) {
			throw(new InvalidSPDXAnalysisException("Must have an SPDX document to get reviewers"));
		}
		ArrayList<SPDXReview> als = new ArrayList<SPDXReview>();
		als.clear();
		Node p = model.getProperty(SPDX_NAMESPACE, PROP_SPDX_REVIEWED_BY).asNode();
		Triple m = Triple.createMatch(spdxDocNode, p, null);
		ExtendedIterator<Triple >tripleIter = model.getGraph().find(m);	
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			als.add(new SPDXReview(model, t.getObject()));
		}
		SPDXReview[] reviewers = new SPDXReview[als.size()];
		reviewers = als.toArray(reviewers);
		return reviewers;
	}

	/**
	 * @param reviewers the reviewers to set
	 * @throws InvalidSPDXAnalysisException 
	 */
	public void setReviewers(SPDXReview[] reviewers) throws InvalidSPDXAnalysisException {
		Node spdxDocNode = getSpdxDocNode();
		if (spdxDocNode == null) {
			throw(new InvalidSPDXAnalysisException("Must have an SPDX document to set reviewers"));
		}
		// delete any previous created
		Property p = model.getProperty(SPDX_NAMESPACE, PROP_SPDX_REVIEWED_BY);
		Resource s = getResource(spdxDocNode);
		model.removeAll(s, p, null);
		// add the property
		for (int i = 0; i < reviewers.length; i++) {
			p = model.createProperty(SPDX_NAMESPACE, PROP_SPDX_REVIEWED_BY);
			s.addProperty(p, reviewers[i].createResource(model));
		}
	}

	@Deprecated
	/**
	 * This method id deprecated - please use the getCreator() method for this information
	 * @return the created
	 * @throws InvalidSPDXAnalysisException 
	 */
	public String getCreated() throws InvalidSPDXAnalysisException {
		SPDXCreatorInformation creator = this.getCreatorInfo();
		if (creator != null) {
			return creator.getCreated();
		} else {
			return null;
		}
	}

	/**
	 * @return the spdxPackage
	 * @throws InvalidSPDXAnalysisException 
	 */
	public SPDXPackage getSpdxPackage() throws InvalidSPDXAnalysisException {
		if (this.spdxPackage != null) {
			return this.spdxPackage;
		}
		Node spdxDocNode = getSpdxDocNode();
		if (spdxDocNode == null) {
			throw(new InvalidSPDXAnalysisException("Must set an SPDX doc before getting an SPDX package"));
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
	 * @throws InvalidSPDXAnalysisException 
	 */
	public void createSpdxPackage(String uri) throws InvalidSPDXAnalysisException {
		Node spdxDocNode = getSpdxDocNode();
		if (spdxDocNode == null) {
			throw(new InvalidSPDXAnalysisException("Must create the SPDX document before creating an SPDX Package"));
		}
		// delete the previous SPDX package
		Property p = model.getProperty(SPDX_NAMESPACE, PROP_SPDX_PACKAGE);
		Resource s = getResource(getSpdxDocNode());
		model.removeAll(s, p, null);
		p = model.createProperty(SPDX_NAMESPACE, PROP_SPDX_PACKAGE);
		Resource pkgType = model.createResource(SPDX_NAMESPACE+CLASS_SPDX_PACKAGE);
		Resource spdxPkg = model.createResource(uri, pkgType);
		s.addProperty(p, spdxPkg);
		this.spdxPackage = new SPDXPackage(spdxPkg.asNode());
	}
	
	public void createSpdxPackage() throws InvalidSPDXAnalysisException {
		// generate a unique URI by appending a "?package" to the end of the doc URI
		Node spdxDocNode = getSpdxDocNode();
		if (spdxDocNode == null) {
			throw(new InvalidSPDXAnalysisException("Must create the SPDX document before creating an SPDX Package"));
		}
		createSpdxPackage(spdxDocNode.getURI()+"?package");
	}

	/**
	 * @return the nonStandardLicenses
	 * @throws InvalidSPDXAnalysisException 
	 */
	public SPDXNonStandardLicense[] getNonStandardLicenses() throws InvalidSPDXAnalysisException {
		// nonStandardLicenses
		Node spdxDocNode = getSpdxDocNode();
		if (spdxDocNode == null) {
			throw(new InvalidSPDXAnalysisException("No SPDX Document - can not get the Non Standard Licenses"));
		}
		ArrayList<SPDXNonStandardLicense> alLic = new ArrayList<SPDXNonStandardLicense>();
		Node p = model.getProperty(SPDX_NAMESPACE, PROP_SPDX_NONSTANDARD_LICENSES).asNode();
		Triple m = Triple.createMatch(spdxDocNode, p, null);
		ExtendedIterator<Triple> tripleIter = model.getGraph().find(m);	
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			alLic.add(new SPDXNonStandardLicense(model, t.getObject()));
		}
		SPDXNonStandardLicense[] nonStandardLicenses = new SPDXNonStandardLicense[alLic.size()];
		nonStandardLicenses = alLic.toArray(nonStandardLicenses);
		return nonStandardLicenses;
	}

	/**
	 * @param nonStandardLicenses the nonStandardLicenses to set
	 * @throws InvalidSPDXAnalysisException 
	 */
	public void setNonStandardLicenses(SPDXNonStandardLicense[] nonStandardLicenses) throws InvalidSPDXAnalysisException {
		Node spdxDocNode = getSpdxDocNode();
		if (spdxDocNode == null) {
			throw(new InvalidSPDXAnalysisException("Must create the SPDX document before setting Non-Standard Licenses"));
		}
		// validate the license ID's and update the next license ID property
		initializeNextLicenseRef(nonStandardLicenses);
		// delete the previous createdby's
		Property p = model.getProperty(SPDX_NAMESPACE, PROP_SPDX_NONSTANDARD_LICENSES);
		Resource s = getResource(getSpdxDocNode());
		model.removeAll(s, p, null);
		for (int i = 0; i < nonStandardLicenses.length; i++) {
			p = model.createProperty(SPDX_NAMESPACE, PROP_SPDX_NONSTANDARD_LICENSES);
			s.addProperty(p, nonStandardLicenses[i].createResource(model));
		}
		// need to re-update the max license ID
	}
	
	/**
	 * Adds a new non-standard license containing the text provided.  Forms the license ID
	 * from the next License ID available
	 * @param licenseText
	 * @return the newly created NonStandardLicense
	 * @throws InvalidSPDXAnalysisException
	 */
	public synchronized SPDXNonStandardLicense addNewNonStandardLicense(String licenseText) throws InvalidSPDXAnalysisException {
		int nextLicNum = this.getAndIncrementNextLicenseRef();
		String licenseID = formNonStandardLicenseID(nextLicNum);
		SPDXNonStandardLicense retval = new SPDXNonStandardLicense(licenseID, licenseText);
		Property p = model.getProperty(SPDX_NAMESPACE, PROP_SPDX_NONSTANDARD_LICENSES);
		Resource s = getResource(getSpdxDocNode());
		s.addProperty(p, retval.createResource(model));
		return retval;
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
	public void createSpdxAnalysis(String uri) {
		Node spdxDocNode = getSpdxDocNode();
		if (spdxDocNode != null) {
			// delete
			model.removeAll();
		}
		model.setNsPrefix("", SPDX_NAMESPACE);
		Resource spdxAnalysisType = model.createResource(SPDX_NAMESPACE+CLASS_SPDX_ANALYSIS);
		model.createResource(uri, spdxAnalysisType);
	}
	
	/**
	 * @return the spdx doc node from the model
	 */
	private Node getSpdxDocNode() {
		Node spdxDocNode = null;
		Node rdfTypePredicate = this.model.getProperty(RDF_NAMESPACE, RDF_PROP_TYPE).asNode();
		Node spdxDocObject = this.model.getProperty(SPDX_NAMESPACE, CLASS_SPDX_ANALYSIS).asNode();
		Triple m = Triple.createMatch(null, rdfTypePredicate, spdxDocObject);
		ExtendedIterator<Triple> tripleIter = model.getGraph().find(m);	// find the document
		while (tripleIter.hasNext()) {
			Triple docTriple = tripleIter.next();
			spdxDocNode = docTriple.getSubject();
		}
		return spdxDocNode;
	}
}
