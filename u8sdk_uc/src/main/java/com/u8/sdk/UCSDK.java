package com.u8.sdk;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.text.TextUtils;
import android.widget.Toast;

import com.tendcloud.tenddata.TDGAProfile;
import com.tendcloud.tenddata.TDGAVirtualCurrency;
import com.u8.common.PromptDialog;
import com.u8.sdk.log.Log;
import com.u8.sdk.verify.UToken;


import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import cn.gundam.sdk.shell.even.SDKEventKey;
import cn.gundam.sdk.shell.even.SDKEventReceiver;
import cn.gundam.sdk.shell.even.Subscribe;
import cn.gundam.sdk.shell.exception.AliLackActivityException;
import cn.gundam.sdk.shell.exception.AliNotInitException;
import cn.gundam.sdk.shell.open.OrderInfo;
import cn.gundam.sdk.shell.open.ParamInfo;
import cn.gundam.sdk.shell.open.UCOrientation;
import cn.gundam.sdk.shell.param.SDKParamKey;
import cn.uc.gamesdk.UCGameSdk;

import static com.u8.sdk.utils.EncryptUtils.md5;

public class UCSDK {
    private Activity activity;

    private static final String TAG = "ucsdk";

    private static UCSDK instance;

    private int gameId;
    private String apiKey;
    private String orientation;
    private boolean debugMode = false;

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
                Log.i(TAG, "pay success" + sb);
                flag = true;

                TDGAVirtualCurrency.onChargeSuccess(orderId);
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

    private void upgrade(UserExtraData extraData){
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

    private String orderId;

    public void pay(PayParams params) {
        try {
            UToken token = U8SDK.getInstance().getUToken();
            if (token == null) {
                Toast.makeText(activity, "登录失效，请重新登录", Toast.LENGTH_SHORT).show();
                return;
            }

            orderId = params.getOrderID();

            Map<String, String> paramMap = new HashMap<String, String>();
            paramMap.put(SDKParamKey.CALLBACK_INFO, orderId);
            // paramMap.put(SDKParamKey.NOTIFY_URL, ""); //在后台配置
            paramMap.put(SDKParamKey.AMOUNT, params.getPrice() + ".00");
            paramMap.put(SDKParamKey.CP_ORDER_ID, orderId);
            paramMap.put(SDKParamKey.ACCOUNT_ID, token.getSdkUserID());
            paramMap.put(SDKParamKey.SIGN_TYPE, "MD5");

            String signStr = sign(paramMap, apiKey);
            Log.i(TAG, "signStr:" + signStr);

            Map<String, Object> map = new HashMap<String, Object>();
            map.putAll(paramMap);

            cn.gundam.sdk.shell.param.SDKParams sdkParams = new cn.gundam.sdk.shell.param.SDKParams();
            sdkParams.putAll(map);
            sdkParams.put(SDKParamKey.SIGN, signStr);

            TDGAVirtualCurrency.onChargeRequest(orderId, params.getProductName(), params.getPrice(), "CNY", params.getPrice() * 7,"uc SDK");

            UCGameSdk.defaultSdk().pay(activity, sdkParams);
        } catch (IllegalArgumentException e) {
            //传入参数错误异常处理
            Log.e(TAG, "pay:" + e.getMessage());
        } catch (AliNotInitException e) {
            //未初始化或正在初始化时，异常处理
            Log.e(TAG, "pay:" + e.getMessage());
        } catch (AliLackActivityException e) {
            //activity为空，异常处理
            Log.e(TAG, "pay:" + e.getMessage());
        }
    }

    /**
     * 签名工具方法
     *
     * @param reqMap
     * @return
     */
    public static String sign(Map<String, String> reqMap, String signKey) {
        //将所有key按照字典顺序排序
        TreeMap<String, String> signMap = new TreeMap<String, String>(reqMap);
        StringBuilder stringBuilder = new StringBuilder(1024);
        for (Map.Entry<String, String> entry : signMap.entrySet()) {
            // sign和signType不参与签名
            if ("sign".equals(entry.getKey()) || "signType".equals(entry.getKey())) {
                continue;
            }
            //值为null的参数不参与签名
            if (entry.getValue() != null) {
                stringBuilder.append(entry.getKey()).append("=").append(entry.getValue());
            }
        }
        //拼接签名秘钥
        stringBuilder.append(signKey);
        //剔除参数中含有的'&'符号
        String signSrc = stringBuilder.toString().replaceAll("&", "");
        return md5(signSrc).toLowerCase();
    }
}
