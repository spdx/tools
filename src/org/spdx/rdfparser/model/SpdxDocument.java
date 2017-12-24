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

import java.util.Calendar;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.spdx.rdfparser.IModelContainer;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.RdfModelHelper;
import org.spdx.rdfparser.SPDXCreatorInformation;
import org.spdx.rdfparser.SPDXReview;
import org.spdx.rdfparser.SpdxDocumentContainer;
import org.spdx.rdfparser.SpdxRdfConstants;
import org.spdx.rdfparser.license.AnyLicenseInfo;
import org.spdx.rdfparser.license.ExtractedLicenseInfo;
import org.spdx.rdfparser.license.ListedLicenses;
import org.spdx.rdfparser.license.SpdxListedLicense;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.common.base.Objects;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;

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
@JsonPropertyOrder({"specVersion", "creationInfo", "spdxVersion", "dataLicense", "id", "name", 
	"comment", "documentNamespace",	"externalDocumentRefs", "documentDescribes", "extractedLicenseInfos", 
	"annotations", "relationships", "reviewers"})
public class SpdxDocument extends SpdxElement {
	
	@JsonIgnore
	private SpdxDocumentContainer documentContainer;
	SPDXCreatorInformation creationInfo;	//TODO Refactor to RdfModelObject
	AnyLicenseInfo dataLicense;
	String specVersion;
	@Deprecated	// Replaced by annotations
	SPDXReview[] reviewers;			

	/**
	 * @param documentContainer
	 * @param node
	 * @throws InvalidSPDXAnalysisException
	 */
	public SpdxDocument(SpdxDocumentContainer documentContainer, Node node)
			throws InvalidSPDXAnalysisException {
		super(documentContainer, node);
		this.documentContainer = documentContainer;
		getMyPropertiesFromModel();
		if (this.getCreationInfo() == null){
			String licenseListVersion = ListedLicenses.getListedLicenses().getLicenseListVersion();
			String creationDate = DateFormatUtils.format(Calendar.getInstance(), SpdxRdfConstants.SPDX_DATE_FORMAT);
			SPDXCreatorInformation creationInfo = new SPDXCreatorInformation(new String[] {  }, creationDate, null, licenseListVersion);
			setCreationInfo(creationInfo);
		}
		else if (StringUtils.isBlank(this.getCreationInfo().getLicenseListVersion())){
			this.getCreationInfo().setLicenseListVersion(ListedLicenses.getListedLicenses().getLicenseListVersion());
		}

	}
	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.RdfModelObject#getPropertiesFromModel()
	 */
	@Override
	public void getPropertiesFromModel() throws InvalidSPDXAnalysisException {
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
	}


