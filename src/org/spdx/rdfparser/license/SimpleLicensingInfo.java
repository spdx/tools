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
package org.spdx.rdfparser.license;

import java.util.Arrays;

import org.spdx.rdfparser.IModelContainer;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.RdfModelHelper;
import org.spdx.rdfparser.SpdxRdfConstants;
import org.spdx.rdfparser.model.IRdfModel;

import com.google.common.base.Objects;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.AnonId;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.iterator.ExtendedIterator;

/**
 * The SimpleLicenseInfo class includes all resources that represent
 * simple, atomic, licensing information.
 *
 * @author Gary O'Neall
 *
 */
public abstract class SimpleLicensingInfo extends AnyLicenseInfo {
	protected String licenseId;
	protected String comment;
	protected String name;
	protected String[] seeAlso;
	protected String[] crossRef;


	/**
	 * @param modelContainer container which includes the license
	 * @param node RDF Node that defines the SimpleLicensingInfo
	 * @throws InvalidSPDXAnalysisException
	 */
	SimpleLicensingInfo(IModelContainer modelContainer, Node node) throws InvalidSPDXAnalysisException {
		super(modelContainer, node);
		getPropertiesFromModel();
	}

	@Override
	public void getPropertiesFromModel() throws InvalidSPDXAnalysisException {
		// id
		this.licenseId = this.findSinglePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_LICENSE_ID);
		// name
		this.name = findSinglePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_NAME);
		if (this.name == null) {
			// for compatability
			this.name = findSinglePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_NAME_VERSION_1);
		}
		// SourceUrl/seeAlso
		this.seeAlso = this.findMultiplePropertyValues(SpdxRdfConstants.RDFS_NAMESPACE, SpdxRdfConstants.RDFS_PROP_SEE_ALSO);
		// The following is added for compatibility with earlier versions
		String[] moreSeeAlso = findMultiplePropertyValues(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_URL_VERSION_1);
		if (moreSeeAlso != null && moreSeeAlso.length > 0) {
			int startExtraIndex = this.seeAlso.length;
			this.seeAlso = Arrays.copyOf(this.seeAlso, startExtraIndex + moreSeeAlso.length);
			for (int i = 0; i < moreSeeAlso.length; i++) {
				this.seeAlso[startExtraIndex + i] = moreSeeAlso[i];
			}
		}
		// SourceUrlDetails/crossRef
		this.crossRef = this.findMultiplePropertyValues(SpdxRdfConstants.RDFS_NAMESPACE, SpdxRdfConstants.LICENSEXML_ELEMENT_CROSS_REF);
		// The following is added for compatibility with earlier versions
		String[] moreCrossRef = findMultiplePropertyValues(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_URL_VERSION_1);
		if (moreCrossRef != null && moreCrossRef.length > 0) {
			int startExtraIndex = this.crossRef.length;
			this.crossRef = Arrays.copyOf(this.crossRef, startExtraIndex + moreCrossRef.length);
			for (int i = 0; i < moreCrossRef.length; i++) {
				this.crossRef[startExtraIndex + i] = moreCrossRef[i];
			}
		}
		// comments
		this.comment = findSinglePropertyValue(SpdxRdfConstants.RDFS_NAMESPACE, SpdxRdfConstants.RDFS_PROP_COMMENT);
		if (this.comment == null) {
			// for backwards compatibility
			this.comment = findSinglePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_NOTES_VERSION_1);
		}
	}

	/**
	 * @param name License name
	 * @param id License ID
	 * @param comments Optional license comments
	 * @param sourceUrl Optional reference URL's
	 */
	SimpleLicensingInfo(String name, String id, String comments, String[] sourceUrl) {
		super();
		this.licenseId = id;
		this.name = name;
		this.comment = comments;
		this.seeAlso = sourceUrl;
	}

	/**
	 * @param name License name
	 * @param id License ID
	 * @param comments Optional license comments
	 * @param sourceUrl Optional reference URL's
	 * @param sourceUrlDetails Optional reference URL details
	 */
	SimpleLicensingInfo(String name, String id, String comments, String[] sourceUrl, String[] sourceUrlDetails) {
		super();
		this.licenseId = id;
		this.name = name;
		this.comment = comments;
		this.seeAlso = sourceUrl;
		this.crossRef = sourceUrlDetails;
	}
	/**
	 * @return the id
	 */
	public String getLicenseId() {
		if (this.resource != null && this.refreshOnGet) {
			this.licenseId = this.findSinglePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_LICENSE_ID);
		}
		return this.licenseId;
	}

	/**
	 * @param id the id to set
	 */
	public void setLicenseId(String id) {
		this.licenseId = id;
		if (id == null) {
			removePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_LICENSE_ID);
		} else {
			setPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE,
					SpdxRdfConstants.PROP_LICENSE_ID, id);
		}
	}

	/**
	 * @return the name
	 */
	public String getName() {
		if (this.resource != null && this.refreshOnGet) {
			this.name = findSinglePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_NAME);
			if (this.name == null) {
				// for compatability
				this.name = findSinglePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_NAME_VERSION_1);
			}
		}
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
		if (this.node != null) {
			removePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_NAME_VERSION_1);
			if (name == null) {
				removePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_NAME);
			} else {
				setPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_NAME, name);
			}
		}
	}
	/**
	 * @return the comments
	 */
	public String getComment() {
		if (this.resource != null && this.refreshOnGet) {
			this.comment = findSinglePropertyValue(SpdxRdfConstants.RDFS_NAMESPACE, SpdxRdfConstants.RDFS_PROP_COMMENT);
			if (this.comment == null) {
				// for backwards compatibility
				this.comment = findSinglePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_NOTES_VERSION_1);
			}
		}
		return comment;
	}

	/**
	 * @param comment the comment to set
	 */
	public void setComment(String comment) {
		this.comment = comment;
		if (this.node != null) {
			removePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_NOTES_VERSION_1);
			if (comment == null) {
				removePropertyValue(SpdxRdfConstants.RDFS_NAMESPACE, SpdxRdfConstants.RDFS_PROP_COMMENT);
			} else {
				setPropertyValue(SpdxRdfConstants.RDFS_NAMESPACE, SpdxRdfConstants.RDFS_PROP_COMMENT, comment);
			}
		}
	}

	/**
	 * @return the urls which reference the same license information
	 */
	public String[] getSeeAlso() {
		if (this.resource != null && this.refreshOnGet) {
			this.seeAlso = this.findMultiplePropertyValues(SpdxRdfConstants.RDFS_NAMESPACE, SpdxRdfConstants.RDFS_PROP_SEE_ALSO);
			// The following is added for compatibility with earlier versions
			String[] moreSeeAlso = findMultiplePropertyValues(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_URL_VERSION_1);
			if (moreSeeAlso != null && moreSeeAlso.length > 0) {
				int startExtraIndex = this.seeAlso.length;
				this.seeAlso = Arrays.copyOf(this.seeAlso, startExtraIndex + moreSeeAlso.length);
				for (int i = 0; i < moreSeeAlso.length; i++) {
					this.seeAlso[startExtraIndex + i] = moreSeeAlso[i];
				}
			}
		}
		return seeAlso;
	}
	/**
	 * @param seeAlsoUrl the urls which are references to the same license to set
	 */
	public void setSeeAlso(String[] seeAlsoUrl) {
		this.seeAlso = seeAlsoUrl;
		if (this.node != null) {
			removePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_URL_VERSION_1);
			if (seeAlso == null) {
				removePropertyValue(SpdxRdfConstants.RDFS_NAMESPACE, SpdxRdfConstants.RDFS_PROP_SEE_ALSO);
			} else {
				setPropertyValue(SpdxRdfConstants.RDFS_NAMESPACE, SpdxRdfConstants.RDFS_PROP_SEE_ALSO, seeAlso);
			}
		}
	}

	/**
	 * @return the urls which reference the same license information
	 */
	public String[] getCrossRef() {
		if (this.resource != null && this.refreshOnGet) {
			this.crossRef = this.findMultiplePropertyValues(SpdxRdfConstants.RDFS_NAMESPACE, SpdxRdfConstants.LICENSEXML_ELEMENT_CROSS_REF);
			// The following is added for compatibility with earlier versions
			String[] moreCrossRef = findMultiplePropertyValues(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_URL_VERSION_1);
			if (moreCrossRef != null && moreCrossRef.length > 0) {
				int startExtraIndex = this.crossRef.length;
				this.crossRef = Arrays.copyOf(this.crossRef, startExtraIndex + moreCrossRef.length);
				for (int i = 0; i < moreCrossRef.length; i++) {
					this.crossRef[startExtraIndex + i] = moreCrossRef[i];
				}
			}
		}
		return crossRef;
	}


	/**
	 * @param seeAlsoUrl the urls which are references to the same license to set
	 */
	public void setCrossRef(String[] seeAlsoUrl) {
		this.crossRef = seeAlsoUrl;
		if (this.node != null) {
			removePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_URL_VERSION_1);
			if (crossRef == null) {
				removePropertyValue(SpdxRdfConstants.RDFS_NAMESPACE, SpdxRdfConstants.LICENSEXML_ELEMENT_CROSS_REF);
			} else {
				setPropertyValue(SpdxRdfConstants.RDFS_NAMESPACE, SpdxRdfConstants.LICENSEXML_ELEMENT_CROSS_REF, crossRef);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.RdfModelObject#findDuplicateResource(org.spdx.rdfparser.IModelContainer, java.lang.String)
	 */
	@Override
	public Resource findDuplicateResource(IModelContainer modelContainer, String uri) throws InvalidSPDXAnalysisException {
		Property idProperty = modelContainer.getModel().createProperty(SpdxRdfConstants.SPDX_NAMESPACE,
				SpdxRdfConstants.PROP_LICENSE_ID);
		Property typeProperty = modelContainer.getModel().getProperty(SpdxRdfConstants.RDF_NAMESPACE,
				SpdxRdfConstants.RDF_PROP_TYPE);
		Triple m = Triple.createMatch(null, idProperty.asNode(), null);
		ExtendedIterator<Triple> tripleIter = modelContainer.getModel().getGraph().find(m);
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			if (t.getObject().toString(false).equals(this.licenseId)) {
				Triple typeMatch = Triple.createMatch(t.getSubject(), typeProperty.asNode(), getType(modelContainer.getModel()).asNode());
				ExtendedIterator<Triple> typeTripleIter = modelContainer.getModel().getGraph().find(typeMatch);
				if (typeTripleIter.hasNext()) {
					// found it
					if (t.getSubject().isURI()) {
						return modelContainer.getModel().createResource(t.getSubject().getURI());
					} else if (t.getSubject().isBlank()) {
						return modelContainer.getModel().createResource(new AnonId(t.getSubject().getBlankNodeId()));
					}
				}
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.RdfModelObject#populateModel()
	 */
	@Override
	public void populateModel() throws InvalidSPDXAnalysisException {
		// licenseId
		if (this.licenseId == null) {
			removePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_LICENSE_ID);
		} else {
			setPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE,
					SpdxRdfConstants.PROP_LICENSE_ID, this.licenseId);
		}
		// Comment
		if (comment == null) {
			removePropertyValue(SpdxRdfConstants.RDFS_NAMESPACE, SpdxRdfConstants.RDFS_PROP_COMMENT);
		} else {
			setPropertyValue(SpdxRdfConstants.RDFS_NAMESPACE, SpdxRdfConstants.RDFS_PROP_COMMENT, comment);
		}
		// name
		if (name == null) {
			removePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_NAME);
		} else {
			setPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_NAME, name);
		}
		// SeeAlso
		if (seeAlso == null) {
			removePropertyValue(SpdxRdfConstants.RDFS_NAMESPACE, SpdxRdfConstants.RDFS_PROP_SEE_ALSO);
		} else {
			setPropertyValue(SpdxRdfConstants.RDFS_NAMESPACE, SpdxRdfConstants.RDFS_PROP_SEE_ALSO, seeAlso);
		}
		// crossRef
		if (crossRef == null) {
			removePropertyValue(SpdxRdfConstants.RDFS_NAMESPACE, SpdxRdfConstants.LICENSEXML_ELEMENT_CROSS_REF);
		} else {
			setPropertyValue(SpdxRdfConstants.RDFS_NAMESPACE, SpdxRdfConstants.LICENSEXML_ELEMENT_CROSS_REF, crossRef);
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

	@Override
	public boolean equals(Object comp) {
		if (comp == this) {
			return true;
		}
		if (!(comp instanceof License)) {
			return false;
		}
		License compl = (License)comp;
		return compl.getLicenseId().equals(this.getLicenseId());
	}

	@Override
    public boolean equivalent(IRdfModel compare) {
		if (compare == this) {
			return true;
		}
		if (!(compare instanceof SimpleLicensingInfo)) {
			return false;
		}
		SimpleLicensingInfo sCompare = (SimpleLicensingInfo)compare;
        return Objects.equal(this.comment, sCompare.getComment()) &&
                Objects.equal(this.name, sCompare.getName()) &&
				RdfModelHelper.arraysEqual(this.seeAlso, sCompare.getSeeAlso());
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.RdfModelObject#getUri(org.spdx.rdfparser.IModelContainer)
	 */
	@Override
	public String getUri(IModelContainer modelContainer)
			throws InvalidSPDXAnalysisException {
		if (this.node != null && this.node.isURI()) {
			return this.node.getURI();
		} else {
			// Create a URI using the document namespace and liense REF
			return modelContainer.getDocumentNamespace() + this.licenseId;
		}
	}
}
