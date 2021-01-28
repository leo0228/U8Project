package com.u8.sdk;


import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.samsung.interfaces.callback.ILoginResultCallback;
import com.samsung.interfaces.callback.IPayResultCallback;
import com.samsung.sdk.main.IAppPay;
import com.samsung.sdk.main.IAppPayOrderUtils;
import com.samsung.sdk.notice.SamsungNoticeSdk;
import com.samsung.sdk.notice.callback.SamsungNoticeLoginCallback;
import com.samsung.sdk.notice.callback.SamsungNoticeQuitCallback;
import com.samsung.sdk.notice.main.SamsungNoticeSignUtils;
import com.tendcloud.tenddata.TDGAProfile;
import com.tendcloud.tenddata.TDGAVirtualCurrency;
import com.zjtx.prompt.PromptDialog;

import org.json.JSONObject;

import java.util.Map;

public class SanXingSDK {
    private Activity activity;
    private String TAG = "SanXingSDK";
    public static SanXingSDK instance;

    private String appID;
    private String acid;
    private String clientID;
    private String clientSecret;
    private int screenType;
    private String privateKey;
    private String publicKey;
    private String Notifyurl;

    private SanXingSDK() {
        activity = U8SDK.getInstance().getContext();
    }

    public static SanXingSDK getInstance() {
        if (instance == null) {
            instance = new SanXingSDK();
        }
        return instance;
    }

    public void intiSDK(SDKParams params) {
        this.parseSDKParams(params);
        PromptDialog.init(activity, new PromptDialog.OnPromptListener() {
            @Override
            public void onInitSDK() {
                initSDK();
            }
        });

    }

    private void parseSDKParams(SDKParams params) {
        this.appID = params.getString("appID");
        this.acid = params.getString("acid");
        this.clientID = params.getString("clientID");
        this.clientSecret = params.getString("clientSecret");
        this.screenType = params.getInt("screenType");
        this.privateKey = params.getString("privateKey");
        this.publicKey = params.getString("publicKey");
        this.Notifyurl = params.getString("Notifyurl");

        Log.d(TAG, " initSDK start");
        Log.d(TAG, " appID is " + appID);
        Log.d(TAG, " acid is " + acid);
        Log.d(TAG, " clientID is " + clientID);
        Log.d(TAG, " clientSecret is " + clientSecret);
        Log.d(TAG, " screenType is " + screenType);
        Log.d(TAG, " privateKey is " + privateKey);
        Log.d(TAG, " publicKey is " + publicKey);
        Log.d(TAG, " NotifyUrl is " + Notifyurl);
    }

    protected String[] needPermissions = {Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA};

    private void initSDK() {

        if (android.os.Build.VERSION.SDK_INT >= 23) {

            if (ContextCompat.checkSelfPermission(activity,
                    Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, needPermissions, 0);
            }
        }

        IAppPay.init(activity, screenType, appID, acid);

    }

    public void login() {

        /**
         * 获取登录参数
         * 1、登陆参数是 appid和package的签名值：
         * 	 String data = AppID +"&"+ 包名。
         *   String loginParams = sign(data);
         *
         * 2、重要说明：
         *   这里是为了方便演示在客户端生成 loginParams，所以Demo中加签过程直接放在客户端完成。
         *   真实App里，privateKey等数据严禁放在客户端，加签过程务必在服务端完成；（服务端代码示例有签名代码）
         *   防止商户私密数据泄露，造成不必要的损失。
         */

        String loginParams = IAppPayOrderUtils.getLoginParams(this.appID, activity.getPackageName(), this.privateKey);

        IAppPay.startLogin(activity, loginParams, new ILoginResultCallback() {

            @Override
            public void onSuccess(String signValue, Map<String, String> resultMapStr) {
                Log.d(TAG, "signValue:" + signValue);

                String str = encodeLoginResult(signValue);
                U8SDK.getInstance().onLoginResult(str);
                U8SDK.getInstance().onResult(U8Code.CODE_LOGIN_SUCCESS, str);

                //弹出登录之后公告
                showLoginNotice();
            }

            @Override
            public void onFaild(String errorCode, String errorMessage) {
                U8SDK.getInstance().onResult(U8Code.CODE_LOGIN_FAIL, "login failed");
            }

            @Override
            public void onCanceled() {
                U8SDK.getInstance().onResult(U8Code.CODE_LOGIN_FAIL, "login canceled");
            }
        });

    }

