package com.u8.sdk;

import android.app.Activity;

public class GuoPanPay implements IPay{

	public GuoPanPay(Activity context){
		
	}
	
	@Override
	public void pay(PayParams data) {
		GuoPanSDK.getInstance().pay(data);
	}

	@Override
	public boolean isSupportMethod(String methodName) {
		
		return true;
	}

}
