package com.talanton.music.player.utilities;

import android.os.Environment;

import java.io.File;

public class Constant {
	public static final String TAG = "MusicPlayer";
	public static final String SP_NAME = "musicplayer.txt";
	public static final String NUMBER_OF_DOWNLOAD = "remain_music";
	public static final int RATE_CANCEL_LIKE = 10;
	public static final String APP_DIRECTORY = "MusicPlayer";
	public static final String DB_NAME = "MusicListDB.db";
	public static final String DB_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator +
			APP_DIRECTORY;
	public static final String DB_FULL_NAME = DB_PATH + File.separator + DB_NAME;
}
