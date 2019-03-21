/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.talanton.music.player;

import com.talanton.music.player.*;
import com.talanton.music.player.MinutePicker.*;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.content.DialogInterface.OnClickListener;

/**
 * A dialog that prompts the user for the time of day using a {@link TimePicker}.
 *
 * <p>See the <a href="{@docRoot}resources/tutorials/views/hello-timepicker.html">Time Picker
 * tutorial</a>.</p>
 */
public class MinutePickerDialog extends AlertDialog implements OnClickListener, 
        OnTimeChangedListener {
	/**
     * The callback interface used to indicate the user is done filling in
     * the time (they clicked on the 'Set' button).
     */
    public interface OnTimeSetListener {

        /**
         * @param mMinutePicker The view associated with this listener.
         * @param hourOfDay The hour that was set.
         * @param minute The minute that was set.
         */
        void onTimeSet(MinutePicker mMinutePicker, int minute, int second);
    }

    private static final String MINUTE = "minute";
    private static final String SECOND = "second";

    private final MinutePicker mMinutePicker;
    private final OnTimeSetListener mCallback;

    int mInitialMinute;
    int mInitialSecond;

    /**
     * @param context Parent.
     * @param callBack How parent is notified.
     * @param hourOfDay The initial hour.
     * @param minute The initial minute.
     * @param is24HourView Whether this is a 24 hour view, or AM/PM.
     */
    public MinutePickerDialog(Context context,
            OnTimeSetListener callBack,
            int minute, int second) {
        this(context, android.R.style.Theme_Dialog,
                callBack, minute, second);
    }

    /**
     * @param context Parent.
     * @param theme the theme to apply to this dialog
     * @param callBack How parent is notified.
     * @param hourOfDay The initial hour.
     * @param minute The initial minute.
     * @param is24HourView Whether this is a 24 hour view, or AM/PM.
     */
    public MinutePickerDialog(Context context,
            int theme,
            OnTimeSetListener callBack,
            int minute, int second) {
        super(context, theme);
        mCallback = callBack;
        mInitialMinute = minute;
        mInitialSecond = second;

        updateTitle(mInitialMinute, mInitialSecond);

        setButton(BUTTON_POSITIVE, context.getText(R.string.date_time_set), this);
        setButton(BUTTON_NEGATIVE, context.getText(android.R.string.cancel),
                (OnClickListener) null);
        setIcon(R.drawable.ic_dialog_time);

        LayoutInflater inflater =
                (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.time_picker_dialog, null);
        setView(view);
        mMinutePicker = (MinutePicker) view.findViewById(R.id.minutePicker);

        // initialize state
        mMinutePicker.setCurrentSecond(mInitialSecond);
        mMinutePicker.setCurrentMinute(mInitialMinute);
        mMinutePicker.setOnTimeChangedListener(this);
    }

    public void onClick(DialogInterface dialog, int which) {
        if (mCallback != null) {
        	mMinutePicker.clearFocus();
            mCallback.onTimeSet(mMinutePicker, mMinutePicker.getCurrentMinute(),
            		mMinutePicker.getCurrentSecond());
        }
    }

    public void onTimeChanged(MinutePicker view, int minute, int second) {
        updateTitle(minute, second);
    }

    public void updateTime(int minutOfHour, int secondOfMinute) {
    	mMinutePicker.setCurrentMinute(minutOfHour);
    	mMinutePicker.setCurrentSecond(secondOfMinute);
    }

    private void updateTitle(int minute, int second) {
        setTitle(String.format("%02d:%02d", minute, second));
    }

    @Override
    public Bundle onSaveInstanceState() {
        Bundle state = super.onSaveInstanceState();
        state.putInt(MINUTE, mMinutePicker.getCurrentMinute());
        state.putInt(SECOND, mMinutePicker.getCurrentSecond());
        return state;
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        int minute = savedInstanceState.getInt(MINUTE);
        int second = savedInstanceState.getInt(SECOND);
        mMinutePicker.setCurrentMinute(minute);
        mMinutePicker.setCurrentSecond(second);
        mMinutePicker.setOnTimeChangedListener(this);
        updateTitle(minute, second);
    }
}