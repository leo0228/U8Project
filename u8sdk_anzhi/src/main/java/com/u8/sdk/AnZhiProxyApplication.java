package com.u8.sdk;

import com.anzhi.sdk.middle.manage.AnzhiGameApplication;
import com.anzhi.sdk.middle.manage.AnzhiSDKExceptionHandler;
import com.tendcloud.tenddata.TalkingDataGA;

import android.content.Context;
import android.content.res.Configuration;

public class AnZhiProxyApplication extends AnzhiGameApplication implements IApplicationListener {
    @Override
    public void onProxyCreate() {
        super.onCreate();

        SDKParams params = U8SDK.getInstance().getSDKParams();
        String talkingAppid = params.getString("TALKING_APPID");
        String talkingChannel = params.getString("TALKING_CHANNEL");

        TalkingDataGA.init(U8SDK.getInstance().getApplication(), talkingAppid, talkingChannel);

        // 需要在AndroidManifest.xml中<application>标签中加入android:name="路径"，详见本例的AndroidManifest.xml
        Thread.setDefaultUncaughtExceptionHandler(new AnzhiSDKExceptionHandler(U8SDK.getInstance().getApplication()));

    }

    @Override
    public void onProxyAttachBaseContext(Context base) {
        super.attachBaseContext(base);
    }

    @Override
    public void onProxyConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
    }

    @Override
    public void onProxyTerminate() {
        super.onTerminate();
    }

}
