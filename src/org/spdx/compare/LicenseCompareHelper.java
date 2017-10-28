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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.spdx.compare.CompareTemplateOutputHandler.DifferenceDescription;
import org.spdx.licenseTemplate.LicenseTemplateRuleException;
import org.spdx.licenseTemplate.SpdxLicenseTemplateHelper;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.license.AnyLicenseInfo;
import org.spdx.rdfparser.license.ConjunctiveLicenseSet;
import org.spdx.rdfparser.license.DisjunctiveLicenseSet;
import org.spdx.rdfparser.license.ExtractedLicenseInfo;
import org.spdx.rdfparser.license.License;
import org.spdx.rdfparser.license.LicenseInfoFactory;
import org.spdx.rdfparser.license.LicenseSet;
import org.spdx.rdfparser.license.SpdxListedLicense;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Primarily a static class of helper functions for comparing two SPDX licenses
 * @author Gary O'Neall
 *
 */
public class LicenseCompareHelper {
	
//	protected static final String TOKEN_DELIM = "\\s";	// white space
	protected static final String TOKEN_SPLIT_REGEX = 		"(^|[^\\s\\.,?]+)((\\s|\\.|,|\\?|$)+)";
	protected static final Pattern TOKEN_SPLIT_PATTERN = Pattern.compile(TOKEN_SPLIT_REGEX);
	
	protected static final ImmutableSet<String> PUNCTUATION = ImmutableSet.<String>builder()
			.add(".").add(",").add("?").build();
	
	// most of these are comments for common programming languages (C style, Java, Ruby, Python)
	protected static final ImmutableSet<String> SKIPPABLE_TOKENS = ImmutableSet.<String>builder()
		.add("//").add("/*").add("*/").add("/**").add("#").add("##")
		.add("*").add("\"\"\"").add("=begin").add("=end").build();
	
	protected static final Map<String, String> NORMALIZE_TOKENS = Maps.newHashMap();
	
	static {
		//TODO: These should be moved to a property file
		NORMALIZE_TOKENS.put("acknowledgment","acknowledgement");   
		NORMALIZE_TOKENS.put("analogue","analog");   
		NORMALIZE_TOKENS.put("analyse","analyze");   
		NORMALIZE_TOKENS.put("artefact","artifact");   
		NORMALIZE_TOKENS.put("authorisation","authorization");   
		NORMALIZE_TOKENS.put("authorised","authorized");   
		NORMALIZE_TOKENS.put("calibre","caliber");   
		NORMALIZE_TOKENS.put("cancelled","canceled");   
		NORMALIZE_TOKENS.put("apitalisations","apitalizations");   
		NORMALIZE_TOKENS.put("catalogue","catalog");   
		NORMALIZE_TOKENS.put("categorise","categorize");   
		NORMALIZE_TOKENS.put("centre","center");   
		NORMALIZE_TOKENS.put("emphasised","emphasized");   
		NORMALIZE_TOKENS.put("favour","favor");   
		NORMALIZE_TOKENS.put("favourite","favorite");   
		NORMALIZE_TOKENS.put("fulfil","fulfill");   
		NORMALIZE_TOKENS.put("fulfilment","fulfillment");   
		NORMALIZE_TOKENS.put("initialise","initialize");   
		NORMALIZE_TOKENS.put("judgment","judgement");   
		NORMALIZE_TOKENS.put("labelling","labeling");   
		NORMALIZE_TOKENS.put("labour","labor");   
		NORMALIZE_TOKENS.put("licence","license");   
		NORMALIZE_TOKENS.put("maximise","maximize");   
		NORMALIZE_TOKENS.put("modelled","modeled");   
		NORMALIZE_TOKENS.put("modelling","modeling");   
		NORMALIZE_TOKENS.put("offence","offense");   
		NORMALIZE_TOKENS.put("optimise","optimize");   
		NORMALIZE_TOKENS.put("organisation","organization");   
		NORMALIZE_TOKENS.put("organise","organize");   
		NORMALIZE_TOKENS.put("practise","practice");   
		NORMALIZE_TOKENS.put("programme","program");   
		NORMALIZE_TOKENS.put("realise","realize");   
		NORMALIZE_TOKENS.put("recognise","recognize");   
		NORMALIZE_TOKENS.put("signalling","signaling");   
		NORMALIZE_TOKENS.put("utilisation","utilization");   
		NORMALIZE_TOKENS.put("whilst","while");   
		NORMALIZE_TOKENS.put("wilful","wilfull");   
		NORMALIZE_TOKENS.put("non-commercial","noncommercial");    
		NORMALIZE_TOKENS.put("copyright-owner", "copyright-holder");
		NORMALIZE_TOKENS.put("sublicense", "sub-license");
		NORMALIZE_TOKENS.put("non-infringement", "noninfringement");
		NORMALIZE_TOKENS.put("©", "(c)");
		NORMALIZE_TOKENS.put("copyright", "(c)");
	}
	
	
	static final String DASHES_REGEX = "[\\u2012\\u2013\\u2014\\u2015]";
	static final String PER_CENT_REGEX = "(?i)per\\scent";
	static final Pattern PER_CENT_PATTERN = Pattern.compile(PER_CENT_REGEX, Pattern.CASE_INSENSITIVE);
	static final String COPYRIGHT_HOLDER_REGEX = "(?i)copyright\\sholder";
	static final Pattern COPYRIGHT_HOLDER_PATTERN = Pattern.compile(COPYRIGHT_HOLDER_REGEX, Pattern.CASE_INSENSITIVE);
	static final String COPYRIGHT_OWNER_REGEX = "(?i)copyright\\sowner";
	static final Pattern COPYRIGHT_OWNER_PATTERN = Pattern.compile(COPYRIGHT_OWNER_REGEX, Pattern.CASE_INSENSITIVE);
	
