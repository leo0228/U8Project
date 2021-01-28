package com.u8.sdk;

import android.app.Activity;

import com.u8.sdk.utils.Arrays;

public class UCAds extends U8AdAdapter {

    public UCAds(Activity context) {

    }

    private String[] supportedMethods = {"rewardAd"};

    @Override
    public boolean isSupportMethod(String methodName) {
        return Arrays.contain(supportedMethods, methodName);
    }


    @Override
    public void rewardAd(String posId) {
        UCSDK.getInstance().showUCAd(posId);
    }

}
