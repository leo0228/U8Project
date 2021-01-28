package com.u8.sdk;

import com.u8.sdk.utils.Arrays;

import android.app.Activity;

public class MMYUser extends U8UserAdapter{

	
	private String[] supportedMethods = {"login", "switchLogin", "logout", "submitExtraData"};
	/**
	 * 必须定义包含一个Activity参数的构造函数，否则实例化的时候，会失败
	 * 
	 * 一般SDK初始化方法的调用也在这里调用。除非SDK要求要在Application对应的方法调用。
	 * @param context
	 */
	
	public MMYUser(Activity context){
		MMYSDK.getInstance().initSDK(U8SDK.getInstance().getSDKParams());
	}

	@Override
	public void login() {
		MMYSDK.getInstance().login();
	}

	@Override
	public void switchLogin() {
		MMYSDK.getInstance().login();
	}

	@Override
	public void submitExtraData(UserExtraData extraData) {
		MMYSDK.getInstance().submitGameData(extraData);
	}

	/**
	 * 判断当前插件是否支持接口中定义的方法
	 */
	@Override
	public boolean isSupportMethod(String methodName) {
	    return Arrays.contain(supportedMethods, methodName);
	}
	
	@Override
	public boolean showAccountCenter() {
		MMYSDK.getInstance().enterUserCenter();
		return true;
	}
	
	@Override
	public void logout() {
		MMYSDK.getInstance().logout();
	}
	
	@Override
	public void exit() {
		MMYSDK.getInstance().exit();
	}
}
