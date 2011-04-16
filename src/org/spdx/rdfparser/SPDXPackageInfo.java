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
	private SPDXLicenseInfo[] declaredLicenses;
	private SPDXLicenseInfo[] detectedLicenses;
	private String declaredCopyright;
	private String shortDescription;
	private String description;
	private String url;
	private String fileChecksum;

	public SPDXPackageInfo(String declaredName, String machineName, String sha1,
			String sourceInfo, SPDXLicenseInfo[] declaredLicenses, 
			SPDXLicenseInfo[] seenLicenses, String declaredCopyright, String shortDescription,
			String description, String url, String fileChecksum) {
		this.declaredName = declaredName;
		this.fileName = machineName;
		this.sha1 = sha1;
		this.sourceInfo = sourceInfo;
		this.declaredLicenses = declaredLicenses;
		this.detectedLicenses = seenLicenses;
		this.declaredCopyright = declaredCopyright;
		this.shortDescription = shortDescription;
		this.description = description;
		this.url = url;
		this.fileChecksum = fileChecksum;
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
	 * @return the fileChecksum
	 */
	public String getFileChecksum() {
		return fileChecksum;
	}

	/**
	 * @param fileChecksum the fileChecksum to set
	 */
	public void setFileChecksum(String fileChecksum) {
		this.fileChecksum = fileChecksum;
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
	public SPDXLicenseInfo[] getDeclaredLicenses() {
		return declaredLicenses;
	}

	/**
	 * @param declaredLicenses the declaredLicenses to set
	 */
	public void setDeclaredLicenses(SPDXLicenseInfo[] declaredLicenses) {
		this.declaredLicenses = declaredLicenses;
	}

	/**
	 * @return the detectedLicenses
	 */
	public SPDXLicenseInfo[] getDetectedLicenses() {
		return detectedLicenses;
	}

	/**
	 * @param detectedLicenses the detectedLicenses to set
	 */
	public void setDetectedLicenses(SPDXLicenseInfo[] detectedLicenses) {
		this.detectedLicenses = detectedLicenses;
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
