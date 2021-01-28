package com.u8.sdk;


import android.app.Activity;

public class DownjoyPay implements IPay{

	
	public DownjoyPay(Activity context){
		
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
    	DownjoySDK.getInstance().pay(data);
    }


}
