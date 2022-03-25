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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.license.AnyLicenseInfo;
import org.spdx.rdfparser.model.Annotation;
import org.spdx.rdfparser.model.Checksum;
import org.spdx.rdfparser.model.DoapProject;
import org.spdx.rdfparser.model.Relationship;
import org.spdx.rdfparser.model.SpdxFile;
import org.spdx.rdfparser.model.SpdxFile.FileType;
import org.spdx.rdfparser.model.SpdxSnippet;

import com.google.common.collect.Lists;

/**
 * Context for describing SPDX Files
 *
 * @author Gary O'Neall
 *
 */
public class FileContext {

	SpdxFile spdxFile = null;
	Exception error = null;
	private Map<String, String> spdxIdToUrl;
	private Map<String, List<SpdxSnippet>> fileIdToSnippets;

	/**
	 * @param SpdxFile
	 */
	public FileContext(SpdxFile SpdxFile, Map<String, String> spdxIdToUrl,
			Map<String, List<SpdxSnippet>> fileIdToSnippets) {
		this.spdxFile = SpdxFile;
		this.spdxIdToUrl = spdxIdToUrl;
		this.fileIdToSnippets = fileIdToSnippets;
	}

	/**
	 * @param e
	 */
	public FileContext(InvalidSPDXAnalysisException e) {
		this.error = e;
	}

	public String fileName() {
		if (spdxFile == null && error != null) {
			return "Error getting SPDX file information: "+ error.getMessage();
		}
		if (spdxFile != null) {
			return spdxFile.getName();
		} else {
			return null;
		}
	}

	public String spdxId() {
		if (spdxFile == null) {
			return "Error getting SPDX file information: "+ (error != null ? error.getMessage() : "null");
		}
		return spdxFile.getId();
	}

	public List<String> fileInfoErrorWithRetrievalList(String errorMessage) {
		List<String> retval = Lists.newArrayList();
		retval.add(errorMessage);
		return retval;
	}

	public List<String> checksums() {

		if (spdxFile == null) {
			return fileInfoErrorWithRetrievalList("Error getting SPDX file information: "+ (error != null ? error.getMessage() : "null"));
		}
		Checksum[] fileChecksums = this.spdxFile.getChecksums();
		if (fileChecksums == null || fileChecksums.length ==0) {
			return null;
		}
		List<String> retval = Lists.newArrayList();
		for (int i = 0; i < fileChecksums.length; i++) {
			retval.add(Checksum.CHECKSUM_ALGORITHM_TO_TAG.get(fileChecksums[i].getAlgorithm())+
					" "+fileChecksums[i].getValue());
		}
		Collections.sort(retval);
		return retval;
	}

	public List<String> fileType() {
		if (spdxFile == null && error != null) {
			return fileInfoErrorWithRetrievalList("Error getting SPDX file information: "+error.getMessage());
		} else {
			if (spdxFile != null && spdxFile.getFileTypes() != null &&
					spdxFile.getFileTypes().length > 0) {
				List<String> retval = Lists.newArrayList();
				FileType[] fileTypes = spdxFile.getFileTypes();
				for (int i = 0; i < fileTypes.length; i++) {
					retval.add(fileTypes[i].getTag());
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
		List<String> retval = Lists.newArrayList();
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

	@SuppressWarnings("deprecation")
	public List<ProjectContext> artifactOf() {
		List<ProjectContext> retval = Lists.newArrayList();
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

	public List<String> attributionText() {
		if (spdxFile == null || spdxFile.getAttributionText() == null) {
			return null;
		}
		return Arrays.asList(spdxFile.getAttributionText());
	}

	public List<String> fileDependencies() {
		if (spdxFile == null || spdxFile.getFileContributors() == null) {
			return null;
		}
		List<String> retval = Lists.newArrayList();
		@SuppressWarnings("deprecation")
		SpdxFile[] dep = this.spdxFile.getFileDependencies();
		for (int i = 0; i < dep.length; i++) {
			retval.add(dep[i].getName());
		}
		return retval;
	}

	public List<RelationshipContext> fileRelationships() {
	    List<RelationshipContext> retval = Lists.newArrayList();
	    if (this.spdxFile != null) {
		    Relationship[] relationships = spdxFile.getRelationships();
		    if (relationships != null) {
		        Arrays.sort(relationships);

	    		for (Relationship relationship : relationships) {
	    		    retval.add(new RelationshipContext(relationship, spdxIdToUrl));
	    		}
		    }
	    }
		return retval;
	}

	public List<AnnotationContext> fileAnnotations() {
		List<AnnotationContext> retval  = Lists.newArrayList();
		if (this.spdxFile != null) {
			Annotation[] annotations = spdxFile.getAnnotations();
			if (annotations != null) {
				Arrays.sort(annotations);
				for (Annotation annotation : annotations) {
					retval.add(new AnnotationContext(annotation));
				}
			}
		}
		return retval;
	}

	public List<ElementContext> fileSnippets() {
		List<ElementContext> retval = Lists.newArrayList();
		if (this.spdxFile != null) {
			List<SpdxSnippet> snippets = this.fileIdToSnippets.get(this.spdxFile.getId());
			if (snippets != null) {
				for (SpdxSnippet snippet:snippets) {
					retval.add(new ElementContext(snippet, spdxIdToUrl));
				}
			}
		}
		return retval;
	}
}