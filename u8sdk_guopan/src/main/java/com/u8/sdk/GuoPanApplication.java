package com.u8.sdk;

import android.content.Context;
import android.content.res.Configuration;
import android.util.Log;

import com.tendcloud.tenddata.TalkingDataGA;
import com.u8.sdk.IApplicationListener;
import com.u8.sdk.U8SDK;

public class GuoPanApplication implements IApplicationListener{
	@Override
	public void onProxyCreate() {
		SDKParams params = U8SDK.getInstance().getSDKParams();
		
		String talkingAppid = params.getString("TALKING_APPID");
		String talkingChannel = params.getString("TALKING_CHANNEL");
		Log.e("guopansdk", "talkingAppid : "+talkingAppid);
		Log.e("guopansdk", "talkingChannel : "+talkingChannel);
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
		
	}

}
