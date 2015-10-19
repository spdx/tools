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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Java sha1 checksum generator using MessageDigest
 * @author Gary O'Neall
 *
 */
public class JavaSha1ChecksumGenerator implements IFileChecksumGenerator {
	static final String SHA1_ALGORITHM = "SHA-1";
	static final String PACKAGE_VERIFICATION_CHARSET = "UTF-8";
	private MessageDigest digest = null;

	public JavaSha1ChecksumGenerator() throws NoSuchAlgorithmException {
		this.digest = MessageDigest.getInstance(SHA1_ALGORITHM);
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.IFileChecksumGenerator#getFileChecksum(java.io.File)
	 */
	@Override
	public String getFileChecksum(File file) throws IOException {
		digest.reset();
		FileInputStream in = new FileInputStream(file);
		try {
			byte[] buffer = new byte[2048];
			int numBytes = in.read(buffer);
			while (numBytes >= 0) {
				digest.update(buffer, 0, numBytes);
				numBytes = in.read(buffer);
			}
			byte[] digestBytes = digest.digest();
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < digestBytes.length; i++) {
				String hex = Integer.toHexString(0xff & digestBytes[i]);
				if (hex.length() < 2) {
					sb.append("0");
				}
				sb.append(hex);
			}
			return sb.toString();
		} finally {
			in.close();
		}
	}

}
