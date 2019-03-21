package com.talanton.music.player;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.talanton.music.player.utilities.*;
import com.talanton.music.player.utilities.MusicListDB.*;

public class ContentManagement extends MusicListBaseActivity {
	private static final String TAG = "MusicPlayer";
	protected static final int DOWNLOAD_MUSIC_DIALOG = 100;
	static final Object EXTERNAL_DIRECTORY = "MusicPlayer";
	private int mCategory = 0;
	private TextView count_tv;
	private EditText key_et;
	private ListView music_list;
	private Cursor mCursor;
	private ListAdapter resultSetAdapter;
	private Handler handler = new Handler();
	private Thread mThread = null;
	protected static final int MAXIMUM_RX_POSTS = 10;

	private static final int SEARCH_MUSIC_DIALOG = 102;
	private static final int SEARCH_MUSIC_BY_ID = 103;
	
	private static final int SEARCH_MUSIC = Menu.FIRST + 1;
	private static final int ADD_MUSIC = Menu.FIRST + 2;
	private static final int SYNC_URL_INFO = Menu.FIRST + 3;
	private static final int AUTO_URL_GET = Menu.FIRST + 4;
	
//	private static final String MY_MUSIC_TAG = "me2cast";
	private ArrayList<MusicCategory> mainCategory = new ArrayList<MusicCategory>();
	private ArrayList<MusicCategory> subCategory = new ArrayList<MusicCategory>();
	private int mMainPosition;	// Main Category Position
	private int mSubPosition;	// SUb Category Position index
	private String language;
	private int nofMusic;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		Log.i(TAG, "ContentManagement:onCreate()");
		setContentView(R.layout.activity_contentmanagement);
		
		initActivity();
//		closeSoftKeyboard();
	}
	
