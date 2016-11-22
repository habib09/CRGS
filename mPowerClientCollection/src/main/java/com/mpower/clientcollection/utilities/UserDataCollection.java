package com.mpower.clientcollection.utilities;

import com.mpower.clientcollection.logic.FormDetails;

import java.util.HashMap;

public class UserDataCollection {
	private String username = "";
	private String password = "";
	private String role = "";
	private String fullName = "";
	private HashMap<String, FormDetails> formList = new HashMap<String, FormDetails>();
	
	public void resetAll() {
		username = "";
		password = "";
		role = "";
		fullName = "";
		formList = new HashMap<String, FormDetails>();
	}
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getRole() {return role;}

	public void setRole(String role) {this.role = role;	}

	public String getFullName() {return fullName;}

	public void setFullName(String fullName) {this.fullName = fullName;}
	public HashMap<String, FormDetails> getFormList() {
		return formList;
	}

	public void setFormList(HashMap<String, FormDetails> formList) {
		this.formList = formList;
	}

}
