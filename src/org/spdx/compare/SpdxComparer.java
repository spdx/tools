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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;

import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SPDXCreatorInformation;
import org.spdx.rdfparser.license.AnyLicenseInfo;
import org.spdx.rdfparser.license.ExtractedLicenseInfo;
import org.spdx.rdfparser.model.Annotation;
import org.spdx.rdfparser.model.Checksum;
import org.spdx.rdfparser.model.ExternalDocumentRef;
import org.spdx.rdfparser.model.RdfModelObject;
import org.spdx.rdfparser.model.Relationship;
import org.spdx.rdfparser.model.SpdxDocument;
import org.spdx.rdfparser.model.SpdxElement;
import org.spdx.rdfparser.model.SpdxFile;
import org.spdx.rdfparser.model.SpdxItem;
import org.spdx.rdfparser.model.SpdxPackage;
import org.spdx.rdfparser.SPDXReview;
import org.spdx.rdfparser.SpdxPackageVerificationCode;

/**
 * Performs a comparison between two or more SPDX documents and holds the results of the comparison
 * The main function to perform the comparison is <code>compare(spdxdoc1, spdxdoc2)</code>
 * 
 * For reviewers, the comparison results are separated into unique reviewers for a give document
 * which can be obtained by the method <code>getUniqueReviewers(index1, index2)</code>.  The 
 * uniqueness is determined by the reviewer name.  If two documents contain reviewers with the 
 * same name but different dates or comments, the reviews are considered to be the same review
 * with different data.  The differences for these reviews can be obtained through the method
 * <code>getReviewerDifferences(index1, index2)</code>
 * 
 * For files, the comparison results are separated into unique files based on the file names
 * which can be obtained by the method <code>getUniqueFiles(index1, index2)</code>.  If two
 * documents contain files with the same name, but different data, the differences for these
 * files can be obtained through the method <code>getFileDifferences(index1, index2)</code>
 * 
 * Multi-threading considerations: This class is "mostly" threadsafe in that the calls to 
 * perform the comparison are synchronized and a flag is used to throw an error for any
 * calls to getters when a compare is in progress.  There is a small theoretical window in the
 * getters where the compare operation is started in the middle of a get operation.
 * 
 * @author Gary O'Neall
 *
 */
public class SpdxComparer {
	
	/**
	 * Contains the results of a comparison between two SPDXReviews where
	 * the reviewer name is the same but there is a difference in the
	 * reviewer comment or the reviewer date
	 * @author Gary O'Neall
	 *
	 */
	public class SPDXReviewDifference {
		
		boolean commentsEqual;
		boolean datesEqual;
		String comment1;
		String comment2;
		String date1;
		String date2;
		String reviewer;

		/**
		 * @param spdxReview
		 * @param spdxReview2
		 */
		public SPDXReviewDifference(SPDXReview spdxReview,
				SPDXReview spdxReview2) {
			commentsEqual = spdxReview.getComment().trim().equals(spdxReview2.getComment().trim());
			datesEqual = spdxReview.getReviewDate().equals(spdxReview2.getReviewDate());
			this.comment1 = spdxReview.getComment();
			this.comment2 = spdxReview2.getComment();
			this.date1 = spdxReview.getReviewDate();
			this.date2 = spdxReview2.getReviewDate();
			this.reviewer = spdxReview.getReviewer();
		}

		/**
		 * @return true of the dates are equal
		 */
		public boolean isDateEqual() {
			return this.datesEqual;
		}

		/**
		 * @return
		 */
		public String getReviewer() {
			return this.reviewer;
		}

		/**
		 * Get the reviewer date for one of the two reviews compared
		 * @param i if 0, the review date of the first reviewer, if 1, it is the second reviewer
		 * @return
		 * @throws SpdxCompareException 
		 */
		public String getDate(int i) throws SpdxCompareException {
			if (i == 0) {
				return this.date1;
			} else if (i == 1) {
				return this.date2;
			} else {
				throw(new SpdxCompareException("Invalid index for get reviewer date"));
			}
		}

		/**
		 * @return true if comments are equal
		 */
		public boolean isCommentEqual() {
			return this.commentsEqual;
		}

		/**
		 * Get the reviewer comment for one of the two reviews compared
		 * @param i if 0, the review date of the first reviewer, if 1, it is the second reviewer
		 * @return
		 * @throws SpdxCompareException 
		 */
		public String getComment(int i) throws SpdxCompareException {
			if (i == 0) {
				return this.comment1;
			} else if (i == 1) {
				return this.comment2;
			} else {
				throw(new SpdxCompareException("Invalid index for get reviewer date"));
			}
		}
		
	}
	

	
	private SpdxDocument[] spdxDocs = null;
	private boolean differenceFound = false;
	private boolean compareInProgress = false;
	
	// Document level results
	private boolean spdxVersionsEqual = true;
	private boolean documentCommentsEqual = true;
	private boolean dataLicenseEqual = true;
	private boolean licenseListVersionEquals = true;
	
	// Reviewer results
	/**
	 * Holds a map of all SPDX documents which have reviewers unique relative to other SPDX document
	 * based on the reviewer name.  The results of the map is another map of all SPDX documents in 
	 * the comparison which do not contain some of the reviewers in the key document.  See the
	 * implementation of compareReviewers for details
	 */
	private HashMap<SpdxDocument, HashMap<SpdxDocument, SPDXReview[]>> uniqueReviews = 
		new HashMap<SpdxDocument, HashMap<SpdxDocument, SPDXReview[]>>();
	/**
	 * Holds a map of any SPDX documents which have reviewer differenes.  A reviewer difference
	 * is an SPDXReview with the same reviewer name but a different reviewer date or comment
	 */
	private HashMap<SpdxDocument, HashMap<SpdxDocument, SPDXReviewDifference[]>> reviewerDifferences = 
		new HashMap<SpdxDocument, HashMap<SpdxDocument, SPDXReviewDifference[]>>();
	
	// Extracted Licensing Info results
	/**
	 * Holds a map of all SPDX documents which have extracted license infos unique relative to other SPDX document
	 * based on the reviewer name.  The results of the map is another map of all SPDX documents in 
	 * the comparison which do not contain some of the reviewers in the key document.  See the
	 * implementation of compareReviewers for details
	 */
	private HashMap<SpdxDocument, HashMap<SpdxDocument, ExtractedLicenseInfo[]>> uniqueExtractedLicenses = 
		new HashMap<SpdxDocument, HashMap<SpdxDocument, ExtractedLicenseInfo[]>>();
	/**
	 * Map of any SPDX documents that have extraced license infos with equivalent text but different comments, id's or other fields
	 */
	private HashMap<SpdxDocument, HashMap<SpdxDocument, SpdxLicenseDifference[]>> licenseDifferences = 
		new HashMap<SpdxDocument, HashMap<SpdxDocument, SpdxLicenseDifference[]>>();
	/**
	 * Maps the license ID's for the extracted license infos of the documents being compared.  License ID's are mapped based on the text
	 * being equivalent 
	 */
	private HashMap<SpdxDocument, HashMap<SpdxDocument, HashMap<String, String>>> extractedLicenseIdMap = 
		new HashMap<SpdxDocument, HashMap<SpdxDocument, HashMap<String, String>>>();

	private boolean creatorInformationEquals;
	private HashMap<SpdxDocument, HashMap<SpdxDocument, String[]>> uniqueCreators = 
		new HashMap<SpdxDocument, HashMap<SpdxDocument, String[]>>();
	
	// file compare results
	/**
	 * Holds a map of all SPDX documents which have files unique relative to other SPDX document
	 * based on the file name.  The results of the map is another map of all SPDX documents in 
	 * the comparison which do not contain some of the files in the key document.  See the
	 * implementation of compareFiles for details
	 */
	private HashMap<SpdxDocument, HashMap<SpdxDocument, SpdxFile[]>> uniqueFiles = 
		new HashMap<SpdxDocument, HashMap<SpdxDocument, SpdxFile[]>>();
	
	/**
	 * Holds a map of any SPDX documents which have file differences.  A file difference
	 * is an SPDXReview with the same filename name but a different file property
	 */
	private HashMap<SpdxDocument, HashMap<SpdxDocument, SpdxFileDifference[]>> fileDifferences = 
		new HashMap<SpdxDocument, HashMap<SpdxDocument, SpdxFileDifference[]>>();

	// Package compare results
	/**
	 * Holds a map of all SPDX documents which have packages unique relative to other SPDX document
	 * based on the package name and package version.  The results of the map is another map of all SPDX documents in 
	 * the comparison which do not contain some of the packages in the key document.  See the
	 * implementation of comparePackages for details
	 */
	private HashMap<SpdxDocument, HashMap<SpdxDocument, SpdxPackage[]>> uniquePackages = 
		new HashMap<SpdxDocument, HashMap<SpdxDocument, SpdxPackage[]>>();
	
	/**
	 * Map of package names to package comparisons
	 */
	private HashMap<String, SpdxPackageComparer> packageComparers = new HashMap<String, SpdxPackageComparer>();
	// Annotation comparison results
	private HashMap<SpdxDocument, HashMap<SpdxDocument, Annotation[]>> uniqueDocumentAnnotations = 
		new HashMap<SpdxDocument, HashMap<SpdxDocument, Annotation[]>>();

	// Document Relationships comparison results
	private HashMap<SpdxDocument, HashMap<SpdxDocument, Relationship[]>> uniqueDocumentRelationships = 
		new HashMap<SpdxDocument, HashMap<SpdxDocument, Relationship[]>>();

