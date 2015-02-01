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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.model.Checksum;
import org.spdx.rdfparser.model.SpdxFile;
import org.spdx.rdfparser.model.DoapProject;


/**
 * Compares two SPDX files.  The <code>compare(fileA, fileB)</code> method will perform the comparison and
 * store the results.  <code>isDifferenceFound()</code> will return true of any 
 * differences were found.
 * @author Gary O'Neall
 *
 */
public class SpdxFileComparer extends SpdxItemComparer {
	private boolean inProgress = false;
	private boolean differenceFound = false;
	private SpdxFile fileA = null;
	private SpdxFile fileB = null;
	private boolean artifactOfEquals;
	private boolean fileDependenciesEquals;
	private boolean contributorsEquals;
	private boolean noticeTextEquals;
	/**
	 * @return the fileDependenciesEquals
	 */
	public boolean isFileDependenciesEquals() {
		return fileDependenciesEquals;
	}

	/**
	 * @return the contributorsEquals
	 */
	public boolean isContributorsEquals() {
		return contributorsEquals;
	}

	/**
	 * @return the noticeTextEquals
	 */
	public boolean isNoticeTextEquals() {
		return noticeTextEquals;
	}
	/**
	 * artifactOf projects found in fileA but not fileB
	 */
	private DoapProject[] uniqueArtifactOfA;
	/**
	 * ArtifactoOf projects found in fileB but not fileA
	 */
	private DoapProject[] uniqueArtifactOfB;
	
	private Checksum[] uniqueChecksumsA;
	private Checksum[] uniqueChecksumsB;
	
	/**
	 * Compares two DOAP projects based on the name, then by the home page,
	 * then by the URI
	 * @author Gary O'Neall
	 *
	 */
	class DoapComparator implements Comparator<DoapProject> {

		/* (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(DoapProject arg0, DoapProject arg1) {
			int retval = SpdxComparer.compareStrings(arg0.getName(), arg1.getName());
			if (retval == 0) {
				retval = SpdxComparer.compareStrings(arg0.getHomePage(), arg1.getHomePage());
			}
			if (retval == 0) {
				retval = SpdxComparer.compareStrings(arg0.getProjectUri(), arg1.getProjectUri());
			}
			return retval;
		}	
	}
	
	private Comparator<DoapProject> doapComparer = new DoapComparator();
	private boolean checksumsEquals;
	private boolean typesEquals;

	
	public SpdxFileComparer() {
		
	}

	/**
	 * Compare two SPDX files and store the results
	 * @param filesA
	 * @param filesB
	 * @param licenseXlationMap A mapping between the license IDs from licenses in fileA to fileB
	 * @throws SpdxCompareException 
	 */
	@SuppressWarnings("deprecation")
	public void compare(SpdxFile filesA, SpdxFile filesB, 
			HashMap<String, String> licenseXlationMap) throws SpdxCompareException {
		super.compare(filesA, filesB, licenseXlationMap);
		inProgress = true;
		differenceFound = super.isDifferenceFound();
		this.fileA = filesA;
		this.fileB = filesB;		
		// Artifact Of
		compareArtifactOf(filesA.getArtifactOf(), filesB.getArtifactOf());

		// Checksums
		if (SpdxComparer.elementsEquivalent(filesA.getChecksums(), filesB.getChecksums())) {
			this.checksumsEquals = true;
		} else {
			this.checksumsEquals = false;
			this.differenceFound = true;
			this.uniqueChecksumsA = SpdxComparer.findUniqueChecksums(filesA.getChecksums(), 
					filesB.getChecksums());
			this.uniqueChecksumsB = SpdxComparer.findUniqueChecksums(filesB.getChecksums(), 
					filesA.getChecksums());
		}
		// Type
		if (SpdxComparer.arraysEqual(filesA.getFileTypes(), filesB.getFileTypes())) {
			this.typesEquals = true;
		} else {
			this.typesEquals = false;
			this.differenceFound = true;
		}
		// contributors
		if (SpdxComparer.stringArraysEqual(filesA.getFileContributors(), filesB.getFileContributors())) {
			this.contributorsEquals = true;
		} else {
			this.contributorsEquals = false;
			this.differenceFound = true;
		}
		// notice text
		if (SpdxComparer.stringsEqual(filesA.getNoticeText(), filesB.getNoticeText())) {
			this.noticeTextEquals = true;
		} else {
			this.noticeTextEquals = false;
			this.differenceFound = true;
		}
		// file dependencies
		if (fileNamesEquals(filesA.getFileDependencies(), filesB.getFileDependencies())) {
			this.fileDependenciesEquals = true;
		} else {
			this.fileDependenciesEquals = false;
			this.differenceFound = true;
		}
		inProgress = false;
	}
	
	/**
	 * Compare the file names from two arrays of SPDX files for equality ignoring order
	 * @param filesA
	 * @param filesB
	 * @return
	 */
	private boolean fileNamesEquals(SpdxFile[] filesA,
			SpdxFile[] filesB) {
		String[] fileNamesA = filesToFileNames(filesA);
		String[] fileNamesB = filesToFileNames(filesB);
		return SpdxComparer.stringArraysEqual(fileNamesA, fileNamesB);
	}

