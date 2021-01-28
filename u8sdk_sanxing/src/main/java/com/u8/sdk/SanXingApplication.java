package com.u8.sdk;

import android.content.Context;
import android.content.res.Configuration;
import android.util.Log;

import com.tendcloud.tenddata.TalkingDataGA;
import com.u8.sdk.IApplicationListener;
import com.u8.sdk.U8SDK;

public class SanXingApplication implements IApplicationListener{
	private static final String TAG = "U8SDK";
	

	@Override
	public void onProxyCreate() {
		
		SDKParams params = U8SDK.getInstance().getSDKParams();
		
		String talkingAppid = params.getString("TALKING_APPID");
		String talkingChannel = params.getString("TALKING_CHANNEL");
		Log.e("sanxing", "talkingAppid : "+talkingAppid);
		Log.e("sanxing", "talkingChannel : "+talkingChannel);
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
		Log.d(TAG,"onTerminate");
		// 防止内存泄露，清理相关数据务必调用SDK结束接口
		
	}

}
