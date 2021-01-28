
package com.u8.sdk;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;

import com.baidu.gamesdk.ActivityAdPage;
import com.baidu.gamesdk.ActivityAdPage.Listener;
import com.baidu.gamesdk.ActivityAnalytics;
import com.baidu.gamesdk.BDGameSDK;
import com.baidu.gamesdk.BDGameSDKSetting;
import com.baidu.gamesdk.BDGameSDKSetting.Domain;
import com.baidu.gamesdk.IResponse;
import com.baidu.gamesdk.OnGameExitListener;
import com.baidu.gamesdk.ResultCode;
import com.baidu.platformsdk.PayOrderInfo;
import com.tendcloud.tenddata.TDGAProfile;
import com.tendcloud.tenddata.TDGAVirtualCurrency;
import com.u8.sdk.baidu.Utils;
import com.zjtx.prompt.PromptDialog;

public class BaiduSDK {

    private String TAG = "BaiduSDK";

    private static BaiduSDK instance;

    private int appID;
    private String appKey;
    private Domain domain;

    private ActivityAdPage mActivityAdPage;
    private ActivityAnalytics mActivityAnalytics;

    private Activity activity;

    private BaiduSDK() {
        activity = U8SDK.getInstance().getContext();
    }

    public static BaiduSDK getInstance() {
        if (instance == null) {
            instance = new BaiduSDK();
        }
        return instance;
    }

    private void parseSDKParams(SDKParams params) {
        this.appID = params.getInt("AppID");
        this.appKey = params.getString("AppKey");
        String domain = params.getString("Domain");
        if ("DEBUG".equalsIgnoreCase(domain)) {
            this.domain = Domain.DEBUG;
        } else {
            this.domain = Domain.RELEASE;
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

    protected String[] needPermissions = {Manifest.permission.READ_PHONE_STATE};

    public void initSDK() {
        // TODO::这里调用AAA的SDK初始化方法

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int i = ContextCompat.checkSelfPermission(activity, needPermissions[0]);
            if (i != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, needPermissions, 321);
            }
        }

        mActivityAnalytics = new ActivityAnalytics(activity);
        mActivityAdPage = new ActivityAdPage(activity, new Listener() {

            @Override
            public void onClose() {
                // TODO 关闭暂停页, CP可以让玩家继续游戏
            }

        });

        U8SDK.getInstance().setActivityCallback(new ActivityCallbackAdapter() {

            public void onResume() {
                mActivityAdPage.onResume();
                mActivityAnalytics.onResume();
                BDGameSDK.onResume(activity);

//                // 显示悬浮窗
//                BDGameSDK.showFloatView(activity);
            }

            @Override
            public void onStop() {
                mActivityAdPage.onStop();
            }

            @Override
            public void onPause() {
                mActivityAdPage.onPause();
                mActivityAnalytics.onPause();
                BDGameSDK.onPause(activity);

//                // 关闭悬浮球
//                BDGameSDK.closeFloatView(activity);
            }

            @Override
            public void onDestroy() {
                mActivityAdPage.onDestroy();
                BDGameSDK.destroy();
            }

        });

        BDGameSDKSetting mBDGameSDKSetting = new BDGameSDKSetting();
        mBDGameSDKSetting.setAppID(appID);// APPID设置
        mBDGameSDKSetting.setAppKey(appKey);// APPKEY设置
        mBDGameSDKSetting.setDomain(domain);// 设置为正式模式
        mBDGameSDKSetting.setOrientation(Utils.getOrientation(activity));

        BDGameSDK.init(activity, mBDGameSDKSetting, new IResponse<Void>() {

            @Override
            public void onResponse(int resultCode, String resultDesc, Void extraData) {
                switch (resultCode) {
                    case ResultCode.INIT_SUCCESS:
                        // 初始化成功
                        U8SDK.getInstance().onResult(U8Code.CODE_INIT_SUCCESS, "baidu sdk init success");

                        U8SDK.getInstance().runOnMainThread(new Runnable() {

                            @Override
                            public void run() {
                                BDGameSDK.getAnnouncementInfo(activity);
                            }
                        });

                        break;

                    case ResultCode.INIT_FAIL:
                    default:
                        U8SDK.getInstance().onResult(U8Code.CODE_INIT_FAIL, "baidu sdk init failed.");
                }
            }
        });

    }

    public void login() {
        if (!SDKTools.isNetworkAvailable(activity)) {
            U8SDK.getInstance().onResult(U8Code.CODE_NO_NETWORK, "The network now is unavailable");
            return;
        }

        BDGameSDK.login(activity, new IResponse<Void>() {

            @Override
            public void onResponse(int resultCode, String resultDesc, Void extraData) {
                Log.d(TAG, "this resultCode is " + resultCode);
                switch (resultCode) {
                    case ResultCode.LOGIN_SUCCESS:
                        String token = BDGameSDK.getLoginAccessToken();
//						String uid = BDGameSDK.getLoginUid();// 获取账号 uid
                        U8SDK.getInstance().onResult(U8Code.CODE_LOGIN_SUCCESS, token);
                        U8SDK.getInstance().onLoginResult(token);

                        // 显示悬浮窗
                        BDGameSDK.showFloatView(activity);

                        setSuspendWindowChangeAccountListener();
                        setSessionInvalidListener();
                        break;
                    case ResultCode.LOGIN_CANCEL:
                        U8SDK.getInstance().onResult(U8Code.CODE_LOGIN_FAIL, "baidu sdk login canceled");
                        break;
                    case ResultCode.LOGIN_FAIL:
                    default:
                        U8SDK.getInstance().onResult(U8Code.CODE_LOGIN_FAIL, "baidu sdk login failed " + resultCode);
                }
            }
        });


    }

