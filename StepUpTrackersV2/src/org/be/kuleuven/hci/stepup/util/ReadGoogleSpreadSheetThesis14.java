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

public class ReadGoogleSpreadSheetThesis14 {

	public static void read() {
		MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
	    syncCache.setErrorHandler(ErrorHandlers.getConsistentLogAndContinue(Level.INFO));
		// TODO Auto-generated method stub
		SpreadsheetService service = new SpreadsheetService("com.banshee");
		service.setConnectTimeout(120000);
	    try {

	      String urlString = "https://spreadsheets.google.com/feeds/list/1YOLMrtRbcFGCZFfYF80Ih0nadoXrTxYQcSshqS7GmB0/default/public/values";

	      // turn the string into a URL
	      URL url = new URL(urlString);

	      // You could substitute a cell feed here in place of
	      // the list feed
	      ListFeed feed = service.getFeed(url, ListFeed.class);
	      ArrayList<String> urlsBlogs = new ArrayList<String>();
	      int i = 0;
	      System.out.println("length of entries:"+ feed.getEntries().size());
	      for (ListEntry entry : feed.getEntries()) {
		        CustomElementCollection elements = entry.getCustomElements();  
		        String wordpressblogurl = elements.getValue("wpurl").toLowerCase();
		        if (!checingIfAURLExists(urlsBlogs,wordpressblogurl.replaceAll("http:", "").replaceAll("/", ""))) urlsBlogs.add(wordpressblogurl.replaceAll("http:", "").replaceAll("/", ""));
	      }
	      syncCache.put("urlsBlogsthesis14", urlsBlogs);
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
	
}
