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
import java.util.HashSet;

import com.google.common.collect.Sets;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.iterator.ExtendedIterator;

import java.util.regex.Matcher;

import org.spdx.spdxspreadsheet.InvalidLicenseStringException;


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
public class SPDXDocument implements SpdxRdfConstants {

	public static final String POINT_EIGHT_SPDX_VERSION = "SPDX-0.8";
	public static final String POINT_NINE_SPDX_VERSION = "SPDX-0.9";
	public static final String CURRENT_SPDX_VERSION = "SPDX-1.0";
	public static final String CURRENT_IMPLEMENTATION_VERSION = "0.9.4";

	static Set<String> SUPPORTED_SPDX_VERSIONS = Sets.newHashSet();

	static {
		SUPPORTED_SPDX_VERSIONS.add(CURRENT_SPDX_VERSION);
		SUPPORTED_SPDX_VERSIONS.add(POINT_EIGHT_SPDX_VERSION);
		SUPPORTED_SPDX_VERSIONS.add(POINT_NINE_SPDX_VERSION);
	}

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
		 * @return Version information of the package
		 * @throws InvalidSPDXAnalysisException
		 */
		public String getVersionInfo() throws InvalidSPDXAnalysisException {
			String[] versionInfos = findDocPropertieStringValues(this.node, PROP_PACKAGE_VERSION_INFO);
			if (versionInfos == null || versionInfos.length == 0) {
				return null;
			}
			if (versionInfos.length > 1) {
				throw(new InvalidSPDXAnalysisException("More than one version info for an SPDX package"));
			}
			return versionInfos[0];
		}

		/**
		 * Set the version information of the package
		 * @param versionInfo
		 * @throws InvalidSPDXAnalysisException
		 */
		public void setVersionInfo(String versionInfo) throws InvalidSPDXAnalysisException {
			removeProperties(node, PROP_PACKAGE_VERSION_INFO);
			addProperty(node, PROP_PACKAGE_VERSION_INFO, new String[] {versionInfo});
		}

		/**
		 * @return the declaredLicenses
		 * @throws InvalidSPDXAnalysisException
		 */
		public SPDXLicenseInfo getDeclaredLicense() throws InvalidSPDXAnalysisException {
			ArrayList<SPDXLicenseInfo> alLic = new ArrayList<SPDXLicenseInfo>();
			Node p = model.getProperty(SPDX_NAMESPACE, PROP_PACKAGE_DECLARED_LICENSE).asNode();
			Triple m = Triple.createMatch(this.node, p, null);
			ExtendedIterator<Triple> tripleIter = model.getGraph().find(m);
			while (tripleIter.hasNext()) {
				Triple t = tripleIter.next();
				alLic.add(SPDXLicenseInfoFactory.getLicenseInfoFromModel(model, t.getObject()));
			}
			if (alLic.size() > 1) {
				throw(new InvalidSPDXAnalysisException("Too many declared licenses"));
			}
			if (alLic.size() == 0) {
				return null;
			}
			return alLic.get(0);
		}
		/**
		 * @param declaredLicenses the declaredLicenses to set
		 * @throws InvalidSPDXAnalysisException
		 */
		public void setDeclaredLicense(SPDXLicenseInfo declaredLicense) throws InvalidSPDXAnalysisException {
			removeProperties(node, PROP_PACKAGE_DECLARED_LICENSE);
			Resource s = getResource(this.node);
			Property p = model.createProperty(SPDX_NAMESPACE, PROP_PACKAGE_DECLARED_LICENSE);

			Resource lic = declaredLicense.createResource(model);
			s.addProperty(p, lic);
		}


