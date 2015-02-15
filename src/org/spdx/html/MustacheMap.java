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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SPDXReview;
import org.spdx.rdfparser.license.AnyLicenseInfo;
import org.spdx.rdfparser.license.ExtractedLicenseInfo;
import org.spdx.rdfparser.license.SimpleLicensingInfo;
import org.spdx.rdfparser.model.Annotation;
import org.spdx.rdfparser.model.ExternalDocumentRef;
import org.spdx.rdfparser.model.Relationship;
import org.spdx.rdfparser.model.SpdxDocument;
import org.spdx.rdfparser.model.SpdxFile;
import org.spdx.rdfparser.model.SpdxItem;
import org.spdx.rdfparser.model.SpdxPackage;

/**
 * Provides a hashmap which maps the Mustache template strings to SPDX Document
 * methods and strings.  The constants are used in the SpdxHTMLTemplate.html file
 * in the resources directory.
 * 
 * Note that the Mustache variable names are the tag values in the SPDX specification
 * 
 * @author Gary O'Neall
 *
 */
public class MustacheMap {

	public static HashMap<String, Object> buildDocMustachMap(SpdxDocument doc,
			HashMap<String, String> spdxIdToUrl) throws InvalidSPDXAnalysisException {
		HashMap<String, Object>  retval = new HashMap<String, Object>();
		// Document level information
		retval.put("documentName", doc.getName());
		retval.put("documentUri", doc.getDocumentUri());
		retval.put("specVersion", doc.getSpecVersion());
		retval.put("dataLicense", getDataLicenseName(doc));
		retval.put("creationInfo", new CreatorInfoContext(doc));
		retval.put("docComment", doc.getComment());
		Annotation[] sortedAnnotations = doc.getAnnotations();
		Arrays.sort(sortedAnnotations);
		List<Annotation> annotations = Arrays.asList(sortedAnnotations);
		retval.put("annotations", annotations);
		List<RelationshipContext> relationships = getRelationshipContexts(
				doc.getRelationships(), spdxIdToUrl);
		retval.put("relationships", relationships);
		ExternalDocumentRef[] sortedDocRefs = doc.getExternalDocumentRefs();
		Arrays.sort(sortedDocRefs);
		List<ExternalDocumentRef> externalDocumentReferences = 
				Arrays.asList(sortedDocRefs);
		retval.put("externalDocRelationships", externalDocumentReferences);
		retval.put("reviewed", getReviewers(doc));
		SpdxItem[] describedItems = doc.getDocumentDescribes();
		Arrays.sort(describedItems, new Comparator<SpdxItem>() {

			@Override
			public int compare(SpdxItem o1, SpdxItem o2) {
				if (o1 == null || o1.getId() == null) {
					if (o2 != null && o2.getId() != null) {
						return 1;
					}
				}
				if (o2 == null || o2.getId() == null) {
					return -1;
				}
				return o1.getId().compareTo(o2.getId());
			}
			
		});
		ArrayList<ElementContext> describedPkgs = new ArrayList<ElementContext>();
		ArrayList<ElementContext> describedFiles = new ArrayList<ElementContext>();
		for (int i = 0; i < describedItems.length; i++) {
			if (describedItems[i] instanceof SpdxPackage) {
				describedPkgs.add(new ElementContext(describedItems[i], spdxIdToUrl));
			} else if (describedItems[i] instanceof SpdxFile) {
				describedFiles.add(new ElementContext(describedItems[i], spdxIdToUrl));
			}
		}
		
		retval.put("describesPackage", describedPkgs);
		retval.put("describesFile", describedFiles);
		retval.put("hasExtractedLicensingInfo", getExtractedLicensingInfo(doc, spdxIdToUrl));
		return retval;
	}
	
	/**
	 * @param relationships
	 * @return
	 */
	private static List<RelationshipContext> getRelationshipContexts(
			Relationship[] relationships, HashMap<String, String> spdxIdToUrl) {
		Arrays.sort(relationships);
		ArrayList<RelationshipContext> retval = new ArrayList<RelationshipContext>();
		if (relationships == null) {
			return retval;
		}
		 for (int i = 0; i < relationships.length; i++) {
			 retval.add(new RelationshipContext(relationships[i], spdxIdToUrl));
		 }
		 return retval;
	}

