package com.u8.sdk;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

import com.flamingo.sdk.access.Callback;
import com.flamingo.sdk.access.GPApiFactory;
import com.flamingo.sdk.access.GPExitResult;
import com.flamingo.sdk.access.GPPayResult;
import com.flamingo.sdk.access.GPSDKGamePayment;
import com.flamingo.sdk.access.GPSDKInitResult;
import com.flamingo.sdk.access.GPSDKPlayerInfo;
import com.flamingo.sdk.access.GPUploadPlayerInfoResult;
import com.flamingo.sdk.access.GPUserResult;
import com.flamingo.sdk.access.IGPApi;
import com.flamingo.sdk.access.IGPExitObsv;
import com.flamingo.sdk.access.IGPPayObsv;
import com.flamingo.sdk.access.IGPSDKInitObsv;
import com.flamingo.sdk.access.IGPSDKInnerEventObserver;
import com.flamingo.sdk.access.IGPUploadPlayerInfoObsv;
import com.flamingo.sdk.access.IGPUserObsv;
import com.tendcloud.tenddata.TDGAProfile;
import com.tendcloud.tenddata.TDGAVirtualCurrency;
import com.u8.sdk.log.Log;
import com.zjtx.prompt.PromptDialog;

import org.json.JSONObject;

public class GuoPanSDK {
    private Activity activity;
    private final String TAG = "GuoPanSDK";
    private static GuoPanSDK instance;

    private String appID;
    private String appKey;
    private IGPApi mIGPApi;

    private GuoPanSDK() {
        activity = U8SDK.getInstance().getContext();
    }

