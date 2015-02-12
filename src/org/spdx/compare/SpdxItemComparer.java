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
import java.util.Iterator;
import java.util.Map.Entry;

import org.spdx.rdfparser.license.AnyLicenseInfo;
import org.spdx.rdfparser.model.Annotation;
import org.spdx.rdfparser.model.Relationship;
import org.spdx.rdfparser.model.SpdxDocument;
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
	private boolean differenceFound = false;
	private boolean concludedLicenseEquals = true;
	private boolean seenLicenseEquals = true;
	protected String name = null;
	/**
	 * Map of unique extractedLicenseInfos between two documents
	 */
	HashMap<SpdxDocument, HashMap<SpdxDocument, AnyLicenseInfo[]>> uniqueLicenseInfosInFiles = 
			new HashMap<SpdxDocument, HashMap<SpdxDocument, AnyLicenseInfo[]>>();
	private boolean commentsEquals = true;
	private boolean copyrightsEquals = true;
	private boolean licenseCommmentsEquals = true;
	private boolean relationshipsEquals = true;
	/**
	 * Map of unique relationships between two documents
	 */
	HashMap<SpdxDocument, HashMap<SpdxDocument, Relationship[]>> uniqueRelationships =
			new HashMap<SpdxDocument, HashMap<SpdxDocument, Relationship[]>>();
	private boolean annotationsEquals = true;
	/**
	 * Map of unique annotations between two documents
	 */
	HashMap<SpdxDocument, HashMap<SpdxDocument, Annotation[]>> uniqueAnnotations =
			new HashMap<SpdxDocument, HashMap<SpdxDocument, Annotation[]>>();
	/**
	 * Map of SPDX document to Items
	 */
	protected HashMap<SpdxDocument, SpdxItem> documentItem = 
			new HashMap<SpdxDocument, SpdxItem>();
	/**
	 * Mapping of all extracted license info ID's between all SPDX documents included in the comparer
	 */
	protected HashMap<SpdxDocument, HashMap<SpdxDocument, HashMap<String, String>>> extractedLicenseIdMap;

	
	public SpdxItemComparer(HashMap<SpdxDocument, HashMap<SpdxDocument, HashMap<String, String>>> extractedLicenseIdMap) {
		this.extractedLicenseIdMap = extractedLicenseIdMap;
	}
	
	/**
	 * Add a new item to the comparer and compare the contents of the item
	 * to all items which have been previously added
	 * @param spdxDocument
	 * @param spdxItem
	 * @throws SpdxCompareException 
	 */
	public void addDocumentItem(SpdxDocument spdxDocument,
			SpdxItem spdxItem) throws SpdxCompareException {
		if (this.inProgress) {
			throw(new SpdxCompareException("Trying to add a document item while another document item is being added."));
		}
		if (this.name == null) {
			this.name = spdxItem.getName();
		} else if (!this.name.equals(spdxItem.getName())) {
			throw(new SpdxCompareException("Names do not match for item being added to comparer: "+
					spdxItem.getName()+", expecting "+this.name));
		}
		this.inProgress = true;
		this.differenceFound = false;
		Iterator<Entry<SpdxDocument, SpdxItem>> iter = this.documentItem.entrySet().iterator();
		if (iter.hasNext()) {
			Entry<SpdxDocument, SpdxItem> entry = iter.next();
			SpdxItem itemB = entry.getValue();
			HashMap<String, String> licenseXlationMap = this.extractedLicenseIdMap.get(spdxDocument).get(entry.getKey());
			if (!SpdxComparer.stringsEqual(spdxItem.getComment(), itemB.getComment())) {
				this.commentsEquals = false;
				this.differenceFound = true;
			}
			// Concluded License
			if (!LicenseCompareHelper.isLicenseEqual(spdxItem.getLicenseConcluded(), 
					itemB.getLicenseConcluded(), licenseXlationMap)) {
				this.concludedLicenseEquals = false;
				this.differenceFound = true;
			}
			// Copyrights
			if (!SpdxComparer.stringsEqual(spdxItem.getCopyrightText(), itemB.getCopyrightText())) {
				this.copyrightsEquals = false;
				this.differenceFound = true;
			}
			// license comments
			if (!SpdxComparer.stringsEqual(spdxItem.getLicenseComment(),
					itemB.getLicenseComment())) {
				this.licenseCommmentsEquals = false;
				this.differenceFound = true;
			}
			// Seen licenses
			compareLicenseInfosInFiles(spdxDocument, spdxItem.getLicenseInfoFromFiles());
			// relationships
			compareRelationships(spdxDocument, spdxItem.getRelationships());
			// Annotations
			compareAnnotation(spdxDocument, spdxItem.getAnnotations());
		}
		this.documentItem.put(spdxDocument, spdxItem);
		this.inProgress = false;
	}	
	
	/**
	 * Compares annotations and initializes the uniqueAnnotations
	 * as well as the annotationsEquals flag and sets the differenceFound to
	 * true if a difference was found for a newly added item
	 * @param spdxDocument document containing the item
	 * @param annotations
	 */
	private void compareAnnotation(SpdxDocument spdxDocument,
			Annotation[] annotations) {
		HashMap<SpdxDocument, Annotation[]> uniqueDocAnnotations = 
				this.uniqueAnnotations.get(spdxDocument);
		if (uniqueDocAnnotations == null) {
			uniqueDocAnnotations = new HashMap<SpdxDocument, Annotation[]>();
			this.uniqueAnnotations.put(spdxDocument, uniqueDocAnnotations);
		}
		Iterator<Entry<SpdxDocument, SpdxItem>> iter = this.documentItem.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<SpdxDocument, SpdxItem> entry = iter.next();
			HashMap<SpdxDocument, Annotation[]> compareDocAnnotations = 
					this.uniqueAnnotations.get(entry.getKey());
			if (compareDocAnnotations == null) {
				compareDocAnnotations = new HashMap<SpdxDocument, Annotation[]>();
				this.uniqueAnnotations.put(entry.getKey(), compareDocAnnotations);
			}
			Annotation[] compareAnnotations = entry.getValue().getAnnotations();
			Annotation[] uniqueAnnotations = SpdxComparer.findUniqueAnnotations(annotations, compareAnnotations);
			if (uniqueAnnotations.length > 0) {
				this.annotationsEquals = false;
				this.differenceFound = true;
			}
			uniqueDocAnnotations.put(entry.getKey(), uniqueAnnotations);
			uniqueAnnotations = SpdxComparer.findUniqueAnnotations(compareAnnotations, annotations);
			if (uniqueAnnotations.length > 0) {
				this.annotationsEquals = false;
				this.differenceFound = true;
			}
			compareDocAnnotations.put(spdxDocument, uniqueAnnotations);
		}
	}

	/**
	 * Compares relationships and initializes the uniqueRelationships 
	 * as well as the relationshipsEquals flag and sets the differenceFound to
	 * true if a difference was found for a newly added item
	 * @param spdxDocument document containing the item
	 * @param relationships
	 */
	private void compareRelationships(SpdxDocument spdxDocument,
			Relationship[] relationships) {
		HashMap<SpdxDocument, Relationship[]> uniqueDocRelationship = 
				this.uniqueRelationships.get(spdxDocument);
		if (uniqueDocRelationship == null) {
			uniqueDocRelationship = new HashMap<SpdxDocument, Relationship[]>();
			this.uniqueRelationships.put(spdxDocument, uniqueDocRelationship);
		}
		Iterator<Entry<SpdxDocument, SpdxItem>> iter = this.documentItem.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<SpdxDocument, SpdxItem> entry = iter.next();
			HashMap<SpdxDocument, Relationship[]> uniqueCompareRelationship = 
					this.uniqueRelationships.get(entry.getKey());
			if (uniqueCompareRelationship == null) {
				uniqueCompareRelationship = new HashMap<SpdxDocument, Relationship[]>();
				this.uniqueRelationships.put(entry.getKey(), uniqueCompareRelationship);
			}
			Relationship[] compareRelationships = entry.getValue().getRelationships();
			Relationship[] uniqueRelationships = SpdxComparer.findUniqueRelationships(relationships, compareRelationships);
			if (uniqueRelationships.length > 0) {
				this.relationshipsEquals = false;
				this.differenceFound = true;
			}
			uniqueDocRelationship.put(entry.getKey(), uniqueRelationships);
			uniqueRelationships = SpdxComparer.findUniqueRelationships(compareRelationships, relationships);
			if (uniqueRelationships.length > 0) {
				this.relationshipsEquals = false;
				this.differenceFound = true;
			}
			uniqueCompareRelationship.put(spdxDocument, uniqueRelationships);
		}
	}

	/**
	 * Compares seen licenses and initializes the uniqueSeenLicenses 
	 * as well as the seenLicenseEquals flag and sets the differenceFound to
	 * true if a difference was found for a newly added item
	 * @param spdxDocument document containing the item
	 * @param licenses
	 * @throws SpdxCompareException 
	 */
	private void compareLicenseInfosInFiles(SpdxDocument spdxDocument,
			AnyLicenseInfo[] licenses) throws SpdxCompareException {
		HashMap<SpdxDocument, AnyLicenseInfo[]> uniqueDocLicenses = 
				this.uniqueLicenseInfosInFiles.get(spdxDocument);
		if (uniqueDocLicenses == null) {
			uniqueDocLicenses = new HashMap<SpdxDocument, AnyLicenseInfo[]>();
			this.uniqueLicenseInfosInFiles.put(spdxDocument, uniqueDocLicenses);
		}
		Iterator<Entry<SpdxDocument, SpdxItem>> iter = this.documentItem.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<SpdxDocument, SpdxItem> entry = iter.next();
			HashMap<SpdxDocument, AnyLicenseInfo[]> uniqueCompareLicenses = 
					this.uniqueLicenseInfosInFiles.get(entry.getKey());
			if (uniqueCompareLicenses == null) {
				uniqueCompareLicenses = new HashMap<SpdxDocument, AnyLicenseInfo[]>();
				this.uniqueLicenseInfosInFiles.put(entry.getKey(), uniqueCompareLicenses);
			}
			AnyLicenseInfo[] compareLicenses = entry.getValue().getLicenseInfoFromFiles();
			ArrayList<AnyLicenseInfo> uniqueInDoc = new ArrayList<AnyLicenseInfo>();
			ArrayList<AnyLicenseInfo> uniqueInCompare = new ArrayList<AnyLicenseInfo>();
			HashMap<String, String> licenseXlationMap = this.extractedLicenseIdMap.get(spdxDocument).get(entry.getKey());
			compareLicenseArrays(licenses, compareLicenses, uniqueInDoc, uniqueInCompare, licenseXlationMap);
			if (uniqueInDoc.size() > 0 || uniqueInCompare.size() > 0) {
				this.seenLicenseEquals = false;
				this.differenceFound = true;
			}
			uniqueDocLicenses.put(entry.getKey(), uniqueInDoc.toArray(
					new AnyLicenseInfo[uniqueInDoc.size()]));
			uniqueCompareLicenses.put(spdxDocument, uniqueInCompare.toArray(
					new AnyLicenseInfo[uniqueInCompare.size()]));
		}
	}
		
	/**
	 * Compares to arrays of licenses updating the alUniqueA and alUniqueB to
	 * include any licenses found in A but not B and B but not A resp.
	 * @param licensesA
	 * @param licensesB
	 * @param alUniqueA
	 * @param alUniqueB
	 * @param licenseXlationMap
	 * @throws SpdxCompareException 
	 */
	private void compareLicenseArrays(AnyLicenseInfo[] licensesA,
			AnyLicenseInfo[] licensesB,
			ArrayList<AnyLicenseInfo> alUniqueA,
			ArrayList<AnyLicenseInfo> alUniqueB,
			HashMap<String, String> licenseXlationMap) throws SpdxCompareException {
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
	 * Get any licenses found in docA but not in docB
	 * @param docA
	 * @param docB
	 * @return
	 * @throws SpdxCompareException
	 */
	public AnyLicenseInfo[] getUniqueSeenLicenses(SpdxDocument docA, SpdxDocument docB) throws SpdxCompareException {
		checkInProgress();
		checkCompareMade();
		HashMap<SpdxDocument, AnyLicenseInfo[]> unique = 
				this.uniqueLicenseInfosInFiles.get(docA);
		if (unique == null) {
			return new AnyLicenseInfo[0];
		}
		AnyLicenseInfo[] retval = unique.get(docB);
		if (retval == null) {
			return new AnyLicenseInfo[0];
		} else {
			return retval;
		}
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
	 * checks to make sure there is not a compare in progress
	 * @throws SpdxCompareException 
	 * 
	 */
	protected void checkInProgress() throws SpdxCompareException {
		if (inProgress) {
			throw(new SpdxCompareException("File compare in progress - can not obtain compare results until compare has completed"));
		}
	}
	
	private void checkCompareMade() throws SpdxCompareException {
		if (this.documentItem.entrySet().size() < 1) {
			throw(new SpdxCompareException("Trying to obtain results of a file compare before a file compare has been performed"));
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
	 * Get the item contained by the document doc
	 * @param doc
	 * @return
	 * @throws SpdxCompareException
	 */
	public SpdxItem getItem(SpdxDocument doc) throws SpdxCompareException {
		checkInProgress();
		checkCompareMade();
		return this.documentItem.get(doc);
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
	 * Get relationships that are in docA but not in docB
	 * @param docA
	 * @param docB
	 * @return
	 * @throws SpdxCompareException
	 */
	public Relationship[] getUniqueRelationship(SpdxDocument docA, SpdxDocument docB) throws SpdxCompareException {
		checkInProgress();
		checkCompareMade();
		HashMap<SpdxDocument, Relationship[]> unique = this.uniqueRelationships.get(docA);
		if (unique == null) {
			return new Relationship[0];
		}
		Relationship[] retval = unique.get(docB);
		if (retval == null) {
			return new Relationship[0];
		} else {
			return retval;
		}
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
	 * Get annotations that are in docA but not in docB
	 * @param docA
	 * @param docB
	 * @return
	 * @throws SpdxCompareException
	 */
	public Annotation[] getUniqueAnnotations(SpdxDocument docA, SpdxDocument docB) throws SpdxCompareException {
		checkInProgress();
		checkCompareMade();
		HashMap<SpdxDocument, Annotation[]> unique = this.uniqueAnnotations.get(docA);
		if (unique == null) {
			return new Annotation[0];
		}
		Annotation[] retval = unique.get(docB);
		if (retval == null) {
			return new Annotation[0];
		} else {
			return retval;
		}
	}
}