	/**
	 * @param doc
	 * @param files
	 * @param spdxIdToUrl
	 * @return
	 * @throws InvalidSPDXAnalysisException 
	 */
	public static HashMap<String, Object> buildDocFileMustacheMap(
			SpdxDocument doc, SpdxFile[] files,
			HashMap<String, String> spdxIdToUrl) throws InvalidSPDXAnalysisException {
		HashMap<String, Object> retval = new HashMap<String, Object>();
		retval.put("about", "SPDX Document "+doc.getName());
		SpdxItem[] describedItems = doc.getDocumentDescribes();
		Arrays.sort(describedItems, new Comparator<SpdxItem>() {

			@Override
			public int compare(SpdxItem o1, SpdxItem o2) {
				if (o1 == null || o1.getId() == null) {
					if (o2 != null && o2.getId() != null) {
						return 1;
					}
				}
				if (o2 == null || o2.getId() == null) {
					return -1;
				}
				return o1.getId().compareTo(o2.getId());
			}
			
		});
		ArrayList<FileContext> describedFiles = new ArrayList<FileContext>();
		for (int i = 0; i < describedItems.length; i++) {
			if (describedItems[i] instanceof SpdxFile) {
				describedFiles.add(new FileContext((SpdxFile)describedItems[i]));
			}
		}
		retval.put("hasFile", describedFiles);
		return retval;
	}
	
	private static ArrayList<ExtractedLicensingInfoContext> getExtractedLicensingInfo(SpdxDocument doc,
			HashMap<String, String> spdxIdToUrl) {
		ArrayList<ExtractedLicensingInfoContext> retval = new ArrayList<ExtractedLicensingInfoContext>();
		try {
			ExtractedLicenseInfo[] extractedLicenseInfos = doc.getExtractedLicenseInfos();
			Arrays.sort(extractedLicenseInfos, new Comparator<ExtractedLicenseInfo>() {

				@Override
				public int compare(ExtractedLicenseInfo o1,
						ExtractedLicenseInfo o2) {
					if (o1 == null || o2.getLicenseId() == null) {
						if (o2 != null && o2.getLicenseId() != null) {
							return 1;
						}
					}
					if (o2 == null || o2.getLicenseId() == null) {
						return -1;
					}
					return o1.getLicenseId().compareTo(o2.getLicenseId());
				}
				
			});
			for (int i = 0;i < extractedLicenseInfos.length; i++) {
				retval.add(new ExtractedLicensingInfoContext(extractedLicenseInfos[i], spdxIdToUrl));
			}
		} catch (InvalidSPDXAnalysisException e) {
			retval.add(new ExtractedLicensingInfoContext(e));
		}
		return retval;
	}


	/**
	 * @param doc
	 * @return
	 */
	private static ArrayList<ReviewerContext> getReviewers(SpdxDocument doc) {
		ArrayList<ReviewerContext> retval = new ArrayList<ReviewerContext>();
		try {
			@SuppressWarnings("deprecation")
			SPDXReview[] reviewers = doc.getReviewers();
			if (reviewers != null) {
				for (int i = 0; i < reviewers.length; i++) {
					retval.add(new ReviewerContext(reviewers[i]));
				}
			}
		} catch (InvalidSPDXAnalysisException e) {
			retval.add(new ReviewerContext(e));
		}
		return retval;
	}


	/**
	 * @param doc
	 * @return
	 * @throws InvalidSPDXAnalysisException 
	 */
	private static String getDataLicenseName(SpdxDocument doc) throws InvalidSPDXAnalysisException {
		AnyLicenseInfo dataLicense = doc.getDataLicense();
		if (dataLicense != null) {
			if (dataLicense instanceof SimpleLicensingInfo) {
				return ((SimpleLicensingInfo)dataLicense).getName();
			} else {
				return dataLicense.toString();
			}			
		} else {
			return "NONE";
		}
	}

	/**
	 * @param doc
	 * @param spdxIdToUrl
	 * @return
	 */
	public static HashMap<String, Object> buildExtractedLicMustachMap(
			SpdxDocument doc, HashMap<String, String> spdxIdToUrl) {
		HashMap<String, Object> retval = new HashMap<String, Object>();
		retval.put("hasExtractedLicensingInfo", getExtractedLicensingInfo(doc, spdxIdToUrl));
		return retval;
	}

	/**
	 * @param pkg
	 * @param spdxIdToUrl
	 * @return
	 * @throws InvalidSPDXAnalysisException 
	 */
	public static HashMap<String, Object> buildPkgFileMap(SpdxPackage pkg,
			HashMap<String, String> spdxIdToUrl) throws InvalidSPDXAnalysisException {
		HashMap<String, Object> retval = new HashMap<String, Object>();
		retval.put("about", "SPDX Package "+pkg.getName());
		SpdxFile[] files = pkg.getFiles();
		Arrays.sort(files, new Comparator<SpdxFile>() {

			@Override
			public int compare(SpdxFile o1, SpdxFile o2) {
				if (o1 == null || o1.getName() == null) {
					if (o2 != null && o2.getName() != null) {
						return 1;
					}
				}
				if (o2 == null || o2.getName() == null) {
					return -1;
				}
				return o1.getName().compareTo(o2.getName());
			}
			
		});
		ArrayList<FileContext> alFiles = new ArrayList<FileContext>();
		for (int i = 0; i < files.length; i++) {
			if (files[i] instanceof SpdxFile) {
				alFiles.add(new FileContext((SpdxFile)files[i]));
			}
		}
		retval.put("hasFile", alFiles);
		return retval;
	}
}
