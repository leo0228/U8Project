package com.u8.sdk;

/**
 * 初始化结果
 * @author xiaohei
 *
 */
public class InitResult {


	public InitResult(){
		
	}
	
	public InitResult(boolean isSDKExit){
		this.isSDKExit = isSDKExit;
	}
	
	public InitResult(boolean isSDKExist, String ext){
		this.isSDKExit = isSDKExist;
		this.extension = ext;
	}
	
	private boolean isSDKExit;			//SDK是否含有退出确认界面
	private String extension;			//扩展数据项
	
	public boolean isSDKExit() {
		return isSDKExit;
	}
	public void setSDKExit(boolean isSDKExit) {
		this.isSDKExit = isSDKExit;
	}
	public String getExtension() {
		return extension;
	}
	public void setExtension(String extension) {
		this.extension = extension;
	}
	
	
}
