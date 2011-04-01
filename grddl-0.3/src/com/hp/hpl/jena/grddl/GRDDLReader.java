/*
  (c) Copyright 2006 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: GRDDLReader.java 2242 2007-09-24 15:35:19Z jeremy_carroll $
*/
package com.hp.hpl.jena.grddl;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import com.hp.hpl.jena.grddl.impl.GRDDL;
import com.hp.hpl.jena.grddl.impl.GRDDLReaderBase;
import com.hp.hpl.jena.grddl.impl.RewindableInputStream;
import com.hp.hpl.jena.grddl.impl.RewindableReader;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFReader;

/** 
 * An implementation of GRDDL for Jena,
 * as an RDFReader.
 * @author Jeremy J. Carroll
 */
public class GRDDLReader extends GRDDLReaderBase implements RDFReader {
	public GRDDLReader() {
    }
	

	public void read(Model model, Reader r, String base) {
		try {
			new GRDDL(this,model,new RewindableReader(r,base)).go();
		} catch (IOException e) {
			failure(e);
		}
	}

	public void read(Model model, InputStream r, String base) {
		try {
			new GRDDL(this,model,new RewindableInputStream(r,base)).go();
		} catch (IOException e) {
			failure(e);
		}
	}

	public void read(Model model, String url) {
		try {
			new GRDDL(this,model,url).go();
		} catch (IOException e) {
			failure(e);
		}
	}
	private void failure(IOException e) {
		eHandler().fatalError(e);
	}
	
	/**
	 * Set properties of the GRDDL reader and its
	 * subsystems.
	 * 
	 * <p>
	 * GRDDL specific properties include the following:
	 * </p>
	 * <table>
	 * <tr><td>header.*</td><td>Sets an HTTP request header.</td></tr>
	 * <tr><td>header.negotiate</td><td>See section 8.4 of
	 * <a href="http://www.ietf.org/rfc/rfc2295.txt">RFC 2295</a>.
	 * <br/>
	 * Also enables client side support for transparent content negotiation,
	 * including getting all results.</td></tr>
	 * <tr><td>grddl.rdfa</td><td>set to true for RDFA processing only</td></tr>
	 * <tr><td>grddl.disabled</td><td>if true then only transforms specified 
	 *   with properties are applied</td></tr>
	 * <tr><td>grddl.xml-xforms</td><td>Add propvalue to list of xforms
	 * to apply to all (non-XHTML) XML documents. (null clears list)</td></tr>
	 * <tr><td>grddl.html-xforms</td><td>Add propvalue to list of xforms
	 * to apply to all (X)HTML documents. (null clears list)</td></tr>
	 * </table>
	 * <p>
	 * Properties starting
	 * <code>"http://cyberneko.org/"</code>
	 * modify the behaviour of the HTML parser,
	 * as 
	 * <a href=
	 * "http://people.apache.org/~andyc/neko/doc/html/settings.html">
	 * documented</a>; and 
	 * <a href=
	 * "http://jena.sourceforge.net/javadoc/com/hp/hpl/jena/rdf/arp/JenaReader.html#setProperty(java.lang.String,%20java.lang.Object)">
	 * other properties
	 * </a> modify the behaviour of the RDF/XML parser.
	 * </p>
	 * @param propName A property name.
	 * @param propValue The new value of the property.
	 * @return The old value of the property.
	 */
	public Object setProperty(String propName, Object propValue) {
	  return super.setProperty(propName, propValue);
	}

}


/*
    (c) Copyright 2006 Hewlett-Packard Development Company, LP
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/