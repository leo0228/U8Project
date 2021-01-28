package com.u8.sdk;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.anzhi.sdk.middle.manage.AnzhiSDK;
import com.anzhi.sdk.middle.manage.GameCallBack;
import com.anzhi.sdk.middle.util.MD5;
import com.tendcloud.tenddata.TDGAProfile;
import com.tendcloud.tenddata.TDGAVirtualCurrency;
import com.u8.sdk.anzhi.Des3Util;
import com.zjtx.prompt.PromptDialog;

import org.json.JSONException;
import org.json.JSONObject;


public class AnZhiSDK {

    private Activity activity;
    private String TAG = "AnZhiSDK";
    private static AnZhiSDK instance;
    private String appKey;
    private String appSecret;
    private String gameName;

    private AnzhiSDK mAnzhiCenter;

    public AnZhiSDK() {
        activity = U8SDK.getInstance().getContext();
    }

    public static AnZhiSDK getInstance() {
        if (instance == null) {
            instance = new AnZhiSDK();
        }
        return instance;
    }

    private void parseSDKParams(SDKParams params) {
        this.appKey = params.getString("AnZhi_AppKey");
        this.appSecret = params.getString("AnZhi_Secret");
        this.gameName = params.getString("AnZhi_GameName");
        Log.d(TAG, "gameName:" + gameName + "--appKey:" + appKey);
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

    private void initSDK() {

        mAnzhiCenter = AnzhiSDK.getInstance();

        U8SDK.getInstance().setActivityCallback(new ActivityCallbackAdapter() {
            @Override
            public void onActivityResult(int requestCode, int resultCode, Intent data) {
                mAnzhiCenter.onActivityResultInvoked(requestCode, resultCode, data);
            }

            @Override
            public void onPause() {
                mAnzhiCenter.onPauseInvoked();
            }

            @Override
            public void onResume() {
                mAnzhiCenter.onResumeInvoked();

            }

            @Override
            public void onNewIntent(Intent newIntent) {
                mAnzhiCenter.onNewIntentInvoked(newIntent);
            }

            @Override
            public void onStop() {
                mAnzhiCenter.onStopInvoked();

            }

            @Override
            public void onDestroy() {
                mAnzhiCenter.onDestoryInvoked();

            }

            @Override
            public void onStart() {
                mAnzhiCenter.onStartInvoked();

            }
        });

        mAnzhiCenter.init(activity, appKey, appSecret, new GameCallBack() {

            @Override
            public void callBack(int code, String data) {
                android.util.Log.i(TAG, "code:" + code + "--data:" + data);
                switch (code) {
                    case SDK_TYPE_INIT:
                        U8SDK.getInstance().onResult(U8Code.CODE_INIT_SUCCESS, "init success");
                        android.util.Log.i(TAG, "init success");
                        break;
                    case SDK_TYPE_LOGIN:
                        android.util.Log.i(TAG, "login");
                        try {
                            JSONObject json = new JSONObject(data);
                            android.util.Log.i(TAG, json.toString());
                            if (json.getInt("code") == 200) {
                                String cptoken = json.getString("cptoken");
                                String deviceId = json.getString("deviceId");
                                String requestUrl = json.getString("requestUrl");
                                mAnzhiCenter.addPop(activity);

                                String loginResult = encodeLoginResult(cptoken, deviceId, requestUrl);
                                android.util.Log.i(TAG, loginResult);

                                U8SDK.getInstance().onLoginResult(loginResult);
                                U8SDK.getInstance().onResult(U8Code.CODE_LOGIN_SUCCESS, cptoken);
                            } else {
                                U8SDK.getInstance().onResult(U8Code.CODE_LOGIN_FAIL, "login failed.code:" + code);
                                android.util.Log.i(TAG, "login failed:" + code);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            android.util.Log.i(TAG, "error");
                            U8SDK.getInstance().onResult(U8Code.CODE_LOGIN_FAIL, "login failed. exception:" + e.toString());
                        }

                        break;
                    case SDK_TYPE_LOGOUT:
                        android.util.Log.d(TAG, "登出账号");

                        U8SDK.getInstance().onLogout();
                        U8SDK.getInstance().onResult(U8Code.CODE_LOGOUT_SUCCESS, "logout success");
                        break;
                    case SDK_TYPE_EXIT_GAME:
                        android.util.Log.d(TAG, "退出游戏");
                        try {
                            JSONObject json = new JSONObject(data);
                            boolean kill = json.optBoolean("killSelf");
                            if (kill) {
                                android.os.Process.killProcess(android.os.Process.myPid());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            U8SDK.getInstance().onLogout();
                            U8SDK.getInstance().onResult(U8Code.CODE_LOGOUT_SUCCESS, "logout success");
                            activity.finish();
                            System.exit(0);
                        }

                        break;
                    case SDK_TYPE_PAY:
                        android.util.Log.d(TAG, "支付");
                        try {
                            JSONObject json = new JSONObject(data);
                            int status = json.optInt("payStatus"); // 支付状态
                            if (status == 1) { // 支付成功
                                TDGAVirtualCurrency.onChargeSuccess(orderId);
                                U8SDK.getInstance().onResult(U8Code.CODE_PAY_SUCCESS, "pay success");
                            } else if (status == 2) { // 支付中状态
                                U8SDK.getInstance().onResult(U8Code.CODE_PAYING, "paying");
                            } else { // 支付失败
                                U8SDK.getInstance().onResult(U8Code.CODE_PAY_FAIL, "pay failed");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            U8SDK.getInstance().onResult(U8Code.CODE_PAY_UNKNOWN, "unkown error");
                        }
                        break;
                    case SDK_TYPE_CANCEL_PAY:
                        android.util.Log.d(TAG, "取消支付");
                        U8SDK.getInstance().onResult(U8Code.CODE_PAY_CANCEL, "pay canneled");
                        break;
                    case SDK_TYPE_CANCEL_LOGIN:
                        android.util.Log.d(TAG, "取消登录");
                        U8SDK.getInstance().onResult(U8Code.CODE_LOGIN_FAIL, "login canceled");
                        break;
                    case SDK_TYPE_CANCEL_EXIT_GAME:
                        android.util.Log.d(TAG, "取消退出");
                        // 取消退出游戏操作事件通知码
                        break;
                }
            }
        });

    }

    private String encodeLoginResult(String cptoken, String deviceId, String requestUrl) {

        JSONObject json = new JSONObject();
        try {
            json.put("cptoken", cptoken);
            json.put("deviceId", deviceId);
            json.put("requestUrl", requestUrl);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return json.toString();
    }

    public void login() {
        mAnzhiCenter.login(activity);// 用户中心登录的方法，
    }

    public void logout() {
        mAnzhiCenter.logout();
    }

    public void submitExtraData(UserExtraData extraData) {

        try {
            JSONObject gameInfoJson = new JSONObject();
            gameInfoJson.put(AnzhiSDK.GAME_AREA, extraData.getServerName());
            gameInfoJson.put("gameAreaId", extraData.getServerID()); // 角色所在服务区编码
            gameInfoJson.put(AnzhiSDK.GAME_LEVEL, extraData.getRoleLevel());
            gameInfoJson.put(AnzhiSDK.ROLE_ID, extraData.getRoleID());
            gameInfoJson.put(AnzhiSDK.USER_ROLE, extraData.getRoleName());
            mAnzhiCenter.subGameInfo(gameInfoJson.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }


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

    public void sdkExit() {
        mAnzhiCenter.exitGame(activity);
    }

    private String orderId;

    public void pay(PayParams params) {
        orderId = params.getOrderID();
        int amount = params.getPrice() * 100;
        String productId = params.getProductId();
        String productName = params.getProductName();
        try {
            JSONObject json = new JSONObject();
            json.put("cpOrderId", orderId);
            json.put("cpOrderTime", System.currentTimeMillis());
            json.put("amount", amount);
            json.put("productCode", productId);
            json.put("cpCustomInfo", orderId);
            json.put("productName", productName);

            TDGAVirtualCurrency.onChargeRequest(orderId, productName, params.getPrice(), "CNY", params.getPrice() * 7, "anzhi SDK");
            mAnzhiCenter.pay(Des3Util.encrypt(json.toString(), appSecret), MD5.encodeToString(appSecret));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
