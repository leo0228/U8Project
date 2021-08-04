package com.u8.sdk;

import android.app.Activity;

import com.u8.sdk.utils.Arrays;

public class MoyoiUser extends U8UserAdapter {
    private String[] supportedMethods = {"login", "switchLogin", "logout", "submitExtraData"};

    public MoyoiUser(Activity context) {
        MoyoiSDK.getInstance().initSDK(U8SDK.getInstance().getSDKParams());
    }

    @Override
    public boolean isSupportMethod(String methodName) {
        return Arrays.contain(supportedMethods, methodName);
    }

    @Override
    public void login() {
        MoyoiSDK.getInstance().login();
    }


    @Override
    public void switchLogin() {
        MoyoiSDK.getInstance().logout();
    }


    @Override
    public void logout() {
        MoyoiSDK.getInstance().logout();
    }

    @Override
    public void submitExtraData(UserExtraData extraData) {
        MoyoiSDK.getInstance().submitGameData(extraData);
    }

}
