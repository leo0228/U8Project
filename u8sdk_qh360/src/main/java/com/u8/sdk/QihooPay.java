package com.u8.sdk;

import android.app.Activity;

public class QihooPay implements IPay{

	public QihooPay(Activity context){

	}
	
	@Override
	public void pay(PayParams data) {
		QihooSDK.getInstance().pay(data);
	}

	@Override
	public boolean isSupportMethod(String methodName) {

		return true;
	}

}
