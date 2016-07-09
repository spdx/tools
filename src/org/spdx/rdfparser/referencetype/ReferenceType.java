/**
 * Copyright (c) 2016 Source Auditor Inc.
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
package org.spdx.rdfparser.referencetype;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.spdx.rdfparser.IModelContainer;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SpdxRdfConstants;
import org.spdx.rdfparser.model.IRdfModel;
import org.spdx.rdfparser.model.RdfModelObject;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Type of external reference
 * Note that there are very few required fields for this class in that
 * the external reference type does not need to be provided in the SPDX
 * document for the document to be valid.
 * 
 * @author Gary O'Neall
 *
 */
public class ReferenceType extends RdfModelObject implements Comparable<ReferenceType> {
	
	//TODO: Current implementation only uses the uri field.  Implement additional fields
	// once the SPDX listed reference type pages are live
	
	static final Logger logger = Logger.getLogger(ReferenceType.class);

	String contextualExample;
	URL documentation;
	URL externalReferenceSite;
	URI referenceTypeUri;
	
	/**
	 * Create a reference type from basic values
	 * @param uri Unique resource identifier for the reference type resource
	 * @param contextualExample Example for the reference type
	 * @param documentation URL pointing to documentation
	 * @param externalReferenceSite URL pointing to the reference site
	 * @throws InvalidSPDXAnalysisException 
	 */
	public ReferenceType(URI uri, String contextualExample, URL documentation, 
			URL externalReferenceSite) throws InvalidSPDXAnalysisException {
		this.referenceTypeUri = uri;
		this.contextualExample = contextualExample;
		this.documentation = documentation;
		this.externalReferenceSite = externalReferenceSite;
	}

	/**
	 * @param modelContainer
	 * @param object
	 * @throws InvalidSPDXAnalysisException 
	 */
	public ReferenceType(IModelContainer modelContainer, Node node) throws InvalidSPDXAnalysisException {
		super(modelContainer, node);
		getPropertiesFromModel();
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.IRdfModel#verify()
	 */
	@Override
	public List<String> verify() {
		ArrayList<String> retval = new ArrayList<String>();
		if (this.referenceTypeUri == null) {
			retval.add("Missing required URI for Reference Type");
		} else if (!ListedReferenceTypes.getListedReferenceTypes().isListedReferenceType(this.referenceTypeUri)) {
			retval.add("URI "+this.referenceTypeUri.toString()+" is not a listed SPDX reference type.");
		}
		return retval;
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.IRdfModel#equivalent(org.spdx.rdfparser.model.IRdfModel)
	 */
	@Override
	public boolean equivalent(IRdfModel compare) {
		if (!(compare instanceof ReferenceType)) {
			return false;
		}
		ReferenceType oComp = (ReferenceType)compare;
		if (this.referenceTypeUri == null) {
			return oComp.getReferenceTypeUri() == null;
		}
		return this.referenceTypeUri.equals(oComp.getReferenceTypeUri());
	}

	/**
	 * @return the contextualExample
	 * @throws InvalidSPDXAnalysisException 
	 */
	public String getContextualExample() throws InvalidSPDXAnalysisException {
		throw new InvalidSPDXAnalysisException("This field is not implemented.");
	}

	/**
	 * @param contextualExample the contextualExample to set
	 * @throws InvalidSPDXAnalysisException 
	 */
	public void setContextualExample(String contextualExample) throws InvalidSPDXAnalysisException {
		throw new InvalidSPDXAnalysisException("This field is not implemented.");
	}

	/**
	 * @return the documentation
	 * @throws InvalidSPDXAnalysisException 
	 */
	public URL getDocumentation() throws InvalidSPDXAnalysisException {
		throw new InvalidSPDXAnalysisException("This field is not implemented.");
	}

	/**
	 * @param documentation the documentation to set
	 * @throws InvalidSPDXAnalysisException 
	 */
	public void setDocumentation(URL documentation) throws InvalidSPDXAnalysisException {
		throw new InvalidSPDXAnalysisException("This field is not implemented.");
	}

	/**
	 * @return the externalReferenceSite
	 * @throws InvalidSPDXAnalysisException 
	 */
	public URL getExternalReferenceSite() throws InvalidSPDXAnalysisException {
		throw new InvalidSPDXAnalysisException("This field is not implemented.");
	}

	/**
	 * @param externalReferenceSite the externalReferenceSite to set
	 * @throws InvalidSPDXAnalysisException 
	 */
	public void setExternalReferenceSite(URL externalReferenceSite) throws InvalidSPDXAnalysisException {
		throw new InvalidSPDXAnalysisException("This field is not implemented.");
	}

	/**
	 * @return the referenceTypeUri
	 */
	public URI getReferenceTypeUri() {
		return referenceTypeUri;
	}

	/**
	 * @param referenceTypeUri the referenceTypeUri to set
	 */
	public void setReferenceTypeUri(URI referenceTypeUri) {
		this.referenceTypeUri = referenceTypeUri;
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.RdfModelObject#getPropertiesFromModel()
	 */
	@Override
	public void getPropertiesFromModel() throws InvalidSPDXAnalysisException {
		if (this.node == null) {
			return;
		}
		if (!this.node.isURI()) {
			throw(new InvalidSPDXAnalysisException("Only URI based reference types are supported at this time."));
		}
		try {
			this.referenceTypeUri = new URI(this.node.getURI());
		} catch (URISyntaxException e) {
			logger.error("Invalid URI for external reference type found in the model.",e);
			throw(new InvalidSPDXAnalysisException("Invalid URI for external reference type found in the model."));
		}
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.RdfModelObject#getUri(org.spdx.rdfparser.IModelContainer)
	 */
	@Override
	public String getUri(IModelContainer modelContainer)
			throws InvalidSPDXAnalysisException {
		return this.referenceTypeUri.toString();
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.RdfModelObject#getType(com.hp.hpl.jena.rdf.model.Model)
	 */
	@Override
	public Resource getType(Model model) {
		return model.createResource(SpdxRdfConstants.SPDX_NAMESPACE + SpdxRdfConstants.CLASS_SPDX_REFERENCE_TYPE);
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.RdfModelObject#populateModel()
	 */
	@Override
	public void populateModel() throws InvalidSPDXAnalysisException {
		// No properties to populate, just the URI is used

	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(ReferenceType o) {
		if (this.getReferenceTypeUri() == null) {
			if (o.getReferenceTypeUri() == null) {
				return 0;
			} else {
				return 1;
			}
		}
		if (o.getReferenceTypeUri() == null) {
			return -1;
		}
		return this.getReferenceTypeUri().compareTo(o.getReferenceTypeUri());
	}
	
	@Override
	public String toString() {
		if (this.referenceTypeUri == null) {
			return "";
		}
		if (ListedReferenceTypes.getListedReferenceTypes().isListedReferenceType(this.referenceTypeUri)) {
			try {
				return ListedReferenceTypes.getListedReferenceTypes().getListedReferenceName(referenceTypeUri);
			} catch (InvalidSPDXAnalysisException e) {
				return this.referenceTypeUri.toString();
			}
		} else {
			return this.referenceTypeUri.toString();
		}
	}

	@Override
	public ReferenceType clone() {
		try {
			return new ReferenceType(this.referenceTypeUri, this.contextualExample, this.documentation, this.externalReferenceSite);
		} catch (InvalidSPDXAnalysisException e) {
			logger.error("Error cloning reference type",e);
			return null;
		}
	}
}
