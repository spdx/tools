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

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.spdx.rdfparser.IModelContainer;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SpdxRdfConstants;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * A Relationship represents a relationship between two SpdxElements.
 * @author Gary O'Neall
 *
 */
public class Relationship extends RdfModelObject implements Comparable<Relationship> {
	
	static final Logger logger = Logger.getLogger(RdfModelObject.class);
	
	public enum RelationshipType {
		relationshipType_describes, relationshipType_describedBy,
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
		 relationshipType_amends, relationshipType_staticLink,
		 relationshipType_testcaseOf, relationshipType_prerequisiteFor,
		 relationshipType_hasPrerequisite;

		
		/** @return Returns the tag value for this relationship type */
		public String getTag(){
			return RELATIONSHIP_TYPE_TO_TAG.get(this);
		}

		/** @return  The relationship type corresponding to the provided tag */
		public static RelationshipType fromTag(String tag){
			return TAG_TO_RELATIONSHIP_TYPE.get(tag);
		}
	}

	@Deprecated
	/**
	 * Use {@link RelationshipType#getTag()} instead.
	 * @deprecated
	 */
	public static final Map<RelationshipType, String> RELATIONSHIP_TYPE_TO_TAG;

	@Deprecated
	/**
	 * Use {@link #RelationshipType.fromTag} instead.
	 * @deprecated
	 */
	public static final Map<String, RelationshipType> TAG_TO_RELATIONSHIP_TYPE;

