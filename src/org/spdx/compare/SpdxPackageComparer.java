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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.model.Checksum;
import org.spdx.rdfparser.model.SpdxDocument;
import org.spdx.rdfparser.model.SpdxFile;
import org.spdx.rdfparser.model.SpdxItem;
import org.spdx.rdfparser.model.SpdxPackage;

/**
 * Compares two SPDX package.  The <code>compare(pkgA, pkgB)</code> method will perform the comparison and
 * store the results.  <code>isDifferenceFound()</code> will return true of any 
 * differences were found.
 * @author Gary O'Neall
 *
 */
public class SpdxPackageComparer extends SpdxItemComparer {
	private boolean inProgress = false;
	private boolean differenceFound = false;
	private boolean packageVersionsEquals = true;
	private boolean packageFilenamesEquals = true;
	private boolean packageSuppliersEquals = true;
	private boolean packageDownloadLocationsEquals = true;
	private boolean packageVerificationCodesEquals = true;
	private boolean packageChecksumsEquals = true;
	private boolean packageSourceInfosEquals = true;
	private boolean declaredLicensesEquals = true;
	private boolean packageSummaryEquals = true;
	private boolean packageDescriptionsEquals = true;
	private boolean packageOriginatorsEqual = true;
	private boolean packageHomePagesEquals = true;
	private boolean packageFilesEquals = true;
	/**
	 * Map of documents to a map of documents with unique checksums
	 */
	private HashMap<SpdxDocument, HashMap<SpdxDocument, Checksum[]>> uniqueChecksums = 
			new HashMap<SpdxDocument, HashMap<SpdxDocument, Checksum[]>>();

	/**
	 * Map of documents to a map of documents with unique files
	 */
	private HashMap<SpdxDocument, HashMap<SpdxDocument, SpdxFile[]>> uniqueFiles = 
			new HashMap<SpdxDocument, HashMap<SpdxDocument, SpdxFile[]>>();
	/**
	 * Map of all file differences founds between any two spdx document packages
	 */
	private HashMap<SpdxDocument, HashMap<SpdxDocument, SpdxFileDifference[]>> fileDifferences = 
			new HashMap<SpdxDocument, HashMap<SpdxDocument, SpdxFileDifference[]>>();
	/**
	 * @param extractedLicenseIdMap map of all extracted license IDs for any SPDX documents to be added to the comparer
	 */
	public SpdxPackageComparer(HashMap<SpdxDocument, HashMap<SpdxDocument, HashMap<String, String>>> extractedLicenseIdMap) {
		super(extractedLicenseIdMap);
	}
	
