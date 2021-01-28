package com.u8.sdk;

import com.tendcloud.tenddata.TalkingDataGA;

import android.content.Context;
import android.content.res.Configuration;
import android.util.Log;

public class X7Application implements IApplicationListener{

	@Override
	public void onProxyCreate() {
		// TODO Auto-generated method stub
		try {
			SDKParams params = U8SDK.getInstance().getSDKParams();
			String talkingAppid = params.getString("TALKING_APPID");
			String talkingChannel = params.getString("TALKING_CHANNEL");
			Log.e("U8SDK", "x7 app..talkingAppid.." + talkingAppid);
			Log.e("U8SDK", "x7 app..talkingChannel.." + talkingChannel);
			TalkingDataGA.init(U8SDK.getInstance().getApplication(), talkingAppid, talkingChannel);
		} catch (Exception e) {
			e.printStackTrace();
		}		
				
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
