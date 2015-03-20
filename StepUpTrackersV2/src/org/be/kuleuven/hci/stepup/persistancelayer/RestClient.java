package org.be.kuleuven.hci.stepup.persistancelayer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.util.logging.Logger;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.be.kuleuven.hci.stepup.servlets.AddTweetServlet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RestClient {
	public static final int HTTP_OK = 200;
	public static final int TIMEOUT = 20000;

	final static HttpClient httpClient = new DefaultHttpClient();
	private static final Logger log = Logger.getLogger(AddTweetServlet.class.getName());
	
	public static String doGet(final String urlString) throws UnsupportedEncodingException{
		BufferedReader rd  = null;
	    StringBuilder sb = null;
	    String line = null;
	    try {
	        URL url = new URL(urlString);
	        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	        connection.setRequestProperty("Content-Type", "application/json");
	        connection.setDoOutput(true);
	        connection.setRequestMethod("GET");
     
	        rd  = new BufferedReader(new InputStreamReader(connection.getInputStream()));
	        sb = new StringBuilder();
	        
	        while ((line = rd.readLine()) != null)
	        {
	            sb.append(line + '\n');
	        }

	        return sb.toString();
	    } catch (MalformedURLException e) {
	        System.out.println(e.toString());
	        log.severe(e.toString());
	    } catch (IOException e) {
	    	System.out.println(e.toString());
	    	log.severe(e.toString());
	    }
		return "";
	}
	
	public static String doGetAuth(final String urlString) throws UnsupportedEncodingException{
		URL url;
		try {
			url = new URL (urlString);//actions/runId/199236
			URLConnection conn = url.openConnection();
			conn.setRequestProperty("Authorization","{ARLearn Auth}");
			BufferedReader r = new BufferedReader(
					new InputStreamReader(
					    conn.getInputStream()
					)
				    );
			String line = r.readLine();
			String final_object = line;
			while (line != null) {
			    System.out.println(line);
			    line = r.readLine();
			    final_object+=line;
			}
			return final_object;		
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return null;
	}
	
	public static String doPostAuth(final String urlString, final String POSTText) throws UnsupportedEncodingException{
		String message = URLEncoder.encode(POSTText, "UTF-8");
		BufferedReader rd  = null;
	    StringBuilder sb = null;
	    String line = null;
	    try {
	        URL url = new URL(urlString);
	        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	        connection.setRequestProperty("Content-Type", "application/json");
	        connection.setRequestProperty("Authorization","{Auth data store}");
	        connection.setDoOutput(true);
	        connection.setRequestMethod("POST");
	        OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
	        writer.write(POSTText);
	        writer.close();	        
	        return read(connection.getInputStream());
	    } catch (MalformedURLException e) {
	        log.severe(e.toString());
	    } catch (IOException e) {
	    	log.severe(e.toString());
	    }
		return "";
	}

	public static String doPost(final String urlString, final String POSTText) throws UnsupportedEncodingException{
		String message = URLEncoder.encode(POSTText, "UTF-8");
		BufferedReader rd  = null;
	    StringBuilder sb = null;
	    String line = null;
	    try {
	        URL url = new URL(urlString);
	        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	        connection.setRequestProperty("Content-Type", "application/json");
	        connection.setDoOutput(true);
	        connection.setRequestMethod("POST");

	        OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
	        writer.write(POSTText);
	        writer.close();
	        
	        rd  = new BufferedReader(new InputStreamReader(connection.getInputStream()));
	        sb = new StringBuilder();
	        
	        while ((line = rd.readLine()) != null)
	        {
	            sb.append(line + '\n');
	        }

	        return sb.toString();
	    } catch (MalformedURLException e) {
	        System.out.println(e.toString()+POSTText);
	        log.severe(e.toString()+"-"+POSTText);
	    } catch (IOException e) {
	    	System.out.println(e.toString()+POSTText);
	    	log.severe(e.toString()+"-"+POSTText);
	    }
		return "";
	}

	public static boolean doPut(final String url, final String PUTText)
			throws URISyntaxException, HttpException, IOException {
		
		HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), TIMEOUT);

		HttpPut httpPut = new HttpPut(url);
		httpPut.addHeader("Accept", "application/json");
		httpPut.addHeader("Content-Type", "application/json");
		StringEntity entity = new StringEntity(PUTText, "UTF-8");
		entity.setContentType("application/json");
		httpPut.setEntity(entity);
		HttpResponse response = httpClient.execute(httpPut);
		System.out.println(response.getStatusLine());
		int statusCode = response.getStatusLine().getStatusCode();
		return statusCode == HTTP_OK ? true : false;
	}

	public static boolean doDelete(final String url) throws HttpException,
			IOException, URISyntaxException {
		HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), TIMEOUT);

		HttpDelete httpDelete = new HttpDelete(url);
		httpDelete.addHeader("Accept","text/html, image/jpeg, *; q=.2, */*; q=.2");
		HttpResponse response = httpClient.execute(httpDelete);
		int statusCode = response.getStatusLine().getStatusCode();
		return statusCode == HTTP_OK ? true : false;
	}

	private static String read(InputStream in) throws IOException {
		StringBuilder sb = new StringBuilder();
		BufferedReader r = new BufferedReader(new InputStreamReader(in), TIMEOUT);
		for (String line = r.readLine(); line != null; line = r.readLine()) {
			sb.append(line);
		}
		in.close();
		return sb.toString();
	}
}
