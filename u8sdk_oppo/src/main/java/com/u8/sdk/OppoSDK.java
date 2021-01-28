package com.u8.sdk;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.heytap.msp.mobad.api.InitParams;
import com.heytap.msp.mobad.api.MobAdManager;
import com.heytap.msp.mobad.api.ad.InterstitialAd;
import com.heytap.msp.mobad.api.ad.RewardVideoAd;
import com.heytap.msp.mobad.api.listener.IInitListener;
import com.heytap.msp.mobad.api.listener.IInterstitialAdListener;
import com.heytap.msp.mobad.api.listener.IRewardVideoAdListener;
import com.heytap.msp.mobad.api.params.RewardVideoAdParams;
import com.nearme.game.sdk.GameCenterSDK;
import com.nearme.game.sdk.callback.ApiCallback;
import com.nearme.game.sdk.callback.GameExitCallback;
import com.nearme.game.sdk.common.model.ApiResult;
import com.nearme.game.sdk.common.model.biz.PayInfo;
import com.nearme.game.sdk.common.model.biz.ReportUserGameInfoParam;
import com.nearme.game.sdk.common.util.AppUtil;
import com.nearme.platform.opensdk.pay.PayResponse;
import com.tendcloud.tenddata.TDGAProfile;
import com.tendcloud.tenddata.TDGAVirtualCurrency;
import com.zjtx.prompt.PromptDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class OppoSDK {
    public static final String TAG = "oppoSdk";

    private Activity activity;
    private static OppoSDK instance;

    private String appSecret;
    private String payCallbackUrl;
    private boolean debug;

    private static final int REQUEST_CODE = 1;// 悬浮权限请求码
    private SharedPreferences sp;
    private Editor editor;

    private RewardVideoAd mRewardVideoAd;
    private InterstitialAd mInterstitialAd;
    private OppoNativeAdPopupWindow mAdPopupWindow;

    private boolean isShowOppoAd;
    private String ad_appId;

    private String ad_video_posId;
    private String ad_interstitial_posId;
    private String ad_native_posId;

    private List<String> mNeedRequestPMSList = new ArrayList();
    private static final int REQUEST_PERMISSIONS_CODE = 100;

    private OppoSDK() {
        activity = U8SDK.getInstance().getContext();
    }

    public static OppoSDK getInstance() {
        if (instance == null) {
            instance = new OppoSDK();
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
        appSecret = params.getString("appSecret");
        payCallbackUrl = params.getString("PayNotifyUrl");

        debug = params.getBoolean("debug_mode");
        isShowOppoAd = params.getBoolean("isShowOppoAd");
        showLog("isShowOppoAd is " + isShowOppoAd);
        if (isShowOppoAd) {
            ad_appId = params.getString("ad_appId");
            showLog(" ad_appId is " + ad_appId);
            ad_video_posId = params.getString("ad_video_posId");
            ad_interstitial_posId = params.getString("ad_interstitial_posId");
            ad_native_posId = params.getString("ad_native_posId");
        }
    }

    private void initSDK() {
        showLog("initSDK start");

        sp = activity.getSharedPreferences("usercenter", 0);
        editor = sp.edit();

        GameCenterSDK.init(appSecret, activity);

        showFloat();

        if (isShowOppoAd) {
            if (Build.VERSION.SDK_INT >= 23) {
                checkAndRequestPermissions();
            } else {
                initModAd();
            }
        }

        U8SDK.getInstance().setActivityCallback(new ActivityCallbackAdapter() {

            @Override
            public void onDestroy() {
                if (isShowOppoAd) {
                    if (null != mRewardVideoAd) {
                        mRewardVideoAd.destroyAd();
                    }

                    if (null != mInterstitialAd) {
                        mInterstitialAd.destroyAd();
                    }

                    MobAdManager.getInstance().exit(activity);
                }
            }

            @Override
            public void onActivityResult(int requestCode, int resultCode, Intent data) {
                if (requestCode == REQUEST_CODE) {
                    if (Build.VERSION.SDK_INT >= 23) {
                        if (!Settings.canDrawOverlays(activity)) {
                            Toast.makeText(activity, "显示在其他应用的上层权限未开启,悬浮窗可能无法显示", Toast.LENGTH_LONG).show();
                        }
                    }
                }
            }

            @Override
            public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) {
                switch (requestCode) {
                    case REQUEST_PERMISSIONS_CODE:
                        if (hasNecessaryPMSGranted()) {
                            showLog("权限已开启");
                            initModAd();
                        } else {
                            showLog("权限未开启");
                        }
                }
            }
        });

    }

    @TargetApi(23)
    private void showFloat() {
        if (!Settings.canDrawOverlays(activity)) {
            boolean isShow = sp.getBoolean("isShow", true);
            if (isShow) {
                createDialog();
            }
        }
    }

    /**
     * 悬浮框权限提示框
     */
    private void createDialog() {
        new AlertDialog.Builder(activity).setTitle("悬浮窗").setMessage("是否显示悬浮窗")
                .setPositiveButton("是", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        editor.putBoolean("isShow", true).commit();
                        dialog.dismiss();
                        try {
                            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                            intent.setData(Uri.parse("package:" + activity.getPackageName()));
                            activity.startActivityForResult(intent, REQUEST_CODE);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).setNegativeButton("不再提示", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                editor.putBoolean("isShow", false).commit();
                dialog.dismiss();
            }
        }).setCancelable(false).show();
    }

    public void login() {
        showLog("login start");

        GameCenterSDK.getInstance().doLogin(activity, new ApiCallback() {

            @Override
            public void onSuccess(String resultMsg) {
                showLog("doLogin == " + resultMsg);
                getToken();
            }

            @Override
            public void onFailure(String msg, int code) {
                Log.e(TAG, "登录失败->code:" + code + ",msg:" + msg);
                U8SDK.getInstance().onResult(U8Code.CODE_LOGIN_FAIL, "login fail");
            }
        });
    }

    private void getToken() {
        GameCenterSDK.getInstance().doGetTokenAndSsoid(new ApiCallback() {

            @Override
            public void onSuccess(String resultMsg) {
                showLog("doGetTokenAndSsoid == " + resultMsg);
                try {
                    JSONObject json = new JSONObject(resultMsg);
                    String token = json.getString("token");
                    String ssoid = json.getString("ssoid");
                    String loginResult = encodeLoginResult(token, ssoid);
                    showLog("loginResult == " + loginResult);
                    U8SDK.getInstance().onLoginResult(loginResult);
                    U8SDK.getInstance().onResult(U8Code.CODE_LOGIN_SUCCESS, "login success");

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(String msg, int code) {
                Log.e(TAG, "登录获取Token失败->code:" + code + ",msg:" + msg);
                U8SDK.getInstance().onResult(U8Code.CODE_LOGIN_FAIL, "login fail");
            }
        });
    }

    private String encodeLoginResult(String token, final String ssoid) {
        final JSONObject json = new JSONObject();
        try {
            json.put("token", token);
            json.put("ssoid", ssoid);

//                GameCenterSDK.getInstance().doGetUserInfo(new ReqUserInfoParam(token, ssoid), new ApiCallback() {
//                    @Override
//                    public void onSuccess(String resultMsg) {
//                        showLog("resultMsg == " + resultMsg);
//                    }
//
//                    @Override
//                    public void onFailure(String resultMsg, int resultCode) {
//                        Log.e(TAG, "获取用户信息失败->resultCode:" + resultCode + ",resultMsg:" + resultMsg);
//                    }
//                });

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }

    public void exit() {
        showLog("exit start");

        GameCenterSDK.getInstance().onExit(activity, new GameExitCallback() {

            @Override
            public void exitGame() {
                // TODO Auto-generated method stub
                MobAdManager.getInstance().exit(activity);

                U8SDK.getInstance().onLogout();
                U8SDK.getInstance().onResult(U8Code.CODE_LOGOUT_SUCCESS, "logout success");
                AppUtil.exitGameProcess(activity);
            }
        });
    }

    public void submitGameData(UserExtraData extraData) {
        switch (extraData.getDataType()) {
            case UserExtraData.TYPE_ENTER_GAME:
                getVerInfo();
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

    private void goInGame(UserExtraData extraData) {
        showLog("进入游戏");
        GameCenterSDK.getInstance().doReportUserGameInfoData(new ReportUserGameInfoParam(extraData.getRoleID(),
                extraData.getRoleName(), Integer.parseInt(extraData.getRoleLevel()),
                extraData.getServerID() + "", extraData.getServerName(), extraData.getRoleLevel(), null), new ApiCallback() {

            @Override
            public void onSuccess(String resultMsg) {
                showLog("goInGame == " + resultMsg);
                U8SDK.getInstance().onResult(U8Code.CODE_UPDATE_SUCCESS, "Report Success" + resultMsg);
            }

            @Override
            public void onFailure(String resultMsg, int resultCode) {
                Log.e(TAG, "进入游戏失败->resultCode:" + resultCode + ",resultMsg:" + resultMsg);
                U8SDK.getInstance().onResult(U8Code.CODE_UPDATE_FAILED, "Report Failure" + resultMsg);
            }
        });
    }

    public void getVerInfo() {
        showLog("防沉迷");
        GameCenterSDK.getInstance().doGetVerifiedInfo(new ApiCallback() {

            @Override
            public void onSuccess(String resultMsg) {
                try { // 解析年龄（age）
                    int age = Integer.parseInt(resultMsg);

                    if (age < 18) {
                        // 已实名但未成年，CP开始处理防沉迷
                        showLog("请注意游戏时间，切勿沉迷游戏！！");
                    } else {
                        // 已实名且已成年，尽情玩游戏吧
                        showLog("愉快的玩游戏吧！");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(String resultMsg, int resultCode) {
                Log.e(TAG, "实名认证失败->resultCode:" + resultCode + ",resultMsg:" + resultMsg);

                if (resultCode == ApiResult.RESULT_CODE_VERIFIED_FAILED_AND_RESUME_GAME) {
                    // 实名认证失败，但还可以继续玩游戏

                } else if (resultCode == ApiResult.RESULT_CODE_VERIFIED_FAILED_AND_STOP_GAME) {
                    // 实名认证失败，不允许继续游戏，CP需自己处理退出游戏

                }
            }
        });
    }

    public void pay(final PayParams params) {
        showLog("pay start");

        PayInfo payInfo = new PayInfo(params.getOrderID(), params.getExtension(), params.getPrice() * 100);
        payInfo.setProductDesc(params.getProductDesc());
        payInfo.setProductName(params.getProductName());
        payInfo.setCallbackUrl(payCallbackUrl);

        TDGAVirtualCurrency.onChargeRequest(params.getOrderID(), params.getProductName(), params.getPrice(), "CNY", params.getPrice() * 7, "oppo SDK");

        GameCenterSDK.getInstance().doPay(activity, payInfo, new ApiCallback() {

            @Override
            public void onSuccess(String resultMsg) {
                TDGAVirtualCurrency.onChargeSuccess(params.getOrderID());

                U8SDK.getInstance().onResult(U8Code.CODE_PAY_SUCCESS, "pay success" + resultMsg);
            }

            @Override
            public void onFailure(String resultMsg, int resultCode) {
                Log.e(TAG, "支付失败->resultMsg:" + resultMsg + ",resultCode:" + resultCode);
                if (PayResponse.CODE_CANCEL == resultCode) {
                    // 取消支付处理
                    U8SDK.getInstance().onResult(U8Code.CODE_PAY_CANCEL, "pay cancel");
                } else {
                    U8SDK.getInstance().onResult(U8Code.CODE_PAY_FAIL, "pay failed" + resultMsg);
                }
            }
        });
    }

    public void jumpLeisureSubject() {
        showLog("跳转超休闲专区（OPPO）");
        GameCenterSDK.getInstance().jumpLeisureSubject();
    }

    public void showRewardAd(String posId) {
        mRewardVideoAd = new RewardVideoAd(activity, ad_video_posId, new IRewardVideoAdListener() {
            @Override
            public void onAdSuccess() {
                showLog("onAdSuccess");
                U8SDK.getInstance().onResult(U8Code.CODE_ADS_LOADED, "广告加载完成，开始播放");

                //播放视频广告
                if (mRewardVideoAd.isReady()) {
                    mRewardVideoAd.showAd();
                    mRewardVideoAd = null;
                }
            }

            @Override
            public void onAdFailed(String s) {
                /**
                 * 已废弃，使用onAdFailed(int i, String s)
                 */
            }

            @Override
            public void onAdFailed(int i, String s) {
                Log.e(TAG, "onAdFailed->code:" + i + ",msg:" + s);
                U8SDK.getInstance().onResult(U8Code.CODE_ADS_FAILED, "广告加载或播放失败");
            }

            @Override
            public void onAdClick(long l) {
                showLog("onAdClick");
                U8SDK.getInstance().onResult(U8Code.CODE_ADS_CLICKED, "广告被点击");
            }

            @Override
            public void onVideoPlayStart() {
                showLog("onVideoPlayStart");
                U8SDK.getInstance().onResult(U8Code.CODE_ADS_SHOW, "开始播放广告");
            }

            @Override
            public void onVideoPlayComplete() {
                /**
                 * TODO 注意：从 SDK 3.0.1 版本开始，不能在激励视频广告播放完成回调 onVideoPlayComplete 里做任何激励操作，统一在 onReward 回调里给予用户激励。
                 */
                showLog("onVideoPlayComplete SDK 3.0.1之后不做处理");
            }

            @Override
            public void onVideoPlayError(String s) {
                Log.e(TAG, "onVideoPlayError:" + s);
                U8SDK.getInstance().onResult(U8Code.CODE_ADS_FAILED, "广告加载或播放失败");
            }

            @Override
            public void onVideoPlayClose(long l) {
                showLog("onVideoPlayClose");
                U8SDK.getInstance().onResult(U8Code.CODE_ADS_CLOSED, "广告被关闭");
            }

            @Override
            public void onLandingPageOpen() {
                //视频广告落地页打开，打开广告下载详情
                showLog("onLandingPageOpen");
            }

            @Override
            public void onLandingPageClose() {
                //视频广告落地页关闭
                showLog("onLandingPageClose");
                U8SDK.getInstance().onResult(U8Code.CODE_ADS_CLOSED, "广告被关闭");
            }

            @Override
            public void onReward(Object... objects) {
                /**
                 * TODO 注意：只能在收到onReward回调的时候才能给予用户奖励。
                 */
                showLog("onReward");
                U8SDK.getInstance().onResult(U8Code.CODE_ADS_COMPLETE, "广告播放完成");
            }
        });

        RewardVideoAdParams rewardVideoAdParams = new RewardVideoAdParams.Builder()
                .setFetchTimeout(3000)
                .build();
        mRewardVideoAd.loadAd(rewardVideoAdParams);
    }

    public void showInsertAd(String posId) {
        mInterstitialAd = new InterstitialAd(activity, ad_interstitial_posId);
        mInterstitialAd.setAdListener(new IInterstitialAdListener() {
            @Override
            public void onAdReady() {
                showLog("onAdReady");
                U8SDK.getInstance().onResult(U8Code.CODE_ADS_LOADED, "广告加载完成，开始展示");

                if (mInterstitialAd != null) {
                    mInterstitialAd.showAd();
                    mInterstitialAd = null;
                }
            }

            @Override
            public void onAdClose() {
                showLog("onAdClose");
                U8SDK.getInstance().onResult(U8Code.CODE_ADS_CLOSED, "广告被关闭");
                U8SDK.getInstance().onResult(U8Code.CODE_ADS_COMPLETE, "广告展示完成");

            }

            @Override
            public void onAdShow() {
                showLog("onAdShow");
                U8SDK.getInstance().onResult(U8Code.CODE_ADS_SHOW, "开始展示广告");
            }

            @Override
            public void onAdFailed(String s) {
                /**
                 *请求广告失败，已废弃，请使用onAdFailed(int i, String s)
                 */
            }

            @Override
            public void onAdFailed(int i, String s) {
                Log.e(TAG, "onAdFailed:code=" + i + ", msg:" + s);
                U8SDK.getInstance().onResult(U8Code.CODE_ADS_FAILED, "广告加载或展示失败");
            }

            @Override
            public void onAdClick() {
                showLog("onAdClick");
                U8SDK.getInstance().onResult(U8Code.CODE_ADS_CLICKED, "广告被点击");
            }
        });
        mInterstitialAd.loadAd();
    }

    public void showNativeAd(String posId, U8AdParams adParams) {
        mAdPopupWindow = new OppoNativeAdPopupWindow(activity, ad_native_posId, debug, adParams);
        mAdPopupWindow.show();
    }


    public void nativeClose() {
        if (mAdPopupWindow != null) {
            mAdPopupWindow.close();
        }
    }

    private void initModAd() {
        InitParams initParams = new InitParams.Builder()
                .setDebug(debug)
                .build();
        //调用这行代码初始化广告SDK
        MobAdManager.getInstance().init(activity, ad_appId, initParams, new IInitListener() {
            @Override
            public void onSuccess() {
                showLog("MobAd init onSuccess");
            }

            @Override
            public void onFailed(String s) {
                Log.e(TAG, "MobAd init onFailed ==" + s);
            }
        });
    }

    /**
     * 申请SDK运行需要的权限
     * 注意：在Android Q以下READ_PHONE_STATE权限是必须权限，没有这个权限SDK无法正常获得广告。
     * WRITE_EXTERNAL_STORAGE 、ACCESS_FINE_LOCATION 是可选权限；没有不影响SDK获取广告；但是如果应用申请到该权限，会显著提升应用的广告收益。
     */
    private void checkAndRequestPermissions() {
        /**
         * 在Android Q以下READ_PHONE_STATE 权限是必须权限，没有这个权限SDK无法正常获得广告。
         */
        if (Build.VERSION.SDK_INT < 29 && PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_PHONE_STATE)) {
            mNeedRequestPMSList.add(Manifest.permission.READ_PHONE_STATE);
        }
        /**
         * WRITE_EXTERNAL_STORAGE、ACCESS_FINE_LOCATION 是两个可选权限；没有不影响SDK获取广告；但是如果应用申请到该权限，会显著提升应用的广告收益。
         */
        if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            mNeedRequestPMSList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)) {
            mNeedRequestPMSList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (0 == mNeedRequestPMSList.size()) {
            /**
             * 权限都已经有了，那么直接调用SDK请求广告。
             */
            initModAd();
        } else {
            /**
             * 有权限需要申请，主动申请。
             */
            String[] temp = new String[mNeedRequestPMSList.size()];
            mNeedRequestPMSList.toArray(temp);
            ActivityCompat.requestPermissions(activity, temp, REQUEST_PERMISSIONS_CODE);
        }
    }

    /**
     * 判断应用是否已经获得SDK运行必须的READ_PHONE_STATE权限。
     *
     * @return
     */
    private boolean hasNecessaryPMSGranted() {
        if (Build.VERSION.SDK_INT >= 29 || PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_PHONE_STATE)) {
            return true;
        }
        return false;
    }

    private void showLog(String msg) {
        if (debug) {
            Log.i(TAG, msg);
        }
    }

}
