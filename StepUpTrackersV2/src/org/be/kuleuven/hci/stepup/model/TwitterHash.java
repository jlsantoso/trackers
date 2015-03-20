package org.be.kuleuven.hci.stepup.model;

import java.io.Serializable;

import com.googlecode.objectify.annotation.Id;

import com.googlecode.objectify.annotation.Entity;

@Entity
public class TwitterHash implements Serializable {

	@Id Long id;
	String hash;
	
	public TwitterHash(){
		
	}
	
	public void setHash(String hash){
		this.hash=hash;
	}
	
	public String getHash(){
		return this.hash;
	}
	
}
