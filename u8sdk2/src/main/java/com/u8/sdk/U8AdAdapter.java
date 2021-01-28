package com.u8.sdk;

public abstract class U8AdAdapter implements IAds {
    @Override
    public abstract boolean isSupportMethod(String methodName);

    @Override
    public void rewardAd(String posId) {

    }

    @Override
    public void bannerAd(String posId) {

    }

    @Override
    public void insertAd(String posId) {

    }

    @Override
    public void splashAd(String posId) {

    }

    @Override
    public void nativeAd(String posId, U8AdParams adParams) {

    }

    @Override
    public void nativeClose() {

    }
}
