package com.u8.sdk;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;

import com.qihoo.gamecenter.sdk.matrix.Matrix;
import com.tendcloud.tenddata.TalkingDataGA;

public class QihooProxyApplication extends Application implements IApplicationListener{

	@Override
	public void onProxyCreate() {
		SDKParams params = U8SDK.getInstance().getSDKParams();
		String talkingAppid = params.getString("TALKING_APPID");
		String talkingChannel = params.getString("TALKING_CHANNEL");
		
		TalkingDataGA.init(U8SDK.getInstance().getApplication(), talkingAppid, talkingChannel);
		Matrix.initInApplication(U8SDK.getInstance().getApplication());
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
