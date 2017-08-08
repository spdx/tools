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
package org.spdx.tools;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.regex.Pattern;

import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SPDXDocumentFactory;
import org.spdx.rdfparser.SPDXReview;
import org.spdx.rdfparser.SpdxDocumentContainer;
import org.spdx.rdfparser.SpdxRdfConstants;
import org.spdx.rdfparser.license.ExtractedLicenseInfo;
import org.spdx.rdfparser.model.Annotation;
import org.spdx.rdfparser.model.ExternalRef;
import org.spdx.rdfparser.model.Relationship;
import org.spdx.rdfparser.model.SpdxDocument;
import org.spdx.rdfparser.model.SpdxElement;
import org.spdx.rdfparser.model.SpdxFile;
import org.spdx.rdfparser.model.SpdxPackage;
import org.spdx.rdfparser.model.SpdxSnippet;
import org.spdx.spdxspreadsheet.AnnotationsSheet;
import org.spdx.spdxspreadsheet.DocumentInfoSheet;
import org.spdx.spdxspreadsheet.ExternalRefsSheet;
import org.spdx.spdxspreadsheet.NonStandardLicensesSheet;
import org.spdx.spdxspreadsheet.PackageInfoSheet;
import org.spdx.spdxspreadsheet.PerFileSheet;
import org.spdx.spdxspreadsheet.RelationshipsSheet;
import org.spdx.spdxspreadsheet.ReviewersSheet;
import org.spdx.spdxspreadsheet.SPDXSpreadsheet;
import org.spdx.spdxspreadsheet.SnippetSheet;
import org.spdx.spdxspreadsheet.SpreadsheetException;

import com.google.common.collect.Maps;

/**
 * Translates an RDF XML file to a SPDX Spreadsheet format
 * Usage: RdfToSpreadsheet rdfxmlfile.rdf spreadsheetfile.xls
 * where rdfxmlfile.rdf is a valid SPDX RDF XML file and spreadsheetfile.xls is 
 * the output SPDX spreadsheeet file.
 * @author Gary O'Neall
 *
 */
public class RdfToSpreadsheet {

