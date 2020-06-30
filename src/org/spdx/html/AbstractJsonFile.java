/**
 *
 */
package org.spdx.html;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.json.simple.JSONObject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;

/**
 * Supports writing files in JSON format
 * Implementation classes need to implement the getJsonObject method to provide the JSON object to be written
 * @author Gary O'Nell
 *
 */
public abstract class AbstractJsonFile {

	/**
	 * @return the JsonObject to be written
	 */
	abstract protected JSONObject getJsonObject();

	/**
	 * @param jsonFile File to write JSON data to
	 * @throws IOException
	 */
	public void writeToFile(File jsonFile) throws IOException {
		OutputStreamWriter writer = null;
		if (!jsonFile.exists()) {
			if (!jsonFile.createNewFile()) {
				throw(new IOException("Can not create new file "+jsonFile.getName()));
			}
		}
		try {
			// Use GSON to create a pretty-printed version of JSON
			// TODO: We could just move this all from simple-JSON to GSON using a class to store
			// the license data for GSON conversion
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			JsonParser jsonParser = new JsonParser();
			String licenseJsonPretty = gson.toJson(jsonParser.parse(getJsonObject().toJSONString()));
			writer = new OutputStreamWriter(new FileOutputStream(jsonFile), "UTF-8");
			writer.write(licenseJsonPretty);
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}

}
