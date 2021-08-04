package com.u8.sdk;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.nj9you.sdk.Nj9youSdk;
import com.nj9you.sdk.ad.TTAdCallback;
import com.nj9you.sdk.ad.TTAdClient;
import com.nj9you.sdk.listener.OnUserExchangeListener;
import com.nj9you.sdk.listener.OnUserLoginListener;
import com.nj9you.sdk.listener.OnUserLogoutListener;
import com.nj9you.sdk.listener.PayCallback;
import com.nj9you.sdk.talkingdata.TalkingdataSDK;
import com.tendcloud.tenddata.TDGAProfile;
import com.u8.common.PromptDialog;

import org.json.JSONException;
import org.json.JSONObject;

public class MoyoiSDK {
    public static final String TAG = "moyoisdk";
    private Activity activity;
    private static MoyoiSDK instance;

    private String sdk_Appid;
    private String cloud_channel;

    private boolean isShowTTAd;
    private String ttAd_posId;

    private MoyoiSDK() {
        activity = U8SDK.getInstance().getContext();
    }

    public static MoyoiSDK getInstance() {
        if (instance == null) {
            instance = new MoyoiSDK();
        }
        return instance;
    }

    private void parseSDKParams(SDKParams params) {
        sdk_Appid = params.getString("sdk_Appid");
        cloud_channel = params.getString("cloud_channel");
        isShowTTAd = params.getBoolean("isShowTTAd");
        if (isShowTTAd) {
            ttAd_posId = params.getString("ttAd_posId");
        }
    }

    public void initSDK(SDKParams params) {
        parseSDKParams(params);

        PromptDialog.init(activity, new PromptDialog.OnPromptListener() {
            @Override
            public void onInitSDK() {
                initSDK();
            }
        });
    }

    public void initSDK() {
        Nj9youSdk.init(activity, sdk_Appid, cloud_channel);
        U8SDK.getInstance().onResult(U8Code.CODE_INIT_SUCCESS, "init success");
        Log.i(TAG, "init success");

        Nj9youSdk.checkFloatPermission();
        Nj9youSdk.checkUpdate(activity);

        // 初始化talkingdata,使用设备号初始化
        TalkingdataSDK.getInstance().init(activity);

        if (isShowTTAd) {
            //穿山甲广告
            TTAdClient.getInstance().init(activity, ttAd_posId, new TTAdCallback() {
                @Override
                public void onAdLoaded() {
                    Log.i(TAG, "onAdLoaded");
                    U8SDK.getInstance().onResult(U8Code.CODE_ADS_LOADED, "广告加载完成，开始播放");
                }

                @Override
                public void onAdClicked() {
                    Log.i(TAG, "onAdClicked");
                    U8SDK.getInstance().onResult(U8Code.CODE_ADS_CLICKED, "广告被点击");
                }

                @Override
                public void onAdShow() {
                    Log.i(TAG, "onAdShow");
                    U8SDK.getInstance().onResult(U8Code.CODE_ADS_SHOW, "开始播放广告");
                }

                @Override
                public void onAdComplete() {
                    Log.i(TAG, "onAdComplete");
                }

                @Override
                public void onRewardVerify(boolean rewardVerify, int rewardAmount, String rewardName) {
                    Log.i(TAG, "onRewardVerify");
                    U8SDK.getInstance().onResult(U8Code.CODE_ADS_COMPLETE, "广告播放完成");
                }

                @Override
                public void onAdClosed() {
                    Log.i(TAG, "onAdClosed");
                    U8SDK.getInstance().onResult(U8Code.CODE_ADS_CLOSED, "广告被关闭");
                }

                @Override
                public void onAdFailed() {
                    Log.e(TAG, "onAdFailed");
                    U8SDK.getInstance().onResult(U8Code.CODE_ADS_FAILED, "广告加载或播放失败");
                }
            });
        }

        U8SDK.getInstance().setActivityCallback(new ActivityCallbackAdapter() {
            @Override
            public void onResume() {
                Nj9youSdk.showUserCenter();
                Nj9youSdk.onResume();
            }

            @Override
            public void onPause() {
                Nj9youSdk.hideUserCenter();
                Nj9youSdk.onPause();
            }

            @Override
            public void onDestroy() {
                Nj9youSdk.destory();
            }

            @Override
            public void onActivityResult(int requestCode, int resultCode, Intent data) {
                Nj9youSdk.onActivityResult(requestCode, resultCode, data);
            }
        });

        Nj9youSdk.setExchangeAccountListener(new OnUserExchangeListener() {
            @Override
            public void onExchangeComplete(boolean success, String loginInfo) {
                if (success) {
                    try {
                        Log.i(TAG, "loginInfo is " + loginInfo);

                        JSONObject info = new JSONObject(loginInfo);
                        String userId = info.getString("id");
                        String userName = info.getString("username");
                        String userKey = info.getString("userKey");

                        String loginResult = encodeLoginResult(userId, userName, userKey);
                        U8SDK.getInstance().onLoginResult(loginResult);
                        U8SDK.getInstance().onResult(U8Code.CODE_LOGIN_SUCCESS, "switch success");
                    } catch (JSONException e) {
                        U8SDK.getInstance().onResult(U8Code.CODE_LOGIN_FAIL, "switch fail");
                    }
                } else {
                    U8SDK.getInstance().onResult(U8Code.CODE_LOGIN_FAIL, "switch fail");
                }
            }
        });

        Nj9youSdk.setLoginAccountListener(new OnUserLoginListener() {
            @Override
            public void onLoginComplete(boolean success, String loginInfo) {
                if (success) {
                    try {
                        Log.i(TAG, "loginInfo is " + loginInfo);

                        JSONObject info = new JSONObject(loginInfo);
                        String userId = info.getString("id");
                        String userName = info.getString("username");
                        String userKey = info.getString("userKey");

                        String loginResult = encodeLoginResult(userId, userName, userKey);
                        U8SDK.getInstance().onLoginResult(loginResult);
                        U8SDK.getInstance().onResult(U8Code.CODE_LOGIN_SUCCESS, "login success");
                    } catch (JSONException e) {
                        U8SDK.getInstance().onResult(U8Code.CODE_LOGIN_FAIL, "login fail");
                    }
                } else {
                    U8SDK.getInstance().onResult(U8Code.CODE_LOGIN_FAIL, "login fail");
                }

                Nj9youSdk.showUserCenter();
            }
        });

        Nj9youSdk.setLogoutListener(new OnUserLogoutListener() {
            @Override
            public void onLogoutComplete(boolean success, String username) {
                if (success) {
                    U8SDK.getInstance().onLogout();
                    U8SDK.getInstance().onResult(U8Code.CODE_LOGOUT_SUCCESS, "logout success" + username);
                } else {
                    U8SDK.getInstance().onResult(U8Code.CODE_LOGOUT_FAIL, "logout fail");
                }
            }
        });

    }

