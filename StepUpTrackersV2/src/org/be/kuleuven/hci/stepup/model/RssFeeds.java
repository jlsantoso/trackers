package org.be.kuleuven.hci.stepup.model;

import java.io.Serializable;

import com.googlecode.objectify.annotation.Id;

import com.googlecode.objectify.annotation.Entity;

@Entity
public class RssFeeds implements Serializable {

	@Id Long id;
	String url;
	
	public RssFeeds(){
		
	}
	
	public void setURL(String url){
		this.url=url;
	}
	
	public String getURL(){
		return this.url;
	}
	
}
