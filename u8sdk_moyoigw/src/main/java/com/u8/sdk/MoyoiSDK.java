package com.u8.sdk;

import org.json.JSONException;
import org.json.JSONObject;

import com.nj9you.sdk.Nj9youSdk;
import com.nj9you.sdk.listener.OnUserExchangeListener;
import com.nj9you.sdk.listener.OnUserLoginListener;
import com.nj9you.sdk.listener.OnUserLogoutListener;
import com.nj9you.sdk.listener.PayCallback;
import com.nj9you.sdk.share.FacebookShare;
import com.nj9you.sdk.share.ShareCallback;
import com.nj9you.sdk.utils.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import java.security.MessageDigest;

public class MoyoiSDK {
    private String TAG = "moyoisdk";

    private static MoyoiSDK instance;

    private String sdk_Appid;
    private String cloud_channel;

    private Activity activity;

    SharedPreferences sp;
    Editor editor;

    private MoyoiSDK() {
        activity = U8SDK.getInstance().getContext();
    }

    public static MoyoiSDK getInstance() {
        if (instance == null) {
            instance = new MoyoiSDK();
        }
        return instance;
    }

    public void initSDK(SDKParams params) {
        parseSDKParams(params);
        initSDK();
    }

    private void parseSDKParams(SDKParams params) {
        this.sdk_Appid = params.getString("sdk_Appid");
        this.cloud_channel = params.getString("cloud_channel");
    }

    public void initSDK() {

        try {
            int i = 0;
            PackageInfo info = activity.getPackageManager().getPackageInfo(activity.getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                i++;
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String KeyHash = Base64.encodeToString(md.digest(), Base64.DEFAULT);
                //KeyHash 就是你要的，不用改任何代码  复制粘贴 ;
                Log.i("tyl", "KeyHash=" + KeyHash);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        sp = activity.getSharedPreferences("usercenter", 0);
        editor = sp.edit();

        try {
            Nj9youSdk.init(activity, sdk_Appid, cloud_channel);
            U8SDK.getInstance().onResult(U8Code.CODE_INIT_SUCCESS, "init success");

            Nj9youSdk.showPrivacyPolicy(activity);

//            Nj9youSdk.checkFloatPermission();

            Nj9youSdk.checkUpdate(activity);

            U8SDK.getInstance().setActivityCallback(new ActivityCallbackAdapter() {
                @Override
                public void onResume() {
                    super.onResume();
                    Nj9youSdk.showUserCenter();
                    Nj9youSdk.onResume();
                }

                @Override
                public void onPause() {
                    super.onPause();
                    Nj9youSdk.hideUserCenter();
                    Nj9youSdk.onPause();
                }

                @Override
                public void onDestroy() {
                    super.onDestroy();
                    Nj9youSdk.destory();
                }

                @Override
                public void onActivityResult(int requestCode, int resultCode, Intent data) {
                    super.onActivityResult(requestCode, resultCode, data);
                    Nj9youSdk.onActivityResult(requestCode, resultCode, data);
                }
            });

            /**
             * 设置切换账号回调接口
             */
            Nj9youSdk.setExchangeAccountListener(new OnUserExchangeListener() {
                @Override
                public void onExchangeComplete(boolean success, String loginInfo) {
                    if (success) {
                        Log.e(TAG, "exchange success");
                        try {
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

            /**
             * 设置登录回调接口
             */
            Nj9youSdk.setLoginAccountListener(new OnUserLoginListener() {
                @Override
                public void onLoginComplete(boolean success, String loginInfo) {
                    if (success) {
                        Log.e(TAG, "login success");
                        try {
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

            /**
             * 设置注销账号回调接口
             */
            Nj9youSdk.setLogoutListener(new OnUserLogoutListener() {
                @Override
                public void onLogoutComplete(boolean success, String username) {
                    if (success) {
                        Log.e(TAG, "logout success");
                        U8SDK.getInstance().onLogout();
                        U8SDK.getInstance().onResult(U8Code.CODE_LOGOUT_SUCCESS, "logout success" + username);
                    } else {
                        U8SDK.getInstance().onResult(U8Code.CODE_LOGOUT_FAIL, "logout fail");
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            U8SDK.getInstance().onResult(U8Code.CODE_INIT_FAIL, "init failed." + e.getMessage());
        }
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
        Log.e(TAG, "LoginResult:" + json.toString());
        return json.toString();
    }

    public void logout() {
        Nj9youSdk.logout(activity);
    }


    public void pay(final PayParams params) {
        Log.e(TAG, "Pay:" + params.toString());

        com.nj9you.sdk.params.PayParams njParams = getParams(params);
        Nj9youSdk.pay(activity, njParams, new PayCallback() {
            @Override
            public void onPayResult(int error, String desc, com.nj9you.sdk.params.PayParams njParams) {
                Log.i("U8SDK", "The PAY is " + desc);

                if (error == com.nj9you.sdk.framework.PayResult.PAY_SUCCESS) {
                    U8SDK.getInstance().onResult(U8Code.CODE_PAY_SUCCESS, desc);
                } else if (error == com.nj9you.sdk.framework.PayResult.PAY_FAILURE) {
                    U8SDK.getInstance().onResult(U8Code.CODE_PAY_FAIL, desc);
                } else if (error == com.nj9you.sdk.framework.PayResult.PAY_CANCEL) {
                    U8SDK.getInstance().onResult(U8Code.CODE_PAY_CANCEL, desc);
                } else if (error == com.nj9you.sdk.framework.PayResult.PAY_WAITING) {
                    U8SDK.getInstance().onResult(U8Code.CODE_PAY_UNKNOWN, desc);
                } else if (error == com.nj9you.sdk.framework.PayResult.PAY_ONGOING) {
                    U8SDK.getInstance().onResult(U8Code.CODE_PAY_FAIL, desc);
                }
            }
        });
    }

    public com.nj9you.sdk.params.PayParams getParams(PayParams params) {
        com.nj9you.sdk.params.PayParams payParams = new com.nj9you.sdk.params.PayParams();
        payParams.setSubject(params.getProductName());
        payParams.setPrice((params.getPrice()) + "");
        payParams.setAttach(params.getExtension());
        payParams.setOrder(params.getOrderID());
        payParams.setBody(params.getProductDesc());
        payParams.setServer(params.getServerId());
        payParams.setItemid(params.getProductId());
        return payParams;
    }

    public void share(ShareParams params) {
        Log.e(TAG, "Share:" + params.toString());

        FacebookShare.getInstance().createApi(activity, new ShareCallback() {

            @Override
            public void sharedSuccess() {
                U8SDK.getInstance().onResult(U8Code.CODE_SHARE_SUCCESS, "share success");
            }

            @Override
            public void sharedFailed() {
                U8SDK.getInstance().onResult(U8Code.CODE_SHARE_FAILED, "share failes");
            }

            @Override
            public void sharedCancel() {
                U8SDK.getInstance().onResult(U8Code.CODE_SHARE_FAILED, "share cancel");
            }
        });

        FacebookShare.getInstance().shareToImage(params.getImgUrl());
    }

    public void exit() {
        Nj9youSdk.exit(activity, new Nj9youSdk.OnExitCallback() {
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

}