	// External Document References comparison results
	private HashMap<SpdxDocument, HashMap<SpdxDocument, ExternalDocumentRef[]>> uniqueExternalDocumentRefs = 
		new HashMap<SpdxDocument, HashMap<SpdxDocument, ExternalDocumentRef[]>>();
	
	public SpdxComparer() {
		
	}
	
	/**
	 * Compares 2 SPDX documents
	 * @param spdxDoc1
	 * @param spdxDoc2
	 * @throws InvalidSPDXAnalysisException
	 * @throws SpdxCompareException
	 */
	public synchronized void compare(SpdxDocument spdxDoc1, SpdxDocument spdxDoc2) throws InvalidSPDXAnalysisException, SpdxCompareException {
		compare(new SpdxDocument[] {spdxDoc1, spdxDoc2});
	}
	
	/**
	 * Compares multiple SPDX documents
	 * @param spdxDocuments
	 * @throws SpdxCompareException 
	 * @throws InvalidSPDXAnalysisException 
	 */
	public void compare(SpdxDocument[] spdxDocuments) throws InvalidSPDXAnalysisException, SpdxCompareException {
		//TODO: Add a monitor function which allows for cancel
		clearCompareResults();
		this.spdxDocs = spdxDocuments;
		differenceFound = false;
		performCompare();	
	}

	/**
	 * @throws InvalidSPDXAnalysisException 
	 * @throws SpdxCompareException 
	 * 
	 */
	private void performCompare() throws InvalidSPDXAnalysisException, SpdxCompareException {
		compareInProgress = true;
		differenceFound = false;
		compareExtractedLicenseInfos();	// note - this must be done first to build the translation map of IDs
		compareDocumentFields();
		compareFiles();
		comparePackages();
		compareReviewers();
		compareCreators();
		compareDocumentAnnotations();
		compareDocumentRelationships();
		compareExternalDocumentRefs();
		compareInProgress = false;	
	}

	/**
	 * @throws InvalidSPDXAnalysisException 
	 * 
	 */
	private void compareExternalDocumentRefs() throws InvalidSPDXAnalysisException {
		// this will be a N x N comparison of all external document relationships to fill the
		// hashmap uniqueExternalDocumentRefs
		for (int i = 0; i < spdxDocs.length; i++) {
			ExternalDocumentRef[] externalDocRefsA = spdxDocs[i].getExternalDocumentRefs();
			HashMap<SpdxDocument, ExternalDocumentRef[]> uniqueAMap = uniqueExternalDocumentRefs.get(spdxDocs[i]);
			if (uniqueAMap == null) {
				uniqueAMap = new HashMap<SpdxDocument, ExternalDocumentRef[]>();
				// We will put this into the hashmap at the end of this method if it is not empty
			}
			for (int j = 0; j < spdxDocs.length; j++) {
				if (j == i) {
					continue;	// skip comparing to ourself
				}
				ExternalDocumentRef[] externalDocRefsB = spdxDocs[j].getExternalDocumentRefs();

				// find any external refs in A that are not in B
				ExternalDocumentRef[] uniqueA = findUniqueExternalDocumentRefs(externalDocRefsA, externalDocRefsB);
				if (uniqueA != null && uniqueA.length > 0) {
					uniqueAMap.put(spdxDocs[j], uniqueA);					
				}
			}
			if (uniqueAMap.keySet().size() > 0) {
				this.uniqueExternalDocumentRefs.put(spdxDocs[i], uniqueAMap);
			}
		}
		if (!this._isExternalDcoumentRefsEqualsNoCheck()) {
			this.differenceFound = true;
		}	
	}

	/**
	 * Compare all of the document level relationships
	 */
	private void compareDocumentRelationships() {
		// this will be a N x N comparison of all document level relationships to fill the
		// hashmap uniqueDocumentRelationships
		for (int i = 0; i < spdxDocs.length; i++) {
			Relationship[] relationshipsA = spdxDocs[i].getRelationships();
			HashMap<SpdxDocument, Relationship[]> uniqueAMap = uniqueDocumentRelationships.get(spdxDocs[i]);
			if (uniqueAMap == null) {
				uniqueAMap = new HashMap<SpdxDocument, Relationship[]>();
				// We will put this into the hashmap at the end of this method if it is not empty
			}
			for (int j = 0; j < spdxDocs.length; j++) {
				if (j == i) {
					continue;	// skip comparing to ourself
				}
				Relationship[] relationshipsB = spdxDocs[j].getRelationships();

				// find any creators in A that are not in B
				Relationship[] uniqueA = findUniqueRelationships(relationshipsA, relationshipsB);
				if (uniqueA != null && uniqueA.length > 0) {
					uniqueAMap.put(spdxDocs[j], uniqueA);					
				}
			}
			if (uniqueAMap.keySet().size() > 0) {
				this.uniqueDocumentRelationships.put(spdxDocs[i], uniqueAMap);
			}
		}
		if (!this._isDocumentRelationshipsEqualsNoCheck()) {
			this.differenceFound = true;
		}	
	}

	/**
	 * Compare all of the Document level annotations
	 */
	private void compareDocumentAnnotations() {
		// this will be a N x N comparison of all document level annotations to fill the
		// hashmap uniqueAnnotations
		for (int i = 0; i < spdxDocs.length; i++) {
			Annotation[] annotationsA = spdxDocs[i].getAnnotations();
			HashMap<SpdxDocument, Annotation[]> uniqueAMap = uniqueDocumentAnnotations.get(spdxDocs[i]);
			if (uniqueAMap == null) {
				uniqueAMap = new HashMap<SpdxDocument, Annotation[]>();
				// We will put this into the hashmap at the end of this method if it is not empty
			}
			for (int j = 0; j < spdxDocs.length; j++) {
				if (j == i) {
					continue;	// skip comparing to ourself
				}
				Annotation[] annotationsB = spdxDocs[j].getAnnotations();

				// find any creators in A that are not in B
				Annotation[] uniqueA = findUniqueAnnotations(annotationsA, annotationsB);
				if (uniqueA != null && uniqueA.length > 0) {
					uniqueAMap.put(spdxDocs[j], uniqueA);					
				}
			}
			if (uniqueAMap.keySet().size() > 0) {
				this.uniqueDocumentAnnotations.put(spdxDocs[i], uniqueAMap);
			}
		}
		if (!this._isDocumentAnnotationsEqualsNoCheck()) {
			this.differenceFound = true;
		}	
	}

	/**
	 * @throws InvalidSPDXAnalysisException 
	 * @throws SpdxCompareException 
	 * 
	 */
	private void compareFiles() throws InvalidSPDXAnalysisException, SpdxCompareException {
		this.uniqueFiles.clear();
		this.fileDifferences.clear();
		// N x N comparison of all files
		for (int i = 0; i < spdxDocs.length; i++) {
			SpdxFile[] filesA = collectAllFiles(spdxDocs[i]);
			// note - the file arrays MUST be sorted for the comparator methods to work
			Arrays.sort(filesA);
			HashMap<SpdxDocument, SpdxFile[]> uniqueAMap = 
				this.uniqueFiles.get(spdxDocs[i]);
			if (uniqueAMap == null) {
				uniqueAMap = new HashMap<SpdxDocument, SpdxFile[]>();
			}
			// this map will be added to uniqueFiles at the end if we find anything
			HashMap<SpdxDocument, SpdxFileDifference[]> diffMap = 
				this.fileDifferences.get(spdxDocs[i]);
			if (diffMap == null) {
				diffMap = new HashMap<SpdxDocument, SpdxFileDifference[]>();
			}
			for (int j = 0; j < spdxDocs.length; j++) {
				if (j == i) {
					continue;
				}
				SpdxFile[] filesB = collectAllFiles(spdxDocs[j]);
				//Note that the files arrays must be sorted for the find methods to work
				Arrays.sort(filesB);
				SpdxFile[] uniqueAB = findUniqueFiles(filesA, filesB);
				if (uniqueAB != null && uniqueAB.length > 0) {
					uniqueAMap.put(spdxDocs[j], uniqueAB);
				}
				SpdxFileDifference[] differences = findFileDifferences(spdxDocs[i], spdxDocs[j], filesA, filesB, this.extractedLicenseIdMap);
				if (differences != null && differences.length > 0) {
					diffMap.put(spdxDocs[j], differences);
				}
			}
			if (!uniqueAMap.isEmpty()) {
				this.uniqueFiles.put(spdxDocs[i], uniqueAMap);
			}
			if (!diffMap.isEmpty()) {
				this.fileDifferences.put(spdxDocs[i], diffMap);
			}
		}
		if (!_isFilesEqualsNoCheck()) {
			this.differenceFound = true;
		}
	}
	
	/**
	 * Add all files found in the related elements (including descendant related elements)
	 * @param element
	 * @param files
	 * @throws InvalidSPDXAnalysisException 
	 */
	private void addAllRelatedFiles(SpdxElement element, HashSet<SpdxFile> files,
			HashSet<SpdxElement> visitedElements) throws InvalidSPDXAnalysisException {
		if (element == null || visitedElements.contains(element)) {
			return;
		}
		visitedElements.add(element);
		Relationship[] relationships = element.getRelationships();
		if (relationships != null) {
			for (int j = 0; j < relationships.length; j++) {
				if (relationships[j] != null && 
						relationships[j].getRelatedSpdxElement() instanceof SpdxFile &&
						!files.contains(relationships[j].getRelatedSpdxElement())) {
					files.add((SpdxFile)(relationships[j].getRelatedSpdxElement()));
				} else if (relationships[j] != null && 
						relationships[j].getRelatedSpdxElement() instanceof SpdxPackage) {
					SpdxFile[] pkgFiles = ((SpdxPackage)(relationships[j].getRelatedSpdxElement())).getFiles();
					if (pkgFiles != null) {
						for (int k = 0; k < pkgFiles.length; k++) {
							files.add(pkgFiles[k]);
						}
					}
				}
				// recursively add all of the related files to this relationships
				addAllRelatedFiles(relationships[j].getRelatedSpdxElement(), files, visitedElements);
			}
		}
	}
	
