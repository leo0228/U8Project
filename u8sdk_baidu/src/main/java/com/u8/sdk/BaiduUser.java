package com.u8.sdk;

import com.u8.sdk.utils.Arrays;

import android.app.Activity;

public class BaiduUser extends U8UserAdapter{

	private String[] supportedMethods = {"login", "switchLogin", "logout", "exit"};

	public BaiduUser(Activity context){
		BaiduSDK.getInstance().initSDK(U8SDK.getInstance().getSDKParams());
	}
	
	@Override
	public void login() {
		BaiduSDK.getInstance().login();
	}
	
	public void switchLogin(){
		BaiduSDK.getInstance().logout();
	}

	@Override
	public void submitExtraData(UserExtraData extraData) {
		// TODO Auto-generated method stub
		BaiduSDK.getInstance().submitExtendData(extraData);
	}
	
	@Override
	public void logout() {
		BaiduSDK.getInstance().logout();
	}
	
	public void exit(){
		BaiduSDK.getInstance().exitSDK();
	}

	@Override
	public boolean isSupportMethod(String methodName) {

		return Arrays.contain(supportedMethods, methodName);
	}

}
