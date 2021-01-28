package com.u8.sdk;

import com.u8.sdk.utils.Arrays;

import android.app.Activity;

public class QihooUser extends U8UserAdapter{

	private String[] supportedMethods = {"login","switchLogin","logout","submitExtraData","exit","realNameRegister","queryAntiAddiction"};

	public QihooUser(Activity context){
		QihooSDK.getInstance().initSDK(U8SDK.getInstance().getSDKParams());
	}
	
	@Override
	public void login() {
		QihooSDK.getInstance().login();
	}

	@Override
	public void switchLogin() {	
		QihooSDK.getInstance().switchAccount();
	}

	@Override
	public void logout() {
		QihooSDK.getInstance().logout();
	}

	@Override
	public void submitExtraData(UserExtraData extraData) {
	
		QihooSDK.getInstance().submitExtendData(extraData);
	}

	@Override
	public void exit() {
		QihooSDK.getInstance().quitSDK();
	}

	@Override
	public void realNameRegister() {
		QihooSDK.getInstance().realNameRegister();
	}

	@Override
	public void queryAntiAddiction() {
		QihooSDK.getInstance().doSdkAntiAddictionQuery();
	}

	@Override
	public boolean isSupportMethod(String methodName) {

		return Arrays.contain(supportedMethods, methodName);
	}


}
