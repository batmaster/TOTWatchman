package com.tot.totwatchman;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.zxing.client.android.CaptureActivity;

import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.text.Editable;
import android.text.TextWatcher;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnScrollChangedListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.TextView;

public class ScanFragment extends Fragment {

    private static final int REQUEST_QR_SCAN = 12888;
    private static final int REQUEST_LOCATION_SETTING_FORCE_CLOSE = 12889;
    private static final int REQUEST_MOCK_SETTING_FORCE_CLOSE = 12890;
    private static final int REQUEST_LOCATION_SETTING = 12891;
    private static final int REQUEST_MOCK_SETTING = 12892;
	
    private EditText editTextId;
    private EditText editTextName;
    private EditText editTextLocation;
    private EditText editTextGPS;
	private Button buttonScanner;
	private Button buttonCheckin;
	
	private String code;
	private GPSTracker gpsTracker;
	
	private SwipeRefreshLayout swipeRefreshLayout;
	
	
	public ScanFragment() {
		
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_scan, container, false);
		
		editTextId = (EditText) view.findViewById(R.id.editTextId);
		editTextId.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (editTextId.getText().toString().length() == 4) {
					CheckIdTask task = new CheckIdTask(editTextId.getText().toString());
					task.execute();
				} else {
					editTextName.setHint("ชื่อ");
					buttonCheckin.setVisibility(View.GONE);
				}
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				
			}
		});
		
		editTextName = (EditText) view.findViewById(R.id.editTextName);
		editTextLocation = (EditText) view.findViewById(R.id.editTextLocation);
		
		editTextGPS = (EditText) view.findViewById(R.id.editTextGPS);
		
		buttonScanner = (Button) view.findViewById(R.id.buttonScanner);
		buttonScanner.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity().getApplicationContext(), CaptureActivity.class);
				getActivity().startActivityForResult(intent, REQUEST_QR_SCAN);
			}
		});
		
		buttonCheckin = (Button) view.findViewById(R.id.buttonCheckin);
		buttonCheckin.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				gpsTracker = new GPSTracker(getActivity().getApplicationContext());
				checkGPS(false);
			}
		});
		
		gpsTracker = new GPSTracker(getActivity().getApplicationContext());
		checkGPS(true);
		
		return view;
	}
	
	private double la;
	private double lo;
	
	private void checkGPS(final boolean forceClose) {
		if (!gpsTracker.canGetLocation()) {
			AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
			dialog.setTitle("ระบบหาตำแหน่งถูกปิด");
			dialog.setMessage("เปิดการใช้งาน \"ตำแหน่ง\" หรือ \"Location\" เพื่อทำการเช็คอิน");
			dialog.setPositiveButton("ตั้งค่า", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
					getActivity().startActivityForResult(intent, forceClose ? REQUEST_LOCATION_SETTING_FORCE_CLOSE : REQUEST_LOCATION_SETTING);
					dialog.dismiss();
				}
			});
			dialog.setNegativeButton("ยกเลิก", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (forceClose)
						getActivity().finish();
				}
			});
			dialog.setIcon(android.R.drawable.ic_menu_manage);
			dialog.show();
		}
		else {
			checkMock(forceClose);
		}
	}
	
	private void checkMock(final boolean forceClose) {
		if (isMockSettingsON()) {
			AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
			dialog.setTitle("ตรวจพบการเปิดระบบตำแหน่งจำลอง");
			dialog.setMessage("ปิด \"อนุญาตตำแหน่งจำลอง\" หรือ \"Allow mock locations\" เพื่อทำการเช็คอิน");
			dialog.setPositiveButton("ตั้งค่า", new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int which) { 
		        	Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS);
					getActivity().startActivityForResult(intent, forceClose ? REQUEST_MOCK_SETTING_FORCE_CLOSE : REQUEST_MOCK_SETTING);
					dialog.dismiss();
		        }
		     });
			dialog.setNegativeButton("ยกเลิก", new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int which) {
		        	if (forceClose)
		        		getActivity().finish();
		        }
			});
			dialog.setIcon(android.R.drawable.ic_menu_manage);
			dialog.show();
		}
		else {
			la = gpsTracker.getLatitude();
			lo = gpsTracker.getLongitude();
			if (forceClose)
				editTextGPS.setText(la + " , " + lo);
			else {
				CheckInTask task = new CheckInTask();
				task.execute();
			}
		}
	}
	
	private boolean isMockSettingsON() {
		return !Settings.Secure.getString(getActivity().getApplicationContext().getContentResolver(), Settings.Secure.ALLOW_MOCK_LOCATION).equals("0");
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == REQUEST_QR_SCAN && resultCode == Activity.RESULT_OK) {
			code = intent.getStringExtra("CONTENT");
			
			CheckLocationTask task = new CheckLocationTask(code);
			task.execute();
		} else if (requestCode == REQUEST_QR_SCAN && resultCode == Activity.RESULT_CANCELED) {
			
		}
		else if (requestCode == REQUEST_LOCATION_SETTING_FORCE_CLOSE) {
			gpsTracker = new GPSTracker(getActivity().getApplicationContext());
			checkGPS(true);
		}
		else if (requestCode == REQUEST_MOCK_SETTING_FORCE_CLOSE) {
			checkMock(true);
		}
		else if (requestCode == REQUEST_LOCATION_SETTING) {
			gpsTracker = new GPSTracker(getActivity().getApplicationContext());
			checkGPS(true);
		}
		else if (requestCode == REQUEST_MOCK_SETTING) {
			checkMock(true);
		}
	};
	
	private class CheckIdTask extends AsyncTask<String, Integer, String> {
		
		private String id;
		private String name;
		
		public CheckIdTask(String id) {
			this.id = id;
			name = "";
		}
		
		@Override
		protected String doInBackground(String[] params) {
			
			try {
				name = Parser.parse(Request.getName(id));
			} catch (ConnectTimeoutException e) {
				name = "";
			} catch (SocketTimeoutException e) {
				name = "";
			} catch (HttpHostConnectException e) {
				name = "";
			}
			return "some message";
		}
		
		@Override
		protected void onPostExecute(String message) {
			Log.d("sql", name);
			try {
				JSONArray js = new JSONArray(name);
				for (int i = 0; i < js.length(); i++) {
					JSONObject jo = js.getJSONObject(i);
					name = jo.getString("name");
				}
			} catch (JSONException e) {
				name = "";
				e.printStackTrace();
			}
			
			if (!name.equals("")) {
				if (name.equals("[]"))
					name = "ไม่พบชื่อ";
				editTextName.setHint(name);

				if (!editTextLocation.getHint().equals("สถานที่") && !editTextLocation.getHint().equals("ไม่พบสถานที่") && !editTextName.getHint().equals("ชื่อ") && !editTextName.getHint().equals("ไม่พบชื่อ"))
					buttonCheckin.setVisibility(View.VISIBLE);
				else
					buttonCheckin.setVisibility(View.GONE);
			}
			else {
				
			}
		}
		
		public AsyncTask<String, Integer, String> execute() {
			return execute("test");
		}
	}
	
	private class CheckLocationTask extends AsyncTask<String, Integer, String> {

		private String code;
		private String location;
		
		private ProgressDialog loading;
		
		public CheckLocationTask(String code) {
			this.code = code;
			location = "";
			
			loading = new ProgressDialog(getActivity());
			loading.setTitle("ตรวจสอบสถานที่");
			loading.setMessage("กำลังโหลด...");
//			loading.setCancelable(false);
			loading.setProgressStyle(ProgressDialog.STYLE_SPINNER); 
		}
		
		@Override
		protected String doInBackground(String[] params) {
			
			try {
				location = Parser.parse(Request.getLocation(code));
			} catch (ConnectTimeoutException e) {
				loading.setMessage("เชื่อมต่อเซิร์ฟนานเกินไป");
				location = "";
			} catch (SocketTimeoutException e) {
				loading.setMessage("รอผลตอบกลับนานเกินไป");
				location = "";
			} catch (HttpHostConnectException e) {
				loading.setMessage("เชื่อมต่อเซิร์ฟเวอร์ไม่ได้");
				location = "";
			}
			return "some message";
		}
		
		@Override
		protected void onPreExecute() {
			loading.show();
			super.onPreExecute();
		}

		@Override
		protected void onPostExecute(String message) {
			loading.dismiss();
			
			try {
				JSONArray js = new JSONArray(location);
				for (int i = 0; i < js.length(); i++) {
					JSONObject jo = js.getJSONObject(i);
					location = jo.getString("name_lo");
				}
			} catch (JSONException e) {
				location = "";
				e.printStackTrace();
			}
			
			if (!location.equals("")) {
				if (location.equals("[]"))
					location = "ไม่พบสถานที่";
				editTextLocation.setHint(location);
				
				if (!editTextLocation.getHint().equals("สถานที่") && !editTextLocation.getHint().equals("ไม่พบสถานที่") && !editTextName.getHint().equals("ชื่อ") && !editTextName.getHint().equals("ไม่พบชื่อ"))
					buttonCheckin.setVisibility(View.VISIBLE);
				else
					buttonCheckin.setVisibility(View.GONE);
			}
			else {
				
			}
		}
		
		public AsyncTask<String, Integer, String> execute() {
			return execute("test");
		}
	}
	
	private class CheckInTask extends AsyncTask<String, Integer, String> {

		private ProgressDialog loading;
		
		private boolean checked;
		private boolean error;
		
		public CheckInTask() {
			loading = new ProgressDialog(getActivity());
			loading.setTitle("เช็คอิน");
			loading.setMessage("กำลังส่งข้อมูล...");
//			loading.setCancelable(false);
			loading.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			
			checked = false;
			error = false;
		}
		
		@Override
		protected String doInBackground(String[] params) {
			try {
				checked = Request.checkin(editTextId.getText().toString(), code, la, lo);
			} catch (ConnectTimeoutException e) {
				loading.setMessage("เชื่อมต่อเซิร์ฟนานเกินไป");
				error = true;
			} catch (SocketTimeoutException e) {
				loading.setMessage("รอผลตอบกลับนานเกินไป");
				error = true;
			} catch (HttpHostConnectException e) {
				loading.setMessage("เชื่อมต่อเซิร์ฟเวอร์ไม่ได้");
				error = true;
			}
		    return "some text";
		}
		
		@Override
		protected void onPreExecute() {
			loading.show();
			super.onPreExecute();
		}

		@Override
		protected void onPostExecute(String message) {
			loading.dismiss();

			final Dialog dialog = new Dialog(getActivity());
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.about_dialog);
            dialog.setCancelable(true);
			
			if (checked) {	            
	            TextView textViewCurrentVersionName = (TextView) dialog.findViewById(R.id.textViewCurrentVersionName);
            	textViewCurrentVersionName.setText("เช็คอินเสร็จสิ้น");
            	
            	TextView textViewNewVersionName = (TextView) dialog.findViewById(R.id.textViewNewVersionName);
            	textViewNewVersionName.setVisibility(View.GONE);          	
			}
			else {	            
	            TextView textViewCurrentVersionName = (TextView) dialog.findViewById(R.id.textViewCurrentVersionName);
	            if (error) {
	            	textViewCurrentVersionName.setText("ไม่สามารถเช็คอิน");
	            	
	            	TextView textViewNewVersionName = (TextView) dialog.findViewById(R.id.textViewNewVersionName);
	            	textViewNewVersionName.setText("มีปัญหาการเชื่อมต่อ");
	            }
	            else {
	            	textViewCurrentVersionName.setText("ไม่สามารถเช็คอินซ้ำสถานที่นี้ได้ภายในเวลา 1 ชั่วโมง");
	            	AsyncTask<String, Integer, String> task = new AsyncTask<String, Integer, String>() {
	            		
	            		private String lastDateString;
	            		private String lastCheck;
						@Override
						protected String doInBackground(String... params) {
							lastDateString = "";
							
							try {
								lastDateString = Parser.parse(Request.getLastCheck(editTextId.getText().toString(), code));
								
							} catch (HttpHostConnectException e) {
								e.printStackTrace();
							} catch (ConnectTimeoutException e) {
								e.printStackTrace();
							} catch (SocketTimeoutException e) {
								e.printStackTrace();
							}
							
							return lastDateString;
						}
						@Override
						protected void onPostExecute(String result) {
							super.onPostExecute(result);
							
							TextView textViewNewVersionName = (TextView) dialog.findViewById(R.id.textViewNewVersionName);
							textViewNewVersionName.setText("เช็คอินล่าสุด: " + lastDateString);
						}
	            	};
	            	task.execute("");
	            }
			}

        	Button buttonUpdate = (Button) dialog.findViewById(R.id.buttonUpdate);
        	buttonUpdate.setVisibility(View.GONE);
			
			dialog.show();
		}
		
		public AsyncTask<String, Integer, String> execute() {
			return execute("test");
		}
	}
}
