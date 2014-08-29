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
package org.spdx.rdfparser;

import java.util.ArrayList;

import org.spdx.compare.LicenseCompareHelper;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * A non-standard license which is valid only within an SPDXAnalysis.  This is
 * equivelant to the extractedLicensingInfo
 * @author Gary O'Neall
 *
 */
public class SPDXNonStandardLicense extends SPDXLicense {
	
	private String text;
	private String comment = null;
	private String licenseName = null;
	private String[] sourceUrls = null;

	
	/**
	 * @param model
	 * @param licenseInfoNode
	 * @throws InvalidSPDXAnalysisException 
	 */
	public SPDXNonStandardLicense(Model model, Node licenseInfoNode) throws InvalidSPDXAnalysisException {
		super(model, licenseInfoNode);
		// Text
		Node p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_EXTRACTED_TEXT).asNode();
		Triple m = Triple.createMatch(licenseInfoNode, p, null);
		ExtendedIterator<Triple> tripleIter = model.getGraph().find(m);	
		if (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			this.text = t.getObject().toString(false);
		}
		// comment
		p = model.getProperty(SpdxRdfConstants.RDFS_NAMESPACE, SpdxRdfConstants.RDFS_PROP_COMMENT).asNode();
		m = Triple.createMatch(licenseInfoNode, p, null);
		tripleIter = model.getGraph().find(m);	
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			this.comment = t.getObject().toString(false);
		}
		// license name
		p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_LICENSE_NAME).asNode();
		m = Triple.createMatch(licenseInfoNode, p, null);
		tripleIter = model.getGraph().find(m);	
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			this.licenseName = t.getObject().toString(false);
		}
		// license urls
		p = model.getProperty(SpdxRdfConstants.RDFS_NAMESPACE, SpdxRdfConstants.RDFS_PROP_SEE_ALSO).asNode();
		m = Triple.createMatch(licenseInfoNode, p, null);
		tripleIter = model.getGraph().find(m);	
		if (tripleIter.hasNext()) {
			ArrayList<String> alLicenseUrls = new ArrayList<String>();
			while (tripleIter.hasNext()) {
				Triple t = tripleIter.next();
				alLicenseUrls.add(t.getObject().toString(false));
			}
			this.sourceUrls = alLicenseUrls.toArray(new String[alLicenseUrls.size()]);
		}
	}
	
	public SPDXNonStandardLicense(String id, String text, String licenseName, String[] crossReferenceUrls, String comment) {
		super(id);
		this.text = text;
		this.licenseName = licenseName;
		this.sourceUrls = crossReferenceUrls;
		this.comment = comment;
	}

	/**
	 * @param licenseID
	 * @param licenseText
	 */
	public SPDXNonStandardLicense(String licenseID, String licenseText) {
		this(licenseID, licenseText, null, null, null);
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.SPDXLicenseInfo#_createResource(com.hp.hpl.jena.rdf.model.Model)
	 */
	@Override
	protected Resource _createResource(Model model) throws InvalidSPDXAnalysisException {
		Resource type = model.createResource(SpdxRdfConstants.SPDX_NAMESPACE + SpdxRdfConstants.CLASS_SPDX_EXTRACTED_LICENSING_INFO);
		Resource r = super._createResource(model, type);
		// check to make sure we are not overwriting an existing license with the same ID
		String existingLicenseText = getLicenseTextFromModel(model, r.asNode());
		if (existingLicenseText != null && this.text != null) {
			if (!LicenseCompareHelper.isLicenseTextEquivalent(existingLicenseText, this.text)) {
				throw(new DuplicateNonStandardLicenseIdException("Non-standard license ID "+this.id+" already exists.  Can not add a license with the same ID but different text."));
			}
		}
		if (this.text != null) {			
			Property textProperty = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, 
					SpdxRdfConstants.PROP_EXTRACTED_TEXT);
			model.removeAll(r, textProperty, null);
			r.addProperty(textProperty, this.text);
		}
		if (this.comment != null && !this.comment.isEmpty()) {
			Property commentProperty = model.createProperty(SpdxRdfConstants.RDFS_NAMESPACE, 
					SpdxRdfConstants.RDFS_PROP_COMMENT);
			model.removeAll(r, commentProperty, null);
			r.addProperty(commentProperty, this.comment);
		}
		if (this.licenseName != null && !this.licenseName.isEmpty()) {
			Property nameProperty = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, 
					SpdxRdfConstants.PROP_LICENSE_NAME);
			model.removeAll(r, nameProperty, null);
			r.addProperty(nameProperty, this.licenseName);
		}
		if (this.sourceUrls != null && this.sourceUrls.length > 0) {
			Property licenseUrlProperty = model.createProperty(SpdxRdfConstants.RDFS_NAMESPACE, 
					SpdxRdfConstants.RDFS_PROP_SEE_ALSO);
			model.removeAll(r, licenseUrlProperty, null);
			for (int i = 0; i < sourceUrls.length; i++) {
				r.addProperty(licenseUrlProperty, sourceUrls[i]);
			}
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
	 * @see org.spdx.rdfparser.SPDXLicenseInfo#toString()
	 */
	@Override
	public String toString() {
		// must be only the ID if we are to use this to create 
		// parseable license strings
		return this.id;
	}
	
	/**
	 * @return the text
	 */
	public String getText() {
		return this.text;
	}

	/**
	 * @param text the text to set
	 */
	public void setText(String text) {
		this.text = text;
		if (this.licenseInfoNode != null) {
			// delete any previous created
			Property p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_EXTRACTED_TEXT);
			model.removeAll(resource, p, null);
			// add the property
			p = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_EXTRACTED_TEXT);
			resource.addProperty(p, text);
		}
	}
	
	/**
	 * @return License comment (null if none has been set)
	 */
	public String getComment() {
		return this.comment;
	}
	
	/**
	 * Set the license comment
	 * @param comment
	 */
	public void setComment(String comment) {
		this.comment = comment;
		if (licenseInfoNode != null) {
			// delete any previous comments
			Property p = model.getProperty(SpdxRdfConstants.RDFS_NAMESPACE, SpdxRdfConstants.RDFS_PROP_COMMENT);
			model.removeAll(resource, p, null);
			// add the property
			p = model.createProperty(SpdxRdfConstants.RDFS_NAMESPACE, SpdxRdfConstants.RDFS_PROP_COMMENT);
			resource.addProperty(p, comment);
		}
	}
	
	public void setSourceUrls(String[] urls) {
		this.sourceUrls = urls;
		if (licenseInfoNode != null) {
			// delte any previous license URLs
			Property p = model.getProperty(SpdxRdfConstants.RDFS_NAMESPACE, SpdxRdfConstants.RDFS_PROP_SEE_ALSO);
			model.removeAll(resource, p, null);
			for (int i = 0; i < urls.length; i++) {
				resource.addProperty(p, urls[i]);
			}
		}
	}
	
	public void setLicenseName(String name) {
		this.licenseName = name;
		if (licenseInfoNode != null) {
			// delete any previous license name
			Property p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_LICENSE_NAME);
			model.removeAll(resource, p, null);
			// add the property
			resource.addProperty(p, name);
		}
	}
	
	@Override 
	public int hashCode() {
		if (this.getId() == null) {
			return 0;
		} else {
			return this.getId().hashCode();
		}
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.SPDXLicenseInfo#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (!(o instanceof SPDXNonStandardLicense)) {
			// covers o == null, as null is not an instance of anything
			return false;
		}
		SPDXNonStandardLicense comp = (SPDXNonStandardLicense)o;
		if (this.id == null) {
			return (comp.getId() == null);
		} else {
			return (this.id.equals(comp.getId()));			
		}
	}

	/**
	 * @return
	 */
	public ArrayList<String> verify() {
		ArrayList<String> retval = new ArrayList<String>();
		String id = this.getId();
		if (id == null || id.isEmpty()) {
			retval.add("Missing required license ID");
		} else {
			String idError = SpdxVerificationHelper.verifyNonStdLicenseid(id);
			if (idError != null && !idError.isEmpty()) {
				retval.add(idError);
			}
		}
		String licenseText = this.getText();
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

	/**
	 * @return
	 */
	public String getLicenseName() {
		return this.licenseName;
	}

	/**
	 * @return
	 */
	public String[] getSourceUrls() {
		return this.sourceUrls;
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.SPDXLicenseInfo#clone()
	 */
	@Override
	public SPDXLicenseInfo clone() {
		return new SPDXNonStandardLicense(this.getId(), this.getText(), this.getLicenseName(), 
				this.getSourceUrls(), this.getComment());
	}
}
