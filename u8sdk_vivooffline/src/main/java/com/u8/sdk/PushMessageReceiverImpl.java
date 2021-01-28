package com.u8.sdk;

import com.vivo.push.model.UPSNotificationMessage;
import com.vivo.push.sdk.OpenClientPushMessageReceiver;

import android.content.Context;
import android.util.Log;

public class PushMessageReceiverImpl extends OpenClientPushMessageReceiver{

	@Override
    public void onNotificationMessageClicked(Context context, UPSNotificationMessage msg) {
        String customContentString = msg.getSkipContent();
        String notifyString = "通知点击 msgId " + msg.getMsgId() + " ;customContent=" + customContentString;
        Log.d(VivoSDK.TAG, notifyString);

        // 应用请在这里加入自己的处理逻辑
    }

    @Override
    public void onReceiveRegId(Context context, String regId) {
        String responseString = "onReceiveRegId regId = " + regId;
        Log.d(VivoSDK.TAG, responseString);

        // 应用请在这里加入自己的处理逻辑
    }

}
