/**
 * Copyright (c) 2013 Source Auditor Inc.
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

import org.spdx.rdfparser.SPDXNonStandardLicense;

/**
 * Contains the results of a comparison between two SPDX non-standard licenses
 * where the license text is equivalent and the license comment, license ID, or
 * other fields are different
 * @author Gary O'Neall
 *
 */
public class SpdxLicenseDifference {

	private String licenseText;
	private String licenseNameA;
	/**
	 * @return the licenseText
	 */
	public String getLicenseText() {
		return licenseText;
	}

	/**
	 * @return the licenseNameA
	 */
	public String getLicenseNameA() {
		return licenseNameA;
	}

	/**
	 * @return the licenseNameB
	 */
	public String getLicenseNameB() {
		return licenseNameB;
	}

	/**
	 * @return the licenseNamesEqual
	 */
	public boolean isLicenseNamesEqual() {
		return licenseNamesEqual;
	}

	/**
	 * @return the idA
	 */
	public String getIdA() {
		return IdA;
	}

	/**
	 * @return the idB
	 */
	public String getIdB() {
		return IdB;
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
	 * @return the commentsEqual
	 */
	public boolean isCommentsEqual() {
		return commentsEqual;
	}

	/**
	 * @return the sourceUrlsA
	 */
	public String[] getSourceUrlsA() {
		return sourceUrlsA;
	}

	/**
	 * @return the sourceUrlsB
	 */
	public String[] getSourceUrlsB() {
		return sourceUrlsB;
	}

	/**
	 * @return the sourceUrlsEqual
	 */
	public boolean isSourceUrlsEqual() {
		return sourceUrlsEqual;
	}

	private String licenseNameB;
	private boolean licenseNamesEqual;
	private String IdA;
	private String IdB;
	private String commentA;
	private String commentB;
	private boolean commentsEqual;
	private String[] sourceUrlsA;
	private String[] sourceUrlsB;
	private boolean sourceUrlsEqual;

	/**
	 * @param licenseA
	 * @param licenseB
	 */
	public SpdxLicenseDifference(
			SPDXNonStandardLicense licenseA,
			SPDXNonStandardLicense licenseB) {
		this.licenseText = licenseA.getText();
		this.licenseNameA = licenseA.getLicenseName();
		this.licenseNameB = licenseB.getLicenseName();
		this.licenseNamesEqual = SpdxComparer.stringsEqual(licenseNameA, licenseNameB);
		this.IdA = licenseA.getId();
		this.IdB = licenseB.getId();
		this.commentA = licenseA.getComment();
		this.commentB = licenseB.getComment();
		this.commentsEqual = SpdxComparer.stringsEqual(commentA, commentB);
		this.sourceUrlsA = licenseA.getSourceUrls();
		this.sourceUrlsB = licenseB.getSourceUrls();
		this.sourceUrlsEqual = SpdxComparer.stringArraysEqual(sourceUrlsA, sourceUrlsB);			
	}
	
}