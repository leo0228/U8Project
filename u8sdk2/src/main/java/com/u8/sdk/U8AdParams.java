package com.u8.sdk;

public class U8AdParams {
    /**
     * 点击广告之外区域（屏幕或者back键）是否可以关闭广告
     * 如果接入方要通过自定义按钮关闭广告时，并且点击广告之外区域（屏幕或者back键）不可以关闭广告，可设置为false
     * 如果接入方需要点击广告之外区域（屏幕或者back键）可以关闭广告时，并且广告之外按钮失效，可设置为true
     */
    private boolean isClose;
    private int width;       //广告尺寸，宽， 单位pixel
    private int height;      //广告尺寸，高， 单位pixel
    private int x;           //广告显示位置，x，单位pixel，对于左上角（0.0）X轴的偏移量
    private int y;           //广告显示位置，y，单位pixel，对于左上角（0.0）Y轴的偏移量

    public U8AdParams(boolean isClose, int width, int height, int x, int y) {
        this.isClose = isClose;
        this.width = width;
        this.height = height;
        this.x = x;
        this.y = y;
    }


    public boolean isClose() {
        return isClose;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
