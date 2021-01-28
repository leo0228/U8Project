package com.u8.sdk;

import org.json.JSONObject;

import com.papa91.pay.callback.PPLoginCallBack;
import com.papa91.pay.callback.PPayCallback;
import com.papa91.pay.callback.PpaLogoutCallback;
import com.papa91.pay.pa.activity.PaayActivity;
import com.papa91.pay.pa.business.LoginResult;
import com.papa91.pay.pa.business.PPayCenter;
import com.papa91.pay.pa.business.PaayArg;
import com.papa91.pay.pa.dto.LogoutResult;
import com.tendcloud.tenddata.TDGAProfile;
import com.tendcloud.tenddata.TDGAVirtualCurrency;
import com.zjtx.prompt.PromptDialog;

import android.app.Activity;
import android.util.Log;

public class WuFanSDK {
    private final String TAG = "wufansdk";

    public static WuFanSDK instance;
    private Activity activity;
    private int openUid = 0;

    private String appName;
    private String callBackUrl;


    private WuFanSDK() {
        activity = U8SDK.getInstance().getContext();
    }

    public static WuFanSDK getInstance() {
        if (instance == null) {
            instance = new WuFanSDK();
        }
        return instance;
    }

    public void intiSDK(SDKParams params) {
        appName = params.getString("gameName");
        callBackUrl = params.getString("callBackUrl");
        Log.e(TAG, "appName = " + appName);
        Log.e(TAG, "callBackUrl = " + callBackUrl);

        PromptDialog.init(activity, new PromptDialog.OnPromptListener() {
            @Override
            public void onInitSDK() {
                initSDK();
            }
        });

    }

    private void initSDK() {
        PPayCenter.init(activity);
        //sdk主动切换帐号使用
//        PPayCenter.initChangAccountCallback(new PPChangeAccountCallBack() {
//            @Override
//            public void onChangeAccount() {
//                //这里可以去做切换账户退回游戏登录界面等操作
//
//                //完成以后调下面的切换账户接口
//                PPayCenter.changeAccount(new PPLoginCallBack() {
//                    @Override
//                    public void onLoginFinish(LoginResult result) {
//
//                    }
//                });
//            }
//        });

        U8SDK.getInstance().setActivityCallback(new ActivityCallbackAdapter() {
            @Override
            public void onDestroy() {
                PPayCenter.destroy();
                super.onDestroy();
            }

            @Override
            public void onResume() {
                PPayCenter.onResume(activity);
                super.onResume();
            }

            @Override
            public void onPause() {
                PPayCenter.onPause(activity);
                super.onPause();
            }

        });
    }

    public void login() {
        Log.e(TAG, "login is start");

        PPayCenter.login(activity, new PPLoginCallBack() {

            @Override
            public void onLoginFinish(LoginResult result) {
                Log.i(TAG, "result:" + result.toString());
                switch (result.getCode()) {
                    case LoginResult.LOGIN_CODE_APPID_NOT_FOUND:
                        U8SDK.getInstance().onResult(U8Code.CODE_LOGIN_FAIL, "login fail not found appid");
                        break;
                    case LoginResult.LOGIN_CODE_SUCCESS:// 登录成功
                        openUid = result.getOpenUid();// 返回openUid
                        String token = result.getToken();// 返回token

                        U8SDK.getInstance().onResult(U8Code.CODE_LOGIN_SUCCESS, "login success");
                        String loginResult = encodeLoginResult(openUid, token);
                        U8SDK.getInstance().onLoginResult(loginResult);

                        PPayCenter.loginFinish(activity);

                        if (1 == result.getRealNameStatus()) {
                            Log.i(TAG, "已实名");
                        } else if (2 == result.getRealNameStatus()) {
                            Log.i(TAG, "未实名");
                        } else if (3 == result.getRealNameStatus()) {
                            Log.i(TAG, "未成年");
                        }

                        break;
                    case LoginResult.LOGIN_CODE_FAILED:// 登录失败
                        U8SDK.getInstance().onResult(U8Code.CODE_LOGIN_FAIL, "login failed");
                        break;
                    case LoginResult.LOGIN_CODE_CANCEL:// 登录取消
                        U8SDK.getInstance().onResult(U8Code.CODE_LOGIN_FAIL, "login cancel");
                        break;
                    case LoginResult.NOT_INIT:
                        PPayCenter.init(activity);
                        break;
                }
            }
        });
    }

