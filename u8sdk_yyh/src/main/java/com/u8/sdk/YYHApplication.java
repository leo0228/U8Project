package com.u8.sdk;

import com.tendcloud.tenddata.TalkingDataGA;

import android.content.Context;
import android.content.res.Configuration;


public class YYHApplication implements IApplicationListener{

	@Override
	public void onProxyCreate() {
		
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