	/**
	 * Add all packages found in the related elements (including descendant related elements)
	 * @param element
	 * @param pkgs
	 * @throws InvalidSPDXAnalysisException 
	 */
	private void addAllRelatedPackages(SpdxElement element, 
			HashSet<SpdxPackage> pkgs,
			HashSet<SpdxElement> visitedElements) throws InvalidSPDXAnalysisException {
		if (element == null || visitedElements.contains(element)) {
			return;
		}
		visitedElements.add(element);
		Relationship[] relationships = element.getRelationships();
		if (relationships != null) {
			for (int j = 0; j < relationships.length; j++) {
				if (relationships[j] != null && 
						relationships[j].getRelatedSpdxElement() instanceof SpdxPackage &&
						!pkgs.contains(relationships[j].getRelatedSpdxElement())) {
					pkgs.add((SpdxPackage)(relationships[j].getRelatedSpdxElement()));
				}
				// recursively add all of the related files to this relationships
				addAllRelatedPackages(relationships[j].getRelatedSpdxElement(), 
						pkgs, visitedElements);
			}
		}
	}
	
	/**
	 * Collect all of the packages present in the SPDX document including packages 
	 * embedded in other relationships within documents
	 * @param spdxDocument
	 * @return
	 * @throws InvalidSPDXAnalysisException 
	 */
	protected SpdxPackage[] collectAllPackages(SpdxDocument spdxDocument) throws InvalidSPDXAnalysisException {
		HashSet<SpdxPackage> retval = new HashSet<SpdxPackage>();
		SpdxItem[] items = spdxDocument.getSpdxItems();
		for (int i = 0; i < items.length; i++) {
			if (items[i] instanceof SpdxPackage) {
				retval.add((SpdxPackage)items[i]);
			}
			addAllRelatedPackages(items[i], retval, new HashSet<SpdxElement>());
		}	
		return retval.toArray(new SpdxPackage[retval.size()]);
	}

	/**
	 * Collect all of the files present in the SPDX document including files within documents
	 * and files embedded in packages
	 * @param spdxDocument
	 * @return
	 * @throws InvalidSPDXAnalysisException 
	 */
	protected SpdxFile[] collectAllFiles(SpdxDocument spdxDocument) throws InvalidSPDXAnalysisException {
		HashSet<SpdxFile> retval = new HashSet<SpdxFile>();
		SpdxItem[] items = spdxDocument.getSpdxItems();
		for (int i = 0; i < items.length; i++) {
			if (items[i] instanceof SpdxFile) {
				retval.add((SpdxFile)items[i]);			
			} else if (items[i] instanceof SpdxPackage) {
				SpdxFile[] pkgFiles = ((SpdxPackage)items[i]).getFiles();
				for (int j = 0; j < pkgFiles.length; j++) {
					retval.add(pkgFiles[j]);
				}
			}
			addAllRelatedFiles(items[i], retval, new HashSet<SpdxElement>());
		}	
		return retval.toArray(new SpdxFile[retval.size()]);
	}

	/**
	 * Returns an array of files differences between A and B where the names
	 * are the same, but one or more properties are different for that file
	 * @param filesA
	 * @param filesB
	 * @return
	 * @throws SpdxCompareException 
	 */
	static SpdxFileDifference[] findFileDifferences(SpdxDocument docA, SpdxDocument docB,
			SpdxFile[] filesA, SpdxFile[] filesB, 
			HashMap<SpdxDocument, HashMap<SpdxDocument, HashMap<String, String>>> licenseIdXlationMap) throws SpdxCompareException {
		
		ArrayList<SpdxFileDifference> alRetval = new ArrayList<SpdxFileDifference>();
		int aIndex = 0;
		int bIndex = 0;
		while (aIndex < filesA.length && bIndex < filesB.length) {
			int compare = filesA[aIndex].getName().compareTo(filesB[bIndex].getName());
			if (compare == 0) {
				SpdxFileComparer fileComparer = new SpdxFileComparer(licenseIdXlationMap);
				fileComparer.addDocumentFile(docA, filesA[aIndex]);
				fileComparer.addDocumentFile(docB, filesB[bIndex]);
				if (fileComparer.isDifferenceFound()) {
					alRetval.add(fileComparer.getFileDifference(docA, docB));
				}
				aIndex++;
				bIndex++;
			} else if (compare > 0) {
				// fileA is greater than fileB
				bIndex++;
			} else {
				// fileB is greater than fileA
				aIndex++;
			}
		}
		SpdxFileDifference[] retval = alRetval.toArray(new SpdxFileDifference[alRetval.size()]);
		return retval;
	}

	/**
	 * finds any packages in A that are not in B.  Packages are considered the
	 * same if they have the same package name and the same package version.
	 * NOTE: The arrays must be sorted by file name
	 * @param pkgsA
	 * @param pkgsB
	 * @return
	 */
	static SpdxPackage[] findUniquePackages(SpdxPackage[] pkgsA, SpdxPackage[] pkgsB) {
		int bIndex = 0;
		int aIndex = 0;
		ArrayList<SpdxPackage> alRetval = new ArrayList<SpdxPackage>();
		while (aIndex < pkgsA.length) {
			if (bIndex >= pkgsB.length) {
				alRetval.add(pkgsA[aIndex]);
				aIndex++;
			} else {
				int compareVal = pkgsA[aIndex].compareTo(pkgsB[bIndex]);
				if (compareVal == 0) {
					// packages are equal
					aIndex++;
					bIndex++;
				} else if (compareVal > 0) {
					// pkgA is greater than pkgB
					bIndex++;
				} else {
					// pkgB is greater than pkgA
					alRetval.add(pkgsA[aIndex]);
					aIndex++;
				}
			}
		}
		SpdxPackage[] retval = alRetval.toArray(new SpdxPackage[alRetval.size()]);
		return retval;
	}
	/**
	 * finds any files in A that are not in B.  NOTE: The arrays must be sorted by file name
	 * @param filesA
	 * @param filesB
	 * @return
	 */
	static SpdxFile[] findUniqueFiles(SpdxFile[] filesA, SpdxFile[] filesB) {
		int bIndex = 0;
		int aIndex = 0;
		ArrayList<SpdxFile> alRetval = new ArrayList<SpdxFile>();
		while (aIndex < filesA.length) {
			if (bIndex >= filesB.length) {
				alRetval.add(filesA[aIndex]);
				aIndex++;
			} else {
				int compareVal = filesA[aIndex].getName().compareTo(filesB[bIndex].getName());
				if (compareVal == 0) {
					// files are equal
					aIndex++;
					bIndex++;
				} else if (compareVal > 0) {
					// fileA is greater than fileB
					bIndex++;
				} else {
					// fileB is greater tha fileA
					alRetval.add(filesA[aIndex]);
					aIndex++;
				}
			}
		}
		SpdxFile[] retval = alRetval.toArray(new SpdxFile[alRetval.size()]);
		return retval;
	}

	/**
	 * @throws InvalidSPDXAnalysisException 
	 * 
	 */
	private void compareCreators() throws InvalidSPDXAnalysisException {
		this.creatorInformationEquals = true;
		this.licenseListVersionEquals = true;
		// this will be a N x N comparison of all creators to fill the
		// hashmap uniqueCreators
		for (int i = 0; i < spdxDocs.length; i++) {
			SPDXCreatorInformation creatorInfoA = spdxDocs[i].getCreationInfo();
			String[] creatorsA = creatorInfoA.getCreators();
			HashMap<SpdxDocument, String[]> uniqueAMap = uniqueCreators.get(spdxDocs[i]);
			if (uniqueAMap == null) {
				uniqueAMap = new HashMap<SpdxDocument, String[]>();
				// We will put this into the hashmap at the end of this method if it is not empty
			}
			for (int j = 0; j < spdxDocs.length; j++) {
				if (j == i) {
					continue;	// skip comparing to ourself
				}
				SPDXCreatorInformation creatorInfoB = spdxDocs[j].getCreationInfo();
				String[] creatorsB = creatorInfoB.getCreators();

				// find any creators in A that are not in B
				String[] uniqueA = findUniqueString(creatorsA, creatorsB);
				if (uniqueA != null && uniqueA.length > 0) {
					uniqueAMap.put(spdxDocs[j], uniqueA);					
				}
				// compare creator comments
				if (!stringsEqual(creatorInfoA.getComment(), creatorInfoB.getComment())) {
					this.creatorInformationEquals = false;
				}
				// compare creation dates
				if (!stringsEqual(creatorInfoA.getCreated(), creatorInfoB.getCreated())) {
					this.creatorInformationEquals = false;
				}
				// compare license list versions
				if (!stringsEqual(creatorInfoA.getLicenseListVersion(), creatorInfoB.getLicenseListVersion())) {
					this.creatorInformationEquals = false;
					this.licenseListVersionEquals = false;
				}
			}
			if (uniqueAMap.keySet().size() > 0) {
				this.uniqueCreators.put(spdxDocs[i], uniqueAMap);
				this.creatorInformationEquals = false;
			}
		}
		if (!this.creatorInformationEquals) {
			this.differenceFound = true;
		}	
	}