    public void login() {
        Nj9youSdk.login(activity);
    }

    private String encodeLoginResult(String userId, String userName, String userKey) {
        JSONObject json = new JSONObject();
        try {
            json.put("userId", userId);
            json.put("userName", userName);
            json.put("userKey", userKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return json.toString();
    }

    public void logout() {
        Nj9youSdk.logout(activity);
    }

    public void switchLogin() {
        Nj9youSdk.exchange(activity);
    }

    public void submitGameData(UserExtraData extraData) {
        switch (extraData.getDataType()) {
            case UserExtraData.TYPE_ENTER_GAME:
                TalkingdataSDK.getInstance().setProfileType(TDGAProfile.ProfileType.ANONYMOUS);
                TalkingdataSDK.getInstance().setLevel(Integer.parseInt(extraData.getRoleLevel()));
                TalkingdataSDK.getInstance().setGameServer(extraData.getServerID() + "");
                TalkingdataSDK.getInstance().setProfileName(extraData.getRoleName());
                break;
            case UserExtraData.TYPE_LEVEL_UP:
                TalkingdataSDK.getInstance().setLevel(Integer.parseInt(extraData.getRoleLevel()));
                break;
        }
    }

    public void pay(final PayParams params) {
        com.nj9you.sdk.params.PayParams njParams = getParams(params);

        TalkingdataSDK.getInstance().onChargeRequest(params.getOrderID(), params.getProductName(), params.getPrice(), "CNY", params.getPrice() * 7,"moyoi SDK");

        Nj9youSdk.pay(activity, njParams, new PayCallback() {
            @Override
            public void onPayResult(int error, String desc, com.nj9you.sdk.params.PayParams njParams) {
                Log.i(TAG, "支付结果->error: " + error + ",desc:" + desc);

                if (error == com.nj9you.sdk.framework.PayResult.PAY_SUCCESS) {
                    TalkingdataSDK.getInstance().onChargeSuccess(params.getOrderID());

                    U8SDK.getInstance().onResult(U8Code.CODE_PAY_SUCCESS, "pay success" + desc);
                } else if (error == com.nj9you.sdk.framework.PayResult.PAY_FAILURE) {
                    U8SDK.getInstance().onResult(U8Code.CODE_PAY_FAIL, "pay failed" + desc);
                } else if (error == com.nj9you.sdk.framework.PayResult.PAY_CANCEL) {
                    U8SDK.getInstance().onResult(U8Code.CODE_PAY_CANCEL, "pay failed" + desc);
                } else if (error == com.nj9you.sdk.framework.PayResult.PAY_WAITING) {
                    U8SDK.getInstance().onResult(U8Code.CODE_PAY_UNKNOWN, "pay waiting" + desc);
                } else if (error == com.nj9you.sdk.framework.PayResult.PAY_ONGOING) {
                    U8SDK.getInstance().onResult(U8Code.CODE_PAY_FAIL, "pay failed" + desc);
                }
            }
        });
    }

    public com.nj9you.sdk.params.PayParams getParams(PayParams params) {
        Log.i(TAG, "params" + params.toString());

        com.nj9you.sdk.params.PayParams payParams = new com.nj9you.sdk.params.PayParams();
        payParams.setSubject(params.getProductName());
        payParams.setPrice((params.getPrice() * 100) + "");
        payParams.setAttach(params.getExtension());
        payParams.setOrder(params.getOrderID());
        payParams.setBody(params.getProductDesc());
        payParams.setServer(params.getServerId());
        payParams.setItemid(params.getProductId());

        return payParams;
    }

    public void showTTAd(String posId) {
        if (!isShowTTAd) return;

        TTAdClient.getInstance().requestPermission(activity);

        //播放视频广告
        TTAdClient.getInstance().show(activity);
    }
}
