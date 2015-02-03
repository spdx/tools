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
import java.util.HashMap;
import java.util.HashSet;

import org.spdx.rdfparser.IModelContainer;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SPDXCreatorInformation;
import org.spdx.rdfparser.SPDXReview;
import org.spdx.rdfparser.SpdxPackageVerificationCode;
import org.spdx.rdfparser.SpdxRdfConstants;
import org.spdx.rdfparser.license.AnyLicenseInfo;
import org.spdx.rdfparser.license.LicenseInfoFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
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
 * - equivalent: Returns true if the parameter has the same property values
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
	
	// the following hashmaps translate between pre-defined 
	// property values and their URI's used to uniquely identify them
	// in the RDF model
	static final HashMap<String, String> PRE_DEFINED_VALUE_URI = new HashMap<String, String>();
	static final HashMap<String, String> PRE_DEFINED_URI_VALUE = new HashMap<String, String>();
	
	static {
		PRE_DEFINED_VALUE_URI.put(SpdxRdfConstants.NOASSERTION_VALUE, SpdxRdfConstants.URI_VALUE_NOASSERTION);
		PRE_DEFINED_URI_VALUE.put(SpdxRdfConstants.URI_VALUE_NOASSERTION, SpdxRdfConstants.NOASSERTION_VALUE);
		PRE_DEFINED_VALUE_URI.put(SpdxRdfConstants.NONE_VALUE, SpdxRdfConstants.URI_VALUE_NONE);
		PRE_DEFINED_URI_VALUE.put(SpdxRdfConstants.URI_VALUE_NONE, SpdxRdfConstants.NONE_VALUE);
	}
	
	protected Model model;
	protected Resource resource;
	protected Node node;
	protected IModelContainer modelContainer;
	
	/**
	 * Force a refresh for the model on every property get.  This is slower, but
	 * will make sure that the correct value is returned if there happens to be
	 * two Java objects using the same RDF properties.
	 */
	protected boolean refreshOnGet = true;	//TODO make this a configurable property
	
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
		return createResource(modelContainer, true);
	}
	
	/**
	 * @param modelContainer
	 * @param updateModel If true, update the model from the element.  If false, update the 
	 * element from the model.  This is used for relationships to make sure we don't overwrite
	 * the original element when setting the related element property value.
	 * @return
	 * @throws InvalidSPDXAnalysisException
	 */
	public Resource createResource(IModelContainer modelContainer, boolean updateModel) throws InvalidSPDXAnalysisException {
		if (this.modelContainer != null && this.modelContainer.equals(modelContainer) &&
				this.resource != null) {
			if (!this.resource.isURIResource() || this.resource.getURI().equals(getUri(modelContainer))) {
				return this.resource;
			}
		}
		String uri = getUri(modelContainer);
		Resource duplicate = findDuplicateResource(modelContainer, uri);
		// we need to wait to set the following to fields since they are checked
		// by some of the setters
		this.modelContainer = modelContainer;
		this.model = modelContainer.getModel();	
		if (duplicate != null) {
			this.resource = duplicate;
		} else if (uri == null) {			
			this.resource = model.createResource(getType(model));
		} else {
			this.resource = model.createResource(uri, getType(model));
		}
		this.node = this.resource.asNode();
		if (duplicate == null || updateModel) {
			populateModel();
		} else {
			getPropertiesFromModel();
		}
		return resource;
	};
	
	/**
	 * Fetch all of the properties from the model and populate the local Java properties
	 * @throws InvalidSPDXAnalysisException 
	 */
	abstract void getPropertiesFromModel() throws InvalidSPDXAnalysisException;

	/**
	 * Search the model to see if there is a duplicate resource either based on the
	 * URI or based on other information.  Subclasses may choose to override this
	 * method to prevent duplicate resource from being created with the same properties.
	 * @param modelContainer
	 * @param uri
	 * @return Any duplicate resource found.  Null if no duplicate resource was found.
	 * @throws InvalidSPDXAnalysisException 
	 */
	protected Resource findDuplicateResource(IModelContainer modelContainer, String uri) throws InvalidSPDXAnalysisException {
		if (uri == null || uri.isEmpty()) {
			return null;
		}
		Resource retval = ResourceFactory.createResource(uri);
		if (modelContainer.getModel().containsResource(retval)) {
			return modelContainer.getModel().getResource(uri);
		} else {
			return null;
		}
	}
	
	/**
	 * Get the URI for this RDF object. Null if this is for an anonomous node.
	 * @param modelContainer
	 * @return
	 * @throws InvalidSPDXAnalysisException 
	 */
	abstract String getUri(IModelContainer modelContainer) throws InvalidSPDXAnalysisException;
	
	/**
	 * @return the RDF class name for the object
	 */
	abstract Resource getType(Model model);
	
	/**
	 * Populate the RDF model from the Java properties
	 * @throws InvalidSPDXAnalysisException 
	 */
	abstract void populateModel() throws InvalidSPDXAnalysisException;
	
	/**
	 * Returns true if the two resources represent the same node
	 * @param r1
	 * @param r2
	 * @return
	 */
	protected boolean resourcesEqual(Resource r1,
			Resource r2) {
		if (r1 == null) {
			return (r2 == null);
		}
		if (r2 == null) {
			return false;
		}
		if (r1.isAnon()) {
			if (!r2.isAnon()) {
				return false;
			}
			return r1.getId().equals(r2.getId());
		} else {
			if (!r2.isURIResource()) {
				return false;
			}
			return r1.getURI().equals(r2.getURI());
		} 
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof RdfModelObject)) {
			return false;
		}
		RdfModelObject comp = (RdfModelObject)o;
		if (comp.resource != null || this.resource != null) {
			// We consider them to be equal if they represent the same
			// resource nocde
			return resourcesEqual(comp.resource, this.resource);
		} else {
			return super.equals(o);
		}
	}
	
	@Override
	public int hashCode() {
		if (this.resource != null) {
			return this.resource.hashCode() ^ model.hashCode();
		} else {
			return super.hashCode();
		}
	}
	
	public abstract boolean equivalent(RdfModelObject compare);
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
	 * Compares the properties of two RdfModelObjects considering possible null values
	 * @param o1
	 * @param o2
	 * @return
	 */
	protected boolean equivalentConsideringNull(RdfModelObject o1, RdfModelObject o2) {
		if (o1 == null) {
			return (o2 == null);
		} else {
			return o1.equivalent(o2);
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
		HashSet<Integer> foundIndexes = new HashSet<Integer>();
		for (int i = 0; i < array1.length; i++) {
			boolean found = false;
			for (int j = 0; j < array2.length; j++) {
				if (!foundIndexes.contains(j) &&
						equalsConsideringNull(array1[i],array2[j])) {
					found = true;
					foundIndexes.add(j);
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
	 * Compares 2 arrays to see if the property values for the element RdfModelObjects are the same independent of
	 * order and considering nulls
	 * @param array1
	 * @param array2
	 * @return
	 */
	protected boolean arraysEquivalent(RdfModelObject[] array1, RdfModelObject[] array2) {
		if (array1 == null) {
			return array2 == null;
		}
		if (array2 == null) {
			return false;
		}
		if (array1.length != array2.length) {
			return false;
		}
		HashSet<Integer> foundIndexes = new HashSet<Integer>();
		for (int i = 0; i < array1.length; i++) {
			boolean found = false;
			for (int j = 0; j < array2.length; j++) {
				if (!foundIndexes.contains(j) &&
						equivalentConsideringNull(array1[i],array2[j])) {
					found = true;
					foundIndexes.add(j);
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
	 * Finds all SPDX elements with a subject of this object
	 * @param namespace
	 * @param propertyName
	 * @return
	 * @throws InvalidSPDXAnalysisException 
	 */
	protected SpdxElement[] findMultipleElementPropertyValues(String namespace,
			String propertyName) throws InvalidSPDXAnalysisException {
		if (this.model == null || this.node == null) {
			return null;
		}
		Node p = model.getProperty(namespace, propertyName).asNode();
		Triple m = Triple.createMatch(node, p, null);
		ExtendedIterator<Triple> tripleIter = model.getGraph().find(m);	
		ArrayList<SpdxElement> retval = new ArrayList<SpdxElement>();
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			retval.add(SpdxElementFactory.createElementFromModel(modelContainer, 
					t.getObject()));
		}
		return retval.toArray(new SpdxElement[retval.size()]);
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
		SpdxElement[] elements = findMultipleElementPropertyValues(namespace, propertyName);
		if (elements != null && elements.length > 0) {
			return elements[0];
		} else {
			return null;
		}
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
			if (t.getObject().isURI()) {
				// check for predefined
				String retval = PRE_DEFINED_URI_VALUE.get(t.getObject().getURI());
				if (retval != null) {
					return retval;
				}
			}
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
			if (t.getObject().isURI() && PRE_DEFINED_URI_VALUE.containsKey(t.getObject().getURI())) {
				retval.add(PRE_DEFINED_URI_VALUE.get(t.getObject().getURI()));
			} else {
				retval.add(t.getObject().toString(false));
			}
		}
		return retval.toArray(new String[retval.size()]);
	}
	
	/**
	 * Set a property values for this resource.  Clears any existing resource.
	 * If the string matches one of the SPDX pre-defined string values, the URI
	 * for that value is stored.  Otherwise, it is stored as a literal value.
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
						String valueUri = PRE_DEFINED_VALUE_URI.get(values[i]);
						if (valueUri != null) {
							// this is a pre-defined "special" SPDX value
							Resource valueResource = this.model.createResource(valueUri);
							this.resource.addProperty(p, valueResource);
						} else {
							this.resource.addProperty(p, values[i]);
						}
					}
				}
			}
		}
	}
	
	/**
	 * Sets the spdx element property value for this resource
	 * @param nameSpace
	 * @param propertyName
	 * @param element
	 * @param updateModel If true, update the model from the element.  If false, update the 
	 * element from the model.  This is used for relationships to make sure we don't overwrite
	 * the original element when setting the related element property value.
	 * @throws InvalidSPDXAnalysisException
	 */
	protected void setPropertyValue(String nameSpace, String propertyName,
			SpdxElement[] elements, boolean updateModel) throws InvalidSPDXAnalysisException {
		if (model != null && resource != null) {
			Property p = model.createProperty(nameSpace, propertyName);
			model.removeAll(this.resource, p, null);
			if (elements != null) {
				for (int i = 0; i < elements.length; i++) {
					if (elements[i] != null) {
						this.resource.addProperty(p, elements[i].createResource(modelContainer, updateModel));
					}
				}		
			}
		}
	}
	
	/**
	 * Sets the spdx element property value for this resource
	 * @param nameSpace
	 * @param propertyName
	 * @param element
	 * @param updateModel If true, update the model from the element.  If false, update the 
	 * element from the model.  This is used for relationships to make sure we don't overwrite
	 * the original element when setting the related element property value.
	 * @throws InvalidSPDXAnalysisException
	 */
	protected void setPropertyValue(String nameSpace, String propertyName,
			SpdxElement element, boolean updateModel) throws InvalidSPDXAnalysisException {
		if (model != null && resource != null) {
			Property p = model.createProperty(nameSpace, propertyName);
			model.removeAll(this.resource, p, null);
			if (element != null) {
				this.resource.addProperty(p, element.createResource(modelContainer, updateModel));
			}
		}
	}
	
	/**
	 * Adds an SPDX element property value for this resource without removing the old property values
	 * @param nameSpace
	 * @param propertyName
	 * @param element
	 * @param updateModel If true, update the model from the element.  If false, update the 
	 * element from the model.  This is used for relationships to make sure we don't overwrite
	 * the original element when setting the related element property value.
	 * @throws InvalidSPDXAnalysisException
	 */
	protected void addPropertyValue(String nameSpace, String propertyName,
			SpdxElement element, boolean updateModel) throws InvalidSPDXAnalysisException {
		if (model != null && resource != null) {
			Property p = model.createProperty(nameSpace, propertyName);
			if (element != null) {
				this.resource.addProperty(p, element.createResource(modelContainer, updateModel));
			}
		}
	}
	
	/**
	 * Adds an SPDX element property value for this resource without removing the old property values
	 * @param nameSpace
	 * @param propertyName
	 * @param element
	 * @throws InvalidSPDXAnalysisException
	 */
	protected void addPropertyValue(String nameSpace, String propertyName,
			SpdxElement element) throws InvalidSPDXAnalysisException {
		addPropertyValue(nameSpace, propertyName, element, true);
	}
	
	protected void setPropertyValue(String nameSpace, String propertyName,
			SpdxElement element) throws InvalidSPDXAnalysisException {
		setPropertyValue(nameSpace, propertyName, element, true);
	}
	
	protected void setPropertyValue(String nameSpace, String propertyName,
			SpdxElement[] element) throws InvalidSPDXAnalysisException {
		setPropertyValue(nameSpace, propertyName, element, true);
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
	 * @param licenses
	 * @throws InvalidSPDXAnalysisException 
	 */
	protected void setPropertyValues(String nameSpace,
			String propertyName, AnyLicenseInfo[] licenses) throws InvalidSPDXAnalysisException {
		if (model != null && resource != null) {
			Property p = model.createProperty(nameSpace, propertyName);
			model.removeAll(this.resource, p, null);
			if (licenses != null) {
				for (int i = 0; i < licenses.length; i++) {
					if (licenses[i] != null) {
						this.resource.addProperty(p, licenses[i].createResource(this.modelContainer));
					}
				}
			}
		}
	}
	
	/**
	 * Set a property value for this resource.  Clears any existing resource.
	 * @param nameSpace
	 * @param propertyName
	 * @param license
	 * @throws InvalidSPDXAnalysisException 
	 */
	protected void setPropertyValue(String nameSpace,
			String propertyName, AnyLicenseInfo license) throws InvalidSPDXAnalysisException {
		setPropertyValues(nameSpace, propertyName, new AnyLicenseInfo[] {license});
	}
	
	/**
	 * Find a property value with a subject of this object
	 * @param namespace Namespace for the property name
	 * @param propertyName Name of the property
	 * @return The AnyLicenseInfo value of the property or null if no property exists
	 * @throws InvalidSPDXAnalysisException 
	 */
	public AnyLicenseInfo[] findAnyLicenseInfoPropertyValues(String namespace, String propertyName) throws InvalidSPDXAnalysisException {
		if (this.model == null || this.node == null) {
			return new AnyLicenseInfo[0];
		}
		ArrayList<AnyLicenseInfo> retval = new ArrayList<AnyLicenseInfo>();
		Node p = model.getProperty(namespace, propertyName).asNode();
		Triple m = Triple.createMatch(node, p, null);
		ExtendedIterator<Triple> tripleIter = model.getGraph().find(m);	
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			retval.add(LicenseInfoFactory.getLicenseInfoFromModel(modelContainer, t.getObject()));
		}
		return retval.toArray(new AnyLicenseInfo[retval.size()]);
	}

	/**
	 * Find a property value with a subject of this object
	 * @param namespace Namespace for the property name
	 * @param propertyName Name of the property
	 * @return The AnyLicenseInfo value of the property or null if no property exists
	 * @throws InvalidSPDXAnalysisException 
	 */
	public AnyLicenseInfo findAnyLicenseInfoPropertyValue(String namespace, String propertyName) throws InvalidSPDXAnalysisException {
		AnyLicenseInfo[] licenses = findAnyLicenseInfoPropertyValues(namespace, propertyName);
		if (licenses == null || licenses.length == 0) {
			return null;
		} else {
			return licenses[0];
		}
	}
	
	/**
	 * @param nameSpace
	 * @param propertyName
	 * @return
	 * @throws InvalidSPDXAnalysisException 
	 */
	protected Checksum[] findMultipleChecksumPropertyValues(String nameSpace,
			String propertyName) throws InvalidSPDXAnalysisException {
		if (this.model == null || this.node == null) {
			return new Checksum[0];
		}
		ArrayList<Checksum> retval = new ArrayList<Checksum>();
		Node p = model.getProperty(nameSpace, propertyName).asNode();
		Triple m = Triple.createMatch(node, p, null);
		ExtendedIterator<Triple> tripleIter = model.getGraph().find(m);	
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			retval.add(new Checksum(modelContainer, t.getObject()));
		}
		return retval.toArray(new Checksum[retval.size()]);
	}
	
	/**
	 * @param nameSpace
	 * @param propertyName
	 * @return
	 * @throws InvalidSPDXAnalysisException 
	 */
	protected Checksum findChecksumPropertyValue(String nameSpace,
			String propertyName) throws InvalidSPDXAnalysisException {
		Checksum[] checksums = findMultipleChecksumPropertyValues(nameSpace, propertyName);
		if (checksums == null || checksums.length == 0) {
			return null;
		} else {
			return checksums[0];
		}
	}
	
	/**
	 * Add a checksum as a property to this resource
	 * @param nameSpace
	 * @param propertyName
	 * @param checksumValues
	 * @throws InvalidSPDXAnalysisException 
	 */
	protected void addPropertyValue(String nameSpace,
			String propertyName, Checksum checksumValue) throws InvalidSPDXAnalysisException {
		if (model != null && resource != null) {
			Property p = model.createProperty(nameSpace, propertyName);
			if (checksumValue != null) {
				this.resource.addProperty(p, checksumValue.createResource(this.modelContainer));
			}
		}
	}
	/**
	 * @param nameSpace
	 * @param propertyName
	 * @param checksumValues
	 * @throws InvalidSPDXAnalysisException 
	 */
	protected void setPropertyValues(String nameSpace,
			String propertyName, Checksum[] checksumValues) throws InvalidSPDXAnalysisException {
		if (model != null && resource != null) {
			Property p = model.createProperty(nameSpace, propertyName);
			model.removeAll(this.resource, p, null);
			if (checksumValues != null) {
				for (int i = 0; i < checksumValues.length; i++) {
					if (checksumValues[i] != null) {
						this.resource.addProperty(p, checksumValues[i].createResource(this.modelContainer));
					}
				}
			}
		}
	}

	/**
	 * @param nameSpace
	 * @param propertyName
	 * @param checksumValue
	 * @throws InvalidSPDXAnalysisException 
	 */
	protected void setPropertyValue(String nameSpace,
			String propertyName, Checksum checksumValue) throws InvalidSPDXAnalysisException {
		if (checksumValue == null) {
			setPropertyValues(nameSpace, propertyName, new Checksum[0]);
		} else {
			setPropertyValues(nameSpace, propertyName, new Checksum[] {checksumValue});
		}	
	}
	
	/**
	 * @param nameSpace
	 * @param propertyName
	 * @param checksumValue
	 * @return
	 * @throws InvalidSPDXAnalysisException 
	 */
	protected DoapProject[] findMultipleDoapPropertyValues(String nameSpace,
			String propertyName) throws InvalidSPDXAnalysisException {
		if (this.model == null || this.node == null) {
			return new DoapProject[0];
		}
		ArrayList<DoapProject> retval = new ArrayList<DoapProject>();
		Node p = model.getProperty(nameSpace, propertyName).asNode();
		Triple m = Triple.createMatch(node, p, null);
		ExtendedIterator<Triple> tripleIter = model.getGraph().find(m);	
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			retval.add(new DoapProject(modelContainer, t.getObject()));
		}
		return retval.toArray(new DoapProject[retval.size()]);
	}
	
	/**
	 * @param nameSpace
	 * @param propertyName
	 * @param doapProjectValues
	 * @throws InvalidSPDXAnalysisException 
	 */
	protected void setPropertyValue(String nameSpace,
			String propertyName, DoapProject[] doapProjectValues) throws InvalidSPDXAnalysisException {
		if (model != null && resource != null) {
			Property p = model.createProperty(nameSpace, propertyName);
			model.removeAll(this.resource, p, null);
			if (doapProjectValues != null) {
				for (int i = 0; i < doapProjectValues.length; i++) {
					this.resource.addProperty(p, doapProjectValues[i].createResource(this.modelContainer));
				}
			}
		}
	}
	
	/**
	 * Find a single URI as a property value to this node
	 * @param namespace
	 * @param propertyName
	 * @return
	 */
	protected String findUriPropertyValue(String namespace,
			String propertyName) {
		String[] values = findUriPropertyValues(namespace, propertyName);
		if (values == null || values.length == 0) {
			return null;
		} else {
			return values[0];
		}
	}
	
	/**
	 * Find a single URI as a property value to this node
	 * @param namespace
	 * @param propertyName
	 * @return
	 */
	protected String[] findUriPropertyValues(String namespace,
			String propertyName) {
		if (this.model == null || this.node == null) {
			return new String[0];
		}
		Node p = model.getProperty(namespace, propertyName).asNode();
		Triple m = Triple.createMatch(node, p, null);
		ExtendedIterator<Triple> tripleIter = model.getGraph().find(m);	
		ArrayList<String> retval = new ArrayList<String>();
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			if (t.getObject().isURI()) {				
				retval.add(model.expandPrefix(t.getObject().getURI()));
			}
		}
		return retval.toArray(new String[retval.size()]);
	}
	
	/**
	 * Sets a property value as a list of Uris
	 * @param nameSpace
	 * @param propertyName
	 * @param uri
	 * @throws InvalidSPDXAnalysisException
	 */
	protected void setPropertyUriValues(String nameSpace,
			String propertyName, String[] uris) throws InvalidSPDXAnalysisException {
		if (model != null && resource != null) {
			Property p = model.createProperty(nameSpace, propertyName);
			model.removeAll(this.resource, p, null);
			if (uris != null) {
				for (int i = 0; i < uris.length; i++) {
					Resource uriResource = model.createResource(uris[i]);
					this.resource.addProperty(p, uriResource);
				}
			}
		}
	}
	
	/**
	 * Adds a property value as a list of Uris
	 * @param nameSpace
	 * @param propertyName
	 * @param uri
	 * @throws InvalidSPDXAnalysisException
	 */
	protected void addPropertyUriValue(String nameSpace,
			String propertyName, String uri) throws InvalidSPDXAnalysisException {
		if (model != null && resource != null) {
			Property p = model.createProperty(nameSpace, propertyName);
			if (uri != null) {
				Resource uriResource = model.createResource(uri);
				this.resource.addProperty(p, uriResource);
			}
		}
	}
	
	/**
	 * Sets a property value as a Uri
	 * @param nameSpace
	 * @param propertyName
	 * @param uri
	 * @throws InvalidSPDXAnalysisException
	 */
	protected void setPropertyUriValue(String nameSpace,
			String propertyName, String uri) throws InvalidSPDXAnalysisException {
		setPropertyUriValues(nameSpace, propertyName, new String[] {uri});
	}
	
	/**
	 * @param nameSpace
	 * @param propertyName
	 * @return
	 * @throws InvalidSPDXAnalysisException 
	 */
	protected SPDXCreatorInformation findCreationInfoPropertyValue(
			String nameSpace, String propertyName) throws InvalidSPDXAnalysisException {
		if (this.model == null || this.node == null) {
			return null;
		}
		Node p = model.getProperty(nameSpace, propertyName).asNode();
		Triple m = Triple.createMatch(node, p, null);
		ExtendedIterator<Triple> tripleIter = model.getGraph().find(m);	
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			return new SPDXCreatorInformation(model, t.getObject());
		}
		return null;
	}
	
	/**
	 * @param nameSpace
	 * @param propertyName
	 * @param creatorInfo
	 */
	protected void setPropertyValue(String nameSpace, 
			String propertyName, SPDXCreatorInformation creatorInfo) {
		if (model != null && resource != null) {
			Property p = model.createProperty(nameSpace, propertyName);
			model.removeAll(this.resource, p, null);
			if (creatorInfo != null) {
				this.resource.addProperty(p, creatorInfo.createResource(model));
			}
		}
	}
	
	/**
	 * @param nameSpace
	 * @param propSpdxExternalDocRef
	 * @return
	 * @throws InvalidSPDXAnalysisException 
	 */
	protected ExternalDocumentRef[] findExternalDocRefPropertyValues(
			String nameSpace, String propertyName) throws InvalidSPDXAnalysisException {
		if (this.model == null || this.node == null) {
			return new ExternalDocumentRef[0];
		}
		ArrayList<ExternalDocumentRef> retval = new ArrayList<ExternalDocumentRef>();
		Node p = model.getProperty(nameSpace, propertyName).asNode();
		Triple m = Triple.createMatch(node, p, null);
		ExtendedIterator<Triple> tripleIter = model.getGraph().find(m);	
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			retval.add(new ExternalDocumentRef(modelContainer, t.getObject()));
		}
		return retval.toArray(new ExternalDocumentRef[retval.size()]);
	}
	
	/**
	 * @param nameSpace
	 * @param propertyName
	 * @param externalDocRefs
	 * @throws InvalidSPDXAnalysisException 
	 */
	protected void setPropertyValues(String nameSpace, String propertyName, 
			ExternalDocumentRef[] externalDocRefs) throws InvalidSPDXAnalysisException {
		if (model != null && resource != null) {
			Property p = model.createProperty(nameSpace, propertyName);
			model.removeAll(this.resource, p, null);
			if (externalDocRefs != null) {
				for (int i = 0; i < externalDocRefs.length; i++) {
					this.resource.addProperty(p, externalDocRefs[i].createResource(modelContainer));
				}
			}
		}
	}
	

	/**
	 * @param nameSpace
	 * @param propertyName
	 * @param reviewers
	 */
	protected void setPropertyValues(String nameSpace,
			String propertyName, SPDXReview[] reviewers) {
		if (model != null && resource != null) {
			Property p = model.createProperty(nameSpace, propertyName);
			model.removeAll(this.resource, p, null);
			if (reviewers != null) {
				for (int i = 0; i < reviewers.length; i++) {
					this.resource.addProperty(p, reviewers[i].createResource(model));
				}
			}
		}
	}


	/**
	 * @param nameSpace
	 * @param propertyName
	 * @return
	 * @throws InvalidSPDXAnalysisException 
	 */
	protected SPDXReview[] findReviewPropertyValues(String nameSpace,
			String propertyName) throws InvalidSPDXAnalysisException {
		if (this.model == null || this.node == null) {
			return new SPDXReview[0];
		}
		ArrayList<SPDXReview> retval = new ArrayList<SPDXReview>();
		Node p = model.getProperty(nameSpace, propertyName).asNode();
		Triple m = Triple.createMatch(node, p, null);
		ExtendedIterator<Triple> tripleIter = model.getGraph().find(m);	
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			retval.add(new SPDXReview(model, t.getObject()));
		}
		return retval.toArray(new SPDXReview[retval.size()]);
	}
	
	/**
	 * @param nameSpace
	 * @param propertyName
	 * @return
	 * @throws InvalidSPDXAnalysisException
	 */
	protected SpdxPackageVerificationCode findVerificationCodePropertyValue(
			String nameSpace,String propertyName) throws InvalidSPDXAnalysisException {
		if (this.model == null || this.node == null) {
			return null;
		}
		Node p = model.getProperty(nameSpace, propertyName).asNode();
		Triple m = Triple.createMatch(node, p, null);
		ExtendedIterator<Triple> tripleIter = model.getGraph().find(m);	
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			return new SpdxPackageVerificationCode(model, t.getObject());
		}
		return null;
	}
	
	/**
	 * @param nameSpace
	 * @param propertyName
	 * @param verificationCode
	 * @throws InvalidSPDXAnalysisException
	 */
	protected void setPropertyValue(String nameSpace,
			String propertyName, SpdxPackageVerificationCode verificationCode) throws InvalidSPDXAnalysisException {
		if (model != null && resource != null) {
			Property p = model.createProperty(nameSpace, propertyName);
			model.removeAll(this.resource, p, null);
			if (verificationCode != null) {
				this.resource.addProperty(p, verificationCode.createResource(model));
			}
		}
	}
	
}
