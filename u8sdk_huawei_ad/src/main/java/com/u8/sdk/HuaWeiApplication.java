package com.u8.sdk;

import com.huawei.agconnect.config.AGConnectServicesConfig;
import com.huawei.agconnect.config.LazyInputStream;
import com.huawei.hms.api.HuaweiMobileServicesUtil;
import com.tendcloud.tenddata.TalkingDataGA;

import android.content.Context;
import android.content.res.Configuration;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

public class HuaWeiApplication implements IApplicationListener{

	@Override
	public void onProxyCreate() {

		SDKParams params = U8SDK.getInstance().getSDKParams();
		String talkingAppid = params.getString("TALKING_APPID");
		String talkingChannel = params.getString("TALKING_CHANNEL");
		
		Log.e("huawei", "talkingAppid : "+talkingAppid);
		Log.e("huawei", "talkingChannel : "+talkingChannel);
		
		TalkingDataGA.init(U8SDK.getInstance().getApplication(), talkingAppid, talkingChannel);

		HuaweiMobileServicesUtil.setApplication(U8SDK.getInstance().getApplication());
	}

	@Override
	public void onProxyAttachBaseContext(Context base) {
		AGConnectServicesConfig config = AGConnectServicesConfig.fromContext(U8SDK.getInstance().getApplication());
		config.overlayWith(new LazyInputStream(U8SDK.getInstance().getApplication()) {
			public InputStream get(Context context) {
				try {
					return context.getAssets().open("agconnect-services.json");
				} catch (IOException e) {
					return null;
				}
			}
		});
	}

	@Override
	public void onProxyConfigurationChanged(Configuration config) {

	}

	@Override
	public void onProxyTerminate() {

	}

}
