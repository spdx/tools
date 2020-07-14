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

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spdx.rdfparser.IModelContainer;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SpdxRdfConstants;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * A Relationship represents a relationship between two SpdxElements.
 * @author Gary O'Neall
 *
 */
public class Relationship extends RdfModelObject implements Comparable<Relationship> {

	static final Logger logger = LoggerFactory.getLogger(RdfModelObject.class);

	public enum RelationshipType {
		DESCRIBES("relationshipType_describes"),
		DESCRIBED_BY("relationshipType_describedBy"),
		ANCESTOR_OF("relationshipType_ancestorOf"),
		BUILD_TOOL_OF("relationshipType_buildToolOf"),
		CONTAINED_BY("relationshipType_containedBy"),
		CONTAINS("relationshipType_contains"),
		COPY_OF("relationshipType_copyOf"),
		DATA_FILE_OF("relationshipType_dataFile"),
		DESCENDANT_OF("relationshipType_descendantOf"),
		DISTRIBUTION_ARTIFACT("relationshipType_distributionArtifact"),
		DOCUMENTATION_OF("relationshipType_documentation"),
		DYNAMIC_LINK("relationshipType_dynamicLink"),
		EXPANDED_FROM_ARCHIVE("relationshipType_expandedFromArchive"),
		FILE_ADDED("relationshipType_fileAdded"),
		FILE_DELETED("relationshipType_fileDeleted"),
		FILE_MODIFIED("relationshipType_fileModified"),
		GENERATED_FROM("relationshipType_generatedFrom"),
		GENERATES("relationshipType_generates"),
		METAFILE_OF("relationshipType_metafileOf"),
		OPTIONAL_COMPONENT_OF("relationshipType_optionalComponentOf"),
		OTHER("relationshipType_other"),
		PACKAGE_OF("relationshipType_packageOf"),
		PATCH_APPLIED("relationshipType_patchApplied"),
		PATCH_FOR("relationshipType_patchFor"),
		AMENDS("relationshipType_amends"),
		STATIC_LINK("relationshipType_staticLink"),
		TEST_CASE_OF("relationshipType_testcaseOf"),
		PREREQUISITE_FOR("relationshipType_prerequisiteFor"),
		HAS_PREREQUISITE("relationshipType_hasPrerequisite"),
		VARIANT_OF("relationshipType_variantOf"),
		BUILD_DEPENDENCY_OF("relationshipType_buildDependencyOf"),
		DEPENDENCY_MANIFEST_OF("relationshipType_dependencyManifestOf"),
		DEPENDENCY_OF("relationshipType_dependencyOf"),
		DEPENDS_ON("relationshipType_dependsOn"),
		DEV_DEPENDENCY_OF("relationshipType_devDependencyOf"),
		DEV_TOOL_OF("relationshipType_devToolOf"),
		EXAMPLE_OF("relationshipType_exampleOf"),
		OPTIONAL_DEPENDENCY_OF("relationshipType_optionalDependencyOf"),
		PROVIDED_DEPENDENCY_OF("relationshipType_providedDependencyOf"),
		RUNTIME_DEPENDENCY_OF("relationshipType_runtimeDependencyOf"),
		TEST_DEPENDENCY_OF("relationshipType_testDependencyOf"),
		TEST_OF("relationshipType_testOf"),
		TEST_TOOL_OF("relationshipType_testToolOf"),
		NONE("relationshipType_none"),
		NOASSERTION("relationshipType_noAssertion");

		private static final Map<String, RelationshipType> STRING_TO_TYPE;
		private String rdfString;

		static {
			ImmutableMap.Builder<String, RelationshipType> stringToTypeBuilder = new ImmutableMap.Builder<String, RelationshipType>();
			for (RelationshipType type : RelationshipType.values()) {
				stringToTypeBuilder.put(type.toString(), type);
			}
			STRING_TO_TYPE = stringToTypeBuilder.build();
		}

		RelationshipType(String rdfString) {
			this.rdfString = rdfString;
		}

		@Override
		public String toString() {
			return rdfString;
		}

		public static RelationshipType fromString(String rdfString) {
			return STRING_TO_TYPE.get(rdfString);
		}

		@Deprecated
		/**
		 * Use {@link toTag()} instead.
		 * @deprecated
		 */
		public String getTag(){
			return toTag();
		}

		/** @return Returns the tag value for this relationship type */
		public String toTag(){
			return name();
		}

		/** @return  The relationship type corresponding to the provided tag */
		public static RelationshipType fromTag(String tag){
			return valueOf(tag);
		}
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
	public void getPropertiesFromModel() throws InvalidSPDXAnalysisException {
		this.relatedSpdxElement = findElementPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE,
				SpdxRdfConstants.PROP_RELATED_SPDX_ELEMENT);
		this.comment = findSinglePropertyValue(SpdxRdfConstants.RDFS_NAMESPACE, SpdxRdfConstants.RDFS_PROP_COMMENT);
		String relationshipTypeUri = findUriPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE,
				SpdxRdfConstants.PROP_RELATIONSHIP_TYPE);
		if (relationshipTypeUri != null) {
			String relationshipString = relationshipTypeUri.substring(SpdxRdfConstants.SPDX_NAMESPACE.length());
			try {
				this.relationshipType = RelationshipType.fromString(relationshipString);
			}catch (Exception ex) {
				logger.error("Invalid relationship type found in the model - " + relationshipString);
				throw(new InvalidSPDXAnalysisException("Invalid relationship type: " + relationshipString));
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
	 * @see org.spdx.rdfparser.model.RdfModelObject#getType(org.apache.jena.rdf.model.Model)
	 */
	@Override
	public Resource getType(Model model) {
		return model.createResource(SpdxRdfConstants.SPDX_NAMESPACE + SpdxRdfConstants.CLASS_RELATIONSHIP);
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.RdfModelObject#populateModel()
	 */
	@Override
	public void populateModel() throws InvalidSPDXAnalysisException {
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
	public String getUri(IModelContainer modelContainer) {
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
				String relationshipString = relationshipTypeUri.substring(SpdxRdfConstants.SPDX_NAMESPACE.length());
				try {
					this.relationshipType = RelationshipType.fromString(relationshipString);
				}catch (Exception ex) {
					logger.error("Invalid relationship type found in the model - " + relationshipString);
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
