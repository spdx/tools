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

import java.util.List;

import com.google.common.collect.Lists;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.AnonId;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.iterator.ExtendedIterator;

/**
 * Contains a DOAP project
 * Currently, only the home page and name properties are supported
 *
 * This class is provided for compatibility with the 1.2 version of the library
 * and is no longer used in version 2.0 and above (replaced by model.DoapProject)
 * @author Gary O'Neall
 *
 */
@Deprecated
public class DOAPProject implements Cloneable {

	public static final String UNKNOWN_URI = "UNKNOWN";
	private String name = null;
	private String homePage = null;
	private Node projectNode = null;
	private Resource projectResource = null;
	private Model model = null;
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
	static DOAPProject getExistingProject(Model model, String projectUrl) throws InvalidSPDXAnalysisException {
		Resource projectResource = model.createResource(projectUrl);
		model.read(projectUrl);
		return new DOAPProject(model, projectResource.asNode());
	}

	public DOAPProject(Model model, Node node) throws InvalidSPDXAnalysisException {
		this.model = model;
		this.projectNode = node;
		if (projectNode.isBlank()) {
			this.projectResource = model.createResource(new AnonId(node.getBlankNodeId()));
		} else if (projectNode.isURI()) {
			this.projectResource = model.createResource(node.getURI());
			this.uri = node.getURI();
		} else {
			throw(new InvalidSPDXAnalysisException("Can not create a DOAP project from a literal node"));
		}

		// name
		Node p = model.getProperty(SpdxRdfConstants.DOAP_NAMESPACE, SpdxRdfConstants.PROP_PROJECT_NAME).asNode();
		Triple m = Triple.createMatch(projectNode, p, null);
		ExtendedIterator<Triple> tripleIter = model.getGraph().find(m);
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			this.name = t.getObject().toString(false);
		}
		// home page
		p = model.getProperty(SpdxRdfConstants.DOAP_NAMESPACE, SpdxRdfConstants.PROP_PROJECT_HOMEPAGE).asNode();
		m = Triple.createMatch(projectNode, p, null);
		tripleIter = model.getGraph().find(m);
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			this.homePage = t.getObject().toString(false);
		}
	}

	/**
	 * @param projectName
	 * @param homePage
	 */
	public DOAPProject(String projectName, String homePage) {
		this.name = projectName;
		this.homePage = homePage;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
		if (this.projectNode != null && this.model != null) {
			Property p = model.createProperty(SpdxRdfConstants.DOAP_NAMESPACE, SpdxRdfConstants.PROP_PROJECT_NAME);
			model.removeAll(projectResource, p, null);
			p = model.createProperty(SpdxRdfConstants.DOAP_NAMESPACE, SpdxRdfConstants.PROP_PROJECT_NAME);
			projectResource.addProperty(p, name);
		}
	}

	/**
	 * @return the homePage
	 */
	public String getHomePage() {
		return homePage;
	}

	/**
	 * @param homePage the homePage to set
	 */
	public void setHomePage(String homePage) {
		this.homePage = homePage;
		if (this.projectNode != null && this.model != null) {
			Property p = model.createProperty(SpdxRdfConstants.DOAP_NAMESPACE, SpdxRdfConstants.PROP_PROJECT_HOMEPAGE);
			model.removeAll(projectResource, p, null);
			if (homePage != null) {
				p = model.createProperty(SpdxRdfConstants.DOAP_NAMESPACE, SpdxRdfConstants.PROP_PROJECT_HOMEPAGE);
				Resource homePageResource = model.createResource(homePage);
				projectResource.addProperty(p, homePageResource);
			}
		}
	}

	public String getProjectUri() {
		if (projectNode == null) {
			if (uri == null || uri.isEmpty()) {
				return UNKNOWN_URI;
			} else {
				return uri;
			}
		} else {
			if (projectNode.isURI()) {
				return projectNode.getURI();
			} else {
				return UNKNOWN_URI;
			}
		}
	}

	public Resource createResource(Model model) {
		Resource type = model.createResource(SpdxRdfConstants.DOAP_NAMESPACE + SpdxRdfConstants.CLASS_DOAP_PROJECT);
		Resource retval;
		if (uri != null && !uri.isEmpty() && !uri.equals(UNKNOWN_URI)) {
			retval = model.createResource(uri, type);
		} else {
			retval = model.createResource(type);
		}
		populateModel(model, retval);
		return retval;
	}

	/**
	 * @param model Jena model to populate
	 * @param projectResource Project resource to populate
	 */
	private void populateModel(Model model, Resource projectResource) {
		this.model = model;
		this.projectNode = projectResource.asNode();
		this.projectResource = projectResource;

		// Name
		if (name != null) {
			Property p = model.createProperty(SpdxRdfConstants.DOAP_NAMESPACE, SpdxRdfConstants.PROP_PROJECT_NAME);
			projectResource.addProperty(p, name);
		}

		// HomePage
		if (homePage != null) {
			Property p = model.createProperty(SpdxRdfConstants.DOAP_NAMESPACE, SpdxRdfConstants.PROP_PROJECT_HOMEPAGE);
			projectResource.addProperty(p, homePage);
		}
	}

	/**
	 * @return
	 */
	public List<String> verify() {
		return Lists.newArrayList();	// anything to verify?
	}

	/**
	 * @param uri
	 * @throws InvalidSPDXAnalysisException
	 */
	public void setUri(String uri) throws InvalidSPDXAnalysisException {
		if (this.projectResource != null) {
			if (!this.projectResource.hasURI(uri)) {
				throw(new InvalidSPDXAnalysisException("Can not set a URI value for a resource which has already been created."));
			}
		}
		if (!uri.equals(UNKNOWN_URI) &&!SpdxVerificationHelper.isValidUri(uri)) {
			throw(new InvalidSPDXAnalysisException("Invalid URI for DOAP Project "+this.name+": "+uri));
		}
		this.uri = uri;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof DOAPProject)) {
			return false;
		}
		DOAPProject compare = (DOAPProject)o;
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
		if (compare.getHomePage() != null && this.getHomePage() != null) {
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

	/**
	 * @return
	 */
	public Resource getResource() {
		return this.projectResource;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 * Provides a deep copy of the DOAPProject
	 */
	@Override
	public DOAPProject clone() {
		DOAPProject retval = new DOAPProject(this.getName(), this.getHomePage());
		if (this.getProjectUri() != null) {
			try {
				retval.setUri(this.getProjectUri());
			} catch (InvalidSPDXAnalysisException e) {
				// ignore the exception and just go without the URI
			}
		}
		return retval;
	}
}
