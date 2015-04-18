package com.nielsen.cloudapi.model;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

public class CloudAPI {
	//Note: This is where all API calls are currently located. You are free to change the architecture as you please
	//Error handling is not covered here, and you will have to implement that yourself
	
	public static String request = "http://sandbox.cloudapi.nielsen.com/nmapi/v1/";
	public static String appId = "FHG163HR-BH45-JKY6-BKH7-67GJKY68GJK8";
	public static String sessionId;
	public static int sequence = 2;
	
	public static String sessionInit(String app_ID, String adId, String appName) throws JSONException{
		appId = app_ID;
		
		String location = "";

		try{
			URL url = new URL ("http://sandbox.cloudapi.nielsen.com/nmapi/v1/"+appId+"/sessions/");
		
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
		
			con.setDoOutput(true);
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type",  "application/json");
			
			OutputStreamWriter writer = new OutputStreamWriter(
		            con.getOutputStream());
			
			JSONObject jsonParam = new JSONObject();
			jsonParam.put("sequence", "0");
			jsonParam.put("apn", appName);
			jsonParam.put("appver", "v1.0");
			jsonParam.put("osver", "Android");
			jsonParam.put("devtype", "tablet");
			jsonParam.put("deviceId", adId);
			jsonParam.put("collection", "true");
			
		    writer.write(jsonParam.toString());
		    writer.close();
			
			int responseCode = con.getResponseCode();
			
			if(responseCode == 201){
				location = con.getHeaderField("Location");
				sessionId = location.substring(location.indexOf("sessions/")+9);
			}

			return sessionId;
		} catch (IOException e){
			return "error";
		}
	}
	
	public static int loadMetadata() {
		try{
			URL url = new URL ("http://sandbox.cloudapi.nielsen.com/nmapi/v1/"+appId+"/sessions/"+sessionId);
		
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
		
			con.setDoOutput(true);
			con.setRequestMethod("PUT");
			con.setRequestProperty("Content-Type",  "application/json");
			
			OutputStreamWriter writer = new OutputStreamWriter(
		            con.getOutputStream());
			
			JSONObject jsonParam = new JSONObject();
			jsonParam.put("sequence", "1");
			
			JSONObject jsonData = new JSONObject();
			jsonData.put("type", "content");
			jsonData.put("assetid", "12345");
			jsonData.put("adModel", "0");
			jsonData.put("dataSrc", "id3");
			
			jsonParam.put("data", jsonData);
			jsonParam.put("event", "loadMetadata");

			
		    writer.write(jsonParam.toString());
		    writer.close();
			
			int responseCode = con.getResponseCode();
		
			return responseCode;
			
		} catch (IOException e){
			return 0;
		} catch (JSONException e){
			return 0;
		}
	}
	
	public static int sendId3(String id3String) {
		try{
			URL url = new URL ("http://sandbox.cloudapi.nielsen.com/nmapi/v1/"+appId+"/sessions/"+sessionId);
		
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
		
			con.setDoOutput(true);
			con.setRequestMethod("PUT");
			con.setRequestProperty("Content-Type",  "application/json");
			
			OutputStreamWriter writer = new OutputStreamWriter(
		            con.getOutputStream());
			
			JSONObject jsonParam = new JSONObject();
			jsonParam.put("sequence", sequence++);
			jsonParam.put("event", "sendId3");
			jsonParam.put("data", id3String);
			
		    writer.write(jsonParam.toString());
		    writer.close();
			
			int responseCode = con.getResponseCode();
		
			return responseCode;
			
		} catch (IOException e){
			return 0;
		} catch (JSONException e){
			return 0;
		}
	}
	
	public static int stopId3(){
		try{
			URL url = new URL ("http://sandbox.cloudapi.nielsen.com/nmapi/v1/"+appId+"/sessions/"+sessionId);
		
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
		
			con.setDoOutput(true);
			con.setRequestMethod("PUT");
			con.setRequestProperty("Content-Type",  "application/json");
			
			OutputStreamWriter writer = new OutputStreamWriter(
		            con.getOutputStream());
			
			JSONObject jsonParam = new JSONObject();
			jsonParam.put("sequence", sequence++);
			jsonParam.put("event", "stop");
			jsonParam.put("data", "0");
			
		    writer.write(jsonParam.toString());
		    writer.close();
			
			int responseCode = con.getResponseCode();
		
			return responseCode;
			
		} catch (IOException e){
			return 0;
		} catch (JSONException e){
			return 0;
		}
	}
	
	public static void sessionEnd(){
		try{
			//Note: DELETE requests do not support writing out, so the parameters are url encoded after the ?
			URL url = new URL ("http://sandbox.cloudapi.nielsen.com/nmapi/v1/"+appId+"/sessions/"+sessionId+"?sequence="+sequence+"&event=end");
		
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
		
			//con.setDoOutput(true);
			con.setRequestMethod("DELETE");
			con.setRequestProperty("Content-Type",  "application/json");
			
			int responseCode = con.getResponseCode();

		} catch (IOException e){
			
		}
	}
}