//	/**
//	 * Close the on-screen keyboard.
//	 */
//	private void closeSoftKeyboard() {
//		InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//		inputMethodManager.hideSoftInputFromWindow(key_et.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
//	}

	private void retrieveMusicListFromDB(int category, String keyWord) {
//		Log.i(TAG, "ContentManagement:retrieveMusicListFromDB(category=" + category + ", keyword=" + keyWord + ")");
		SQLiteDatabase dbHandler = musicDB.getReadableDatabase();
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		
		Log.i(TAG, "category = " + category + ", keyword = " + keyWord + ", language = " + language);
		if(keyWord != null) {	// �˻�� ���� �˻�
			queryBuilder.setTables(MusicServerInfo.TABLE_NAME);
			StringBuilder sb = new StringBuilder();
			if(category == 0 && language.equals("ko")) {	// ����
				sb.append(MusicServerInfo.TITLE_KO).append(" LIKE \"%").append(keyWord).append("%\"");
			}
			else if(category == 0 && !language.equals("ko")) {	// ����
				sb.append(MusicServerInfo.TITLE_EN).append(" LIKE \"%").append(keyWord).append("%\"");
			}
			else if(category == 1 && language.equals("ko")) {
				sb.append(MusicServerInfo.AUTHOR_KO).append(" LIKE \"%").append(keyWord).append("%\"");
			}
			else {
				sb.append(MusicServerInfo.AUTHOR_EN).append(" LIKE \"%").append(keyWord).append("%\"");
			}
			queryBuilder.appendWhere(sb.toString());
		}
		else if(keyWord == null && category == 0){					// �з� ���� �˻�
			queryBuilder.setTables(MusicContentsInfo.TABLE_NAME + "," + MusicServerInfo.TABLE_NAME);
			int lPid = getMainCategoryId() * 10000 + getSubCategoryId() * 100;	//mSubPosition
			int hPid = lPid + 100;
			StringBuilder sb = new StringBuilder();
			sb.append(MusicContentsInfo.TABLE_NAME).append(".").append(MusicContentsInfo.L_ID).append(" >= ").append(lPid).append(" AND ")
				.append(MusicContentsInfo.TABLE_NAME).append(".").append(MusicContentsInfo.L_ID).append(" < ").append(hPid)
				.append(" AND ").append(MusicContentsInfo.TABLE_NAME).append(".").append(MusicContentsInfo.P_ID)
				.append("=").append(MusicServerInfo.TABLE_NAME).append(".").append(MusicServerInfo.PRODUCT_ID);
			queryBuilder.appendWhere(sb.toString());
		}
		else {	// keyWord == null && category != 0 (_ID >= mMainPosition && _ID < mSubPosition)
			queryBuilder.setTables(MusicServerInfo.TABLE_NAME);
			StringBuilder sb = new StringBuilder();
			sb.append(MusicServerInfo.PRODUCT_ID).append(" >= ").append(mMainPosition).append(" AND ")
				.append(MusicServerInfo.PRODUCT_ID).append(" <= ").append(mSubPosition);
			queryBuilder.appendWhere(sb.toString());
		}

		//��� �������� ������ �÷��� �̸���(2�� �̻� ���̺�� ����� Ǯ������ �־�� ��)
		String columnsToReturn [] = {
				MusicServerInfo.TABLE_NAME + "." + MusicServerInfo.TITLE_KO,
				MusicServerInfo.TABLE_NAME + "." + MusicServerInfo.AUTHOR_KO,
				MusicServerInfo.TABLE_NAME + "." + MusicServerInfo.TITLE_EN,
				MusicServerInfo.TABLE_NAME + "." + MusicServerInfo.AUTHOR_EN,
				MusicServerInfo.TABLE_NAME + "." + MusicServerInfo.PLAY_TIME,
				MusicServerInfo.TABLE_NAME + "." + MusicServerInfo.UUID_INFO,
				MusicServerInfo.TABLE_NAME + "." + MusicServerInfo.BOOKMARK,
				MusicServerInfo.TABLE_NAME + "." + MusicServerInfo.PRODUCT_ID,
				MusicServerInfo.TABLE_NAME + "." + MusicServerInfo._ID,
		};
		
		String sortOrder = MusicServerInfo.TABLE_NAME + "." + MusicServerInfo.PRODUCT_ID;
		//����� ������ �ش� ��� ������ ���� �´�
		mCursor = queryBuilder.query(dbHandler, columnsToReturn,null,null,null,null, sortOrder);
		
		Log.i(TAG, "count of cursor = " + mCursor.getCount());
		//Ŀ���� �� ���� Activity�� ����
		startManagingCursor(mCursor);
		
		//���� ��� ������ Adapter�� Mapping ���� ���̾ƿ� ��� �� ��� �Ѵ�
		resultSetAdapter = new MySimpleCursorAdapter(
				this,
			    R.layout.list_download,
			    mCursor,
			    new String[]{
					MusicServerInfo.TITLE_KO,
					MusicServerInfo.AUTHOR_KO,
					MusicServerInfo.PLAY_TIME,
					MusicServerInfo.UUID_INFO,
					MusicServerInfo.BOOKMARK
			    },
			    new int[]{
					R.id.title_content,
			        R.id.author_content,
			        R.id.time_content,
			        R.id.url_mark,
			        R.id.bookmark_cb
			    }
		);
		music_list.setAdapter(resultSetAdapter);
	}
	
	private int getSubCategoryId() {
		return subCategory.get(mSubPosition-1).getCode();
	}

	// ����Ʈ ���� �ڽ� �並 �����ϰ� ǥ��
	static class PEMusicGroupListItemContainer  {
	    TextView mMusicName;
	    TextView mAuthorName;
	    TextView mTime;
	    TextView mUrlMark;
	    CheckBox mBookmark;
	}
	
	// Adapter implementation
	public class MySimpleCursorAdapter extends SimpleCursorAdapter {
		private Context mContext;
		private int mLayout;

		public MySimpleCursorAdapter(Context context, int layout, Cursor cursor, String[] from, int[] to) {
			super(context, layout, cursor, from, to);
//			Log.i(TAG, "ContentManagement:MySimpleCursorAdapter()");
			mContext = context;
			mLayout = layout;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
//			Log.i(TAG, "getView start(position = " + position + ", convertView = " + convertView + ")");
			final Cursor cursor = getCursor();
			
			PEMusicGroupListItemContainer musicListItem;
			
			if(convertView == null) {
				LayoutInflater inflate = LayoutInflater.from(mContext); 
				convertView = (View)inflate.inflate(mLayout, null);
				musicListItem = new PEMusicGroupListItemContainer();
				
				musicListItem.mMusicName = (TextView)convertView.findViewById(R.id.title_content);
				musicListItem.mAuthorName = (TextView)convertView.findViewById(R.id.author_content);
				musicListItem.mTime = (TextView)convertView.findViewById(R.id.time_content);
				musicListItem.mUrlMark = (TextView)convertView.findViewById(R.id.url_mark);
				musicListItem.mBookmark = (CheckBox)convertView.findViewById(R.id.bookmark_cb);

				convertView.setTag(musicListItem);
			}
			else {
				musicListItem = (PEMusicGroupListItemContainer) convertView.getTag();
			}
			
			cursor.moveToPosition(position);
			
			if(!language.equals("ko")) {	// English
				musicListItem.mMusicName.setText(cursor.getString(cursor.getColumnIndex(MusicServerInfo.TITLE_EN)));
				musicListItem.mAuthorName.setText(cursor.getString(cursor.getColumnIndex(MusicServerInfo.AUTHOR_EN)));
			}
			else {							// Korean
				musicListItem.mMusicName.setText(cursor.getString(cursor.getColumnIndex(MusicServerInfo.TITLE_KO)));
				musicListItem.mAuthorName.setText(cursor.getString(cursor.getColumnIndex(MusicServerInfo.AUTHOR_KO)));
			}
			musicListItem.mTime.setText(cursor.getString(cursor.getColumnIndex(MusicServerInfo.PLAY_TIME)));
			
			String urlInfo = cursor.getString(cursor.getColumnIndex(MusicServerInfo.UUID_INFO));
			if(urlInfo == null || urlInfo.equals("")) {
				musicListItem.mUrlMark.setText("X");
			}
			else {
				musicListItem.mUrlMark.setText("O");
			}
			
			if(cursor.getInt(cursor.getColumnIndex(MusicServerInfo.BOOKMARK)) == 1) {	// ���ã�� ��Ͽ� ����
				musicListItem.mBookmark.setChecked(true);
//				Log.i(TAG, "checkbox = true");
			}
			else {
				musicListItem.mBookmark.setChecked(false);
//				Log.i(TAG, "checkbox = false");
			}
			musicListItem.mBookmark.setClickable(true);
			musicListItem.mBookmark.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					int pointer = Integer.valueOf(v.getTag().toString());
//					Log.i(TAG, "isChecked = " + ((CheckBox) v).isChecked() + ", position = " + position);
					cursor.moveToPosition(pointer);
					String productID = cursor.getString(cursor.getColumnIndex(MusicServerInfo.PRODUCT_ID));	// Me2day �˻�
					updateBookmarkInformation(productID, ((CheckBox) v).isChecked());
				}
			});
			musicListItem.mBookmark.setTag(position);
			
