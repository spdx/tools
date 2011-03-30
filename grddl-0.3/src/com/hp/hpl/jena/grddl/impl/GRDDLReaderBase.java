/*
 (c) Copyright 2007 Hewlett-Packard Development Company, LP
 [See end of file]
 $Id: GRDDLReaderBase.java 1393 2007-05-25 12:21:58Z jeremy_carroll $
 */
package com.hp.hpl.jena.grddl.impl;

import java.net.URLConnection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;

import net.sf.saxon.TransformerFactoryImpl;

import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

import com.hp.hpl.jena.grddl.GRDDLSecurityException;
import com.hp.hpl.jena.rdf.arp.impl.ARPSaxErrorHandler;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFErrorHandler;
import com.hp.hpl.jena.rdf.model.RDFReader;
import com.hp.hpl.jena.rdf.model.impl.RDFDefaultErrorHandler;
import com.hp.hpl.jena.shared.BrokenException;
import com.hp.hpl.jena.shared.JenaException;

/**
 * GRDDLReaderBase
 * 
 * @author Jeremy J. Carroll
 */
public class GRDDLReaderBase {
	// static public class ProhibitUnparsedText {
	// public ProhibitUnparsedText() {
	// throw new GRDDLSecurityException("unparsed-text() not permitted in this
	// implementation");
	// }
	// }
	// static {
	// Entry unparsedText = StandardFunction.getFunction("unparsed-text", 1);
	// unparsedText.implementationClass = ProhibitUnparsedText.class;
	// unparsedText = StandardFunction.getFunction("unparsed-text-available",
	// 1);
	// unparsedText.implementationClass = ProhibitUnparsedText.class;
	//	    
	// }
	XMLReader tidyParser = new org.cyberneko.html.parsers.SAXParser();

	XMLReader saxParser = new org.apache.xerces.parsers.SAXParser();

	RDFReader rdfxml, n3;

	boolean disabled;

	private boolean rdfa;

	Set<String> xmlXforms = null;

	Set<String> htmlXforms = null;

	Map<String, String> headers = new HashMap<String, String>();

	private RDFErrorHandler eHandler = new RDFDefaultErrorHandler() {

		public void error(Exception e) {

			super.error(w(e));
		}

		public void fatalError(Exception e) {
			super.fatalError(w(e));
		}

		public void warning(Exception e) {
			super.warning(w(e));
		}

	};

