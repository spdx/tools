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
import java.util.Iterator;
import java.util.Map.Entry;

import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SPDXCreatorInformation;
import org.spdx.rdfparser.SPDXDocument;
import org.spdx.rdfparser.SPDXDocument.SPDXPackage;
import org.spdx.rdfparser.license.AnyLicenseInfo;
import org.spdx.rdfparser.license.ExtractedLicenseInfo;
import org.spdx.rdfparser.license.SpdxListedLicense;
import org.spdx.rdfparser.SPDXFile;
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
	

	
	private SPDXDocument[] spdxDocs = null;
	private boolean differenceFound = false;
	private boolean compareInProgress = false;
	
	// Document level results
	private boolean spdxVersionsEqual = true;
	private boolean documentCommentsEqual = true;
	private boolean dataLicenseEqual = true;
	
	// Reviewer results
	/**
	 * Holds a map of all SPDX documents which have reviewers unique relative to other SPDX document
	 * based on the reviewer name.  The results of the map is another map of all SPDX documents in 
	 * the comparison which do not contain some of the reviewers in the key document.  See the
	 * implementation of compareReviewers for details
	 */
	private HashMap<SPDXDocument, HashMap<SPDXDocument, SPDXReview[]>> uniqueReviews = 
		new HashMap<SPDXDocument, HashMap<SPDXDocument, SPDXReview[]>>();
	/**
	 * Holds a map of any SPDX documents which have reviewer differenes.  A reviewer difference
	 * is an SPDXReview with the same reviewer name but a different reviewer date or comment
	 */
	private HashMap<SPDXDocument, HashMap<SPDXDocument, SPDXReviewDifference[]>> reviewerDifferences = 
		new HashMap<SPDXDocument, HashMap<SPDXDocument, SPDXReviewDifference[]>>();
	
	// Extracted Licensing Info results
	/**
	 * Holds a map of all SPDX documents which have extracted license infos unique relative to other SPDX document
	 * based on the reviewer name.  The results of the map is another map of all SPDX documents in 
	 * the comparison which do not contain some of the reviewers in the key document.  See the
	 * implementation of compareReviewers for details
	 */
	private HashMap<SPDXDocument, HashMap<SPDXDocument, ExtractedLicenseInfo[]>> uniqueExtractedLicenses = 
		new HashMap<SPDXDocument, HashMap<SPDXDocument, ExtractedLicenseInfo[]>>();
	/**
	 * Map of any SPDX documents that have extraced license infos with equivalent text but different comments, id's or other fields
	 */
	private HashMap<SPDXDocument, HashMap<SPDXDocument, SpdxLicenseDifference[]>> licenseDifferences = 
		new HashMap<SPDXDocument, HashMap<SPDXDocument, SpdxLicenseDifference[]>>();
	/**
	 * Maps the license ID's for the extracted license infos of the documents being compared.  License ID's are mapped based on the text
	 * being equivalent 
	 */
	private HashMap<SPDXDocument, HashMap<SPDXDocument, HashMap<String, String>>> extractedLicenseIdMap = 
		new HashMap<SPDXDocument, HashMap<SPDXDocument, HashMap<String, String>>>();
	private boolean packagesEquals;
	private boolean packageNamesEquals;
	private boolean packageVersionsEquals;
	private boolean packageFilenamesEquals;
	private boolean packageSuppliersEquals;
	private boolean packageDownloadLocationsEquals;
	private boolean packageVerificationCodeesEquals;
	private boolean packageChecksumsEquals;
	private boolean packageSourceInfosEquals;
	private boolean concludedLicennsesEquals;
	private boolean licenseInfoFromFilesEquals;
	private boolean declaredLicennsesEquals;
	private boolean licenseCommentsEquals;
	private boolean packageCopyrightsEquals;
	private boolean packageSummaryEquals;
	private boolean packageDescriptionsEquals;
	private boolean packageOriginatorsEqual;
	private boolean creatorInformationEquals;
	private HashMap<SPDXDocument, HashMap<SPDXDocument, String[]>> uniqueCreators = 
		new HashMap<SPDXDocument, HashMap<SPDXDocument, String[]>>();
	
	// file compare results
	/**
	 * Holds a map of all SPDX documents which have files unique relative to other SPDX document
	 * based on the file name.  The results of the map is another map of all SPDX documents in 
	 * the comparison which do not contain some of the files in the key document.  See the
	 * implementation of compareFiles for details
	 */
	private HashMap<SPDXDocument, HashMap<SPDXDocument, SPDXFile[]>> uniqueFiles = 
		new HashMap<SPDXDocument, HashMap<SPDXDocument, SPDXFile[]>>();
	
	/**
	 * Holds a map of any SPDX documents which have file differences.  A file difference
	 * is an SPDXReview with the same filename name but a different file property
	 */
	private HashMap<SPDXDocument, HashMap<SPDXDocument, SpdxFileDifference[]>> fileDifferences = 
		new HashMap<SPDXDocument, HashMap<SPDXDocument, SpdxFileDifference[]>>();
	
	class FileByNameComparator implements Comparator<SPDXFile> {

		/* (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(SPDXFile arg0, SPDXFile arg1) {
			return arg0.getName().compareTo(arg1.getName());
		}
		
	}
	private Comparator<SPDXFile> fileSortByNameComaprator = new FileByNameComparator();
	private SpdxFileComparer fileComparer = new SpdxFileComparer();
	private boolean packageHomePagesEquals;
	private boolean licenseListVersionEquals;
	
	public SpdxComparer() {
		
	}
	
	/**
	 * Compares 2 SPDX documents
	 * @param doc1
	 * @param doc2
	 * @throws InvalidSPDXAnalysisException
	 * @throws SpdxCompareException
	 */
	public synchronized void compare(SPDXDocument doc1, SPDXDocument doc2) throws InvalidSPDXAnalysisException, SpdxCompareException {
		compare(new SPDXDocument[] {doc1, doc2});
	}
	
	/**
	 * Compares multiple SPDX documents
	 * @param compareDocs
	 * @throws SpdxCompareException 
	 * @throws InvalidSPDXAnalysisException 
	 */
	public void compare(SPDXDocument[] compareDocs) throws InvalidSPDXAnalysisException, SpdxCompareException {
		//TODO: Add a monitor function which allows for cancel
		clearCompareResults();
		this.spdxDocs = compareDocs;
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
		compareInProgress = false;	
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
			SPDXFile[] filesA = spdxDocs[i].getSpdxPackage().getFiles();
			// note - the file arrays MUST be sorted for the comparator methods to work
			Arrays.sort(filesA, fileSortByNameComaprator);
			HashMap<SPDXDocument, SPDXFile[]> uniqueAMap = 
				this.uniqueFiles.get(spdxDocs[i]);
			if (uniqueAMap == null) {
				uniqueAMap = new HashMap<SPDXDocument, SPDXFile[]>();
			}
			// this map will be added to uniqueFiles at the end if we find anything
			HashMap<SPDXDocument, SpdxFileDifference[]> diffMap = 
				this.fileDifferences.get(spdxDocs[i]);
			if (diffMap == null) {
				diffMap = new HashMap<SPDXDocument, SpdxFileDifference[]>();
			}
			for (int j = 0; j < spdxDocs.length; j++) {
				if (j == i) {
					continue;
				}
				SPDXFile[] filesB = spdxDocs[j].getSpdxPackage().getFiles();
				//Note that the files arrays must be sorted for the find methods to work
				Arrays.sort(filesB, fileSortByNameComaprator);
				SPDXFile[] uniqueAB = findUniqueFiles(filesA, filesB);
				if (uniqueAB != null && uniqueAB.length > 0) {
					uniqueAMap.put(spdxDocs[j], uniqueAB);
				}
				HashMap<String, String> licenseIdMap = this.extractedLicenseIdMap.get(spdxDocs[i]).get(spdxDocs[j]);
				SpdxFileDifference[] differences = findFileDifferences(filesA, filesB, licenseIdMap);
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
	 * Returns an array of files differences between A and B where the names
	 * are the same, but one or more properties are different for that file
	 * @param filesA
	 * @param filesB
	 * @return
	 * @throws SpdxCompareException 
	 */
	private SpdxFileDifference[] findFileDifferences(SPDXFile[] filesA,
			SPDXFile[] filesB, HashMap<String, String> licenseIdXlationMap) throws SpdxCompareException {
		ArrayList<SpdxFileDifference> alRetval = new ArrayList<SpdxFileDifference>();
		int aIndex = 0;
		int bIndex = 0;
		while (aIndex < filesA.length && bIndex < filesB.length) {
			int compare = filesA[aIndex].getName().compareTo(filesB[bIndex].getName());
			if (compare == 0) {
				fileComparer.compare(filesA[aIndex], filesB[bIndex], licenseIdXlationMap);
				if (fileComparer.isDifferenceFound()) {
					alRetval.add(fileComparer.getFileDifference());
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
	 * finds any files in A that are not in B.  NOTE: The arrays must be sorted by file name
	 * @param filesA
	 * @param filesB
	 * @return
	 */
	private SPDXFile[] findUniqueFiles(SPDXFile[] filesA, SPDXFile[] filesB) {
		int bIndex = 0;
		int aIndex = 0;
		ArrayList<SPDXFile> alRetval = new ArrayList<SPDXFile>();
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
		SPDXFile[] retval = alRetval.toArray(new SPDXFile[alRetval.size()]);
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
			SPDXCreatorInformation creatorInfoA = spdxDocs[i].getCreatorInfo();
			String[] creatorsA = creatorInfoA.getCreators();
			HashMap<SPDXDocument, String[]> uniqueAMap = uniqueCreators.get(spdxDocs[i]);
			if (uniqueAMap == null) {
				uniqueAMap = new HashMap<SPDXDocument, String[]>();
				// We will put this into the hashmap at the end of this method if it is not empty
			}
			for (int j = 0; j < spdxDocs.length; j++) {
				if (j == i) {
					continue;	// skip comparing to ourself
				}
				SPDXCreatorInformation creatorInfoB = spdxDocs[j].getCreatorInfo();
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
		this.packagesEquals = true;
		this.packageNamesEquals = true;
		this.packageVersionsEquals = true;
		this.packageFilenamesEquals = true;
		this.packageSuppliersEquals = true;
		this.packageOriginatorsEqual = true;
		this.packageDownloadLocationsEquals = true;
		this.packageVerificationCodeesEquals = true;
		this.packageChecksumsEquals = true;
		this.packageSourceInfosEquals = true;
		this.concludedLicennsesEquals = true;
		this.licenseInfoFromFilesEquals = true;
		this.declaredLicennsesEquals = true;
		this.licenseCommentsEquals = true;
		this.packageCopyrightsEquals = true;
		this.packageSummaryEquals = true;
		this.packageDescriptionsEquals = true;
		this.packageHomePagesEquals = true;
		
		if (this.spdxDocs == null || this.spdxDocs.length < 1) {
			return;
		}
		SPDXPackage pkg1 = null;
		SPDXDocument doc1 = this.spdxDocs[0];
		try {
			pkg1 = doc1.getSpdxPackage();
		} catch (InvalidSPDXAnalysisException e) {
			throw(new SpdxCompareException("Error getting SPDX package: "+e.getMessage(), e));
		}
		if (pkg1 == null) {
			throw(new SpdxCompareException("Invalid SPDX Document - no SPDX package"));
		}
		for (int i = 1; i < this.spdxDocs.length; i++) {
			SPDXDocument doc2 = this.spdxDocs[i];
			SPDXPackage pkg2 = null;
			try {
				pkg2 = doc2.getSpdxPackage();
			} catch (InvalidSPDXAnalysisException e) {
				throw(new SpdxCompareException("Invalid SPDX Document - no SPDX package"));
			}
			if (pkg2 == null) {
				throw(new SpdxCompareException("Invalid SPDX Document - no SPDX package"));
			}
			// package name
			try {
				if (!stringsEqual(pkg1.getDeclaredName(), pkg2.getDeclaredName())) {
					this.packageNamesEquals = false;
					this.packagesEquals = false;
					this.differenceFound = true;
				}
			} catch (InvalidSPDXAnalysisException e) {
				throw(new SpdxCompareException("SPDX error getting package names: "+e.getMessage(),e));
			}
			// package version
			try {
				if (!stringsEqual(pkg1.getVersionInfo(), pkg2.getVersionInfo())) {
					this.packageVersionsEquals = false;
					this.packagesEquals = false;
					this.differenceFound = true;
				}
			} catch (InvalidSPDXAnalysisException e) {
				throw(new SpdxCompareException("SPDX error getting package versions: "+e.getMessage(),e));
			}
			// package file name
			try {
				if (!stringsEqual(pkg1.getFileName(), pkg2.getFileName())) {
					this.packageFilenamesEquals = false;
					this.packagesEquals = false;
					this.differenceFound = true;
				}
			} catch (InvalidSPDXAnalysisException e) {
				throw(new SpdxCompareException("SPDX error getting package file names: "+e.getMessage(),e));
			}
			// package supplier
			try {
				if (!stringsEqual(pkg1.getSupplier(), pkg2.getSupplier())) {
					this.packageSuppliersEquals = false;
					this.packagesEquals = false;
					this.differenceFound = true;
				}
			} catch (InvalidSPDXAnalysisException e) {
				throw(new SpdxCompareException("SPDX error getting package suppliers: "+e.getMessage(),e));
			}
			// package originator
			try {
				if (!stringsEqual(pkg1.getOriginator(), pkg2.getOriginator())) {
					this.packageOriginatorsEqual = false;
					this.packagesEquals = false;
					this.differenceFound = true;
				}
			} catch (InvalidSPDXAnalysisException e) {
				throw(new SpdxCompareException("SPDX error getting package originators: "+e.getMessage(),e));
			}
			// package download location
			try {
				if (!stringsEqual(pkg1.getDownloadUrl(), pkg2.getDownloadUrl())) {
					this.packageDownloadLocationsEquals = false;
					this.packagesEquals = false;
					this.differenceFound = true;
				}
			} catch (InvalidSPDXAnalysisException e) {
				throw(new SpdxCompareException("SPDX error getting package download locations: "+e.getMessage(),e));
			}
			// package home pages
			try {
				if (!stringsEqual(pkg1.getHomePage(), pkg2.getHomePage())) {
					this.packageHomePagesEquals = false;
					this.packagesEquals = false;
					this.differenceFound = true;
				}
			} catch (InvalidSPDXAnalysisException e) {
				throw(new SpdxCompareException("SPDX error getting package home page: "+e.getMessage(),e));
			}
			// package verification code
			try {
				if (!compareVerificationCodes(pkg1.getVerificationCode(), pkg2.getVerificationCode())) {
					this.packageVerificationCodeesEquals = false;
					this.packagesEquals = false;
					this.differenceFound = true;
				}
			} catch (InvalidSPDXAnalysisException e) {
				throw(new SpdxCompareException("SPDX error getting package verification codes: "+e.getMessage(),e));
			}
			// package checksum
			try {
				if (!stringsEqual(pkg1.getSha1(), pkg2.getSha1())) {
					this.packageChecksumsEquals = false;
					this.packagesEquals = false;
					this.differenceFound = true;
				}
			} catch (InvalidSPDXAnalysisException e) {
				throw(new SpdxCompareException("SPDX error getting package checksums: "+e.getMessage(),e));
			}
			// source information
			try {
				if (!stringsEqual(pkg1.getSourceInfo(), pkg2.getSourceInfo())) {
					this.packageSourceInfosEquals = false;
					this.packagesEquals = false;
					this.differenceFound = true;
				}
			} catch (InvalidSPDXAnalysisException e) {
				throw(new SpdxCompareException("SPDX error getting package source information: "+e.getMessage(),e));
			}
			// concluded license
			try {
				if (!compareLicense(0, pkg1.getConcludedLicenses(), i,
						pkg2.getConcludedLicenses())) {
					this.concludedLicennsesEquals = false;
					this.packagesEquals = false;
					this.differenceFound = true;
				}
			} catch (InvalidSPDXAnalysisException e) {
				throw(new SpdxCompareException("SPDX error getting concluded license: "+e.getMessage(),e));
			}
			// all license information from files
			try {
				if (!compareLicenseInfoFromFiles(0, pkg1.getLicenseInfoFromFiles(),
						i, pkg2.getLicenseInfoFromFiles())) {					
					this.licenseInfoFromFilesEquals = false;
					this.packagesEquals = false;
					this.differenceFound = true;
				}
			} catch (InvalidSPDXAnalysisException e) {
				throw(new SpdxCompareException("SPDX error getting license info from files: "+e.getMessage(),e));
			}
			// declared license
			try {
				if (!compareLicense(0, pkg1.getDeclaredLicense(), i,
						pkg2.getDeclaredLicense())) {
					this.declaredLicennsesEquals = false;
					this.packagesEquals = false;
					this.differenceFound = true;
				}
			} catch (InvalidSPDXAnalysisException e) {
				throw(new SpdxCompareException("SPDX error getting declared license: "+e.getMessage(),e));
			}
			// comments on license
			try {
				if (!stringsEqual(pkg1.getLicenseComment(), pkg2.getLicenseComment())) {
					this.licenseCommentsEquals = false;
					this.packagesEquals = false;
					this.differenceFound = true;
				}
			} catch (InvalidSPDXAnalysisException e) {
				throw(new SpdxCompareException("SPDX error getting license comments: "+e.getMessage(),e));
			}
			// copyright text
			try {
				if (!stringsEqual(pkg1.getDeclaredCopyright(), pkg2.getDeclaredCopyright())) {
					this.packageCopyrightsEquals = false;
					this.packagesEquals = false;
					this.differenceFound = true;
				}
			} catch (InvalidSPDXAnalysisException e) {
				throw(new SpdxCompareException("SPDX error getting package copyrights: "+e.getMessage(),e));
			}
			// package summary description
			try {
				if (!stringsEqual(pkg1.getShortDescription(), pkg2.getShortDescription())) {
					this.packageSummaryEquals = false;
					this.packagesEquals = false;
					this.differenceFound = true;
				}
			} catch (InvalidSPDXAnalysisException e) {
				throw(new SpdxCompareException("SPDX error getting package summary descriptions: "+e.getMessage(),e));
			}
			// package detailed description
			try {
				if (!stringsEqual(pkg1.getDescription(), pkg2.getDescription())) {
					this.packageDescriptionsEquals = false;
					this.packagesEquals = false;
					this.differenceFound = true;
				}
			} catch (InvalidSPDXAnalysisException e) {
				throw(new SpdxCompareException("SPDX error getting package descriptions: "+e.getMessage(),e));
			}
		}

		
	}

	/**
	 * Compare the licenses from files from two different SPDX documents taking into account
	 * the extracted license infos who's ID's may be different between the two documents
	 * Note: The ExtracedLicenseIDMap must be initialized before this method is invoked
	 * @param doc1 Index of the SPDX document for license1
	 * @param licenseInfoFromFiles
	 * @param doc2
	 * @param licenseInfoFromFiles2
	 * @return
	 * @throws SpdxCompareException 
	 */
	private boolean compareLicenseInfoFromFiles(int doc1,
			AnyLicenseInfo[] licenseInfoFromFiles, int doc2,
			AnyLicenseInfo[] licenseInfoFromFiles2) throws SpdxCompareException {
		//Note that the license order need not be the same
		this.checkDocsIndex(doc1);
		this.checkDocsIndex(doc2);
		if (licenseInfoFromFiles.length != licenseInfoFromFiles2.length) {
			return false;
		}
		HashMap<SPDXDocument, HashMap<String, String>> hm = this.extractedLicenseIdMap.get(this.spdxDocs[doc1]);
		if (hm == null) {
			throw(new SpdxCompareException("Compare License Error - Extracted license id map has not been initialized."));
		}
		HashMap<String, String> xlationMap = hm.get(this.spdxDocs[doc2]);
		if (xlationMap == null) {
			throw(new SpdxCompareException("Compare License Exception - Extracted license id map has not been initialized."));
		}
		for (int i = 0; i < licenseInfoFromFiles.length; i++) {
			boolean found = false;
			for (int j = 0; j < licenseInfoFromFiles2.length;j++) {
				if (LicenseCompareHelper.isLicenseEqual(licenseInfoFromFiles[i], 
						licenseInfoFromFiles2[j], xlationMap))  {
					found = true;
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
		HashMap<SPDXDocument, HashMap<String, String>> hm = this.extractedLicenseIdMap.get(this.spdxDocs[doc1]);
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
	private boolean compareVerificationCodes(
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
		try {
			docVer1 = spdxDocs[0].getSpdxVersion();
		} catch (InvalidSPDXAnalysisException e) {
			throw(new SpdxCompareException("SPDX analysis error during compare SPDX Version: "+e.getMessage(),e));
		}
		this.spdxVersionsEqual = true;
		for (int i = 1; i < spdxDocs.length; i++) {
			try {
				if (!spdxDocs[i].getSpdxVersion().equals(docVer1)) {
					this.spdxVersionsEqual = false;
					break;
				}
			} catch (InvalidSPDXAnalysisException e) {
				throw(new SpdxCompareException("SPDX analysis error during compare: "+e.getMessage(),e));
			}
		}
	}

	/**
	 * @throws SpdxCompareException 
	 * 
	 */
	private void compareDocumentComments() throws SpdxCompareException {
		try {
			String comment1 = this.spdxDocs[0].getDocumentComment();
			this.documentCommentsEqual = true;
			for (int i = 1; i < spdxDocs.length; i++) {
				String comment2 = this.spdxDocs[i].getDocumentComment();
				if (!stringsEqual(comment1, comment2)) {
					this.documentCommentsEqual = false;
					break;
				}
			}
		} catch (InvalidSPDXAnalysisException e) {
			throw(new SpdxCompareException("SPDX analysis error during compare document comments: "+e.getMessage(),e));
		}
	}

	/**
	 * @throws SpdxCompareException 
	 * 
	 */
	private void compareDataLicense() throws SpdxCompareException {
		try {
			SpdxListedLicense lic1 = this.spdxDocs[0].getDataLicense();
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
			HashMap<SPDXDocument, ExtractedLicenseInfo[]> uniqueMap = 
				new HashMap<SPDXDocument, ExtractedLicenseInfo[]>();
			HashMap<SPDXDocument, SpdxLicenseDifference[]> differenceMap = 
				new HashMap<SPDXDocument, SpdxLicenseDifference[]>();
			HashMap<SPDXDocument, HashMap<String, String>> licenseIdMap = 
				new HashMap<SPDXDocument, HashMap<String, String>>();

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
			for (int i = 0; i < stringsA.length; i++) {
				boolean found = false;
				for (int j = 0; j < stringsB.length; j++) {
					if (stringsEqual(stringsA[i], stringsB[j])) {
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
			return -1;
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
			SPDXReview[] reviewA = spdxDocs[i].getReviewers();
			HashMap<SPDXDocument, SPDXReview[]> uniqueAMap = uniqueReviews.get(spdxDocs[i]);
			if (uniqueAMap == null) {
				uniqueAMap = new HashMap<SPDXDocument, SPDXReview[]>();
				// We will put this into the hashmap at the end of this method if it is not empty
			}
			HashMap<SPDXDocument, SPDXReviewDifference[]> diffMap = this.reviewerDifferences.get(this.spdxDocs[i]);
			if (diffMap == null) {
				diffMap = new HashMap<SPDXDocument, SPDXReviewDifference[]>();
				// We will put this into the hashmap at the end of this method if it is not empty
			}
			for (int j = 0; j < spdxDocs.length; j++) {
				if (j == i) {
					continue;	// skip comparing to ourself
				}
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
	public SPDXDocument getSpdxDoc(int docIndex) throws SpdxCompareException {
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
		Iterator<Entry<SPDXDocument, HashMap<SPDXDocument, SPDXReview[]>>> uniqueIter =
			this.uniqueReviews.entrySet().iterator();
		while (uniqueIter.hasNext()) {
			Entry <SPDXDocument, HashMap<SPDXDocument, SPDXReview[]>> entry = uniqueIter.next();
			Iterator<Entry<SPDXDocument, SPDXReview[]>> entryIter = entry.getValue().entrySet().iterator();
			while (entryIter.hasNext()) {
				SPDXReview[] val = entryIter.next().getValue();
				if (val != null && val.length > 0) {
					return false;
				}
			}
		}
		// check differences
		Iterator<Entry<SPDXDocument, HashMap<SPDXDocument, SPDXReviewDifference[]>>> diffIter = this.reviewerDifferences.entrySet().iterator();
		while (diffIter.hasNext()) {
			Iterator<Entry<SPDXDocument, SPDXReviewDifference[]>> entryIter = diffIter.next().getValue().entrySet().iterator();
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
		Iterator<Entry<SPDXDocument, HashMap<SPDXDocument, ExtractedLicenseInfo[]>>> uniqueIter = 
			this.uniqueExtractedLicenses.entrySet().iterator();
		while (uniqueIter.hasNext()) {
			Entry<SPDXDocument, HashMap<SPDXDocument, ExtractedLicenseInfo[]>> entry = uniqueIter.next();
			Iterator<Entry<SPDXDocument, ExtractedLicenseInfo[]>> entryIter = entry.getValue().entrySet().iterator();
			while(entryIter.hasNext()) {
				ExtractedLicenseInfo[] licenses = entryIter.next().getValue();
				if (licenses != null && licenses.length > 0) {
					return false;
				}
			}
		}
		// check differences
		Iterator<Entry<SPDXDocument, HashMap<SPDXDocument,SpdxLicenseDifference[]>>> diffIterator = this.licenseDifferences.entrySet().iterator();
		while (diffIterator.hasNext()) {
			Iterator<Entry<SPDXDocument,SpdxLicenseDifference[]>> entryIter = diffIterator.next().getValue().entrySet().iterator();
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
		HashMap<SPDXDocument, SPDXReview[]> uniques = this.uniqueReviews.get(spdxDocs[docindex1]);
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
		HashMap<SPDXDocument, SPDXReviewDifference[]> doc1Differences =
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
		HashMap<SPDXDocument, ExtractedLicenseInfo[]> uniques = this.uniqueExtractedLicenses.get(spdxDocs[docIndexA]);
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
		HashMap<SPDXDocument, SpdxLicenseDifference[]> differences = this.licenseDifferences.get(spdxDocs[docIndexA]);
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
	public boolean isPackageEqual() throws SpdxCompareException {
		this.checkDocsField();
		this.checkInProgress();
		return this.packagesEquals;
	}

	/**
	 * @return
	 * @throws SpdxCompareException 
	 */
	public boolean isPackageNamesEqual() throws SpdxCompareException {
		this.checkDocsField();
		this.checkInProgress();
		return this.packageNamesEquals;
	}
	
	/**
	 * @return
	 * @throws SpdxCompareException 
	 */
	public boolean isPackageVersionsEqual() throws SpdxCompareException {
		this.checkDocsField();
		this.checkInProgress();
		return this.packageVersionsEquals;
	}

	/**
	 * @return
	 * @throws SpdxCompareException 
	 */
	public boolean isPackageLicenseInfoFromFilesEqual() throws SpdxCompareException {
		this.checkDocsField();
		this.checkInProgress();
		return this.licenseInfoFromFilesEquals;

	}

	/**
	 * @return
	 */
	public boolean isPackageFileNamesEqual() throws SpdxCompareException  {
		this.checkDocsField();
		this.checkInProgress();
		return this.packageFilenamesEquals;
	}

	/**
	 * @return
	 */
	public boolean isPackageSuppliersEqual() throws SpdxCompareException  {
		this.checkDocsField();
		this.checkInProgress();
		return this.packageSuppliersEquals;
	}

	/**
	 * @return
	 */
	public boolean isPackageOriginatorsEqual() throws SpdxCompareException  {
		this.checkDocsField();
		this.checkInProgress();
		return this.packageOriginatorsEqual;
	}

	/**
	 * @return
	 */
	public boolean isPackageDownloadLocationsEqual() throws SpdxCompareException  {
		this.checkDocsField();
		this.checkInProgress();
		return this.packageDownloadLocationsEquals;
	}

	/**
	 * @return
	 */
	public boolean isPackageVerificationCodesEqual()  throws SpdxCompareException {
		this.checkDocsField();
		this.checkInProgress();
		return this.packageVerificationCodeesEquals;
	}

	/**
	 * @return
	 */
	public boolean isPackageChecksumsEqual() throws SpdxCompareException  {
		this.checkDocsField();
		this.checkInProgress();
		return this.packageChecksumsEquals;
	}

	/**
	 * @return
	 */
	public boolean isSourceInformationEqual() throws SpdxCompareException  {
		this.checkDocsField();
		this.checkInProgress();
		return this.packageSourceInfosEquals;
	}

	/**
	 * @return
	 */
	public boolean isPackageDeclaredLicensesEqual() throws SpdxCompareException  {
		this.checkDocsField();
		this.checkInProgress();
		return this.declaredLicennsesEquals;
	}

	/**
	 * @return
	 */
	public boolean isPackageConcludedLicensesEqual()  throws SpdxCompareException {
		this.checkDocsField();
		this.checkInProgress();
		return this.concludedLicennsesEquals;
	}

	/**
	 * @return
	 */
	public boolean isLicenseCommentsEqual() throws SpdxCompareException  {
		this.checkDocsField();
		this.checkInProgress();
		return this.licenseCommentsEquals;
	}

	/**
	 * @return
	 */
	public boolean isCopyrightTextsEqual() throws SpdxCompareException  {
		this.checkDocsField();
		this.checkInProgress();
		return this.packageCopyrightsEquals;
	}

	/**
	 * @return
	 */
	public boolean isPackageSummariesEqual() throws SpdxCompareException  {
		this.checkDocsField();
		this.checkInProgress();
		return this.packageSummaryEquals;
	}

	/**
	 * @return
	 */
	public boolean isPackageDescriptionsEqual()  throws SpdxCompareException {
		this.checkDocsField();
		this.checkInProgress();
		return this.packageDescriptionsEquals;
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
		HashMap<SPDXDocument, String[]> uniques = this.uniqueCreators.get(this.getSpdxDoc(doc1index));
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
	 * Return any files which are in spdx document index 1 but not in spdx document index 2
	 * @param docindex1
	 * @param docindex2
	 * @return
	 * @throws SpdxCompareException 
	 */
	public SPDXFile[] getUniqueFiles(int docindex1, int docindex2) throws SpdxCompareException {
		this.checkDocsField();
		this.checkInProgress();
		this.checkDocsIndex(docindex1);
		this.checkDocsIndex(docindex2);
		HashMap<SPDXDocument, SPDXFile[]> uniqueMap = this.uniqueFiles.get(this.spdxDocs[docindex1]);
		if (uniqueMap == null) {
			return new SPDXFile[0];
		}
		SPDXFile[] retval = uniqueMap.get(this.spdxDocs[docindex2]);
		if (retval == null) {
			return new SPDXFile[0];
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
		HashMap<SPDXDocument, SpdxFileDifference[]> uniqueMap = this.fileDifferences.get(this.spdxDocs[docindex1]);
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
	 * @return
	 */
	public int getNumSpdxDocs() {
		return this.spdxDocs.length;
	}

	/**
	 * @return
	 * @throws SpdxCompareException 
	 */
	public boolean ispackageHomePagesEqual() throws SpdxCompareException {
		this.checkDocsField();
		this.checkInProgress();
		return this.packageHomePagesEquals;
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
}