//			Log.i(DTAG, "getView end(position = " + position + ", convertView = " + convertView + ")");
			return convertView;
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			return null;
		}
	}

	private void updateBookmarkInformation(final String productID, final boolean isChecked) {
//		Log.i(TAG, "tid = " + tid + ", p_id = " + productID);
	}
	
	protected void deleteDownloadedMusicFile(String filename) {
		Log.i(TAG, "filename = " + filename);
		
		StringBuilder sb = new StringBuilder("/data/data/");
		sb.append("/").append(getPackageName()).append("/files/").append(filename);
		File file = new File(sb.toString());
		if(file.exists()) {
			file.delete();
		}
	}

//	private String retrieveMusicURLFromMe2day(String productID) {
//		ArrayList<Post> postList;
//		postPoster = new GetPostPoster();
//		Me2dayInfo.setLoginId("tentalanton");
//		postPoster.setTag(productID);
//		
//		try {
//			HttpParams params = new BasicHttpParams();
//			HttpConnectionParams.setConnectionTimeout(params, Me2dayInfo.TIMEOUT);  
//			HttpConnectionParams.setSoTimeout(params, Me2dayInfo.TIMEOUT); 
//			DefaultHttpClient httpClient = new DefaultHttpClient(params);
//			
//			HttpRequestBase method = postPoster.createHttpMehtod();
//			HttpResponse response = httpClient.execute(method);
//			
//			int responseCode = response.getStatusLine().getStatusCode();
//			if(responseCode == 200) {
//				HttpEntity entity = response.getEntity();
//				InputStream is = entity.getContent();
////				saveFile(is);
//				XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
//				XmlPullParser xpp = factory.newPullParser();
//				xpp.setInput(is, "UTF-8");
//				Parse mParse = new Parse(null);
//				postList = mParse.parsePosts(xpp);
//				is.close();
//				
//				return postList.get(0).getSource(); 	// Parsing ��� ��
//			}
//			else {
//				Log.d(TAG, "Response Error : " + responseCode);
//				InputStream is;
//				is = response.getEntity().getContent();
//				BufferedReader br = new BufferedReader(new InputStreamReader(is));
//				String s;
//				while ((s = br.readLine()) != null) {
//					Log.d(TAG, s);
//				}
//				br.close();
//			}
//		} catch (IOException e) {
//			Log.e(TAG, "IOException: " + e.getMessage());
//		} catch (IllegalStateException e) {
//			Log.e(TAG, "IllegalStateException: " + e.getMessage());
//		} catch (XmlPullParserException e) {
//			Log.e(TAG, "XmlPullParserException: " + e.getMessage());
//		}
//		return null;
//	}

	private void initActivity() {
		Locale lo = Locale.getDefault();
		language = lo.getLanguage();
//		Log.i(TAG, "Locale language = " + language);
		
		nofMusic = MusicUtils.getConfigInteger(getBaseContext(), MusicPlayer.KEY_NOFMUSIC, 0);
		readMainCategoryList();

		count_tv = (TextView)findViewById(R.id.count_content);

		Spinner main_spin = (Spinner)findViewById(R.id.main_spin);
		main_spin.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				Log.i(TAG, "main spin onItemSelected(position = " + position + ")");
				mMainPosition = position + 1;		// +1 offset
				Spinner sub_spin = (Spinner)findViewById(R.id.sub_spin);
				sub_spin.setOnItemSelectedListener(new OnItemSelectedListener() {
					public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
						Log.i(TAG, "sub spin onItemSelected(position = " + position + ")");
						mSubPosition = position + 1;
						searchMusicClick(null);
					}

					public void onNothingSelected(AdapterView<?> arg0) {
					}
				});
				String[] subItems = fillSubItemsFromCategoryList();
				ArrayAdapter<String> bb = new ArrayAdapter<String>(ContentManagement.this, android.R.layout.simple_spinner_item, subItems);
				bb.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				sub_spin.setAdapter(bb);
			}

			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
		final String[] mainItems = fillMainItemsFromCategoryList();
		ArrayAdapter<String> aa = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mainItems);
		aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		main_spin.setAdapter(aa);
		
		music_list = (ListView)findViewById(R.id.control_music_list);
