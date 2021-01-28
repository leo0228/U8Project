package com.u8.sdk;

import android.app.ActivityManager;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Process;
import android.util.Log;

import com.heytap.msp.mobad.api.InitParams;
import com.heytap.msp.mobad.api.MobAdManager;
import com.heytap.msp.mobad.api.listener.IInitListener;
import com.tendcloud.tenddata.TalkingDataGA;

import java.util.List;

import static com.u8.sdk.OppoSDK.TAG;

public class OppoProxyApplication implements IApplicationListener {

    @Override
    public void onProxyCreate() {
        String curProcess = getProcessName(U8SDK.getInstance().getApplication(), Process.myPid());
        if (!U8SDK.getInstance().getApplication().getPackageName().equals(curProcess)) {
            return;
        }

        SDKParams params = U8SDK.getInstance().getSDKParams();
        String talkingAppid = params.getString("TALKING_APPID");
        String talkingChannel = params.getString("TALKING_CHANNEL");
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

    }

    /**
     * 获取进程名
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
