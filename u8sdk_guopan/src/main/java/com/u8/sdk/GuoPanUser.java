package com.u8.sdk;

import com.u8.sdk.log.Log;
import com.u8.sdk.utils.Arrays;

import android.app.Activity;

public class GuoPanUser extends U8UserAdapter{

	private String[] supportedMethods = {"login", "switchLogin","logout","submitExtraData", "exit"};
	
	public GuoPanUser(Activity context){
		
		GuoPanSDK.getInstance().initSDK(U8SDK.getInstance().getSDKParams());
	}
	
	@Override
	public void login() {
		GuoPanSDK.getInstance().login();
	}

	@Override
	public void switchLogin() {
		Log.e("GPSDK", "switchLogin");
		GuoPanSDK.getInstance().switchLogin();
	}

	@Override
	public void logout() {
		Log.e("GPSDK", "logout");
		GuoPanSDK.getInstance().logout();
	}
	
	@Override
	public void submitExtraData(UserExtraData extraData) {
		GuoPanSDK.getInstance().sendExtraData(extraData);
	}

	@Override
	public void exit() {
		Log.e("GPSDK", "exit");
		GuoPanSDK.getInstance().exitSDK();
	}

	@Override
	public boolean isSupportMethod(String methodName) {
		return Arrays.contain(supportedMethods, methodName);
	}

}
