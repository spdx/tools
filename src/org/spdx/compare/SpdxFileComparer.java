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

import org.spdx.rdfparser.DOAPProject;
import org.spdx.rdfparser.SPDXFile;
import org.spdx.rdfparser.SPDXLicenseInfo;


/**
 * Compares two SPDX files.  The <code>compare(fileA, fileB)</code> method will perform the comparison and
 * store the results.  <code>isDifferenceFound()</code> will return true of any 
 * differences were found.
 * @author Gary O'Neall
 *
 */
public class SpdxFileComparer {
	
	private boolean inProgress = false;
	private SPDXFile fileA = null;
	private SPDXFile fileB = null;
	private boolean differenceFound = false;
	private boolean concludedLicenseEquals;
	private boolean seenLicenseEquals;
	private boolean artifactOfEquals;
	/**
	 * Seen licenses found in fileB but not in fileA
	 */
	private SPDXLicenseInfo[] uniqueSeenLicensesB;
	/**
	 * Seen licenses found in fileA but not in fileB
	 */
	private SPDXLicenseInfo[] uniqueSeenLicensesA;
	/**
	 * artifactOf projects found in fileA but not fileB
	 */
	private DOAPProject[] uniqueArtifactOfA;
	/**
	 * ArtifactoOf projects found in fileB but not fileA
	 */
	private DOAPProject[] uniqueArtifactOfB;
	
	/**
	 * Compares two DOAP projects based on the name, then by the home page,
	 * then by the URI
	 * @author Gary O'Neall
	 *
	 */
	class DoapComparator implements Comparator<DOAPProject> {

		/* (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(DOAPProject arg0, DOAPProject arg1) {
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
	
	private Comparator<DOAPProject> doapComparer = new DoapComparator();
	private boolean commentsEquals;
	private boolean copyrightsEquals;
	private boolean licenseCommmentsEquals;
	private boolean namesEquals;
	private boolean checksumsEquals;
	private boolean typesEquals;

	
	public SpdxFileComparer() {
		
	}

	/**
	 * Compare two SPDX files and store the results
	 * @param fileA
	 * @param fileB
	 * @param licenseXlationMap A mapping between the license IDs from licenses in fileA to fileB
	 * @throws SpdxCompareException 
	 */
	public void compare(SPDXFile fileA, SPDXFile fileB, 
			HashMap<String, String> licenseXlationMap) throws SpdxCompareException {
		inProgress = true;
		differenceFound = false;
		this.fileA = fileA;
		this.fileB = fileB;
		// Artifact Of
		compareArtifactOf(fileA.getArtifactOf(), fileB.getArtifactOf());
		// Comments
		if (SpdxComparer.stringsEqual(fileA.getComment(), fileB.getComment())) {
			this.commentsEquals = true;
		} else {
			this.commentsEquals = false;
			this.differenceFound = true;
		}
		// Concluded License
		if (LicenseCompareHelper.isLicenseEqual(fileA.getConcludedLicenses(), 
				fileB.getConcludedLicenses(), licenseXlationMap)) {
			this.concludedLicenseEquals = true;
		} else {
			this.concludedLicenseEquals = false;
			this.differenceFound = true;
		}
		// Copyrights
		if (SpdxComparer.stringsEqual(fileA.getCopyright(), fileB.getCopyright())) {
			this.copyrightsEquals = true;
		} else {
			this.copyrightsEquals = false;
			this.differenceFound = true;
		}
		//
		if (SpdxComparer.stringsEqual(fileA.getLicenseComments(),
				fileB.getLicenseComments())) {
			this.licenseCommmentsEquals = true;
		} else {
			this.licenseCommmentsEquals = false;
			this.differenceFound = true;
		}
		// Name
		if (SpdxComparer.stringsEqual(fileA.getName(), fileB.getName())) {
			this.namesEquals = true;
		} else {
			this.namesEquals = false;
			this.differenceFound = true;
		}
		// Seen licenses
		compareSeenLicenses(fileA.getSeenLicenses(), fileB.getSeenLicenses(),
				licenseXlationMap);
		// Sha1
		if (SpdxComparer.stringsEqual(fileA.getSha1(), fileB.getSha1())) {
			this.checksumsEquals = true;
		} else {
			this.checksumsEquals = false;
			this.differenceFound = true;
		}
		// Type
		if (SpdxComparer.stringsEqual(fileA.getType(), fileB.getType())) {
			this.typesEquals = true;
		} else {
			this.typesEquals = false;
			this.differenceFound = true;
		}
		
		inProgress = false;
	}
	
