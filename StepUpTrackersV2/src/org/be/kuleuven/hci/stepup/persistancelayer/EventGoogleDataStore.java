package org.be.kuleuven.hci.stepup.persistancelayer;
import java.io.IOException;
import java.math.BigInteger;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.be.kuleuven.hci.stepup.model.Event;
import org.be.kuleuven.hci.stepup.model.RssFeeds;
import org.be.kuleuven.hci.stepup.model.TwitterHash;
import org.be.kuleuven.hci.stepup.model.utils.JSONandEvent;
import org.be.kuleuven.hci.stepup.util.StepUpConstants;
import org.json.JSONException;

import com.google.appengine.api.datastore.DatastoreNeedIndexException;
import com.google.appengine.api.memcache.ErrorHandlers;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;




public class EventGoogleDataStore {
	
	private static final Logger log = Logger.getLogger(EventGoogleDataStore.class.getName());
	
	public static void insertEvent(Event event){
		//System.out.println("[VERB]"+event.getVerb());
	    if (event.getVerb().compareTo(StepUpConstants.TWITTER)==0||event.getVerb().compareTo(StepUpConstants.RETWEET)==0) updateLastTwitterId(event);
	    if (event.getVerb().compareTo(StepUpConstants.BLOGCOMMENT)==0||event.getVerb().compareTo(StepUpConstants.BLOGPOST)==0) updateLastUpdateRss(event.getStartTime());
	    if (event.getVerb().compareTo(StepUpConstants.DIIGOVERB)==0) updateLastUpdateDiigo(event.getStartTime());
	    event.setTimeStamp(Calendar.getInstance().getTime());
	    String result="";
	    String eventToString = "";
	    String exception = "";
		try {
			eventToString = JSONandEvent.transformFromEvemtToJson(event).toString();
			System.out.println("Event:"+eventToString);
			result = RestClient.doPostAuth("{url data store} ",eventToString);
			System.out.println("Result:"+result);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			log.severe(e.toString());
			exception += e.toString();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			log.severe(e.toString());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			log.severe(e.toString());
		}
		if (result.contains("Success")) event.setInserted(true);
		else{
			event.setInserted(false);
			log.severe("Problem pushing event:"+result+eventToString);
			//Mail.sendmail("Problem pushing event", "<br/>"+result+"<br/>"+eventToString);
		}
		OfyService.getOfyService().ofy().save().entity(event); 
	}
	
	public static void insertTwitterHash(TwitterHash twitterHash){
		OfyService.getOfyService().ofy().save().entity(twitterHash); 
	}
	
	public static void insertRssFeeds(RssFeeds rssFeeds){
		OfyService.getOfyService().ofy().save().entity(rssFeeds); 
	}
	
	public static void insertHashTag(TwitterHash twitterHash){
		OfyService.getOfyService().ofy().save().entity(twitterHash); 
	}
	
	public static void clearCache(){
		OfyService.getOfyService().ofy().clear();
	}
	
	public static List<TwitterHash> getTwitterHashTags(){
		MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
	    syncCache.setErrorHandler(ErrorHandlers.getConsistentLogAndContinue(Level.INFO));
	    if (syncCache.get("twitterhashtags")==null){
	    	List<TwitterHash> twitterhashtags = OfyService.getOfyService().ofy().load().type(TwitterHash.class).list();
	    	syncCache.put("twitterhashtags", ( new ArrayList<TwitterHash>(twitterhashtags)));
	    	return twitterhashtags;
	    }else{
	    	return ((List<TwitterHash>)syncCache.get("twitterhashtags"));
	    }
	}
	
	public static List<RssFeeds> getDiigoFeeds(){
		MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
	    syncCache.setErrorHandler(ErrorHandlers.getConsistentLogAndContinue(Level.INFO));
	    if (syncCache.get("rssfeeds")==null){
	    	List<RssFeeds> rssFeeds = OfyService.getOfyService().ofy().load().type(RssFeeds.class).list();
	    	syncCache.put("rssfeeds", new ArrayList<RssFeeds>(rssFeeds));
	    	return rssFeeds;
	    }else{
	    	return ((List<RssFeeds>)syncCache.get("rssfeeds"));
	    }
	}
	
	public static List<RssFeeds> getRssFeeds(){
		MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
	    syncCache.setErrorHandler(ErrorHandlers.getConsistentLogAndContinue(Level.INFO));
	    if (syncCache.get("rssfeeds")==null){
	    	List<RssFeeds> rssFeeds = OfyService.getOfyService().ofy().load().type(RssFeeds.class).list();
	    	syncCache.put("rssfeeds", new ArrayList<RssFeeds>(rssFeeds));
	    	return rssFeeds;
	    }else{
	    	return ((List<RssFeeds>)syncCache.get("rssfeeds"));
	    }
	}
	
