package com.u8.sdk;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.text.TextUtils;

import com.tendcloud.tenddata.TDGAProfile;
import com.u8.sdk.log.Log;
import com.u8.common.PromptDialog;

import java.util.HashMap;
import java.util.Map;

import cn.gundam.sdk.shell.even.SDKEventKey;
import cn.gundam.sdk.shell.even.SDKEventReceiver;
import cn.gundam.sdk.shell.even.Subscribe;
import cn.gundam.sdk.shell.exception.AliLackActivityException;
import cn.gundam.sdk.shell.exception.AliNotInitException;
import cn.gundam.sdk.shell.open.OrderInfo;
import cn.gundam.sdk.shell.open.ParamInfo;
import cn.gundam.sdk.shell.open.UCOrientation;
import cn.gundam.sdk.shell.param.SDKParamKey;
import cn.sirius.nga.NGASDK;
import cn.sirius.nga.NGASDKFactory;
import cn.sirius.nga.properties.NGAVideoController;
import cn.sirius.nga.properties.NGAVideoListener;
import cn.sirius.nga.properties.NGAVideoProperties;
import cn.sirius.nga.properties.NGAdController;
import cn.uc.gamesdk.UCGameSdk;

public class UCSDK {
    private Activity activity;

    private static final String TAG = "ucsdk";

    private static UCSDK instance;

    private int gameId;
    private String apiKey;
    private String orientation;
    private boolean debugMode = false;

    private String ad_appId;
    private String ad_posId;
    private NGAVideoController mController;
    private NGAVideoListener mListener;

    private boolean flag = false;

    public UCSDK() {
        activity = U8SDK.getInstance().getContext();
    }

    public static UCSDK getInstance() {
        if (instance == null) {
            instance = new UCSDK();
        }
        return instance;
    }

    private void parseSDKParams(SDKParams params) {
        gameId = params.getInt("UCGameId");
        apiKey = params.getString("UCApiKey");

        debugMode = params.getBoolean("UCDebugMode");
        orientation = params.getString("ali_screen_orientation");

        ad_appId = params.getString("ad_appId");
        ad_posId = params.getString("ad_posId");
    }

    public void initSDK(SDKParams data) {
        this.parseSDKParams(data);

        new Handler().postAtTime(new Runnable() {

            @Override
            public void run() {
                PromptDialog.init(activity, new PromptDialog.OnPromptListener() {
                    @Override
                    public void onInitSDK() {
                        initAliSdk();
                    }
                });
            }
        }, 1500);
    }

    public void initAliSdk() {
        U8SDK.getInstance().setActivityCallback(new ActivityCallbackAdapter() {

            @Override
            public void onDestroy() {
                UCGameSdk.defaultSdk().unregisterSDKEventReceiver(eventReceiver);

                if (mController != null) {
                    mController.destroyAd();
                }
            }
        });

        UCGameSdk.defaultSdk().registerSDKEventReceiver(eventReceiver);

        ParamInfo gameParamInfo = new ParamInfo();
        gameParamInfo.setGameId(gameId);

        // 设置SDK登录界面为竖屏
        if ("landscape".equalsIgnoreCase(orientation)) {
            gameParamInfo.setOrientation(UCOrientation.LANDSCAPE);
        } else {
            gameParamInfo.setOrientation(UCOrientation.PORTRAIT);
        }
        cn.gundam.sdk.shell.param.SDKParams sdkParams = new cn.gundam.sdk.shell.param.SDKParams();
        sdkParams.put(SDKParamKey.GAME_PARAMS, gameParamInfo);

        Intent intent = activity.getIntent();
        String pullupInfo = intent.getDataString();
        if (TextUtils.isEmpty(pullupInfo)) {
            pullupInfo = intent.getStringExtra("data");
        }
        sdkParams.put(SDKParamKey.PULLUP_INFO, pullupInfo);

        try {
            UCGameSdk.defaultSdk().initSdk(activity, sdkParams);
        } catch (AliLackActivityException e) {
            Log.e(TAG, "initSdk:" + e.getMessage());
        }

        //广告SDK初始化
        NGASDK ngasdk = NGASDKFactory.getNGASDK();
        Map<String, Object> args = new HashMap<>();
        args.put(NGASDK.APP_ID, ad_appId);
        //打Release包的时候，需要把DebugMode设置为false
        args.put(NGASDK.DEBUG_MODE, debugMode);
        ngasdk.init(activity, args, new NGASDK.InitCallback() {
            @Override
            public void success() {
                Log.i(TAG, "广告初始化成功");
            }

            @Override
            public void fail(Throwable throwable) {
                throwable.printStackTrace();
            }
        });

        mListener = new NGAVideoListener() {
            @Override
            public void onCompletedAd() {
                Log.i(TAG, "onCompletedAd");
                U8SDK.getInstance().onResult(U8Code.CODE_ADS_COMPLETE, "广告播放完成");
            }

            @Override
            public void onShowAd() {
                Log.i(TAG, "onShowAd");
                U8SDK.getInstance().onResult(U8Code.CODE_ADS_SHOW, "开始播放广告");
            }

            @Override
            public void onClickAd() {
                Log.i(TAG, "onClickAd");
                U8SDK.getInstance().onResult(U8Code.CODE_ADS_CLICKED, "广告被点击");
            }

            @Override
            public void onCloseAd() {
                Log.i(TAG, "onCloseAd");
                U8SDK.getInstance().onResult(U8Code.CODE_ADS_CLOSED, "广告被关闭");
            }

            @Override
            public void onErrorAd(int i, String s) {
                mController = null;
                Log.e(TAG, "onErrorAd->code:" + i + ",msg:" + s);
                U8SDK.getInstance().onResult(U8Code.CODE_ADS_FAILED, "广告加载或播放失败");
            }

            @Override
            public void onRequestAd() {
                Log.i(TAG, "onRequestAd");
            }

            @Override
            public <T extends NGAdController> void onReadyAd(T controller) {
                mController = (NGAVideoController) controller;
                Log.i(TAG, "onReadyAd");
                U8SDK.getInstance().onResult(U8Code.CODE_ADS_LOADED, "广告加载完成，开始播放");

                if (mController != null) {
                    //播放视频广告
                    mController.showAd();

                    boolean hasCacheAd = mController.hasCacheAd();
                    Log.i(TAG, "hasCacheAd=" + hasCacheAd);
                }
            }
        };
    }