	/**
	 * Compares seen licenses and initializes the uniqueSeenLicenses arrays
	 * as well as the seenLicenseEquals flag and sets the differenceFound to
	 * true if a difference was found
	 * @param licensesA
	 * @param licensesB
	 * @throws SpdxCompareException 
	 */
	private void compareSeenLicenses(SPDXLicenseInfo[] licensesA,
			SPDXLicenseInfo[] licensesB, HashMap<String, String> licenseXlationMap) throws SpdxCompareException {
		ArrayList<SPDXLicenseInfo> alUniqueA = new ArrayList<SPDXLicenseInfo>();
		ArrayList<SPDXLicenseInfo> alUniqueB = new ArrayList<SPDXLicenseInfo>();		
		// a bit brute force, but sorting licenses is a bit complex
		// an N x M comparison of the licenses to determine which ones are unique
		for (int i = 0; i < licensesA.length; i++) {
			boolean found = false;
			for (int j = 0; j < licensesB.length; j++) {
				if (LicenseCompareHelper.isLicenseEqual(
						licensesA[i], licensesB[j], licenseXlationMap)) {
					found = true;
					break;
				}
			}
			if (!found) {
				alUniqueA.add(licensesA[i]);
			}
		}
		
		for (int i = 0; i < licensesB.length; i++) {
			boolean found= false;
			for (int j = 0; j < licensesA.length; j++) {
				if (LicenseCompareHelper.isLicenseEqual(
						// note that the order must be A, B to match the tranlation map
						licensesA[j], licensesB[i], licenseXlationMap)) {
					found = true;
					break;
				}
			}
			if (!found) {
				alUniqueB.add(licensesB[i]);
			}
		}
		this.uniqueSeenLicensesA = alUniqueA.toArray(new SPDXLicenseInfo[alUniqueA.size()]);
		this.uniqueSeenLicensesB = alUniqueB.toArray(new SPDXLicenseInfo[alUniqueB.size()]);
		if (this.uniqueSeenLicensesA.length == 0 && this.uniqueSeenLicensesB.length == 0) {
			this.seenLicenseEquals = true;
		} else {
			this.seenLicenseEquals = false;
			this.differenceFound = true;
		}
	}

	/**
	 * @return the fileA
	 */
	public SPDXFile getFileA() {
		return fileA;
	}

	/**
	 * @return the fileB
	 */
	public SPDXFile getFileB() {
		return fileB;
	}

	/**
	 * @return the concludedLicenseEquals
	 */
	public boolean isConcludedLicenseEquals() {
		return concludedLicenseEquals;
	}

	/**
	 * @return the seenLicenseEquals
	 */
	public boolean isSeenLicenseEquals() {
		return seenLicenseEquals;
	}

	/**
	 * @return the artifactOfEquals
	 */
	public boolean isArtifactOfEquals() {
		return artifactOfEquals;
	}

	/**
	 * @return the uniqueSeenLicensesB
	 */
	public SPDXLicenseInfo[] getUniqueSeenLicensesB() {
		return uniqueSeenLicensesB;
	}

	/**
	 * @return the uniqueSeenLicensesA
	 */
	public SPDXLicenseInfo[] getUniqueSeenLicensesA() {
		return uniqueSeenLicensesA;
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
	 * @return the commentsEquals
	 */
	public boolean isCommentsEquals() {
		return commentsEquals;
	}

	/**
	 * @return the copyrightsEquals
	 */
	public boolean isCopyrightsEquals() {
		return copyrightsEquals;
	}

	/**
	 * @return the licenseCommmentsEquals
	 */
	public boolean isLicenseCommmentsEquals() {
		return licenseCommmentsEquals;
	}

	/**
	 * @return the namesEquals
	 */
	public boolean isNamesEquals() {
		return namesEquals;
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
	private void compareArtifactOf(DOAPProject[] artifactOfA,
			DOAPProject[] artifactOfB) {
		Arrays.sort(artifactOfA, doapComparer);
		Arrays.sort(artifactOfB, doapComparer);
		int aIndex = 0;
		int bIndex = 0;
		ArrayList<DOAPProject> alUniqueA = new ArrayList<DOAPProject>();
		ArrayList<DOAPProject> alUniqueB = new ArrayList<DOAPProject>();
		
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
		this.uniqueArtifactOfA = alUniqueA.toArray(new DOAPProject[alUniqueA.size()]);
		this.uniqueArtifactOfB = alUniqueB.toArray(new DOAPProject[alUniqueB.size()]);
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
		return new SpdxFileDifference(fileA, fileB, concludedLicenseEquals, seenLicenseEquals, 
				uniqueSeenLicensesA, uniqueSeenLicensesB, artifactOfEquals, 
				uniqueArtifactOfA, uniqueArtifactOfB);
	}	
}