    private String encodeLoginResult(int openUid, String token) {

        JSONObject json = new JSONObject();

        try {
            json.put("uid", openUid);
            json.put("token", token);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return json.toString();
    }

    public void submitExtraData(UserExtraData data) {

        switch (data.getDataType()) {
            case UserExtraData.TYPE_ENTER_GAME:
                PPayCenter.enterGame(data.getRoleName(), String.valueOf(data.getServerID()), data.getRoleID(),
                        data.getRoleLevel());
                enterGame(data);
                break;
            case UserExtraData.TYPE_CREATE_ROLE:
                PPayCenter.createRole(data.getRoleName(), String.valueOf(data.getServerID()));
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
    public void showUserCenter() {
        PPayCenter.userCenter(activity);
    }

    public void switchLogin() {
        U8SDK.getInstance().onLogout();
        PPayCenter.changeAccount(new PPLoginCallBack() {

            @Override
            public void onLoginFinish(LoginResult result) {
                switch (result.getCode()) {
                    case LoginResult.LOGIN_CODE_APPID_NOT_FOUND:
                        // 没找到appid
                        U8SDK.getInstance().onResult(U8Code.CODE_LOGIN_FAIL, "login fail not found appid");
                        break;
                    case LoginResult.LOGIN_CODE_SUCCESS:// 登录成功
                        openUid = result.getOpenUid();// 返回openUid
                        String token = result.getToken();// 返回token
                        U8SDK.getInstance().onResult(U8Code.CODE_LOGIN_SUCCESS, "login success");
                        String loginResult = encodeLoginResult(openUid, token);
                        U8SDK.getInstance().onLoginResult(loginResult);

                        PPayCenter.loginFinish(activity);

                        if (1 == result.getRealNameStatus()) {
                            Log.i(TAG, "已实名");
                        } else if (2 == result.getRealNameStatus()) {
                            Log.i(TAG, "未实名");
                        } else if (3 == result.getRealNameStatus()) {
                            Log.i(TAG, "未成年");
                        }

                        break;
                    case LoginResult.LOGIN_CODE_FAILED:// 登录失败
                        U8SDK.getInstance().onResult(U8Code.CODE_LOGIN_FAIL, "login failed");
                        break;
                    case LoginResult.LOGIN_CODE_CANCEL:// 登录取消
                        U8SDK.getInstance().onResult(U8Code.CODE_LOGIN_FAIL, "login cancel");
                        break;
                    case LoginResult.NOT_INIT:
                        PPayCenter.init(activity);
                        break;
                }

            }
        });
    }

    public void exit() {
        if (openUid != 0) {
            PPayCenter.loginOut(activity, openUid, new PpaLogoutCallback() {
                @Override
                public void onLoginOut(LogoutResult result) {
                    // TODO Auto-generated method stub
                    switch (result.getCode()) {
                        case LogoutResult.LOGOUT_CODE_OUT:
                            activity.finish();
                            System.exit(0);
                            break;
                        default:
                            break;
                    }
                }
            });
        }
    }

    public void logout() {
        U8SDK.getInstance().onResult(U8Code.CODE_LOGOUT_SUCCESS, "");
        U8SDK.getInstance().onLogout();
    }

    public void Pay(final PayParams params) {
        Log.i(TAG, "pay params == " + params.toString());

        PaayArg paayArg = new PaayArg();
        paayArg.APP_NAME = appName;
        paayArg.APP_ORDER_ID = params.getOrderID();
        paayArg.APP_USER_ID = params.getRoleId();
        paayArg.APP_USER_NAME = params.getRoleName();
        paayArg.GAME_SERVER = params.getServerId();
        paayArg.PRODUCT_ID = params.getProductId();
        paayArg.PRODUCT_NAME = params.getProductName();
        paayArg.MONEY_AMOUNT = params.getPrice() + "";
        paayArg.NOTIFY_URI = callBackUrl;
        if (openUid != 0) {
            paayArg.PA_OPEN_UID = openUid;
        }
        paayArg.APP_EXT1 = params.getExtension();
        // paayArg.APP_EXT2 = "ext2...";

        TDGAVirtualCurrency.onChargeRequest(params.getOrderID(), params.getProductName(), params.getPrice(), "CNY", params.getPrice() * 7,"wufan SDK");

        PPayCenter.pay(paayArg, new PPayCallback() {
            @Override
            public void onPayFinished(int status) {
                Log.e(TAG, "status == " + status);

                switch (status) {
                    case PaayActivity.PAPAPay_RESULT_CODE_SUCCESS:
                        TDGAVirtualCurrency.onChargeSuccess(params.getOrderID());

                        U8SDK.getInstance().onResult(U8Code.CODE_PAY_SUCCESS, "pay success");
                        break;
                    case PaayActivity.PAPAPay_RESULT_CODE_FAILURE:
                        U8SDK.getInstance().onResult(U8Code.CODE_PAY_FAIL, "pay failed");
                        break;
                    case PaayActivity.PAPAPay_RESULT_CODE_CANCEL:
                        U8SDK.getInstance().onResult(U8Code.CODE_PAY_CANCEL, "pay failed");
                        break;
                    case PaayActivity.PAPAPay_RESULT_CODE_WAIT:
                        break;
                    default:
                        U8SDK.getInstance().onResult(U8Code.CODE_PAY_FAIL, "pay failed");
                        break;
                }
            }
        });
    }

}
