/**
 * Copyright (c) 2013 Source Auditor Inc.
 * Copyright (c) 2013 Black Duck Software Inc.
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
package org.spdx.compare;

import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.spdx.rdfparser.SPDXConjunctiveLicenseSet;
import org.spdx.rdfparser.SPDXDisjunctiveLicenseSet;
import org.spdx.rdfparser.SPDXLicenseInfo;
import org.spdx.rdfparser.SPDXLicenseSet;
import org.spdx.rdfparser.SPDXNonStandardLicense;

/**
 * Primarily a static class of helper functions for comparing two SPDX licenses
 * @author Gary O'Neall
 *
 */
public class LicenseCompareHelper {
	
	static final String TOKEN_DELIM = "\\s";	// white space
	static final HashSet<String> SKIPPABLE_TOKENS = new HashSet<String>();
	static {
		// most of these are comments for common programming languages (C style, Java, Ruby, Python)
		SKIPPABLE_TOKENS.add("//");		SKIPPABLE_TOKENS.add("/*");
		SKIPPABLE_TOKENS.add("*/");		SKIPPABLE_TOKENS.add("/**");
		SKIPPABLE_TOKENS.add("#");		SKIPPABLE_TOKENS.add("##");
		SKIPPABLE_TOKENS.add("*");		SKIPPABLE_TOKENS.add("\"\"\"");
		SKIPPABLE_TOKENS.add("=begin");	SKIPPABLE_TOKENS.add("=end");
	}
	static final HashMap<String, String> EQUIV_TOKENS = new HashMap<String, String>();
	static {
		EQUIV_TOKENS.put("acknowledgement","acknowledgment");   EQUIV_TOKENS.put("acknowledgment","acknowledgement");   
		EQUIV_TOKENS.put("analog","analogue");   EQUIV_TOKENS.put("analogue","analog");   
		EQUIV_TOKENS.put("analyze","analyse");   EQUIV_TOKENS.put("analyse","analyze");   
		EQUIV_TOKENS.put("artifact","artefact");   EQUIV_TOKENS.put("artefact","artifact");   
		EQUIV_TOKENS.put("authorization","authorisation");   EQUIV_TOKENS.put("authorisation","authorization");   
		EQUIV_TOKENS.put("authorized","authorised");   EQUIV_TOKENS.put("authorised","authorized");   
		EQUIV_TOKENS.put("caliber","calibre");   EQUIV_TOKENS.put("calibre","caliber");   
		EQUIV_TOKENS.put("canceled","cancelled");   EQUIV_TOKENS.put("cancelled","canceled");   
		EQUIV_TOKENS.put("apitalizations","apitalisations");   EQUIV_TOKENS.put("apitalisations","apitalizations");   
		EQUIV_TOKENS.put("catalog","catalogue");   EQUIV_TOKENS.put("catalogue","catalog");   
		EQUIV_TOKENS.put("categorize","categorise");   EQUIV_TOKENS.put("categorise","categorize");   
		EQUIV_TOKENS.put("center","centre");   EQUIV_TOKENS.put("centre","center");   
		EQUIV_TOKENS.put("emphasized","emphasised");   EQUIV_TOKENS.put("emphasised","emphasized");   
		EQUIV_TOKENS.put("favor","favour");   EQUIV_TOKENS.put("favour","favor");   
		EQUIV_TOKENS.put("favorite","favourite");   EQUIV_TOKENS.put("favourite","favorite");   
		EQUIV_TOKENS.put("fulfill","fulfil");   EQUIV_TOKENS.put("fulfil","fulfill");   
		EQUIV_TOKENS.put("fulfillment","fulfilment");   EQUIV_TOKENS.put("fulfilment","fulfillment");   
		EQUIV_TOKENS.put("initialize","initialise");   EQUIV_TOKENS.put("initialise","initialize");   
		EQUIV_TOKENS.put("judgement","judgment");   EQUIV_TOKENS.put("judgment","judgement");   
		EQUIV_TOKENS.put("labeling","labelling");   EQUIV_TOKENS.put("labelling","labeling");   
		EQUIV_TOKENS.put("labor","labour");   EQUIV_TOKENS.put("labour","labor");   
		EQUIV_TOKENS.put("license","licence");   EQUIV_TOKENS.put("licence","license");   
		EQUIV_TOKENS.put("maximize","maximise");   EQUIV_TOKENS.put("maximise","maximize");   
		EQUIV_TOKENS.put("modeled","modelled");   EQUIV_TOKENS.put("modelled","modeled");   
		EQUIV_TOKENS.put("modeling","modelling");   EQUIV_TOKENS.put("modelling","modeling");   
		EQUIV_TOKENS.put("offense","offence");   EQUIV_TOKENS.put("offence","offense");   
		EQUIV_TOKENS.put("optimize","optimise");   EQUIV_TOKENS.put("optimise","optimize");   
		EQUIV_TOKENS.put("organization","organisation");   EQUIV_TOKENS.put("organisation","organization");   
		EQUIV_TOKENS.put("organize","organise");   EQUIV_TOKENS.put("organise","organize");   
		EQUIV_TOKENS.put("practice","practise");   EQUIV_TOKENS.put("practise","practice");   
		EQUIV_TOKENS.put("program","programme");   EQUIV_TOKENS.put("programme","program");   
		EQUIV_TOKENS.put("realize","realise");   EQUIV_TOKENS.put("realise","realize");   
		EQUIV_TOKENS.put("recognize","recognise");   EQUIV_TOKENS.put("recognise","recognize");   
		EQUIV_TOKENS.put("signaling","signalling");   EQUIV_TOKENS.put("signalling","signaling");   
		EQUIV_TOKENS.put("utilization","utilisation");   EQUIV_TOKENS.put("utilisation","utilization");   
		EQUIV_TOKENS.put("while","whilst");   EQUIV_TOKENS.put("whilst","while");   
		EQUIV_TOKENS.put("wilfull","wilful");   EQUIV_TOKENS.put("wilful","wilfull");   
		EQUIV_TOKENS.put("noncommercial","non-commercial");   EQUIV_TOKENS.put("non-commercial","noncommercial");    
		EQUIV_TOKENS.put("copyright-holder", "copyright-owner");   EQUIV_TOKENS.put("copyright-owner", "copyright-holder");
	}
	static final String DASHES_REGEX = "[\\u2012\\u2013\\u2014\\u2015]";
	static final String PER_CENT_REGEX = "(?i)per\\scent";
	static final Pattern PER_CENT_PATTERN = Pattern.compile(PER_CENT_REGEX, Pattern.CASE_INSENSITIVE);
	static final String COPYRIGHT_HOLDER_REGEX = "(?i)copyright\\sholder";
	static final Pattern COPYRIGHT_HOLDER_PATTERN = Pattern.compile(COPYRIGHT_HOLDER_REGEX, Pattern.CASE_INSENSITIVE);
	static final String COPYRIGHT_OWNER_REGEX = "(?i)copyright\\sowner";
	static final Pattern COPYRIGHT_OWNER_PATTERN = Pattern.compile(COPYRIGHT_OWNER_REGEX, Pattern.CASE_INSENSITIVE);
	
