/*
 (c) Copyright 2006 Hewlett-Packard Development Company, LP
 [See end of file]
  $Id: GRDDL.java 2237 2007-09-24 10:04:04Z jeremy_carroll $
 */
package com.hp.hpl.jena.grddl.impl;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.Controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import com.hp.hpl.jena.grddl.GRDDLReader;
import com.hp.hpl.jena.grddl.GRDDLSecurityException;
import com.hp.hpl.jena.grddl.license.License;
import com.hp.hpl.jena.rdf.arp.JenaReader;
import com.hp.hpl.jena.rdf.arp.SAX2Model;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.util.cache.Cache;
import com.hp.hpl.jena.util.cache.CacheManager;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * ReaderImpl
 * 
 * @author Jeremy J. Carroll
 */
public class GRDDL {
	static {
		License.check();
	}
	final List<String> transforms = new ArrayList<String>();

	final List<String> alternates = new ArrayList<String>();
	public static final String NAMESPACE = "http://www.w3.org/2003/g/data-view#";

	public static final String PROFILE = "http://www.w3.org/2003/g/data-view";

	public static final String XHTMLNS[] = { "http://www.w3.org/1999/xhtml",
			"http://www.w3.org/2002/06/xhtml2/", "" };

	private static final Property ProfileTransformation = ResourceFactory
			.createProperty(NAMESPACE, "profileTransformation");

	private static final Property NamespaceTransformation = ResourceFactory
			.createProperty(NAMESPACE, "namespaceTransformation");

	final Model model;

	final GRDDLReader reader;

	Rewindable input;

	public GRDDL(GRDDLReader r, Model m, String url) throws IOException {
		this(r,m);
		RewindableURL rewindableURL = new RewindableURL(url,this);
		input = rewindableURL;
//		if (!rewindableURL.conn.getURL().toString().equals(url))
//			System.err.println("URL changed: "+rewindableURL.conn.getURL() + " was "+url);
		if (((GRDDLReaderBase)r).headers.containsKey("negotiate")) {
			tcn(rewindableURL);
		}
			
	}
	
	private void tcn(RewindableURL rewindableURL) {
		String alternates = rewindableURL.conn.getHeaderField("alternates");
		if (alternates==null)
			return;
		String current = rewindableURL.conn.getHeaderField("content-location");
		if (current==null)
			current = input.retrievalIRI();
		else
			current = input.resolveAgainstRetrievalIRI(current);
		parseAlternates(alternates,current);
		
	}

	private void parseAlternates(String alternates, String done) {
		Iterator<String> it = tokenize(alternates).iterator();
		int state = 0;
		while (it.hasNext()) {
			String n = it.next();
			if (n.equals(",")) {
				state = 0;
			} else if (state == 0 && n.equals("{")) {
				state = 1;
			} else if (state == 1 && n.startsWith("\"")) {
                String a = unescape(n.substring(1,n.length()-1));
                a = input.resolveAgainstRetrievalIRI(a);
                if (!a.equals(done)) {
                	addAlternate(a);
                }
				state = 2;
			} else {
				state = 2;
			}
		}
		
	}




	static Pattern unescaper = Pattern.compile(
				"\\\\(.)"
		);
	/**
	 * Replace \. with . 
	 * @param string Does not end in unescaped \
	 * @return unescaped version
	 */
	public String unescape(String string) {
		return unescaper.matcher(string).replaceAll("$1");
	}


	static Pattern tokenizer = Pattern.compile(
		// quoted-string
				"\"([^\\\"]|\\.)*\"" 
			    + "|" +
		//  separators
			    "[\\0133\\0135()<>@,;:\\/?={}]"
			    + "|" +
		// tokens	   
			   "[\\041-\\0176&&[^\\0133\\0135()<>@,;:\\\"/?={}]]+"
		);
	
