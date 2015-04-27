package com.sap.inspection.constant;

import java.util.HashMap;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.os.AsyncTask;

public class GlobalVar {

	private static GlobalVar instance;
	
	public HashMap<String, AsyncTask<Void, Void, String>> request;

	public static GlobalVar getInstance() {
		if (instance == null) {
			instance = new GlobalVar();
		}
		return instance;
	}
	
	public GlobalVar() {
		request = new HashMap<String, AsyncTask<Void,Void,String>>();
	}
	
	public boolean anyNetwork(Context context){
		return anyWifi(context) || anyMobileNet(context);
	}

	public boolean anyWifi(Context context){
		ConnectivityManager conMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

		//wifi
		State wifi = conMan.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
		return (wifi == NetworkInfo.State.CONNECTED || wifi == NetworkInfo.State.CONNECTING);
	}

	public boolean anyMobileNet(Context context){
		ConnectivityManager conMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

		//mobile
		State mobile = conMan.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
		return (mobile == NetworkInfo.State.CONNECTED || mobile == NetworkInfo.State.CONNECTING);
	}
}
