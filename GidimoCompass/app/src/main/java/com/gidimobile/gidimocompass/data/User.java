package com.gidimobile.gidimocompass.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class User implements Serializable{
	private String uid;
	private String userName;
	private List<UserLocation> locationHistory = new ArrayList<>();
	private Map<Long, String> shoppingCart = new HashMap<>();
	private List<Object> userConnectivities;

    public User(){}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}


	public Map<Long, String> getShoppingCart() {
		return shoppingCart;
	}

	public void setShoppingCart(Map<Long, String> shoppingCart) {
		this.shoppingCart = shoppingCart;
	}

	public List<UserLocation> getLocationHistory() {
		return locationHistory;
	}

	public void setLocationHistory(List<UserLocation> locationHistory) {
		this.locationHistory = locationHistory;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

}