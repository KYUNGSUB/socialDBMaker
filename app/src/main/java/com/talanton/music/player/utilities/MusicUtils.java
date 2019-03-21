package com.talanton.music.player.utilities;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Date;
import java.util.HashMap;
import org.codehaus.jackson.JsonNode;
import com.talanton.music.player.*;
import android.app.Activity;
import android.app.backup.BackupManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

public class MusicUtils {
	private static final String TAG = "MusicPlayer";

	public static void attributeChanged(BackupManager backupManager, File dataFile) {
		long dateFlag = (new Date()).getTime();
        try {
            synchronized (MusicPlayer.sDataLock) {
                RandomAccessFile file = new RandomAccessFile(dataFile, "rw");
                file.setLength(0L);
                file.writeLong(dateFlag);
            }
        } catch (IOException e) {
            Log.e(TAG, "Unable to record new UI state");
        }

        backupManager.dataChanged();
	}
	
	public static void saveConfig(Context context , String name , Object value){
		
		if (value == null) {
			return;
		}
		
		SharedPreferences sp = context.getSharedPreferences(Constant.SP_NAME, Context.MODE_PRIVATE);
		Editor editor = sp.edit();
		
		if ( value instanceof String ) {
			editor.putString(name, (String)value);
		} else if ( value instanceof Integer ) {
			editor.putInt(name, (Integer)value);
		} else if ( value instanceof Boolean ) {
			editor.putBoolean(name, (Boolean)value);
		} else if ( value instanceof Long ) {
			editor.putLong(name, (Long)value);
		}
		
		editor.commit();
	}
	
	public static String getConfigString(Context context , String name ){
		SharedPreferences sp = context.getSharedPreferences(Constant.SP_NAME, Context.MODE_PRIVATE);
		return sp.getString(name, "");
	}
	
	public static int getConfigInteger(Context context , String name, int defaultVal ){
		SharedPreferences sp = context.getSharedPreferences(Constant.SP_NAME, Context.MODE_PRIVATE);
		return sp.getInt(name, defaultVal);
	}
	
	public static boolean getConfigBoolean(Context context , String name, boolean defaultVal ){
		SharedPreferences sp = context.getSharedPreferences(Constant.SP_NAME, Context.MODE_PRIVATE);
		return sp.getBoolean(name, defaultVal);
	}
	
	public static long getConfigLong(Context context , String name, long defaultVal ){
		SharedPreferences sp = context.getSharedPreferences(Constant.SP_NAME, Context.MODE_PRIVATE);
		return sp.getLong(name, defaultVal);
	}
//	
//	public static String getStringFromEntity(BaasioFile baasioFile, String key) {
//        JsonNode node = baasioFile.getProperties().get(key);
//        if (node != null) {
//            // String result = node.toString();
//            // if(result != null && result.length() > 0) {
//            // result = result.replace("<BR>", "\n");
//            // result = result.replace("<br>", "\n");
//            // return result.replace("\"", "").trim();
//            // }
//            String value = node.getTextValue();
//            if (value != null && value.length() > 0) {
//                return value.replace("<br>", "\n").replace("<BR>", "\n").trim();
//            } else {
//                return null;
//            }
//        }
//
//        return null;
//    }
}