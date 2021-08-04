package com.u8.sdk;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;
import android.widget.Toast;

import com.coolcloud.uac.android.api.Coolcloud;
import com.coolcloud.uac.android.api.ErrInfo;
import com.coolcloud.uac.android.api.OnResultListener;
import com.coolcloud.uac.android.api.provider.LocalComplexProvider;
import com.coolcloud.uac.android.api.view.RnaDialogActivity;
import com.coolcloud.uac.android.common.Constants;
import com.coolcloud.uac.android.common.Params;
import com.coolcloud.uac.android.common.provider.TKTEntity;
import com.coolcloud.uac.android.common.util.KVUtils;
import com.coolcloud.uac.android.gameassistplug.GameAssistApi;
import com.coolcloud.uac.android.gameassistplug.GameAssistConfig;
import com.tendcloud.tenddata.TDGAProfile;
import com.tendcloud.tenddata.TDGAVirtualCurrency;
import com.u8.sdk.kupai.GoodInfo;
import com.u8.sdk.verify.UToken;
import com.yulong.android.paysdk.api.CoolpayApi;
import com.yulong.android.paysdk.base.IPayResult;
import com.yulong.android.paysdk.base.common.CoolPayResult;
import com.yulong.android.paysdk.base.common.CoolYunAccessInfo;
import com.yulong.android.paysdk.base.common.PayInfo;
import com.u8.common.PromptDialog;

