package com.u8.sdk;

import android.content.Context;
import android.content.res.Configuration;
import android.util.Log;

import com.papa91.pay.pa.business.PPayCenter;
import com.tendcloud.tenddata.TalkingDataGA;
import com.u8.sdk.IApplicationListener;
import com.u8.sdk.U8SDK;

public class WFApplication implements IApplicationListener{
	
	@Override
	public void onProxyCreate() {	
		SDKParams params = U8SDK.getInstance().getSDKParams();
		
		String talkingAppid = params.getString("TALKING_APPID");
		String talkingChannel = params.getString("TALKING_CHANNEL");
		Log.e("wufansdk", "talkingAppid : "+talkingAppid);
		Log.e("wufansdk", "talkingChannel : "+talkingChannel);
		TalkingDataGA.init(U8SDK.getInstance().getApplication(), talkingAppid, talkingChannel);
		PPayCenter.initConfig(U8SDK.getInstance().getApplication());

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
