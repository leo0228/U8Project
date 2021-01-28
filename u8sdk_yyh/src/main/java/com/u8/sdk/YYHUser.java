package com.u8.sdk;

import com.u8.sdk.utils.Arrays;

import android.app.Activity;

public class YYHUser extends U8UserAdapter {

    private String[] supportedMethods = {"login", "logout", "switchLogin","showAccountCenter","submitExtraData"};

    public YYHUser(Activity context) {
        YYHSDK.getInstance().initSDK(U8SDK.getInstance().getSDKParams());
    }

    @Override
    public void login() {
        YYHSDK.getInstance().login();
    }

    @Override
    public void logout() {
        YYHSDK.getInstance().logout();
    }

    @Override
    public void switchLogin() {
        YYHSDK.getInstance().login();
    }

    @Override
    public boolean showAccountCenter() {
        YYHSDK.getInstance().showAccountCenter();
        return true;
    }

    @Override
    public void submitExtraData(UserExtraData extraData) {
        YYHSDK.getInstance().submitGameData(extraData);
    }

    @Override
    public boolean isSupportMethod(String methodName) {
        return Arrays.contain(supportedMethods, methodName);
    }

}
