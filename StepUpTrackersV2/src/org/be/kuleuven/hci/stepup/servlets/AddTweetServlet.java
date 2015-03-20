package org.be.kuleuven.hci.stepup.servlets;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
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
import org.be.kuleuven.hci.stepup.model.TwitterHash;
import org.be.kuleuven.hci.stepup.persistancelayer.EventGoogleDataStore;
import org.be.kuleuven.hci.stepup.persistancelayer.RestClient;

import org.be.kuleuven.hci.stepup.util.ReadGoogleSpreadSheetChikul13;
import org.be.kuleuven.hci.stepup.util.StepUpConstants;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import twitter4j.Paging;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.RateLimitStatus;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.UserMentionEntity;
import twitter4j.json.DataObjectFactory;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.memcache.ErrorHandlers;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

public class AddTweetServlet extends HttpServlet {

	private static final Logger log = Logger.getLogger(AddTweetServlet.class.getName());

	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		log.log(Level.INFO, "Cron job");
		
		String since_id = "119719744281640960";
		String lastId = EventGoogleDataStore.getLastTwitterId();
		if (lastId!=null) since_id=lastId;
		//since_id="309057833792581634";
		//log.warning("Ids"+lastId+"-"+since_id);
		System.out.println("Ids"+lastId+"-"+since_id);
		Twitter twitter = new TwitterFactory().getInstance();
		createTweetEntitiesfromHastTag("chikul14", since_id);
		
	}
	
	private void createTweetEntitiesfromHastTag(String hashtag, String since_id) {
		log.log(Level.INFO, "createTweetEntitiesfromHastTag");
		String username = "";
		MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
	    syncCache.setErrorHandler(ErrorHandlers.getConsistentLogAndContinue(Level.INFO));
	    if (syncCache.get("twitterusernames")==null){
	    	ReadGoogleSpreadSheetChikul13.read();
		}
	    
	    Hashtable<String,String> twitterusernames = (Hashtable<String,String>)syncCache.get("twitterusernames");
		

			//while(!finish){
		Twitter twitter = new TwitterFactory().getInstance();
        Query query = new Query("%23"+hashtag);
        query.setSinceId(Long.parseLong(since_id));
        try{
        	QueryResult result = twitter.search(query);
	        for (Status s : result.getTweets()) {
	        	if (!twitterusernames.containsKey(s.getUser().getScreenName().toLowerCase())) processStatus(s,hashtag);
	        }
        }catch (TwitterException e) {
			// TODO Auto-generated catch block
			log.severe(e.toString());
			//return null;
		} 
        System.out.println("=======================================================");
        Paging paging = new Paging(1); 
		paging.setSinceId(Long.parseLong(since_id));
		//paging.setMaxId(Long.parseLong("309605046696439808"));
        paging.setCount(200);
        Enumeration e = twitterusernames.keys();
		while( e.hasMoreElements()) {
			  String key = (String)e.nextElement();
			  try{
				  ResponseList<Status> status = twitter.getUserTimeline(key,paging);
				  for (Status s:status) processStatus(s, hashtag);
			  }catch (TwitterException er) {
					// TODO Auto-generated catch block
					log.severe("USERNAME WITH PROBLEMS:"+key+"-"+er.toString());
					//return null;
			  }
		}
		
	}

	public void processStatus(Status s, String hashtag){
		if (s.getText().toLowerCase().contains(hashtag)){
			try{
				Event event = new Event();
	        	event.setUsername(s.getUser().getScreenName().toLowerCase());
	        	if (s.getRetweetedStatus()!=null)
	        		event.setVerb("retweeted");
	        	else
	        		event.setVerb("tweeted");
	        	event.setStartTime(s.getCreatedAt());
	        	event.setObject(String.valueOf(s.getId()));
	        	event.setContext(hashtag);
	        	if (s.getInReplyToScreenName()!=null) event.setTarget(s.getInReplyToScreenName());
	        	event.setOriginalRequest(new JSONObject(DataObjectFactory.getRawJSON(s).replaceAll("\\P{Print}", "")));
	        	EventGoogleDataStore.insertEvent(event);

			}catch (JSONException e) {
				// TODO Auto-generated catch block
				log.severe(e.toString());
			} /*catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} */
		}
	}


	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doGet(req, resp);
	}

}
