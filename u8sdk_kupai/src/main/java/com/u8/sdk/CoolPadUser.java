package com.u8.sdk;

import com.u8.sdk.utils.Arrays;

import android.app.Activity;

public class CoolPadUser extends U8UserAdapter{

	private String[] supportedMethods = {"login","switchLogin","logout","submitExtraData"};
	
	public CoolPadUser(Activity context){
		CoolPadSDK.getInstance().initSDK(U8SDK.getInstance().getSDKParams());
	}
	@Override
	public void login() {
		CoolPadSDK.getInstance().login();
	}

	@Override
	public void switchLogin() {
		CoolPadSDK.getInstance().switchLogin();
	}

	@Override
	public void logout() {
		CoolPadSDK.getInstance().logout();
	}

	@Override
	public void submitExtraData(UserExtraData extraData) {
		CoolPadSDK.getInstance().submitGameData(extraData);
	}
	
	@Override
	public boolean isSupportMethod(String methodName) {

		return Arrays.contain(supportedMethods, methodName);
	}

}