    public static GuoPanSDK getInstance() {
        if (instance == null) {
            instance = new GuoPanSDK();
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
        this.appID = params.getString("GuoPan_AppID");
        this.appKey = params.getString("GuoPan_AppKey");
        Log.e(TAG, appID);
        Log.e(TAG, appKey);
    }

    private void initSDK() {

        U8SDK.getInstance().setActivityCallback(new ActivityCallbackAdapter() {
            @Override
            public void onStart() {
                // TODO Auto-generated method stub
                super.onStart();
                if (mIGPApi != null) {
                    mIGPApi.onStart(activity);
                }
            }

            @Override
            public void onRestart() {
                // TODO Auto-generated method stub
                super.onRestart();
                if (mIGPApi != null) {
                    mIGPApi.onRestart(activity);
                }
            }

            @Override
            public void onPause() {
                // TODO Auto-generated method stub
                super.onPause();
                if (mIGPApi != null) {
                    mIGPApi.onPause(activity);
                }
            }

            @Override
            public void onResume() {
                // TODO Auto-generated method stub
                super.onResume();
                if (mIGPApi != null) {
                    mIGPApi.onResume(activity);
                }
            }

            @Override
            public void onStop() {
                // TODO Auto-generated method stub
                super.onStop();
                if (mIGPApi != null) {
                    mIGPApi.onStop(activity);
                }
            }

            @Override
            public void onDestroy() {
                // TODO Auto-generated method stub
                super.onDestroy();
                if (mIGPApi != null) {
                    mIGPApi.onDestroy(activity);
                }
            }

            @Override
            public void onActivityResult(int requestCode, int resultCode, Intent data) {
                // TODO Auto-generated method stub
                super.onActivityResult(requestCode, resultCode, data);
                if (mIGPApi != null) {
                    mIGPApi.onActivityResult(activity, requestCode, resultCode, data);
                    ;
                }
            }

            @Override
            public void onNewIntent(Intent newIntent) {
                // TODO Auto-generated method stub
                super.onNewIntent(newIntent);
                if (mIGPApi != null) {
                    mIGPApi.onNewIntent(activity);
                }
            }
        });

        initGuoPan();

    }

    private void initGuoPan() {
        // 这是兼容当targetSdk设置为23或者以上的情况，如果targetSdk在22或者以下则可以直接同步调用GPApiFactory.getGPApi()即可
        GPApiFactory.getGPApiForMarshmellow(activity, new Callback() {

            @Override
            public void onCallBack(IGPApi igpApi) {
                mIGPApi = igpApi;
                // 打开日志、发布状态切记不要打开
                mIGPApi.setLogOpen(false);
                mIGPApi.onCreate(activity);
                // sdk内部事件回调接口
                mIGPApi.setSDKInnerEventObserver(new IGPSDKInnerEventObserver() {

                    @Override
                    public void onSdkSwitchAccount() {
                        // 新版这个回调废弃，这里可以留空

                    }

                    @Override
                    public void onSdkLogout() {
                        // sdk内部登出了，游戏应该回到登录界面，然后重新调用登陆
                        Log.e(TAG, "onSdkLogout");
                        U8SDK.getInstance().onLogout();
                        U8SDK.getInstance().onResult(U8Code.CODE_LOGOUT_SUCCESS, "logout success");
                    }
                });

                mIGPApi.initSdk(activity, appID, appKey, new IGPSDKInitObsv() {

                    @Override
                    public void onInitFinish(GPSDKInitResult result) {
                        Log.e(TAG, "init:" + result.mInitErrCode);
                        if (result.mInitErrCode == GPSDKInitResult.GPInitErrorCodeNone) {
                            U8SDK.getInstance().onResult(U8Code.CODE_INIT_SUCCESS, "init success");
                        } else {
                            U8SDK.getInstance().onResult(U8Code.CODE_INIT_FAIL, "init failed");
                        }
                    }
                });
            }
        });


        // TargetSdkApi < 23
        // mIGPApi = GPApiFactory.getGPApi();
        // mIGPApi.setSDKInnerEventObserver(new IGPSDKInnerEventObserver() {
        // @Override
        // public void onSdkLogout() {
        // // sdk内部登出了，游戏应该回到登录界面，然后重新调用登陆
        // writeLog("sdk登出回调:登录成功");
        // mIGPApi.login(MainActivity.this.getApplication(), mUserObsv);
        // }
        //
        // @Override
        // public void onSdkSwitchAccount() {
        // // sdk内部切换了账号，这个时候游戏也应该回到登录界面，然后重新获取新的账号的参数（相当于sdk登录成功回调了）
        // writeLog("sdk切换回调:登录成功");
        // writeLog("可通过getLoginUin获取用户唯一uid");
        // writeLog("可通过getLoginToken获取用户的令牌");
        // }
        // });
    }

    private String encodeLoginResult(String uni, String token, String name) {

        JSONObject json = new JSONObject();

        try {
            json.put("uin", uni);
            json.put("token", token);
            json.put("username", name);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return json.toString();
    }

    public void login() {
        mIGPApi.login(activity, mUserObsv);
    }

    public void switchLogin() {
        mIGPApi.reLogin(activity, mUserObsv);
    }

    /**
     * 登录回调接口
     */
    private IGPUserObsv mUserObsv = new IGPUserObsv() {

        @Override
        public void onFinish(GPUserResult result) {
            Log.e(TAG, "login:" + result.mErrCode);
            if (result.mErrCode == GPUserResult.USER_RESULT_LOGIN_SUCC) {
                U8SDK.getInstance().onResult(U8Code.CODE_LOGIN_SUCCESS, mIGPApi.getLoginUin());
                String loginResult = encodeLoginResult(mIGPApi.getLoginUin(), mIGPApi.getLoginToken(),
                        mIGPApi.getAccountName());

                U8SDK.getInstance().onLoginResult(loginResult);
            } else {
                U8SDK.getInstance().onResult(U8Code.CODE_LOGIN_FAIL, "login failed.code:" + result.mErrCode);
            }

        }
    };

    public void logout() {
        mIGPApi.logout();
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

    public void sendExtraData(UserExtraData data) {
        GPSDKPlayerInfo info = new GPSDKPlayerInfo();
        info.mGameLevel = data.getRoleLevel();
        info.mPlayerId = data.getRoleID();
        info.mPlayerNickName = data.getRoleName();
        info.mServerId = data.getServerID() + "";
        info.mServerName = data.getServerName();
        mIGPApi.uploadPlayerInfo(info, new IGPUploadPlayerInfoObsv() {

            @Override
            public void onUploadFinish(GPUploadPlayerInfoResult arg0) {
                Log.e(TAG, "guopan upload info success...");
            }
        });

        switch (data.getDataType()) {
            case UserExtraData.TYPE_ENTER_GAME:
                enterGame(data);
                break;
            case UserExtraData.TYPE_LEVEL_UP:
                upgrade(data);
                break;
        }
    }

    public void exitSDK() {
        mIGPApi.exit(new IGPExitObsv() {

            @Override
            public void onExitFinish(GPExitResult result) {
                Log.e(TAG, "exit:" + result.mResultCode);
                if (result.mResultCode == GPExitResult.GPSDKExitResultCodeExitGame) {
                    U8SDK.getInstance().onLogout();
                    U8SDK.getInstance().onResult(U8Code.CODE_LOGOUT_SUCCESS, "exit success");

                    activity.finish();
                    System.exit(0);
                } else {
                    U8SDK.getInstance().onResult(U8Code.CODE_LOGOUT_FAIL, "exit fail");
                }
            }
        });
    }

    public void pay(final PayParams params) {

        try {
            GPSDKGamePayment order = new GPSDKGamePayment();
            order.mItemName = params.getProductName();
            order.mPaymentDes = params.getProductDesc();
//			DecimalFormat df = new DecimalFormat("#.00");
//			order.mItemPrice = Float.parseFloat(df.format(params.getPrice()));
//			order.mItemOrigPrice = Float.parseFloat(df.format(params.getPrice()));

            float iPrice;
            try {
                iPrice = Float.parseFloat(String.valueOf(params.getPrice() + ".00"));
            } catch (Exception e) {
                Toast.makeText(activity, "金额不合法", Toast.LENGTH_LONG).show();
                return;
            }

            order.mItemPrice = iPrice;
            order.mItemCount = 1;
            order.mCurrentActivity = activity;
            order.mSerialNumber = params.getOrderID();
            order.mItemId = params.getProductId();
            order.mReserved = params.getExtension();
            order.mPlayerId = params.getRoleId();
            order.mPlayerNickName = params.getRoleName();
            order.mServerId = params.getServerId();
            order.mServerName = params.getServerName();
            order.mRate = 8.33f;

            TDGAVirtualCurrency.onChargeRequest(params.getOrderID(), params.getProductName(), params.getPrice(), "CNY", params.getPrice() * 7, "guopan SDK");

            mIGPApi.buy(order, new IGPPayObsv() {

                @Override
                public void onPayFinish(GPPayResult result) {
                    Log.e(TAG, "buy:" + result.mErrCode);
                    switch (result.mErrCode) {
                        case GPPayResult.GPSDKPayResultCodeSucceed:
                            TDGAVirtualCurrency.onChargeSuccess(params.getOrderID());
                            U8SDK.getInstance().onResult(U8Code.CODE_PAY_SUCCESS, "pay success");
                            break;
                        case GPPayResult.GPSDKPayResultCodeCancel:
                            U8SDK.getInstance().onResult(U8Code.CODE_PAY_CANCEL, "pay cancel");
                            break;
                        default:
                            U8SDK.getInstance().onResult(U8Code.CODE_PAY_FAIL, "pay failed.code:" + result.mErrCode);
                    }
                }
            });

        } catch (Exception e) {
            U8SDK.getInstance().onResult(U8Code.CODE_PAY_FAIL, e.getMessage());
        }
    }
}
