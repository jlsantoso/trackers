package org.be.kuleuven.hci.stepup.persistancelayer;

import java.util.Hashtable;

import org.be.kuleuven.hci.stepup.model.Event;
import org.be.kuleuven.hci.stepup.model.RssFeeds;
import org.be.kuleuven.hci.stepup.model.TwitterHash;

import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.ObjectifyService;

public class OfyService {
	
	private static OfyService _ofyService;
    
	OfyService (){
        factory().register(Event.class);
        factory().register(RssFeeds.class);
        factory().register(TwitterHash.class);
    }
	
	public static synchronized OfyService getOfyService() {
		if (_ofyService == null) {
			_ofyService = new OfyService();
			
		}
		return _ofyService;
	}

    public static Objectify ofy() {
        return ObjectifyService.ofy();
    }

    public static ObjectifyFactory factory() {
        return ObjectifyService.factory();
    }
    
}