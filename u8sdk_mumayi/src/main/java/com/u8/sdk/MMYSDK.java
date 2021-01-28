package com.u8.sdk;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.mumayi.paymentmain.business.ILoginCallback;
import com.mumayi.paymentmain.business.ILogoutCallback;
import com.mumayi.paymentmain.business.ITradeCallback;
import com.mumayi.paymentmain.ui.PaymentCenterInstance;
import com.mumayi.paymentmain.ui.PaymentUsercenterContro;
import com.mumayi.paymentmain.ui.pay.MMYInstance;
import com.mumayi.paymentmain.util.PaymentConstants;
import com.tendcloud.tenddata.TDGAProfile;
import com.tendcloud.tenddata.TDGAVirtualCurrency;
import com.zjtx.prompt.PromptDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;

public class MMYSDK {
    private Activity activity;
    private String TAG = "MMYSDK";
    private static MMYSDK instance;

    private PaymentCenterInstance sdkInstance;
    private PaymentUsercenterContro userCenter;
//	private PaymentFloatInteface floatInteface;

    private String appKey;
    private String gameName;

    private MMYSDK() {
        activity = U8SDK.getInstance().getContext();
    }

    public static MMYSDK getInstance() {
        if (instance == null) {
            instance = new MMYSDK();

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
        this.appKey = params.getString("MuMaYi_AppKey");
        this.gameName = params.getString("MuMaYi_GameName");
        Log.e(TAG, "appKey == " + appKey);
        Log.e(TAG, "gameName == " + gameName);
    }

    protected String[] needPermissions = {Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_COARSE_LOCATION};

    private void initSDK() {

        if (android.os.Build.VERSION.SDK_INT >= 23) {

            // 检查权限，开启必要权限
            if (ContextCompat.checkSelfPermission(activity,
                    Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, needPermissions, 0);
            }
        }

        U8SDK.getInstance().setActivityCallback(new ActivityCallbackAdapter() {

            @Override
            public void onResume() {
                // 检查用户是否登录。没登陆就跳转到登录页面
                // userCenter.checkLogin();

                // 显示悬浮窗
                if (userCenter != null) {
                    Log.e(TAG, "显示悬浮窗");

                    userCenter.showFloat();
                }

//				 if(floatInteface != null) {
//				 floatInteface.show();
//				 }
            }

            @Override
            public void onPause() {
                // 关闭悬浮窗
                if (userCenter != null) {
                    Log.e(TAG, "关闭悬浮窗");

                    userCenter.closeFloat();
                }

//				 if(floatInteface != null) {
//				 floatInteface.close();
//				 }
            }

            @Override
            public void onDestroy() {
                if (sdkInstance != null) {
                    sdkInstance.finish();
                }

                if (userCenter != null) {
                    userCenter.finish();
                }
            }


        });

        initMMY();
    }

    private void initMMY() {
        Log.e(TAG, "initMMY == ");

        try {
            sdkInstance = PaymentCenterInstance.getInstance(activity);

            String s2 = new String(gameName.getBytes("UTF-8"));
            sdkInstance.initial(appKey, s2);
            // 设置是否开启 bug模式 ,true打开可以显示 Log日 志,false不显示
            sdkInstance.setTestMode(false);
            // 设置切换完账号后是否自动跳转登陆,可以设置为false,根据需求
            sdkInstance.setChangeAccountAutoToLogin(false);
            // 确保每次在刚进入游戏都会检测本地数据
            sdkInstance.findUserInfo();
            // 检测是否有开启悬浮窗的权限
            sdkInstance.checkFloatPermission();

            userCenter = sdkInstance.getUsercenterApi(activity);
            userCenter.getForumInfo();

//			 floatInteface = sdkInstance.createFloat();

            sdkInstance.setLoginCallBack(new ILoginCallback() {
                @Override
                public void onLoginSuccess(String loginSuccess) {

                    Log.d(TAG, "这是登陆成功的回调数据 ----->" + loginSuccess);

                    try {
                        JSONObject loginobject = new JSONObject(loginSuccess);
                        String loginState = loginobject.getString(PaymentConstants.LOGIN_STATE);
                        if (loginState != null && loginState.equals(PaymentConstants.STATE_SUCCESS)) {

                            String uname = loginobject.getString("uname");
                            String uid = loginobject.getString("uid");
                            String token = loginobject.getString("token");
                            String session = loginobject.getString("session");

                            U8SDK.getInstance().onResult(U8Code.CODE_LOGIN_SUCCESS, uid);
                            String loginResult = encodeLoginResult(uname, uid, token, session, uname);
                            U8SDK.getInstance().onLoginResult(loginResult);

                            if (userCenter != null) {
                                Log.e(TAG, "显示悬浮窗");
                                userCenter.showFloat();
                            }

//								if(floatInteface != null) {
//									 floatInteface.show();
//									 }

                        } else {
                            U8SDK.getInstance().onResult(U8Code.CODE_LOGIN_FAIL, "login failed");
                        }
                    } catch (JSONException e) {
                        Log.d(TAG, "解析登陆回调异常" + e);
                        U8SDK.getInstance().onResult(U8Code.CODE_LOGIN_FAIL, e.getMessage());
                        e.printStackTrace();
                    }

                }

                @Override
                public void onLoginFail(String status, String logiFail) {
                    Log.d(TAG, "登录失败" + logiFail);
                    U8SDK.getInstance().onResult(U8Code.CODE_LOGIN_FAIL, logiFail);
                }

            });

            sdkInstance.setLogoutCallback(new ILogoutCallback() {

                @Override
                public void onLogoutSuccess(String logoutSuccess) {
                    U8SDK.getInstance().onResult(U8Code.CODE_LOGOUT_SUCCESS, logoutSuccess);
                }

                @Override
                public void onLogoutFail(String logoutFail) {
                    U8SDK.getInstance().onResult(U8Code.CODE_LOGOUT_FAIL, logoutFail);
                }
            });

            sdkInstance.setTradeCallback(new ITradeCallback() {
                @Override
                public void onTradeSuccess(String tradeType, int code, Intent intent) {
                    if (code == MMYInstance.PAY_RESULT_SUCCESS) {
                        TDGAVirtualCurrency.onChargeSuccess(orderId);

                        U8SDK.getInstance().onResult(U8Code.CODE_PAY_SUCCESS, "pay success");
                    } else {
                        U8SDK.getInstance().onResult(U8Code.CODE_PAY_FAIL, "pay failed.code:" + code);
                    }
                }

                @Override
                public void onTradeFail(String tradeType, int code, Intent intent) {
                    U8SDK.getInstance().onResult(U8Code.CODE_PAY_FAIL, "pay failed.code:" + code);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String encodeLoginResult(String username, String uid, String token, String session, String uname) {

        JSONObject json = new JSONObject();
        String urlString = "";
        try {
            json.put("uid", uid);
            json.put("username", uname);
            json.put("token", token);
            // 将普通字符创转换成application/x-www-from-urlencoded字符串
            urlString = URLEncoder.encode(json.toString(), "GBK");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return urlString;
    }

    public void submitGameData(UserExtraData extraData) {
        // TODO::调用AAA上报玩家数据接口
        // 如果AAA没有提供对应的接口，则不用实现该方法
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

        // userCenter.go2Login();
        if (sdkInstance != null) {
            sdkInstance.go2Login(activity);
        }

    }

    public void logout() {
        U8SDK.getInstance().onLogout();
        U8SDK.getInstance().onResult(U8Code.CODE_LOGOUT_SUCCESS, "logout");
    }

    public void exit() {
        if (sdkInstance != null) {
            sdkInstance.exit();
        }
    }

    public void enterUserCenter() {
        if (userCenter != null) {
            userCenter.go2Ucenter();
        }
    }

    private String orderId;

    public void pay(PayParams params) {

        if (!SDKTools.isNetworkAvailable(activity)) {
            U8SDK.getInstance().onResult(U8Code.CODE_NO_NETWORK, "The network now is unavailable");
            return;
        }

        orderId = params.getOrderID();
        String price = params.getPrice() + ".00";
        String productName = params.getProductName();

        if (sdkInstance != null) {
            sdkInstance.setUserArea(params.getServerId());
            sdkInstance.setUserName(params.getRoleName());
            sdkInstance.setUserLevel(params.getRoleLevel());
        }

        if (userCenter != null) {
            TDGAVirtualCurrency.onChargeRequest(orderId, productName, params.getPrice(), "CNY", params.getPrice() * 7, "mumayi SDK");
            userCenter.pay(activity, productName, price, orderId);
        }
    }
}
