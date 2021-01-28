package com.u8.sdk;

import android.app.Activity;

import com.u8.sdk.utils.Arrays;

public class OppoUser extends U8UserAdapter {
    private String[] supportedMethods = {"login", "exit", "logout", "submitExtraData", "jumpLeisureSubject"};

    public OppoUser(Activity context) {
        OppoSDK.getInstance().initSDK(U8SDK.getInstance().getSDKParams());
    }

    /**
     * 判断当前插件是否支持接口中定义的方法
     */
    @Override
    public boolean isSupportMethod(String methodName) {
        return Arrays.contain(supportedMethods, methodName);
    }

    @Override
    public void login() {
        OppoSDK.getInstance().login();
    }

    @Override
    public void logout() {
        U8SDK.getInstance().onLogout();
        U8SDK.getInstance().onResult(U8Code.CODE_LOGOUT_SUCCESS, "logout success");
    }

    @Override
    public void submitExtraData(UserExtraData extraData) {
        OppoSDK.getInstance().submitGameData(extraData);
    }

    @Override
    public void exit() {
        OppoSDK.getInstance().exit();
    }

    @Override
    public void jumpLeisureSubject() {
        OppoSDK.getInstance().jumpLeisureSubject();
    }
}
