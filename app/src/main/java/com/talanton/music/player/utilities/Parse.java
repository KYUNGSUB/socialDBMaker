package com.talanton.music.player.utilities;

import java.io.IOException;
import java.util.ArrayList;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.talanton.music.player.utilities.MusicListDB.*;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
//import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;

public class Parse {
	private static final String TAG = "MusicPlayer";
	MusicListDBHelper mMusicDB;
	private int nOfMusic;

	public Parse(MusicListDBHelper musicDB) {
		this.mMusicDB = musicDB;
		this.nOfMusic = 0;
	}
	
	public void parsePhysicalMusicLists(XmlPullParser xpp) throws XmlPullParserException, IOException {
		while(true) {
			int event = xpp.nextTag();
			if(event == XmlPullParser.START_TAG) {
				String tag = xpp.getName();
//				Log.d(TAG, "parseMusicCategory : " + tag);
				if("word".equals(tag)) {
					// Do Nothing
				} else if("music".equals(tag)) {
					parseOnePhysicalMusic(xpp);
					this.nOfMusic++;
					Log.d(TAG, "number of physical id = " + nOfMusic);
				} else {
					int nextType = xpp.next();
					if(nextType == XmlPullParser.TEXT) {	// 4
						if(xpp.nextTag() == XmlPullParser.START_TAG) {
							xpp.nextText();
						}
					}
				}
			} else if(event == XmlPullParser.END_TAG) {
				if(xpp.getName().equals("word")) {
					break;		// End parse
				}
			}
		}
		Log.d(TAG, "number of physical id = " + nOfMusic);
	}
	
	public void parseLogicalMusicLists(XmlPullParser xpp) throws XmlPullParserException, IOException {
		MusicCategory mCategory;
		while(true) {
			int event = xpp.nextTag();
			if(event == XmlPullParser.START_TAG) {
				String tag = xpp.getName();
				Log.d(TAG, "parseMusicCategory : " + tag);
				if("word".equals(tag)) {
					// Do Nothing
				} else if("main-category".equals(tag)) {
					mCategory = parseMainCategoryList(xpp);
					storeMainCategoryInformation(mCategory);
				} else {
					int nextType = xpp.next();
					if(nextType == XmlPullParser.TEXT) {	// 4
						if(xpp.nextTag() == XmlPullParser.START_TAG) {
							xpp.nextText();
						}
					}
				}
			} else if(event == XmlPullParser.END_TAG) {
				if(xpp.getName().equals("word")) {
					break;		// End parse
				}
			}
		}
	}

	public MusicCategory parseMainCategoryList(XmlPullParser xpp) throws XmlPullParserException, IOException {
		MusicCategory result = new MusicCategory();
		int count = 0;
		while(true) {
			int event = xpp.nextTag();
			if(event == XmlPullParser.START_TAG) {
				String tag = xpp.getName();
				Log.d(TAG, "parseMainCategory : " + tag);
				if("code".equals(tag)) {
					result.setCode(Integer.valueOf(xpp.nextText()));
				} else if("ko-subject".equals(tag)) {
					result.setKname(xpp.nextText());
				} else if("en-subject".equals(tag)) {
					result.setEname(xpp.nextText());
				} else if("sub-category".equals(tag)) {
					MusicCategory subC = parseSubCategoryLists(xpp);
					storeSubCategoryInformation(result.getCode(), subC);
					count += subC.getCount();
				} else {
					int nextType = xpp.next();
					if(nextType == XmlPullParser.TEXT) {	// 4
						if(xpp.nextTag() == XmlPullParser.START_TAG) {
							xpp.nextText();
						}
					}
				}
			} else if(event == XmlPullParser.END_TAG) {
				if(xpp.getName().equals("main-category")) {
					break;		// End parse
				}
			}
		}
		result.setCount(count);
		return result;
	}
	
