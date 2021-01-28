package com.u8.sdk;

import android.app.Activity;

public class BaiduPay implements IPay{

	public BaiduPay(Activity context){

	}
	
	@Override
	public void pay(PayParams data) {

		BaiduSDK.getInstance().pay(data);
	}

	@Override
	public boolean isSupportMethod(String methodName) {
		return true;
	}

}
