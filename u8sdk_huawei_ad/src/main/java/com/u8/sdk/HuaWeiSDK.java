package com.u8.sdk;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.huawei.hmf.tasks.OnCompleteListener;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.common.ApiException;
import com.huawei.hms.framework.common.NetworkUtil;
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
import com.huawei.openalliance.ad.inter.HiAd;
import com.huawei.openalliance.ad.inter.RewardAdLoader;
import com.huawei.openalliance.ad.inter.data.IRewardAd;
import com.huawei.openalliance.ad.inter.listeners.IRewardAdStatusListener;
import com.huawei.openalliance.ad.inter.listeners.RewardAdListener;
import com.huawei.updatesdk.service.appmgr.bean.ApkUpgradeInfo;
import com.huawei.updatesdk.service.otaupdate.CheckUpdateCallBack;
import com.huawei.updatesdk.service.otaupdate.UpdateKey;
import com.tendcloud.tenddata.TDGAProfile;
import com.u8.common.PromptDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

//import com.huawei.hms.ads.HwAds;

public class HuaWeiSDK {
    private static final String TAG = "HuaWeiSDK";

    private boolean debug;

    private static HuaWeiSDK instance;
    private Activity activity;

    private final int SIGN_IN_INTENT = 8888;

    private PlayersClient playersClient;
    private AppUpdateClient updateClient;
    private AccountAuthService authService;

    private String transactionId = "";
    private Timer timer = null;

    private RewardAdLoader rewardAdLoader;
    private RewardAdListener mRewardAdListener;
    private IRewardAdStatusListener mRewardAdStatusListener;
    private String ad_appId;
    private String ad_posId;

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
        ad_appId = params.getString("ad_appId");
        i("ad_appId=" + ad_appId);
        ad_posId = params.getString("ad_posId");
        i("ad_posId=" + ad_posId);
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

        U8SDK.getInstance().setActivityCallback(new ActivityCallbackAdapter() {
            @Override
            public void onResume() {

                Games.getBuoyClient(activity).showFloatWindow();
                //当玩家登录游戏或将游戏从后台切到游戏前台时,游戏定期
                getPlayerExtraInfo();
            }

            @Override
            public void onPause() {

                Games.getBuoyClient(activity).hideFloatWindow();
                //当玩家退出游戏、从前台切到后台或游戏异常退出（进程终止、手机重启等）时
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

            }

            @Override
            public void onDestroy() {
                if (updateClient != null) {
                    updateClient.releaseCallBack();
                }

            }
        });


        // 初始化HUAWEI Ads SDK
//        HwAds.init(activity);
        HiAd.getInstance(activity).initLog(debug, Log.INFO); // 初始化日志

        mRewardAdListener = new RewardAdListener() {

            @Override
            public void onAdsLoaded(Map<String, List<IRewardAd>> adMap) {
                i("onAdsLoaded");
                if (!checkAdMap(adMap)) {
                    U8SDK.getInstance().onResult(U8Code.CODE_ADS_FAILED, "广告加载或播放失败");
                    return;
                }

                U8SDK.getInstance().onResult(U8Code.CODE_ADS_LOADED, "广告加载完成，开始播放");

                addRewardAdView(adMap.get(ad_posId));
            }

            @Override
            public void onAdFailed(int errorCode) {
                Log.e(TAG, "RewardAdListener.onAdFailed, errorCode:" + errorCode);
                U8SDK.getInstance().onResult(U8Code.CODE_ADS_FAILED, "广告加载或播放失败");
            }
        };

