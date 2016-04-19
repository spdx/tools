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
package org.spdx.html;

import java.util.Map;

import org.spdx.rdfparser.model.Relationship;
import org.spdx.rdfparser.model.SpdxElement;

/**
 * Mustache context for relationships
 * @author Gary O'Neall
 *
 */
public class RelationshipContext {
	
	private String relatedElementId;
	private String comment;
	private String type;
	private String elementLink;
	
	public RelationshipContext() {
		
	}
	
	public RelationshipContext(Relationship relationship, Map<String, String> idToUrlMap) {
		if (relationship == null) {
			return;
		}
		SpdxElement relatedElement = relationship.getRelatedSpdxElement();
		if (relatedElement != null) {
			relatedElementId = relatedElement.getId();
		}
		type = relationship.getRelationshipType().toTag();
		comment = relationship.getComment();
		elementLink = idToUrlMap.get(relatedElementId);
	}

	public String getRelatedElementId() {
		return relatedElementId;
	}

	public void setRelatedElementId(String relatedElementId) {
		this.relatedElementId = relatedElementId;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getElementLink() {
		return elementLink;
	}

	public void setElementLink(String elementLink) {
		this.elementLink = elementLink;
	}
}
