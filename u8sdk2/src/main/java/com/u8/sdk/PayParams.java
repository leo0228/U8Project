package com.u8.sdk;

/***
 * 支付参数
 * @author xiaohei
 *
 */
public class PayParams {

    private String productId; //商品ID
    private String productName; //商品名称
    private String productDesc; //商品描述
    private int price; //商品价格，单位元
    private int ratio;    //兑换比例，暂时无用
    private int buyNum = 1; //购买数量，默认1
    private int coinNum; //获得金币（物品）数量
    private String serverId; //服务器id
    private String serverName;//服务器名称
    private String roleId;//角色id
    private String roleName;//角色名
    private int roleLevel;//角色等级
    private String payNotifyUrl;//支付回调地址，可为""
    private String vip;//vip说明
    private String orderID;//订单id
    private String extension;//	附加参数

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductDesc() {
        return productDesc;
    }

    public void setProductDesc(String productDesc) {
        this.productDesc = productDesc;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getBuyNum() {
        return buyNum;
    }

    public void setBuyNum(int buyNum) {
        this.buyNum = buyNum;
    }

    public int getCoinNum() {
        return coinNum;
    }

    public void setCoinNum(int coinNum) {
        this.coinNum = coinNum;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public int getRoleLevel() {
        return roleLevel;
    }

    public void setRoleLevel(int roleLevel) {
        this.roleLevel = roleLevel;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public int getRatio() {
        return ratio;
    }

    public void setRatio(int ratio) {
        this.ratio = ratio;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getVip() {
        return vip;
    }

    public void setVip(String vip) {
        this.vip = vip;
    }

    public String getPayNotifyUrl() {
        return payNotifyUrl;
    }

    public void setPayNotifyUrl(String payNotifyUrl) {
        this.payNotifyUrl = payNotifyUrl;
    }

    public String getOrderID() {
        return orderID;
    }

    public void setOrderID(String orderID) {
        this.orderID = orderID;
    }

    @Override
    public String toString() {
        return "PayParams [productId=" + productId + ", productName=" + productName + ", productDesc=" + productDesc
                + ", price=" + price + ", ratio=" + ratio + ", buyNum=" + buyNum + ", coinNum=" + coinNum
                + ", serverId=" + serverId + ", serverName=" + serverName + ", roleId=" + roleId + ", roleName="
                + roleName + ", roleLevel=" + roleLevel + ", payNotifyUrl=" + payNotifyUrl + ", vip=" + vip
                + ", orderID=" + orderID + ", extension=" + extension + "]";
    }


}
