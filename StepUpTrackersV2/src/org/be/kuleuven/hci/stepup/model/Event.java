package org.be.kuleuven.hci.stepup.model;

import java.util.Date;

import org.json.JSONObject;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Ignore;
import com.googlecode.objectify.annotation.Index;

import javax.xml.bind.annotation.XmlRootElement; 

@Entity

public class Event {
	
	@Id Long id;
	@Index String username;
	@Index String verb;
	@Index Date starttime;
	@Index Date endtime;
	@Index String object;
	@Index String target;
	@Index String location;
	@Index String context;
	@Index Date timestamp;
	@Index boolean inserted;
	String originalrequestString;
	@Ignore JSONObject originalrequest;
	
	public Event(){
		this.username=null;
		this.verb=null;
		this.starttime=null;
		this.endtime=null;
		this.object=null;
		this.target=null;
		this.location=null;
		this.context=null;
		this.originalrequest=null;
		this.inserted=false;
		this.timestamp=null;
	}
	
	public void setUsername(String username){
		this.username=username;
	}
	
	public void setVerb(String verb){
		this.verb=verb;
	}
	
	public void setStartTime(Date starttime){
		this.starttime=starttime;
	}
	
	public void setEndTime(Date endtime){
		this.endtime=endtime;
	}
	
	public void setObject(String object){
		this.object=object;
	}
	
	public void setTarget(String target){
		this.target=target;
	}
	
	public void setLocation(String location){
		this.location=location;
	}
	
	public void setContext(String context){
		this.context=context;
	}
	
	public void setTimeStamp(Date timestamp){
		this.timestamp=timestamp;
	}
	
	public void setInserted(boolean inserted){
		this.inserted=inserted;
	}
	
	public void setOriginalRequest(JSONObject originalrequest){
		this.originalrequest=originalrequest;
		this.originalrequestString=originalrequest.toString();
	}
	
	public String getUsername(){
		return this.username;
	}
	
	public String getVerb(){
		return this.verb;
	}
	
	public Date getStartTime(){
		return this.starttime;
	}
	
	public Date getEndTime(){
		return this.endtime;
	}
	
	public String getObject(){
		return this.object;
	}
	
	public String getTarget(){
		return this.target;
	}
	
	public String getLocation(){
		return this.location;
	}
	
	public String getContext(){
		return this.context;
	}
	
	public JSONObject getOriginalRequest(){
		return this.originalrequest;
	}
	
	public String getOriginalRequestString(){
		return this.originalrequestString;
	}
}