	/**
	 * @return all SPDX items connected directly to this document.  Does not include
	 * children SPDX items (e.g. files within packages).
	 * @throws InvalidSPDXAnalysisException 
	 */
	public SpdxItem[] getDocumentDescribes() throws InvalidSPDXAnalysisException {
		Relationship[] allRelationships = this.getRelationships();
		int count = 0;
		for (int i = 0; i < allRelationships.length; i++) {
			if (allRelationships[i].getRelationshipType() == Relationship.RelationshipType.DESCRIBES &&
					allRelationships[i].getRelatedSpdxElement() instanceof SpdxItem) {
				count++;
			}
		}
		SpdxItem[] refresh = new SpdxItem[count];
		int refreshIndex = 0;
		for (int i = 0; i < allRelationships.length; i++) {
			if (allRelationships[i].getRelationshipType() == Relationship.RelationshipType.DESCRIBES &&
					allRelationships[i].getRelatedSpdxElement() instanceof SpdxItem) {
				refresh[refreshIndex++] = (SpdxItem)allRelationships[i].getRelatedSpdxElement();
			}
		}
		return refresh;
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

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.RdfModelObject#getUri(org.spdx.rdfparser.IModelContainer)
	 */
	@Override
	public String getUri(IModelContainer modelContainer) throws InvalidSPDXAnalysisException {
		if (this.node != null && this.node.isURI()) {
			return this.node.getURI();
		} else {
			// for the document, the URI is the same as the namespace
			return modelContainer.getDocumentNamespace();
		}
	}
	
	/**
	 * @return The unique Document Namespace
	 * @throws InvalidSPDXAnalysisException 
	 */
	@JsonIgnore
	public String getDocumentUri() throws InvalidSPDXAnalysisException {
		return this.getUri(documentContainer);
	}
	
	@Override
	public String getDocumentNamespace() throws InvalidSPDXAnalysisException {
		String[] parts = this.getDocumentUri().split("#");
		return parts[0];
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.RdfModelObject#getType(org.apache.jena.rdf.model.Model)
	 */
	@Override
	public Resource getType(Model model) {
		return model.createResource(SpdxRdfConstants.SPDX_NAMESPACE + SpdxRdfConstants.CLASS_SPDX_DOCUMENT);
	}
	
	@Override
	public void populateModel() throws InvalidSPDXAnalysisException {
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
	
	@JsonGetter("dataLicense")
	public String getDataLicenseStr() throws InvalidSPDXAnalysisException {
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
		return this.dataLicense.toString();
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
	public List<String> verify() {
		List<String> retval = super.verify();
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
				List<String> creatorVerification = creator.verify();
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
					List<String> reviewerVerification = reviews[i].verify();
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
					List<String> extractedLicInfoVerification = extractedLicInfos[i].verify();
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
		// documentDescribes relationships
		try {
			SpdxItem[] items = getDocumentDescribes();
			if (items.length == 0) {
				retval.add("Document must have at least one relationship of type DOCUMENT_DESCRIBES");
			}
		} catch (InvalidSPDXAnalysisException e) {
			retval.add("Invalid document items: "+e.getMessage());
		}
		try {
			List<SpdxElement> allElements = documentContainer.findAllElements();
			for (SpdxElement element:allElements) {
				if (!element.getId().equals(this.getId())) {
					retval.addAll(element.verify());
				}				
			}
		} catch (InvalidSPDXAnalysisException e) {
			retval.add("Invalid elements: "+e.getMessage());
		}
		return retval;
	} 
	
	@Override
	public boolean equivalent(IRdfModel o) {
		return this.equivalent(o, true);
	}

	@Override
	public boolean equivalent(IRdfModel o, boolean testRelationships) {
		if (o == this) {
			return true;
		}
		if (!(o instanceof SpdxDocument)) {
			return false;
		}
		if (!(super.equivalent(o, testRelationships))) {
			return false;
		}
		SpdxDocument comp = (SpdxDocument)o;
		try {
            return (Objects.equal(this.creationInfo, comp.getCreationInfo()) &&
                    Objects.equal(this.dataLicense, comp.getDataLicense()) &&
				arraysEquivalent(this.getExternalDocumentRefs(), comp.getExternalDocumentRefs(), testRelationships) &&
				RdfModelHelper.arraysEqual(this.getExtractedLicenseInfos(), comp.getExtractedLicenseInfos()) &&
                    RdfModelHelper.arraysEqual(this.reviewers, comp.getReviewers()) && Objects.equal(this.specVersion, comp.getSpecVersion()));
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
	@JsonIgnore
	@Deprecated
	public SPDXCreatorInformation getCreatorInfo() throws InvalidSPDXAnalysisException {
		return this.getCreationInfo();
	}


	/**
	 * This method has been replaced by getComment to match the specification property name
	 * @return
	 */
	@JsonIgnore
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
	@JsonIgnore
	public SpdxPackage getSpdxPackage() throws InvalidSPDXAnalysisException {
		SpdxItem[] retval = this.getDocumentDescribes();
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
		this.documentContainer.addExtractedLicenseInfos( license );
	}
}
