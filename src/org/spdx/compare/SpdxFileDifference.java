/**
 * Copyright (c) 2013 Source Auditor Inc.
 * Copyright (c) 2013 Black Duck Software Inc.
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

import org.spdx.rdfparser.DOAPProject;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SPDXFile;
import org.spdx.rdfparser.SPDXLicenseInfo;

/**
 * Contains the results of a comparison between two SPDX files with the same name
 * @author Gary O'Neall
 *
 */
public class SpdxFileDifference {
	/**
	 * @return the fileName
	 */
	public String getFileName() {
		return fileName;
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
	 * @return the artifactsOfA
	 */
	public DOAPProject[] getArtifactsOfA() {
		return artifactsOfA;
	}

	/**
	 * @return the artifactsOfB
	 */
	public DOAPProject[] getArtifactsOfB() {
		return artifactsOfB;
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
	public boolean isSeenLicensesEqual() {
		return seenLicensesEqual;
	}

	/**
	 * @return the uniqueSeenLicensesA
	 */
	public SPDXLicenseInfo[] getUniqueSeenLicensesA() {
		return uniqueSeenLicensesA;
	}

	/**
	 * @return the uniqueSeenLicensesB
	 */
	public SPDXLicenseInfo[] getUniqueSeenLicensesB() {
		return uniqueSeenLicensesB;
	}

	/**
	 * @return the artifactOfsEquals
	 */
	public boolean isArtifactOfsEquals() {
		return artifactOfsEquals;
	}

	/**
	 * @return the uniqueArtifactOfA
	 */
	public DOAPProject[] getUniqueArtifactOfA() {
		return uniqueArtifactOfA;
	}

	/**
	 * @return the uniqueArtifactOfB
	 */
	public DOAPProject[] getUniqueArtifactOfB() {
		return uniqueArtifactOfB;
	}

	/**
	 * @return the sha1A
	 */
	public String getSha1A() {
		return sha1A;
	}

	/**
	 * @return the sha1B
	 */
	public String getSha1B() {
		return sha1B;
	}

	/**
	 * @return the fileTypeA
	 */
	public String getFileTypeA() {
		return fileTypeA;
	}

	/**
	 * @return the fileTypeB
	 */
	public String getFileTypeB() {
		return fileTypeB;
	}

	private String fileName;
	private String commentA;
	private String commentB;
	private String concludedLicenseA;
	private String concludedLicenseB;
	private boolean concludedLicenseEquals;
	private DOAPProject[] artifactsOfA;
	private DOAPProject[] artifactsOfB;
	private String copyrightA;
	private String copyrightB;
	private String licenseCommentsA;
	private String licenseCommentsB;
	private boolean seenLicensesEqual;
	private SPDXLicenseInfo[] uniqueSeenLicensesA;
	private SPDXLicenseInfo[] uniqueSeenLicensesB;
	private boolean artifactOfsEquals;
	private DOAPProject[] uniqueArtifactOfA;
	private DOAPProject[] uniqueArtifactOfB;
	private String sha1A;
	private String sha1B;
	private String fileTypeA;
	private String fileTypeB;
	private String[] contributorsA;
	private String noticeA;
	private String[] contributorsB;
	private String noticeB;
	private String[] dependantFileNamesA;
	private String[] dependantFileNamesB;

	public SpdxFileDifference(SPDXFile fileA, SPDXFile fileB, 
			boolean concludedLicensesEqual, boolean seenLicensesEqual,
			SPDXLicenseInfo[] uniqueSeenLicensesA,
			SPDXLicenseInfo[] uniqueSeenLicensesB,
			boolean artifactOfsEquals,
			DOAPProject[] uniqueArtifactOfA,
			DOAPProject[] uniqueArtifactOfB) throws InvalidSPDXAnalysisException {
		this.fileName = fileA.getName();
		this.artifactsOfA = fileA.getArtifactOf();
		if (this.artifactsOfA == null) {
			this.artifactsOfA = new DOAPProject[0];
		}
		this.artifactsOfB = fileB.getArtifactOf();
		if (this.artifactsOfB == null) {
			this.artifactsOfB = new DOAPProject[0];
		}
		this.commentA = fileA.getComment();
		if (this.commentA == null) {
			this.commentA = "";
		}
		this.commentB = fileB.getComment();
		if (this.commentB == null) {
			this.commentB = "";
		}
		this.concludedLicenseA = fileA.getConcludedLicenses().toString();
		this.concludedLicenseB = fileB.getConcludedLicenses().toString();
		this.concludedLicenseEquals = concludedLicensesEqual;
		this.copyrightA = fileA.getCopyright();
		if (this.copyrightA == null) {
			this.copyrightA = "";
		}
		this.copyrightB = fileB.getCopyright();
		if (this.copyrightB == null) {
			this.copyrightB = "";
		}
		this.licenseCommentsA = fileA.getLicenseComments();
		if (this.licenseCommentsA == null) {
			this.licenseCommentsA = "";
		}
		this.licenseCommentsB = fileB.getLicenseComments();
		if (this.licenseCommentsB == null) {
			this.licenseCommentsB = "";
		}
		this.seenLicensesEqual = seenLicensesEqual;
		this.uniqueSeenLicensesA = uniqueSeenLicensesA;
		this.uniqueSeenLicensesB = uniqueSeenLicensesB;
		this.artifactOfsEquals = artifactOfsEquals;
		this.uniqueArtifactOfA = uniqueArtifactOfA;
		this.uniqueArtifactOfB = uniqueArtifactOfB;
		this.sha1A = fileA.getSha1();
		this.sha1B = fileB.getSha1();
		this.fileTypeA = fileA.getType();
		this.fileTypeB = fileB.getType();	
		this.contributorsA = fileA.getContributors();
		this.contributorsB = fileB.getContributors();
		this.noticeA = fileA.getNoticeText();
		this.noticeB = fileB.getNoticeText();
		this.dependantFileNamesA = SpdxFileComparer.filesToFileNames(fileA.getFileDependencies());
		this.dependantFileNamesB = SpdxFileComparer.filesToFileNames(fileB.getFileDependencies());
	}
	
	public boolean isContributorsEqual() {
		return SpdxComparer.stringArraysEqual(this.contributorsA, this.contributorsB);
	}
	
	public boolean isNoticeTextsEqual() {
		return SpdxComparer.stringsEqual(this.noticeA, this.noticeB);
	}
	
	public boolean isFileDependenciesEqual() {
		return SpdxComparer.stringArraysEqual(this.dependantFileNamesA, this.dependantFileNamesB);
	}
		
	public boolean isCommentsEqual() {
		return SpdxComparer.stringsEqual(commentA, commentB);
	}
	
	public boolean isCopyrightsEqual() {
		return SpdxComparer.stringsEqual(copyrightA, copyrightB);
	}
	
	public boolean isLicenseCommentsEqual() {
		return SpdxComparer.stringsEqual(licenseCommentsA, licenseCommentsB);
	}

	/**
	 * @return
	 */
	public boolean isTypeEqual() {
		return SpdxComparer.stringsEqual(fileTypeA, fileTypeB);
	}

	/**
	 * @return
	 */
	public boolean isChecksumsEqual() {
		return SpdxComparer.stringsEqual(sha1A, sha1B);
	}

	/**
	 * @return
	 */
	public String getContributorsAAsString() {
		return stringArrayToString(this.contributorsA);
	}
	
	/**
	 * @return
	 */
	public String getContributorsBAsString() {
		return stringArrayToString(this.contributorsB);
	}
	
	
	
	static String stringArrayToString(String[] s) {
		StringBuilder sb = new StringBuilder();
		if (s != null && s.length > 0) {
			sb.append(s[0]);
		}
		for (int i = 1; i < s.length; i++) {
			sb.append(", ");
			sb.append(s[i]);
		}
		return sb.toString();
	}

	/**
	 * @return
	 */
	public String getFileDependenciesAAsString() {
		return stringArrayToString(this.dependantFileNamesA);
	}
	
	/**
	 * @return
	 */
	public String getFileDependenciesBAsString() {
		return stringArrayToString(this.dependantFileNamesB);
	}
}
