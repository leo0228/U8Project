package com.u8.sdk;

import android.app.Activity;

import com.u8.sdk.utils.Arrays;

public class MoyoiAds extends U8AdAdapter {

    public MoyoiAds(Activity context) {

    }

    private String[] supportedMethods = {"rewardAd"};

    @Override
    public boolean isSupportMethod(String methodName) {
        return Arrays.contain(supportedMethods, methodName);
    }


    @Override
    public void rewardAd(String posId) {
        MoyoiSDK.getInstance().showTopOnAd(posId);
    }

}