import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CoolPadSDK {
    private final static String TAG = "CoolPadSDK";
    private static CoolPadSDK instance;
    private Activity activity;
    private GameAssistApi mGameAssistApi;
    private GameAssistConfig mGameAssistConfig;

    private Coolcloud mCoolcloud = null;
    private CoolpayApi payApi = null;
    private String appID;
    private String payKey;
    private int orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
    private Map<String, GoodInfo> goods;

    private static final int REQUEST_CODE = 1;// 悬浮权限请求码
    private static final int RNA_REQUEST = 2;// 实名认证结果请求码
    private static final int REQUEST_CODE_ASK_PERMISSIONS = 123;

    SharedPreferences sp;
    Editor editor;

    private CoolPadSDK() {
        activity = U8SDK.getInstance().getContext();
        goods = new HashMap<String, GoodInfo>();
    }

    public static CoolPadSDK getInstance() {
        if (instance == null) {
            instance = new CoolPadSDK();
        }
        return instance;
    }

    public void initSDK(SDKParams params) {
        this.loadKuPaiPayGoods();
        parseSDKParams(params);

        PromptDialog.init(activity, new PromptDialog.OnPromptListener() {
            @Override
            public void onInitSDK() {
                initSDK();
            }
        });
    }

    private void parseSDKParams(SDKParams params) {
        this.appID = params.getString("CoolPad_AppID");
        this.payKey = params.getString("CoolPad_PayKey");
        String orn = params.getString("CoolPad_Orientation");
        if ("landscape".equalsIgnoreCase(orn)) {
            this.orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        } else if ("portrait".equalsIgnoreCase(orn)) {
            this.orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        } else {
            this.orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        }

        Log.e(TAG, appID);
        Log.e(TAG, payKey);
        Log.e(TAG, orn);
    }

    private void initSDK() {
        try {

            sp = activity.getSharedPreferences("usercenter", 0);
            editor = sp.edit();

            mCoolcloud = Coolcloud.get(activity, this.appID);
            payApi = CoolpayApi.createCoolpayApi(activity, this.appID);

            // 版本判断。当手机系统大于 23 时，才有必要去判断权限是否获取
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                checkPermissions(needPermissions);

                showFloat();
            } else {
                initGameAssist();
            }

            U8SDK.getInstance().setActivityCallback(new ActivityCallbackAdapter() {
                @Override
                public void onPause() {
                    if (mGameAssistApi != null) {
                        mGameAssistApi.onPause();
                    }
                }

                @Override
                public void onResume() {
                    if (mGameAssistApi != null) {
                        mGameAssistApi.onResume();
                    }
                }

                // @Override
                // public void onConfigurationChanged(Configuration newConfig) {
                // if (mGameAssistApi != null) {
                // mGameAssistApi.onConfigurationChanged();
                // }
                // super.onConfigurationChanged(newConfig);
                // }

                @Override
                public void onActivityResult(int requestCode, int resultCode, Intent data) {
                    // TODO Auto-generated method stub
                    super.onActivityResult(requestCode, resultCode, data);
                    if (null != payApi) {
                        payApi.onPayResult(requestCode, resultCode, data);
                    }

                    if (requestCode == REQUEST_CODE) {
                        showFloat();
                    } else if (requestCode == RNA_REQUEST) {
                        // 20190510 此处处理认证后的逻辑 TODO
                        if (resultCode == Activity.RESULT_OK) {
                            Bundle bundle = data.getExtras();
                            if (bundle != null) {
                                String name = bundle.getString(Constants.KEY_RNA_NAME);
                                String birthday = bundle.getString(Constants.KEY_RNA_IDENTITY_CARD_NUMBER);
                                Toast.makeText(activity, "名字：" + name + "；出生年月日：" + birthday,
                                        Toast.LENGTH_LONG).show();
                                // 两者均不为空，那么实名认证成功，其他实名认证失败
                                return;
                            }
                        }
                        // 没有进行实名认证
                        Toast.makeText(activity, "没有实名", Toast.LENGTH_LONG).show();
                    }
                }

            });

            U8SDK.getInstance().onResult(U8Code.CODE_INIT_SUCCESS, "init success");

        } catch (Exception e) {
            U8SDK.getInstance().onResult(U8Code.CODE_INIT_SUCCESS, "init failed");
            e.printStackTrace();
        }
    }

    /**
     * 检查权限根据集合里边的权限去动态申请权限否则去设置里边
     */

    private void checkPermissions(String... permissions) {
        List<String> needRequestPermissonList = findDeniedPermissions(permissions);
        if (null != needRequestPermissonList && needRequestPermissonList.size() > 0) {
            ActivityCompat.requestPermissions(activity,
                    needRequestPermissonList.toArray(new String[needRequestPermissonList.size()]),
                    REQUEST_CODE_ASK_PERMISSIONS);
        }
    }

    /**
     * 将所需权限遍历并添加到list集合里边
     */
    private List<String> findDeniedPermissions(String[] permissions) {
        List<String> needRequestPermissonList = new ArrayList<String>();
        for (String perm : permissions) {
            if (ContextCompat.checkSelfPermission(activity,
                    perm) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.shouldShowRequestPermissionRationale(activity, perm)) {
                needRequestPermissonList.add(perm);
            }
        }
        return needRequestPermissonList;
    }

    @TargetApi(23)
    private void showFloat() {
        if (Settings.canDrawOverlays(activity)) {
            initGameAssist();
        } else {

            boolean isShow = sp.getBoolean("isShow", true);
            if (isShow) {
                createDialog();
            }

            Toast.makeText(activity, "悬浮球权限未开启", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 若获取到悬浮窗权限，开启小助手悬浮窗
     */
    protected void initGameAssist() {
        mGameAssistConfig = new GameAssistConfig();
        mGameAssistConfig.setHideGift(true);
        if (mCoolcloud != null) {
            mGameAssistApi = (GameAssistApi) mCoolcloud.getGameAssistApi(activity,
                    mGameAssistConfig);
            if (mGameAssistApi != null) {
                mGameAssistApi.addOnSwitchingAccountListen(new GameAssistApi.SwitchingAccount() {

                    @Override
                    public void onSwitchingAccounts() { // 重要
                        // 切换账号
                        switchLogin();
                    }
                });
            }
        }
    }

    /**
     * 悬浮框权限提示框
     */
    private void createDialog() {
        new AlertDialog.Builder(activity).setTitle("悬浮窗").setMessage("是否显示悬浮窗")
                .setPositiveButton("是", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        editor.putBoolean("isShow", true).commit();
                        dialog.dismiss();

                        try {
                            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                            intent.setData(Uri.parse("package:" + activity.getPackageName()));
                            activity.startActivityForResult(intent, REQUEST_CODE);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).setNegativeButton("不再提示", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                editor.putBoolean("isShow", false).commit();
                dialog.dismiss();
            }
        }).setCancelable(false).show();
    }

    /**
     * 需要进行检测的权限数组 这里只列举了几项 小伙伴可以根据自己的项目需求 来添加
     */
    protected String[] needPermissions = {Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};

    public void login() {

        try {
            final Bundle input = new Bundle();
            input.putInt(Constants.KEY_SCREEN_ORIENTATION, this.orientation);
            input.putString(Constants.KEY_RESPONSE_TYPE, Constants.RESPONSE_TYPE_CODE);
            input.putString(Constants.KEY_SCOPE, "get_basic_userinfo");

            Log.e(TAG, "login start");

            mCoolcloud.login(activity, input, new Handler(), new OnResultListener() {

                @Override
                public void onResult(Bundle data) {
                    String code = data.getString(Params.KEY_AUTHCODE);
                    Log.e(TAG, "onResult：" + code);
                    U8SDK.getInstance().onResult(U8Code.CODE_LOGIN_SUCCESS, code);

                    U8SDK.getInstance().onLoginResult(code);
                }

                @Override
                public void onError(ErrInfo err) {
                    Log.e(TAG, "onError：" + err.getMessage());
                    U8SDK.getInstance().onResult(U8Code.CODE_LOGIN_FAIL, err.getError() + ";" + err.getMessage());
                }

                @Override
                public void onCancel() {
                    Log.e(TAG, "onCancel");
                    U8SDK.getInstance().onResult(U8Code.CODE_LOGIN_FAIL, "user cancelled");
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "login start Exception:" + e);
            U8SDK.getInstance().onResult(U8Code.CODE_LOGIN_FAIL, e.getMessage());
            e.printStackTrace();
        }
    }

    public void switchLogin() {

        U8SDK.getInstance().onLogout();
        try {
            final Bundle input = new Bundle();
            input.putInt(Constants.KEY_SCREEN_ORIENTATION, this.orientation);
            input.putString(Constants.KEY_SCOPE, "get_basic_userinfo");
            input.putString(Constants.KEY_RESPONSE_TYPE, Constants.RESPONSE_TYPE_CODE);

            Log.e(TAG, "switchLogin start");

            mCoolcloud.loginNew(activity, input, new Handler(), new OnResultListener() {

                @Override
                public void onResult(Bundle data) {
                    String code = data.getString(Params.KEY_AUTHCODE);
                    Log.e(TAG, "onResult：" + code);
                    U8SDK.getInstance().onResult(U8Code.CODE_LOGIN_SUCCESS, code);

                    U8SDK.getInstance().onLoginResult(code);
                }

                @Override
                public void onError(ErrInfo err) {
                    Log.e(TAG, "onError：" + err.getMessage());
                    U8SDK.getInstance().onResult(U8Code.CODE_LOGIN_FAIL, err.getError() + ";" + err.getMessage());
                }

                @Override
                public void onCancel() {
                    Log.e(TAG, "onCancel");
                    U8SDK.getInstance().onResult(U8Code.CODE_LOGIN_FAIL, "user cancelled");
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "login start Exception:" + e);
            U8SDK.getInstance().onResult(U8Code.CODE_LOGIN_FAIL, e.getMessage());
            e.printStackTrace();
        }
    }

    private TKTEntity entity;

    /**
     * 实名认证弹窗测试
     */
    private void rnaTest() {
        if (!TextUtils.isEmpty(appID)) {// 通过APPID获取信息
            entity = LocalComplexProvider.get(activity).getTKT(appID);
        }

        Intent mIntent = new Intent(activity, RnaDialogActivity.class);
        KVUtils.put(mIntent, Constants.KEY_SCREEN_ORIENTATION, orientation);// 设置横竖屏
        if (entity != null) {
            KVUtils.put(mIntent, Constants.KEY_TKT, entity.getTKT());// 传入TKT
            KVUtils.put(mIntent, Constants.KEY_UID, entity.getUID());// 传入UID
        }
        activity.startActivityForResult(mIntent, RNA_REQUEST);// 需要返回结果码方式启动界面
    }

    public void submitGameData(UserExtraData extraData) {
        // TODO::调用AAA上报玩家数据接口
        // 如果AAA没有提供对应的接口，则不用实现该方法
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

    public void logout() {
        mCoolcloud.logout(activity);
    }

    public void pay(final PayParams params) {
        try {
            GoodInfo good = getGoodInfo(params.getProductId());
            String waresid = "0";
            if (good != null) {
                waresid = good.getWaresid();
            }

            Log.i(TAG, "The waresid is " + waresid);

            UToken token = U8SDK.getInstance().getUToken();

            if (token == null) {
                U8SDK.getInstance().onResult(U8Code.CODE_PAY_FAIL, "the token is null");
                return;
            }

            Log.d(TAG, "The auth extInfo is " + token.getExtension());

            JSONObject extInfo = new JSONObject(token.getExtension());
            String accessToken = extInfo.getString("access_token");
            String openID = extInfo.getString("openid");

            String expiredIn = extInfo.getString("expiredIn");
            String refreshToken = extInfo.getString("refreshToken");

            CoolYunAccessInfo accessInfo = new CoolYunAccessInfo();
            accessInfo.setAccessToken(accessToken);
            // accessInfo.setExpiresIn(expiredIn);
            accessInfo.setOpenId(openID);
            // accessInfo.setRefreshToken(refreshToken);

            PayInfo payData = new PayInfo();
            payData.setAppId(this.appID);
            payData.setPayKey(this.payKey);
            payData.setName(params.getProductName());
            payData.setPoint(Integer.parseInt(waresid));
            payData.setPrice(params.getPrice() * 100);
            payData.setQuantity(1);
            payData.setCpOrder(params.getOrderID());
            payData.setCpPrivate(params.getExtension());

            TDGAVirtualCurrency.onChargeRequest(params.getOrderID(), params.getProductName(), params.getPrice(), "CNY", params.getPrice() * 7, "CoolPad SDK");

            Log.d(TAG, "start pay...");
            payApi.startPay(activity, payData, accessInfo, new IPayResult() {

                @Override
                public void onResult(CoolPayResult result) {

                    Log.d(TAG, "pay result:" + result.getResultStatus() + ";msg:" + result.getResult());

                    switch (result.getResultStatus()) {
                        case 0:
                            TDGAVirtualCurrency.onChargeSuccess(params.getOrderID());

                            U8SDK.getInstance().onResult(U8Code.CODE_PAY_SUCCESS, "pay success");
                            break;
                        case -1:
                            U8SDK.getInstance().onResult(U8Code.CODE_PAY_FAIL, "pay failed");
                            break;
                        case -2:
                            U8SDK.getInstance().onResult(U8Code.CODE_PAY_CANCEL, "pay canceled");
                            break;
                        default:
                            U8SDK.getInstance().onResult(U8Code.CODE_PAY_FAIL, "pay failed");
                            break;
                    }
                }
            }, CoolpayApi.PAY_STYLE_DIALOG, orientation);

        } catch (Exception e) {
            U8SDK.getInstance().onResult(U8Code.CODE_PAY_UNKNOWN, e.getMessage());
            e.printStackTrace();
            Log.e(TAG, "CoolPadSDK pay error:" + e.getMessage());
        }
    }

    private GoodInfo getGoodInfo(String productID) {

        if (this.goods.containsKey(productID)) {
            return this.goods.get(productID);
        }
        return null;
    }

    private void loadKuPaiPayGoods() {
        String xmlGoods = SDKTools.getAssetConfigs(activity, "kupai_pay.xml");

        if (xmlGoods == null) {
            Log.i(TAG, "fail to load kupai_pay.xml");
            return;
        }

        Log.i(TAG, "The kupai_pay Str:" + xmlGoods);

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
                                Log.i(TAG, "Curr Good: " + productId + " has duplicated.");
                            }

                            Log.i(TAG, "Curr Good: " + productId + "; waresid:" + wareid + ";openPrice:" + openPrice);
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
