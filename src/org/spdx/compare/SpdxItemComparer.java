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

import java.util.ArrayList;
import java.util.HashMap;

import org.spdx.rdfparser.license.AnyLicenseInfo;
import org.spdx.rdfparser.model.Annotation;
import org.spdx.rdfparser.model.Relationship;
import org.spdx.rdfparser.model.SpdxItem;

/**
 * Compares two SPDX items.  The <code>compare(itemA, itemB)</code> method will perform the comparison and
 * store the results.  <code>isDifferenceFound()</code> will return true of any 
 * differences were found.
 * @author Gary
 *
 */
public class SpdxItemComparer {
	private boolean inProgress = false;
	private SpdxItem itemA;
	private SpdxItem itemB;
	private boolean differenceFound = false;
	private boolean concludedLicenseEquals;
	private boolean seenLicenseEquals;
	/**
	 * Seen licenses found in fileB but not in itemA
	 */
	private AnyLicenseInfo[] uniqueSeenLicensesB;
	/**
	 * Seen licenses found in fileA but not in itemB
	 */
	private AnyLicenseInfo[] uniqueSeenLicensesA;
	private boolean commentsEquals;
	private boolean copyrightsEquals;
	private boolean licenseCommmentsEquals;
	private boolean namesEquals;
	private boolean relationshipsEquals;
	private Relationship[] uniqueRelationshipA;
	private Relationship[] uniqueRelationshipB;
	private boolean annotationsEquals;
	private Annotation[] uniqueAnnotationsA;
	private Annotation[] uniqueAnnotationsB;
	
	public SpdxItemComparer() {
		
	}
	
	/**
	 * Compare two SPDX items and store the results
	 * @param itemA
	 * @param itemA
	 * @param licenseXlationMap A mapping between the license IDs from licenses in itemA to itemB
	 * @throws SpdxCompareException 
	 */
	public void compare(SpdxItem itemA, SpdxItem itemB, 
			HashMap<String, String> licenseXlationMap) throws SpdxCompareException {
		this.inProgress = true;
		this.differenceFound = false;
		this.itemA = itemA;
		this.itemB = itemB;
		// Comments
		if (SpdxComparer.stringsEqual(itemA.getComment(), itemB.getComment())) {
			this.commentsEquals = true;
		} else {
			this.commentsEquals = false;
			this.differenceFound = true;
		}
		// Concluded License
		if (LicenseCompareHelper.isLicenseEqual(itemA.getLicenseConcluded(), 
				itemB.getLicenseConcluded(), licenseXlationMap)) {
			this.concludedLicenseEquals = true;
		} else {
			this.concludedLicenseEquals = false;
			this.differenceFound = true;
		}
		// Copyrights
		if (SpdxComparer.stringsEqual(itemA.getCopyrightText(), itemB.getCopyrightText())) {
			this.copyrightsEquals = true;
		} else {
			this.copyrightsEquals = false;
			this.differenceFound = true;
		}
		// license comments
		if (SpdxComparer.stringsEqual(itemA.getLicenseComment(),
				itemB.getLicenseComment())) {
			this.licenseCommmentsEquals = true;
		} else {
			this.licenseCommmentsEquals = false;
			this.differenceFound = true;
		}
		// Name
		if (SpdxComparer.stringsEqual(itemA.getName(), itemB.getName())) {
			this.namesEquals = true;
		} else {
			this.namesEquals = false;
			this.differenceFound = true;
		}
		// Seen licenses
		compareSeenLicenses(itemA.getLicenseInfoFromFiles(), itemB.getLicenseInfoFromFiles(),
				licenseXlationMap);
		// relationships
		compareRelationships(itemA.getRelationships(), itemB.getRelationships());
		// Annotations
		compareAnnotation(itemA.getAnnotations(), itemB.getAnnotations());
		this.inProgress = false;
	}
	
	/**
	 * @param annotationsA
	 * @param annotationsB
	 */
	private void compareAnnotation(Annotation[] annotationsA,
			Annotation[] annotationsB) {
		if (SpdxComparer.elementsEquivalent(annotationsA, annotationsB)) {
			this.annotationsEquals = true;
			this.uniqueAnnotationsA= new Annotation[0];
			this.uniqueAnnotationsB = new Annotation[0];
		} else {
			this.annotationsEquals = false;
			this.differenceFound = true;
			this.uniqueAnnotationsA = SpdxComparer.findUniqueAnnotations(annotationsA, annotationsB);
			this.uniqueAnnotationsB = SpdxComparer.findUniqueAnnotations(annotationsB, annotationsA);
		}
	}

	/**
	 * @param relationshipsA
	 * @param relationshipsB
	 */
	private void compareRelationships(Relationship[] relationshipsA,
			Relationship[] relationshipsB) {
		if (SpdxComparer.elementsEquivalent(relationshipsA, relationshipsB)) {
			this.relationshipsEquals = true;
			this.uniqueRelationshipA = new Relationship[0];
			this.uniqueRelationshipB = new Relationship[0];
		} else {
			this.relationshipsEquals = false;
			this.differenceFound = true;
			this.uniqueRelationshipA = SpdxComparer.findUniqueRelationships(relationshipsA, relationshipsB);
			this.uniqueRelationshipB = SpdxComparer.findUniqueRelationships(relationshipsB, relationshipsA);
		}
	}

