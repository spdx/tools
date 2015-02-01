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

import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.model.Checksum;
import org.spdx.rdfparser.model.SpdxFile;
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
	private boolean packageVersionsEquals;
	private boolean packageFilenamesEquals;
	private boolean packageSuppliersEquals;
	private boolean packageDownloadLocationsEquals;
	private boolean packageVerificationCodeesEquals;
	private boolean packageChecksumsEquals;
	private boolean packageSourceInfosEquals;
	private boolean declaredLicennsesEquals;
	private boolean packageSummaryEquals;
	private boolean packageDescriptionsEquals;
	private boolean packageOriginatorsEqual;
	private boolean packageHomePagesEquals;
	private SpdxPackage pkgA;
	private SpdxPackage pkgB;
	private Checksum[] uniqueChecksumsA;
	private Checksum[] uniqueChecksumsB;
	private boolean packageFilesEquals;
	private SpdxFileDifference[] fileDifferences;
	private SpdxFile[] uniqueFilesA;
	private SpdxFile[] uniqueFilesB;
	
	public SpdxPackageComparer() {
		
	}
	
	/**
	 * Compare two SPDX documents and store the results
	 * @param pkg1
	 * @param pkg2
	 * @param licenseXlationMap A mapping between the license IDs from licenses in fileA to fileB
	 * @throws SpdxCompareException 
	 */
	public void compare(SpdxPackage pkg1, SpdxPackage pkg2, 
			HashMap<String, String> licenseXlationMap) throws SpdxCompareException {
		super.compare(pkg1, pkg2, licenseXlationMap);
		inProgress = true;
		differenceFound = super.isDifferenceFound();
		this.pkgA = pkg1;
		this.pkgB = pkg2;

		this.packageVersionsEquals = true;
		if (!SpdxComparer.stringsEqual(pkg1.getVersionInfo(), pkg2.getVersionInfo())) {
			this.packageVersionsEquals = false;
			this.differenceFound = true;
		}
		this.packageFilenamesEquals = true;
		if (!SpdxComparer.stringsEqual(pkg1.getPackageFileName(), pkg2.getPackageFileName())) {
			this.packageFilenamesEquals = false;
			this.differenceFound = true;
		}
		this.packageSuppliersEquals = true;
		if (!SpdxComparer.stringsEqual(pkg1.getSupplier(), pkg2.getSupplier())) {
			this.packageSuppliersEquals = false;
			this.differenceFound = true;
		}
		this.packageOriginatorsEqual = true;
		if (!SpdxComparer.stringsEqual(pkg1.getOriginator(), pkg2.getOriginator())) {
			this.packageOriginatorsEqual = false;
			this.differenceFound = true;
		}
		this.packageDownloadLocationsEquals = true;
		if (!SpdxComparer.stringsEqual(pkg1.getDownloadLocation(), pkg2.getDownloadLocation())) {
			this.packageDownloadLocationsEquals = false;
			this.differenceFound = true;
		}
		this.packageVerificationCodeesEquals = true;
		try {
			if (!SpdxComparer.compareVerificationCodes(pkg1.getPackageVerificationCode(), pkg2.getPackageVerificationCode())) {
				this.packageVerificationCodeesEquals = false;
				this.differenceFound = true;
			}
		} catch (InvalidSPDXAnalysisException e) {
			throw(new SpdxCompareException("SPDX error getting package verification codes: "+e.getMessage(),e));
		}
		this.packageChecksumsEquals = true;
		try {
			if (!SpdxComparer.elementsEquivalent(pkg1.getChecksums(), pkg2.getChecksums())) {
				this.packageChecksumsEquals = false;
				this.differenceFound = true;
				this.uniqueChecksumsA = SpdxComparer.findUniqueChecksums(pkg1.getChecksums(), 
						pkg2.getChecksums());
				this.uniqueChecksumsB = SpdxComparer.findUniqueChecksums(pkg2.getChecksums(), 
						pkg1.getChecksums());
			} else {
				this.uniqueChecksumsA = new Checksum[0];
				this.uniqueChecksumsB = new Checksum[0];
			}
		} catch (InvalidSPDXAnalysisException e) {
			throw(new SpdxCompareException("SPDX error getting package checksums: "+e.getMessage(),e));
		}
		this.packageSourceInfosEquals = true;
		if (!SpdxComparer.stringsEqual(pkg1.getSourceInfo(), pkg2.getSourceInfo())) {
			this.packageSourceInfosEquals = false;
			this.differenceFound = true;
		}
		this.declaredLicennsesEquals = true;
		try {
			
			if (!LicenseCompareHelper.isLicenseEqual(pkg1.getLicenseDeclared(), 
					pkg2.getLicenseDeclared(), licenseXlationMap)) {
				this.declaredLicennsesEquals = false;
				this.differenceFound = true;
			}
		} catch (InvalidSPDXAnalysisException e) {
			throw(new SpdxCompareException("SPDX error getting declared license: "+e.getMessage(),e));
		}
		this.packageSummaryEquals = true;
		if (!SpdxComparer.stringsEqual(pkg1.getSummary(), pkg2.getSummary())) {
			this.packageSummaryEquals = false;
			this.differenceFound = true;
		}
		this.packageDescriptionsEquals = true;
		if (!SpdxComparer.stringsEqual(pkg1.getDescription(), pkg2.getDescription())) {
			this.packageDescriptionsEquals = false;
			this.differenceFound = true;
		}

		this.packageHomePagesEquals = true;
		if (!SpdxComparer.stringsEqual(pkg1.getHomepage(), pkg2.getHomepage())) {
			this.packageHomePagesEquals = false;
			this.differenceFound = true;
		}
		this.packageFilesEquals = true;
		try {
			compareFiles(pkg1.getFiles(), pkg2.getFiles(), licenseXlationMap);
			if (uniqueFilesA.length > 0 || uniqueFilesB.length > 0 || fileDifferences.length > 0) {
				this.packageFilesEquals = false;
				this.differenceFound = true;
			}
		} catch (InvalidSPDXAnalysisException e) {
			throw(new SpdxCompareException("SPDX error getting package files: "+e.getMessage(),e));
		}
	}
	
	/**
	 * @param filesA
	 * @param filesB
	 * @throws SpdxCompareException 
	 */
	private void compareFiles(SpdxFile[] filesA, SpdxFile[] filesB, 
			HashMap<String, String> licenseXlationMap) throws SpdxCompareException {
	Arrays.sort(filesA);
		Arrays.sort(filesB);
		this.fileDifferences = SpdxComparer.findFileDifferences(filesA, filesB, licenseXlationMap);
		this.uniqueFilesA = SpdxComparer.findUniqueFiles(filesA, filesB);
		this.uniqueFilesB = SpdxComparer.findUniqueFiles(filesB, filesA);
	}

	public SpdxPackageDifference getPackageDifference() throws SpdxCompareException {
		SpdxPackageDifference retval = new SpdxPackageDifference(this.pkgA, this.pkgB, 
				this.isConcludedLicenseEquals(), this.isSeenLicenseEquals(), 
				this.getUniqueSeenLicensesA(), this.getUniqueSeenLicensesB(), 
				this.isRelationshipsEquals(), this.getUniqueRelationshipA(), this.getUniqueRelationshipB(),
				this.isAnnotationsEquals(), this.getUniqueAnnotationsA(), this.getUniqueAnnotationsB());
		retval.setPackageVersionsEquals(packageVersionsEquals);
		retval.setPackageFilenamesEquals(packageFilenamesEquals);
		retval.setPackageSuppliersEquals(packageSuppliersEquals);
		retval.setPackageDownloadLocationsEquals(packageDownloadLocationsEquals);
		retval.setPackageVerificationCodeesEquals(packageVerificationCodeesEquals);
		retval.setPackageChecksumsEquals(packageChecksumsEquals);
		retval.setPackageSourceInfosEquals(packageSourceInfosEquals);
		retval.setDeclaredLicennsesEquals(declaredLicennsesEquals);
		retval.setPackageSummaryEquals(packageSummaryEquals);
		retval.setPackageDescriptionsEquals(packageDescriptionsEquals);
		retval.setPackageOriginatorsEqual(packageOriginatorsEqual);
		retval.setPackageHomePagesEquals(packageHomePagesEquals);
		retval.setUniqueChecksumsA(uniqueChecksumsA);
		retval.setUniqueChecksumsB(uniqueChecksumsB);
		retval.setPackageFilesEquals(packageFilesEquals);
		retval.setUniqueFilesA(uniqueFilesA);
		retval.setUniqueFilesB(uniqueFilesB);
		retval.setFileDifferences(this.fileDifferences);
		return retval;
	}

	/**
	 * @return the inProgress
	 */
	public boolean isInProgress() {
		return inProgress;
	}

	/**
	 * @return the differenceFound
	 */
	public boolean isDifferenceFound() {
		return differenceFound;
	}

	/**
	 * @return the packageVersionsEquals
	 */
	public boolean isPackageVersionsEquals() {
		return packageVersionsEquals;
	}

	/**
	 * @return the packageFilenamesEquals
	 */
	public boolean isPackageFilenamesEquals() {
		return packageFilenamesEquals;
	}

	/**
	 * @return the packageSuppliersEquals
	 */
	public boolean isPackageSuppliersEquals() {
		return packageSuppliersEquals;
	}

	/**
	 * @return the packageDownloadLocationsEquals
	 */
	public boolean isPackageDownloadLocationsEquals() {
		return packageDownloadLocationsEquals;
	}

	/**
	 * @return the packageVerificationCodeesEquals
	 */
	public boolean isPackageVerificationCodesEquals() {
		return packageVerificationCodeesEquals;
	}

	/**
	 * @return the packageChecksumsEquals
	 */
	public boolean isPackageChecksumsEquals() {
		return packageChecksumsEquals;
	}

	/**
	 * @return the packageSourceInfosEquals
	 */
	public boolean isPackageSourceInfosEquals() {
		return packageSourceInfosEquals;
	}

	/**
	 * @return the declaredLicennsesEquals
	 */
	public boolean isDeclaredLicennsesEquals() {
		return declaredLicennsesEquals;
	}

	/**
	 * @return the packageSummaryEquals
	 */
	public boolean isPackageSummaryEquals() {
		return packageSummaryEquals;
	}

	/**
	 * @return the packageDescriptionsEquals
	 */
	public boolean isPackageDescriptionsEquals() {
		return packageDescriptionsEquals;
	}

	/**
	 * @return the packageOriginatorsEqual
	 */
	public boolean isPackageOriginatorsEqual() {
		return packageOriginatorsEqual;
	}

	/**
	 * @return the packageHomePagesEquals
	 */
	public boolean isPackageHomePagesEquals() {
		return packageHomePagesEquals;
	}

	/**
	 * @return the pkgA
	 */
	public SpdxPackage getPkgA() {
		return pkgA;
	}

	/**
	 * @return the pkgB
	 */
	public SpdxPackage getPkgB() {
		return pkgB;
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
	 * @return the packageFilesEquals
	 */
	public boolean isPackageFilesEquals() {
		return packageFilesEquals;
	}

	/**
	 * @return the fileDifferences
	 */
	public SpdxFileDifference[] getFileDifferences() {
		return fileDifferences;
	}

	/**
	 * @return the uniqueFilesA
	 */
	public SpdxFile[] getUniqueFilesA() {
		return uniqueFilesA;
	}

	/**
	 * @return the uniqueFilesB
	 */
	public SpdxFile[] getUniqueFilesB() {
		return uniqueFilesB;
	}
	
}
