/**
 * Copyright (c) 2017 Source Auditor Inc.
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
package org.spdx.tools.licensegenerator;

import java.io.IOException;
import java.util.List;

import org.spdx.compare.SpdxCompareException;
import org.spdx.rdfparser.license.License;
import org.spdx.rdfparser.license.LicenseException;

/**
 * Interface for license testers
 * @author Gary O'Neall
 *
 */
public interface ILicenseTester {

	/**
	 * Test exception against the test files directory
	 * @param exception
	 * @return
	 * @throws IOException 
	 */
	public List<String> testException(LicenseException exception) throws IOException;

	/**
	 * Test a license against the license test files
	 * @param license license to test
	 * @return list of test failure descriptions.  List is empty if all tests pass.
	 * @throws IOException
	 * @throws SpdxCompareException
	 */
	public List<String> testLicense(License license) throws IOException, SpdxCompareException;

}
