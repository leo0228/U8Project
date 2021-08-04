package com.u8.sdk;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.util.Log;
import android.util.Xml;
import android.widget.Toast;

import com.appchina.usersdk.model.Account;
import com.appchina.usersdk.model.ErrorMsg;
import com.tendcloud.tenddata.TDGAProfile;
import com.tendcloud.tenddata.TDGAVirtualCurrency;
import com.u8.sdk.appchina.GoodInfo;
import com.yyh.sdk.AccountCallback;
import com.yyh.sdk.CPInfo;
import com.yyh.sdk.LoginCallback;
import com.yyh.sdk.PayResultCallback;
import com.yyh.sdk.YYHSDKAPI;
import com.u8.common.PromptDialog;

import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

public class YYHSDK {
    private Activity activity;
    private String TAG = "YYHSDK";

    private static YYHSDK instance;

    private String appId;
    private String appKey;
    private String privateKey;
    private String publicKey;
    private String callBackUrl;

    private int orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;

    private Map<String, GoodInfo> goods;

    private YYHSDK() {
        activity = U8SDK.getInstance().getContext();
        this.goods = new HashMap<String, GoodInfo>();
    }

    public static YYHSDK getInstance() {
        if (instance == null) {
            instance = new YYHSDK();
        }
        return instance;
    }

    public void parseSDKParams(SDKParams params) {

        this.appId = params.getString("AppChina_AppId");
        this.appKey = params.getString("AppChina_AppKey");
        this.privateKey = params.getString("AppChina_PrivateKey");
        this.publicKey = params.getString("AppChina_PublicKey");
        this.callBackUrl = params.getString("AppChina_CallbackUrl");

        String orn = params.getString("AppChina_Orientation");
        if ("landscape".equalsIgnoreCase(orn)) {
            this.orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        } else if ("portrait".equalsIgnoreCase(orn)) {
            this.orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        } else {
            this.orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        }

        Log.e(TAG, "appId : " + appId);
        Log.e(TAG, "appKey : " + appKey);
        Log.e(TAG, "privateKey : " + privateKey);
        Log.e(TAG, "publicKey : " + publicKey);
        Log.e(TAG, "callBackUrl : " + callBackUrl);
        Log.e(TAG, "orn : " + orn);
    }

    public void initSDK(SDKParams params) {
        this.loadPayGoods();
        this.parseSDKParams(params);

        PromptDialog.init(activity, new PromptDialog.OnPromptListener() {
            @Override
            public void onInitSDK() {
                initSDK();
            }
        });

    }

