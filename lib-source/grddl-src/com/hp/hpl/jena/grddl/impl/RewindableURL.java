/*
 (c) Copyright 2007 Hewlett-Packard Development Company, LP
 [See end of file]
 $Id: RewindableURL.java 1170 2007-04-24 13:50:52Z jeremy_carroll $
 */
package com.hp.hpl.jena.grddl.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.stream.StreamSource;

/**
 * RewindableURL
 * 
 * @author Jeremy J. Carroll
 */
public class RewindableURL extends Rewindable {
	final URL url;

	final String encoding;

	final String mimetype;

	StreamSource source;
	
	final GRDDL grddl;

	private StreamSource openSource = null;
	final URLConnection conn;
//	public RewindableURL(String u) throws IOException {
//		this(u,null);
//	}
	public RewindableURL(String u, GRDDL g) throws IOException {
		super(u);
		GRDDL.logurl(u);
		grddl = g;
		url = new URL(u);
		conn = url.openConnection();
		conn.setRequestProperty("accept", "application/rdf+xml; q=0.5, "
				+ "application/xhtml+xml; q=1.0, " + "text/html; q=0.7, "
				+ "application/xml; q=1.0, " + "text/xml; q=0.7, "
				+ "application/rss+xml; q=0.2, " + "*/*; q=0.1");

//		conn.setRequestProperty("negotiate","*");
		g.setHeaders(conn);
		encoding = conn.getContentEncoding();
		mimetype = conn.getContentType();
		String newU = conn.getURL().toString();
		if (!newU.equals(url.toString())) {
			// TODO worry about IRI issues here
			updateRetrivalIRI(newU);

			GRDDL.logurl(newU);
		}
		
//		System.err.println("GET "+u);
//		Iterator<Entry<String,List<String>>> it = conn.getHeaderFields().entrySet().iterator();
//		while (it.hasNext()){
//			Entry<String,List<String>> e = it.next();
//			System.err.print(e.getKey()+":");
//			Iterator<String> i2 = e.getValue().iterator();
//			while (i2.hasNext()) {
//				System.err.println(" "+i2.next());
//			}
//		}
//		System.err.println();
		String link = conn.getHeaderField("Link");
			String profile = conn.getHeaderField("Profile");
			if (link != null && profile != null
			  && ( profile.equals(GRDDL.PROFILE) ||
					  profile.equals("<"+GRDDL.PROFILE+">") )
			 ) {
				addTransforms(link,g);
			}
		
		source = toStreamSource(conn.getInputStream());
	}

	Pattern linkRegex = Pattern.compile("<([^>]*)>(?: *; *([-a-zA-Z0-9_]+) *=([^,]*)),?");
	// link = <http://www.w3.org/2000/06/dc-extract/dc-extract.xsl>; rel="transformation"
	private void addTransforms(String link, GRDDL g) {
		Matcher m = linkRegex.matcher(link);
		while (m.find()) {
			if (m.groupCount()==3) {
				String url = m.group(1);
				String prop = m.group(2);
				String val = m.group(3);
				if (prop.equals("rel")) {
					val = val.trim();
					if (val.equals("\"transformation\"")) {
//						System.err.println("t "+url);
						g.addTransform(this.resolveAgainstRetrievalIRI(url));
					}
				}
			}
		}
	}
	private StreamSource toStreamSource(InputStream in)
			throws UnsupportedEncodingException {
		if (encoding == null) {
			return new StreamSource(in, url.toString());
		} else {
			return new StreamSource(new InputStreamReader(in, encoding), url
					.toString());
		}
	}

	String encoding() {
		return encoding;
	}

	String mimetype() {
		return mimetype;
	}

	StreamSource startAfreshRaw(boolean rewindable) throws IOException {
		close();
		if (source != null) {
			openSource = source;
			source = null;
		} else {
			openSource = toStreamSource(reopen().getInputStream());
		}
		return openSource;
	}

	private URLConnection reopen() throws IOException {
		URLConnection conn = url.openConnection();
		
		grddl.setHeaders(conn);
		conn.setRequestProperty("accept", mimetype);
		
//		conn.setRequestProperty("Cache-Control","no-cache");
		if (!equals(encoding,conn.getContentEncoding()))
			throw new RuntimeException("error handling not implemented");
		if (!equals(mimetype,conn.getContentType()))
			throw new RuntimeException("error handling not implemented");

		return conn;
	}

	private boolean equals(Object a, Object b) {
		if (a==null)
			return b==null;
		return a.equals(b);
	}

	void close() throws IOException {
		if (openSource != null) {
			if (openSource.getInputStream() != null)
				openSource.getInputStream().close();
			if (openSource.getReader() != null)
				openSource.getReader().close();
			openSource = null;
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