	/**
	 * Returns true if two sets of license text is considered a match per
	 * the SPDX License matching guidlines documented at spdx.org (currently http://spdx.org/wiki/spdx-license-list-match-guidelines)
	 * There are 2 unimplemented features - bullets/numbering is not considered and comments with no whitespace between text is not skipped
	 * @param licenseTextA
	 * @param licenseTextB
	 * @return
	 */
	public static boolean licensesMatch(String licenseTextA, String licenseTextB) {
		//TODO: Handle comment characters without white space before text
		//TODO: Handle bullets and numbering
		// Need to take care of multi-word equivalent words - convert to single words with hypens
		
		// tokenize each of the strings
		
		String[] licenseATokens = replaceMultWord(licenseTextA).split(TOKEN_DELIM);
		String[] licenseBTokens = replaceMultWord(licenseTextB).split(TOKEN_DELIM);
		int bTokenCounter = 0;
		int aTokenCounter = 0;
		String nextAToken = getTokenAt(licenseATokens, aTokenCounter++);
		String nextBToken = getTokenAt(licenseBTokens, bTokenCounter++);
		while (nextAToken != null) {
			if (nextBToken == null) {
				// end of b stream
				while (nextAToken != null && canSkip(nextAToken)) {
					nextAToken = getTokenAt(licenseATokens, aTokenCounter++);
				}
				if (nextAToken != null) {
					return false;	// there is more stuff in the license text B, so not equal
				}
			} else if (tokensEquivalent(nextAToken, nextBToken)) { 
				// just move onto the next set of tokens
				nextAToken = getTokenAt(licenseATokens, aTokenCounter++);
				nextBToken = getTokenAt(licenseBTokens, bTokenCounter++);
			} else {
				// see if we can skip through some B tokens to find a match
				while (nextBToken != null && canSkip(nextBToken)) {
					nextBToken = getTokenAt(licenseBTokens, bTokenCounter++);
				}
				// just to be sure, skip forward on the A license
				while (nextAToken != null && canSkip(nextAToken)) {
					nextAToken = getTokenAt(licenseATokens, aTokenCounter++);
				}
				if (!tokensEquivalent(nextAToken, nextBToken)) {
					return false;
				} else {
					nextAToken = getTokenAt(licenseATokens, aTokenCounter++);
					nextBToken = getTokenAt(licenseBTokens, bTokenCounter++);
				}
			}
		}
		// need to make sure B is at the end
		while (nextBToken != null && canSkip(nextBToken)) {
			nextBToken = getTokenAt(licenseBTokens, bTokenCounter++);
		}
		return (nextBToken == null);
	}
	
