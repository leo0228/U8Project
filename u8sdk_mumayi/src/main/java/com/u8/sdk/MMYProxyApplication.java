package com.u8.sdk;



import com.mumayi.paymentmain.ui.MMYApplication;
import com.tendcloud.tenddata.TalkingDataGA;

import android.content.Context;
import android.content.res.Configuration;
import android.util.Log;

public class MMYProxyApplication extends MMYApplication implements IApplicationListener{
	
	@Override
	public void onProxyCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		Log.e("mmysdk", "MuMaYiApplication is start ");
		SDKParams params = U8SDK.getInstance().getSDKParams();
		
		String talkingAppid = params.getString("TALKING_APPID");
		String talkingChannel = params.getString("TALKING_CHANNEL");
		
		Log.e("mmysdk", "talkingAppid : "+talkingAppid);
		Log.e("mmysdk", "talkingChannel : "+talkingChannel);
		
		TalkingDataGA.init(U8SDK.getInstance().getApplication(), talkingAppid, talkingChannel);
	
	}

	
	@Override
	public void onProxyAttachBaseContext(Context base) {
		// TODO Auto-generated method stub
		super.attachBaseContext(base);
	}

	@Override
	public void onProxyConfigurationChanged(Configuration config) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(config);
	}

	@Override
	public void onProxyTerminate() {
		// TODO Auto-generated method stub
		super.onTerminate();
	}
}
