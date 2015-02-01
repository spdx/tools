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
import java.util.HashMap;

import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SPDXReview;
import org.spdx.rdfparser.license.AnyLicenseInfo;
import org.spdx.rdfparser.license.ExtractedLicenseInfo;
import org.spdx.rdfparser.license.SimpleLicensingInfo;
import org.spdx.rdfparser.model.SpdxDocument;

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

	static public HashMap<String, Object> buildMustachMap(SpdxDocument doc) throws InvalidSPDXAnalysisException {
		HashMap<String, Object>  retval = new HashMap<String, Object>();
		// Document level information
		retval.put("documentName", doc.getName());
		retval.put("documentUri", doc.getDocumentUri());
		retval.put("specVersion", doc.getSpecVersion());
		retval.put("dataLicense", getDataLicenseName(doc));
		retval.put("creationInfo", new CreatorInfoContext(doc));
		retval.put("docComment", doc.getComment());
		//TODO add annotations
		//TODO add relationships
		//TODO add external documents
		retval.put("reviewed", getReviewers(doc));
		//TODO change to describes elements
//		retval.put("describesPackage", new PackageContext(doc));
		retval.put("hasExtractedLicensingInfo", getExtractedLicensingInfo(doc));
		return retval;
	}
	
	private static ArrayList<ExtractedLicensingInfoContext> getExtractedLicensingInfo(SpdxDocument doc) {
		ArrayList<ExtractedLicensingInfoContext> retval = new ArrayList<ExtractedLicensingInfoContext>();
		try {
			ExtractedLicenseInfo[] extractedLicenseInfos = doc.getExtractedLicenseInfos();
			for (int i = 0;i < extractedLicenseInfos.length; i++) {
				retval.add(new ExtractedLicensingInfoContext(extractedLicenseInfos[i]));
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
}
