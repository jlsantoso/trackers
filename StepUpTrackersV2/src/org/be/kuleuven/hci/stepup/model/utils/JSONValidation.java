package org.be.kuleuven.hci.stepup.model.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

public class JSONValidation {

	public static boolean checkJSONMandatoryAtributtes(JSONObject event){
		if (event.has("username")&&event.has("verb")&&event.has("starttime")&&event.has("object")){
			return true;
		}
		return false;
	}
	
	public static boolean checkJSONStarttimeAtributte(JSONObject event){
		
		if (event.has("starttime")){
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss ZZZZZ");
			try {
				Date dateStr = formatter.parse(event.getString("starttime"));
			} catch (ParseException e) {
				return false;
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				return false;
			}
			return true;
		}
		return false;
	}
	
	public static boolean checkJSONEndtimeAtributte(JSONObject event){
		
		if (event.has("endtime")){
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss ZZZZZ");
			try {
				Date dateStr = formatter.parse(event.getString("starttime"));
			} catch (ParseException e) {
				return false;
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				return false;
			}
			return true;
		}else if (!event.has("endtime")){
			return true;
		}
		return false;
	}	
}
