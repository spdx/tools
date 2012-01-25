/*
  (c) Copyright 2007 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: HomeDir.java 1121 2007-04-11 15:02:58Z jeremy_carroll $
*/
package com.hp.hpl.jena.grddl.license;

import java.io.File;

/**
 * HomeDir
 * @author Jeremy J. Carroll
 */
public class HomeDir {
	
	static String fs;
	/**
	 * The Jena config dir.
	 * @return The Jena config dir, or null on error.
	 */
	static public File jenaDir() {
		fs = System.getProperty("file.separator");
		String homeDir = System.getProperty("user.home");
		String os = System.getProperty("os.name");
//		System.err.println(homeDir + " "+ os);
		String dir = homeDir + fs + dirName(os);
//		System.err.println(dir);
		File result = new File(dir);
		try {
		if (result.isDirectory() || result.mkdir())
		   return result;
		}
		catch (SecurityException e) {
		}
		return null;
	}

	private static String dirName(String os) {
		if (os.toLowerCase().startsWith("windows")) {
			return windowsJenaDir();
		}
		if (os.toLowerCase().startsWith("mac")) {
			return macJenaDir();
		}
		return unixJenaDir();
	}
	
	private static String unixJenaDir() {
		return ".jena";
	}

	private static String macJenaDir() {
		return "Library" + fs + "Jena";
	}

	private static String windowsJenaDir() {
		return "Local Settings" + fs +"Application Data" + fs + "Jena";
	}

	static public void main(String a[]) {
		jenaDir();
	}
	
	

}


/*
    (c) Copyright 2007 Hewlett-Packard Development Company, LP
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