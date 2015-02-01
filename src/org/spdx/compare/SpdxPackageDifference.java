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

import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.license.AnyLicenseInfo;
import org.spdx.rdfparser.model.Annotation;
import org.spdx.rdfparser.model.Checksum;
import org.spdx.rdfparser.model.Relationship;
import org.spdx.rdfparser.model.SpdxFile;
import org.spdx.rdfparser.model.SpdxPackage;

/**
 * @author Gary
 *
 */
public class SpdxPackageDifference extends SpdxItemDifference {

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
	private Checksum[] uniqueChecksumsA;
	private Checksum[] uniqueChecksumsB;
	private boolean packageFilesEquals;
	private SpdxFile[] uniqueFilesA;
	private SpdxFile[] uniqueFilesB;
	private SpdxFileDifference[] fileDifferences;

	/**
	 * @param itemA
	 * @param itemB
	 * @param concludedLicensesEqual
	 * @param seenLicensesEqual
	 * @param uniqueSeenLicensesA
	 * @param uniqueSeenLicensesB
	 * @param relationshipsEquals
	 * @param uniqueRelationshipA
	 * @param uniqueRelationshipB
	 * @param annotationsEquals
	 * @param uniqueAnnotationsA
	 * @param uniqueAnnotationsB
	 * @throws InvalidSPDXAnalysisException
	 */
	public SpdxPackageDifference(SpdxPackage itemA, SpdxPackage itemB,
			boolean concludedLicensesEqual, boolean seenLicensesEqual,
			AnyLicenseInfo[] uniqueSeenLicensesA,
			AnyLicenseInfo[] uniqueSeenLicensesB, boolean relationshipsEquals,
			Relationship[] uniqueRelationshipA,
			Relationship[] uniqueRelationshipB, boolean annotationsEquals,
			Annotation[] uniqueAnnotationsA, Annotation[] uniqueAnnotationsB)
			throws SpdxCompareException {
		super(itemA, itemB, concludedLicensesEqual, seenLicensesEqual,
				uniqueSeenLicensesA, uniqueSeenLicensesB, relationshipsEquals,
				uniqueRelationshipA, uniqueRelationshipB, annotationsEquals,
				uniqueAnnotationsA, uniqueAnnotationsB);		
	}

	/**
	 * @param packageVersionsEquals
	 */
	public void setPackageVersionsEquals(boolean packageVersionsEquals) {
		this.packageVersionsEquals = packageVersionsEquals;
	}

	/**
	 * @param packageFilenamesEquals
	 */
	public void setPackageFilenamesEquals(boolean packageFilenamesEquals) {
		this.packageFilenamesEquals = packageFilenamesEquals;
	}

	/**
	 * @param packageSuppliersEquals
	 */
	public void setPackageSuppliersEquals(boolean packageSuppliersEquals) {
		this.packageSuppliersEquals = packageSuppliersEquals;
		
	}

	/**
	 * @param packageDownloadLocationsEquals
	 */
	public void setPackageDownloadLocationsEquals(
			boolean packageDownloadLocationsEquals) {
		this.packageDownloadLocationsEquals = packageDownloadLocationsEquals;
		
	}

	/**
	 * @param packageVerificationCodeesEquals
	 */
	public void setPackageVerificationCodeesEquals(
			boolean packageVerificationCodeesEquals) {
		this.packageVerificationCodeesEquals = packageVerificationCodeesEquals;
		
	}

	/**
	 * @param packageChecksumsEquals
	 */
	public void setPackageChecksumsEquals(boolean packageChecksumsEquals) {
		this.packageChecksumsEquals = packageChecksumsEquals;
		
	}

	/**
	 * @param packageSourceInfosEquals
	 */
	public void setPackageSourceInfosEquals(boolean packageSourceInfosEquals) {
		this.packageSourceInfosEquals = packageSourceInfosEquals;
		
	}

	/**
	 * @param declaredLicennsesEquals
	 */
	public void setDeclaredLicennsesEquals(boolean declaredLicennsesEquals) {
		this.declaredLicennsesEquals = declaredLicennsesEquals;
		
	}

	/**
	 * @param packageSummaryEquals
	 */
	public void setPackageSummaryEquals(boolean packageSummaryEquals) {
		this.packageSummaryEquals = packageSummaryEquals;
		
	}

	/**
	 * @param packageDescriptionsEquals
	 */
	public void setPackageDescriptionsEquals(boolean packageDescriptionsEquals) {
		this.packageDescriptionsEquals = packageDescriptionsEquals;
	}

	/**
	 * @param packageOriginatorsEqual
	 */
	public void setPackageOriginatorsEqual(boolean packageOriginatorsEqual) {
		this.packageOriginatorsEqual = packageOriginatorsEqual;
		
	}

	/**
	 * @param packageHomePagesEquals
	 */
	public void setPackageHomePagesEquals(boolean packageHomePagesEquals) {
		this.packageHomePagesEquals = packageHomePagesEquals;
		
	}

	/**
	 * @param uniqueChecksumsA
	 */
	public void setUniqueChecksumsA(Checksum[] uniqueChecksumsA) {
		this.uniqueChecksumsA = uniqueChecksumsA;
		
	}

	/**
	 * @param uniqueChecksumsB
	 */
	public void setUniqueChecksumsB(Checksum[] uniqueChecksumsB) {
		this.uniqueChecksumsB = uniqueChecksumsB;
		
	}

	/**
	 * @param packageFilesEquals
	 */
	public void setPackageFilesEquals(boolean packageFilesEquals) {
		this.packageFilesEquals = packageFilesEquals;
		
	}

	/**
	 * @param uniqueFilesA
	 */
	public void setUniqueFilesA(SpdxFile[] uniqueFilesA) {
		this.uniqueFilesA = uniqueFilesA;
		
	}

	/**
	 * @param uniqueFilesB
	 */
	public void setUniqueFilesB(SpdxFile[] uniqueFilesB) {
		this.uniqueFilesB = uniqueFilesB;
		
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
	public boolean isPackageVerificationCodeesEquals() {
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

	/**
	 * @param fileDifferences
	 */
	public void setFileDifferences(SpdxFileDifference[] fileDifferences) {
		this.fileDifferences = fileDifferences;
	}

	public SpdxFileDifference[] getFileDifferences() {
		return this.fileDifferences;
	}
}
