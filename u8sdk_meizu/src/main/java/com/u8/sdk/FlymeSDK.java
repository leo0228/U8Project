package com.u8.sdk;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.meizu.gamesdk.model.callback.MzExitListener;
import com.meizu.gamesdk.model.callback.MzLoginListener;
import com.meizu.gamesdk.model.callback.MzPayListener;
import com.meizu.gamesdk.model.model.LoginResultCode;
import com.meizu.gamesdk.model.model.MzAccountInfo;
import com.meizu.gamesdk.model.model.MzBuyInfo;
import com.meizu.gamesdk.model.model.PayResultCode;
import com.meizu.gamesdk.online.core.MzGameBarPlatform;
import com.meizu.gamesdk.online.core.MzGameCenterPlatform;
import com.tendcloud.tenddata.TDGAProfile;
import com.tendcloud.tenddata.TDGAVirtualCurrency;
import com.u8.sdk.utils.EncryptUtils;
import com.u8.common.PromptDialog;

import org.json.JSONObject;

public class FlymeSDK {

    private Activity activity;
    private String TAG = "FlymeSDK";
    private static FlymeSDK instance;

    private String appID;
    private String appKey;
    private String appSecret;
    private String pushAppid;
    private String pushAppkey;

    private String uid;

    private MzGameBarPlatform mBar;

    private FlymeSDK() {
        activity = U8SDK.getInstance().getContext();
    }

    public static FlymeSDK getInstance() {
        if (instance == null) {
            instance = new FlymeSDK();
        }
        return instance;
    }

    public void initSDK(SDKParams params) {
        this.parseSDKParams(params);
        PromptDialog.init(activity, new PromptDialog.OnPromptListener() {
            @Override
            public void onInitSDK() {
                initSDK();
            }
        });
    }

    private void parseSDKParams(SDKParams params) {
        this.appID = params.getString("Flyme_AppID");
        this.appKey = params.getString("Flyme_AppKey");
        this.appSecret = params.getString("Flyme_AppSecret");
        this.pushAppid = params.getString("MEIZU_APP_ID");
        this.pushAppkey = params.getString("MEIZU_APP_KEY");

    }

    private void initSDK() {
        Log.e(TAG, "Init Begin....");

        onActivityCreate();

        U8SDK.getInstance().setActivityCallback(new ActivityCallbackAdapter() {

            @Override
            public void onPause() {
                if (mBar != null) {
                    mBar.onActivityPause();
                }
            }

            @Override
            public void onResume() {
                if (mBar != null) {
                    mBar.onActivityResume();
                }
            }

            @Override
            public void onDestroy() {
                logout();
                if (mBar != null) {
                    mBar.onActivityDestroy();
                }
            }

        });

        Log.d(TAG, "appID:" + appID);
        Log.d(TAG, "appKey:" + appKey);

        MzGameCenterPlatform.init(activity, this.appID, this.appKey);


    }

    private void onActivityCreate() {
        mBar = new MzGameBarPlatform(activity);
        mBar.onActivityCreate();
        U8SDK.getInstance().onResult(U8Code.CODE_INIT_SUCCESS, "init success");
    }


