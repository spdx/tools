/**
 * Copyright (c) 2016 Source Auditor Inc.
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
package org.spdx.compare;

import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.model.ExternalRef;
import org.spdx.rdfparser.model.ExternalRef.ReferenceCategory;
import org.spdx.rdfparser.referencetype.ReferenceType;

import com.google.common.base.Objects;

/**
 * Contains information on differences between two different External Refs.
 *
 * @author Gary O'Neall
 *
 */
public class SpdxExternalRefDifference {

	String commentA;
	String commentB;
	ReferenceCategory catA;
	ReferenceCategory catB;
	private String referenceLocator;
	private ReferenceType referenceType;

	SpdxExternalRefDifference(ExternalRef externalRefA, ExternalRef externalRefB) throws InvalidSPDXAnalysisException {
		this.commentA = externalRefA.getComment();
		this.commentB = externalRefB.getComment();
		catA = externalRefA.getReferenceCategory();
		catB = externalRefB.getReferenceCategory();
		this.referenceLocator = externalRefA.getReferenceLocator();
		this.referenceType = externalRefA.getReferenceType();
	}
	public boolean isCommentsEqual() {
		return SpdxComparer.stringsEqual(this.commentA, this.commentB);
	}

	public boolean isReferenceCategoriesEqual() {
		return Objects.equal(catA, catB);
	}
	/**
	 * @return the commentA
	 */
	public String getCommentA() {
		return commentA;
	}
	/**
	 * @return the commentB
	 */
	public String getCommentB() {
		return commentB;
	}
	/**
	 * @return the catA
	 */
	public ReferenceCategory getCatA() {
		return catA;
	}
	/**
	 * @return the catB
	 */
	public ReferenceCategory getCatB() {
		return catB;
	}
	/**
	 * @return the referenceLocator
	 */
	public String getReferenceLocator() {
		return referenceLocator;
	}
	/**
	 * @return the referenceType
	 */
	public ReferenceType getReferenceType() {
		return referenceType;
	}
}
