package com.u8.sdk;

import android.content.Context;
import android.content.res.Configuration;
import android.util.Log;

import com.mob4399.adunion.AdUnionSDK;
import com.mob4399.adunion.listener.OnAuInitListener;
import com.tendcloud.tenddata.TalkingDataGA;

import static com.u8.sdk.M4399SDK.TAG;

public class M4399Application implements IApplicationListener{

	@Override
	public void onProxyCreate() {
		SDKParams params = U8SDK.getInstance().getSDKParams();
		String talkingAppid = params.getString("TALKING_APPID");
		String talkingChannel = params.getString("TALKING_CHANNEL");
		TalkingDataGA.init(U8SDK.getInstance().getApplication(), talkingAppid, talkingChannel);

		boolean isShow4399Ad = params.getBoolean("isShow4399Ad");
		Log.e(TAG, " isShow4399Ad is " + isShow4399Ad);
		if (isShow4399Ad){
			String ad_appId = params.getString("ad_appId");
			AdUnionSDK.init(U8SDK.getInstance().getApplication(), ad_appId, new OnAuInitListener() {
				@Override
				public void onSucceed() {
					Log.e(TAG, "广告初始化成功");
				}

				@Override
				public void onFailed(String s) {
					Log.e(TAG, "广告初始化失败：" + s);
				}
			});
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
