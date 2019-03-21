package com.talanton.music.player;

import com.talanton.music.player.*;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class HelpDisplay extends Activity {
	private static final String TAG = "MusicPlayer";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG, "HelpDisplay:onCreate()");
		
		setContentView(R.layout.activity_helpdisplay);
	}
}