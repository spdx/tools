/**
 * Copyright (c) 2015 Source Auditor Inc.
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
package org.spdx.rdfparser.model;

import java.util.ArrayList;

import org.spdx.rdfparser.IModelContainer;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.RdfParserHelper;
import org.spdx.rdfparser.SpdxPackageVerificationCode;
import org.spdx.rdfparser.SpdxRdfConstants;
import org.spdx.rdfparser.SpdxVerificationHelper;
import org.spdx.rdfparser.license.AnyLicenseInfo;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * A Package represents a collection of software files that are 
 * delivered as a single functional component.
 * @author Gary O'Neall
 *
 */
public class SpdxPackage extends SpdxItem implements SpdxRdfConstants {
	
	AnyLicenseInfo licenseDeclared;
	Checksum[] checksums;
	String description;
	String downloadLocation;
	String homepage;
	String originator;
	String packageFileName;
	SpdxPackageVerificationCode packageVerificationCode;	//TODO Move this to RdfModelObject
	String sourceInfo;
	String summary;
	String supplier;
	String versionInfo;
	SpdxFile[] files;
	

	/**
	 * @param name
	 * @param comment
	 * @param annotations
	 * @param relationships
	 * @param licenseConcluded
	 * @param licenseDeclared
	 * @param copyrightText
	 * @param licenseComment
	 */
	public SpdxPackage(String name, String comment, Annotation[] annotations,
			Relationship[] relationships, AnyLicenseInfo licenseConcluded,
			AnyLicenseInfo[] licenseInfosFromFiles, String copyrightText,
			String licenseComment, AnyLicenseInfo licenseDeclared,
			Checksum[] checksums, String description, String downloadLocation, 
			SpdxFile[] files, String homepage, String originator, String packageFileName,
			SpdxPackageVerificationCode packageVerificationCode,
			String sourceInfo, String summary, String supplier,
			String versionInfo) {
		super(name, comment, annotations, relationships, licenseConcluded,
				licenseInfosFromFiles, copyrightText, licenseComment);
		this.licenseDeclared = licenseDeclared;
		this.checksums = checksums;
		this.description = description;
		this.downloadLocation = downloadLocation;
		this.files = files;
		this.homepage = homepage;
		this.originator = originator;
		this.packageFileName = packageFileName;
		this.packageVerificationCode = packageVerificationCode;	
		this.sourceInfo = sourceInfo;
		this.summary = summary;
		this.supplier = supplier;
		this.versionInfo = versionInfo;
	}
	
	public SpdxPackage(String name, AnyLicenseInfo licenseConcluded,
			AnyLicenseInfo[] licenseInfosFromFiles, String copyrightText,
			AnyLicenseInfo licenseDeclared, String downloadLocation, SpdxFile[] files,
			SpdxPackageVerificationCode packageVerificationCode) {
		this(name, null, null, null, licenseConcluded, 
				licenseInfosFromFiles, copyrightText, null, licenseDeclared,
				null, null, null, files, null, null, null, packageVerificationCode,
				null, null, null, null);
	}

