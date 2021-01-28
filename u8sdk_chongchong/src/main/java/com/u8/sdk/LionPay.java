package com.u8.sdk;

import android.app.Activity;

public class LionPay implements IPay {
	
	public LionPay(Activity context){
	}
	
	@Override
	public void pay(PayParams data) {
		LionSDK.getInstance().pay(data);
	}

	@Override
	public boolean isSupportMethod(String methodName) {

		return true;
	}

}
