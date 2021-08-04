package com.u8.sdk;

import android.app.Activity;
import android.util.Log;

import com.downjoy.CallbackListener;
import com.downjoy.CallbackStatus;
import com.downjoy.Downjoy;
import com.downjoy.InitListener;
import com.downjoy.LoginInfo;
import com.downjoy.LogoutListener;
import com.downjoy.ResultListener;
import com.tendcloud.tenddata.TDGAProfile;
import com.tendcloud.tenddata.TDGAVirtualCurrency;
import com.u8.common.PromptDialog;

import org.json.JSONException;
import org.json.JSONObject;

public class DownjoySDK {
    private Activity activity;
    private static DownjoySDK instance;

    private String appID;
    private String appKey;
    private String merchantId;
    private String serverSeqNum;

    private String tag = "DownJoysdk";

    private Downjoy downjoy;

    private DownjoySDK() {
        activity = U8SDK.getInstance().getContext();
    }

    public static DownjoySDK getInstance() {
        if (instance == null) {
            instance = new DownjoySDK();
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
        this.appID = params.getString("APP_ID");
        this.appKey = params.getString("APP_KEY");
        this.merchantId = params.getString("MERCHANT_ID");
        this.serverSeqNum = params.getString("SERVER_SEQ_NUM");

        Log.i(tag, "appID:" + appID);
        Log.i(tag, "appKey:" + appKey);
        Log.i(tag, "merchantId:" + merchantId);
        Log.i(tag, "serverSeqNum:" + serverSeqNum);
    }

    private void initSDK() {
        Downjoy.initDownjoy(activity, merchantId, appID, serverSeqNum, appKey,
                new InitListener() {

                    @Override
                    public void onInitComplete() {
                        // TODO Auto-generated method stub
                        U8SDK.getInstance().onResult(U8Code.CODE_INIT_SUCCESS, "init success");

                        downjoy = Downjoy.getInstance();

                        // 设置登录成功后是否显示当乐的悬浮按钮
                        downjoy.showDownjoyIconAfterLogined(true);
                        // 设置悬浮窗显 示位置，在服务器设置了悬浮窗位置时，此设置失效
                        downjoy.setInitLocation(Downjoy.LOCATION_LEFT_CENTER_VERTICAL);

                        // 设置全局注销监听器，浮标中的注销也能接收到回调
                        downjoy.setLogoutListener(new LogoutListener() {
                            @Override
                            public void onLogoutSuccess() {
                                U8SDK.getInstance().onResult(U8Code.CODE_LOGOUT_SUCCESS, "logout success");
                                logout();
                            }

                            @Override
                            public void onLogoutError(String msg) {
                                U8SDK.getInstance().onResult(U8Code.CODE_LOGOUT_FAIL, "logout fail msg : " + msg);
                            }
                        });
                    }
                });

        U8SDK.getInstance().setActivityCallback(new ActivityCallbackAdapter() {
            @Override
            public void onPause() {
                super.onPause();
                if (downjoy != null) {
                    downjoy.pause();
                }
            }

            @Override
            public void onResume() {
                super.onResume();
                if (downjoy != null) {
                    downjoy.resume(activity);
                }
            }

            @Override
            public void onDestroy() {
                super.onDestroy();
                if (downjoy != null) {
                    downjoy.destroy();
                    downjoy = null;
                }
            }
        });

    }

    public void login() {
        // TODO::这里调用AAA的登录方法
        if (downjoy == null) return;
        downjoy.openLoginDialog(activity, new CallbackListener<LoginInfo>() {

            @Override
            public void callback(int status, LoginInfo data) {
                // TODO Auto-generated method stub
                if (status == CallbackStatus.SUCCESS && data != null) {
                    // 当乐提供的 openid ，用户唯一标识
                    String umid = data.getUmid();
                    String username = data.getUserName();
                    String nickname = data.getNickName();

                    // 本次登录生成的 token
                    // 必接，必须校验 , 具体看 服务器端文档
                    String token = data.getToken();

                    String loginResult = encodeLoginResult(umid, token);

                    U8SDK.getInstance().onResult(U8Code.CODE_LOGIN_SUCCESS, "login success");

                    U8SDK.getInstance().onLoginResult(loginResult);

                } else {

                    U8SDK.getInstance().onResult(U8Code.CODE_LOGIN_FAIL, "login fail");

                }
            }
        });
    }

    private String encodeLoginResult(String uid, String token) {
        JSONObject json = new JSONObject();
        try {
            json.put("mid", uid);
            json.put("token", token);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return json.toString();
    }

    public void switchLogin() {
        // TODO::这里调用AAA切换帐号的方法
        // 如果没有提供切换帐号的方法，那么切换帐号的逻辑就是[先登出，再登录]，也就是先调用logout，再调用login
        logout();
        login();
    }

    public void logout() {
        // TODO::调用AAA的登出方法
        if (downjoy == null) return;
        downjoy.logout(activity);
    }

    public void showUserCenter() {
        // TODO::调用AAA显示个人中心的方法
        // 如果AAA没有提供对应的接口，则不用实现该方法
        if (downjoy == null) return;
        downjoy.openMemberCenterDialog(activity);
    }

    public void exit() {
        // TODO::调用AAA显示退出确认框接口
        // 如果AAA没有提供对应的接口，则不用实现该方法
        if (downjoy == null) return;
        downjoy.openExitDialog(activity, new CallbackListener<String>() {
            @Override
            public void callback(int status, String data) {
                // TODO Auto-generated method stub
                if (CallbackStatus.SUCCESS == status) {
                    // 收到回调后， CP 自己实现游戏的退出， SDK 只会回收 SDK 的资源
                    activity.finish();
                    System.exit(0);
                } else if (CallbackStatus.CANCEL == status) {
                    // Util. alert (getBaseContext(), "退出回调-> " + data);
                }
            }
        });
    }

    public void submitGameData(UserExtraData extraData) {
        // TODO::调用AAA上报玩家数据接口
        // 如果AAA没有提供对应的接口，则不用实现该方法
        switch (extraData.getDataType()) {
            case UserExtraData.TYPE_ENTER_GAME:
                djSubmitData(extraData, 1);
                enterGame(extraData);
                break;
            case UserExtraData.TYPE_CREATE_ROLE:
                djSubmitData(extraData, 2);
                break;
            case UserExtraData.TYPE_LEVEL_UP:
                djSubmitData(extraData, 3);
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

    // //进入游戏
    private void djSubmitData(UserExtraData extraData, int eventType) {
        // 4.3.5 新增，登录成功后，提交游戏数据（也可选择从服务器提交，具体参见服务器端接入文档）
        // 所有参数尽量不要填空值
        // 关于什么时候上报游戏数据：确保每次登录上传一次数据即可，不一 定要在登录后马上上传！
        String zoneId = extraData.getServerID() + ""; // 玩家区服 id 没有传字符串 1
        String zoneName = extraData.getServerName(); // 玩家区服名称，没有传字符串 001
        String roleId = extraData.getRoleID(); // 玩家角色 Id 没有传字符串 1
        String roleName = extraData.getRoleName(); // 玩家角色名称 没有传字符串 001
        long roleCTime = extraData.getRoleCreateTime(); // 角色创建时间戳。获取不了创建的时间戳，就传 DEMO中的这个值。
        long roleLevelMTime = extraData.getRoleLevelUpTime(); // 角色等级变化时间戳。获取不了等级变化时间，就跟角色创建时间传一样的值。
        String roleLevel = extraData.getRoleLevel(); // 角色等级，如果没有这个值，传字符串 1

        Log.e(tag,
                "zoneId == " + zoneId + " || zoneName == " + zoneName + " || roleId == " + roleId + " || roleName == "
                        + roleName + " || roleCTime == " + roleCTime + " || roleLevelMTime == " + roleLevelMTime
                        + " || roleLevel == " + roleLevel);

        if (downjoy == null) return;
        downjoy.submitGameRoleData(zoneId, zoneName, roleId, roleName, roleCTime, roleLevelMTime, roleLevel, eventType,
                new ResultListener() {

                    @Override
                    public void onResult(Object result) {
                        // TODO Auto-generated method stub
                        // 上传角色结果
                        String resultStr = (String) result;
                        if (resultStr.equals("true")) {
                            // 提交角色成功
                            // Log.e(tag, " submitGameRoleData 提交角色成功");
                        }
                    }
                });
    }

    public void pay(PayParams params) {
        // TODO::调用AAA充值接口
        // 所有支付参数不要为空值以及 ""
        final float money = params.getPrice(); // 商品价格
        final String productId = params.getProductId(); // 商品名称
        final String productName = params.getProductName(); // 商品名称
        final String body = params.getProductDesc(); // 商品描述
        final String transNo = params.getOrderID(); // cp 订单号，计费结果通知时原样返回，尽 量不要使用除字母和数字之外的特殊字符。
        final String ext = params.getExtension(); // cp 透传字段，非必须，不大于 100 个字符，不能有特殊字符
        String zoneId = params.getServerId(); // 玩家区服 id 没有传字符串 1
        String zoneName = params.getServerName(); // 玩家区服名称，没有传字符串 001
        String roleId = params.getRoleId(); // 玩家 角色 Id 没有传字符串 1
        String roleName = params.getRoleName(); // 玩家角色名称 没有传字符串 001

        JSONObject exJson;
        String cpSign = null;
        try {
            exJson = new JSONObject(ext);
            cpSign = exJson.getString("cpSign");
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        TDGAVirtualCurrency.onChargeRequest(transNo, productName, money, "CNY", money * 7, "dangle SDK");

        // 这里的""是保留字段，传空字符
        downjoy.openPaymentDialog(activity, money, productId, productName, body, transNo, "",
                zoneId, zoneName, roleId, roleName, cpSign, new CallbackListener<String>() {

                    @Override
                    public void callback(int status, String data) {
                        // TODO Auto-generated method stub
                        if (status == CallbackStatus.SUCCESS) {
                            TDGAVirtualCurrency.onChargeSuccess(transNo);

                            Log.e(tag, "成功支付回调->订单号：" + data);
                            U8SDK.getInstance().onResult(U8Code.CODE_PAY_SUCCESS, "pay success" + data);
                        } else if (status == CallbackStatus.FAIL) {
                            Log.e(tag, "失败支付回调->error:" + data);
                            U8SDK.getInstance().onResult(U8Code.CODE_PAY_FAIL, "pay failed" + data);

                        } else if (status == CallbackStatus.CANCEL) {
                            Log.e(tag, "取消支付回调->" + data);
                            U8SDK.getInstance().onResult(U8Code.CODE_PAY_CANCEL, "pay cancel" + data);

                        }
                    }
                });

    }
}
