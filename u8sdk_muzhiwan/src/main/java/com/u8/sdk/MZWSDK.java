package com.u8.sdk;

import android.Manifest;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.muzhiwan.sdk.core.MzwSdkController;
import com.muzhiwan.sdk.core.callback.MzwInitCallback;
import com.muzhiwan.sdk.core.callback.MzwLoignCallback;
import com.muzhiwan.sdk.core.callback.MzwPayCallback;
import com.muzhiwan.sdk.core.callback.MzwPostGiftCodeCallback;
import com.muzhiwan.sdk.service.MzwOrder;
import com.tendcloud.tenddata.TDGAProfile;
import com.tendcloud.tenddata.TDGAVirtualCurrency;
import com.zjtx.prompt.PromptDialog;

import java.util.ArrayList;
import java.util.List;

public class MZWSDK {

    private final String TAG = "MZWSDK";
    private static MZWSDK instance;
    private Activity activity;

    private int orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

    private MZWSDK() {
        activity = U8SDK.getInstance().getContext();
    }

    public static MZWSDK getInstance() {
        if (instance == null) {
            instance = new MZWSDK();
        }
        return instance;
    }

    private void parseSDKParams(SDKParams params) {
        String orn = params.getString("MZW_Orientation");
        if ("landscape".equalsIgnoreCase(orn)) {
            this.orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        } else if ("portrait".equalsIgnoreCase(orn)) {
            this.orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        } else {
            this.orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        }
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

    private void initSDK() {

        if (android.os.Build.VERSION.SDK_INT >= 23) {
            //检查权限，开启必要权限
            checkPermissions(needPermissions);
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                initMZW();
            }
        }, 1500);


        U8SDK.getInstance().setActivityCallback(new ActivityCallbackAdapter() {

            @Override
            public void onDestroy() {
                super.onDestroy();
                MzwSdkController.getInstance().destory();
            }

            @Override
            public void onRequestPermissionResult(int requestCode, String[] permissions, int[] paramArrayOfInt) {
                // TODO Auto-generated method stub
                if (requestCode == PERMISSON_REQUESTCODE) {
                    if (!verifyPermissions(paramArrayOfInt)) {
                        Toast.makeText(activity, "请先允许权限,否则会影响程序正常使用", Toast.LENGTH_SHORT).show();
                    }
                }
            }

        });
    }

    private void initMZW() {
        int orn = MzwSdkController.ORIENTATION_VERTICAL;
        if (this.orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            orn = MzwSdkController.ORIENTATION_HORIZONTAL;
        }

        MzwSdkController.getInstance().init(activity, orn, new MzwInitCallback() {
            @Override
            public void onResult(int code, String msg) {
                Log.d(TAG, "muzhiwan init result:" + code + ";msg:" + msg);
                if (code == 1) {
                    U8SDK.getInstance().onResult(U8Code.CODE_INIT_SUCCESS, "init success");
                } else {
                    U8SDK.getInstance().onResult(U8Code.CODE_INIT_FAIL, "init failed");
                }
            }
        });
    }

    public void login() {

        MzwSdkController.getInstance().doLogin(new MzwLoignCallback() {

            @Override
            public void onResult(int code, String msg) {
                Log.d(TAG, "muzhiwan login callback:" + code + ";msg:" + msg);
                if (code == 1) {
                    String token = msg;
                    U8SDK.getInstance().onResult(U8Code.CODE_LOGIN_SUCCESS, token);
                    U8SDK.getInstance().onLoginResult(token);
                } else if (code == 6) {
                    U8SDK.getInstance().onResult(U8Code.CODE_LOGOUT_SUCCESS, "logout success");
                } else {
                    U8SDK.getInstance().onResult(U8Code.CODE_LOGIN_FAIL, "login failed.code:" + code);
                }
            }
        });

    }

    public void logout() {
        MzwSdkController.getInstance().doLogout();
    }

    public void postGiftCode(String code) {
        MzwSdkController.getInstance().doPostGiftCode(code, new MzwPostGiftCodeCallback() {

            @Override
            public void onResult(int code, String msg) {
                if (code == 1) {
                    U8SDK.getInstance().onResult(U8Code.CODE_POST_GIFT_SUC, "post gift code success");
                } else {
                    U8SDK.getInstance().onResult(U8Code.CODE_POST_GIFT_FAILED, msg);
                }
            }
        });
    }

    public void sendExtraData(UserExtraData extraData) {
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

    public void pay(final PayParams params) {
        Log.e(TAG, "params=" + params.toString());

        MzwOrder order = new MzwOrder();
        order.setProductname(params.getProductName());
        order.setProductdesc(params.getProductDesc());
        order.setProductid(params.getProductId());
        order.setMoney(params.getPrice());
        order.setExtern(params.getOrderID());

        TDGAVirtualCurrency.onChargeRequest(params.getOrderID(), params.getProductName(), params.getPrice(), "CNY", params.getPrice() * 7, "muzhiwan SDK");

        MzwSdkController.getInstance().doPay(order, new MzwPayCallback() {

            @Override
            public void onResult(int code, MzwOrder o) {
                Log.e(TAG, "code=" + code);
                if (code == 0) {
                    U8SDK.getInstance().onResult(U8Code.CODE_PAY_FAIL, "pay failed.code:" + code);
                } else if (code == 1) {
                    TDGAVirtualCurrency.onChargeSuccess(params.getOrderID());
                    U8SDK.getInstance().onResult(U8Code.CODE_PAY_SUCCESS, "pay success");
                } else {
                    U8SDK.getInstance().onResult(U8Code.CODE_PAY_FAIL, "pay failed");
                }
            }

        });

    }


    private static final int PERMISSON_REQUESTCODE = 0;

    protected String[] needPermissions = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,//存储卡写入权限
            Manifest.permission.READ_EXTERNAL_STORAGE,//存储卡读取权限
            Manifest.permission.READ_PHONE_STATE,//读取手机状态权限
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS
    };

    /**
     * 检查权限
     */
    private void checkPermissions(String... permissions) {
        List<String> needRequestPermissionList = findDeniedPermissions(permissions);
        if (null != needRequestPermissionList && needRequestPermissionList.size() > 0) {
            ActivityCompat.requestPermissions(activity, needRequestPermissionList.toArray(new String[needRequestPermissionList.size()]), PERMISSON_REQUESTCODE);
        }
    }

    /**
     * 获取权限集中需要申请权限的列表
     *
     * @param permissions
     * @return
     * @since 2.5.0
     */
    private List<String> findDeniedPermissions(String[] permissions) {
        List<String> needRequestPermissionList = new ArrayList<String>();
        for (String perm : permissions) {
            if (ContextCompat.checkSelfPermission(activity, perm) != PackageManager.PERMISSION_GRANTED || ActivityCompat.shouldShowRequestPermissionRationale(activity, perm)) {
                needRequestPermissionList.add(perm);
            }
        }
        return needRequestPermissionList;
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
}