	public static String getLastTwitterId(){
		try{
			MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
		    syncCache.setErrorHandler(ErrorHandlers.getConsistentLogAndContinue(Level.INFO));
		    if (syncCache.get("lastTwitterId")==null){
		    	ArrayList<String> verbs = new ArrayList<String>();
		    	verbs.add(StepUpConstants.TWITTER);
		    	verbs.add(StepUpConstants.RETWEET);
		    	
		    	Event event = OfyService.getOfyService().ofy().load().type(Event.class).order("-object").filter("verb in", verbs).first().get();
		    	if (event==null){
		    		syncCache.put("lastTwitterId","119719744281640960");
		    		return "119719744281640960";
		    	}
		    	syncCache.put("lastTwitterId", event.getObject());
		    	return event.getObject();
		    }else{
		    	return syncCache.get("lastTwitterId").toString();
		    }
		}catch (DatastoreNeedIndexException e){
			MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
		    syncCache.setErrorHandler(ErrorHandlers.getConsistentLogAndContinue(Level.INFO));
			syncCache.put("lastTwitterId","119719744281640960");
    		return "119719744281640960";
		}
	}
	
	public static Date getLastUpdateRss(){
		MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
	    syncCache.setErrorHandler(ErrorHandlers.getConsistentLogAndContinue(Level.INFO));
	    log.warning("Access from EvetnGoogleDataStore");
	    try{
		    if (syncCache.get("lastUpdateRss")==null){
		    	log.warning("No access to the cache");
		    	System.out.println("No access to the cache");
		    	ArrayList<String> verbs = new ArrayList<String>();
		    	verbs.add(StepUpConstants.BLOGCOMMENT);
		    	verbs.add(StepUpConstants.BLOGPOST);
		    	Event event = OfyService.getOfyService().ofy().load().type(Event.class).order("-starttime").filter("verb in", verbs).first().get();
		    	if (event==null){
		    		log.warning("No RSS feed in the database");
		    		System.out.println("No RSS feed in the database");
		    		Calendar lastUpdate = Calendar.getInstance();
		    		lastUpdate.add(Calendar.DAY_OF_MONTH, -90);
		    		syncCache.put("lastUpdateRss", lastUpdate.getTime());
		    		return lastUpdate.getTime();
		    	}
		    	log.warning("Last Event:"+event.getObject());
		    	syncCache.put("lastUpdateRss", event.getStartTime());
		    	return event.getStartTime();
		    }else{
		    	log.warning("Cache access");
		    	return ((Date)syncCache.get("lastUpdateRss"));
		    }
	    }catch(Exception e){
	    	log.warning(e.toString());
	    	System.out.println(e.toString());
	    	Calendar lastUpdate = Calendar.getInstance();
    		lastUpdate.add(Calendar.DAY_OF_MONTH, -90);
    		syncCache.put("lastUpdateRss", lastUpdate.getTime());
    		return lastUpdate.getTime();
	    }
	}
	
	public static void updatedFalseValues (String context){
		Iterator<Event> events = OfyService.getOfyService().ofy().load().type(Event.class).filter("context", context).filter("inserted", false).iterator();
		int count=0;
		while (events.hasNext()){
			Event e = events.next();
			log.warning("Non-in the database:"+e.getOriginalRequestString());
			insertEvent(e);
			count++;
			if(count==2) break;
		}
		log.warning("Finished");
	}
	
	public static void updatedFalseValues (){
		Iterator<Event> events = OfyService.getOfyService().ofy().load().type(Event.class).filter("inserted", false).iterator();
		int count=0;
		while (events.hasNext()){
			Event e = events.next();
			log.warning("Non-in the database:"+e.getOriginalRequestString());
			OfyService.getOfyService().ofy().delete().entities(e);
			insertEvent(e);
			count++;
		}
		log.warning("Finished");
	}
	
	public static void removeValues (){
		Calendar remover = Calendar.getInstance();
		remover.set(Calendar.DAY_OF_MONTH, 18);
		remover.set(Calendar.MONTH, 0);
		remover.set(Calendar.YEAR, 2015);
		Iterator<Event> events = OfyService.getOfyService().ofy().load().type(Event.class).filter("starttime <", remover.getTime()).iterator();
		//log.warning(remover.getTime().toString());
		while (events.hasNext()){
			Event e = events.next();
			//log.warning(e.getObject());
			OfyService.getOfyService().ofy().delete().entities(e);
		}
		log.warning("Finished");
	}
	
	public static void updatedObjectValues(String object){
		Iterator<Event> events = OfyService.getOfyService().ofy().load().type(Event.class).filter("object", object).iterator();
		int count=0;
		while (events.hasNext()){
			Event e = events.next();
			log.warning("Non-in the database:"+e.getOriginalRequestString());
			e.setContext("prueba15");
			insertEvent(e);
			count++;
			if(count==2) break;
		}
		log.warning("Finished");
	}
	
