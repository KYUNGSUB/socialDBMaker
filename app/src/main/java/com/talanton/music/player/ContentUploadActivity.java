package com.talanton.music.player;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.net.HttpURLConnection;
import java.util.*;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import com.talanton.music.player.utilities.*;
import com.talanton.music.player.utilities.MusicListDB.*;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class ContentUploadActivity extends MusicListBaseActivity {
	private EditText i_et;
	private EditText f_et;
//	private BaasioUploadAsyncTask mUploadFileAsyncTask = null;

	private TextView status;
	private String mPid;
	private static final String MUSIC_DIRECTORY = "MusicPoster";
//	private static final String DOWNLOAD_DIRECTORY = "MusicPlayer";
	private static final String DOWNLOAD_DIRECTORY = "MusicPlayer";
	private static final String TAG = "MusicPlayer";
	private String selectedMediaFile = null;
	private boolean isSendCompleted;
	private int compressCount;
	private Handler handler = new Handler();
	private Dialog customProgressDialog;
	
//	private BaasioQuery mQuery;
	private String mWhere;
	public static final String ENTITY_PROPERTY_NAME_PID = "pid";
//	private ArrayList<BaasioFile> mEntityList;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_createpost);
		initActivity();
	}
	
	private void initActivity() {
//		mEntityList = new ArrayList<BaasioFile>();
		i_et = (EditText)findViewById(R.id.product_edit);
//		v_et = (EditText)findViewById(R.id.version_edit);
		f_et = (EditText)findViewById(R.id.file_edit);
		status = (TextView)findViewById(R.id.post_result);
	}
	
	public void createPostButtonClick(View view) {
		if(i_et.length() == 0 || f_et.length() < 11) {
			Toast.makeText(getBaseContext(), getString(R.string.miss_input_help), Toast.LENGTH_LONG).show();
			return;
		}
//		closeSoftKeyboard();
		
		mPid = i_et.getText().toString();
//		long iPid = Long.valueOf(mPid);
//		if(iPid < 10000000000L || iPid > 20000000000L) {
//			Toast.makeText(getBaseContext(), getString(R.string.miss_input_help), Toast.LENGTH_LONG).show();
//			return;
//		}
		
		String mFileinfo = f_et.getText().toString();
//		uploadFileToBaasIOBackend(mPid, mFileinfo);
	}
	
//	private boolean uploadFileToBaasIOBackend(final String pid, final String filename) {
//		if(checkFileExistenceFromSD(filename) == false) {
//			Toast.makeText(getBaseContext(), getString(R.string.file_check), Toast.LENGTH_LONG).show();
//			return false;
//		}
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//            	handler.post(new Runnable() {
//        			public void run() {
//        				if (selectedMediaFile != null) {
//        					createProgressDialog();
//        				}
//        			}
//        		});
//
//                BaasioFile uploadFile = new BaasioFile();
//                uploadFile.setProperty("pid", pid);	// 파일이름으로 PID를 저장
//                
//                Log.i(TAG, "url : " + selectedMediaFile);
//
//                mUploadFileAsyncTask = uploadFile.fileUploadInBackground(selectedMediaFile, null, new BaasioUploadCallback() {
//                	@Override
//                	public void onResponse(BaasioFile response) {
//                		if (customProgressDialog != null) {
//                			handler.post(new Runnable() {
//                				public void run() {
//                					if (selectedMediaFile != null) {
//                						customProgressDialog.dismiss();
//                						customProgressDialog = null;
//                					}
//                				}
//                			});
//                		}
//
//                		if (response != null) {
//                			if(pid.length() > 10)	// 음악 컨텐츠
//                				storeUuidToDbTable(pid, response.getUuid());
//                			else
//                				Log.i(TAG, "contents-type = " + response.getContentType());
//                		}
//                		
//                		handler.post(new Runnable() {
//                			public void run() {
//                				status.setText(getString(R.string.success_help));
//                			}
//                		});
//                	}
//
//					@Override
//                	public void onProgress(long total, long current) {
//						float rate = (float)((double)current / (double)total);
//						ProgressBar progress = (ProgressBar)customProgressDialog.findViewById(R.id.progress);
//						progress.setProgress((int)rate);
//					}
//
//					@Override
//					public void onException(BaasioException e) {
//						if (customProgressDialog != null) {
//							handler.post(new Runnable() {
//								public void run() {
//									if (selectedMediaFile != null) {
//										customProgressDialog.dismiss();
//										customProgressDialog = null;
//									}
//								}
//							});
//						}
//
////						LogUtils.LOGE(TAG, "fileUploadInBackground =>" + e.toString());
//						Toast.makeText(getApplicationContext(), "fileUploadInBackground =>" + e.toString(),
//                                        Toast.LENGTH_LONG).show();
//					}
//                });
//            }
//        }, 300);
//
//		return true;
//	}
	
	private void storeUuidToDbTable(String pid, UUID uuid) {
		Log.i(TAG, "ContentUpload:storeUuidToDbTable(pid=" + pid + ", uuid = " + uuid.toString());
		
		String urlInfo = uuid.toString();
		
		SQLiteDatabase dbHandler = musicDB.getWritableDatabase();
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		
		queryBuilder.setTables(MusicServerInfo.TABLE_NAME);
		String w = MusicServerInfo.PRODUCT_ID + "='" + pid + "'";
		queryBuilder.appendWhere(w);
		
		//����� ������ �ش� ��� ������ ���� �´�
		Cursor cursor = queryBuilder.query(dbHandler, null,null,null,null,null, null);
		Log.i(TAG, "count = " + cursor.getCount());
		
		//Ŀ���� �� ���� Activity�� ����
		startManagingCursor(cursor);
		if(cursor.getCount() > 0) {
			ContentValues  musicRowValues = new ContentValues();
			musicRowValues.put(MusicServerInfo.UUID_INFO, urlInfo);
			dbHandler.update(MusicServerInfo.TABLE_NAME, musicRowValues, w, null);
		}
	}

	private boolean checkFileExistenceFromSD(String mFileinfo) {
		StringBuilder sb = new StringBuilder(Environment.getExternalStorageDirectory().toString());
		sb.append("/").append(MUSIC_DIRECTORY).append("/").append(mFileinfo);
		
		File file = new File(sb.toString());
		if(file.exists()) {
			this.selectedMediaFile = sb.toString();
			return true;
		}
		return false;
	}
	
	public void contentDownloadExecution(View view) {
		if(i_et.length() == 0) {
			Toast.makeText(getApplicationContext(), "다운로드할 PID를 입력 하시오.", Toast.LENGTH_SHORT).show();
			return;
		}
		mPid = i_et.getText().toString();
//		getContentUuidInformation();
	}

