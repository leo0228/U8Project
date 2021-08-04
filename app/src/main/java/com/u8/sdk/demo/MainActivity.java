package com.u8.sdk.demo;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.widget.Toast;

import com.u8.sdk.IU8SDKListener;
import com.u8.sdk.InitResult;
import com.u8.sdk.PayParams;
import com.u8.sdk.PayResult;
import com.u8.sdk.ShareParams;
import com.u8.sdk.U8AdParams;
import com.u8.sdk.U8Code;
import com.u8.sdk.U8SDK;
import com.u8.sdk.UserExtraData;
import com.u8.sdk.log.Log;
import com.u8.sdk.plugin.U8Ads;
import com.u8.sdk.plugin.U8Pay;
import com.u8.sdk.plugin.U8Share;
import com.u8.sdk.plugin.U8User;
import com.u8.sdk.verify.UToken;


/**
 * U8SDK接入层
 */
public class MainActivity extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //必接
        U8SDK.getInstance().init(MainActivity.this);
        //必接
        U8SDK.getInstance().onCreate();

        setU8SDKListener();
    }

    //必接
    private void setU8SDKListener() {
        U8SDK.getInstance().runOnMainThread(new Runnable() {
            @Override
            public void run() {
                U8SDK.getInstance().setSDKListener(new IU8SDKListener() {
                    @Override
                    public void onResult(int code, String msg) {
                        //按需添加，在对应code下实现逻辑
                        switch (code) {
                            case U8Code.CODE_INIT_SUCCESS:
                                Toast.makeText(MainActivity.this, "初始化成功", Toast.LENGTH_SHORT).show();
                                break;
                            case U8Code.CODE_INIT_FAIL:
                                Toast.makeText(MainActivity.this, "初始化失败", Toast.LENGTH_SHORT).show();
                                break;
                            case U8Code.CODE_LOGIN_SUCCESS:
                                Toast.makeText(MainActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                                //（写入自己相关逻辑）,如果游戏客户端不需要获取userinfo去服务器验证登录，可在这里处理登录成功逻辑
                                break;
                            case U8Code.CODE_LOGIN_FAIL:
                                Toast.makeText(MainActivity.this, "登录失败", Toast.LENGTH_SHORT).show();
                                break;
                            case U8Code.CODE_LOGOUT_SUCCESS:
                                Toast.makeText(MainActivity.this, "登出成功", Toast.LENGTH_SHORT).show();
                                break;
                            case U8Code.CODE_PAY_SUCCESS:
                                Toast.makeText(MainActivity.this, "支付成功", Toast.LENGTH_SHORT).show();
                                //（写入自己相关逻辑）,如果是游戏客户端发货，在这里处理;如果服务器发货，不需要处理
                                break;
                            case U8Code.CODE_PAY_CANCEL:
                                Toast.makeText(MainActivity.this, "支付取消", Toast.LENGTH_SHORT).show();
                                break;
                            case U8Code.CODE_PAY_FAIL:
                            case U8Code.CODE_PAY_UNKNOWN:
                                Toast.makeText(MainActivity.this, "支付失败", Toast.LENGTH_SHORT).show();
                                break;
                            case U8Code.CODE_ADS_LOADED:
                                Toast.makeText(MainActivity.this, "广告加载完成", Toast.LENGTH_SHORT).show();
                                break;
                            case U8Code.CODE_ADS_SHOW:
                                Toast.makeText(MainActivity.this, "开始播放广告", Toast.LENGTH_SHORT).show();
                                break;
                            case U8Code.CODE_ADS_CLICKED:
                                Toast.makeText(MainActivity.this, "广告被点击", Toast.LENGTH_SHORT).show();
                                break;
                            case U8Code.CODE_ADS_COMPLETE:
                                Toast.makeText(MainActivity.this, "广告播放完成", Toast.LENGTH_SHORT).show();
                                //（写入自己相关逻辑）,如果游戏客户端发放奖励，可以在这里处理;如果服务器发货，不需要处理
                                break;
                            case U8Code.CODE_ADS_CLOSED:
                                Toast.makeText(MainActivity.this, "广告被关闭", Toast.LENGTH_SHORT).show();
                                break;
                            case U8Code.CODE_ADS_FAILED:
                                Toast.makeText(MainActivity.this, "广告加载失败", Toast.LENGTH_SHORT).show();
                                break;
                            default:
                                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onInitResult(InitResult result) {
                        // 此处可以不做处理
                    }

                    @Override
                    public void onLoginResult(String data) {
                        Log.d("U8SDK", "The sdk login result is " + data);
                        Toast.makeText(MainActivity.this, "SDK登录成功", Toast.LENGTH_SHORT).show();
                        //（写入自己相关逻辑）,data为各渠道登录成功后返回数据，字段不同，无法统一解析，一般这里不做处理
                    }

                    @Override
                    public void onSwitchAccount() {
                        Toast.makeText(MainActivity.this, "切换帐号成功", Toast.LENGTH_SHORT).show();
                        //（写入自己相关逻辑）
                    }

                    @Override
                    public void onSwitchAccount(String data) {
                        Toast.makeText(MainActivity.this, "切换帐号并登录成功", Toast.LENGTH_SHORT).show();
                        //（写入自己相关逻辑）
                    }

                    @Override
                    public void onLogout() {
                        Toast.makeText(MainActivity.this, "退出帐号成功", Toast.LENGTH_SHORT).show();
                        //（写入自己相关逻辑）
                    }

                    @Override
                    public void onAuthResult(UToken authResult) {
                        //登录成功，由myserver服务器统一各渠道用户信息后返回
                        if (authResult.isSuc()) {
                            String token = authResult.getToken();
                            String sdkUserId = authResult.getSdkUserID();
                            String sdkUserName = authResult.getSdkUsername();
                            int userId = authResult.getUserID();
                            String userName = authResult.getUsername();
                            String extension = authResult.getExtension();

                            /**
                             * TODO 1.如果游戏方不需要和myserver服务器交互，只需获取用户信息，登录验证可不处理
                             * 2.去myserver服务器验证登录
                             * 3.验证地址是我方提供，地址读取方式由接入方自行决定，可写死，可从AndroidManifest.xml读取
                             */


                            // TODO 游戏方使用自己的方式进行网络请求，下面是例子
//                            String loginUrl = SDKTools.getMetaData(MainActivity.this, "U8_LOGIN_GAME_URL");
//                            Log.d("U8SDK", "the login url is " + loginUrl);

                        } else {
                            Toast.makeText(MainActivity.this, "获取Token失败", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onPayResult(PayResult result) {
                        Toast.makeText(MainActivity.this, "支付成功,商品Id:" + result.getProductID(), Toast.LENGTH_SHORT).show();
                        //此处可以不做处理，以游戏服务器收到结果为准
                        //如果游戏客户端发货，又需要支付成功后的商品信息，可特殊说明在这里处理
                    }
                });
            }
        });
    }

    //必接
    //登录
    public void login() {
        U8SDK.getInstance().runOnMainThread(new Runnable() {
            @Override
            public void run() {
                U8User.getInstance().login();
            }
        });
    }

    //先调用登出，在调用切换账号
    public void switchLogin() {
        U8SDK.getInstance().runOnMainThread(new Runnable() {
            @Override
            public void run() {
                U8User.getInstance().switchLogin();
            }
        });
    }

    //必接
    //账号登出
    public void logout() {
        U8SDK.getInstance().runOnMainThread(new Runnable() {
            @Override
            public void run() {
                U8User.getInstance().logout();
            }
        });
    }

    //必接
    //显示客服中心
    public void showCallCenter() {
        U8SDK.getInstance().runOnMainThread(new Runnable() {
            @Override
            public void run() {
                U8User.getInstance().showCallCenter();
            }
        });
    }

    //必接
    //显示隐私政策
    public void showPrivacyPolicy() {
        U8SDK.getInstance().runOnMainThread(new Runnable() {
            @Override
            public void run() {
                U8User.getInstance().showPrivacyPolicy();
            }
        });
    }

    //跳转超休闲专区（OPPO）
    public void jumpLeisureSubject() {
        U8SDK.getInstance().runOnMainThread(new Runnable() {
            @Override
            public void run() {
                U8User.getInstance().jumpLeisureSubject();
            }
        });
    }

    //必接
    //支付(无内购不接)
    public void pay() {
        U8SDK.getInstance().runOnMainThread(new Runnable() {
            @Override
            public void run() {
                PayParams params = new PayParams();
                params.setBuyNum(1);
                params.setCoinNum(100);
                params.setExtension(System.currentTimeMillis() + "");
                params.setPrice(1);
                params.setProductId("1");
                params.setProductName("元宝");
                params.setProductDesc("购买100元宝");
                params.setRoleId("1");
                params.setRoleLevel(1);
                params.setRoleName("测试角色名");
                params.setServerId("10");
                params.setServerName("测试");
                params.setVip("vip1");

                /**
                 * TODO 1.如果游戏方不需要和myserver服务器交互，即客户端发货，订单号可由接入方自行决定19位随机码（字母+数字）
                 * 2.去myserver服务器请求订单号，如果通过服务器发货，必须获取服务器订单号
                 * 3.获取订单号地址是我方提供，地址读取方式由接入方自行决定，可写死，可从AndroidManifest.xml读取
                 */

                // TODO 游戏方使用自己的方式进行网络请求，下面是例子
//                String orderUrl = SDKTools.getMetaData(MainActivity.this, "U8_ORDER_URL");
//                Log.d("U8SDK", "the order url is " + orderUrl);


                params.setOrderID("orderId");
                //调用支付接口
                U8Pay.getInstance().pay(params);
            }
        });
    }

    //必接
    //提交扩展数据
    public void submitExtraData(final int dataType) {
        UserExtraData data = new UserExtraData();
        data.setDataType(dataType);
        data.setMoneyNum(100);
        data.setRoleCreateTime(System.currentTimeMillis() / 1000);
        data.setRoleID("role_100");
        data.setRoleName("test_112");
        data.setRoleLevel("10");
        data.setRoleLevelUpTime(System.currentTimeMillis() / 1000);
        data.setServerID(10);
        data.setServerName("server_10");
        U8User.getInstance().submitExtraData(data);
    }

    //必接
    public void exit() {
        if (isSupportExit()) {
            U8SDK.getInstance().runOnMainThread(new Runnable() {

                @Override
                public void run() {
                    U8User.getInstance().exit();
                }
            });
        } else {
            //游戏自己的退出确认框
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("退出确认");
            builder.setMessage("主公，现在还早，要不要再玩一会？");
            builder.setCancelable(true);
            builder.setPositiveButton("好吧",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,
                                            int whichButton) {
                            //这里什么都不用做
                        }
                    });
            builder.setNeutralButton("一会再玩",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,
                                            int whichButton) {
                            //退出游戏
                            MainActivity.this.finish();
                            System.exit(0);
                        }
                    });
            builder.show();
        }
    }

    //是否有渠道退出页
    public boolean isSupportExit() {
        return U8User.getInstance().isSupport("exit");
    }


    //必接
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        U8SDK.getInstance().onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    //必接
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        U8SDK.getInstance().onRequestPermissionResult(requestCode, permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    //必接
    @Override
    public void onStart() {
        U8SDK.getInstance().onStart();
        super.onStart();
    }

    //必接
    @Override
    public void onPause() {
        U8SDK.getInstance().onPause();
        super.onPause();
    }

    //必接
    @Override
    public void onResume() {
        U8SDK.getInstance().onResume();
        super.onResume();
    }

    //必接
    @Override
    public void onNewIntent(Intent newIntent) {
        U8SDK.getInstance().onNewIntent(newIntent);
        super.onNewIntent(newIntent);
    }

    //必接
    @Override
    public void onStop() {
        U8SDK.getInstance().onStop();
        super.onStop();
    }

    //必接
    @Override
    public void onDestroy() {
        U8SDK.getInstance().onDestroy();
        super.onDestroy();
    }

    //必接
    @Override
    public void onRestart() {
        U8SDK.getInstance().onRestart();
        super.onRestart();
    }

    //必接
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        U8SDK.getInstance().onConfigurationChanged(newConfig);
        super.onConfigurationChanged(newConfig);
    }

    //如果是unity开发的游戏，需要把onBackPressed和onKeyDown注释掉
    @Override
    public void onBackPressed() {
        Log.d("U8SDK", "OnBackPressed.");
        U8SDK.getInstance().onBackPressed();
        if (U8User.getInstance().isSupport("exit")) {
            U8User.getInstance().exit();
        } else {
            this.finish();
            System.exit(0);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d("U8SDK", "OnKeyDown:" + keyCode);
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (U8User.getInstance().isSupport("exit")) {
                U8User.getInstance().exit();
            } else {
                this.finish();
                System.exit(0);
            }
        }
        return true;
    }

    //选接
    //分享
    public void share() {
        U8SDK.getInstance().runOnMainThread(new Runnable() {
            @Override
            public void run() {
                ShareParams params = new ShareParams();
                params.setTitle("分享的标题");//必须，最大30个字符
                params.setContent("内容");//必须，最大130个字符
                params.setImgUrl("分享的本地图片地址");//必须
                params.setTitleUrl("标题链接");
                params.setSourceName("分享此内容显示的出处名称");
                params.setSourceUrl("出处链接");
                params.setUrl("链接，微信分享使用");
                params.setDialogMode(false);//是否全屏还是对话框
                params.setNotifyIcon(0);//Notification的图标
                params.setNotifyIconText("Notification的文字");
                params.setComment("内容的评论");//人人网分享必须参数，不能为空

                U8Share.getInstance().share(params);
            }
        });
    }

    //渠道是否有激励广告,参数为对应方法名
    public boolean isSupportRewardAd() {
        return U8Ads.getInstance().isSupport("rewardAd");
    }

    //选接
    //广告,不同渠道接入广告类型不同，新加判断，判断后再调用具体广告显示
    public void ad() {
        U8SDK.getInstance().runOnMainThread(new Runnable() {
            @Override
            public void run() {
                /**
                 * 可选模式，
                 * 激励视频广告 U8Ads.getInstance().rewardAd(String posId);
                 * banner广告 U8Ads.getInstance().bannerAd(String posId);
                 * 插屏广告  U8Ads.getInstance().insertAd(String posId);
                 * 开屏广告  U8Ads.getInstance().splashAd(String posId);
                 * 原生广告  U8Ads.getInstance().nativeAd(String posId,U8AdParams adParams);
                 */

                //原生广告
                String posId = "test-posid";//广告位Id

                /**
                 * TODO 1.如果游戏方不需要和myserver服务器交互，posId可为空
                 * 2.去myserver服务器获取广告位id
                 * 3.获取广告位id地址是我方提供，地址读取方式由接入方自行决定，可写死，可从AndroidManifest.xml读取
                 */

                // TODO 游戏方使用自己的方式进行网络请求
//                String posIdUrl = SDKTools.getMetaData(MainActivity.this, "U8_POSID_URL");
//               Log.d("U8SDK", "the getPosId url is " + posIdUrl);


                U8AdParams adParams = new U8AdParams(false, 400, 210, 100, 50);
                U8Ads.getInstance().nativeAd(posId, adParams);
            }
        });
    }

    //选接
    //广告,原生广告关闭按钮，只有调用原生广告，并设置isClose = false才生效
    public void closeNativeAd() {
        U8SDK.getInstance().runOnMainThread(new Runnable() {
            @Override
            public void run() {
                U8Ads.getInstance().nativeClose();
            }
        });
    }
}
