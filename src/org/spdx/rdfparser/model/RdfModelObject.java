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
import org.spdx.rdfparser.license.AnyLicenseInfo;
import org.spdx.rdfparser.license.LicenseInfoFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * The superclass for all classes the use the Jena RDF model.
 * 
 * There are two different lifecycles for objects that subclass RdfModelObject:
 * - If there is an existing model which already contains this object, use the constructor
 * <code>RdfModelObject(ModelContainer container, Node node)</code>
 * where the node contains the property values for the class.  The subclass
 * implementations should implement the population of the Java properties from the 
 * model.  From that point forward, using standard getters and setters will keep
 * the Jena model updated along with the Java properties.
 * 
 * - If creating a new object use the constructor and pass in the initial property
 * values or use setters to set the property values.  To populate the Jena model,
 * invoke the method <code>Resource createResource(IModelContainer modelContainer)</code>.
 * This create a new resource in the model and populate the Jena model from the
 * Java properties.  Once this method has been invoked, all subsequent calls to
 * setters will update both the Java properties and the Jena RDF property values.
 * 
 * To implement a new RdfModelObject subclass, the following methods must be implemented:
 * - Clone: All concrete classes must implement a clone method which will copy the
 * Java values but not copy the model data.  The clone method can be used to duplicate
 * an RdfModelObject in a different Jena model.
 * - getType: Return the RDF Resource that describes RDF class associated with the Java class
 * - getUri: Returns a URI string for RDF resoures where an absolute URI is required.  If null, an anonymous node is created.
 * - populateModel: Populates the RDF model from the Java properties
 * - equals: Must override the Object equals method using the Java properties to determine equality
 * - hashCode: Must override the Object hashcode to be consistent with the equals method
 * - A constructor of the form O(Type1 p1, Type2 p2, ...) where p1, p2, ... are Java properties to initialize the Java object.
 * - A constructor of the form O(ModelContainer modelContainer, Node node)
 * 
 * This class implements several common and helper methods including
 * methods to find and set resources to the model.  The methods to set a resource
 * are named <code>setPropertyValue</code> while the methods to find a 
 * resource value is named <code>findTypePropertyValue</code> where where Type
 * is the type of Java object to be found.  If no property value is found, null is returned.
 * 
 * @author Gary O'Neall
 *
 */
public abstract class RdfModelObject implements IRdfModel, Cloneable {
	
	protected Model model;
	protected Resource resource;
	protected Node node;
	protected IModelContainer modelContainer;
	
