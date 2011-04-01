/*
 	(c) Copyright 2007 Hewlett-Packard Development Company, LP
 	All rights reserved.
*/

package com.hp.hpl.jena.grddl.test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import com.hp.hpl.jena.grddl.impl.JBufferedReader;

public class BufferedReadTest extends TestCase
    {
	public class Sums extends Enc {
        int pos;
        int markLimit = -1;
        int mark = 0;
		void fill(char[] b) throws IOException {
			for (int i=0;i<b.length;i++)
				b[i] = (char)(i+1000+pos);
			pos += b.length;
		}

		@Override
		void mark(int n) {
			mark = pos;
			markLimit = pos + n;
		}

		@Override
		boolean reset() {
			if (pos > markLimit)
			   return false;
			pos = mark;
			return true;
		}

		@Override
		void skip(int l) throws IOException {
			pos += l;
		}

	}
	private static abstract class Enc {
		abstract void mark(int n) throws IOException;
		abstract void fill(char b[]) throws IOException;
		abstract boolean reset();
		abstract void skip(int l) throws IOException;
		List log = new ArrayList();
		@SuppressWarnings("unchecked")
		void read(int n) throws IOException {
			char b[] = new char[n];
			fill(b);
//			log.add("read("+n+")");
			log.add(b);
		}
		@SuppressWarnings("unchecked")
		void back() {
//			log.add("reset()");
			log.add(new Boolean(reset()));
		}
	}
	private class Rdr extends Enc {
		char data[] = new char[16000];
	    Reader r;
	    int rc = 0;
	    Rdr() {
	    	for (int i=0;i<data.length;i++) {
	    		data[i] = (char)(i+1000);
	    	}
	    	r = new JBufferedReader(
	    			new StringReader(new String(data)),
	    			1);
	    }
		void fill(char[] b) throws IOException {
	    	int off = 0;
	    	int len = b.length;
	    	while (len > 0) {
	    		int nc = r.read(b,off,len);
	    		if (nc<0)
	    			fail("Premature EOF");
	    		off += nc;
	    		len -= nc;
	    		rc ++;
	    	}
			
		}
		void mark(int n) throws IOException {
			r.mark(n);
		}
		boolean reset() {
			try {
				r.reset();
				return true;
			} catch (IOException e) {
				return false;
			}
		}
		void skip(int l) throws IOException {
			r.skip(l);
		}
	}
    public BufferedReadTest( String name )
        { super( name ); }
    
    void p1(Enc a) throws IOException {
    	a.mark(50);
    	a.read(30);
    	a.mark(10);
    	a.read(5);
    	a.back();
    	a.read(8);
    	a.back();
    	a.read(12);
    	a.back();
    	a.read(30);
    	a.read(55);
    	a.mark(2000);
    	a.read(305);
    	a.read(200);
    	a.back();
    	a.skip(40);
    	a.read(4000);
    }
    

    void resetAfterMark(Enc a) throws IOException {
    	a.mark(5000);
    	a.reset();
    	a.read(100);
    	a.reset();
    	a.read(273);
    	a.mark(30);
    	a.reset();
    	a.read(90);
    	a.reset();
    }
    void skipping(Enc a) throws IOException {
    	a.mark(50);
    	a.skip(40);
    	a.back();  //0
    	a.read(30); //1
    	a.skip(19);
    	a.back(); //2
    	a.mark(10);
    	a.read(5); //3
    	a.back();
    	a.read(8);
    	a.back();
    	a.read(12);
    	a.back();
    	a.mark(25);
    	a.skip(50);
    	a.read(30);
    	a.read(55);
    	a.mark(2000);
    	a.read(305);
        a.skip(400);
    	a.back();
    	a.skip(300);
    	a.read(4000);
    	a.skip(55);
    	a.read(300);
    }
    
    public void test1() throws IOException {
    	Rdr rdr = new Rdr();
    	Sums sums = new Sums();
		p1(rdr);
		p1(sums);
    	mustBeEqual(rdr.log,sums.log);
//    	System.err.println(rdr.rc);
    }
    public void testSkipping() throws IOException {
    	Rdr rdr = new Rdr();
    	Sums sums = new Sums();
		skipping(rdr);
		skipping(sums);
    	mustBeEqual(rdr.log,sums.log);
//    	System.err.println(rdr.rc);
    }
    public void testReset() throws IOException {
    	Rdr rdr = new Rdr();
    	Sums sums = new Sums();
		resetAfterMark(rdr);
		resetAfterMark(sums);
    	mustBeEqual(rdr.log,sums.log);
//    	System.err.println(rdr.rc);
    }
	private void mustBeEqual(List got, List wanted) {
		assertEquals(got.size(),wanted.size());
		Iterator gi = got.iterator();
		Iterator wi = wanted.iterator();
		int step = 0;
		while (gi.hasNext()) {
			Object go = gi.next();
			Object wo = wi.next();
			if (go instanceof char[]) {
				char gc[] = (char[])go;
				char wc[] = (char[])wo;
				assertEquals(gc.length,wc.length);
				for (int i=0; i<gc.length;i++) {
					assertEquals("step: "+step,gc[i],wc[i]);
				}
			} else
				assertEquals("step: "+step,go,wo);
			step ++;
		}
	}
    
    
    
    

    
    }

/*
 * (c) Copyright  2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/