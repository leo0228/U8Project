package com.u8.sdk;

import java.util.ArrayList;
import java.util.List;

import com.u8.sdk.plugin.U8Ads;
import com.u8.sdk.plugin.U8Analytics;
import com.u8.sdk.plugin.U8Download;
import com.u8.sdk.plugin.U8Pay;
import com.u8.sdk.plugin.U8Push;
import com.u8.sdk.plugin.U8Share;
import com.u8.sdk.plugin.U8User;
import com.u8.sdk.verify.U8Verify;
import com.u8.sdk.verify.UToken;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.multidex.MultiDex;
import android.text.TextUtils;

import com.u8.sdk.log.Log;

public class U8SDK {

    private static final String DEFAULT_PKG_NAME = "com.u8.sdk";
    private static final String APP_PROXY_NAME = "U8_APPLICATION_PROXY_NAME";
    private static final String APP_GAME_NAME = "U8_Game_Application";
    private static final String LOGIC_CHANNEL_PREFIX = "u8channel_";

    private static U8SDK instance;

    private Application application;
    private Activity context;
    private Handler mainThreadHandler;

    private SDKParams developInfo;
    private Bundle metaData;

    private List<IU8SDKListener> listeners;

    private List<IActivityCallback> activityCallbacks;

    private List<IApplicationListener> applicationListeners;

    private String sdkUserID = null;
    private UToken tokenData = null;

    private int channel;


    private U8SDK() {
        mainThreadHandler = new Handler(Looper.getMainLooper());
        listeners = new ArrayList<IU8SDKListener>();
        activityCallbacks = new ArrayList<IActivityCallback>(1);
        applicationListeners = new ArrayList<IApplicationListener>(2);
    }

    public static U8SDK getInstance() {
        if (instance == null) {
            instance = new U8SDK();
        }
        return instance;
    }

    public SDKParams getSDKParams() {
        return developInfo;
    }

    public Bundle getMetaData() {
        return metaData;
    }

    /**
     * 获取CPS,CPA,CPD等非SDK联运渠道的逻辑渠道号
     *
     * @return
     */
    public int getLogicChannel() {

        if (this.channel > 0) {
            return this.channel;
        }

        String chStr = SDKTools.getLogicChannel(application, LOGIC_CHANNEL_PREFIX);
        if (!TextUtils.isEmpty(chStr)) {
            this.channel = Integer.valueOf(chStr);
        } else {
            this.channel = 0;
        }


        return this.channel;
    }

    /**
     * 获取当前SDK对应的渠道号
     *
     * @return
     */
    public int getCurrChannel() {

        if (this.developInfo == null || !this.developInfo.contains("U8_Channel")) {
            return 0;
        } else {
            return this.developInfo.getInt("U8_Channel");
        }

    }

    public int getAppID() {
        if (this.developInfo == null || !this.developInfo.contains("U8_APPID")) {
            return 0;
        }

        return this.developInfo.getInt("U8_APPID");
    }

    public String getAppKey() {
        if (this.developInfo == null || !this.developInfo.contains("U8_APPKEY")) {
            return "";
        }

        return this.developInfo.getString("U8_APPKEY");
    }

    //是否走登录验证
    public boolean isAuth() {

        return getAuthURL() != null;
    }

    public String getAuthURL() {
        if (this.developInfo == null || !this.developInfo.contains("U8_AUTH_URL")) {
            return null;
        }

        return this.developInfo.getString("U8_AUTH_URL");
    }

    //当前渠道SDK是否需要显示闪屏
    public boolean isSDKShowSplash() {
        if (this.developInfo == null || !this.developInfo.contains("U8_SDK_SHOW_SPLASH")) {
            return false;
        }

        String show = this.developInfo.getString("U8_SDK_SHOW_SPLASH");
        return "true".equalsIgnoreCase(show);
    }

    //获取当前渠道SDK的版本号
    public String getSDKVersionCode() {
        if (this.developInfo == null || !this.developInfo.contains("U8_SDK_VERSION_CODE")) {
            return "";
        }

        return this.developInfo.getString("U8_SDK_VERSION_CODE");
    }

