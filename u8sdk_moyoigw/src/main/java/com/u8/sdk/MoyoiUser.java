package com.u8.sdk;

import com.u8.sdk.utils.Arrays;

import android.app.Activity;

public class MoyoiUser extends U8UserAdapter {
	private String[] supportedMethods = { "login", "switchLogin", "logout","exit"};

	/**
	 * 必须定义包含一个Activity参数的构造函数，否则实例化的时候，会失败
	 * 
	 * 一般SDK初始化方法的调用也在这里调用。除非SDK要求要在Application对应的方法调用。
	 * 
	 * @param context
	 */
	public MoyoiUser(Activity context) {
		MoyoiSDK.getInstance().initSDK(U8SDK.getInstance().getSDKParams());
	}

	/**
	 * 判断当前插件是否支持接口中定义的方法
	 */
	@Override
	public boolean isSupportMethod(String methodName) {
		return Arrays.contain(supportedMethods, methodName);
	}

	/**
	 * 打开SDK登录界面
	 */
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
    public void exit() {
        MoyoiSDK.getInstance().exit();
    }
}
