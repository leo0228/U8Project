package com.u8.sdk;

import android.app.Activity;

import com.u8.sdk.U8SDK;
import com.u8.sdk.U8UserAdapter;
import com.u8.sdk.utils.Arrays;

public class WuFanUser extends U8UserAdapter {
    private String[] suppertMethods = {"login", "logout", "switchLogin", "exit", "submitExtraData", "showAccountCenter"};


    public WuFanUser(Activity context) {
        WuFanSDK.getInstance().intiSDK(U8SDK.getInstance().getSDKParams());
    }

    public void login() {
        WuFanSDK.getInstance().login();
    }

    @Override
    public void switchLogin() {
        WuFanSDK.getInstance().switchLogin();
    }

    @Override
    public void submitExtraData(UserExtraData extraData) {
        // TODO Auto-generated method stub
        WuFanSDK.getInstance().submitExtraData(extraData);
    }

    @Override
    public void logout() {
        WuFanSDK.getInstance().logout();
    }

    @Override
    public void exit() {
        WuFanSDK.getInstance().exit();
    }

    @Override
    public boolean showAccountCenter() {
        // TODO Auto-generated method stub
        WuFanSDK.getInstance().showUserCenter();
        return true;
    }

    @Override
    public boolean isSupportMethod(String methodName) {
        return Arrays.contain(suppertMethods, methodName);
    }

}
