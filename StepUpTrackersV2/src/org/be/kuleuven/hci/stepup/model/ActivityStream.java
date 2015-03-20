package org.be.kuleuven.hci.stepup.model;



import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

public class ActivityStream {
	
	JSONObject activityStream;

	public ActivityStream(){
		activityStream = new JSONObject();
	}
	
	public void setPublishedDate(Date publishedDate){
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		try {
			this.activityStream.put("published",formatter.format(publishedDate));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	public void setActor(String username){
		JSONObject actor = new JSONObject();
		try {
			actor.put("objectType", "person");
			actor.put("id", username);
			actor.put("displayName", username);
			this.activityStream.put("actor", actor);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
	}
	
	public void setActor(String username, String urlImage){
		JSONObject actor = new JSONObject();
		try {
			actor.put("objectType", "person");
			actor.put("id", username);
			JSONObject image = new JSONObject();
			image.put("url", urlImage);
			image.put("width", 72);
			image.put("height", 72);
			actor.put("image",image);
			this.activityStream.put("actor", actor);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void setVerb(String verb){
		try {
			this.activityStream.put("verb", verb);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void setObject(String objectURL, String displayName){
		JSONObject object = new JSONObject();
		try {
			object.put("id", "generated");
			object.put("url", objectURL);
			object.put("displayName", displayName);
			this.activityStream.put("object",object);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	public void setTarget(String objectURL, String objectType, String displayName){
		JSONObject target = new JSONObject();
		try {
			target.put("url", objectURL);
			target.put("objectType", objectType);
			target.put("displayName", displayName);
			target.put("id", "generated");
			this.activityStream.put("target",target);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public JSONObject getActivityStream(){
		return this.activityStream;
	}
/*
    "target" : { (optativo)
      "url": "http://example.org/blog/",
      "objectType": "blog",
      "id": "tag:example.org,2011:abc123",
      "displayName": "Martin's Blog"
    }
  }*/
}