	/**
	 * Finds any strings which are in A but not in B
	 * @param stringsA
	 * @param stringsB
	 * @return
	 */
	private String[] findUniqueString(String[] stringsA, String[] stringsB) {
		if (stringsA == null) {
			return new String[0];
		}
		if (stringsB == null) {	
			return Arrays.copyOf(stringsA, stringsA.length);	
		}
		ArrayList<String> al = new ArrayList<String>();
		for (int i = 0; i < stringsA.length; i++) {
			boolean found = false;
			for (int j = 0; j < stringsB.length; j++) {
				if (stringsA[i].trim().equals(stringsB[j].trim())) {
					found = true;
					break;
				}
			}
			if (!found) {
				al.add(stringsA[i]);
			}
		}
		return al.toArray(new String[al.size()]);
	}

	/**
	 * Compares the SPDX documents and sets the appropriate flags
	 * @throws SpdxCompareException 
	 */
	private void comparePackages() throws SpdxCompareException {
		if (this.spdxDocs == null || this.spdxDocs.length < 1) {
			return;
		}
		this.uniquePackages.clear();
		this.packageComparers.clear();
		// N x N comparison of all files
		for (int i = 0; i < spdxDocs.length; i++) {
			SpdxPackage[] pkgsA;
			try {
				pkgsA = collectAllPackages(spdxDocs[i]);
			} catch (InvalidSPDXAnalysisException e) {
				throw(new SpdxCompareException("Error collecting packages from SPDX document "+spdxDocs[i].getName(), e));
			}
			// note - the package arrays MUST be sorted for the comparator methods to work
			Arrays.sort(pkgsA);
			addPackageComparers(spdxDocs[i], pkgsA, this.extractedLicenseIdMap);
			HashMap<SpdxDocument, SpdxPackage[]> uniqueAMap = 
				this.uniquePackages.get(spdxDocs[i]);
			if (uniqueAMap == null) {
				uniqueAMap = new HashMap<SpdxDocument, SpdxPackage[]>();
			}
			for (int j = 0; j < spdxDocs.length; j++) {
				if (j == i) {
					continue;
				}
				SpdxPackage[] pkgsB;
				try {
					pkgsB = collectAllPackages(spdxDocs[j]);
				} catch (InvalidSPDXAnalysisException e) {
					throw(new SpdxCompareException("Error collecting packages from SPDX document "+spdxDocs[i].getName(), e));
				}
				//Note that the files arrays must be sorted for the find methods to work
				Arrays.sort(pkgsB);
				SpdxPackage[] uniqueAB = findUniquePackages(pkgsA, pkgsB);
				if (uniqueAB != null && uniqueAB.length > 0) {
					uniqueAMap.put(spdxDocs[j], uniqueAB);
				}
			}
			if (!uniqueAMap.isEmpty()) {
				this.uniquePackages.put(spdxDocs[i], uniqueAMap);
			}
		}
		if (!_isPackagesEqualsNoCheck()) {
			this.differenceFound = true;
		}		
	}

	/**
	 * add all the document packages to the multi-comparer
	 * @param spdxDocument
	 * @param pkgs
	 * @param extractedLicenseIdMap 
	 * @throws SpdxCompareException 
	 */
	private void addPackageComparers(SpdxDocument spdxDocument,
			SpdxPackage[] pkgs, HashMap<SpdxDocument, HashMap<SpdxDocument, HashMap<String, String>>> extractedLicenseIdMap) throws SpdxCompareException {
		for (int i = 0; i < pkgs.length; i++) {
			SpdxPackageComparer mpc = this.packageComparers.get(pkgs[i].getName());
			if (mpc == null) {
				mpc = new SpdxPackageComparer(extractedLicenseIdMap);
				this.packageComparers.put(pkgs[i].getName(), mpc);
			}
			mpc.addDocumentPackage(spdxDocument, pkgs[i]);
		}
	}

	/**
	 * Compares two licenses from two different SPDX documents taking into account
	 * the extracted license infos who's ID's may be different between the two documents
	 * Note: The ExtracedLicenseIDMap must be initialized before this method is invoked
	 * @param doc1 Index of the SPDX document for license1
	 * @param license1
	 * @param doc2 Index of the SPDX document for license2
	 * @param license2
	 * @return true if the licenses are equivalent
	 * @throws SpdxCompareException 
	 */
	public boolean compareLicense(int doc1,
			AnyLicenseInfo license1, int doc2,
			AnyLicenseInfo license2) throws SpdxCompareException {
		this.checkDocsIndex(doc1);
		this.checkDocsIndex(doc2);
		HashMap<SpdxDocument, HashMap<String, String>> hm = this.extractedLicenseIdMap.get(this.spdxDocs[doc1]);
		if (hm == null) {
			throw(new SpdxCompareException("Compare License Error - Extracted license id map has not been initialized."));
		}
		HashMap<String, String> xlationMap = hm.get(this.spdxDocs[doc2]);
		if (xlationMap == null) {
			throw(new SpdxCompareException("Compare License Exception - Extracted license id map has not been initialized."));
		}
		return LicenseCompareHelper.isLicenseEqual(license1, license2, xlationMap);
	}

	/**
	 * @param verificationCode
	 * @param verificationCode2
	 * @return
	 */
	static boolean compareVerificationCodes(
			SpdxPackageVerificationCode verificationCode,
			SpdxPackageVerificationCode verificationCode2) {
		if (!stringsEqual(verificationCode.getValue(), verificationCode2.getValue())) {
			return false;
		}
		if (!stringArraysEqual(verificationCode.getExcludedFileNames(), 
				verificationCode2.getExcludedFileNames())) {
			return false;
		}
		return true;		
	}

	/**
	 * Compare the document level fields and sets the difference found depending on any differences
	 * @throws SpdxCompareException 
	 */
	private void compareDocumentFields() throws SpdxCompareException {
		compareDataLicense();
		compareDocumentComments();
		compareSpdxVerions();
		if (!this.dataLicenseEqual || !this.spdxVersionsEqual || !this.documentCommentsEqual) {
			this.differenceFound = true;
		}
	}

	/**
	 * @throws SpdxCompareException 
	 * 
	 */
	private void compareSpdxVerions() throws SpdxCompareException {
		String docVer1;
		docVer1 = spdxDocs[0].getSpecVersion();
		this.spdxVersionsEqual = true;
		for (int i = 1; i < spdxDocs.length; i++) {
			if (!spdxDocs[i].getSpecVersion().equals(docVer1)) {
				this.spdxVersionsEqual = false;
				break;
			}
		}
	}

	/**
	 * @throws SpdxCompareException 
	 * 
	 */
	private void compareDocumentComments() throws SpdxCompareException {
		String comment1 = this.spdxDocs[0].getComment();
		this.documentCommentsEqual = true;
		for (int i = 1; i < spdxDocs.length; i++) {
			String comment2 = this.spdxDocs[i].getComment();
			if (!stringsEqual(comment1, comment2)) {
				this.documentCommentsEqual = false;
				break;
			}
		}
	}

	/**
	 * @throws SpdxCompareException 
	 * 
	 */
	private void compareDataLicense() throws SpdxCompareException {
		try {
			AnyLicenseInfo lic1 = this.spdxDocs[0].getDataLicense();
			this.dataLicenseEqual = true;
			for (int i = 1; i < spdxDocs.length; i++) {
				if (!lic1.equals(spdxDocs[i].getDataLicense())) {
					this.dataLicenseEqual = false;
					break;
				}
			}
		} catch (InvalidSPDXAnalysisException e) {
			throw(new SpdxCompareException("SPDX analysis error during compare data license: "+e.getMessage(),e));
		}
	}

	/**
	 * Compares the extracted license infos in all documents and builds the 
	 * maps for translating IDs as well as capturing any differences between the
	 * extracted licensing information
	 * @throws InvalidSPDXAnalysisException 
	 * @throws SpdxCompareException 
	 */
	private void compareExtractedLicenseInfos() throws InvalidSPDXAnalysisException, SpdxCompareException {
		for (int i = 0; i < spdxDocs.length; i++) {
			ExtractedLicenseInfo[] extractedLicensesA = spdxDocs[i].getExtractedLicenseInfos();
			HashMap<SpdxDocument, ExtractedLicenseInfo[]> uniqueMap = 
				new HashMap<SpdxDocument, ExtractedLicenseInfo[]>();
			HashMap<SpdxDocument, SpdxLicenseDifference[]> differenceMap = 
				new HashMap<SpdxDocument, SpdxLicenseDifference[]>();
			HashMap<SpdxDocument, HashMap<String, String>> licenseIdMap = 
				new HashMap<SpdxDocument, HashMap<String, String>>();

			for (int j = 0; j < spdxDocs.length; j++) {
				if (i == j) {
					continue;	// no need to compare to ourself;
				}
				HashMap<String, String> idMap = new HashMap<String, String>();
				ArrayList<SpdxLicenseDifference> alDifferences = new ArrayList<SpdxLicenseDifference>();
				ExtractedLicenseInfo[] extractedLicensesB = spdxDocs[j].getExtractedLicenseInfos();
				ArrayList<ExtractedLicenseInfo> uniqueLicenses = new ArrayList<ExtractedLicenseInfo>();
				compareLicenses(extractedLicensesA, extractedLicensesB,
						idMap, alDifferences, uniqueLicenses);
				// unique
				if (uniqueLicenses.size() > 0) {
					uniqueMap.put(spdxDocs[j], uniqueLicenses.toArray(
							new ExtractedLicenseInfo[uniqueLicenses.size()]));
				}
				// differences
				if (alDifferences.size() > 0) {
					differenceMap.put(spdxDocs[j], alDifferences.toArray(
							new SpdxLicenseDifference[alDifferences.size()]));
				}
				// map
				licenseIdMap.put(spdxDocs[j], idMap);
			}
			if (uniqueMap.keySet().size() > 0) {
				this.uniqueExtractedLicenses.put(spdxDocs[i], uniqueMap);
			}
			if (differenceMap.keySet().size() > 0) {
				this.licenseDifferences.put(spdxDocs[i], differenceMap);
			}
			this.extractedLicenseIdMap.put(spdxDocs[i], licenseIdMap);
		}
		if (!_isExtractedLicensingInfoEqualsNoCheck()) {
			this.differenceFound = true;
		}
	}

