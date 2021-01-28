package com.u8.sdk;

import android.app.Activity;
import android.util.Log;

import com.u8.sdk.utils.Arrays;

public class YSDKUser extends U8UserAdapter {

    private String[] supportedMethods = {"login", "loginCustom", "switchLogin", "logout", "submitExtraData"};

    public YSDKUser(Activity context) {
        YSDK.getInstance().initSDK(U8SDK.getInstance().getSDKParams());
    }

    @Override
    public void login() {
        Log.e(YSDK.TAG, "login");
        YSDK.getInstance().login();
    }

    @Override
    public void loginCustom(String customData) {
        Log.e(YSDK.TAG, "loginCustom");
        if ("QQ".equalsIgnoreCase(customData)) {
            YSDK.getInstance().login(YSDK.LOGIN_TYPE_QQ);
        } else {
            YSDK.getInstance().login(YSDK.LOGIN_TYPE_WX);
        }
    }

    @Override
    public void submitExtraData(UserExtraData extraData) {
        YSDK.getInstance().submitExtraData(extraData);
    }

    @Override
    public void switchLogin() {
        YSDK.getInstance().login();
    }

    @Override
    public void logout() {
        YSDK.getInstance().logout();
    }

    @Override
    public boolean isSupportMethod(String methodName) {

        return Arrays.contain(supportedMethods, methodName);
    }


}
