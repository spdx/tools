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
import java.util.Arrays;

import org.spdx.rdfparser.IModelContainer;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SPDXCreatorInformation;
import org.spdx.rdfparser.SPDXReview;
import org.spdx.rdfparser.SpdxDocumentContainer;
import org.spdx.rdfparser.SpdxRdfConstants;
import org.spdx.rdfparser.license.AnyLicenseInfo;
import org.spdx.rdfparser.license.ExtractedLicenseInfo;
import org.spdx.rdfparser.license.SpdxListedLicense;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * An SpdxDocument is a summary of the contents, provenance, ownership and licensing 
 * analysis of a specific software package. 
 * This is, effectively, the top level of SPDX information.
 * 
 * Documents always have a model
 * 
 * @author Gary O'Neall
 *
 */
public class SpdxDocument extends SpdxElement {
	
	private SpdxDocumentContainer documentContainer;
	SpdxItem[] spdxItems;
	SPDXCreatorInformation creationInfo;	//TODO Refactor to RdfModelObject
	AnyLicenseInfo dataLicense;
	String specVersion;
	@Deprecated	// Replaced by annotations
	SPDXReview[] reviewers;			

	/**
	 * @param modelContainer
	 * @param node
	 * @throws InvalidSPDXAnalysisException
	 */
	public SpdxDocument(SpdxDocumentContainer documentContainer, Node node)
			throws InvalidSPDXAnalysisException {
		super(documentContainer, node);
		this.documentContainer = documentContainer;
		getMyPropertiesFromModel();
	}
	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.RdfModelObject#getPropertiesFromModel()
	 */
	@Override
	void getPropertiesFromModel() throws InvalidSPDXAnalysisException {
		super.getPropertiesFromModel();
		getMyPropertiesFromModel();
	}
	
