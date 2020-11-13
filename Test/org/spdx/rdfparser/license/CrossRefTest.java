package org.spdx.rdfparser.license;

import static org.junit.Assert.*;

import java.util.Objects;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.spdx.rdfparser.IModelContainer;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SpdxRdfConstants;
import org.spdx.rdfparser.model.ModelContainerForTest;

public class CrossRefTest {
	
	private static final String TEST_URL1 = "http://test1/index.html";
	private static final String TEST_URL2 = "http://test2/index.html";
	private static final Boolean TEST_ISLIVE1 = true;
	private static final Boolean TEST_ISLIVE2 = false;
	private static final Boolean TEST_ISWAYBACK1 = true;
	private static final Boolean TEST_ISWAYBACK2 = false;
	private static final Boolean TEST_ISVALID1 = true;
	private static final Boolean TEST_ISVALID2 = false;
	private static final String TEST_MATCH1 = "true";
	private static final String TEST_MATCH2 = "false";
	private static final Integer TEST_ORDER1 = 1;
	private static final Integer TEST_ORDER2 = 2;
	private static final String TEST_TIMESTAMP1 = "timestamp1";
	private static final String TEST_TIMESTAMP2 = "timestamp2";
	CrossRef TEST_CROSSREF;
	
	Model model;
	IModelContainer modelContainer;

