/**
 * Copyright (c) 2010 Source Auditor Inc.
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

/**
 * Exception for invalid SPDX Documents
 * @author Gary O'Neall
 *
 */
public class InvalidSPDXAnalysisException extends Exception {
	/**
	 *
	 */
	private static final long serialVersionUID = -8042590523688807173L;
	public InvalidSPDXAnalysisException(String msg) {
		super(msg);
	}
	public InvalidSPDXAnalysisException(String msg, Throwable inner) {
		super(msg, inner);
	}
}
