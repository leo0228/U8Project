package com.u8.sdk.plugin;

import com.u8.sdk.IUser;
import com.u8.sdk.PluginFactory;
import com.u8.sdk.UserExtraData;
import com.u8.sdk.impl.SimpleDefaultUser;

/**
 * 用户插件
 *
 * @author xiaohei
 */
public class U8User {
    private static U8User instance;

    private IUser userPlugin;

    private U8User() {

    }

    public void init() {
        this.userPlugin = (IUser) PluginFactory.getInstance().initPlugin(IUser.PLUGIN_TYPE);
        if (this.userPlugin == null) {
            this.userPlugin = new SimpleDefaultUser();
        }
    }

    public static U8User getInstance() {
        if (instance == null) {
            instance = new U8User();
        }

        return instance;
    }

    /**
     * 是否支持某个方法
     *
     * @param method
     */
    public boolean isSupport(String method) {
        if (userPlugin == null) {
            return false;
        }
        return userPlugin.isSupportMethod(method);
    }

    /**
     * 登录接口
     */
    public void login() {
        if (userPlugin == null) {
            return;
        }

        userPlugin.login();
    }

    /**
     * 部分渠道可能有特殊的需求
     * 比如MSDK，有微信登录和QQ登录。游戏可能需要在游戏中
     * 增加微信和QQ登录按钮，然后通过customData来选择
     * 调用的是微信还是QQ
     *
     * @param customData
     */
    public void login(String customData) {
        if (userPlugin == null) {
            return;
        }
        userPlugin.loginCustom(customData);
    }

    /**
     * 切换账号
     */
    public void switchLogin() {
        if (userPlugin == null) {
            return;
        }

        userPlugin.switchLogin();
    }

    /**
     * 显示用户中心
     */
    public void showAccountCenter() {
        if (userPlugin == null) {
            return;
        }

        userPlugin.showAccountCenter();
    }

    /**
     * 退出当前帐号
     */
    public void logout() {
        if (userPlugin == null) {
            return;
        }

        userPlugin.logout();
    }

    /***
     * 提交扩展数据，角色登录成功之后，需要调用
     * @param extraData
     */
    public void submitExtraData(UserExtraData extraData) {
        if (this.userPlugin == null) {
            return;
        }
        userPlugin.submitExtraData(extraData);
    }

    /**
     * SDK退出接口，有的SDK需要在退出的时候，弹出SDK的退出确认界面。
     * 如果SDK不需要退出确认界面，则弹出游戏自己的退出确认界面
     */
    public void exit() {
        if (this.userPlugin == null) {
            return;
        }
        userPlugin.exit();
    }

    /**
     * 上传礼包兑换码
     *
     * @param code
     */
    public void postGiftCode(String code) {
        if (this.userPlugin == null) {
            return;
        }
        userPlugin.postGiftCode(code);
    }

    /***
     * 实名注册方法
     */
    public void realNameRegister() {
        if (this.userPlugin == null) {
            return;
        }
        userPlugin.realNameRegister();
    }

    /***
     * 防沉迷系统查询接口
     */
    public void queryAntiAddiction() {
        if (this.userPlugin == null) {
            return;
        }
        userPlugin.queryAntiAddiction();
    }

    /**
     * 客服中心
     */
    public void showCallCenter() {
        if (this.userPlugin == null) {
            return;
        }
        userPlugin.showCallCenter();
    }

    /**
     * 隐私政策
     */
    public void showPrivacyPolicy() {
        if (this.userPlugin == null) {
            return;
        }
        userPlugin.showPrivacyPolicy();
    }

    /**
     * 跳转超休闲专区（OPPO）
     */
    public void jumpLeisureSubject() {
        if (this.userPlugin == null) {
            return;
        }
        userPlugin.jumpLeisureSubject();
    }
}
