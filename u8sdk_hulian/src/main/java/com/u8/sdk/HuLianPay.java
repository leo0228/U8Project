package com.u8.sdk;


import android.app.Activity;

public class HuLianPay implements IPay{

	
	public HuLianPay(Activity context){
		
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
    	HuLianSDK.getInstance().pay(data);
    }


}
