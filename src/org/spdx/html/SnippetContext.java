/**
 * Copyright (c) 2016 Source Auditor Inc.
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
import java.util.List;
import java.util.Map;

import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.license.AnyLicenseInfo;
import org.spdx.rdfparser.model.Annotation;
import org.spdx.rdfparser.model.Relationship;
import org.spdx.rdfparser.model.SpdxFile;
import org.spdx.rdfparser.model.SpdxSnippet;
import org.spdx.rdfparser.model.pointer.StartEndPointer;

import com.google.common.collect.Lists;

/**
 * Mustache Context for a snippet
 * @author Gary O'Neall
 *
 */
public class SnippetContext {

	private SpdxSnippet snippet;
	private Map<String, String> spdxIdToUrl;
	private Exception error = null;

	/**
	 * @param snippet
	 * @param spdxIdToUrl
	 */
	public SnippetContext(SpdxSnippet snippet, Map<String, String> spdxIdToUrl) {
		this.snippet = snippet;
		this.spdxIdToUrl = spdxIdToUrl;
	}

	/**
	 * @param e
	 */
	public SnippetContext(InvalidSPDXAnalysisException e) {
		error = e;
	}

	public String spdxId() {
		if (snippet == null) {
			return "Error getting SPDX snippet information: "+ (error != null ? error.getMessage() : "null");
		}
		return snippet.getId();
	}
	
	public String byteRange() {
		if (snippet == null) {
			return "Error getting SPDX snippet information: "+ (error != null ? error.getMessage() : "null");
		}
		try {
			StartEndPointer byteRange = snippet.getByteRange();
			if (byteRange == null) {
				return null;
			} else {
				return byteRange.toString();
			}
		} catch (InvalidSPDXAnalysisException e) {
			return "Error getting SPDX snippet byte range: "+ e.getMessage();
		}
	}
	
	public String lineRange() {
		if (snippet == null) {
			return "Error getting SPDX snippet information: "+ (error != null ? error.getMessage() : "null");
		}
		try {
			StartEndPointer lineRange = snippet.getLineRange();
			if (lineRange == null) {
				return null;
			} else {
				return lineRange.toString();
			}
		} catch (InvalidSPDXAnalysisException e) {
			return "Error getting SPDX snippet line range: "+ e.getMessage();
		}
	}
	
	public String licenseConcluded() {
		if (snippet == null) {
			return "Error getting SPDX snippet information: "+ (error != null ? error.getMessage() : "null");
		}
		if (snippet != null) {
			return snippet.getLicenseConcluded().toString();
		} else {
			return null;
		}
	}
	
	public String licenseComments() {
		if (snippet == null) {
			return "Error getting SPDX snippet information: "+ (error != null ? error.getMessage() : "null");
		}
		if (snippet != null) {
			return snippet.getLicenseComments();
		} else {
			return null;
		}
	}
	
	public List<String> licenseInfoInSnippet() {
		List<String> retval = Lists.newArrayList();
		if (snippet == null && error != null) {
			retval.add("Error getting SPDX snippet information: "+error.getMessage());
		}
		if (snippet != null) {
			AnyLicenseInfo[] licenseInfos = snippet.getLicenseInfoFromFiles();
			for (int i = 0; i < licenseInfos.length; i++) {
				retval.add(licenseInfos[i].toString());
			}
		}
		return retval;
	}
	
	public String copyrightText() {
		if (snippet == null) {
			return "Error getting SPDX snippet information: "+ (error != null ? error.getMessage() : "null");
		}
		if (snippet != null) {
			return snippet.getCopyrightText();
		} else {
			return null;
		}
	}
	
	public String comment() {
		if (snippet != null) {
			return snippet.getComment();
		} else {
			return null;
		}
	}
	
	public List<RelationshipContext> snippetRelationships() {
	    List<RelationshipContext> retval = Lists.newArrayList();
	    if (this.snippet != null) {
	    	Relationship[] relationships = snippet.getRelationships();
		    if (relationships != null) {
		        Arrays.sort(relationships);
			
	    		for (Relationship relationship : relationships) {
	    		    retval.add(new RelationshipContext(relationship, spdxIdToUrl));
	    		}
		    }
	    }	    
		return retval;
	}
	
	public List<AnnotationContext> snippetAnnotations() {
		List<AnnotationContext> retval  = Lists.newArrayList();
		if (this.snippet != null) {
			Annotation[] annotations = snippet.getAnnotations();
			if (annotations != null) {
				Arrays.sort(annotations);
				for (Annotation annotation : annotations) {
					retval.add(new AnnotationContext(annotation));
				}
			}
		}
		return retval;
	}
	
	public String snippetFromFile() {
		if (snippet == null) {
			return "Error getting SPDX snippet information: "+ (error != null ? error.getMessage() : "null");
		}
		try {
			SpdxFile fromFile = this.snippet.getSnippetFromFile();
			if (fromFile.getId() != null && fromFile.getName() != null) {
				return fromFile.getName() + " (" + fromFile.getId() + ")";
			} else {
				return "[UNKNOWN]";
			}
		} catch (InvalidSPDXAnalysisException e) {
			return "Error getting SPDX snippet from file: " + e.getMessage();
		}
	}
	
}
