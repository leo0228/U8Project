package com.u8.sdk;

import android.app.Activity;

public class MMYPay implements IPay{

	public MMYPay(Activity context){
		
	}
	
	@Override
	public void pay(PayParams data) {
		MMYSDK.getInstance().pay(data);
	}

	@Override
	public boolean isSupportMethod(String methodName) {
		
		return true;
	}

}
