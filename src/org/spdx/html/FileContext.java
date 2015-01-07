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
import java.util.List;

import org.spdx.rdfparser.DOAPProject;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SPDXFile;
import org.spdx.rdfparser.license.AnyLicenseInfo;

/**
 * Context for describing SPDX Files
 * 
 * @author Gary O'Neall
 *
 */
public class FileContext {
	
	SPDXFile spdxFile = null;
	Exception error = null;

	/**
	 * @param spdxFile
	 */
	public FileContext(SPDXFile spdxFile) {
		this.spdxFile = spdxFile;
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
	
	public String fileType() {
		if (spdxFile == null && error != null) {
			return "Error getting SPDX file information: "+error.getMessage();
		}
		if (spdxFile != null) {
			return spdxFile.getType();
		} else {
			return null;
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
			return spdxFile.getConcludedLicenses().toString();
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
			AnyLicenseInfo[] licenseInfos = spdxFile.getSeenLicenses();
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
			return spdxFile.getCopyright();
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
			DOAPProject[] projects = spdxFile.getArtifactOf();
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
		if (spdxFile == null || spdxFile.getContributors() == null) {
			return null;
		}
		return Arrays.asList(spdxFile.getContributors());
	}
	
	public List<String> fileDependencies() {
		if (spdxFile == null || spdxFile.getFileDependencies() == null) {
			return null;
		}
		ArrayList<String> retval = new ArrayList<String>();
		SPDXFile[] dep = this.spdxFile.getFileDependencies();
		for (int i = 0; i < dep.length; i++) {
			retval.add(dep[i].getName());
		}
		return retval;
	}

}
