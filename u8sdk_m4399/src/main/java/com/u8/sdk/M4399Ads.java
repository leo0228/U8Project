package com.u8.sdk;

import android.app.Activity;

import com.u8.sdk.utils.Arrays;

public class M4399Ads extends U8AdAdapter {

    public M4399Ads(Activity context) {

    }

    private String[] supportedMethods = {"rewardAd"};

    @Override
    public boolean isSupportMethod(String methodName) {
        return Arrays.contain(supportedMethods, methodName);
    }


    @Override
    public void rewardAd(String posId) {
        M4399SDK.getInstance().show4399Ad(posId);
    }

}