	/**
	 * Add a package to the comparer and performs the comparison to any existing documents
	 * @param spdxDocument document containing the package
	 * @param spdxPackage packaged to be added
	 * @param licenseXlationMap A mapping between the license IDs from licenses in fileA to fileB
	 * @throws SpdxCompareException 
	 */
	public void addDocumentPackage(SpdxDocument spdxDocument,
			SpdxPackage spdxPackage) throws SpdxCompareException {
		checkInProgress();
		if (this.name == null) {
			this.name = spdxPackage.getName();
		} else if (!this.name.equals(spdxPackage.getName())) {
			throw(new SpdxCompareException("Names do not match for item being added to comparer: "+
					spdxPackage.getName()+", expecting "+this.name));
		}
		inProgress = true;
		Iterator<Entry<SpdxDocument, SpdxItem>> iter = this.documentItem.entrySet().iterator();
		SpdxPackage pkg2 = null;
		HashMap<String, String> licenseXlationMap = null;
		while (iter.hasNext() && pkg2 == null) {
			Entry<SpdxDocument, SpdxItem> entry = iter.next();
			if (entry.getValue() instanceof SpdxPackage) {
				pkg2 = (SpdxPackage)entry.getValue();
				licenseXlationMap = this.extractedLicenseIdMap.get(spdxDocument).get(entry.getKey());
			}
		}
		if (pkg2 != null) {
			if (!SpdxComparer.stringsEqual(spdxPackage.getVersionInfo(), pkg2.getVersionInfo())) {
				this.packageVersionsEquals = false;
				this.differenceFound = true;
			}
			if (!SpdxComparer.stringsEqual(spdxPackage.getPackageFileName(), pkg2.getPackageFileName())) {
				this.packageFilenamesEquals = false;
				this.differenceFound = true;
			}
			if (!SpdxComparer.stringsEqual(spdxPackage.getSupplier(), pkg2.getSupplier())) {
				this.packageSuppliersEquals = false;
				this.differenceFound = true;
			}
			if (!SpdxComparer.stringsEqual(spdxPackage.getOriginator(), pkg2.getOriginator())) {
				this.packageOriginatorsEqual = false;
				this.differenceFound = true;
			}
			if (!SpdxComparer.stringsEqual(spdxPackage.getDownloadLocation(), pkg2.getDownloadLocation())) {
				this.packageDownloadLocationsEquals = false;
				this.differenceFound = true;
			}
			try {
				if (!SpdxComparer.compareVerificationCodes(spdxPackage.getPackageVerificationCode(), pkg2.getPackageVerificationCode())) {
					this.packageVerificationCodesEquals = false;
					this.differenceFound = true;
				}
			} catch (InvalidSPDXAnalysisException e) {
				throw(new SpdxCompareException("SPDX error getting package verification codes: "+e.getMessage(),e));
			}
			try {
				compareNewPackageChecksums(spdxDocument, spdxPackage.getChecksums());
			} catch (InvalidSPDXAnalysisException e) {
				throw(new SpdxCompareException("SPDX error getting package checksums: "+e.getMessage(),e));
			}
			if (!SpdxComparer.stringsEqual(spdxPackage.getSourceInfo(), pkg2.getSourceInfo())) {
				this.packageSourceInfosEquals = false;
				this.differenceFound = true;
			}
			try {
				if (!LicenseCompareHelper.isLicenseEqual(spdxPackage.getLicenseDeclared(), 
						pkg2.getLicenseDeclared(), licenseXlationMap)) {
					this.declaredLicensesEquals = false;
					this.differenceFound = true;
				}
			} catch (InvalidSPDXAnalysisException e) {
				throw(new SpdxCompareException("SPDX error getting declared license: "+e.getMessage(),e));
			}
			if (!SpdxComparer.stringsEqual(spdxPackage.getSummary(), pkg2.getSummary())) {
				this.packageSummaryEquals = false;
				this.differenceFound = true;
			}
			if (!SpdxComparer.stringsEqual(spdxPackage.getDescription(), pkg2.getDescription())) {
				this.packageDescriptionsEquals = false;
				this.differenceFound = true;
			}
			if (!SpdxComparer.stringsEqual(spdxPackage.getHomepage(), pkg2.getHomepage())) {
				this.packageHomePagesEquals = false;
				this.differenceFound = true;
			}
			try {
				compareNewPackageFiles(spdxDocument, spdxPackage.getFiles());
			} catch (InvalidSPDXAnalysisException e) {
				throw(new SpdxCompareException("SPDX error getting package files: "+e.getMessage(),e));
			}	
		}
		inProgress = false;
		super.addDocumentItem(spdxDocument, spdxPackage);
	}
	
