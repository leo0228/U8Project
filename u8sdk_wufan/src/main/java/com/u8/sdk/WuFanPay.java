package com.u8.sdk;

import android.app.Activity;

import com.u8.sdk.IPay;
import com.u8.sdk.PayParams;

public class WuFanPay implements IPay {
	public WuFanPay(Activity context) {

	}

	@Override
	public boolean isSupportMethod(String methodName) {
		return true;
	}

	@Override
	public void pay(PayParams data) {
		WuFanSDK.getInstance().Pay(data);
	}

}
