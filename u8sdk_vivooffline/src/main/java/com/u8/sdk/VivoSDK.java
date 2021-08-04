package com.u8.sdk;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.tendcloud.tenddata.TDGAProfile;
import com.tendcloud.tenddata.TDGAVirtualCurrency;
import com.vivo.ad.video.VideoAdListener;
import com.vivo.mobilead.interstitial.InterstitialAdParams;
import com.vivo.mobilead.interstitial.VivoInterstitialAd;
import com.vivo.mobilead.listener.IAdListener;
import com.vivo.mobilead.model.BackUrlInfo;
import com.vivo.mobilead.model.VivoAdError;
import com.vivo.mobilead.video.VideoAdParams;
import com.vivo.mobilead.video.VivoVideoAd;
import com.vivo.unionsdk.open.VivoAccountCallback;
import com.vivo.unionsdk.open.VivoConstants;
import com.vivo.unionsdk.open.VivoExitCallback;
import com.vivo.unionsdk.open.VivoPayCallback;
import com.vivo.unionsdk.open.VivoPayInfo;
import com.vivo.unionsdk.open.VivoRealNameInfoCallback;
import com.vivo.unionsdk.open.VivoRoleInfo;
import com.vivo.unionsdk.open.VivoUnionSDK;
import com.u8.common.PromptDialog;

import org.json.JSONException;
import org.json.JSONObject;

public class VivoSDK {

    public static final String TAG = "vivoSdk";
    private static VivoSDK instance;

    private String appID;
    private boolean debug;

    private VivoVideoAd mVivoVideoAd;
    private VideoAdListener mVideoAdListener;

    private VivoInterstitialAd mVivoInterstitialAd;
    private IAdListener mIAdListener;

    private VivoNativeAdPopupWindow mAdPopupWindow;

    private boolean isShowVivoAd;
    private String ad_video_posId;
    private String ad_interstitial_posId;
    private String ad_native_posId;

    private Activity activity;
    private String myOpenId;

    private VivoSDK() {
        activity = U8SDK.getInstance().getContext();
    }

    public static VivoSDK getInstance() {
        if (instance == null) {
            instance = new VivoSDK();
        }
        return instance;
    }

    private void parseSDKParams(SDKParams params) {
        appID = params.getString("AppID");
        debug = params.getBoolean("debug_mode");

        isShowVivoAd = params.getBoolean("isShowVivoAd");
        if (isShowVivoAd) {
            ad_video_posId = params.getString("ad_video_posId");
            ad_interstitial_posId = params.getString("ad_interstitial_posId");
            ad_native_posId = params.getString("ad_native_posId");
        }
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
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(activity,
                    Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
            }
        }

        VivoUnionSDK.registerAccountCallback(activity, new VivoAccountCallback() {

            @Override
            public void onVivoAccountLogout(int requestCode) {
                U8SDK.getInstance().onResult(U8Code.CODE_LOGOUT_SUCCESS, "logout " + requestCode);
            }

            @Override
            public void onVivoAccountLoginCancel() {
                U8SDK.getInstance().onResult(U8Code.CODE_LOGIN_FAIL, "login cancel");
            }

            @Override
            public void onVivoAccountLogin(String userName, String openId, String authToken) {
                myOpenId = openId;

                String loginResult = encodeLoginResult(userName, openId, authToken);
                showLog("loginResult:" + loginResult);
                U8SDK.getInstance().onLoginResult(loginResult);
                U8SDK.getInstance().onResult(U8Code.CODE_LOGIN_SUCCESS, "login success");

                realNameInfo();
            }
        });

