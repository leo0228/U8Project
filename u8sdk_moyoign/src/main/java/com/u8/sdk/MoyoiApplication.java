package com.u8.sdk;

import com.nj9you.sdk.ad.TTAdApplication;
import com.nj9you.sdk.talkingdata.TalkingdataApplication;

import android.content.Context;
import android.content.res.Configuration;
import android.util.Log;

import static com.u8.sdk.MoyoiSDK.TAG;

public class MoyoiApplication implements IApplicationListener {

    @Override
    public void onProxyCreate() {
        SDKParams params = U8SDK.getInstance().getSDKParams();
        String talkingAppId = params.getString("TALKING_APPID");
        String talkingChannel = params.getString("TALKING_CHANNEL");
        TalkingdataApplication.init(U8SDK.getInstance().getApplication(), talkingAppId, talkingChannel);

        boolean isShowTTAd = params.getBoolean("isShowTTAd");
        Log.i(TAG, "isShowTTAd is " + isShowTTAd);
        if (isShowTTAd) {
            String ttAd_appId = params.getString("ttAd_appId");
            Log.i(TAG, "ttAd_appId is " + ttAd_appId);
            TTAdApplication.init(U8SDK.getInstance().getApplication(), ttAd_appId, false);
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