	/**
	 * Split input into tokens
	 * ignoring whitespace
	 * except the quoted-string production.
	 * @param alternates
	 * @return
	 */
	private List<String> tokenize(String alternates) {
		List<String> r = new ArrayList<String>();
//		System.err.println("Tokenizing: "+alternates);
		Matcher m = tokenizer.matcher(alternates);
		while (m.find()) {
			r.add(m.group());
		}
		return r;
	}

	public GRDDL(GRDDLReader r, Model m, Rewindable rw) {
		this(r,m);
		input = rw;
	}
	private GRDDL(GRDDLReader r, Model m) {
		model = m;
		reader = r;
	}

	/**
	 * Apply GRDDL algorithm.
	 * 
	 * @throws IOException
	 * 
	 */
	public synchronized void go() throws IOException {
		initialParse();
		reapTransforms();
		applyTransforms();
		getAlternates();
	}


	private void getAlternates() {
		Iterator<String> urls = alternates.iterator();
		while (urls.hasNext())
		   reader.read(model, urls.next());
	}

	private void addAlternate(String a) {
		alternates.add(a);
	}
	
	private TransformerFactory xformFactory() {
		return ((GRDDLReaderBase) reader).xformFactory;
	}

	private void initialParse() throws IOException
	{
		schemas = new HashSet<String>();
		profiles = new HashSet<String>();
		needTidy = false;

		GRDDLReaderBase readerB = ((GRDDLReaderBase)reader);
		input.useSaxOrTidy(readerB.saxParser);
		InitialContentHandler ic;
		ic = new InitialContentHandler(readerB.disabled);
		SAXResult r = new SAXResult(ic);
		try {
			Transformer idTransform = xformFactory().newTransformer();
			try {
				idTransform.transform(input.startAfresh(true), r);
			} catch (SeenEnoughExpectedException e) {
				// parse finished normally
			} catch (TransformerException e) {
				if (!e.getCause().getClass().equals(
						SeenEnoughExpectedException.class)) {
					throw e;
				}
			}
			if (needTidy) {
				input.useSaxOrTidy(readerB.tidyParser);
				ic = new InitialContentHandler(readerB.disabled);
				
				r = new SAXResult(ic);
				idTransform.transform(input.startAfresh(true), r);
			}
		} catch (SeenEnoughExpectedException e) {
		} catch (TransformerException e) {
			if (!e.getCause().getClass().equals(
					SeenEnoughExpectedException.class)) {
				throw new JenaException(e);
			}
			// else parse finished normally
		} catch (RuntimeException rte) {
			throw rte;
		} catch (IOException ioe) {
			throw ioe;
		} catch (Exception nrte) {
			throw new JenaException(nrte);
		} finally {
			input.close();
		}
		
		addTransforms(ic.html?readerB.htmlXforms:readerB.xmlXforms);

	}

	private void addTransforms(Set<String> xforms) {
		if (xforms != null) {
			Iterator<String> it = xforms.iterator();
			while (it.hasNext()) {
				addTransform(it.next());
			}
		}
	}

	private void applyTransforms() {
		Iterator<String> it = transforms.iterator();
		while (it.hasNext()) {
			String n = it.next();
			transformWith(n, it.hasNext());
		}
	}

	private boolean transformWith(String string, boolean needRewind) {
		try {
			try {
				final Transformer t = transformerFor(string);
				String mimetype = mimetype(t);
				final Result result = resultFor(mimetype);
				if (result==null)
					return false;
				final Source in = input.startAfresh(needRewind);
				runInSandbox(new TERunnable() {
					public void run() throws TransformerException {
							t.transform(in, result);
					}
				}, true);
				postProcess(mimetype, result);
				return true;
			} catch (TransformerException e) {
				error(e);
				return false;
			} catch (SAXParseException e) {
				error(e);
				return false;
			} catch (InterruptedException e) {
				throw new InterruptedIOException("In GRDDL transformWith");
			} finally {
				input.close();
				if (subThread != null)
					subThread.interrupt();
			}
		} catch (IOException ioe) {
			error(ioe);
			return false;
		}
	}