	/**
	 * Compares two arrays of non standard licenses
	 * @param extractedLicensesA
	 * @param extractedLicensesB
	 * @param idMap Map of license IDs for licenses considered equal
	 * @param alDifferences Array list of license differences found where the license text is equivalent but other properties are different
	 * @param uniqueLicenses ArrayList if licenses found in the A but not found in B
	 */
	private void compareLicenses(ExtractedLicenseInfo[] extractedLicensesA,
			ExtractedLicenseInfo[] extractedLicensesB,
			HashMap<String, String> idMap,
			ArrayList<SpdxLicenseDifference> alDifferences,
			ArrayList<ExtractedLicenseInfo> uniqueLicenses) {
		idMap.clear();
		alDifferences.clear();
		uniqueLicenses.clear();
		for (int k = 0; k < extractedLicensesA.length; k++) {
			boolean foundMatch = false;
			boolean foundTextMatch = false;
			for (int q = 0; q < extractedLicensesB.length; q++) {
				if (LicenseCompareHelper.isLicenseTextEquivalent(extractedLicensesA[k].getExtractedText(), 
						extractedLicensesB[q].getExtractedText())) {
					foundTextMatch = true;
					if (!foundMatch) {
						idMap.put(extractedLicensesA[k].getLicenseId(), extractedLicensesB[q].getLicenseId());
						// always add to the map any matching licenses.  If more than one, add
						// the license matches where the entire license match.  This condition checks
						// to make sure we are not over-writing an exact match
					}
					if (nonTextLicenseFieldsEqual(extractedLicensesA[k], extractedLicensesB[q])) {
						foundMatch = true;
					} else {
						alDifferences.add(new SpdxLicenseDifference(extractedLicensesA[k], extractedLicensesB[q]));
					}
				}
			}
			if (!foundTextMatch) {	// we treat the licenses as equivalent if the text matches even if other fields do not match
				uniqueLicenses.add(extractedLicensesA[k]);
			}
		}
	}

	/**
	 * Compares the non-license text and non-id fields and returns true
	 * if all relevant fields are equal
	 * @param spdxNonStandardLicenseA
	 * @param spdxNonStandardLicenseB
	 * @return
	 */
	private boolean nonTextLicenseFieldsEqual(
			ExtractedLicenseInfo spdxNonStandardLicenseA,
			ExtractedLicenseInfo spdxNonStandardLicenseB) {
		
		// license name
		if (!stringsEqual(spdxNonStandardLicenseA.getName(),
				spdxNonStandardLicenseB.getName())) {
			return false;
		}

		// comment;
		if (!stringsEqual(spdxNonStandardLicenseA.getComment(),
					spdxNonStandardLicenseB.getComment())) {
			return false;
		}
		// Source URL's
		if (!stringArraysEqual(spdxNonStandardLicenseA.getSeeAlso(), spdxNonStandardLicenseB.getSeeAlso())) {
			return false;
		}
		// if we made it here, everything is equal
		return true;
	}

	/**
	 * Compares 2 arrays and returns true if the contents are equal
	 * ignoring order and trimming strings.  Nulls are also considered as equal to other nulls.
	 * @param stringsA
	 * @param stringsB
	 * @return
	 */
	static boolean stringArraysEqual(String[] stringsA, String[] stringsB) {
		
		if (stringsA == null) {
			if (stringsB != null) {
				return false;
			}
		} else {
			if (stringsB == null) {
				return false;
			}
			if (stringsA.length != stringsB.length) {
				return false;
			}
			HashSet<Integer> foundIndexes = new HashSet<Integer>();
			for (int i = 0; i < stringsA.length; i++) {
				boolean found = false;
				for (int j = 0; j < stringsB.length; j++) {
					if (!foundIndexes.contains(j) &&
							stringsEqual(stringsA[i], stringsB[j])) {
						found = true;
						foundIndexes.add(j);
						break;
					}
				}
				if (!found) {
					return false;
				}
			}
		}
		return true;
	}
	
	/**
	 * returns true if the two objects are equal considering nulls
	 * @param o1
	 * @param o2
	 * @return
	 */
	public static boolean objectsEqual(Object o1, Object o2) {
		if (o1 == null) {
			return o2 == null;
		}
		return o1.equals(o2);
	}
	
	public static boolean elementsEquivalent(RdfModelObject elementA, RdfModelObject elementB) {
		if (elementA == null) {
			return elementB == null;
		}
		return elementA.equivalent(elementB);
	}

	/**
	 * Compare two object arrays
	 * @param a1
	 * @param a2
	 * @return
	 */
	public static boolean arraysEqual(Object[] a1, Object[] a2) {
		if (a1 == null) {
			if (a2 != null) {
				return false;
			}
		} else {
			if (a2 == null) {
				return false;
			}
			if (a1.length != a2.length) {
				return false;
			}
			for (int i = 0; i < a1.length; i++) {
				boolean found = false;
				for (int j = 0; j < a2.length; j++) {
					if (objectsEqual(a1[i], a2[j])) {
						found = true;
						break;
					}
				}
				if (!found) {
					return false;
				}
			}
		}
		return true;
	}
	/**
	 * Compares two strings returning true if they are equal
	 * considering null values and trimming the strings.  Empty strings are
	 * treated as the same as null values.
	 * @param stringA
	 * @param stringB
	 * @return
	 */
	public static boolean stringsEqual(String stringA, String stringB) {
		String compA;
		String compB;
		if (stringA == null) {
			compA = "";
		} else {
			compA = stringA.trim();
		}
		if (stringB == null) {
			compB = "";
		} else {
			compB = stringB.trim();
		}
		return (compA.equals(compB));
	}
	
	/**
	 * Compares two strings including trimming the string and taking into account
	 * they may be null.  Null is considered a smaller value
	 * @param stringA
	 * @param stringB
	 * @return
	 */
	public static int compareStrings(String stringA, String stringB) {
		if (stringA == null) {
			if (stringB == null) {
				return 0;
			} else {
				return -1;
			}
		}
		if (stringB == null) {
			return 1;
		}
		return (stringA.trim().compareTo(stringB.trim()));
	}

	/**
	 * Compares the reviewers for all documents and creates the differences hasmaps related
	 * to the reviewers
	 * @throws InvalidSPDXAnalysisException 
	 * @throws SpdxCompareException 
	 */
	private void compareReviewers() throws InvalidSPDXAnalysisException, SpdxCompareException {
		// this will be a N x N comparison of all reviewer data to fill in the
		// hashmaps uniqueReviews
		for (int i = 0; i < spdxDocs.length; i++) {
			@SuppressWarnings("deprecation")
			SPDXReview[] reviewA = spdxDocs[i].getReviewers();
			HashMap<SpdxDocument, SPDXReview[]> uniqueAMap = uniqueReviews.get(spdxDocs[i]);
			if (uniqueAMap == null) {
				uniqueAMap = new HashMap<SpdxDocument, SPDXReview[]>();
				// We will put this into the hashmap at the end of this method if it is not empty
			}
			HashMap<SpdxDocument, SPDXReviewDifference[]> diffMap = this.reviewerDifferences.get(this.spdxDocs[i]);
			if (diffMap == null) {
				diffMap = new HashMap<SpdxDocument, SPDXReviewDifference[]>();
				// We will put this into the hashmap at the end of this method if it is not empty
			}
			for (int j = 0; j < spdxDocs.length; j++) {
				if (j == i) {
					continue;	// skip comparing to ourself
				}
				@SuppressWarnings("deprecation")
				SPDXReview[] reviewB = spdxDocs[j].getReviewers();
				// find any reviewers in A that are not in B
				SPDXReview[] uniqueA = findUniqueReviewers(reviewA, reviewB);
				if (uniqueA != null && uniqueA.length > 0) {
					uniqueAMap.put(spdxDocs[j], uniqueA);					
				}
				//Find any reviewers that are the same reviewer but have different dates or comments
				SPDXReviewDifference[] reviewerDifferences = findReviewerDifferences(reviewA, reviewB);
				if (reviewerDifferences != null && reviewerDifferences.length > 0) {
					diffMap.put(this.spdxDocs[j], reviewerDifferences);
				}
			}
			if (uniqueAMap.keySet().size() > 0) {
				this.uniqueReviews.put(spdxDocs[i], uniqueAMap);
			}
			if (diffMap.keySet().size() > 0) {
				this.reviewerDifferences.put(this.spdxDocs[i], diffMap);
			}
		}
		if (!this._isReviewersEqualNoCheck()) {
			this.differenceFound = true;
		}
	}

