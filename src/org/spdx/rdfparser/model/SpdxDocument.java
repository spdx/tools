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

import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SPDXCreatorInformation;
import org.spdx.rdfparser.SPDXReview;
import org.spdx.rdfparser.SpdxDocumentContainer;
import org.spdx.rdfparser.SpdxRdfConstants;
import org.spdx.rdfparser.license.AnyLicenseInfo;
import org.spdx.rdfparser.license.ExtractedLicenseInfo;

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
	SPDXCreatorInformation creationInfo;	//TODO Refactor to RdfModelObject
	AnyLicenseInfo dataLicense;
	ExternalDocumentRef[] externalDocumentRefs;
	ExtractedLicenseInfo[] extractedLicenseInfos;
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
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.RdfModelObject#getType(com.hp.hpl.jena.rdf.model.Model)
	 */
	@Override
	Resource getType(Model model) {
		return model.createResource(SpdxRdfConstants.SPDX_NAMESPACE + SpdxRdfConstants.CLASS_SPDX_ANALYSIS);
	}
	
	protected void populateModel() throws InvalidSPDXAnalysisException {
		super.populateModel();
		//TODO Implement
	}
	
	
	
	/**
	 * @return the documentContainer
	 */
	public SpdxDocumentContainer getDocumentContainer() {
		return documentContainer;
	}


	/**
	 * @return the creationInfo
	 */
	public SPDXCreatorInformation getCreationInfo() {
		return creationInfo;
	}

	/**
	 * @param creationInfo the creationInfo to set
	 */
	public void setCreationInfo(SPDXCreatorInformation creationInfo) {
		this.creationInfo = creationInfo;
	}

	/**
	 * @return the dataLicense
	 */
	public AnyLicenseInfo getDataLicense() {
		return dataLicense;
	}

	/**
	 * @param dataLicense the dataLicense to set
	 */
	public void setDataLicense(AnyLicenseInfo dataLicense) {
		this.dataLicense = dataLicense;
	}

	/**
	 * @return the externalDocumentRefs
	 */
	public ExternalDocumentRef[] getExternalDocumentRefs() {
		return externalDocumentRefs;
	}

	/**
	 * @param externalDocumentRefs the externalDocumentRefs to set
	 */
	public void setExternalDocumentRefs(ExternalDocumentRef[] externalDocumentRefs) {
		this.externalDocumentRefs = externalDocumentRefs;
	}

	/**
	 * @return the extractedLicenseInfos
	 */
	public ExtractedLicenseInfo[] getExtractedLicenseInfos() {
		return extractedLicenseInfos;
	}

	/**
	 * @return the specVersion
	 */
	
	public String getSpecVersion() {
		return specVersion;
	}
	
	

	/**
	 * @return the reviewers
	 */
	@Deprecated
	public SPDXReview[] getReviewers() {
		return reviewers;
	}

	/**
	 * @param reviewers the reviewers to set
	 */
	@Deprecated
	public void setReviewers(SPDXReview[] reviewers) {
		this.reviewers = reviewers;
	}

	/**
	 * @param specVersion the specVersion to set
	 */
	public void setSpecVersion(String specVersion) {
		this.specVersion = specVersion;
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.IRdfModel#verify()
	 */
	@Override
	public ArrayList<String> verify() {
		//TODO Implment
		return null;
	}
	
	@Override
	public boolean equivalent(RdfModelObject o) {
		// TODO Implement
		return false;
	}
	
	@Override
	protected String getNamePropertyName() {
		return SpdxRdfConstants.PROP_NAME;
	}


	//NOTE: We can  not implement clone since there is only one SPDX document per model
}