	/**
	 * @param modelContainer
	 * @param node
	 * @throws InvalidSPDXAnalysisException
	 */
	public SpdxPackage(IModelContainer modelContainer, Node node)
			throws InvalidSPDXAnalysisException {
		super(modelContainer, node);
		this.licenseDeclared = findAnyLicenseInfoPropertyValue(SPDX_NAMESPACE, 
				PROP_PACKAGE_DECLARED_LICENSE);
		this.checksums = findMultipleChecksumPropertyValues(SPDX_NAMESPACE, 
				PROP_PACKAGE_CHECKSUM);
		this.description = findSinglePropertyValue(SPDX_NAMESPACE,
				PROP_PACKAGE_DESCRIPTION);
		this.downloadLocation = findSinglePropertyValue(SPDX_NAMESPACE,
				PROP_PACKAGE_DOWNLOAD_URL);
		this.homepage = findSinglePropertyValue(DOAP_NAMESPACE, 
				PROP_PROJECT_HOMEPAGE);
		this.originator = findSinglePropertyValue(SPDX_NAMESPACE,
				PROP_PACKAGE_ORIGINATOR);
		this.packageFileName = findSinglePropertyValue(SPDX_NAMESPACE,
				PROP_PACKAGE_FILE_NAME);
		this.packageVerificationCode = findVerificationCodePropertyValue(SPDX_NAMESPACE,
				PROP_PACKAGE_VERIFICATION_CODE);	
		this.sourceInfo = findSinglePropertyValue(SPDX_NAMESPACE,
				PROP_PACKAGE_SOURCE_INFO);
		this.summary = findSinglePropertyValue(SPDX_NAMESPACE,
				PROP_PACKAGE_SHORT_DESC);
		this.supplier = findSinglePropertyValue(SPDX_NAMESPACE,
				PROP_PACKAGE_SUPPLIER);
		this.versionInfo = findSinglePropertyValue(SPDX_NAMESPACE,
				PROP_PACKAGE_VERSION_INFO);
		SpdxElement[] filesE = findMultipleElementPropertyValues(SPDX_NAMESPACE,
				PROP_PACKAGE_FILE);
		this.files = new SpdxFile[filesE.length];
		for (int i = 0; i < filesE.length; i++) {
			if (!(filesE[i] instanceof SpdxFile)) {
				throw(new InvalidSPDXAnalysisException("Incorrect type for a file belonging to a package: "+filesE[i].getName()));
			}
			this.files[i] = (SpdxFile)filesE[i];
		}
	}
	
	@Override
	protected Resource findDuplicateResource(IModelContainer modelContainer, String uri) throws InvalidSPDXAnalysisException {
		// A duplicate package has the same name and verification code
		if (this.name == null || this.name.isEmpty()) {
			return null;
		}
		if (this.packageVerificationCode == null) {
			return null;
		}
		if (this.packageVerificationCode.getValue() == null || this.packageVerificationCode.getValue().isEmpty()) {
			return null;
		}
		Model localModel = modelContainer.getModel();
		// look for a matching name
		Node nameProperty = localModel.getProperty(SPDX_NAMESPACE, this.getNamePropertyName()).asNode();
		Node verificationCodeProperty = localModel.getProperty(SPDX_NAMESPACE, PROP_PACKAGE_VERIFICATION_CODE).asNode();
		Node verificationCodeValueProperty = localModel.getProperty(SPDX_NAMESPACE, PROP_VERIFICATIONCODE_VALUE).asNode();
		
		Triple nameMatch = Triple.createMatch(null, nameProperty, Node.createLiteral(this.name));
		ExtendedIterator<Triple> nameleIter = localModel.getGraph().find(nameMatch);	
		while (nameleIter.hasNext()) {
			Triple t = nameleIter.next();
			// Check for the package verification code
			Node packageNode = t.getSubject();
			Triple verifcationMatch = Triple.createMatch(packageNode, verificationCodeProperty, null);
			ExtendedIterator<Triple> verificationIter = localModel.getGraph().find(verifcationMatch);
			while (verificationIter.hasNext()) {
				Triple vt = verificationIter.next();
				Triple valueMatch = Triple.createMatch(vt.getObject(), verificationCodeValueProperty, null);
				ExtendedIterator<Triple> valueIter = localModel.getGraph().find(valueMatch);
				while (valueIter.hasNext()) {
					Triple valuetrip = valueIter.next();
					String verificationCodeValue = valuetrip.getObject().toString(false);
					if (this.packageVerificationCode.getValue().equals(verificationCodeValue)) {
						return RdfParserHelper.convertToResource(localModel, packageNode);
					}
				}
			}
		}		
		return null;	// if we got here, we didn't find a duplicate
	}
	
