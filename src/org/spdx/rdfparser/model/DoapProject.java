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

import org.spdx.rdfparser.IModelContainer;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SpdxRdfConstants;
import org.spdx.rdfparser.SpdxVerificationHelper;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Contains a DOAP project
 * Currently, only the home page and name properties are supported
 * @author Gary O'Neall
 *
 */
public class DoapProject extends RdfModelObject {

	public static final String UNKNOWN_URI = "UNKNOWN";
	private String name = null;
	private String homePage = null;
	private String uri = null;

	/**
	 * This method will create a DOAP Project object from a DOAP document
	 * which already exists.  The DOAP project is read from the uri and
	 * the model is created from the existing data.
	 * @param model Jena model to populate
	 * @param projectUrl The URL of the DOAP project
	 * @return
	 * @throws InvalidSPDXAnalysisException 
	 */
	static DoapProject getExistingProject(IModelContainer modelContainer, String projectUrl) throws InvalidSPDXAnalysisException {
		Resource projectResource = modelContainer.getModel().createResource(projectUrl);
		modelContainer.getModel().read(projectUrl);
		return new DoapProject(modelContainer, projectResource.asNode());
	}
	/**
	 * @param modelContainer
	 * @param node
	 * @throws InvalidSPDXAnalysisException
	 */
	public DoapProject(IModelContainer modelContainer, Node node)
			throws InvalidSPDXAnalysisException {
		super(modelContainer, node);
		getPropertiesFromModel();
	}
	
	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.RdfModelObject#getPropertiesFromModel()
	 */
	@Override
	void getPropertiesFromModel() throws InvalidSPDXAnalysisException {
		// name
		this.name = findSinglePropertyValue(SpdxRdfConstants.DOAP_NAMESPACE, 
				SpdxRdfConstants.PROP_PROJECT_NAME);
		// home page
		this.homePage = findSinglePropertyValue(SpdxRdfConstants.DOAP_NAMESPACE, 
				SpdxRdfConstants.PROP_PROJECT_HOMEPAGE);
	}
	/**
	 * 
	 */
	public DoapProject(String name, String homePage) {
		this.name = name;
		this.homePage = homePage;
	}
	

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.RdfModelObject#populateModel()
	 */
	@Override
	void populateModel() throws InvalidSPDXAnalysisException {
		setPropertyValue(SpdxRdfConstants.DOAP_NAMESPACE, 
				SpdxRdfConstants.PROP_PROJECT_NAME, name);
		setPropertyValue(SpdxRdfConstants.DOAP_NAMESPACE, 
				SpdxRdfConstants.PROP_PROJECT_HOMEPAGE, homePage);
	}

	
	/**
	 * @return the name
	 */
	public String getName() {
		if (this.resource != null && this.refreshOnGet) {
			this.name = findSinglePropertyValue(SpdxRdfConstants.DOAP_NAMESPACE, 
					SpdxRdfConstants.PROP_PROJECT_NAME);
		}
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
		setPropertyValue(SpdxRdfConstants.DOAP_NAMESPACE, 
					SpdxRdfConstants.PROP_PROJECT_NAME, name);
	}
	/**
	 * @return the homePage
	 */
	public String getHomePage() {
		if (this.resource != null && this.refreshOnGet) {
			this.homePage = findSinglePropertyValue(SpdxRdfConstants.DOAP_NAMESPACE, 
					SpdxRdfConstants.PROP_PROJECT_HOMEPAGE);
		}
		return homePage;
	}
	/**
	 * @param homePage the homePage to set
	 */
	public void setHomePage(String homePage) {
		this.homePage = homePage;
		setPropertyValue(SpdxRdfConstants.DOAP_NAMESPACE, 
				SpdxRdfConstants.PROP_PROJECT_HOMEPAGE, homePage);
	}
	/**
	 * @return the uri
	 */
	public String getProjectUri() {
		if (this.resource != null) {
			if (this.resource.isURIResource()) {
				this.uri = this.resource.getURI();
			}
		}
		return uri;
	}
	/**
	 * @param uri the uri to set
	 * @throws InvalidSPDXAnalysisException 
	 */
	public void setProjectUri(String uri) throws InvalidSPDXAnalysisException {
		if (this.resource != null) {
			if (!this.resource.hasURI(uri)) {
				throw(new InvalidSPDXAnalysisException("Can not set a URI value for a resource which has already been created."));
			}
		}
		if (!uri.equals(UNKNOWN_URI) &&!SpdxVerificationHelper.isValidUri(uri)) {
			throw(new InvalidSPDXAnalysisException("Invalid URI for DOAP Project "+this.name+": "+uri));
		}
		this.uri = uri;
	}
	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.IRdfModel#verify()
	 */
	@Override
	public ArrayList<String> verify() {
		ArrayList<String> retval = new ArrayList<String>();	
		if (this.homePage != null && !this.homePage.isEmpty()) {
			if (!SpdxVerificationHelper.isValidUri(homePage)) {
				retval.add("Invalid project home page - not a URL");
			}
		}
		return retval;
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.RdfModelObject#getUri(org.spdx.rdfparser.IModelContainer)
	 */
	@Override
	String getUri(IModelContainer modelContainer) {
		if (uri == UNKNOWN_URI) {
			return null;
		}
		return uri;
	}

	@Override 
	public boolean equals(Object o) {
		if (!(o instanceof DoapProject)) {
			return false;
		}
		DoapProject compare = (DoapProject)o;
		if (this.resource != null && this.resource.equals(compare.resource)) {
			return true;
		}
		// Compare URI's first as they are definitive
		if (compare.getProjectUri() != null && this.getProjectUri() != null &&
				!compare.getProjectUri().equals(UNKNOWN_URI) && !this.getProjectUri().equals(UNKNOWN_URI)) {
			return this.getProjectUri().equals(compare.getProjectUri());
		}
		if ((compare.getProjectUri() != null && !compare.getProjectUri().equals(UNKNOWN_URI)) || 
				(this.getProjectUri() != null && !this.getProjectUri().equals(UNKNOWN_URI))) {
			return false;
		}
		// if the home pages are the same and the project names are the same, we can assume they are equal
		if (compare.getHomePage() != null && this.getHomePage() != null &&
				compare.getName() != null && this.getName() != null) {
			return (compare.getHomePage().equals(this.getHomePage()) && compare.getName().equals(this.getName()));
		}
		// just use the object compares if the above shortcuts did not work out
		return super.equals(o);
	}
	
	@Override
	public int hashCode() {
		// need this method to match the equals for proper behavior
		if (this.getProjectUri() != null && !this.getProjectUri().equals(UNKNOWN_URI)) {
			return(this.getProjectUri().hashCode());
		} else if (this.getHomePage() != null) {
			return this.getHomePage().hashCode();
		} else {
			return super.hashCode();
		}		
	}
	
	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.RdfModelObject#getType(com.hp.hpl.jena.rdf.model.Model)
	 */
	@Override
	Resource getType(Model model) {
		return model.createResource(SpdxRdfConstants.DOAP_NAMESPACE + SpdxRdfConstants.CLASS_DOAP_PROJECT);
	}

	@Override
	public DoapProject clone() {
		DoapProject retval = new DoapProject(this.getName(), this.getHomePage());
		if (this.getProjectUri() != null) {
			try {
				retval.setProjectUri(this.getProjectUri());
			} catch (InvalidSPDXAnalysisException e) {
				// ignore the exception and just go without the URI
			}
		}
		return retval;
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.RdfModelObject#equivalent(org.spdx.rdfparser.model.RdfModelObject)
	 */
	@Override
	public boolean equivalent(RdfModelObject compare) {
		if (!(compare instanceof DoapProject)) {
			return false;
		}
		DoapProject comp = (DoapProject)compare;
		return (equalsConsideringNull(this.name, comp.getName()) &&
				equalsConsideringNull(this.homePage, comp.getHomePage()));
	}
}
