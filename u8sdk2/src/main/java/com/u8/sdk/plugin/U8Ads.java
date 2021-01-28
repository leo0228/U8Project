package com.u8.sdk.plugin;

import com.u8.sdk.IAds;
import com.u8.sdk.PluginFactory;
import com.u8.sdk.U8AdParams;

/***
 * 广告插件
 * @author leo
 *
 */
public class U8Ads {

    private static U8Ads instance;

    private IAds adsPlugin;

    private U8Ads() {

    }

    public static U8Ads getInstance() {
        if (instance == null) {
            instance = new U8Ads();
        }
        return instance;
    }

    public void init() {
        this.adsPlugin = (IAds) PluginFactory.getInstance().initPlugin(IAds.PLUGIN_TYPE);

    }

    public boolean isSupport(String method) {
        if (this.adsPlugin == null) {
            return false;
        }

        return this.adsPlugin.isSupportMethod(method);
    }


    /***
     * 激励广告
     * @param posId
     */
    public void rewardAd(String posId) {
        if (this.adsPlugin == null) {
            return;
        }
        this.adsPlugin.rewardAd(posId);
    }

    /***
     * banner 广告
     * @param posId
     */
    public void bannerAd(String posId) {
        if (this.adsPlugin == null) {
            return;
        }
        this.adsPlugin.bannerAd(posId);
    }

    /***
     * 插屏广告
     * @param posId
     */
    public void insertAd(String posId) {
        if (this.adsPlugin == null) {
            return;
        }
        this.adsPlugin.insertAd(posId);
    }

    /**
     * 开屏广告
     *
     * @param posId 广告位id
     */
    public void splashAd(String posId) {
        if (this.adsPlugin == null) {
            return;
        }
        this.adsPlugin.splashAd(posId);
    }

    /**
     * 原生广告
     *
     * @param posId 广告位id
     */
    public void nativeAd(String posId, U8AdParams adParams) {
        if (this.adsPlugin == null) {
            return;
        }
        this.adsPlugin.nativeAd(posId, adParams);
    }

    /**
     * 原生广告关闭
     */
    public void nativeClose() {
        if (this.adsPlugin == null) {
            return;
        }
        this.adsPlugin.nativeClose();
    }
}
