package com.u8.sdk;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.tencent.ysdk.api.YSDKApi;
import com.tencent.ysdk.framework.common.BaseRet;
import com.tencent.ysdk.framework.common.eFlag;
import com.tencent.ysdk.framework.common.ePlatform;
import com.tencent.ysdk.module.AntiAddiction.model.AntiAddictRet;
import com.tencent.ysdk.module.pay.PayItem;
import com.tencent.ysdk.module.pay.PayListener;
import com.tencent.ysdk.module.pay.PayRet;
import com.tencent.ysdk.module.user.UserLoginRet;
import com.tendcloud.tenddata.TDGAProfile;
import com.tendcloud.tenddata.TDGAVirtualCurrency;
import com.u8.sdk.utils.EncryptUtils;
import com.u8.sdk.utils.ResourceHelper;
import com.u8.sdk.utils.U8HttpUtils;
import com.u8.common.PromptDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class YSDK {

    private Activity activity;

    public static String TAG = "YSDK_MOYOI";
    public static final int LOGIN_TYPE_QQ = 1; // QQ登录类型
    public static final int LOGIN_TYPE_WX = 2; // 微信登录类型
    public static int lastLoginType = 0;

    public final int REQ_TYPE_QUERY = 1; // 查询余额
    public final int REQ_TYPE_CHARGE = 2; // 扣款

    private int payType = 1; // 支付类型，1：游戏币托管模式；2：道具直购模式

    private boolean fixedPay;
    private boolean multiServers;
    private int ratio;
    private String coinIconName;
    private String queryUrl;
    private String payUrl;
    private String appKey;

    private boolean logined = false;

    private String lastLoginResult = null;

    private boolean mAntiAddictExecuteState = false;//处理指令互斥

    private static YSDK instance;

    public static YSDK getInstance() {
        if (instance == null) {
            instance = new YSDK();
        }
        return instance;
    }

    private void parseSDKParams(SDKParams params) {
        fixedPay = params.getBoolean("WG_FIXEDPAY");
        coinIconName = params.getString("WG_COIN_ICON_NAME");
        multiServers = params.getBoolean("WG_MULTI_SERVERS");
        queryUrl = params.getString("WG_QUERY_URL");
        payUrl = params.getString("WG_PAY_URL");
        ratio = params.getInt("WG_RATIO");
        appKey = params.getString("M_APP_KEY");
        payType = params.getInt("M_PAY_TYPE");
        Log.d(TAG, fixedPay + "," + coinIconName + "," + multiServers + "," + queryUrl + "," + payUrl + "," + ratio
                + "," + appKey + "," + payType);
    }

    public boolean isStartLogined() {
        return logined;
    }

    public YSDK() {
        activity = U8SDK.getInstance().getContext();
    }

    public void initSDK(SDKParams params) {
        parseSDKParams(params);

        PromptDialog.init(activity, new PromptDialog.OnPromptListener() {
            @Override
            public void onInitSDK() {
                initSDK();
            }
        });
    }

    private void initSDK() {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(activity,
                    Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, needPermissions, 0);
            }
        }

        U8SDK.getInstance().setActivityCallback(new ActivityCallbackAdapter() {

            @Override
            public void onActivityResult(int requestCode, int resultCode, Intent data) {
                YSDKApi.onActivityResult(requestCode, resultCode, data);
            }

            @Override
            public void onStop() {
                YSDKApi.onStop(activity);
            }

            @Override
            public void onResume() {
                YSDKApi.onResume(activity);
                // TODO GAME 展示调式界面悬浮球，上线前需要关闭
//                YSDKApi.showDebugIcon(activity);
            }

            @Override
            public void onRestart() {
                YSDKApi.onRestart(activity);
            }

            @Override
            public void onPause() {
                YSDKApi.onPause(activity);
            }

            @Override
            public void onNewIntent(Intent newIntent) {
                YSDKApi.handleIntent(newIntent);
            }

            @Override
            public void onDestroy() {
                //游戏时长统计结束
                YSDKApi.setAntiAddictGameEnd();
                YSDKApi.onDestroy(activity);
            }

        });

        YSDKApi.onCreate(activity);
        YSDKApi.setUserListener(new YSDKCallback());
        YSDKApi.setBuglyListener(new YSDKCallback());
        YSDKApi.setAntiAddictListener(new YSDKCallback());
        YSDKApi.setAntiAddictLogEnable(false);
        YSDKApi.handleIntent(activity.getIntent());

        U8SDK.getInstance().onResult(U8Code.CODE_INIT_SUCCESS, "init success");
        Log.e(TAG, "init success...");
    }


    private String[] needPermissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, // 存储卡写入权限
            Manifest.permission.READ_EXTERNAL_STORAGE, // 存储卡读取权限
            Manifest.permission.READ_PHONE_STATE, // 读取手机状态权限
    };

    // 获取当前登录平台
    public ePlatform getPlatform() {
        UserLoginRet ret = new UserLoginRet();
        YSDKApi.getLoginRecord(ret);

        if (ret.flag == eFlag.Succ) {
            return ePlatform.getEnum(ret.platform);
        }
        return ePlatform.None;
    }

    public void logout() {
        YSDKApi.logout();
    }

    public void switchLogin() {
        YSDKApi.switchUser(true);
    }

    public void login() {
        if (!SDKTools.isNetworkAvailable(activity)) {
            U8SDK.getInstance().onResult(U8Code.CODE_NO_NETWORK, "The network now is unavailable");
            return;
        }

        if (lastLoginResult != null) {
            Log.e(TAG, "已登陆有缓存：" + lastLoginResult);
            U8SDK.getInstance().onLoginResult(lastLoginResult);
            cleanLoginInfo();
        } else {
            Log.e(TAG, "未登陆，调用界面");
            openLoginUI();
        }

    }

    public void cleanLoginInfo(){
        lastLoginResult = null;
    }

    private void openLoginUI() {
        Intent intent = new Intent(activity, ChooseLoginTypeActivity.class);
        activity.startActivity(intent);
    }

    public void login(int loginType) {
        logined = true;
        lastLoginType = loginType;
        ePlatform platform = getPlatform();

        Log.e(TAG, "call login. login platform:" + platform + ",loginType:" + loginType);

        switch (loginType) {
            case LOGIN_TYPE_QQ:
                switch (platform) {
                    case QQ:
                        // QQ已经登录
                        Log.e(TAG, "QQ");
                        letUserLogin(true);
                        break;
                    case None:
                        Log.e(TAG, "None");
                        YSDKApi.login(ePlatform.QQ);
                        break;
                    default:
                        Log.e(TAG, "default");
                        YSDKApi.logout();
                        YSDKApi.login(ePlatform.QQ);
                        break;
                }
                break;
            case LOGIN_TYPE_WX:
                switch (platform) {
                    case WX:
                        // 微信已经登录
                        letUserLogin(true);
                        break;
                    case None:
                        Log.e(TAG, "None");
                        YSDKApi.login(ePlatform.WX);
                        break;
                    default:
                        Log.e(TAG, "default");
                        YSDKApi.logout();
                        YSDKApi.login(ePlatform.WX);
                        break;
                }
                break;
        }
    }

    private String openId;
    private String openKey;
    private String pf;
    private String pfKey;
    private String accessToken;

    // 平台授权成功,让用户进入游戏. 由游戏自己实现登录的逻辑
    public void letUserLogin(final boolean autoLogin) {
        U8SDK.getInstance().runOnMainThread(new Runnable() {
            @Override
            public void run() {
                UserLoginRet ret = new UserLoginRet();
                YSDKApi.getLoginRecord(ret);
                Log.e(TAG, "platform: " + ret.platform);
                if (ret.ret == BaseRet.RET_FAIL) {
                    Log.e(TAG, "UserLoginRet error");
                    if (autoLogin) {
                        openLoginUI();
                    } else {
                        U8SDK.getInstance().onResult(U8Code.CODE_LOGIN_FAIL, "login failed.");
                    }
                    return;
                }

                openId = ret.open_id;
                pf = ret.pf;
                pfKey = ret.pf_key;
                accessToken = ret.getAccessToken();

                int requestType = -1; // 0:qq 1:微信
                if (ret.platform == ePlatform.PLATFORM_ID_QQ) {
                    requestType = 0;
                    openKey = ret.getPayToken();

                } else if (ret.platform == ePlatform.PLATFORM_ID_WX) {
                    requestType = 1;
                    openKey = accessToken;
                }

                JSONObject json = new JSONObject();
                try {
                    json.put("accountType", requestType);
                    json.put("openId", openId);
                    json.put("openKey", accessToken);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (logined) {
                    // 如果是点击登录之后回调
                    logined = false;
                    U8SDK.getInstance().onLoginResult(json.toString());
                } else {
                    // 如果是初始化自动登录回调，那么这里先缓存，等客户端调用login接口的时候，直接返回自动登录结果。
                    Log.e(TAG, "cache auto login result:" + json.toString());
                    lastLoginResult = json.toString();
                }

                //游戏时长统计开始
                YSDKApi.setAntiAddictGameStart();
            }
        });

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

    private String orderId;

    // ysdk支付
    public void pay(PayParams params) {
        Log.e(TAG, "data:" + params);
        orderId = params.getOrderID();
        TDGAVirtualCurrency.onChargeRequest(orderId, params.getProductName(), params.getPrice(), "CNY", params.getPrice() * 7,"YSDK");

        if (payType == 1) {
            payForCoin(params);// 游戏币托管模式
        } else {
            payForItem(params);// 道具直购模式
        }

    }

    private PayParams payData;

    // 道具直购模式
    public void payForItem(PayParams params) {
        PayItem item = new PayItem();
        item.id = params.getProductId();
        item.name = params.getProductName();
        item.desc = params.getProductDesc();
        item.price = params.getPrice() * ratio;// data.getPrice() * ratio;
        item.num = 1;

        int resID = ResourceHelper.getIdentifier(activity, "R.drawable." + coinIconName);
        Bitmap bmp = BitmapFactory.decodeResource(activity.getResources(), resID);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, bos);
        byte[] resData = bos.toByteArray();

        YSDKApi.buyGoods(fixedPay, multiServers ? payData.getServerId() : "1", item, appKey, resData, params.getOrderID(),
                params.getOrderID(), new PayListener() {

                    @Override
                    public void OnPayNotify(PayRet ret) {
                        Log.e(TAG, "pay for item result:" + ret.toString());
                        if (PayRet.RET_SUCC == ret.ret) {
                            switch (ret.payState) {
                                case PayRet.PAYSTATE_PAYSUCC:
                                    TDGAVirtualCurrency.onChargeSuccess(orderId);

                                    U8SDK.getInstance().onResult(U8Code.CODE_PAY_SUCCESS, "pay success");
                                    break;
                                case PayRet.PAYSTATE_PAYCANCEL:
                                    U8SDK.getInstance().onResult(U8Code.CODE_PAY_CANCEL, "pay cancel");
                                    break;
                                case PayRet.PAYSTATE_PAYERROR:
                                    U8SDK.getInstance().onResult(U8Code.CODE_PAY_FAIL, "pay failed");
                                    break;
                                case PayRet.PAYSTATE_PAYUNKOWN:
                                    U8SDK.getInstance().onResult(U8Code.CODE_PAY_UNKNOWN, "pay unknown");
                                    break;
                            }
                        } else {
                            switch (ret.flag) {
                                case eFlag.Login_TokenInvalid:
                                    Log.e(TAG, "local token invalid. you now to login again.");
                                    U8SDK.getInstance().onResult(U8Code.CODE_PAY_FAIL, "pay failed");
                                    break;
                                case eFlag.Pay_User_Cancle:
                                    U8SDK.getInstance().onResult(U8Code.CODE_PAY_CANCEL, "pay cancel");
                                    break;
                                default:
                                    U8SDK.getInstance().onResult(U8Code.CODE_PAY_FAIL, "pay failed");
                            }
                        }
                    }
                });
    }

    public void payForCoin(PayParams data) {

        payData = data;

        QueryReqTask payReqTask = new QueryReqTask(true);
        payReqTask.execute(REQ_TYPE_QUERY + "", payData.getOrderID(), (multiServers ? payData.getServerId() : "1"));

    }

    public void payInternal(final int leftMoney) {

        Log.e(TAG, "payInternal Start");

        U8SDK.getInstance().runOnMainThread(new Runnable() {

            @Override
            public void run() {
                try {

                    String zoneID = "1";
                    if (multiServers) {
                        // 如果在腾讯后台配置了分区，那么游戏层穿过来的服务器ID，必须和后台配置的一致
                        zoneID = payData.getServerId();
                    }

                    int savedValue = payData.getPrice() * ratio - leftMoney;

                    boolean isCanChange = !fixedPay;
                    int resID = ResourceHelper.getIdentifier(activity,
                            "R.drawable." + coinIconName);
                    Bitmap bmp = BitmapFactory.decodeResource(activity.getResources(), resID);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bmp.compress(Bitmap.CompressFormat.PNG, 100, baos);
                    byte[] resData = baos.toByteArray();

                    YSDKApi.recharge(zoneID, savedValue + "", isCanChange, resData, payData.getOrderID(),
                            new YSDKCallback());

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

    }

    public void chargeWhenPaySuccess() {
        // 支付成功向U8Server发送通知，调用查询余额接口并扣费

        if (payData == null) {
            Log.e(TAG, "the payData is null");
            return;
        }

        PayReqTask payReqTask = new PayReqTask(true);
        payReqTask.execute(REQ_TYPE_CHARGE + "", payData.getOrderID(), (multiServers ? payData.getServerId() : "1"));
        payData = null;
    }

    private String reqCharge(int reqType, String orderID, String serverID) {

        if (reqType == REQ_TYPE_CHARGE && TextUtils.isEmpty(payUrl)) {
            Log.e(TAG, "the pay url is not config");
            return null;
        }

        if (reqType == REQ_TYPE_QUERY && TextUtils.isEmpty(queryUrl)) {
            Log.e(TAG, "the query url is not config");
            return null;
        }

        try {
            ePlatform p = getPlatform();
            int accountType = 0;
            if (p == ePlatform.WX) {
                accountType = 1;
            }

            Map<String, String> params = new HashMap<String, String>();
            params.put("orderID", orderID);
            params.put("channelID", U8SDK.getInstance().getCurrChannel() + "");
            params.put("userID", U8SDK.getInstance().getUToken().getUserID() + "");
            params.put("accountType", accountType + "");
            params.put("openID", openId);
            params.put("openKey", openKey);
            params.put("pf", pf);
            params.put("pfkey", pfKey);
            params.put("zoneid", serverID);
            params.put("sign", generateSign(params));

            String url = null;
            if (reqType == REQ_TYPE_QUERY) {
                url = queryUrl;
            } else {
                url = payUrl;
            }

            String result = U8HttpUtils.httpGet(url, params);

            Log.e(TAG, "the pay req to u8server response : " + result);

            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    private String generateSign(Map<String, String> params) {
        StringBuffer content = new StringBuffer();

        // 按照key做排序
        List<String> keys = new ArrayList<String>(params.keySet());
        Collections.sort(keys);
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            String value = params.get(key) == null ? "" : params.get(key).toString();
            if (value != null) {
                content.append(key + "=" + value);
            } else {
                content.append(key + "=");
            }
        }

        String signData = content.toString();

        signData = signData + U8SDK.getInstance().getAppKey();
        Log.e(TAG, "the pay urlencode signdata : " + signData);
        Log.e(TAG,
                "the pay urlencode tolowercase : " + EncryptUtils.md5(signData).toLowerCase(Locale.getDefault()));
        return EncryptUtils.md5(signData).toLowerCase(Locale.getDefault());

    }

    class QueryReqTask extends AsyncTask<String, Void, String> {

        private boolean showTip = false;

        public QueryReqTask(boolean showTip) {
            this.showTip = showTip;
        }

        protected void onPreExecute() {
            if (showTip) {
                U8SDK.getInstance().runOnMainThread(new Runnable() {

                    @Override
                    public void run() {
                        showProgressDialog("正在查询余额，请稍后...");
                    }
                });
            }
        }

        @Override
        protected String doInBackground(String... args) {

            String opType = args[0];
            String orderID = args[1];
            String zoneID = args[2];

            return reqCharge(Integer.valueOf(opType), orderID, zoneID);
        }

        protected void onPostExecute(final String result) {
            U8SDK.getInstance().runOnMainThread(new Runnable() {

                @Override
                public void run() {
                    hideProgressDialog();
                }
            });

            if (TextUtils.isEmpty(result)) {
                showTip("查询剩余金额失败");
                return;
            }

            try {
                JSONObject json = new JSONObject(result);
                int state = json.getInt("state");

                if (state == 1 || state == 0) {
                    final int money = json.optInt("money");

                    Log.e(TAG, "the left money is " + money);
                    if (showTip && money > 0) {
                        // showTip("当前有"+money+"余额，目前需要："+(payData.getPrice() * ratio));
                        final int leftRmby = money / ratio;
                        if (leftRmby >= payData.getPrice()) {
                            U8SDK.getInstance().runOnMainThread(new Runnable() {

                                @Override
                                public void run() {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(
                                            activity);
                                    builder.setTitle("购买确认");
                                    builder.setMessage("您当前拥有" + leftRmby + "元余额，是否使用余额支付？");
                                    builder.setCancelable(true);
                                    builder.setPositiveButton("确  定", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            // 确定就用余额支付
                                            chargeWhenPaySuccess();
                                        }
                                    });
                                    builder.setNeutralButton("取  消", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            // 如果取消，就直接支付，不使用余额
                                            payInternal(0);
                                        }
                                    });
                                    builder.show();
                                }
                            });
                            return;
                        } else {
                            U8SDK.getInstance().runOnMainThread(new Runnable() {

                                @Override
                                public void run() {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(
                                            activity);
                                    builder.setTitle("购买确认");
                                    builder.setMessage("您当前拥有" + leftRmby + "元余额，是否使用这部分余额？");
                                    builder.setCancelable(true);
                                    builder.setPositiveButton("确  定", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            // 确定就用余额支付
                                            // chargeWhenPaySuccess();
                                            payInternal(money);

                                        }
                                    });
                                    builder.setNeutralButton("取  消", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            // 如果取消，就直接支付，不使用余额
                                            payInternal(0);
                                        }
                                    });
                                    builder.show();
                                }
                            });

                            return;
                        }

                    } else {
                        Log.e(TAG, "the query result is " + result);
                        payInternal(0);
                    }

                }

            } catch (JSONException e) {
                U8SDK.getInstance().onResult(U8Code.CODE_PAY_FAIL, e.getMessage());
                e.printStackTrace();
            }
        }

    }

    class PayReqTask extends AsyncTask<String, Void, String> {

        private boolean showTip = false;

        public PayReqTask(boolean showTip) {
            this.showTip = showTip;
        }

        protected void onPreExecute() {
            if (showTip) {
                U8SDK.getInstance().runOnMainThread(new Runnable() {

                    @Override
                    public void run() {
                        showProgressDialog("正在处理,请稍候...");
                    }
                });
            }
        }

        @Override
        protected String doInBackground(String... args) {

            String opType = args[0];
            String orderID = args[1];
            String zoneID = args[2];

            return reqCharge(Integer.valueOf(opType), orderID, zoneID);
        }

        protected void onPostExecute(final String result) {

            U8SDK.getInstance().runOnMainThread(new Runnable() {

                @Override
                public void run() {
                    hideProgressDialog();

                }
            });

            try {
                JSONObject json = new JSONObject(result);
                int state = json.getInt("state");

                if (state == 1) {
                    TDGAVirtualCurrency.onChargeSuccess(orderId);

                    U8SDK.getInstance().onResult(U8Code.CODE_PAY_SUCCESS, "pay success");
                    // showTip("支付成功,到账可能稍有延迟");
                } else {
                    U8SDK.getInstance().onResult(U8Code.CODE_PAY_FAIL, "pay fail");
                    // showTip("支付失败,重新登录后,会重新查询并支付");
                }

            } catch (JSONException e) {
                e.printStackTrace();
                U8SDK.getInstance().onResult(U8Code.CODE_PAY_FAIL, "pay fail" + e.getMessage());
                U8SDK.getInstance().onResult(U8Code.CODE_PAY_UNKNOWN, e.getMessage());
            }

        }

    }

    private void showProgressDialog(String msg) {
        if (loadingActivity != null) {
            return;
        }

        loadingActivity = new ProgressDialog(activity);
        loadingActivity.setIndeterminate(true);
        loadingActivity.setCancelable(false);
        loadingActivity.setMessage(msg);
        loadingActivity.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface arg0) {
            }
        });
        loadingActivity.show();
    }

    private void hideProgressDialog() {
        if (loadingActivity == null) {
            return;
        }
        loadingActivity.dismiss();
        loadingActivity = null;
    }

    private ProgressDialog loadingActivity = null;

    public void showTip(final String tip) {
        U8SDK.getInstance().runOnMainThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activity, tip, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 防沉迷
     */
    @SuppressLint("SetJavaScriptEnabled")
    @SuppressWarnings("deprecation")
    public void executeInstruction(final AntiAddictRet ret) {

        switch (ret.type) {
            case AntiAddictRet.TYPE_TIPS:
            case AntiAddictRet.TYPE_LOGOUT:
                Log.e(TAG, "防沉迷弹提示");
                if (!mAntiAddictExecuteState) {
                    mAntiAddictExecuteState = true;
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    builder.setTitle(ret.title);
                    builder.setMessage(ret.content);
                    builder.setPositiveButton("知道了", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int index) {
                            if (ret.modal == 1) {
                                //强制用户下线
                                activity.finish();
                                System.exit(0);
                            }
                            mAntiAddictExecuteState = false;
                        }
                    });
                    builder.setCancelable(false);
                    builder.show();
                    //已执行指令，上报接口
                    YSDKApi.reportAntiAddictExecute(ret, System.currentTimeMillis());
                }

                break;

            case AntiAddictRet.TYPE_OPEN_URL:
                Log.e(TAG, "防沉迷打开网页窗口");
                if (!mAntiAddictExecuteState) {
                    mAntiAddictExecuteState = true;
                    View popWindowView = View.inflate(activity, getLayoutId(activity, "u8_pop_window_web_layout"), null);
                    WebView webView = (WebView) popWindowView.findViewById(getId(activity, "u8_pop_window_webview"));
                    ImageButton closeButton = (ImageButton) popWindowView.findViewById(getId(activity, "u8_pop_window_close"));

                    WebSettings settings = webView.getSettings();
                    settings.setJavaScriptEnabled(true);
                    webView.setWebViewClient(new WebViewClient());
                    webView.loadUrl(ret.url);

                    final PopupWindow popupWindow = new PopupWindow(popWindowView, 1000, 1000);
                    popupWindow.setTouchable(true);
                    popupWindow.setOutsideTouchable(false);
                    popupWindow.setBackgroundDrawable(new BitmapDrawable());

                    closeButton.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {
                            if (ret.modal == 1) {
                                //强制用户下线
                                activity.finish();
                                System.exit(0);
                            }
                            popupWindow.dismiss();
                            mAntiAddictExecuteState = false;
                        }
                    });

                    popupWindow.showAtLocation(popWindowView, Gravity.CENTER, 0, 0);
                    //已执行指令，上报接口
                    YSDKApi.reportAntiAddictExecute(ret, System.currentTimeMillis());
                }

                break;

        }
    }

    // 拉取异常号
    public void showDiffLogin() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("异账号提示");
        builder.setMessage("你当前拉起的账号与你本地的账号不一致，请选择使用哪个账号登陆：");
        builder.setPositiveButton("本地账号", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Toast.makeText(activity, "选择使用本地账号", Toast.LENGTH_LONG).show();
                if (!YSDKApi.switchUser(false)) {
                    U8SDK.getInstance().onLogout();
                }
            }
        });
        builder.setNeutralButton("拉起账号", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Toast.makeText(activity, "选择使用拉起账号", Toast.LENGTH_LONG).show();
                if (!YSDKApi.switchUser(true)) {
                    U8SDK.getInstance().onLogout();
                }
            }
        });
        builder.show();
    }

    public int getLayoutId(Context paramContext, String paramString) {
        return paramContext.getResources().getIdentifier(paramString, "layout", paramContext.getPackageName());
    }

    public int getId(Context paramContext, String paramString) {
        return paramContext.getResources().getIdentifier(paramString, "id", paramContext.getPackageName());
    }
}
