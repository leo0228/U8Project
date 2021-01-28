package com.u8.sdk;

import com.tendcloud.tenddata.TalkingDataGA;

import android.content.Context;
import android.content.res.Configuration;
import android.util.Log;

/***
 * Baidu SDK 需要自定义的Application,同时需要配置到SDK_Manifest.xml中
 *
 */
public class CoolPadApplication implements IApplicationListener{
//U8Application
	String tag = "coolpad";
	@Override
	public void onProxyCreate() {

		Log.e(tag, "CoolPadApplication is start");
		SDKParams params = U8SDK.getInstance().getSDKParams();
		String talkingAppid = params.getString("TALKING_APPID");
		String talkingChannel = params.getString("TALKING_CHANNEL");
		
		TalkingDataGA.init(U8SDK.getInstance().getApplication(), talkingAppid, talkingChannel);	
	}

	@Override
	public void onProxyAttachBaseContext(Context base) {

	}

	@Override
	public void onProxyConfigurationChanged(Configuration config) {

	}

	@Override
	public void onProxyTerminate() {
		// TODO Auto-generated method stub
		
	}

}
