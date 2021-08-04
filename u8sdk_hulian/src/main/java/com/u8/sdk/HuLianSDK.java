package com.u8.sdk;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.game.sdk.HuosdkManager;
import com.game.sdk.domain.CustomPayParam;
import com.game.sdk.domain.LoginErrorMsg;
import com.game.sdk.domain.LogincallBack;
import com.game.sdk.domain.PaymentCallbackInfo;
import com.game.sdk.domain.PaymentErrorMsg;
import com.game.sdk.domain.RoleInfo;
import com.game.sdk.domain.SubmitRoleInfoCallBack;
import com.game.sdk.listener.OnInitSdkListener;
import com.game.sdk.listener.OnLoginListener;
import com.game.sdk.listener.OnLogoutListener;
import com.game.sdk.listener.OnPaymentListener;
import com.tendcloud.tenddata.TDGAProfile;
import com.tendcloud.tenddata.TDGAVirtualCurrency;
import com.u8.common.PromptDialog;

import org.json.JSONObject;

public class HuLianSDK {
	private Activity activity;
	private static HuLianSDK instance;

	HuosdkManager sdkManager;

	String tag = "hulian";

	private HuLianSDK() {
		activity = U8SDK.getInstance().getContext();
	}

	public static HuLianSDK getInstance() {
		if (instance == null) {
			instance = new HuLianSDK();
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

		// this.appID = params.getString("app_Id");
		// this.appSecret = params.getString("appSecret");
		// this.appKey = params.getString("app_key");
		// this.isOffLine = params.getBoolean("is_offline_game");

	}

	private void initSDK() {
		// TODO::这里调用AAA的SDK初始化方法
		sdkManager = HuosdkManager.getInstance();

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			int i = ContextCompat.checkSelfPermission(activity, needPermissions[0]);
			if (i != PackageManager.PERMISSION_GRANTED) {
				ActivityCompat.requestPermissions(activity, needPermissions,
						PERMISSON_REQUESTCODE);
			} 
		}

		U8SDK.getInstance().setActivityCallback(new ActivityCallbackAdapter() {

			@Override
			public void onDestroy() {
				// TODO Auto-generated method stub
				super.onDestroy();
				if (sdkManager != null) {
					sdkManager.recycle();
				}
			}
		});
		
		new Handler().postDelayed(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				initHuLian();
			}
		}, 3000);

	}

	private void initHuLian() {
		Log.e(tag, "开始初始化------》");

		sdkManager.initSdk(activity, new OnInitSdkListener() {

			@Override
			public void initSuccess(String arg0, String arg1) {
				// TODO Auto-generated method stub
				U8SDK.getInstance().onResult(U8Code.CODE_INIT_SUCCESS, "init success");
			}

			@Override
			public void initError(String arg0, String arg1) {
				// TODO Auto-generated method stub
				U8SDK.getInstance().onResult(U8Code.CODE_INIT_FAIL, "login fail");
			}
		});

		sdkManager.addLoginListener(new OnLoginListener() {

			@Override
			public void loginSuccess(LogincallBack logincBack) {
				// TODO Auto-generated method stub
				Log.e(tag, "登陆memId=" + logincBack.mem_id + "  token=" + logincBack.user_token);

				String loginResult = encodeLoginResult(logincBack.mem_id, logincBack.user_token);

				U8SDK.getInstance().onLoginResult(loginResult);

				U8SDK.getInstance().onResult(U8Code.CODE_LOGIN_SUCCESS, "login success");
				// 一般登陆成功后需要显示浮点
				sdkManager.showFloatView();

			}

			@Override
			public void loginError(LoginErrorMsg arg0) {
				// TODO Auto-generated method stub
				U8SDK.getInstance().onResult(U8Code.CODE_LOGIN_FAIL, "login fail");
			}
		});

		sdkManager.addLogoutListener(new OnLogoutListener() {
			@Override
			public void logoutSuccess(int type, String code, String msg) {
				Log.e(tag, "登出成功，类型type=" + type + " code=" + code + " msg=" + msg);
				if (type == OnLogoutListener.TYPE_NORMAL_LOGOUT) {// 正常退出成功
					U8SDK.getInstance().onResult(U8Code.CODE_LOGOUT_SUCCESS, "logout success");

					sdkManager.showLogin(false);
				}

				if (type == OnLogoutListener.TYPE_SWITCH_ACCOUNT) {// 切换账号退出成功
					// 游戏此时可跳转到登陆页面，让用户进行切换账号
				
					U8SDK.getInstance().onResult(U8Code.CODE_LOGOUT_SUCCESS, "logout success");

					sdkManager.showLogin(false);
				}
				if (type == OnLogoutListener.TYPE_TOKEN_INVALID) {// 登陆过期退出成功
					// 游戏此时可跳转到登陆页面，让用户进行重新登陆
					U8SDK.getInstance().onResult(U8Code.CODE_LOGOUT_SUCCESS, "logout success");

					sdkManager.showLogin(false);
				}
			}

			@Override
			public void logoutError(int type, String code, String msg) {
				Log.e(tag, "登出失败，类型type=" + type + " code=" + code + " msg=" + msg);
				if (type == OnLogoutListener.TYPE_NORMAL_LOGOUT) {// 正常退出失败
					U8SDK.getInstance().onResult(U8Code.CODE_LOGOUT_FAIL, "logout fail");
				}
				if (type == OnLogoutListener.TYPE_SWITCH_ACCOUNT) {// 切换账号退出失败
					U8SDK.getInstance().onResult(U8Code.CODE_LOGOUT_FAIL, "logout fail");
				}
				if (type == OnLogoutListener.TYPE_TOKEN_INVALID) {// 登陆过期退出失败
					U8SDK.getInstance().onResult(U8Code.CODE_LOGOUT_FAIL, "logout fail");
				}
			}
		});

	}

	private static final int PERMISSON_REQUESTCODE = 321;

	/**
	 * 需要进行检测的权限数组 这里只列举了几项 小伙伴可以根据自己的项目需求 来添加
	 */
	protected String[] needPermissions = { Manifest.permission.READ_PHONE_STATE,
			Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE };


	public void login() {
		// TODO::这里调用AAA的登录方法
		Log.e(tag, " login start");
		sdkManager.showLogin(true);

	}

	private String encodeLoginResult(String mem_id, String user_token) {
		JSONObject json = new JSONObject();
		try {
			json.put("mem_id", mem_id);
			json.put("user_token", user_token);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return json.toString();
	}

	public void switchLogin() {
		// TODO::这里调用AAA切换帐号的方法
		// 如果没有提供切换帐号的方法，那么切换帐号的逻辑就是[先登出，再登录]，也就是先调用logout，再调用login
		sdkManager.switchAccount();
	}

	public void logout() {
		// TODO::调用AAA的登出方法
		sdkManager.logout();
	}

	public void showUserCenter() {
		// TODO::调用AAA显示个人中心的方法
		// 如果AAA没有提供对应的接口，则不用实现该方法
		sdkManager.openUcenter();
	}


	public void submitGameData(UserExtraData extraData) {
		// TODO::调用AAA上报玩家数据接口
		// 如果AAA没有提供对应的接口，则不用实现该方法
		switch (extraData.getDataType()) {
		case UserExtraData.TYPE_ENTER_GAME:
			subDataInSdk(extraData, 1);
			Log.e(tag, "creatTime : " + extraData.getRoleCreateTime() + "");
			enterGame(extraData);
			break;
		case UserExtraData.TYPE_LEVEL_UP:
			subDataInSdk(extraData, 3);
			upgrade(extraData);
			break;
		case UserExtraData.TYPE_CREATE_ROLE:
			subDataInSdk(extraData, 2);
			break;
		case UserExtraData.TYPE_EXIT_GAME:
			subDataInSdk(extraData, 5);
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

	RoleInfo roleInfo = new RoleInfo();

	public void subDataInSdk(UserExtraData extraData, int type) {
		roleInfo = initTestRoleInfo(extraData);
		roleInfo.setRole_type(type);

		// 提交角色信息
		sdkManager.setRoleInfo(roleInfo, new SubmitRoleInfoCallBack() {
			@Override
			public void submitSuccess() {
				Log.e(tag, "提交成功");
			}

			@Override
			public void submitFail(String msg) {
				Log.e(tag, "提交失败" + msg);
			}
		});
	}

	// 初始化测试的支付参数
	private void initTestParam(CustomPayParam payParam, PayParams params) {
		payParam.setCp_order_id(params.getOrderID());
		payParam.setProduct_price(Float.parseFloat(params.getPrice() + ""));
		payParam.setProduct_count(1);
		payParam.setProduct_id(params.getProductId());
		payParam.setProduct_name(params.getProductName());
		payParam.setProduct_desc(params.getProductDesc());
		payParam.setExchange_rate(0);
		payParam.setCurrency_name("金块");
		payParam.setExt(params.getExtension());
	}

	// 初始化测试的支付角色信息
	private RoleInfo initTestRoleInfo(UserExtraData extraData) {
		roleInfo.setRolelevel_ctime(extraData.getRoleCreateTime() + "");
		roleInfo.setRolelevel_mtime(extraData.getRoleLevelUpTime() + "");
		roleInfo.setParty_name("2");
		roleInfo.setRole_balence(Float.valueOf(extraData.getMoneyNum()));
		roleInfo.setRole_id(extraData.getRoleID());
		roleInfo.setRole_level(Integer.parseInt(extraData.getRoleLevel()));
		roleInfo.setRole_name(extraData.getRoleName());
		roleInfo.setRole_vip(1);
		roleInfo.setServer_id("2");
		roleInfo.setServer_name(extraData.getServerName());

		return roleInfo;
	}

	public void pay(final PayParams params) {
		// TODO::调用AAA充值接口
		// String payCallbackUrl ="http://myserver.moyoi.com:8093/pay/oppo/payCallback/19";

		Log.e(tag, "pay is start 12121212");

		CustomPayParam customPayParam = new CustomPayParam();
		initTestParam(customPayParam, params);
		customPayParam.setRoleinfo(roleInfo);

		TDGAVirtualCurrency.onChargeRequest(params.getOrderID(), params.getProductName(), params.getPrice(), "CNY", params.getPrice() * 7, "hulian SDK");
		// 调起支付
		sdkManager.showPay(customPayParam, new OnPaymentListener() {
			@Override
			public void paymentSuccess(PaymentCallbackInfo callbackInfo) {
				TDGAVirtualCurrency.onChargeSuccess(params.getOrderID());

				U8SDK.getInstance().onResult(U8Code.CODE_PAY_SUCCESS, "pay success");
			}

			@Override
			public void paymentError(PaymentErrorMsg errorMsg) {

				U8SDK.getInstance().onResult(U8Code.CODE_PAY_FAIL, "pay fail");
			}
		});

	}

}
