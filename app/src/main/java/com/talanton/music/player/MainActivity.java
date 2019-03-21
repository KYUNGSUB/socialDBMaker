package com.talanton.music.player;

import java.util.Timer;
import com.talanton.music.player.utilities.*;
import com.talanton.music.player.utilities.MusicListDB.*;
import android.app.*;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

public class MainActivity extends MusicListBaseActivity {
	private static final String TAG = "MusicPlayer";
	private Handler handler = new Handler();
	private Cursor mCursor;
	private int mPosition;
	public static final int REQUEST_SIGNIN = 1;
	private static final int CONFIRM_FINISH_DIALOG = 100;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "MainActivity:onCreate()");
		setContentView(R.layout.activity_main);
	}

	public void downloadMusicOperation(View view) {
		Intent intent = new Intent(this, ContentManagement.class);
		startActivity(intent);
	}
	
	public void showHelpInformation(View view) {
		Intent intent = new Intent(this, HelpDisplay.class);
		startActivity(intent);
	}
	
	public void exitApplication(View view) throws RemoteException {
		showDialog(CONFIRM_FINISH_DIALOG);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
//		Log.i(TAG, "KeyCode = " + keyCode);
		if(keyCode == KeyEvent.KEYCODE_BACK){
			showDialog(CONFIRM_FINISH_DIALOG);
		}
		return false;
	}
	
	int which_menu = 0;
	@Override
	protected Dialog onCreateDialog(int id) {
		switch(id) {
		case CONFIRM_FINISH_DIALOG: {
			return new AlertDialog.Builder(MainActivity.this)
			.setIcon(R.drawable.alert_dialog_icon)
	        .setTitle(R.string.title_finish_music_confirm)
	        .setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    finish();
                }
            })
            .setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {

                }
            })
	        .create();
		}
		}
		return super.onCreateDialog(id);
	}

    public void playMusicClick(View v) throws RemoteException {
//		Log.d(TAG, "MainActivity:playMusicClick (playState = " + playState + ")");

	}

	private Timer myTimer;
	boolean mTouched = false;
	Handler mHandler = new Handler();

	public void previousMusicClick(View v) throws RemoteException {
		Log.d(TAG, "MainActivity:previousMusicClick()");

		SQLiteDatabase dbHandler = musicDB.getReadableDatabase();
		Log.i(Constant.TAG, "database version : " + dbHandler.getVersion());
//		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
//		queryBuilder.setTables(SubCategoryInfo.TABLE_NAME);
//		mCursor = queryBuilder.query(dbHandler, null, null, null, null, null, null);
//		int mainCategory;
//		int subCategory;
//		if(mCursor.getCount() > 0) {
//			mCursor.moveToFirst();
//			do {
//				mainCategory = mCursor.getInt(mCursor.getColumnIndex(SubCategoryInfo.M_ID));
//				subCategory = mCursor.getInt(mCursor.getColumnIndex(SubCategoryInfo.S_ID));
//				Log.d(TAG, "main category = " + mainCategory + ", sub category = " + subCategory);
//				storeCategoryInfoToBaasio(mainCategory, subCategory);
//				try {
//					Thread.sleep(5000);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//			}
//			while (mCursor.moveToNext());
//			mCursor.close();
//		}
		dbHandler.close();
	}

	public static final String ENTITY_TYPE = "category";
    public static final String ENTITY_PROPERTY_NAME_MID = "mid";
    public static final String ENTITY_PROPERTY_NAME_SID = "sid";
//    private BaasioQuery mQuery;
    
	private void storeCategoryInfoToBaasio(final int mainCategory, final int subCategory) {
//		BaasioEntity entity = new BaasioEntity(ENTITY_TYPE);
//        entity.setProperty(ENTITY_PROPERTY_NAME_MID, mainCategory);
//        entity.setProperty(ENTITY_PROPERTY_NAME_SID, subCategory);
//        entity.saveInBackground(new BaasioCallback<BaasioEntity>() {
//            @Override
//            public void onException(BaasioException e) {
//                Toast.makeText(getApplicationContext(), "saveInBackground =>" + e.toString(),
//                        Toast.LENGTH_LONG).show();
//            }
//
//            @Override
//            public void onResponse(BaasioEntity response) {
//            	Log.d(TAG, "response = " + response);
//                Log.d(TAG, "store mid = " + mainCategory + ", sid = " + subCategory + " succeed");
//            }
//        });
	}

	public void stopMusicClick(View v) throws RemoteException {
//		Log.d(TAG, "MainActivity:stopMusicClick()");

	}

	public void nextMusicClick(View v) throws RemoteException {
//		Log.d(TAG, "MainActivity:nextMusicClick()");

	}

	public void purchaseCoupon(View view) {
//		Log.i(TAG, "ContentManagement:purchaseDownloadCouponClick()");
		Intent intent = new Intent(this, ContentUploadActivity.class);
		startActivity(intent);
	}
}