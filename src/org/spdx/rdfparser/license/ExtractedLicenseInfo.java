/**
 * Copyright (c) 2011 Source Auditor Inc.
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
package org.spdx.rdfparser.license;

import java.util.List;

import org.spdx.compare.LicenseCompareHelper;
import org.spdx.rdfparser.IModelContainer;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SpdxRdfConstants;
import org.spdx.rdfparser.SpdxVerificationHelper;
import org.spdx.rdfparser.model.IRdfModel;

import com.google.common.collect.Lists;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * An ExtractedLicensingInfo represents a license or licensing notice that was found in the package. 
 * Any license text that is recognized as a license may be represented as a License 
 * rather than an ExtractedLicensingInfo.
 * @author Gary O'Neall
 *
 */
public class ExtractedLicenseInfo extends SimpleLicensingInfo implements Comparable<ExtractedLicenseInfo> {
	
	private String extractedText;

	
	/**
	 * @param modelContainer container which includes the license
	 * @param licenseInfoNode Node that defines the ExtractedLicenseInfo
	 * @throws InvalidSPDXAnalysisException 
	 */
	public ExtractedLicenseInfo(IModelContainer modelContainer, Node licenseInfoNode) throws InvalidSPDXAnalysisException {
		super(modelContainer, licenseInfoNode);
		getPropertiesFromModel();
	}
	
	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.RdfModelObject#getPropertiesFromModel()
	 */
	@Override
	public void getPropertiesFromModel() throws InvalidSPDXAnalysisException {
		super.getPropertiesFromModel();
		// Text
		this.extractedText = findSinglePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_EXTRACTED_TEXT);
	}
	/**
	 * @param id licenseID
	 * @param text license text
	 * @param licenseName license name
	 * @param crossReferenceUrls Optional URL's that refer to the same license
	 * @param comment optional comment
	 */
	public ExtractedLicenseInfo(String id, String text, String licenseName, String[] crossReferenceUrls, String comment) {
		super(licenseName, id, comment, crossReferenceUrls);
		this.extractedText = text;
	}

	/**
	 * @param licenseID
	 * @param licenseText
	 */
	public ExtractedLicenseInfo(String licenseID, String licenseText) {
		this(licenseID, licenseText, null, null, null);
	}
	
	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.RdfModelObject#populateModel()
	 */
	@Override
	public void populateModel() throws InvalidSPDXAnalysisException {
		super.populateModel();
		// ExtractedText
		String existingLicenseText = findSinglePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_EXTRACTED_TEXT);
		if (existingLicenseText != null && this.extractedText != null) {
			if (!LicenseCompareHelper.isLicenseTextEquivalent(existingLicenseText, this.extractedText)) {
				throw(new DuplicateExtractedLicenseIdException("Non-standard license ID "+this.licenseId+" already exists.  Can not add a license with the same ID but different text."));
			}
		}
		setPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_EXTRACTED_TEXT, this.extractedText);
	}
	
	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.RdfModelObject#getType(com.hp.hpl.jena.rdf.model.Model)
	 */
	@Override
	public Resource getType(Model model) {
		return model.createResource(SpdxRdfConstants.SPDX_NAMESPACE + SpdxRdfConstants.CLASS_SPDX_EXTRACTED_LICENSING_INFO);
	}


	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.license.AnyLicenseInfo#toString()
	 */
	@Override
	public String toString() {
		// must be only the ID if we are to use this to create 
		// parseable license strings
		return this.licenseId;
	}
	
	/**
	 * @return the text
	 */
	public String getExtractedText() {
		if (this.resource != null && this.refreshOnGet) {
			this.extractedText = findSinglePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_EXTRACTED_TEXT);
		}
		return this.extractedText;
	}

	/**
	 * @param text the text to set
	 */
	public void setExtractedText(String text) {
		this.extractedText = text;
		setPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_EXTRACTED_TEXT, text);
	}
	

	@Override 
	public int hashCode() {
		if (this.getLicenseId() == null) {
			return 0;
		} else {
			return this.getLicenseId().hashCode();
		}
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.license.AnyLicenseInfo#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (!(o instanceof ExtractedLicenseInfo)) {
			// covers o == null, as null is not an instance of anything
			return false;
		}
		ExtractedLicenseInfo comp = (ExtractedLicenseInfo)o;
		if (this.licenseId == null) {
			return (comp.getLicenseId() == null);
		} else {
			return (this.licenseId.equalsIgnoreCase(comp.getLicenseId()));			
		}
	}

	/**
	 * @return
	 */
	@Override
    public List<String> verify() {
		List<String> retval = Lists.newArrayList();
		String id = this.getLicenseId();
		if (id == null || id.isEmpty()) {
			retval.add("Missing required license ID");
		} else {
			String idError = SpdxVerificationHelper.verifyNonStdLicenseid(id);
			if (idError != null && !idError.isEmpty()) {
				retval.add(idError);
			}
		}
		String licenseText = this.getExtractedText();
		if (licenseText == null || licenseText.isEmpty()) {
			retval.add("Missing required license text for " + id);
		}
		// comment
		// make sure there is not more than one comment
		try {
			if (node != null) {
				Node p = model.getProperty(SpdxRdfConstants.RDFS_NAMESPACE, SpdxRdfConstants.RDFS_PROP_COMMENT).asNode();
				Triple m = Triple.createMatch(node, p, null);
				ExtendedIterator<Triple> tripleIter = model.getGraph().find(m);
				int count = 0;
				while (tripleIter.hasNext()) {
					count++;
					tripleIter.next();
				}
				if (count > 1) {
					retval.add("More than one comment on Extracted License Info id "+id.toString());
				}
			}
		} catch (Exception e) {
			retval.add("Error getting license comments: "+e.getMessage());
		}

		return retval;
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.license.AnyLicenseInfo#clone()
	 */
	@Override
	public AnyLicenseInfo clone() {
		return new ExtractedLicenseInfo(this.getLicenseId(), this.getExtractedText(), this.getName(), 
				this.getSeeAlso(), this.getComment());
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.IRdfModel#equivalent(org.spdx.rdfparser.model.IRdfModel)
	 */
	@Override
	public boolean equivalent(IRdfModel compare) {
		if (!(compare instanceof ExtractedLicenseInfo)) {
			return false;
		}
		// Only test for the text - other fields do not need to equal to be considered equivalent
		return LicenseCompareHelper.isLicenseTextEquivalent(this.getExtractedText(), ((ExtractedLicenseInfo)compare).getExtractedText());
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(ExtractedLicenseInfo o) {
		if (this.getLicenseId() == null) {
			if (o.getLicenseId() == null) {
				if (this.getExtractedText() == null) {
					if (o.getExtractedText() == null) {
						return 0;
					} else {
						return 1;
					}
				}else if (o.getExtractedText() == null) {
					return -1;
				} else {
					return this.getExtractedText().compareToIgnoreCase(o.getExtractedText());
				}
			} else {
				return 1;
			}
		} else {
			if (o.getLicenseId() == null) {
				return -1;
			} else {
				return this.getLicenseId().compareToIgnoreCase(o.getLicenseId());
			}
		}
	}
}
