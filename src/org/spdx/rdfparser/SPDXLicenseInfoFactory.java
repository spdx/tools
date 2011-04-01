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

import java.util.ArrayList;
import java.util.HashSet;

import org.spdx.spdxspreadsheet.InvalidLicenseStringException;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * Factory for creating SPDXLicenseInfo objects from a Jena model
 * @author Gary O'Neall
 *
 */
public class SPDXLicenseInfoFactory {
	
	static  final String[] STANDARD_LICENSE_IDS = new String[] {
		"AFL-3","AFL-1.1","AFL-1.2","AFL-2","AFL-2.1","APL-1",
		"Apache-1","Apache-1.1","Apache-2","APSL-1","APSL-1.1",
		"APSL-1.2","APSL-2","Artistic-1","Artistic-2","AAL",
		"BSL-1","BSD-2-Clause","BSD-3-Clause","BSD-4-Clause",
		"CECILL-1","CECILL-2","CECILL-B","CECILL-C","ClArtistic",
		"CDDL-1","CPAL-1","CPL-1","CATOSL-1.1","CC-BY-1","CC-BY-2",
		"CC-BY-2.5","CC-BY-3","CC-BY-ND-1","CC-BY-ND-2","CC-BY-ND-2.5",
		"CC-BY-ND-3","CC-BY-NC-1","CC-BY-NC-2","CC-BY-NC-2.5","CC-BY-NC-3",
		"CC-BY-NC-ND-1","CC-BY-NC-ND-2","CC-BY-NC-ND-2.5","CC-BY-NC-ND-3",
		"CC-BY-NC-SA-1","CC-BY-NC-SA-2","CC-BY-NC-SA-2.5","CC-BY-NC-SA-3",
		"CC-BY-SA-1","CC-BY-SA-2","CC-BY-SA-2.5","CC-BY-SA-3","CUA-OPL-1",
		"EPL-1","eCos-2","ECL-1","ECL-2","EFL-1","EFL-2","Entessa",
		"ErlPL-1.1","EUDatagrid","EUPL-1","EUPL-1.1","Fair","Frameworx-1",
		"AGPL-3","GFDL-1.2","GFDL-1.2","GFDL-1.3","GPL-1","GPL-1+",
		"GPL-2","GPL-2+","GPL-2-with-autoconf-exception","GPL-2-with-bison-exception",
		"GPL-2-with-classpath-exception","GPL-2-with-GCC-exception",
		"GPL-2-with-font-exception","GPL-3","GPL-3+","GPL-3-with-autoconf-exception",
		"GPL-3-with-GCC-exception","LGPL-2.1","LGPL-2.1+","LGPL-3",
		"LGPL-3+","LGPL-2","LGPL-2+","LGPL+","gSOAP-1.3b","HPND",
		"IPL-1","IPA","ISC","LPPL-1","LPPL-1.1","LPPL-1.2","LPPL-1.3c",
		"Libpng","LPL-1.02","MS-PL","MS-RL","MirOS","MIT","Motosoto",
		"MPL-1","MPL-1.1","Multics","NASA-1.3","Nauman","NGPL","Nokia",
		"NPOSL-3","NTP","OCLC-2","OGTSL","OSL-1","OSL-2","OSL-3","OLDAP-2.8",
		"OpenSSL","PHP-3","PostgreSQL","Python-CNRI","Python","QPL-1",
		"RPSL-1","RPL-1.5","RHeCos-1.1","RSCPL","Ruby","OFL-1.1","Simple-2",
		"Sleepycat","SugarCRM-1.1.3","SPL","Watcom-1","NCSA","VSL-1",
		"W3C","WXwindows","Xnet","XFree86-1.1","YPL-1.1","Zimbra-1.3",
		"Zlib","ZPL-1.1","ZPL-2","ZPL-2.1"
	};
	
	static final HashSet<String> STANDARD_LICENSE_ID_SET = new HashSet<String>();
	
	static {
		for (int i = 0; i < STANDARD_LICENSE_IDS.length; i++) {
			STANDARD_LICENSE_ID_SET.add(STANDARD_LICENSE_IDS[i]);
		}			
	}
	
