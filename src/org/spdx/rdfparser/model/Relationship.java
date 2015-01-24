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

import org.apache.log4j.Logger;
import org.spdx.rdfparser.IModelContainer;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SpdxRdfConstants;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * A Relationship represents a relationship between two SpdxElements.
 * @author Gary O'Neall
 *
 */
public class Relationship extends RdfModelObject {
	
	static final Logger logger = Logger.getLogger(RdfModelObject.class);
	
	public enum RelationshipType {
		 relationshipType_ancestorOf, relationshipType_buildToolOf,
		 relationshipType_containedBy, relationshipType_contains,
		 relationshipType_copyOf, relationshipType_dataFile,
		 relationshipType_descendantOf, relationshipType_distributionArtifact,
		 relationshipType_documentation, relationshipType_dynamicLink,
		 relationshipType_expandedFromArchive, relationshipType_fileAdded,
		 relationshipType_fileDeleted, relationshipType_fileModified,
		 relationshipType_generatedFrom, relationshipType_generates,
		 relationshipType_metafileOf, relationshipType_optionalComponentOf,
		 relationshipType_other, relationshipType_packageOf,
		 relationshipType_patchApplied, relationshipType_patchFor,
		 relationshipType_spdxAmendment, relationshipType_staticLink,
		 relationshipType_testcaseOf
	}

	private RelationshipType relationshipType;
	private String comment;
	private SpdxElement relatedSpdxElement;
	
