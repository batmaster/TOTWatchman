package com.tot.totwatchman;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.format.DateFormat;
import android.util.Log;

public class Request {
	
	private static final String REQ_GET_GUARD_NAME = "SELECT name FROM guard WHERE id_gard = %s";
	private static final String REQ_GET_LOCATION_NAME = "SELECT name_lo FROM location WHERE qrcode = %s";
	
	private static final String REQ_GET_LAST_CHECK_TIME = "SELECT CONCAT(dates, ' ', times) as date FROM timcheck WHERE idguard = %s AND area = %s ORDER BY id_th DESC LIMIT 1";
	private static final String REQ_CHECKIN = "INSERT INTO timcheck (idguard, area, dates, times) VALUES (%s, %s, '%s', '%s')";
	
	private static final String REQ_GET_CHECKIN_LIST = "SELECT t.*, l.name_lo as area_name FROM timcheck t, location l WHERE t.idguard = %s AND t.area = l.id_co ORDER BY t.id_th DESC LIMIT 50";
	
	private Request() {
		
	}
	
	public static String getName(String guardId) throws HttpHostConnectException, ConnectTimeoutException, SocketTimeoutException {
		return request(String.format(REQ_GET_GUARD_NAME, guardId));
	}
	
	public static String getLocation(String qrCode) throws HttpHostConnectException, ConnectTimeoutException, SocketTimeoutException {
		return request(String.format(REQ_GET_LOCATION_NAME, qrCode));
	}
	
	public static String getLastCheck(String guardId, String qrCode) throws HttpHostConnectException, ConnectTimeoutException, SocketTimeoutException {
		String last = Parser.parse(request(String.format(REQ_GET_LAST_CHECK_TIME, guardId, qrCode)));
		
		if (!last.equals("[]")) {
			try {
				JSONArray js = new JSONArray(last);
				JSONObject jo = js.getJSONObject(0);

				return jo.getString("date");
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return "";
	}
	
	public static boolean checkin(String guardId, String qrCode) throws HttpHostConnectException, ConnectTimeoutException, SocketTimeoutException {
		String lastDateString = Parser.parse(getLastCheck(guardId, qrCode));
		SimpleDateFormat format = new SimpleDateFormat("[yyyy-MM-dd hh:mm:ss]");
		
		try {
			if (!lastDateString.equals("[]")) {
				Date lastDate = format.parse(lastDateString);
				long diffInHour = ((new Date()).getTime() - lastDate.getTime()) / (60 * 60 * 1000);
				
				if (diffInHour < 1)
					return false;
			}
			
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd");
			SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
			Date current = new Date();
			
			request(String.format(REQ_CHECKIN, guardId, qrCode, dateFormat.format(current), timeFormat.format(current)));
		} catch (ParseException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	public static String getList(String guardId) throws HttpHostConnectException, ConnectTimeoutException, SocketTimeoutException {
		return request(String.format(REQ_GET_CHECKIN_LIST, guardId));
	}
	
	public static String request(String str) throws org.apache.http.conn.ConnectTimeoutException, java.net.SocketTimeoutException, org.apache.http.conn.HttpHostConnectException {
		Log.d("sql", str);
		str = str.replace("'", "xxaxx");
		try {
			HttpParams httpParameters = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParameters, 10000);
			HttpConnectionParams.setSoTimeout(httpParameters, 10000);

			HttpClient httpClient = new DefaultHttpClient(httpParameters);
			HttpPost httpPost = new HttpPost(SharedValues.HOST_DB);
	
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("sql", str));
			
			httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
	
			HttpResponse httpResponse = httpClient.execute(httpPost);
			HttpEntity entity = httpResponse.getEntity();
	
			if (entity != null) {
				InputStream is = entity.getContent();
				StringBuffer sb = new StringBuffer();
				String line = null;
	
				BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
				while ((line = reader.readLine()) != null) {
					sb.append(line + "!!!");
				}
				reader.close();
	
				return sb.toString();
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return "";
	}
	
	public static String getVersion() throws ConnectTimeoutException, SocketTimeoutException, HttpHostConnectException {
		try {
			HttpParams httpParameters = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParameters, 10000);
			HttpConnectionParams.setSoTimeout(httpParameters, 10000);

			HttpClient httpClient = new DefaultHttpClient(httpParameters);
			HttpPost httpPost = new HttpPost(SharedValues.HOST_VERSION);
	
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("version", ""));
			
			httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
	
			HttpResponse httpResponse = httpClient.execute(httpPost);
			HttpEntity entity = httpResponse.getEntity();
	
			if (entity != null) {
				InputStream is = entity.getContent();
				StringBuffer sb = new StringBuffer();
				String line = null;
	
				BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
				while ((line = reader.readLine()) != null) {
					sb.append(line + ",");
				}
				reader.close();
	
				return sb.toString();
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return "";
	}
}