	void getMyPropertiesFromModel() throws InvalidSPDXAnalysisException {
		dataLicense = findAnyLicenseInfoPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE,
				SpdxRdfConstants.PROP_SPDX_DATA_LICENSE);
		creationInfo = findCreationInfoPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE,
				SpdxRdfConstants.PROP_SPDX_CREATION_INFO);
		specVersion = findSinglePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, 
				SpdxRdfConstants.PROP_SPDX_VERSION);
		reviewers = findReviewPropertyValues(SpdxRdfConstants.SPDX_NAMESPACE, 
				SpdxRdfConstants.PROP_SPDX_REVIEWED_BY);
		spdxItems = findAllItems();
	}

	/**
	 * @return All SpdxItems considering all properties and subproerties
	 * @throws InvalidSPDXAnalysisException 
	 */
	private SpdxItem[] findAllItems() throws InvalidSPDXAnalysisException {
		// Packages use the PROP_SPDX_PACKAGE property
		SpdxElement[] packages = findMultipleElementPropertyValues(
				SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_SPDX_PACKAGE);
		SpdxElement[] files = findMultipleElementPropertyValues(
				SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_SPDX_DESCRIBES_FILE);
		SpdxItem[] retval = new SpdxItem[packages.length + files.length];
		for (int i = 0; i < packages.length; i++) {
			if (!(packages[i] instanceof SpdxItem)) {
				throw(new InvalidSPDXAnalysisException("Described package property value is not an SPDX Item type"));
			}
			retval[i] = (SpdxItem)packages[i];
		}
		for (int i = 0; i < files.length; i++) {
			if (!(files[i] instanceof SpdxItem)) {
				throw(new InvalidSPDXAnalysisException("Described file property value is not an SPDX Item type"));
			}
			retval[i + packages.length] = (SpdxItem)files[i];
		}
		return retval;
	}
	
	/**
	 * @return all SPDX items connected directly to this document.  Does not include
	 * children SPDX items (e.g. files within packages).
	 * @throws InvalidSPDXAnalysisException 
	 */
	public SpdxItem[] getSpdxItems() throws InvalidSPDXAnalysisException {
		if (this.resource != null && this.refreshOnGet) {
			SpdxItem[] refresh = findAllItems();
			if (refresh == null || !arraysEquivalent(refresh, this.spdxItems)) {
				this.spdxItems = refresh;
			}
		}
		return this.spdxItems;
	}
	
	SpdxItem[] getPackagesFromItems(SpdxItem[] items) {
		int count = 0;
		for (int i = 0; i < items.length; i++) {
			if (items[i] instanceof SpdxPackage) {
				count++;
			}
		}
		SpdxItem[] retval = new SpdxItem[count];
		count = 0;
		for (int i = 0; i < items.length; i++) {
			if (items[i] instanceof SpdxPackage) {
				retval[count++] = items[i];
			}
		}
		return retval;
	}
	
	SpdxItem[] getFilesFromItems(SpdxItem[] items) {
		int count = 0;
		for (int i = 0; i < items.length; i++) {
			if (items[i] instanceof SpdxFile) {
				count++;
			}
		}
		SpdxItem[] retval = new SpdxItem[count];
		count = 0;
		for (int i = 0; i < items.length; i++) {
			if (items[i] instanceof SpdxFile) {
				retval[count++] = items[i];
			}
		}
		return retval;
	}
	
	/**
	 * Sets the items which are connected directly to this document
	 * @param items
	 * @throws InvalidSPDXAnalysisException
	 */
	public void setSpdxItems(SpdxItem[] items) throws InvalidSPDXAnalysisException {
		this.spdxItems = items;
		if (items == null) {
			this.spdxItems = new SpdxItem[0];
		}
		// use the appropriate property names based on the item type
		if (this.resource != null) {
			setPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, 
					SpdxRdfConstants.PROP_SPDX_PACKAGE,
					getPackagesFromItems(this.spdxItems));
			setPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, 
					SpdxRdfConstants.PROP_SPDX_DESCRIBES_FILE,
					getFilesFromItems(this.spdxItems));
		}		
	}
	
	/**
	 * Adds an item to be directly connected to this document
	 * @param item
	 * @throws InvalidSPDXAnalysisException 
	 */
	public void addItem(SpdxItem item) throws InvalidSPDXAnalysisException {
		if (item == null) {
			return;
		}
		this.spdxItems = Arrays.copyOf(this.spdxItems, this.spdxItems.length + 1);
		this.spdxItems[this.spdxItems.length-1] = item;
		if (item instanceof SpdxPackage) {
			addPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, 
					SpdxRdfConstants.PROP_SPDX_PACKAGE, item);
		} else if (item instanceof SpdxFile) {
			addPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, 
					SpdxRdfConstants.PROP_SPDX_DESCRIBES_FILE, item);
		} else {
			throw(new InvalidSPDXAnalysisException("Invalid type for document item."));
		}
	}
	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.RdfModelObject#getUri(org.spdx.rdfparser.IModelContainer)
	 */
	@Override
	protected String getUri(IModelContainer modelContainer) throws InvalidSPDXAnalysisException {
		if (this.node != null && this.node.isURI()) {
			return this.node.getURI();
		} else {
			// for the document, the URI is the same as the namespace
			return modelContainer.getDocumentNamespace();
		}
	}
	
	/**
	 * @return The unique Document URI
	 * @throws InvalidSPDXAnalysisException 
	 */
	public String getDocumentUri() throws InvalidSPDXAnalysisException {
		return this.getUri(documentContainer);
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.RdfModelObject#getType(com.hp.hpl.jena.rdf.model.Model)
	 */
	@Override
	Resource getType(Model model) {
		return model.createResource(SpdxRdfConstants.SPDX_NAMESPACE + SpdxRdfConstants.CLASS_SPDX_DOCUMENT);
	}
	
	protected void populateModel() throws InvalidSPDXAnalysisException {
		super.populateModel();
		setPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE,
				SpdxRdfConstants.PROP_SPDX_DATA_LICENSE, this.dataLicense);
		setPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE,
				SpdxRdfConstants.PROP_SPDX_CREATION_INFO, this.creationInfo);
		setPropertyValues(SpdxRdfConstants.SPDX_NAMESPACE, 
				SpdxRdfConstants.PROP_SPDX_EXTRACTED_LICENSES, this.documentContainer.getExtractedLicenseInfos());
		setPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, 
				SpdxRdfConstants.PROP_SPDX_VERSION, specVersion);
		setPropertyValues(SpdxRdfConstants.SPDX_NAMESPACE, 
				SpdxRdfConstants.PROP_SPDX_REVIEWED_BY, reviewers);
		// set the IDs
		if (this.spdxItems != null) {
			setPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, 
					SpdxRdfConstants.PROP_SPDX_PACKAGE,
					getPackagesFromItems(this.spdxItems));
			setPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, 
					SpdxRdfConstants.PROP_SPDX_DESCRIBES_FILE,
					getFilesFromItems(this.spdxItems));
		}
	}
	
	/**
	 * @return the documentContainer
	 */
	public SpdxDocumentContainer getDocumentContainer() {
		return documentContainer;
	}

	/**
	 * @return the creationInfo
	 * @throws InvalidSPDXAnalysisException 
	 */
	public SPDXCreatorInformation getCreationInfo() throws InvalidSPDXAnalysisException {
		if (this.resource != null && this.refreshOnGet) {
			try {
				//TODO Once CreationInfo has been refactored to an RdfModelObjet, check for equivalent
				creationInfo = findCreationInfoPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE,
						SpdxRdfConstants.PROP_SPDX_CREATION_INFO);
			} catch (InvalidSPDXAnalysisException e) {
				logger.error("Error getting creationInfo from model");
				throw(e);
			}
		}
		return creationInfo;
	}

	/**
	 * @param creationInfo the creationInfo to set
	 */
	public void setCreationInfo(SPDXCreatorInformation creationInfo) {
		this.creationInfo = creationInfo;
		setPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE,
				SpdxRdfConstants.PROP_SPDX_CREATION_INFO, this.creationInfo);
	}

	/**
	 * @return the dataLicense
	 * @throws InvalidSPDXAnalysisException 
	 */
	public AnyLicenseInfo getDataLicense() throws InvalidSPDXAnalysisException {
		if (this.resource != null && this.refreshOnGet) {
			try {
				AnyLicenseInfo refresh = findAnyLicenseInfoPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE,
						SpdxRdfConstants.PROP_SPDX_DATA_LICENSE);
				if (refresh == null || !refresh.equals(this.dataLicense)) {
					this.dataLicense = refresh;
				}
			} catch (InvalidSPDXAnalysisException e) {
				logger.error("Error getting data license from model");
				throw(e);
			}
		}
		return this.dataLicense;
	}

	/**
	 * @param dataLicense the dataLicense to set
	 * @throws InvalidSPDXAnalysisException 
	 */
	public void setDataLicense(AnyLicenseInfo dataLicense) throws InvalidSPDXAnalysisException {
		this.dataLicense = dataLicense;
		setPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE,
				SpdxRdfConstants.PROP_SPDX_DATA_LICENSE, this.dataLicense);
	}

	/**
	 * @return the externalDocumentRefs
	 * @throws InvalidSPDXAnalysisException 
	 */
	public ExternalDocumentRef[] getExternalDocumentRefs() throws InvalidSPDXAnalysisException {
		return this.documentContainer.getExternalDocumentRefs();
	}

	/**
	 * @param externalDocumentRefs the externalDocumentRefs to set
	 * @throws InvalidSPDXAnalysisException 
	 */
	public void setExternalDocumentRefs(ExternalDocumentRef[] externalDocumentRefs) throws InvalidSPDXAnalysisException {
		this.documentContainer.setExternalDocumentRefs(externalDocumentRefs);
	}

	/**
	 * @return the extractedLicenseInfos
	 * @throws InvalidSPDXAnalysisException 
	 */
	public ExtractedLicenseInfo[] getExtractedLicenseInfos() throws InvalidSPDXAnalysisException {
		return this.documentContainer.getExtractedLicenseInfos();
	}

	
	/**
	 * @param extractedLicenseInfos the extractedLicenseInfos to set
	 * @throws InvalidSPDXAnalysisException 
	 */
	public void setExtractedLicenseInfos(
			ExtractedLicenseInfo[] extractedLicenseInfos) throws InvalidSPDXAnalysisException {
		this.documentContainer.setExtractedLicenseInfos(extractedLicenseInfos);
	}

	/**
	 * @return the specVersion
	 */
	
	public String getSpecVersion() {
		if (this.resource != null && this.refreshOnGet) {
			specVersion = findSinglePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, 
					SpdxRdfConstants.PROP_SPDX_VERSION);
		}
		return specVersion;
	}
	
	/**
	 * @return the reviewers
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Deprecated
	public SPDXReview[] getReviewers() throws InvalidSPDXAnalysisException {
		if (this.resource != null && this.refreshOnGet) {
			try {
				// Note - this will always create new objects which may be considered a bug
				// No intention to fix since the reviewers are deprecated as of 2.0
				reviewers = findReviewPropertyValues(SpdxRdfConstants.SPDX_NAMESPACE, 
						SpdxRdfConstants.PROP_SPDX_REVIEWED_BY);
			} catch (InvalidSPDXAnalysisException e) {
				logger.error("Error getting reviews from model");
				throw(e);
			}
		}
		return reviewers;
	}

	/**
	 * @param reviewers the reviewers to set
	 */
	@Deprecated
	public void setReviewers(SPDXReview[] reviewers) {
		this.reviewers = reviewers;
		setPropertyValues(SpdxRdfConstants.SPDX_NAMESPACE, 
				SpdxRdfConstants.PROP_SPDX_REVIEWED_BY, reviewers);
	}

	/**
	 * @param specVersion the specVersion to set
	 */
	public void setSpecVersion(String specVersion) {
		this.specVersion = specVersion;
		setPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, 
				SpdxRdfConstants.PROP_SPDX_VERSION, specVersion);
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.IRdfModel#verify()
	 */
	@Override
	public ArrayList<String> verify() {
		ArrayList<String> retval = super.verify();
		// specVersion
		String docSpecVersion = "";	// note - this is used later in verify to verify version specific info
		if (this.specVersion == null || this.specVersion.isEmpty()) {
			retval.add("Missing required SPDX version");
			docSpecVersion = "UNKNOWN";
		} else {
			docSpecVersion = this.specVersion;
			String verify = this.documentContainer.verifySpdxVersion(docSpecVersion);
			if (verify != null) {
				retval.add(verify);
			}			
		}
		// creationInfo
		try {
			SPDXCreatorInformation creator = this.getCreationInfo();
			if (creator == null) {
				retval.add("Missing required Creator");
			} else {
				ArrayList<String> creatorVerification = creator.verify();
				retval.addAll(creatorVerification);
			}
		} catch (InvalidSPDXAnalysisException e) {
			retval.add("Invalid creator information: "+e.getMessage());
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
		// Extracted licensine infos
		try {
			ExtractedLicenseInfo[] extractedLicInfos = this.getExtractedLicenseInfos();
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
		if (!docSpecVersion.equals(SpdxDocumentContainer.POINT_EIGHT_SPDX_VERSION) && 
				!docSpecVersion.equals(SpdxDocumentContainer.POINT_NINE_SPDX_VERSION)) { // added as a mandatory field in 1.0
			try {
				AnyLicenseInfo dataLicense = this.getDataLicense();
				if (dataLicense == null) {
					retval.add("Missing required data license");
				} else {
					if (!(dataLicense instanceof SpdxListedLicense)) {
						retval.add("Invalid license type for data license - must be an SPDX Listed license");
					} else {
						if (docSpecVersion.equals(SpdxDocumentContainer.ONE_DOT_ZERO_SPDX_VERSION)) 
							{ 
							if (!((SpdxListedLicense)dataLicense).getLicenseId().equals(
									SpdxDocumentContainer.SPDX_DATA_LICENSE_ID_VERSION_1_0)) {
								retval.add("Incorrect data license for SPDX version 1.0 document - found "+
										((SpdxListedLicense)dataLicense).getLicenseId()+", expected "+
										SpdxDocumentContainer.SPDX_DATA_LICENSE_ID_VERSION_1_0);
							}
						} else {
							if (!((SpdxListedLicense)dataLicense).getLicenseId().equals(
									SpdxDocumentContainer.SPDX_DATA_LICENSE_ID)) {
								retval.add("Incorrect data license for SPDX document - found "+
										((SpdxListedLicense)dataLicense).getLicenseId()+
									", expected "+SpdxDocumentContainer.SPDX_DATA_LICENSE_ID);
							}					
						}
					}
				}
			} catch (InvalidSPDXAnalysisException e) {
				retval.add("Invalid data license: "+e.getMessage());
			}
		}
		// External document references
		try {
			ExternalDocumentRef[] externalRefs = this.getExternalDocumentRefs();
			for (int i = 0; i < externalRefs.length; i++) {
				retval.addAll(externalRefs[i].verify());
			}
		} catch (InvalidSPDXAnalysisException e) {
			retval.add("Invalid external document references: "+e.getMessage());
		}
		// Elements
		try {
			SpdxItem[] items = getSpdxItems();
			for (int i = 0; i < items.length; i++) {
				retval.addAll(items[i].verify());
			}
		} catch (InvalidSPDXAnalysisException e) {
			retval.add("Invalid document items: "+e.getMessage());
		}
		return retval;
	} 

	@Override
	public boolean equivalent(RdfModelObject o) {
		if (!(o instanceof SpdxDocument)) {
			return false;
		}
		if (!(super.equivalent(o))) {
			return false;
		}
		SpdxDocument comp = (SpdxDocument)o;
		try {
		return (equalsConsideringNull(this.creationInfo, comp.getCreationInfo()) &&
				equalsConsideringNull(this.dataLicense, comp.getDataLicense()) &&
				arraysEquivalent(this.getExternalDocumentRefs(), comp.getExternalDocumentRefs()) &&
				arraysEqual(this.getExtractedLicenseInfos(), comp.getExtractedLicenseInfos()) &&
				arraysEqual(this.reviewers, comp.getReviewers()) &&
				arraysEquivalent(this.spdxItems, comp.getSpdxItems()) &&
				equalsConsideringNull(this.specVersion, comp.getSpecVersion()));
		} catch (InvalidSPDXAnalysisException ex) {
			logger.error("Error testing for equivalent",ex);
			return false;
		}
	}
	
	@Override
	protected String getNamePropertyName() {
		return SpdxRdfConstants.PROP_NAME;
	}
	//NOTE: We can  not implement clone since there is only one SPDX document per model


	/**
	 * This method has been replaced by getSpecVersion to match the specification property name
	 * @return 
	 */
	@Deprecated
	public String getSpdxVersion() {
		return this.getSpecVersion();
	}


	/**
	 * This method has been replaced by getCreationInfo to match the specification property name
	 * @return
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Deprecated
	public SPDXCreatorInformation getCreatorInfo() throws InvalidSPDXAnalysisException {
		return this.getCreationInfo();
	}


	/**
	 * This method has been replaced by getComment to match the specification property name
	 * @return
	 */
	@Deprecated
	public String getDocumentComment() {
		return this.getComment();
	}
	/**
	 * This method has been replaced by getSpdxItems
	 * This method will fail unless there is one and only 1 SPDX document
	 * @return
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Deprecated
	public SpdxPackage getSpdxPackage() throws InvalidSPDXAnalysisException {
		SpdxItem[] retval = getPackagesFromItems(this.spdxItems);
		if (retval.length != 1) {
			throw(new InvalidSPDXAnalysisException("More than one SPDX package defined in the document.  Must use getSpdxItems - Likely this application has not been upgraded for SPDX 2.0"));
		}
		return (SpdxPackage)retval[0];
	}
	/**
	 * @param license license to be added to the extracted licensing infos
	 * @throws InvalidSPDXAnalysisException 
	 */
	public void addExtractedLicenseInfos(
			ExtractedLicenseInfo license) throws InvalidSPDXAnalysisException {
		this.documentContainer.addExtractedLicenseInfos(license);
	}
}
