/**
 * Copyright (c) 2020 Source Auditor Inc.
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
 */
package org.spdx.compare;

import java.util.ArrayList;
import java.util.List;

import org.spdx.licenseTemplate.ILicenseTemplateOutputHandler;
import org.spdx.licenseTemplate.LicenseTemplateRule;
import org.spdx.rdfparser.license.LicenseParserException;

/**
 * Filter the template output to create a list of strings filtering out optional and/or var text
 * @author Gary O'Neall
 *
 */
public class FilterTemplateOutputHandler implements ILicenseTemplateOutputHandler {
	
	private boolean includeVarText;
	private List<String> filteredText = new ArrayList<>();
	StringBuilder currentString = new StringBuilder();
	private int optionalDepth = 0;	// depth of optional rules

	/**
	 * @param includVarText if true, include the default variable text; if false remove the variable text
	 */
	public FilterTemplateOutputHandler(boolean includVarText) {
		this.includeVarText = includVarText;
	}

	/* (non-Javadoc)
	 * @see org.spdx.licenseTemplate.ILicenseTemplateOutputHandler#text(java.lang.String)
	 */
	@Override
	public void text(String text) {
		if (optionalDepth <= 0) {
			currentString.append(text);
		}
	}

	/* (non-Javadoc)
	 * @see org.spdx.licenseTemplate.ILicenseTemplateOutputHandler#variableRule(org.spdx.licenseTemplate.LicenseTemplateRule)
	 */
	@Override
	public void variableRule(LicenseTemplateRule rule) {
		if (includeVarText && optionalDepth <= 0) {
			currentString.append(rule.getOriginal());
		} else {
			if (currentString.length() > 0) {
				filteredText.add(currentString.toString());
				currentString.setLength(0);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.spdx.licenseTemplate.ILicenseTemplateOutputHandler#beginOptional(org.spdx.licenseTemplate.LicenseTemplateRule)
	 */
	@Override
	public void beginOptional(LicenseTemplateRule rule) {
		optionalDepth++;
	}

	/* (non-Javadoc)
	 * @see org.spdx.licenseTemplate.ILicenseTemplateOutputHandler#endOptional(org.spdx.licenseTemplate.LicenseTemplateRule)
	 */
	@Override
	public void endOptional(LicenseTemplateRule rule) {
		optionalDepth--;
		if (optionalDepth == 0 && currentString.length() > 0) {
				filteredText.add(currentString.toString());
				currentString.setLength(0);
		}
	}

	/* (non-Javadoc)
	 * @see org.spdx.licenseTemplate.ILicenseTemplateOutputHandler#completeParsing()
	 */
	@Override
	public void completeParsing() throws LicenseParserException {
		if (currentString.length() > 0) {
			filteredText.add(currentString.toString());
			currentString.setLength(0);
		}
	}

	/**
	 * @return the includeVarText
	 */
	public boolean isIncludeVarText() {
		return includeVarText;
	}

	/**
	 * @return the filteredText
	 */
	public List<String> getFilteredText() {
		return filteredText;
	}
}
