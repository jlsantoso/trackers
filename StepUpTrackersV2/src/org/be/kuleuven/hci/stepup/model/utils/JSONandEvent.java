package org.be.kuleuven.hci.stepup.model.utils;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.be.kuleuven.hci.stepup.model.Event;
import org.json.JSONException;
import org.json.JSONObject;
import org.mortbay.log.Log;

public class JSONandEvent{
	
	public static Event transformFromJsonToEvent(JSONObject event) throws JSONException, ParseException{
		Event eventTransformed = new Event();
		eventTransformed.setUsername(event.getString("username"));
		eventTransformed.setVerb(event.getString("verb"));
		eventTransformed.setObject(event.getString("object"));
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss ZZZZZ");
		eventTransformed.setStartTime(formatter.parse(event.getString("starttime")));	
		if (event.has("endtime")) eventTransformed.setEndTime(formatter.parse(event.getString("endtime")));
		if (event.has("context")) eventTransformed.setContext(event.getString("context"));
		if (event.has("target")) eventTransformed.setTarget(event.getString("target"));
		if (event.has("location")) eventTransformed.setLocation(event.getString("location"));
		eventTransformed.setOriginalRequest(event);
		return eventTransformed;
	}
	
	public static JSONObject transformFromEvemtToJson(Event event) throws JSONException, ParseException{
		JSONObject eventTransformed = new JSONObject();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss ZZZZZ");
		eventTransformed.put("username", event.getUsername());
		eventTransformed.put("verb", event.getVerb());
		eventTransformed.put("object", event.getObject());
		eventTransformed.put("starttime", formatter.format(event.getStartTime()).toString());
		if (event.getEndTime()!=null) eventTransformed.put("endtime", formatter.format(event.getEndTime()).toString());;
		if (event.getContext()!=null){
			if (event.getContext().contains("{"))
				try{
					eventTransformed.put("context", new JSONObject(event.getContext()));
				}catch(JSONException e){
					eventTransformed.put("context", event.getContext());
				}
			else
				eventTransformed.put("context", event.getContext());
		}
		if (event.getTarget()!=null) eventTransformed.put("target", event.getTarget());
		if (event.getLocation()!=null) eventTransformed.put("location", event.getLocation());
		if (event.getOriginalRequestString()!=null)
			eventTransformed.put("originalrequest", new JSONObject(event.getOriginalRequestString()));
		else 
			eventTransformed.put("originalrequest", event.getOriginalRequest());
		return eventTransformed;
	}
}