	static final int MIN_ARGS = 2;
	static final int MAX_ARGS = 2;
	static Pattern datePattern = Pattern.compile(".. ... \\d\\d\\d\\d \\d\\d:\\d\\d:\\d\\d GMT$");
	public static final String NOT_SUPPORTED_STRING = "This field is not yet supported by SPDX";
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length < MIN_ARGS) {
			usage();
			return;
		}
		if (args.length > MAX_ARGS) {
			usage();
			return;
		}
		try {
			onlineFunction(args);
		} catch (OnlineToolException e){
			System.out.println(e.getMessage());
			usage();
			return;
		}
    }
	
	/**
	 * 
	 * @param args args[0] is the RDF file to be converted, args[1] is the result HTML file name
	 * @throws OnlineToolException Exception caught by JPype and displayed to the user
	 * @return Warnings of the conversion, displayed to the user
	 */
	public static List<String> onlineFunction(String[] args) throws OnlineToolException{
		// Arguments length(args length== 2 ) will checked in the Python Code
		File spdxRdfFile = new File(args[0]);
		// Output File name will be checked in the Python code for no clash, but if still found
		if (!spdxRdfFile.exists()) {
			throw new OnlineToolException("Error: File " + args[0] + " does not exist.");
		}
		File spdxSpreadsheetFile = new File(args[1]);
		if (spdxSpreadsheetFile.exists()) {
			throw new OnlineToolException("Spreadsheet file already exists - please specify a new file.");
		}
		SpdxDocument doc = null;
		try {
			doc = SPDXDocumentFactory.createSpdxDocument(args[0]);
		} catch (InvalidSPDXAnalysisException ex) {
			throw new OnlineToolException("Error creating SPDX Document: "+ex.getMessage());
		} catch (IOException e) {
			throw new OnlineToolException("Error creating SPDX Document:"+args[0]+", "+e.getMessage());
		} catch (Exception e) {
			throw new OnlineToolException("Error creating SPDX Document: "+e.getMessage(),e);
		}
		List<String> verify = new ArrayList<String>();
        if (doc != null) {
            SPDXSpreadsheet ss = null;
            try {
                ss = new SPDXSpreadsheet(spdxSpreadsheetFile, true, false);
                copyRdfXmlToSpreadsheet(doc, ss);
                verify = doc.verify();
                if (verify != null && verify.size() > 0) {
                    System.out.println("Warning: The following verifications failed for the resultant SPDX RDF file:");
                    for (int i = 0; i < verify.size(); i++) {
                        System.out.println("\t" + verify.get(i));
                    }
                }
            } catch (SpreadsheetException e) {
                throw new OnlineToolException("Error opening or writing to spreadsheet: " + e.getMessage());
            } catch (InvalidSPDXAnalysisException e) {
                throw new OnlineToolException("Error translating the RDF file: " + e.getMessage());
            } catch (Exception ex) {
                throw new OnlineToolException("Unexpected error translating the RDF to spreadsheet: " + ex.getMessage());
            } finally {
                if (ss != null) {
                    try {
                        ss.close();
                    } catch (SpreadsheetException e) {
                        throw new OnlineToolException("Error closing spreadsheet: " + e.getMessage());
                    }
                }
            }
        }else{
            throw new OnlineToolException("Error creating SPDX document reference, null reference returned");
        }
        return verify;
	}
	
	@SuppressWarnings("deprecation")
	public static void copyRdfXmlToSpreadsheet(SpdxDocument doc,
			SPDXSpreadsheet ss) throws InvalidSPDXAnalysisException, SpreadsheetException {
		if (doc == null) {
			System.out.println("Warning: No document to copy");
			return;
		}
		copyOrigins(doc, ss.getOriginsSheet());
		Map<String, ExternalRef[]> externalRefs = new TreeMap<String, ExternalRef[]>();
		Map<String, String> fileIdToPackageId = copyPackageInfo(doc.getDocumentContainer().findAllPackages(),
				ss.getPackageInfoSheet(), externalRefs);
		copyExternalRefs(externalRefs, ss.getExternalRefsSheet(), doc.getDocumentContainer());
		copyNonStdLicenses(doc.getExtractedLicenseInfos(), ss.getNonStandardLicensesSheet());
		copyPerFileInfo(doc.getDocumentContainer().findAllFiles(), ss.getPerFileSheet(), fileIdToPackageId);
		copySnippetInfo(doc.getDocumentContainer().findAllSnippets(), ss.getSnippetSheet());
		Map<String, Relationship[]> allRelationships = new TreeMap<String, Relationship[]>();
		Map<String, Annotation[]> allAnnotations = new TreeMap<String, Annotation[]>();
		allRelationships.put(doc.getId(), doc.getRelationships());
		allAnnotations.put(doc.getId(), doc.getAnnotations());
		List<SpdxElement> allElements = doc.getDocumentContainer().findAllElements();		
		for (SpdxElement element:allElements) {
			allRelationships.put(element.getId(), element.getRelationships());
			allAnnotations.put(element.getId(), element.getAnnotations());
		}
		copyRelationships(allRelationships, ss.getRelationshipsSheet());
		copyAnnotations(allAnnotations, ss.getAnnotationsSheet());
		copyReviewerInfo(doc.getReviewers(), ss.getReviewersSheet());
		ss.resizeRow();
	}

	/**
	 * Copy external references to the spreadsheet
	 * @param externalRefsMap
	 * @throws SpreadsheetException 
	 */
	private static void copyExternalRefs(Map<String, ExternalRef[]> externalRefsMap,
			ExternalRefsSheet externalRefSheet, SpdxDocumentContainer container) throws SpreadsheetException {
		String[] keys = externalRefsMap.keySet().toArray(new String[externalRefsMap.keySet().size()]);
		Arrays.sort(keys);
		for (String key:keys) {
			ExternalRef[] externalRefs = externalRefsMap.get(key);
			Arrays.sort(externalRefs);
			for (ExternalRef externalRef:externalRefs) {
				externalRefSheet.add(key, externalRef, container);
			}
		}
	}

	/**
	 * @param annotations
	 * @param annotationsSheet
	 */
	private static void copyAnnotations(Map<String, Annotation[]> annotationMap,
			AnnotationsSheet annotationsSheet) {
		for (Entry<String, Annotation[]> entry:annotationMap.entrySet()) {
			Annotation[] annotations = entry.getValue();
			Arrays.sort(annotations);
			for (int i = 0; i < annotations.length; i++) {
				annotationsSheet.add(annotations[i], entry.getKey());
			}
		}
	}

	/**
	 * @param relationships
	 * @param relationshipsSheet
	 */
	private static void copyRelationships(Map<String, Relationship[]> relationshipMap,
			RelationshipsSheet relationshipsSheet) {

		String[] keys = relationshipMap.keySet().toArray(new String[relationshipMap.keySet().size()]);
		Arrays.sort(keys);
		for (String key:keys) {
			Relationship[] relationships = relationshipMap.get(key);
			Arrays.sort(relationships);
			for (int i = 0; i < relationships.length; i++) {
				relationshipsSheet.add(relationships[i], key);
			}
		}
	}

	private static void copyReviewerInfo(SPDXReview[] reviewers,
			ReviewersSheet reviewersSheet) throws InvalidSPDXAnalysisException {
		DateFormat dateFormat = new SimpleDateFormat(SpdxRdfConstants.SPDX_DATE_FORMAT);
		Arrays.sort(reviewers);
		for (int i = 0; i < reviewers.length; i++) {
			String reviewerName = reviewers[i].getReviewer();
			Date reviewDate = null;
			String dateString = reviewers[i].getReviewDate();
			if (dateString != null && !dateString.isEmpty()) {
				try {
					dateString = dateString.trim();
					reviewDate = dateFormat.parse(dateString);
				} catch (Exception ex) {
					throw(new InvalidSPDXAnalysisException("Invalid reviewer date format for reviewer "+reviewers[i]));
				}
			}
			reviewersSheet.addReviewer(reviewerName, reviewDate, reviewers[i].getComment());
		}
	}

	private static void copyPerFileInfo(List<SpdxFile> fileList,
			PerFileSheet perFileSheet, Map<String, String> fileIdToPackageId) {            
            Collections.sort(fileList);
            /* Print out sorted files */            
		for (SpdxFile file : fileList) {
			perFileSheet.add(file, fileIdToPackageId.get(file.getId()));
		}
	}
	
	private static void copySnippetInfo(List<SpdxSnippet> snippets,
			SnippetSheet snippetSheet) throws SpreadsheetException {            
            Collections.sort(snippets);
            /* Print out sorted files */            
		for (SpdxSnippet snippet : snippets) {
			snippetSheet.add(snippet);
		}
	}

	private static void copyNonStdLicenses(ExtractedLicenseInfo[] nonStandardLicenses,
			NonStandardLicensesSheet nonStandardLicensesSheet) {
		Arrays.sort(nonStandardLicenses, new Comparator<ExtractedLicenseInfo>() {

			@Override
			public int compare(ExtractedLicenseInfo o1, ExtractedLicenseInfo o2) {
				int result = 0;
				if (o1.getName() != null && !(o1.getName().trim().isEmpty())) {
					if (o2.getName() != null && !(o2.getName().trim().isEmpty())) {
						result = o1.getName().compareToIgnoreCase(o2.getName());
					} else {
						result = 1;
					}
				} else {
					result = -1;
				}
				if (result == 0) {
					result = o1.getLicenseId().compareToIgnoreCase(o2.getLicenseId());
				}
				return result;
			}
			
		});
		for(int i = 0; i < nonStandardLicenses.length; i++) {
			nonStandardLicensesSheet.add(nonStandardLicenses[i].getLicenseId(), nonStandardLicenses[i].getExtractedText(), 
					nonStandardLicenses[i].getName(),
					nonStandardLicenses[i].getSeeAlso(),
					nonStandardLicenses[i].getComment());
		}
	}

	private static Map<String, String> copyPackageInfo(List<SpdxPackage> packages,
			PackageInfoSheet packageInfoSheet, Map<String, ExternalRef[]> externalRefs) throws InvalidSPDXAnalysisException {
		Map<String, String> fileIdToPkgId = Maps.newHashMap();
		Collections.sort(packages);
		Iterator<SpdxPackage> iter = packages.iterator();
		while (iter.hasNext()) {
			SpdxPackage pkg = iter.next();
			String pkgId = pkg.getId();
			SpdxFile[] files = pkg.getFiles();
			for (int i = 0; i < files.length; i++) {
				String fileId = files[i].getId();
				String pkgIdsForFile = fileIdToPkgId.get(fileId);
				if (pkgIdsForFile == null) {
					pkgIdsForFile = pkgId;
				} else {
					pkgIdsForFile = pkgIdsForFile + ", " + pkgId;
				}
				fileIdToPkgId.put(fileId, pkgIdsForFile);
			}
			ExternalRef[] pkgExternalRefs = pkg.getExternalRefs();
			if (pkgExternalRefs != null && pkgExternalRefs.length > 0) {
				externalRefs.put(pkgId, pkgExternalRefs);
			}
			packageInfoSheet.add(pkg);
		}
		return fileIdToPkgId;
	}

	private static void copyOrigins(SpdxDocument doc, DocumentInfoSheet originsSheet) throws SpreadsheetException {
		originsSheet.addDocument(doc);
	}

	private static void usage() {
		System.out.println("Usage: RdfToSpreadsheet rdfxmlfile.rdf spreadsheetfile.xls\n"+
				"where rdfxmlfile.rdf is a valid SPDX RDF XML file and spreadsheetfile.xls is\n"+
				"the output SPDX spreadsheeet file.");
	}
}