	private void storeMainCategoryInformation(MusicCategory mainC) {
		SQLiteDatabase dbHandler = null;
		try {
			dbHandler = mMusicDB.getWritableDatabase();
//			dbHandler.beginTransaction();
			SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
			
			queryBuilder.setTables(MainCategoryInfo.TABLE_NAME);
			queryBuilder.appendWhere(MainCategoryInfo.C_ID + "=" + mainC.getCode());

			//����� ������ �ش� ��� ������ ���� �´�
			Cursor mCursor = queryBuilder.query(dbHandler,
		                           null,null,null,null,null,null);
			
			// copy Music files to SD
			if(mCursor != null && mCursor.getCount() == 0) {
				ContentValues  musicRowValues = new ContentValues();
				musicRowValues.put(MainCategoryInfo.C_ID, mainC.getCode());
				musicRowValues.put(MainCategoryInfo.KO_NAME, mainC.getKname());
				musicRowValues.put(MainCategoryInfo.EN_NAME, mainC.getEname());
				musicRowValues.put(MainCategoryInfo.COUNT, mainC.getCount());
//				dbHandler.insert(MusicServerInfo.TABLE_NAME, "NODATA", musicRowValues);
				long id = dbHandler.insert(MainCategoryInfo.TABLE_NAME, "NODATA", musicRowValues);
//				Log.i(TAG, "insert id = " + id + ", sid = " + mainC.getCode());
			}
			mCursor.close();
//			dbHandler.setTransactionSuccessful();
		}
		catch (SQLiteException sql) {
			Log.e(TAG, "insert Music Information error : " + sql.toString());
		}
		finally {
//			dbHandler.endTransaction();
	    	if(dbHandler != null){
	    		dbHandler.close();
	    	}
		}
	}
	
	private void storeSubCategoryInformation(int mainCode, MusicCategory subC) {
		SQLiteDatabase dbHandler = null;
		try {
			dbHandler = mMusicDB.getWritableDatabase();
//			dbHandler.beginTransaction();
			SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
			
			queryBuilder.setTables(SubCategoryInfo.TABLE_NAME);
			queryBuilder.appendWhere(SubCategoryInfo.M_ID + "=" + mainCode +
					" AND " + SubCategoryInfo.S_ID + "=" + subC.getCode());

			//����� ������ �ش� ��� ������ ���� �´�
			Cursor mCursor = queryBuilder.query(dbHandler,
		                           null,null,null,null,null,null);
			   
			// copy Music files to SD
			if(mCursor != null && mCursor.getCount() == 0) {
				ContentValues  musicRowValues = new ContentValues();
				musicRowValues.put(SubCategoryInfo.M_ID, mainCode);
				musicRowValues.put(SubCategoryInfo.S_ID, subC.getCode());
				musicRowValues.put(SubCategoryInfo.KO_NAME, subC.getKname());
				musicRowValues.put(SubCategoryInfo.EN_NAME, subC.getEname());
				musicRowValues.put(SubCategoryInfo.COUNT, subC.getCount());
//				dbHandler.insert(MusicServerInfo.TABLE_NAME, "NODATA", musicRowValues);
				long id = dbHandler.insert(SubCategoryInfo.TABLE_NAME, "NODATA", musicRowValues);
//				Log.i(TAG, "insert id = " + id + ", mid = " + mainCode + ", sid = " + subC.getCode());
			}
			mCursor.close();
//			dbHandler.setTransactionSuccessful();
		}
		catch (SQLiteException sql) {
			Log.e(TAG, "insert Music Information error : " + sql.toString());
		}
		finally {
//			dbHandler.endTransaction();
	    	if(dbHandler != null){
	    		dbHandler.close();
	    	}
		}
	}

	public MusicCategory parseSubCategoryLists(XmlPullParser xpp) throws XmlPullParserException, IOException {
//		Log.i(TAG, "start of parsePosts()");
		
		MusicCategory result = new MusicCategory();
		int count = 0;
		while(true) {
			int event = xpp.nextTag();
			if(event == XmlPullParser.START_TAG) {
				String tag = xpp.getName();
				Log.d(TAG, "parsePostList : " + tag);
				if("code".equals(tag)) {
					result.setCode(Integer.valueOf(xpp.nextText()));
				} else if("ko-subject".equals(tag)) {
					result.setKname(xpp.nextText());
				} else if("en-subject".equals(tag)) {
					result.setEname(xpp.nextText());
				} else if("information".equals(tag)) {
					parseOneLogicalMusic(xpp);
					count++;
				} else {
					int nextType = xpp.next();
					if(nextType == XmlPullParser.TEXT) {	// 4
						if(xpp.nextTag() == XmlPullParser.START_TAG) {
							xpp.nextText();
						}
					}
				}
			} else if(event == XmlPullParser.END_TAG) {
				if(xpp.getName().equals("sub-category")) {
					break;		// End parse
				}
			}
		}
		result.setCount(count);
		return result;
	}
	
