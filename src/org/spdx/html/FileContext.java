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
package org.spdx.html;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.model.Checksum;
import org.spdx.rdfparser.model.DoapProject;
import org.spdx.rdfparser.model.SpdxFile;
import org.spdx.rdfparser.model.SpdxFile.FileType;
import org.spdx.rdfparser.license.AnyLicenseInfo;

/**
 * Context for describing SPDX Files
 * 
 * @author Gary O'Neall
 *
 */
public class FileContext {
	
	SpdxFile spdxFile = null;
	Exception error = null;

	/**
	 * @param SpdxFile
	 */
	public FileContext(SpdxFile SpdxFile) {
		this.spdxFile = SpdxFile;
	}

	/**
	 * @param e
	 */
	public FileContext(InvalidSPDXAnalysisException e) {
		this.error = null;
	}
	
	public String fileName() {
		if (spdxFile == null && error != null) {
			return "Error getting SPDX file information: "+error.getMessage();
		}
		if (spdxFile != null) {
			return spdxFile.getName();
		} else {
			return null;
		}
	}
	
	public String spdxId() {
		if (spdxFile == null && error != null) {
			return "Error getting SPDX file information: "+error.getMessage();
		}
		return spdxFile.getId();
	}
	
	public List<String> checksums() {
		if (spdxFile == null && error != null) {
			ArrayList<String> retval = new ArrayList<String>();
			retval.add("Error getting SPDX file information: "+error.getMessage());
			return retval;
		}
		Checksum[] fileChecksums = this.spdxFile.getChecksums();
		if (fileChecksums == null || fileChecksums.length ==0) {
			return null;
		}
		ArrayList<String> retval = new ArrayList<String>();
		for (int i = 0; i < fileChecksums.length; i++) {
			retval.add(Checksum.CHECKSUM_ALGORITHM_TO_TAG.get(fileChecksums[i].getAlgorithm())+
					" "+fileChecksums[i].getValue());
		}
		Collections.sort(retval);
		return retval;
	}
	
	public List<String> fileType() {
		if (spdxFile == null && error != null) {
			ArrayList<String> retval = new ArrayList<String>();
			retval.add("Error getting SPDX file information: "+error.getMessage());
			return retval;
		} else {
			if (spdxFile != null && spdxFile.getFileTypes() != null && 
					spdxFile.getFileTypes().length > 0) {	
				ArrayList<String> retval = new ArrayList<String>();
				FileType[] fileTypes = spdxFile.getFileTypes();
				for (int i = 0; i < fileTypes.length; i++) {
					retval.add(SpdxFile.FILE_TYPE_TO_TAG.get(fileTypes[i]));
				}
				Collections.sort(retval);
				return retval;
			} else {
				return null;
			}
		}
	}
	
	public String checksum() {
		if (spdxFile == null && error != null) {
			return "Error getting SPDX file information: "+error.getMessage();
		}
		if (spdxFile != null) {
			return spdxFile.getSha1();
		} else {
			return null;
		}
	}
	
	public String licenseConcluded() {
		if (spdxFile == null && error != null) {
			return "Error getting SPDX file information: "+error.getMessage();
		}
		if (spdxFile != null) {
			return spdxFile.getLicenseConcluded().toString();
		} else {
			return null;
		}
	}
	
	public String licenseComments() {
		if (spdxFile == null && error != null) {
			return "Error getting SPDX file information: "+error.getMessage();
		}
		if (spdxFile != null) {
			return spdxFile.getLicenseComments();
		} else {
			return null;
		}
	}
	
	public List<String> licenseInfoInFile() {
		ArrayList<String> retval = new ArrayList<String>();
		if (spdxFile == null && error != null) {
			retval.add("Error getting SPDX file information: "+error.getMessage());
		}
		if (spdxFile != null) {
			AnyLicenseInfo[] licenseInfos = spdxFile.getLicenseInfoFromFiles();
			for (int i = 0; i < licenseInfos.length; i++) {
				retval.add(licenseInfos[i].toString());
			}
		}
		return retval;
	}
	
	public String copyrightText() {
		if (spdxFile == null && error != null) {
			return "Error getting SPDX file information: "+error.getMessage();
		}
		if (spdxFile != null) {
			return spdxFile.getCopyrightText();
		} else {
			return null;
		}
	}
	
	public List<ProjectContext> artifactOf() {
		ArrayList<ProjectContext> retval = new ArrayList<ProjectContext>();
		if (spdxFile == null && error != null) {
			retval.add(new ProjectContext(error));
		}
		if (spdxFile != null) {
			DoapProject[] projects = spdxFile.getArtifactOf();
			if (projects != null) {
				for (int i = 0; i < projects.length; i++) {
					retval.add(new ProjectContext(projects[i]));
				}
			}
		}
		return retval;
	}
	
	public String comment() {
		if (spdxFile != null) {
			return spdxFile.getComment();
		} else {
			return null;
		}
	}
	
	public String noticeText() {
		if (spdxFile != null) {
			return spdxFile.getNoticeText();
		} else {
			return null;
		}
	}
	
	public List<String> contributors() {
		if (spdxFile == null || spdxFile.getFileContributors() == null) {
			return null;
		}
		return Arrays.asList(spdxFile.getFileContributors());
	}
	
	public List<String> fileDependencies() {
		if (spdxFile == null || spdxFile.getFileContributors() == null) {
			return null;
		}
		ArrayList<String> retval = new ArrayList<String>();
		@SuppressWarnings("deprecation")
		SpdxFile[] dep = this.spdxFile.getFileDependencies();
		for (int i = 0; i < dep.length; i++) {
			retval.add(dep[i].getName());
		}
		return retval;
	}

}
