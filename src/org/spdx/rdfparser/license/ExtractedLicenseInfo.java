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

import java.util.ArrayList;

import org.spdx.compare.LicenseCompareHelper;
import org.spdx.rdfparser.IModelContainer;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SpdxRdfConstants;
import org.spdx.rdfparser.SpdxVerificationHelper;
import org.spdx.rdfparser.model.IRdfModel;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * An ExtractedLicensingInfo represents a license or licensing notice that was found in the package. 
 * Any license text that is recognized as a license may be represented as a License 
 * rather than an ExtractedLicensingInfo.
 * @author Gary O'Neall
 *
 */
public class ExtractedLicenseInfo extends SimpleLicensingInfo {
	
	private String extractedText;

	
	/**
	 * @param modelContainer container which includes the license
	 * @param licenseInfoNode Node that defines the ExtractedLicenseInfo
	 * @throws InvalidSPDXAnalysisException 
	 */
	public ExtractedLicenseInfo(IModelContainer modelContainer, Node licenseInfoNode) throws InvalidSPDXAnalysisException {
		super(modelContainer, licenseInfoNode);
		// Text
		Node p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_EXTRACTED_TEXT).asNode();
		Triple m = Triple.createMatch(licenseInfoNode, p, null);
		ExtendedIterator<Triple> tripleIter = model.getGraph().find(m);	
		if (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			this.extractedText = t.getObject().toString(false);
		}
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
	 * @see org.spdx.rdfparser.license.AnyLicenseInfo#_createResource(com.hp.hpl.jena.rdf.model.Model)
	 */
	@Override
	protected Resource _createResource() throws InvalidSPDXAnalysisException {
		if (this.licenseId == null || this.licenseId.isEmpty()) {
			throw(new InvalidSPDXAnalysisException("Can not create a resource for an Extracted License without a license ID"));
		}
		Resource type = model.createResource(SpdxRdfConstants.SPDX_NAMESPACE + SpdxRdfConstants.CLASS_SPDX_EXTRACTED_LICENSING_INFO);
		Resource r = super._createResource(type, modelContainer.getDocumentNamespace() + this.licenseId);
		// check to make sure we are not overwriting an existing license with the same ID
		String existingLicenseText = getLicenseTextFromModel(model, r.asNode());
		if (existingLicenseText != null && this.extractedText != null) {
			if (!LicenseCompareHelper.isLicenseTextEquivalent(existingLicenseText, this.extractedText)) {
				throw(new DuplicateExtractedLicenseIdException("Non-standard license ID "+this.licenseId+" already exists.  Can not add a license with the same ID but different text."));
			}
		}
		if (this.extractedText != null) {			
			Property textProperty = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, 
					SpdxRdfConstants.PROP_EXTRACTED_TEXT);
			model.removeAll(r, textProperty, null);
			r.addProperty(textProperty, this.extractedText);
		}
		return r;
	}
	
	/**
	 * Get the license text from the model, returning null if no license text is found
	 * @param model
	 * @param licenseResource
	 * @return
	 */
	public static String getLicenseTextFromModel(Model model, Node licenseResourceNode) {
		Node p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_EXTRACTED_TEXT).asNode();
		Triple m = Triple.createMatch(licenseResourceNode, p, null);
		ExtendedIterator<Triple> tripleIter = model.getGraph().find(m);	
		if (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			return t.getObject().toString(false);
		}
		return null;
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
		return this.extractedText;
	}

	/**
	 * @param text the text to set
	 */
	public void setExtractedText(String text) {
		this.extractedText = text;
		if (this.licenseInfoNode != null) {
			// delete any previous created
			Property p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_EXTRACTED_TEXT);
			model.removeAll(resource, p, null);
			// add the property
			p = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_EXTRACTED_TEXT);
			resource.addProperty(p, text);
		}
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
			return (this.licenseId.equals(comp.getLicenseId()));			
		}
	}

	/**
	 * @return
	 */
	public ArrayList<String> verify() {
		ArrayList<String> retval = new ArrayList<String>();
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
			if (licenseInfoNode != null) {
				Node p = model.getProperty(SpdxRdfConstants.RDFS_NAMESPACE, SpdxRdfConstants.RDFS_PROP_COMMENT).asNode();
				Triple m = Triple.createMatch(licenseInfoNode, p, null);
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
		return LicenseCompareHelper.isLicenseTextEquivalent(this.extractedText, ((ExtractedLicenseInfo)compare).getExtractedText());
	}
}
