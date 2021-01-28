package com.u8.sdk;

import android.content.Context;
import android.content.res.Configuration;
import android.util.Log;

import com.tendcloud.tenddata.TalkingDataGA;

public class UCApplication implements IApplicationListener{

	@Override
	public void onProxyCreate() {
		SDKParams params = U8SDK.getInstance().getSDKParams();
		String talkingAppId = params.getString("TALKING_APPID");
		String talkingChannel = params.getString("TALKING_CHANNEL");
		
		TalkingDataGA.init(U8SDK.getInstance().getApplication(), talkingAppId, talkingChannel);
	}

	@Override
	public void onProxyAttachBaseContext(Context base) {
		
	}

	@Override
	public void onProxyConfigurationChanged(Configuration config) {
		
	}

	@Override
	public void onProxyTerminate() {
		
	}
	
}