		/**
		 * @return the detectedLicenses
		 * @throws InvalidSPDXAnalysisException
		 */
		public SPDXLicenseInfo getConcludedLicenses() throws InvalidSPDXAnalysisException {
			ArrayList<SPDXLicenseInfo> alLic = new ArrayList<SPDXLicenseInfo>();
			Node p = model.getProperty(SPDX_NAMESPACE, PROP_PACKAGE_CONCLUDED_LICENSE).asNode();
			Triple m = Triple.createMatch(this.node, p, null);
			ExtendedIterator<Triple> tripleIter = model.getGraph().find(m);
			while (tripleIter.hasNext()) {
				Triple t = tripleIter.next();
				alLic.add(SPDXLicenseInfoFactory.getLicenseInfoFromModel(model, t.getObject()));
			}
			if (alLic.size() > 1) {
				throw(new InvalidSPDXAnalysisException("Too many concluded licenses"));
			}
			if (alLic.size() == 0) {
				return null;
			}
			return alLic.get(0);
		}
		/**
		 * @param detectedLicenses the detectedLicenses to set
		 * @throws InvalidSPDXAnalysisException
		 */
		public void setConcludedLicenses(SPDXLicenseInfo detectedLicenses) throws InvalidSPDXAnalysisException {
			removeProperties(node, PROP_PACKAGE_CONCLUDED_LICENSE);
			Resource s = getResource(this.node);
			Property p = model.createProperty(SPDX_NAMESPACE, PROP_PACKAGE_CONCLUDED_LICENSE);
			Resource lic = detectedLicenses.createResource(model);
			s.addProperty(p, lic);
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
		 * Set the originator
		 * @param originator Either a valid originator string or NOASSERTION
		 * @throws InvalidSPDXAnalysisException
		 */
		public void setOriginator(String originator) throws InvalidSPDXAnalysisException {
			String error = SpdxVerificationHelper.verifyOriginator(originator);
			if (error != null && !error.isEmpty()) {
				throw(new InvalidSPDXAnalysisException(error));
			}
			removeProperties(node, PROP_PACKAGE_ORIGINATOR);
			addProperty(node, PROP_PACKAGE_ORIGINATOR, new String[] {originator});
		}

		/**
		 * Set the Supplier
		 * @param supplier Either a valid originator string or NOASSERTION
		 * @throws InvalidSPDXAnalysisException
		 */
		public void setSupplier(String supplier) throws InvalidSPDXAnalysisException {
			String error = SpdxVerificationHelper.verifySupplier(supplier);
			if (error != null && !error.isEmpty()) {
				throw(new InvalidSPDXAnalysisException(error));
			}
			removeProperties(node, PROP_PACKAGE_SUPPLIER);
			addProperty(node, PROP_PACKAGE_SUPPLIER, new String[] {supplier});
		}

		public String getOriginator() throws InvalidSPDXAnalysisException {
			String[] originators = findDocPropertieStringValues(this.node, PROP_PACKAGE_ORIGINATOR);
			if (originators == null || originators.length == 0) {
				return null;
			}
			if (originators.length > 1) {
				throw(new InvalidSPDXAnalysisException("More than one originator for a package"));
			}
			return(originators[0]);
		}

		public String getSupplier() throws InvalidSPDXAnalysisException {
			String[] suppliers = findDocPropertieStringValues(this.node, PROP_PACKAGE_SUPPLIER);
			if (suppliers == null || suppliers.length == 0) {
				return null;
			}
			if (suppliers.length > 1) {
				throw(new InvalidSPDXAnalysisException("More than one supplier for a package"));
			}
			return(suppliers[0]);
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
			Resource docResource = getResource(getSpdxDocNode());
			Property docP = model.createProperty(SPDX_NAMESPACE, PROP_SPDX_FILE);
			for (int i = 0; i < files.length; i++) {
				Resource file = files[i].createResource(model);
				s.addProperty(p, file);
				docResource.addProperty(docP, file);
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

		public SpdxPackageVerificationCode getVerificationCode() throws InvalidSPDXAnalysisException {
			SpdxPackageVerificationCode retval = null;
			Node p = model.getProperty(SPDX_NAMESPACE, PROP_PACKAGE_VERIFICATION_CODE).asNode();
			Triple m = Triple.createMatch(this.node, p, null);
			ExtendedIterator<Triple> tripleIter = model.getGraph().find(m);
			while (tripleIter.hasNext()) {
				Triple t = tripleIter.next();
				retval = new SpdxPackageVerificationCode(model, t.getObject());
			}
			return retval;
		}

		public void setVerificationCode(SpdxPackageVerificationCode verificationCode) throws InvalidSPDXAnalysisException {
			removeProperties(node, PROP_PACKAGE_VERIFICATION_CODE);
			Resource verificationCodeResource = verificationCode.createResource(model);
			Resource s = getResource(this.node);
			Property p = model.createProperty(SPDX_NAMESPACE, PROP_PACKAGE_VERIFICATION_CODE);
			s.addProperty(p, verificationCodeResource);
		}
		public SPDXPackageInfo getPackageInfo() throws InvalidSPDXAnalysisException {
			return new SPDXPackageInfo(this.getDeclaredName(), this.getVersionInfo(), this.getFileName(),
					this.getSha1(), this.getSourceInfo(), this.getDeclaredLicense(),
					this.getConcludedLicenses(), this.getLicenseInfoFromFiles(),
					this.getLicenseComment(), this.getDeclaredCopyright(),
					this.getShortDescription(), this.getDescription(), this.getDownloadUrl(),
					this.getVerificationCode(), this.getSupplier(), this.getOriginator());
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
		/**
		 * @return
		 */
		public ArrayList<String> verify() {
			ArrayList<String> retval = new ArrayList<String>();
			// name
			try {
				String name = this.getDeclaredName();
				if (name == null || name.isEmpty()) {
					retval.add("Missing required name for package");
				}
			} catch (InvalidSPDXAnalysisException e) {
				retval.add("Invalid name: "+e.getMessage());
			}
			// summary
			try {
				@SuppressWarnings("unused")
				String summary = this.getShortDescription();
				//TODO: rdf has this as mandatory, optional in pdf - if mandatory add check
			} catch (InvalidSPDXAnalysisException e) {
				retval.add("Invalid summary: "+e.getMessage());
			}
			// description
			try {
				@SuppressWarnings("unused")
				String description = this.getDescription();
				//TODO: rdf has this as mandatory, optional in pdf - if mandatory add check
			} catch (InvalidSPDXAnalysisException e) {
				retval.add("Invalid description: "+e.getMessage());
			}
			// download location
			try {
				String downloadLocation = this.getDownloadUrl();
				if (downloadLocation == null || downloadLocation.isEmpty()) {
					retval.add("Missing required download location for package");
				}
			} catch (InvalidSPDXAnalysisException e) {
				retval.add("Invalid download location: "+e.getMessage());
			}
			// checksum
			try {
				String checksum = this.getSha1();
				if (checksum != null && !checksum.isEmpty()) {
					String verify = SpdxVerificationHelper.verifyChecksumString(checksum);
					if (verify != null) {
						retval.add("Package checksum error: "+verify);
					}
				}
			} catch (InvalidSPDXAnalysisException e) {
				retval.add("Invalid checksum: "+e.getMessage());
			}
			// source Info - optional
			try {
				@SuppressWarnings("unused")
				String sourceInfo = this.getSourceInfo();
			} catch (InvalidSPDXAnalysisException e) {
				retval.add("Invalid package source info: "+e.getMessage());
			}

			// copyright text - mandatory
			try {
				String copyrightText = this.getDeclaredCopyright();
				if (copyrightText == null || copyrightText.isEmpty()) {
					retval.add("Missing required package copyright text");
				}
			} catch (InvalidSPDXAnalysisException e) {
				retval.add("Invalid package copyright: "+e.getMessage());
			}

			// license comments - optional
			try {
				@SuppressWarnings("unused")
				String licenseComments = this.getLicenseComment();
			} catch (InvalidSPDXAnalysisException e) {
				retval.add("Invalid license comments: "+e.getMessage());
			}

			// license declared - mandatory - 1 (need to change return values)
			try {
				SPDXLicenseInfo declaredLicense = this.getDeclaredLicense();
				if (declaredLicense == null) {
					retval.add("Missing required declared license");
				} else {
					retval.addAll(declaredLicense.verify());
				}
			} catch (InvalidSPDXAnalysisException e) {
				retval.add("Invalid package declared license: "+e.getMessage());
			}

			// license concluded - mandatory - 1 (need to change return values)
			try {
				SPDXLicenseInfo concludedLicense = this.getConcludedLicenses();
				if (concludedLicense == null) {
					retval.add("Missing required concluded license");
				} else {
					retval.addAll(concludedLicense.verify());
				}
			} catch (InvalidSPDXAnalysisException e) {
				retval.add("Invalid package concluded license: "+e.getMessage());
			}

			// license infos from files - mandatory - 1 or more
			try {
				SPDXLicenseInfo[] licenseInfosFromFiles = this.getLicenseInfoFromFiles();
				if (licenseInfosFromFiles == null || licenseInfosFromFiles.length == 0) {
					retval.add("Missing required license infos from files");
				} else {
					for (int i = 0; i < licenseInfosFromFiles.length; i++) {
						retval.addAll(licenseInfosFromFiles[i].verify());
					}
				}
			} catch (InvalidSPDXAnalysisException e) {
				retval.add("Invalid package license infos from files: "+e.getMessage());
			}

			// hasFiles mandatory one or more
			try {
				SPDXFile[] files = this.getFiles();
				if (files == null || files.length == 0) {
					retval.add("Missing required package files");
				} else {
					for (int i = 0; i < files.length; i++) {
						retval.addAll(files[i].verify());
					}
				}
			} catch (InvalidSPDXAnalysisException e) {
				retval.add("Invalid package files: "+e.getMessage());
			}

			// verification code
			SpdxPackageVerificationCode verificationCode = null;
			try {
				verificationCode = this.getVerificationCode();
				if (verificationCode == null) {
					retval.add("Missing required package verification code.");
				} else {
					retval.addAll(verificationCode.verify());
				}
			} catch (InvalidSPDXAnalysisException e) {
				retval.add("Invalid package verification code: "+e.getMessage());
			}

			// supplier
			String supplier = null;
			try {
				supplier = this.getSupplier();
				if (supplier != null) {
					String error = SpdxVerificationHelper.verifySupplier(supplier);
					if (error != null && !error.isEmpty()) {
						retval.add("Supplier error - "+error);
					}
				}
			} catch (InvalidSPDXAnalysisException e) {
				retval.add("Invalid supplier: "+e.getMessage());
			}

			// originator
			String originator = null;
			try {
				originator = this.getOriginator();
				if (originator != null) {
					String error = SpdxVerificationHelper.verifySupplier(originator);
					if (error != null && !error.isEmpty()) {
						retval.add("Originator error - "+error);
					}
				}
			} catch (InvalidSPDXAnalysisException e) {
				retval.add("Invalid originator: "+e.getMessage());
			}
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
		initializeNextLicenseRef(this.getExtractedLicenseInfos());
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

	public String verifySpdxVersion(String spdxVersion) {
		if (!spdxVersion.startsWith("SPDX-")) {
			return "Invalid spdx version - must start with 'SPDX-'";
		}
		Matcher docSpecVersionMatcher = SpdxRdfConstants.SPDX_VERSION_PATTERN.matcher(spdxVersion);
		if (!docSpecVersionMatcher.matches()) {
			return "Invalid spdx version format - must match 'SPDX-M.N'";
		}
		return null;	// if we got here, there is no problem
	}
	/**
	 * Verifies the spdx document
	 * @return error messages for any fields which do not match the spec.  Return an empty array list if no issues.
	 */
	public ArrayList<String> verify() {
		ArrayList<String> retval = new ArrayList<String>();
		// specVersion
		String docSpecVersion = "";	// note - this is used later in verify to verify version specific info
		try {
			docSpecVersion = this.getSpdxVersion();
			if (docSpecVersion == null || docSpecVersion.isEmpty()) {
				retval.add("Missing required SPDX version");
			} else {
				String verify = verifySpdxVersion(docSpecVersion);
				if (verify != null) {
					retval.add(verify);
				} else {
					if (!SUPPORTED_SPDX_VERSIONS.contains(docSpecVersion)) {
						retval.add("Version "+docSpecVersion+" is not supported by this version of the rdf parser");
					}
				}
			}
		} catch (InvalidSPDXAnalysisException e) {
			retval.add("Invalid spec version: "+e.getMessage());
		}
		// creationInfo
		try {
			SPDXCreatorInformation creator = this.getCreatorInfo();
			if (creator == null) {
				retval.add("Missing required Creator");
			} else {
				ArrayList<String> creatorVerification = creator.verify();
				retval.addAll(creatorVerification);
			}
		} catch (InvalidSPDXAnalysisException e) {
			retval.add("Invalid creator information: "+e.getMessage());
		}
		// Package
		try {
			SPDXPackage sPkg = this.getSpdxPackage();
			if (sPkg == null) {
				retval.add("Missing required SPDX Package");
			} else {
				ArrayList<String> packageVerification = sPkg.verify();
				retval.addAll(packageVerification);
			}
		} catch (InvalidSPDXAnalysisException e) {
			retval.add("Invalid SPDX Package: "+e.getMessage());
		}
		// Reviewers
		try {
			SPDXReview[] reviews = this.getReviewers();
			if (reviews != null) {
				for (int i = 0; i < reviews.length; i++) {
					ArrayList<String> reviewerVerification = reviews[i].verify();
					retval.addAll(reviewerVerification);
				}
			}
		} catch (InvalidSPDXAnalysisException e) {
			retval.add("Invalid reviewers: "+e.getMessage());
		}
		// Non standard licenses
		try {
			SPDXNonStandardLicense[] extractedLicInfos = this.getExtractedLicenseInfos();
			if (extractedLicInfos != null) {
				for (int i = 0; i < extractedLicInfos.length; i++) {
					ArrayList<String> extractedLicInfoVerification = extractedLicInfos[i].verify();
					retval.addAll(extractedLicInfoVerification);
				}
			}
		} catch (InvalidSPDXAnalysisException e) {
			retval.add("Invalid extracted licensing info: "+e.getMessage());
		}
		// data license
		if (!docSpecVersion.equals(POINT_EIGHT_SPDX_VERSION) && !docSpecVersion.equals(POINT_NINE_SPDX_VERSION)) { // added as a mandatory field in 1.0
			try {
				SPDXStandardLicense dataLicense = this.getDataLicense();
				if (dataLicense == null) {
					retval.add("Missing required data license");
				}
				if (!dataLicense.getId().equals(SPDX_DATA_LICENSE_ID)) {
					retval.add("Incorrect data license for SPDX document - found "+dataLicense.getId()+", expected "+SPDX_DATA_LICENSE_ID);
				}
			} catch (InvalidSPDXAnalysisException e) {
				retval.add("Invalid data license: "+e.getMessage());
			}
		}
		return retval;
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
			if (t.getObject().isURI()) {
				if (t.getObject().getURI().equals(SpdxRdfConstants.URI_VALUE_NONE)) {
					alResult.add(SpdxRdfConstants.NONE_VALUE);
				} else if (t.getObject().getURI().equals(SpdxRdfConstants.URI_VALUE_NOASSERTION)) {
					alResult.add(SpdxRdfConstants.NOASSERTION_VALUE);
				} else {
					alResult.add(t.getObject().toString(false));
				}
			} else {
				alResult.add(t.getObject().toString(false));
			}
		}
		String[] retval = new String[alResult.size()];
		return alResult.toArray(retval);
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

	private void addProperty(Node subject, String propertyName, String[] propertyValue) throws InvalidSPDXAnalysisException { Resource s = getResource(subject);
		for (int i = 0; i < propertyValue.length; i++) {
			Property p = model.createProperty(SPDX_NAMESPACE, propertyName);
			if (propertyValue[i].equals(SpdxRdfConstants.NONE_VALUE)) {
				Resource r = model.createResource(SpdxRdfConstants.URI_VALUE_NONE);
				s.addProperty(p, r);
			} else if (propertyValue[i].equals(SpdxRdfConstants.NOASSERTION_VALUE)) {
				Resource r = model.createResource(SpdxRdfConstants.URI_VALUE_NOASSERTION);
				s.addProperty(p, r);
			} else {
				s.addProperty(p, propertyValue[i]);
			}
		}
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
	 * @param spdxVersion the spdxVersion to set
	 * @throws InvalidSPDXAnalysisException
	 */
	public void setSpdxVersion(String spdxVersion) throws InvalidSPDXAnalysisException {
		String versionVerify = verifySpdxVersion(spdxVersion);
		if (versionVerify != null && !versionVerify.isEmpty()) {
			throw(new InvalidSPDXAnalysisException(versionVerify));
		}
		Node spdxDocNode = getSpdxDocNode();
		if (spdxDocNode == null) {
			throw(new InvalidSPDXAnalysisException("Must create the SPDX document before setting spdxVersion"));
		}
		removeProperties(spdxDocNode, PROP_SPDX_VERSION);
		addProperty(spdxDocNode, PROP_SPDX_VERSION, new String[] {spdxVersion});
	}

	public SPDXStandardLicense getDataLicense() throws InvalidSPDXAnalysisException {
		ArrayList<SPDXLicenseInfo> alLic = new ArrayList<SPDXLicenseInfo>();
		Node p = model.getProperty(SPDX_NAMESPACE, PROP_SPDX_DATA_LICENSE).asNode();
		Triple m = Triple.createMatch(getSpdxDocNode(), p, null);
		ExtendedIterator<Triple> tripleIter = model.getGraph().find(m);
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			alLic.add(SPDXLicenseInfoFactory.getLicenseInfoFromModel(model, t.getObject()));
		}
		if (alLic.size() > 1) {
			throw(new InvalidSPDXAnalysisException("Too many data licenses"));
		}
		if (alLic.size() == 0) {
			throw(new InvalidSPDXAnalysisException("Missing required data license"));
		}
		if (!(alLic.get(0) instanceof SPDXStandardLicense)) {
			throw(new InvalidSPDXAnalysisException("Incorrect license for datalicense - must be a standard SPDX license type"));
		}
		return (SPDXStandardLicense)(alLic.get(0));
	}

	public void setDataLicense(SPDXStandardLicense dataLicense) throws InvalidSPDXAnalysisException {
		if (!dataLicense.getId().equals(SPDX_DATA_LICENSE_ID)) {
			throw(new InvalidSPDXAnalysisException("Invalid data license for SPDX document - license must have ID "+SPDX_DATA_LICENSE_ID));
		}
		removeProperties(getSpdxDocNode(), PROP_SPDX_DATA_LICENSE);
		Resource s = getResource(getSpdxDocNode());
		Property p = model.createProperty(SPDX_NAMESPACE, PROP_SPDX_DATA_LICENSE);

		Resource lic = dataLicense.createResource(model);
		s.addProperty(p, lic);
	}

	public SPDXFile[] getFileReferences() throws InvalidSPDXAnalysisException {
		ArrayList<SPDXFile> alFiles = new ArrayList<SPDXFile>();
		Node p = model.getProperty(SPDX_NAMESPACE, PROP_SPDX_FILE).asNode();
		Triple m = Triple.createMatch(getSpdxDocNode(), p, null);
		ExtendedIterator<Triple> tripleIter = model.getGraph().find(m);
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			alFiles.add(new SPDXFile(model, t.getObject()));
		}
		SPDXFile[] retval = new SPDXFile[alFiles.size()];
		return alFiles.toArray(retval);
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
		if (reviewers.length > 0) {
			ArrayList<String> errors = new ArrayList<String>();
			for (int i = 0;i < reviewers.length; i++) {
				errors.addAll(reviewers[i].verify());
			}
			if (errors.size() > 0) {
				StringBuilder sb = new StringBuilder("Invalid reviewers due to the following errors in validation:\n");
				for (int i = 0; i < errors.size(); i++) {
					sb.append(errors.get(i));
					sb.append('\n');
				}
				throw(new InvalidSPDXAnalysisException(sb.toString()));
			}
		}
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
	public SPDXNonStandardLicense[] getExtractedLicenseInfos() throws InvalidSPDXAnalysisException {
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
	public void setExtractedLicenseInfos(SPDXNonStandardLicense[] nonStandardLicenses) throws InvalidSPDXAnalysisException {
		ArrayList<String> errors = new ArrayList<String>();
		// verify the licenses
		for (int i = 0;i < nonStandardLicenses.length; i++) {
			errors.addAll(nonStandardLicenses[i].verify());
		}
		if (errors.size() > 0) {
			StringBuilder sb = new StringBuilder("Invalid extracted license infos due to the following verification failures:\n");
			for (int i = 0; i < errors.size(); i++) {
				sb.append(errors.get(i));
				sb.append('\n');
			}
			throw new InvalidSPDXAnalysisException(sb.toString());
		}
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
	public synchronized SPDXNonStandardLicense addNewExtractedLicenseInfo(String licenseText) throws InvalidSPDXAnalysisException {
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
	 * @throws InvalidLicenseStringException
	 * @throws InvalidSPDXAnalysisException
	 */
	public void createSpdxAnalysis(String uri) throws InvalidSPDXAnalysisException {
		Node spdxDocNode = getSpdxDocNode();
		if (spdxDocNode != null) {
			// delete
			model.removeAll();
		}
		model.setNsPrefix("", SPDX_NAMESPACE);
		Resource spdxAnalysisType = model.createResource(SPDX_NAMESPACE+CLASS_SPDX_ANALYSIS);
		model.createResource(uri, spdxAnalysisType);
		// add the default data license
		SPDXStandardLicense dataLicense;
		try {
			dataLicense = (SPDXStandardLicense)(SPDXLicenseInfoFactory.parseSPDXLicenseString(SPDX_DATA_LICENSE_ID));
		} catch (InvalidLicenseStringException e) {
			throw(new InvalidSPDXAnalysisException("Error generating the data license for the SPDX document"));
		}
		this.setDataLicense(dataLicense);
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