//		
//		mEntityList = new ArrayList<BaasioFile>();
	}

	private String[] fillMainItemsFromCategoryList() {
		Log.i(TAG, "count = " + mainCategory.size());
		String[] result = new String[mainCategory.size()];
		for(int i = 0;i < mainCategory.size();i++) {
			if(language.equals("ko"))
				result[i] = mainCategory.get(i).getKname();
			else
				result[i] = mainCategory.get(i).getEname();
		}
		return result;
	}

    private String[] fillSubItemsFromCategoryList() {
    	subCategory.clear();
    	SQLiteDatabase dbHandler = musicDB.getWritableDatabase();
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		
		queryBuilder.setTables(SubCategoryInfo.TABLE_NAME);
		String w = SubCategoryInfo.M_ID + "=" + getMainCategoryId();				// not mMainPosition
		queryBuilder.appendWhere(w);
		// ��� �������� ������ �÷��� �̸���(2�� �̻� ���̺�� ����� Ǯ������ �־�� ��)
		String columnsToReturn [] = {
				SubCategoryInfo.S_ID,
				SubCategoryInfo.KO_NAME,
				SubCategoryInfo.EN_NAME
		};

		//����� ������ �ش� ��� ������ ���� �´�
		Cursor cursor = queryBuilder.query(dbHandler, columnsToReturn,null,null,null,null, null);
		Log.i(TAG, "count = " + cursor.getCount());
		
		//Ŀ���� �� ���� Activity�� ����
		startManagingCursor(cursor);
		String[] result = null;
		
		if(cursor.getCount() > 0) {
			result = new String[cursor.getCount()];
			cursor.moveToFirst();
			int i = 0;
			do {
				if(language.equals("ko"))
					result[i++] = cursor.getString(cursor.getColumnIndex(SubCategoryInfo.KO_NAME));
				else
					result[i++] = cursor.getString(cursor.getColumnIndex(SubCategoryInfo.EN_NAME));
				MusicCategory mc = new MusicCategory();
				mc.setCode(cursor.getInt(cursor.getColumnIndex(SubCategoryInfo.S_ID)));
				subCategory.add(mc);
			} while (cursor.moveToNext());
		}
		cursor.close();
		dbHandler.close();
		return result;
	}

	private int getMainCategoryId() {		// main category ID 값을 지정해야 함.;
		return mainCategory.get(mMainPosition-1).getCode();
	}

	private void readMainCategoryList() {
    	// ���� resource�� �ִ� ���� ���� ��� ó��
		SQLiteDatabase dbHandler = musicDB.getWritableDatabase();
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		
		queryBuilder.setTables(MainCategoryInfo.TABLE_NAME);
		
		// ��� �������� ������ �÷��� �̸���(2�� �̻� ���̺�� ����� Ǯ������ �־�� ��)
		String columnsToReturn [] = {
				MainCategoryInfo.C_ID,
				MainCategoryInfo.KO_NAME,
				MainCategoryInfo.EN_NAME,
				MainCategoryInfo.COUNT
		};

		//����� ������ �ش� ��� ������ ���� �´�
		Cursor cursor = queryBuilder.query(dbHandler, columnsToReturn,null,null,null,null, null);
		Log.i(TAG, "count = " + cursor.getCount());
		
		//Ŀ���� �� ���� Activity�� ����
		startManagingCursor(cursor);
		if(cursor.getCount() > 0) {
			cursor.moveToFirst();
			do {
				MusicCategory category = new MusicCategory();
				category.setCode(cursor.getInt(cursor.getColumnIndex(MainCategoryInfo.C_ID)));
				category.setKname(cursor.getString(cursor.getColumnIndex(MainCategoryInfo.KO_NAME)));
				category.setEname(cursor.getString(cursor.getColumnIndex(MainCategoryInfo.EN_NAME)));
				category.setCount(cursor.getInt(cursor.getColumnIndex(MainCategoryInfo.COUNT)));
				mainCategory.add(category);
			} while (cursor.moveToNext());
		}
		cursor.close();
		dbHandler.close();
	}
	
	public void searchMusicClick(View view) {
//		Log.i(TAG, "ContentManagement:searchMusicClick()");

		retrieveMusicListFromDB(0, null);
		updateCountInformation();
	}

	private void updateCountInformation() {
		count_tv.setText("( " + mCursor.getCount() + " / " + nofMusic + " )");
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch(id) {
		case SEARCH_MUSIC_DIALOG: {
			LayoutInflater factory = LayoutInflater.from(this);
			View textEntryView = factory.inflate(R.layout.alert_dialog_search_music, null);
//			Log.i(TAG, "view = " + textEntryView);
			RadioGroup rg = (RadioGroup)textEntryView.findViewById(R.id.search_type);
			rg.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				public void onCheckedChanged(RadioGroup group, int checkedId) {
					if(checkedId == R.id.title_rb) {
						mCategory = 0;
					}
					else if(checkedId == R.id.author_rb) {
						mCategory = 1;
					}
				}

			});
			rg.check(R.id.title_rb);
    		
    		key_et = (EditText)textEntryView.findViewById(R.id.search_keyword);
//    		Log.i(TAG, "edit = " + key_et);
			return new AlertDialog.Builder(ContentManagement.this)
	        .setTitle(R.string.music_search_btn)
	        .setView(textEntryView)
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int whichButton) {
//	            	Log.i(TAG, "contents list synchronization request");
	        		String keyWord;
	        		if(key_et.getText() == null || key_et.length() == 0) {
	        			keyWord = null;
	        			Toast.makeText(getBaseContext(), getString(R.string.guide_keyword_input), Toast.LENGTH_SHORT).show();
	        		}
	        		else {
	        			keyWord = key_et.getText().toString();
	        			retrieveMusicListFromDB(mCategory, keyWord);
		        		updateCountInformation();
	        		}
	            }
	        })
	        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int whichButton) {
	            }
	        })
            .create();
		}
		case SEARCH_MUSIC_BY_ID: {
			LayoutInflater factory = LayoutInflater.from(this);
			View textEntryView = factory.inflate(R.layout.search_music_by_id, null);
//			Log.i(TAG, "view = " + textEntryView);
			final EditText startID = (EditText)textEntryView.findViewById(R.id.start_mid);
			final EditText endID = (EditText)textEntryView.findViewById(R.id.end_mid);

			return new AlertDialog.Builder(ContentManagement.this)
	        .setTitle(R.string.music_search_btn)
	        .setView(textEntryView)
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int whichButton) {
//	            	Log.i(TAG, "contents list synchronization request");
	        		if(startID.getText() == null || startID.length() == 0 || endID.getText() == null || endID.length() == 0) {
	        			Toast.makeText(getBaseContext(), R.string.guide_keyword_input, Toast.LENGTH_SHORT).show();
	        		}
	        		else {
	        			mMainPosition = Integer.valueOf(startID.getText().toString());
	        			mSubPosition = Integer.valueOf(endID.getText().toString());
	        			retrieveMusicListFromDB(1, null);
		        		updateCountInformation();
	        		}
	            }
	        })
	        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int whichButton) {
	            }
	        })
            .create();
		}
		}
		return super.onCreateDialog(id);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
