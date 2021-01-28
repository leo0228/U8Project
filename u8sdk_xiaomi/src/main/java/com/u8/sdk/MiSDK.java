package com.u8.sdk;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.miui.zeus.mimo.sdk.RewardVideoAd;
import com.tendcloud.tenddata.TDGAProfile;
import com.tendcloud.tenddata.TDGAVirtualCurrency;
import com.xiaomi.gamecenter.sdk.GameInfoField;
import com.xiaomi.gamecenter.sdk.MiCommplatform;
import com.xiaomi.gamecenter.sdk.MiErrorCode;
import com.xiaomi.gamecenter.sdk.OnExitListner;
import com.xiaomi.gamecenter.sdk.OnLoginProcessListener;
import com.xiaomi.gamecenter.sdk.OnPayProcessListener;
import com.xiaomi.gamecenter.sdk.OnRealNameVerifyProcessListener;
import com.xiaomi.gamecenter.sdk.RoleData;
import com.xiaomi.gamecenter.sdk.entry.MiAccountInfo;
import com.xiaomi.gamecenter.sdk.entry.MiBuyInfo;
import com.zjtx.prompt.PromptDialog;

import org.json.JSONObject;

public class MiSDK {
    public static final String TAG = "xiaomi";

    private static MiSDK instance;
    private Activity activity;

    private boolean debug_mode;

    private boolean isShowXiaoMiAd;
    private String ad_appId;
    private String ad_posId;
    private RewardVideoAd mRewardVideoAd;
    private RewardVideoAd.RewardVideoLoadListener mRewardVideoLoadListener;
    private RewardVideoAd.RewardVideoInteractionListener mRewardVideoInteractionListener;

    private static final int PERMISSON_REQUEST_CODE = 1;

    public static MiSDK getInstance() {
        if (instance == null) {
            instance = new MiSDK();
        }
        return instance;
    }

    private MiSDK() {
        activity = U8SDK.getInstance().getContext();
    }

    public void initSDK(SDKParams params) {
        debug_mode = params.getBoolean("debug_mode");
        isShowXiaoMiAd = params.getBoolean("isShowXiaoMiAd");
        if (isShowXiaoMiAd) {
            ad_appId = params.getString("ad_appId");
            ad_posId = params.getString("ad_posId");
        }

        PromptDialog.init(activity, new PromptDialog.OnPromptListener() {
            @Override
            public void onInitSDK() {
                initSDK();
            }
        });
    }

