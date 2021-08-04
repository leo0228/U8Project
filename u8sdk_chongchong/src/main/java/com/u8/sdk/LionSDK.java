package com.u8.sdk;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.util.Xml;

import com.lion.ccpay.bean.PlayUserInfo;
import com.lion.ccpay.sdk.CCPaySdk;
import com.lion.ccsdk.OnPermissionsListener;
import com.lion.ccsdk.SdkExitAppListener;
import com.lion.ccsdk.SdkLoginListener;
import com.lion.ccsdk.SdkLogoutListener;
import com.lion.ccsdk.SdkPayListener;
import com.lion.ccsdk.SdkUser;
import com.tendcloud.tenddata.TDGAProfile;
import com.tendcloud.tenddata.TDGAVirtualCurrency;
import com.u8.sdk.lion.GoodInfo;
import com.u8.common.PromptDialog;

import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

public class LionSDK {
    private Activity activity;
    private String TAG = "lionsdk";

    private static LionSDK instance;
    private Map<String, GoodInfo> goods;

    public static LionSDK getInstance() {
        if (instance == null) {
            instance = new LionSDK();
        }
        return instance;
    }

    private LionSDK() {
        activity = U8SDK.getInstance().getContext();
        this.goods = new HashMap<String, GoodInfo>();
    }

    public void initSDK(SDKParams params) {
        this.loadPayGoods();

        PromptDialog.init(activity, new PromptDialog.OnPromptListener() {
            @Override
            public void onInitSDK() {
                initSDK();
            }
        });
    }

