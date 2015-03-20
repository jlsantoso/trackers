package org.be.kuleuven.hci.stepup.util;

import java.io.IOException;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.be.kuleuven.hci.stepup.model.Event;
import org.be.kuleuven.hci.stepup.persistancelayer.EventGoogleDataStore;
import org.be.kuleuven.hci.stepup.persistancelayer.RestClient;
import org.be.kuleuven.hci.stepup.servlets.AddRSSServlet;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.appengine.api.memcache.ErrorHandlers;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

public class ReadingBlogs {
	
	private static final Logger log = Logger.getLogger(ReadingBlogs.class.getName());

	
	public static void getRssFeeds(String urlString, Date lastUpdate, String context, String verb){
		
		System.out.println(lastUpdate.toString());
		
		MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
	    syncCache.setErrorHandler(ErrorHandlers.getConsistentLogAndContinue(Level.INFO));

		SyndFeedInput sfi=new SyndFeedInput();
		URL url;
		try {
			url = new URL(urlString);
			SyndFeed feed = null;
			feed= sfi.build(new XmlReader(url));
			List<SyndEntry>	entries = feed.getEntries();
			java.util.Date created = null;
			Calendar cal = Calendar.getInstance();
			String author_general = feed.getAuthor();

			for (SyndEntry entry:entries){
				//System.out.println(entry.getUpdatedDate().toString()+"="+lastUpdate.toString());
				
				if ((entry.getPublishedDate()!=null&&entry.getPublishedDate().compareTo(lastUpdate)>0)||(entry.getUpdatedDate()!=null&&entry.getUpdatedDate().compareTo(lastUpdate)>0)){
					String username = "";
					if (entry.getAuthor().length()>0){						
						username = entry.getAuthor().toLowerCase();
						System.out.println("Hemos entrado:"+entry.getAuthor());
					}else username="neler";
					
					//if (username.length()==0) username = author_general;
					System.out.println("Name:"+username);
					Event event = new Event();
					event.setUsername(username);
					Date starttime = null;
					if (entry.getPublishedDate()!=null){
						starttime=entry.getPublishedDate();
					}
					if (entry.getUpdatedDate()!=null){
						starttime=entry.getUpdatedDate();
					}
					event.setStartTime(starttime);
					event.setObject(entry.getLink());
					event.setVerb(verb);
					event.setContext(context);
					JSONObject originalrequest = new JSONObject();
					//System.out.println(entry.getContents().isEmpty());
					//System.out.println(((SyndContent)entry.getContents().get(0)).getValue());
					//System.out.println(entry.getDescription());
					if (entry.getDescription()!=null) originalrequest.put("description",entry.getDescription().getValue().replaceAll("\\P{Print}", ""));
					else if (!entry.getContents().isEmpty()) originalrequest.put("description",((SyndContent)entry.getContents().get(0)).getValue().replaceAll("\\P{Print}", ""));
					originalrequest.put("title",entry.getTitle().replaceAll("\\P{Print}", ""));
					event.setOriginalRequest(originalrequest);
					EventGoogleDataStore.insertEvent(event);
				}
			}
		} catch (IllegalArgumentException e) {
			log.severe(e.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			log.severe(e.toString());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			log.severe(e.toString());
		} catch (FeedException e) {
			// TODO Auto-generated catch block
			log.severe(e.toString());
		}		
	}
	
	public static void getRssFeedsWordpress(String urlString, Date lastUpdate, String context){
		
		//System.out.println(lastUpdate.toString());
		urlString = "https://public-api.wordpress.com/rest/v1/sites/"+urlString+"/posts/";
		log.warning(urlString);
		try {
			String wordpressPosts = RestClient.doGet(urlString);
			JSONArray posts = new JSONObject(wordpressPosts).getJSONArray("posts");
			DateTimeFormatter format = ISODateTimeFormat.dateTimeNoMillis();

			java.util.Date created = null;
			Calendar cal = Calendar.getInstance();
			log.warning("Length" + posts.length());
			for (int i=0; i<posts.length(); i++){
				JSONObject post = posts.getJSONObject(i);				
				/*long from = Long.parseLong("1415885161000");
				Date fromd = new Date(from);
				long to = Long.parseLong("1416521545000");
				Date tod = new Date(to);
				System.out.println(format.parseDateTime(post.getString("date")).toDate().toString()+"-"+fromd.toString()+"-"+tod.toString());
				if (format.parseDateTime(post.getString("date")).toDate().compareTo(fromd)>0&&format.parseDateTime(post.getString("date")).toDate().compareTo(tod)<0){*/
				if (format.parseDateTime(post.getString("date")).toDate().compareTo(lastUpdate)>0){
					String username = post.getJSONObject("author").getString("name").toLowerCase();

					Event event = new Event();
					event.setUsername(username);
					event.setStartTime(format.parseDateTime(post.getString("date")).toDate());
					event.setObject(post.getString("URL"));
					if (urlString.contains("comment")) event.setVerb(StepUpConstants.BLOGCOMMENT);
					else event.setVerb(StepUpConstants.BLOGPOST);
					event.setContext(context);
					JSONObject originalrequest = new JSONObject();
					originalrequest.put("description",post.getString("content").replaceAll("\\P{Print}", ""));
					originalrequest.put("title",post.getString("title").replaceAll("\\P{Print}", ""));
					event.setOriginalRequest(originalrequest);
					System.out.println(event.getObject());
					EventGoogleDataStore.insertEvent(event);
				}
			}
		} catch (IllegalArgumentException e) {
			log.severe(e.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			log.severe(e.toString());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			log.severe(e.toString());
		} 	
	}
	
	public static void getRssFeedsCommentsWordpress(String urlString, Date lastUpdate, String context){
		
		//System.out.println(lastUpdate.toString());
		urlString = "https://public-api.wordpress.com/rest/v1/sites/"+urlString+"/comments/?number=100";
		try {
			String wordpressPosts = RestClient.doGet(urlString);
			JSONArray posts = new JSONObject(wordpressPosts).getJSONArray("comments");
			DateTimeFormatter format = ISODateTimeFormat.dateTimeNoMillis();
			java.util.Date created = null;
			Calendar cal = Calendar.getInstance();
			System.out.println("Length"+posts.length());
			for (int i=0; i<posts.length(); i++){
				JSONObject post = posts.getJSONObject(i);
				//System.out.println(format.parseDateTime(post.getString("date")).toDate().toString());
				if (post.getString("type").compareTo("pingback")!=0&&format.parseDateTime(post.getString("date")).toDate().compareTo(lastUpdate)>0){
					String username = post.getJSONObject("author").getString("name").toLowerCase();
					Event event = new Event();
					event.setUsername(username);
					event.setStartTime(format.parseDateTime(post.getString("date")).toDate());
					event.setObject(post.getString("URL"));
					if (urlString.contains("comment")) event.setVerb(StepUpConstants.BLOGCOMMENT);
					else event.setVerb(StepUpConstants.BLOGPOST);
					event.setContext(context);
					event.setOriginalRequest(new JSONObject().put("description",post.getString("content").replaceAll("\\P{Print}", "")));
					EventGoogleDataStore.insertEvent(event);
				}
			}
		} catch (IllegalArgumentException e) {
			log.severe(e.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			log.severe(e.toString());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			log.severe(e.toString());
		} 	
	}

}
