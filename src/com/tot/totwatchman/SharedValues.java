package com.tot.totwatchman;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

public class SharedValues {
	
	public static final String HOST_DB = "http://203.114.104.242/watchman/getRecord.php";
	public static final String HOST_VERSION = "http://203.114.104.242/watchman/getVersion.php";
	
	public final static String FILE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/totapk/";
	public final static String FILE_NAME = "TOTWatchman.apk";
	public static final String HOST_APK = "http://203.114.104.242/watchman/TOTWatchman.apk";
	
	
	private SharedValues () {
		
	}
	
}
