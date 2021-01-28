package com.u8.sdk;

import android.app.Activity;

public class MoyoiShare implements IShare {
	public MoyoiShare(Activity context) {

	}

	@Override
	public boolean isSupportMethod(String methodName) {
		return true;
	}

	@Override
	public void share(ShareParams params) {
		MoyoiSDK.getInstance().share(params);		
	}
}
