package com.u8.sdk;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.tencent.ysdk.api.YSDKApi;
import com.tencent.ysdk.framework.common.ePlatform;

/**
 * 选择QQ、微信登录方式
 */
public class ChooseLoginTypeActivity extends Activity {
	
	private Button btnQQ;
	private Button btnWX;
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setFinishOnTouchOutside(false);
		
		int layoutID = getResources().getIdentifier("u8_layout_login_choice", "layout", getPackageName());
		setContentView(layoutID);
		btnQQ = (Button) findViewById(getResources().getIdentifier("btn_qq", "id", getPackageName()));
		btnWX = (Button) findViewById(getResources().getIdentifier("btn_wx", "id", getPackageName()));

		btnQQ.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				Log.d(YSDK.TAG,"login by QQ");
				ChooseLoginTypeActivity.this.finish();
				if(YSDKApi.isPlatformInstalled(ePlatform.QQ)){
					YSDK.getInstance().login(YSDK.LOGIN_TYPE_QQ);
				}else{
					YSDK.getInstance().showTip("您还没有安装QQ，请先安装QQ");
					U8SDK.getInstance().onResult(U8Code.CODE_LOGIN_FAIL, "login failed");
				}
				
			}
		});
		btnWX.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Log.e(YSDK.TAG,"login by WX");
				ChooseLoginTypeActivity.this.finish();
				if(YSDKApi.isPlatformInstalled(ePlatform.WX)){
					YSDK.getInstance().login(YSDK.LOGIN_TYPE_WX);
				}else{
					YSDK.getInstance().showTip("您还没有安装微信，请先安装微信");
					U8SDK.getInstance().onResult(U8Code.CODE_LOGIN_FAIL, "login failed");
				}

			}
		});
	}

	@Override
	public void onBackPressed() {

	}
}
