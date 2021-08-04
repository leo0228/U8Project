package com.u8.sdk;

import android.content.Context;
import android.content.res.Configuration;
import android.util.Log;

import com.anythink.core.api.ATSDK;
import com.nj9you.sdk.talkingdata.TalkingdataApplication;

import static com.u8.sdk.MoyoiSDK.TAG;

public class MoyoiApplication implements IApplicationListener {

    @Override
    public void onProxyCreate() {
        SDKParams params = U8SDK.getInstance().getSDKParams();
        String talkingAppId = params.getString("TALKING_APPID");
        String talkingChannel = params.getString("TALKING_CHANNEL");
        TalkingdataApplication.init(U8SDK.getInstance().getApplication(), talkingAppId, talkingChannel);

        ATSDK.setNetworkLogDebug(true);//SDK日志功能，集成测试阶段建议开启，上线前必须关闭
        Log.i(TAG, "TopOn SDK version: " + ATSDK.getSDKVersionName());//SDK版本

        String topOnAppID = params.getString("toponAd_appId");
        String topOnAppKey = params.getString("toponAd_appKey");
        ATSDK.init(U8SDK.getInstance().getApplication(), topOnAppID, topOnAppKey);//初始化SDK
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
