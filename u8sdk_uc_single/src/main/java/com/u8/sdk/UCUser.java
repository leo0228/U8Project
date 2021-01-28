package com.u8.sdk;

import android.app.Activity;

import com.u8.sdk.utils.Arrays;

public class UCUser extends U8UserAdapter {

	private String[] supportedMethods = {"login","switchLogin","logout","submitExtraData","exit"};
	
	public UCUser(Activity context){
		UCSDK.getInstance().initSDK(U8SDK.getInstance().getSDKParams());
	}
	
	@Override
	public boolean isSupportMethod(String methodName) {
		return Arrays.contain(supportedMethods, methodName);
	}
	
	@Override
	public void login() {
		UCSDK.getInstance().login();
	}

	@Override
	public void switchLogin() {
		UCSDK.getInstance().login();
	}	
	
	@Override
	public void logout() {
		UCSDK.getInstance().logout();
	}

	@Override
	public void submitExtraData(UserExtraData extraData) {
		UCSDK.getInstance().submitExtraData(extraData);
	}

	@Override
	public void exit() {
		UCSDK.getInstance().exitSDK();
	}	

}
