package com.u8.sdk;

import com.tendcloud.tenddata.TalkingDataGA;

import android.content.Context;
import android.content.res.Configuration;
import android.util.Log;

public class FlymeApplication implements IApplicationListener{

	@Override
	public void onProxyCreate() {
		Log.e("U8SDK", "proxy init......");
		SDKParams params = U8SDK.getInstance().getSDKParams();
		String talkingAppid = params.getString("TALKING_APPID");
		String talkingChannel = params.getString("TALKING_CHANNEL");
		
		String pushAppid = params.getString("MEIZU_APP_ID");
		String pushAppkey = params.getString("MEIZU_APP_KEY");
		
		Log.e("U8SDK", "meizu init......talkingAppid" + talkingAppid);
		Log.e("U8SDK", "meizu init......talkingChannel" + talkingChannel);
		Log.e("U8SDK", "meizu init......pushAppid" + pushAppid);
		Log.e("U8SDK", "meizu init......pushAppkey" + pushAppkey);
		
		TalkingDataGA.init(U8SDK.getInstance().getApplication(), talkingAppid, talkingChannel);
	}

	@Override
	public void onProxyAttachBaseContext(Context base) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProxyConfigurationChanged(Configuration config) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProxyTerminate() {
		// TODO Auto-generated method stub
		
	}

}