	@Override
	protected void populateModel() throws InvalidSPDXAnalysisException {
		super.populateModel();
		 setPropertyValue(SPDX_NAMESPACE, 
				PROP_PACKAGE_DECLARED_LICENSE, this.licenseDeclared);
		setPropertyValues(SPDX_NAMESPACE, 
				PROP_PACKAGE_CHECKSUM, this.checksums);
		setPropertyValue(SPDX_NAMESPACE,
				PROP_PACKAGE_DESCRIPTION, this.description);
		setPropertyValue(SPDX_NAMESPACE,
				PROP_PACKAGE_DOWNLOAD_URL, this.downloadLocation);
		setPropertyValue(DOAP_NAMESPACE, 
				PROP_PROJECT_HOMEPAGE, this.homepage);
		setPropertyValue(SPDX_NAMESPACE,
				PROP_PACKAGE_ORIGINATOR, this.originator);
		setPropertyValue(SPDX_NAMESPACE,
				PROP_PACKAGE_FILE_NAME, this.packageFileName);
		setPropertyValue(SPDX_NAMESPACE,
				PROP_PACKAGE_VERIFICATION_CODE, this.packageVerificationCode);	
		setPropertyValue(SPDX_NAMESPACE,
				PROP_PACKAGE_SOURCE_INFO, this.sourceInfo);
		setPropertyValue(SPDX_NAMESPACE,
				PROP_PACKAGE_SHORT_DESC, this.summary);
		setPropertyValue(SPDX_NAMESPACE,
				PROP_PACKAGE_SUPPLIER, this.supplier);
		setPropertyValue(SPDX_NAMESPACE,
				PROP_PACKAGE_VERSION_INFO, this.versionInfo);
		setPropertyValue(SPDX_NAMESPACE,
				PROP_PACKAGE_FILE, this.files);
	}

