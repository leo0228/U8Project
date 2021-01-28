package com.u8.sdk;


import android.content.Context;
import android.content.res.Configuration;
import android.util.Log;


public class MoyoiApplication implements IApplicationListener {

    @Override
    public void onProxyCreate() {
        try {
            Log.d("U8SDK", "init success...");
        } catch (Exception e) {
            Log.e("U8SDK", e.getMessage());
        }

    }

    @Override
    public void onProxyAttachBaseContext(Context base) {
//		super.attachBaseContext(base);
    }

    @Override
    public void onProxyConfigurationChanged(Configuration config) {
//		super.onConfigurationChanged(config);
    }

    @Override
    public void onProxyTerminate() {
//		super.onTerminate();
    }

}
