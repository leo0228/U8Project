package com.u8.sdk;

import com.baidu.gamesdk.BDGameSDK;
import com.baidu.sapi2.base.debug.Log;
import com.tendcloud.tenddata.TalkingDataGA;

import android.content.Context;
import android.content.res.Configuration;

/***
 * Baidu SDK 需要自定义的Application,同时需要配置到SDK_Manifest.xml中
 *
 */
public class BaiduApplication implements IApplicationListener{
//U8Application
	@Override
	public void onProxyCreate() {

		BDGameSDK.initApplication(U8SDK.getInstance().getApplication());
		
		SDKParams params = U8SDK.getInstance().getSDKParams();
		String talkingAppid = params.getString("TALKING_APPID");
		String talkingChannel = params.getString("TALKING_CHANNEL");
		
		Log.e("bdsdk", "TALKING_APPID" + params.getString("TALKING_APPID"));
		
		TalkingDataGA.init(U8SDK.getInstance().getApplication(), talkingAppid, talkingChannel);
		
//		try{
//			Class<?> clazz = Class.forName("com.baidu.gamesdk.BDGameSDK");
//			Method m = clazz.getMethod("initApplication", Application.class);
//			m.invoke(null, U8SDK.getInstance().getApplication());
//		}catch(Exception e){
//			e.printStackTrace();
//		}
		
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