	private void postProcess(String mimetype, Result result) throws IOException, InterruptedException {
		if (mimetype.equalsIgnoreCase("text/rdf+n3"))
		    endN3(result);
		if ( mimetype.equalsIgnoreCase("text/html")) 
		      endGrddlResult(result);
		if ( mimetype.equalsIgnoreCase("application/xhtml+xml")) 
		      endGrddlResult(result);
	}

	private void endGrddlResult(Result result) {
		// TODO security issues here
		StreamResult sr = (StreamResult)result;
		StringWriter sw = (StringWriter)sr.getWriter();
		
		String html = sw.toString();
		System.err.println(html);
		reader.read(model, 
				new StringReader(html), 
				input.retrievalIRI());
		
	}

	private void endN3(final Result result) throws IOException, InterruptedException {
		((StreamResult) result).getWriter().close();
		if (subThread != null)
			subThread.join();
		subThread = null;
	}

	private void runInSandbox(final TERunnable r, boolean protect) throws TransformerException {
		final TransformerException te[] = new TransformerException[]{null};
		final RuntimeException re[] = new RuntimeException[]{null};
		Runnable rr = new Runnable() {
			public void run() {
				try {
					r.run();
				} catch (TransformerException e) {
					te[0] = e;
				} catch (RuntimeException e) {
					re[0] = e;
				}
			}
		};
		if (protect) {
		// Check that the user is aware of the risk,
		// before running the untrusted code.
		  License.check();
		  SecManager.sandbox(rr);
		} else {
			rr.run();
		}
		if (te[0]!= null) 
			throw te[0];
		if (re[0]!= null) 
			throw re[0];
	}

	private void fatalError(Exception e) {
		((GRDDLReaderBase)reader).checkException(e);
		reader.eHandler().fatalError(e);
	}

	private void error(Exception e) {
		((GRDDLReaderBase)reader).checkException(e);
		reader.eHandler().error(e);
	}

	private Result resultFor(String mimeType) throws SAXParseException,
			IOException {
			if ( mimeType.equalsIgnoreCase("text/rdf+n3")) 
			      return n3result();

			if ( mimeType.equalsIgnoreCase("text/html")) 
			      return grddlResult();
			if ( mimeType.equalsIgnoreCase("application/xhtml+xml")) 
			      return grddlResult();
			if ( mimeType.equalsIgnoreCase("application/rdf+xml")) 
				return rdfXmlResult();

			if ( mimeType.equalsIgnoreCase("application/xml")) 
				return rdfXmlResult();
			System.err.println("Unsupported mimetype: "+mimeType);
			return null;
	}

	private String mimetype(Transformer t) {
		String mt =  t.getOutputProperty(OutputKeys.MEDIA_TYPE);
		return mt==null?
				"application/rdf+xml" : mt;
	}

	private Result grddlResult() throws IOException {
		return new StreamResult(
				new StringWriter()
		
		);
	}




	Thread subThread = null;

	private PipedWriter pipe;

	private boolean needTidy = false;

	private Set<String> profiles;

	private Result n3result() throws IOException {
		pipe = new PipedWriter();
		final PipedReader pr = new PipedReader(pipe);
		Result rslt = new StreamResult(pipe);
		subThread = new Thread() {
			public void run() {
				((GRDDLReaderBase) reader).n3
						.read(model, pr, input.retrievalIRI());
			}
		};
		subThread.start();
		return rslt;
	}

	private Result rdfXmlResult() throws SAXParseException {
		// TODO check resolve here
		SAX2Model s2m = SAX2Model.create(input.resolve(""), 
				model);
		s2m.setErrorHandler(reader.eHandler());
		s2m.setOptionsWith(((JenaReader) ((GRDDLReaderBase) reader).rdfxml)
				.getOptions());
		SAXResult r = new SAXResult(s2m);
		r.setLexicalHandler(s2m);
		return r;
	}

