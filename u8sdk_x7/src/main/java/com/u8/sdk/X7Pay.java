package com.u8.sdk;

import android.app.Activity;

public class X7Pay implements IPay{

	public X7Pay(Activity act) {
		
	}
	
	@Override
	public boolean isSupportMethod(String methodName) {
		return true;
	}

	@Override
	public void pay(PayParams data) {
		X7SDK.getInstance().pay(data);
	}
	
}
