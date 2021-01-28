package com.u8.sdk.plugin;

import com.u8.sdk.IPay;
import com.u8.sdk.PayParams;
import com.u8.sdk.PluginFactory;
import com.u8.sdk.impl.SimpleDefaultPay;

/***
 * 支付插件
 * @author xiaohei
 *
 */
public class U8Pay{
	
	private static U8Pay instance;
	
	private IPay payPlugin;
	
	private U8Pay(){
		
	}
	
	public static U8Pay getInstance(){
		if(instance == null){
			instance = new U8Pay();
		}
		return instance;
	}
	
	public void init(){
		this.payPlugin = (IPay)PluginFactory.getInstance().initPlugin(IPay.PLUGIN_TYPE);
		if(this.payPlugin == null){
			this.payPlugin = new SimpleDefaultPay();
		}
	}
	
	public boolean isSupport(String method){
		if(this.payPlugin == null){
			return false;
		}
		
		return this.payPlugin.isSupportMethod(method);
	}
	
	/***
	 * 支付接口（弹出支付界面）
	 * @param data
	 */
	public void pay(PayParams data){
		if(this.payPlugin == null){
			return;
		}
		this.payPlugin.pay(data);
	}
}
