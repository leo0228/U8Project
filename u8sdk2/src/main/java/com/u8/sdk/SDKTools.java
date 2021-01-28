package com.u8.sdk;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.u8.sdk.log.Log;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.text.TextUtils;

public class SDKTools {
	
	/**
	 * 获取mac地址
	 * @param activity
	 * @return
	 */
	public static String getMacAddress(Activity activity)
	{
	    WifiManager localWifiManager = (WifiManager)activity.getSystemService("wifi");
	    WifiInfo localWifiInfo = localWifiManager == null ? null : localWifiManager.getConnectionInfo();
	    if (localWifiInfo != null)
	    {
	      String str = localWifiInfo.getMacAddress();
	      if ((str == null) || (str.equals(""))) {
	        str = "null";
	      }
	      return str;
	    }
	    return "null";
	}

	/***
	 * 网络是否可用
	 * @param activity
	 * @return
	 */
	public static boolean isNetworkAvailable(Context activity){
		try {
			ConnectivityManager connectivityManager = (ConnectivityManager)activity.getSystemService("connectivity");
			NetworkInfo localNetworkInfo = connectivityManager.getActiveNetworkInfo();
			return localNetworkInfo != null && localNetworkInfo.isAvailable();			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;

	}
	
	public static boolean isNullOrEmpty(String str){
		
		return str == null || str.trim().length() == 0;
	}	
	
	/***
	 * 获取assets目录下文件内容
	 * @param context
	 * @param assetsFile
	 * @return
	 */
	public static String getAssetConfigs(Context context, String assetsFile){
		InputStreamReader reader = null;
		BufferedReader br = null;
		try {
			reader = new InputStreamReader(context.getAssets().open(assetsFile));
			br = new BufferedReader(reader);
			
			StringBuilder sb = new StringBuilder();
			String line = null;
			while((line = br.readLine())!= null){
				sb.append(line);
			}
			
			return sb.toString();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if(br != null){
				try {
					br.close();
					br = null;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
			
			if(reader != null){
				try {
					reader.close();
					reader = null;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}
		
		return null;
		
	}
	
	/**
	 * 获取assets目录下面指定的.properties文件内容
	 * @param context
	 * @param assetsPropertiesFile
	 * @return
	 */
	public static Map<String, String> getAssetPropConfig(Context context, String assetsPropertiesFile){
		try {
			Properties pro = new Properties();
			pro.load(new InputStreamReader(context.getAssets().open(assetsPropertiesFile), "UTF-8"));
			
			Map<String, String> result = new HashMap<String, String>();
			
			for(Entry<Object, Object> entry : pro.entrySet()){
				String keyStr = entry.getKey().toString().trim();
				String keyVal = entry.getValue().toString().trim();
				if(!result.containsKey(keyStr)){
					result.put(keyStr, keyVal);
				}
			}
			
			return result;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	/***
	 * 获取Manifest.xml 中MetaData属性
	 * @param activity
	 * @param key
	 * @return
	 */
	public static String getMetaData(Context ctx, String key){
		try{
			ApplicationInfo appInfo = ctx.getPackageManager().getApplicationInfo(ctx.getPackageName(), PackageManager.GET_META_DATA);
			if(appInfo != null && appInfo.metaData != null && appInfo.metaData.containsKey(key)){
				
				return "" + appInfo.metaData.get(key);
			}else{
				Log.e("U8SDK", "The meta-data key is not exists."+key);
			}
		}catch(Exception e){
			e.printStackTrace();
		}

		return null;
	}
	
	public static void copyToClipBoard(final Activity activity,final String s){
		activity.runOnUiThread(new Runnable() {
			
			@SuppressLint("NewApi")
			@Override
			public void run() {
				android.text.ClipboardManager cmb = (android.text.ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
				cmb.setText(s);				
			}
		});
	}
	
	//收集设备信息
	public static Map<String, String> collectDeviceInfo(Context ctx){
		
		Map<String, String> info = new HashMap<String, String>();
		try{
			PackageManager pm = ctx.getPackageManager();
			PackageInfo p = pm.getPackageInfo(ctx.getPackageName(), PackageManager.GET_ACTIVITIES);
			if(p != null){
				String versionName = p.versionName == null ? "null" : p.versionName;
				String versionCode = p.versionCode + "";
				info.put("versionName", versionName);
				info.put("versionCode", versionCode);
			}
			
			Field[] fields = Build.class.getDeclaredFields();
			for(Field f : fields){
				try{
					f.setAccessible(true);
					Object obj = f.get(null);
					info.put(f.getName(), obj == null ? "null" : obj.toString());
				}catch(Exception e){
					e.printStackTrace();
				}
			}
			
		}catch(Exception e){
			Log.e("U8SDK", "an error occured when collect package info...");
			e.printStackTrace();
		}
		
		return info;
	}
	
	/***
	 * 获取剪贴板中的文本数据
	 * @param activity
	 * @return
	 */
	public static String getSystemKeyboard(final Activity activity){
		
		FutureTask<String> futureResult = new FutureTask<String>(new Callable<String>() {

			@SuppressLint("NewApi") 
			@Override
			public String call() throws Exception {
				android.text.ClipboardManager cmb = (android.text.ClipboardManager)activity.getSystemService(Activity.CLIPBOARD_SERVICE);
				if (cmb.hasText()){
					return cmb.getText().toString();
				}
				return "";
			}
			
		});
		
		activity.runOnUiThread(futureResult);
		
		try {
			return futureResult.get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return "";
	}	

	/**
	 * 有时候，我们需要根据一个SDK渠道包，去生成多个仅仅渠道号不同的包。但是使用的SDK还是相同的。那么这个时候
	 * 我们通常称这些包为逻辑渠道包，打包工具我们提供一个工具通过在META-INF/目录下生成空文件的形式来标识
	 * 逻辑渠道号
	 * @param context
	 * @param prefix
	 * @return
	 */
	public static String getLogicChannel(Context context, String prefix){
		ApplicationInfo appInfo = context.getApplicationInfo();
		String sourceDir = appInfo.sourceDir;
		String key = "META-INF/"+prefix;		
		ZipFile zip = null;
		try{
			zip = new ZipFile(sourceDir);
			Enumeration<?> entries = zip.entries();
			String ret = null;
			while(entries.hasMoreElements()){
				ZipEntry entry = (ZipEntry)entries.nextElement();
				String entryName = entry.getName();
				if(entryName.startsWith(key)){
					ret = entryName;
					break;				
				}
			}
			
			if(!TextUtils.isEmpty(ret)){
				
			    String[] split = ret.split("_");
			    if (split != null && split.length >= 2) {
			    	return ret.substring(split[0].length() + 1);
			    }				
			}
		}catch(Exception e){
			e.printStackTrace();
			
		}finally{
			if(zip != null){
				try{
					zip.close();
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
		
		return null;
	}	
}