    public void setSDKListener(IU8SDKListener listener) {
        if (!listeners.contains(listener) && listener != null) {
            this.listeners.add(listener);
        }
    }

    public void setActivityCallback(IActivityCallback callback) {
        //this.activityCallback = callback;
        if (!this.activityCallbacks.contains(callback) && callback != null) {
            this.activityCallbacks.add(callback);
        }

    }

    public Application getApplication() {

        return this.application;
    }


    public String getSDKUserID() {
        return this.sdkUserID;
    }

    public UToken getUToken() {
        return this.tokenData;
    }

    /**
     * called from onCreate method of Application
     *
     * @param application
     */
    public void onAppCreate(Application application) {
        try {
            Log.d("U8SDK", "application create.");
//			Log.d("U8SDK", "the application is "+this.application.toString());
            //this.application = application;
            for (IApplicationListener lis : applicationListeners) {
                lis.onProxyCreate();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * called from attachBaseContext method of Application
     *
     * @param application
     * @param context
     */
    public void onAppAttachBaseContext(Application application, Context context) {
        this.application = application;
//		Log.d("U8SDK", "the application is "+this.application.toString());
        try {

            applicationListeners.clear();

            PluginFactory.getInstance().loadPluginInfo(context);
            developInfo = PluginFactory.getInstance().getSDKParams(context);
            metaData = PluginFactory.getInstance().getMetaData(context);

            if (metaData.containsKey(APP_PROXY_NAME)) {
                String proxyAppNames = metaData.getString(APP_PROXY_NAME);
                String[] proxyApps = proxyAppNames.split(",");
                for (String proxy : proxyApps) {
                    if (!TextUtils.isEmpty(proxy)) {
                        Log.d("U8SDK", "add a new application listener:" + proxy);
                        IApplicationListener listener = newApplicationInstance(application, proxy);
                        if (listener != null) {
                            applicationListeners.add(listener);
                        }
                    }
                }
            }

            if (metaData.containsKey(APP_GAME_NAME)) {
                String gameAppName = metaData.getString(APP_GAME_NAME);
                IApplicationListener listener = newApplicationInstance(application, gameAppName);
                if (listener != null) {
                    Log.e("U8SDK", "add a game application listener:" + gameAppName);
                    applicationListeners.add(listener);
                }

            }

            for (IApplicationListener lis : applicationListeners) {
                lis.onProxyAttachBaseContext(context);
            }
            MultiDex.install(application);
            Log.init(context);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * called from onConfigurationChanged method of Application
     *
     * @param application
     * @param newConfig
     */
    public void onAppConfigurationChanged(Application application, Configuration newConfig) {
        for (IApplicationListener lis : applicationListeners) {
            lis.onProxyConfigurationChanged(newConfig);
        }
    }

    public void onTerminate() {
        for (IApplicationListener lis : applicationListeners) {
            lis.onProxyTerminate();
        }
        Log.destory();
    }

    @SuppressWarnings("rawtypes")
    private IApplicationListener newApplicationInstance(Application application, String proxyAppName) {

        if (proxyAppName == null || SDKTools.isNullOrEmpty(proxyAppName)) {
            return null;
        }

        if (proxyAppName.startsWith(".")) {
            proxyAppName = DEFAULT_PKG_NAME + proxyAppName;
        }

        try {
            Class clazz = Class.forName(proxyAppName);
            return (IApplicationListener) clazz.newInstance();

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {

            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }


        return null;
    }

    /***
     * 游戏调用抽象层的时候，需要在Activity的onCreate方法中调用该方法
     * @param context
     */
    public void init(Activity context) {
        this.context = context;

//		Log.d("U8SDK", "the application is "+this.application.toString());

        try {
            U8User.getInstance().init();
            U8Pay.getInstance().init();
            U8Share.getInstance().init();
            U8Ads.getInstance().init();
            U8Push.getInstance().init();
            U8Analytics.getInstance().init();
            U8Download.getInstance().init();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void runOnMainThread(Runnable runnable) {
        if (mainThreadHandler != null) {
            mainThreadHandler.post(runnable);
            return;
        }

        if (context != null) {
            context.runOnUiThread(runnable);
        }
    }

    public Activity getContext() {
        return this.context;
    }

    public void onResult(int code, String msg) {
        for (IU8SDKListener listener : listeners) {
            listener.onResult(code, msg);
        }
    }

    public void onInitResult(InitResult result) {
        for (IU8SDKListener listener : listeners) {
            listener.onInitResult(result);
        }
    }

    public void onLoginResult(String result) {

        for (IU8SDKListener listener : listeners) {
            listener.onLoginResult(result);
        }

        if (isAuth()) {
            AuthTask authTask = new AuthTask();
            authTask.execute(result);
        }

    }

    public void onSwitchAccount() {
        for (IU8SDKListener listener : listeners) {
            listener.onSwitchAccount();
        }
    }

    public void onSwitchAccount(String result) {
        for (IU8SDKListener listener : listeners) {
            listener.onSwitchAccount(result);
        }

        AuthTask authTask = new AuthTask();
        authTask.execute(result);
    }

    public void onLogout() {
        for (IU8SDKListener listener : listeners) {
            listener.onLogout();
        }
    }

    private void onAuthResult(UToken token) {

        if (token.isSuc()) {
            this.sdkUserID = token.getSdkUserID();
            this.tokenData = token;
        }

        for (IU8SDKListener listener : listeners) {
            listener.onAuthResult(token);
        }
    }

    public void onPayResult(PayResult result) {
        for (IU8SDKListener listener : listeners) {
            listener.onPayResult(result);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (this.activityCallbacks != null) {
            for (IActivityCallback callback : this.activityCallbacks) {
                callback.onActivityResult(requestCode, resultCode, data);
            }

        }
    }

    public void onBackPressed() {
        if (this.activityCallbacks != null) {
            for (IActivityCallback callback : this.activityCallbacks) {
                callback.onBackPressed();
            }

        }
    }

    public void onCreate() {
        if (this.activityCallbacks != null) {
            for (IActivityCallback callback : this.activityCallbacks) {
                callback.onCreate();
            }

        }
    }

    public void onStart() {
        if (this.activityCallbacks != null) {
            for (IActivityCallback callback : this.activityCallbacks) {
                callback.onStart();
            }
        }
    }

    public void onPause() {
        if (this.activityCallbacks != null) {
            for (IActivityCallback callback : this.activityCallbacks) {
                callback.onPause();
            }

        }
    }


    public void onResume() {

        if (this.activityCallbacks != null) {
            for (IActivityCallback callback : this.activityCallbacks) {
                callback.onResume();
            }

        }

    }


    public void onNewIntent(Intent newIntent) {
        if (this.activityCallbacks != null) {
            for (IActivityCallback callback : this.activityCallbacks) {
                callback.onNewIntent(newIntent);
            }

        }

    }

    public void onStop() {
        if (this.activityCallbacks != null) {
            for (IActivityCallback callback : this.activityCallbacks) {
                callback.onStop();
            }

        }

    }


    public void onDestroy() {
        if (this.activityCallbacks != null) {
            for (IActivityCallback callback : this.activityCallbacks) {
                callback.onDestroy();
            }

        }

    }


    public void onRestart() {
        if (this.activityCallbacks != null) {
            for (IActivityCallback callback : this.activityCallbacks) {
                callback.onRestart();
            }

        }

    }

    public void onConfigurationChanged(Configuration newConfig) {
        if (this.activityCallbacks != null) {
            for (IActivityCallback callback : this.activityCallbacks) {
                callback.onConfigurationChanged(newConfig);
            }
        }
    }

    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) {
        if (this.activityCallbacks != null) {
            for (IActivityCallback callback : this.activityCallbacks) {
                callback.onRequestPermissionResult(requestCode, permissions, grantResults);
            }
        }
    }

    class AuthTask extends AsyncTask<String, Void, UToken> {


        @Override
        protected UToken doInBackground(String... args) {
            String result = args[0];
            Log.d("U8SDK", "begin to auth...");
            UToken token = U8Verify.auth(result);
            return token;
        }

        protected void onPostExecute(UToken token) {

            onAuthResult(token);
        }

    }
}
