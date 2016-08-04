package com.rindang.zconfig;

import android.view.View;


public class WawanConfig extends BaseConfig{

	public static final String SENDER_ID = "934425217631";
	
	@Override
	public String getHost() {
		return "http://103.17.54.187";
	}

	@Override
	public void setHost(String host) {
	}
	
	@Override
	public String getGCMSenderId() {
		return SENDER_ID;
	}

	@Override
	public int getURLChangingVisibility() {
		return View.GONE;
	}

	@Override
	public boolean isProduction() {
		return true;
	}

}
