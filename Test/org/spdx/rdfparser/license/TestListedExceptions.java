package org.spdx.rdfparser.license;

import static org.junit.Assert.*;

import org.junit.Test;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;

/**
 * @author Gary O'Neall
 *         SPDX-License-Identifier: Apache-2.0
 */
public class TestListedExceptions {
	
	@Test
	public void testGetListedExceptionById() throws InvalidSPDXAnalysisException {
		ListedExceptions.resetListedExceptions();
		String id = "Classpath-exception-2.0";
		assertTrue(ListedExceptions.getListedExceptions().isSpdxListedLExceptionID(id));
		ListedLicenseException result = ListedExceptions.getListedExceptions().getListedExceptionById(id);
		assertEquals(id, result.getLicenseExceptionId());
	}
	
	@Test
	public void testGetLicenseIbyIdLocal() throws InvalidSPDXAnalysisException {
		System.setProperty("SPDXParser.OnlyUseLocalLicenses", "true");
		ListedLicenses.resetListedLicenses();
		ListedExceptions.resetListedExceptions();
		try {
			String id = "Classpath-exception-2.0";
			assertTrue(ListedExceptions.getListedExceptions().isSpdxListedLExceptionID(id));
			ListedLicenseException result = ListedExceptions.getListedExceptions().getListedExceptionById(id);
			assertEquals(id, result.getLicenseExceptionId());
		} finally {
			System.setProperty("SPDXParser.OnlyUseLocalLicenses", "false");
			ListedLicenses.resetListedLicenses();
			ListedExceptions.resetListedExceptions();
		}
	}
}
