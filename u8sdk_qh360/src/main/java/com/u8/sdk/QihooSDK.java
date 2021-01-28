package com.u8.sdk;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.qihoo.gamecenter.sdk.activity.ContainerActivity;
import com.qihoo.gamecenter.sdk.common.IDispatcherCallback;
import com.qihoo.gamecenter.sdk.matrix.Matrix;
import com.qihoo.gamecenter.sdk.protocols.CPCallBackMgr.MatrixCallBack;
import com.qihoo.gamecenter.sdk.protocols.ProtocolConfigs;
import com.qihoo.gamecenter.sdk.protocols.ProtocolKeys;
import com.tendcloud.tenddata.TDGAProfile;
import com.tendcloud.tenddata.TDGAVirtualCurrency;
import com.u8.sdk.qh360.QihooPayInfo;
import com.zjtx.prompt.PromptDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class QihooSDK {
    private Activity activity;
    private String TAG = "QihooSDK";
    private static QihooSDK instance;

    private boolean isShowCloseIcon = true;
    private boolean isShowAutoLoginSwitch = true;
    private boolean isHideWelcome = true;
    private String appName = "";
    private int ratio = 10;

    private String accessToken = "";

    private QihooSDK() {
        activity = U8SDK.getInstance().getContext();
    }

    public static QihooSDK getInstance() {
        if (instance == null) {
            instance = new QihooSDK();
        }

        return instance;
    }

    private void parseSDKParams(SDKParams params) {

        this.isShowCloseIcon = params.getBoolean("QHIsShowCloseIcon");
        this.isShowAutoLoginSwitch = params.getBoolean("QHIsShowAutoLoginSwitch");
        this.isHideWelcome = params.getBoolean("QHIsHideWelcome");
        this.appName = params.getString("QHAppName");
        this.ratio = params.getInt("QHRatio");

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

    protected String[] needPermissions = {Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private void initSDK() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int i = ContextCompat.checkSelfPermission(activity, needPermissions[0]);
            if (i != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, needPermissions, 321);
            }
        }

        initQiHoo();

        U8SDK.getInstance().setActivityCallback(new ActivityCallbackAdapter() {

            @Override
            public void onResume() {
                super.onResume();
                Matrix.onResume(activity);
            }

            @Override
            public void onStart() {
                super.onStart();
                Matrix.onStart(activity);
            }

            @Override
            public void onRestart() {
                super.onRestart();
                Matrix.onRestart(activity);
            }

            @Override
            public void onActivityResult(int requestCode, int resultCode, Intent data) {
                super.onActivityResult(requestCode, resultCode, data);
                Matrix.onActivityResult(activity, requestCode, resultCode, data);
            }

            @Override
            public void onPause() {
                super.onPause();
                Matrix.onPause(activity);
            }

            @Override
            public void onStop() {
                super.onStop();
                Matrix.onStop(activity);
            }

            @Override
            public void onNewIntent(Intent intent) {
                super.onNewIntent(intent);
                Matrix.onNewIntent(activity, intent);
            }

            public void onDestroy() {
                Matrix.destroy(activity);
            }

            @Override
            public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) {
                // TODO Auto-generated method stub
                super.onRequestPermissionResult(requestCode, permissions, grantResults);
                if (requestCode == 321) {
                    if (verifyPermissions(grantResults)) {
                        initQiHoo();
                    } else {
                        ActivityCompat.requestPermissions(activity, needPermissions, 321);
                    }
                }
            }
        });

    }

    private void initQiHoo() {
        Matrix.setActivity(activity, new MatrixCallBack() {

            @Override
            public void execute(Context c, int code, String params) {

                if (code == ProtocolConfigs.FUNC_CODE_SWITCH_ACCOUNT) {
                    // U8SDK.getInstance().onSwitchAccount();
                    U8SDK.getInstance().onResult(U8Code.CODE_LOGOUT_SUCCESS, "logout success" + code);

                } else if (code == ProtocolConfigs.FUNC_CODE_INITSUCCESS) {
                    U8SDK.getInstance().onResult(U8Code.CODE_INIT_SUCCESS, "init success");
                } else {
                    Log.d("U8SDK", "init failed. code:" + code + ";params:" + params);
                    U8SDK.getInstance().onResult(U8Code.CODE_INIT_FAIL, params);
                }

            }
        }, false);
    }

    /**
     * 检测是否有的权限都已经授权
     *
     * @param grantResults
     */
    private boolean verifyPermissions(int[] grantResults) {
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public void login() {
        login(ProtocolConfigs.FUNC_CODE_LOGIN);
    }

    public void switchAccount() {
        login(ProtocolConfigs.FUNC_CODE_SWITCH_ACCOUNT);
    }

    public void login(int functionCode) {

        if (!SDKTools.isNetworkAvailable(activity)) {
            U8SDK.getInstance().onResult(U8Code.CODE_NO_NETWORK, "The network now is unavailable");
            return;
        }

        try {
            Intent intent = getLoginIntent(functionCode);

            Matrix.execute(activity, intent, new IDispatcherCallback() {

                @Override
                public void onFinished(String data) {
                    Log.d("360", "the login data is " + data);
                    if (data == null) {
                        U8SDK.getInstance().onResult(U8Code.CODE_LOGIN_FAIL, "login failed. data is null");
                        return;
                    }

                    try {
                        JSONObject joData = new JSONObject(data);
                        int errno = joData.optInt("errno", -1);
                        if (-1 == errno) {
                            U8SDK.getInstance().onResult(U8Code.CODE_LOGIN_FAIL, "login canceled.data is " + data);
                            return;
                        }

                        JSONObject joDataInfo = joData.getJSONObject("data");
                        String accessToken = joDataInfo.getString("access_token");
                        QihooSDK.this.accessToken = accessToken;
                        U8SDK.getInstance().onResult(U8Code.CODE_LOGIN_SUCCESS, accessToken);

                        U8SDK.getInstance().onLoginResult(accessToken);

                    } catch (Exception e) {

                        e.printStackTrace();
                        U8SDK.getInstance().onResult(U8Code.CODE_LOGIN_FAIL, "login failed." + e.getMessage());
                        return;
                    }

                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            U8SDK.getInstance().onResult(U8Code.CODE_LOGIN_FAIL, "login failed." + e.getMessage());
        }

    }

    // 提交扩展数据
    public void submitExtendData(UserExtraData extraData) {

        if (extraData.getDataType() == UserExtraData.TYPE_SELECT_SERVER)
            return;

        try {

            HashMap<String, Object> eventParams = new HashMap<String, Object>();
            // ----------------------------模拟数据------------------------------
            eventParams.put("zoneid", extraData.getServerID());// 当前角色所在游戏区服id(具体要求参考文档)
            eventParams.put("zonename", extraData.getServerName());// 当前角色所在游戏区服名称(具体要求参考文档)
            eventParams.put("roleid", extraData.getRoleID());// 当前角色id(具体要求参考文档)
            eventParams.put("rolename", extraData.getRoleName());// 当前角色名称(具体要求参考文档)

            eventParams.put("professionid", "0");
            eventParams.put("profession", "无");
            eventParams.put("gender", "无");
            eventParams.put("professionroleid", "0");
            eventParams.put("professionrolename", "无");
            eventParams.put("rolelevel", extraData.getRoleLevel());
            eventParams.put("power", "0");
            eventParams.put("vip", "0");
            eventParams.put("balance", "0");
            eventParams.put("partyid", "0");

            eventParams.put("partyname", "无");
            eventParams.put("partyroleid", "0");
            eventParams.put("partyrolename", "无");
            eventParams.put("friendlist", "无");

            // 参数eventParams相关的 key、value键值对 相关具体使用说明，请参考文档。
            // ----------------------------模拟数据------------------------------
            switch (extraData.getDataType()) {
                case UserExtraData.TYPE_CREATE_ROLE:
                    eventParams.put("type", "createRole");// 角色信息接口触发的场景(具体要求参考文档)
                    Matrix.statEventInfo(activity, eventParams);
                    break;
                case UserExtraData.TYPE_ENTER_GAME:
                    enterGame(extraData);
                    eventParams.put("type", "enterServer");// 角色信息接口触发的场景(具体要求参考文档)
                    Matrix.statEventInfo(activity, eventParams);
                    break;
                case UserExtraData.TYPE_LEVEL_UP:
                    upgrade(extraData);
                    eventParams.put("type", "levelUp");// 角色信息接口触发的场景(具体要求参考文档)
                    Matrix.statEventInfo(activity, eventParams);
                    break;
                case UserExtraData.TYPE_EXIT_GAME:
                    eventParams.put("type", "exitServer");// 角色信息接口触发的场景(具体要求参考文档)
                    Matrix.statEventInfo(activity, eventParams);
                    break;
            }

        } catch (Exception e) {
            e.printStackTrace();
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
        Intent intent = getLogoutIntent();
        Matrix.execute(activity, intent, new IDispatcherCallback() {
            @Override
            public void onFinished(String data) {
                U8SDK.getInstance().onLogout();
                U8SDK.getInstance().onResult(U8Code.CODE_LOGOUT_SUCCESS, data);
            }
        });
    }

    public void quitSDK() {
        Intent intent = this.getSDKQuitIntent();
        Matrix.invokeActivity(activity, intent, new IDispatcherCallback() {

            @Override
            public void onFinished(String data) {
                JSONObject json;
                try {
                    json = new JSONObject(data);
                    int which = json.optInt("which", -1);

                    switch (which) {
                        case 0: // 用户关闭退出界面
                            return;
                        default:// 退出游戏
                            activity.finish();
                            Process.killProcess(Process.myPid());
                            System.exit(0);
                            return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void realNameRegister() {

        Intent intent = getRealNameRegisterIntent();
        Matrix.invokeActivity(activity, intent, new IDispatcherCallback() {
            @Override
            public void onFinished(String data) {
                U8SDK.getInstance().onResult(U8Code.CODE_REAL_NAME_REG_SUC, data);
            }
        });
    }

    /**
     * 本方法中的callback实现仅用于测试, 实际使用由游戏开发者自己处理
     */
    public void doSdkAntiAddictionQuery() {

        Intent intent = getAntAddictionIntent();
        Matrix.execute(activity, intent, new IDispatcherCallback() {

            @Override
            public void onFinished(String data) {

                // 暂时360只需要接入该接口，游戏中不需要处理
                U8SDK.getInstance().onResult(U8Code.CODE_ADDICTION_ANTI_RESULT, data);
            }
        });
    }

    /***
     * 该方法仅仅作为测试时使用 真正上线时，这里是登录认证部分所做的工作

     */
    // private void getUserInfo(final Activity context) {
    //
    // final QihooUserInfoTask mUserInfoTask = QihooUserInfoTask.newInstance();
    // // 请求应用服务器，用AccessToken换取UserInfo
    // mUserInfoTask.doRequest(context, this.accessToken, Matrix.getAppKey(context),
    // new QihooUserInfoListener() {
    //
    // @Override
    // public void onGotUserInfo(QihooUserInfo userInfo) {
    // if (null == userInfo || !userInfo.isValid()) {
    // Toast.makeText(context, "从应用服务器获取用户信息失败", Toast.LENGTH_LONG).show();
    // } else {
    // QihooSDK.this.userID = userInfo.getId();
    // }
    // }
    // });
    // }
    public void pay(final PayParams params) {

        QihooPayInfo payInfo = decodePayParams(params);
        Intent intent = getPayIntent(payInfo, params);

		TDGAVirtualCurrency.onChargeRequest(params.getOrderID(), params.getProductName(), params.getPrice(), "CNY", params.getPrice() * 7,"360 SDK");

        Matrix.invokeActivity(activity, intent, new IDispatcherCallback() {

            @Override
            public void onFinished(String data) {
                if (TextUtils.isEmpty(data)) {
                    U8SDK.getInstance().onResult(U8Code.CODE_PAY_FAIL, "the pay result is null.");
                    return;
                }

                JSONObject jsonRes;
                try {
                    jsonRes = new JSONObject(data);
                    // error_code 状态码： 0 支付成功， -1 支付取消， 1 支付失败， -2 支付进行中, 4010201和4009911
                    // 登录状态已失效，引导用户重新登录
                    // error_msg 状态描述
                    int errorCode = jsonRes.optInt("error_code");
                    switch (errorCode) {
                        case 0:
                            TDGAVirtualCurrency.onChargeSuccess(params.getOrderID());
                            U8SDK.getInstance().onResult(U8Code.CODE_PAY_SUCCESS, "pay success");
                            break;
                        case 1:
                            String errorMsg = jsonRes.optString("error_msg");
                            U8SDK.getInstance().onResult(U8Code.CODE_PAY_FAIL, "pay failed. error:" + errorMsg);
                            break;
                        case -1:
                            String errorMsg2 = jsonRes.optString("error_msg");
                            U8SDK.getInstance().onResult(U8Code.CODE_PAY_CANCEL, "pay canceled. error:" + errorMsg2);
                            break;
                        case -2:
                            U8SDK.getInstance().onResult(U8Code.CODE_PAYING, "pay now paying...");
                            break;
                        case 4010201:
                            // acess_token失效
                            Toast.makeText(activity, "会话已失效，请重新登录", Toast.LENGTH_SHORT).show();
                            U8SDK.getInstance().onResult(U8Code.CODE_PAY_FAIL, "pay failed. acess_token is invalid.");
                            break;
                        case 4009911:
                            // QT失效
                            Toast.makeText(activity, "会话已失效，请重新登录", Toast.LENGTH_SHORT).show();
                            U8SDK.getInstance().onResult(U8Code.CODE_PAY_FAIL, "pay failed. QT is invalid.");
                            break;
                        default:
                            break;
                    }
                } catch (JSONException e) {
                    U8SDK.getInstance().onResult(U8Code.CODE_PAY_UNKNOWN, e.getMessage());
                    e.printStackTrace();
                }
            }
        });

    }

    private QihooPayInfo decodePayParams(PayParams params) {

        String qihooUserId = U8SDK.getInstance().getSDKUserID();

        String orderID = params.getOrderID();
        String ext = params.getExtension();
        // String notifyUrl = "";

        // JSONObject jsonExt = new JSONObject(ext);
        // notifyUrl = jsonExt.getString("notifyUrl");

        Log.e("U8SDK", "The notify url is " + ext);

        // 创建QihooPay
        QihooPayInfo qihooPay = new QihooPayInfo();
        qihooPay.setQihooUserId(qihooUserId);
        qihooPay.setMoneyAmount((params.getPrice() * 100) + "");// (params.getPrice() * 100) + ""
        qihooPay.setExchangeRate(this.ratio + "");

        qihooPay.setProductName(params.getProductName());
        qihooPay.setProductId(params.getProductId());

        qihooPay.setNotifyUri(ext);

        qihooPay.setAppName(this.appName);
        qihooPay.setAppUserName(params.getRoleName());
        qihooPay.setAppUserId(params.getRoleId());
        qihooPay.setAppOrderId(orderID);
        // 可选参数
        // qihooPay.setAppExt1("");
        // qihooPay.setAppExt2("");

        return qihooPay;
    }

    public boolean isLandscape() {

        int orientation = activity.getResources().getConfiguration().orientation;

        if (orientation != Configuration.ORIENTATION_LANDSCAPE && orientation != Configuration.ORIENTATION_PORTRAIT) {
            orientation = Configuration.ORIENTATION_PORTRAIT;
        }

        return orientation == Configuration.ORIENTATION_LANDSCAPE;

    }

    /**
     * 生成调用360SDK登录接口的Intent
     *
     * @return intent
     */
    private Intent getLoginIntent(int functionCode) {

        Intent intent = new Intent(activity, ContainerActivity.class);

        // 界面相关参数，360SDK界面是否以横屏显示。
        intent.putExtra(ProtocolKeys.IS_SCREEN_ORIENTATION_LANDSCAPE, this.isLandscape());

        // 必需参数，使用360SDK的登录模块。
        intent.putExtra(ProtocolKeys.FUNCTION_CODE, functionCode);

        // 是否显示关闭按钮
        intent.putExtra(ProtocolKeys.IS_LOGIN_SHOW_CLOSE_ICON, this.isShowCloseIcon);

        // 可选参数，是否在自动登录的过程中显示切换账号按钮
        intent.putExtra(ProtocolKeys.IS_SHOW_AUTOLOGIN_SWITCH, this.isShowAutoLoginSwitch);

        // 可选参数，是否隐藏欢迎界面
        intent.putExtra(ProtocolKeys.IS_HIDE_WELLCOME, this.isHideWelcome);

        // // 可选参数，登录界面的背景图片路径，必须是本地图片路径
        // intent.putExtra(ProtocolKeys.UI_BACKGROUND_PICTRUE,
        // getUiBackgroundPicPath());
        // // 可选参数，指定assets中的图片路径，作为背景图
        // intent.putExtra(ProtocolKeys.UI_BACKGROUND_PICTURE_IN_ASSERTS,
        // getUiBackgroundPathInAssets());

        // -- 以下参数仅仅针对自动登录过程的控制
        // 可选参数，自动登录过程中是否不展示任何UI，默认展示。
        // intent.putExtra(ProtocolKeys.IS_AUTOLOGIN_NOUI,
        // getCheckBoxBoolean(R.id.isAutoLoginHideUI));

        // 可选参数，静默自动登录失败后是否显示登录窗口，默认不显示
        // intent.putExtra(ProtocolKeys.IS_SHOW_LOGINDLG_ONFAILED_AUTOLOGIN,
        // getCheckBoxBoolean(R.id.isShowDlgOnFailedAutoLogin));
        // 测试参数，发布时要去掉
        // intent.putExtra(ProtocolKeys.IS_SOCIAL_SHARE_DEBUG,
        // getCheckBoxBoolean(R.id.isDebugSocialShare));

        return intent;
    }

    private Intent getUploadScoreIntent(UserExtraData data) {
        Intent intent = new Intent();
        intent.putExtra(ProtocolKeys.FUNCTION_CODE, ProtocolConfigs.FUNC_CODE_UPLOAD_SCORE);
        intent.putExtra(ProtocolKeys.SCORE, data.getRoleLevel());
        intent.putExtra(ProtocolKeys.TOPNID, 0);
        return intent;
    }

    /**
     * 使用360SDK的退出接口
     */
    private Intent getSDKQuitIntent() {

        Bundle bundle = new Bundle();

        // 界面相关参数，360SDK界面是否以横屏显示。
        bundle.putBoolean(ProtocolKeys.IS_SCREEN_ORIENTATION_LANDSCAPE, this.isLandscape());

        // 必需参数，使用360SDK的退出模块。
        bundle.putInt(ProtocolKeys.FUNCTION_CODE, ProtocolConfigs.FUNC_CODE_QUIT);

        Intent intent = new Intent(activity, ContainerActivity.class);
        intent.putExtras(bundle);

        return intent;
    }

    private Intent getLogoutIntent() {

        Intent intent = new Intent();
        intent.putExtra(ProtocolKeys.FUNCTION_CODE, ProtocolConfigs.FUNC_CODE_LOGOUT);
        return intent;
    }

    String tag = "360sdk";

    /***
     * 生成调用360SDK支付接口的Intent
     *
     * @param pay
     * @return Intent
     */
    public Intent getPayIntent(QihooPayInfo pay, PayParams data) {
        Bundle bundle = new Bundle();

        // 界面相关参数，360SDK界面是否以横屏显示。
        bundle.putBoolean(ProtocolKeys.IS_SCREEN_ORIENTATION_LANDSCAPE, this.isLandscape());

        // *** 以下非界面相关参数 ***

        // 设置QihooPay中的参数。String str= str.replace("\"", "");

        // 必需参数，360账号id，整数。
        bundle.putString(ProtocolKeys.QIHOO_USER_ID, pay.getQihooUserId());

        // 必需参数，所购买商品金额, 以分为单位。金额大于等于100分，360SDK运行定额支付流程； 金额数为0，360SDK运行不定额支付流程。
        bundle.putString(ProtocolKeys.AMOUNT, pay.getMoneyAmount());

        // 必需参数，人民币与游戏充值币的默认比例，例如2，代表1元人民币可以兑换2个游戏币，整数。
        bundle.putString(ProtocolKeys.RATE, pay.getExchangeRate());

        // 必需参数，所购买商品名称，应用指定，建议中文，最大10个中文字。
        bundle.putString(ProtocolKeys.PRODUCT_NAME, pay.getProductName());

        // 必需参数，购买商品的商品id，应用指定，最大16字符。
        bundle.putString(ProtocolKeys.PRODUCT_ID, pay.getProductId());

        // 必需参数，应用方提供的支付结果通知uri，最大255字符。360服务器将把支付接口回调给该uri，具体协议请查看文档中，支付结果通知接口–应用服务器提供接口。
        bundle.putString(ProtocolKeys.NOTIFY_URI, pay.getNotifyUri().replace("\"", ""));
        Log.e(tag, "getNotifyUri ==" + pay.getNotifyUri().replace("\"", ""));
        // 必需参数，游戏或应用名称，最大16中文字。
        bundle.putString(ProtocolKeys.APP_NAME, pay.getAppName());

        // 必需参数，应用内的用户名，如游戏角色名。 若应用内绑定360账号和应用账号，则可用360用户名，最大16中文字。（充值不分区服，
        // 充到统一的用户账户，各区服角色均可使用）。
        bundle.putString(ProtocolKeys.APP_USER_NAME, pay.getAppUserName());

        // 必需参数，应用内的用户id。
        // 若应用内绑定360账号和应用账号，充值不分区服，充到统一的用户账户，各区服角色均可使用，则可用360用户ID最大32字符。
        bundle.putString(ProtocolKeys.APP_USER_ID, pay.getAppUserId());

        // 可选参数，应用扩展信息1，原样返回，最大255字符。
        bundle.putString(ProtocolKeys.APP_EXT_1, pay.getAppExt1());

        // 可选参数，应用扩展信息2，原样返回，最大255字符。
        bundle.putString(ProtocolKeys.APP_EXT_2, pay.getAppExt2());

        // 可选参数，应用订单号，应用内必须唯一，最大32字符。
        bundle.putString(ProtocolKeys.APP_ORDER_ID, pay.getAppOrderId());

        // 必需参数，使用360SDK的支付模块。
        bundle.putInt(ProtocolKeys.FUNCTION_CODE, ProtocolConfigs.FUNC_CODE_PAY);

        bundle.putInt(ProtocolKeys.PRODUCT_COUNT, 1);

        bundle.putString(ProtocolKeys.SERVER_ID, data.getServerId());

        bundle.putString(ProtocolKeys.SERVER_NAME, data.getServerName());

        // 必需参数，兑换比例（demo模拟数据）（游戏内虚拟币兑换人民币） ,游戏内逻辑请传递游戏内真实数据
        bundle.putInt(ProtocolKeys.EXCHANGE_RATE, Integer.valueOf(pay.getExchangeRate()));

        // 必需参数，货币名称（demo模拟数据）（比如：钻石）,游戏内逻辑请传递游戏内真实数据
        bundle.putString(ProtocolKeys.GAMEMONEY_NAME, data.getProductName());

        // 必需参数，角色id（demo模拟数据）,游戏内逻辑请传递游戏内真实数据
        bundle.putString(ProtocolKeys.ROLE_ID, data.getRoleId());

        // 必需参数，角色名称（demo模拟数据）,游戏内逻辑请传递游戏内真实数据
        bundle.putString(ProtocolKeys.ROLE_NAME, data.getRoleName());

        // 必需参数，角色等级（demo模拟数据）,游戏内逻辑请传递游戏内真实数据
        bundle.putInt(ProtocolKeys.ROLE_GRADE, data.getRoleLevel());

        // 必需参数，虚拟币余额（demo模拟数据）,游戏内逻辑请传递游戏内真实数据
        bundle.putInt(ProtocolKeys.ROLE_BALANCE, data.getCoinNum());

        // 必需参数，vip等级（demo模拟数据）,游戏内逻辑请传递游戏内真实数据
        bundle.putString(ProtocolKeys.ROLE_VIP, data.getVip());

        // 必需参数，公会名称（demo模拟数据）,游戏内逻辑请传递游戏内真实数据
        bundle.putString(ProtocolKeys.ROLE_USERPARTY, "无");

        // 若接入3.3.1下单接口【服务端调用】，则以下两个参数必需传递，由此服务端接口返回值获得；若未接入此服务端接口，则以下两个参数无需传递。
        // bundle.putExtra(ProtocolKeys.TOKEN_ID, tokenId);
        // bundle.putExtra(ProtocolKeys.ORDER_TOKEN, orderId);

        Intent intent = new Intent(activity, ContainerActivity.class);
        intent.putExtras(bundle);

        return intent;
    }

    private Intent getRealNameRegisterIntent() {

        Bundle bundle = new Bundle();
        // 界面相关参数，360SDK界面是否以横屏显示。
        bundle.putBoolean(ProtocolKeys.IS_SCREEN_ORIENTATION_LANDSCAPE, this.isLandscape());

        // 必需参数，360账号id，整数。
        bundle.putString(ProtocolKeys.QIHOO_USER_ID, U8SDK.getInstance().getSDKUserID());

        // 可选参数，登录界面的背景图片路径，必须是本地图片路径
        bundle.putString(ProtocolKeys.UI_BACKGROUND_PICTRUE, "");

        // 必需参数，使用360SDK的实名注册模块。
        bundle.putInt(ProtocolKeys.FUNCTION_CODE, ProtocolConfigs.FUNC_CODE_REAL_NAME_REGISTER);

        Intent intent = new Intent(activity, ContainerActivity.class);
        intent.putExtras(bundle);
        return intent;
    }

    private Intent getAntAddictionIntent() {

        Bundle bundle = new Bundle();

        // 必需参数，用户access token，要使用注意过期和刷新问题，最大64字符。
        bundle.putString(ProtocolKeys.ACCESS_TOKEN, this.accessToken);

        // 必需参数，360账号id，整数。
        bundle.putString(ProtocolKeys.QIHOO_USER_ID, U8SDK.getInstance().getSDKUserID());

        // 必需参数，使用360SDK的防沉迷查询模块。
        bundle.putInt(ProtocolKeys.FUNCTION_CODE, ProtocolConfigs.FUNC_CODE_ANTI_ADDICTION_QUERY);

        Intent intent = new Intent(activity, ContainerActivity.class);
        intent.putExtras(bundle);
        return intent;
    }

}
