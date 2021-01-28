package com.u8.sdk;

import android.app.Activity;

import com.u8.sdk.utils.Arrays;

public class SanXingUser extends U8UserAdapter {
	private String[] suppertMethods = { "login", "logout","switchLogin", "exit", "submitExtraData", "showAccountCenter" };


	public  SanXingUser(Activity context) {
		try{
			SanXingSDK.getInstance().intiSDK(U8SDK.getInstance().getSDKParams());
		}catch(Exception e){
		}
	}

	public void login() {
		SanXingSDK.getInstance().login();
	}

	@Override
	public void switchLogin() {
		SanXingSDK.getInstance().logout();
		SanXingSDK.getInstance().login();
	}
	
	@Override
	public void submitExtraData(UserExtraData extraData) {
		// TODO Auto-generated method stub
		SanXingSDK.getInstance().submitExtraData(extraData);
	}
	
	@Override
	public void logout() {
		SanXingSDK.getInstance().logout();
	}

	@Override
	public void exit() {
		SanXingSDK.getInstance().exit();
	}

	@Override
	public boolean isSupportMethod(String methodName) {
		return Arrays.contain(suppertMethods, methodName);
	}

}