	private Transformer transformerFor(final String url) throws TransformerException {
		if (url.equals("RDF/XML")) {
			return xformFactory().newTransformer();
		} else {
				logurl(url);
			try {
				((GRDDLReaderBase)reader).lastSecurityException = null;
				final Transformer rslt[] =  {null};
				// TODO  network and source issues
				final Source src = xsltStreamSource(url);
				runInSandbox(new TERunnable() {
					public void run() throws TransformerException {
							rslt[0] = xformFactory().newTransformer(src);
					}

					
				},true);
				
				
				SafeURIResolver safeURIResolver = new SafeURIResolver();
				rslt[0].setURIResolver(safeURIResolver);
			    ((Controller)rslt[0]).setUnparsedTextURIResolver(safeURIResolver);
			    
				return rslt[0];

			}
//			catch (AssertionError e) {
//				if (e.getMessage().startsWith("Failed to load system function: unparsed-text()"))
//				   throw new GRDDLSecurityException("unparsed-text() not permitted in this implementation");
//			     throw e;
//			}
			catch (SecurityException e) {
				throw new GRDDLSecurityException(e);
			}
			catch (TransformerException e) {
//				if (e.toString().contains("result-document")
//					|| e.toString().contains("disabled")
//					|| e.toString().contains("extension") )
//					throw new GRDDLSecurityException(e);
				if (((GRDDLReaderBase)reader).lastSecurityException != null)
					throw ((GRDDLReaderBase)reader).lastSecurityException;
				System.err.println("<" + url+"> A.Rethrowing "+ e.getMessage());
				throw e;
					
			}
			catch (RuntimeException e) {
				System.err.println("<" + url+"> B.Rethrowing "+ e.toString()+ ":" + e.getMessage());
				throw e;
			}
		}
	}
	
	static Set<String> allUrls = new HashSet<String>();
	static void logurl(String url) {
//		if (!url.startsWith("http://www.w3.org/2001/sw/grddl-wg/td/"))
		if (!allUrls.contains(url)) {
			allUrls.add(url);
//			System.err.println("Using url: "+url);
		}
	}

	private Source xsltStreamSource(String url) throws TransformerException {
		try {
			URL urlx = new URL(url);
			URLConnection conn = urlx.openConnection();
			conn.setRequestProperty("accept",
					"application/xslt+xml; q=1.0, "
					+ "text/xsl; q=0.8, " + "application/xsl; q=0.8, "
					+ "application/xml; q=0.7, " + "text/xml; q=0.6, "
					+ "application/xsl+xml; q=0.8, " + "*/*; q=0.1"
			);
			return new StreamSource(conn.getInputStream(),conn.getURL().toString());
		} catch (IOException e) {
			throw new TransformerException(e);
		}
	}

	/*
	 * 
	 * Read beginning of file ... as XML if first element is xhtml:html then a)
	 * assume legal XHTML+XML read as XML looking for profile and/or grddl
	 * transformation thing if profile found then need to go through whole doc
	 * looking for rel's either transformation or profileTransformation and
	 * apply b) if first element is html or case variants and not in xml
	 * namespace then apply tidy (streaming) and go to a) c) other wise look for
	 * grddl namespace stuff at top level and/or in namespace doc
	 * 
	 * 
	 * 
	 */

	private class InitialContentHandler extends DefaultHandler {
		
		private boolean disabled;

		InitialContentHandler(boolean dis) {
			disabled = dis;
		}
		boolean rootElement = true;

		boolean grddlNamespace = false;

		boolean grddlProfile = false;
		
		boolean html = false;
		
		boolean inHead = false;