    public void submitExtendData(UserExtraData extraData) {
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

    public void logout() {
        BDGameSDK.logout();
    }

    public void exitSDK() {
        BDGameSDK.gameExit(activity, new OnGameExitListener() {

            @Override
            public void onGameExit() {
                activity.finish();
                System.exit(0);
            }
        });
    }

    public void pay(final PayParams params) {
        PayOrderInfo payOrderInfo = buildOrderInfo(params);
        if (payOrderInfo == null) {
            return;
        }

        if (!SDKTools.isNetworkAvailable(activity)) {
            U8SDK.getInstance().onResult(U8Code.CODE_NO_NETWORK, "The network now is unavailable");
            return;
        }

        TDGAVirtualCurrency.onChargeRequest(params.getOrderID(), params.getProductName(), params.getPrice(), "CNY", params.getPrice() * 7, "baidu SDK");
        BDGameSDK.pay(activity, payOrderInfo, null, new IResponse<PayOrderInfo>() {

            @Override
            public void onResponse(int resultCode, String resultDesc, PayOrderInfo extraData) {
                switch (resultCode) {
                    case ResultCode.PAY_SUCCESS:// 支付成功
                        TDGAVirtualCurrency.onChargeSuccess(params.getOrderID());
                        U8SDK.getInstance().onResult(U8Code.CODE_PAY_SUCCESS, "baidu sdk pay success.");
                        break;
                    case ResultCode.PAY_CANCEL:// 订单支付取消
                        U8SDK.getInstance().onResult(U8Code.CODE_PAY_CANCEL, "baidu sdk pay canceled.");
                        break;
                    case ResultCode.PAY_FAIL:// 订单支付失败
                        U8SDK.getInstance().onResult(U8Code.CODE_PAY_FAIL, "baidu sdk pay failed.");
                        break;
                    case ResultCode.PAY_SUBMIT_ORDER:// 订单已经提交，支付结果未知（比如：已经请求了，但是查询超时）
                        U8SDK.getInstance().onResult(U8Code.CODE_PAY_UNKNOWN, "baidu sdk pay order unknown.");
                        break;
                }
            }

        });

    }

    /**
     * 构建订单信息
     */
    private PayOrderInfo buildOrderInfo(PayParams params) {
        String cpOrderId = params.getOrderID();
        String goodsName = params.getProductName();
        String totalAmount = (params.getPrice() * 100) + "";// 支付总金额 （以分为单位）
        int ratio = params.getRatio();// 该参数为非定额支付时生效 (支付金额为0时为非定额支付,具体参见使用手册)
        String extInfo = "";// 扩展字段，该信息在支付成功后原样返回给CP
        String uid = BDGameSDK.getLoginUid();// 获取账号 uid

        if (TextUtils.isEmpty(totalAmount)) {
            totalAmount = "0";
        }

        PayOrderInfo payOrderInfo = new PayOrderInfo();
        payOrderInfo.setCooperatorOrderSerial(cpOrderId);
        payOrderInfo.setProductName(goodsName);
        long p = Long.parseLong(totalAmount);
        payOrderInfo.setTotalPriceCent(p);// 以分为单位
        payOrderInfo.setRatio(ratio);
        payOrderInfo.setExtInfo(extInfo);// 该字段将会在支付成功后原样返回给CP(不超过500个字符)
        payOrderInfo.setCpUid(uid);

        return payOrderInfo;
    }

    private void setSessionInvalidListener() {
        BDGameSDK.setSessionInvalidListener(new IResponse<Void>() {

            @Override
            public void onResponse(int resultCode, String resultDesc, Void extraData) {
                if (resultCode == ResultCode.SESSION_INVALID) {
                    U8SDK.getInstance().onLogout();
                }
            }
        });
    }

    // 设置切换账号事件监听（个人中心界面切换账号）
    private void setSuspendWindowChangeAccountListener() {
        BDGameSDK.setSuspendWindowChangeAccountListener(activity, new IResponse<Void>() {

            @Override
            public void onResponse(int resultCode, String resultDesc, Void extraData) {
                switch (resultCode) {
                    case ResultCode.LOGIN_SUCCESS:
                        // TODO 登录成功，不管之前是什么登录状态，游戏内部都要切换成新的用户
                        U8SDK.getInstance().onSwitchAccount();
                        String token = BDGameSDK.getLoginAccessToken();
                        U8SDK.getInstance().onResult(U8Code.CODE_LOGIN_SUCCESS, token);
                        break;
                    case ResultCode.LOGIN_FAIL:
                        U8SDK.getInstance().onResult(U8Code.CODE_LOGIN_FAIL, "baidu sdk login failed " + resultCode);
                        break;
                    case ResultCode.LOGIN_CANCEL:
                        // TODO 取消，操作前后的登录状态没变化
                        U8SDK.getInstance().onResult(U8Code.CODE_LOGIN_FAIL, "baidu sdk login canceled");
                        break;

                }
            }

        });
    }
}
