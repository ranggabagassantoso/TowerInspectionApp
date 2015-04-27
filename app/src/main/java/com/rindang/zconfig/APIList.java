package com.rindang.zconfig;

public class APIList {

	//Account
	public static String loginUrl(){
		return AppConfig.getInstance().getV1()+"/login";
	}
	
	public static String gcmTokenRegeisterUrl(){
		return AppConfig.getInstance().getV1()+"/device/register";
	}
	
	public static String updateTokenUrl(){
		return AppConfig.getInstance().getV1()+"/update_token";
	}
	
	public static String logoutUrl(){
		return AppConfig.getInstance().getV1()+"/logout";
	}

	//User
	public static String userUrl(){
		return AppConfig.getInstance().getV1()+"/users";
	}

	//Schedules
	public static String schedulesUrl(){
		return AppConfig.getInstance().getV1()+"/schedules";
	}

	//Forms
	public static String formsUrl(){
		return AppConfig.getInstance().getV1()+"/work_forms";
	}
	
	public static String formVersionUrl(){
		return AppConfig.getInstance().getV1()+"/form_version";
	}
	
	public static String uploadUrl(){
		return AppConfig.getInstance().getV1()+"/schedules/upload_item";
	}
	
	//APK
	public static String apkUrl(){
		return AppConfig.getInstance().getV1()+"/apk";
	}

}
