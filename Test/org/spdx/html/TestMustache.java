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


import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.github.mustachejava.Mustache;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.MustacheException;

/**
 * @author Gary O'Neall
 *
 */
public class TestMustache {

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}
	
	static final String TEST_FIELD1 = "testField1";
	static final String TEST_RESULT1 = "testResult1";
	@Test
	public void testMustache() throws MustacheException, IOException {
		File root = new File("TestFiles");
		DefaultMustacheFactory builder = new DefaultMustacheFactory(root);
		Map<String, Object> context = new HashMap<String, Object>();
		context.put(TEST_FIELD1, TEST_RESULT1);
		Mustache m = builder.compile("testSimpleTemplate.txt");
		StringWriter writer = new StringWriter();
		m.execute(writer, context);
		assertEquals(TEST_RESULT1, writer.toString());
	}

}