    private String encodeLoginResult(String uid, String session) {
        JSONObject json = new JSONObject();
        try {
            json.put("uid", uid);
            json.put("session", session);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return json.toString();
    }

    public void sendExtraData(UserExtraData extraData) {
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

    private void upgrade(UserExtraData extraData){
        TDGAProfile profile = TDGAProfile.setProfile(extraData.getRoleID());
        //玩家升级时，做如下调用
        profile.setLevel(Integer.parseInt(extraData.getRoleLevel()));
    }

    public void login() {

        if (!SDKTools.isNetworkAvailable(activity)) {
            U8SDK.getInstance().onResult(U8Code.CODE_NO_NETWORK, "The network now is unavailable");
            return;
        }

        MzGameCenterPlatform.login(activity, new MzLoginListener() {

            @Override
            public void onLoginResult(int code, MzAccountInfo accountInfo, String errorMsg) {

                Log.d(TAG, "code:" + code + ";msg:" + errorMsg);

                switch (code) {
                    case LoginResultCode.LOGIN_SUCCESS:

                        String uid = accountInfo.getUid();

                        FlymeSDK.this.uid = accountInfo.getUid();
                        String session = accountInfo.getSession();

                        U8SDK.getInstance().onResult(U8Code.CODE_LOGIN_SUCCESS, uid);

                        String loginResult = encodeLoginResult(uid, session);
                        U8SDK.getInstance().onLoginResult(loginResult);

                        break;
                    case LoginResultCode.LOGIN_LOGOUT:
                        U8SDK.getInstance().onLogout();
                        U8SDK.getInstance().onResult(U8Code.CODE_LOGOUT_SUCCESS, "logout success");
                        break;
                    case LoginResultCode.LOGIN_ERROR_CANCEL:

                        U8SDK.getInstance().onResult(U8Code.CODE_LOGIN_FAIL, "login fail");
                        break;
                    default:


                        U8SDK.getInstance().onResult(U8Code.CODE_LOGIN_FAIL, errorMsg);
                        break;
                }
            }
        });

    }

    public void exitSDK() {
        MzGameCenterPlatform.exitSDK(activity, new MzExitListener() {
            public void callback(int code, String msg) {
                if (code == MzExitListener.CODE_SDK_EXIT) {
                    //TODO 在这里处理退出逻辑
                    activity.finish();
                    System.exit(0);
                } else if (code == MzExitListener.CODE_SDK_CONTINUE) {
                    //TODO 继续游戏
                }

            }
        });
    }


    private MzBuyInfo encodeBuyInfo(PayParams data) {

        String orderId = data.getOrderID();
        String productID = data.getProductId();
        String productSubject = data.getProductName();
        String productBody = "";
        String productUnit = "";
        String buyNum = "1";
        String perPrice = data.getPrice() + "";
        ;//
        String totalPrice = data.getPrice() + "";
        ;//data.getPrice() + "";
        String createTime = System.currentTimeMillis() + "";
        String pay_type = "0";
        String userInfo = data.getExtension();

        StringBuilder sb = new StringBuilder();
        sb.append("app_id=").append(this.appID).append("&")
                .append("buy_amount=").append(buyNum).append("&")
                .append("cp_order_id=").append(orderId).append("&")
                .append("create_time=").append(createTime).append("&")
                .append("pay_type=").append(pay_type).append("&")
                .append("product_body=").append(productBody).append("&")
                .append("product_id=").append(productID).append("&")
                .append("product_per_price=").append(perPrice).append("&")
                .append("product_subject=").append(productSubject).append("&")
                .append("product_unit=").append(productUnit).append("&")
                .append("total_price=").append(totalPrice).append("&")
                .append("uid=").append(this.uid).append("&")
                .append("user_info=").append(userInfo)
                .append(":").append(this.appSecret);


        String sign = EncryptUtils.md5(sb.toString());
        String signType = "md5";

        MzBuyInfo buyInfo = new MzBuyInfo();
        buyInfo.setAppid(this.appID);
        buyInfo.setBuyCount(Integer.valueOf(buyNum));
        buyInfo.setCpUserInfo(userInfo);
        buyInfo.setCreateTime(Long.valueOf(createTime));
        buyInfo.setOrderAmount(totalPrice);
        buyInfo.setOrderId(orderId);
        buyInfo.setPayType(Integer.valueOf(pay_type));
        buyInfo.setPerPrice(perPrice);//
        buyInfo.setProductBody(productBody);
        buyInfo.setProductId(productID);
        buyInfo.setProductSubject(productSubject);
        buyInfo.setProductUnit(productUnit);
        buyInfo.setSign(sign);
        buyInfo.setSignType(signType);
        buyInfo.setUserUid(this.uid);

        return buyInfo;
    }

    public void pay(final PayParams params) {

        if (!SDKTools.isNetworkAvailable(activity)) {
            U8SDK.getInstance().onResult(U8Code.CODE_NO_NETWORK, "The network now is unavailable");
            return;
        }

        MzBuyInfo buyInfo = encodeBuyInfo(params);

        TDGAVirtualCurrency.onChargeRequest(params.getOrderID(), params.getProductName(), params.getPrice(), "CNY", params.getPrice() * 7,"meizu SDK");

        MzGameCenterPlatform.payOnline(activity, buyInfo.toBundle(), new MzPayListener() {

            @Override
            public void onPayResult(int code, Bundle arg1, String errorMsg) {
                Log.e(TAG, "code:" + code + " || arg1: " + arg1 + " || errorMsg: " + errorMsg);

                switch (code) {
                    case PayResultCode.PAY_SUCCESS:
                        TDGAVirtualCurrency.onChargeSuccess(params.getOrderID());

                        U8SDK.getInstance().onResult(U8Code.CODE_PAY_SUCCESS, "pay success");
                        break;
                    case PayResultCode.PAY_ERROR_CANCEL:
                        U8SDK.getInstance().onResult(U8Code.CODE_PAY_CANCEL, "pay cannel");
                        break;

                    default:
                        U8SDK.getInstance().onResult(U8Code.CODE_PAY_FAIL, errorMsg);
                        break;
                }
            }
        });


    }

    public void logout() {

        MzGameCenterPlatform.logout(activity, new MzLoginListener() {

            @Override
            public void onLoginResult(int code, MzAccountInfo arg1, String arg2) {
                switch (code) {

                    case LoginResultCode.LOGIN_LOGOUT:
                        U8SDK.getInstance().onResult(U8Code.CODE_LOGOUT_SUCCESS, "logout success");
                        U8SDK.getInstance().onLogout();
                        break;

                }
            }
        });
    }
}
