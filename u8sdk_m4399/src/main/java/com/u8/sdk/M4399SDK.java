package com.u8.sdk;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.util.Log;

import com.mob4399.adunion.AdUnionVideo;
import com.mob4399.adunion.listener.OnAuVideoAdListener;
import com.tendcloud.tenddata.TDGAProfile;
import com.tendcloud.tenddata.TDGAVirtualCurrency;
import com.zjtx.prompt.PromptDialog;

import org.json.JSONObject;

import java.net.URLEncoder;

import cn.m4399.operate.OperateCenter;
import cn.m4399.operate.OperateCenter.OnLoginFinishedListener;
import cn.m4399.operate.OperateCenter.OnQuitGameListener;
import cn.m4399.operate.OperateCenter.OnRechargeFinishedListener;
import cn.m4399.operate.OperateCenterConfig;
import cn.m4399.operate.OperateCenterConfig.PopLogoStyle;
import cn.m4399.operate.OperateCenterConfig.PopWinPosition;
import cn.m4399.operate.User;

public class M4399SDK {
    private Activity activity;
    public static final String TAG = "M4399SDK";
    private static M4399SDK instance;

    private String appKey;
    private int orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
    private PopLogoStyle logoStyle = PopLogoStyle.POPLOGOSTYLE_ONE;
    private PopWinPosition position = PopWinPosition.POS_RIGHT;

    private AdUnionVideo mAdUnionVideo;
    private boolean isShow4399Ad;
    private String ad_posId;
    private boolean isAdLoaded = false;

    private M4399SDK() {
        activity = U8SDK.getInstance().getContext();
    }

    public static M4399SDK getInstance() {
        if (instance == null) {
            instance = new M4399SDK();
        }

        return instance;
    }

