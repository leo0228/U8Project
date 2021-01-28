package com.u8.sdk;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Process;
import android.util.Log;

import com.tendcloud.tenddata.TalkingDataGA;
import com.vivo.mobilead.manager.VivoAdManager;
import com.vivo.mobilead.util.VOpenLog;
import com.vivo.push.IPushActionListener;
import com.vivo.push.PushClient;
import com.vivo.unionsdk.open.VivoUnionSDK;

import java.util.List;

import static com.u8.sdk.VivoSDK.TAG;

public class VivoProxyApplication implements IApplicationListener {
    @Override
    public void onProxyCreate() {
        String curProcess = getProcessName(U8SDK.getInstance().getApplication(), Process.myPid());
        if (!U8SDK.getInstance().getApplication().getPackageName().equals(curProcess)) {
            return;
        }

        SDKParams params = U8SDK.getInstance().getSDKParams();
        String talkingAppId = params.getString("TALKING_APPID");
        String talkingChannel = params.getString("TALKING_CHANNEL");
        TalkingDataGA.init(U8SDK.getInstance().getApplication(), talkingAppId, talkingChannel);

        String appId = params.getString("AppID");
        Log.i(TAG,"appId is " + appId);
        boolean debug = params.getBoolean("debug_mode");
        VivoUnionSDK.initSdk(U8SDK.getInstance().getApplication(), appId, debug);

        boolean isShowVivoAd = params.getBoolean("isShowVivoAd");
        Log.i(TAG,"isShowVivoAd is " + isShowVivoAd);
        if (isShowVivoAd) {
            String mediaId = params.getString("ad_appId");
            Log.i(TAG,"mediaId is " + mediaId);
            //广告SDK初始化
            VivoAdManager.getInstance().init(U8SDK.getInstance().getApplication(), mediaId);
            //开启日志输出
            VOpenLog.setEnableLog(debug);
        }

        PushClient.getInstance(U8SDK.getInstance().getApplication()).initialize();
        PushClient.getInstance(U8SDK.getInstance().getApplication()).turnOnPush(new IPushActionListener() {

            @Override
            public void onStateChanged(int state) {
                Log.i(TAG, "目前push开关状态 : " + state);
            }
        });
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

    /**
     * 获取进程名
     *
     * @param cxt
     * @param pid
     * @return
     */
    private String getProcessName(Context cxt, int pid) {
        ActivityManager am = (ActivityManager) cxt.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningApps = am.getRunningAppProcesses();
        if (runningApps == null) {
            return null;
        }
        for (ActivityManager.RunningAppProcessInfo procInfo : runningApps) {
            if (procInfo.pid == pid) {
                return procInfo.processName;
            }
        }
        return null;
    }
}