//		Log.i(TAG, "ContentManagement:onActivityResult");
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		populateMenu(menu);
		return super.onCreateOptionsMenu(menu);
	}

	private void populateMenu(Menu menu) {
		menu.add(Menu.NONE, SEARCH_MUSIC, Menu.NONE, "키워드 검색");
		menu.add(Menu.NONE, ADD_MUSIC, Menu.NONE, "음악 추가");
		menu.add(Menu.NONE, SYNC_URL_INFO, Menu.NONE, "Get URL");
		menu.add(Menu.NONE, AUTO_URL_GET, Menu.NONE, "AUTO GET");
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return (applyMenuChoice(item) || super.onOptionsItemSelected(item));
	}
	
	private boolean applyMenuChoice(MenuItem item) {
		switch(item.getItemId()) {
		case SEARCH_MUSIC:
			showDialog(SEARCH_MUSIC_DIALOG);
			break;
		case ADD_MUSIC:		/* 2013.01.21 */
			addMusicToDB();
			break;
		case SYNC_URL_INFO:
			getContentUrlInformation();
			break;
		case AUTO_URL_GET:
			showDialog(SEARCH_MUSIC_BY_ID);
			break;	
		}
		return false;
	}

	private void addMusicToDB() {
		mThread = new Thread() {
			public void run() {		// Background job�� run���� ����
				final ProgressBar pb = (ProgressBar) findViewById(R.id.progress_large);
				handler.post(new Runnable() {
					public void run() {
						pb.setVisibility(View.VISIBLE);
					}
				});
				final int nofNewMusic = readPhysicalMusicList("physical_add");
				readLogicalMusicList("logical_add");
				handler.post(new Runnable() {
					public void run() {
						pb.setVisibility(View.GONE);
						Toast.makeText(getApplicationContext(), "새로 추가된 음악은 " + nofNewMusic + "개 입니다.", Toast.LENGTH_LONG).show();
					}
				});
			}
		};		// Thread ����
		mThread.start();
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

	protected int readPhysicalMusicList(String filename) {
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
			int oldNofMusic = MusicUtils.getConfigInteger(getApplicationContext(), MusicPlayer.KEY_NOFMUSIC, 0);
			MusicUtils.saveConfig(getBaseContext(), MusicPlayer.KEY_NOFMUSIC, oldNofMusic + mParse.getnOfMusic());
			return mParse.getnOfMusic();
		} catch(XmlPullParserException e) {
			Log.e(TAG, "XmlPullParserException: " + e.getMessage());
		} catch (FileNotFoundException e) {
			Log.e(TAG, "FileNotFoundException: " + e.getMessage());
		} catch(IOException e) {
			Log.e(TAG, "IOException: " + e.getMessage());
		}
		return 0;
	}

	private void getContentUrlInformation() {
//		setProgressBarVisibility(true);
//				
//		int	totalCount = mCursor.getCount();
////		for(int i = 0;i < 1;i++) {
//		for(int i = 0;i < totalCount;i++) {
//			mCursor.moveToPosition(i);
//			String urlInfo = mCursor.getString(mCursor.getColumnIndex(MusicServerInfo.UUID_INFO));
//			if(urlInfo != null && urlInfo.length() > 1) {
//				continue;
//			}
//			String pid = String.valueOf(mCursor.getString(mCursor.getColumnIndex(MusicServerInfo.PRODUCT_ID)));
//			retrieveMusicUuidFromBaasio(pid);
//			Log.i(TAG, "pid = " + pid);
////			int tid = mCursor.getInt(mCursor.getColumnIndex(MusicServerInfo.PRODUCT_ID));
////			updateUrlInfoToDB(tid, urlInfo);
//			try {
//				Thread.sleep(5000);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		}
//		mCursor.requery();
//		setProgressBarVisibility(false);
	}