	/**
	 * @param spdxDocument
	 * @param files
	 * @throws SpdxCompareException 
	 * @throws InvalidSPDXAnalysisException 
	 */
	private void compareNewPackageFiles(SpdxDocument spdxDocument,
			SpdxFile[] files) throws SpdxCompareException, InvalidSPDXAnalysisException {
		Arrays.sort(files);
		HashMap<SpdxDocument, SpdxFile[]> docUniqueFiles = this.uniqueFiles.get(spdxDocument);
		if (docUniqueFiles == null) {
			docUniqueFiles = new HashMap<SpdxDocument, SpdxFile[]>();
			this.uniqueFiles.put(spdxDocument, docUniqueFiles);
		}
		HashMap<SpdxDocument, SpdxFileDifference[]> docDifferentFiles = this.fileDifferences.get(spdxDocument);
		if (docDifferentFiles == null) {
			docDifferentFiles = new HashMap<SpdxDocument, SpdxFileDifference[]>();
			this.fileDifferences.put(spdxDocument, docDifferentFiles);
		}
		Iterator<Entry<SpdxDocument, SpdxItem>> iter = this.documentItem.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<SpdxDocument, SpdxItem> entry = iter.next();
			if (entry.getValue() instanceof SpdxPackage) {
				SpdxFile[] compareFiles = ((SpdxPackage)entry.getValue()).getFiles();
				Arrays.sort(compareFiles);
				SpdxFileDifference[] fileDifferences = 
						SpdxComparer.findFileDifferences(spdxDocument, entry.getKey(), files, compareFiles, this.extractedLicenseIdMap);
				if (fileDifferences.length > 0) {
					this.packageFilesEquals = false;
					this.differenceFound = true;
				}
				docDifferentFiles.put(entry.getKey(), fileDifferences);
				HashMap<SpdxDocument, SpdxFileDifference[]> compareDifferentFiles = 
						this.fileDifferences.get(entry.getKey());
				if (compareDifferentFiles == null) {
					compareDifferentFiles = new HashMap<SpdxDocument, SpdxFileDifference[]>();
					this.fileDifferences.put(entry.getKey(), compareDifferentFiles);
				}
				compareDifferentFiles.put(spdxDocument, fileDifferences);
				SpdxFile[] uniqueFiles = SpdxComparer.findUniqueFiles(files, compareFiles);
				if (uniqueFiles.length > 0) {
					this.packageFilesEquals = false;
					this.differenceFound = true;
				}
				docUniqueFiles.put(entry.getKey(), uniqueFiles);
				HashMap<SpdxDocument, SpdxFile[]> compareUniqueFiles = 
						this.uniqueFiles.get(entry.getKey());
				if (compareUniqueFiles == null) {
					compareUniqueFiles = new HashMap<SpdxDocument, SpdxFile[]>();
					this.uniqueFiles.put(entry.getKey(), compareUniqueFiles);
				}
				uniqueFiles = SpdxComparer.findUniqueFiles(compareFiles, files);
				if (uniqueFiles.length > 0) {
					this.packageFilesEquals = false;
					this.differenceFound = true;
				}
				compareUniqueFiles.put(spdxDocument, uniqueFiles);
			}
		}
	}

	/**
	 * Compare the checks for a new package being added to the existing
	 * package checksums filling in the unique checksums map
	 * @param spdxDocument
	 * @param checksums
	 * @throws SpdxCompareException 
	 */
	private void compareNewPackageChecksums(SpdxDocument spdxDocument,
			Checksum[] checksums) throws SpdxCompareException {
		try {
			HashMap<SpdxDocument, Checksum[]> docUniqueChecksums = 
					new HashMap<SpdxDocument, Checksum[]>();
			this.uniqueChecksums.put(spdxDocument, docUniqueChecksums);
			Iterator<Entry<SpdxDocument,SpdxItem>> iter = this.documentItem.entrySet().iterator();
			while (iter.hasNext()) {
				Entry<SpdxDocument,SpdxItem> entry = iter.next();
				if (entry.getValue() instanceof SpdxPackage) {
					Checksum[] compareChecksums = ((SpdxPackage)entry.getValue()).getChecksums();
					Checksum[] uniqueChecksums = SpdxComparer.findUniqueChecksums(checksums, compareChecksums);
					if (uniqueChecksums.length > 0) {
						this.packageChecksumsEquals = false;
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
						this.packageChecksumsEquals = false;
						this.differenceFound = true;
					}
					compareUniqueChecksums.put(spdxDocument, uniqueChecksums);
				}
			}
		} catch (InvalidSPDXAnalysisException e) {
			throw(new SpdxCompareException("SPDX error getting package checksums: "+e.getMessage(),e));
		}
	}

	/**
	 * @return the inProgress
	 */
	public boolean isInProgress() {
		return inProgress;
	}

	/**
	 * @return the differenceFound
	 * @throws SpdxCompareException 
	 */
	public boolean isDifferenceFound() throws SpdxCompareException {
		checkInProgress();
		return differenceFound || super.isDifferenceFound();
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
		super.checkInProgress();
	}
	/**
	 * @return the packageVersionsEquals
	 * @throws SpdxCompareException 
	 */
	public boolean isPackageVersionsEquals() throws SpdxCompareException {
		checkInProgress();
		return packageVersionsEquals;
	}

	/**
	 * @return the packageFilenamesEquals
	 */
	public boolean isPackageFilenamesEquals() throws SpdxCompareException {
		checkInProgress();
		return packageFilenamesEquals;
	}

	/**
	 * @return the packageSuppliersEquals
	 */
	public boolean isPackageSuppliersEquals() throws SpdxCompareException {
		checkInProgress();
		return packageSuppliersEquals;
	}

	/**
	 * @return the packageDownloadLocationsEquals
	 */
	public boolean isPackageDownloadLocationsEquals() throws SpdxCompareException {
		checkInProgress();
		return packageDownloadLocationsEquals;
	}

	/**
	 * @return the packageVerificationCodeesEquals
	 */
	public boolean isPackageVerificationCodesEquals() throws SpdxCompareException {
		checkInProgress();
		return packageVerificationCodesEquals;
	}

	/**
	 * @return the packageChecksumsEquals
	 */
	public boolean isPackageChecksumsEquals() throws SpdxCompareException {
		checkInProgress();
		return packageChecksumsEquals;
	}

	/**
	 * @return the packageSourceInfosEquals
	 */
	public boolean isPackageSourceInfosEquals() throws SpdxCompareException {
		checkInProgress();
		return packageSourceInfosEquals;
	}

	/**
	 * @return the declaredLicensesEquals
	 */
	public boolean isDeclaredLicensesEquals() throws SpdxCompareException {
		checkInProgress();
		return declaredLicensesEquals;
	}

	/**
	 * @return the packageSummaryEquals
	 */
	public boolean isPackageSummaryEquals() throws SpdxCompareException {
		checkInProgress();
		return packageSummaryEquals;
	}

	/**
	 * @return the packageDescriptionsEquals
	 */
	public boolean isPackageDescriptionsEquals() throws SpdxCompareException {
		checkInProgress();
		return packageDescriptionsEquals;
	}

	/**
	 * @return the packageOriginatorsEqual
	 */
	public boolean isPackageOriginatorsEqual() throws SpdxCompareException {
		checkInProgress();
		return packageOriginatorsEqual;
	}

	/**
	 * @return the packageHomePagesEquals
	 */
	public boolean isPackageHomePagesEquals() throws SpdxCompareException {
		checkInProgress();
		return packageHomePagesEquals;
	}
	
	/**
	 * Return the package associated with the document
	 * @param document 
	 * @return The document associated with the document
	 */
	public SpdxPackage getDocPackage(SpdxDocument document) throws SpdxCompareException {
		SpdxItem retItem = this.documentItem.get(document);
		if (retItem != null && retItem instanceof SpdxPackage) {
			return (SpdxPackage)retItem;
		} else {
			return null;
		}
	}

	/**
	 * Get the checksums which are present in document A but not in document B
	 * @return the uniqueChecksums
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
	 * @return the packageFilesEquals
	 */
	public boolean isPackageFilesEquals() throws SpdxCompareException {
		checkInProgress();
		return packageFilesEquals;
	}

	/**
	 * Get any fileDifferences which are in docA but not in docB
	 * @param docA
	 * @param docB
	 * @return
	 */
	public SpdxFileDifference[] getFileDifferences(SpdxDocument docA, 
			SpdxDocument docB) throws SpdxCompareException {
		checkInProgress();
		HashMap<SpdxDocument, SpdxFileDifference[]> uniqueMap = this.fileDifferences.get(docA);
		if (uniqueMap == null) {
			return new SpdxFileDifference[0];
		}
		SpdxFileDifference[] retval = uniqueMap.get(docB);
		if (retval == null) {
			return new SpdxFileDifference[0];
		}
		return retval;
	}


	/**
	 * Return any unique files by name which are in docA but not in docB
	 * @param docA
	 * @param docB
	 * @return
	 */
	public SpdxFile[] getUniqueFiles(SpdxDocument docA, SpdxDocument docB) throws SpdxCompareException {
		checkInProgress();
		HashMap<SpdxDocument, SpdxFile[]> uniqueMap = this.uniqueFiles.get(docA);
		if (uniqueMap == null) {
			return new SpdxFile[0];
		}
		SpdxFile[] retval = uniqueMap.get(docB);
		if (retval == null) {
			return new SpdxFile[0];
		}
		return retval;
	}

	/**
	 * @return
	 */
	public String getPackageName() throws SpdxCompareException {
		checkInProgress();
		return this.name;
	}

	/**
	 * @return
	 * @throws SpdxCompareException 
	 */
	public int getNumPackages() throws SpdxCompareException {
		checkInProgress();
		return this.documentItem.size();
	}
	
}
