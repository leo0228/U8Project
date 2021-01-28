package com.u8.sdk;

import com.u8.sdk.utils.Arrays;

import android.app.Activity;

public class MiUser extends U8UserAdapter {

    private String[] supportedMethods = {"login", "exit", "switchLogin", "logout", "submitExtraData", "showCallCenter"};

    public MiUser(Activity context) {
        MiSDK.getInstance().initSDK(U8SDK.getInstance().getSDKParams());
    }

    @Override
    public boolean isSupportMethod(String methodName) {
        return Arrays.contain(supportedMethods, methodName);
    }
    @Override
    public void login() {
        MiSDK.getInstance().login();
    }

    @Override
    public void switchLogin() {
        MiSDK.getInstance().login();
    }

    @Override
    public void submitExtraData(UserExtraData extraData) {
        MiSDK.getInstance().submitExtendData(extraData);
    }

    @Override
    public void logout() {
        MiSDK.getInstance().logout();
    }

    @Override
    public void exit() {
        MiSDK.getInstance().exit();
    }
}
