package com.u8.sdk;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.smwl.smsdk.abstrat.SMInitListener;
import com.smwl.smsdk.abstrat.SMLoginListener;
import com.smwl.smsdk.abstrat.SMLoginOutListener;
import com.smwl.smsdk.app.SMPlatformManager;
import com.smwl.smsdk.bean.PayInfo;
import com.smwl.smsdk.bean.SMUserInfo;
import com.tendcloud.tenddata.TDGAProfile;
import com.u8.common.PromptDialog;

public class X7SDK {
    private String TAG = "xiao7sdk";
    private Activity activity;
    private boolean isLogin = false;

    private static X7SDK instance;

    private String appkey; // 游戏的ID号，游戏第一次注册时由小7平台提供

    private X7SDK() {
        activity = U8SDK.getInstance().getContext();
    }

    public static X7SDK getInstance() {
        if (instance == null) {
            instance = new X7SDK();
        }
        return instance;
    }

    public void initSDK(SDKParams params) {
        appkey = params.getString("appkey");
        Log.e(TAG, "x7 appkey......" + appkey);

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
            if (ContextCompat.checkSelfPermission(U8SDK.getInstance().getContext(),
                    Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(U8SDK.getInstance().getContext(), needPermissions, 123);
            }
        }

        initX7();
    }

    private void initX7() {
        Log.e(TAG, "x7 init......");

        SMPlatformManager.getInstance().init(activity, appkey, new SMInitListener() {

            @Override
            public void onSuccess() {
                // TODO Auto-generated method stub
                Log.e(TAG, "游戏初始化成功");
                U8SDK.getInstance().onResult(U8Code.CODE_INIT_SUCCESS, "init success");
            }

            @Override
            public void onFail(String reason) {
                // TODO Auto-generated method stub
                Log.e(TAG, "游戏初始化失败：" + reason);
                U8SDK.getInstance().onResult(U8Code.CODE_INIT_FAIL, "init fail");
            }
        });
    }

    public void login() {
        Log.e(TAG, "x7 login......");

        SMPlatformManager.getInstance().login(activity, new SMLoginListener() {

            @Override
            public void onLoginSuccess(SMUserInfo loginInfo) {
                // TODO 登录成功
                isLogin = true;
                Log.e(TAG, "游戏登录成功");
                // 客户端登录成功后，会通过如下方式得到token值，游戏客户端传token给自己服务器
                // 服务器做登录验证，登陆成功后，会返回给游戏guid，每一个小7通行证可以拥有至多10个子账号（guid），guid对应了游戏中的游戏账号

                String tokenkey = loginInfo.getTokenkey();
                Log.e(TAG, "tokenkey......" + tokenkey);

                U8SDK.getInstance().onLoginResult(tokenkey);
                U8SDK.getInstance().onResult(U8Code.CODE_LOGIN_SUCCESS, "login success");
            }

            @Override
            public void onLoginFailed(String reason) {
                // TODO 登录失败
                Log.e(TAG, "游戏登录失败:" + reason);
                U8SDK.getInstance().onResult(U8Code.CODE_LOGIN_FAIL, reason);
            }

            @Override
            public void onLoginCancell(String reason) {
                // TODO 登录取消
                U8SDK.getInstance().onResult(U8Code.CODE_LOGIN_FAIL, "login cancel");
            }

            @Override
            public void onLogoutSuccess() {
                // TODO 登出成功	登出回调调用两次，导致游戏点击悬浮球切换账号不正常
                if (isLogin) {
                    Log.e(TAG, "游戏登出成功");
                    U8SDK.getInstance().onResult(U8Code.CODE_LOGOUT_SUCCESS, "logout success");
                    U8SDK.getInstance().onLogout();
                    isLogin = false;
                } else {
                    Log.e(TAG, "游戏未登陆");
                }
            }

        });

    }

    public void logout() {
        Log.e(TAG, "x7 logout......");
        SMPlatformManager.getInstance().loginOut();
    }

