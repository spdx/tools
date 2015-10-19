/**
 * Copyright (c) 2011 Source Auditor Inc.
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
package org.spdx.rdfparser;


/**
 * @author Gary O'Neall
 *
 */
public class SPDXPackageInfo {
	private String declaredName;
	private String fileName;
	private String sha1;
	private String sourceInfo;
	private SPDXLicenseInfo declaredLicenses;
	private SPDXLicenseInfo concludedLicense;
	private SPDXLicenseInfo[] licensesFromFiles;
	private String declaredCopyright;
	private String shortDescription;
	private String description;
	private String url;
	private String licenseComments;
	private SpdxPackageVerificationCode verificationCode;
	private String versionInfo;
	private String supplier;
	private String originator;

	/**
	 * @return the verificationCode
	 */
	public SpdxPackageVerificationCode getVerificationCode() {
		return verificationCode;
	}

	/**
	 * @param verificationCode the verificationCode to set
	 */
	public void setVerificationCode(SpdxPackageVerificationCode verificationCode) {
		this.verificationCode = verificationCode;
	}

	/**
	 * @return the versionInfo
	 */
	public String getVersionInfo() {
		return versionInfo;
	}

	/**
	 * @param versionInfo the versionInfo to set
	 */
	public void setVersionInfo(String versionInfo) {
		this.versionInfo = versionInfo;
	}

	/**
	 * @param concludedLicense the concludedLicense to set
	 */
	public void setConcludedLicense(SPDXLicenseInfo concludedLicense) {
		this.concludedLicense = concludedLicense;
	}

	public SPDXPackageInfo(String declaredName, String versionInfo, String machineName,
			String sha1, String sourceInfo, SPDXLicenseInfo declaredLicense,
			SPDXLicenseInfo concludedLicense, SPDXLicenseInfo[] licensesFromFiles,
			String licenseComments, String declaredCopyright, String shortDescription,
			String description, String url, SpdxPackageVerificationCode spdxPackageVerificationCode,
			String supplier, String originator) {
		this.declaredName = declaredName;
		this.fileName = machineName;
		this.sha1 = sha1;
		this.sourceInfo = sourceInfo;
		this.declaredLicenses = declaredLicense;
		this.concludedLicense = concludedLicense;
		this.licensesFromFiles = licensesFromFiles;
		this.licenseComments = licenseComments;
		this.declaredCopyright = declaredCopyright;
		this.shortDescription = shortDescription;
		this.description = description;
		this.url = url;
		this.verificationCode = spdxPackageVerificationCode;
		this.versionInfo = versionInfo;
		this.supplier = supplier;
		this.originator = originator;
	}

	/**
	 * @return the supplier
	 */
	public String getSupplier() {
		return supplier;
	}

	/**
	 * @param supplier the supplier to set
	 */
	public void setSupplier(String supplier) {
		this.supplier = supplier;
	}

	/**
	 * @return the originator
	 */
	public String getOriginator() {
		return originator;
	}

	/**
	 * @param originator the originator to set
	 */
	public void setOriginator(String originator) {
		this.originator = originator;
	}

	/**
	 * @return the licensesFromFiles
	 */
	public SPDXLicenseInfo[] getLicensesFromFiles() {
		return licensesFromFiles;
	}

	/**
	 * @param licensesFromFiles the licensesFromFiles to set
	 */
	public void setLicensesFromFiles(SPDXLicenseInfo[] licensesFromFiles) {
		this.licensesFromFiles = licensesFromFiles;
	}

	/**
	 * @return the licenseComments
	 */
	public String getLicenseComments() {
		return licenseComments;
	}

	/**
	 * @param licenseComments the licenseComments to set
	 */
	public void setLicenseComments(String licenseComments) {
		this.licenseComments = licenseComments;
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @param url the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * @return the verificationCode
	 */
	public SpdxPackageVerificationCode getPackageVerification() {
		return verificationCode;
	}

	/**
	 * @param verificationCode the verificationCode to set
	 */
	public void setPackageVerification(SpdxPackageVerificationCode verificationCode) {
		this.verificationCode = verificationCode;
	}

	/**
	 * @return the declaredName
	 */
	public String getDeclaredName() {
		return declaredName;
	}

	/**
	 * @param declaredName the declaredName to set
	 */
	public void setDeclaredName(String declaredName) {
		this.declaredName = declaredName;
	}

	/**
	 * @return the fileName
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * @param fileName the fileName to set
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * @return the sha1
	 */
	public String getSha1() {
		return sha1;
	}

	/**
	 * @param sha1 the sha1 to set
	 */
	public void setSha1(String sha1) {
		this.sha1 = sha1;
	}

	/**
	 * @return the sourceInfo
	 */
	public String getSourceInfo() {
		return sourceInfo;
	}

	/**
	 * @param sourceInfo the sourceInfo to set
	 */
	public void setSourceInfo(String sourceInfo) {
		this.sourceInfo = sourceInfo;
	}

	/**
	 * @return the declaredLicenses
	 */
	public SPDXLicenseInfo getDeclaredLicenses() {
		return declaredLicenses;
	}

	/**
	 * @param declaredLicenses the declaredLicenses to set
	 */
	public void setDeclaredLicenses(SPDXLicenseInfo declaredLicenses) {
		this.declaredLicenses = declaredLicenses;
	}

	/**
	 * @return the detectedLicenses
	 */
	public SPDXLicenseInfo getConcludedLicense() {
		return concludedLicense;
	}

	/**
	 * @param concludedLicense the detectedLicenses to set
	 */
	public void setDConcludedLicense(SPDXLicenseInfo concludedLicense) {
		this.concludedLicense = concludedLicense;
	}

	/**
	 * @return the declaredCopyright
	 */
	public String getDeclaredCopyright() {
		return declaredCopyright;
	}

	/**
	 * @param declaredCopyright the declaredCopyright to set
	 */
	public void setDeclaredCopyright(String declaredCopyright) {
		this.declaredCopyright = declaredCopyright;
	}

	/**
	 * @return the shortDescription
	 */
	public String getShortDescription() {
		return shortDescription;
	}

	/**
	 * @param shortDescription the shortDescription to set
	 */
	public void setShortDescription(String shortDescription) {
		this.shortDescription = shortDescription;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
}