    private SDKEventReceiver eventReceiver = new SDKEventReceiver() {

        @Subscribe(event = SDKEventKey.ON_INIT_SUCC)
        private void onInitSucc() {
            // 初始化成功
            Log.i(TAG, "init success");
            U8SDK.getInstance().onResult(U8Code.CODE_INIT_SUCCESS, "init success");
        }

        @Subscribe(event = SDKEventKey.ON_INIT_FAILED)
        private void onInitFailed(String data) {
            Log.e(TAG, "init fail data is " + data);
            // 初始化失败
            U8SDK.getInstance().onResult(U8Code.CODE_INIT_FAIL, "init failed");
        }

        @Subscribe(event = SDKEventKey.ON_LOGIN_SUCC)
        private void onLoginSucc(String sid) {
            Log.i(TAG, "sid:" + sid);
            U8SDK.getInstance().onLoginResult(sid);
            U8SDK.getInstance().onResult(U8Code.CODE_LOGIN_SUCCESS, "login success");
        }

        @Subscribe(event = SDKEventKey.ON_LOGIN_FAILED)
        private void onLoginFailed(String desc) {
            Log.e(TAG, "login failed." + desc);
            U8SDK.getInstance().onResult(U8Code.CODE_LOGIN_FAIL, "login failed." + desc);
        }

        @Subscribe(event = SDKEventKey.ON_LOGOUT_SUCC)
        private void onLogoutSucc() {
            Log.i(TAG, "logout success");
            U8SDK.getInstance().onLogout();
            U8SDK.getInstance().onResult(U8Code.CODE_LOGOUT_SUCCESS, "logout success");
        }

        @Subscribe(event = SDKEventKey.ON_LOGOUT_FAILED)
        private void onLogoutFailed() {
            Log.e(TAG, "logout fail");
        }

        @Subscribe(event = SDKEventKey.ON_EXIT_SUCC)
        private void onExit(String desc) {
            activity.finish();
            android.os.Process.killProcess(android.os.Process.myPid());
        }

        @Subscribe(event = SDKEventKey.ON_EXIT_CANCELED)
        private void onExitCanceled(String desc) {

        }

        @Subscribe(event = SDKEventKey.ON_CREATE_ORDER_SUCC)
        private void onCreateOrderSucc(OrderInfo orderInfo) {
            if (orderInfo != null) {
                StringBuilder sb = new StringBuilder();
                sb.append(String.format("'orderId':'%s'", orderInfo.getOrderId()));
                sb.append(String.format("'orderAmount':'%s'", orderInfo.getOrderAmount()));
                sb.append(String.format("'payWay':'%s'", orderInfo.getPayWay()));
                sb.append(String.format("'payWayName':'%s'", orderInfo.getPayWayName()));

                flag = true;
                Log.i(TAG, "pay success" + sb);
                U8SDK.getInstance().onResult(U8Code.CODE_PAY_SUCCESS, "pay success");
            } else {
                U8SDK.getInstance().onResult(U8Code.CODE_PAY_FAIL, "pay fail");
            }
        }

        @Subscribe(event = SDKEventKey.ON_PAY_USER_EXIT)
        private void onPayUserExit(OrderInfo orderInfo) {
            if (flag) {
                flag = false;
            } else {
                U8SDK.getInstance().onResult(U8Code.CODE_PAY_FAIL, "pay failed");
            }
        }
    };

    public void login() {
        try {
            UCGameSdk.defaultSdk().login(activity, null);
        } catch (AliNotInitException e) {
            //未初始化或正在初始化时，异常处理
            Log.e(TAG, "login:" + e.getMessage());
        } catch (AliLackActivityException e) {
            //activity为空，异常处理
            Log.e(TAG, "login:" + e.getMessage());
        }
    }

    public void submitExtraData(UserExtraData data) {
        switch (data.getDataType()) {
            case UserExtraData.TYPE_ENTER_GAME:
                enterGame(data);
                break;
            case UserExtraData.TYPE_LEVEL_UP:
                upgrade(data);
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

    public void logout() {
        try {
            UCGameSdk.defaultSdk().logout(activity, null);
        } catch (AliLackActivityException e) {
            Log.e(TAG, "logout:" + e.getMessage());
        } catch (AliNotInitException e) {
            Log.e(TAG, "logout:" + e.getMessage());
        }
    }

    public void exitSDK() {
        try {
            UCGameSdk.defaultSdk().exit(activity, null);
        } catch (AliLackActivityException e) {
            Log.e(TAG, "exit:" + e.getMessage());
        } catch (AliNotInitException e) {
            Log.e(TAG, "exit:" + e.getMessage());
        }
    }

    public void showUCAd(String videoPosId) {
        final NGAVideoProperties properties = new NGAVideoProperties(activity, ad_appId, ad_posId);
        properties.setListener(mListener);
        NGASDK ngasdk = NGASDKFactory.getNGASDK();
        ngasdk.loadAd(properties);
    }
}
