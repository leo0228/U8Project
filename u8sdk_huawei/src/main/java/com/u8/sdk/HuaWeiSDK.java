package com.u8.sdk;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.huawei.hmf.tasks.OnCompleteListener;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.common.ApiException;
import com.huawei.hms.iap.Iap;
import com.huawei.hms.iap.IapApiException;
import com.huawei.hms.iap.IapClient;
import com.huawei.hms.iap.entity.ConsumeOwnedPurchaseReq;
import com.huawei.hms.iap.entity.ConsumeOwnedPurchaseResult;
import com.huawei.hms.iap.entity.InAppPurchaseData;
import com.huawei.hms.iap.entity.IsEnvReadyResult;
import com.huawei.hms.iap.entity.OrderStatusCode;
import com.huawei.hms.iap.entity.OwnedPurchasesReq;
import com.huawei.hms.iap.entity.OwnedPurchasesResult;
import com.huawei.hms.iap.entity.PurchaseIntentResult;
import com.huawei.hms.iap.entity.PurchaseIntentWithPriceReq;
import com.huawei.hms.iap.entity.PurchaseResultInfo;
import com.huawei.hms.iap.util.IapClientHelper;
import com.huawei.hms.jos.AppUpdateClient;
import com.huawei.hms.jos.JosApps;
import com.huawei.hms.jos.JosAppsClient;
import com.huawei.hms.jos.games.AppPlayerInfo;
import com.huawei.hms.jos.games.Games;
import com.huawei.hms.jos.games.PlayersClient;
import com.huawei.hms.jos.games.player.GameTrialProcess;
import com.huawei.hms.jos.games.player.Player;
import com.huawei.hms.jos.games.player.PlayerExtraInfo;
import com.huawei.hms.support.account.AccountAuthManager;
import com.huawei.hms.support.account.request.AccountAuthParams;
import com.huawei.hms.support.account.request.AccountAuthParamsHelper;
import com.huawei.hms.support.account.result.AuthAccount;
import com.huawei.hms.support.account.service.AccountAuthService;
import com.huawei.hms.support.api.client.Status;
import com.huawei.updatesdk.service.appmgr.bean.ApkUpgradeInfo;
import com.huawei.updatesdk.service.otaupdate.CheckUpdateCallBack;
import com.huawei.updatesdk.service.otaupdate.UpdateKey;
import com.tendcloud.tenddata.TDGAProfile;
import com.tendcloud.tenddata.TDGAVirtualCurrency;
import com.u8.sdk.utils.U8HttpUtils;
import com.zjtx.prompt.PromptDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class HuaWeiSDK {

    private static final String URL = "http://myserver.moyoi.cn:8093/pay/huawei2/payCallback/24";

    private static final String TAG = "HuaWeiSDK";
    private boolean debug;

    private static HuaWeiSDK instance;
    private Activity activity;

    private final int SIGN_IN_INTENT = 3000;
    private final int PAY_INTENT = 6666;
    private final int PAY_NOT_AGREEMENT_INTENT = 8888;

    private PlayersClient playersClient;
    private AppUpdateClient updateClient;
    private AccountAuthService authService;

    private String playerId = "";
    private String transactionId = "";

    private String inAppPurchaseData = "";

    private SendTask sendTask;

    private HuaWeiSDK() {
        activity = U8SDK.getInstance().getContext();
    }

    public static HuaWeiSDK getInstance() {
        if (instance == null) {
            instance = new HuaWeiSDK();
        }
        return instance;
    }

    private void parseSDKParams(SDKParams params) {
        debug = params.getBoolean("debug_mode");
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
            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};

    private void initSDK() {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(activity,
                    Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, needPermissions, 0);
            }

        }

        JosAppsClient appsClient = JosApps.getJosAppsClient(activity);
        appsClient.init();
        U8SDK.getInstance().onResult(U8Code.CODE_INIT_SUCCESS, "init success");

        //应用升级
        updateClient = JosApps.getAppUpdateClient(activity);
        updateClient.checkAppUpdate(activity, new UpdateCallBack());

        obtainOwnedPurchases();//补单

        U8SDK.getInstance().setActivityCallback(new ActivityCallbackAdapter() {

            @Override
            public void onStart() {
                super.onStart();

                submitPlayerBeginEvent();
            }

            public void onResume() {
                super.onResume();

                Games.getBuoyClient(activity).showFloatWindow();
            }

            public void onPause() {
                super.onPause();

                Games.getBuoyClient(activity).hideFloatWindow();
            }

            @Override
            public void onStop() {
                super.onStop();

                submitPlayerEndEvent();
            }


            @Override
            public void onActivityResult(int requestCode, int resultCode, Intent data) {
                // TODO Auto-generated method stub
                super.onActivityResult(requestCode, resultCode, data);

                if (requestCode == SIGN_IN_INTENT) {
                    if (data != null) {
                        handleSignInResult(data);
                    }
                }

                if (requestCode == PAY_INTENT) {
                    if (data != null) {
                        PurchaseResultInfo purchaseResultInfo = Iap.getIapClient(activity).parsePurchaseResultInfoFromIntent(data);
                        i("onActivityResult->pay, code: " + purchaseResultInfo.getReturnCode());

                        switch (purchaseResultInfo.getReturnCode()) {
                            case OrderStatusCode.ORDER_STATE_CANCEL:
                                U8SDK.getInstance().onResult(U8Code.CODE_PAY_CANCEL, "pay cancel");
                                break;
                            case OrderStatusCode.ORDER_STATE_SUCCESS:
                                TDGAVirtualCurrency.onChargeSuccess(orderId);

                                inAppPurchaseData = purchaseResultInfo.getInAppPurchaseData();
                                i("正常->inAppPurchaseData:" + inAppPurchaseData);
                                String inAppDataSignature = purchaseResultInfo.getInAppDataSignature();

                                try {
                                    //验证公钥
                                    if (CipherUtil.doCheck(inAppPurchaseData, inAppDataSignature)) {
                                        //需要客户端向服务器请求发货，发货后消耗商品
                                        InAppPurchaseData inAppPurchaseDataBean = new InAppPurchaseData(inAppPurchaseData);
                                        send(inAppPurchaseDataBean);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                U8SDK.getInstance().onResult(U8Code.CODE_PAY_SUCCESS, "pay success");
                                break;

                            case OrderStatusCode.ORDER_PRODUCT_OWNED:// 已拥有该商品
                            case OrderStatusCode.ORDER_STATE_FAILED:
                            default:
                                U8SDK.getInstance().onResult(U8Code.CODE_PAY_FAIL, "pay fail");
                                break;
                        }
                    }
                    return;
                }

                if (requestCode == PAY_NOT_AGREEMENT_INTENT) {
                    int returnCode = IapClientHelper.parseRespCodeFromIntent(data);
                    if (returnCode == OrderStatusCode.ORDER_STATE_SUCCESS) {
                        // The request was successful, and then you need to make another createPurchaseIntentWithPrice request
                    }
                    return;
                }

            }

            @Override
            public void onDestroy() {
                if (updateClient != null) {
                    updateClient.releaseCallBack();
                }
                if (sendTask != null) {
                    sendTask = null;
                }
            }
        });

    }

    public void switchLogin() {
        U8SDK.getInstance().onLogout();
        U8SDK.getInstance().onResult(U8Code.CODE_LOGOUT_SUCCESS, "logout");
    }

    public void logout() {
        if (authService == null) {
            U8SDK.getInstance().onLogout();
            U8SDK.getInstance().onResult(U8Code.CODE_LOGOUT_SUCCESS, "logout");
        } else {
            Task<Void> signOutTask = authService.signOut();
            signOutTask.addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(Task<Void> task) {
                    //完成退出后的处理
                    i("signOut complete");
                    U8SDK.getInstance().onLogout();
                    U8SDK.getInstance().onResult(U8Code.CODE_LOGOUT_SUCCESS, "logout");
                }
            });
        }
    }

    public void login() {
//        HuaweiIdAuthParams authParams = new HuaweiIdAuthParamsHelper(HuaweiIdAuthParams.DEFAULT_AUTH_REQUEST_PARAM_GAME).createParams();
//        HuaweiIdAuthService service = HuaweiIdAuthManager.getService(activity, authParams);
//        activity.startActivityForResult(service.getSignInIntent(), SIGN_IN_INTENT);

        AccountAuthParams authParams = new AccountAuthParamsHelper(AccountAuthParams.DEFAULT_AUTH_REQUEST_PARAM_GAME)
                .createParams();
        authService = AccountAuthManager.getService(activity, authParams);
        activity.startActivityForResult(authService.getSignInIntent(), SIGN_IN_INTENT);
    }

    private void handleSignInResult(Intent data) {
        if (null == data) {
            Log.e(TAG, "signIn intent is null");
            return;
        }
//        Task<AuthHuaweiId> task = HuaweiIdAuthManager.parseAuthResultFromIntent(data);
//        task.addOnSuccessListener(new OnSuccessListener<AuthHuaweiId>() {
//            @Override
//            public void onSuccess(AuthHuaweiId authHuaweiId) {
//                Log.i(TAG, "sign in success.");
//                // 获取玩家信息
//                getPlayersInfo(authHuaweiId);
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(Exception e) {
//                U8SDK.getInstance().onResult(U8Code.CODE_LOGIN_FAIL, "login failed");
//
//                if (e instanceof ApiException) {
//                    Log.e(TAG, "parseAuthResultFromIntent failed, code: " + ((ApiException) e).getStatusCode());
//                }
//            }
//        });

        Task<AuthAccount> authAccountTask = AccountAuthManager.parseAuthResultFromIntent(data);
        if (authAccountTask.isSuccessful()) {
            i("sign in success.");
            //登录成功，获取用户的帐号信息和ID Token
            AuthAccount authAccount = authAccountTask.getResult();
            getPlayersInfo(authAccount);
        } else {
            U8SDK.getInstance().onResult(U8Code.CODE_LOGIN_FAIL, "login failed");
            //登录失败，不需要做处理，打点日志方便定位
            Log.e(TAG, "sign in failed : " + ((ApiException) authAccountTask.getException()).getStatusCode());
        }
    }

    private void getPlayersInfo(final AuthAccount authAccount) {
        if (playersClient == null) {
            playersClient = Games.getPlayersClient(activity);
//            playersClient = Games.getPlayersClient(activity, authHuaweiId);
        }
        //获取当前玩家信息
        Task<Player> playerTask = playersClient.getCurrentPlayer();
        playerTask.addOnSuccessListener(new OnSuccessListener<Player>() {
            @Override
            public void onSuccess(Player player) {
                if (player == null) return;

                // 请求成功，获取玩家信息
                String signTs = player.getSignTs();
                i("signTs:" + signTs);
                playerId = player.getPlayerId();
                i("playerId:" + playerId);
                String accessToken = player.getPlayerSign();
                i("accessToken:" + accessToken);
                String nickName = player.getDisplayName();
                i("nickName:" + nickName);

                try {
                    JSONObject json = new JSONObject();
                    json.put("ts", signTs);
                    json.put("playerId", playerId);
                    json.put("accessToken", accessToken);
                    json.put("nickName", nickName);

                    String urlString = URLEncoder.encode(json.toString(), "GBK");
                    U8SDK.getInstance().onLoginResult(urlString);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                submitPlayerBeginEvent();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                U8SDK.getInstance().onResult(U8Code.CODE_LOGIN_FAIL, "login failed");
                // 获取玩家信息失败
                if (e instanceof ApiException) {
                    //7005表示请求参数错误，如appid、cpid等信息错误
                    //7021表示实名认证返回强制实名但是用户取消，或需要强制实名但没有实名，请牵引玩家上传身份证照片或银行卡进行实名
                    Log.e(TAG, "getPlayerInfo failed, code: " + ((ApiException) e).getStatusCode());
                }
            }
        });
    }

    /**
     * 上报玩家进入游戏事件
     */
    private void submitPlayerBeginEvent() {
        if (playersClient == null) return;
        if (playerId == null || playerId.equals("")) return;

        String uid = UUID.randomUUID().toString();
        Task<String> task = playersClient.submitPlayerEvent(playerId, uid, "GAMEBEGIN");
        task.addOnSuccessListener(new OnSuccessListener<String>() {
            @Override
            public void onSuccess(String jsonRequest) {
                i("submitPlayerBeginEvent successfully");
                try {
                    JSONObject data = new JSONObject(jsonRequest);
                    transactionId = data.getString("transactionId");

                    //每15分钟查询玩家附加信息
                    timeCheck();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                if (e instanceof ApiException) {
                    //7022表示该用户为成年用户或未实名，不支持实名时长统计功能。
                    //7013未登录华为帐号。 请先调用华为帐号授权接口，引导用户完成帐号授权。
                    Log.e(TAG, "submitPlayerBeginEvent failed, code: " + ((ApiException) e).getStatusCode());
                }
            }
        });
    }

    /**
     * 上报玩家退出游戏事件
     */
    private void submitPlayerEndEvent() {
        if (playersClient == null) return;
        if (playerId == null || playerId.equals("")) return;
        if (transactionId == null || transactionId.equals("")) return;

        Task<String> task = playersClient.submitPlayerEvent(playerId, transactionId, "GAMEEND");
        task.addOnSuccessListener(new OnSuccessListener<String>() {
            @Override
            public void onSuccess(String s) {
                i("submitPlayerEndEvent successfully");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                if (e instanceof ApiException) {
                    Log.e(TAG, "submitPlayerEndEvent failed, code: " + ((ApiException) e).getStatusCode());
                }
            }
        });
    }

    /**
     * 查询玩家附加信息
     */
    private void getPlayerExtraInfo() {
        if (playersClient == null) return;
        if (transactionId == null || transactionId.equals("")) return;

        Task<PlayerExtraInfo> task = playersClient.getPlayerExtraInfo(transactionId);
        task.addOnSuccessListener(new OnSuccessListener<PlayerExtraInfo>() {
            @Override
            public void onSuccess(PlayerExtraInfo extra) {
                if (extra != null) {
                    i("IsRealName: " + extra.getIsRealName() + ", IsAdult: " + extra.getIsAdult()
                            + ", PlayerId: " + extra.getPlayerId() + ", PlayerDuration: " + extra.getPlayerDuration());
                    if (!extra.getIsAdult()) {
                        //未成年
                    }
                } else {
                    Log.e(TAG, "Player extra info is empty. ");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                if (e instanceof ApiException) {
                    Log.e(TAG, "getPlayerExtraInfo failed, code: " + ((ApiException) e).getStatusCode());
                }
            }
        });
    }

    private AlertDialog dialog;

    /**
     * 游戏试玩
     */
    private void gameTrialProcess() {
        if (playersClient == null) return;
        playersClient.setGameTrialProcess(new GameTrialProcess() {
            @Override
            public void onTrialTimeout() {
                //试玩时间结束
                if (activity == null || activity.isFinishing()) {
                    return;
                }

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                        dialog = new AlertDialog.Builder(activity).setMessage("Pause game").setCancelable(false).create();
                        dialog.show();
                    }
                });
            }

            @Override
            public void onCheckRealNameResult(boolean hasRealName) {
                if (hasRealName) {
                    // 已实名，继续后续的游戏登录处理
                    onRealName();
                    return;
                }
                //未实名，建议您提示玩家后退出游戏或引导玩家重新登录并实名认证
                onNoRealName();
            }
        });
    }

    private void onRealName() {
        Log.i(TAG, "resume game");
        if (activity == null || activity.isFinishing()) {
            return;
        }

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (dialog != null) {
                    dialog.dismiss();
                }
                dialog = new AlertDialog.Builder(activity).setMessage("Resume game")
                        .setCancelable(false)
                        .setPositiveButton("Go on", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .create();
                dialog.show();
            }
        });

    }

    private void onNoRealName() {
        Log.i(TAG, "not real name");
        if (activity == null || activity.isFinishing()) {
            return;
        }

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (dialog != null) {
                    dialog.dismiss();
                }
                dialog = new AlertDialog.Builder(activity).setMessage("Not real name")
                        .setCancelable(false)
                        .setPositiveButton("I got it", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .create();
                dialog.show();
            }
        });
    }

    public void submitGameData(UserExtraData extraData) {
        switch (extraData.getDataType()) {
            case UserExtraData.TYPE_ENTER_GAME:
                enterGame(extraData);
                sendExtraData(extraData);
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

    public void sendExtraData(UserExtraData data) {
        if (playersClient == null) return;
        if (playerId == null || playerId.equals("")) return;

        AppPlayerInfo appPlayerInfo = new AppPlayerInfo();
        appPlayerInfo.area = data.getServerName();
        appPlayerInfo.rank = data.getRoleLevel();
        appPlayerInfo.role = data.getRoleName();
        appPlayerInfo.playerId = playerId;

        Task<Void> task = playersClient.savePlayerInfo(appPlayerInfo);
        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void v) {
                i("save player info successfully");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                if (e instanceof ApiException) {
                    Log.e(TAG, "savePlayerInfo failed, code: " + ((ApiException) e).getStatusCode());
                }
            }
        });
    }

    private String orderId;

    public void pay(final PayParams params) {
        //判断用户当前登录的华为帐号所在的服务地是否在华为IAP支持结算的国家或地区中。
        //如果您的应用未接入华为帐号的登录接口，您可以通过该接口完成登录操作
        Iap.getIapClient(activity).isEnvReady().addOnSuccessListener(new OnSuccessListener<IsEnvReadyResult>() {
            @Override
            public void onSuccess(IsEnvReadyResult result) {
                i("IsEnvReadyResult successfully");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                U8SDK.getInstance().onResult(U8Code.CODE_PAY_FAIL, "pay fail");

                if (e instanceof IapApiException) {
                    IapApiException apiException = (IapApiException) e;
                    Status status = apiException.getStatus();

                    //未登录
                    if (status.getStatusCode() == OrderStatusCode.ORDER_HWID_NOT_LOGIN) {
                        if (status.hasResolution()) {
                            try {
                                //启动华为IAP登录
                                status.startResolutionForResult(activity, SIGN_IN_INTENT);
                            } catch (IntentSender.SendIntentException exp) {
                            }
                        }
                    } else if (status.getStatusCode() == OrderStatusCode.ORDER_ACCOUNT_AREA_NOT_SUPPORTED) {
                        // 目前该地区不支持华为IAP
                        Log.e(TAG, "目前该地区不支持华为IAP");
                    }
                }

                return;
            }
        });

        orderId = params.getOrderID();
        String productId = params.getProductId();
        String productName = params.getProductName();
        String amount = String.format("%.2f", Float.valueOf(params.getPrice()));
        String extension = params.getExtension();

        PurchaseIntentWithPriceReq req = new PurchaseIntentWithPriceReq();
//        PurchaseIntentReq req = new PurchaseIntentReq();//5.1版本
        req.setPriceType(IapClient.PriceType.IN_APP_CONSUMABLE);
        req.setSdkChannel("3");//代表游戏中心
        req.setServiceCatalog("X6");//游戏
        req.setCountry("CN");
        req.setCurrency("CNY");
        req.setProductId(productId);
        req.setProductName(productName);
        req.setAmount(amount);
        req.setDeveloperPayload(orderId);

        try {
            JSONObject json = new JSONObject();
            json.put("ext", extension);
            req.setReservedInfor(json.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        TDGAVirtualCurrency.onChargeRequest(orderId, params.getProductName(), params.getPrice(), "CNY", params.getPrice() * 7, "huawai SDK");

        Task<PurchaseIntentResult> task = Iap.getIapClient(activity).createPurchaseIntentWithPrice(req);
//        Task<PurchaseIntentResult> task = Iap.getIapClient(activity).createPurchaseIntent(req);//5.1版本
        task.addOnSuccessListener(new OnSuccessListener<PurchaseIntentResult>() {
            @Override
            public void onSuccess(PurchaseIntentResult result) {
                i("PurchaseIntentResult successfully");

                Status status = result.getStatus();
                if (status.hasResolution()) {
                    try {
                        //启动华为IAP收银台
                        status.startResolutionForResult(activity, PAY_INTENT);
                    } catch (IntentSender.SendIntentException exp) {
                    }
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                if (e instanceof IapApiException) {
                    U8SDK.getInstance().onResult(U8Code.CODE_PAY_FAIL, "pay fail");

                    IapApiException apiException = (IapApiException) e;
                    Status status = apiException.getStatus();
                    int returnCode = apiException.getStatusCode();
                    Log.e(TAG, "onFailure->pay failed, code: " + returnCode + ", status: " + status.getStatusMessage());

                    switch (returnCode) {
                        case OrderStatusCode.ORDER_HWID_NOT_LOGIN:// 未登录
                            if (status.hasResolution()) {
                                try {
                                    //启动华为IAP登录
                                    status.startResolutionForResult(activity, SIGN_IN_INTENT);
                                } catch (IntentSender.SendIntentException exp) {
                                }
                            }
                            break;

                        case OrderStatusCode.ORDER_NOT_ACCEPT_AGREEMENT:// 未同意协议
                            if (status.hasResolution()) {
                                try {
                                    status.startResolutionForResult(activity, PAY_NOT_AGREEMENT_INTENT);
                                } catch (IntentSender.SendIntentException exp) {
                                }
                            }
                            break;

                        case OrderStatusCode.ORDER_PRODUCT_OWNED:// 已拥有该商品
                        case OrderStatusCode.ORDER_STATE_FAILED:
                            obtainOwnedPurchases();//补单
                            U8SDK.getInstance().onResult(U8Code.CODE_PAY_FAIL, "pay fail");
                            break;

                    }
                }
            }
        });

    }

    /**
     * 消耗商品
     */
    private void consume(String inAppPurchaseData) {
        String purchaseToken = "";
        try {
            InAppPurchaseData inAppPurchaseDataBean = new InAppPurchaseData(inAppPurchaseData);
            purchaseToken = inAppPurchaseDataBean.getPurchaseToken();
        } catch (JSONException e) {
        }
        ConsumeOwnedPurchaseReq req = new ConsumeOwnedPurchaseReq();
        req.setPurchaseToken(purchaseToken);
        Task<ConsumeOwnedPurchaseResult> task = Iap.getIapClient(activity).consumeOwnedPurchase(req);
        task.addOnSuccessListener(new OnSuccessListener<ConsumeOwnedPurchaseResult>() {
            @Override
            public void onSuccess(ConsumeOwnedPurchaseResult result) {
                i("ConsumeOwnedPurchaseResult successfully");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                if (e instanceof IapApiException) {
                    IapApiException apiException = (IapApiException) e;
                    Status status = apiException.getStatus();
                    int returnCode = apiException.getStatusCode();
                    Log.e(TAG, "consume failed, code: " + returnCode + ", status: " + status.getStatusMessage());
                }
            }
        });
    }

    /**
     * 消耗型商品的补单机制
     */
    private void obtainOwnedPurchases() {
        OwnedPurchasesReq ownedPurchasesReq = new OwnedPurchasesReq();
        ownedPurchasesReq.setPriceType(IapClient.PriceType.IN_APP_CONSUMABLE);
        Task<OwnedPurchasesResult> task = Iap.getIapClient(activity).obtainOwnedPurchases(ownedPurchasesReq);
        task.addOnSuccessListener(new OnSuccessListener<OwnedPurchasesResult>() {
            @Override
            public void onSuccess(OwnedPurchasesResult result) {
                i("OwnedPurchasesResult successfully");

                if (result != null && result.getInAppPurchaseDataList() != null) {
                    for (int i = 0; i < result.getInAppPurchaseDataList().size(); i++) {
                        inAppPurchaseData = result.getInAppPurchaseDataList().get(i);
                        i("补单->inAppPurchaseData:" + inAppPurchaseData);
                        String inAppDataSignature = result.getInAppSignature().get(i);

                        //验证公钥
                        if (CipherUtil.doCheck(inAppPurchaseData, inAppDataSignature)) {
                            try {
                                InAppPurchaseData inAppPurchaseDataBean = new InAppPurchaseData(inAppPurchaseData);
                                int purchaseState = inAppPurchaseDataBean.getPurchaseState();
                                if (purchaseState == 0) {
                                    i("此次交易是成功的，应用仅需要对这部分商品进行补发货操作");
                                    //需要客户端向服务器请求发货，发货后消耗商品
                                    send(inAppPurchaseDataBean);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                if (e instanceof IapApiException) {
                    IapApiException apiException = (IapApiException) e;
                    Status status = apiException.getStatus();
                    int returnCode = apiException.getStatusCode();
                    Log.e(TAG, "ownedPurchases failed, code: " + returnCode + ", status: " + status.getStatusMessage());
                }
            }
        });
    }

    private void send(InAppPurchaseData inAppPurchaseDataBean) {
        String requestId = inAppPurchaseDataBean.getDeveloperPayload();
        String amount = String.valueOf(inAppPurchaseDataBean.getPrice());
        String purchaseToken = inAppPurchaseDataBean.getPurchaseToken();
        String productId = inAppPurchaseDataBean.getProductId();
        String sign = Md5Util.string2MD5(requestId + amount + purchaseToken + productId);

        sendTask = new SendTask();
        sendTask.execute(requestId, amount, purchaseToken, productId, sign);
    }


    private class SendTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... args) {
            Map<String, String> params = new HashMap<String, String>();
            params.put("requestId", args[0]);
            params.put("amount", args[1]);
            params.put("purchaseToken", args[2]);
            params.put("productId", args[3]);
            params.put("sign", args[4]);
            return U8HttpUtils.httpPost(URL, params);
        }

        @Override
        protected void onPostExecute(String result) {
            if (result == null) return;
            i("result:" + result);
            try {
                JSONObject obj = new JSONObject(result);
                int code = obj.optInt("result");

                if (code == 0) {
                    consume(inAppPurchaseData);
                }
            } catch (JSONException e) {

            }
        }
    }

    //查看用户购买历史
    public void obtainOwnedPurchaseRecord() {
        OwnedPurchasesReq req = new OwnedPurchasesReq();
        req.setPriceType(0);
        Task<OwnedPurchasesResult> task = Iap.getIapClient(activity).obtainOwnedPurchaseRecord(req);
        task.addOnSuccessListener(new OnSuccessListener<OwnedPurchasesResult>() {
            @Override
            public void onSuccess(OwnedPurchasesResult result) {
                i("OwnedPurchasesResult successfully");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                if (e instanceof IapApiException) {
                    IapApiException apiException = (IapApiException) e;
                    Status status = apiException.getStatus();
                    int returnCode = apiException.getStatusCode();
                    Log.e(TAG, "ownedPurchases failed, code: " + returnCode + ", status: " + status.getStatusMessage());
                }
            }
        });
    }

    private void timeCheck() {
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                getPlayerExtraInfo();
            }
        };
        timer.schedule(task, 15 * 60 * 1000);
    }

    private void i(String log) {
        if (debug) {
            Log.i(TAG, log);
        }
    }

    private class UpdateCallBack implements CheckUpdateCallBack {

        int DEFAULT_VALUE = -1;

        public UpdateCallBack() {
        }

        @Override
        public void onUpdateInfo(Intent intent) {
            if (intent != null) {
                // 获取更新状态码， Default_value为取不到status时默认的返回码，由应用自行决定
                int status = intent.getIntExtra(UpdateKey.STATUS, DEFAULT_VALUE);
                // 错误码，建议打印
                int rtnCode = intent.getIntExtra(UpdateKey.FAIL_CODE, DEFAULT_VALUE);
                // 失败信息，建议打印
                String rtnMessage = intent.getStringExtra(UpdateKey.FAIL_REASON);
                Serializable info = intent.getSerializableExtra(UpdateKey.INFO);
                //可通过获取到的info是否属于ApkUpgradeInfo类型来判断应用是否有更新
                if (info instanceof ApkUpgradeInfo) {
                    // 这里调用showUpdateDialog接口拉起更新弹窗
                    if (updateClient != null) {
                        /**
                         * 强制更新按钮选择。
                         *
                         * true：升级提示框只有升级按钮，无取消按钮，用户只能选择升级。
                         * false：升级提示框有升级按钮和取消按钮，用户可选择不升级。
                         */
                        updateClient.showUpdateDialog(activity, (ApkUpgradeInfo) info, true);
                    }
                }

                Log.e(TAG, "onUpdateInfo status: " + status + ", rtnCode: " + rtnCode + ", rtnMessage: " + rtnMessage);
            }
        }

        @Override
        public void onMarketInstallInfo(Intent intent) {

        }

        @Override
        public void onMarketStoreError(int i) {

        }

        @Override
        public void onUpdateStoreError(int i) {

        }
    }
}
