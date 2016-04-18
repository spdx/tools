/**
 * Copyright (c) 2015 Source Auditor Inc.
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

import java.util.ArrayList;
import java.util.List;

import org.spdx.compare.SpdxCompareException;
import org.spdx.rdfparser.model.SpdxDocument;

/**
 * Verifies an SPDX document and lists any verification errors
 * @author Gary O'Neall
 *
 */
public class Verify {
	
	static final int MIN_ARGS = 1;
	static final int MAX_ARGS = 1;
	static final int ERROR_STATUS = 1;

	/**
	 * @param args Single argument SPDX file - can be tag/value or RDF
	 */
	public static void main(String[] args) {
		if (args.length < MIN_ARGS) {
			System.err
					.println("Usage:\n Verify file\nwhere file is the file path to an SPDX RDF XML or an SPDX Tag/Value file");
			System.exit(ERROR_STATUS);
		}
		if (args.length > MAX_ARGS) {
			System.out.printf("Warning: Extra arguments will be ignored");
		}
		SpdxDocument doc = null;
		List<String> parserWarnings = new ArrayList<String>();
		try {
			doc = CompareSpdxDocs.openRdfOrTagDoc(args[0], parserWarnings);
		} catch (SpdxCompareException e) {
			System.out
				.printf("Unable to open the SPDX file file: "+e.getMessage());
			System.exit(ERROR_STATUS);
		}
		List<String> verify = doc.verify();
		if (verify.size() > 0) {
			System.out.println("This SPDX Document is not valid due to:");
			for (int i = 0; i < verify.size(); i++) {
				System.out.print("\t" + verify.get(i)+"\n");
				parserWarnings.remove(verify.get(i));
			}
			if (!parserWarnings.isEmpty()) {
				System.out.println("The following parser warnings were found:");
				for (String warning:parserWarnings) {
					System.out.print("\t" + warning+"\n");
				}
			}
		} else {
			System.out.println("This SPDX Document is valid.");
		}
	}
}