    protected String[] needPermissions = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.CAMERA,
    };

    private void initSDK() {
        CCPaySdk.getInstance().init(activity);
        U8SDK.getInstance().onResult(U8Code.CODE_INIT_SUCCESS, "init success");
        CCPaySdk.getInstance().onCreate(activity);

        U8SDK.getInstance().setActivityCallback(new ActivityCallbackAdapter() {

            @Override
            public void onDestroy() {
                super.onDestroy();
                CCPaySdk.getInstance().onDestroy(activity);
            }

            @Override
            public void onPause() {
                super.onPause();
                CCPaySdk.getInstance().dismissFloating(activity);
                CCPaySdk.getInstance().onPause(activity);
            }

            @Override
            public void onStop() {
                super.onStop();
                CCPaySdk.getInstance().onStop(activity);
            }

            @Override
            public void onRestart() {
                super.onRestart();
                CCPaySdk.getInstance().onRestart(activity);
            }

            @Override
            public void onResume() {
                super.onResume();
                CCPaySdk.getInstance().showFloating(activity);
                CCPaySdk.getInstance().onResume(activity);
            }

            @Override
            public void onActivityResult(int requestCode, int resultCode, Intent data) {
                super.onActivityResult(requestCode, resultCode, data);
                CCPaySdk.getInstance().onActivityResult(activity, requestCode, resultCode,
                        data);
            }

            @Override
            public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) {
                super.onRequestPermissionResult(requestCode, permissions, grantResults);
                CCPaySdk.getInstance().onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        });
        // 登出回调方法
        CCPaySdk.getInstance().setOnLoginOutListener(new SdkLogoutListener() {

            @Override
            public void onLoginOut() {
                U8SDK.getInstance().onLogout();
                U8SDK.getInstance().onResult(U8Code.CODE_LOGOUT_SUCCESS, "logout success");
            }
        });

        CCPaySdk.getInstance().requestPermission(needPermissions, 2000, new OnPermissionsListener() {
            @Override
            public void onSuccess(int i) {
                //申请权限成功调用
            }

            @Override
            public void onFail(int i) {
                //申请权限失败调用
            }

            @Override
            public void onCancel() {
                //申请权限取消调用
            }

            @Override
            public boolean onShowTipDialog() {
                //是否显示提示对话框
                return false;
            }

            @Override
            public String getPermissionTip() {
                //自定义显示提示对话框内容
                return null;
            }
        });

    }


    public void login(boolean autoLogin) {
        CCPaySdk.getInstance().login(activity, autoLogin, new SdkLoginListener() {

            @Override
            public void onLoginSuccess(SdkUser user) {
                String uid = user.uid;
                String token = user.token;
                String userName = user.userName;

                String loginResult = encodeLoginResult(uid, token, userName);
                U8SDK.getInstance().onResult(U8Code.CODE_LOGIN_SUCCESS, uid);
                U8SDK.getInstance().onLoginResult(loginResult);
                CCPaySdk.getInstance().showFloating(activity);
            }

            @Override
            public void onLoginFail(String message) {
                U8SDK.getInstance().onResult(U8Code.CODE_LOGIN_FAIL, "login failed");
            }

            @Override
            public void onLoginCancel() {
                U8SDK.getInstance().onResult(U8Code.CODE_LOGIN_FAIL, "login cancel");
            }

        });

    }

    public void logout() {
        CCPaySdk.getInstance().logout(activity);
    }

    private PlayUserInfo userInfo;

    public void submitExtendData(UserExtraData extraData) {
        switch (extraData.getDataType()) {
            case UserExtraData.TYPE_SELECT_SERVER:
                CCPaySdk.getInstance().submitExtraData(getPlayUserInfo(extraData, PlayUserInfo.TYPE_SELECT_SERVER));
                break;
            case UserExtraData.TYPE_CREATE_ROLE:
                CCPaySdk.getInstance().submitExtraData(getPlayUserInfo(extraData, PlayUserInfo.TYPE_CREATE_ROLE));
                break;
            case UserExtraData.TYPE_ENTER_GAME:
                enterGame(extraData);
                userInfo = getPlayUserInfo(extraData, PlayUserInfo.TYPE_ENTER_GAME);
                CCPaySdk.getInstance().submitExtraData(userInfo);
                break;
            case UserExtraData.TYPE_LEVEL_UP:
                upgrade(extraData);
                CCPaySdk.getInstance().submitExtraData(getPlayUserInfo(extraData, PlayUserInfo.TYPE_LEVEL_UP));
                break;
            case UserExtraData.TYPE_EXIT_GAME:
                CCPaySdk.getInstance().submitExtraData(getPlayUserInfo(extraData, PlayUserInfo.TYPE_EXIT_GAME));
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

    public void exit() {
        CCPaySdk.getInstance().exitApp(activity, true, new SdkExitAppListener() {
            @Override
            public void onExitApp() {
                CCPaySdk.getInstance().killApp(activity);
                // 处理游戏APP退出业务
                activity.finish();
                System.exit(0);
            }
        });
    }

    public PlayUserInfo getPlayUserInfo(UserExtraData extraData, int type) {
        PlayUserInfo playUserInfo = new PlayUserInfo();
        playUserInfo.setDataType(type);

        playUserInfo.setServerID(extraData.getServerID());
        playUserInfo.setServerName(extraData.getServerName());

        if (PlayUserInfo.TYPE_SELECT_SERVER != type) {
            playUserInfo.setMoneyNum(extraData.getMoneyNum());
            playUserInfo.setRoleCreateTime(extraData.getRoleCreateTime());
            playUserInfo.setRoleGender(1);
            playUserInfo.setRoleID(extraData.getRoleID());
            playUserInfo.setRoleLevel(Integer.parseInt(extraData.getRoleLevel()));
            playUserInfo.setRoleLevelUpTime(extraData.getRoleLevelUpTime());
            playUserInfo.setRoleName(extraData.getRoleName());
            playUserInfo.setVip("1");
        }
        return playUserInfo;
    }

    private String encodeLoginResult(String sid, String token, String userName) {

        JSONObject json = new JSONObject();
        try {
            json.put("uid", sid);
            json.put("token", token);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return json.toString();
    }

    public void pay(final PayParams params) {
        if (userInfo == null) {
            return;
        }
        Log.e(TAG, "pay is start");

        GoodInfo good = getGoodInfo(params.getProductId());
        String waresid = "0";
        if (good != null) {
            waresid = good.getWaresid();
        }

        TDGAVirtualCurrency.onChargeRequest(params.getOrderID(), params.getProductName(), params.getPrice(), "CNY", params.getPrice() * 7, "cc SDK");

        CCPaySdk.getInstance().pay4OLGame(activity, params.getOrderID(), waresid,
                params.getProductName(), "", params.getExtension(), userInfo, new SdkPayListener() {
                    @Override
                    public void onPayResult(int status, String tn, String money) {
                        Log.e(TAG, "pay is status = " + status + " || tn = " + tn + " || money = " + money);
                        switch (status) {
                            case SdkPayListener.CODE_SUCCESS:// 支付成功
                                TDGAVirtualCurrency.onChargeSuccess(params.getOrderID());

                                U8SDK.getInstance().onResult(U8Code.CODE_PAY_SUCCESS, "pay success");
                                break;
                            case SdkPayListener.CODE_FAIL:// 支付失败
                                U8SDK.getInstance().onResult(U8Code.CODE_PAY_FAIL, "pay failed");
                                break;
                            case SdkPayListener.CODE_CANCEL:// 支付取消
                                U8SDK.getInstance().onResult(U8Code.CODE_PAY_CANCEL, "pay cancel");
                                break;
                            case SdkPayListener.CODE_UNKNOWN:// 支付结果未知
                                U8SDK.getInstance().onResult(U8Code.CODE_PAY_UNKNOWN, "pay waiting");
                                break;
                            default:
                                U8SDK.getInstance().onResult(U8Code.CODE_PAY_FAIL, "pay failed");
                                break;
                        }
                    }
                });

    }

    private GoodInfo getGoodInfo(String productID) {
        if (this.goods.containsKey(productID)) {
            return this.goods.get(productID);
        }
        return null;
    }

    private void loadPayGoods() {
        String xmlGoods = SDKTools.getAssetConfigs(activity, "lion_pay.xml");

        if (xmlGoods == null) {
            Log.e("U8SDK", "fail to load lion_pay.xml");
            return;
        }

        XmlPullParser parser = Xml.newPullParser();

        try {
            parser.setInput(new StringReader(xmlGoods));

            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {

                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        String tag = parser.getName();
                        if ("good".equals(tag)) {
                            String productId = parser.getAttributeValue(0);
                            String wareid = parser.getAttributeValue(1);
                            boolean openPrice = Boolean.parseBoolean(parser.getAttributeValue(2));

                            GoodInfo good = new GoodInfo(productId, wareid, openPrice);

                            if (!this.goods.containsKey(productId)) {
                                this.goods.put(productId, good);
                            } else {
                                Log.e("U8SDK", "Curr Good: " + productId + " has duplicated.");
                            }

                            Log.d("U8SDK", "Curr Good: " + productId + "; waresid:" + wareid + ";openPrice:" + openPrice);
                        }
                }
                eventType = parser.next();
            }

        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