	//TODO: Add equiv for quotes
	/**
	 * Returns true if two sets of license text is considered a match per
	 * the SPDX License matching guidelines documented at spdx.org (currently http://spdx.org/wiki/spdx-license-list-match-guidelines)
	 * There are 2 unimplemented features - bullets/numbering is not considered and comments with no whitespace between text is not skipped
	 * @param licenseTextA
	 * @param licenseTextB
	 * @return
	 */
	public static boolean isLicenseTextEquivalent(String licenseTextA, String licenseTextB) {
		//TODO: Handle comment characters without white space before text
		//TODO: Handle bullets and numbering
		// Need to take care of multi-word equivalent words - convert to single words with hypens
		
		// tokenize each of the strings
		if (licenseTextA == null) {
			return (licenseTextB == null || licenseTextB.isEmpty());
		}
		if (licenseTextB == null) {
			return licenseTextA.isEmpty();
		}
		if (licenseTextA.equals(licenseTextB)) {
			return true;
		}
		Map<Integer, LineColumn> tokenToLocationA = new HashMap<Integer, LineColumn>();
		Map<Integer, LineColumn> tokenToLocationB = new HashMap<Integer, LineColumn>();
		String[] licenseATokens = tokenizeLicenseText(licenseTextA,tokenToLocationA);
		String[] licenseBTokens = tokenizeLicenseText(licenseTextB,tokenToLocationB);
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
	
	private static String normalizeQuotes(String s) {
		return s.replaceAll("‘|’|‛|‚", "'").replaceAll("“|”|‟|„", "\"");
	}
	
	/**
	 * Tokenizes the license text, normalizes quotes, lowercases and converts multi-words for better equiv. comparisons
	 * @param tokenLocations location for all of the tokens
	 * @param licenseText
	 * @return
	 * @throws IOException 
	 */
	public static String[] tokenizeLicenseText(String licenseText, Map<Integer, LineColumn> tokenToLocation) {
		String textToTokenize = normalizeQuotes(replaceMultWord(licenseText)).toLowerCase();
		List<String> tokens = new ArrayList<String>();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new StringReader(textToTokenize));
			int currentLine = 1;
			int currentToken = 0;
			String line = reader.readLine();
			while (line != null) {
				Matcher lineMatcher = LicenseCompareHelper.TOKEN_SPLIT_PATTERN.matcher(line);
				while (lineMatcher.find()) {
					String token = lineMatcher.group(1).trim();
					tokens.add(token);
					tokenToLocation.put(currentToken, new LineColumn(currentLine, lineMatcher.start(), token.length()));
					currentToken++;
					String separator = lineMatcher.group(2).trim();
					if (LicenseCompareHelper.PUNCTUATION.contains(separator)) {
						tokens.add(separator);
						tokenToLocation.put(currentToken, new LineColumn(currentToken, lineMatcher.start()+token.length(), token.length()));
						currentToken++;
					}
				}
				currentLine++;
				line = reader.readLine();
			}
		} catch (IOException e) {
			// Don't fill in the lines, take a simpler approach
			Matcher m = TOKEN_SPLIT_PATTERN.matcher(textToTokenize);
			while (m.find()) {
				String word = m.group(1).trim();
				String seperator = m.group(2).trim();
				tokens.add(word);
				if (PUNCTUATION.contains(seperator)) {
					tokens.add(seperator);
				}
			}
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}
		return tokens.toArray(new String[tokens.size()]);
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
	static String getTokenAt(String[] tokens, int tokenIndex) {
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
	static boolean tokensEquivalent(String tokenA, String tokenB) {
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
				// check for equivalent tokens by normalizing the tokens
				String ns1 = NORMALIZE_TOKENS.get(s1);
				if (ns1 == null) {
					ns1 = s1;
				}
				String ns2 = NORMALIZE_TOKENS.get(s2);
				if (ns2 == null) {
					ns2 = s2;
				}
				return ns1.equals(ns2);
			}
		}
	}
	/**
	 * Returns true if the token can be ignored per the rules
	 * @param token
	 * @return
	 */
	static boolean canSkip(String token) {
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
	public static boolean isLicenseEqual(AnyLicenseInfo license1,
			AnyLicenseInfo license2, Map<String, String> xlationMap) throws SpdxCompareException {
		if (license1 instanceof ConjunctiveLicenseSet) {
			if (!(license2 instanceof ConjunctiveLicenseSet)) {
				return false;
			} else {
				return isLicenseSetsEqual((ConjunctiveLicenseSet)license1,
						(ConjunctiveLicenseSet)license2, xlationMap);
			}
		} else if (license1 instanceof DisjunctiveLicenseSet) {
			if (!(license2 instanceof DisjunctiveLicenseSet)) {
				return false;
			} else {
				return isLicenseSetsEqual((DisjunctiveLicenseSet)license1,
						(DisjunctiveLicenseSet)license2, xlationMap);
			}
		} else if (license1 instanceof ExtractedLicenseInfo) {
			if (!(license2 instanceof ExtractedLicenseInfo)) {
				return false;
			} else {
				String licenseid1 = ((ExtractedLicenseInfo)license1).getLicenseId();
				String licenseid2 = ((ExtractedLicenseInfo)license2).getLicenseId();
				String xlatedLicenseId = xlationMap.get(licenseid1);
				if (xlatedLicenseId == null) {
					return false;	// no equivalent license was found
				}
				return xlatedLicenseId.equals(licenseid2);
			}
		} else {
            return license1.equals(license2);
        }
	}

	/**
	 * Compares two license sets using the xlationMap for the non-standard license IDs
	 * @param license1
	 * @param license2
	 * @return
	 * @throws SpdxCompareException 
	 */
	private static boolean isLicenseSetsEqual(LicenseSet license1, LicenseSet license2, Map<String, String> xlationMap) throws SpdxCompareException {
		// note - order does not matter
		AnyLicenseInfo[] licenseInfos1 = license1.getMembers();
		AnyLicenseInfo[] licenseInfos2 = license2.getMembers();
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
	
	/**
	 * Compares license text to the license text of an SPDX Standard License
	 * @param license SPDX Standard License to compare
	 * @param compareText Text to compare to the standard license
	 * @return True if the license text is the same per the license matching guidelines
	 * @throws SpdxCompareException
	 */
	public static DifferenceDescription isTextStandardLicense(License license, String compareText) throws SpdxCompareException {
		String licenseTemplate = license.getStandardLicenseTemplate();
		if (licenseTemplate == null || licenseTemplate.trim().isEmpty()) {
			licenseTemplate = license.getLicenseText();
		}
		CompareTemplateOutputHandler compareTemplateOutputHandler = null;
		try {
			compareTemplateOutputHandler = new CompareTemplateOutputHandler(compareText);
		} catch (IOException e1) {
			throw(new SpdxCompareException("IO Error reading the compare text: "+e1.getMessage(),e1));
		}
		try {
			SpdxLicenseTemplateHelper.parseTemplate(licenseTemplate, compareTemplateOutputHandler);
		} catch (LicenseTemplateRuleException e) {
			throw(new SpdxCompareException("Invalid template rule found during compare: "+e.getMessage(),e));
		}
		return compareTemplateOutputHandler.getDifferences();
	}
	
	/**
	 * Returns a list of SPDX Standard License ID's that match the text provided using
	 * the SPDX matching guidelines.
	 * @param licenseText Text to compare to the standard license texts
	 * @return Array of SPDX standard license IDs that match
	 * @throws InvalidSPDXAnalysisException If an error occurs accessing the standard licenses
	 * @throws SpdxCompareException If an error occurs in the comparison
	 */
	public static String[] matchingStandardLicenseIds(String licenseText) throws InvalidSPDXAnalysisException, SpdxCompareException {
		String[] stdLicenseIds = LicenseInfoFactory.getSpdxListedLicenseIds();
		List<String> matchingIds  = Lists.newArrayList();
		for (String stdLicId : stdLicenseIds) {
			SpdxListedLicense license = LicenseInfoFactory.getListedLicenseById(stdLicId);
			if (!isTextStandardLicense(license, licenseText).isDifferenceFound()) {
				matchingIds.add(license.getLicenseId());
			}
		}
		return matchingIds.toArray(new String[matchingIds.size()]);
	}
}