	public void parseOnePhysicalMusic(XmlPullParser xpp) throws XmlPullParserException, IOException {
//		Log.i(TAG, "start of parsePosts()");
		MusicSong ms = new MusicSong();
		
		while(true) {
			int event = xpp.nextTag();
			if(event == XmlPullParser.START_TAG) {
				String tag = xpp.getName();
//				Log.d(TAG, "parsePostList : " + tag);
				if("pid".equals(tag)) {
					ms.setPid(xpp.nextText());
				} else if("k-title".equals(tag)) {
					ms.setTitle(xpp.nextText());
				} else if("k-author".equals(tag)) {
					ms.setAuthor(xpp.nextText());
				} else if("genre".equals(tag)) {
					ms.setGenre(Integer.valueOf(xpp.nextText()));
				} else if("e-title".equals(tag)) {
					ms.setDownload(xpp.nextText());
				} else if("e-author".equals(tag)) {
					ms.setDownload_date(xpp.nextText());
				} else if("duration".equals(tag)) {
					ms.setTimeinfo(xpp.nextText());
				} else if("filename".equals(tag)) {
					ms.setFileinfo(xpp.nextText());
					storeMusicListInformationToDBTable(ms);
				} else {
					int nextType = xpp.next();
					if(nextType == XmlPullParser.TEXT) {	// 4
						if(xpp.nextTag() == XmlPullParser.START_TAG) {
							xpp.nextText();
						}
					}
				}
			} else if(event == XmlPullParser.END_TAG) {
				if(xpp.getName().equals("music")) {
					break;		// End parse
				}
			}
		}
	}
	
	public void parseOneLogicalMusic(XmlPullParser xpp) throws XmlPullParserException, IOException {
//		Log.i(TAG, "start of parsePosts()");
		MusicSong ms = new MusicSong();
		
		while(true) {
			int event = xpp.nextTag();
			if(event == XmlPullParser.START_TAG) {
				String tag = xpp.getName();
//				Log.d(TAG, "parsePostList : " + tag);
				if("no".equals(tag)) {
					ms.setPid(xpp.nextText());
				} else if("pid".equals(tag)) {
					ms.setFileinfo(xpp.nextText());
					storeLogicalMusicListInformationToDBTable(ms);
				} else {
					int nextType = xpp.next();
					if(nextType == XmlPullParser.TEXT) {	// 4
						if(xpp.nextTag() == XmlPullParser.START_TAG) {
							xpp.nextText();
						}
					}
				}
			} else if(event == XmlPullParser.END_TAG) {
				if(xpp.getName().equals("information")) {
					break;		// End parse
				}
			}
		}
	}
	
	private void storeLogicalMusicListInformationToDBTable(MusicSong ms) {
		SQLiteDatabase dbHandler = null;
		try {
			dbHandler = mMusicDB.getWritableDatabase();
//			dbHandler.beginTransaction();
			SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
			
			queryBuilder.setTables(MusicContentsInfo.TABLE_NAME);
			queryBuilder.appendWhere(MusicContentsInfo.L_ID + "=" + ms.getPid());

			//����� ������ �ش� ��� ������ ���� �´�
			Cursor mCursor = queryBuilder.query(dbHandler,
		                           null,null,null,null,null,null);
			   
			// copy Music files to SD
			if(mCursor != null && mCursor.getCount() == 0) {
				ContentValues  musicRowValues = new ContentValues();
				musicRowValues.put(MusicContentsInfo.L_ID, ms.getPid());
				musicRowValues.put(MusicContentsInfo.P_ID, ms.getFileinfo());
//				dbHandler.insert(MusicServerInfo.TABLE_NAME, "NODATA", musicRowValues);
				long id = dbHandler.insert(MusicContentsInfo.TABLE_NAME, "NODATA", musicRowValues);
				Log.i(TAG, "insert id = " + id + ", pid = " + ms.getPid() + ", filename = " + ms.getFileinfo());
			}
			mCursor.close();
//			dbHandler.setTransactionSuccessful();
		}
		catch (SQLiteException sql) {
			Log.e(TAG, "insert Music Information error : " + sql.toString());
		}
		finally {
//			dbHandler.endTransaction();
	    	if(dbHandler != null){
	    		dbHandler.close();
	    	}
		}
	}
	
