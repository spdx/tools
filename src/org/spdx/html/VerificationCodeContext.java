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
import java.util.List;

import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SpdxPackageVerificationCode;

/**
 * Context for a Package Verification Code
 * @author Gary O'Neall
 *
 */
public class VerificationCodeContext {
	
	SpdxPackageVerificationCode code = null;
	Exception error = null;

	/**
	 * @param verificationCode
	 */
	public VerificationCodeContext(SpdxPackageVerificationCode verificationCode) {
		this.code = verificationCode;
	}

	/**
	 * @param e
	 */
	public VerificationCodeContext(InvalidSPDXAnalysisException e) {
		error = e;
	}

	public String packageVerificationCodeValue() {
		if (code == null || code.getValue() == null) {
			if (error != null) {
				return "Error getting package verification code: "+error.getMessage();
			} else {
				return "NONE";
			}
		} else {
			return code.getValue();
		}
	}
	
	public List<String> packageVerificationCodeExcludedFile() {
		ArrayList<String> retval = new ArrayList<String>();
		if (code != null) {
			String[] skippedFiles = code.getExcludedFileNames();
			if (skippedFiles != null) {
				for (int i = 0; i < skippedFiles.length; i++) {
					retval.add(skippedFiles[i]);
				}
			}
		}
		return retval;
	}
}
