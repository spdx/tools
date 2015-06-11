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
import org.spdx.rdfparser.license.AnyLicenseInfo;
import org.spdx.rdfparser.model.Annotation;
import org.spdx.rdfparser.model.Checksum;
import org.spdx.rdfparser.model.Relationship;
import org.spdx.rdfparser.model.SpdxDocument;
import org.spdx.rdfparser.model.SpdxFile;
import org.spdx.rdfparser.model.DoapProject;
import org.spdx.rdfparser.model.SpdxItem;


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
	private boolean artifactOfEquals = true;
	private boolean fileDependenciesEquals = true;
	private boolean contributorsEquals = true;
	private boolean noticeTextEquals = true;

	/**
	 * Map of artfifactOfs found in one document but not another
	 */
	HashMap<SpdxDocument, HashMap<SpdxDocument, DoapProject[]>> uniqueArtifactOfs = 
			new HashMap<SpdxDocument, HashMap<SpdxDocument, DoapProject[]>>();

	/**
	 *  Map of checksums found in one document but not another
	 */
	private HashMap<SpdxDocument, HashMap<SpdxDocument, Checksum[]>> uniqueChecksums =
			new HashMap<SpdxDocument, HashMap<SpdxDocument, Checksum[]>>();

	
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
	private boolean checksumsEquals = true;
	private boolean typesEquals = true;

	
	public SpdxFileComparer(HashMap<SpdxDocument, HashMap<SpdxDocument, HashMap<String, String>>> extractedLicenseIdMap) {
		super(extractedLicenseIdMap);
	}
	
	/**
	 * Add a file to the comparer and compare to the existing files
	 * @param spdxDocument document containing the file
	 * @param spdxFile
	 * @throws SpdxCompareException
	 */
	@SuppressWarnings("deprecation")
	public void addDocumentFile(SpdxDocument spdxDocument,
			SpdxFile spdxFile) throws SpdxCompareException {
		checkInProgress();
		inProgress = true;
		Iterator<Entry<SpdxDocument, SpdxItem>> iter = this.documentItem.entrySet().iterator();
		Entry<SpdxDocument, SpdxItem> entry;
		SpdxFile filesB = null;
		while (iter.hasNext() && filesB == null) {
			entry = iter.next();
			if (entry.getValue() instanceof SpdxFile) {
				filesB = (SpdxFile)entry.getValue();
			}
		}
		if (filesB != null) {
			// Artifact Of
			compareNewArtifactOf(spdxDocument, spdxFile.getArtifactOf());
			// Checksums
			compareNewFileChecksums(spdxDocument, spdxFile.getChecksums());
			// Type
			if (!SpdxComparer.arraysEqual(spdxFile.getFileTypes(), filesB.getFileTypes())) {
				this.typesEquals = false;
				this.differenceFound = true;
			}
			// contributors
			if (!SpdxComparer.stringArraysEqual(spdxFile.getFileContributors(), filesB.getFileContributors())) {
				this.contributorsEquals = false;
				this.differenceFound = true;
			}
			// notice text
			if (!SpdxComparer.stringsEqual(spdxFile.getNoticeText(), filesB.getNoticeText())) {
				this.noticeTextEquals = false;
				this.differenceFound = true;
			}
			// file dependencies
			if (!fileNamesEquals(spdxFile.getFileDependencies(), filesB.getFileDependencies())) {
				this.fileDependenciesEquals = false;
				this.differenceFound = true;
			}
		}

		super.addDocumentItem(spdxDocument, spdxFile);
		inProgress = false;
	}
	
	/**
	 * Compare the checks for a new file being added to the existing
	 * package checksums filling in the unique checksums map
	 * @param spdxDocument
	 * @param checksums
	 * @throws SpdxCompareException 
	 */
	private void compareNewFileChecksums(SpdxDocument spdxDocument,
			Checksum[] checksums) throws SpdxCompareException {

		HashMap<SpdxDocument, Checksum[]> docUniqueChecksums = 
				new HashMap<SpdxDocument, Checksum[]>();
		this.uniqueChecksums.put(spdxDocument, docUniqueChecksums);
		Iterator<Entry<SpdxDocument,SpdxItem>> iter = this.documentItem.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<SpdxDocument,SpdxItem> entry = iter.next();
			if (entry.getValue() instanceof SpdxFile) {
				Checksum[] compareChecksums = ((SpdxFile)entry.getValue()).getChecksums();
				Checksum[] uniqueChecksums = SpdxComparer.findUniqueChecksums(checksums, compareChecksums);
				if (uniqueChecksums.length > 0) {
					this.checksumsEquals = false;
					this.differenceFound = true;
				}
				docUniqueChecksums.put(entry.getKey(), uniqueChecksums);
				HashMap<SpdxDocument, Checksum[]> compareUniqueChecksums = this.uniqueChecksums.get(entry.getKey());
				if (compareUniqueChecksums == null) {
					compareUniqueChecksums = new HashMap<SpdxDocument, Checksum[]>();
					this.uniqueChecksums.put(entry.getKey(), compareUniqueChecksums);
				}
				uniqueChecksums = SpdxComparer.findUniqueChecksums(compareChecksums, checksums);
				if (uniqueChecksums.length > 0) {
					this.checksumsEquals = false;
					this.differenceFound = true;
				}
				compareUniqueChecksums.put(spdxDocument, uniqueChecksums);
			}
		}
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
	
	public SpdxFile getFile(SpdxDocument spdxDocument) throws SpdxCompareException {
		checkInProgress();
		checkCompareMade();
		SpdxItem item = this.getItem(spdxDocument);
		if (item instanceof SpdxFile) {
			return (SpdxFile) item;
		} else {
			return null;
		}
	}

	/**
	 * @return the artifactOfEquals
	 * @throws SpdxCompareException 
	 */
	public boolean isArtifactOfEquals() throws SpdxCompareException {
		checkInProgress();
		checkCompareMade();
		return artifactOfEquals;
	}


	/**
	 * Return all artifactOfs which are in the file contained in docA but not in file contained in docB
	 * @param docA
	 * @param docB
	 * @return
	 */
	public DoapProject[] getUniqueArtifactOf(SpdxDocument docA, SpdxDocument docB) throws SpdxCompareException {
		checkInProgress();
		checkCompareMade();
		HashMap<SpdxDocument, DoapProject[]> unique = this.uniqueArtifactOfs.get(docA);
		if (unique == null) {
			return new DoapProject[0];
		}
		DoapProject[] retval = unique.get(docB);
		if (retval == null) {
			return new DoapProject[0];
		} else {
			return retval;
		}
	}

	/**
	 * @return the checksumsEquals
	 */
	public boolean isChecksumsEquals() throws SpdxCompareException {
		checkInProgress();
		checkCompareMade();
		return checksumsEquals;
	}
	
	/**
	 * Get the checksums which are present in the file contained document A but not in document B
	 * @param docA
	 * @param docB
	 * @return
	 * @throws SpdxCompareException 
	 */
	public Checksum[] getUniqueChecksums(SpdxDocument docA, SpdxDocument docB) throws SpdxCompareException {
		checkInProgress();
		HashMap<SpdxDocument, Checksum[]> uniqueMap = 
				this.uniqueChecksums.get(docA);
		if (uniqueMap == null) {
			return new Checksum[0];
		}
		Checksum[] retval = uniqueMap.get(docB);
		if (retval == null) {
			return new Checksum[0];
		}
		return retval;
	}

	/**
	 * @return the typesEquals
	 */
	public boolean isTypesEquals() throws SpdxCompareException {
		checkInProgress();
		checkCompareMade();
		return typesEquals;
	}

	/**
	 * Compares the artifactOfs for a newly added file to the existing files
	 * populating the uniqueArtifactOf hashmap and sets the differencefound
	 * Note that an artifactOf is considered unique if ANY of the properties
	 * are different
	 * @param spdxDocument
	 * @param artifactOfs
	 */
	private void compareNewArtifactOf(SpdxDocument spdxDocument,
			DoapProject[] artifactOfs) {
		HashMap<SpdxDocument, DoapProject[]> uniqueDocArtifactOf = 
				this.uniqueArtifactOfs.get(spdxDocument);
		if (uniqueDocArtifactOf == null) {
			uniqueDocArtifactOf = new HashMap<SpdxDocument, DoapProject[]>();
			this.uniqueArtifactOfs.put(spdxDocument, uniqueDocArtifactOf);
		}
		Iterator<Entry<SpdxDocument, SpdxItem>> iter = this.documentItem.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<SpdxDocument, SpdxItem> entry = iter.next();
			if (!(entry.getValue() instanceof SpdxFile)) {
				continue;
			}
			DoapProject[] compareArtifactOf = ((SpdxFile)entry.getValue()).getArtifactOf();
			HashMap<SpdxDocument, DoapProject[]> uniqueCompareArtifactOf = 
					this.uniqueArtifactOfs.get(entry.getKey());
			if (uniqueCompareArtifactOf == null) {
				uniqueCompareArtifactOf = new HashMap<SpdxDocument, DoapProject[]>();
				this.uniqueArtifactOfs.put(entry.getKey(), uniqueCompareArtifactOf);
			}
			ArrayList<DoapProject> alDocUnique = new ArrayList<DoapProject>();
			ArrayList<DoapProject> alCompareUnique = new ArrayList<DoapProject>();
			compareArtifactOf(artifactOfs, compareArtifactOf, alDocUnique, alCompareUnique);
			if (alDocUnique.size() > 0 || alCompareUnique.size() > 0) {
				this.differenceFound = true;
				this.artifactOfEquals = false;
			}
			uniqueDocArtifactOf.put(entry.getKey(), 
					alDocUnique.toArray(new DoapProject[alDocUnique.size()]));
			uniqueCompareArtifactOf.put(spdxDocument, 
					alCompareUnique.toArray(new DoapProject[alCompareUnique.size()]));
		}
	}
	
	private void compareArtifactOf(DoapProject[] artifactOfA,
			DoapProject[] artifactOfB, ArrayList<DoapProject> alUniqueA,
			ArrayList<DoapProject> alUniqueB) {
		Arrays.sort(artifactOfA, doapComparer);
		Arrays.sort(artifactOfB, doapComparer);
		int aIndex = 0;
		int bIndex = 0;
		
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
	}

	/**
	 * checks to make sure there is not a compare in progress
	 * @throws SpdxCompareException 
	 * 
	 */
	protected void checkInProgress() throws SpdxCompareException {
		super.checkInProgress();
		if (inProgress) {
			throw(new SpdxCompareException("File compare in progress - can not obtain compare results until compare has completed"));
		}
	}
	
	private void checkCompareMade() throws SpdxCompareException {
		if (this.documentItem.size() < 1) {
			throw(new SpdxCompareException("Trying to obgain results of a file compare before a file compare has been performed"));
		}	
	}
	/**
	 * @return the fileDependenciesEquals
	 */
	public boolean isFileDependenciesEquals() throws SpdxCompareException {
		checkInProgress();
		checkCompareMade();
		return fileDependenciesEquals;
	}

	/**
	 * @return the contributorsEquals
	 */
	public boolean isContributorsEquals() throws SpdxCompareException {
		checkInProgress();
		checkCompareMade();
		return contributorsEquals;
	}

	/**
	 * @return the noticeTextEquals
	 */
	public boolean isNoticeTextEquals() throws SpdxCompareException {
		checkInProgress();
		checkCompareMade();
		return noticeTextEquals;
	}

	/**
	 * @return
	 * @throws SpdxCompareException 
	 */
	public boolean isDifferenceFound() throws SpdxCompareException {
		checkInProgress();
		checkCompareMade();
		return differenceFound || super.isDifferenceFound();
	}


	/**
	 * Return a file difference for the file contained in two different documents
	 * @param docA
	 * @param docB
	 * @return
	 * @throws SpdxCompareException
	 */
	public SpdxFileDifference getFileDifference(SpdxDocument docA, SpdxDocument docB) throws SpdxCompareException {
		checkInProgress();
		checkCompareMade();
		try {
			SpdxItem itemA = this.documentItem.get(docA);
			if (itemA == null || !(itemA instanceof SpdxFile)) {
				throw(new SpdxCompareException("No SPDX File associated with "+docA.getName()));
			}
			SpdxFile fileA = (SpdxFile)itemA;
			SpdxItem itemB = this.documentItem.get(docB);
			if (itemB == null || !(itemB instanceof SpdxFile)) {
				throw(new SpdxCompareException("No SPDX File associated with "+docB.getName()));
			}
			SpdxFile fileB = (SpdxFile)itemB;
			AnyLicenseInfo[] uniqueLicenseInfoInFilesA = this.getUniqueSeenLicenses(docA, docB);
			AnyLicenseInfo[] uniqueLicenseInfoInFilesB = this.getUniqueSeenLicenses(docB, docA);
			boolean licenseInfoInFilesEquals = uniqueLicenseInfoInFilesA.length == 0 &&
					uniqueLicenseInfoInFilesB.length == 0;
			DoapProject[] uniqueArtifactOfA = this.getUniqueArtifactOf(docA, docB);
			DoapProject[] uniqueArtifactOfB = this.getUniqueArtifactOf(docB, docA);
			boolean artifactOfEquals = uniqueArtifactOfA.length == 0 &&
					uniqueArtifactOfA.length == 0;
			Checksum[] uniqueChecksumsA = this.getUniqueChecksums(docA, docB);
			Checksum[] uniqueChecksumsB = this.getUniqueChecksums(docB, docA);
			boolean checksumsEquals = uniqueChecksumsA.length == 0 && 
					uniqueChecksumsB.length == 0;
			Relationship[] uniqueRelationshipA = this.getUniqueRelationship(docA, docB);
			Relationship[] uniqueRelationshipB = this.getUniqueRelationship(docB, docA);
			boolean relationshipsEquals = uniqueRelationshipA.length == 0 &&
					uniqueRelationshipB.length == 0;
			Annotation[] uniqueAnnotationsA = this.getUniqueAnnotations(docA, docB);
			Annotation[] uniqueAnnotationsB = this.getUniqueAnnotations(docB, docA);
			boolean annotationsEquals = uniqueAnnotationsA.length == 0 &&
					uniqueAnnotationsB.length == 0;
			
			return new SpdxFileDifference(fileA, fileB, 
					fileA.getLicenseConcluded().equals(fileB.getLicenseConcluded()),
					licenseInfoInFilesEquals, uniqueLicenseInfoInFilesA, uniqueLicenseInfoInFilesB,					
					artifactOfEquals, uniqueArtifactOfA, uniqueArtifactOfB, 
					checksumsEquals, uniqueChecksumsA, uniqueChecksumsB, 				
					relationshipsEquals, uniqueRelationshipB, uniqueRelationshipB,
					annotationsEquals, uniqueAnnotationsA, uniqueAnnotationsB);
		} catch (InvalidSPDXAnalysisException e) {
			throw (new SpdxCompareException("Error reading SPDX file propoerties: "+e.getMessage(),e));
		}
	}	
}
