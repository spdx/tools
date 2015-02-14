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
import java.util.HashMap;

import org.spdx.rdfparser.IModelContainer;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.RdfModelHelper;
import org.spdx.rdfparser.SpdxRdfConstants;
import org.spdx.rdfparser.license.AnyLicenseInfo;
import org.spdx.rdfparser.license.OrLaterOperator;
import org.spdx.rdfparser.license.SimpleLicensingInfo;
import org.spdx.rdfparser.license.SpdxNoAssertionLicense;
import org.spdx.rdfparser.license.SpdxNoneLicense;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * An SpdxItem is a potentially copyrightable work.
 * @author Gary O'Neall
 *
 */
public class SpdxItem extends SpdxElement {
	
	AnyLicenseInfo licenseConcluded;
	AnyLicenseInfo[] licenseInfoFromFiles;
	String copyrightText;
	String licenseComment;

	/**
	 * Create an SPDX item from a Jena model
	 * @param modelContainer Container containing the model
	 * @param node
	 * @throws InvalidSPDXAnalysisException
	 */
	public SpdxItem(IModelContainer modelContainer, Node node)
			throws InvalidSPDXAnalysisException {
		super(modelContainer, node);
		getMyPropertiesFromModel();
	}
	
	/**
	 * 
	 */
	private void getMyPropertiesFromModel() throws InvalidSPDXAnalysisException {
		this.copyrightText = findSinglePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_COPYRIGHT_TEXT);
		this.licenseComment = findSinglePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_LIC_COMMENTS);
		this.licenseConcluded = findAnyLicenseInfoPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, 
				SpdxRdfConstants.PROP_LICENSE_CONCLUDED);
		this.licenseInfoFromFiles = findAnyLicenseInfoPropertyValues(SpdxRdfConstants.SPDX_NAMESPACE, 
				getLicenseInfoFromFilesPropertyName());
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.RdfModelObject#getPropertiesFromModel()
	 */
	@Override
	void getPropertiesFromModel() throws InvalidSPDXAnalysisException {
		super.getPropertiesFromModel();
		getMyPropertiesFromModel();
	}
	/**
	 * @param name Name of the item
	 * @param comment Optional comment about the item
	 * @param annotations Optional annotations on the items
	 * @param relationships Optional relationships with other SPDX elements
	 * @param licenseConcluded Concluded license for this item
	 * @param licenseInfoFromFiles License infos from files for this item
	 * @param copyrightText Copyright text for this item
	 * @param licenseComment Optional comment on the license
	 */
	public SpdxItem(String name, String comment, Annotation[] annotations,
			Relationship[] relationships,AnyLicenseInfo licenseConcluded, 
			AnyLicenseInfo[] licenseInfoFromFiles, String copyrightText, 
			String licenseComment) {
		super(name, comment, annotations, relationships);
		this.licenseConcluded = licenseConcluded;
		this.licenseInfoFromFiles = licenseInfoFromFiles;
		if (this.licenseInfoFromFiles == null) {
			this.licenseInfoFromFiles = new AnyLicenseInfo[0];
		}
		this.copyrightText = copyrightText;
		this.licenseComment = licenseComment;
	}
	
	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.RdfModelObject#populateModel()
	 */
	protected void populateModel() throws InvalidSPDXAnalysisException {
		super.populateModel();
		if (this.resource != null) {
			if (this.licenseConcluded != null) {
				setPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, 
						SpdxRdfConstants.PROP_LICENSE_CONCLUDED, licenseConcluded);
			}
			if (this.licenseInfoFromFiles != null) {
				setPropertyValues(SpdxRdfConstants.SPDX_NAMESPACE, 
						getLicenseInfoFromFilesPropertyName(), licenseInfoFromFiles);
			}
			if (this.copyrightText != null) {
				setPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_COPYRIGHT_TEXT, copyrightText);
			}
			if (this.licenseComment != null) {
				setPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_LIC_COMMENTS, licenseComment);
			}
		}
	}

	/**
	 * @return Property name for licenseInfoFromFiles.  Override if using a subproperty of "licenseDeclared".
	 */
	protected String getLicenseInfoFromFilesPropertyName() {
		return SpdxRdfConstants.PROP_PACKAGE_LICENSE_INFO_FROM_FILES;
	}

	/**
	 * @return the licenseConcluded
	 */
	public AnyLicenseInfo getLicenseConcluded() {
		if (this.resource != null && this.refreshOnGet) {
			try {
				AnyLicenseInfo refresh = findAnyLicenseInfoPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, 
						SpdxRdfConstants.PROP_LICENSE_CONCLUDED);
				if (refresh == null || !refresh.equals(this.licenseConcluded)) {
					this.licenseConcluded = refresh;
				}
			} catch (InvalidSPDXAnalysisException e) {
				logger.error("Invalid licenseConcluded in model",e);
			}
		}
		return licenseConcluded;
	}

	/**
	 * @param licenseConcluded the licenseConcluded to set
	 * @throws InvalidSPDXAnalysisException 
	 */
	public void setLicenseConcluded(AnyLicenseInfo licenseConcluded) throws InvalidSPDXAnalysisException {
		this.licenseConcluded = licenseConcluded;
		setPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_LICENSE_CONCLUDED, licenseConcluded);
	}

	/**
	 * @return the licenseInfoFromFiles 
	 */
	public AnyLicenseInfo[] getLicenseInfoFromFiles() {
		if (this.resource != null && this.refreshOnGet) {
			try {
				AnyLicenseInfo[] refresh = findAnyLicenseInfoPropertyValues(SpdxRdfConstants.SPDX_NAMESPACE, 
						getLicenseInfoFromFilesPropertyName());
				if (!RdfModelHelper.arraysEqual(refresh, this.licenseInfoFromFiles)) {
					this.licenseInfoFromFiles = refresh;
				}
			} catch (InvalidSPDXAnalysisException e) {
				logger.error("Invalid licenseDeclared in model",e);
			}
		}
		return licenseInfoFromFiles;
	}

	/**
	 * @param licenseInfoFromFiles the licenseInfoFromFiles to set
	 */
	public void setLicenseInfosFromFiles(AnyLicenseInfo[] licenseInfoFromFiles)  throws InvalidSPDXAnalysisException {
		this.licenseInfoFromFiles = licenseInfoFromFiles;
		setPropertyValues(SpdxRdfConstants.SPDX_NAMESPACE, 
				getLicenseInfoFromFilesPropertyName(), licenseInfoFromFiles);
	}

	/**
	 * @return the copyrightText
	 */
	public String getCopyrightText() {
		if (this.resource != null && this.refreshOnGet) {
			this.copyrightText = findSinglePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_COPYRIGHT_TEXT);
		}
		return copyrightText;
	}

	/**
	 * @param copyrightText the copyrightText to set
	 */
	public void setCopyrightText(String copyrightText) {
		this.copyrightText = copyrightText;
		setPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_COPYRIGHT_TEXT, copyrightText);
	}

	/**
	 * @return the licenseComment
	 */
	public String getLicenseComment() {
		if (this.resource != null && this.refreshOnGet) {
			this.licenseComment = findSinglePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_LIC_COMMENTS);
		}
		return licenseComment;
	}

	/**
	 * @param licenseComment the licenseComment to set
	 */
	public void setLicenseComment(String licenseComment) {
		this.licenseComment = licenseComment;
		setPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_LIC_COMMENTS, licenseComment);
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.SpdxElement#getType(com.hp.hpl.jena.rdf.model.Model)
	 */
	@Override
	Resource getType(Model model) {
		return model.createResource(SpdxRdfConstants.SPDX_NAMESPACE + SpdxRdfConstants.CLASS_SPDX_ITEM);
	}
	
	@Override
	public boolean equivalent(IRdfModel o) {
		if (!(o instanceof SpdxItem)) {
			return false;
		}
		SpdxItem comp = (SpdxItem)o;
		if (!super.equivalent(comp)) {
			return false;
		}
		return (RdfModelHelper.equalsConsideringNull(this.copyrightText, comp.getCopyrightText()) &&
				RdfModelHelper.equivalentConsideringNull(this.licenseConcluded, comp.getLicenseConcluded()) &&
				RdfModelHelper.arraysEquivalent(this.licenseInfoFromFiles, comp.getLicenseInfoFromFiles()) &&
				RdfModelHelper.equalsConsideringNull(this.licenseComment, comp.getLicenseComment()));
	}
	
	protected AnyLicenseInfo cloneLicenseConcluded() {
		if (this.licenseConcluded == null) {
			return null;
		}
		return this.licenseConcluded.clone();
	}
	
	
	protected AnyLicenseInfo[] cloneLicenseInfosFromFiles() {
		if (this.licenseInfoFromFiles == null) {
			return new AnyLicenseInfo[0];
		}
		AnyLicenseInfo[] retval = new AnyLicenseInfo[this.licenseInfoFromFiles.length];
		for (int i = 0; i < this.licenseInfoFromFiles.length; i++) {
			retval[i] = this.licenseInfoFromFiles[i].clone();
		}
		return retval;
	}
	
	@Override
	public SpdxItem clone() {
		return clone(new HashMap<String, SpdxElement>());
	}
	
	public SpdxItem clone(HashMap<String, SpdxElement> clonedElementIds) {
		if (clonedElementIds.containsKey(this.getId())) {
			return (SpdxItem)clonedElementIds.get(this.getId());
		}
		SpdxItem retval =  new SpdxItem(this.name, this.comment, cloneAnnotations(),null,
				cloneLicenseConcluded(), cloneLicenseInfosFromFiles(), this.copyrightText, 
				this.licenseComment);
		clonedElementIds.put(this.getId(), retval);
		try {
			retval.setRelationships(cloneRelationships(clonedElementIds));
		} catch (InvalidSPDXAnalysisException e) {
			logger.error("Unexected error setting relationships during clone",e);
		}
		return retval;
	}
	
	@Override
	public ArrayList<String> verify() {
		ArrayList<String> retval = super.verify();
		String name = "UNKNOWN";
		if (this.name != null) {
			name = this.name;
		}
		if (this.licenseConcluded == null) {
			retval.add("Missing required concluded license for "+name);
		}
		if (this.copyrightText == null) {
			retval.add("Missing required copyright text for "+name);
		}
		if (this.licenseInfoFromFiles == null || this.licenseInfoFromFiles.length == 0) {
			retval.add("Missing required license information from files for "+name);
		} else {
			boolean foundNonSimpleLic = false;
			for (int i = 0; i < this.licenseInfoFromFiles.length; i++) {
				AnyLicenseInfo lic = this.licenseInfoFromFiles[i];
				if (!(lic instanceof SimpleLicensingInfo ||
						lic instanceof SpdxNoAssertionLicense ||
						lic instanceof SpdxNoneLicense ||
						lic instanceof OrLaterOperator)) {
					foundNonSimpleLic = true;
					break;
				}
			}
			if (foundNonSimpleLic) {
				retval.add("license info from files contains complex licenses for "+name);
			}
		}
		addNameToWarnings(retval);
		return retval;
	}
}
