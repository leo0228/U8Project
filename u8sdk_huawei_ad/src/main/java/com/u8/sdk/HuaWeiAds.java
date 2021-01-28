package com.u8.sdk;

import android.app.Activity;

import com.u8.sdk.utils.Arrays;

public class HuaWeiAds extends U8AdAdapter {
    public HuaWeiAds(Activity context) {

    }

    private String[] supportedMethods = {"rewardAd"};

    @Override
    public boolean isSupportMethod(String methodName) {
        return Arrays.contain(supportedMethods, methodName);
    }

    @Override
    public void rewardAd(String posId) {
        HuaWeiSDK.getInstance().showHuaWeiAd(posId);
    }

}
