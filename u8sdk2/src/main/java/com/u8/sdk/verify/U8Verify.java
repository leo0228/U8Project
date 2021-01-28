package com.u8.sdk.verify;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;
import com.u8.sdk.log.Log;
import com.u8.sdk.U8SDK;
import com.u8.sdk.utils.EncryptUtils;
import com.u8.sdk.utils.U8HttpUtils;

public class U8Verify{

	/***
	 * 访问U8Server验证sid的合法性，同时获取U8Server返回的token，userID,sdkUserID信息
	 * @param result
	 * @return
	 */
	public static UToken auth(String result){
		
		try{
			Map<String, String> params = new HashMap<String, String>();
			params.put("appID", U8SDK.getInstance().getAppID()+"");
			params.put("channelID", "" + U8SDK.getInstance().getCurrChannel());
			params.put("extension", result);
			params.put("sdkVersionCode", U8SDK.getInstance().getSDKVersionCode());
			
            StringBuilder sb = new StringBuilder();
            sb.append("appID=").append(U8SDK.getInstance().getAppID()+"")
                    .append("channelID=").append(U8SDK.getInstance().getCurrChannel())
                    .append("extension=").append(result).append(U8SDK.getInstance().getAppKey());			
			
            String sign = EncryptUtils.md5(sb.toString()).toLowerCase();
            
            params.put("sign", sign);
            
			String authResult = U8HttpUtils.httpGet(U8SDK.getInstance().getAuthURL(), params);
			
			Log.d("U8SDK", "The sign is " + sign + " The auth result is "+authResult);
			
			return parseAuthResult(authResult);
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return new UToken();
		
	}
	
	
	private static UToken parseAuthResult(String authResult){
		
		if(authResult == null || TextUtils.isEmpty(authResult)){
			
			return new UToken();
		}
		
		try {
			JSONObject jsonObj = new JSONObject(authResult);
			int state = jsonObj.getInt("state");
			
			if(state != 1){
				Log.d("U8SDK", "auth failed. the state is "+ state);
				return new UToken();
			}
			
			JSONObject jsonData = jsonObj.getJSONObject("data");
			
			return new UToken(jsonData.getInt("userID")
					, jsonData.getString("sdkUserID")
					, jsonData.getString("username")
					, jsonData.getString("sdkUserName")
					, jsonData.getString("token")
					, jsonData.getString("extension"));
			
		} catch (JSONException e) {

			e.printStackTrace();
		}
		
		return new UToken();
	}
}
