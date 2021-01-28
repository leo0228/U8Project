package com.u8.sdk;

import android.app.Activity;

public class SanXingPay implements IPay {
	public SanXingPay(Activity context) {

	}

	@Override
	public boolean isSupportMethod(String methodName) {
		return true;
	}

	@Override
	public void pay(PayParams data) {
		SanXingSDK.getInstance().Pay(data);
	}

}
