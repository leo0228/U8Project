package com.u8.sdk;

import com.lion.ccpay.sdk.CCPaySdk;
import com.tendcloud.tenddata.TalkingDataGA;

import android.content.Context;
import android.content.res.Configuration;

public class LionProxyApplication implements IApplicationListener{

	@Override
	public void onProxyCreate() {
		SDKParams params = U8SDK.getInstance().getSDKParams();
		String talkingAppid = params.getString("TALKING_APPID");
		String talkingChannel = params.getString("TALKING_CHANNEL");
		
		TalkingDataGA.init(U8SDK.getInstance().getApplication(), talkingAppid, talkingChannel);
		
		CCPaySdk.getInstance().onCreate(U8SDK.getInstance().getApplication());
	}

	@Override
	public void onProxyAttachBaseContext(Context base) {
		CCPaySdk.getInstance().attachBaseContext(U8SDK.getInstance().getApplication(), base);
	}

	
	
	@Override
	public void onProxyConfigurationChanged(Configuration config) {
		CCPaySdk.getInstance().onConfigurationChanged(U8SDK.getInstance().getApplication(), config);
	}

	@Override
	public void onProxyTerminate() {
		// TODO Auto-generated method stub
		CCPaySdk.getInstance().onTerminate();
	}

}
