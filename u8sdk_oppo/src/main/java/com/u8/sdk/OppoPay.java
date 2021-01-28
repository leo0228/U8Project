package com.u8.sdk;


import android.app.Activity;

public class OppoPay implements IPay{

	
	public OppoPay(Activity context){
		
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
    	OppoSDK.getInstance().pay(data);
    }


}
