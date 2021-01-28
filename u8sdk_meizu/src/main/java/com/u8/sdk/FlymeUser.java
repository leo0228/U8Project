package com.u8.sdk;

import android.app.Activity;

import com.u8.sdk.utils.Arrays;

public class FlymeUser extends U8UserAdapter{

	private String[] supportedMethods = {"login","switchLogin","exit", "logout", "submitExtraData"};
	
	public FlymeUser(Activity context){
		FlymeSDK.getInstance().initSDK(U8SDK.getInstance().getSDKParams());
	}
	
	@Override
	public void login() {
		FlymeSDK.getInstance().login();
	}

	@Override
	public void switchLogin() {
		FlymeSDK.getInstance().login();
	}

	@Override
	public void exit() {
		FlymeSDK.getInstance().exitSDK();
	}
	
	@Override
	public void submitExtraData(UserExtraData extraData) {
		FlymeSDK.getInstance().sendExtraData(extraData);
	}
	
	@Override
	public void logout() {
		FlymeSDK.getInstance().logout();
	}
	
	@Override
	public boolean isSupportMethod(String methodName) {

		return Arrays.contain(supportedMethods, methodName);
	}
	
}