	/**
	 * Create the appropriate SPDXLicenseInfo from the model and node provided.
	 * The appropriate SPDXLicenseInfo subclass object will be chosen based on
	 * the class (rdf type) of the node
	 * @param model
	 * @param node
	 * @return
	 */
	static SPDXLicenseInfo getLicenseInfoFromModel(Model model, Node node) throws InvalidSPDXAnalysisException {
		if (!node.isURI() && !node.isBlank()) {
			throw(new InvalidSPDXAnalysisException("Can not create a LicenseInfo from a literal node"));
		}
		// find the subclass
		Node rdfTypePredicate = model.getProperty(SPDXAnalysis.RDF_NAMESPACE, 
				SPDXAnalysis.RDF_PROP_TYPE).asNode();
		Triple m = Triple.createMatch(node, rdfTypePredicate, null);
		ExtendedIterator<Triple> tripleIter = model.getGraph().find(m);	// find the type(s)
		if (!tripleIter.hasNext()) {
			throw(new InvalidSPDXAnalysisException("There is no type associated with a licenseInfo"));
		}
		Triple triple = tripleIter.next();
		if (tripleIter.hasNext()) {
			throw(new InvalidSPDXAnalysisException("More than one type associated with a licenseInfo"));
		}
		Node typeNode = triple.getObject();
		if (!typeNode.isURI()) {
			throw(new InvalidSPDXAnalysisException("Invalid type for licenseInfo - not a URI"));
		}
		// need to parse the URI
		String typeUri = typeNode.getURI();
		if (!typeUri.startsWith(SPDXAnalysis.SPDX_NAMESPACE)) {
			throw(new InvalidSPDXAnalysisException("Invalid type for licenseInfo - not an SPDX type"));
		}
		String type = typeUri.substring(SPDXAnalysis.SPDX_NAMESPACE.length());
		if (type.equals(SPDXAnalysis.CLASS_SPDX_CONJUNCTIVE_LICENSE_SET)) {
			return new SPDXConjunctiveLicenseSet(model, node);
		} else if (type.equals(SPDXAnalysis.CLASS_SPDX_DISJUNCTIVE_LICENSE_SET)) {
			return new SPDXDisjunctiveLicenseSet(model, node);
		}else if (type.equals(SPDXAnalysis.CLASS_SPDX_NON_STANDARD_LICENSE)) {
			return new SPDXNonStandardLicense(model, node);
		}else if (type.equals(SPDXAnalysis.CLASS_SPDX_STANDARD_LICENSE)) {
			return new SPDXStandardLicense(model, node);
		} else {
			throw(new InvalidSPDXAnalysisException("Invalid type for licenseInfo '"+type+"'"));
		}
	}
	
	/**
	 * Parses a license string and converts it into a SPDXLicenseInfo object
	 * Syntax - A license set must start and end with a parenthesis "("
	 * 			A conjunctive license set will have and AND after the first
	 *				licenseInfo term
	 * 			A disjunctive license set will have an OR after the first 
	 *				licenseInfo term
	 *			If there is no And or Or, then it is converted to a simple
	 *				license type (standard or non-standard)
	 *			A space or tab must be used between license ID's and the 
	 *				keywords AND and OR
	 *			A licenseID must NOT be "AND" or "OR"
	 * @param licenseString String conforming to the syntax
	 * @return an SPDXLicenseInfo created from the string
	 * @throws InvalidLicenseStringException 
	 */
	public static SPDXLicenseInfo parseSPDXLicenseString(String licenseString) throws InvalidLicenseStringException {
		String parseString = licenseString.trim();
		if (parseString.startsWith("(")) {
			if (!parseString.endsWith(")")) {
				throw(new InvalidLicenseStringException("Missing end ')'"));
			}
			// this will be treated some form of License Set
			parseString = parseString.substring(1, parseString.length()-1).trim();
			return parseLicenseSet(parseString);
		} else {
			// this is either a standard license or a non-standard license
			int startOfIDPos = skipWhiteSpace(parseString, 0);
			int endOfIDPos = skipNonWhiteSpace(parseString, startOfIDPos);
			String licenseID = parseString.substring(startOfIDPos, endOfIDPos);
			if (isStandardLicenseID(licenseID)) {
				return new SPDXStandardLicense(licenseID, null, null, null, null, null, null);
			} else {
				return new SPDXNonStandardLicense(licenseID, null);
			}
		}
	}