	private void storeMusicListInformationToDBTable(MusicSong ms) {
		SQLiteDatabase dbHandler = null;
		try {
			dbHandler = mMusicDB.getWritableDatabase();
//			dbHandler.beginTransaction();
			SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
			
			queryBuilder.setTables(MusicServerInfo.TABLE_NAME);
			queryBuilder.appendWhere(MusicServerInfo.FILE_INFO + "='" + ms.getFileinfo() + "'");

			//����� ������ �ش� ��� ������ ���� �´�
			Cursor mCursor = queryBuilder.query(dbHandler,
		                           null,null,null,null,null,null);
			   
			// copy Music files to SD
			if(mCursor != null && mCursor.getCount() == 0) {
				ContentValues  musicRowValues = new ContentValues();
				musicRowValues.put(MusicServerInfo.PRODUCT_ID, ms.getPid());
				musicRowValues.put(MusicServerInfo.TITLE_KO, ms.getTitle());
				musicRowValues.put(MusicServerInfo.AUTHOR_KO, ms.getAuthor());
				musicRowValues.put(MusicServerInfo.TITLE_EN, ms.getDownload());
				musicRowValues.put(MusicServerInfo.AUTHOR_EN, ms.getDownload_date());
				musicRowValues.put(MusicServerInfo.MUSIC_GENRE, ms.getGenre());
				musicRowValues.put(MusicServerInfo.PLAY_TIME, ms.getTimeinfo());
				musicRowValues.put(MusicServerInfo.FILE_SIZE, 0);
				musicRowValues.put(MusicServerInfo.FILE_INFO, ms.getFileinfo());
				musicRowValues.put(MusicServerInfo.UUID_INFO, "");					// Initialize
//				musicRowValues.put(MusicServerInfo.MY_RATE, Constant.RATE_CANCEL_LIKE);
				musicRowValues.put(MusicServerInfo.DOWNLOAD, 0);
				musicRowValues.put(MusicServerInfo.BOOKMARK, 0);
//				dbHandler.insert(MusicServerInfo.TABLE_NAME, "NODATA", musicRowValues);
				long id = dbHandler.insert(MusicServerInfo.TABLE_NAME, "NODATA", musicRowValues);
//				Log.i(TAG, "insert id = " + id + ", pid = " + ms.getFileinfo() + "n of music " + nOfMusic);
			}
			mCursor.close();
//			dbHandler.setTransactionSuccessful();
		}
		catch (SQLiteException sql) {
			Log.e(TAG, "insert Music Information error : " + sql.toString());
		}
		finally {
//			dbHandler.endTransaction();
	    	if(dbHandler != null){
	    		dbHandler.close();
	    	}
		}
	}

//	public ArrayList<MainCategory> parseMusicCategory (XmlPullParser xpp) throws XmlPullParserException, IOException {
//		ArrayList<MainCategory> categoryList = new ArrayList<MainCategory>();
//		
//		while(true) {
//			int event = xpp.nextTag();
//			if(event == XmlPullParser.START_TAG) {
//				String tag = xpp.getName();
////				Log.d(TAG, "parseMusicCategory : " + tag);
//				if("word".equals(tag)) {
//					// Do Nothing
//				} else if("main-category".equals(tag)) {
//					categoryList.add(parseMainCategory(xpp));
//				} else {
//					int nextType = xpp.next();
//					if(nextType == XmlPullParser.TEXT) {	// 4
//						if(xpp.nextTag() == XmlPullParser.START_TAG) {
//							xpp.nextText();
//						}
//					}
//				}
//			} else if(event == XmlPullParser.END_TAG) {
//				if(xpp.getName().equals("word")) {
//					break;		// End parse
//				}
//			}
//		}
//		return categoryList;
//	}
	
//	public MainCategory parseMainCategory (XmlPullParser xpp)
//		throws XmlPullParserException, IOException {
//	
//		MainCategory mMainCategory = new MainCategory();
//		while(true) {
//			int event = xpp.nextTag();
//			if(event == XmlPullParser.START_TAG) {
//				String tag = xpp.getName();
////				Log.d(TAG, "parseMainCategory : " + tag);
//				if("code".equals(tag)) {
//					mMainCategory.setCode(Integer.valueOf(xpp.nextText()));
//				} else if("subject".equals(tag)) {
//					mMainCategory.setName(xpp.nextText());
//				} else if("sub-category".equals(tag)) {
//					mMainCategory.getSubCategory().add(parseSubCategory(xpp));
//				} else {
//					int nextType = xpp.next();
//					if(nextType == XmlPullParser.TEXT) {	// 4
//						if(xpp.nextTag() == XmlPullParser.START_TAG) {
//							xpp.nextText();
//						}
//					}
//				}
//			} else if(event == XmlPullParser.END_TAG) {
//				if(xpp.getName().equals("main-category")) {
//					break;		// End parse
//				}
//			}
//		}
//		return mMainCategory;
//	}
//	
//	public MusicCategory parseSubCategory (XmlPullParser xpp)
//		throws XmlPullParserException, IOException {
//	
//		MusicCategory mMusicCategory = new MusicCategory();
//		while(true) {
//			int event = xpp.nextTag();
//			if(event == XmlPullParser.START_TAG) {
//				String tag = xpp.getName();
////				Log.d(TAG, "parseSubCategory : " + tag);
//				if("code".equals(tag)) {
//					mMusicCategory.setCode(Integer.valueOf(xpp.nextText()));
//				} else if("subject".equals(tag)) {
//					mMusicCategory.setName(xpp.nextText());
//				} else {
//					int nextType = xpp.next();
//					if(nextType == XmlPullParser.TEXT) {	// 4
//						if(xpp.nextTag() == XmlPullParser.START_TAG) {
//							xpp.nextText();
//						}
//					}
//				}
//			} else if(event == XmlPullParser.END_TAG) {
//				if(xpp.getName().equals("sub-category")) {
//					break;		// End parse
//				}
//			}
//		}
//		return mMusicCategory;
//	}
	