	/**
	 * Compares two arrays of SPDXReview and returns any differences found
	 * A difference is an SPDXReview with the same reviewer but a different comment and date
	 * @param reviewA
	 * @param reviewB
	 * @return
	 */
	private SPDXReviewDifference[] findReviewerDifferences(
			SPDXReview[] reviewA, SPDXReview[] reviewB) {
		//Note that we need to take into account the possibility of two SPDXReviews in the
		//same array with the same reviewer name
		ArrayList<SPDXReviewDifference> retval = new ArrayList<SPDXReviewDifference>();
		for (int i = 0; i < reviewA.length; i++) {
			boolean reviewDifferent = false;
			int differentReviewerIndex = -1;
			for (int j = 0; j < reviewB.length; j++) {
				if (reviewA[i].getReviewer().trim().equals(reviewB[j].getReviewer().trim())) {
					// reviewer name is the same
					boolean commentsEqual = reviewA[i].getComment().trim().equals(reviewB[j].getComment().trim());
					boolean datesEqual = reviewA[i].getReviewDate().equals(reviewB[j].getReviewDate());
					if (commentsEqual && datesEqual) {
						reviewDifferent = false;	// note that we may have a situation where a previous 
						// entry was found with the same reviewer name and a different comment/date
						// in that situation, we should report no difference since a matching entry was found
						break;
					} else {
						reviewDifferent = true;
						differentReviewerIndex = j;
					}
				} 
			}
			if (reviewDifferent) {
				retval.add(new SPDXReviewDifference(reviewA[i], reviewB[differentReviewerIndex]));
			}
		}
		return retval.toArray(new SPDXReviewDifference[retval.size()]);
	}

	/**
	 * Finds any reviewer names that are contained in reviewA but not in reviewB
	 * @param reviewA
	 * @param reviewB
	 * @return
	 */
	private SPDXReview[] findUniqueReviewers(SPDXReview[] reviewA,
			SPDXReview[] reviewB) {
		ArrayList<SPDXReview> retval = new ArrayList<SPDXReview>();
		for (int i = 0; i < reviewA.length; i++) {
			boolean found = false;
			for (int j = 0; j < reviewB.length; j++) {
				if (reviewA[i].getReviewer().trim().equals(reviewB[j].getReviewer().trim())) {
					found = true;
					break;
				}
			}
			if (!found) {
				retval.add(reviewA[i]);
			}
		}
		return retval.toArray(new SPDXReview[retval.size()]);
	}

	/**
	 * 
	 */
	private void clearCompareResults() {
		this.differenceFound = false;
		this.reviewerDifferences.clear();
		this.uniqueReviews.clear();
		this.licenseDifferences.clear();
		this.uniqueExtractedLicenses.clear();
		this.extractedLicenseIdMap.clear();
		this.uniqueCreators.clear();
	}

	/**
	 * @return
	 */
	public boolean isDifferenceFound() {
		return this.differenceFound;
	}

	/**
	 * @return
	 * @throws SpdxCompareException 
	 */
	public boolean isSpdxVersionEqual() throws SpdxCompareException {
		checkInProgress();
		checkDocsField();
		return this.spdxVersionsEqual;
	}

	/**
	 * checks to make sure there is not a compare in progress
	 * @throws SpdxCompareException 
	 * 
	 */
	private void checkInProgress() throws SpdxCompareException {
		if (compareInProgress) {
			throw(new SpdxCompareException("Compare in progress - can not obtain compare results until compare has completed"));
		}
	}

	/**
	 * Validates that the spdx dcouments field has been initialized
	 * @throws SpdxCompareException 
	 */
	private void checkDocsField() throws SpdxCompareException {
		if (this.spdxDocs == null) {
			throw(new SpdxCompareException("No compare has been performed"));
		}
		if (this.spdxDocs.length < 2) {
			throw(new SpdxCompareException("Insufficient documents compared - must provide at least 2 SPDX documents"));
		}
	}
	
	private void checkDocsIndex(int index) throws SpdxCompareException {
		if (this.spdxDocs == null) {
			throw(new SpdxCompareException("No compare has been performed"));
		}
		if (index < 0) {
			throw(new SpdxCompareException("Invalid index for SPDX document compare - must be greater than or equal to zero"));
		}
		if (index >= spdxDocs.length) {
			throw(new SpdxCompareException("Invalid index for SPDX document compare - SPDX document index "+String.valueOf(index)+" does not exist."));
		}
	}

	/**
	 * @param docIndex Reference to which document number - 0 is the first document parameter in compare
	 * @return
	 * @throws SpdxCompareException 
	 */
	public SpdxDocument getSpdxDoc(int docIndex) throws SpdxCompareException {
		this.checkDocsField();
		if (this.spdxDocs ==  null) {
			return null;
		}
		if (docIndex < 0) {
			return null;
		}
		if (docIndex > this.spdxDocs.length) {
			return null;
		}
		return this.spdxDocs[docIndex];
	}

	/**
	 * @return
	 * @throws SpdxCompareException 
	 */
	public boolean isDataLicenseEqual() throws SpdxCompareException {
		checkInProgress();
		checkDocsField();
		return this.dataLicenseEqual;
	}

	/**
	 * @return
	 * @throws SpdxCompareException 
	 */
	public boolean isDocumentCommentsEqual() throws SpdxCompareException {
		checkInProgress();
		checkDocsField();
		return this.documentCommentsEqual;
	}

	/**
	 * @return
	 * @throws SpdxCompareException 
	 */
	public boolean isReviewersEqual() throws SpdxCompareException {
		checkInProgress();
		checkDocsField();
		return _isReviewersEqualNoCheck();
	}
	
	
	/**
	 * @return
	 */
	private boolean _isReviewersEqualNoCheck() {
		// check for unique reviewers
		Iterator<Entry<SpdxDocument, HashMap<SpdxDocument, SPDXReview[]>>> uniqueIter =
			this.uniqueReviews.entrySet().iterator();
		while (uniqueIter.hasNext()) {
			Entry <SpdxDocument, HashMap<SpdxDocument, SPDXReview[]>> entry = uniqueIter.next();
			Iterator<Entry<SpdxDocument, SPDXReview[]>> entryIter = entry.getValue().entrySet().iterator();
			while (entryIter.hasNext()) {
				SPDXReview[] val = entryIter.next().getValue();
				if (val != null && val.length > 0) {
					return false;
				}
			}
		}
		// check differences
		Iterator<Entry<SpdxDocument, HashMap<SpdxDocument, SPDXReviewDifference[]>>> diffIter = this.reviewerDifferences.entrySet().iterator();
		while (diffIter.hasNext()) {
			Iterator<Entry<SpdxDocument, SPDXReviewDifference[]>> entryIter = diffIter.next().getValue().entrySet().iterator();
			while(entryIter.hasNext()) {
				SPDXReviewDifference[] reviewDifferences = entryIter.next().getValue();
				if (reviewDifferences != null && reviewDifferences.length > 0) {
					return false;
				}
			}
		}
		// if we got to here - they are equal
		return true;
	}
	
	private boolean _isExternalDcoumentRefsEqualsNoCheck() {
		Iterator<Entry<SpdxDocument, HashMap<SpdxDocument, ExternalDocumentRef[]>>> iter = 
				this.uniqueExternalDocumentRefs.entrySet().iterator();
		while (iter.hasNext()) {
			Iterator<ExternalDocumentRef[]> docIterator = iter.next().getValue().values().iterator();
			while (docIterator.hasNext()) {
				if (docIterator.next().length > 0) {
					return false;
				}
			}
		}
		return true;
	}


	public boolean isExternalDcoumentRefsEquals() throws SpdxCompareException {
		checkInProgress();
		checkDocsField();
		return _isExternalDcoumentRefsEqualsNoCheck();
	}

	
	public boolean isExtractedLicensingInfosEqual() throws SpdxCompareException {
		checkInProgress();
		checkDocsField();
		return _isExtractedLicensingInfoEqualsNoCheck();
	}

