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
import java.util.Arrays;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.spdx.rdfparser.IModelContainer;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.RdfModelHelper;
import org.spdx.rdfparser.SpdxRdfConstants;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * An SpdxElement is any thing described in SPDX, either a document or an SpdxItem. 
 * SpdxElements can be related to other SpdxElements.
 * 
 * All subclasses should override getType, equals and hashCode.
 * 
 * If a subproperty is used for the name property name, getNamePropertyName should be overridden.
 * 
 * If absolute URIs are required, getUri should be overriden.
 * 
 * @author Gary O'Neall
 *
 */
public class SpdxElement extends RdfModelObject {
	
	static final Logger logger = Logger.getLogger(RdfModelObject.class);
	
	protected Annotation[] annotations;
	protected String comment;
	protected String name;
	protected Relationship[] relationships;
	/**
	 * The ID is a special property for the SpdxElement.  It used to create
	 * the unique URI for the item.  The URI is the namespace of the modelContainer + id
	 */
	private String id;
	
	public SpdxElement(IModelContainer modelContainer, Node node) throws InvalidSPDXAnalysisException {
		super(modelContainer, node);
		getPropertiesFromModel();
		SpdxElementFactory.addToCreatedElements(modelContainer, node, this);
	}
	
	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.RdfModelObject#getPropertiesFromModel()
	 */
	@Override
	void getPropertiesFromModel() throws InvalidSPDXAnalysisException {
		SpdxElementFactory.addToCreatedElements(modelContainer, node, this);
		this.annotations = findAnnotationPropertyValues(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_ANNOTATION);
		this.comment = findSinglePropertyValue(SpdxRdfConstants.RDFS_NAMESPACE, SpdxRdfConstants.RDFS_PROP_COMMENT);
		this.name = findSinglePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, this.getNamePropertyName());
		this.relationships = findRelationshipPropertyValues(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_RELATIONSHIP);
		// ID
		this.id = null;
		if (this.resource.isURIResource()) {
			if (this.resource.getURI().startsWith(modelContainer.getDocumentNamespace())) {
				this.id = this.resource.getURI().substring(modelContainer.getDocumentNamespace().length());
			} else {
				// look for external document ID
				String[] parts = this.resource.getURI().split("#");
				if (parts.length == 2) {
					String docId = this.modelContainer.documentNamespaceToId(parts[0]);
					if (docId != null) {
						this.id = docId + ":" + parts[1];
					}
				}
			}
		}
	}

	/**
	 * @param name Name of the element
	 * @param comment Optional comment on the element
	 * @param annotations Optional annotations for the element
	 * @param relationships Optional relationships with other elements
	 */
	public SpdxElement(String name, String comment, Annotation[] annotations,
			Relationship[] relationships) {
		this.name = name;
		this.comment = comment;
		if (annotations != null) {
			this.annotations = annotations;
		} else {
			this.annotations = new Annotation[0];
		}
		if (relationships != null) {
			this.relationships = relationships;
		} else {
			this.relationships = new Relationship[0];
		}
	}
	
	protected void populateModel() throws InvalidSPDXAnalysisException {
		if (this.resource != null) {
			if (this.name != null) {
				setPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, getNamePropertyName(), name);
			}
			if (this.comment != null) {
				setPropertyValue(SpdxRdfConstants.RDFS_NAMESPACE, SpdxRdfConstants.RDFS_PROP_COMMENT, comment);
			}
			if (this.annotations != null) {
				setPropertyValues(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_ANNOTATION, annotations);
			}
			if (this.relationships != null) {
				setPropertyValues(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_RELATIONSHIP, relationships);
			}
			SpdxElementFactory.addToCreatedElements(modelContainer, node, this);
		}
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.IRdfModel#verify()
	 */
	@Override
	public ArrayList<String> verify() {
		ArrayList<String> retval = new ArrayList<String>();
		if (this.name == null) {
			retval.add("Missing required name for type "+this.getClass().getName());
		}
		return retval;
	}
	
	/**
	 * Add the name of the element to all strings in the arraylist
	 * @param warnings
	 */
	protected void addNameToWarnings(ArrayList<String> warnings) {
		if (warnings == null) {
			return;
		}
		String localName = this.name;
		if (localName == null) {
			localName = "[UNKNOWN]";
		}
		for (int i = 0; i < warnings.size(); i++) {
			warnings.set(i, warnings.get(i)+" in "+localName);
		}
	}


	/**
	 * @return the annotations
	 */
	public Annotation[] getAnnotations() {
		if (model != null && this.refreshOnGet) {
			try {
				Annotation[] refresh = findAnnotationPropertyValues(SpdxRdfConstants.SPDX_NAMESPACE, 
						SpdxRdfConstants.PROP_ANNOTATION);
				if (refresh == null || !RdfModelHelper.arraysEquivalent(refresh, this.annotations)) {
					this.annotations = refresh;
				}
			} catch (InvalidSPDXAnalysisException e) {
				logger.error("Invalid annotations in the model",e);
			}
		}
		return annotations;
	}


	/**
	 * @param annotations the annotations to set
	 * @throws InvalidSPDXAnalysisException 
	 */
	public void setAnnotations(Annotation[] annotations) throws InvalidSPDXAnalysisException {
		if (annotations == null) {
			this.annotations = new Annotation[0];
		} else {
			this.annotations = annotations;
		}
		setPropertyValues(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_ANNOTATION, annotations);
	}


	/**
	 * @return the comment
	 */
	public String getComment() {
		if (this.resource != null && this.refreshOnGet) {
			this.comment = findSinglePropertyValue(SpdxRdfConstants.RDFS_NAMESPACE, SpdxRdfConstants.RDFS_PROP_COMMENT);
		}
		return comment;
	}


	/**
	 * @param comment the comment to set
	 */
	public void setComment(String comment) {
		this.comment = comment;
		setPropertyValue(SpdxRdfConstants.RDFS_NAMESPACE, SpdxRdfConstants.RDFS_PROP_COMMENT, comment);
	}


	/**
	 * @return the name
	 */
	public String getName() {
		if (this.resource != null && this.refreshOnGet) {
			this.name = findSinglePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, this.getNamePropertyName());
		}
		return name;
	}

	/**
	 * @return the property name used for the Name property.  Override this function if using a subproperty of SPDX Name
	 */
	protected String getNamePropertyName() {
		return SpdxRdfConstants.PROP_NAME;
	}

	/**
	 * Set the name
	 * @param name the name to set 
	 */
	public void setName(String name) {
		this.name = name;
		setPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, getNamePropertyName(), name);
	}


	/**
	 * @return the relationships
	 */
	public Relationship[] getRelationships() {
		if (model != null && this.refreshOnGet) {
			try {
				Relationship[] refresh = findRelationshipPropertyValues(SpdxRdfConstants.SPDX_NAMESPACE,
						SpdxRdfConstants.PROP_RELATIONSHIP);
				if (refresh == null && !RdfModelHelper.arraysEquivalent(refresh, this.relationships)) {
					this.relationships = refresh;
				}
			} catch (InvalidSPDXAnalysisException e) {
				logger.error("Invalid relationships in the model",e);
			}
		}
		return relationships;
	}


	/**
	 * @param relationships the relationships to set
	 * @throws InvalidSPDXAnalysisException 
	 */
	public void setRelationships(Relationship[] relationships) throws InvalidSPDXAnalysisException {
		if (relationships == null) {
			this.relationships = new Relationship[0];
		} else {
			this.relationships = relationships;
		}
		setPropertyValues(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_RELATIONSHIP, relationships);
	}
	
	/**
	 * The ID is a unique identify for the SPDX element.  It is only required
	 * if this element is to be used outside of the RDF model containing the element.
	 * @return the id
	 */
	public String getId() {
		// the ID from the resource URI.
		if (this.resource != null) {
			if (this.resource.isURIResource()) {
				if (this.resource.getURI().startsWith(modelContainer.getDocumentNamespace())) {
					this.id = this.resource.getURI().substring(modelContainer.getDocumentNamespace().length());
				}
			}
		}
		return id;
	}

	/**
	 * The ID is a unique identify for the SPDX element.  It is only required
	 * if this element is to be used outside of the RDF model containing the element.
	 * @param id
	 * @throws InvalidSPDXAnalysisException
	 */
	public void setId(String id) throws InvalidSPDXAnalysisException {
		if (this.modelContainer != null) {
			if (this.resource != null) {
				throw(new InvalidSPDXAnalysisException("Can not set a file ID for an SPDX element already in an RDF Model. You must create a new SPDX File with this ID."));
			}
		}
		this.id = id;
	}
	

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.RdfModelObject#getUri(org.spdx.rdfparser.IModelContainer)
	 */
	@Override
	String getUri(IModelContainer modelContainer) throws InvalidSPDXAnalysisException {
		if (this.node != null && this.node.isURI()) {
			return this.node.getURI();
		} else {
			if (this.id == null || this.id.isEmpty()) {
				this.id = modelContainer.getNextSpdxElementRef();
			} else if (modelContainer.spdxElementRefExists(this.id)) {
				throw(new InvalidSPDXAnalysisException("Duplicate ID: "+this.id));
			} else {
				modelContainer.addSpdxElementRef(id);
			}
			return modelContainer.getDocumentNamespace() + this.id;
		}
	}


	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.RdfModelObject#getType(com.hp.hpl.jena.rdf.model.Model)
	 */
	@Override
	Resource getType(Model model) {
		return model.createResource(SpdxRdfConstants.SPDX_NAMESPACE + SpdxRdfConstants.CLASS_SPDX_ELEMENT);
	}

	protected Annotation[] cloneAnnotations() {
		if (this.annotations == null) {
			return null;
		}
		Annotation[] clonedAnnotations = new Annotation[this.annotations.length];
		for (int i = 0; i < this.annotations.length; i++) {
			clonedAnnotations[i] = this.annotations[i].clone();
		}
		return clonedAnnotations;
	}
	
	protected Relationship[] cloneRelationships(HashMap<String, SpdxElement> clonedElementIds) {
		if (this.relationships == null) {
			return null;
		}
		Relationship[] clonedRelationships =new Relationship[this.relationships.length];
		for (int i = 0; i < this.relationships.length; i++) {
			clonedRelationships[i] = this.relationships[i].clone(clonedElementIds);
		}
		return clonedRelationships;
	}
	
	@Override
	public SpdxElement clone() {
		return clone(new HashMap<String, SpdxElement>());
	}
	
	/**
	 * Clones this element, but prevents infinite recursion by 
	 * keeping track of all elements which have been cloned
	 * @param clonedElementIds element ID's fo all elements which have been cloned
	 * @return
	 */
	public SpdxElement clone(HashMap<String, SpdxElement> clonedElementIds) {
		if (clonedElementIds.containsKey(this.getId())) {
			return clonedElementIds.get(this.getId());
		}
		SpdxElement retval = new SpdxElement(this.name, this.comment, cloneAnnotations(), 
				null);
		clonedElementIds.put(this.getId(), retval);
		try {
			retval.setRelationships(cloneRelationships(clonedElementIds));
		} catch (InvalidSPDXAnalysisException e) {
			logger.error("Unexected error setting relationships during clone",e);
		}
		return retval;
	}
	
	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.RdfModelObject#equivalent(org.spdx.rdfparser.model.RdfModelObject)
	 */
	@Override
	public boolean equivalent(IRdfModel o) {
		return equivalent(o, true);
	}


	/**
	 * Test for equivalent
	 * @param o
	 * @param testRelationships If true, test relationships
	 * @return
	 */
	public boolean equivalent(IRdfModel o, boolean testRelationships) {
		if (!(o instanceof SpdxElement)) {
			return false;
		}
		SpdxElement comp = (SpdxElement)o;
		
		if (testRelationships && !RdfModelHelper.arraysEquivalent(comp.getRelationships(), this.getRelationships())) {
			return false;
		}
		return (RdfModelHelper.equalsConsideringNull(comp.getName(), this.getName()) &&
				RdfModelHelper.arraysEquivalent(comp.getAnnotations(), this.getAnnotations()) &&
				RdfModelHelper.equalsConsideringNull(comp.getComment(), this.getComment()));
	}
	
	
	@Override
	public String toString() {
		if (this.name == null) {
			return super.toString();
		} else {
			return this.name;
		}
	}
	
	/**
	 * @param describesRelationship
	 * @throws InvalidSPDXAnalysisException 
	 */
	public void addRelationship(Relationship relationship) throws InvalidSPDXAnalysisException {
		if (relationship != null) {
			this.relationships = Arrays.copyOf(this.relationships, this.relationships.length + 1);
			this.relationships[this.relationships.length-1] = relationship;
			addPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_RELATIONSHIP, relationship);
		} 		
	}

	/**
	 * @param annotation
	 * @throws InvalidSPDXAnalysisException 
	 */
	public void addAnnotation(Annotation annotation) throws InvalidSPDXAnalysisException {
		if (annotation != null) {
			this.annotations = Arrays.copyOf(this.annotations, this.annotations.length + 1);
			this.annotations[this.annotations.length-1] = annotation;
			addPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_ANNOTATION, annotation);
		}
	}
}
