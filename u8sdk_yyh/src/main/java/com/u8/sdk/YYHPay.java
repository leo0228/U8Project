package com.u8.sdk;

import android.app.Activity;

public class YYHPay implements IPay{

	public YYHPay(Activity context){
		
	}
	
	@Override
	public void pay(PayParams data) {
		YYHSDK.getInstance().pay(data);
	}

	@Override
	public boolean isSupportMethod(String methodName) {
		
		return true;
	}

}