	/**
	 * @return
	 */
	private boolean _isExtractedLicensingInfoEqualsNoCheck() {
		// check for unique extraced license infos
		Iterator<Entry<SpdxDocument, HashMap<SpdxDocument, ExtractedLicenseInfo[]>>> uniqueIter = 
			this.uniqueExtractedLicenses.entrySet().iterator();
		while (uniqueIter.hasNext()) {
			Entry<SpdxDocument, HashMap<SpdxDocument, ExtractedLicenseInfo[]>> entry = uniqueIter.next();
			Iterator<Entry<SpdxDocument, ExtractedLicenseInfo[]>> entryIter = entry.getValue().entrySet().iterator();
			while(entryIter.hasNext()) {
				ExtractedLicenseInfo[] licenses = entryIter.next().getValue();
				if (licenses != null && licenses.length > 0) {
					return false;
				}
			}
		}
		// check differences
		Iterator<Entry<SpdxDocument, HashMap<SpdxDocument,SpdxLicenseDifference[]>>> diffIterator = this.licenseDifferences.entrySet().iterator();
		while (diffIterator.hasNext()) {
			Iterator<Entry<SpdxDocument,SpdxLicenseDifference[]>> entryIter = diffIterator.next().getValue().entrySet().iterator();
			while (entryIter.hasNext()) {
				SpdxLicenseDifference[] differences = entryIter.next().getValue();
				if (differences != null && differences.length > 0) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Get all unique reviewers in SPDX document at index 1 relative to reviewers
	 * in SPDX Document at index 2
	 * @param docindex1
	 * @param docindex2
	 * @return
	 * @throws SpdxCompareException 
	 */
	public SPDXReview[] getUniqueReviewers(int docindex1, int docindex2) throws SpdxCompareException {
		this.checkDocsField();
		this.checkInProgress();
		checkDocsIndex(docindex1);
		checkDocsIndex(docindex2);
		HashMap<SpdxDocument, SPDXReview[]> uniques = this.uniqueReviews.get(spdxDocs[docindex1]);
		if (uniques != null) {
			SPDXReview[] retval = uniques.get(spdxDocs[docindex2]);
			if (retval != null) {
				return retval;
			} else {
				return new SPDXReview[0];
			}
		} else {
			return new SPDXReview[0];
		}
	}

	/**
	 * Get all reviewer differences between two documents.  A reviewer difference is
	 * where the reviewer name is the same but the reviewer date and/or the reviewer comment
	 * is different
	 * @param docindex1
	 * @param docindex2
	 * @return
	 * @throws SpdxCompareException 
	 */
	public SPDXReviewDifference[] getReviewerDifferences(int docindex1, int docindex2) throws SpdxCompareException {
		this.checkDocsField();
		this.checkInProgress();
		checkDocsIndex(docindex1);
		checkDocsIndex(docindex2);
		HashMap<SpdxDocument, SPDXReviewDifference[]> doc1Differences =
			this.reviewerDifferences.get(spdxDocs[docindex1]);
		if (doc1Differences == null) {
			return new SPDXReviewDifference[0];
		}
		SPDXReviewDifference[] retval = doc1Differences.get(spdxDocs[docindex2]);
		if (retval == null) {
			return new SPDXReviewDifference[0];
		} 
		return retval;
	}

	/**
	 * Retrieves any unique extracted licenses fromt the first SPDX document index
	 * relative to the second - unique is determined by the license text matching
	 * @param docIndexA
	 * @param docIndexB
	 * @return
	 * @throws SpdxCompareException 
	 */
	public ExtractedLicenseInfo[] getUniqueExtractedLicenses(int docIndexA, int docIndexB) throws SpdxCompareException {
		this.checkDocsField();
		this.checkInProgress();
		checkDocsIndex(docIndexA);
		checkDocsIndex(docIndexB);
		HashMap<SpdxDocument, ExtractedLicenseInfo[]> uniques = this.uniqueExtractedLicenses.get(spdxDocs[docIndexA]);
		if (uniques != null) {
			ExtractedLicenseInfo[] retval = uniques.get(spdxDocs[docIndexB]);
			if (retval != null) {
				return retval;
			} else {
				return new ExtractedLicenseInfo[0];
			}
		} else {
			return new ExtractedLicenseInfo[0];
		}
	}

	/**
	 * Retrieves any licenses which where the text matches in both documents but
	 * other fields are different
	 * @param docIndexA
	 * @param docIndexB
	 * @return
	 * @throws SpdxCompareException 
	 */
	public SpdxLicenseDifference[] getExtractedLicenseDifferences(int docIndexA, int docIndexB) throws SpdxCompareException {
		this.checkDocsField();
		this.checkInProgress();
		checkDocsIndex(docIndexA);
		checkDocsIndex(docIndexB);
		HashMap<SpdxDocument, SpdxLicenseDifference[]> differences = this.licenseDifferences.get(spdxDocs[docIndexA]);
		if (differences != null) {
			SpdxLicenseDifference[] retval = differences.get(spdxDocs[docIndexB]);
			if (retval != null) {
				return retval;
			} else {
				return new SpdxLicenseDifference[0];
			}
		} else {
			return new SpdxLicenseDifference[0];
		}
	}

	/**
	 * @return
	 * @throws SpdxCompareException 
	 */
	public boolean isCreatorInformationEqual() throws SpdxCompareException {
		this.checkDocsField();
		this.checkInProgress();
		return this.creatorInformationEquals;
	}

	/**
	 * Returns any creators which are in the SPDX document 1 which are not in document 2
	 * @param doc1index
	 * @param doc2index
	 * @return
	 * @throws SpdxCompareException 
	 */
	public String[] getUniqueCreators(int doc1index, int doc2index) throws SpdxCompareException {
		this.checkDocsField();
		this.checkInProgress();
		HashMap<SpdxDocument, String[]> uniques = this.uniqueCreators.get(this.getSpdxDoc(doc1index));
		if (uniques == null) {
			return new String[0];
		}
		String[] retval = uniques.get(this.getSpdxDoc(doc2index));
		if (retval == null) {
			return new String[0];
		} else {
			return retval;
		}
	}

	/**
	 * @return
	 * @throws SpdxCompareException 
	 */
	public boolean isfilesEquals() throws SpdxCompareException {
		this.checkDocsField();
		this.checkInProgress();
		return this._isFilesEqualsNoCheck();
	}
	
	/**
	 * @return
	 * @throws SpdxCompareException 
	 */
	public boolean isPackagesEquals() throws SpdxCompareException {
		this.checkDocsField();
		this.checkInProgress();
		return this._isPackagesEqualsNoCheck();
	}

	/**
	 * @return
	 * @throws SpdxCompareException
	 */
	public boolean isDocumentAnnotationsEquals() throws SpdxCompareException {
		this.checkDocsField();
		this.checkInProgress();
		return _isDocumentAnnotationsEqualsNoCheck();
	}
	/**
	 * @return
	 */
	private boolean _isDocumentAnnotationsEqualsNoCheck() {
		Iterator<Entry<SpdxDocument, HashMap<SpdxDocument, Annotation[]>>> iter = 
				this.uniqueDocumentAnnotations.entrySet().iterator();
		while (iter.hasNext()) {
			Iterator<Annotation[]> docIterator = iter.next().getValue().values().iterator();
			while (docIterator.hasNext()) {
				if (docIterator.next().length > 0) {
					return false;
				}
			}
		}
		return true;
	}
	
	/**
	 * @return
	 * @throws SpdxCompareException
	 */
	public boolean isDocumentRelationshipsEquals() throws SpdxCompareException {
		this.checkDocsField();
		this.checkInProgress();
		return _isDocumentRelationshipsEqualsNoCheck();
	}
	/**
	 * @return
	 */
	private boolean _isDocumentRelationshipsEqualsNoCheck() {
		Iterator<Entry<SpdxDocument, HashMap<SpdxDocument, Relationship[]>>> iter = 
				this.uniqueDocumentRelationships.entrySet().iterator();
		while (iter.hasNext()) {
			Iterator<Relationship[]> docIterator = iter.next().getValue().values().iterator();
			while (docIterator.hasNext()) {
				if (docIterator.next().length > 0) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * @return
	 */
	private boolean _isFilesEqualsNoCheck() {
		if (!this.uniqueFiles.isEmpty()) {
			return false;
		}
		if (!this.fileDifferences.isEmpty()) {
			return false;
		}
		return true;
	}

	/**
	 * @return
	 * @throws SpdxCompareException 
	 */
	private boolean _isPackagesEqualsNoCheck() throws SpdxCompareException {
		Iterator<Entry<SpdxDocument, HashMap<SpdxDocument, SpdxPackage[]>>> iter = 
				this.uniquePackages.entrySet().iterator();
		while (iter.hasNext()) {
			Iterator<SpdxPackage[]> docIterator = iter.next().getValue().values().iterator();
			while (docIterator.hasNext()) {
				if (docIterator.next().length > 0) {
					return false;
				}
			}
		}
		Iterator<SpdxPackageComparer> diffIter = this.packageComparers.values().iterator();
		while (diffIter.hasNext()) {
			if (diffIter.next().isDifferenceFound()) {
				return false;
			}
		}
		return true;
	}
	/**
	 * Return any files which are in spdx document index 1 but not in spdx document index 2
	 * @param docindex1
	 * @param docindex2
	 * @return
	 * @throws SpdxCompareException 
	 */
	public SpdxFile[] getUniqueFiles(int docindex1, int docindex2) throws SpdxCompareException {
		this.checkDocsField();
		this.checkInProgress();
		this.checkDocsIndex(docindex1);
		this.checkDocsIndex(docindex2);
		HashMap<SpdxDocument, SpdxFile[]> uniqueMap = this.uniqueFiles.get(this.spdxDocs[docindex1]);
		if (uniqueMap == null) {
			return new SpdxFile[0];
		}
		SpdxFile[] retval = uniqueMap.get(this.spdxDocs[docindex2]);
		if (retval == null) {
			return new SpdxFile[0];
		}
		return retval;
	}

	/**
	 * Returns any file differences found between the first and second SPDX documents
	 * as specified by the document index
	 * @param docindex1
	 * @param docindex2
	 * @return
	 * @throws SpdxCompareException 
	 */
	public SpdxFileDifference[] getFileDifferences(int docindex1, int docindex2) throws SpdxCompareException {
		this.checkDocsField();
		this.checkInProgress();
		this.checkDocsIndex(docindex1);
		this.checkDocsIndex(docindex2);
		HashMap<SpdxDocument, SpdxFileDifference[]> uniqueMap = this.fileDifferences.get(this.spdxDocs[docindex1]);
		if (uniqueMap == null) {
			return new SpdxFileDifference[0];
		}
		SpdxFileDifference[] retval = uniqueMap.get(this.spdxDocs[docindex2]);
		if (retval == null) {
			return new SpdxFileDifference[0];
		}
		return retval;
	}
	
	/**
	 * Return any files which are in spdx document index 1 but not in spdx document index 2
	 * @param docindex1
	 * @param docindex2
	 * @return
	 * @throws SpdxCompareException 
	 */
	public SpdxPackage[] getUniquePackages(int docindex1, int docindex2) throws SpdxCompareException {
		this.checkDocsField();
		this.checkInProgress();
		this.checkDocsIndex(docindex1);
		this.checkDocsIndex(docindex2);
		HashMap<SpdxDocument, SpdxPackage[]> uniqueMap = this.uniquePackages.get(this.spdxDocs[docindex1]);
		if (uniqueMap == null) {
			return new SpdxPackage[0];
		}
		SpdxPackage[] retval = uniqueMap.get(this.spdxDocs[docindex2]);
		if (retval == null) {
			return new SpdxPackage[0];
		}
		return retval;
	}
	
	/**
	 * Return any external document references which are in spdx document index 1 but not in spdx document index 2
	 * @param docindex1
	 * @param docindex2
	 * @return
	 * @throws SpdxCompareException
	 */
	public ExternalDocumentRef[] getUniqueExternalDocumentRefs(int docindex1, int docindex2) throws SpdxCompareException {
		this.checkDocsField();
		this.checkInProgress();
		this.checkDocsIndex(docindex1);
		this.checkDocsIndex(docindex2);
		HashMap<SpdxDocument, ExternalDocumentRef[]> uniqueMap = this.uniqueExternalDocumentRefs.get(this.spdxDocs[docindex1]);
		if (uniqueMap == null) {
			return new ExternalDocumentRef[0];
		}
		ExternalDocumentRef[] retval = uniqueMap.get(this.spdxDocs[docindex2]);
		if (retval == null) {
			return new ExternalDocumentRef[0];
		}
		return retval;
	}

	/**
	 * Return any document annotations which are in spdx document index 1 but not in spdx document index 2
	 * @param docindex1
	 * @param docindex2
	 * @return
	 * @throws SpdxCompareException
	 */
	public Annotation[] getUniqueDocumentAnnotations(int docindex1, int docindex2) throws SpdxCompareException {
		this.checkDocsField();
		this.checkInProgress();
		this.checkDocsIndex(docindex1);
		this.checkDocsIndex(docindex2);
		HashMap<SpdxDocument, Annotation[]> uniqueMap = this.uniqueDocumentAnnotations.get(this.spdxDocs[docindex1]);
		if (uniqueMap == null) {
			return new Annotation[0];
		}
		Annotation[] retval = uniqueMap.get(this.spdxDocs[docindex2]);
		if (retval == null) {
			return new Annotation[0];
		}
		return retval;
	}
	
	/**
	 * Return any document annotations which are in spdx document index 1 but not in spdx document index 2
	 * @param docindex1
	 * @param docindex2
	 * @return
	 * @throws SpdxCompareException
	 */
	public Relationship[] getUniqueDocumentRelationship(int docindex1, int docindex2) throws SpdxCompareException {
		this.checkDocsField();
		this.checkInProgress();
		this.checkDocsIndex(docindex1);
		this.checkDocsIndex(docindex2);
		HashMap<SpdxDocument, Relationship[]> uniqueMap = this.uniqueDocumentRelationships.get(this.spdxDocs[docindex1]);
		if (uniqueMap == null) {
			return new Relationship[0];
		}
		Relationship[] retval = uniqueMap.get(this.spdxDocs[docindex2]);
		if (retval == null) {
			return new Relationship[0];
		}
		return retval;
	}
	
	/**
	 * @return Package comparers where there is at least one difference
	 * @throws SpdxCompareException 
	 */
	public SpdxPackageComparer[] getPackageDifferences() throws SpdxCompareException {
		Collection<SpdxPackageComparer> comparers = this.packageComparers.values();
		Iterator<SpdxPackageComparer> iter = comparers.iterator();
		int count = 0;
		while (iter.hasNext()) {
			if (iter.next().isDifferenceFound()) {
				count++;
			}
		}
		SpdxPackageComparer[] retval = new SpdxPackageComparer[count];		
		iter = comparers.iterator();
		int i = 0;
		while (iter.hasNext()) {
			SpdxPackageComparer comparer = iter.next();
			if (comparer.isDifferenceFound()) {
				retval[i++] = comparer;
			}
		}
		return retval;
	}
	
	/**
	 * @return all package comparers
	 */
	public SpdxPackageComparer[] getPackageComparers() {
		return this.packageComparers.values().toArray(
				new SpdxPackageComparer[this.packageComparers.values().size()]);
	}

	/**
	 * @return
	 */
	public int getNumSpdxDocs() {
		return this.spdxDocs.length;
	}

	/**
	 * @return
	 * @throws SpdxCompareException 
	 */
	public boolean isLicenseListVersionEqual() throws SpdxCompareException {
		this.checkDocsField();
		this.checkInProgress();
		return this.licenseListVersionEquals;
	}

	/**
	 * Find any SPDX checksums which are in elementsA but not in elementsB
	 * @param checksumsA
	 * @param checksumsB
	 * @return
	 */
	public static Checksum[] findUniqueChecksums(Checksum[] checksumsA,
			Checksum[] checksumsB) {
		ArrayList<Checksum> retval = new ArrayList<Checksum>();
		if (checksumsA != null) {
			for (int i = 0; i < checksumsA.length; i++) {
				if (checksumsA[i] == null) {
					continue;
				}
				boolean found = false;
				if (checksumsB != null) {
					for (int j = 0; j < checksumsB.length; j++) {
						if (checksumsA[i].equivalent(checksumsB[j])) {
							found = true;
							break;
						}
					}
				}
				if (!found) {
					retval.add(checksumsA[i]);
				}
			}
		}
		return retval.toArray(new Checksum[retval.size()]);
	}
	
	/**
	 * Find any SPDX annotations which are in annotationsA but not in annotationsB
	 * @param annotationsA
	 * @param annotationsB
	 * @return
	 */
	public static Annotation[] findUniqueAnnotations(Annotation[] annotationsA,
			Annotation[] annotationsB) {
		ArrayList<Annotation> retval = new ArrayList<Annotation>();
		if (annotationsA != null) {
			for (int i = 0; i < annotationsA.length; i++) {
				if (annotationsA[i] == null) {
					continue;
				}
				boolean found = false;
				if (annotationsB != null) {
					for (int j = 0; j < annotationsB.length; j++) {
						if (annotationsA[i].equivalent(annotationsB[j])) {
							found = true;
							break;
						}
					}
				}
				if (!found) {
					retval.add(annotationsA[i]);
				}
			}
		}
		return retval.toArray(new Annotation[retval.size()]);
	}

	/**
	 * Returns true if two arrays of SPDX elements contain equivalent elements
	 * @param elementsA
	 * @param elementsB
	 * @return
	 */
	public static boolean elementsEquivalent(RdfModelObject[] elementsA,
			RdfModelObject[] elementsB) {
		if (elementsA == null) {
			return elementsB == null;
		}
		if (elementsB == null) {
			return false;
		}
		if (elementsA.length != elementsB.length) {
			return false;
		}
		HashSet<Integer> matchedIndexes = new HashSet<Integer>();
		for (int i = 0; i < elementsA.length; i++) {
			boolean found = false;
			for (int j = 0; j < elementsB.length; j++) {
				if (!matchedIndexes.contains(j) &&
						elementsA[i].equivalent(elementsB[j])) {
					found = true;
					matchedIndexes.add(j);
					break;
				}
			}
			if (!found) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Find unique relationships that are present in relationshipsA but not relationshipsB
	 * @param relationshipsA
	 * @param relationshipsB
	 * @return
	 */
	public static Relationship[] findUniqueRelationships(
			Relationship[] relationshipsA, Relationship[] relationshipsB) {
		ArrayList<Relationship> retval = new ArrayList<Relationship>();
		if (relationshipsA == null) {
			return new Relationship[0];
		}
		for (int i = 0; i < relationshipsA.length; i++) {
			if (relationshipsA[i] == null) {
				continue;
			}
			boolean found = false;
			if (relationshipsB != null) {
				for (int j = 0; j < relationshipsB.length; j++) {
					if (relationshipsA[i].equivalent(relationshipsB[j])) {
						found = true;
						break;
					}
				}
			}
			if (!found) {
				retval.add(relationshipsA[i]);
			}
		}
		return retval.toArray(new Relationship[retval.size()]);
	}
	
	/**
	 * Find unique relationships that are present in relationshipsA but not relationshipsB
	 * @param externalDocRefsA
	 * @param externalDocRefsB
	 * @return
	 * @throws InvalidSPDXAnalysisException 
	 */
	public static ExternalDocumentRef[] findUniqueExternalDocumentRefs(
			ExternalDocumentRef[] externalDocRefsA, ExternalDocumentRef[] externalDocRefsB) throws InvalidSPDXAnalysisException {
		ArrayList<ExternalDocumentRef> retval = new ArrayList<ExternalDocumentRef>();
		if (externalDocRefsA == null) {
			return new ExternalDocumentRef[0];
		}
		for (int i = 0; i < externalDocRefsA.length; i++) {
			if (externalDocRefsA[i] == null) {
				continue;
			}
			boolean found = false;
			if (externalDocRefsB != null) {
				for (int j = 0; j < externalDocRefsB.length; j++) {
					if (compareStrings(externalDocRefsA[i].getSpdxDocumentNamespace(),
							externalDocRefsB[j].getSpdxDocumentNamespace()) == 0 &&
							elementsEquivalent(externalDocRefsA[i].getChecksum(),
									externalDocRefsB[j].getChecksum())) {
						found = true;
						break;
					}
				}
			}
			if (!found) {
				retval.add(externalDocRefsA[i]);
			}
		}
		return retval.toArray(new ExternalDocumentRef[retval.size()]);
	}

	/**
	 * @return
	 */
	public SpdxDocument[] getSpdxDocuments() {
		return this.spdxDocs;
	}

}
