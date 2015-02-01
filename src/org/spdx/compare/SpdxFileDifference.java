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

import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.license.AnyLicenseInfo;
import org.spdx.rdfparser.model.Annotation;
import org.spdx.rdfparser.model.Checksum;
import org.spdx.rdfparser.model.DoapProject;
import org.spdx.rdfparser.model.Relationship;
import org.spdx.rdfparser.model.SpdxFile;
import org.spdx.rdfparser.model.SpdxFile.FileType;

/**
 * Contains the results of a comparison between two SPDX files with the same name
 * @author Gary O'Neall
 *
 */
public class SpdxFileDifference extends SpdxItemDifference {

	private DoapProject[] artifactsOfA;
	private DoapProject[] artifactsOfB;
	private boolean artifactOfsEquals;
	private DoapProject[] uniqueArtifactOfA;
	private DoapProject[] uniqueArtifactOfB;
	private FileType[] fileTypeA;
	private FileType[] fileTypeB;
	private String[] contributorsA;
	private String noticeA;
	private String[] contributorsB;
	private String noticeB;
	private String[] dependantFileNamesA;
	private String[] dependantFileNamesB;
	private boolean checksumsEquals;
	private Checksum[] uniqueChecksumsA;
	private Checksum[] uniqueChecksumsB;
	private String spdxIdA;
	private String spdxIdB;

	@SuppressWarnings("deprecation")
	public SpdxFileDifference(SpdxFile fileA, SpdxFile fileB, 
			boolean concludedLicensesEqual, boolean seenLicensesEqual,
			AnyLicenseInfo[] uniqueSeenLicensesA,
			AnyLicenseInfo[] uniqueSeenLicensesB,
			boolean artifactOfsEquals,
			DoapProject[] uniqueArtifactOfA2,
			DoapProject[] uniqueArtifactOfB2,
			boolean checksumsEquals,
			Checksum[] uniqueChecksumsA,
			Checksum[] uniqueChecksumsB,
			boolean relationshipsEquals,
			Relationship[] uniqueRelationshipA,
			Relationship[] uniqueRelationshipB,
			boolean annotationsEquals,
			Annotation[] uniqueAnnotationsA,
			Annotation[] uniqueAnnotationsB
			) throws InvalidSPDXAnalysisException, SpdxCompareException {
		super(fileA, fileB, concludedLicensesEqual, seenLicensesEqual,
				uniqueSeenLicensesA, uniqueSeenLicensesB, 
				relationshipsEquals, uniqueRelationshipA,  uniqueRelationshipB,
				annotationsEquals, uniqueAnnotationsA,uniqueAnnotationsB);
		this.artifactsOfA = fileA.getArtifactOf();
		if (this.artifactsOfA == null) {
			this.artifactsOfA = new DoapProject[0];
		}
		this.artifactsOfB = fileB.getArtifactOf();
		if (this.artifactsOfB == null) {
			this.artifactsOfB = new DoapProject[0];
		}
		this.artifactOfsEquals = artifactOfsEquals;
		this.uniqueArtifactOfA = uniqueArtifactOfA2;
		this.uniqueArtifactOfB = uniqueArtifactOfB2;
		this.fileTypeA = fileA.getFileTypes();
		this.fileTypeB = fileB.getFileTypes();	
		this.contributorsA = fileA.getFileContributors();
		this.contributorsB = fileB.getFileContributors();
		this.noticeA = fileA.getNoticeText();
		this.noticeB = fileB.getNoticeText();
		this.dependantFileNamesA = SpdxFileComparer.filesToFileNames(fileA.getFileDependencies());
		this.dependantFileNamesB = SpdxFileComparer.filesToFileNames(fileB.getFileDependencies());
		this.checksumsEquals = checksumsEquals;
		this.uniqueChecksumsA = uniqueChecksumsA;
		this.uniqueChecksumsB = uniqueChecksumsB;
		this.spdxIdA = fileA.getId();
		this.spdxIdB = fileB.getId();
	}
	
	/**
	 * @return the fileName
	 */
	public String getFileName() {
		return this.getName();
	}



	/**
	 * @return the artifactsOfA
	 */
	public DoapProject[] getArtifactsOfA() {
		return artifactsOfA;
	}

	/**
	 * @return the artifactsOfB
	 */
	public DoapProject[] getArtifactsOfB() {
		return artifactsOfB;
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
	public DoapProject[] getUniqueArtifactOfA() {
		return uniqueArtifactOfA;
	}

	/**
	 * @return the uniqueArtifactOfB
	 */
	public DoapProject[] getUniqueArtifactOfB() {
		return uniqueArtifactOfB;
	}
	
	/**
	 * @return the fileTypeA
	 */
	public FileType[] getFileTypeA() {
		return fileTypeA;
	}

	/**
	 * @return the fileTypeB
	 */
	public FileType[] getFileTypeB() {
		return fileTypeB;
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

	/**
	 * @return
	 */
	public boolean isTypeEqual() {
		return SpdxComparer.arraysEqual(fileTypeA, fileTypeB);
	}

	/**
	 * @return
	 */
	public boolean isChecksumsEquals() {
		return this.checksumsEquals;
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



	/**
	 * @return the contributorsA
	 */
	public String[] getContributorsA() {
		return contributorsA;
	}



	/**
	 * @return the noticeA
	 */
	public String getNoticeA() {
		return noticeA;
	}



	/**
	 * @return the contributorsB
	 */
	public String[] getContributorsB() {
		return contributorsB;
	}



	/**
	 * @return the noticeB
	 */
	public String getNoticeB() {
		return noticeB;
	}



	/**
	 * @return the dependantFileNamesA
	 */
	public String[] getDependantFileNamesA() {
		return dependantFileNamesA;
	}



	/**
	 * @return the dependantFileNamesB
	 */
	public String[] getDependantFileNamesB() {
		return dependantFileNamesB;
	}

	/**
	 * @return the uniqueChecksumsA
	 */
	public Checksum[] getUniqueChecksumsA() {
		return uniqueChecksumsA;
	}



	/**
	 * @return the uniqueChecksumsB
	 */
	public Checksum[] getUniqueChecksumsB() {
		return uniqueChecksumsB;
	}

	/**
	 * @return
	 */
	public String getSpdxIdA() {
		return this.spdxIdA;
	}
	
	/**
	 * @return
	 */
	public String getSpdxIdB() {
		return this.spdxIdB;
	}
	
}
