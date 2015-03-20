package org.be.kuleuven.hci.stepup.servlets;

import java.io.IOException;
import java.net.URL;
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
import org.be.kuleuven.hci.stepup.persistancelayer.EventGoogleDataStore;
import org.be.kuleuven.hci.stepup.persistancelayer.RestClient;
import org.be.kuleuven.hci.stepup.util.ReadGoogleSpreadSheetChikul13;
import org.be.kuleuven.hci.stepup.util.ReadGoogleSpreadSheetThesis14;
import org.be.kuleuven.hci.stepup.util.ReadingBlogs;
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

public class AddRSSServlet extends HttpServlet {

	private static final Logger log = Logger.getLogger(AddRSSServlet.class.getName());

	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
	    syncCache.setErrorHandler(ErrorHandlers.getConsistentLogAndContinue(Level.INFO));
		log.log(Level.INFO, "Cron job");
		log.warning("Last RSS feed AddRSSServlet");
		Date lastUpdate = EventGoogleDataStore.getLastUpdateRss();
		if (syncCache.get("urlsBlogsthesis14")==null){
			ReadGoogleSpreadSheetThesis14.read();
		}

		ArrayList<String> urlsBlogs = (ArrayList<String>) syncCache.get("urlsBlogsthesis14");
		String context = "thesis14";
		log.warning("Context:"+context+"last update of blogs:"+lastUpdate.toString());
		for ( String key:urlsBlogs) {
			  log.warning("URL feed: "+key);
			  if (key.contains("wordpress")){
				  ReadingBlogs.getRssFeedsWordpress(key, lastUpdate, context);
				  ReadingBlogs.getRssFeedsCommentsWordpress(key, lastUpdate, context);
			  }				  
			  else{
				  if (key.contains("github")){
					  ReadingBlogs.getRssFeeds("http://neler.github.io/Thesis/atom.xml", lastUpdate, context,StepUpConstants.BLOGPOST);
				  	  ReadingBlogs.getRssFeeds("https://neler.disqus.com/comments.rss", lastUpdate, context, StepUpConstants.BLOGCOMMENT);
				  }
				  else{
					  ReadingBlogs.getRssFeeds("http://"+key+"/feed/", lastUpdate, context, StepUpConstants.BLOGPOST);
					  ReadingBlogs.getRssFeeds("http://"+key+"/comments/feed/", lastUpdate, context, StepUpConstants.BLOGCOMMENT);
				  }
			  }
			  
		}
	}



	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doGet(req, resp);
	}

}
