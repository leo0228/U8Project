package com.u8.sdk;

import android.util.Log;

import com.tencent.ysdk.api.YSDKApi;
import com.tencent.ysdk.framework.common.eFlag;
import com.tencent.ysdk.framework.common.ePlatform;
import com.tencent.ysdk.module.AntiAddiction.listener.AntiAddictListener;
import com.tencent.ysdk.module.AntiAddiction.model.AntiAddictRet;
import com.tencent.ysdk.module.bugly.BuglyListener;
import com.tencent.ysdk.module.pay.PayListener;
import com.tencent.ysdk.module.pay.PayRet;
import com.tencent.ysdk.module.user.UserListener;
import com.tencent.ysdk.module.user.UserLoginRet;
import com.tencent.ysdk.module.user.UserRelationRet;
import com.tencent.ysdk.module.user.WakeupRet;

public class YSDKCallback implements UserListener, BuglyListener, PayListener, AntiAddictListener {

    public YSDKCallback() {
    }

    @Override
    public void OnPayNotify(PayRet ret) {
        Log.d(YSDK.TAG, "OnPayNotify flag:" + ret.flag + "msg:" + ret.msg);
        if (PayRet.RET_SUCC == ret.ret) {
            //支付流程成功
            switch (ret.payState) {
                //支付成功
                case PayRet.PAYSTATE_PAYSUCC:
                    YSDK.getInstance().chargeWhenPaySuccess();
                    break;
                //取消支付
                case PayRet.PAYSTATE_PAYCANCEL:
                    U8SDK.getInstance().onResult(U8Code.CODE_PAY_CANCEL, "支付取消");
                    break;
                //支付结果未知
                case PayRet.PAYSTATE_PAYUNKOWN:
                    U8SDK.getInstance().onResult(U8Code.CODE_PAY_UNKNOWN, "支付结果未知");
                    break;
                //支付失败
                case PayRet.PAYSTATE_PAYERROR:
                    U8SDK.getInstance().onResult(U8Code.CODE_PAY_FAIL, "pay failed:" + ret.msg);
                    break;
            }
        } else {
            switch (ret.flag) {
                case eFlag.Login_TokenInvalid:
                    if (YSDK.getInstance().isStartLogined()) {
                        YSDK.getInstance().logout();
                        switch (YSDK.lastLoginType) {
                            case YSDK.LOGIN_TYPE_QQ:
                                YSDKApi.login(ePlatform.QQ);
                                break;
                            case YSDK.LOGIN_TYPE_WX:
                                YSDKApi.login(ePlatform.WX);
                                break;
                            default:
                                YSDK.getInstance().login(YSDK.lastLoginType);
                                break;
                        }
                    }
                    break;
                case eFlag.Pay_User_Cancle:
                    U8SDK.getInstance().onResult(U8Code.CODE_PAY_CANCEL, "支付取消");
                    break;
                case eFlag.Pay_Param_Error:
                    U8SDK.getInstance().onResult(U8Code.CODE_PAY_FAIL, "支付失败，参数错误");
                    break;
                case eFlag.Error:
                default:
                    U8SDK.getInstance().onResult(U8Code.CODE_PAY_FAIL, "pay failed:" + ret.msg);
                    break;
            }
        }
    }


    @Override
    public byte[] OnCrashExtDataNotify() {
        return null;
    }


    @Override
    public String OnCrashExtMessageNotify() {
        return null;
    }


    @Override
    public void OnLoginNotify(UserLoginRet ret) {
        Log.e(YSDK.TAG, "OnLoginNotify flag:" + ret.flag + "msg:" + ret.msg);
        switch (ret.flag) {
            case eFlag.Succ:
                YSDK.getInstance().letUserLogin(false);
                break;

            //需要重新调起登录界面
            case eFlag.Login_NotRegisterRealName:
            case eFlag.Login_NeedRegisterRealName:
                YSDK.getInstance().logout();
                YSDK.getInstance().cleanLoginInfo();
                U8SDK.getInstance().onResult(U8Code.CODE_LOGIN_FAIL, "未实名，登陆失败");
                break;

            case eFlag.Login_TokenInvalid:
                if (YSDK.getInstance().isStartLogined()) {
                    YSDK.getInstance().logout();
                    switch (YSDK.lastLoginType) {
                        case YSDK.LOGIN_TYPE_QQ:
                            YSDKApi.login(ePlatform.QQ);
                            break;
                        case YSDK.LOGIN_TYPE_WX:
                            YSDKApi.login(ePlatform.WX);
                            break;
                        default:
                            YSDK.getInstance().login(YSDK.lastLoginType);
                            break;
                    }
                }
                break;
            case eFlag.QQ_UserCancel:
            case eFlag.QQ_LoginFail:
            case eFlag.QQ_NetworkErr:
            case eFlag.QQ_NotInstall:
            case eFlag.QQ_NotSupportApi:
            case eFlag.WX_NotInstall:
            case eFlag.WX_NotSupportApi:
            case eFlag.WX_UserCancel:
            case eFlag.WX_UserDeny:
            case eFlag.WX_LoginFail:
            default:
                U8SDK.getInstance().onResult(U8Code.CODE_LOGIN_FAIL, "login failed");
                break;
        }

    }

    @Override
    public void OnRelationNotify(UserRelationRet arg0) {

    }


    @Override
    public void OnWakeupNotify(WakeupRet ret) {
        Log.e(YSDK.TAG, "OnWakeupNotify flag:" + ret.flag + "msg:" + ret.msg);

        switch(ret.flag){
            case eFlag.Wakeup_YSDKLogining:
                break;
            case eFlag.Wakeup_NeedUserSelectAccount:
                YSDK.getInstance().showDiffLogin();
                break;
            case eFlag.Wakeup_NeedUserLogin:
                break;

        }

    }

    /**
     * 时长限制的防沉迷回调
     */
    @Override
    public void onTimeLimitNotify(AntiAddictRet ret) {
        Log.e(YSDK.TAG, "onTimeLimitNotify flag:" + ret.flag + "msg:" + ret.msg);
        if (AntiAddictRet.RET_SUCC == ret.ret) {
            switch (ret.ruleFamily) {
                case AntiAddictRet.RULE_WORK_TIP:
                case AntiAddictRet.RULE_WORK_NO_PLAY:
                case AntiAddictRet.RULE_HOLIDAY_TIP:
                case AntiAddictRet.RULE_HOLIDAY_NO_PLAY:
                case AntiAddictRet.RULE_NIGHT_NO_PLAY:

                default:
                    YSDK.getInstance().executeInstruction(ret);
                    break;
            }
        }

    }

    /**
     * 登录限制的防沉迷回调
     */
    @Override
    public void onLoginLimitNotify(AntiAddictRet ret) {
        Log.e(YSDK.TAG, "onLoginLimitNotify flag:" + ret.flag + "msg:" + ret.msg);
        if (AntiAddictRet.RET_SUCC == ret.ret) {
            switch (ret.ruleFamily) {
                case AntiAddictRet.RULE_WORK_TIP:
                case AntiAddictRet.RULE_WORK_NO_PLAY:
                case AntiAddictRet.RULE_HOLIDAY_TIP:
                case AntiAddictRet.RULE_HOLIDAY_NO_PLAY:
                case AntiAddictRet.RULE_NIGHT_NO_PLAY:

                default:
                    YSDK.getInstance().executeInstruction(ret);
                    break;
            }
        }

    }


}
