package com.u8.sdk;

import android.app.Activity;

public class MZWPay implements IPay{

	public MZWPay(Activity context){
		
	}
	
	@Override
	public void pay(PayParams data) {
		MZWSDK.getInstance().pay(data);
	}

	@Override
	public boolean isSupportMethod(String methodName) {

		return true;
	}

}
