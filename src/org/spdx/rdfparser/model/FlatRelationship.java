/**
 * Copyright (c) 2017 Source Auditor Inc.
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
 */
package org.spdx.rdfparser.model;

import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.model.Relationship.RelationshipType;

/**
 * Represents a relationship using only the ID of the related element rather than the full element.
 * 
 * This representation can be used as an alternative to Relationship when trying to avoid infinate
 * recursion from referencing the related object.
 * 
 * @author Gary O'Neall
 *
 */
public class FlatRelationship {

	private RelationshipType relationshipType;
	private String comment;
	private String relatedSpdxElementId;
	
	/**
	 * @param relationshipType type of relationship
	 * @param relatedSpdxElementId String conforming to the data format for relationship document references (section 7.1.4 in the 2.1 version of the SPDX specification)
	 * @param comment Comment related to the relationship (optional)
	 */
	public FlatRelationship(RelationshipType relationshipType, String relatedSpdxElementId, String comment) {
		this.relatedSpdxElementId = relatedSpdxElementId;
		this.comment = comment;
		this.relationshipType = relationshipType;
	}
	
	/**
	 * Create a FlatRelationship from a full relationship
	 * @param relationship
	 * @throws InvalidSPDXAnalysisException 
	 */
	public FlatRelationship(Relationship relationship) throws InvalidSPDXAnalysisException {
		this.comment = relationship.getComment();
		this.relationshipType = relationship.getRelationshipType();
		this.relatedSpdxElementId = relationship.getRelatedSpdxElement().getId();
	}

	/**
	 * @return the relationshipType
	 */
	public RelationshipType getRelationshipType() {
		return relationshipType;
	}

	/**
	 * @param relationshipType the relationshipType to set
	 */
	public void setRelationshipType(RelationshipType relationshipType) {
		this.relationshipType = relationshipType;
	}

	/**
	 * @return the comment
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * @param comment the comment to set
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}

	/**
	 * @return the relatedSpdxElementId conforming to the data format for relationship document references (section 7.1.4 in the 2.1 version of the SPDX specification)
	 */
	public String getRelatedSpdxElementId() {
		return relatedSpdxElementId;
	}

	/**
	 * @param relatedSpdxElementId the relatedSpdxElementId String conforming to the data format for relationship document references (section 7.1.4 in the 2.1 version of the SPDX specification) to set
	 */
	public void setRelatedSpdxElementId(String relatedSpdxElementId) {
		this.relatedSpdxElementId = relatedSpdxElementId;
	}
	
	
}
