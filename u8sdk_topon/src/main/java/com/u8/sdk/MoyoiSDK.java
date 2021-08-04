package com.u8.sdk;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.anythink.china.api.ATChinaSDKHandler;
import com.anythink.core.api.ATAdInfo;
import com.anythink.core.api.AdError;
import com.anythink.rewardvideo.api.ATRewardVideoAd;
import com.anythink.rewardvideo.api.ATRewardVideoListener;
import com.nj9you.sdk.Nj9youSdk;
import com.nj9you.sdk.listener.OnUserExchangeListener;
import com.nj9you.sdk.listener.OnUserLoginListener;
import com.nj9you.sdk.listener.OnUserLogoutListener;
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

    private ATRewardVideoAd mRewardVideoAd;
    private String ad_video_posId;

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

        ad_video_posId = params.getString("ad_video_posId");

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

        ATChinaSDKHandler atChinaSDKHandler = new ATChinaSDKHandler();
        atChinaSDKHandler.requestPermissionIfNecessary(activity);

        mRewardVideoAd = new ATRewardVideoAd(activity, ad_video_posId);
        mRewardVideoAd.setAdListener(new ATRewardVideoListener() {
            @Override
            public void onRewardedVideoAdLoaded() {
                Log.i(TAG, "onRewardedVideoAdLoaded");
                U8SDK.getInstance().onResult(U8Code.CODE_ADS_LOADED, "广告加载完成，开始播放");
            }

            @Override
            public void onRewardedVideoAdFailed(AdError adError) {
                Log.e(TAG, "onRewardedVideoAdFailed:" + adError.getFullErrorInfo());
                U8SDK.getInstance().onResult(U8Code.CODE_ADS_FAILED, "广告加载或播放失败");
            }

            @Override
            public void onRewardedVideoAdPlayStart(ATAdInfo atAdInfo) {
                Log.i(TAG, "onRewardedVideoAdPlayStart");
                U8SDK.getInstance().onResult(U8Code.CODE_ADS_SHOW, "开始播放广告");
            }

            @Override
            public void onRewardedVideoAdPlayEnd(ATAdInfo atAdInfo) {
                Log.i(TAG, "onRewardedVideoAdPlayEnd");
            }

            @Override
            public void onRewardedVideoAdPlayFailed(AdError adError, ATAdInfo atAdInfo) {
                Log.e(TAG, "onRewardedVideoAdPlayFailed:" + adError.getFullErrorInfo());
                U8SDK.getInstance().onResult(U8Code.CODE_ADS_FAILED, "广告加载或播放失败");
            }

            @Override
            public void onRewardedVideoAdClosed(ATAdInfo atAdInfo) {
                Log.i(TAG, "onRewardedVideoAdClosed");
                U8SDK.getInstance().onResult(U8Code.CODE_ADS_CLOSED, "广告被关闭");

                //建议在此回调中调用load进行广告的加载，方便下一次广告的展示（不需要调用isAdReady()）
                mRewardVideoAd.load();
            }

            @Override
            public void onRewardedVideoAdPlayClicked(ATAdInfo atAdInfo) {
                Log.i(TAG, "onRewardedVideoAdPlayClicked");
                U8SDK.getInstance().onResult(U8Code.CODE_ADS_CLICKED, "广告被点击");
            }

            @Override
            public void onReward(ATAdInfo atAdInfo) {
                Log.i(TAG, "onReward");
                U8SDK.getInstance().onResult(U8Code.CODE_ADS_COMPLETE, "广告播放完成");
            }
        });
        mRewardVideoAd.load();

        U8SDK.getInstance().setActivityCallback(new ActivityCallbackAdapter() {
            @Override
            public void onResume() {
                Nj9youSdk.onResume();
            }

            @Override
            public void onPause() {
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
                loginResult(success, loginInfo);
            }
        });

        Nj9youSdk.setLoginAccountListener(new OnUserLoginListener() {
            @Override
            public void onLoginComplete(boolean success, String loginInfo) {
                loginResult(success, loginInfo);

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

    private void loginResult(boolean success, String loginInfo) {
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

    public void showTopOnAd(String posId) {
        if (mRewardVideoAd != null) {
            if (mRewardVideoAd.isAdReady()) {
                mRewardVideoAd.show(activity);
            } else {
                mRewardVideoAd.load();
            }
        }
    }
}