	public ArrayList<Post> parsePosts (XmlPullParser xpp)
		throws XmlPullParserException, IOException {
//		Log.i(TAG, "start of parsePosts()");
		ArrayList<Post> postList = new ArrayList<Post>();
	
		while(true) {
			int event = xpp.nextTag();
			if(event == XmlPullParser.START_TAG) {
				String tag = xpp.getName();
//				Log.d(TAG, "parsePostList : " + tag);
				if("posts".equals(tag)) {
					// Do Nothing
				} else if("post".equals(tag)) {
//					postList.add(0,parsePost(xpp));
					postList.add(parsePost(xpp));
				} else {
					int nextType = xpp.next();
					if(nextType == XmlPullParser.TEXT) {	// 4
						if(xpp.nextTag() == XmlPullParser.START_TAG) {
							xpp.nextText();
						}
					}
				}
			} else if(event == XmlPullParser.END_TAG) {
				if(xpp.getName().equals("posts")) {
					break;		// End parse
				}
			}
		}
//		Log.i(TAG, "end of parsePosts(count = " + postList.size() + ")");
		return postList;
	}
	
	public Post parsePost (XmlPullParser xpp)
		throws XmlPullParserException, IOException {
	
		Post mPost = new Post();
		while(true) {
			int event = xpp.nextTag();
			if(event == XmlPullParser.START_TAG) {
				String tag = xpp.getName();
//				Log.d(TAG, "parsePost : " + tag);
				if("post_id".equals(tag)) {
					mPost.setPost_id(xpp.nextText());
				} else if("tagText".equals(tag)) {
					mPost.setTag(xpp.nextText());
				} else if("body".equals(tag)) {
					String mBody = xpp.nextText();
//					Log.i(TAG, "body(before) = " + mBody);
					mBody = mBody.replace('@', ' ');
//					Log.i(TAG, "body(after) = " + mBody);
					mPost.setBody(mBody);
				} else if("source".equals(tag)) {
					mPost.setSource(xpp.nextText());
				} else if("photoUrl".equals(tag)) {
					mPost.setSource(xpp.nextText());
				} else if("tags".equals(tag)) {
					xpp.nextTag();
				} else if("tag".equals(tag)) {
					xpp.next();
				} else if("author".equals(tag)) {
					
				} else if("location".equals(tag)) {
					
				} else if("media".equals(tag)) {
					int nextType = xpp.next();
//					Log.d(TAG, "parsePost nextType = " + nextType);
					if(nextType == XmlPullParser.TEXT) {	// 4
						xpp.next();
					}
				} else {
					int nextType = xpp.next();
//					Log.d(TAG, "parsePost nextType = " + nextType);
					if(nextType == XmlPullParser.TEXT) {	// 4
						int sub_event = xpp.nextTag();
//						Log.d(TAG, "sub_event = " + sub_event + ", name = " + xpp.getName());
					}
				}
			} else if(event == XmlPullParser.END_TAG) {
				if(xpp.getName().equals("post")) {
					break;		// End parse
				}
			}
		}
		return mPost;
	}

