package com.u8.sdk;

import com.u8.sdk.utils.Arrays;

import android.app.Activity;
import android.util.Log;

public class LionUser extends U8UserAdapter{

	private String[] supportedMethods = {"login", "switchLogin", "logout", "exit","submitExtraData"};
	
	public LionUser(Activity context){
		LionSDK.getInstance().initSDK(U8SDK.getInstance().getSDKParams());
	}
	
	/**
	 * 打开SDK登录界面
	 */
	@Override
	public void login() {
		LionSDK.getInstance().login(true);
	}

	@Override
	public void switchLogin() {
		LionSDK.getInstance().logout();
		LionSDK.getInstance().login(false);
	}
	
	@Override
	public void exit() {
		LionSDK.getInstance().exit();
	}

	@Override
	public void submitExtraData(UserExtraData extraData) {
		LionSDK.getInstance().submitExtendData(extraData);
	}
	
	
	/**
	 * 判断当前插件是否支持接口中定义的方法
	 */
	@Override
	public boolean isSupportMethod(String methodName) {
	
	    return Arrays.contain(supportedMethods, methodName);
	}

	
	@Override
	public void logout() {
		LionSDK.getInstance().logout();
	}
	

}
