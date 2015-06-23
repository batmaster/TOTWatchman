package com.tot.totwatchman;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;

import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.zxing.client.android.CaptureActivity;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private static final int REQUEST_QR_SCAN = 12888;
	
    private EditText editTextId;
    private EditText editTextName;
    private EditText editTextLocation;
	private Button buttonScanner;
	private Button buttonCheckin;
	private Button buttonHistory;
	
	private String code;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		editTextId = (EditText) findViewById(R.id.editTextId);
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
		
		editTextName = (EditText) findViewById(R.id.editTextName);
		editTextLocation = (EditText) findViewById(R.id.editTextLocation);
		
		buttonScanner = (Button) findViewById(R.id.buttonScanner);
		buttonScanner.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), CaptureActivity.class);
				startActivityForResult(intent, REQUEST_QR_SCAN);
			}
		});
		
		buttonCheckin = (Button) findViewById(R.id.buttonCheckin);
		buttonCheckin.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				CheckInTask task = new CheckInTask();
				task.execute();
			}
		});
		
		buttonHistory = (Button) findViewById(R.id.buttonHistory);
		buttonHistory.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(),	HistoryActivity.class);
				intent.putExtra("guardName", editTextName.getHint().toString());
				intent.putExtra("guardId", editTextId.getText().toString());
				startActivity(intent);
			}
		});
		
		File apk = new File(SharedValues.FILE_PATH, SharedValues.FILE_NAME);
		if (apk.exists())
			apk.delete();
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == REQUEST_QR_SCAN && resultCode == RESULT_OK) {
			code = intent.getStringExtra("CONTENT");
			
			CheckLocationTask task = new CheckLocationTask(code);
			task.execute();
		} else if (requestCode == REQUEST_QR_SCAN && resultCode == RESULT_CANCELED) {
			
		}
		
	};
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main_activity_actions, menu);
	    return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
        case R.id.logo:
        	CheckUpdateTask task = new CheckUpdateTask();
        	task.execute();
            return true;
        default:
            return super.onOptionsItemSelected(item);
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
				if (!editTextName.getHint().equals("ชื่อ") && !editTextName.getHint().equals("ไม่พบชื่อ")) {
					buttonHistory.setVisibility(View.VISIBLE);
					if (!editTextLocation.getHint().equals("สถานที่") && !editTextLocation.getHint().equals("ไม่พบสถานที่"))
						buttonCheckin.setVisibility(View.VISIBLE);
					else
						buttonCheckin.setVisibility(View.GONE);
				}
				else {
					buttonHistory.setVisibility(View.GONE);
				}
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
			
			loading = new ProgressDialog(MainActivity.this);
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
	
	private class CheckUpdateTask extends AsyncTask<String, Integer, String> {

		private String version;
		
		private ProgressDialog loading;
		
		public CheckUpdateTask() {
			loading = new ProgressDialog(MainActivity.this);
			loading.setTitle("ตรวจสอบเวอร์ชันล่าสุด");
			loading.setMessage("กำลังโหลด...");
//			loading.setCancelable(false);
			loading.setProgressStyle(ProgressDialog.STYLE_SPINNER); 
		}
		
		@Override
		protected String doInBackground(String[] params) {
			
			try {
				version = Request.getVersion();
			} catch (ConnectTimeoutException e) {
				loading.setMessage("เชื่อมต่อเซิร์ฟนานเกินไป");
				version = "";
			} catch (SocketTimeoutException e) {
				loading.setMessage("รอผลตอบกลับนานเกินไป");
				version = "";
			} catch (HttpHostConnectException e) {
				loading.setMessage("เชื่อมต่อเซิร์ฟเวอร์ไม่ได้");
				version = "";
				e.printStackTrace();
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
			
        	final Dialog dialog = new Dialog(MainActivity.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.about_dialog);
            dialog.setCancelable(true);
            
            if (!version.equals("")) {
	            String currentVersionName = "";
				try {
					currentVersionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
				} catch (NameNotFoundException e) {
					e.printStackTrace();
				}
	            String versionName = version.split(",")[0];
	            
	            if (currentVersionName.equals(versionName)) {
	            	TextView textViewCurrentVersionName = (TextView) dialog.findViewById(R.id.textViewCurrentVersionName);
	            	textViewCurrentVersionName.setText("เวอร์ชันปัจจุบัน " + currentVersionName);
	            	
	            	TextView textViewNewVersionName = (TextView) dialog.findViewById(R.id.textViewNewVersionName);
	            	textViewNewVersionName.setText("เวอร์ชันล่าสุดแล้ว");
	
	            	Button buttonUpdate = (Button) dialog.findViewById(R.id.buttonUpdate);
	            	buttonUpdate.setVisibility(View.GONE);
	            }
	            else {
	            	TextView textViewCurrentVersionName = (TextView) dialog.findViewById(R.id.textViewCurrentVersionName);
	            	textViewCurrentVersionName.setText("เวอร์ชันปัจจุบัน " + currentVersionName);
	            	
	            	TextView textViewNewVersionName = (TextView) dialog.findViewById(R.id.textViewNewVersionName);
	            	textViewNewVersionName.setText("เวอร์ชันล่าสุด " + versionName);
	
	                Button buttonUpdate = (Button) dialog.findViewById(R.id.buttonUpdate);
	                buttonUpdate.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							LoadAPKTask task = new LoadAPKTask();
							task.execute();
						}
					});
	            }
            }
            else {
            	TextView textViewCurrentVersionName = (TextView) dialog.findViewById(R.id.textViewCurrentVersionName);
            	textViewCurrentVersionName.setText("เชื่อมต่อเซิร์ฟเวอร์ไม่ได้ 1");

            	TextView textViewNewVersionName = (TextView) dialog.findViewById(R.id.textViewNewVersionName);
            	textViewNewVersionName.setVisibility(View.GONE);

            	Button buttonUpdate = (Button) dialog.findViewById(R.id.buttonUpdate);
            	buttonUpdate.setVisibility(View.GONE);
            }
            
            dialog.show();
		}
		
		public AsyncTask<String, Integer, String> execute() {
			return execute("test");
		}
	}
	
	private class LoadAPKTask extends AsyncTask<String, Integer, String> {

		private ProgressDialog loading;
		
		public LoadAPKTask() {
			loading = new ProgressDialog(MainActivity.this);
			loading.setTitle("ดาวน์โหลดตัวติดตั้ง");
			loading.setMessage("กำลังโหลด...");
//			loading.setCancelable(false);
			loading.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		}
		
		@Override
		protected String doInBackground(String[] params) {
			try {
				URL url = new URL(SharedValues.HOST_APK);
				URLConnection connection = url.openConnection();
				connection.connect();
				
				int fileLength = connection.getContentLength();
				
				File dir = new File(SharedValues.FILE_PATH);
				dir.mkdirs();
				
				InputStream input = new BufferedInputStream(url.openStream());
				OutputStream output = new FileOutputStream(SharedValues.FILE_PATH + SharedValues.FILE_NAME);
				
				byte data[] = new byte[1024];
				long total = 0;
				int count;
				while ((count = input.read(data)) != -1) {
					total += count;
					publishProgress((int) (total * 100 / fileLength));
					output.write(data, 0, count);
				}

				output.flush();
				output.close();
				input.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		    return SharedValues.FILE_NAME;
		}
		
		@Override
		protected void onPreExecute() {
			loading.show();
			super.onPreExecute();
		}

		@Override
		protected void onPostExecute(String message) {
			loading.dismiss();
			
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_VIEW);
			File apk = new File(SharedValues.FILE_PATH, SharedValues.FILE_NAME);
			if (apk.exists()) {
				intent.setDataAndType(Uri.fromFile(apk), "application/vnd.android.package-archive");
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			    startActivity(intent);
			}
			else {
				final Dialog dialog = new Dialog(MainActivity.this);
	            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
	            dialog.setContentView(R.layout.about_dialog);
	            dialog.setCancelable(true);
	            
	            TextView textViewCurrentVersionName = (TextView) dialog.findViewById(R.id.textViewCurrentVersionName);
            	textViewCurrentVersionName.setText("เชื่อมต่อเซิร์ฟเวอร์ไม่ได้ 2");
            	
            	TextView textViewNewVersionName = (TextView) dialog.findViewById(R.id.textViewNewVersionName);
            	textViewNewVersionName.setVisibility(View.GONE);

            	Button buttonUpdate = (Button) dialog.findViewById(R.id.buttonUpdate);
            	buttonUpdate.setVisibility(View.GONE);
            	
            	dialog.show();
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
			loading = new ProgressDialog(MainActivity.this);
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
				checked = Request.checkin(editTextId.getText().toString(), code);
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

			final Dialog dialog = new Dialog(MainActivity.this);
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
