package com.u8.sdk;

import android.app.Activity;

import com.u8.sdk.utils.Arrays;

public class MiAds extends U8AdAdapter{

    public MiAds(Activity context){

    }

    private String[] supportedMethods = {"rewardAd"};

    @Override
    public boolean isSupportMethod(String methodName) {
        return Arrays.contain(supportedMethods, methodName);
    }

    @Override
    public void rewardAd(String posId) {
        MiSDK.getInstance().showXiaoMiAd(posId);
    }

}