	static {
		ImmutableMap.Builder<RelationshipType, String> relationshipTypeToTagBuilder = new ImmutableBiMap.Builder<RelationshipType, String>();
		ImmutableMap.Builder<String, RelationshipType> tagToRelationshipTypeBuilder = new ImmutableMap.Builder<String, RelationshipType>();
		
		relationshipTypeToTagBuilder.put(RelationshipType.relationshipType_describes, "DESCRIBES");
		tagToRelationshipTypeBuilder.put("DESCRIBES", RelationshipType.relationshipType_describes);
		relationshipTypeToTagBuilder.put(RelationshipType.relationshipType_describedBy, "DESCRIBED_BY");
		tagToRelationshipTypeBuilder.put("DESCRIBED_BY", RelationshipType.relationshipType_describedBy);
		relationshipTypeToTagBuilder.put(RelationshipType.relationshipType_hasPrerequisite, "HAS_PREQUISITE");
		tagToRelationshipTypeBuilder.put("HAS_PREQUISITE", RelationshipType.relationshipType_hasPrerequisite);
		relationshipTypeToTagBuilder.put(RelationshipType.relationshipType_prerequisiteFor, "PREQUISITE_FOR");
		tagToRelationshipTypeBuilder.put("PREQUISITE_FOR", RelationshipType.relationshipType_prerequisiteFor);
		relationshipTypeToTagBuilder.put(RelationshipType.relationshipType_ancestorOf, "ANCESTOR_OF");
		tagToRelationshipTypeBuilder.put("ANCESTOR_OF", RelationshipType.relationshipType_ancestorOf);
		relationshipTypeToTagBuilder.put(RelationshipType.relationshipType_buildToolOf, "BUILD_TOOL_OF");
		tagToRelationshipTypeBuilder.put("BUILD_TOOL_OF",RelationshipType.relationshipType_buildToolOf);
		relationshipTypeToTagBuilder.put(RelationshipType.relationshipType_containedBy, "CONTAINED_BY");
		tagToRelationshipTypeBuilder.put("CONTAINED_BY", RelationshipType.relationshipType_containedBy);
		relationshipTypeToTagBuilder.put(RelationshipType.relationshipType_contains, "CONTAINS");
		tagToRelationshipTypeBuilder.put("CONTAINS", RelationshipType.relationshipType_contains);
		relationshipTypeToTagBuilder.put(RelationshipType.relationshipType_copyOf, "COPY_OF");
		tagToRelationshipTypeBuilder.put("COPY_OF", RelationshipType.relationshipType_copyOf);
		relationshipTypeToTagBuilder.put(RelationshipType.relationshipType_dataFile, "DATA_FILE_OF");
		tagToRelationshipTypeBuilder.put("DATA_FILE_OF", RelationshipType.relationshipType_dataFile);
		relationshipTypeToTagBuilder.put(RelationshipType.relationshipType_descendantOf, "DESCENDANT_OF");
		tagToRelationshipTypeBuilder.put("DESCENDANT_OF", RelationshipType.relationshipType_descendantOf);
		relationshipTypeToTagBuilder.put(RelationshipType.relationshipType_distributionArtifact, "DISTRIBUTION_ARTIFACT");
		tagToRelationshipTypeBuilder.put("DISTRIBUTION_ARTIFACT", RelationshipType.relationshipType_distributionArtifact);
		relationshipTypeToTagBuilder.put(RelationshipType.relationshipType_documentation, "DOCUMENTATION_OF");
		tagToRelationshipTypeBuilder.put("DOCUMENTATION_OF", RelationshipType.relationshipType_documentation);
		relationshipTypeToTagBuilder.put(RelationshipType.relationshipType_dynamicLink, "DYNAMIC_LINK");
		tagToRelationshipTypeBuilder.put("DYNAMIC_LINK", RelationshipType.relationshipType_dynamicLink);
		relationshipTypeToTagBuilder.put(RelationshipType.relationshipType_expandedFromArchive, "EXPANDED_FROM_ARCHIVE");
		tagToRelationshipTypeBuilder.put("EXPANDED_FROM_ARCHIVE", RelationshipType.relationshipType_expandedFromArchive);
		relationshipTypeToTagBuilder.put(RelationshipType.relationshipType_fileAdded, "FILE_ADDED");
		tagToRelationshipTypeBuilder.put("FILE_ADDED", RelationshipType.relationshipType_fileAdded);
		relationshipTypeToTagBuilder.put(RelationshipType.relationshipType_fileDeleted, "FILE_DELETED");
		tagToRelationshipTypeBuilder.put("FILE_DELETED", RelationshipType.relationshipType_fileDeleted);
		relationshipTypeToTagBuilder.put(RelationshipType.relationshipType_fileModified, "FILE_MODIFIED");
		tagToRelationshipTypeBuilder.put("FILE_MODIFIED", RelationshipType.relationshipType_fileModified);
		relationshipTypeToTagBuilder.put(RelationshipType.relationshipType_generatedFrom, "GENERATED_FROM");
		tagToRelationshipTypeBuilder.put("GENERATED_FROM", RelationshipType.relationshipType_generatedFrom);
		relationshipTypeToTagBuilder.put(RelationshipType.relationshipType_generates, "GENERATES");
		tagToRelationshipTypeBuilder.put("GENERATES", RelationshipType.relationshipType_generates);
		relationshipTypeToTagBuilder.put(RelationshipType.relationshipType_metafileOf, "METAFILE_OF");
		tagToRelationshipTypeBuilder.put("METAFILE_OF", RelationshipType.relationshipType_metafileOf);
		relationshipTypeToTagBuilder.put(RelationshipType.relationshipType_optionalComponentOf, "OPTIONAL_COMPONENT_OF");
		tagToRelationshipTypeBuilder.put("OPTIONAL_COMPONENT_OF", RelationshipType.relationshipType_optionalComponentOf);
		relationshipTypeToTagBuilder.put(RelationshipType.relationshipType_other, "OTHER");
		tagToRelationshipTypeBuilder.put("OTHER",RelationshipType.relationshipType_other);
		relationshipTypeToTagBuilder.put(RelationshipType.relationshipType_packageOf, "PACKAGE_OF");
		tagToRelationshipTypeBuilder.put("PACKAGE_OF", RelationshipType.relationshipType_packageOf);
		relationshipTypeToTagBuilder.put(RelationshipType.relationshipType_patchApplied, "PATCH_APPLIED");
		tagToRelationshipTypeBuilder.put("PATCH_APPLIED", RelationshipType.relationshipType_patchApplied);
		relationshipTypeToTagBuilder.put(RelationshipType.relationshipType_patchFor, "PATCH_FOR");
		tagToRelationshipTypeBuilder.put("PATCH_FOR", RelationshipType.relationshipType_patchFor);
		relationshipTypeToTagBuilder.put(RelationshipType.relationshipType_amends, "AMENDS");
		tagToRelationshipTypeBuilder.put("AMENDS", RelationshipType.relationshipType_amends);
		relationshipTypeToTagBuilder.put(RelationshipType.relationshipType_staticLink, "STATIC_LINK");
		tagToRelationshipTypeBuilder.put("STATIC_LINK", RelationshipType.relationshipType_staticLink);
		relationshipTypeToTagBuilder.put(RelationshipType.relationshipType_testcaseOf, "TEST_CASE_OF");
		tagToRelationshipTypeBuilder.put("TEST_CASE_OF", RelationshipType.relationshipType_testcaseOf);
		
		TAG_TO_RELATIONSHIP_TYPE = tagToRelationshipTypeBuilder.build();
		RELATIONSHIP_TYPE_TO_TAG = relationshipTypeToTagBuilder.build();
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
		this.refreshOnGet = true;
	}
	/**
	 * @param model Model containing the relationship
	 * @param node Node describing the relationship
	 * @throws InvalidSPDXAnalysisException
	 */
	public Relationship(IModelContainer modelContainer, Node node)
			throws InvalidSPDXAnalysisException {
		super(modelContainer, node);
		getPropertiesFromModel();
	}
	
	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.RdfModelObject#getPropertiesFromModel()
	 */
	@Override
	void getPropertiesFromModel() throws InvalidSPDXAnalysisException {
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
	public List<String> verify() {
		List<String> retval;
		if (this.relatedSpdxElement == null) {
			retval = Lists.newArrayList();
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
			setPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, 
					SpdxRdfConstants.PROP_RELATED_SPDX_ELEMENT, relatedSpdxElement, false);
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
				if (refresh == null || !refresh.equivalent(this.relatedSpdxElement, false)) {
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
		return clone(Maps.<String, SpdxElement>newHashMap());
	}
	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.RdfModelObject#equivalent(org.spdx.rdfparser.model.RdfModelObject)
	 */
	@Override
	public boolean equivalent(IRdfModel o) {
		if (o == this) {
			return true;
		}
		if (!(o instanceof Relationship)) {
			return false;
		}
		Relationship comp = (Relationship)o;
		if (relatedSpdxElement == null) {
			if (comp.getRelatedSpdxElement() != null) {
				return false;
			} else {
                return (Objects.equal(relationshipType, comp.getRelationshipType()) && Objects.equal(comment, comp.getComment()));
			}
		} else {
			return (relatedSpdxElement.equivalent(comp.getRelatedSpdxElement(), false) &&	// Note - we don't want to test relationships since that may send us into an infinite loop
                    Objects.equal(relationshipType, comp.getRelationshipType()) && Objects.equal(comment, comp.getComment()));
		}
	}
	/**
	 * @param clonedElementIds
	 * @return
	 */
	public Relationship clone(Map<String, SpdxElement> clonedElementIds) {
		return new Relationship(this.relatedSpdxElement.clone(clonedElementIds), this.relationshipType, this.comment);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Relationship o) {
		if (o.getRelationshipType() == null) {
			if (this.relationshipType != null) {
				return 1;
			}
		}
		if (this.relationshipType == null) {
			return -1;
		}
		int retval = this.relationshipType.toString().compareTo(o.getRelationshipType().toString());
		if (retval != 0) {
			return retval;
		}
		SpdxElement compareRelatedElement = o.getRelatedSpdxElement();
		if (compareRelatedElement == null || compareRelatedElement.getId() == null) {
			if (this.relatedSpdxElement != null && this.relatedSpdxElement.getId() != null) {
				return 1;
			}
		}
		if (this.relatedSpdxElement == null || this.relatedSpdxElement.getId() == null) {
			return -1;
		}
		retval = this.relatedSpdxElement.getId().compareTo(compareRelatedElement.getId());
		if (retval != 0) {
			return retval;
		}
		if (o.getComment() == null) {
			if (this.comment != null) {
				return 1;
			}
		}
		if (this.comment == null) {
			return -1;
		}
		return this.comment.compareTo(o.getComment());
	}
}