		public void endElement(String uri, String localName, String qname) {
			trace("<element: "+qname);
			if (html && isHtmlNS(uri) && localName.equalsIgnoreCase("html")) {
				inHead = false;
			}
		}
		public void startElement(String uri, String localName, String qname,
				Attributes attr) throws SAXException {
		    trace(">element: "+qname);
			if (rootElement) {
				html = isHtmlMimetype();
				if (uri != null && !uri.equals("")) {
					html = html || isHtmlNS(uri);
					if (disabled)
						throw new SeenEnoughExpectedException();
					checkSchema(input.resolve(uri));
				} else if (localName.equalsIgnoreCase("html"))
					html = true;

				if (grddlNamespace)
					checkRootAttrs(attr);
				if (disabled || !html)
					throw new SeenEnoughExpectedException();

				if (!localName.equalsIgnoreCase("html")) {
					needTidy(); // doesn't usually return
				}

			}
			// doing html
			if (attr.getValue("xml:base")!=null) {
				// TODO error handling
				System.err.println(input.retrievalIRI()+ ": xml:base should not be used within HTML - ignored");
			}
			if (!grddlProfile) {
				if (localName.equalsIgnoreCase("head")
						&& isHtmlNS(uri) ) {
					checkProfileAttrs(attr);
					inHead = true;
				}
				if ((!rootElement) && (!grddlProfile)) {
					// The head must be second, or maybe first
					// in ill-formed HTML.
					// If we've not seen the profile by this
					// point, then we won't and we stop.
//					throw new SeenEnoughExpectedException();
				}
				rootElement = false;
				if ((!inHead) && !grddlProfile)
					return;
			}
			if (localName.equalsIgnoreCase("base")
					&& isHtmlNS(uri)) {
				String href = attr.getValue("href");
				if ( href != null) {
//					System.err.println("setting base to: "+href);
					input.setBase(href);
				}
			}
			if (!grddlProfile)
				return;
			if (localName.equalsIgnoreCase("a")
					|| localName.equalsIgnoreCase("link")) {
				checkLinkAttrs(attr);
			}

		}
		private boolean isHtmlNS(String uri) {
			for (int i = 0; i < XHTMLNS.length; i++)
				if (uri.equalsIgnoreCase(XHTMLNS[i])) {
					return true;
				}
			return false;
		}

		public void fatalError(SAXParseException e) throws SAXException {
			if (grddlProfile || isHtmlMimetype()) {
				needTidy();
			}
			reader.eHandler().fatalError(e);
		}

		public void error(SAXParseException e) {
			reader.eHandler().error(e);
		}

		public void warning(SAXParseException e) {
			reader.eHandler().warning(e);
		}

		private void checkLinkAttrs(Attributes attr) {
			String rel = getValueIgnoreCase(attr, "rel");
			if (rel == null)
				return;
			String r[] = rel.split(" +");
			for (int i = 0; i < r.length; i++) {
				if (r[i].equalsIgnoreCase("transformation"))
					addTransform(input.resolve(getValueIgnoreCase(attr, "href")));
			}
		}

		private String getValueIgnoreCase(Attributes attr, String arg) {
			int ln = attr.getLength();
			for (int i = 0; i < ln; i++) {
				if (arg.equalsIgnoreCase(attr.getQName(i)))
					return attr.getValue(i);
			}
			return null;
		}

		private void checkProfileAttrs(Attributes attr) {
			String profs = getValueIgnoreCase(attr, "profile");
			if (profs == null)
				return;
			String p[] = profs.split("[ \t\n]+");
			for (int i = 0; i < p.length; i++) {
				if (PROFILE.equals(p[i]) || NAMESPACE.equals(p[i])) {
//					System.err.println("Grddl profile");
					grddlProfile = true;
				} else
				    checkProfile(input.resolve(p[i]));
			}

		}

		private void checkRootAttrs(Attributes attr) {
			String xmlBase = attr.getValue("xml:base");
			if (xmlBase != null)
				input.setBase(xmlBase);
			
			String transforms = attr.getValue(NAMESPACE, "transformation");
			if (transforms == null)
				return;
			String t[] = transforms.split(" +");
			for (int i = 0; i < t.length; i++)
				addTransform(input.resolve(t[i]));
		}

		public void startPrefixMapping(String prefix, String uri)
				throws SAXException {
			if (rootElement && (!grddlNamespace) && NAMESPACE.equals(uri)) {
				grddlNamespace = true;
			}

		}
	}