    private void parseSDKParams(SDKParams params) {
        appKey = params.getString("M4399_AppKey");
        String orn = params.getString("M4399_Orientation");
        if ("landscape".equalsIgnoreCase(orn)) {
            orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        } else if ("portrait".equalsIgnoreCase(orn)) {
            orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        } else {
            orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        }

        String style = params.getString("M4399_PopLogoStyle");
        if ("one".equalsIgnoreCase(style)) {
            logoStyle = PopLogoStyle.POPLOGOSTYLE_ONE;
        } else if ("two".equalsIgnoreCase(style)) {
            logoStyle = PopLogoStyle.POPLOGOSTYLE_TWO;
        } else if ("three".equalsIgnoreCase(style)) {
            logoStyle = PopLogoStyle.POPLOGOSTYLE_THREE;
        } else if ("four".equalsIgnoreCase(style)) {
            logoStyle = PopLogoStyle.POPLOGOSTYLE_FOUR;
        }

        String pos = params.getString("M4399_Position");
        if ("left".equalsIgnoreCase(pos)) {
            position = PopWinPosition.POS_LEFT;
        } else if ("right".equalsIgnoreCase(pos)) {
            position = PopWinPosition.POS_RIGHT;
        } else if ("top".equalsIgnoreCase(pos)) {
            position = PopWinPosition.POS_TOP;
        } else if ("bottom".equalsIgnoreCase(pos)) {
            position = PopWinPosition.POS_BOTTOM;
        } else {
            position = PopWinPosition.POS_RIGHT;
        }

        isShow4399Ad = params.getBoolean("isShow4399Ad");
        if (isShow4399Ad) {
            ad_posId = params.getString("ad_posId");
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
        U8SDK.getInstance().setActivityCallback(new ActivityCallbackAdapter() {

            @Override
            public void onBackPressed() {
                exitSDK();
            }

            @Override
            public void onDestroy() {
                OperateCenter.getInstance().destroy();
            }

        });

        // 配置sdk属性,比如可扩展横竖屏配置
        OperateCenterConfig opeConfig = new OperateCenterConfig.Builder(activity)
                .setDebugEnabled(false)
                .setOrientation(orientation)
                .setPopLogoStyle(logoStyle)
                .setPopWinPosition(position)
                .setSupportExcess(false)
                .setGameKey(appKey)
                .build();
        OperateCenter.getInstance().setConfig(opeConfig);

        //初始化SDK，在这个过程中会读取各种配置和检查当前帐号是否在登录中
        //只有在init之后， isLogin()返回的状态才可靠
        OperateCenter.getInstance().init(activity, new OperateCenter.OnInitGloabListener() {

            // 初始化结束执行后回调
            @Override
            public void onInitFinished(boolean isLogin, User userInfo) {
                Log.i(TAG, "onInitFinished:" + isLogin);
                U8SDK.getInstance().onResult(U8Code.CODE_INIT_SUCCESS, "init success");
            }

            // 注销帐号的回调， 包括个人中心里的注销和logout()注销方式
            // fromUserCenter区分是否从悬浮窗-个人中心("4399游戏助手页面")注销的，若是则为true，不是为false
            @Override
            public void onUserAccountLogout(boolean fromUserCenter) {
                Log.i(TAG, "onUserAccountLogout:" + fromUserCenter);
                U8SDK.getInstance().onResult(U8Code.CODE_LOGOUT_SUCCESS, "logout success");
                U8SDK.getInstance().onLogout();
            }

            // 个人中心里切换帐号的回调
            // fromUserCenter区分是否从悬浮窗-个人中心("4399游戏助手页面")切换的，若是则为true，不是为false
            @Override
            public void onSwitchUserAccountFinished(boolean fromUserCenter, User userInfo) {
                Log.i(TAG, "onSwitchUserAccountFinished:" + fromUserCenter);
                U8SDK.getInstance().onResult(U8Code.CODE_LOGOUT_SUCCESS, "logout success");
                U8SDK.getInstance().onLogout();
            }
        });

        if (isShow4399Ad){
            mAdUnionVideo = new AdUnionVideo(activity, ad_posId, new OnAuVideoAdListener() {
                @Override
                public void onVideoAdLoaded() {
                    //播放完自动加载广告
                    Log.i(TAG, "onVideoAdLoaded");
                    U8SDK.getInstance().onResult(U8Code.CODE_ADS_LOADED, "广告加载完成，开始播放");
                    isAdLoaded = true;
                }

                @Override
                public void onVideoAdShow() {
                    Log.i(TAG, "onVideoAdShow");
                    U8SDK.getInstance().onResult(U8Code.CODE_ADS_SHOW, "开始播放广告");
                }

                @Override
                public void onVideoAdFailed(String s) {
                    Log.e(TAG, "onVideoAdFailed:" + s);
                    U8SDK.getInstance().onResult(U8Code.CODE_ADS_FAILED, "广告加载或播放失败");
                    isAdLoaded = false;
                }

                @Override
                public void onVideoAdClicked() {
                    Log.i(TAG, "onVideoAdClicked");
                    U8SDK.getInstance().onResult(U8Code.CODE_ADS_CLICKED, "广告被点击");
                }

                @Override
                public void onVideoAdClosed() {
                    Log.i(TAG, "onVideoAdClosed");
                    U8SDK.getInstance().onResult(U8Code.CODE_ADS_CLOSED, "广告被关闭");
                }

                @Override
                public void onVideoAdComplete() {
                    Log.i(TAG, "onVideoAdComplete");
                    U8SDK.getInstance().onResult(U8Code.CODE_ADS_COMPLETE, "广告播放完成");
                }
            });
        }
    }

    private boolean isLogin = false;

    public void login() {
        Log.i(TAG, "login");
        isLogin = true;
        OperateCenter.getInstance().login(activity, loginFinishedListener);
    }

    public void switchAccount() {
        Log.i(TAG, "switchAccount");
        isLogin = false;
        OperateCenter.getInstance().switchAccount(activity, loginFinishedListener);
    }

    private OnLoginFinishedListener loginFinishedListener = new OnLoginFinishedListener() {

        @Override
        public void onLoginFinished(boolean success, int resultCode, User userInfo) {
            if (success) {
                String loginResult = encodeLoginResult(userInfo);
                if (isLogin) {
                    U8SDK.getInstance().onLoginResult(loginResult);
                } else {
                    U8SDK.getInstance().onSwitchAccount(loginResult);
                }
                U8SDK.getInstance().onResult(U8Code.CODE_LOGIN_SUCCESS, "switch or login success");
            } else {
                U8SDK.getInstance().onResult(U8Code.CODE_LOGIN_FAIL, "switch or login failed");
            }
        }
    };

    private String encodeLoginResult(User user) {
        JSONObject json = new JSONObject();
        String urlString = "";
        try {
            json.put("uid", user.getUid());
            json.put("nickname", user.getNick());
            json.put("username", user.getName());
            json.put("token", user.getState());
            Log.i(TAG, "login result:" + json.toString());
            urlString = URLEncoder.encode(json.toString(), "GBK");
            Log.i(TAG, "urlString:" + urlString);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return urlString;
    }

    public void logout() {
        Log.i(TAG, "logout");
        OperateCenter.getInstance().logout();
    }

    public void exitSDK() {
        OperateCenter.getInstance().shouldQuitGame(activity, new OnQuitGameListener() {

            @Override
            public void onQuitGame(boolean shouldQuit) {
                if (shouldQuit) {
                    activity.finish();
                    android.os.Process.killProcess(android.os.Process.myPid());
                }
            }
        });
    }

    public void sendExtraData(UserExtraData extraData) {
        switch (extraData.getDataType()) {
            case UserExtraData.TYPE_ENTER_GAME:
                OperateCenter.getInstance().setServer(extraData.getServerName());
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

    public void pay(final PayParams params) {
        TDGAVirtualCurrency.onChargeRequest(params.getOrderID(), params.getProductName(), params.getPrice(), "CNY", params.getPrice() * 7,"4399 SDK");

        OperateCenter.getInstance().recharge(activity, params.getPrice(), params.getOrderID(), params.getProductName(), new OnRechargeFinishedListener() {

            @Override
            public void onRechargeFinished(boolean success, int resultCode, String msg) {
                if (success) {
                    TDGAVirtualCurrency.onChargeSuccess(params.getOrderID());
                    U8SDK.getInstance().onResult(U8Code.CODE_PAY_SUCCESS, "pay success");
                } else {
                    U8SDK.getInstance().onResult(U8Code.CODE_PAY_FAIL, "pay failed");
                }

            }
        });
    }


    public void show4399Ad(String posId) {
        if (!isShow4399Ad) return;

        //播放视频广告
        if (isAdLoaded && mAdUnionVideo != null)
            mAdUnionVideo.show();
    }

}