	/**
	 * replaces all mult-words with a single token using a dash to separate
	 * @param s
	 * @return
	 */
	private static String replaceMultWord(String s) {
		Matcher m = COPYRIGHT_HOLDER_PATTERN.matcher(s);
		String retval = m.replaceAll("copyright-holder");
		m = COPYRIGHT_OWNER_PATTERN.matcher(retval);
		retval = m.replaceAll("copyright-owner");
		m = PER_CENT_PATTERN.matcher(retval);
		retval = m.replaceAll("percent");
		return retval;
	}
	
	/**
	 * Just fetches the string at the index checking for range.  Returns null if index is out of range.
	 * @param tokens
	 * @param tokenIndex
	 * @return
	 */
	private static String getTokenAt(String[] tokens, int tokenIndex) {
		if (tokenIndex >= tokens.length) {
			return null;
		} else {
			return tokens[tokenIndex];
		}
	}
	/**
	 * Returns true if the two tokens can be considered equlivalent per the SPDX license matching rules
	 * @param tokenA
	 * @param tokenB
	 * @return
	 */
	private static boolean tokensEquivalent(String tokenA, String tokenB) {
		if (tokenA == null) {
			if (tokenB == null) {
				return true;
			} else {
				return false;
			}
		} else if (tokenB == null) {
			return false;
		} else {
			String s1 = tokenA.trim().toLowerCase().replaceAll(DASHES_REGEX, "-");
			String s2 = tokenB.trim().toLowerCase().replaceAll(DASHES_REGEX, "-");
			if (s1.equals(s2)) {
				return true;
			} else {
				// check for equivalent tokens
				if (EQUIV_TOKENS.get(s1) != null) {
					return s2.equals(EQUIV_TOKENS.get(s1));
				} else {
					return false;
				}
			}
		}
	}
	/**
	 * Returns true if the token can be ignored per the rules
	 * @param token
	 * @return
	 */
	private static boolean canSkip(String token) {
		if (token == null) {
			return false;
		}
		if (token.trim().isEmpty()) {
			return true;
		}
		return SKIPPABLE_TOKENS.contains(token.trim().toLowerCase());
	}

	/**
	 * Compares two licenses from potentially two different documents which may have
	 * different license ID's for the same license
	 * @param license1
	 * @param license2
	 * @param xlationMap Mapping the license ID's from license 1 to license 2
	 * @return
	 * @throws SpdxCompareException 
	 */
	public static boolean isLicenseEqual(SPDXLicenseInfo license1,
			SPDXLicenseInfo license2, HashMap<String, String> xlationMap) throws SpdxCompareException {
		if (license1 instanceof SPDXConjunctiveLicenseSet) {
			if (!(license2 instanceof SPDXConjunctiveLicenseSet)) {
				return false;
			} else {
				return isLicenseSetsEqual((SPDXConjunctiveLicenseSet)license1,
						(SPDXConjunctiveLicenseSet)license2, xlationMap);
			}
		} else if (license1 instanceof SPDXDisjunctiveLicenseSet) {
			if (!(license2 instanceof SPDXDisjunctiveLicenseSet)) {
				return false;
			} else {
				return isLicenseSetsEqual((SPDXDisjunctiveLicenseSet)license1,
						(SPDXDisjunctiveLicenseSet)license2, xlationMap);
			}
		} else if (license1 instanceof SPDXNonStandardLicense) {
			if (!(license2 instanceof SPDXNonStandardLicense)) {
				return false;
			} else {
				String licenseid1 = ((SPDXNonStandardLicense)license1).getId();
				String licenseid2 = ((SPDXNonStandardLicense)license2).getId();
				String xlatedLicenseId = xlationMap.get(licenseid1);
				if (xlatedLicenseId == null) {
					return false;	// no equivalent license was found
				}
				return xlatedLicenseId.equals(licenseid2);
			}
		} else return license1.equals(license2);
	}

	/**
	 * Compares two license sets using the xlationMap for the non-standard license IDs
	 * @param license1
	 * @param license2
	 * @return
	 * @throws SpdxCompareException 
	 */
	private static boolean isLicenseSetsEqual(
			SPDXLicenseSet license1,
			SPDXLicenseSet license2, HashMap<String, String> xlationMap) throws SpdxCompareException {
		// note - order does not matter
		SPDXLicenseInfo[] licenseInfos1 = license1.getSPDXLicenseInfos();
		SPDXLicenseInfo[] licenseInfos2 = license2.getSPDXLicenseInfos();
		if (licenseInfos1 == null) {
			return licenseInfos2 == null;
		}
		if (licenseInfos2 == null) {
			return false;
		}
		if (licenseInfos1.length != licenseInfos2.length) {
			return false;
		}
		for (int i = 0; i < licenseInfos1.length; i++) {
			boolean found = false;
			for (int j = 0; j < licenseInfos2.length; j++) {
				if (isLicenseEqual(licenseInfos1[i], licenseInfos2[j], xlationMap)) {
					found = true;
					break;
				}
			}
			if (!found) {
				return false;
			}
		}
		return true;
	}
}
