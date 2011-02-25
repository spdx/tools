/**
 * Copyright (c) 2011 Source Auditor Inc.
* Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.spdx.rdfparser;

import org.spdx.rdfparser.LicenseDeclaration;

/**
 * @author Source Auditor
 *
 */
public class SPDXPackageInfo {
	private String declaredName;
	private String fileName;
	private String sha1;
	private String sourceInfo;
	private LicenseDeclaration[] declaredLicenses;
	private LicenseDeclaration[] detectedLicenses;
	private String declaredCopyright;
	private String shortDescription;
	private String description;
	private String url;
	private String fileChecksum;

	public SPDXPackageInfo(String declaredName, String machineName, String sha1,
			String sourceInfo, LicenseDeclaration[] declaredLicenses, 
			LicenseDeclaration[] seenLicenses, String declaredCopyright, String shortDescription,
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
	public LicenseDeclaration[] getDeclaredLicenses() {
		return declaredLicenses;
	}

	/**
	 * @param declaredLicenses the declaredLicenses to set
	 */
	public void setDeclaredLicenses(LicenseDeclaration[] declaredLicenses) {
		this.declaredLicenses = declaredLicenses;
	}

	/**
	 * @return the detectedLicenses
	 */
	public LicenseDeclaration[] getDetectedLicenses() {
		return detectedLicenses;
	}

	/**
	 * @param detectedLicenses the detectedLicenses to set
	 */
	public void setDetectedLicenses(LicenseDeclaration[] detectedLicenses) {
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