//	protected void retrieveMusicUuidFromBaasio(String pid) {
//		Log.i(TAG, "retrieveMusicUuidFromBaasio(pid = " + pid + ")");
//		BaasioQuery query = new BaasioQuery();
//		query.setType("files");
//		query.setProjectionIn("uuid");
//		query.setLimit(1);                            // 한번에 받을 갯수 설정
//		query.setOrderBy(BaasioBaseEntity.PROPERTY_MODIFIED, ORDER_BY.DESCENDING);
//		query.setWheres("pid='" + pid + "'");
//		query.queryInBackground(mQueryCallback);
//	}
	
//	private BaasioQueryCallback mQueryCallback = new BaasioQueryCallback() {
//		@Override
//        public void onResponse(List<BaasioBaseEntity> entities, List<Object> list, BaasioQuery query, long timestamp) {
//            Log.i(TAG, "onResponse (size = " + entities.size() + ")");
//       		List<BaasioFile> files = BaasioBaseEntity.toType(entities, BaasioFile.class);
//       		if(files.size() > 0) {
//       			BaasioFile uFile = files.get(0);
//                Log.i(TAG, "from baasio, uuid = " + uFile.getUuid());
////          		mEntityList.addAll(files);
//                updateUrlInfoToDB(MusicUtils.getStringFromEntity(uFile, "pid"), uFile.getUuid(), uFile.getContentLength());
//       		}
//       		else {
//       			Log.d(TAG, "error from baasio : cannot find file");
//       		}
//    	}
// 
//    	@Override
//    	public void onException(BaasioException e) {
//    		Log.i(TAG, "onException (error = " + e.toString());
//    	}
//	};

	protected void updateUrlInfoToDB(String pid, UUID uuid, Long length) {
		Log.i(TAG, "uuid = " + uuid.toString());
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
			musicRowValues.put(MusicServerInfo.UUID_INFO, uuid.toString());
			musicRowValues.put(MusicServerInfo.FILE_SIZE, length.intValue());
			dbHandler.update(MusicServerInfo.TABLE_NAME, musicRowValues, w, null);
			cursor.close();
		}
	}
}