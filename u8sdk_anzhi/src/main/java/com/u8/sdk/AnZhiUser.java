package com.u8.sdk;

import android.app.Activity;

import com.u8.sdk.utils.Arrays;

public class AnZhiUser extends U8UserAdapter{

	private String[] supportedMethods = {"login", "switchLogin", "logout", "submitExtraData", "exit"};
	
	public AnZhiUser(Activity context){
		AnZhiSDK.getInstance().initSDK(U8SDK.getInstance().getSDKParams());
	}
	
	
	@Override
	public void login() {
		AnZhiSDK.getInstance().login();
	}

	@Override
	public void switchLogin() {
		AnZhiSDK.getInstance().logout();
	}

	@Override
	public void logout() {
		AnZhiSDK.getInstance().logout();
	}


	@Override
	public void submitExtraData(UserExtraData extraData) {
		AnZhiSDK.getInstance().submitExtraData(extraData);
	}

	@Override
	public void exit() {
		AnZhiSDK.getInstance().sdkExit();
	}

	@Override
	public boolean isSupportMethod(String methodName) {
		return Arrays.contain(supportedMethods, methodName);
	}	
	
	
}
