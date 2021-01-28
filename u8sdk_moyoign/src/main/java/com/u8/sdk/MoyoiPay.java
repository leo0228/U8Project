package com.u8.sdk;

import android.app.Activity;

public class MoyoiPay implements IPay{

	
	public MoyoiPay(Activity context){
		
	}

	@Override
    public boolean isSupportMethod(String methodName) {
        return true;
    }

    /**
     * 打开SDK支付界面
     */
    @Override
    public void pay(PayParams data) {
    	MoyoiSDK.getInstance().pay(data);
    }


}