	/**
	 * Compares seen licenses and initializes the uniqueSeenLicenses arrays
	 * as well as the seenLicenseEquals flag and sets the differenceFound to
	 * true if a difference was found
	 * @param licensesA
	 * @param licensesB
	 * @throws SpdxCompareException 
	 */
	private void compareSeenLicenses(AnyLicenseInfo[] licensesA,
			AnyLicenseInfo[] licensesB, HashMap<String, String> licenseXlationMap) throws SpdxCompareException {
		ArrayList<AnyLicenseInfo> alUniqueA = new ArrayList<AnyLicenseInfo>();
		ArrayList<AnyLicenseInfo> alUniqueB = new ArrayList<AnyLicenseInfo>();		
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
		this.uniqueSeenLicensesA = alUniqueA.toArray(new AnyLicenseInfo[alUniqueA.size()]);
		this.uniqueSeenLicensesB = alUniqueB.toArray(new AnyLicenseInfo[alUniqueB.size()]);
		if (this.uniqueSeenLicensesA.length == 0 && this.uniqueSeenLicensesB.length == 0) {
			this.seenLicenseEquals = true;
		} else {
			this.seenLicenseEquals = false;
			this.differenceFound = true;
		}
	}
	
	/**
	 * @return the concludedLicenseEquals
	 */
	public boolean isConcludedLicenseEquals() throws SpdxCompareException {
		checkInProgress();
		checkCompareMade();
		return concludedLicenseEquals;
	}

	/**
	 * @return the seenLicenseEquals
	 */
	public boolean isSeenLicenseEquals() throws SpdxCompareException {
		checkInProgress();
		checkCompareMade();
		return seenLicenseEquals;
	}
	/**
	 * @return the uniqueSeenLicensesB
	 */
	public AnyLicenseInfo[] getUniqueSeenLicensesB() throws SpdxCompareException {
		checkInProgress();
		checkCompareMade();
		return uniqueSeenLicensesB;
	}

	/**
	 * @return the uniqueSeenLicensesA
	 */
	public AnyLicenseInfo[] getUniqueSeenLicensesA() throws SpdxCompareException {
		checkInProgress();
		checkCompareMade();
		return uniqueSeenLicensesA;
	}
	
	/**
	 * @return the commentsEquals
	 */
	public boolean isCommentsEquals() throws SpdxCompareException {
		checkInProgress();
		checkCompareMade();
		return commentsEquals;
	}

	/**
	 * @return the copyrightsEquals
	 */
	public boolean isCopyrightsEquals() throws SpdxCompareException {
		checkInProgress();
		checkCompareMade();
		return copyrightsEquals;
	}

	/**
	 * @return the licenseCommmentsEquals
	 */
	public boolean isLicenseCommmentsEquals() throws SpdxCompareException {
		checkInProgress();
		checkCompareMade();
		return licenseCommmentsEquals;
	}

	/**
	 * @return the namesEquals
	 */
	public boolean isNamesEquals() throws SpdxCompareException {
		checkInProgress();
		checkCompareMade();
		return namesEquals;
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
		if (this.itemA == null || this.itemB == null) {
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
	 * @return the inProgress
	 */
	public boolean isInProgress() throws SpdxCompareException {
		checkInProgress();
		checkCompareMade();
		return inProgress;
	}

	/**
	 * @return the itemA
	 */
	public SpdxItem getItemA() throws SpdxCompareException {
		checkInProgress();
		checkCompareMade();
		return itemA;
	}

	/**
	 * @return the itemB
	 */
	public SpdxItem getItemB() throws SpdxCompareException {
		checkInProgress();
		checkCompareMade();
		return itemB;
	}

	/**
	 * @return the relationshipsEquals
	 */
	public boolean isRelationshipsEquals() throws SpdxCompareException {
		checkInProgress();
		checkCompareMade();
		return relationshipsEquals;
	}

	/**
	 * @return the uniqueRelationshipA
	 */
	public Relationship[] getUniqueRelationshipA() throws SpdxCompareException {
		checkInProgress();
		checkCompareMade();
		return uniqueRelationshipA;
	}

	/**
	 * @return the uniqueRelationshipB
	 */
	public Relationship[] getUniqueRelationshipB() throws SpdxCompareException {
		checkInProgress();
		checkCompareMade();
		return uniqueRelationshipB;
	}

	/**
	 * @return the annotationsEquals
	 */
	public boolean isAnnotationsEquals() throws SpdxCompareException {
		checkInProgress();
		checkCompareMade();
		return annotationsEquals;
	}

	/**
	 * @return the uniqueAnnotationsA
	 */
	public Annotation[] getUniqueAnnotationsA() throws SpdxCompareException {
		checkInProgress();
		checkCompareMade();
		return uniqueAnnotationsA;
	}

	/**
	 * @return the uniqueAnnotationsB
	 * @throws SpdxCompareException 
	 */
	public Annotation[] getUniqueAnnotationsB() throws SpdxCompareException {
		checkInProgress();
		checkCompareMade();
		return uniqueAnnotationsB;
	}

	/**
	 * Return a file difference - the two file names must equal
	 * @return
	 * @throws SpdxCompareException 
	 */
	public SpdxItemDifference getItemDifference() throws SpdxCompareException {
		checkInProgress();
		checkCompareMade();
		if (!itemA.getName().equals(itemB.getName())) {
			throw(new SpdxCompareException("Can not create an SPDX item difference for two files with different names"));
		}
		return new SpdxItemDifference(itemA, itemB, concludedLicenseEquals, seenLicenseEquals, 
				uniqueSeenLicensesA, uniqueSeenLicensesB, 
				relationshipsEquals, uniqueRelationshipA, uniqueRelationshipB,
				annotationsEquals, uniqueAnnotationsA, uniqueAnnotationsB);
	}	
}
