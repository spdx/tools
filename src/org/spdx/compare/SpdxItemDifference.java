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
package org.spdx.compare;

import org.spdx.rdfparser.license.AnyLicenseInfo;
import org.spdx.rdfparser.model.Annotation;
import org.spdx.rdfparser.model.Relationship;
import org.spdx.rdfparser.model.SpdxItem;

/**
 *  Contains the results of a comparison between two SPDX items with the same name
 * @author Gary O'Neall
 *
 */
public class SpdxItemDifference {

	private String name;
	private String commentA;
	private String commentB;
	private String concludedLicenseA;
	private String concludedLicenseB;
	private boolean concludedLicenseEquals;
	private String copyrightA;
	private String copyrightB;
	private String licenseCommentsA;
	private String licenseCommentsB;
	private boolean seenLicensesEqual;
	private AnyLicenseInfo[] uniqueSeenLicensesA;
	private AnyLicenseInfo[] uniqueSeenLicensesB;
	private boolean relationshipsEquals;
	private Relationship[] uniqueRelationshipA;
	private Relationship[] uniqueRelationshipB;
	private boolean annotationsEquals;
	private Annotation[] uniqueAnnotationsA;
	private Annotation[] uniqueAnnotationsB;

	public SpdxItemDifference(SpdxItem itemA, SpdxItem itemB,
			boolean concludedLicensesEqual, boolean seenLicensesEqual,
			AnyLicenseInfo[] uniqueSeenLicensesA,
			AnyLicenseInfo[] uniqueSeenLicensesB,
			boolean relationshipsEquals,
			Relationship[] uniqueRelationshipA,
			Relationship[] uniqueRelationshipB,
			boolean annotationsEquals,
			Annotation[] uniqueAnnotationsA,
			Annotation[] uniqueAnnotationsB
			) throws SpdxCompareException {
		this.name = itemA.getName();
		this.commentA = itemA.getComment();
		if (this.commentA == null) {
			this.commentA = "";
		}
		this.commentB = itemB.getComment();
		if (this.commentB == null) {
			this.commentB = "";
		}
		this.concludedLicenseA = itemA.getLicenseConcluded().toString();
		this.concludedLicenseB = itemB.getLicenseConcluded().toString();
		this.concludedLicenseEquals = concludedLicensesEqual;
		this.copyrightA = itemA.getCopyrightText();
		if (this.copyrightA == null) {
			this.copyrightA = "";
		}
		this.copyrightB = itemB.getCopyrightText();
		if (this.copyrightB == null) {
			this.copyrightB = "";
		}
		this.licenseCommentsA = itemA.getLicenseComments();
		if (this.licenseCommentsA == null) {
			this.licenseCommentsA = "";
		}
		this.licenseCommentsB = itemB.getLicenseComments();
		if (this.licenseCommentsB == null) {
			this.licenseCommentsB = "";
		}
		this.seenLicensesEqual = seenLicensesEqual;
		this.uniqueSeenLicensesA = uniqueSeenLicensesA;
		this.uniqueSeenLicensesB = uniqueSeenLicensesB;
		this.relationshipsEquals = relationshipsEquals;
		this.uniqueRelationshipA = uniqueRelationshipA;
		this.uniqueRelationshipB = uniqueRelationshipB;
		this.annotationsEquals = annotationsEquals;
		this.uniqueAnnotationsA = uniqueAnnotationsA;
		this.uniqueAnnotationsB = uniqueAnnotationsB;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
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
	 * @return the concludedLicenseA
	 */
	public String getConcludedLicenseA() {
		return concludedLicenseA;
	}

	/**
	 * @return the concludedLicenseB
	 */
	public String getConcludedLicenseB() {
		return concludedLicenseB;
	}

	/**
	 * @return the concludedLicenseEquals
	 */
	public boolean isConcludedLicenseEquals() {
		return concludedLicenseEquals;
	}
	/**
	 * @return the copyrightA
	 */
	public String getCopyrightA() {
		return copyrightA;
	}

	/**
	 * @return the copyrightB
	 */
	public String getCopyrightB() {
		return copyrightB;
	}

	/**
	 * @return the licenseCommentsA
	 */
	public String getLicenseCommentsA() {
		return licenseCommentsA;
	}

	/**
	 * @return the licenseCommentsB
	 */
	public String getLicenseCommentsB() {
		return licenseCommentsB;
	}

	/**
	 * @return the seenLicensesEqual
	 */
	public boolean isSeenLicensesEquals() {
		return seenLicensesEqual;
	}

	/**
	 * @return the uniqueSeenLicensesA
	 */
	public AnyLicenseInfo[] getUniqueSeenLicensesA() {
		return uniqueSeenLicensesA;
	}

	/**
	 * @return the uniqueSeenLicensesB
	 */
	public AnyLicenseInfo[] getUniqueSeenLicensesB() {
		return uniqueSeenLicensesB;
	}

	public boolean isCommentsEquals() {
		return SpdxComparer.stringsEqual(commentA, commentB);
	}

	public boolean isCopyrightsEqual() {
		return SpdxComparer.stringsEqual(copyrightA, copyrightB);
	}

	public boolean isLicenseCommentsEqual() {
		return SpdxComparer.stringsEqual(licenseCommentsA, licenseCommentsB);
	}

	/**
	 * @return the relationshipsEquals
	 */
	public boolean isRelationshipsEquals() {
		return relationshipsEquals;
	}

	/**
	 * @return the uniqueRelationshipA
	 */
	public Relationship[] getUniqueRelationshipA() {
		return uniqueRelationshipA;
	}

	/**
	 * @return the uniqueRelationshipB
	 */
	public Relationship[] getUniqueRelationshipB() {
		return uniqueRelationshipB;
	}

	/**
	 * @return the annotationsEquals
	 */
	public boolean isAnnotationsEquals() {
		return annotationsEquals;
	}

	/**
	 * @return the uniqueAnnotationsA
	 */
	public Annotation[] getUniqueAnnotationsA() {
		return uniqueAnnotationsA;
	}

	/**
	 * @return the uniqueAnnotationsB
	 */
	public Annotation[] getUniqueAnnotationsB() {
		return uniqueAnnotationsB;
	}

}