	/**
	 * @param relatedSpdxElement The SPDX Element that is related
	 * @param relationshipType Type of relationship - See the specification for a description of the types
	 * @param comment optional comment for the relationship
	 */
	public Relationship(SpdxElement relatedSpdxElement, 
			RelationshipType relationshipType, String comment) {
		super();
		this.relatedSpdxElement = relatedSpdxElement;
		this.relationshipType = relationshipType;
		this.comment = comment;
	}
	/**
	 * @param model Model containing the relationship
	 * @param node Node describing the relationship
	 * @throws InvalidSPDXAnalysisException
	 */
	public Relationship(IModelContainer modelContainer, Node node)
			throws InvalidSPDXAnalysisException {
		super(modelContainer, node);
		this.relatedSpdxElement = findElementPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, 
				SpdxRdfConstants.PROP_RELATED_SPDX_ELEMENT);
		this.comment = findSinglePropertyValue(SpdxRdfConstants.RDFS_NAMESPACE, SpdxRdfConstants.RDFS_PROP_COMMENT);
		String relationshipTypeUri = findUriPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, 
				SpdxRdfConstants.PROP_RELATIONSHIP_TYPE);
		if (relationshipTypeUri != null) {
			String sRelationshipType = relationshipTypeUri.substring(SpdxRdfConstants.SPDX_NAMESPACE.length());
			try {
				this.relationshipType = RelationshipType.valueOf(sRelationshipType);
			}catch (Exception ex) {
				logger.error("Invalid relationship type found in the model - "+sRelationshipType);
				throw(new InvalidSPDXAnalysisException("Invalid relationship type: "+sRelationshipType));
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.IRdfModel#verify()
	 */
	@Override
	public ArrayList<String> verify() {
		ArrayList<String> retval;
		if (this.relatedSpdxElement == null) {
			retval = new ArrayList<String>();
			retval.add("Missing related SPDX element");
		} else {
			retval = this.relatedSpdxElement.verify();
		}
		if (this.relationshipType == null) {
			retval.add("Missing relationship type");
		}
		return retval;
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.RdfModelObject#getType(com.hp.hpl.jena.rdf.model.Model)
	 */
	@Override
	Resource getType(Model model) {
		return model.createResource(SpdxRdfConstants.SPDX_NAMESPACE + SpdxRdfConstants.CLASS_RELATIONSHIP);
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.RdfModelObject#populateModel()
	 */
	@Override
	void populateModel() throws InvalidSPDXAnalysisException {
		if (this.comment != null) {
			setPropertyValue(SpdxRdfConstants.RDFS_NAMESPACE, SpdxRdfConstants.RDFS_PROP_COMMENT, comment);
		}
		if (this.relatedSpdxElement != null) {
			// set the ID for the related element if it does not exist
			if (this.relatedSpdxElement.getId() == null) {
				this.relatedSpdxElement.setId(modelContainer.getNextSpdxElementRef());
			}
			setPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, 
					SpdxRdfConstants.PROP_RELATED_SPDX_ELEMENT, relatedSpdxElement);
		}
		if (this.relationshipType != null) {
			setPropertyUriValue(SpdxRdfConstants.SPDX_NAMESPACE, 
					SpdxRdfConstants.PROP_RELATIONSHIP_TYPE, 
					SpdxRdfConstants.SPDX_NAMESPACE + this.relationshipType.toString());
		}
	}
	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.RdfModelObject#getUri(org.spdx.rdfparser.IModelContainer)
	 */
	@Override
	String getUri(IModelContainer modelContainer) {
		// We will just use anonymous nodes for relationships
		return null;
	}
	/**
	 * @return the relationshipType
	 */
	public RelationshipType getRelationshipType() {
		if (model != null && this.refreshOnGet) {
			String relationshipTypeUri = findUriPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, 
					SpdxRdfConstants.PROP_RELATIONSHIP_TYPE);
			if (relationshipTypeUri != null) {
				String sRelationshipType = relationshipTypeUri.substring(SpdxRdfConstants.SPDX_NAMESPACE.length());
				try {
					this.relationshipType = RelationshipType.valueOf(sRelationshipType);
				}catch (Exception ex) {
					logger.error("Invalid relationship type found in the model - "+sRelationshipType);
				}
			}
		}
		return relationshipType;
	}
	/**
	 * @param relationshipType the relationshipType to set
	 * @throws InvalidSPDXAnalysisException 
	 */
	public void setRelationshipType(RelationshipType relationshipType) throws InvalidSPDXAnalysisException {
		this.relationshipType = relationshipType;
		if (relationshipType != null) {
			setPropertyUriValue(SpdxRdfConstants.SPDX_NAMESPACE, 
					SpdxRdfConstants.PROP_RELATIONSHIP_TYPE, 
					SpdxRdfConstants.SPDX_NAMESPACE + this.relationshipType.toString());
		} else {
			removePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, 
					SpdxRdfConstants.PROP_RELATIONSHIP_TYPE);
		}
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
		setPropertyValue(SpdxRdfConstants.RDFS_NAMESPACE, 
				SpdxRdfConstants.RDFS_PROP_COMMENT, comment);
	}
	/**
	 * @return the relatedSpdxElement
	 */
	public SpdxElement getRelatedSpdxElement() {
		if (this.resource != null && this.refreshOnGet) {
			try {
				SpdxElement refresh = findElementPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, 
						SpdxRdfConstants.PROP_RELATED_SPDX_ELEMENT);
				if (refresh == null || !refresh.equivalent(this.relatedSpdxElement)) {
					this.relatedSpdxElement = refresh;
				}
			} catch (InvalidSPDXAnalysisException e) {
				logger.error("Invalid related SPDX element found in the model", e);
			}
		}
		return relatedSpdxElement;
	}
	/**
	 * @param relatedSpdxElement the relatedSpdxElement to set
	 * @throws InvalidSPDXAnalysisException 
	 */
	public void setRelatedSpdxElement(SpdxElement relatedSpdxElement) throws InvalidSPDXAnalysisException {
		this.relatedSpdxElement = relatedSpdxElement;
		setPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, 
				SpdxRdfConstants.PROP_RELATED_SPDX_ELEMENT, relatedSpdxElement);
	}
	
	@Override
	public Relationship clone() {
		return new Relationship(this.relatedSpdxElement.clone(),
				this.relationshipType, this.comment);
	}
	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.RdfModelObject#equivalent(org.spdx.rdfparser.model.RdfModelObject)
	 */
	@Override
	public boolean equivalent(RdfModelObject o) {
		if (!(o instanceof Relationship)) {
			return false;
		}
		Relationship comp = (Relationship)o;
		return (equivalentConsideringNull(relatedSpdxElement, comp.getRelatedSpdxElement()) &&
				equalsConsideringNull(relationshipType, comp.getRelationshipType()) &&
				equalsConsideringNull(comment, comp.getComment()));
	}

}