	@Override
	protected String getNamePropertyName() {
		return SpdxRdfConstants.PROP_PROJECT_NAME;
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.RdfModelObject#getType(com.hp.hpl.jena.rdf.model.Model)
	 */
	@Override
	Resource getType(Model model) {
		return model.createResource(SpdxRdfConstants.SPDX_NAMESPACE + SpdxRdfConstants.CLASS_SPDX_PACKAGE);
	}
	
	
	
	/**
	 * @return the licenseDeclared
	 * @throws InvalidSPDXAnalysisException 
	 */
	public AnyLicenseInfo getLicenseDeclared() throws InvalidSPDXAnalysisException {
		if (this.resource != null && refreshOnGet) {
			AnyLicenseInfo refresh = findAnyLicenseInfoPropertyValue(SPDX_NAMESPACE, 
					PROP_PACKAGE_DECLARED_LICENSE);
			if (!refresh.equals(this.licenseDeclared)) {
				this.licenseDeclared = refresh;
			}
		}
		return licenseDeclared;
	}

	/**
	 * @param licenseDeclared the licenseDeclared to set
	 * @throws InvalidSPDXAnalysisException 
	 */
	public void setLicenseDeclared(AnyLicenseInfo licenseDeclared) throws InvalidSPDXAnalysisException {
		this.licenseDeclared = licenseDeclared;
		 setPropertyValue(SPDX_NAMESPACE, 
					PROP_PACKAGE_DECLARED_LICENSE, this.licenseDeclared);
	}

	/**
	 * @return the checksums
	 * @throws InvalidSPDXAnalysisException 
	 */
	public Checksum[] getChecksums() throws InvalidSPDXAnalysisException {
		if (this.resource != null && refreshOnGet) {
			Checksum[] refresh = findMultipleChecksumPropertyValues(SPDX_NAMESPACE, 
					PROP_PACKAGE_CHECKSUM);
			if (!this.arraysEquivalent(refresh, this.checksums)) {
				this.checksums = refresh;
			}
		}
		return checksums;
	}

	/**
	 * @param checksums the checksums to set
	 * @throws InvalidSPDXAnalysisException 
	 */
	public void setChecksums(Checksum[] checksums) throws InvalidSPDXAnalysisException {
		this.checksums = checksums;
		setPropertyValues(SPDX_NAMESPACE, 
					PROP_PACKAGE_CHECKSUM, this.checksums);
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		if (this.resource != null && refreshOnGet) {
			this.description = findSinglePropertyValue(SPDX_NAMESPACE,
					PROP_PACKAGE_DESCRIPTION);
		}
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
		setPropertyValue(SPDX_NAMESPACE,
					PROP_PACKAGE_DESCRIPTION, this.description);
	}

	/**
	 * @return the downloadLocation
	 */
	public String getDownloadLocation() {
		if (this.resource != null && refreshOnGet) {
			this.downloadLocation = findSinglePropertyValue(SPDX_NAMESPACE,
					PROP_PACKAGE_DOWNLOAD_URL);
		}
		return downloadLocation;
	}

	/**
	 * @param downloadLocation the downloadLocation to set
	 */
	public void setDownloadLocation(String downloadLocation) {
		this.downloadLocation = downloadLocation;
		setPropertyValue(SPDX_NAMESPACE,
					PROP_PACKAGE_DOWNLOAD_URL, this.downloadLocation);
	}

	/**
	 * @return the homepage
	 */
	public String getHomepage() {
		if (this.resource != null && refreshOnGet) {
			this.homepage = findSinglePropertyValue(DOAP_NAMESPACE, 
					PROP_PROJECT_HOMEPAGE);
		}
		return homepage;
	}

	/**
	 * @param homepage the homepage to set
	 */
	public void setHomepage(String homepage) {
		this.homepage = homepage;
		setPropertyValue(DOAP_NAMESPACE, 
					PROP_PROJECT_HOMEPAGE, this.homepage);
	}

	/**
	 * @return the originator
	 */
	public String getOriginator() {
		if (this.resource != null && refreshOnGet) {
			this.originator = findSinglePropertyValue(SPDX_NAMESPACE,
					PROP_PACKAGE_ORIGINATOR);
		}
		return originator;
	}

	/**
	 * @param originator the originator to set
	 */
	public void setOriginator(String originator) {
		this.originator = originator;
		setPropertyValue(SPDX_NAMESPACE,
					PROP_PACKAGE_ORIGINATOR, this.originator);
	}

	/**
	 * @return the packageFileName
	 */
	public String getPackageFileName() {
		if (this.resource != null && refreshOnGet) {
			this.packageFileName = findSinglePropertyValue(SPDX_NAMESPACE,
					PROP_PACKAGE_FILE_NAME);
		}
		return packageFileName;
	}

	/**
	 * @param packageFileName the packageFileName to set
	 */
	public void setPackageFileName(String packageFileName) {
		this.packageFileName = packageFileName;
		setPropertyValue(SPDX_NAMESPACE,
					PROP_PACKAGE_FILE_NAME, this.packageFileName);
	}

	/**
	 * @return the packageVerificationCode
	 * @throws InvalidSPDXAnalysisException 
	 */
	public SpdxPackageVerificationCode getPackageVerificationCode() throws InvalidSPDXAnalysisException {
		if (this.resource != null && refreshOnGet) {
			SpdxPackageVerificationCode refresh = findVerificationCodePropertyValue(SPDX_NAMESPACE,
					PROP_PACKAGE_VERIFICATION_CODE);	
			if (refresh == null || !refresh.equivalent(this.packageVerificationCode)) {
				this.packageVerificationCode = refresh;
			}
		}
		return packageVerificationCode;
	}

	/**
	 * @param packageVerificationCode the packageVerificationCode to set
	 * @throws InvalidSPDXAnalysisException 
	 */
	public void setPackageVerificationCode(
			SpdxPackageVerificationCode packageVerificationCode) throws InvalidSPDXAnalysisException {
		this.packageVerificationCode = packageVerificationCode;
		setPropertyValue(SPDX_NAMESPACE,
					PROP_PACKAGE_VERIFICATION_CODE, this.packageVerificationCode);	
	}

	/**
	 * @return the sourceInfo
	 */
	public String getSourceInfo() {
		if (this.resource != null && refreshOnGet) {	
			this.sourceInfo = findSinglePropertyValue(SPDX_NAMESPACE,
					PROP_PACKAGE_SOURCE_INFO);
		}
		return sourceInfo;
	}

	/**
	 * @param sourceInfo the sourceInfo to set
	 */
	public void setSourceInfo(String sourceInfo) {
		this.sourceInfo = sourceInfo;	
		setPropertyValue(SPDX_NAMESPACE,
					PROP_PACKAGE_SOURCE_INFO, this.sourceInfo);
	}

	/**
	 * @return the summary
	 */
	public String getSummary() {
		if (this.resource != null && refreshOnGet) {
			this.summary = findSinglePropertyValue(SPDX_NAMESPACE,
					PROP_PACKAGE_SHORT_DESC);
		}
		return summary;
	}

	/**
	 * @param summary the summary to set
	 */
	public void setSummary(String summary) {
		this.summary = summary;
		setPropertyValue(SPDX_NAMESPACE,
					PROP_PACKAGE_SHORT_DESC, this.summary);
	}

	/**
	 * @return the supplier
	 */
	public String getSupplier() {
		if (this.resource != null && refreshOnGet) {
			this.supplier = findSinglePropertyValue(SPDX_NAMESPACE,
					PROP_PACKAGE_SUPPLIER);
		}
		return supplier;
	}

	/**
	 * @param supplier the supplier to set
	 */
	public void setSupplier(String supplier) {
		this.supplier = supplier;
		setPropertyValue(SPDX_NAMESPACE,
					PROP_PACKAGE_SUPPLIER, this.supplier);
	}

	/**
	 * @return the versionInfo
	 */
	public String getVersionInfo() {
		if (this.resource != null && refreshOnGet) {
			this.versionInfo = findSinglePropertyValue(SPDX_NAMESPACE,
					PROP_PACKAGE_VERSION_INFO);
		}
		return versionInfo;
	}

	/**
	 * @param versionInfo the versionInfo to set
	 */
	public void setVersionInfo(String versionInfo) {
		this.versionInfo = versionInfo;
		setPropertyValue(SPDX_NAMESPACE,
					PROP_PACKAGE_VERSION_INFO, this.versionInfo);
	}

	/**
	 * @return the files
	 * @throws InvalidSPDXAnalysisException 
	 */
	public SpdxFile[] getFiles() throws InvalidSPDXAnalysisException {
		if (this.resource != null && refreshOnGet) {
			SpdxElement[] filesE = findMultipleElementPropertyValues(SPDX_NAMESPACE,
					PROP_PACKAGE_FILE);
			if (!this.arraysEquivalent(filesE, this.files)) {
				this.files = new SpdxFile[filesE.length];
				for (int i = 0; i < filesE.length; i++) {
					if (!(filesE[i] instanceof SpdxFile)) {
						throw(new InvalidSPDXAnalysisException("Incorrect type for a file belonging to a package: "+filesE[i].getName()));
					}
					this.files[i] = (SpdxFile)filesE[i];
				}
			}
		}
		return files;
	}

	/**
	 * @param files the files to set
	 * @throws InvalidSPDXAnalysisException 
	 */
	public void setFiles(SpdxFile[] files) throws InvalidSPDXAnalysisException {
		this.files = files;
		setPropertyValue(SPDX_NAMESPACE,
				PROP_PACKAGE_FILE, this.files);
	}

	@Override
	public boolean equivalent(RdfModelObject o) {
		if (!(o instanceof SpdxPackage)) {
			return false;
		}
		if (!super.equivalent(o)) {
			return false;
		}
		SpdxPackage comp = (SpdxPackage)o;
		try {
		return (equalsConsideringNull(this.licenseDeclared, comp.getLicenseDeclared()) &&
				arraysEquivalent(this.checksums, comp.getChecksums()) &&
				equalsConsideringNull(this.description, comp.getDescription()) &&
				equalsConsideringNull(this.downloadLocation, comp.getDownloadLocation()) &&
				arraysEquivalent(this.files, comp.getFiles()) &&
				equalsConsideringNull(this.homepage, comp.getHomepage()) &&
				equalsConsideringNull(this.originator, comp.getOriginator()) &&
				equalsConsideringNull(this.packageFileName, comp.getPackageFileName()) &&
				equalsConsideringNull(this.packageVerificationCode, comp.getPackageVerificationCode()) &&
				equalsConsideringNull(this.sourceInfo, comp.getSourceInfo()) &&
				equalsConsideringNull(this.summary, comp.getSummary()) &&
				equalsConsideringNull(this.supplier, comp.getSupplier()) &&
				equalsConsideringNull(this.versionInfo, comp.getVersionInfo()));
		} catch (InvalidSPDXAnalysisException e) {
			logger.error("Invalid analysis exception on comparing equivalent: "+e.getMessage(),e);
			return false;
		}
	}
	
	@Override
	public SpdxPackage clone() {
		return new SpdxPackage(this.name, this.comment, this.cloneAnnotations(), 
				this.cloneRelationships(), this.cloneLicenseConcluded(),
				this.cloneLicenseInfosFromFiles(), this.copyrightText,
				this.licenseComment, this.cloneLicenseDeclared(), this.cloneCheckums(),
				this.description, this.downloadLocation, this.cloneFiles(),
				this.homepage, this.originator, this.packageFileName, 
				this.clonePackageVerificationCode(), this.sourceInfo, 
				this.summary, this.supplier, this.versionInfo);
	}

	/**
	 * @return
	 */
	private SpdxPackageVerificationCode clonePackageVerificationCode() {
		if (this.packageVerificationCode == null) {
			return null;
		}
		return new SpdxPackageVerificationCode(this.packageVerificationCode.getValue(),
				this.packageVerificationCode.getExcludedFileNames());
	}

	/**
	 * @return
	 */
	private SpdxFile[] cloneFiles() {
		if (this.files == null) {
			return new SpdxFile[0];
		}
		SpdxFile[] retval = new SpdxFile[this.files.length];
		for (int i = 0; i < files.length; i++) {
			retval[i] = this.files[i].clone();
		}
		return retval;
	}

	/**
	 * @return
	 */
	private AnyLicenseInfo cloneLicenseDeclared() {
		if (this.licenseDeclared == null) {
			return null;
		} else {
			return this.licenseDeclared.clone();
		}
	}

	/**
	 * @return
	 */
	private Checksum[] cloneCheckums() {
		if (this.checksums == null) {
			return new Checksum[0];
		}
		Checksum[] retval = new Checksum[this.checksums.length];
		for (int i = 0; i < this.checksums.length; i++) {
			retval[i] = this.checksums[i].clone();
		}
		return retval;
	}
	
	@Override
	public ArrayList<String> verify() {
		ArrayList<String> retval = super.verify();
		// summary - nothing really to check
	
		// description - nothing really to check

		// download location
		String downloadLocation = this.getDownloadLocation();
		if (downloadLocation == null || downloadLocation.isEmpty()) {
			retval.add("Missing required download location for package");
		}

		// checksum
		for (int i = 0; i < checksums.length; i++) {
			retval.addAll(checksums[i].verify());
		}

		// sourceinfo - nothing really to check
		
		// license declared - mandatory - 1 (need to change return values)
		try {
			AnyLicenseInfo declaredLicense = this.getLicenseDeclared();
			if (declaredLicense == null) {
				retval.add("Missing required declared license");
			} else {
				retval.addAll(declaredLicense.verify());
			}				
		} catch (InvalidSPDXAnalysisException e) {
			retval.add("Invalid package declared license: "+e.getMessage());
		}
		// hasFiles mandatory one or more
		try {
			SpdxFile[] files = this.getFiles();
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
			verificationCode = this.getPackageVerificationCode();
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
		supplier = this.getSupplier();
		if (supplier != null) {
			String error = SpdxVerificationHelper.verifySupplier(supplier);
			if (error != null && !error.isEmpty()) {
				retval.add("Supplier error - "+error);
			}
		}
		// originator
		String originator = this.getOriginator();
		if (originator != null) {
			String error = SpdxVerificationHelper.verifyOriginator(originator);
			if (error != null && !error.isEmpty()) {
				retval.add("Originator error - "+error);
			}
		}
		return retval;
	}
	
}
