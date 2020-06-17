/**
 * 
 */
package org.spdx.rdfparser.license;

import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.validator.UrlValidator;

/**
 * @author Smith
 *
 */
public class UrlHelper {
	
	public static boolean urlLinkExists(String URLName){
	    try {
	      HttpURLConnection.setFollowRedirects(false);
	      HttpURLConnection con = (HttpURLConnection) new URL(URLName).openConnection();
	      con.setRequestMethod("HEAD");
	      return (con.getResponseCode() == HttpURLConnection.HTTP_OK);
	    }
	    catch (Exception e) {
	       e.printStackTrace();
	       return false;
	    }
	  }
	
	public static boolean urlValidator(String url){
		// Get an UrlValidator using default schemes
		UrlValidator defaultValidator = new UrlValidator();
		return defaultValidator.isValid(url);
	}

}
