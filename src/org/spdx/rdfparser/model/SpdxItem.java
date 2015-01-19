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

import org.spdx.rdfparser.IModelContainer;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SpdxRdfConstants;
import org.spdx.rdfparser.license.AnyLicenseInfo;

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
	AnyLicenseInfo licenseDeclared;
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
	}
	
	/**
	 * @param name Name of the item
	 * @param comment Optional comment about the item
	 * @param annotations Optional annotations on the items
	 * @param relationships Optional relationships with other SPDX elements
	 * @param licenseConcluded Concluded license for this item
	 * @param licenseDeclared Declared license for this item
	 * @param copyrightText Copyright text for this item
	 * @param licenseComment Optional comment on the license
	 */
	public SpdxItem(String name, String comment, Annotation[] annotations,
			Relationship[] relationships,AnyLicenseInfo licenseConcluded, 
			AnyLicenseInfo licenseDeclared, String copyrightText, 
			String licenseComment) {
		super(name, comment, annotations, relationships);
		this.licenseConcluded = licenseConcluded;
		this.licenseDeclared = licenseDeclared;
		this.copyrightText = copyrightText;
		this.licenseComment = licenseComment;
	}
	
	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.RdfModelObject#populateModel()
	 */
	protected void populateModel() throws InvalidSPDXAnalysisException {
		super.populateModel();
		if (this.model != null) {
			if (this.licenseConcluded != null) {
				setPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, 
						SpdxRdfConstants.PROP_LICENSE_CONCLUDED, licenseConcluded);
			}
			if (this.licenseDeclared != null) {
				setPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, 
						getLicenseDeclaredPropertyName(), licenseDeclared);
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
	 * @return Property name for licenseDeclared.  Override if using a subproperty of "licenseDeclared".
	 */
	protected String getLicenseDeclaredPropertyName() {
		return SpdxRdfConstants.PROP_LICENSE_DECLARED;
	}

	/**
	 * @return the licenseConcluded
	 */
	public AnyLicenseInfo getLicenseConcluded() {
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
	 * @return the licenseDeclared
	 */
	public AnyLicenseInfo getLicenseDeclared() {
		return licenseDeclared;
	}

	/**
	 * @param licenseDeclared the licenseDeclared to set
	 */
	public void setLicenseDeclared(AnyLicenseInfo licenseDeclared)  throws InvalidSPDXAnalysisException {
		this.licenseDeclared = licenseDeclared;
		setPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, 
				getLicenseDeclaredPropertyName(), licenseDeclared);
	}

	/**
	 * @return the copyrightText
	 */
	public String getCopyrightText() {
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
}
