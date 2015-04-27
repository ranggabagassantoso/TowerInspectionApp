package com.sap.inspection;


import android.content.SharedPreferences;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Window;

import com.flurry.android.FlurryAgent;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
//import com.sap.inspection.gcm.GCMService;
import com.sap.inspection.manager.ScreenManager;

public abstract class BaseActivity extends FragmentActivity{
	protected FragmentActivity activity;
	public static final int ACTIVITY_REQUEST_CODE = 311; 
	protected SharedPreferences mPref;

	public static ImageLoader imageLoader = ImageLoader.getInstance();
	public static DisplayImageOptions  avatarOptions = new DisplayImageOptions.Builder()
	.showStubImage(R.drawable.logo_sap)
	.cacheInMemory()
	.cacheOnDisc()
//	.resetViewBeforeLoading()
//	.delayBeforeLoading(300)
//	.displayer(new RoundedBitmapDisplayer(30))
	.build();
	
	public static DisplayImageOptions  itemOptions = new DisplayImageOptions.Builder()
//	.showStubImage(R.drawable.ic_launcher)
	.cacheInMemory()
	.cacheOnDisc()
//	.resetViewBeforeLoading()
//	.delayBeforeLoading(300)
//	.displayer(new RoundedBitmapDisplayer(30))
	.build();

	private boolean instanceStateSaved;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        FlurryAgent.logEvent(this.getClass().getName());
		activity = this;
//        int x = 0;
//        int y = 1/x;
		mPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		requestWindowFeature(Window.FEATURE_NO_TITLE);
//		 getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		// WindowManager.LayoutParams.FLAG_FULLSCREEN);
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		ScreenManager.getInstance().setHeight(metrics.heightPixels);
		ScreenManager.getInstance().setWidth(metrics.widthPixels); 
	}
	
	@Override
	protected void onResume() {
		super.onResume();
//		GCMService.baseActivity = this;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		instanceStateSaved = true;
	}

	@Override
	protected void onDestroy() {
		if (!instanceStateSaved) {
//			imageLoader.stop();
		}
		super.onDestroy();
	}
	
	public void log(String logString){
		Log.d(getClass().getName(), logString);
	}
	
	public void log(int logInt){
		Log.d(getClass().getName(), String.valueOf(logInt));
	}
	
	public void writePreference(int key, String value) {
		mPref.edit().putString(getString(key), value).commit();
	}
	
	public void writePreference(String key, String value) {
		mPref.edit().putString(key, value).commit();
	}
	
	public void writePreference(int key, int value) {
		mPref.edit().putInt(getString(key), value).commit();
	}
	
	public void writePreference(int key, boolean value) {
		mPref.edit().putBoolean(getString(key), value).commit();
	}
	
	public String getPreference(int key, String defaultValue) {
		return mPref.getString(getString(key), defaultValue);
	}
	
	public String getPreference(String key, String defaultValue) {
		return mPref.getString(key, defaultValue);
	}
	
	public int getPreference(int key, int defaultValue) {
		return mPref.getInt(getString(key), defaultValue);
	}
	
	public boolean getPreference(int key, boolean defaultValue) {
		return mPref.getBoolean(getString(key), defaultValue);
	}

    @Override
    protected void onStart() {
        super.onStart();
        FlurryAgent.onStartSession(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        FlurryAgent.onEndSession(this);
    }
}
