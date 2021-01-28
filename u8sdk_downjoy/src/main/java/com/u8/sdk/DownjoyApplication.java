package com.u8.sdk;


import com.tendcloud.tenddata.TalkingDataGA;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.util.Log;

/***
 * Baidu SDK 需要自定义的Application,同时需要配置到SDK_Manifest.xml中
 *
 */
public class DownjoyApplication extends Application implements IApplicationListener{
//U8Application
	@Override
	public void onProxyCreate() {
		super.onCreate();
		com.downjoy.DownjoyApplication.onAppCreate();
		SDKParams params = U8SDK.getInstance().getSDKParams();
		String talkingAppid = params.getString("TALKING_APPID");
		String talkingChannel = params.getString("TALKING_CHANNEL");
		Log.e("downjoySDK","talkingAppid ==" + talkingAppid);
		Log.e("downjoySDK","talkingChannel ==" + talkingChannel);
		
		TalkingDataGA.init(U8SDK.getInstance().getApplication(), talkingAppid, talkingChannel);
	}

	@Override
	public void onProxyAttachBaseContext(Context base) {
		super.attachBaseContext(base);
		com.downjoy.DownjoyApplication.onAttachBaseContext(U8SDK.getInstance().getApplication(), base);
	}

	@Override
	public void onProxyConfigurationChanged(Configuration config) {
		super.onConfigurationChanged(config);
	}

	@Override
	public void onProxyTerminate() {
		// TODO Auto-generated method stub
		super.onTerminate();
	}

}