	final TransformerFactory xformFactory = new TransformerFactoryImpl();
	{
		// headers.put("negotiate", "*");
		// System.err.println(xformFactory.getClass());
		xformFactory.setErrorListener(new ErrorListener() {
			public void error(TransformerException e)
					throws TransformerException {
				if (e.getCause() instanceof SeenEnoughExpectedException) {
					throw e;
				}
				checkException(e);
				eHandler.error(e);
			}

			public void fatalError(TransformerException e)
					throws TransformerException {
				checkException(e);
				eHandler.error(e);
				throw e;
			}

			public void warning(TransformerException e)
					throws TransformerException {
				eHandler.warning(e);
			}
		});
		xformFactory.setAttribute(
				net.sf.saxon.FeatureKeys.ALLOW_EXTERNAL_FUNCTIONS,
				Boolean.FALSE);

		try {
			xformFactory.setFeature(
					"http://javax.xml.XMLConstants/feature/secure-processing",
					true);
		} catch (TransformerConfigurationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		xformFactory.setAttribute(
				"http://saxon.sf.net/feature/version-warning", Boolean.FALSE);

		// xformFactory.setURIResolver(new SafeURIResolver(xformFactory
		// .getURIResolver()));
		Model m = ModelFactory.createDefaultModel();
		rdfxml = m.getReader("RDF/XML");
		n3 = m.getReader("N3");
		rdfxml.setErrorHandler(eHandler);
		n3.setErrorHandler(eHandler);
		setProperty("http://cyberneko.org/html/features/insert-namespaces",
				"true");
		setProperty("http://cyberneko.org/html/properties/names/elems", "lower");

		ARPSaxErrorHandler saxErrorHandler = new ARPSaxErrorHandler(eHandler);
		tidyParser.setErrorHandler(saxErrorHandler);
		saxParser.setErrorHandler(saxErrorHandler);
		try {
			saxParser.setFeature(
					"http://xml.org/sax/features/use-entity-resolver2", false);
			saxParser.setFeature(
					"http://xml.org/sax/features/external-general-entities"

					, false);
			saxParser.setFeature(
					"http://xml.org/sax/features/external-parameter-entities"

					, false);
			// saxParser.setFeature(
			// "http://apache.org/xml/features/nonvalidating/load-external-dtd"
			//						 
			// , false);

		} catch (SAXNotRecognizedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (SAXNotSupportedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

	public RDFErrorHandler setErrorHandler(RDFErrorHandler errHandler) {
		RDFErrorHandler old = eHandler;
		eHandler = errHandler;
		rdfxml.setErrorHandler(eHandler);
		n3.setErrorHandler(eHandler);
		((ARPSaxErrorHandler) tidyParser.getErrorHandler())
				.setErrorHandler(eHandler);
		return old;
	}

	protected Exception w(Exception e) {
		if (e instanceof TransformerException) {
			System.err
					.println(((TransformerException) e).getLocationAsString());
		}
		return e;
	}

	protected RDFErrorHandler eHandler() {
		return eHandler;
	}

	/**
	 * There are currently no properties specific to the GRDDL reader.
	 * Properties starting <code>"http://cyberneko.org/"</code> modify the
	 * behaviour of the HTML parser, as documented; and other properties modify
	 * the behaviour of the RDF/XML parser.
	 * 
	 * @param propName
	 *            A property name.
	 * @param propValue
	 *            The new value of the property.
	 * @return Thge old value of the property.
	 */
	public Object setProperty(String propName, Object propValue) {

		Object old;
		String propNameLC = propName.toLowerCase();
		// "http://apache.org/xml/features/xinclude"
		// "http://apache.org/xml/features/nonvalidating/load-external-dtd"
		if (propNameLC.startsWith("grddl.")) {
			String p = propName.substring(6).toLowerCase();
			if (p.equals("xml-xform")) {
				old = xmlXforms;
				xmlXforms = addXform(propValue, xmlXforms );
			} else if (p.equals("html-xform")) {
				old = htmlXforms;
				htmlXforms = addXform(propValue, htmlXforms );
			} else if (p.equals("disable")) {
				old = new Boolean(disabled);
				disabled = toBoolean(propValue, disabled);
			} else if (p.equals("rdfa")) {
				old = new Boolean(rdfa);
				rdfa = toBoolean(propValue, rdfa);
				if (rdfa) {
					disabled = true;
					htmlXforms = addXform("http://www-sop.inria.fr/acacia/soft/RDFa2RDFXML_v_0_8.xsl", htmlXforms );
				}
			} else {
				error(propName);
				old = null;
			}
			return old;
		}

		if (propNameLC.startsWith("header.")) {
			String hdr = propName.substring(7).toLowerCase();
			old = headers.get(hdr);
			headers.put(hdr, (String) propValue);
			return old;
		}

		if (propName.startsWith("http://cyberneko.org/")) {
			try {
				return setSAXFeatureOrProperty(propName, propValue, tidyParser);
			} catch (SAXNotRecognizedException e) {
				eHandler.error(e);
				return null;
			} catch (SAXNotSupportedException e) {
				eHandler.error(e);
				return null;
			}
		}

		if (propName.startsWith("http://apache.org/")) {
			try {
				old = setSAXFeatureOrProperty(propName, propValue, saxParser);
			} catch (SAXNotRecognizedException e) {
				eHandler.error(e);
				return null;
			} catch (SAXNotSupportedException e) {
				eHandler.error(e);
				return null;
			}
			try {
				setSAXFeatureOrProperty(propName, propValue, tidyParser);
			} catch (SAXNotRecognizedException e) {
				// ignore errors.
			} catch (SAXNotSupportedException e) {
			}
			rdfxml.setProperty(propName, propValue);
			return old;
		}

		if (propName.startsWith("http://saxon.sf.net/")) {
			int b = toBoolean(propValue);
			switch (b) {
			case 0:
			case 1:
				try {
					boolean oldb = xformFactory.getFeature(propName);
					xformFactory.setFeature(propName, b == 1);
					return new Boolean(oldb);
				} catch (Exception e) {
					// fall through
				}
			case -1:
				try {
					old = xformFactory.getAttribute(propName);
					xformFactory.setAttribute(propName, propValue);
					return old;
				} catch (Exception e) {
					eHandler.error(e);
					return null;
				}
			}
		}

		return rdfxml.setProperty(propName, propValue);
	}

	private Set<String> addXform(Object propValue, Set<String> xf) {
		if (propValue != null) {
			if (xf == null) {
				xf = new HashSet<String>();
			}
			xf.add(propValue.toString());
			return xf;
		} else {
			return null;
		}
	}

	private void error(String msg) {
		eHandler.error(new JenaException("unrecognised option: " + msg));
	}

	private boolean toBoolean(Object propValue, boolean def) {
		switch (toBoolean(propValue)) {
		case 1:
			return true;
		case 0:
			return false;
		case -1:
		default:
			error("illegal value for boolean option: " + propValue);
			return def;
		}
	}

	/**
	 * 
	 * @param propValue
	 * @return 0 if propValue is false or variant, 1 if propValue is true, -1 if
	 *         non-Boolean
	 */
	private int toBoolean(Object propValue) {
		if (propValue instanceof Boolean) {
			return ((Boolean) propValue).booleanValue() ? 1 : 0;
		} else if (propValue instanceof String) {
			if ("true".equalsIgnoreCase((String) propValue))
				return 1;
			if ("false".equalsIgnoreCase((String) propValue))
				return 0;
		}
		return -1;
	}

	private Object setSAXFeatureOrProperty(String propName, Object propValue,
			XMLReader parser) throws SAXNotRecognizedException,
			SAXNotSupportedException {
		int b = toBoolean(propValue);
		switch (b) {
		case 0:
		case 1:
			boolean oldb = parser.getFeature(propName);
			parser.setFeature(propName, b == 1);
			return new Boolean(oldb);
		case -1:
			Object old = parser.getProperty(propName);
			parser.setProperty(propName, propValue);
			return old;
		}
		throw new BrokenException("impossible");
	}

	GRDDLSecurityException lastSecurityException;

	void checkException(Exception e) {
		if (e.getCause() instanceof GRDDLSecurityException) {
			lastSecurityException = (GRDDLSecurityException) e.getCause();
			throw lastSecurityException;
		}

		String msg = e.getMessage();
		if (msg.contains("result-document") || msg.contains("disabled")
				|| msg.contains("extension")) {
			lastSecurityException =

			new GRDDLSecurityException(e);
			throw lastSecurityException;
		}
	}

	void setHeaders(URLConnection conn) {

		Iterator<Entry<String, String>> i = headers.entrySet().iterator();
		while (i.hasNext()) {
			Entry<String, String> e = i.next();
			conn.setRequestProperty(e.getKey(), e.getValue());
			// System.err.println(e.getKey() + ": "+ e.getValue());
		}
	}
}

/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP All rights
 * reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 
 * 3. The name of the author may not be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */