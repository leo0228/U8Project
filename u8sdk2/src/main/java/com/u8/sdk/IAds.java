package com.u8.sdk;

/***
 * 广告接口
 * @author xiaohei
 *
 */
public interface IAds extends IPlugin {
    public static final int PLUGIN_TYPE = Constants.PLUGIN_TYPE_ADS;

    /**
     * 激励视频广告
     *
     * @param posId 广告位id
     */
    void rewardAd(String posId);

    /**
     * banner 广告
     *
     * @param posId 广告位id
     */
    void bannerAd(String posId);

    /**
     * 插屏广告
     *
     * @param posId 广告位id
     */
    void insertAd(String posId);

    /**
     * 开屏广告
     *
     * @param posId 广告位id
     */
    void splashAd(String posId);

    /**
     * 原生广告
     *
     * @param posId
     * @param adParams 是否可以关闭，广告尺寸，广告显示位置
     */
    void nativeAd(String posId, U8AdParams adParams);

    /**
     * 原生广告关闭
     */
    void nativeClose();
}