    private void initSDK() {

        CPInfo info = new CPInfo();
        info.appId = Integer.valueOf(this.appId);
        info.appKey = this.appKey;
        info.privateKey = this.privateKey;
        info.publicKey = this.publicKey;
        info.orientation = (this.orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE ? CPInfo.LANDSCAPE
                : CPInfo.PORTRAIT);

        // 开启Debug模式，可在控制台查看SDK内部日志
        YYHSDKAPI.setDebugModel(false);

        YYHSDKAPI.init(activity, info, new AccountCallback() {

            @Override
            public void onSwitchAccount(Account arg0, Account user) {

                JSONObject json = new JSONObject();
                try {
                    json.put("userId", user.userId);
                    json.put("username", user.userName);
                    json.put("ticket", user.ticket);
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                U8SDK.getInstance().onSwitchAccount(json.toString());
            }

            @Override
            public void onLogout() {
                U8SDK.getInstance().onLogout();
                U8SDK.getInstance().onResult(U8Code.CODE_LOGOUT_SUCCESS, "logout success");

            }
        });

        U8SDK.getInstance().setActivityCallback(new ActivityCallbackAdapter() {
            @Override
            public void onResume() {
                super.onResume();
                YYHSDKAPI.showToolbar(activity, true);
            }

            @Override
            public void onPause() {
                super.onPause();
                YYHSDKAPI.showToolbar(activity, false);
            }

        });

    }

    public void logout() {
        YYHSDKAPI.logout(activity);
    }

    public void login() {

        if (!SDKTools.isNetworkAvailable(activity)) {
            U8SDK.getInstance().onResult(U8Code.CODE_NO_NETWORK, "The network now is unavailable");
            return;
        }


        YYHSDKAPI.login(activity, new LoginCallback() {

            @Override
            public void onLoginCancel() {
                U8SDK.getInstance().onResult(U8Code.CODE_LOGIN_FAIL, "login canceled");
            }

            @Override
            public void onLoginError(Activity act, ErrorMsg msg) {
                U8SDK.getInstance().onResult(U8Code.CODE_LOGIN_FAIL, msg.message);
            }

            @Override
            public void onLoginSuccess(Activity act, Account user) {
                YYHSDKAPI.showToolbar(act, true);

                String userName = user.userName;
                int userId = user.userId;
                String ticket = user.ticket;


                JSONObject json = new JSONObject();
                try {
                    json.put("userId", userId);
                    json.put("username", userName);
                    json.put("ticket", ticket);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                U8SDK.getInstance().onResult(U8Code.CODE_LOGIN_SUCCESS, userName);
                U8SDK.getInstance().onLoginResult(json.toString());
            }

        });

    }

    public void showAccountCenter() {
        YYHSDKAPI.openAccountCenter(activity);// 打开个人中心
    }

    public void submitGameData(UserExtraData extraData) {
        switch (extraData.getDataType()) {
            case UserExtraData.TYPE_ENTER_GAME:
                enterGame(extraData);
                break;
            case UserExtraData.TYPE_LEVEL_UP:
                upgrade(extraData);
                break;
        }
    }

    private void enterGame(UserExtraData extraData) {
        //游戏玩家以匿名（快速登录）方式在国服2区进行游戏时，做如下调用
        TDGAProfile profile = TDGAProfile.setProfile(extraData.getRoleID());
        profile.setProfileType(TDGAProfile.ProfileType.ANONYMOUS);
        profile.setLevel(Integer.parseInt(extraData.getRoleLevel()));
        profile.setGameServer(String.valueOf(extraData.getServerID()));

        //玩家显性注册成功时，做如下调用
        profile.setProfileName(extraData.getRoleName());
    }

    private void upgrade(UserExtraData extraData) {
        TDGAProfile profile = TDGAProfile.setProfile(extraData.getRoleID());
        //玩家升级时，做如下调用
        profile.setLevel(Integer.parseInt(extraData.getRoleLevel()));
    }

    public void pay(final PayParams params) {

        if (!SDKTools.isNetworkAvailable(activity)) {
            U8SDK.getInstance().onResult(U8Code.CODE_NO_NETWORK, "The network now is unavailable");
            return;
        }

        Log.e(TAG, "ProductId : " + params.getProductId());
        GoodInfo good = getGoodInfo(params.getProductId());
        String waresId = "0";
        if (good != null) {
            waresId = good.getWaresid();
            Log.e(TAG, "waresid : " + waresId);
        }

        float iPrice;
        try {
            iPrice = Float.parseFloat(String.valueOf(params.getPrice()));
        } catch (Exception e) {
            Toast.makeText(activity, "金额不合法", Toast.LENGTH_LONG).show();
            return;
        }

        com.yyh.sdk.PayParams p = new com.yyh.sdk.PayParams();
        p.waresId = Integer.valueOf(waresId);
        p.waresName = params.getProductName();
        p.cpOrderId = params.getOrderID();
        p.cpPrivateInfo = params.getExtension();
        p.price = iPrice;
        p.notifyUrl = callBackUrl;

        TDGAVirtualCurrency.onChargeRequest(params.getOrderID(), params.getProductName(), params.getPrice(), "CNY", params.getPrice() * 7, "yyh SDK");

        YYHSDKAPI.startPay(activity, p, new PayResultCallback() {

            @Override
            public void onPayCanceled() {
                Log.e(TAG, "支付取消");
                U8SDK.getInstance().onResult(U8Code.CODE_PAY_CANCEL, "pay canceled");
            }

            @Override
            public void onPayFailed(int resultCode, String resultInfo) {
                Log.e(TAG, "支付失败 -> " + resultCode + " - " + resultInfo);
                U8SDK.getInstance().onResult(U8Code.CODE_PAY_FAIL, resultInfo);
            }

            @Override
            public void onPaySuccess() {
                TDGAVirtualCurrency.onChargeSuccess(params.getOrderID());

                U8SDK.getInstance().onResult(U8Code.CODE_PAY_SUCCESS, "pay success");
            }
        });
    }

    private GoodInfo getGoodInfo(String productID) {
        if (this.goods.containsKey(productID)) {
            return this.goods.get(productID);
        }
        return null;
    }

    private void loadPayGoods() {
        String xmlGoods = SDKTools.getAssetConfigs(activity, "appchina_pay.xml");

        if (xmlGoods == null) {
            Log.e(TAG, "fail to load appchina_pay.xml");
            return;
        }

        Log.e(TAG, "The appchina_pay Str:" + xmlGoods);

        XmlPullParser parser = Xml.newPullParser();

        try {
            parser.setInput(new StringReader(xmlGoods));

            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {

                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        String tag = parser.getName();
                        if ("good".equals(tag)) {
                            String productId = parser.getAttributeValue(0);
                            String wareid = parser.getAttributeValue(1);
                            boolean openPrice = Boolean.parseBoolean(parser.getAttributeValue(2));

                            GoodInfo good = new GoodInfo(productId, wareid, openPrice);

                            if (!this.goods.containsKey(productId)) {
                                this.goods.put(productId, good);
                            } else {
                                Log.e(TAG, "Curr Good: " + productId + " has duplicated.");
                            }

                            Log.d(TAG, "Curr Good: " + productId + "; waresid:" + wareid + ";openPrice:" + openPrice);
                        }
                }
                eventType = parser.next();
            }

        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