	public int getnOfMusic() {
		return this.nOfMusic;
	}

	public void setnOfMusic(int count) {
		this.nOfMusic = count;
	}

	public void parseFileUuidInfo(XmlPullParser xpp)
			throws XmlPullParserException, IOException {
		while(true) {
			int event = xpp.nextTag();
			if(event == XmlPullParser.START_TAG) {
				String tag = xpp.getName();
//				Log.d(TAG, "parseMusicCategory : " + tag);
				if("word".equals(tag)) {
					// Do Nothing
				} else if("file".equals(tag)) {
					parseOneFileInformation(xpp);
				} else {
					int nextType = xpp.next();
					if(nextType == XmlPullParser.TEXT) {	// 4
						if(xpp.nextTag() == XmlPullParser.START_TAG) {
							xpp.nextText();
						}
					}
				}
			} else if(event == XmlPullParser.END_TAG) {
				if(xpp.getName().equals("word")) {
					break;		// End parse
				}
			}
		}
	}

	private void parseOneFileInformation(XmlPullParser xpp)
			throws XmlPullParserException, IOException {
//		Log.i(TAG, "start of parsePosts()");
		MusicSong ms = new MusicSong();
		
		while(true) {
			int event = xpp.nextTag();
			if(event == XmlPullParser.START_TAG) {
				String tag = xpp.getName();
//				Log.d(TAG, "parsePostList : " + tag);
				if("pid".equals(tag)) {
					ms.setPid(xpp.nextText());
					Log.d(TAG, "number of physical id = " + ms.getPid());
				} else if("filesize".equals(tag)) {
					ms.setFile_size(Integer.valueOf(xpp.nextText()));
				} else if("uuid".equals(tag)) {
					ms.setUuid(xpp.nextText());
					storeFileUuidInformationToDBTable(ms);
				} else {
					int nextType = xpp.next();
					if(nextType == XmlPullParser.TEXT) {	// 4
						if(xpp.nextTag() == XmlPullParser.START_TAG) {
							xpp.nextText();
						}
					}
				}
			} else if(event == XmlPullParser.END_TAG) {
				if(xpp.getName().equals("file")) {
					break;		// End parse
				}
			}
		}
	}