        if (isShowVivoAd) {
            initAdListener();
        }
    }

    private void initAdListener() {
        mVideoAdListener = new VideoAdListener() {
            @Override
            public void onAdLoad() {
                Log.i(TAG, "onAdLoad");
                U8SDK.getInstance().onResult(U8Code.CODE_ADS_LOADED, "广告加载完成，开始播放");

                //播放视频广告
                if (mVivoVideoAd != null) {
                    mVivoVideoAd.showAd(activity);
                    mVivoVideoAd = null;
                }
            }

            @Override
            public void onAdFailed(String s) {
                Log.e(TAG, "onAdFailed == " + s);
                U8SDK.getInstance().onResult(U8Code.CODE_ADS_FAILED, "广告加载失败");
            }

            @Override
            public void onVideoStart() {
                Log.i(TAG, "onVideoStart");
                //视频广告开始播放
                U8SDK.getInstance().onResult(U8Code.CODE_ADS_SHOW, "开始播放广告");
            }

            @Override
            public void onVideoCompletion() {
                Log.i(TAG, "onVideoCompletion");
                //视频播放完成, 新版本 发放奖励 通知不再由此方法发送
            }

            @Override
            public void onVideoClose(int i) {
                Log.i(TAG, "onVideoClose == " + i);
                U8SDK.getInstance().onResult(U8Code.CODE_ADS_CLOSED, "视频关闭");
            }

            @Override
            public void onVideoCloseAfterComplete() {
                Log.i(TAG, "onVideoCloseAfterComplete");
                U8SDK.getInstance().onResult(U8Code.CODE_ADS_CLOSED, "视频关闭");
                U8SDK.getInstance().onResult(U8Code.CODE_ADS_COMPLETE, "视频播放完成回到游戏界面, 开始发放奖励!");
            }

            @Override
            public void onVideoError(String s) {
                Log.e(TAG, "onVideoError == " + s);
                U8SDK.getInstance().onResult(U8Code.CODE_ADS_FAILED, "视频播放错误");
            }

            @Override
            public void onFrequency() {
                Log.e(TAG, "onFrequency");
                U8SDK.getInstance().onResult(U8Code.CODE_ADS_FAILED, "频繁请求广告（一分钟内只能请求一次）");
            }

            @Override
            public void onNetError(String s) {
                Log.e(TAG, "onNetError == " + s);
                U8SDK.getInstance().onResult(U8Code.CODE_ADS_FAILED, "网络错误");
            }

            @Override
            public void onRequestLimit() {
                Log.e(TAG, "onRequestLimit");
                U8SDK.getInstance().onResult(U8Code.CODE_ADS_FAILED, "限制视频请求");
            }
        };

        mIAdListener = new IAdListener() {
            @Override
            public void onAdFailed(VivoAdError error) {
                Log.e(TAG, "onAdFailed" + error);
                U8SDK.getInstance().onResult(U8Code.CODE_ADS_FAILED, "广告加载失败");
            }

            @Override
            public void onAdClosed() {
                Log.i(TAG, "onAdClose");
                U8SDK.getInstance().onResult(U8Code.CODE_ADS_CLOSED, "广告被关闭");
                U8SDK.getInstance().onResult(U8Code.CODE_ADS_COMPLETE, "图片广告展示成功, 开始发放奖励!");
            }

            @Override
            public void onAdReady() {
                Log.i(TAG, "onAdReady");
                U8SDK.getInstance().onResult(U8Code.CODE_ADS_LOADED, "广告加载完成，开始展示");

                if (mVivoInterstitialAd != null) {
                    mVivoInterstitialAd.showAd();
                    mVivoInterstitialAd = null;
                }
            }

            @Override
            public void onAdClick() {
                Log.i(TAG, "onAdClick");
                U8SDK.getInstance().onResult(U8Code.CODE_ADS_CLICKED, "广告被点击了");
            }

            @Override
            public void onAdShow() {
                Log.i(TAG, "onAdShow");
                U8SDK.getInstance().onResult(U8Code.CODE_ADS_SHOW, "广告展示成功");
            }
        };
    }

    public void login() {
        //不允许开发者在游戏中通过此接口进行帐号切换
        VivoUnionSDK.login(activity);
    }

    private String encodeLoginResult(String userName, String openId, String authToken) {
        JSONObject json = new JSONObject();
        try {
            json.put("name", userName);
            json.put("openid", openId);
            json.put("token", authToken);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return json.toString();
    }


    public void logout() {
        U8SDK.getInstance().onLogout();
        U8SDK.getInstance().onResult(U8Code.CODE_LOGOUT_SUCCESS, "logout success");
    }

    /**
     * 防沉迷接口
     */
    private void realNameInfo() {
        VivoUnionSDK.getRealNameInfo(activity, new VivoRealNameInfoCallback() {
            @Override
            public void onGetRealNameInfoSucc(boolean isRealName, int age) {
                //isRealName是否已实名制，age为年龄信息，请根据这些信息进行防沉迷操作
                Log.i(TAG, "实名状态:" + isRealName + ",年龄:" + age);
            }

            @Override
            public void onGetRealNameInfoFailed() {
                //获取实名制信息失败，请自行处理是否防沉迷
                Log.e(TAG, "实名失败");
            }
        });
    }

    public void exit() {
        VivoUnionSDK.exit(activity, new VivoExitCallback() {

            @Override
            public void onExitConfirm() {
                activity.finish();
                System.exit(0);
            }

            @Override
            public void onExitCancel() {

            }
        });
    }

    public void submitGameData(UserExtraData extraData) {
        switch (extraData.getDataType()) {
            case UserExtraData.TYPE_ENTER_GAME:
                goInGame(extraData);
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

    // 进入游戏
    private void goInGame(UserExtraData extraData) {
        VivoUnionSDK.reportRoleInfo(new VivoRoleInfo(extraData.getRoleID(), extraData.getRoleLevel(),
                extraData.getRoleName(), extraData.getServerID() + "", extraData.getServerName()));
    }

    //打开论坛接口
    private void jumpTo() {
        VivoUnionSDK.jumpTo(VivoConstants.JumpType.FORUM);
    }

    public void pay(final PayParams params) {
        String sccesskey = "";
        String transNo = "";
        try {
            JSONObject obj = new JSONObject(params.getExtension());
            sccesskey = obj.getString("accessKey");
            transNo = obj.getString("transNo");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        String newPrice = params.getPrice() * 100 + "";

        VivoPayInfo.Builder builder = new VivoPayInfo.Builder()
                .setProductName(params.getProductName())
                .setProductDes(params.getProductDesc())
                .setProductPrice(newPrice)
                .setVivoSignature(sccesskey)
                .setAppId(this.appID)
                .setTransNo(transNo)
                .setUid(myOpenId);
        VivoPayInfo vivoPayInfo = builder.build();

        TDGAVirtualCurrency.onChargeRequest(params.getOrderID(), params.getProductName(), params.getPrice(), "CNY", params.getPrice() * 7,"vivo SDK");

        VivoUnionSDK.pay(activity, vivoPayInfo, new VivoPayCallback() {

            @Override
            public void onVivoPayResult(String transNo, boolean isSucc, String errorCode) {
                if (isSucc) {
                    TDGAVirtualCurrency.onChargeSuccess(params.getOrderID());
                    U8SDK.getInstance().onResult(U8Code.CODE_PAY_SUCCESS, "pay success");
                } else {
                    U8SDK.getInstance().onResult(U8Code.CODE_PAY_FAIL, "pay fail" + errorCode);
                }
            }
        });
    }

    private void showLog(String msg) {
        if (debug) {
            Log.i(TAG, msg);
        }
    }

    public void showRewardAd(String posId) {
        VideoAdParams.Builder builder = new VideoAdParams.Builder(ad_video_posId);
        mVivoVideoAd = new VivoVideoAd(activity, builder.build(), mVideoAdListener);
        mVivoVideoAd.loadAd();
    }

    public void showInsertAd(String posId) {
        InterstitialAdParams.Builder builder = new InterstitialAdParams.Builder(ad_interstitial_posId);
        String backUrl = "vivobrowser://browser.vivo.com";
        String btnName = "城堡英雄";
        BackUrlInfo backUrlInfo = new BackUrlInfo(backUrl, btnName);
        builder.setBackUrlInfo(backUrlInfo);
        mVivoInterstitialAd = new VivoInterstitialAd(activity, builder.build(), mIAdListener);
        //加载插屏图片广告
        mVivoInterstitialAd.load();
    }

    public void showNativeAd(String posId, U8AdParams adParams) {
        mAdPopupWindow = new VivoNativeAdPopupWindow(activity, ad_native_posId, debug, adParams);
        mAdPopupWindow.show();
    }

    public void nativeClose() {
        if (mAdPopupWindow != null) {
            mAdPopupWindow.close();
        }
    }
}
