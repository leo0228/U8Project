package com.u8.sdk.demo;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.u8.common.PromptDialog;
import com.u8.sdk.log.Log;

/**
 * @Description u8
 * @Author Lu
 * @Date 2021/6/4 11:06
 * @Version: 1.0
 */
public class TestMainActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PromptDialog.init(this, new PromptDialog.OnPromptListener() {
            @Override
            public void onInitSDK() {
                Log.d("TestMainActivity","onInitSDK");
            }
        });
    }
}
