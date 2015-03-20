package org.be.kuleuven.hci.stepup.servlets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.be.kuleuven.hci.stepup.model.ActivityStream;
import org.be.kuleuven.hci.stepup.model.Event;
import org.be.kuleuven.hci.stepup.model.RssFeeds;
import org.be.kuleuven.hci.stepup.model.utils.JSONandEvent;
import org.be.kuleuven.hci.stepup.persistancelayer.EventGoogleDataStore;
import org.be.kuleuven.hci.stepup.persistancelayer.RestClient;
import org.be.kuleuven.hci.stepup.util.StepUpConstants;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import com.google.appengine.api.memcache.ErrorHandlers;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

public class AddARLearn extends HttpServlet {

	private static final Logger log = Logger.getLogger(AddARLearn.class.getName());

	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		
		long date = 1374142261536L;
		String elggRunsIds = "http://streetlearn.appspot.com/rest/response/runId/";
		String actions = "http://streetlearn.appspot.com/rest/actions/runId/";
		String response = "http://streetlearn.appspot.com/rest/response/runId/";
		Date lastUpdate = EventGoogleDataStore.getLastUpdateARLearn(); 
		ArrayList<Integer> runsARLearn = new ArrayList<Integer>();
		Hashtable<String,String> runIdsInquiryId = new Hashtable<String,String>();
		try {
			runIdsInquiryId = getARLearnRuns(elggRunsIds);
			Enumeration e = runIdsInquiryId.keys();
			while (e.hasMoreElements()){
				String runId = (String)e.nextElement();
				if (runIdsInquiryId.containsKey(runId)){
					getUserDataFromARLearnActions(actions, lastUpdate, Long.parseLong(runId), runIdsInquiryId.get(runId));
					getUserDataFromARLearnResponses(response, lastUpdate, Long.parseLong(runId), runIdsInquiryId.get(runId));
				}
			}
		} catch (JSONException e) {
			log.severe("ERROR doGet: "+e.toString());
		}		
	}
	
	private Hashtable<String,String> getARLearnRuns(String urlElgg) throws UnsupportedEncodingException, JSONException{
		Hashtable<String,String> runIdsInquiryId = new Hashtable<String,String>();
		String final_object = RestClient.doGet("http://inquiry.wespot.net//services/api/rest/json/?method=site.inquiries&api_key={insert_key}&minutes=4448");
		if (final_object!=null&&final_object.length()>0){
			JSONObject json = new JSONObject(final_object);
			JSONArray inquiries = json.getJSONArray("result");
			ArrayList<Integer> listRuns = new ArrayList<Integer>();
			for (int i=0; i<inquiries.length();i++){
				String final_object2 = RestClient.doGet("http://inquiry.wespot.net/services/api/rest/json/?method=inquiry.arlearnrun&api_key={insert_key}&inquiryId="+inquiries.getJSONObject(i).getInt("inquiryId"));
				if (final_object2!=null&&final_object2.length()>0){
					JSONObject jsonRun = new JSONObject(final_object2);
					if (jsonRun.getInt("status")==0){
						runIdsInquiryId.put(jsonRun.getString("result"), inquiries.getJSONObject(i).getString("inquiryId"));
						listRuns.add(jsonRun.getInt("result"));
					}
				}
			}
		}
		return runIdsInquiryId;
	}

	private void getUserDataFromARLearnActions(String urlString, Date lastUpdate, long runId, String inquiry){
		URL url;
		try {
			String final_object = RestClient.doGetAuth(urlString+runId+"?from="+(lastUpdate.getTime()+10));
			if (final_object!=null&&final_object.length()>0){
				JSONObject json = new JSONObject(final_object);
				JSONArray jsonActions = json.getJSONArray("actions");
				for (int i=0;i<jsonActions.length();i++){
					JSONObject action = jsonActions.getJSONObject(i);
					String username = modifyUsername(action.getString("userEmail"));
					
					Event event = new Event();
					event.setUsername(username);
					event.setStartTime(new Date(action.getLong("timestamp")));
					EventGoogleDataStore.updateLastUpdateARLearn(event.getStartTime());
					event.setContext("{\"course\":\""+inquiry+"\",\"phase\":\""+StepUpConstants.PHASE3+"\",\"subphase\":\""+StepUpConstants.PHASE3_S2+"\"}");
					event.setObject(action.getString("generalItemId"));
					event.setVerb(action.getString("action"));
					event.setOriginalRequest(action);
					EventGoogleDataStore.insertEvent(event);
				}
			}		
		} catch (IOException e) {
			log.severe("ERROR getUserDataFromARLearnActions (IOException): "+e.toString());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			log.severe("ERROR getUserDataFromARLearnActions (JSONException): "+e.toString());
		} 
	}
	
	private String modifyUsername(String username){
		username = username.replace("1:", "facebook_");
		username = username.replace("2:", "google_");
		username = username.replace("3:", "linkedin_");
		username = username.replace("4:", "twitter_");
		username = username.replace("5:", "wespot_");
		return username;
	}
		
	private void getUserDataFromARLearnResponses(String urlString, Date lastUpdate, long runId, String inquiry){
		
		URL url;
		try {
			String final_object = RestClient.doGetAuth(urlString+runId+"?from="+(lastUpdate.getTime()+10));
			if (final_object!=null&&final_object.length()>0){
				JSONObject json = new JSONObject(final_object);
				JSONArray jsonActions = json.getJSONArray("responses");
				for (int i=0;i<jsonActions.length();i++){
					JSONObject action = jsonActions.getJSONObject(i);
					JSONObject responseValue = new JSONObject(action.getString("responseValue"));
					action.remove("responseValue");
					action.put("responseValue", responseValue);
					String username = modifyUsername(action.getString("userEmail"));
					Event event = new Event();
					event.setUsername(username);
					event.setStartTime(new Date(action.getLong("lastModificationDate")));
					EventGoogleDataStore.updateLastUpdateARLearn(event.getStartTime());
					event.setObject(action.getString("generalItemId"));
					event.setVerb("response");
					event.setContext("{\"course\":\""+inquiry+"\",\"phase\":\""+StepUpConstants.PHASE3+"\",\"subphase\":\""+StepUpConstants.PHASE3_S2+"\"}");
					event.setOriginalRequest(action);
					EventGoogleDataStore.insertEvent(event);
				}
			}		
		} catch (IOException e) {
			log.severe("ERROR getUserDataFromARLearnResponses (IOException): "+e.toString());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			log.severe("ERROR getUserDataFromARLearnResponses (JSONException): "+e.toString());
		} 
	}
	
	
	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doGet(req, resp);
	}

}