	public static Date getLastUpdateARLearn(){
		MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
	    syncCache.setErrorHandler(ErrorHandlers.getConsistentLogAndContinue(Level.INFO));
	    try{
		    if (syncCache.get("lastUpdateARLearn")==null){
		    	log.info("No access to the cache");
		    	System.out.println("No access to the cache");
		    	ArrayList<String> verbs = new ArrayList<String>();
		    	verbs.add("answer_given");
		    	verbs.add("read");
		    	verbs.add("startRun");
		    	verbs.add("response");
		    	Event event = OfyService.getOfyService().ofy().load().type(Event.class).order("-starttime").filter("verb in", verbs).first().get();
		    	if (event==null){
		    		log.info("No ARLearn actions in the database");
		    		//System.out.println("No RSS feed in the database");
		    		Calendar lastUpdate = Calendar.getInstance();
		    		lastUpdate.add(Calendar.DAY_OF_MONTH, -90);
		    		syncCache.put("lastUpdateARLearn", lastUpdate.getTime());
		    		return lastUpdate.getTime();
		    	}
		    	Date lastUpdateARLearn = new Date(event.getStartTime().getTime()+1000);
		    	syncCache.put("lastUpdateARLearn", lastUpdateARLearn);
		    	return lastUpdateARLearn;
		    }else{
		    	return new Date(((Date)syncCache.get("lastUpdateARLearn")).getTime()+10);
		    }
	    }catch(Exception e){
	    	log.warning(e.toString());
	    	System.out.println(e.toString());
	    	Calendar lastUpdate = Calendar.getInstance();
    		lastUpdate.add(Calendar.DAY_OF_MONTH, -90);
    		syncCache.put("lastUpdateARLearn", lastUpdate.getTime());
    		return lastUpdate.getTime();
	    }
	}

	public static Date getLastUpdateDiigo(){
		MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
	    syncCache.setErrorHandler(ErrorHandlers.getConsistentLogAndContinue(Level.INFO));
	    try{
		    if (syncCache.get("lastUpdateDiigo")==null){
		    	log.info("No access to the cache");
		    	ArrayList<String> verbs = new ArrayList<String>();
		    	verbs.add(StepUpConstants.DIIGOVERB);
		    	Event event = OfyService.getOfyService().ofy().load().type(Event.class).order("-starttime").filter("verb in", verbs).first().get();
		    	if (event==null){
		    		log.info("No RSS feed in the database");
		    		Calendar lastUpdate = Calendar.getInstance();
		    		lastUpdate.add(Calendar.DAY_OF_MONTH, -90);
		    		syncCache.put("lastUpdateDiigo", lastUpdate.getTime());
		    		return lastUpdate.getTime();
		    	}
		    	syncCache.put("lastUpdateDiigo", event.getStartTime());
		    	return event.getStartTime();
		    }else{
		    	return ((Date)syncCache.get("lastUpdateDiigo"));
		    }
	    }catch(Exception e){
	    	Calendar lastUpdate = Calendar.getInstance();
    		lastUpdate.add(Calendar.DAY_OF_MONTH, -90);
    		syncCache.put("lastUpdateDiigo", lastUpdate.getTime());
    		return lastUpdate.getTime();
	    }
	}
	
	public static void updateLastUpdateDiigo(Date lastUpdateDiigo){
		MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
	    syncCache.setErrorHandler(ErrorHandlers.getConsistentLogAndContinue(Level.INFO));
	    if (syncCache.get("lastUpdateDiigo")!=null){
	    	if (((Date)syncCache.get("lastUpdateDiigo")).compareTo(lastUpdateDiigo)<0){
	    		syncCache.put("lastUpdateDiigo",lastUpdateDiigo);
	    	}
	    }
	}
	
	public static void updateLastUpdateARLearn(Date lastUpdateARLearn){
		MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
	    syncCache.setErrorHandler(ErrorHandlers.getConsistentLogAndContinue(Level.INFO));
	    if (syncCache.get("lastUpdateARLearn")!=null){
	    	if (((Date)syncCache.get("lastUpdateARLearn")).compareTo(lastUpdateARLearn)<0){
	    		syncCache.put("lastUpdateARLearn",lastUpdateARLearn);
	    	}
	    }
	}
	
	public static void updateLastUpdateRss(Date lastUpdateRss){
		MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
	    syncCache.setErrorHandler(ErrorHandlers.getConsistentLogAndContinue(Level.INFO));
	    if (syncCache.get("lastUpdateRss")!=null){
	    	System.out.println("[UPDATE]"+((Date)syncCache.get("lastUpdateRss")).toString()+"="+((Date)syncCache.get("lastUpdateRss")).toString());
	    	if (((Date)syncCache.get("lastUpdateRss")).compareTo(lastUpdateRss)<0){
	    		System.out.println("Updating..."+lastUpdateRss.toString());
	    		syncCache.put("lastUpdateRss",lastUpdateRss);
	    	}
	    }
	}
	
	public static void updateLastTwitterId(Event event){
		MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
	    syncCache.setErrorHandler(ErrorHandlers.getConsistentLogAndContinue(Level.INFO));
	    if (syncCache.get("lastTwitterId")!=null){
	    	if (new BigInteger(syncCache.get("lastTwitterId").toString()).compareTo(new BigInteger(event.getObject()))==-1){
	    		syncCache.put("lastTwitterId",event.getObject());
	    	}
	    }
	}
	
	public static void sendEventToTiNYARM(){
		
	}
}
