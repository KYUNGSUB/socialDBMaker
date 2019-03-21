/****************************************************************************
 *  Girls Group DB�� ����ϴ� ��� Activity�� ��� ���� ���� Ŭ���� ����
 ****************************************************************************/
package com.talanton.music.player.utilities;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import java.io.File;

public class MusicListBaseActivity extends Activity {
	protected MusicListDBHelper musicDB = null;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//		File directory = new File(Constant.DB_PATH);
//		if(!directory.exists()) {
//			directory.mkdir();
//		}
//		directory.setWritable(true);
        musicDB = new MusicListDBHelper(this);
//        directory = new File(Constant.DB_FULL_NAME);
//        directory.setWritable(true);
    }
	@Override
	protected void onDestroy() {
	   super.onDestroy();
	   if( musicDB != null){
		   musicDB.close();
	   }
	}
}