    private String encodeLoginResult(String signValue) {
        JSONObject json = new JSONObject();
        try {
            json.put("signValue", signValue);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return json.toString();
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

    public void logout() {
        U8SDK.getInstance().onLogout();
        U8SDK.getInstance().onResult(U8Code.CODE_LOGOUT_SUCCESS, "logout success");
    }

    /**
     * 1、登录之后调用
     * 登录三星账号之后，调起登录公告弹窗展示登录活动信息
     */
    private void showLoginNotice() {
        /**
         * 获取登录公告弹窗接口参数
         * 1、登录弹窗接口参数是 appid和package的签名值：
         * 	 String data = AppID +"&"+ 包名。
         *   String signData = sign(data);
         *
         * 2、重要说明：
         *   这里是为了方便演示在客户端生成 signData，所以Demo中加签过程直接放在客户端完成。
         *   真实App里，privateKey等数据严禁放在客户端，加签过程务必在服务端完成；（服务端代码示例有签名代码）
         *   防止商户私密数据泄露，造成不必要的损失。
         */
        String signData = SamsungNoticeSignUtils.getNoticeParams(this.appID, activity.getPackageName(), this.privateKey);

        //如果不需要登录公告弹窗的回调可以传null
        SamsungNoticeSdk.showLoginNotice(activity, this.appID, signData, new SamsungNoticeLoginCallback() {
            @Override
            public void noticeLoginCallBack(int code, String reason) {
                switch (code) {
                    case SamsungNoticeSdk.NOTICE_PARAMETER_ERROR:
                        //参数有问题，检查传入接口的参数。
                        break;
                    case SamsungNoticeSdk.NOTICE_NO_DATA:
                        //没有配置公告数据，弹窗不显示。
                        break;
                    case SamsungNoticeSdk.NOTICE_NETWORK_ERROR:
                        //获取公告数据时网络错误，弹窗不显示。
                        break;
                    case SamsungNoticeSdk.NOTICE_CANCEL:
                        //如果需要在登录公告弹窗关闭后处理一些逻辑，可以在此操作。
                        break;
                    default:
                        break;
                }
            }
        });
    }

    public void exit() {
		SamsungNoticeSdk.showQuitNotice(activity, new SamsungNoticeQuitCallback() {
			@Override
			public void noticeQuitCallBack(int code, String reason) {
				switch (code) {
					case SamsungNoticeSdk.NOTICE_PARAMETER_ERROR:
						//参数有问题，检查传入接口的参数。
						break;
					case SamsungNoticeSdk.NOTICE_CANCEL:
						//用户主动点击弹窗上的取消按钮，此时关闭弹窗不退出游戏
						break;
					case SamsungNoticeSdk.NOTICE_QUIT:
						//用户主动点击弹窗上的退出游戏按钮，此时退出游戏
						activity.finish();
						System.exit(0);
						break;
					default:
						break;
				}
			}
		});
	}

    private static final int waresid_with_times = 1;        //按次
    private static final int waresid_open_price = 2;        //开放价格

    public void Pay(final PayParams params) {
        String loginParams = IAppPayOrderUtils.getLoginParams(this.appID, activity.getPackageName(), this.privateKey);

		IAppPayOrderUtils orderUtils = new IAppPayOrderUtils();
		orderUtils.setAppid(this.appID);
		orderUtils.setWaresid(waresid_open_price);
		orderUtils.setCporderid(params.getOrderID());
		orderUtils.setAppuserid(params.getRoleId());
		orderUtils.setPrice(Double.valueOf(params.getPrice()));//单位 元
		orderUtils.setWaresname(params.getProductName());//开放价格名称(用户可自定义，如果不传以后台配置为准)
		orderUtils.setCpprivateinfo(params.getExtension());
		orderUtils.setNotifyurl(Notifyurl);
		String orderParams = orderUtils.getTransdata(this.privateKey);

        TDGAVirtualCurrency.onChargeRequest(params.getOrderID(), params.getProductName(), params.getPrice(), "CNY", params.getPrice() * 7,"sanxing SDK");

        IAppPay.startPay(activity, loginParams, orderParams, new IPayResultCallback() {

            @Override
            public void onPayResult(int resultCode, String signValue, String resultInfo) {
                Log.d(TAG, "requestCode:" + resultCode + ",signValue:" + signValue + ",resultInfo:" + resultInfo);

                switch (resultCode) {
                    case IAppPay.PAY_SUCCESS:
                        //调用 IAppPayOrderUtils 的验签方法进行支付结果验证
                        boolean payState = IAppPayOrderUtils.checkPayResult(signValue, publicKey);
                        if (payState) {
                            TDGAVirtualCurrency.onChargeSuccess(params.getOrderID());

                            U8SDK.getInstance().onResult(U8Code.CODE_PAY_SUCCESS, "pay success");
                        }

                        break;
                    case IAppPay.PAY_CANCEL:
                        U8SDK.getInstance().onResult(U8Code.CODE_PAY_CANCEL, "pay cancel");
                        break;
                    default:
                        U8SDK.getInstance().onResult(U8Code.CODE_PAY_FAIL, "pay failed code:" + resultInfo);
                        break;
                }

            }
        });

    }

}