	/**
	 * Extracts out the file names into a string array
	 * @param files
	 * @return
	 */
	static public String[] filesToFileNames(SpdxFile[] files) {
		if (files == null) {
			return null;
		}
		String[] retval = new String[files.length];
		for (int i = 0; i < files.length; i++) {
			retval[i] = files[i].getName();
		}
		return retval;
	}

	/**
	 * @return the fileA
	 */
	public SpdxFile getFileA() {
		return fileA;
	}

	/**
	 * @return the fileB
	 */
	public SpdxFile getFileB() {
		return fileB;
	}

	/**
	 * @return the artifactOfEquals
	 */
	public boolean isArtifactOfEquals() {
		return artifactOfEquals;
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
	 * @return the checksumsEquals
	 */
	public boolean isChecksumsEquals() {
		return checksumsEquals;
	}

	/**
	 * @return the typesEquals
	 */
	public boolean isTypesEquals() {
		return typesEquals;
	}

	/**
	 * Compares two artifact of arrays storing the results in 
	 * uniqueArtifactOfA and uniqueArtifactOfB and sets the differencefound
	 * Note that an artifactOf is considered unique if ANY of the properties
	 * are different
	 * to true if any differences are found
	 * @param artifactOfA
	 * @param artifactOfB
	 */
	private void compareArtifactOf(DoapProject[] artifactOfA,
			DoapProject[] artifactOfB) {
		Arrays.sort(artifactOfA, doapComparer);
		Arrays.sort(artifactOfB, doapComparer);
		int aIndex = 0;
		int bIndex = 0;
		ArrayList<DoapProject> alUniqueA = new ArrayList<DoapProject>();
		ArrayList<DoapProject> alUniqueB = new ArrayList<DoapProject>();
		
		while (aIndex < artifactOfA.length || bIndex < artifactOfB.length) {
			if (aIndex >= artifactOfA.length) {
				alUniqueB.add(artifactOfB[bIndex]);
				bIndex++;
			} else if (bIndex >= artifactOfB.length) {
				alUniqueA.add(artifactOfA[aIndex]);
				aIndex++;
			} else {
				int compare = this.doapComparer.compare(artifactOfA[aIndex], artifactOfB[bIndex]);
				if (compare == 0) {
					// both names are equal - check other fields
					aIndex++;
					bIndex++;	
				} else if (compare > 0) {
					// artifactOfA is greater than artifactOfB
					alUniqueB.add(artifactOfB[bIndex]);
					bIndex++;
				} else {
					// artifactOfB is greater than artifactOfA
					alUniqueA.add(artifactOfA[aIndex]);
					aIndex++;
				}
			}
		}
		this.uniqueArtifactOfA = alUniqueA.toArray(new DoapProject[alUniqueA.size()]);
		this.uniqueArtifactOfB = alUniqueB.toArray(new DoapProject[alUniqueB.size()]);
		if (this.uniqueArtifactOfA.length > 0 || this.uniqueArtifactOfB.length > 0) {
			this.differenceFound = true;
			this.artifactOfEquals = false;
		}	else {
			this.artifactOfEquals = true;
		}
	}

	/**
	 * checks to make sure there is not a compare in progress
	 * @throws SpdxCompareException 
	 * 
	 */
	private void checkInProgress() throws SpdxCompareException {
		if (inProgress) {
			throw(new SpdxCompareException("File compare in progress - can not obtain compare results until compare has completed"));
		}
	}
	
	private void checkCompareMade() throws SpdxCompareException {
		if (this.fileA == null || this.fileB == null) {
			throw(new SpdxCompareException("Trying to obgain results of a file compare before a file compare has been performed"));
		}	
	}


	/**
	 * @return
	 * @throws SpdxCompareException 
	 */
	public boolean isDifferenceFound() throws SpdxCompareException {
		checkInProgress();
		checkCompareMade();
		return this.differenceFound;
	}

	/**
	 * Return a file difference - the two file names must equal
	 * @return
	 * @throws SpdxCompareException 
	 */
	public SpdxFileDifference getFileDifference() throws SpdxCompareException {
		checkInProgress();
		checkCompareMade();
		if (!fileA.getName().equals(fileB.getName())) {
			throw(new SpdxCompareException("Can not create an SPDX file difference for two files with different names"));
		}
		try {
			return new SpdxFileDifference(fileA, fileB, this.isConcludedLicenseEquals(), 
					this.isSeenLicenseEquals(), this.getUniqueSeenLicensesA(), this.getUniqueSeenLicensesB(), 
					artifactOfEquals, uniqueArtifactOfA, uniqueArtifactOfB, 
					checksumsEquals, uniqueChecksumsA, uniqueChecksumsB, 
					this.isRelationshipsEquals(), this.getUniqueRelationshipA(), this.getUniqueRelationshipB(),
					this.isAnnotationsEquals(), this.getUniqueAnnotationsA(), this.getUniqueAnnotationsB());
		} catch (InvalidSPDXAnalysisException e) {
			throw (new SpdxCompareException("Error reading SPDX file propoerties: "+e.getMessage(),e));
		}
	}	
}
