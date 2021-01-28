package com.u8.sdk;

import com.tendcloud.tenddata.TalkingDataGA;

import android.content.Context;
import android.content.res.Configuration;
import android.util.Log;

public class HuLianProxyApplication implements IApplicationListener{
	@Override
	public void onProxyCreate() {
		
		try {
			Log.d("hulian", "onProxyCreate");
			//SDK初始化, 请传入自己游戏的appid替换demo中的appid。
			
			SDKParams params = U8SDK.getInstance().getSDKParams();
			
			String talkingAppid = params.getString("TALKING_APPID");
			String talkingChannel = params.getString("TALKING_CHANNEL");
			
			Log.e("hulian","TALKING_APPID:" + params.getString("TALKING_APPID"));
			Log.e("hulian","TALKING_CHANNEL:" + params.getString("TALKING_CHANNEL"));
			TalkingDataGA.init(U8SDK.getInstance().getApplication(), talkingAppid, talkingChannel);
			//HuosdkManager.getInstance().init(U8SDK.getInstance().getApplication(), "");
	        Log.d("hulian","init success...");
		} catch (Exception e) {
			Log.e("hulian",e.getMessage());
		}
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
