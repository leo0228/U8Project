package com.u8.sdk;

import android.app.Activity;

public class AnZhiPay implements IPay{

	public AnZhiPay(Activity context){
		
	}
	
	@Override
	public void pay(PayParams data) {

		AnZhiSDK.getInstance().pay(data);
		
	}

	@Override
	public boolean isSupportMethod(String methodName) {
		
		return true;
	}

}