        mRewardAdStatusListener = new IRewardAdStatusListener() {

            /**
             * 用户达成奖励，开发者可以在该接口中给用户发放奖励，
             * 同一个广告对象只会回调一次
             * */
            @Override
            public void onRewarded() {
                i("激励广告任务完成，发放奖励");
                U8SDK.getInstance().onResult(U8Code.CODE_ADS_COMPLETE, "广告播放完成");
            }

            /** 激励广告展示 */
            @Override
            public void onAdShown() {
                i("onAdShown");
                U8SDK.getInstance().onResult(U8Code.CODE_ADS_SHOW, "开始播放广告");
            }

            /** 激励广告被点击 */
            @Override
            public void onAdClicked() {
                i("onAdClicked");
                U8SDK.getInstance().onResult(U8Code.CODE_ADS_CLICKED, "广告被点击");
            }

            /** 激励广告视频播放结束 */
            @Override
            public void onAdCompleted() {
                Log.i(TAG, "onAdCompleted");
            }

            /**
             * 1、激励广告未播放完成，触发确认弹框，点击关闭按钮触发
             * 2、激励广告播放完成，点击关闭按钮触发
             * 3、激励广告播放完成，虚拟按钮返回不触发，集成自行处理
             * */
            @Override
            public void onAdClosed() {
                i("onAdClosed");
                U8SDK.getInstance().onResult(U8Code.CODE_ADS_CLOSED, "广告被关闭");
            }

            /** 在广告播放过程中出现异常 */
            @Override
            public void onAdError(int i, int i1) {
                Log.e(TAG, "onAdError:" + i);
                U8SDK.getInstance().onResult(U8Code.CODE_ADS_FAILED, "广告加载或播放失败");
            }
        };
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
        Task<AuthAccount> authAccountTask = AccountAuthManager.parseAuthResultFromIntent(data);
        if (authAccountTask.isSuccessful()) {
            i("sign in success.");
            //登录成功，获取用户的帐号信息和ID Token
            AuthAccount authAccount = authAccountTask.getResult();
            //获取帐号类型，0表示华为帐号、1表示AppTouch帐号
            i("accountFlag:" + authAccount.getAccountFlag());

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
        }
        //获取当前玩家信息
        Task<Player> playerTask = playersClient.getGamePlayer();
        playerTask.addOnSuccessListener(new OnSuccessListener<Player>() {
            @Override
            public void onSuccess(Player player) {
                if (player == null) return;

                String accessToken = player.getAccessToken();
                i("accessToken:" + accessToken);
                String nickName = player.getDisplayName();
                i("nickName:" + nickName);
                try {
                    JSONObject json = new JSONObject();
                    json.put("accessToken", accessToken);
                    json.put("nickName", nickName);

                    String urlString = URLEncoder.encode(json.toString(), "GBK");
                    U8SDK.getInstance().onLoginResult(urlString);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //当玩家登录游戏或将游戏从后台切到游戏前台时,查询玩家是否成年
                getPlayerExtraInfo();
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

        String uid = UUID.randomUUID().toString();
        Task<String> task = playersClient.submitPlayerEvent(uid, "GAMEBEGIN");
        task.addOnSuccessListener(new OnSuccessListener<String>() {
            @Override
            public void onSuccess(String jsonRequest) {
                i("submitPlayerBeginEvent successfully:" + jsonRequest);
                try {
                    JSONObject data = new JSONObject(jsonRequest);
                    transactionId = data.optString("transactionId");

                    //每15分钟查询玩家附加信息
                    if (timer == null) {
                        timer = new Timer();
                        TimerTask task = new TimerTask() {
                            @Override
                            public void run() {
                                getPlayerExtraInfo();
                            }
                        };
                        timer.schedule(task, 15 * 60 * 1000);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                if (e instanceof ApiException) {
                    int rtnCode = ((ApiException) e).getStatusCode();
                    Log.e(TAG, "submitPlayerBeginEvent failed, code: " + rtnCode);
                    //返回7022时，表示该玩家已经成年或者未实名认证，此时不需要上报进入游戏事件
                    if (rtnCode == 7022) {
                        i("The player is an adult or has not been authenticated by real name");
                        return;
                    }
                    //返回7002且当前网络正常，或者直接返回7006，均表示该帐号未在中国大陆注册，请直接放通，无需防沉迷监控
                    if ((rtnCode == 7002 && NetworkUtil.isNetworkAvailable(activity)) || rtnCode == 7006) {
                        i("Allow the player to enter the game without checking the remaining time");
                    }
                }
            }
        });
    }

    /**
     * 上报玩家退出游戏事件
     */
    private void submitPlayerEndEvent() {
        if (playersClient == null) return;
        if (transactionId == null || transactionId.equals("")) return;

        Task<String> task = playersClient.submitPlayerEvent(transactionId, "GAMEEND");
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

        Task<PlayerExtraInfo> task;
        if (transactionId == null || transactionId.equals("")) {
            task = playersClient.getPlayerExtraInfo(null);
        } else {
            task = playersClient.getPlayerExtraInfo(transactionId);
        }

        task.addOnSuccessListener(new OnSuccessListener<PlayerExtraInfo>() {
            @Override
            public void onSuccess(PlayerExtraInfo extra) {
                if (extra != null) {
                    i("IsRealName: " + extra.getIsRealName() + ", IsAdult: " + extra.getIsAdult()
                            + ", PlayerId: " + extra.getPlayerId() + ", PlayerDuration: " + extra.getPlayerDuration());

                    if (extra.getIsRealName()) {//已经实名
                        if (!extra.getIsAdult()) {//未成年
                            //未成年玩家当天最新的累计游戏时长>=90(1.5小时),
                            if (extra.getPlayerDuration() >= 90) {
                                createTipDialog("您好，当天累计游戏时长已达到，请退出游戏", "好的");
                                return;
                            }

                            //未成年,上报玩家进入游戏事件
                            submitPlayerBeginEvent();
                        }
                    }
                } else {
                    Log.e(TAG, "Player extra info is empty. ");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                if (e instanceof ApiException) {
                    int rtnCode = ((ApiException) e).getStatusCode();
                    Log.e(TAG, "getPlayerExtraInfo failed, code: " + rtnCode);
                    //返回7023时，表示接口调用过于频繁，需要将接口调用间隔修改为15分钟。
                    if (rtnCode == 7023) {
                        i("It is recommended to check every 15 minutes.");
                        return;
                    }
                    //返回7002且网络正常，或者直接返回7006，表示未查询到玩家附加信息，可以直接放通处理。
                    if ((rtnCode == 7002 && NetworkUtil.isNetworkAvailable(activity)) || rtnCode == 7006) {
                        i("No additional user information was found and allow the player to enter the game");
                    }
                }
            }
        });
    }

    /**
     * 游戏试玩
     */
    private void gameTrialProcess() {
        if (playersClient == null) return;
        playersClient.setGameTrialProcess(new GameTrialProcess() {
            @Override
            public void onTrialTimeout() {
                //试玩时间结束
            }

            @Override
            public void onCheckRealNameResult(boolean hasRealName) {
                if (hasRealName) {
                    // 已实名，继续后续的游戏登录处理
                    return;
                }
                //未实名，建议您提示玩家后退出游戏或引导玩家重新登录并实名认证
            }
        });
    }

    private void createTipDialog(String message, String tip) {
        new AlertDialog.Builder(activity).setMessage(message)
                .setCancelable(false)
                .setPositiveButton(tip, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        activity.finish();
                        System.exit(0);
                    }
                })
                .create().show();
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

    private void upgrade(UserExtraData extraData) {
        TDGAProfile profile = TDGAProfile.setProfile(extraData.getRoleID());
        //玩家升级时，做如下调用
        profile.setLevel(Integer.parseInt(extraData.getRoleLevel()));
    }

    public void sendExtraData(UserExtraData data) {
        if (playersClient == null) return;

        AppPlayerInfo appPlayerInfo = new AppPlayerInfo();
        appPlayerInfo.area = data.getServerName();
        appPlayerInfo.rank = data.getRoleLevel();
        appPlayerInfo.role = data.getRoleName();

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

    public void showHuaWeiAd(String posId) {
        // 获取广告，每次点击按钮重新获取，不要缓存广告。

        // 设置已同意用户协议标识，前面已经判断是否同意过用户协议，在这里再次调用一下；手机克隆场景，有可能会丢失保存的同意标识。
        HiAd.getInstance(activity).enableUserInfo(true);
        // 实例化激励广告加载器，并设置广告位ID，可以支持一次传多个广告位ID
        rewardAdLoader = new RewardAdLoader(activity, new String[]{ad_posId});
        rewardAdLoader.setListener(mRewardAdListener);
        rewardAdLoader.loadAds(4, false);// 4:手机
    }

    private boolean checkAdMap(Map adMap) {
        // Activity 销毁，则不展示广告
        if (activity.isDestroyed() || activity.isFinishing()) {
            Log.e(TAG, "checkAdMap failed, activity.isDestoryed:" + activity.isDestroyed() + ", activity.isFinishing:" + activity.isFinishing());
            return false;
        }

        // 广告列表为空
        if (adMap == null && adMap.isEmpty()) {
            Log.e(TAG, "checkAdMap failed, ad.size:" + (adMap == null ? null : 0));
            return false;
        }
        return true;
    }

    private void addRewardAdView(List<IRewardAd> rewardAdList) {
        if (rewardAdList == null || rewardAdList.isEmpty()) {
            Log.e(TAG, "addRewardAdView, rewardAdList is empty");
            U8SDK.getInstance().onResult(U8Code.CODE_ADS_FAILED, "广告加载或播放失败");
            return;
        }

        final IRewardAd rewardAd = rewardAdList.get(0);
        if (rewardAd == null) {
            U8SDK.getInstance().onResult(U8Code.CODE_ADS_FAILED, "广告加载或播放失败");
            return;
        }

        //广告展示前判断广告是否过期或是否失效
        if (rewardAd.isExpired() || !rewardAd.isValid()) {
            U8SDK.getInstance().onResult(U8Code.CODE_ADS_FAILED, "广告加载或播放失败");
            return;
        }

        rewardAd.setMute(true);
        rewardAd.show(activity, mRewardAdStatusListener);

        rewardAdLoader = null;
    }

}
