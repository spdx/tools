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
import org.spdx.rdfparser.SpdxRdfConstants;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * The superclass for all classes the use the Jena RDF model.
 * This class implements several common and helper methods.
 * @author Gary O'Neall
 *
 */
public abstract class RdfModelObject implements IRdfModel {
	
	protected Model model;
	protected Resource resource;
	protected Node node;
	
	public RdfModelObject(Model model, Node node) throws InvalidSPDXAnalysisException {
		this.model = model;
		this.node = node;
		if (node.isBlank()) {
			resource = model.createResource(node.getBlankNodeId());
		} else if (node.isURI()) {
			resource = model.createResource(node.getURI());
		} else {
			throw(new InvalidSPDXAnalysisException("Can not have an model node as a literal"));
		}
	}
	
	public RdfModelObject() {
		
	}
	
	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.IRdfModel#createResource(com.hp.hpl.jena.rdf.model.Model, java.lang.String)
	 */
	@Override
	public Resource createResource(Model model, String uri) {
		if (uri == null) {
			this.resource = model.createResource(getType(model));
		} else {
			this.resource = model.createResource(uri, getType(model));
		}
		this.model = model;
		this.node = this.resource.asNode();
		populateModel();
		return resource;
	};
	
	/**
	 * @return the RDF class name for the object
	 */
	abstract Resource getType(Model model);
	
	/**
	 * Populate the RDF model from the Java properties
	 */
	abstract void populateModel();

	/**
	 * Find a property value with a subject of this object
	 * @param namespace Namespace for the property name
	 * @param propertyName Name of the property
	 * @return The string value of the property or null if no property exists
	 */
	public String findSinglePropertyValue(String namespace, String propertyName) {
		if (this.model == null || this.node == null) {
			return null;
		}
		Node p = model.getProperty(namespace, propertyName).asNode();
		Triple m = Triple.createMatch(node, p, null);
		ExtendedIterator<Triple> tripleIter = model.getGraph().find(m);	
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			return t.getObject().toString(false);
		}
		return null;
	}
	
	/**
	 * Finds multiple property values with a subject of this object
	 * @param namespace Namespace for the property name
	 * @param propertyName Name of the property
	 * @return The string value of the property or null if no property exists
	 */
	public String[] findMultiplePropertyValues(String namespace, String propertyName) {
		if (this.model == null || this.node == null) {
			return null;
		}
		ArrayList<String> retval = new ArrayList<String>();
		Node p = model.getProperty(namespace, propertyName).asNode();
		Triple m = Triple.createMatch(node, p, null);
		ExtendedIterator<Triple> tripleIter = model.getGraph().find(m);	
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			retval.add(t.getObject().toString(false));
		}
		return retval.toArray(new String[retval.size()]);
	}
	
	/**
	 * Set a property values for this resource.  Clears any existing resource.
	 * @param nameSpace RDF Namespace for the property
	 * @param propertyName RDF Property Name (the RDF 
	 * @param values Values to associate to this resource
	 */
	protected void setPropertyValue(String nameSpace, String propertyName,
			String[] values) {
		if (model != null && resource != null) {
			Property p = model.createProperty(nameSpace, propertyName);
			model.removeAll(this.resource, p, null);
			if (values != null) {
				for (int i = 0; i < values.length; i++) {
					if (values[i] != null) {
						this.resource.addProperty(p, values[i]);
					}
				}
			}
		}
	}
	
	/**
	 * Set a property value for this resource.  Clears any existing resource.
	 * @param nameSpace RDF Namespace for the property
	 * @param propertyName RDF Property Name
	 * @param value Values to set
	 */
	protected void setPropertyValue(String nameSpace, String propertyName,
			String value) {
		setPropertyValue(nameSpace, propertyName, new String[] {value});
	}
	
	/**
	 * Removes all property values for this resource. 
	 * @param nameSpace RDF Namespace for the property
	 * @param propertyName RDF Property Name
	 */
	protected void removePropertyValue(String nameSpace, String propertyName) {
		if (model != null && resource != null) {
			Property p = model.createProperty(nameSpace, propertyName);
			model.removeAll(this.resource, p, null);
		}
	}
}