    private void initSDK() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_PHONE_STATE)
                    != PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT < 29
                    || ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_PHONE_STATE}, PERMISSON_REQUEST_CODE);
            }
        }

        U8SDK.getInstance().setActivityCallback(new ActivityCallbackAdapter() {

            @Override
            public void onCreate() {
                super.onCreate();
                MiCommplatform.getInstance().onMainActivityCreate(activity);
            }

            @Override
            public void onDestroy() {
                super.onDestroy();
                MiCommplatform.getInstance().onMainActivityDestory();
                if (mRewardVideoAd != null) {
                    mRewardVideoAd.recycle();
                }
            }

            @Override
            public void onPause() {
                super.onPause();
//					MiPushClient.pausePush(activity, null);
            }

            @Override
            public void onResume() {
                super.onResume();
//					MiPushClient.resumePush(activity, null);
            }

            @Override
            public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) {
                // TODO Auto-generated method stub
                super.onRequestPermissionResult(requestCode, permissions, grantResults);
                if (requestCode == PERMISSON_REQUEST_CODE) {
                    if (hasNecessaryPMSGranted()) {
                        showLog("权限已开启");
                    } else {
                        showLog("权限未开启");
                        showDialogTipUserRequestPermission();
                    }
                }
            }
        });

        if (isShowXiaoMiAd) {
            mRewardVideoLoadListener = new RewardVideoAd.RewardVideoLoadListener() {
                @Override
                public void onAdRequestSuccess() {
                    //请求广告成功，服务端成功返回广告
                    showLog("onAdRequestSuccess");
                }

                @Override
                public void onAdLoadSuccess() {
                    //请求广告成功，且素材缓存成功
                    showLog("onAdLoadSuccess");
                    U8SDK.getInstance().onResult(U8Code.CODE_ADS_LOADED, "广告加载完成，开始播放");

                    if (mRewardVideoAd != null) {
                        mRewardVideoAd.showAd(activity, mRewardVideoInteractionListener);
                        mRewardVideoAd = null;
                    }
                }

                @Override
                public void onAdLoadFailed(int i, String s) {
                    Log.e(TAG, "onAdLoadFailed->code:" + i + ",msg:" + s);
                    U8SDK.getInstance().onResult(U8Code.CODE_ADS_FAILED, "广告加载或播放失败");
                }
            };

            mRewardVideoInteractionListener = new RewardVideoAd.RewardVideoInteractionListener() {
                @Override
                public void onAdPresent() {
                    // 广告被曝光
                    showLog("onAdPresent");
                }

                @Override
                public void onAdClick() {
                    showLog("onAdClick");
                    U8SDK.getInstance().onResult(U8Code.CODE_ADS_CLICKED, "广告被点击");
                }

                @Override
                public void onAdDismissed() {
                    showLog("onAdDismissed");
                    U8SDK.getInstance().onResult(U8Code.CODE_ADS_CLOSED, "广告被关闭");
                }

                @Override
                public void onAdFailed(String s) {
                    // 渲染失败
                    showLog("onAdFailed:" + s);
                    U8SDK.getInstance().onResult(U8Code.CODE_ADS_FAILED, "渲染失败");
                }

                @Override
                public void onVideoStart() {
                    showLog("onVideoStart");
                    U8SDK.getInstance().onResult(U8Code.CODE_ADS_SHOW, "开始播放广告");
                }

                @Override
                public void onVideoPause() {
                    // 视频暂停
                    showLog("onVideoPause");
                }

                @Override
                public void onVideoComplete() {
                    showLog("onVideoComplete");
                    U8SDK.getInstance().onResult(U8Code.CODE_ADS_COMPLETE, "广告播放完成");
                }

                @Override
                public void onPicAdEnd() {
                    // 图片类型广告播放完成
                    showLog("onPicAdEnd");
                }
            };
        }
    }

    public void login() {
        MiCommplatform.getInstance().miLogin(activity, new OnLoginProcessListener() {

            @Override
            public void finishLoginProcess(int code, MiAccountInfo account) {
                switch (code) {
                    case MiErrorCode.MI_XIAOMI_PAYMENT_SUCCESS:
                        String uid = account.getUid();
                        String session = account.getSessionId();
                        String loginResult = encodeLoginResult(uid, session);
                        U8SDK.getInstance().onLoginResult(loginResult);
                        U8SDK.getInstance().onResult(U8Code.CODE_LOGIN_SUCCESS, "login success");

//                        realNameVerify();
                        break;
                    default:
                        U8SDK.getInstance().onResult(U8Code.CODE_LOGIN_FAIL, "login failed.code:" + code);
                        break;
                }
            }
        });
    }

    private String encodeLoginResult(String sid, String token) {
        JSONObject json = new JSONObject();
        try {
            json.put("sid", sid);
            json.put("token", token);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return json.toString();
    }

    public void realNameVerify() {
        MiCommplatform.getInstance().realNameVerify(activity, new OnRealNameVerifyProcessListener() {
            @Override
            public void onSuccess() {
                showLog("实名认证成功");
            }

            @Override
            public void closeProgress() {

            }

            @Override
            public void onFailure() {
                Log.e(TAG, "实名认证失败");
            }
        });
    }

    public void logout() {
        U8SDK.getInstance().onLogout();
        U8SDK.getInstance().onResult(U8Code.CODE_LOGOUT_SUCCESS, "logout success");
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

    private void submitRoleData(UserExtraData extraDat) {
        RoleData data = new RoleData();
        data.setLevel(extraDat.getRoleLevel());
        data.setRoleId(extraDat.getRoleID());
        data.setRoleName(extraDat.getRoleName());
        data.setServerId(extraDat.getServerID() + "");
        data.setServerName(extraDat.getServerName());
        data.setZoneId("");
        data.setZoneName("");
        MiCommplatform.getInstance().submitRoleData(activity, data);
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

    public void exit() {
        MiCommplatform.getInstance().miAppExit(activity, new OnExitListner() {
            @Override
            public void onExit(int code) {
                if (code == MiErrorCode.MI_XIAOMI_EXIT) {
                    android.os.Process.killProcess(android.os.Process.myPid());
                }
            }
        });
    }

    public void pay(final PayParams params) {
        Log.i(TAG, params.toString());

        MiBuyInfo miBuyInfo = new MiBuyInfo();
        miBuyInfo.setCpOrderId(params.getOrderID());// 订单号唯一（不为空）
        miBuyInfo.setCpUserInfo(params.getExtension()); // 此参数在用户支付成功后会透传给CP的服务器
        miBuyInfo.setAmount(params.getPrice()); // 必须是大于1的整数，10代表10米币，即10元人民币（不为空）

        // 用户信息，网游必须设置、单机游戏或应用可选
        Bundle mBundle = new Bundle();
        mBundle.putString(GameInfoField.GAME_USER_BALANCE, params.getCoinNum() + ""); // 用户余额
        mBundle.putString(GameInfoField.GAME_USER_GAMER_VIP, params.getVip()); // vip等级
        mBundle.putString(GameInfoField.GAME_USER_LV, params.getRoleLevel() + ""); // 角色等级
        mBundle.putString(GameInfoField.GAME_USER_PARTY_NAME, "默认"); // 工会，帮派
        mBundle.putString(GameInfoField.GAME_USER_ROLE_NAME, params.getRoleName()); // 角色名称
        mBundle.putString(GameInfoField.GAME_USER_ROLEID, params.getRoleId()); // 角色id
        mBundle.putString(GameInfoField.GAME_USER_SERVER_NAME, params.getServerName()); // 所在服务器
        miBuyInfo.setExtraInfo(mBundle); // 设置用户信息

        TDGAVirtualCurrency.onChargeRequest(params.getOrderID(), params.getProductName(), params.getPrice(), "CNY", params.getPrice() * 7, "xiaomi SDK");

        MiCommplatform.getInstance().miUniPay(activity, miBuyInfo, new OnPayProcessListener() {
            @Override
            public void finishPayProcess(int code) {
                switch (code) {
                    case MiErrorCode.MI_XIAOMI_PAYMENT_SUCCESS:
                        // 购买成功
                        TDGAVirtualCurrency.onChargeSuccess(params.getOrderID());

                        U8SDK.getInstance().onResult(U8Code.CODE_PAY_SUCCESS, "pay success");
                        break;
                    case MiErrorCode.MI_XIAOMI_PAYMENT_ERROR_ACTION_EXECUTED:
                        // 操作正在执行
                        U8SDK.getInstance().onResult(U8Code.CODE_PAYING, "paying");
                        break;
                    case MiErrorCode.MI_XIAOMI_PAYMENT_ERROR_CANCEL:
                        // 取消购买
                        U8SDK.getInstance().onResult(U8Code.CODE_PAY_CANCEL, "pay cancel");
                        break;
                    default:
                        // 购买失败
                        U8SDK.getInstance().onResult(U8Code.CODE_PAY_FAIL, "pay failed. code:" + code);
                        break;
                }
            }
        });

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

    // 提示用户该请求权限的弹出框
    private void showDialogTipUserRequestPermission() {
        new AlertDialog.Builder(activity).setTitle("手机设备权限不可用")
                .setMessage("由于该游戏需要获取手机设备码，存储账号信息；\n否则，您将无法正常游玩游戏")
                .setPositiveButton("去设置", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        goToAppSetting();
                    }
                }).setNegativeButton("退出游戏", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                activity.finish();
                System.exit(0);
            }
        }).setCancelable(false).show();
    }

    // 跳转到当前应用的设置界面
    private void goToAppSetting() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
        intent.setData(uri);
        activity.startActivityForResult(intent, PERMISSON_REQUEST_CODE);
    }

    public void showXiaoMiAd(String posId) {
        if (!isShowXiaoMiAd) return;

        mRewardVideoAd = new RewardVideoAd();
        mRewardVideoAd.loadAd(ad_posId, mRewardVideoLoadListener);
    }

    private void showLog(String msg) {
        if (debug_mode) {
            Log.i(TAG, msg);
        }
    }
}