	@Before
	public void setUp() throws Exception {
		this.model = ModelFactory.createDefaultModel();
		modelContainer = new ModelContainerForTest(model, "http://testnamespace.com");
		TEST_CROSSREF = new CrossRef(TEST_URL1, TEST_ISVALID1, TEST_ISLIVE1, TEST_ISWAYBACK1, 
				TEST_MATCH1, TEST_TIMESTAMP1, TEST_ORDER1);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetType() {
		assertEquals(SpdxRdfConstants.SPDX_NAMESPACE + SpdxRdfConstants.CLASS_CROSS_REF, TEST_CROSSREF.getType(this.model).getURI());
	}

	@Test
	public void testPopulateModel() throws InvalidSPDXAnalysisException {
		Resource r = TEST_CROSSREF.createResource(modelContainer);
		CrossRef result = new CrossRef(modelContainer, r.asNode());
		assertEquals(result.isLive(), TEST_ISLIVE1);
		assertEquals(result.getMatch(), TEST_MATCH1);
		assertEquals(result.isValid(), TEST_ISVALID1);
		assertEquals(result.isWayBackLink(), TEST_ISWAYBACK1);
		assertEquals(result.getOrder(), TEST_ORDER1);
		assertEquals(result.getTimestamp(), TEST_TIMESTAMP1);
		assertEquals(result.getUrl(), TEST_URL1);
	}

	@Test
	public void testCrossRefStringBooleanBooleanBooleanStringStringInteger() throws InvalidSPDXAnalysisException {
		CrossRef result = new CrossRef(TEST_URL2, TEST_ISVALID2, TEST_ISLIVE2, TEST_ISWAYBACK2, 
				TEST_MATCH2, TEST_TIMESTAMP2, TEST_ORDER2);
		assertEquals(result.isLive(), TEST_ISLIVE2);
		assertEquals(result.getMatch(), TEST_MATCH2);
		assertEquals(result.isValid(), TEST_ISVALID2);
		assertEquals(result.isWayBackLink(), TEST_ISWAYBACK2);
		assertEquals(result.getOrder(), TEST_ORDER2);
		assertEquals(result.getTimestamp(), TEST_TIMESTAMP2);
		assertEquals(result.getUrl(), TEST_URL2);
	}

	@Test
	public void testCrossRefString() throws InvalidSPDXAnalysisException {
		CrossRef result = new CrossRef(TEST_URL2);
		assertEquals(TEST_URL2, result.getUrl());
		assertTrue(Objects.isNull(result.isLive()));
		assertTrue(Objects.isNull(result.getMatch()));
		assertTrue(Objects.isNull(result.isValid()));
		assertTrue(Objects.isNull(result.isWayBackLink()));
		assertTrue(Objects.isNull(result.getOrder()));
		assertTrue(Objects.isNull(result.getTimestamp()));
	}

	@Test
	public void testVerify() {
		assertEquals(0, TEST_CROSSREF.verify().size());
		TEST_CROSSREF.setUrl(null);
		assertEquals(1, TEST_CROSSREF.verify().size());
	}

	@Test
	public void testEquivalent() {
		CrossRef result = new CrossRef(TEST_URL1);
		assertTrue(result.equivalent(TEST_CROSSREF));
		assertTrue(TEST_CROSSREF.equivalent(result));
	}

	@Test
	public void testGetMatch() throws InvalidSPDXAnalysisException {
		assertEquals(TEST_MATCH1, TEST_CROSSREF.getMatch());
		Resource r = TEST_CROSSREF.createResource(modelContainer);
		CrossRef result = new CrossRef(modelContainer, r.asNode());
		assertEquals(result.getMatch(), TEST_CROSSREF.getMatch());
		TEST_CROSSREF.setMatch(TEST_MATCH2);
		assertEquals(TEST_MATCH2, TEST_CROSSREF.getMatch());
		assertEquals(TEST_MATCH2, result.getMatch());
		TEST_CROSSREF.setMatch(null);
		assertTrue(Objects.isNull(TEST_CROSSREF.getMatch()));
		assertTrue(Objects.isNull(result.getMatch()));
	}

	@Test
	public void testGetUrl() throws InvalidSPDXAnalysisException {
		assertEquals(TEST_URL1, TEST_CROSSREF.getUrl());
		Resource r = TEST_CROSSREF.createResource(modelContainer);
		CrossRef result = new CrossRef(modelContainer, r.asNode());
		assertEquals(TEST_URL1, result.getUrl());
		TEST_CROSSREF.setUrl(TEST_URL2);
		assertEquals(TEST_URL2, TEST_CROSSREF.getUrl());
		assertEquals(TEST_URL2, result.getUrl());
	}

	@Test
	public void testIsValid() throws InvalidSPDXAnalysisException {
		assertEquals(TEST_ISVALID1, TEST_CROSSREF.isValid());
		Resource r = TEST_CROSSREF.createResource(modelContainer);
		CrossRef result = new CrossRef(modelContainer, r.asNode());
		assertEquals(TEST_ISVALID1, result.isValid());
		TEST_CROSSREF.setIsValid(TEST_ISVALID2);
		assertEquals(TEST_ISVALID2, TEST_CROSSREF.isValid());
		assertEquals(TEST_ISVALID2, result.isValid());
		TEST_CROSSREF.setIsValid(null);
		assertTrue(Objects.isNull(TEST_CROSSREF.isValid()));
		assertTrue(Objects.isNull(result.isValid()));
	}

	@Test
	public void testIsLive() throws InvalidSPDXAnalysisException {
		assertEquals(TEST_ISLIVE1, TEST_CROSSREF.isLive());
		Resource r = TEST_CROSSREF.createResource(modelContainer);
		CrossRef result = new CrossRef(modelContainer, r.asNode());
		assertEquals(TEST_ISLIVE1, result.isLive());
		TEST_CROSSREF.setIsLive(TEST_ISLIVE2);
		assertEquals(TEST_ISLIVE2, TEST_CROSSREF.isLive());
		assertEquals(TEST_ISLIVE2, result.isLive());
		TEST_CROSSREF.setIsLive(null);
		assertTrue(Objects.isNull(TEST_CROSSREF.isLive()));
		assertTrue(Objects.isNull(result.isLive()));
	}

	@Test
	public void testGetTimestamp() throws InvalidSPDXAnalysisException {
		assertEquals(TEST_TIMESTAMP1, TEST_CROSSREF.getTimestamp());
		Resource r = TEST_CROSSREF.createResource(modelContainer);
		CrossRef result = new CrossRef(modelContainer, r.asNode());
		assertEquals(TEST_TIMESTAMP1, result.getTimestamp());
		TEST_CROSSREF.setTimestamp(TEST_TIMESTAMP2);
		assertEquals(TEST_TIMESTAMP2, TEST_CROSSREF.getTimestamp());
		assertEquals(TEST_TIMESTAMP2, result.getTimestamp());
		TEST_CROSSREF.setTimestamp(null);
		assertTrue(Objects.isNull(TEST_CROSSREF.getTimestamp()));
		assertTrue(Objects.isNull(result.getTimestamp()));
	}

	@Test
	public void testIsWayBackLink() throws InvalidSPDXAnalysisException {
		assertEquals(TEST_ISWAYBACK1, TEST_CROSSREF.isWayBackLink());
		Resource r = TEST_CROSSREF.createResource(modelContainer);
		CrossRef result = new CrossRef(modelContainer, r.asNode());
		assertEquals(TEST_ISWAYBACK1, result.isWayBackLink());
		TEST_CROSSREF.setIsWayBackLink(TEST_ISWAYBACK2);
		assertEquals(TEST_ISWAYBACK2, TEST_CROSSREF.isWayBackLink());
		assertEquals(TEST_ISWAYBACK2, result.isWayBackLink());
		TEST_CROSSREF.setIsWayBackLink(null);
		assertTrue(Objects.isNull(TEST_CROSSREF.isWayBackLink()));
		assertTrue(Objects.isNull(result.isWayBackLink()));
	}

	@Test
	public void testGetOrder() throws InvalidSPDXAnalysisException {
		assertEquals(TEST_ORDER1, TEST_CROSSREF.getOrder());
		Resource r = TEST_CROSSREF.createResource(modelContainer);
		CrossRef result = new CrossRef(modelContainer, r.asNode());
		assertEquals(TEST_ORDER1, result.getOrder());
		TEST_CROSSREF.setOrder(TEST_ORDER2);
		assertEquals(TEST_ORDER2, TEST_CROSSREF.getOrder());
		assertEquals(TEST_ORDER2, result.getOrder());
		TEST_CROSSREF.setOrder(null);
		assertTrue(Objects.isNull(TEST_CROSSREF.getOrder()));
		assertTrue(Objects.isNull(result.getOrder()));
	}

	@Test
	public void testSetDetails() throws InvalidSPDXAnalysisException {
		CrossRef result = new CrossRef(TEST_URL2);
		assertEquals(TEST_URL2, result.getUrl());
		assertTrue(Objects.isNull(result.isLive()));
		assertTrue(Objects.isNull(result.getMatch()));
		assertTrue(Objects.isNull(result.isValid()));
		assertTrue(Objects.isNull(result.isWayBackLink()));
		assertTrue(Objects.isNull(result.getOrder()));
		assertTrue(Objects.isNull(result.getTimestamp()));
		result.setDetails(TEST_ISVALID2, TEST_ISLIVE2, TEST_ISWAYBACK2, TEST_MATCH2, TEST_TIMESTAMP2);
		assertEquals(result.isLive(), TEST_ISLIVE2);
		assertEquals(result.getMatch(), TEST_MATCH2);
		assertEquals(result.isValid(), TEST_ISVALID2);
		assertEquals(result.isWayBackLink(), TEST_ISWAYBACK2);
		assertEquals(result.getTimestamp(), TEST_TIMESTAMP2);
		assertEquals(result.getUrl(), TEST_URL2);
		assertTrue(Objects.isNull(result.getOrder()));
	}

	@Test
	public void testToString() throws InvalidSPDXAnalysisException {
		TEST_CROSSREF.toString();
	}
}