	private void storeFileUuidInformationToDBTable(MusicSong ms) {
		SQLiteDatabase dbHandler = null;
		try {
			dbHandler = mMusicDB.getWritableDatabase();
			SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
			
			queryBuilder.setTables(MusicServerInfo.TABLE_NAME);
			String w = MusicServerInfo.PRODUCT_ID + "='" + ms.getPid() + "'";
			queryBuilder.appendWhere(w);
			Cursor mCursor = queryBuilder.query(dbHandler, null, null, null, null, null, null);
			// copy Music files to SD
			if(mCursor.getCount() > 0) {
				ContentValues  musicRowValues = new ContentValues();
				musicRowValues.put(MusicServerInfo.FILE_SIZE, ms.getFile_size());
				musicRowValues.put(MusicServerInfo.UUID_INFO, ms.getUuid());
				long id = dbHandler.update(MusicServerInfo.TABLE_NAME, musicRowValues, w, null);
			}
			mCursor.close();
		}
		catch (SQLiteException sql) {
			Log.e(TAG, "insert Music Information error : " + sql.toString());
		}
		finally {
	    	if(dbHandler != null){
	    		dbHandler.close();
	    	}
		}
	}

	public void parseCategoryUuidInfo(XmlPullParser xpp)
			throws XmlPullParserException, IOException {
		while(true) {
			int event = xpp.nextTag();
			if(event == XmlPullParser.START_TAG) {
				String tag = xpp.getName();
//				Log.d(TAG, "parseMusicCategory : " + tag);
				if("word".equals(tag)) {
					// Do Nothing
				} else if("category".equals(tag)) {
					parseOneCategoryInformation(xpp);
				} else {
					int nextType = xpp.next();
					if(nextType == XmlPullParser.TEXT) {	// 4
						if(xpp.nextTag() == XmlPullParser.START_TAG) {
							xpp.nextText();
						}
					}
				}
			} else if(event == XmlPullParser.END_TAG) {
				if(xpp.getName().equals("word")) {
					break;		// End parse
				}
			}
		}
	}

	private void parseOneCategoryInformation(XmlPullParser xpp)
			throws XmlPullParserException, IOException {
//		Log.i(TAG, "start of parsePosts()");
		int mid = 0;
		int sid = 0;
		String uuid;
		
		while(true) {
			int event = xpp.nextTag();
			if(event == XmlPullParser.START_TAG) {
				String tag = xpp.getName();
//				Log.d(TAG, "parsePostList : " + tag);
				if("mid".equals(tag)) {
					mid = Integer.valueOf(xpp.nextText());
				} else if("sid".equals(tag)) {
					sid = Integer.valueOf(xpp.nextText());
				} else if("uuid".equals(tag)) {
					uuid = xpp.nextText();
					Log.d(TAG, "mid = " + mid + ", sid = " + sid + ", uuid = " + uuid);
					storeCategoryUuidInformationToDBTable(mid, sid, uuid);
				} else {
					int nextType = xpp.next();
					if(nextType == XmlPullParser.TEXT) {	// 4
						if(xpp.nextTag() == XmlPullParser.START_TAG) {
							xpp.nextText();
						}
					}
				}
			} else if(event == XmlPullParser.END_TAG) {
				if(xpp.getName().equals("category")) {
					break;		// End parse
				}
			}
		}
	}

	private void storeCategoryUuidInformationToDBTable(int mid, int sid, String uuid) {
		SQLiteDatabase dbHandler = null;
		try {
			dbHandler = mMusicDB.getWritableDatabase();
			SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
			
			queryBuilder.setTables(SubCategoryInfo.TABLE_NAME);
			String w = SubCategoryInfo.M_ID + "=" + mid + " AND " + SubCategoryInfo.S_ID + "=" + sid;
			queryBuilder.appendWhere(w);
			Cursor mCursor = queryBuilder.query(dbHandler, null, null, null, null, null, null);
			// copy Music files to SD
			if(mCursor.getCount() > 0) {
				ContentValues  musicRowValues = new ContentValues();
//				musicRowValues.put(SubCategoryInfo.UUID_INFO, uuid);
				long id = dbHandler.update(SubCategoryInfo.TABLE_NAME, musicRowValues, w, null);
			}
			mCursor.close();
		}
		catch (SQLiteException sql) {
			Log.e(TAG, "insert Music Information error : " + sql.toString());
		}
		finally {
	    	if(dbHandler != null){
	    		dbHandler.close();
	    	}
		}
	}
}