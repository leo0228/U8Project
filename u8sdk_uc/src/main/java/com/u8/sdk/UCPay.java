package com.u8.sdk;

import android.app.Activity;

public class UCPay implements IPay {

	public UCPay(Activity context){
		
	}
	
	@Override
	public boolean isSupportMethod(String methodName) {
		return true;
	}

	@Override
	public void pay(PayParams data) {
		UCSDK.getInstance().pay(data);
	}

}