//	private void getContentUuidInformation() {
//		if(ObjectUtils.isEmpty(mQuery)) {
//			mQuery = new BaasioQuery();
//			mQuery.setType(BaasioFile.ENTITY_TYPE + "s");
//		}
//		mWhere = ENTITY_PROPERTY_NAME_PID + "='" + mPid + "'";
//		mQuery.setWheres(mWhere);
//		mQuery.queryInBackground(mQueryCallback);
//	}
//	
//	private BaasioQueryCallback mQueryCallback = new BaasioQueryCallback() {
//        @Override
//        public void onResponse(List<BaasioBaseEntity> entities, List<Object> list,
//                BaasioQuery query, long timestamp) {
//            mEntityList.clear();
//            mQuery = query;
//            List<BaasioFile> files = BaasioBaseEntity.toType(entities, BaasioFile.class);
//            mEntityList.addAll(files);
//            Log.d(TAG, "size of files = " + files.size());
//            Log.d(TAG, "UUID of pid " + mPid + " : " + files.get(0).getUuid().toString());
//            downloadContentFromServer(files.get(0).getUuid(), files.get(0).getFilename());
//        }
//
//        @Override
//        public void onException(BaasioException e) {
//            Toast.makeText(getApplicationContext(), "queryInBackground =>" + e.toString(), Toast.LENGTH_LONG).show();
//        }
//    };
//
//	private void downloadContentFromServer(UUID uuid, String filename) {
//		final String localpath = makeLocalStoragePath(filename);
//		final BaasioFile downloadFile = new BaasioFile();
//		downloadFile.setUuid(uuid);
//		new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//            	handler.post(new Runnable() {
//        			public void run() {
//       					createProgressDialog();
//        			}
//        		});
//				BaasioDownloadAsyncTask downloadFileAsyncTask = downloadFile.fileDownloadInBackground(
//						localpath, new BaasioDownloadCallback() {
//					@Override
//					public void onResponse(String localFilePath) {		// 성공
//						if (customProgressDialog != null) {
//                			handler.post(new Runnable() {
//                				public void run() {
//               						customProgressDialog.dismiss();
//               						customProgressDialog = null;
//                				}
//                			});
//                		}
//
//                		if (localFilePath != null) {
//               				Log.i(TAG, "filepath = " + localFilePath);
//                		}
//                		
//                		handler.post(new Runnable() {
//                			public void run() {
//                				status.setText(getString(R.string.success_help));
//                			}
//                		});		
//					}
//							
//					@Override
//					public void onProgress(long total, long current) {	// 진행상황
//						float rate = (float)((double)current / (double)total);
//						ProgressBar progress = (ProgressBar)customProgressDialog.findViewById(R.id.progress);
//						progress.setProgress((int)rate);		
//					}
//							
//					@Override
//					public void onException(BaasioException e) {		// 에러
//						if (customProgressDialog != null) {
//							handler.post(new Runnable() {
//								public void run() {
//									customProgressDialog.dismiss();
//									customProgressDialog = null;
//								}
//							});
//						}
//
//						Toast.makeText(getApplicationContext(), "fileDownloadInBackground =>" + e.toString(),
//                                        Toast.LENGTH_LONG).show();		
//					}
//				});
//            }
//		}, 300);
//	}

	private String makeLocalStoragePath(String filename) {
		return Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + DOWNLOAD_DIRECTORY + "/" + filename;
	}

	private void createProgressDialog() {
		customProgressDialog = new Dialog(this);
		customProgressDialog.setTitle(R.string.message_uploading_post);
		customProgressDialog.setContentView(R.layout.upload_progress);
		ProgressBar progress = (ProgressBar)customProgressDialog.findViewById(R.id.progress);
		progress.setMax(100);		
		customProgressDialog.setCancelable(false);
		/**
         * author daesoo,kim
         * date 2010.3.16
         */
		customProgressDialog.setOnKeyListener(new DialogInterface.OnKeyListener(){
			public boolean onKey(DialogInterface dialog, int keyCode,
					KeyEvent event) {
				if(keyCode == KeyEvent.KEYCODE_SEARCH)
					return true;
				return false;
			}
        });
		customProgressDialog.show();
	}
}