    public void exit() {
        Log.e(TAG, "x7 exit......");
        SMPlatformManager.getInstance().exitApp(new SMLoginOutListener() {

            @Override
            public void loginOutSuccess() {
                // TODO 退出成功
                isLogin = false;

                Log.e(TAG, "游戏退出成功");

                U8SDK.getInstance().onLogout();
                U8SDK.getInstance().onResult(U8Code.CODE_LOGOUT_SUCCESS, "exit success");

                activity.finish();
                System.exit(0);
            }

            @Override
            public void loginOutFail(String msg) {
                // TODO 退出失败
                Log.e(TAG, "游戏退出失败：" + (String) msg);
                U8SDK.getInstance().onResult(U8Code.CODE_LOGOUT_FAIL, (String) msg);
            }

            @Override
            public void loginOutCancel() {
                // TODO 退出取消
                Log.e(TAG, "游戏退出取消");
                U8SDK.getInstance().onResult(U8Code.CODE_LOGOUT_FAIL, "exit cancel");
            }
        });
    }

    public void submitGameData(UserExtraData extraData) {
        Log.e(TAG, "x7 sumbit......");
        if (extraData == null)
            return;

        Log.e(TAG, "x7...." + extraData.getDataType());

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

    public void pay(PayParams data) {
        new AlertDialog.Builder(activity)
                .setTitle("温馨提示")
                .setMessage("此渠道已关闭充值功能")
                .setNegativeButton("好的", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();

//        Log.e(TAG, "x7 pay......");
//        if (data == null)
//            return;
//        // 服务器有对应数据上报（talkingdata）
//        PayInfo mPayInfo = getPayInfo(data);
//        SMPlatformManager.getInstance().Pay(activity, mPayInfo, new SMPayListener() {
//
//            @Override
//            public void onPaySuccess(Object msg) {
//                // TODO 支付成功
//                Log.e(TAG, "游戏支付成功");
//                U8SDK.getInstance().onResult(U8Code.CODE_PAY_SUCCESS, (String) msg);
//            }
//
//            @Override
//            public void onPayFailed(Object msg) {
//                // TODO 支付失败
//                Log.e(TAG, "游戏支付失败");
//                U8SDK.getInstance().onResult(U8Code.CODE_PAY_FAIL, (String) msg);
//            }
//
//            @Override
//            public void onPayCancell(Object msg) {
//                // TODO 支付取消
//                Log.e(TAG, "游戏支付取消");
//                U8SDK.getInstance().onResult(U8Code.CODE_PAY_CANCEL, (String) msg);
//            }
//        });
    }

    public PayInfo getPayInfo(PayParams data) {
        String orderId = data.getOrderID();
        String extends_info = data.getExtension();
        String roleId = data.getRoleId();
        String roleName = data.getRoleName();
        String serviceId = data.getServerId();// 与服务器不一样
        String serviceName = data.getServerName();// 与服务器不一样
        String price = data.getPrice() + ".00";// 商品价格(以人民币为单位，4.0后需保留小数点后两位)
        String subject = data.getProductName();// 与服务器不一样
        String notifyId = "-1";// 支付回调ID,如果只有一个支付回调地址可以设置成-1，但 不允许设置为0

        Log.e(TAG, "orderId=" + orderId);
        Log.e(TAG, "extends_info=" + extends_info);
        Log.e(TAG, "roleId=" + roleId);
        Log.e(TAG, "roleName=" + roleName);
        Log.e(TAG, "serviceId=" + serviceId);
        Log.e(TAG, "serviceName=" + serviceName);
        Log.e(TAG, "price=" + price);
        Log.e(TAG, "subject=" + subject);


        /**
         * "game_guid=12155534 &game_level=1711 &game_area=bow2_cn_001(Arena)
         * &game_sign=08e421aad755e01074eed651749fbe8f &notify_id=-1"
         */
        try {
            String[] map = extends_info.split("&");
            PayInfo payInfo = new PayInfo();
            payInfo.setExtends_info_data(orderId);
            payInfo.setGame_orderid(orderId);
            payInfo.setGame_guid(map[0].split("=")[1]);
            payInfo.setGame_level(map[1].split("=")[1]);
            payInfo.setGame_role_id(roleId);
            payInfo.setGame_role_name(roleName);
            payInfo.setGame_area(map[2].split("=")[1]);
            payInfo.setGame_price(price);
            payInfo.setSubject("金块");
            payInfo.setNotify_id(notifyId);
            payInfo.setGame_sign(map[3].split("=")[1]);
            return payInfo;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
