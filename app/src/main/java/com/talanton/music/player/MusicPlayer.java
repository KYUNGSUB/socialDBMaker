package com.talanton.music.player;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import com.talanton.music.player.utilities.*;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MusicPlayer extends MusicListBaseActivity {
    private static final long DISPLAY_INTERVAL = 2000L;
    private static final String TAG = "MusicPlayer";
	protected static final String KEY_NOFMUSIC = "number_of_music";
	
	public static final String DATA_FILE_NAME = "saved_data";
	public static final Object[] sDataLock = new Object[0];
	private static final String KEY_MUSIC_VERSION = "music_data_version";
	private static final String FILE_UUID_LIST = "fileuuid_list";
	private static final String CATEGORY_UUID_LIST = "category_list";
//	private ScrollView uaSv;
	private LinearLayout pLayout;
	private TextView status_tv;
	Handler mHandler = new Handler();
	Thread mThread = null;

	/** Called when the activity is first created. */
    @Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "MusicPlayer:onCreate()");
        
        setContentView(R.layout.activity_musicplayer);
        
//        uaSv = (ScrollView)findViewById(R.id.initial_display);
        pLayout = (LinearLayout)findViewById(R.id.progress_layout);
        status_tv = (TextView)findViewById(R.id.status_guide);

       	int resMusicVersion = Integer.valueOf(getString(R.string.music_data_version));
        if(MusicUtils.getConfigInteger(getBaseContext(), KEY_MUSIC_VERSION, 0) < resMusicVersion) {
           	deleteDBTable();
           	storeMusicInformationFromResource();
        }
        else {
           	callGotoMainByDelay(DISPLAY_INTERVAL);
        }
    }

	private void storeMusicInformationFromResource() {
		mThread = new Thread() {
			public void run() {		// Background job�� run���� ����
				mHandler.post(new Runnable() {
					public void run() {
						pLayout.setVisibility(View.VISIBLE);
						status_tv.setText(getString(R.string.guide_data_initial2));
					}
				});
				readPhysicalMusicList(getString(R.string.physical_music_list));
				readLogicalMusicList(getString(R.string.logical_music_list));
				readFileUuidInformation();
//				readCategoryUuidInformation();
				MusicUtils.saveConfig(getBaseContext(), KEY_MUSIC_VERSION, Integer.valueOf(getString(R.string.music_data_version)));
				mHandler.post(new Runnable() {
					public void run() {
						pLayout.setVisibility(View.GONE);
					}
				});
				TimerMethod();
			}
		};		// Thread ����
		mThread.start();
	}

	protected void readFileUuidInformation() {
		Log.i(TAG, "readFileUuidInformation()");
		int musicDataId = getResources().getIdentifier(FILE_UUID_LIST, "raw", getPackageName());
		InputStream is = getResources().openRawResource(musicDataId);
				
		try {
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			XmlPullParser xpp = factory.newPullParser();
			xpp.setInput(is, "UTF-8");
			Parse mParse = new Parse(musicDB);
			mParse.parseFileUuidInfo(xpp);
		} catch(XmlPullParserException e) {
			Log.e(TAG, "XmlPullParserException: " + e.getMessage());
		} catch (FileNotFoundException e) {
			Log.e(TAG, "FileNotFoundException: " + e.getMessage());
		} catch(IOException e) {
			Log.e(TAG, "IOException: " + e.getMessage());
		}
	}

	protected void readCategoryUuidInformation() {
		Log.i(TAG, "readCategoryUuidInformation()");
		int musicDataId = getResources().getIdentifier(CATEGORY_UUID_LIST, "raw", getPackageName());
		InputStream is = getResources().openRawResource(musicDataId);
				
		try {
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			XmlPullParser xpp = factory.newPullParser();
			xpp.setInput(is, "UTF-8");
			Parse mParse = new Parse(musicDB);
			mParse.parseCategoryUuidInfo(xpp);
		} catch(XmlPullParserException e) {
			Log.e(TAG, "XmlPullParserException: " + e.getMessage());
		} catch (FileNotFoundException e) {
			Log.e(TAG, "FileNotFoundException: " + e.getMessage());
		} catch(IOException e) {
			Log.e(TAG, "IOException: " + e.getMessage());
		}
	}

	protected void readLogicalMusicList(String filename) {
		Log.i(TAG, "MusicPlayer:readLogicalMusicList()");
		// ���� resource�� �ִ� ���� ���� ��� ó��
		int musicDataId = getResources().getIdentifier(filename, "raw", getPackageName());
		InputStream is = getResources().openRawResource(musicDataId);
				
		try {
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			XmlPullParser xpp = factory.newPullParser();
			xpp.setInput(is, "UTF-8");
			Parse mParse = new Parse(musicDB);
			mParse.parseLogicalMusicLists(xpp);
		} catch(XmlPullParserException e) {
			Log.e(TAG, "XmlPullParserException: " + e.getMessage());
		} catch (FileNotFoundException e) {
			Log.e(TAG, "FileNotFoundException: " + e.getMessage());
		} catch(IOException e) {
			Log.e(TAG, "IOException: " + e.getMessage());
		}
	}

	protected void readPhysicalMusicList(String filename) {
		Log.i(TAG, "MusicPlayer:readPhysicalMusicList()");
	   	// ���� resource�� �ִ� ���� ���� ��� ó��
		int musicDataId = getResources().getIdentifier(filename, "raw", getPackageName());
		InputStream is = getResources().openRawResource(musicDataId);
			
		try {
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			XmlPullParser xpp = factory.newPullParser();
			xpp.setInput(is, "UTF-8");
			Parse mParse = new Parse(musicDB);
			mParse.parsePhysicalMusicLists(xpp);
			MusicUtils.saveConfig(getBaseContext(), KEY_NOFMUSIC, mParse.getnOfMusic());
		} catch(XmlPullParserException e) {
			Log.e(TAG, "XmlPullParserException: " + e.getMessage());
		} catch (FileNotFoundException e) {
			Log.e(TAG, "FileNotFoundException: " + e.getMessage());
		} catch(IOException e) {
			Log.e(TAG, "IOException: " + e.getMessage());
		}
	}

	private void callGotoMainByDelay(long delay) {
		// Timer activation for ProgrssBar update
		Timer myTimer = new Timer();
		myTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				TimerMethod();
			}
		}, delay);	// 3000(3��) time interval
	}

    private void deleteDBTable() {
//    	Log.i(TAG, "MusicPlayer:deleteDBTable() start");
    	SQLiteDatabase dbHandler = musicDB.getWritableDatabase();
    	musicDB.onUpgrade(dbHandler, 0, 0);
    	dbHandler.close();
	}

	protected void TimerMethod() {
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
		finish();
	}
}