	public RdfModelObject(IModelContainer modelContainer, Node node) throws InvalidSPDXAnalysisException {
		this.modelContainer = modelContainer;
		this.model = modelContainer.getModel();
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
	public Resource createResource(IModelContainer modelContainer) throws InvalidSPDXAnalysisException {
		this.modelContainer = modelContainer;
		this.model = modelContainer.getModel();
		String uri = getUri(modelContainer);
		if (uri == null) {
			this.resource = model.createResource(getType(model));
		} else {
			this.resource = model.createResource(uri, getType(model));
		}
		this.node = this.resource.asNode();
		populateModel();
		return resource;
	};
	
	/**
	 * Get the URI for this RDF object. Null if this is for an anonomous node.
	 * @param modelContainer
	 * @return
	 */
	abstract String getUri(IModelContainer modelContainer);
	
	/**
	 * @return the RDF class name for the object
	 */
	abstract Resource getType(Model model);
	
	/**
	 * Populate the RDF model from the Java properties
	 * @throws InvalidSPDXAnalysisException 
	 */
	abstract void populateModel() throws InvalidSPDXAnalysisException;
	
	@Override
	public abstract boolean equals(Object o);
	
	@Override
	public abstract int hashCode();
	
	// The following are helper methods use by the subclasses
	
	/**
	 * Compares to objects considering possible null values
	 * @param o1
	 * @param o2
	 * @return
	 */
	protected boolean equalsConsideringNull(Object o1, Object o2) {
		if (o1 == null) {
			return (o2 == null);
		} else {
			return o1.equals(o2);
		}
	}
	
	/**
	 * Compares 2 arrays to see if thier content is the same independent of
	 * order and considering nulls
	 * @param array1
	 * @param array2
	 * @return
	 */
	protected boolean arraysEqual(Object[] array1, Object[] array2) {
		if (array1 == null) {
			return array2 == null;
		}
		if (array2 == null) {
			return false;
		}
		if (array1.length != array2.length) {
			return false;
		}
		for (int i = 0; i < array1.length; i++) {
			boolean found = false;
			for (int j = 0; j < array2.length; j++) {
				if (equalsConsideringNull(array1[i],array2[j])) {
					found = true;
					break;
				}
			}
			if (!found) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Find an SPDX element with a subject of this object
	 * @param namespace
	 * @param propertyName
	 * @return
	 * @throws InvalidSPDXAnalysisException 
	 */
	protected SpdxElement findElementPropertyValue(String namespace,
			String propertyName) throws InvalidSPDXAnalysisException {
		if (this.model == null || this.node == null) {
			return null;
		}
		Node p = model.getProperty(namespace, propertyName).asNode();
		Triple m = Triple.createMatch(node, p, null);
		ExtendedIterator<Triple> tripleIter = model.getGraph().find(m);	
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			return SpdxElementFactory.createElementFromModel(modelContainer, t.getObject());
		}
		return null;
	}
	
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
	
	protected void setPropertyValue(String nameSpace, String propertyName,
			SpdxElement element) throws InvalidSPDXAnalysisException {
		if (model != null && resource != null) {
			Property p = model.createProperty(nameSpace, propertyName);
			model.removeAll(this.resource, p, null);
			if (element != null) {
				this.resource.addProperty(p, element.createResource(modelContainer));
			}
		}
	}
	
	protected void setPropertyValues(String nameSpace, String propertyName,
			Annotation[] annotations) throws InvalidSPDXAnalysisException {
		if (model != null && resource != null) {
			Property p = model.createProperty(nameSpace, propertyName);
			model.removeAll(this.resource, p, null);
			if (annotations != null) {
				for (int i = 0; i < annotations.length; i++) {
					this.resource.addProperty(p, annotations[i].createResource(modelContainer));
				}
			}
		}
	}
	
	/**
	 * Find all annotations with a subject of this object
	 * @param nameSpace
	 * @param propertyName
	 * @return
	 * @throws InvalidSPDXAnalysisException 
	 */
	protected Annotation[] findAnnotationPropertyValues(String nameSpace,
			String propertyName) throws InvalidSPDXAnalysisException {
		if (this.model == null || this.node == null) {
			return null;
		}
		ArrayList<Annotation> retval = new ArrayList<Annotation>();
		Node p = model.getProperty(nameSpace, propertyName).asNode();
		Triple m = Triple.createMatch(node, p, null);
		ExtendedIterator<Triple> tripleIter = model.getGraph().find(m);	
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			retval.add(new Annotation(this.modelContainer, t.getObject()));
		}
		return retval.toArray(new Annotation[retval.size()]);
	}
	
	/**
	 * Find all annotations with a subject of this object
	 * @param nameSpace
	 * @param propertyName
	 * @return
	 * @throws InvalidSPDXAnalysisException 
	 */
	protected Relationship[] findRelationshipPropertyValues(String nameSpace,
			String propertyName) throws InvalidSPDXAnalysisException {
		if (this.model == null || this.node == null) {
			return null;
		}
		ArrayList<Relationship> retval = new ArrayList<Relationship>();
		Node p = model.getProperty(nameSpace, propertyName).asNode();
		Triple m = Triple.createMatch(node, p, null);
		ExtendedIterator<Triple> tripleIter = model.getGraph().find(m);	
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			retval.add(new Relationship(this.modelContainer, t.getObject()));
		}
		return retval.toArray(new Relationship[retval.size()]);
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
	


	/**
	 * Set a property value for this resource.  Clears any existing resource.
	 * @param nameSpace
	 * @param propertyName
	 * @param relationships
	 * @throws InvalidSPDXAnalysisException 
	 */
	protected void setPropertyValues(String nameSpace,
			String propertyName, Relationship[] relationships) throws InvalidSPDXAnalysisException {
		if (model != null && resource != null) {
			Property p = model.createProperty(nameSpace, propertyName);
			model.removeAll(this.resource, p, null);
			if (relationships != null) {
				for (int i = 0; i < relationships.length; i++) {
					this.resource.addProperty(p, relationships[i].createResource(modelContainer));
				}
			}
		}
	}
	
	/**
	 * Set a property value for this resource.  Clears any existing resource.
	 * @param nameSpace
	 * @param propertyName
	 * @param licenseConcluded2
	 * @throws InvalidSPDXAnalysisException 
	 */
	protected void setPropertyValue(String nameSpace,
			String propertyName, AnyLicenseInfo license) throws InvalidSPDXAnalysisException {
		if (model != null && resource != null) {
			Property p = model.createProperty(nameSpace, propertyName);
			model.removeAll(this.resource, p, null);
			if (license != null) {
				this.resource.addProperty(p, license.createResource(this.modelContainer));
			}
		}
	}
	
	/**
	 * Find a property value with a subject of this object
	 * @param namespace Namespace for the property name
	 * @param propertyName Name of the property
	 * @return The AnyLicenseInfo value of the property or null if no property exists
	 * @throws InvalidSPDXAnalysisException 
	 */
	public AnyLicenseInfo findAnyLicenseInfoPropertyValue(String namespace, String propertyName) throws InvalidSPDXAnalysisException {
		if (this.model == null || this.node == null) {
			return null;
		}
		Node p = model.getProperty(namespace, propertyName).asNode();
		Triple m = Triple.createMatch(node, p, null);
		ExtendedIterator<Triple> tripleIter = model.getGraph().find(m);	
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			return LicenseInfoFactory.getLicenseInfoFromModel(modelContainer, t.getObject());
		}
		return null;
	}
}
