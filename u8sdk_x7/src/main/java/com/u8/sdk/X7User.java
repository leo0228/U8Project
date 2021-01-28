package com.u8.sdk;

import com.u8.sdk.utils.Arrays;

import android.app.Activity;

public class X7User extends U8UserAdapter {

	public X7User(Activity act) {
		X7SDK.getInstance().initSDK(U8SDK.getInstance().getSDKParams());
	}
	
	private String[] supportedMethods = { "login", "exit", "logout", "submitExtraData" };

	@Override
	public boolean isSupportMethod(String methodName) {
		return Arrays.contain(supportedMethods, methodName);
	}

	@Override
	public void login() {
		X7SDK.getInstance().login();
	}
	
	@Override
	public void logout() {
		X7SDK.getInstance().logout();
	}
	
	@Override
	public void exit() {
		X7SDK.getInstance().exit();
	}
	
	@Override
	public void submitExtraData(UserExtraData extraData) {
		X7SDK.getInstance().submitGameData(extraData);
	}
	
}

