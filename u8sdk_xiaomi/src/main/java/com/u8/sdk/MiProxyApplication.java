package com.u8.sdk;

import java.util.List;

import com.miui.zeus.mimo.sdk.MimoSdk;
import com.tendcloud.tenddata.TalkingDataGA;
import com.u8.sdk.log.Log;
import com.xiaomi.gamecenter.sdk.MiCommplatform;
import com.xiaomi.gamecenter.sdk.OnInitProcessListener;
import com.xiaomi.gamecenter.sdk.entry.MiAppInfo;
import com.xiaomi.mipush.sdk.MiPushClient;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Process;

import static com.u8.sdk.MiSDK.TAG;

public class MiProxyApplication implements IApplicationListener {

    @Override
    public void onProxyCreate() {
        SDKParams params = U8SDK.getInstance().getSDKParams();
        String talkingAppId = params.getString("TALKING_APPID");
        String talkingChannel = params.getString("TALKING_CHANNEL");
        TalkingDataGA.init(U8SDK.getInstance().getApplication(), talkingAppId, talkingChannel);

        String appID = params.getString("MiAppID");
        String appKey = params.getString("MiAppKey");
        MiAppInfo appInfo = new MiAppInfo();
        appInfo.setAppId(appID);
        appInfo.setAppKey(appKey);
        MiCommplatform.Init(U8SDK.getInstance().getApplication(), appInfo, new OnInitProcessListener() {
            @Override
            public void onMiSplashEnd() {
                //⼩⽶闪屏⻚结束回调，⼩⽶闪屏可配，⽆闪屏也会返回此回调，游戏的闪屏应当在收到此回调之后开始。
            }

            @Override
            public void finishInitProcess(List<String> arg0, int arg1) {
                U8SDK.getInstance().onResult(U8Code.CODE_INIT_SUCCESS, "init success");
            }
        });

        if (shouldInit()) {
            MiPushClient.registerPush(U8SDK.getInstance().getApplication(), appID, appKey);
        }

        boolean debug_mode = params.getBoolean("debug_mode");
        boolean isShowXiaoMiAd = params.getBoolean("isShowXiaoMiAd");
        if (isShowXiaoMiAd){
            MimoSdk.init(U8SDK.getInstance().getApplication());
            MimoSdk.setDebugOn(debug_mode); // 设置sdk 是否打开debug
            MimoSdk.setStagingOn(debug_mode); // 设置sdk是否打开测试环境
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

    private boolean shouldInit() {
        ActivityManager am = ((ActivityManager) U8SDK.getInstance().getApplication().getSystemService(Context.ACTIVITY_SERVICE));
        List<RunningAppProcessInfo> processInfos = am.getRunningAppProcesses();
        String mainProcessName = U8SDK.getInstance().getApplication().getPackageName();
        int myPid = Process.myPid();
        for (RunningAppProcessInfo info : processInfos) {
            if (info.pid == myPid && mainProcessName.equals(info.processName)) {
                return true;
            }
        }
        return false;
    }
}