	private boolean isHtmlMimetype() {
		String mime = input.mimetype();
		if (mime == null)
			return false;
		return mime.equalsIgnoreCase("text/html")
				|| mime.equalsIgnoreCase("application/xhtml+xml");
	}

	void addTransform(String tUrl) {
		trace("transform: " + tUrl);
//		if ("http://www.w3.org/2003/g/sq1t.xsl".equals(tUrl))
//			tUrl = "http://www.w3.org/2001/sw/grddl-wg/td/sq1t.xsl";
//		if ( tUrl.startsWith("http://www.w3.org/2003/g/")
//			&& ( tUrl.contains("embeddedRDF")
//			  || tUrl.contains("glean-profile") ) )
//			tUrl = "http://lists.w3.org/Archives/Public/public-grddl-wg/2007Mar/att-0104/"
//				+
//				  tUrl.substring("http://www.w3.org/2003/g/".length());
//		
		
		transforms.add(tUrl);
	}

	private void checkProfile(String pUrl) {
		trace("profile: " + pUrl);
		profiles.add(pUrl);
//		reapTransforms(pUrl, ProfileTransformation);
	}
    private void reapTransforms() {
    	Set<String> ss = schemas;
    	Set<String> ps = profiles;
    	reapTransforms(ss,NamespaceTransformation);
    	reapTransforms(ps,ProfileTransformation);
    }
	private void reapTransforms(Set<String> ss, Property prop) {
		Iterator<String> it = ss.iterator();
		while (it.hasNext())
			reapTransforms(it.next(),prop);
	}

	private void reapTransforms(String pUrl, Property property) {
//		System.err.println("reaping: "+pUrl+" "+property.getURI());
		StmtIterator it = getModel(pUrl).createResource(pUrl).listProperties(
				property);
		while (it.hasNext()) {
			RDFNode n = it.nextStatement().getObject();
			if (n.isURIResource()) {
//				System.err.println("reaped: "+((Resource) n).getURI());
				
				addTransform(((Resource) n).getURI());
			} else {
				warning("Bad " + property.getLocalName() + "value in <" + pUrl
						+ ">. No tranform applied for this value.");
			}
		}
	}

	private void warning(String string) {
		reader.eHandler().warning(new GRDDLWarningException(string));
	}

	/*
	 * private void addModel(String url) { model.read(url, "GRDDL"); }
	 */

	// static Map<String, Model> known = new HashMap<String, Model>();
	static Cache known = CacheManager.createCache(CacheManager.RAND,
			"GRDDL schema/profile cache", 300);

//	static private boolean saveFlag;
	
	private Model getModel(String url) {

		Model m = (Model) known.get(url);
		if (m == null) {
//			if (url.equals(PROFILE))
//				saveFlag = true;
			m = ModelFactory.createDefaultModel();
			known.put(url, m);
			try {
			   reader.read(m, url);
			}
			catch (GRDDLSecurityException e) {
				// escalate security issues
				throw e;
			}
			catch (Exception e) {
				// ignore anything else
				m = ModelFactory.createDefaultModel();
				known.put(url,m);
			}
//			if (saveFlag) {
//			System.out.println("<!-- GRDDL of "+url+"-->");
//			m.write(System.out);
//			}
//			if (url.equals(PROFILE))
//				saveFlag = false;
		}
		return m;
	}

	public void needTidy() {
		trace("needTidy");
		needTidy = true;
		throw new SeenEnoughExpectedException();
	}
	
    private Set<String> schemas;
	private void checkSchema(String uri) {
		trace("schema: " + uri);
		if (uri.equals(RDF.getURI())) {
			addTransform("RDF/XML");
		} else
			schemas.add(uri);
		
	}

	public static final Log logger = LogFactory.getLog(GRDDL.class);

	private void trace(String string) {
//		System.err.println(string);
		logger.trace(string);
	}
 
	void setHeaders(URLConnection conn) {
		 ((GRDDLReaderBase)reader).setHeaders(conn);
	}

}

/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP All rights
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