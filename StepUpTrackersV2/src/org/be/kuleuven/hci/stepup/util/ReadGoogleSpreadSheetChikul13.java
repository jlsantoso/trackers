package org.be.kuleuven.hci.stepup.util;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.logging.Level;

import com.google.appengine.api.memcache.ErrorHandlers;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.spreadsheet.CustomElementCollection;
import com.google.gdata.data.spreadsheet.ListEntry;
import com.google.gdata.data.spreadsheet.ListFeed;
import com.google.gdata.util.ServiceException;

public class ReadGoogleSpreadSheetChikul13 {

	public static void read() {
		MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
	    syncCache.setErrorHandler(ErrorHandlers.getConsistentLogAndContinue(Level.INFO));
		SpreadsheetService service = new SpreadsheetService("com.banshee");
		service.setConnectTimeout(120000);
	    try {

	      String urlString = "https://spreadsheets.google.com/feeds/list/0AhROFoj5kwtPdFQzeldMYlRETXFMYUV2TkQ5OTVXMVE/default/public/values";

	      // turn the string into a URL
	      URL url = new URL(urlString);

	      ListFeed feed = service.getFeed(url, ListFeed.class);
	      Hashtable<String,String> matchingusernames = new Hashtable<String,String>();
	      Hashtable<String,String> blogsfeeds = new Hashtable<String,String>();

	      Hashtable<String,String> membersGroup = new Hashtable<String,String>();
	      Hashtable<String,String> twitterusernames = new Hashtable<String,String>();
	      ArrayList<String> urlsBlogs = new ArrayList<String>();
	      int i = 0;
	      System.out.println("length of entries:"+ feed.getEntries().size());
	      for (ListEntry entry : feed.getEntries()) {
	    	//if (checkingCells(entry)){	    	
		        CustomElementCollection elements = entry.getCustomElements();
		        
		        String twitter = elements.getValue("twitteraccount");
		        if (twitter!=null) twitter = twitter.toLowerCase();
		        //String name = elements.getValue("studentname").toLowerCase();
		        String groupname = elements.getValue("groupname").toLowerCase();
		        if (membersGroup.containsKey(groupname)){
		        	String members = membersGroup.get(groupname);
		        	members += twitter+";";
		        	membersGroup.put(groupname,members);
		        }
		        String wordpressblogurl = elements.getValue("wordpressurl").toLowerCase();
		        if (!checingIfAURLExists(urlsBlogs,wordpressblogurl.replaceAll("http:", "").replaceAll("/", ""))) urlsBlogs.add(wordpressblogurl.replaceAll("http:", "").replaceAll("/", ""));
		        String wordpresspostrssfeed = elements.getValue("wordpressrssfeed");
		        if (wordpresspostrssfeed!=null) wordpresspostrssfeed = wordpresspostrssfeed.toLowerCase();
		        if (wordpresspostrssfeed!=null)
		        	blogsfeeds.put(wordpresspostrssfeed, groupname);
		        String wordpresscommentrssfeed = elements.getValue("wordpresscommentrssfeed");
		        if (wordpresscommentrssfeed!=null) wordpresscommentrssfeed = wordpresscommentrssfeed.toLowerCase();
		        if (wordpresscommentrssfeed!=null)
		        	blogsfeeds.put(wordpresscommentrssfeed, groupname);
		        
		        //Matching usernames
		        if (twitter!=null)
		        	twitterusernames.put(twitter, twitter);
	    	//}
	      }
	      syncCache.put("blogsfeed", blogsfeeds);
	      syncCache.put("twitterusernames", twitterusernames);
	      syncCache.put("urlsBlogschi14", urlsBlogs);
	    } catch (IOException e) {
	      e.printStackTrace();
	    } catch (ServiceException e) {
	      e.printStackTrace();
	    }
	}
	
	static boolean checingIfAURLExists(ArrayList<String> urlsBlogs, String url){
		for (String iURL : urlsBlogs){
			if (iURL.compareTo(url)==0) return true;
		}
		return false;		
	}
	
	static boolean checkingCells(ListEntry entry){
		CustomElementCollection elements = entry.getCustomElements();
        String twitter = elements.getValue("twitter");
        String name = elements.getValue("studentname");
        String groupname = elements.getValue("groupname");
        String wordpressblogurl = elements.getValue("wordpressblogurl");
        String wordpresspostrssfeed = elements.getValue("wordpresspostrssfeed");
        String wordpresscommentrssfeed = elements.getValue("wordpresscommentrssfeed");
        String diigo = elements.getValue("diigo");	
        String openbadge = elements.getValue("openbadge");
        String emailforstepupnotifications = elements.getValue("emailforstepupnotifications");   
        if (twitter!=null&&name!=null&&groupname!=null&&wordpressblogurl!=null&&wordpresspostrssfeed!=null&&wordpresscommentrssfeed!=null&&diigo!=null&&openbadge!=null&&emailforstepupnotifications!=null)
        	return true;
        else 
        	return false;

	}

}
