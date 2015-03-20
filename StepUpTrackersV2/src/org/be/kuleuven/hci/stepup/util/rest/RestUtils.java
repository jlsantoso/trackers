package org.be.kuleuven.hci.stepup.util.rest;



import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;


public class RestUtils {

	protected static RestUtils instance = null;

	private static final Logger log = Logger.getLogger(RestUtils.class.getName());

	protected RestUtils() {

	}

	public static RestUtils getInstance() {
		if(instance == null) instance = new RestUtils();
		return instance;
	}


	public String getUrlAsString(URL url, Charset charSet) {
		String result = "";
		try {
			BufferedReader reader = null;
			if(charSet == null) {
				reader = new BufferedReader(new InputStreamReader(url.openStream()));
			}else {
				reader = new BufferedReader(new InputStreamReader(url.openStream(),charSet));
			}
			String line;

			while ((line = reader.readLine()) != null) {
				result += line + "\n";
			}
			reader.close();

		} catch (MalformedURLException e) {
			log.log(Level.SEVERE, "Url is malformed",e);
		} catch (IOException e) {
			log.log(Level.SEVERE, "IOException",e);
		}
		return result;
	}
}