	/**
	 * Parses a license set which consists of a list of LicenseInfo strings
	 * @param parseString
	 * @return
	 * @throws InvalidLicenseStringException 
	 */
	private static SPDXLicenseInfo parseLicenseSet(String parseString) throws InvalidLicenseStringException {
		boolean isConjunctive = false;
		boolean isDisjunctive = false;
		ArrayList<SPDXLicenseInfo> licenseInfoList = new ArrayList<SPDXLicenseInfo>();
		int pos = 0;	// character position
		while (pos < parseString.length()) {
			// skip white space
			pos = skipWhiteSpace(parseString, pos);
			if (pos >= parseString.length()) {
				break;	// we are done
			}
			// collect the license information
			if (parseString.charAt(pos) == '(') {
				int startOfSet = pos + 1;
				pos = findEndOfSet(parseString, pos);
				if (pos > parseString.length() || parseString.charAt(pos) != ')') {
					throw(new InvalidLicenseStringException("Missing end ')'"));
				}
				licenseInfoList.add(parseLicenseSet(parseString.substring(startOfSet, pos)));
				pos++;
			} else {
				// a license ID
				int startOfID = pos;
				pos = skipNonWhiteSpace(parseString, pos);
				String licenseID = parseString.substring(startOfID, pos);
				if (isStandardLicenseID(licenseID)) {
					licenseInfoList.add(new SPDXStandardLicense(null, licenseID, null, null, null, null, null));
				} else {
					licenseInfoList.add(new SPDXNonStandardLicense(licenseID, null));
				}
			}
			if (pos >= parseString.length()) {
				break;	// done
			}
			// consume the AND or the OR
			// skip more whitespace
			pos = skipWhiteSpace(parseString, pos);
			if (parseString.charAt(pos) == 'A' || parseString.charAt(pos) == 'a') {
				// And
				if (pos + 4 >= parseString.length() || 
						!parseString.substring(pos, pos+4).toUpperCase().equals("AND ")) {
					throw(new InvalidLicenseStringException("Expecting an AND"));
				}
				isConjunctive = true;
				pos = pos + 4;
			} else if (parseString.charAt(pos) == 'O' || parseString.charAt(pos) == 'o') {
				// or
				if (pos + 3 >= parseString.length() || 
						!parseString.substring(pos, pos+3).toUpperCase().equals("OR ")) {
					throw(new InvalidLicenseStringException("Expecting an OR"));
				}
				isDisjunctive = true;
				pos = pos + 3;
			} else {
				throw(new InvalidLicenseStringException("Expecting an AND or an OR"));
			}
		}
		if (isConjunctive && isDisjunctive) {
			throw(new InvalidLicenseStringException("Can not have both AND's and OR's inside the same set of parenthesis"));
		}
		SPDXLicenseInfo[] licenseInfos = new SPDXLicenseInfo[licenseInfoList.size()];
		licenseInfos = licenseInfoList.toArray(licenseInfos);
		if (isConjunctive) {
			return new SPDXConjunctiveLicenseSet(licenseInfos);
		} else if (isDisjunctive) {
			return new SPDXDisjunctiveLicenseSet(licenseInfos);
		} else {
			throw(new InvalidLicenseStringException("Missing AND or OR inside parenthesis"));
		}
	}

	/**
	 * @param parseString
	 * @return
	 * @throws InvalidLicenseStringException 
	 */
	private static int findEndOfSet(String parseString, int pos) throws InvalidLicenseStringException {
		if (parseString.charAt(pos) != '(') {
			throw(new InvalidLicenseStringException("Expecting '('"));
		}
		int retval = pos;
		retval++;
		while (retval < parseString.length() && parseString.charAt(retval) != ')') {
			if (parseString.charAt(retval) == '(') {
				retval = findEndOfSet(parseString, retval) + 1;
			} else {
				retval++;
			}
		}
		return retval;
	}

	/**
	 * @param parseString
	 * @param pos
	 * @return
	 */
	private static int skipWhiteSpace(String parseString, int pos) {
		int retval = pos;
		char c = parseString.charAt(retval);
		while (retval < parseString.length() &&
				(c == ' ' || c == '\t' || c == '\r' || c == '\n')) {
			retval++;
			if (retval < parseString.length()) {
				c = parseString.charAt(retval);
			}
		}
		return retval;
	}
	
	/**
	 * @param parseString
	 * @param pos
	 * @return
	 */
	private static int skipNonWhiteSpace(String parseString, int pos) {
		int retval = pos;
		char c = parseString.charAt(retval);
		while (retval < parseString.length() &&
				c != ' ' && c != '\t' && c != '\r' && c != '\n') {
			retval++;
			if (retval < parseString.length()) {
				c = parseString.charAt(retval);
			}
		}
		return retval;
	}

	/**
	 * @param licenseID
	 * @return true if the licenseID belongs to a standard license
	 */
	public static boolean isStandardLicenseID(String licenseID) {
		// TODO Replace with a lookup of the website
		return STANDARD_LICENSE_ID_SET.contains(licenseID);
	}
}
