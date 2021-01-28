package com.u8.sdk;

import android.content.Context;
import android.content.res.Configuration;
import android.util.Log;

import com.tendcloud.tenddata.TalkingDataGA;

public class MuzhiwanApplication implements IApplicationListener{

	@Override
	public void onProxyCreate() {
		// TODO Auto-generated method stub
		Log.e("muzhiwan", "MuMaYiApplication is start ");
		
		SDKParams params = U8SDK.getInstance().getSDKParams();
		String talkingAppid = params.getString("TALKING_APPID");
		String talkingChannel = params.getString("TALKING_CHANNEL");
		
		Log.e("muzhiwan", "talkingAppid : "+talkingAppid);
		Log.e("muzhiwan", "talkingChannel : "+talkingChannel);
		
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
