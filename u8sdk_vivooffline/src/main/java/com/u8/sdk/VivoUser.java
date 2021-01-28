package com.u8.sdk;


import com.u8.sdk.utils.Arrays;

import android.app.Activity;

public class VivoUser extends U8UserAdapter{
	private String[] supportedMethods = {"login", "logout", "exit","submitExtraData"};

	public VivoUser(Activity context){
	    VivoSDK.getInstance().initSDK(U8SDK.getInstance().getSDKParams());
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
		VivoSDK.getInstance().login();
	}


	@Override
	public void logout() {
		VivoSDK.getInstance().logout();
	}
	
	@Override
	public void submitExtraData(UserExtraData extraData) {
		VivoSDK.getInstance().submitGameData(extraData);
	}
	
	@Override
	public void exit() {
		VivoSDK.getInstance().exit();
	}
	
}
